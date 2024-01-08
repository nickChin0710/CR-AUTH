/**
 * 授權邏輯查核-額度檢核處理
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-額度檢核處理                       *
 * 2021/03/30  V1.00.01  Kevin       小金額交易檢查邏輯調整                         *
 * 2021/11/19  V1.00.02  Kevin       公司戶在臨調額度改用固定金額，非臨調比例            *
 * 2022/01/05  V1.00.03  Kevin       臨調公司戶調整                               *
 * 2022/02/17  V1.00.04  Kevin       採購卡邏輯變更，視為一般商務卡                   *
 * 2022/03/08  V1.00.05  Kevin       臨調專款專用金額，可使用到基本額度                *
 * 2022/03/14  V1.00.06  Kevin       撈取臨調授權資料，不須判斷專款專用旗標             *
 * 2022/03/23  V1.00.07  Kevin       配合調整免照會VIP不須檢查風險特店                *
 * 2022/03/28  V1.00.08  Kevin       VD自助加油一般授權交易圈存1500                 *
 * 2022/03/28  V1.00.09  Kevin       調整預先授權邏輯在authLogicCheck()授權邏輯查核   *
 * 2022/04/07  V1.00.10  Kevin       分期交易可用餘額須包含溢繳款與payment未消         *
 * 2022/04/19  V1.00.11	 Kevin       風險分類月限額調整                            *
 * 2022/12/07  V1.00.28  Kevin       VD卡加油站交易(5541、5542) ，只要收單銀行送入是0元交易
 *                                   不論是一般或預先授權都一律以系統設定的自助加油金額1500元來圈存。
 * 2022/12/07  V1.00.29  Kevin       代行交易通知時，如果是拒絕交易，系統不需再做後續處理。    *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/06/01  V1.00.46  Kevin       P3批次授權比照一般授權的邏輯，不須特別排除            *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 * 2023/10/16  V1.00.55  Kevin       DEBIT CARD如代行交易拒絕時，防止啟動圈存作業        *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcAuthLogicCheck extends AuthLogic {

	public LogicProcAuthLogicCheck(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcAuthLogicCheck : started");

	}
	
	


	// 授權邏輯查核
	/**
	 * 檢核授權邏輯查核-額度檢核處理
	 * V1.00.28 VD卡加油站交易(5541、5542) ，只要收單銀行送入是0元交易，不論是一般或預先授權都一律以系統設定的自助加油金額1500元來圈存。
	 * V1.00.29 代行交易通知時，如果是拒絕交易，系統不需再做後續處理。
	 * V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(避免預先授權完成交易，因檢查錯誤時，跳過預先授權完成比對工作)
	 * V1.00.55 DEBIT CARD如代行交易拒絕時，防止啟動圈存作業
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean authLogicCheck() throws Exception {
		boolean blResult = true;
		//V1.00.09 調整預先授權邏輯在authLogicCheck()授權邏輯查核
		if ( gGate.preAuthComp ) { // 查核 預先授權完成
			LogicCheckPreAuth logicPreAuth = new LogicCheckPreAuth(gb, gGate, ta);
			blResult = logicPreAuth.checkPreAuthComp();		
		}
		else if ( gGate.preAuth ) { //預先授權
			LogicCheckPreAuth logicPreAuth = new LogicCheckPreAuth(gb, gGate, ta);
			blResult = logicPreAuth.checkPreAuth();		
		}
		else {
			if (gGate.isDebitCard) { 
				//VD自助加油一般授權交易圈存1500
				if ( gGate.selfGas || ("5541".equals(gGate.mccCode) && gGate.isoFiled4Value == 0)) {
					gGate.lockAmt = getPreAuthAmt4SelfGas();
					gGate.ntAmt = gGate.lockAmt;
					gGate.isoFiled4Value =gGate.lockAmt;
					computeBalanceAmt(gGate.isoFiled4Value,0);//kevin:VD自助加油預先授權交易圈存金額isoFiled4Value 去check VD日、月限額
				}
			}
		}
		
		if (!blResult) {
			return false;
		}
		if (!"00".equals(gGate.isoField[39])) {
			return false;
		}	
		if (gGate.isDebitCard) { //debit card不須檢查，統一在debitcheck()處理
			return true;
		}
		if (!preCheckCredit()) {
			return false;
		}

		return blResult;
	}
	
	
	/**
	 * 額度檢核的主要進入點
	 * V1.00.38 P3授權額度查核調整
	 * @return 如果額度檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */

	private boolean preCheckCredit() throws Exception{
		boolean blResult = true;

		try {
			if (gGate.rollbackP2) {
				boolean blPassLowTrade= false;
				//down, 小金額交易檢核
				if (gGate.lowTradeCheck) { //G_Gate.lowTradeCheck 一定要在 mCodeCheck() 值執行過後，才可能會被 on 起來
					if (lowTradeAmtCheck()) { //檢核小額交易參數檔之設定
						blPassLowTrade = true;
	
						if (!gGate.isRedeemTx) {
							if (gGate.preAuthComp) {
								updatePreAuthData("N");
							}
						}
					}
				}
				gb.showLogMessage("I","iso 39-6 : " +gGate.isoField[39]);
				if (!blPassLowTrade) { //如果沒有通過 小額交易 檢核
					if (!checkCredit()) {
						gb.showLogMessage("I","iso 39-7 : " +gGate.isoField[39]);
						return false;
					}
				}
			}
			else {
				if (!checkCredit()) {
					gb.showLogMessage("I","iso 39-7 : " +gGate.isoField[39]);
					return false;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}
		return blResult;
	}
	
	
	/*檢核小額交易參數檔之設定*/
	/**
	 * 檢核小額交易參數檔之設定
	 * V1.00.38 P3授權額度查核調整
	 * @return 檢核成功return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean lowTradeAmtCheck() throws Exception {
		boolean blResult=true;

		if (gGate.isInstallmentTx) //分期交易
			return false;//(須作 Credit Check)
		//V1.00.04 採購卡邏輯變更，視為一般商務卡 
//		if (gGate.isPurchaseCard) { //採購卡不做檢核,但須檢核基本額度,RETURN FALSE
//			return false;//(須作 Credit Check)
//		}

		if ("C".equals(gGate.mccRiskAmountRule)) {
			//預借現金類(須作 Credit Check)
			return false;
		}

		if (gGate.businessCard) { //商務卡須檢核
			if ((gGate.parentOtbAmt- gGate.isoFiled4Value)<0) {
				return false;/*可用餘額不足(須作 Credit Check)*/
			}

		}
		if ((gGate.otbAmt- gGate.isoFiled4Value)<0) {
			return false; /*可用餘額不足(須作 Credit Check)*/
		}

		String slCardNote= ta.getValue("CardBaseCardNote");
		if (!ta.selectPrdTypeIntr4LowTrade(slCardNote)) {
			//用空白 select =>{因為「卡片等級」空白視為通用原則 }
			if (!ta.selectPrdTypeIntr4LowTrade("*")) {
				return false; /*無日限額, 日限次之設定 (須作 Credit Check)*/

			}

		}

		int nlTmpTotCntDay = gGate.cardBaseTotCntDay;
		double dlTmpTotAmtDay = gGate.cardBaseTotAmtDay;

		if (isNewTradeDay()) {//Reset amt & cnt if change date
			nlTmpTotCntDay = 0;
			dlTmpTotAmtDay = 0;
		}
		/*當筆金額 <= 當次限額  且當日累計次數 + 當筆一次 <= 當日限次 OK*/
		if( (gGate.isoFiled4Value <= ta.getDouble("LmtTimesAmt4LowTrade")) && ((nlTmpTotCntDay+1)<= ta.getDouble("LmtDayCnt4LowTrade")) ){
			return true; //不作 Credit Check
		}	

		/*當日累計金額 + 當筆金額 > 當日限額  ====> 超過日限額 (須作 Credit Check)*/
		if ( (dlTmpTotAmtDay+gGate.isoFiled4Value) > ta.getDouble("LmtDayAmt4LowTrade") ) {
			return false;//(須作 Credit Check)
		}

		/*當日累計次數 + 當筆一次 > 當日限次  ====> 超過日限次 (須作 Credit Check)*/
		if( ((nlTmpTotCntDay+1) > ta.getDouble("LmtDayCnt4LowTrade")) ){
			return false;//(須作 Credit Check)
		}

		/*當筆金額 > 當次限額  ====> 超過次限額 (須作 Credit Check)*/
		if (gGate.isoFiled4Value>ta.getDouble("LmtTimesAmt4LowTrade")) {
			return false;//(須作 Credit Check)
		}
		return blResult;
	}
	
	
	/**

	 * 信用額度檢核
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * @return 如果通過信用額度檢核，return true，否則return false 
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCredit()  throws Exception {
		if (gGate.isChildCard) { //檢查子卡卡片帳務檔 . CRD_CARD.SON_CARD_FLAG 來判斷是不是子卡
			if (!checkCardBaseChildTotAmtConsume()) {
				ta.getAndSetErrorCode("QG"); /*子卡可用餘額不足*/
				return false;
			}
		}
		
		//V1.00.04 採購卡邏輯變更，視為一般商務卡
//		if (gGate.isPurchaseCard) { 
//			if (!checkCreditLimit()) {
//				return false;
//			}
//			return true;
//		}

		//無臨調並且為分期付款交易--check install value
		if ( (!gGate.bgHasPersonalAdj) && (!gGate.bgHasCompAdj) && (gGate.isInstallmentTx) ) {
			checkInstallmentValue(); 
		}

		//down,由卡戶之 card acct risk level 取得該等級之預借現金總月限百分比及回覆碼參數
		boolean blWkConsumeFlag=true;
		gGate.wkAdjAmt =1;
		
		String slRiskLevel = gGate.classCode;
		String slCardNote= ta.getValue("CardBaseCardNote");

		gb.showLogMessage("D","gGate.mccRiskType="+gGate.mccRiskType+"slRiskLevel="+slRiskLevel+"slCardNote="+slCardNote);

		if (!ta.selectRiskConsumeParm(gGate.mccRiskType, slRiskLevel, slCardNote)) { //Howard: follow  BRD 2.5.4
			slCardNote = "*";
			if (!ta.selectRiskConsumeParm(gGate.mccRiskType, slRiskLevel, slCardNote)) {
				ta.getAndSetErrorCode("DO");
				blWkConsumeFlag=false;
				gGate.wkAdjAmt =1;
				gGate.wkCashCode = gGate.isoField[39];
				return false;
			}
		}

		//由卡戶之 card acct risk level 取得該等級之預借現金總月限百分比及回覆碼參數
		if ("C".equals(ta.getValue("MccRiskAmtRule"))) { //預借現金總額度(C)
			gGate.cashLimit = getCashBase(); //預借現金總額度(C)
			if (blWkConsumeFlag) {
				gGate.wkAdjAmt = ta.getDouble("RiskConsumeLmtAmtMonthPct")/100;
				gGate.wkCashCode = ta.getValue("RiskConsumeRspCode1");
			}
		}

		//down,由卡戶之 card acct risk level 及 mcc_risk 取得該等級風險分類之月限額百分比/月限次數
		//及次限額百分比/日限次數及各回覆碼參數取得風險分類之月限額倍數, 次數, 次限額倍數, 日限次
		double ddAdjMonthAmt=1,  dlAdjTimeAmt=1, dlAdjMonthCnt=1, dlAdjDayCnt=1;
		if (blWkConsumeFlag) {
			ddAdjMonthAmt = ta.getDouble("RiskConsumeLmtAmtMonthPct")/ 100;
			dlAdjMonthCnt = ta.getDouble("RiskConsumeLmtCntMonth");
			dlAdjTimeAmt = ta.getDouble("RiskConsumeLmtAmtTimePct")/ 100;
			dlAdjDayCnt = ta.getDouble("RiskConsumeLmtCntDay");
		}
		else {
			ddAdjMonthAmt=1;
			dlAdjTimeAmt=1;
			dlAdjMonthCnt=1;
			dlAdjDayCnt=1;

			if ("C".equals(gGate.mccRiskAmountRule)) { //預借現金月限額
				if ("0130".equals(gGate.mesgType)) //ATM交易
					ta.getAndSetErrorCode("DP");
				else
					ta.getAndSetErrorCode("DT");
			}
			else {
				ta.getAndSetErrorCode("CP");
			}


		}

		if ("C".equals(gGate.mccRiskAmountRule)) { //預借現金月限次
			/*取得總月限額 (D)= 等級總額(A)＊月限倍數*/
			gGate.monthLimit = gGate.cashLimit * ddAdjMonthAmt;  

			/*取得總次限額 (E)= 等級總額(A)＊次限倍數*/
			gGate.timesLimit = gGate.cashLimit * dlAdjTimeAmt; 

			/*取得臨調總月限額(F) = 基本總額＊月限倍數*/
			gGate.monthLimitX = getCashBase() * ddAdjMonthAmt; 

			/*取得臨調總次限額(G) = 基本總額＊次限倍數*/
			gGate.timesLimitX = getCashBase() * dlAdjTimeAmt; 
		}
		else {
			/*取得總月限額 (D)= 等級總額(A)＊月限倍數*/
			gGate.monthLimit = gGate.finalTotLimit * ddAdjMonthAmt; 

			/*取得總次限額 (E)= 等級總額(A)＊次限倍數*/
			gGate.timesLimit = gGate.finalTotLimit * dlAdjTimeAmt; 

			/*取得臨調總月限額(F) = 基本總額＊月限倍數*/
			gGate.monthLimitX = ta.getBaseLimit() * ddAdjMonthAmt;

			/*取得臨調總次限額(G) = 基本總額＊次限倍數*/
			gGate.timesLimitX = ta.getBaseLimit() * dlAdjTimeAmt;
		}
		/*取得總月限次*/
		gGate.monthCntLimit = dlAdjMonthCnt; 
		/*取得總日限次*/
		gGate.timesCntLimit = dlAdjDayCnt; 
		
		gb.showLogMessage("D","取得總月限額monthLimit="+gGate.monthLimit);
		gb.showLogMessage("D","取得總次限額timesLimit="+gGate.timesLimit);
		gb.showLogMessage("D","臨調總月限額monthLimitX="+gGate.monthLimitX);
		gb.showLogMessage("D","臨調總次限額timesLimitX="+gGate.timesLimitX);
		gb.showLogMessage("D","取得總日限次monthCntLimit="+gGate.monthCntLimit);
		gb.showLogMessage("D","取得總日限次timesCntLimit="+gGate.timesCntLimit);

		//down, 無臨調, 讀取產品類別臨調檔 ADJ_PROD_PARM
		gb.showLogMessage("D","無臨調, 讀取產品類別臨調檔。個人臨調="+gGate.bgHasPersonalAdj+"公司臨調="+gGate.bgHasCompAdj);
		if ((!gGate.bgHasCompAdj) && (!gGate.bgHasPersonalAdj)) { //proc line 5794
			if (gGate.rollbackP2) {
				if (!computeAdjProd()) { //proc line 11764 => credit_check_adj_prod(pWA);
					//無產品別之設定
					//無產品別臨調, 檢查卡號前 10 碼之國外參數設定
					checkOverseaCredit(gGate.mccRiskAmountRule);
				}
				//17.檢查是否超過總月限額
				if (!checkCreditLimit()) { 
					return false;
				}
			}
			else {
				//17.檢查是否超過總月限額
				if (!checkCreditLimitP3()) { 
					return false;
				}
			}
			return true;
		}

		if (gGate.rollbackP2) {
			//14.個人有臨時調高額度, 算出臨時調高額度檔 ADJ_PARM
			if (gGate.bgHasPersonalAdj) { 
				computeAdjParm(gGate.mccRiskType); 
			}			
			//15.公司有臨時調高額度, 算出臨時調高額度檔 ADJ_PARM
			if (gGate.bgHasCompAdj) { 
				computeAdjParmComp(gGate.mccRiskType); 
			}
			//17-22項檢查是否超過總月限額or次限額等
			if (!checkCreditAdjLimit()) {
				return false;
			}
		}
		else {
			if (!checkCreditLimitP3()) { 
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * 判斷是否是當日的第一筆交易
	 * @return 如果是本日的第一筆交易return true，否則return false
	 */
	private boolean isNewTradeDay() {
//		boolean blResult = gb.getSysDate().equals(ta.getValue("CardAcctLastConsumeDate"));
		//V1.00.01 - 小金額交易檢查邏輯調整
		boolean blResult = gb.getSysDate().equals(gGate.lastTxDate);
		return !blResult;
	}

	
	/**
	 * 檢核子卡可用餘額是否足夠
	 * V1.00.38 P3授權額度查核調整
	 * @return 如果可用餘額足夠，return true，否則return false
	 * @throws Exception 
	 */
	private boolean checkCardBaseChildTotAmtConsume() throws Exception{
		//檢查子卡卡片帳務檔
		boolean blResult = true;

		if (HpeUtil.isCurDateBetweenTwoDays(ta.getValue("CardBaseChildCardEffStartDate"), ta.getValue("CardBaseChildCardEffEndDate"))) {
			//子卡臨調有效
			gGate.isChildAdj = true;
			gGate.cbOtb = ta.getDouble("CardBaseChildCardTmpLimit") - ta.getAlreadyAuthedNotApplyed(5,2);
		}
		else {
			gGate.cbOtb = ta.getDouble("INDIV_CRD_LMT") - ta.getAlreadyAuthedNotApplyed(5,2);
		}

		if (gGate.cbOtb < gGate.isoFiled4Value) {
			blResult = false;//子卡可用餘額不足
		}
		return blResult;

	}
	
	
	/**
	 * 檢核各種限額與限次
	 * @return 如果檢核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCreditLimit() throws Exception {
//		double dlTmpAdj120=0;
//		/***---原調整與120%比大小, 取大值*/
//		/*公用事業交易*/
//		//kevin:取消service4BatchAuth設定，改為單筆connType = "BATCH"決定
//		if ("BATCH".equals(gGate.connType)) {
//			dlTmpAdj120 = 1.2;  //公用事業交易固定調整 120%
//			if ("1".equals(gGate.ecsBatchCode)) {
//				if (gGate.wkAdjTot<dlTmpAdj120) {
//					gGate.wkAdjTot=dlTmpAdj120;
//				}
//				gGate.finalTotLimit = ta.getBaseLimit() * gGate.wkAdjTot; //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit 
//			}
//		}
		getBatchFixAmt();

//		double dlPtmpBaseLimit = ta.getBaseLimitOfComp();//ta.getDouble("ActCorpGpLmtTotConsume");/*該公司戶之基本額度*/// Howard: CardAcctI_LMT_TOT_CONSUME == ta.getDouble("ActCorpGpLmtTotConsume"),  PtmpBaseLimit 用 local 變數就可以了
		gGate.tmpTotLimit2 = ta.getBaseLimitOfComp();//ta.getDouble("ActCorpGpLmtTotConsume");

		//V1.00.02 公司戶在臨調額度改用固定金額，非臨調比例
		gb.showLogMessage("D","公司基本額度gGate.tmpTotLimit2="+gGate.tmpTotLimit2);
//		double dlPadjRate = 1;
		boolean blAdj = false;
		if (gGate.businessCard) {
			blAdj = gGate.bgHasCompAdj;//checkAcctAdj(2);// 檢核公司戶臨調 
			if (blAdj) {
//				dlPadjRate = getAdjRate();
				gGate.tmpTotLimit2 = getAdjAmt();
			}
//			gGate.tmpTotLimit2 = dlPtmpBaseLimit * dlPadjRate;
		}

		/*總消費金額 = 本次交易金額 + 已授權未請款
      					+ 總未付本金
       						(結帳消費+未結帳消費+
        						結帳預現+未結帳預現+指撥金額)
      					+ 分期未結帳金額*/
//		//kevin:已使用額度初始0，並分開公司卡與個人卡處理，確保後續不會互相影響
		double dlTmpAmt1 = gGate.isoFiled4Value + gGate.curTotalUnpaidOfPersonal;
		double dlParentAmt1 = gGate.isoFiled4Value + gGate.curTotalUnpaidOfComp;

		double dlTmpAmt2 = 0, dlParentAmt2=0;
		if (gGate.isInstallmentTx) {
			//V1.00.10 分期交易額度須包含溢繳款與payment未消
			/*總月限額 = (額度 * 調整倍數) + 預付款金額 + payment末消 */
			dlTmpAmt2 = gGate.finalTotLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; // tmpamt2   =tmpTotLimit; //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit 
			dlParentAmt2 = gGate.tmpTotLimit2 + gGate.ccaConsumePrePayAmtOfComp+ gGate.ccaConsumeTotUnPaidAmtOfComp;// parentAmt2=PtmpTotLimit;
		}
		else {
			/*總月限額 = (額度 * 調整倍數) + 預付款金額 + payment末消 */
			dlTmpAmt2 = gGate.finalTotLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; //tmpamt2   =tmpTotLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;  //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit

			dlParentAmt2 = gGate.tmpTotLimit2 + gGate.ccaConsumePrePayAmtOfComp+ gGate.ccaConsumeTotUnPaidAmtOfComp;// parentAmt2=PtmpTotLimit + CardAcctI_PRE_PAID_AMT + CardAcctI_TOT_UNPAID_AMT;
		}

		/* 卡戶餘額 OTB = 基本額度 *調整倍數 + 預付 + 總已付未消
      					- 累計消費 - 結帳消費 - 未結消費 - 指撥金額*/


		gGate.otbAmt = dlTmpAmt2 - dlTmpAmt1 + gGate.isoFiled4Value; // IsoRec.otb_amt =  tmpamt2  - tmpamt1 + IsoRec.tx_nt_amt;			  

		//kevin:總額度檢查
		if ( (dlTmpAmt1>dlTmpAmt2) || (gGate.businessCard && dlParentAmt1>dlParentAmt2) ) {
			ta.getAndSetErrorCode("DM");
			return false;
		}
		//V1.00.04 採購卡邏輯變更，視為一般商務卡 
//		if (gGate.isPurchaseCard)
//			return true;

		if ("C".equals(gGate.mccRiskAmountRule)) {  /*預借現金總額度檢查*/
			/*總預現金額 = 本次預現金額 + 已授權未請款(預現)
          					+ 預現總未付(結帳-預現 + 未結帳-預現) */
			if (gGate.isPrecash)
				gGate.paidPreCash = gGate.ccaConsumePaidPrecash;
			else
				gGate.paidPreCash=0;

			dlTmpAmt1 = gGate.isoFiled4Value +  gGate.totAmtPreCash + gGate.paidPreCash + gGate.ccaConsumeUnPaidPrecash;

			/*總預借月限額 = 額度 * 月限額調整倍數 + 預付款金額 + payment末消 */
			/*比較預現總額度(含臨調後)及卡戶等級預現總最高額度, 取其較小值*/
			if ((gGate.cashLimit > ta.getDouble("RiskLevelMaxCashAmt")) && (ta.getDouble("RiskLevelMaxCashAmt")>0) )
				gGate.cashLimit = ta.getDouble("RiskLevelMaxCashAmt");

			dlTmpAmt2 = gGate.cashLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt;

			gGate.otbAmt = dlTmpAmt2 - dlTmpAmt1 + gGate.isoFiled4Value; /*目前可用餘額*/

			//kevin:公司卡才檢查公司額度
			if ( (dlTmpAmt1>dlTmpAmt2) || (gGate.businessCard && dlParentAmt1>dlParentAmt2) ) {	//kevin:error code 處理
				ta.getAndSetErrorCode(gGate.wkCashCode);
				return false;
			}
		}

		/*13.1.1 Combo卡交易只檢核 OTB 值*/
		//*公用事業交易只檢核總額度*/
		//kevin:取消service4BatchAuth設定，改為單筆connType 決定
		if ("BATCH".equals(gGate.connType)) 
			return true;
		
		//*免照會VIP只檢核總額度*/
//		if (ta.selectCcaVip(ta.getValue("CardBaseAcctType").trim())) {
//			gGate.isAuthVip = true;
		if (gGate.isAuthVip) {
			return true;
		}

		/**** 小額不檢核其他,if AuConsume_BILL_LOW_LIMIT<=0,default=1 ****/
		if (gGate.ccaConsumeBillLowLimit<=0)
			gGate.ccaConsumeBillLowLimit=1;

		/*** 無臨調 and 國內交易時,才需做18項平均消費檢核  ***/
		if( (!gGate.bgHasCompAdj) && (!gGate.bgHasPersonalAdj) && ("T".equals(gGate.areaType)) ){
			if (!checkAverage()) //proc is CCAS_check_average()
				return false;
		}

		/*19.檢查是否超過卡戶等級之風險類別月限額*/
		/*tmpamt1 = 本次交易金額 + 風險類別累計消費金額 */
		dlTmpAmt1 = gGate.isoFiled4Value + gGate.riskTradeMonthAmt; //tmpamt1 = IsoRec.tx_nt_amt + RiskTAmt_T_AMT_MONTH;

		/*總月限額 = 額度*月限額調整倍數 + 預付款金額 + payment末消 */
		dlTmpAmt2 = gGate.monthLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; //tmpamt2 = tmpMonthLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;

		if (dlTmpAmt1>dlTmpAmt2) {
			ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode1"));
			return false;
		}

		/*20.檢查是否超過卡戶等級之風險類別月限次*/
		/**** AUTH_PARM參數設定檢核卡戶等級風險月限次 ***/
		if ("1".equals(ta.getValue("AuthParmMonthRiskChk"))) {
			if ((gGate.riskTradeMonthCnt + 1) > gGate.monthCntLimit) {
				ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode2"));
				return false;
			}
		}


		/*21.檢查是否超過卡戶等級之風險類別次限額*/
		if (gGate.isoFiled4Value > gGate.timesLimit) {
			ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode3"));
			return false;
		}

		/*22.檢查是否超過卡戶等級之風險類別日限次*/
		if("1".equals(ta.getValue("AuthParmDayRiskChk"))) {
			if ((gGate.riskTradeDayCnt + 1) > gGate.timesCntLimit) {
				ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode4"));
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 *無臨調並且為分期付款交易時， 總月限額與參數之最大分期限額,兩者間取孰小
	 * @throws Exception if any exception occurred
	 */
	private void checkInstallmentValue() throws Exception {
		if (gGate.finalTotLimit > (ta.getDouble("ActAcnoLineOfCreditAmt") + ta.getDouble("RiskLevelMaxInstAmt"))) { // Howard : G_Gate.tmpTotLimit1 change to  G_Gate.finalTotLimit
			gGate.finalTotLimit = ta.getDouble("ActAcnoLineOfCreditAmt") + ta.getDouble("RiskLevelMaxInstAmt");  
		}

		if (gGate.tmpTotLimit2 > (ta.getBaseLimitOfComp() + ta.getDouble("RiskLevelMaxInstAmt"))) {
			gGate.tmpTotLimit2 = ta.getBaseLimitOfComp() + ta.getDouble("RiskLevelMaxInstAmt");  
		}
	}
	
	
	/**
	 * 取得產品類別臨調檔
	 * @return 如果成功取得，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean computeAdjProd()  throws Exception{
		if (!ta.selectAdjProdParm())
			return false;

		gGate.wkAdjTot = ta.getDouble("AdjProdParmTotAmtMonth");//放大總月限額
		gGate.wkAdjAmt = ta.getDouble("AdjProdParmTimesAmt")/100; //金額倍數百分比
		gGate.wkAdjCnt = ta.getDouble("AdjProdParmTimesCnt")/100; //次數倍數百分比

		gGate.totalLimit = ta.getBaseLimit() * gGate.wkAdjTot;  /*該等級臨調總月限額倍數*/
		gGate.cashLimit = gGate.cashBase * gGate.wkAdjAmt; 	/*該等級臨調總預借現金倍數*/
		gGate.monthLimit = gGate.monthLimitX * gGate.wkAdjAmt; /*取得臨調總月限額 = 等級總月額＊月限倍數*/
		gGate.timesLimit = gGate.timesLimitX * gGate.wkAdjAmt; /*取得臨調總次限額 = 等級總月次＊次限倍數*/

		gGate.monthCntLimit = gGate.monthCntLimit* gGate.wkAdjCnt;/*取得臨調總月限次*/
		gGate.timesCntLimit = gGate.timesCntLimit* gGate.wkAdjCnt;/*取得臨調總日限次*/

		return true;
	}
	
	
	/**
	 * 檢核是否有產品類別之國外參數設定
	 * 
	 * @param spAmtRule 交易金額歸屬註記(CCA_MCC_RISK.AMOUNT_RULE)
	 * @return 如果檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkOverseaCredit(String spAmtRule)   throws Exception{
		boolean blResult = true;
		//國內交易 and 參數不檢核國外交易
		if ( ("T".equals(gGate.areaType)) || ("0".equals(ta.getValue("AuthParmOverseaChk"))) ) {
			return true; //國內交易 and 參數不檢核國外交易，所以 return 
		}

		if (!ta.selectPrdTypeIntr(spAmtRule)) {
			blResult=false;
		}
		return blResult;
	}
	
	
	/**
	 * 計算某風險類別之臨調參數資料個人
	 * @param spRiskType 風險類別
	 * @throws Exception in any exception occurred
	 */
	private void computeAdjParm(String spRiskType)  throws Exception{
		if (gGate.isInstallmentTx) {
			gGate.wkAdjTot = ta.getDouble("CardAcctAdjInstPct");
		}
		else {
			gGate.wkAdjTot = ta.getDouble("CardAcctTotAmtMonth");
		}
//		/***** 讀取臨調專款專用資料 **/   
		//V1.00.05 臨調專款專用金額，可使用到基本額度
		ResultSet adjRS = ta.loadAdjParmSpecAmt(ta.getValue("CardAcctAdjEffStartDate"), ta.getValue("CardAcctAdjEffEndDate"));
		gb.showLogMessage("D","check CCA ADJ PARM adjRS="+adjRS);
		double dlSpecAmt = 0, dlTmpAmt = 0;
		boolean blMatchRisk = false, blMatchSpec = false;
		String slCurdate = HpeUtil.getCurDateStr(false);  

		if (adjRS == null) {
			gGate.finalTotLimit =  gGate.wkAdjTot;
		}
		else {
			while (adjRS.next()) {
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmRiskType="+adjRS.getString("CcaAdjParmRiskType"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjMonthAmt="+adjRS.getString("CcaAdjParmAdjMonthAmt"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjEffStartDate="+adjRS.getString("CcaAdjParmAdjEffStartDate"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjEffEndDate="+adjRS.getString("CcaAdjParmAdjEffEndDate"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmSpecFlag="+adjRS.getString("CcaAdjParmSpecFlag"));
				//V1.00.06 撈取臨調授權資料，不須判斷專款專用旗標
//				double dlSpecAmt1 = ta.selectAdjTxlog(adjRS.getString("CcaAdjParmAdjEffStartDate"), adjRS.getString("CcaAdjParmAdjEffEndDate"), adjRS.getString("CcaAdjParmSpecFlag"), adjRS.getString("CcaAdjParmRiskType")+"%");
				double dlSpecAmt1 = ta.selectAdjTxlog(adjRS.getString("CcaAdjParmAdjEffStartDate"), adjRS.getString("CcaAdjParmAdjEffEndDate"), "N", adjRS.getString("CcaAdjParmRiskType")+"%");
				if (spRiskType.equals(adjRS.getString("CcaAdjParmRiskType")) && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffStartDate")) >= 0  && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffEndDate")) <= 0) {
					if ("Y".equals(adjRS.getString("CcaAdjParmSpecFlag"))) {
						blMatchSpec = true;
						dlTmpAmt = adjRS.getDouble("CcaAdjParmAdjMonthAmt");
						gb.showLogMessage("D","check CCA ADJ PARM SPEC_FLAG "+spRiskType+" = "+adjRS.getString("CcaAdjParmRiskType")+" dlTmpAmt = "+dlTmpAmt);
					}
					else {
						blMatchRisk = true;
						if (dlSpecAmt1 + gGate.isoFiled4Value > adjRS.getDouble("CcaAdjParmAdjMonthAmt")) {
							dlTmpAmt =  gGate.curTotalUnpaidOfPersonal;
						}
						else {
							if ((gGate.curTotalUnpaidOfPersonal + gGate.isoFiled4Value) > gGate.wkAdjTot) {
								dlTmpAmt =  gGate.curTotalUnpaidOfPersonal;
							}
							else {
								dlTmpAmt =  gGate.curTotalUnpaidOfPersonal + adjRS.getDouble("CcaAdjParmAdjMonthAmt") - dlSpecAmt1;

							}
						}
						gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE "+spRiskType+" = "+adjRS.getString("CcaAdjParmRiskType")+" dlTmpAmt = "+dlTmpAmt);
					}
				}
				else {
					if (dlSpecAmt1 < adjRS.getDouble("CcaAdjParmAdjMonthAmt") && "Y".equals(adjRS.getString("CcaAdjParmSpecFlag"))) {
						dlSpecAmt = dlSpecAmt + (adjRS.getDouble("CcaAdjParmAdjMonthAmt") - dlSpecAmt1);
					}
					gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE = "+adjRS.getString("CcaAdjParmRiskType")+" dlSpecAmt1 = "+dlSpecAmt1+" dlSpecAmt = "+dlSpecAmt);
				}
				gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE = "+adjRS.getString("CcaAdjParmRiskType")+" dlSpecAmt = "+dlSpecAmt);
			}
			if (gGate.adjRiskType) { //個人有設定臨調的風險分類旗標，檢查是否對應到專款專用之限制額度旗標
				if (blMatchSpec) {
					gGate.cacuFlag = "Y";
					gGate.finalTotLimit =  gGate.wkAdjTot - dlSpecAmt;
					gb.showLogMessage("D","專款專用類別_臨調金額，臨調期間額度gGate.finalTotLimit="+gGate.finalTotLimit);
				}
				else if (blMatchRisk) {
					gGate.finalTotLimit =  dlTmpAmt;
					gb.showLogMessage("D","風險限制類別_臨調金額，臨調期間額度gGate.finalTotLimit="+gGate.finalTotLimit);
				}
				else {
					gGate.finalTotLimit =  gGate.wkAdjTot - dlSpecAmt;
					gb.showLogMessage("D","已過期之(專款專用/風險限制)類別，臨調期間額度gGate.finalTotLimit="+gGate.finalTotLimit);
				}			
			}
			else {
				gGate.finalTotLimit =  gGate.wkAdjTot - dlSpecAmt;
				gb.showLogMessage("D","非(專款專用/風險限制)類別，臨調期間額度gGate.finalTotLimit="+gGate.finalTotLimit);			
			}
		}
		
//		gGate.finalTotLimit =  gGate.monthLimit;                  /*該基本臨調總月限額*/ 
		
	}

	/**
	 * 計算某風險類別之臨調參數資料公司
	 * @param spRiskType 風險類別
	 * @throws Exception in any exception occurred
	 */
	private void computeAdjParmComp(String spRiskType)  throws Exception{
		if (gGate.isInstallmentTx) {
			gGate.wkAdjTot = ta.getDouble("CardAcctAdjInstPctOfComp");
		}
		else {
			gGate.wkAdjTot = ta.getDouble("CardAcctTotAmtMonthOfComp");
		}
//		/***** 讀取臨調專款專用資料 **/    
//		gGate.monthLimit =  gGate.wkAdjTot;
		gGate.tmpTotLimit2 =  gGate.wkAdjTot;                  /*該公司基本臨調總月限額*/ 
		gb.showLogMessage("D","公司臨調月限額gGate.tmpTotLimit2="+gGate.tmpTotLimit2);

	}
	
	/**
	 * 取得臨調比率
	 * @return 臨調比率
	 */
	private double getAdjRate() {
		//此 function 應該是不需要了，改用 getAdjAmt() 即可
		double dlAdjRate=1;
		if(gGate.isInstallmentTx) {
			if (ta.getDouble("CardAcctAdjInstPct")>0)
				dlAdjRate = ta.getDouble("CardAcctAdjInstPct")/100;
		}
		else {
			if (ta.getDouble("CardAcctTotAmtMonth")>0)
				dlAdjRate = ta.getDouble("CardAcctTotAmtMonth")/100;
		}
		return dlAdjRate;
	}
	
	/**
	 * 取得臨調後的金額
	 * @return 臨調後的金額
	 */
	private double getAdjAmt() {
		double dlAdjAmt=0;
		if(gGate.isInstallmentTx) {
			if (ta.getDouble("CardAcctAdjInstPctOfComp")>0)
				dlAdjAmt = ta.getDouble("CardAcctAdjInstPctOfComp"); /* 臨調分期付款金額  -本來是 %         */
		}
		else {
			if (ta.getDouble("CardAcctTotAmtMonthOfComp")>0)
				dlAdjAmt = ta.getDouble("CardAcctTotAmtMonthOfComp");/* 臨調放大總月限金額  -本來是 %       */
		}
		return dlAdjAmt;
	}

	/**
	 * 批次授權取原調整與120%比大小, 取大值
	 * V1.00.46 P3批次授權比照一般授權的邏輯，不須特別排除
	 * @return void
	 */
	private void getBatchFixAmt() {
		/***---原調整與120%比大小, 取大值*/
		/*公用事業交易*/
		//kevin:取消service4BatchAuth設定，改為單筆connType = "BATCH"決定
//		if ("BATCH".equals(gGate.connType)) {
//			BigDecimal bdlTmpAdj120 = new BigDecimal("1.20"); //公用事業交易固定調整 120%
//			BigDecimal bdTmpAdj = new BigDecimal(Double.toString(gGate.wkAdjTot)).setScale(2, RoundingMode.HALF_UP); //原來調整倍數
//			if ("1".equals(gGate.ecsBatchCode)) {
//				if (bdTmpAdj.compareTo(bdlTmpAdj120)<0) {
//					bdTmpAdj=bdlTmpAdj120;
//					gGate.wkAdjTot=bdlTmpAdj120.setScale(2, RoundingMode.HALF_UP).doubleValue();
//				}
//				gGate.finalTotLimit = new BigDecimal(ta.getBaseLimit()).multiply(bdTmpAdj).setScale(0, RoundingMode.HALF_UP).doubleValue(); //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit 
//			}
//		}
	}
	


	/*18.平均消費額之判斷*/
	/**
	 * 18.平均消費額之判斷
	 * @return true 取消此一檢核
	 */
	private boolean checkAverage () throws Exception{

		boolean blResult = true;

		return blResult;
	}
	
	/**
	 * 檢核臨調的各種限額與限次
	 * @return 如果檢核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCreditAdjLimit() throws Exception {
//		double dlTmpAdj120=0;
//		/***---原調整與120%比大小, 取大值*/
//		/*公用事業交易*/
//		//kevin:取消service4BatchAuth設定，改為單筆connType = "BATCH"決定
//		if ("BATCH".equals(gGate.connType)) {
//			dlTmpAdj120 = 1.2;  //公用事業交易固定調整 120%
//			if ("1".equals(gGate.ecsBatchCode)) {
//				if (gGate.wkAdjTot<dlTmpAdj120) {
//					gGate.wkAdjTot=dlTmpAdj120;
//				}
//				gGate.finalTotLimit = ta.getBaseLimit() * gGate.wkAdjTot; //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit 
//			}
//		}
		getBatchFixAmt();

//		double dlPtmpBaseLimit = ta.getBaseLimitOfComp();//ta.getDouble("ActCorpGpLmtTotConsume");/*該公司戶之基本額度*/// Howard: CardAcctI_LMT_TOT_CONSUME == ta.getDouble("ActCorpGpLmtTotConsume"),  PtmpBaseLimit 用 local 變數就可以了
		//kevin:20220104 gGate.tmpTotLimit2調整在computeAdjParmComp處理
//		gGate.tmpTotLimit2 = ta.getBaseLimitOfComp();//ta.getDouble("ActCorpGpLmtTotConsume");
//
		//V1.00.02 公司戶在臨調額度改用固定金額，非臨調比例
		gb.showLogMessage("D","公司基本額度gGate.tmpTotLimit2="+gGate.tmpTotLimit2);
//		double dlPadjRate = 1;
		boolean blAdj = false;
		if (gGate.businessCard) {
			blAdj = gGate.bgHasCompAdj;//checkAcctAdj(2);// 檢核公司戶臨調 
			if (blAdj) {
//				dlPadjRate = getAdjRate();
				gGate.tmpTotLimit2 = getAdjAmt();
			}
//			gGate.tmpTotLimit2 = dlPtmpBaseLimit * dlPadjRate;
		}

		/*總消費金額 = 本次交易金額 + 已授權未請款
      					+ 總未付本金
       						(結帳消費+未結帳消費+
        						結帳預現+未結帳預現+指撥金額)
      					+ 分期未結帳金額*/
//		//kevin:已使用額度初始0，並分開公司卡與個人卡處理，確保後續不會互相影響
		double dlTmpAmt1 = gGate.isoFiled4Value + gGate.curTotalUnpaidOfPersonal;
		double dlParentAmt1 = gGate.isoFiled4Value + gGate.curTotalUnpaidOfComp;

		double dlTmpAmt2 = 0, dlParentAmt2=0;
		if (gGate.isInstallmentTx) {
			//V1.00.10 分期交易額度須包含溢繳款與payment未消
			/*總月限額 = (額度 * 調整倍數)*/
			dlTmpAmt2 = gGate.finalTotLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; // tmpamt2   =tmpTotLimit; //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit 
			dlParentAmt2 = gGate.tmpTotLimit2 + gGate.ccaConsumePrePayAmtOfComp+ gGate.ccaConsumeTotUnPaidAmtOfComp;// parentAmt2=PtmpTotLimit;
		}
		else {
			/*總月限額 = (額度 * 調整倍數) + 預付款金額 + payment末消 */
			dlTmpAmt2 = gGate.finalTotLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; //tmpamt2   =tmpTotLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;  //Howard: G_Gate.tmpTotLimit1 change to G_Gate.finalTotLimit

			dlParentAmt2 = gGate.tmpTotLimit2 + gGate.ccaConsumePrePayAmtOfComp+ gGate.ccaConsumeTotUnPaidAmtOfComp;// parentAmt2=PtmpTotLimit + CardAcctI_PRE_PAID_AMT + CardAcctI_TOT_UNPAID_AMT;
		}

		/* 卡戶餘額 OTB = 基本額度 *調整倍數 + 預付 + 總已付未消
      					- 累計消費 - 結帳消費 - 未結消費 - 指撥金額*/


		gGate.otbAmt = dlTmpAmt2 - dlTmpAmt1 + gGate.isoFiled4Value; // IsoRec.otb_amt =  tmpamt2  - tmpamt1 + IsoRec.tx_nt_amt;			  
		
		//kevin:總額度檢查
		gb.showLogMessage("D","adj otbAmt = "+ gGate.otbAmt);
		gb.showLogMessage("D","總額度檢查dlTmpAmt1 = "+dlTmpAmt1+" >  dlTmpAmt2="+dlTmpAmt2);
		gb.showLogMessage("D","總額度檢查dlParentAmt1 = "+dlParentAmt1+" >  dlParentAmt2 = "+dlParentAmt2);
		gb.showLogMessage("D","gGate.businessCard = "+gGate.businessCard);

		if ( (dlTmpAmt1>dlTmpAmt2) || (gGate.businessCard && dlParentAmt1>dlParentAmt2) ) {
			ta.getAndSetErrorCode("DM");
			return false;
		}
		//V1.00.04 採購卡邏輯變更，視為一般商務卡 
//		if (gGate.isPurchaseCard)
//			return true;

		if ("C".equals(gGate.mccRiskAmountRule)) {  /*預借現金總額度檢查*/
			/*總預現金額 = 本次預現金額 + 已授權未請款(預現)
          					+ 預現總未付(結帳-預現 + 未結帳-預現) */
			if (gGate.isPrecash)
				gGate.paidPreCash = gGate.ccaConsumePaidPrecash;
			else
				gGate.paidPreCash=0;

			dlTmpAmt1 = gGate.isoFiled4Value +  gGate.totAmtPreCash + gGate.paidPreCash + gGate.ccaConsumeUnPaidPrecash;

			/*總預借月限額 = 額度 * 月限額調整倍數 + 預付款金額 + payment末消 */
			/*比較預現總額度(含臨調後)及卡戶等級預現總最高額度, 取其較小值*/
			if ((gGate.cashLimit > ta.getDouble("RiskLevelMaxCashAmt")) && (ta.getDouble("RiskLevelMaxCashAmt")>0) )
				gGate.cashLimit = ta.getDouble("RiskLevelMaxCashAmt");

			dlTmpAmt2 = gGate.cashLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt;

			gGate.otbAmt = dlTmpAmt2 - dlTmpAmt1 + gGate.isoFiled4Value; /*目前可用餘額*/

			//kevin:公司卡才檢查公司額度
			if ( (dlTmpAmt1>dlTmpAmt2) || (gGate.businessCard && dlParentAmt1>dlParentAmt2) ) {	//kevin:error code 處理
				ta.getAndSetErrorCode(gGate.wkCashCode);
				return false;
			}
		}

		/*13.1.1 Combo卡交易只檢核 OTB 值*/
		//*公用事業交易只檢核總額度*/
		//kevin:取消service4BatchAuth設定，改為單筆connType 決定
		if ("BATCH".equals(gGate.connType)) 
			return true;
		
		//*免照會VIP只檢核總額度*/
//		if (ta.selectCcaVip(ta.getValue("CardBaseAcctType").trim())) {
//			gGate.isAuthVip = true;
		if (gGate.isAuthVip) {
			return true;
		}

		/**** 小額不檢核其他,if AuConsume_BILL_LOW_LIMIT<=0,default=1 ****/
		if (gGate.ccaConsumeBillLowLimit<=0)
			gGate.ccaConsumeBillLowLimit=1;

		/*** 無臨調 and 國內交易時,才需做18項平均消費檢核  ***/
		if( (!gGate.bgHasCompAdj) && (!gGate.bgHasPersonalAdj) && ("T".equals(gGate.areaType)) ){
			if (!checkAverage()) //proc is CCAS_check_average()
				return false;
		}

		/*19.檢查是否超過卡戶等級之風險類別月限額*/
		/*tmpamt1 = 本次交易金額 + 風險類別累計消費金額 */
		dlTmpAmt1 = gGate.isoFiled4Value + gGate.riskTradeMonthAmt; //tmpamt1 = IsoRec.tx_nt_amt + RiskTAmt_T_AMT_MONTH;

		/*總月限額 = 額度*月限額調整倍數 + 預付款金額 + payment末消 */
		dlTmpAmt2 = gGate.monthLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; //tmpamt2 = tmpMonthLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;

		gb.showLogMessage("D","19.檢查是否超過卡戶等級之風險類別月限額 = "+dlTmpAmt1+">"+dlTmpAmt2);

		if (dlTmpAmt1>dlTmpAmt2) {
			ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode1"));
			return false;
		}
		

		//專款專用不需檢查風險類別月限次、次限額、日限次
		if (!gGate.isAdjSpecAmt) {
			gb.showLogMessage("D","檢查是否超過卡戶等級之風險類別月限次專款專用狀態 = "+gGate.isAdjSpecAmt);

			/*20.檢查是否超過卡戶等級之風險類別月限次*/
			/**** AUTH_PARM參數設定檢核卡戶等級風險月限次 ***/
			if ("1".equals(ta.getValue("AuthParmMonthRiskChk"))) {
				gb.showLogMessage("D","20.檢查是否超過卡戶等級之風險類別AUTH_PARM參數設定月限次 = "+ gGate.riskTradeMonthCnt + 1 +" > "+ gGate.monthCntLimit);
				if ((gGate.riskTradeMonthCnt + 1) > gGate.monthCntLimit) {
					ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode2"));
					return false;
				}
			}
	
	
			/*21.檢查是否超過卡戶等級之風險類別次限額*/
			gb.showLogMessage("D","21.檢查是否超過卡戶等級之風險類別次限額 = "+ gGate.isoFiled4Value +" > "+ gGate.timesLimit);
			if (gGate.isoFiled4Value > gGate.timesLimit) {
				ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode3"));
				return false;
			}
	
			/*22.檢查是否超過卡戶等級之風險類別日限次*/
			if("1".equals(ta.getValue("AuthParmDayRiskChk"))) {
				gb.showLogMessage("D","22.檢查是否超過卡戶等級之風險類別日限次 = "+ gGate.riskTradeDayCnt + 1 +" > "+ gGate.timesCntLimit);
				if ((gGate.riskTradeDayCnt + 1) > gGate.timesCntLimit) {
					ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode4"));
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 檢核臨調的各種限額與限次
	 * V1.00.38 P3授權額度查核調整
	 * @return 如果檢核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCreditLimitP3() throws Exception {
		getBatchFixAmt();

		gb.showLogMessage("D","個人實際額度gGate.realCreditCardBaseLimit="+gGate.realCreditCardBaseLimit);
		gb.showLogMessage("D","個人實際預借額度gGate.realCreditCardBaseLimitOfCash="+gGate.realCreditCardBaseLimitOfCash);
		gb.showLogMessage("D","公司實際額度gGate.realCreditCardBaseLimitOfComp="+gGate.realCreditCardBaseLimitOfComp);
		gb.showLogMessage("D","公司實際預借額度gGate.realCreditCardBaseLimitOfCashOfComp="+gGate.realCreditCardBaseLimitOfCashOfComp);
		gb.showLogMessage("D","個人可用額度gGate.otbAmt="+gGate.otbAmt);
		gb.showLogMessage("D","個人可用預借額度gGate.otbAmtCash="+gGate.otbAmtCash);
		gb.showLogMessage("D","公司可用額度gGate.parentOtbAmt="+gGate.parentOtbAmt);
		gb.showLogMessage("D","公司可用預借額度gGate.parentOtbAmtCash="+gGate.parentOtbAmtCash);

		if ((gGate.otbAmt - gGate.isoFiled4Value < 0) || (gGate.businessCard && (gGate.parentOtbAmt - gGate.isoFiled4Value < 0))) {
			ta.getAndSetErrorCode("DM");
			return false;
		}

		if ("C".equals(gGate.mccRiskAmountRule)) {  /*預借現金總額度檢查*/
			if ((gGate.otbAmtCash - gGate.isoFiled4Value < 0) || (gGate.businessCard && (gGate.parentOtbAmtCash - gGate.isoFiled4Value < 0))) {	//kevin:error code 處理
				ta.getAndSetErrorCode(gGate.wkCashCode);
				return false;
			}
		}

		/*13.1.1 Combo卡交易只檢核 OTB 值*/
		//*公用事業交易只檢核總額度*/
		if ("BATCH".equals(gGate.connType)) 
			return true;
		
		//*免照會VIP只檢核總額度*/
		if (gGate.isAuthVip) {
			return true;
		}
		//專款專用不需檢查風險類別月限額、月限次、次限額、日限次
		if (gGate.isAdjSpecAmt) {
			return true;
		}
		/**** 小額不檢核其他,if AuConsume_BILL_LOW_LIMIT<=0,default=1 ****/
		if (gGate.ccaConsumeBillLowLimit<=0)
			gGate.ccaConsumeBillLowLimit=1;

		/*** 無臨調 and 國內交易時,才需做18項平均消費檢核  ***/
		if( (!gGate.bgHasCompAdj) && (!gGate.bgHasPersonalAdj) && ("T".equals(gGate.areaType)) ){
			if (!checkAverage()) //proc is CCAS_check_average()
				return false;
		}

		/*19.檢查是否超過卡戶等級之風險類別月限額*/
		/*tmpamt1 = 本次交易金額 + 風險類別累計消費金額 */
		double dlTmpAmt1 = gGate.isoFiled4Value + gGate.riskTradeMonthAmt; //tmpamt1 = IsoRec.tx_nt_amt + RiskTAmt_T_AMT_MONTH;

		/*總月限額 = 額度*月限額調整倍數 + 預付款金額 + payment末消 */
		double dlTmpAmt2 = gGate.monthLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt; //tmpamt2 = tmpMonthLimit + AuConsume_PRE_PAY_AMT + AuConsume_TOT_UNPAID_AMT;

		gb.showLogMessage("D","19.檢查是否超過卡戶等級之風險類別月限額 = "+dlTmpAmt1+">"+dlTmpAmt2);

		if (dlTmpAmt1>dlTmpAmt2) {
			ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode1"));
			return false;
		}
		
		gb.showLogMessage("D", "檢查是否超過卡戶等級之風險類別月限次專款專用狀態 = " + gGate.isAdjSpecAmt);

		/* 20.檢查是否超過卡戶等級之風險類別月限次 */
		/**** AUTH_PARM參數設定檢核卡戶等級風險月限次 ***/
		if ("1".equals(ta.getValue("AuthParmMonthRiskChk"))) {
			gb.showLogMessage("D", "20.檢查是否超過卡戶等級之風險類別AUTH_PARM參數設定月限次 = " + gGate.riskTradeMonthCnt + 1 + " > "
					+ gGate.monthCntLimit);
			if ((gGate.riskTradeMonthCnt + 1) > gGate.monthCntLimit) {
				ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode2"));
				return false;
			}
		}

		/* 21.檢查是否超過卡戶等級之風險類別次限額 */
		gb.showLogMessage("D", "21.檢查是否超過卡戶等級之風險類別次限額 = " + gGate.isoFiled4Value + " > " + gGate.timesLimit);
		if (gGate.isoFiled4Value > gGate.timesLimit) {
			ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode3"));
			return false;
		}

		/* 22.檢查是否超過卡戶等級之風險類別日限次 */
		if ("1".equals(ta.getValue("AuthParmDayRiskChk"))) {
			gb.showLogMessage("D",
					"22.檢查是否超過卡戶等級之風險類別日限次 = " + gGate.riskTradeDayCnt + 1 + " > " + gGate.timesCntLimit);
			if ((gGate.riskTradeDayCnt + 1) > gGate.timesCntLimit) {
				ta.getAndSetErrorCode(ta.getValue("RiskConsumeRspCode4"));
				return false;
			}
		}
		return true;
	}
	

}
