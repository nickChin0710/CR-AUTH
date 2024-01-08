/**
 * 授權邏輯查核-帳戶、卡片基本資料檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-帳戶、卡片基本資料檢核處理             *
 * 2021/12/24  V1.00.01  Kevin       針對POS ENTRY MODE 91，一律拒絕交易           *
 * 2022/01/05  V1.00.02  Kevin       臨調公司戶調整                               *
 * 2022/01/19  V1.00.03  Kevin       風險行業別6211管制取消，因合庫無此需求            *
 * 2022/03/23  V1.00.04  Kevin       免照會VIP不須檢查風險特店                      *
 * 2022/04/20  V1.00.05  Kevin       查核HSM排除特定交易                          *
 * 2022/04/21  V1.00.06  Kevin       免照會VIP排除風險國家、Fallback交易檢查          *
 * 2022/05/26  V1.00.07  Kevin       交易類別28xxxx屬於Payment Transaction(PY)   *
 *                                   一律拒絕交易。                               *
 * 2022/07/07  V1.00.08  Kevin       停用、特指、凍結的卡片可以沖銷交易                *
 * 2023/02/01  V1.00.35  Kevin       修改限定MCC消費之採購卡及商務卡可使用語音開卡         *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/08/04  V1.00.49  Kevin       風險特店調整及新增特殊特店名稱檢查(eToro)           *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckTransBasicInfo extends AuthLogic {
	
	public LogicCheckTransBasicInfo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckTransBasicInfo : started");

	}
	
	/**
	 * 各種交易基本資訊檢核
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * @return 如果通過檢核，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkTransBasicInfo() throws Exception {
		//Load Consume前，先取得TXLOG AMOUNT DATE，確保涵蓋已授權的範圍。授權累積金額的指定日期(第一階段)
		if (ta.selectTxlogAmtDate() && gGate.rollbackP2) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); 
			gGate.txlogAmtDate = sdf.format(HpeUtil.addDays(sdf.parse(ta.getValue("PtrSysParmTxlogAmtDate")),1));  
		}
		else {
			gGate.txlogAmtDate = HpeUtil.getCurDateStr(false);
		}
		LogicCheckBasicCardInfo logicCard = new LogicCheckBasicCardInfo(gb, gGate, ta);
		if ( !logicCard.checkBasicCardInfo() ){          
			//kevin:因卡片檢查錯誤時，沒有ccaconsume的資料可以處裡。
			if (gGate.cardAcctIdx.length() > 0) {
				ta.loadCcaConsume(gGate.cardAcctIdx, 1); //PROC LINE 9478 (TB_auth_consume) =>讀取卡戶帳務檔
			}
			return false;
		}
		if ( !checkCardIdNo() )  {       // 查核 CRD_IDNO
			//kevin:因卡片檢查錯誤時，沒有ccaconsume的資料可以處裡。
			ta.loadCcaConsume(gGate.cardAcctIdx, 1); //PROC LINE 9478 (TB_auth_consume) =>讀取卡戶帳務檔
			return false;
		}

		if ( !checkCardAcct() ){         //查核 CCA_CARD_ACCT
			return false;
		}

		//down,要在 checkCardAcct() 之後執行，取得臨調資訊並判斷是否為臨調期間，管制高風險交易
		if (!computeTempAdjInfo()) {
			return false;
		}
		
		if ( !checkPurchaseCard()) {
			return false;
		}

		if (gGate.isDebitCard) {
			if ( !checkDbaAcno() ) {        // DBA_ACNO
				return false;
			}
		}
		else{
			if ( !checkActAcno() ) {        // ACT_ACNO
				return false;
			}
			ta.getClassCode();//o G_Gate.ClassCode value => b checkCardAcct() and checkActAcno() 
			if (!ta.selectRiskLevelParm()) { //Howard: 需在 ta.getClassCode() 之後執行 
				return false;
			}
		}

		ta.getMCode();//o G_Gate.MCode value => b checkCardAcct() and checkActAcno() 

		if ( !commonCheck() )   {      
			return false;
		}
		//查核 CCS_COUNTRY 風險國家
		//kevin: riskfactor
		if ( !checkCountry()  ){        
			ta.getAndSetErrorCode("Q0"); //checkCounty() 裡面不須處理拒絕回覆碼
			return false;
		}

		if ( !rocCheck() ) {        // IC CARD Reason Online Code check
			return false;
		}
//		if (!checkMchtInfo()) {    //查核風險特店 old
		LogicCheckMchtRisk logicMchtRisk = new LogicCheckMchtRisk(gb, gGate, ta);
		if (!logicMchtRisk.checkMchtRisk()) {    //查核風險特店
			return false;
		}

		if (!gGate.reversalTrans) {
			if ( !checkMccRisk()  )  {  // 查核 CCS_MCC_RISK 風險行業 
				return false;
			}
		}

		//-涷結碼-
		if (!checkBlockCode()) {
			return false;
		}

//		if (!gGate.reversalTrans) {        // 排除沖正交易
		if (!ifIgnoreProcess4()) {          //V1.00.05 查核HSM排除特定交易
			LogicCheckHsmData logicHsm = new LogicCheckHsmData(gb, gGate, ta);
			if ( !logicHsm.checkHsmData() ) {       // 查核HSM檢驗結果
				return false;
			}
		}

		setCorpActFlag();
		resetTradeRecord();

		if (gGate.rollbackP2) { //V1.00.38 P3授權額度查核調整
			computeAvailableAmt(); 
			computeBaseNumber();
		}
		else {
			LogicGetOtbAmount logicGetOtb = new LogicGetOtbAmount(gb, gGate, ta);
			logicGetOtb.computeOtb();
			logicGetOtb.computeBusinessCardOtb();
		}
		return true;
	}
	
	/**
	 * 檢核卡人資料檔
	 * @return 如果找到卡人資料return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCardIdNo() throws Exception {

		gb.showLogMessage("I","checkCardIdNo : started");

		ta.selectCardIdNo();
		if ( "Y".equals(ta.notFound) ){ /* CRD_IDNO sb */
			//G_Gate.isoField[39] = "05"; G_Gate.rejectCode = "I1";
			//ta.getAndSetErrorCode("ERR06"); //Howard:0727 confirmed.
			ta.getAndSetErrorCode("Q7");
			return false;
		}
		gGate.idNo = ta.getValue("CrdIdNoIdNo");
		return true;
	}
	
	/**
	 * 查核授權卡戶基本檔
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * @return 如果授權卡戶基本檔可以找到資料，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCardAcct() throws Exception {

		gb.showLogMessage("I","checkCardAcct : started");


		//if ( !ta.loadCardAcct_ibt() ) {
		if ( !ta.loadCcaCardAcct() ) { //取得授權卡戶基本資料

			ta.getAndSetErrorCode("Q7");
			return false;
		}

		//down,select 公司資料
		if (gGate.businessCard) {
			ta.loadCcaCardAcctOfCompany(ta.getValue("CardAcctCardAcctType"), ta.getValue("CardAcctCardCorpPSeqno")); //取得公司的臨調資料
		}

		ta.loadCcaConsume(gGate.cardAcctIdx, 1); //讀取卡戶帳務檔
		if (gGate.rollbackP2) { //V1.00.38 P3授權額度查核調整
			gGate.curTotalUnpaidOfPersonal = getCurTotalUnpaidOfPersonal(); //必須在 loadCcaConsume() 之後 run
		}	
		if (!"".equals(gGate.cardAcctIdxOfComp)) {
			ta.loadCcaConsume(gGate.cardAcctIdxOfComp, 2);
			if (gGate.rollbackP2) { //V1.00.38 P3授權額度查核調整
				gGate.curTotalUnpaidOfComp = getCurTotalUnpaidOfComp(); //必須在 loadCcaConsume() 之後 run
			}
		}

		return true;
	}
	
	/*
	 * 判斷是否有臨調
	 */
	private boolean computeTempAdjInfo() throws Exception{
		
		gGate.bgHasPersonalAdj = false;
		gGate.bgHasCompAdj = false;

		gGate.bgHasPersonalAdj= checkAcctAdj(1);//檢核個人是否有臨調

		if (gGate.businessCard) {
			gGate.bgHasCompAdj = checkAcctAdj(2);//檢核公司是否有臨調, proc is check_acct_adj(2,pWA)    		
		}
		
		if (gGate.bgHasPersonalAdj)	{	
			//(4)臨調高風險管制			 
			if ("Y".equals(ta.getValue("CardAcctAdjRiskFlag")) && ta.selectHighRiskType()) {
				ta.getAndSetErrorCode("D6");
				return false;            
			} 
		}
		else {
			if (gGate.bgHasCompAdj) {
				if ("Y".equals(ta.getValue("CardAcctAdjRiskFlagOfComp")) && ta.selectHighRiskType()) {
					ta.getAndSetErrorCode("D6");
					return false;            
				} 
			}
		}
		return true;
	}
	
	/*
	 * 判斷是否有為採購卡
	 * V1.00.35 修改限定MCC消費之採購卡及商務卡可使用語音開卡
	 * @return DF採購配銷卡在指定特店消費，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkPurchaseCard() throws Exception {

		boolean blResult=true;
		gb.showLogMessage("I","checkPurchaseCard : started");
		if (gGate.txVoice) { //限定MCC消費之採購卡及商務卡可使用語音開卡
			return true;
		}
		if (gGate.isPurchaseCard) {
			int nlResult = ta.selectCcaGroupMcht(ta.getValue("CCA_GROUP_MCHT_CHK"));
			if (nlResult==-1) {
				ta.getAndSetErrorCode("DF"); /*DF採購配銷卡非在指定特店消費*/
				blResult=false;
			}
			else if (nlResult==1) {
				ta.getAndSetErrorCode("CV"); //CV採購卡未設定使用範圍
				blResult=false;
			}
		}
		return blResult;
	}
	
	/**
	 * 檢核 debit card 帳戶基本資料主檔 
	 * @return 如果找到帳戶主檔return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkDbaAcno() throws Exception {

		gb.showLogMessage("I","checkDbaAcno : started");

		boolean blResult = true;

		if (!ta.loadDbaAcno()) {
			ta.getAndSetErrorCode("1A"); //Howard(20190530) : 舊系統無此 error code，應該是不會發生此狀況
			blResult = false;
		}
		else {
			String slDbaAcnoCorpActFlag = ta.getValue("DbaAcnoCorpActFlag");
			//sL_DbaAcnoCorpActFlag="3";//for test			
			if (("3".equals(slDbaAcnoCorpActFlag)) || ("Y".equals(slDbaAcnoCorpActFlag)) ) {
				//表示此卡為商務卡， 需要比對該公司的總額度，所以先找出該卡所屬的公司帳戶資料
				if (!ta.loadDbaAcnoOfCompany(ta.getValue("DbaAcnoAcctType"), ta.getValue("DbaAcnoCorpPSeqno"))) {
					ta.getAndSetErrorCode("1A"); //Howard(20190530) : 舊系統無此 error code，應該是不會發生此狀況
					blResult = false;
				}
			}
		}
		return blResult;
	}
	
	/**
	 * 檢核credit 帳戶基本資料主檔 
	 * @return 如果找到帳戶主檔return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkActAcno() throws Exception {

		gb.showLogMessage("I","checkActAcno : started");

		boolean blResult = true;

		if (!ta.loadActAcno()) {
			ta.getAndSetErrorCode("1A"); 
			blResult = false;
		}
		else {
			if (gGate.businessCard) {
				//表示此卡為商務卡， 需要比對該公司的總額度，所以先找出該卡所屬的公司帳戶資料
				if (!ta.loadActAcnoOfCompany(ta.getValue("ActAcnoAcctType"), ta.getValue("ActAcnoCorpPSeqno"), ta.getValue("CardBasePSeqNo"))) {
					ta.getAndSetErrorCode("1A"); 
					blResult = false;
				}
			}
		}
		return blResult;
	}
	
	/**
	 * 基本交易資料檢核
	 * @return 如果檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred 
	 */
	private boolean commonCheck() throws Exception {
		
		gb.showLogMessage("I","commonCheck : started");

		if ("A".equals(ta.getValue("PtrAcctTypeCardIndicator").trim())) { //一般卡必須有風險等級	
			if ("".equals(gGate.classCode)) { //卡戶無風險等級
				//ta.getAndSetErrorCode("ERR07");
				ta.getAndSetErrorCode("BA");
				return false;
			}
		}

		if (gGate.isDebitCard) {
			if (("6010".equals(gGate.mccCode)) || ("6011".equals(gGate.mccCode))) {
				if ("T".equals(gGate.areaType)) {//國內交易
					ta.getAndSetErrorCode("1D");//VISA DEBIT 禁止國內預現及分期交易
					return false;
				}
			}
		}

//		if ( ("6".equals(gGate.eci)) && ("1".equals(gGate.ucafInd)) ) {
//			ta.getAndSetErrorCode("YY"); //3D Secure特店未走ACS認證 //Howard: 無此 resp code
//			return false;
//		}
		
		//V1.00.01 針對POS ENTRY MODE 91，一律拒絕交易
		if (gGate.contactLessByMagnetic) {
			if (gGate.isAuthVip) {
				gb.showLogMessage("D","Auth Vip bypass contactLessByMagnetic reject");
				return true;
			}
			ta.getAndSetErrorCode("1J");
			return false;
		}
		//V1.00.09  交易類別28xxxx屬於Payment Transaction(PY)一律拒絕交易
		if (gGate.paymentTxn) {
			ta.getAndSetErrorCode("BX");
			return false;
		}

		return true;
	}
	
	/**
	 * 查核是否為拒絕交易國家
	 * kevin: 取得風險分數 riskFactor by Alex
	 * @return 如果刷卡國家不是拒絕交易國家，則return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkCountry() throws Exception {

		gb.showLogMessage("I","checkCountry : started : riskFctorInd=3");
        
		boolean blResult = true;
		if ( !ta.selectCountry() ) {
			//交易國家別不存在系統，將放入不放風險係數
			gGate.riskFctorInd = 3; //kevin: 3. 計算 國別  風險分數
			blResult = true;//不存在參數檔; 交易正常
		}
		else {
			gGate.riskFctorInd = 3; //kevin: 3. 計算 國別  風險分數
			gGate.countryRiskFactor = ta.getDouble("CountryRiskFactor");
			if ( "Y".equals(ta.getValue("CountryRejCode")) )
			{
				blResult = false;//存在參數檔; 且REJECT_FLAG='Y' => 拒絕交易
			}
			else
				blResult = true;
		}
		
		if (!blResult) {
			if (gGate.isAuthVip) {
				gb.showLogMessage("D","Auth Vip bypass Risk Country reject");
				blResult = true;
			}
		}
		
		return blResult;

	}

	
	/**
	 * IC CARD Reason Online Code check
	 * @return 檢核成功return true，否則return false 
	 * @throws Exception if any exception occurred
	 */
	private boolean rocCheck() throws Exception {
		boolean blResult = true;

		gGate.fallback = "N";
		String slTmpX01 = "";
		if (gGate.isoField[35].length()>=23)
			slTmpX01 =gGate.isoField[35].substring(22, 23);
		
		String slIsoField22 =  gGate.isoField[22].trim();
		if ("".equals(slIsoField22))
			return true;
			
		//kevin:NCCC FallBack
		if (   ((("02".equals(slIsoField22.substring(0, 2)))) || (("90".equals(slIsoField22.substring(0, 2)))))   && 
				(   ("2".equals(slTmpX01)) || ("6".equals(slTmpX01))      )  && ("5".equals(gGate.emvDFEE)) && (!"FISC".equals(gGate.connType))) {
			gGate.fallback="Y";
		}
		//kevin:財金FallBack交易定義
		if ("80".equals(slIsoField22.substring(0, 2)) && "FISC".equals(gGate.connType)) {
			gGate.fallback="Y";
		}
		
		if ("Y".equals(gGate.fallback)) {
			//kevin:classcode卡人等級未確定所以暫不檢查
			if (   ("T".equals(gGate.areaType)) || (gGate.isoFiled4Value<1000)) {
				blResult = true;
			}
			else {
				//ta.getAndSetErrorCode("FB1504");
//				ta.getAndSetErrorCode("4R");
				blResult = false;
			}
		}
		if (!blResult) {
			if (gGate.isAuthVip) {
				gb.showLogMessage("D","Auth Vip bypass Fallback (txnamt >1000 or oversea) reject");
				blResult = true;
			}
			else {
				ta.getAndSetErrorCode("4R");
			}
		}
		return blResult;
	}

	/**
	 * 檢查MCC code是否存在，並取得RISK_TYPE
	 * V1.00.03 風險行業別6211管制取消，因合庫無此需求
	 * V1.00.49 風險特店調整及新增特殊特店名稱檢查(eToro)
	 * @return 如果成功取得，return true，否則return false
	 * @throws Exception in any exception occurred 
	 */
	private boolean checkMccRisk() throws Exception {
		gb.showLogMessage("I","checkMccRisk : started");
		if ("6211".equals(gGate.mccCode)) {
			if (!gGate.isDebitCard) {
				if (HpeUtil.matchString(gGate.merchantName.toUpperCase(), "ETORO")) {
					ta.getAndSetErrorCode("ZF");
					gb.showLogMessage("D","checkMccRisk MATCH MCC : "+ gGate.mccCode +"& Merchant ： eToro");
				}
			}
		}
		return true;
	}

	
	/**
	 * 檢核 block code
	 * @return 如果檢核通過，則return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkBlockCode() throws Exception {
		if (ifIgnoreProcess4()) { //停用、特指、凍結的卡片可以沖銷交易
			return true;
		}

		if (ta.isBlocked()) {
			//ta.getAndSetErrorCode("ERR13"); //Howard:0727 confirmed.
			ta.getAndSetErrorCode("Q8"); //Howard:0727 confirmed.
			return false;
		}

		return true;
	}
	
	/**
	 * 判定交易是  一般(個人卡，個繳)、 總繳公司 、商務個繳(商務卡，個繳) 還是 總繳個人(商務卡，公司總繳)
	 */
	private void setCorpActFlag() {
		//一定要在 checkDbaAcno() 與 checkActAcno() 之後執行
		String slCorpActFlag="";
		if (gGate.isDebitCard) {
			slCorpActFlag = ta.getValue("DbaAcnoCorpActFlag").trim();
		}
		else {
			slCorpActFlag = ta.getValue("ActAcnoCorpActFlag").trim();
		}

		gGate.corpActFlag =   slCorpActFlag; //Y:總繳, N:個繳
		//G_Gate.corpActFlag =   sL_CorpActFlag; //"1":一般(個人卡，個繳) or "2":總繳公司() or "3":商務個繳(商務卡，個繳) or "Y":總繳個人(商務卡，公司總繳)
	}
	
	/**
	 * 初始化交易數據
	 * @throws Exception if any exception occurred
	 */
	private void resetTradeRecord() throws Exception{
		try {
			//kevin: 最後消費日期錯誤修正
			String slLastConsumeDate = gGate.lastTxDate;//最後消費日期, //YYYYMMDD 
			String slCurSyDate = HpeUtil.getCurDateStr(false); //系統日期  //YYYYMMDD

			if  (("".equals(slLastConsumeDate)) || (!slCurSyDate.equals(slLastConsumeDate))) { //年月日不同  => 清卡戶檔  "日" 累計額
				gGate.ccaConsumeTxTotAmtDay = 0;//累積日消費額
				gGate.ccaConsumeTxTotCntDay = 0;//累積日消費次數
				gGate.ccaConsumeFnTotAmtDay=0;//國外一般消費日總額
				gGate.ccaConsumeFnTotCntDay=0; //國外一般消費日總次
				gGate.ccaConsumeFcTotAmtDay=0;//國外預借現金日總額
				gGate.ccaConsumeFcTotCntDay=0; //國外預借現金日總次
				gGate.ccaConsumeTrainTotAmtDay =0;//高鐵累積日消費額
				gGate.ccaConsumeRejAuthCntDay = 0; //累積日拒絕交易次數

				//kevin:mark totAmtConsume、totAmtPreCash避免換日後，第一筆未計算到cardlink及day1、day2已授權未請額款金額
				gGate.ccaConsumeTxTotAmtMonth = 0;
				gGate.ccaConsumeTxTotCntMonth = 0;
				

				gGate.ccaConsumeTxTotAmtDayOfComp=0; //累積日消費額
				gGate.ccaConsumeTxTotCntDayOfComp=0; //累積日消費次數
				gGate.ccaConsumeFnTotAmtDayOfComp=0;//國外一般消費日總額
				gGate.ccaConsumeFnTotCntDayOfComp=0; //國外一般消費日總次
				gGate.ccaConsumeFcTotAmtDayOfComp=0;//國外預借現金日總額
				gGate.ccaConsumeFcTotCntDayOfComp=0; //國外預借現金日總次
				gGate.ccaConsumeTrainTotAmtDayOfComp=0;//高鐵累積日消費額
				gGate.ccaConsumeRejAuthCntDayOfComp=0; //累積日拒絕交易次數

				//kevin:mark totAmtConsumeOfComp、totAmtPreCashOfComp避免換日後，第一筆未計算到cardlink及day1、day2已授權未請額款金額
				gGate.ccaConsumeTxTotAmtMonthOfComp= 0;
				gGate.ccaConsumeTxTotCntMonthOfComp= 0;

				gGate.cardBaseTotAmtDay=0; //日累積消費金額
				gGate.cardBaseTotCntDay=0;//日累積消費次數

			}

			if (("".equals(slLastConsumeDate)) || (!slCurSyDate.substring(0, 6).equals(slLastConsumeDate.substring(0, 6)))) { //年月不同  => 清卡戶檔  "月" 累計額
				gGate.ccaConsumeTxTotAmtMonth=0;//累積月消費額
				gGate.ccaConsumeRejAuthCntMonth=0;//累積月拒絕交易次數
				gGate.ccaConsumeTxTotCntMonth=0;//累積月消費次數
				gGate.ccaConsumeFnTotAmtMonth=0;//國外一般消費月總額
				gGate.ccaConsumeFnTotCntMonth=0; //國外一般消費月總次
				gGate.ccaConsumeFcTotAmtMonth=0;//國外預借現金月總額
				gGate.ccaConsumeFcTotCntMonth=0; //國外預借現金月總次
				gGate.ccaConsumeTrainTotAmtMonth =0;//高鐵累積月消費額

				gGate.ccaConsumeTxTotAmtMonthOfComp=0; //累積月消費額
				gGate.ccaConsumeTxTotCntMonthOfComp=0; //累積月消費次數
				gGate.ccaConsumeRejAuthCntMonthOfComp=0;//累積月拒絕交易次數
				gGate.ccaConsumeFnTotAmtMonthOfComp=0; //國外一般消費月總額
				gGate.ccaConsumeFnTotCntMonthOfComp=0; //國外一般消費月總次
				gGate.ccaConsumeFcTotAmtMonthOfComp=0; //國外預借現金月總額
				gGate.ccaConsumeFcTotCntMonthOfComp=0; //國外預借現金月總次
				gGate.ccaConsumeTrainTotAmtMonthOfComp=0; //高鐵累積月消費額

			}

		} catch (Exception ex) {
			// TODO: handle exception
			throw ex;
		}
	}

	
	/**
	 * 計算永調、臨調 以及 比對 CCA_RISK_LEVEL_PARM 後的可用額度 		
	 */
	private boolean computeAvailableAmt()  throws Exception {
		double dlRealBaseLimit = 0;
		double dlRealBaseLimitComp = 0;
		boolean blCheckRiskLevel=true;
		if ((gGate.bgHasPersonalAdj) || (gGate.bgHasCompAdj)) {
			if (gGate.bgHasPersonalAdj) {
				if ( (gGate.isVirtualCard) &&  (gGate.isInstallmentTx) ){
					dlRealBaseLimit = Double.parseDouble(ta.getValue("CardAcctAdjInstPct"));
				}
				else {
					dlRealBaseLimit = gGate.cardAcctTotAmtMonth;  
				}
	
				blCheckRiskLevel=false;
				if (!gGate.bgHasCompAdj) {
					dlRealBaseLimitComp = ta.getBaseLimitOfComp();
				}
				
			}
			if (gGate.bgHasCompAdj) {
				if ( (gGate.isVirtualCard) &&  (gGate.isInstallmentTx) ){
					dlRealBaseLimitComp = Double.parseDouble(ta.getValue("CardAcctAdjInstPctOfComp"));
				}
				else {
					dlRealBaseLimitComp = gGate.cardAcctTotAmtMonthOfComp;
				}
				blCheckRiskLevel=false;
				if (!gGate.bgHasPersonalAdj) {
					dlRealBaseLimit = ta.getBaseLimit();
				}
			}
		}
		else {
			blCheckRiskLevel=true;
			if (gGate.businessCard) {
				dlRealBaseLimitComp = ta.getBaseLimitOfComp();
			}
			dlRealBaseLimit = ta.getBaseLimit();
		}

		if ("F".equals(gGate.areaType)) {
			if (gGate.businessCard) {
				gGate.cashBaseOfComp = dlRealBaseLimitComp* ta.getDouble("AuthParmOverseaCashPct")/100; //該戶之國外預借現金額度。AuthParmOverseaCashPct => 國外預借現金%

			} 
			gGate.cashBase = dlRealBaseLimit* ta.getDouble("AuthParmOverseaCashPct")/100; //該戶之國外預借現金額度。AuthParmOverseaCashPct => 國外預借現金%
		}

		gGate.adjTotAmt = 1;

		if (blCheckRiskLevel) {
			/*由卡戶之 card acct risk level 取得該等級之總月限百分比及回覆碼參數*/
			/*及預現最高限額*/
			if (gGate.isInstallmentTx)
				gGate.adjTotAmt = ta.getDouble("RiskLevelInstMonthPct")/100; //分期總月限%
			else
				gGate.adjTotAmt = ta.getDouble("RiskLevelTotAmtPct")/100;		//消費總額百分比
		}

		if (gGate.isSpecUse) {/* 特指額度內用卡 */
			gGate.adjTotAmt = 1;
		}

		gGate.finalTotLimit = dlRealBaseLimit*gGate.adjTotAmt; //取得該等級之總額度(A) 
		if (gGate.businessCard) {
			gGate.tmpTotLimit2 = dlRealBaseLimitComp;
		}

		gb.showLogMessage("D","P_SEQNO=>" +  ta.getPSeqNo());
		gb.showLogMessage("D","個人臨調幅度為=>" + gGate.adjTotAmt);
		gb.showLogMessage("D","個人額度(含臨調)為=>" + gGate.finalTotLimit);
		gb.showLogMessage("D","公司額度(含臨調)為=>" + gGate.tmpTotLimit2);


		return true;
	}
	
	//檢核是否有臨調
	/**
	 * 檢核是否有臨調
	 * @param npAcctAdjType 1:檢核個人臨調，2:檢核公司臨調
	 * @return 是否有臨調
	 * @throws Exception if any exception  occurred
	 */
	private boolean checkAcctAdj(int npAcctAdjType) throws Exception {
		//檢核 個人或者公司 是否有臨調

		if (npAcctAdjType>2) {
			return false;
		}
		
		boolean blResult = false;
		
		double dlAdjTotRate=0; 
		double dlAdjInstRateOrAmt=0; //Howard: 如果是 credit card, 此值為金額；如果是 debit card, 此值為 % 
		String slAdjQuota="", slAdjEffStartDate="",slAdjEffEndDate="",slAdjArea="",slAdjRiskType="",slAdjRiskFlag=""; 
		if (npAcctAdjType==1) { // check card_acct =>檢查 個人 是否有臨調 
			slAdjQuota=ta.getValue("CardAcctAdjQuota");				//臨時調高額度註記
			slAdjEffStartDate=ta.getValue("CardAcctAdjEffStartDate");//臨時調高生效日期(起)
			slAdjEffEndDate=ta.getValue("CardAcctAdjEffEndDate");//臨時調高生效日期(迄)
			slAdjArea=ta.getValue("CardAcctAdjArea");				//臨時調高有效地區
			dlAdjTotRate = ta.getDouble("CardAcctTotAmtMonth");		//臨調放大總月限
			dlAdjInstRateOrAmt = ta.getDouble("CardAcctAdjInstPct");		//臨調分期付款比率
			slAdjRiskType = ta.getValue("CardAcctAdjRiskType");
		}
		else if (npAcctAdjType==2) { //check Parent card_acct =>檢查 公司 是否有臨調
			slAdjQuota=ta.getValue("CardAcctAdjQuotaOfComp");				//臨時調高額度註記
			slAdjEffStartDate=ta.getValue("CardAcctAdjEffStartDateOfComp");//臨時調高生效日期(起)
			slAdjEffEndDate=ta.getValue("CardAcctAdjEffEndDateOfComp");//臨時調高生效日期(迄)
			slAdjArea=ta.getValue("CardAcctAdjAreaOfComp");			//臨時調高有效地區
			dlAdjTotRate = ta.getDouble("CardAcctTotAmtMonthOfComp");	//臨調放大總月限%
			dlAdjInstRateOrAmt = ta.getDouble("CardAcctAdjInstPctOfComp");//臨調分期付款比率  => Howard: 如果是 credit card, 此值為金額；如果是 debit card, 此值為 %
			slAdjRiskType = ta.getValue("CardAcctAdjRiskTypeOfComp");
		}

		if (!"Y".equals(slAdjQuota)) 
			return false; //(1)無臨時調高額度

		boolean blCorrectPeriod = HpeUtil.isCurDateBetweenTwoDays(slAdjEffStartDate, slAdjEffEndDate);
		if (!blCorrectPeriod) {
			return false;             //(2)有臨時調高額度, 但期限不合
		}

		if (("1".equals(slAdjArea))  && ("T".equals(gGate.areaType)) ){ // 
			return false;             //(3)有臨時調高額度, 但區域非國內
		}
		else if ( ("2".equals(slAdjArea)) && ("F".equals(gGate.areaType)) ) { //
			return false;             //(4)有臨時調高額度, 但區域非國外
		}
		//Debit card不用檢風險分類旗標
		if (gGate.isDebitCard) {
			return true;
		} else {
	    	blResult = true;
			//(5)有設定臨調的風險分類旗標，對應到專款專用之限制額度旗標
			StringTokenizer st = new StringTokenizer(slAdjRiskType, ",");
			while (st.hasMoreTokens()){
			    if (st.nextToken().equals(gGate.mccRiskType)) {
			    	gGate.adjRiskType = true;
			    	break;
			    }
			}
		}
		return blResult;
	}
	
	/**
	 * 計算個人的總消費金額
	 * @return 個人的總消費金額
	 */
	//kevin:合庫第一階段授權相關金額來自於cardlink，所以目前只提供[尚未請款之授權金額] (有包含 「分期交易尚未兌現」之金額)，會轉入cca_card_acct.total_amt_consume 總授權額(已消費未請款
	private double getCurTotalUnpaidOfPersonal() throws Exception {
		double dlCurTotUnpaid = gGate.totAmtConsume +  
				gGate.ccaConsumePaidConsumeFee +    //已結帳-消費，已納入於總消費金額，應該為零
				gGate.ccaConsumeUnPaidConsumeFee +  //未結帳-消費，改成於總消費金額
				gGate.ccaConsumePaidPrecash +       //已結帳-預現，已納入於總消費金額，應該為零
//				gGate.ccaConsumeUnPaidPrecash +     //未結帳-預現，改成於總預借金額(含已結帳與未結帳)，因已經納入總消費金額，所以不列入計算。
//				gGate.ccaConsumeIbmReceiveAmt +     //合庫無撥指交易，應該為零
				gGate.ccaConsumeUnPostInstFee;      //分期未入帳，cardlink此金額已併入: 總授權額(已消費未請款)，應該為零

		gb.showLogMessage("D","傳入的P_SEQNO=>" +ta.getPSeqNo());
		gb.showLogMessage("D","TotAmtConsume=>" + gGate.totAmtConsume);
		gb.showLogMessage("D","已結帳-消費，已納入於總消費金額，應該為零ccaConsumePaidConsumeFee=>" + gGate.ccaConsumePaidConsumeFee);
		gb.showLogMessage("D","未結帳-消費，改成於總消費金額CcaConsumeUnpaidConsumeFee=>" + gGate.ccaConsumeUnPaidConsumeFee);
		gb.showLogMessage("D","已結帳-預現，已納入於總消費金額，應該為零CcaConsumePaidPrecash=>" + gGate.ccaConsumePaidPrecash);
		gb.showLogMessage("D","CcaConsumeUnpaidPrecash=>" + gGate.ccaConsumeUnPaidPrecash);
		gb.showLogMessage("D","CcaConsumeIbmReceiveAmt=>" + gGate.ccaConsumeIbmReceiveAmt);
		gb.showLogMessage("D","分期未入帳，cardlink此金額已併入CcaConsumeUnpostInstFee=>" + gGate.ccaConsumeUnPostInstFee);
		gb.showLogMessage("D","個人的總消費金額為上述數字總和=>" + dlCurTotUnpaid);

		return dlCurTotUnpaid;
	}

	/**
	 * 計算公司的總消費金額
	 * @return 公司的總消費金額
	 * @throws Exception 
	 */
	private double getCurTotalUnpaidOfComp() throws Exception {
		//kevin:合庫第一階段授權相關金額來自於cardlink，所以目前只提供[尚未請款之授權金額] (有包含 「分期交易尚未兌現」之金額)，會轉入cca_card_acct.total_amt_consume 總授權額(已消費未請款
		double dlCurTotUnpaid = gGate.totAmtConsumeOfComp +
				gGate.ccaConsumePaidConsumeFeeOfComp +         //已結帳-消費，已納入於總消費金額，應該為零
				gGate.ccaConsumeUnPaidConsumeFeeOfComp +       //未結帳-消費，改成於總消費金額
				gGate.ccaConsumePaidPrecashOfComp +            //已結帳-預現，已納入於總消費金額，應該為零
//				gGate.ccaConsumeUnPaidPrecashOfComp +          //未結帳-預現，改成於總預借金額(含已結帳與未結帳)，因已經納入總消費金額，所以不列入計算。
//				gGate.ccaConsumeIbmReceiveAmtOfComp +          //合庫無撥指交易，應該為零
				gGate.ccaConsumeUnPostInstFeeOfComp;           //分期未入帳，cardlink此金額已併入: 總授權額(已消費未請款)，應該為零

		gb.showLogMessage("D","傳入的P_SEQNO=>" +ta.getPSeqNo());
		gb.showLogMessage("D","totAmtConsumeOfComp=>" + gGate.totAmtConsumeOfComp);
		gb.showLogMessage("D","ccaConsumePaidConsumeFeeOfComp=>" + gGate.ccaConsumePaidConsumeFeeOfComp);
		gb.showLogMessage("D","ccaConsumeUnPaidConsumeFeeOfComp=>" + gGate.ccaConsumeUnPaidConsumeFeeOfComp);
		gb.showLogMessage("D","ccaConsumePaidPrecashOfComp=>" + gGate.ccaConsumePaidPrecashOfComp);
		gb.showLogMessage("D","ccaConsumeUnPaidPrecashOfComp=>" + gGate.ccaConsumeUnPaidPrecashOfComp);
		gb.showLogMessage("D","ccaConsumeIbmReceiveAmtOfComp=>" + gGate.ccaConsumeIbmReceiveAmtOfComp);
		gb.showLogMessage("D","ccaConsumeUnPostInstFeeOfComp=>" + gGate.ccaConsumeUnPostInstFeeOfComp);
		gb.showLogMessage("D","公司的總消費金額為上述數字總和=>" + dlCurTotUnpaid);

		return dlCurTotUnpaid;
	}
	
	/**
	 * 計算計算可用餘額
	 * @return 可用餘額
	 * @throws Exception 
	 */
	private void computeBaseNumber() throws Exception{

		//down,計算可用餘額
		computeOtb(1); //計算卡戶個人餘額 OTB, proc line 9535

		if (gGate.businessCard)
			computeOtb(2); //計算卡戶公司餘額 OTB, proc line 9675	  
		//up,計算可用餘額
	}
	
	/**
	 * 計算 卡戶餘額
	 * @param npType 1:個人卡；2:公司卡

	 */
	private void computeOtb(Integer npType) throws Exception{
		/* 卡戶餘額 OTB = [基本額度 + (預付 + 總已付Payment未消)]
        - (累計消費 + 結帳消費 + 未結消費
        +  結帳預現 + 未結預現 + 指撥金額
		+  分期未結金額)
		 */
		//如果是 debit ，則dP_AdjRate是比率，否則dP_AdjRate是金額

		if (npType==1) {
			double dlBaseAmt = gGate.finalTotLimit; //基本額度, 臨調後可用額度
			double dlCurTotUnpaid = gGate.curTotalUnpaidOfPersonal;
			

			gGate.otbAmt = dlBaseAmt + 
					gGate.ccaConsumePrePayAmt +
					gGate.ccaConsumeTotUnPaidAmt - dlCurTotUnpaid;

			gGate.otbAmt = (int)(gGate.otbAmt + 0.5);//四捨五入, proc line 9554

			gb.showLogMessage("D","BaseAmt[臨調後可用額度]=>" + dlBaseAmt);
			gb.showLogMessage("D","CcaConsumePrePayAmt[預付款金額(溢繳款)]=>" + ta.getDouble("CcaConsumePrePayAmt"));
			gb.showLogMessage("D","CcaConsumeTotUnpaidAmt[Payment未消]=>" + gGate.ccaConsumeTotUnPaidAmt);
			gb.showLogMessage("D","CurTotUnpaid[個人的總消費金額]=>" + dlCurTotUnpaid);
			gb.showLogMessage("D","卡戶餘額 = dL_BaseAmt + CcaConsumePrePayAmt + CcaConsumeTotUnpaidAmt - dL_CurTotUnpaid，等於 " +gGate.otbAmt);

		}
		else {

			double dlBaseAmt = gGate.tmpTotLimit2; //公司基本額度, 臨調後可用額度
			double dlCurTotUnpaid = gGate.curTotalUnpaidOfComp;

			gGate.parentOtbAmt = dlBaseAmt + 
//					ta.getDouble("CcaConsumePrePayAmtOfComp") + 
					gGate.ccaConsumePrePayAmtOfComp +
					gGate.ccaConsumeTotUnPaidAmtOfComp - dlCurTotUnpaid;

			gGate.parentOtbAmt = (int)(gGate.parentOtbAmt + 0.5);//四捨五入
		}
	}
}
