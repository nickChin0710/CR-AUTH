/**
 * 授權邏輯查核-共用邏輯處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-共用邏輯處理                       *
 * 2022/03/28  V1.00.01  Kevin       VD自助加油一般授權交易圈存1500                 *
 * 2022/04/20  V1.00.02  Kevin       查核HSM排除特定交易                          *
 * 2022/06/03  V1.00.03  Kevin       網銀推播-信用卡消費通知介面處理                  *
 * 2022/06/17  V1.00.04  Kevin       簡訊傳送時間錯誤修正                           *
 * 2022/12/29  V1.00.32	 Kevin		 OEMPAY通知交易未帶入效期，所以不需判定卡片是否開卡     *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/06/01  V1.00.46  Kevin       P3批次授權比照一般授權的邏輯，不須特別排除            *
 * 2023/08/04  V1.00.50  Kevin       經常性身分驗證交易不須檢查到期日與開卡註記            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.math.BigDecimal;

import javax.xml.datatype.DatatypeConfigurationException;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class AuthLogic {
	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm  gb   = null;

	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate     gGate = null;

	/**
	 * 資料庫存取物件
	 */
	TableAccess     ta   = null;

	
	/**
	 * 更新 cacu_amount 與  cacu_cash 的值
	 * @param spValue 新值
	 * @throws Exception if any exception occurred
	 */
	public void updatePreAuthData(String spValue)  throws Exception{
		gGate.cacuAmount =spValue; //--Y.未沖銷退貨調整, N.已沖銷退貨調整
		gGate.cacuCash =spValue;
	}

	/**
	 * 更新 cacu_amount 與  cacu_cash 的值
	 * @param sP_Value 新值
	 * @throws Exception if any exception occurred
	 */
	public void updateCacuData(String spCacuAmount, String spCacuCash)  throws Exception{
		gGate.cacuAmount =spCacuAmount; /* 計入OTB註記           */ //--Y.未沖銷退貨調整, N.已沖銷退貨調整
		gGate.cacuCash =spCacuCash;     /* 計入OTB預現註記        */
	}
	
	/**
	 * 檢核online redeem 欄位
	 * V1.00.37 P3紅利兌換處理方式調整
	 * 
	 */
	public void checkOnlineRedeem() {
		if ((ta.getValue("AuthTxLogOnlineRedeem_SrcTrans").length()>0) && (gGate.c5TxFlag.length()==0) ) {
			gGate.c5TxFlag = ta.getValue("AuthTxLogOnlineRedeem_SrcTrans");
			setInstallmentFlag();
			setRedeemFlag();
		}
	}
	/**
	 * 判定是否為分期交易，並設定分期註記
	 */
	public void setInstallmentFlag() {
		//down,設定分期註記 
		String slC5TxIndex = gGate.c5TxFlag;
		if ( ("A".equals(slC5TxIndex)) || ("I".equals(slC5TxIndex)) || ("E".equals(slC5TxIndex)) || ("Z".equals(slC5TxIndex)) ) {
			gGate.isInstallmentTx=true; //分期交易
		}
		else
			gGate.isInstallmentTx=false;
		//up, 設定分期註記
		//kevin:Fisc設定分期註記
		if (gGate.isoField[112].length()>0) {
			gGate.isInstallmentTx=true; //分期交易
		}
	}

	/**
	 * 判定是否為紅利交易，並設定紅利註記
	 */
	public void setRedeemFlag() {
		//down, 設定紅利註記 
		String slC5TxIndex = gGate.c5TxFlag;
		if ( ("1".equals(slC5TxIndex)) || ("2".equals(slC5TxIndex)) || ("3".equals(slC5TxIndex)) || ("4".equals(slC5TxIndex))  || ("6".equals(slC5TxIndex))  || ("7".equals(slC5TxIndex)) ) {
			gGate.isRedeemTx=true; //紅利交易
		}
		else
			gGate.isRedeemTx = false;
		//up, 設定紅利註記
		//kevin:Fisc設定紅利註記
		if (gGate.f58T21.length()>0) {
			gGate.isRedeemTx=true; //紅利交易
		}

	}
	
	/**
	 * 取得markup 之後的消費金額
	 * @return markup 之後的消費金額 
	 * @throws Exception if any exception occurred
	 */
	public double getFinalIsoField4Value() throws Exception{
		double dlResult = 0;
		if ("F".equals(gGate.areaType)) { 
			dlResult = gGate.isoFiled4ValueAfterMarkup; 
		}
		else 
			dlResult = gGate.isoFiled4Value;

		dlResult = dlResult + gGate.egovMarkupFee; //外加egov手續費
		return dlResult;
	}
	
	
	/**
	 * 計算 balance amount
	 * @param dlSourceAmt 原始交易金額
	 * @param dlNewAmt 調整後的交易金額
	 * @throws Exception if any exception occurred 
	 */
	public void computeBalanceAmt(double dlSourceAmt, double dlNewAmt) throws Exception {
		if ( (gGate.cashAdjust) || (gGate.purchAdjust)) {
			gGate.balanceAmt = dlNewAmt - dlSourceAmt;
		}
		else if  (gGate.refundAdjust) {
			gGate.balanceAmt = (dlNewAmt - dlSourceAmt)*(-1);//10.27 Tony and IBT 討論後改為此規格
		}
		else if ( gGate.refund ) {
			gGate.balanceAmt = 0-dlSourceAmt;
		}
		else if ( gGate.reversalTrans && (gGate.normalPurch || gGate.easyAutoload || gGate.ipassAutoload || gGate.icashAutoload) ) {
			//沖正交易同退貨處理，判斷原交易是否為正向交易類別
			gGate.balanceAmt = 0-dlSourceAmt;
			gb.showLogMessage("D","computeBalanceAmt  Reversal txn blance AMT => "+ gGate.balanceAmt );

		}
		else {
			gGate.balanceAmt = dlSourceAmt;
		}
	}
	
	/**
	 * 判定是否要忽略交易流程
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean ifIgnoreProcess() {
		//for CCAS_spec_check()/specCheck() use
		boolean blResult = false;
		//4,5,6,7,13,11,18,10 =>都不執行
		//退貨,一般消費調整,退貨調整,補登交易,沖銷,預借現金調整,CHANGE ATM PIN,預先授權完成 => 都 直接 return 
		if (	(gGate.refund) ||  // 退貨
				(gGate.purchAdjust) || //一般消費調整 
				(gGate.refundAdjust) || //退貨調整
				(gGate.forcePosting) || //補登交易
				(gGate.reversalTrans)  || //沖銷
				(gGate.cashAdjust) || //預借現金調整
				(gGate.preAuthComp) || //preAuthComp
				(gGate.nonPurchaseTxn) || //非購貨交易
				(gGate.changeAtmPin) || //CHANGE ATM PIN
				(gGate.atmCardOpen))    //ATM combo開卡

			blResult=true;

		if ((1==gGate.isoFiled4Value) && (gGate.txVoice))/*自行卡語音密碼變更不檢核*/
			
			blResult = true;

		return blResult;
	}

	/**
	 * 判定是否要忽略交易流程
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean ifIgnoreProcess2() {
		//for ISOPurch_Check()/purchaseChecking() use
		boolean blResult = false;
		//4,5,6,7,13,11,16,18,10=>都不執行

		//退貨,一般消費調整,退貨調整,補登交易,沖銷,預借現金調整,預借現金指撥,CHANGE ATM PIN,預先授權完成 => 都 直接 return 
		if (	(gGate.refund) ||  // 退貨
				(gGate.purchAdjust) || //一般消費調整 
				(gGate.refundAdjust) || //退貨調整
				(gGate.forcePosting) || //補登交易
				(gGate.reversalTrans)  || //沖銷
				(gGate.cashAdjust) || //預借現金調整
				(gGate.isEInvoice) || //電子發票驗證:kevin:電子發票檢核非完整ID，所以在此排出			
				(gGate.preAuthComp) || //preAuthComp
				(gGate.atmCardOpen) || //ATM開卡通知
				(gGate.changeAtmPin) || //CHANGE ATM PIN	
				(gGate.atmCardOpen))    //ATM combo開卡

			blResult=true;

		if ((1==gGate.isoFiled4Value) && (gGate.txVoice))/*自行卡語音密碼變更不檢核*/
			blResult = true;

		return blResult;
	}

	/**
	 * 判定是否要忽略交易流程
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean ifIgnoreProcess3() {
		//for CCAS_mcode_check()/mCodeCheck() use
		boolean blResult = false;
		//4,5,6,11,16 => 都不要執行此 function

		//退貨(4),一般消費調整(5),退貨調整(6),預借現金調整(11),預借現金指撥(16) => 都 直接 return 
		if (	(gGate.refund) ||  // 退貨
				(gGate.purchAdjust)  || //一般消費調整 
				(gGate.refundAdjust) || //退貨調整
				(gGate.cashAdjust)   || //預借現金調整
				(gGate.atmCardOpen))    //ATM COMBO卡開卡通知不檢查     
				
			blResult=true;

		if ((1==gGate.isoFiled4Value) && (gGate.cashAdjust))/*預借現金 and 金額 =1*/
			blResult = true;

		return blResult;
	}
	
	/**
	 * 判定是否要忽略交易流程
	 * @return 如果要忽略交易流程，return true；否則return false
	 * V1.00.02 查核HSM排除特定交易
	 */
	public boolean ifIgnoreProcess4() {
		boolean blResult = false;
		//退貨,沖正 交易 => 都直接 return 
		if (	(gGate.refund) ||       //退貨
				(gGate.reversalTrans))  //沖正交易     
				
			blResult=true;

		return blResult;
	}
	
	/**
	 * 判定是否要忽略交易流程5
	 * V1.00.32	OEMPAY通知交易未帶入效期，所以不需判定卡片是否開卡
	 * V1.00.46 P3批次授權比照一般授權的邏輯，不須特別排除
	 * V1.00.50 經常性身分驗證交易不須檢查到期日與開卡註記
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean ifIgnoreProcess5() {
		boolean blResult = false;
		if ( gGate.isEInvoice              //電子發票
				|| gGate.reversalTrans     //沖正交易
				|| gGate.isTokenMTCN       //Token Complete Notification
				|| gGate.isTokenMTEN       //Token Event Notification
				|| gGate.isTokenVTNA) {    //Token Notification Advice
			blResult=true;
		}
		//V1.00.50 經常性身分驗證交易不須檢查到期日與開卡註記
		if (gGate.isIdCheckOrg && gGate.recurringTrans) {
			if ("6300".equals(gGate.mccCode)) { //V1.00.01 - 依需求，須為6300保險業，才可以不須檢查到期日
				return true;
			}
		}
		return blResult;
	}
	/**
	 * 驗證客戶id
	 * @return 如果驗證客戶id成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
//	public boolean checkIfVerifyCustIdOfMerchant() throws Exception {
//		boolean blResult = true;
//		String slId = getIdFromIsoString();
//		String slIfVerifyId = ta.getMerchantInfo4VerifyCustID();
//		if ("Y".equals(slIfVerifyId)) {
//			if (!verifyId(slId)) {
//				ta.getAndSetErrorCode("D9");
//				blResult = false;
//			}
//			else
//				blResult = true;
//		}
//		else if ("N".equals(slIfVerifyId)) {
//			blResult = true;
//		}
//		else {
//			if ("".equals(slId.trim()))
//				blResult = true;
//			else {
//				ta.getAndSetErrorCode("QA");
//				blResult = false;
//			}
//		}
//		return blResult;
//	}
	
	/**
	 * 從ISO8583中取得身分證號
	 * @return 身分證號 
	 */
	public String getIdFromIsoString() {
		String slId = "";
		//kevin:新增FISC規格
		if ("FISC".equals(gGate.connType)) {
			if (gGate.f58T67Id.length() > 0) {
				slId = gGate.f58T67Id;
			}
		}
		else {
			if (gGate.isoField[127].length()>=67) {
				slId=gGate.isoField[127].substring(57, 67);
			}
		}
		return slId.trim();
	}
	
	/**
	 * 從ISO8583中取得F127
	 * @return VoiceCode
	 */
	public String getPasswdFromIsoString() {
		String slVoiceCode = "";
		if (gGate.isoField[127].length()>=71) {
			slVoiceCode=gGate.isoField[127].substring(67, 71);
		}
		return slVoiceCode.trim();
	}

	/**
	 * 從ISO8583中取得F127 F43 
	 * @return 語音開卡CODE
	 */
	public String getPasswdFromIsoString4VoiceOpenCard() {
		String slVoiceCode = "";
		if (gGate.isoField[127].length()>=57) {
			slVoiceCode=gGate.isoField[127].substring(51, 57);
		}
		//kevin:FISC語音開卡
		if ("ATM".equals(gGate.connType)) {
			slVoiceCode=HpeUtil.getTaiwanDateStr(gGate.birthday);
			gb.showLogMessage("D","Birthday getTaiwanDateStr error => "+ "birthday:" + gGate.birthday );
		}
		else {
			if ("CVR".equals(gGate.isoField[43].substring(0,3))) {
				slVoiceCode=gGate.isoField[43].substring(9,15);
			}
		}


		return slVoiceCode.trim();
	}
	
	/**
	 * 檢核ID 是否正確
	 * @param spID 將被檢核的ID
	 * @return 如果ID檢核正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean verifyId(String spID) throws Exception {
		boolean blResult = true;
		if (!spID.equals(ta.getValue("CrdIdNoIdNo")))
			blResult = false;

		return blResult;
	}
	
	/**
	 * 取得CashBase 
	 * @return 該戶之預借現金額度
	 */
	public double getCashBase() {
		double dlResult = 0;

		if (gGate.businessCard)
			dlResult = gGate.cashBaseOfComp;
		else
			dlResult = gGate.cashBase;
		return dlResult;
	}
	
	/**
	 * 取得Debit日的可用金額 
	 * @return 該戶之日的可用金額
	 */
	public BigDecimal getDailyLimitAmt() {
		//計算日的可用金額
		BigDecimal bdResult = new BigDecimal("0");;
		bdResult = ta.getBigDecimal("DebitParmDayAmount").subtract(getDailyTradeAmt());
		return bdResult;
	}
	
	/**
	 * 取得Debit日的累積消費金額 
	 * @return 該戶之日的累積消費金額
	 */	
	public BigDecimal getDailyTradeAmt() {
		//計算日累積消費金額
		BigDecimal bdResult = new BigDecimal("0");
//		bdResult = ta.getBigDecimal("CcaConsumeTxTotAmtDay");
		bdResult = new BigDecimal(gGate.ccaConsumeTxTotAmtDay);
		return bdResult;
	}
	
	/**
	 * 取得Debit月累積消費金額 
	 * @return 該戶之月累積消費金額
	 */		
	public BigDecimal getMonthlyTradeAmt() {
		//計算月累積消費金額
		BigDecimal bdResult = new BigDecimal("0");
//		bdResult = ta.getBigDecimal("CcaConsumeTxTotAmtmonth");
		bdResult = new BigDecimal(gGate.ccaConsumeTxTotAmtMonth);
		return bdResult;
	}
	
	/**
	 * 取得Debit日累積消費次數 
	 * @return 該戶之日累積消費次數
	 */		
	public int getDailyTradeCnt() {
		//計算日累積消費次數
		int ilResult = 0;
//		ilResult = ta.getInteger("CcaConsumeTxTotCntDay");
		ilResult = (int) Math.round(gGate.ccaConsumeTxTotCntDay);
		return ilResult;
	}
	
	/**
	 * 取得自助加油的圈存金額
	 * @return 自助加油的圈存金額
	 * 	@throws Exception if any exception occurred 
	 */
	public double getPreAuthAmt4SelfGas() throws Exception {

		gb.showLogMessage("I","getPreAuthAmt4SelfGas : started");
		
		double dlResult = 0;
		//kevin:自助加油預先授權金額與參數GAS-SELF-AMT超過CCA_SYS_PARM2的長度限制，改成GAS-AMT
		if ( !ta.selectSysParm2("VD-GAS-AMT","LOCK","Nvl(SYS_DATA2,'1500')") ) {
			ta.setValue("SYS_DATA2","1500"); 
		}
		dlResult = ta.getDouble("SYS_DATA2");//sB
		return dlResult;
	}

	/**
	 * 2022/06/03 V1.00.00 網銀推播-信用卡消費通知介面處理
	 * @return 簡訊與網銀推播內容
	 * @throws Exception if any exception occurred 
	 */
	public String getSmsContent(int npTransType, String spMsgId) throws Exception{
		String slSmsContent = "" ;
		String slReplaceSms = "" ;
		//down, 依據是否為 debit card 給予不同的簡訊內容
		String slTransType1Field1 = "";
		if (gGate.isDebitCard) {
			slTransType1Field1 ="VISA金融卡";
		}
		else {
			slTransType1Field1 ="信用卡";
		}


		//up, 依據是否為 debit card 給予不同的簡訊內容


		//down, 取出卡號末四碼
		String slLast4CardNo = "";
		if (gGate.cardNo.length()>=4)
			slLast4CardNo = gGate.cardNo.substring(gGate.cardNo.length()-4, gGate.cardNo.length());
		//up, 取出卡號末四碼		


		//down, 取出月日與時間
		String slMonth = gGate.txDate.substring(4, 6);
		String slDate = gGate.txDate.substring(6, 8);
		String slTime = gGate.txTime.substring(0, 2) + ":" + gGate.txTime.substring(2, 4);
		//up, 取出月日與時間


		//down, 判斷國內外交易
		boolean blIsForeignTrands = false;
		String slTransType1Field2="", slTransType1Field3="";
		double dlBillAmount = gGate.ntAmt;
		if ("F".equals(gGate.areaType))
			blIsForeignTrands = true;

		if (blIsForeignTrands) {
			slTransType1Field2 = "國外交易";
			slTransType1Field3 = "，若為外幣交易以帳單金額為準。祝順心!";
			if ("392".equals(gGate.dualCurr4Bill) || "840".equals(gGate.dualCurr4Bill)) {
				dlBillAmount = gGate.dualAmt4Bill;
				//V1.00.04 設定雙幣卡簡訊，美金(9840)，日幣(9392)
				if ("392".equals(gGate.dualCurr4Bill)) {
					spMsgId = "9392";
				}
				if ("840".equals(gGate.dualCurr4Bill)) {
					spMsgId = "9840";
				}
				gb.showLogMessage("D","dual currency amount for billing. CURRENCY = "+gGate.dualCurr4Bill+" ; AMOUNT = "+ dlBillAmount+" ; spMsgId = "+spMsgId);
			}
		}
		else {
			slTransType1Field2 = "國內交易";
			slTransType1Field3 = "。祝順心!";
		}


		//up, 判斷國內外交易


		if (npTransType==1) { //消費簡訊
			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 要回傳下列訊息內容
			/*
    		1.	感謝使用合庫VISA金融卡末四碼8639於12月04日 15:14國外交易台幣1630元，若為外幣交易以帳單金額為準。祝順心!
			2.	感謝使用合庫VISA金融卡末四碼2333於12月04日 15:13國內交易台幣5880元。祝順心!
			3.	感謝使用合庫信用卡末四碼3538於12月04日 15:14國內交易台幣7510元。祝順心!
			4.	感謝使用合庫信用卡末四碼1042於12月04日 15:14國外交易台幣17123元，若為外幣交易以帳單金額為準。祝順心!

			 * */


//			sL_SmsContent = "感謝使用合庫 " +sL_TransType1Field1 + " 末四碼" + sL_Last4CardNo + "於" + sL_Month + "月" + sL_Date + "日 " + sL_Time +   sL_TransType1Field2 + "台幣 " +G_Gate.nt_amt + "元" + sL_TransType1Field3;
			//kevin:取得消費簡訊內容，並放入簡訊變數。
			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
			slSmsContent = ta.getSmsContentnt(spMsgId);

			gb.showLogMessage("D","GET SMS_MSG_ID=" + spMsgId);
			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);

			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
			slSmsContent = slReplaceSms6;

			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);

		}
		else if (npTransType==2) {//特殊簡訊一
			//kevin:取得特殊簡訊一內容，並放入簡訊變數。
			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
			slSmsContent = ta.getSmsContentnt(spMsgId);

			gb.showLogMessage("D","GET SMS2_MSG_ID1=" + spMsgId);
			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);

			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
			slSmsContent = slReplaceSms6;

			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);

		}
		else if (npTransType==3) {//特殊簡訊二
			//kevin:取得特殊簡訊二內容，並放入簡訊變數。
			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
			slSmsContent = ta.getSmsContentnt(spMsgId);

			gb.showLogMessage("D","GET SMS2_MSG_ID2=" + spMsgId);
			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);

			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
			slSmsContent = slReplaceSms6;

			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);

		}
		else {
			//kevin:取得附卡消費通知正卡簡訊一內容，並放入簡訊變數。
			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
			slSmsContent = ta.getSmsContentnt(spMsgId);

			gb.showLogMessage("D","GET SMS_MSG_ID=" + spMsgId);
			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);

			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
			slSmsContent = slReplaceSms6;

			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);
		}

		return slSmsContent;

	}


	/**
	 * 檢核授權邏輯查核-取得戶的基本額度 ( 非臨調 )
	 * V1.00.38 P3授權額度查核調整
	 * @return 戶的基本額度
	 * @throws Exception if any exception occurred
	 */	
	public int getBaseAmt() {
		//取得戶的基本額度 ( 非臨調 )
		int dL_Result = 0;

		if (gGate.isDebitCard) {
			dL_Result = ta.getInteger("DebitParmMonthAmount"); /*該戶之基本額度*/
			gb.showLogMessage("I", "****** Debit card 基本額度(非臨調):" +dL_Result + "******");
		}
		else { 
			dL_Result = ta.getInteger("ActAcnoLineOfCreditAmt"); /*該戶之基本額度*/
			gb.showLogMessage("I", "****** Credit card 基本額度(非臨調):" +dL_Result + "******");
		}
		return dL_Result;
	}

	/**
	 * 檢核授權邏輯查核-取得公司基本額度 ( 非臨調 )
	 * V1.00.38 P3授權額度查核調整
	 * @return 公司基本額度
	 * @throws Exception if any exception occurred
	 */	
	public int getBaseAmtOfComp() {
		//取得公司的基本額度 ( 非臨調 )
		int dL_Result = 0;

		if (gGate.isDebitCard) {
			dL_Result = ta.getInteger("DebitParmMonthAmount"); /*該戶之基本額度*/
			gb.showLogMessage("I", "****** Debit card 基本額度(非臨調)(公司):" +dL_Result + "******");
		}
		else { 
			dL_Result = ta.getInteger("ActAcnoLineOfCreditAmtOfComp"); /*該戶之基本額度*/
			gb.showLogMessage("I", "****** Credit card 基本額度(非臨調)(公司):" +dL_Result + "******");
		}

		if (0==dL_Result) {
			dL_Result = 999999999;
			gb.showLogMessage("I", "****** 信用卡基本額度(公司)為0，所以強制設定為:" +dL_Result + "******");
		}
		return dL_Result;
	}
}
