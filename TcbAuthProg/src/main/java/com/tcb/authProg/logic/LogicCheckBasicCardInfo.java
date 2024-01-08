/**
 * 授權邏輯查核-基本交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-基本交易檢核處理                    *
 * 2021/03/22  V1.00.01  Kevin       經常性身分驗證交易不須檢查到期日                  *
 * 2021/03/29  V1.00.02  Kevin       商務卡不用取CardBaseChild                   *
 * 2021/11/16  V1.00.03  Kevin       VISA OEMPAY通知交易未帶效期，所以不檢查          *
 * 2022/03/10  V1.00.04  Kevin       VD國外交易一律Markup手續費                    *
 * 2022/03/23  V1.00.05  Kevin       免照會VIP不須檢查風險特店                      *
 * 2022/03/25  V1.00.06  Kevin       由於票證交易未能帶正確的信用卡效期，因此直接在此取得卡片*
 *                                   最新效期，提供系統檢查信用卡是否過期。             *
 * 2022/04/07  V1.00.07  Kevin       VD國外交易手續費調整                          *
 * 2022/04/11  V1.00.08  Kevin       授權補登交易取消設定，同預先授權完成交易            *  
 * 2022/05/06  V1.00.09	 Kevin       一卡通自動加值回應未開卡的問題                   *
 * 2022/07/07  V1.00.10  Kevin       停用、特指、凍結的卡片可以沖銷交易                *
 * 2022/11/07  V1.00.23  Kevin       特店編號取得手續費率，除原先4碼符合之外再新增8碼檢查  *
 * 2022/12/29  V1.00.32	 Kevin       OEMPAY通知交易未帶入效期，所以不需判定卡片是否開卡     *
 * 2023/03/15  V1.00.41  Kevin       P3新卡已開卡後，仍舊使用舊卡消費，系統一律拒絕         *
 * 2023/06/01  V1.00.46  Kevin       P3批次授權比照一般授權的邏輯，不須特別排除            *
 * 2023/08/04  V1.00.50  Kevin       經常性身分驗證交易不須檢查到期日與開卡註記            *
 * 2023/11/17  V1.00.57  Kevin       MCC風險分類檔欄位整理                          *
 * 2024/01/05  V1.00.65  Kevin       批次授權不檢查效期及開卡;                        *
 *  								 一卡通自動加值狀態查詢比照自動加值使用系統效期          *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.math.BigDecimal;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckBasicCardInfo extends AuthLogic {
	
	public LogicCheckBasicCardInfo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckBasicCardInfo : started");

	}
	
	// 查核 CRD_CARD
	/**
	 * 基本交易檢核的入口 function
	 * @return 如果檢核失敗，return false，否則return true
	 * @throws Exception if any exception occurred
	 */
	public boolean checkBasicCardInfo() throws Exception {

		//先取得交易風險類別
		getMccRiskInfo();
		
		if (!checkCardBase()) {
			return false;
		}

		if (gGate.isDebitCard) {
			// is debit card        	
			if (!checkDbcCard()) {
				return false;
			}
			if (!checkDebitParm()) {
				return false;
			}
			if (!computeTransAmtOfMarkup()) {  // VD交易將外幣交易依據匯率重新計算換算成台幣, 加上 國外交易匯差markup手續費
				gGate.isoFiled4ValueAfterMarkup = gGate.isoFiled4Value;
			}
			//kevin:新增VD加圈手續費
			computeVdMccEgovFee();       // VD交易將特定MCC 加入egov手續費
			//kevin:圈存金額從authLogicCheck搬過來
			gGate.lockAmt = getFinalIsoField4Value();//G_Gate.isoFiled4Value;

		}
		else {
			// is credit card        	
			if (!checkCrdCard()) {
				return false;
			}
			if (!checkAuthParm()) {
				return false;
			}
		}
		
		computeBalanceAmt(gGate.isoFiled4Value, gGate.adjustAmount);

		ta.selectAcctType(gGate.isDebitCard, ta.getValue("CardBaseAcctType").trim());

		ta.loadRiskTradeInfo(); //讀取卡戶當月之消費總金額   //Howard: 需在 getMccRiskInfo() 之後執行  
		ta.getRiskTradeInfo();  //讀取卡戶當月之風險分類消費，日、月金額次數

		////-- 0:正卡 1:附卡
		if ("1".equals(ta.getValue("SUP_FLAG")))  
			gGate.isSupCard = true;
		else
			gGate.isSupCard = false;

		String slTargetFieldName = ta.getValue("CARD_TYPE");
		if (ta.selectSysParm2("ACCTTYPE", slTargetFieldName, "sys_data2")) {
			if ("N".equals(ta.getValue(slTargetFieldName))) {
				gGate.isPrecash = false;
			}
		}

		if (!checkPtrCardType()) {
			return false;
		}

		//down,一定要在checkDbcCard()  and checkCrdCard() 之前 run
		//根據卡片團代取得card group code相關參數
		ta.selectCrdItemUnit();
		//取得卡樣之OEMPAY卡片設定註記
		gGate.configutationId = ta.getValue("ISSUER_CONFIGURATION_ID");
		gb.showLogMessage("D","checkBasicCardInfo() ISSUE_CONFIGURATION_ID = " + gGate.configutationId );
		if (gGate.configutationId.length() < 10) {
			gGate.configutationId = HpeUtil.fillCharOnRight(gGate.configutationId, 10," ");
		}

		//檢查是否為虛擬卡
		if (ifVirtualCard()) 
			gGate.isVirtualCard = true;
		//up,一定要在checkDbcCard()  and checkCrdCard() 之前 run
		
		//檢查是否為採購卡
		if (ifPurchaseCard()) 
			gGate.isPurchaseCard = true;

		if ( !checkCardStatus() )	{
			return false;  
		}

		if ( !checkCardExpireDate() ) {
			return false; 
		}

		//card_open_chk
		int nlOperationCode=1;
		//語音開卡檢查
		if (gGate.txVoice || gGate.atmCardOpen) {
			nlOperationCode=2;
		}
		if ( !checkCardOpeningStatus(nlOperationCode) ) {
			return false; 
		}

		return true;
	}
	
	/**
	 * 取得MCC對應之風限類別
	 * V1.00.57 MCC風險分類檔欄位整理
	 * @throws Exception if any exception occurred
	 */
	private void getMccRiskInfo() throws Exception {

		gb.showLogMessage("I","getMccRiskInfo : started : riskFctorInd=2");

		if ( !ta.selectMccRisk() ) {
			gb.showLogMessage("I","getMccRiskInfo not found : auto insert risk type(R) and mcc code = " + gGate.mccCode);
		}
		else {
			gGate.mccRiskType= ta.getValue("MccRiskRiskType").trim();
			gGate.mccRiskAmountRule = ta.getValue("MccRiskAmtRule").trim();
			gGate.mccRiskNcccFtpCode= ta.getValue("MccRiskNcccFtpCode").trim();
			gGate.mccRiskMccCode = ta.getValue("MccRiskMccCODE").trim();
			gGate.mccRiskFactor = ta.getDouble("MccRiskFactor");
		}
		gb.showLogMessage("D","MCC CODE="+gGate.mccRiskMccCode+";Risk Type=" + gGate.mccRiskType+";Amount Rule=>" + gGate.mccRiskAmountRule +
		                  ";RiskFactor=" + gGate.mccRiskFactor);
		gGate.riskFctorInd = 2; //kevin: 2. 計算 Mcc_code 風險分數
		return ;
	}
		
	/**
	 * 檢核 CARD_BASE是否有卡瑱資料
	 * @return 如果 CARD_BASE 有卡片的資料return true，否則 return false
	 * @throws Exception if any exception
	 */
	private boolean checkCardBase() throws Exception {

		ta.selectCardBase();
		if ( "Y".equals(ta.notFound) ) {
			if (gGate.txVoice)
				//ta.getAndSetErrorCode("ERR62");
				ta.getAndSetErrorCode("DL");
			else {
				//ta.getAndSetErrorCode("ERR52");
				ta.getAndSetErrorCode("D1");
				gGate.isCardNotExit = true;
				}
			return false;
		}
		//kevin:不是1，全部都算商務卡
		if (!"1".equals(ta.getValue("CardBaseAcnoFlag"))) {
			//G_Gate.isCorp = true;
			gGate.businessCard = true;
			//V1.00.02 - 商務卡不用取CardBaseChild
			//ta.selectCardBaseChild();
		}
		//檢查是否為免照會VIP
		if (ta.selectCcaVip(ta.getValue("CardBaseAcctType").trim())) {
			gGate.isAuthVip = true;
			gb.showLogMessage("D","checkCardBase isAuthVip=>" + gGate.isAuthVip);
		}
		return true;
	}
	
	/**
	 * 檢查debit card 卡號是否存在資料庫
	 * @return 如果卡號存在return true,否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkDbcCard() throws Exception {
		ta.selectDbcCard();
		if ( "Y".equals(ta.notFound) ) {/*dsb*/

			//G_Gate.isoField[39] = "05";
			//G_Gate.rejectCode = "C1";
			//ta.getAndSetErrorCode("ERR52"); //Howard:0727 confirmed.
			ta.getAndSetErrorCode("D1");
			gGate.isCardNotExit = true;
			return false;


		}
		return true;
	}
	
	
	/**
	 * 檢核 debit card 參數檔
	 * @return 如果debit card 參數檔有資料return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkDebitParm() throws Exception {
		gGate.debitParmTableFlag = 1; // assign default value

		ta.selectDebitParm(); //get table CCA_DEBIT_PARM (DEBIT卡參數檔)
		if ( "Y".equals(ta.notFound) ) {
			ta.selectDebitParm2(); //get table CCA_DEBIT_PARM2 (Debit卡風險類別限額限次參數)
			if ( "Y".equals(ta.notFound) ) {
				gGate.debitParmTableFlag = 0;
				ta.getAndSetErrorCode("CU");
				return false;
			}
			else {
				gGate.debitParmTableFlag = 3; //CCA_DEBIT_PARM2
			}
		}
		else {		
			ta.selectDebitParm2(); //get table CCA_DEBIT_PARM2 (Debit卡風險類別限額限次參數)
			if ( "Y".equals(ta.notFound) ) {
				gGate.debitParmTableFlag = 1; //CCA_DEBIT_PARM
			}
			else {
				gGate.debitParmTableFlag = 2; //CCA_DEBIT_PARM & CCA_DEBIT_PARM2
			}
		}

		return true;
	}
	
	
	/**
	 * 計算 markup 後的消費金額與調整金額
	 * @throws Exception if any exception occurred
	 */
	private boolean computeTransAmtOfMarkup() throws Exception {
		boolean blResult = false;
		if ("F".equals(gGate.areaType)) { //國外交易才需要做此動作
			//V1.00.04 VD國外交易一律Markup手續費
			//V1.00.07 VD國外交易手續費調整
//			String slCurrCode = new String(gGate.isoField[49]);
//			if ((!"901".equals(slCurrCode))  && (ta.selectCcaCurrentRate(slCurrCode)) ) {
				BigDecimal dlMarkup  = ta.getBigDecimal("DebitParmMarkup");
				BigDecimal dlCashFee = ta.getBigDecimal("DebitParmWithdrawFee");
				BigDecimal dlisoFiled4ValueAfterMarkup = new BigDecimal("0");
				BigDecimal dladjustAmountAfterMarkup = new BigDecimal("0");
				BigDecimal dlisoFiled4Value = new BigDecimal(Double.toString(gGate.isoFiled4Value));
				BigDecimal dladjustAmount = new BigDecimal(Double.toString(gGate.adjustAmount));
				BigDecimal dlPercentage = new BigDecimal("100");
				BigDecimal dlOne = new BigDecimal("1");
				if (!(dlisoFiled4Value.compareTo(BigDecimal.ZERO) == 0)) {
					dlisoFiled4ValueAfterMarkup = dlisoFiled4Value.multiply(dlOne.add(dlMarkup.divide(dlPercentage)));
					if ( (gGate.isDebitCard) && (gGate.cashAdvance) ) {//debit card 預借現金交易要加國外提款手續費
						dlisoFiled4ValueAfterMarkup = (dlisoFiled4ValueAfterMarkup.add(dlCashFee)) ;
					}
					gGate.isoFiled4ValueAfterMarkup = (dlisoFiled4ValueAfterMarkup.setScale(0,BigDecimal.ROUND_HALF_UP)).doubleValue();
					
					blResult = true;
					gb.showLogMessage("D","computeTransAmtOfMarkup() isoFiled4ValueAfterMarkup=" + gGate.isoFiled4ValueAfterMarkup);

				}
				if (!(dladjustAmount.compareTo(BigDecimal.ZERO) == 0)) {
					dladjustAmountAfterMarkup   = dladjustAmount.multiply(dlOne.add(dlMarkup.divide(dlPercentage)));
					if ( (gGate.isDebitCard) && (gGate.cashAdvance) ) {//debit card 預借現金交易要加國外提款手續費
						dladjustAmountAfterMarkup = dladjustAmountAfterMarkup.add(dlCashFee) ;
					}
					gGate.adjustAmountAfterMarkup   = (dladjustAmountAfterMarkup.setScale(0,BigDecimal.ROUND_HALF_UP)).doubleValue();
					blResult = true;
					gb.showLogMessage("D","computeTransAmtOfMarkup() adjustAmountAfterMarkup=" + gGate.adjustAmountAfterMarkup);

				}
//			}
		}
		else {
			blResult = true;
		}
		return blResult;
	}
	
	
	/**
	 * 計算 Egov 手續費後的消費金額與調整金額
	 * V1.00.23 特店編號取得手續費率，除原先4碼符合之外再新增8碼檢查
	 * @throws Exception if any exception occurred
	 */
	private void computeVdMccEgovFee() throws Exception {
		gb.showLogMessage("D","computeVdMccEgovFee() VD交易檢查Egov手續費 mccCode ="+ gGate.mccCode +"merchantNo = "+gGate.merchantNo+"AREA_TYPE = "+gGate.areaType);

		gGate.egovMarkupFee = 0;

		if (gGate.mccCode.length()>0 && gGate.merchantNo.length()>= 8) { 
			String slAreaType = "";
			if ((ta.selectCcaMccEgovFee(gGate.mccCode, gGate.merchantNo.substring(0,8))) ) {}
			else if (ta.selectCcaMccEgovFee(gGate.mccCode, gGate.merchantNo.substring(0,4))) {}
			else {
				return;
			}
			BigDecimal dlFixFee = ta.getBigDecimal("CcaEgovFeeIntFixAmt");
			BigDecimal dlMaxFee = ta.getBigDecimal("CcaEgovFeeIntMaxAmt");
			BigDecimal dlMinFee = ta.getBigDecimal("CcaEgovFeeIntMinAmt");
			BigDecimal dlRate   = ta.getBigDecimal("CcaEgovFeeRate");
			BigDecimal dlMarkupFee = new BigDecimal("0");
			BigDecimal dlPercentage = new BigDecimal("100");
			BigDecimal dlIsoField4Value = new BigDecimal(Double.toString(gGate.isoFiled4Value));
			slAreaType = ta.getValue("CcaEgovFeeCntryCode");
			if (!("0").equals(slAreaType)) {
				if (("2").equals(slAreaType)) {
					if (!("F").equals(gGate.areaType)) {
						return;
					}
				}
				else {
					if (!"T".equals(gGate.areaType)) {
						return;
					}
				}
			}
			if (dlRate.compareTo(BigDecimal.ZERO) == 1) {
				dlMarkupFee = (dlIsoField4Value.multiply(dlRate.divide(dlPercentage)).add(dlFixFee));
			}
			else {
				dlMarkupFee =  dlFixFee;
			}
			
			if (dlMarkupFee.compareTo(dlMaxFee) == 1) {
				gGate.egovMarkupFee =  dlMaxFee.doubleValue();
			}
			else 
				if (dlMarkupFee.compareTo(dlMinFee) == -1) {
				gGate.egovMarkupFee =  dlMinFee.doubleValue();
			}
			else {
				gGate.egovMarkupFee = (dlMarkupFee.setScale(0,BigDecimal.ROUND_HALF_UP)).doubleValue();
			}
		}
	}
	
	
	/**
	 * 檢查credit card 卡號是否存在資料庫
	 * @return 如果卡號存在return true,否則return false
	 * @throws Exception if any exception occurred
	 */

	private boolean checkCrdCard() throws Exception {
		ta.selectCrdCard();
		if ( "Y".equals(ta.notFound) ) {

			//G_Gate.isoField[39] = "05";
			//G_Gate.rejectCode = "C1";
			//ta.getAndSetErrorCode("ERR52"); //Howard:0727 confirmed.
			ta.getAndSetErrorCode("D1");
			gGate.isCardNotExit = true;
			return false;


		}
		return true;
	}


	/**
	 * 檢核授權參數檔
	 * @return 如果授權參數檔有資料return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkAuthParm() throws Exception {
		ta.selectAuthParm();
		if ( "Y".equals(ta.notFound) ) {

			
			//ta.getAndSetErrorCode("ERR90");
			ta.getAndSetErrorCode("CQ");
			return false;


		}
		if ("1".equals(ta.getValue("AuthParmOpenChk")))
			gGate.ifCheckOpeningCard= true;
		else
			gGate.ifCheckOpeningCard= false;

		return true;
	}
	
	
	/**
	 * 檢查CARD TYPE 是否存在資料庫
	 * @return 如果CARD TYPE存在return true,否則return false
	 * @throws Exception if any exception occurred
	 */

	private boolean checkPtrCardType() throws Exception {
		ta.selectPtrCardType();
		if ( "Y".equals(ta.notFound) ) {
			ta.getAndSetErrorCode("EB"); 
			return false;
		}
		return true;
	}

	
	/**
	 * 判斷是否為虛擬卡
	 * @return 若為虛擬卡 return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean ifVirtualCard() throws Exception {
		boolean blResult = false;
		if ("Y".equals(ta.getValue("VIRTUAL_FLAG").trim())) {
			blResult = true;
		}
		else {
			blResult = false;
		}

		return blResult;
	}
	
	/**
	 * 判斷是否為採購卡
	 * @return 若為採購卡 return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean ifPurchaseCard() throws Exception {
		boolean blResult = false;
		ta.selectPtrGroupCode();
		
		if ("Y".equals(ta.getValue("PURCHASE_CARD_FLAG").trim())) {
			blResult = true;
		}
		else {
			blResult = false;
		}
		return blResult;
	}
	
	
	/**
	 * 檢核卡片狀態
	 * V1.00.46 P3批次授權比照一般授權的邏輯，不須特別排除
	 * @return 如果卡片狀態正常return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCardStatus() throws Exception {
		boolean blResult = true;
		if (ifIgnoreProcess4()) {
			return true;
		}

		String slCurrentCode = ta.getValue("CURRENT_CODE").trim();

		gb.showLogMessage("D","傳入 Current Code=>" + slCurrentCode);

		if ("0".equals(slCurrentCode)) {

			gb.showLogMessage("D","Current Code=>" + slCurrentCode +"，卡況正常");

			blResult = true;
		}
		else {
			if ("1".equals(slCurrentCode)) {
				ta.getAndSetErrorCode("Q5");//該卡已申請停用
			}
			else if ("2".equals(slCurrentCode))
				//ta.getAndSetErrorCode("ERR09");
				ta.getAndSetErrorCode("P1");//掛失停用－遺失
			else if ("3".equals(slCurrentCode))
				//ta.getAndSetErrorCode("ERR10");
				ta.getAndSetErrorCode("P3");//強制停用
			else if ("4".equals(slCurrentCode))
				//ta.getAndSetErrorCode("ERR11");
				ta.getAndSetErrorCode("QD");//其他停用
			else if ("5".equals(slCurrentCode))
				//ta.getAndSetErrorCode("ERR12");
				ta.getAndSetErrorCode("P4");//偽卡
			else if ("6".equals(slCurrentCode))
				//ta.getAndSetErrorCode("ERR13");
				ta.getAndSetErrorCode("Q8");//卡戶凍結:禁止任何消費
			else
				//ta.getAndSetErrorCode("ERR11");
				ta.getAndSetErrorCode("QD");//其他停用
			blResult = false;
		}

		return blResult;
	}
	
	
	/*卡片有效日期檢核*/
	/**
	 * 卡片有效日期檢核
	 * V1.00.32	OEMPAY通知交易未帶入效期，所以不需判定卡片是否開卡
	 * V1.00.41 P3新卡已開卡後，仍舊使用舊卡消費，系統一律拒絕
	 * V1.00.50 經常性身分驗證交易不須檢查到期日與開卡註記
	 * V1.00.65 批次授權不檢查效期及開卡;一卡通自動加值狀態查詢比照自動加值使用系統效期
	 * @return 卡片有效日期檢核成功return true，否則return false 
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCardExpireDate() {
		/*1:有效日期過期*/
		/*2:有效日期與檔案有效日期不同*/
		//kevin:電子發票沒有帶入真正的有效期0000，所以不檢查
		//kevin:MasterCard OEMPAY通知交易未帶效期，所以不檢查
		//V1.00.03 kevin:VISA OEMPAY通知交易未帶效期，所以不檢查
		//V1.00.08 授權補登交易取消設定，同預先授權完成交易
		if (ifIgnoreProcess5()) { 
			return true;
		}
		//V1.00.06 由於票證交易未能帶正確的信用卡效期，因此直接在此取得卡片最新效期，提供系統檢查信用卡是否過期。德盛科長說只有一卡通自動加值才需要
		if (gGate.ipassAutoload || gGate.ipassAutoloadChk) {
			gb.showLogMessage("D","ticket txn card expire date = "+ gGate.expireDate +";crd card exipre date = "+ta.getValue("NEW_END_DATE").substring(2,6));
			gGate.expireDate = ta.getValue("NEW_END_DATE").substring(2,6);
		}

		gGate.effDateEnd = ta.getValue("NEW_END_DATE");
		String slNewEndDateYM = ta.getValue("NEW_END_DATE").substring(0,6);
		String slCardExpireDateYM = "20"+gGate.expireDate; //ex: will be 201812
		//down, 判斷是不是新卡

		gb.showLogMessage("D","1. card expire date = "+ slCardExpireDateYM +";crd card exipre date = "+slNewEndDateYM);

		if (slCardExpireDateYM.equals(slNewEndDateYM))
			gGate.IsNewCard = true;
		else
			gGate.IsNewCard = false;
		//up, 判斷是不是新卡

		// 卡片上之有效期<今天(過期卡)
		if ( slCardExpireDateYM.compareTo(gb.getSysDate().substring(0,6)) < 0 ) {
			ta.getAndSetErrorCode("E1");
			return false; 
		}

		gb.showLogMessage("D","2. card expire date = "+ slCardExpireDateYM +";crd card exipre date = "+slNewEndDateYM);

		//Check ISO effect date is NEW card effect date or OLD card effect date
		//MCC=4814,4816,6300 and entry=01 則比較月份 => let bL_SpecialCond=true 
//		boolean blSpecialCond=false;
//		if (("01".equals(gGate.entryMode.substring(0, 2))) && (("4814".equals(gGate.mccCode)) || ("4816".equals(gGate.mccCode)) || ("6300".equals(gGate.mccCode)))) {
//			blSpecialCond=true;
//		}

		if (gGate.IsNewCard) {
			if (!slCardExpireDateYM.equals(slNewEndDateYM)) {

				gb.showLogMessage("D","3. card expire date = "+ slCardExpireDateYM +";crd card exipre date = "+slNewEndDateYM);

				ta.getAndSetErrorCode("E1"); //卡片上之有效期<CardBase(過期卡)
				return false;

			}
			else {
				gb.showLogMessage("D","傳入卡號=>" + gGate.cardNoMask+"; 傳入卡片效期=>" + slCardExpireDateYM+"; 資料庫卡片效期=>" + slNewEndDateYM+"; 兩者效期一致，效期正確");
			}
		}
		else { //舊卡
			String slTmp = ta.getValue("OLD_END_DATE");
			gGate.effDateEnd = slTmp;
			if ("".equals(slTmp)) {

				gb.showLogMessage("D","4. card expire date = "+ slCardExpireDateYM +";crd card exipre date old = "+slTmp);

				ta.getAndSetErrorCode("E1");
				return false;
			}
			String slOldEndDateYM = ta.getValue("OLD_END_DATE").substring(0,6);

//			if (("BATCH".equals(gGate.connType)) && (blSpecialCond)) { //MCC=4814,4816,6300 and entry=01 h
//				///**** 批次授權只檢核月份  ***/
//				//ISO效期月份<>舊卡效期月份
//				//proc is batch_effdate_chk => 檢核月份是否正確
//				String slDbOldMonth = slOldEndDateYM.substring(4, 6);
//				String slCardOldMonth = slCardExpireDateYM.substring(4, 6);
//				if (!slDbOldMonth.equals(slCardOldMonth))
//
//					gb.showLogMessage("D","5. card expire date old  = "+ slDbOldMonth +";crd card exipre date old = "+slCardOldMonth);
//
//					ta.getAndSetErrorCode("E1");
//				return false;
//			}
//			else {
				if (!slCardExpireDateYM.equals(slOldEndDateYM)) {
					ta.getAndSetErrorCode("E3");
					return false;
				}
//			}
			//V1.00.41 P3新卡已開卡後，仍舊使用舊卡消費，系統一律拒絕
			if ("2".equals(ta.getValue("ACTIVATE_FLAG"))) {
				ta.getAndSetErrorCode("E7");
				return false;
			}
		}
		return true;
	}
	
	
	/*卡片開卡檢核*/ 
	/**
	 * 卡片開卡檢核
	 * V1.00.32	OEMPAY通知交易未帶入效期，所以不需判定卡片是否開卡
	 * V1.00.41 P3新卡已開卡後，仍舊使用舊卡消費，系統一律拒絕
	 * V1.00.65 批次授權不檢查效期及開卡;一卡通自動加值狀態查詢比照自動加值使用系統效期
	 * @param npOperationCode 1:交易檢查是否已開卡，2:語音開卡檢查是否未開卡
	 * @return 如果開卡狀態查核成功return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCardOpeningStatus(int npOperationCode) throws Exception {
		boolean blResult = true;

		if (ifIgnoreProcess5() 
				|| (gGate.connType.equals("BATCH") && (ta.getValue("OLD_END_DATE").length()>0 || "2".equals(ta.getValue("REISSUE_REASON"))))) { //批次授權有續卡或毀補過不檢查開卡
			return true;
		}

		if ("0".equals(ta.getValue("AuthParmOpenChk"))) //不需檢核開卡
			return true;

		if (!ta.selectAuthActive()) {//不需檢核開卡
			return true;
		}
		if (gGate.IsNewCard) { //新卡
			if (npOperationCode==1) {//交易檢查是否已開卡
				if (!"2".equals(ta.getValue("ACTIVATE_FLAG"))) { //1:關閉 2:開卡
					//V1.00.09 一卡通自動加值回應未開卡的問題
					if (gGate.ipassAutoload || gGate.ipassAutoloadChk) {
						if (ta.getValue("OLD_END_DATE").compareTo(gb.getSysDate()) >= 0
							&& "2".equals(ta.getValue("OLD_ACTIVATE_FLAG"))) {
							gGate.expireDate = ta.getValue("OLD_END_DATE").substring(2, 6);
							return true;
						}
					}
					ta.getAndSetErrorCode("E4");
					//未開卡簡訊取消
//					sendSms(4); 
					return false;
				}
				else {
						gb.showLogMessage("D","傳入Card No=>" + gGate.cardNoMask);
						gb.showLogMessage("D","資料庫ACTIVATE_FLAG 等於" + ta.getValue("ACTIVATE_FLAG") + "，表示已經開卡");
				}
				blResult = true;
			}
			else { //新卡語音開卡
				if ("2".equals(ta.getValue("ACTIVATE_FLAG"))) { //1:關閉 2:開卡
					if (!gGate.atmCardOpen) {
						ta.getAndSetErrorCode("E5");
						return false;
					}
					
					gb.showLogMessage("D","ATM COMBO卡啟用，系統顯示新卡已開卡");

					gGate.isAtmCardActivated = true;
				}
				blResult = true;
			}
		}
		else { //舊卡
			if (npOperationCode==1) {//交易檢查是否已開卡
				if (!"2".equals(ta.getValue("OLD_ACTIVATE_FLAG"))) { //1:關閉 2:開卡
					ta.getAndSetErrorCode("E4");
					//未開卡簡訊取消
//					sendSms(4);
					return false;
				}
				blResult = true;
			}
			else { //舊卡語音開卡
				if ("2".equals(ta.getValue("OLD_ACTIVATE_FLAG"))) { //1:關閉 2:開卡
					if (!gGate.atmCardOpen) {
						ta.getAndSetErrorCode("E5");
						return false;
					}

					gb.showLogMessage("D","ATM COMBO卡啟用，系統顯示舊卡已開卡");

					gGate.isAtmCardActivated = true;
				}
				blResult = true;
			}
		}
		return blResult ;
	}

}
