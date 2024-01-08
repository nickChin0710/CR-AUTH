/**
 * 授權資料TABLE存取物件
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
 * 2021/02/08  V1.00.00  Kevin       授權資料TABLE存取物件                        *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      *  
 * 2021/03/26  V1.00.02	 Kevin       原交易取消時，原授權紀錄須紀錄chg_date chg_time  *
 * 2021/03/30  V1.00.03  Kevin       小金額交易檢查邏輯調整                         *
 * 2021/09/28  V1.00.04  Kevin       修改子卡月累績消費邏輯-改用原始卡號               *
 * 2021/12/23  V1.00.05  Kevin       新增簡訊內容在簡訊log中                       *
 * 2022/01/05  V1.00.06  Kevin       臨調公司戶調整                               *
 * 2022/01/07  V1.00.07  Kevin       新增交易Duration Time                      *
 * 2022/01/13  V1.00.08  Kevin       TCB新簡訊發送規則                            *
 * 2022/03/17  V1.00.09  Kevin       麗花襄理要求2447交易需帶入原始RRN               *
 * 2022/03/18  V1.00.10  Kevin       LINE MESSAGE LOG放入傳送時間                *
 * 2022/03/23  V1.00.11  Kevin       簡訊發送排除條件修正                          *
 * 2022/03/26  V1.00.12  Kevin       代行交易可以做沖正                            *
 * 2022/03/30  V1.00.13  Kevin       ECS人工沖正處理與沖正成功檢查原交易是否發生在       *
 *                                   budget date之前，須扣出沖正後金額避免佔額        *
 * 2022/04/22  V1.00.14  Kevin       Line推播外幣幣別未顯示                        *
 * 2022/05/04  V1.00.15  Kevin       ATM預借現金密碼變更功能開發                    *
 * 2022/06/01  V1.00.16  Kevin       預借現金密碼錯誤次數檢查                       *
 * 2022/06/03  V1.00.17  Kevin       網銀推播-信用卡消費通知介面處理                  *
 * 2022/06/21  V1.00.18  Kevin       網銀推播-網銀客戶設定啟用通知                   *
 * 2022/07/07  V1.00.19  Kevin       調整特指戶判斷條件                            *
 * 2022/08/16  V1.00.20  Kevin       修正票證交易日累計自動加值交易金額及次數處理與沖正問題  *
 * 2022/09/21  V1.00.21  Kevin       以沖正交易成功與否作為判斷條件，並忽略的原始回覆碼的判斷*
 * 2022/10/24  V1.00.22  Kevin       風險特店所需之收單代碼資料長度根據不同卡別調整       *
 * 2022/11/07  V1.00.23  Kevin       特店編號取得手續費率，除原先4碼符合之外再新增8碼檢查   *
 * 2022/12/01  V1.00.27  Kevin       OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD  *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/04/18  V1.00.43  Kevin       OEMPAY Token國外交易之管控參數:                *
 *                                   綁定之後72小時只能刷8,000元的國外交易              *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/03/15  V1.00.40  Kevin       P3檢查行動支付手機黑名單                        *
 * 2023/05/24  V1.00.45  Kevin       P3免照會VIP只有定義在ID帳戶                    *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/08/04  V1.00.49  Kevin       風險特店調整及新增特殊特店名稱檢查(eToro)           *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 * 2023/09/15  V1.00.53  Kevin       專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'   *
 * 2023/10/12  V1.00.54  Kevin       OEMPAY綁定Mastercard Token成功通知僅限行動裝置  *
 * 2023/11/17  V1.00.57  Kevin       MCC風險分類檔欄位整理                          *
 * 2023/11/20  V1.00.58  Kevin       TXLOG相關欄位整理                            *
 ******************************************************************************
 */

package com.tcb.authProg.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.Timestamp;
import java.util.Locale;

import com.tcb.authProg.bil.BilOBase;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.ConstObj;
import com.tcb.authProg.util.HpeUtil;

public class TableAccess extends AuthDAO {

	private String addTableOwner(String spTableName) {
		String slResult = spTableName;
		String slTableOwner = gb.getTableOwner().trim(); 
		if (slTableOwner.length()>0)
			slResult = slTableOwner + "." + spTableName;

		return slResult;
	}
	public TableAccess(AuthGlobalParm gb,AuthTxnGate gate) {
		super.gb  = gb;
		this.gate = gate;
	}


	/**
	 * 新增無對應之MCC風險分類檔CCA_MCC_RISK
	 * V1.00.57 MCC風險分類檔欄位整理
	 * @return 如果檢核通過return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean insertMccRick(String spMccCode,  String spRiskType) throws Exception{
		boolean blResult=true;

		daoTable = addTableOwner("CCA_MCC_RISK");



		setValue("MCC_CODE",spMccCode);
		setValue("RISK_TYPE",spRiskType); 

		setValue("MCC_REMARK","授權系統無對應之MCC，自動新增");
		setValue("AMOUNT_RULE",gate.mccRiskAmountRule); //交易金額 歸屬註記
		setValue("NCCC_FTP_CODE",gate.mccRiskNcccFtpCode); //人工授權 交易別
		setValue("RISK_FACTOR",""+gate.mccRiskFactor);
		//setValueInt("SEQ_NO",0);//Howard:這樣寫會導致後續執行sql 時發生 exception
		setValue("SEQ_NO","0");

		setValue("APR_DATE",gb.getSysDate());
		setValue("APR_USER",gate.approveUser);
		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_USER","auth");


		setValue("MOD_USER",ConstObj.MOD_USER);


		setTimestamp("MOD_TIME", gb.getgTimeStamp());
		setValue("MOD_PGM",gb.getSystemName());

		//setValueInt("MOD_SEQNO",0); //Howard:這樣寫會導致後續執行sql 時發生 exception
		setValue("MOD_SEQNO","0");


		insertTable();

		return blResult;


	}


	private boolean insertEntryMode(String spEntryMode, String spEntryType) throws Exception{
		boolean blResult=true;

		daoTable = addTableOwner("cca_entry_mode");



		setValue("ENTRY_MODE",spEntryMode);
		setValue("ENTRY_TYPE",spEntryType); 

		setValue("MODE_DESC","");

		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_USER","auth");

		setValue("APR_DATE"," ");
		setValue("APR_USER"," ");


		setValue("MOD_USER","auth");
		//setTimestamp("MOD_TIME",gate.sG_TransactionStartDateTime);
		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		setValue("MOD_PGM",gb.getSystemName());


		//setValueInt("MOD_SEQNO",0);
		setInt(11, 99);

		insertTable();




		return blResult;


	}

	public String getEntryModeType(String spEntrymode)  throws Exception{
		gb.showLogMessage("I","getEntryModeType()! start");
		//Howard: 取得 entry mode class
		//kevin: 取得風險分數 riskFact
		String slEntryModeType = "";
		String slEntryMode="";
		if (spEntrymode.length()>=2)
			slEntryMode = spEntrymode.substring(0, 2);

		daoTable = addTableOwner("cca_entry_mode");	

		selectSQL ="ENTRY_TYPE,"
				  +"RISK_FACTOR";

		whereStr="WHERE ENTRY_MODE =? ";
		setString(1,slEntryMode);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.getEntryModeType -- can not find data. ENTRY_MODE is  "+slEntryMode + "--");
			slEntryModeType = "CNP"; //default is CNP
			insertEntryMode(slEntryMode, "X");



		}

		if ("X".equals(getValue("ENTRY_TYPE")))
			slEntryModeType = "CNP";
		slEntryModeType = getValue("ENTRY_TYPE");
		
		gate.posRiskFactor = getDouble("RISK_FACTOR");
		gate.riskFctorInd  = 1; //kevin: 1. 計算 Pos Entry Mode 風險分數

		return slEntryModeType;
	}
//	public String getMerchantInfo4VerifyCustID() throws Exception {
//		//Howard: 應該不需要做此檢核
//		String slResult = "";
//		daoTable =addTableOwner("CCS_MERCHANT4VERIFYID"); 	
//
//		selectSQL ="MERCHANTNO, IFVERIFYCUSTID" ;
//
//		whereStr="WHERE MERCHANTNO =? ";
//		setString(1,gate.merchantNo);
//
//		selectTable();
//		if ( "Y".equals(notFound) ) {
//			gb.showLogMessage("I","function: TA.ifMerchant4VerifyCustID -- can not find data. MerchantNo is  "+gate.merchantNo + "--");
//			slResult = "";
//
//		}
//		slResult = getValue("IFVERIFYCUSTID");
//
//		return slResult;
//
//	}
	/**
	 * Select資料到 HCE_CARD for FISC代碼化交易之虛擬卡資料檔
	 * @return
	 * @throws Exception
	 */
	public boolean selectHceCard() throws Exception{
		gb.showLogMessage("I","selectHceCard()! start");
		boolean blResult=true;
		daoTable = addTableOwner("HCE_CARD");	

		selectSQL ="NVL(STATUS_CODE,'0') as HceCardStatusCode "; //

		whereStr="WHERE V_CARD_NO =? and CARD_NO =? ";
		setString(1,gate.tpanTicketNo);
		setString(2,gate.cardNo);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectHceCard -- can not find HCE TPAN ="+gate.tpanTicketNo + "--");
			blResult = false;
		}

		return blResult;
	}
	/**
	 * 將資料寫入 CCA_TPAN_TRADE_INFO 代碼化交易紀錄檔
	 * @return
	 * 		@throws Exception
	 */
//	public boolean insertTpanTradeInfo() throws Exception{
//		boolean blResult=true;
//
//		daoTable = addTableOwner("CCA_TPAN_TRADE_INFO");
//
//
//
//		setValue("CARD_NO",gate.cardNo);//實體卡號
//		setValue("TPAN_NO",gate.tokenS8AcctNum); //虛擬卡號
//
//		setValue("CRT_DATE",gb.sysDate);
//		setValue("CRT_TIME",gb.sysTime);
//
//
//
//		//setTimestamp("MOD_TIME",HpeUtil.getCurTimestamp());
//		//setTimestamp("MOD_TIME",gate.sG_TransactionStartDateTime);
//		setTimestamp("MOD_TIME",gb.gTimeStamp);
//
//
//		setValue("MOD_PGM",ConstObj.MOD_PGM);
//
//		//down, assign Token Q8 的值
//		String slTableFieldName="";
//		String slObjId="", slObjData="";
//		for (int i=0; i<gate.gTokenQ8ObjArrayList.size(); i++) {
//			slObjId= gate.gTokenQ8ObjArrayList.get(i).getObjId();
//			slObjData = gate.gTokenQ8ObjArrayList.get(i).getObjData();
//
//			slTableFieldName = "TOKENQ8TAG" + slObjId;
//
//			setValue(slTableFieldName,slObjData);
//		}
//		//up, assign Token Q8 的值
//
//
//		//down, assign Token Q9 的值
//		setValue("TOKENQ9FIID",gate.tokenQ9Fiid);
//		setValue("TOKENQ9VISADEVTYP",gate.tokenQ9VisaDevTyp);
//		setValue("TOKENQ9VISACHIPTXNIND",gate.tokenQ9VisaChipTxnInd);
//		setValue("TOKENQ9VISAMSGRSNCDE",gate.tokenQ9VisaMsgRsnCde);
//		setValue("TOKENQ9VISAFILLER",gate.tokenQ9VisaFiller);
//		setValue("TOKENQ9MASTERDEVTYP",gate.tokenQ9MasterDevTyp);
//		setValue("TOKENQ9MASTERADVICERSNCDE",gate.tokenQ9MasterAdviceRsnCde);
//		setValue("TOKENQ9MASTERADVCDETLCDE",gate.tokenQ9MasterAdvcDetlCde);
//		setValue("TOKENQ9MASTERAUTHAGENTIDCDE",gate.tokenQ9MasterAuthAgentIdCde);
//		setValue("TOKENQ9MASTERONBEHALF",gate.tokenQ9MasterOnBehalf);
//		setValue("TOKENQ9MASTERFILLER",gate.tokenQ9MasterFiller);
//		//up, assign Token Q9 的值
//
//		//down, assign Token S8 的值
//		setValue("TOKENS8ACCTNUMIND",gate.tokenS8AcctNumInd);
//		setValue("TOKENS8ACCTNUM",gate.tokenS8AcctNum);
//		setValue("TOKENS8EXPDAT",gate.tokenS8ExpDat);
//		setValue("TOKENS8ACVERYRSLT",gate.tokenS8AcVeryRslt);
//		setValue("TOKENS8FRAUDCHKRSLT",gate.tokenS8FraudChkRslt);
//		setValue("TOKENS8Filler",gate.tokenS8Filler);
//
//		//up, assign Token S8 的值
//
//		//down, assign Token F4 的值
//		setValue("TOKENF4WALLETINDFLG",gate.tokenF4WalletIndFlg);
//		setValue("TOKENF4WALLETINDDATA",gate.tokenF4WalletIndData);
//		setValue("TOKENF4Filler",gate.tokenF4Filler);
//		//up, assign Token F4 的值
//		insertTable();
//
//
//
//
//		return blResult;
//
//
//	}
	/**
	 * 將資料寫入 OEMPAY_APPLY_DATA OEMPAY代碼化申請紀錄檔
	 * V1.00.54 OEMPAY綁定Mastercard Token成功通知僅限行動裝置
	 * @return
	 * @throws Exception
	 */
	public boolean insertOempayApplyData() throws Exception{
		gb.showLogMessage("I","insertOempayApplyData()! start");

		boolean blResult=true;

		daoTable = addTableOwner("OEMPAY_APPLY_DATA");



		setValue("CARD_NO",gate.cardNo);//實體卡號
		setValue("V_CARD_NO",gate.tokenS8AcctNum); //虛擬卡號
		setValue("NEW_END_DATE",gate.tpanExpire);
		setValue("ACNO_P_SEQNO",getValue("CardBaseAcnoPSeqNo") );
		setValue("ID_P_SEQNO",getValue("CardBaseIdPSeqNo") );

		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_TIME",gb.getSysTime());

		setTimestamp("MOD_TIME",gb.getgTimeStamp());
		setValue("MOD_PGM",gb.getSystemName());
		
		setValue("TOKEN_TRANSACTION_TYPE",gate.f58T73TokenType);
		if (gate.isTokenMTAR) {
			setValue("TOKEN_MESG_TYPE",gate.tokenMesgType);
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("ACCOUNT_SOURCE",gate.accountSource);
			setValue("ACCT_INSTANCE_ID",gate.acctInstanceId);
			setValue("DEVICE_IP",gate.deviceIp);
			setValue("WALLET_ID_HASH",gate.walletIdHash);
			setValue("CARDHOLDER_NAME",gate.cardholderName);
			setValue("RECOMMENDATION",gate.recommendation);
			setValue("RECOMMEND_VERISON",gate.recommendVerison);
			setValue("DEVICE_SCORE",gate.deviceScore);
			setValue("ACCOUNT_SCORE",gate.accountScore);
			setValue("NUMBER_OF_ACTIVE_TOKEN",gate.numOfActiveToken);
			setValue("WALLET_REASON_CODE",gate.walletReasonCode);
			setValue("DEVICE_LOCATION",gate.deviceLocation);
			setValue("NUMBER_LAST_4_DIGITS",gate.numLast4Digits);
			setValue("TOKEN_TYPE",gate.tokenType);
			setValue("MESSAGE_REASON_CODE",gate.f58T72);
		}
		else if (gate.isTokenMTER) {
			setValue("TOKEN_MESG_TYPE",gate.tokenMesgType);
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("ACCOUNT_SOURCE",gate.accountSource);
			setValue("ACCT_INSTANCE_ID",gate.acctInstanceId);
//			setValue("DEVICE_IP",gate.deviceIp);
			setValue("WALLET_ID_HASH",gate.walletIdHash);
			setValue("CARDHOLDER_NAME",gate.cardholderName);
//			setValue("RECOMMENDATION",gate.recommendation);
//			setValue("RECOMMEND_VERISON",gate.recommendVerison);
//			setValue("DEVICE_SCORE",gate.deviceScore);
//			setValue("ACCOUNT_SCORE",gate.accountScore);
			setValue("NUMBER_OF_ACTIVE_TOKEN",gate.numOfActiveToken);
//			setValue("WALLET_REASON_CODE",gate.walletReasonCode);
//			setValue("DEVICE_LOCATION",gate.deviceLocation);
//			setValue("NUMBER_LAST_4_DIGITS",gate.numLast4Digits);
			setValue("TOKEN_TYPE",gate.tokenType);			
			setValue("MESSAGE_REASON_CODE",gate.f58T72);
		}
		else if (gate.isTokenMTCN) {
			setValue("TOKEN_PROVIDER",gate.tokenProvider);
			setValue("ASSURANCE_LEVEL",gate.assuranceLevel);
			setValue("TOKEN_REQUESTOR_ID",gate.tokenRequetorId);
			setValue("CONTACTLESS_USAGE",gate.contactlessUsage);
			setValue("EC_USAGE",gate.ecUsage);
			setValue("MOBILE_USAGE",gate.mobileEcUsage);			
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("NUMBER_OF_ACTIVE_TOKEN",gate.numOfActiveToken);
			setValue("ISSUE_PRODUCT_ID",gate.issueProductId);
			setValue("CONSUMER_LANGUAGE",gate.consumerLanguage);
			setValue("DEVICE_NAME",gate.deviceName);
			setValue("FINAL_DECISION",gate.finalDecision);
			setValue("FINAL_IND",gate.finalInd);
			setValue("T_C_INDENTIFIER",gate.tcIndentifier);
			setValue("T_C_DATE_TIME",gate.tcDateTime);
			setValue("ACTIVE_ATTEMPTS",gate.activeAttempts);
			setValue("TOKEN_UNIQUE_REF",gate.tokenUniqueRef);
			setValue("ACCOUNT_NUMBER_REF",gate.acctNumberRef);			
			setValue("TOKEN_TYPE",gate.tokenType);			
			setValue("WALLET_ID",gate.walletId);
			setValue("DEVICE_TYPE",gate.deviceType);	
			setValue("MESSAGE_REASON_CODE",gate.f58T72);
		}
		else if (gate.isTokenMTEN) {
			setValue("TOKEN_PROVIDER",gate.tokenProvider);
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("TOKEN_EVENT",gate.tokenEvent);
			setValue("TOKEN_EVENT_REASON",gate.tokenEventReason);
			setValue("CONTACTLESS_USAGE",gate.contactlessUsage);
			setValue("EC_USAGE",gate.ecUsage);
			setValue("MOBILE_USAGE",gate.mobileEcUsage);			
			setValue("EVENT_REQUESTOR",gate.eventRequestor);
			setValue("TOKEN_REQUESTOR_ID",gate.tokenRequetorId);
			setValue("WALLET_ID",gate.walletId);
			setValue("DEVICE_TYPE",gate.deviceType);	
			setValue("MESSAGE_REASON_CODE",gate.f58T72);
		}
		else if (gate.isTokenVTAR) {
			setValue("TOKEN_MESG_TYPE",gate.tokenMesgType);
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("ACCOUNT_SOURCE",gate.accountSource);
			setValue("ACCT_INSTANCE_ID",gate.acctInstanceId);
			setValue("DEVICE_IP",gate.deviceIp);
			setValue("WALLET_ID_HASH",gate.walletIdHash);
			setValue("CARDHOLDER_NAME",gate.cardholderName);
			setValue("RECOMMENDATION",gate.recommendation);
			setValue("RECOMMEND_VERISON",gate.recommendVerison);
			setValue("DEVICE_SCORE",gate.deviceScore);
			setValue("ACCOUNT_SCORE",gate.accountScore);
			setValue("NUMBER_OF_ACTIVE_TOKEN",gate.numOfActiveToken);
			setValue("WALLET_REASON_CODE",gate.walletReasonCode);
			setValue("DEVICE_LOCATION",gate.deviceLocation);
			setValue("NUMBER_LAST_4_DIGITS",gate.numLast4Digits);
			setValue("TOKEN_TYPE",gate.tokenType);
			//new fields for visa oempay
			setValue("NUMBER_OF_INACTIVE_TOKEN",gate.numOfInactiveToken);
			setValue("NUMBER_OF_SUSPENDED_TOKEN",gate.numOfSuspendedToken);
			setValue("MESSAGE_REASON_CODE",gate.f58T70);

		}
		else if (gate.isTokenVTNA) {
			setValue("TOKEN_PROVIDER",gate.tokenProvider);
			setValue("CORRELATION_ID",gate.correlationId);
			setValue("TOKEN_EVENT",gate.tokenEvent);
			setValue("TOKEN_EVENT_REASON",gate.tokenEventReason);
			setValue("CONTACTLESS_USAGE",gate.contactlessUsage);
			setValue("EC_USAGE",gate.ecUsage);
			setValue("MOBILE_USAGE",gate.mobileEcUsage);			
			setValue("EVENT_REQUESTOR",gate.eventRequestor);
			setValue("TOKEN_REQUESTOR_ID",gate.tokenRequetorId);
			setValue("WALLET_ID",gate.walletId);
			setValue("DEVICE_TYPE",gate.deviceType);	
			setValue("MESSAGE_REASON_CODE",gate.f58T70);

		}



		insertTable();




		return blResult;


	}
	/*
	 * 寫入 OTP ，以便批次程式傳送OTP 給消費者
	 * @return
	 */
	public boolean writeOtpInfo() throws Exception{
		boolean blResult=true;

		daoTable = addTableOwner("OTP_INFO table....");
		//OTP is gate.otpValue
		return blResult;
	}
	/**
	 * 新增資料到 MOB_TPAN_INFO 代碼化交易之虛擬卡資料檔
	 * @return
	 * @throws Exception
	 */
//	public boolean insertMobTpanInfo() throws Exception{
//		boolean blResult=true;
//
//		daoTable = addTableOwner("MOB_TPAN_INFO");
//
//
//
//		setValue("CARD_NO",gate.cardNo);//實體卡號
//		setValue("TPAN_NO",gate.tokenS8AcctNum); //虛擬卡號
//		setValue("TPAN_STATUS",gate.tokenQ8TagQ9); //虛擬卡狀態 == Token Status //1: Active，2: Suspended，5: Inactive，4: Deactivated
//
//		setValue("CRT_DATE",gb.sysDate);
//		setValue("CRT_TIME",gb.sysTime);
//
//
//
//		//setTimestamp("MOD_TIME",HpeUtil.getCurTimestamp());
//		//setTimestamp("MOD_TIME",gate.sG_TransactionStartDateTime);
//		setTimestamp("MOD_TIME",gb.gTimeStamp);
//
//		setValue("MOD_PGM",ConstObj.MOD_PGM);
//
//
//		insertTable();
//
//
//
//		return blResult;
//
//
//	}
	/**
	 * Select資料到 OEMPAY_CARD for FISC代碼化交易之虛擬卡資料檔
	 * V1.00.27 OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD
	 * V1.00.43 OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
	 * @return
	 * @throws Exception
	 */
	public boolean selectOempayCard() throws Exception{
		gb.showLogMessage("I","selectOempayCard()! start");

		boolean blResult=true;
		daoTable = addTableOwner("OEMPAY_CARD");	

		selectSQL = "STATUS_CODE as OempayCardStatusCode, " //
				  + "CRT_DATE    as OempayCredCreateDate, "
				  + "EVENT_TIME  as OempayCredCreateTime ";

		whereStr="WHERE V_CARD_NO =? and CARD_NO =? ";
		setString(1,gate.tokenS8AcctNum);
		setString(2,gate.cardNo);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectOempayCard -- can not find V_CARD_NO." + "--");
			blResult = false;
		}
		gb.showLogMessage("D","select oempay create DATE = "+getValue("OempayCredCreateDate")+"TIME = "+getValue("OempayCredCreateTime"));

		return blResult;
	}
	/**
	 * 新增資料到 OEMPAY_CARD for FISC代碼化交易之虛擬卡資料檔
	 * V1.00.27 OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD
	 * V1.00.40 P3檢查行動支付手機黑名單
	 * @return
	 * @throws Exception
	 */
	public boolean insertOempayCard(String slStatusCode) throws Exception{
		gb.showLogMessage("I","insertOempayCard(slStatusCode)! start");

		boolean blResult=true;

		daoTable = addTableOwner("OEMPAY_CARD");

		setValue("CARD_NO",gate.cardNo);//實體卡號
		setValue("V_CARD_NO",gate.tokenS8AcctNum); //虛擬卡號
		setValue("STATUS_CODE",slStatusCode); //虛擬卡狀態 == 狀況代號  0.正常; 1.暫停; 2.中止(人工); 3.中止(實體卡停卡); 4.重複取消; 5.中止(已過效期)
		setValue("NEW_END_DATE",gate.tpanExpire);
		setValue("CRT_DATE",gb.getSysDate());
		
		setValue("EVENT_DATE",gb.getSysDate());
		setValue("EVENT_TIME",gb.getSysTime());
		setValue("ID_P_SEQNO",getValue("CardBaseIdPSeqNo"));
		setValue("ACNO_P_SEQNO",getValue("CardBaseAcnoPSeqNo"));
		setValue("CHANGE_DATE",gb.getSysDate());
		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		setValue("MOD_PGM",gb.getSystemName());
		setValue("TOKEN_PROVIDER",gate.tokenProvider);
		setValue("TOKEN_ASSURANCE_LEVEL",gate.assuranceLevel);
		setValue("TOKEN_REQUESTOR_ID",gate.tokenRequetorId);
		setValue("CONTACTLESS_USAGE",gate.contactlessUsage);
		setValue("EC_USAGE",gate.ecUsage);
		setValue("MOBILE_EC_USAGE",gate.mobileEcUsage);
		setValue("CORRELATION_ID",gate.correlationId);
		setValue("ACTIVE_TOKENS",gate.numOfActiveToken);
		setValue("ISSUE_PRODUCT_ID",gate.issueProductId);
		setValue("CONSUMER_LANGUAGE",gate.consumerLanguage);
		setValue("DEVICE_NAME",gate.deviceName);
		setValue("FINAL_TOKENIZATION_DECISION",gate.finalDecision);
		setValue("FINAL_TOKENIZATION_IND",gate.finalInd);
		setValue("T_C_IDENTIFIER",gate.tcIndentifier);
		setValue("T_C_DATE_TIME",gate.tcDateTime);
		setValue("ACTIVATION_ATTEMPTS",gate.activeAttempts);
		setValue("TOKEN_UNIQUE_REF",gate.tokenUniqueRef);
		setValue("ACCOUNT_NUMBER_REF",gate.acctNumberRef);
		setValue("TOKEN_TYPE",gate.tokenType);
		if (gate.isTokenVTAR || gate.isTokenVTNA ||gate.isTokenVAUT) {
			if (gate.walletIdentifier.length()<=0) {
				gate.walletId = "216";
			}
			else {
				gate.walletId = gate.walletIdentifier;
			}
		}
		setValue("WALLET_IDENTIFIER",gate.walletId);
		setValue("Wallet_ID",gate.walletId);
		setValue("DEVICE_TYPE",gate.deviceType);
		setValue("CELLAR_PHONE",getValue("CrdIdNoCellPhone"));



		insertTable();



		return blResult;


	}
	/**
	 * 新增資料到 OEMPAY_CARD for FISC代碼化交易之虛擬卡資料檔
	 * V1.00.27 OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD
	 * V1.00.43 OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
	 * V1.00.40 P3檢查行動支付手機黑名單
	 * @return
	 * @throws Exception
	 */
	public boolean updateOempayCard(String slStatusCode) throws Exception{
		gb.showLogMessage("I","updateOempayCard()! start");

		boolean blResult=true;
		
		daoTable = addTableOwner("OEMPAY_CARD");
		updateSQL = "STATUS_CODE=?, NEW_END_DATE=?, EVENT_DATE=?, MOD_TIME=?, MOD_PGM=?, CELLAR_PHONE=? ";
		whereStr  = "WHERE V_CARD_NO = ? and CARD_NO =?";

		setString(1, slStatusCode); //虛擬卡狀態 == 狀況代號  0.正常; 1.暫停; 2.中止(人工); 3.中止(實體卡停卡); 4.重複取消; 5.中止(已過效期)
		setString(2, gate.tpanExpire);//虛擬卡到期日
		setString(3, gb.getSysDate());
		setTimestamp(4, gb.getgTimeStamp());
		setString(5, gb.getSystemName());
		setString(6, getValue("CrdIdNoCellPhone"));
		setString(7, gate.tokenS8AcctNum); //虛擬卡號
		setString(8, gate.cardNo);  //實體卡號
		updateTable();

		return blResult;


	}
	public boolean updateOnBatAuthData(ResultSet pOnBatRS,  String spIso38, String spIso39, String spAuthStatusCode) throws Exception {
		gb.showLogMessage("I","updateOnBatAuthData()! start");
		//Howard: 要update onbat_2ccas
		boolean blResult = false;
		StringBuffer sblSql = new StringBuffer();
		int nlParmPos=1;
		String slAuthNo=spIso38;


		if (!"00".equals(spIso39.substring(0,2)))
			slAuthNo = "**";

		daoTable = addTableOwner("onbat_2ccas");


		sblSql.append("PROCESS_STATUS = ? ,");
		setInt(nlParmPos, 1);	
		nlParmPos++;

		sblSql.append("DOP = ? ,");

		//setTimestamp(nL_ParmPos, HpeUtil.getCurTimestamp());	
		//setTimestamp(nL_ParmPos, gate.sG_TransactionStartDateTime);
		setTimestamp(nlParmPos, gb.getgTimeStamp());

		nlParmPos++;

		sblSql.append("PROC_DATE = ? ,");
		setString(nlParmPos, HpeUtil.getCurDateStr(false));	
		nlParmPos++;

		sblSql.append("ISO_RESP_CODE = ? ,");
		setString(nlParmPos, spIso39);	
		nlParmPos++;

		sblSql.append("ICBC_RESP_CODE = ? ,");
		setString(nlParmPos, spAuthStatusCode);	
		nlParmPos++;


		sblSql.append("AUTH_NO = ? ,");
		setString(nlParmPos, slAuthNo);	
		nlParmPos++;





		updateSQL =  sblSql.toString();

		whereStr  = "WHERE  TRANS_TYPE = ? and TRANS_CODE=? and CARD_NO=? andf ACCT_NO=? and PROC_STATUS=? and DOG=? ";



		setString(nlParmPos, pOnBatRS.getString("OnBatTransType"));
		nlParmPos++;

		setString(nlParmPos, pOnBatRS.getString("OnBatTransCode"));
		nlParmPos++;

		setString(nlParmPos, pOnBatRS.getString("OnBatCardNo"));
		nlParmPos++;

		setString(nlParmPos, pOnBatRS.getString("OnBatAcctNo"));
		nlParmPos++;


		setInt(nlParmPos, pOnBatRS.getInt("OnBatProcStatus"));
		nlParmPos++;


		setTimestamp(nlParmPos, pOnBatRS.getString("OnBatDog"));
		nlParmPos++;

		updateTable();



		blResult = true;


		return blResult;
	}

	public boolean updateOnBatAuthDataByRowId(RowId pRowId, String spIso38, String spIso39, String spAuthStatusCode) throws Exception {
		gb.showLogMessage("I","updateOnBatAuthDataByRowId()! start");
		//Howard: 要update onbat_2ccas
		boolean blResult = false;
		StringBuffer sblSql = new StringBuffer();
		int nlParmPos=1;
		String slAuthNo=spIso38;


		if (!"00".equals(spIso39.substring(0,2)))
			slAuthNo = "**";

		daoTable = addTableOwner("onbat_2ccas");


		sblSql.append("PROCESS_STATUS = ? ,");
		setInt(nlParmPos, 1);	
		nlParmPos++;

		sblSql.append("DOP = ? ,");

		//setTimestamp(nL_ParmPos, HpeUtil.getCurTimestamp());	
		//setTimestamp(nL_ParmPos, gate.sG_TransactionStartDateTime);
		setTimestamp(nlParmPos, gb.getgTimeStamp());

		nlParmPos++;

		sblSql.append("PROC_DATE = ? ,");
		setString(nlParmPos, HpeUtil.getCurDateStr(false));	
		nlParmPos++;

		sblSql.append("ISO_RESP_CODE = ? ,");
		setString(nlParmPos, spIso39);	
		nlParmPos++;

		sblSql.append("ICBC_RESP_CODE = ? ,");
		setString(nlParmPos, spAuthStatusCode);	
		nlParmPos++;


		sblSql.append("AUTH_NO = ? ,");
		setString(nlParmPos, slAuthNo);	
		nlParmPos++;





		updateSQL =  sblSql.toString();


		whereStr  = "WHERE  ROWID = ? ";
		setRowId(nlParmPos, pRowId);


		updateTable();



		blResult = true;


		return blResult;
	}

	public boolean updateOutgoingAuthData(RowId pRowId, String spIso38, String spIso39, String spAuthStatusCode) throws Exception {
		gb.showLogMessage("I","updateOutgoingAuthData()! start");
		//Howard: 要update onbat_2ccas
		boolean blResult = false;
		StringBuffer sblSql = new StringBuffer();
		int nlParmPos=1;
		String slAuthNo=spIso38;


		if (!"00".equals(spIso39.substring(0,2)))
			slAuthNo = "**";

		daoTable = addTableOwner("onbat_2ccas");


		sblSql.append("PROCESS_STATUS = ? ,");
		setInt(nlParmPos, 1);	
		nlParmPos++;

		sblSql.append("DOP = ? ,");
		//setTimestamp(nL_ParmPos, HpeUtil.getCurTimestamp());
		//setTimestamp(nL_ParmPos, gate.sG_TransactionStartDateTime);
		setTimestamp(nlParmPos, gb.getgTimeStamp());

		nlParmPos++;

		sblSql.append("PROC_DATE = ? ,");
		setString(nlParmPos, HpeUtil.getCurDateStr(false));	
		nlParmPos++;

		sblSql.append("ISO_RESP_CODE = ? ,");
		setString(nlParmPos, spIso39);	
		nlParmPos++;

		sblSql.append("ICBC_RESP_CODE = ? ,");
		setString(nlParmPos, spAuthStatusCode);	
		nlParmPos++;


		sblSql.append("AUTH_NO = ? ,");
		setString(nlParmPos, slAuthNo);	
		nlParmPos++;





		updateSQL =  sblSql.toString();

		whereStr  = "WHERE  ROWID = ? ";
		setRowId(nlParmPos, pRowId);


		updateTable();



		blResult = true;


		return blResult;
	}

	public boolean updateIbmOutgoingAuthData( String spSrcCrtDate, String spSrcCrtTime, String spSrcSeqNo) throws Exception {
		gb.showLogMessage("I","updateIbmOutgoingAuthData()! start");
		//Howard: 要update CCA_IBA_OUTGOING
		//Howard: 這個 sql 需要再與仁和確認

		boolean blResult = false;
		daoTable = addTableOwner("CCA_IBM_OUTGOING");
		String slSql = " PROC_FLAG= ? ";
		updateSQL =  slSql.toString();

		whereStr  = "WHERE  CRT_DATE = ? and CRT_TIME= ? and SEQ_NO= ? ";
		setString(1, "C"); 
		setString(2, spSrcCrtDate);
		setString(3, spSrcCrtTime);
		setInt(4, Integer.parseInt(spSrcSeqNo));



		updateTable();



		blResult = true;


		return blResult;
	}

	public ResultSet getOnBatAuthData()throws Exception {
		gb.showLogMessage("I","getOnBatAuthData()! start");

		/*
	   selectSQL = "SELECT  ROWID as OnBatRowId,NVL(CARD_NO,' ') as OnBatCardNo,NVL(TRANS_DATE,'00000000') as OnBatTransDate, "
                     +"NVL(TRANS_AMT,0) as OnBatTransAmt,NVL(MCC_CODE,'    ') as OnBatMccCode, "
                     +"NVL(CARD_VALID_TO,'00000000') as OnBatCardValidTo,NVL(CVC2_CODE,'    ') as OnBatCvc2Code,"
                     +"NVL(VOICE_PIN,'      ') as OnBatVoicePin,NVL(ACTIVE_PIN,'      ') as OnBatActivePin,"
                     +"NVL(TRANS_CODE,'01') as OnBatTransCode, NVL(REFE_NO,'0000000000') as OnBatRefeNo,"
                     +"NVL(TELE_NO,' ') as OnBatTeleNo,NVL(CONTRACT_NO,' ') as OnBatContractNo ,"
                     +"NVL(TO_CHAR(DOG,'MMDDhhmmss') as OnBat,'0000000000') as OnBatDog,"
                     +"NVL(substr(ICBC_RESP_DESC,1,15),'NVL') as OnBatIcbcRespDesc ";


	   daoTable ="onbat_2ccas";
	   whereStr="WHERE TRANS_TYPE =? and TO_WHICH=? and PROCESS_STATUS=? ";
	   String sL_TransType="1";
	   int nL_ToWhich=2, nL_ProcStatus=0;

       setString(1,sL_TransType);
       setInt(2, nL_ToWhich);
       setInt(3, nL_ProcStatus);
		 */


		selectSQL = "SELECT  NVL(CARD_NO,' ') as OnBatCardNo,NVL(TRANS_DATE,'00000000') as OnBatTransDate, "
				+"NVL(TRANS_AMT,0) as OnBatTransAmt,NVL(MCC_CODE,'    ') as OnBatMccCode, "
				+"NVL(CARD_VALID_TO,'00000000') as OnBatCardValidTo,NVL(CVC2_CODE,'    ') as OnBatCvc2Code,"
				+"NVL(VOICE_PIN,'      ') as OnBatVoicePin,NVL(ACTIVE_PIN,'      ') as OnBatActivePin,"
				+"NVL(TRANS_CODE,'01') as OnBatTransCode, NVL(REFE_NO,'0000000000') as OnBatRefeNo,"
				+"NVL(TELE_NO,' ') as OnBatTeleNo,NVL(CONTRACT_NO,' ') as OnBatContractNo ,"
				+"NVL(TO_CHAR(DOG,'MMDDhhmmss') as OnBat,'0000000000') as OnBatDog,"
				+"NVL(substr(ICBC_RESP_DESC,1,15),'NVL') as OnBatIcbcRespDesc, "
				+ "TRANS_TYPE as OnBatTransType, "
				+ "ACCT_NO as OnBatTransAcctNo, PROC_STATUS as OnBatProcStatus ";


		daoTable = addTableOwner("onbat_2ccas");
		whereStr="WHERE TRANS_TYPE =? and TO_WHICH=? and PROCESS_STATUS=? ";
		String slTransType="1";
		int nlToWhich=2, nlProcStatus=0;

		setString(1,slTransType);
		setInt(2, nlToWhich);
		setInt(3, nlProcStatus);

		ResultSet lRS = getTableResultSet();


		return lRS;
	}

	public ResultSet getOutgoingAuthData()throws Exception {
		gb.showLogMessage("I","getOutgoingAuthData()! start");


		selectSQL = "SELECT  MSG_HEADER,  MSG_TYPE, ISOFIELD_002 , ISOFIELD_007 , "
				+"ISOFIELD_011, ISOFIELD_048,ISOFIELD_049, ISOFIELD_060, "
				+"ISOFIELD_073, ISOFIELD_091,ISOFIELD_101,"
				+"ISOFIELD_120, CARD_NO,  SEND_TIMES,   ACT_CODE  ";

		daoTable = addTableOwner("cca_outgoing");
		whereStr="WHERE PROC_FLAG =?  ";



		setString(1,"N");


		ResultSet lRS = getTableResultSet();


		return lRS;
	}

	public ResultSet getIbmOutgoingAuthData()throws Exception {
		gb.showLogMessage("I","getIbmOutgoingAuthData()! start");

		//Howard: 這個 sql 需要再與仁和確認
		selectSQL ="NVL(CRT_DATE,' ') as IbmOutgoingCrtDate, "
				+ "NVL(CRT_TIME, ' ') as IbmOutgoingCrtTime, "
				+ "NVL(SEQ_NO,0) as IbmOutgoingSeqNo, "
				+ "NVL(CARD_NO,' ') as IbmOutgoingCardNo, "
				+ "NVL(KEY_TABLE,' ') as IbmOutgoingKeyTable, "
				+ "NVL(BITMAP,' ') as IbmOutgoingBitMap, "
				+ "NVL(PROC_FLAG,'0') as IbmOutgoingProcFlag, "
				+ "NVL(SEND_TIMES,0) as IbmOutgoingSendTimes, "
				+ "NVL(ACT_CODE,'A') as IbmOutgoingActCode " ;


		daoTable = addTableOwner("CCA_IBM_OUTGOING");
		whereStr="where PROC_FLAG <> ? and SEND_TIMES<=?  order by IbmOutgoingCrtDate, IbmOutgoingCrtTime, IbmOutgoingSendTimes ";


		int nlSendTimes = 10;

		setString(1, "C");
		setInt(2, nlSendTimes);




		ResultSet lRS = getTableResultSet();


		return lRS;
	}


	public boolean selectPtrCardType() throws Exception {
		gb.showLogMessage("I","selectPtrCardType()! start");

		boolean blResult = true;
		daoTable = addTableOwner("PTR_CARD_TYPE");	

		String slCardType = getValue("CARD_TYPE");
		//sL_CardType = "AX"; //for test

		selectSQL ="Name as PtrCardTypeName," //
				//			   		+ "BIN_NO,"
				+ "CARD_NOTE," 		//
				+ "RDS_PCARD,"		// 
				+ "CARD_NOTE_JCIC,"  // 
				+ "TOP_CARD_FLAG," 	//
				+ "SORT_TYPE,"  		//
				+ "NEG_CARD_TYPE," 	// 
				+ "OUT_GOING_TYPE"; // 

		whereStr="WHERE CARD_TYPE =? ";
		setString(1,slCardType);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectPtrCardType -- can not find card type." + "--");
			blResult = false;

		}


		return blResult;

	}

	public void selectAcctType(boolean bpIsDebitCard, String spAcctType) throws Exception {
		gb.showLogMessage("I","selectAcctType()! start");

		if (bpIsDebitCard) {
			daoTable =addTableOwner("DBP_ACCT_TYPE");
		}
		else {
			daoTable = addTableOwner("PTR_ACCT_TYPE");
		}

		selectSQL ="CARD_INDICATOR as PtrAcctTypeCardIndicator"; //

		whereStr="WHERE ACCT_TYPE =? ";
		setString(1,spAcctType);

		selectTable();
		if ( "Y".equals(notFound) ) {
			setValue("PtrAcctTypeCardIndicator", "1");//一般卡
			notFound="N";


		}

		gb.showLogMessage("D","CARD_INDICATOR =>" + getValue("PtrAcctTypeCardIndicator").toString());


	}

	public int selectCcaGroupMcht(String spMchtFlag) throws Exception {
		gb.showLogMessage("I","selectCcaGroupMcht()! start");

		int nlResult =0;
		daoTable  = addTableOwner("CCA_GROUP_MCHT"); 
		selectSQL = "COUNT(*) as CcaGroupMchtCount";

		if ("M".equals(spMchtFlag)) {
			whereStr  = "WHERE GROUP_code = ? AND  acq_bank_id  = ? AND  mcht_no = ?";
			String slGroupCode = gate.groupCode;
			String slIsoField18 = gate.isoField[18];
			String slMerchantNo = gate.merchantNo;
			
			setString(1,slGroupCode);
			setString(2,slIsoField18);
			setString(3,slMerchantNo); 
		}
		else {
			whereStr  = "WHERE GROUP_code = ? AND  acq_bank_id  = ?";
			String slGroupCode = gate.groupCode;
			String slIsoField18 = gate.isoField[18];
			
			setString(1,slGroupCode);
			setString(2,slIsoField18);
		}

		selectTable();

		if (getInteger("CcaGroupMchtCount")==0) {

			daoTable  = addTableOwner("CCA_GROUP_MCHT"); 
			selectSQL = "COUNT(*) as CcaGroupMchtCount";

			whereStr  = "WHERE GROUP_code = ? ";
			String slGroupCode = gate.groupCode;

			setString(1,slGroupCode);
			selectTable();
			if (getInteger("CcaGroupMchtCount")==0) {
				nlResult=1; //抓取不到資料,則需檢核機構代碼,繼續往下走 
			}
			else
				nlResult=-1; ///*DF配銷卡非在指定特店消費*/
		}
		else
			nlResult=2;//以 8583的 merchant no 為key，有找到資料

		return nlResult;
	}
	/**
	 * 交易累計次數須包含本次交易
	 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public int selectCcaAuthTxLog4SmsInd(String spAreaType) throws Exception {
		gb.showLogMessage("I","selectCcaAuthTxLog4SmsInd()! start");

		int nlResult =0;

		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
		selectSQL = "count(*) as TxCount4SmsInd ";

		if (gate.ecTrans) {
			if ("3".equals(spAreaType)) {
				whereStr ="where card_no =?"
						+" and tx_date=?"
						+" and cacu_amount<>?"
						+" and ec_ind = ?";
				setString(1,gate.cardNo);
				setString(2,HpeUtil.getCurDateStr(false));
				setString(3,"N");
				setString(4,"Y");
			}
			else {
				whereStr ="where card_no =?"
						+" and tx_date=?"
						+" and cacu_amount<>?"
						+" and ec_ind = ?"
						+" and ccas_area_flag = ?";
				setString(1,gate.cardNo);
				setString(2,HpeUtil.getCurDateStr(false));
				setString(3,"N");
				setString(4,"Y");
				if ("1".equals(spAreaType)) {
					setString(5,"F");
				}
				else {
					setString(5,"T");
				}
			}
		}
		else {
			if ("3".equals(spAreaType)) {
				whereStr ="where card_no =?"
						+" and tx_date=?"
						+" and iso_resp_code =?"
						+" and trans_code = ?";
				setString(1,gate.cardNo);
				setString(2,HpeUtil.getCurDateStr(false));
				setString(3,"00");
				setString(4,gate.transCode);
			}
			else {
				whereStr ="where card_no =?"
						+" and tx_date=?"
						+" and iso_resp_code =?=?"
						+" and trans_code = ?"
						+" and ccas_area_flag = ?";
				setString(1,gate.cardNo);
				setString(2,HpeUtil.getCurDateStr(false));
				setString(3,"00");
				setString(4,gate.transCode);
				if ("1".equals(spAreaType)) {
					setString(5,"F");
				}
				else {
					setString(5,"T");
				}
			}
		}


		selectTable();

		nlResult = getInteger("TxCount4SmsInd");

		return nlResult;
	}
	public boolean selectCcaAuthTxLog4RiskChecking() throws Exception {
		gb.showLogMessage("I","selectCcaAuthTxLog4RiskChecking()! start");

		boolean blResult =true;


		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
		selectSQL = "count(*) as TxCount4RiskChecking, NVL(sum(nt_amt),0) as TotalTxAmt4RiskChecking";

		whereStr  = "WHERE card_no=? and tx_date=? and  mcht_no = ? group by TX_DATE";







		setString(1,gate.cardNo);
		setString(2,HpeUtil.getCurDateStr(false));
		setString(3,gate.merchantNo);


		selectTable();

		if (getInteger("TxCount4RiskChecking")==0)		 
			blResult = false; //表示當天沒有在 該特店 消費

		return blResult;
	}
	/**
	 * 取得OEMPAY卡片海外交易累計金額及小時參數
	 * V1.00.43 OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectPrdTypeIntrOempay() throws Exception {
		gb.showLogMessage("I","selectPrdTypeIntrOempay()! start");
	
		boolean blResult =true;
		String slCardNote=getValue("CardBaseCardNote");

		getPrdTypeIntr("F", "T",slCardNote);
		
		if ( "Y".equals(notFound) ) {
			slCardNote = "*";
			getPrdTypeIntr("F", "T",slCardNote);
			if ( "Y".equals(notFound) ) {
				return false;
			}
		}

		return blResult;
	}
	/**
	 * OEMPAY海外交易累計次數須包含本次交易
	 * V1.00.43 OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectCcaAuthTxLog4OempayOversea(Timestamp tpTimestamp) throws Exception {
		gb.showLogMessage("I","selectCcaAuthTxLog4OempayOversea()! start");

		boolean blResult =true;

		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
		selectSQL = "count(*) as TxCount4Oempay, NVL(sum(nt_amt),0) as TotalTxAmt4Oempay";

		whereStr  = "WHERE card_no=? and V_CARD_NO=? and cacu_amount<>? and tx_datetime > ? and POS_MODE like ? ";

		setString(1,gate.cardNo);
		setString(2,gate.tokenS8AcctNum);
		setString(3,"N");
		setTimestamp(4,tpTimestamp);
		setString(5,"07%");


		selectTable();
		gb.showLogMessage("D","TotalTxAmt4Oempay+ntAmt = "+(getDouble("TotalTxAmt4Oempay")+gate.ntAmt)+" TxCount4Oempay+1 = "+(getInteger("TxCount4Oempay")+1)+" Create timestamp ="+tpTimestamp);

		if ((getDouble("TotalTxAmt4Oempay") + gate.ntAmt) > getDouble("INT_TOT_LMT_CNT_DAY")) {	 
			blResult = false; //Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
		}
		return blResult;
	}
	
	private int getBaseLimit4AllTypeCard(){
		int dlRealBaseLimit =0;
		if (gate.businessCard) {
			dlRealBaseLimit = getBaseLimitOfComp();
		}
		else {
			dlRealBaseLimit = getBaseLimit();
		}
		return dlRealBaseLimit;

	}
	public int getBaseLimit() {
		int dlResult = 0;

		if (gate.isDebitCard)
			//改讀 UI 5250 寫入的 table : cca_debit_parm
			dlResult = getInteger("DebitParmMonthAmount"); /*該戶之基本額度*/ //=> Howard(20190705): JH 說debit card 基本額度看這裡
		else 
			dlResult = getInteger("ActAcnoLineOfCreditAmt"); /*該戶之基本額度*/
		return dlResult;
	}

	public int getBaseLimitOfComp() {
		int dlResult = 0;

		if (gate.isDebitCard)
			dlResult = getInteger("DebitParmMonthAmount"); /*該戶之基本額度*/ //=> Howard(20190705): JH 說debit card 額度不是看這裡
		else 
			dlResult = getInteger("ActAcnoLineOfCreditAmtOfComp"); /*該戶之基本額度*/
		return dlResult;
	}

	public boolean getMchtRisk(String spMchtNo, String spAcqBankId, String spMccCode) {
		boolean blResult=true;

		try {
			daoTable  = addTableOwner("CCA_MCHT_RISK"); 
			selectSQL = "MCHT_RISK_CODE as MchtRiskMchtRiskCode," /* OnUs特店風險等級 */
					+ "RISK_START_DATE as MchtRiskRiskStartDate, " /* 有效日期(起)     */
					+ "RISK_END_DATE as MchtRiskRiskEndDate, "  /* 有效日期(迄)     */
					+ "NVL(AUTH_AMT_S,0) as MchtRiskAuthAmtS, " /* 金額區間-s       */
					+ "NVL(AUTH_AMT_E,0) as MchtRiskAuthAmtE, " /* 金額區間-e       */
					+ "EDC_POS_NO1 as MchtRiskEdcPosNo1, "/* 端未機編號-1     */
					+ "EDC_POS_NO2 as MchtRiskEdcPosNo2, "/* 端未機編號-2     */
					+ "EDC_POS_NO3 as MchtRiskEdcPosNo3, "/* 端未機編號-3     */
					+ "NVL(DAY_LIMIT_CNT,0) as MchtRiskDayLimitCnt, " /* 日累積限制筆數   */
					+ "NVL(AUTH_AMT_RATE,0) as MchtRiskAuthAmtRate, " /* 管制金額%        */
					+ "NVL(DAY_TOT_AMT,0) as MchtRiskDayTotAmt, " /* 管制日累計金額   */
			        + "RISK_FACTOR as MchtRiskFactor"; /* kevin: 取得風險分數 riskFactor */
			whereStr  = "WHERE  MCHT_NO = ? and ACQ_BANK_ID = ? and MCC_CODE= ? ";


			setString(1,spMchtNo); /* 特店代碼         */



			setString(2,spAcqBankId); /* 收單行代碼       */


			setString(3,spMccCode);/* MCC_CODE         */
			selectTable();
			if ( "Y".equals(notFound) ) {
				blResult=false;
			}
			else {
				gate.mchtRiskFactor = getDouble("MchtRiskFactor");
			}

		} catch (Exception e) {
			// TODO: handle exception
			blResult=false;
			gb.showLogMessage("E","Exception on getMchtRisk(): Exception=>" + e.getMessage());
		}
		gate.riskFctorInd = 4; //kevin: 4. 計算 黑名單特店 風險分數
		return blResult;
	}

	/**
	 * 取得黑名單特店的風險分數
	 * V1.00.49 風險特店調整及新增特殊特店名稱檢查(eToro)
	 * @return return AcqBankId
	 * @throws Exception if any exception occurred
	 */
	public void getMchtRiskScore() throws Exception {
		
		gb.showLogMessage("I","getMchtRiskScore()! start");
		//授權參數檔不檢核風險特店、非購貨交易或預借現金，例如三大票證來源，不需檢查特店
		if ("0".equals(getValue("AuthParmMchtChk")) || gate.nonPurchaseTxn || gate.ticketTxn) {
			return;
		}
		String slMchtNo= gate.merchantNo; /* 特店代碼         */
		String slAcqBankId = getAcqBankId(gate.isoField[32]); /* 收單行代碼       */
		String slMccCode= gate.mccCode;

		getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
		gb.showLogMessage("D","getMchtRiskScore()-1，MCHT_NO="+ gate.merchantNo);
		gb.showLogMessage("D","getMchtRiskScore()-1，ACQ_BANK_ID="+slAcqBankId);
		gb.showLogMessage("D","getMchtRiskScore()-1，MCC_CODE="+slMccCode);
		if ( notFound.equals("Y") ) {
			gb.showLogMessage("D","getMchtRiskScore()-1，沒找到資料，用MCC_CODE=*，再找一次");
			slMccCode="*";
			getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
			gb.showLogMessage("D","getMchtRiskScore()-2，MCHT_NO="+ gate.merchantNo);
			gb.showLogMessage("D","getMchtRiskScore()-2，ACQ_BANK_ID="+slAcqBankId);
			gb.showLogMessage("D","getMchtRiskScore()-2，MCC_CODE="+slMccCode);
			if ( notFound.equals("Y") ) {
				gb.showLogMessage("D","getMchtRiskScore()-2，沒找到資料，表示沒有管制，所以允許交易!");
			}
		}
	}

	/**
	 * 取得收單代號，根據BIN TYPE決定收單代號長度
	 * V1.00.22 風險特店所需之收單代碼資料長度根據不同卡別調整
	 * @return return AcqBankId
	 * @throws Exception if any exception occurred
	 */
	public String getAcqBankId(String spIsoField32) {
		String slAcqBankId = spIsoField32.trim(); 
		if (spIsoField32.length()>6) {
			slAcqBankId = spIsoField32.trim().substring(0, 6);
			if ("J".equals(getValue("CardBinType").toUpperCase(Locale.TAIWAN))) {
				slAcqBankId = spIsoField32.trim().substring(0, 8);
			}
		}
		return slAcqBankId;
	}
	// 特店風險註記 CCA_MCHT_RISK_DETL
	public boolean isMchtRiskDetl4ExceptionCard() throws Exception {
		gb.showLogMessage("I","isMchtRiskDetl4ExceptionCard()! start");


		/*
BRD第2.3.4規則，新增欄位：額度百分比(與金額區間取孰低)
與新增管制MCC CODE欄位與【排除卡號(白名單)】
新增維護欄位：開放迄日(DEFAULT當日)及日累計金額。

故要調整判斷授權規則
據王sir表示，以前系統只抓收單行及特店代碼相同就管制，
此次修改可新增多筆同一特店不同mcc code，
所以授權邏輯要改多判斷一個條件mcc code.

		 * */
		boolean blResult =true;

		//Howard: 檢核排除卡號(白名單)，如果有資料，就表示為排除卡號
		daoTable  = addTableOwner("CCA_MCHT_RISK_DETL"); 
		selectSQL = "COUNT(*) as MchtRiskDetlCount";

		whereStr  = "WHERE  mcht_no = ? and acq_bank_id = decode(?,'493817','493817','400996') and data_type=? and data_code=? and ( mcc_code=? or mcc_code=? ) ";



		setString(1,gate.merchantNo);

		String slAcqBankId =getAcqBankId(gate.isoField[32]);
		setString(2,slAcqBankId);
		setString(3,"1"); 
		setString(4,gate.cardNo);
		setString(5,gate.mccCode); //Howard: 20190103 加入
		setString(6, "*"); //Howard: 20190103 加入
		selectTable();

		//gb.showLogMessage("D","merchant No=>" + gate.merchantNo);
		//gb.showLogMessage("D","isoField[32]=>" + gate.isoField[32]);
		//gb.showLogMessage("D","Card No=>" + gate.cardNoMask);
		//gb.showLogMessage("D","mcc Code=>" + gate.mccCode);

		if (getInteger("MchtRiskDetlCount")==0) {

			//gb.showLogMessage("D","檢核結果=>於　CCA_MCHT_RISK_DETL　找不到資料，所以此卡號不是排除卡號");
			blResult = false;
		}
		else {
			//gb.showLogMessage("D","檢核結果=>於CCA_MCHT_RISK_DETL找到資料");
			//gb.showLogMessage("D","檢核結果=>所以此卡號是排除卡號");
		}

		return blResult;
	}

	/**
	 * 國外POS ENTRY MODE(90、02)交易累計次數
	 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectCcaAuthTxLog4ForeignTrade() throws Exception {
		gb.showLogMessage("I","selectCcaAuthTxLog4ForeignTrade()! start");

		boolean blResult =true;


		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
		selectSQL = "COUNT(*) as AuthTxLogCount4ForeignTrade";

		whereStr  = "WHERE card_no=? and ccas_area_flag =? and substr(pos_mode,1,2) in (?,?) and CACU_AMOUNT<> ? and trunc((sysdate-to_date(?,'YYYYMMDDHH24MISS'))*24*60) <= 10";

		String slCurDateTime = HpeUtil.getCurDateTimeStr(false,false);
		gb.showLogMessage("D","slCurDateTime = "+slCurDateTime);

		setString(1,gate.cardNo);
		setString(2,"F");
		setString(3,"02");
		setString(4,"90");
		setString(5,"N"); 
		setString(6,slCurDateTime);

		selectTable();


		return blResult;
	}

	/**
	 * 取得日交易額與交易次數
	 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectCcaAuthTxLog() throws Exception {
		gb.showLogMessage("I","selectCcaAuthTxLog()! start");

		boolean blResult =true;


		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
//		selectSQL = "COUNT(*) as AuthTxLogCount";
		selectSQL = "Nvl(COUNT(*),0) as AuthTxLogDayCount, Nvl(sum(NT_AMT),0) as AuthTxLogDayAmount"; //本日累積交易次數與金額， 依MchtNo統計

//		whereStr  = "WHERE card_no=? and tx_date=? and  mcht_no = ? and NVL(CACU_AMOUNT,'N') = ?";
		whereStr  = "WHERE CARD_NO = ? and ISO_RESP_CODE=? and TX_DATE=? and MCHT_NO= ? and NVL(CACU_AMOUNT,'N') <> ?";


		setString(1,gate.cardNo);
		setString(2,"00"); 
		setString(3,HpeUtil.getCurDateStr(false));
		setString(4,gate.merchantNo);
		setString(5,"N"); 

		selectTable();
		
		if (getInteger("AuthTxLogDayCount")==0)
			blResult = false;

		return blResult;
	}


	//DEBIT非面對面刷卡特店 CCA_MCHT_NOTONLINE
	public boolean selectCcaMchtNotOnLine() throws Exception {
		gb.showLogMessage("I","selectCcaMchtNotOnLine()! start");

		boolean blResult =true;


		daoTable  = addTableOwner("CCA_MCHT_NOTONLINE"); 
		selectSQL = "apr_date";

		whereStr  = "WHERE acq_id = ? and mcht_no=? and apr_date  is not null and ? between nvl(online_date,'20010101') and nvl(stop_date  ,'29991231') and apr_flag=? ";


		setString(1,"");
		//setString(2,"");
		setString(2,gate.merchantNo);
		setString(3,gb.getSysDate());
		setString(4,"Y"); //已覆核

		selectTable();

		if ( "Y".equals(notFound) ) 
			blResult = false;

		return blResult;
	}

	// Ū CCA_AUTH_PARM
	public boolean selectAuthParm() throws Exception {
		gb.showLogMessage("I","selectAuthParm()! start");


		//String sL_CardNote=getValue("PtrCardTypeCardNote"); 
		String slCardNote=getValue("CardBaseCardNote");

		String slAreaType=gate.areaType;
		//kevin:此參數沒有分國內外，一律使用國內:T
		slAreaType="T";
		String slCurDate=HpeUtil.getCurDateStr(false);
		boolean blResult =true;
		daoTable  = addTableOwner("CCA_AUTH_PARM");  //授權參數檔 
		selectSQL = "open_chk as AuthParmOpenChk,"				//是否要做 開卡 檢核
				+ "mcht_chk as AuthParmMchtChk,"				//是否要做 風險特店  檢核 => 2017/11/14 還沒用到此變數
				+ "delinquent as AuthParmDelinquent,"			//是否要做 Delinquent 檢核 
				+ "oversea_chk as AuthParmOverseaChk,"			//是否要做 國外消費 檢核 => 2017/11/14
				+ "avg_consume_chk as AuthParmAvgConsumeChk,"  //是否要做 平均消費 檢核  => 2017/11/14 還沒用到此變數
				+ "month_risk_chk as AuthParmMonthRiskChk," //是否要做 月限次 風險檢核
				+ "day_risk_chk as AuthParmDayRiskChk,"	//是否要做 日限次 風險檢核
				+ "oversea_cash_pct as AuthParmOverseaCashPct," //國外預借現金%
				+ "over_add_amt_mm ";  /* 月限超額金額  */ // +月限額=可用額度




		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  end_date >= ?";


		//sL_AreaType="T"; //for test
		setString(1,slCardNote);
		setString(2,slAreaType);
		setString(3,slCurDate); 
		selectTable();

		gb.showLogMessage("D","function: TA.selectAuthParm-1 -- sL_CardNote is=>"+slCardNote + "--");
		gb.showLogMessage("D","function: TA.selectAuthParm-1 -- sL_AreaType is=>"+slAreaType + "--");
		gb.showLogMessage("D","function: TA.selectAuthParm-1 -- sL_CurDate is=>"+slCurDate + "--");


		if ( "Y".equals(notFound) ) {

			daoTable  = addTableOwner("CCA_AUTH_PARM");  //授權參數檔 
			selectSQL = "open_chk as AuthParmOpenChk,"				//是否要做 開卡 檢核
					+ "mcht_chk as AuthParmMchtChk,"				//是否要做 風險特店  檢核 => 2017/11/14 還沒用到此變數
					+ "delinquent as AuthParmDelinquent,"			//是否要做 Delinquent 檢核 
					+ "oversea_chk as AuthParmOverseaChk,"			//是否要做 國外消費 檢核 => 2017/11/14
					+ "avg_consume_chk as AuthParmAvgConsumeChk,"  //是否要做 平均消費 檢核  => 2017/11/14 還沒用到此變數
					+ "month_risk_chk as AuthParmMonthRiskChk," //是否要做 月限次 風險檢核
					+ "day_risk_chk as AuthParmDayRiskChk,"	//是否要做 日限次 風險檢核
					+ "oversea_cash_pct as AuthParmOverseaCashPct," //國外預借現金%
					+ "over_add_amt_mm ";  /* 月限超額金額  */ // +月限額=可用額度


			whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  end_date >= ? ";
			slCardNote = "*";
			setString(1,slCardNote);
			setString(2,slAreaType);
			setString(3,slCurDate); 


			gb.showLogMessage("D","function: TA.selectAuthParm-2 -- sL_CardNote is=>"+slCardNote + "--");
			gb.showLogMessage("D","function: TA.selectAuthParm-2 -- sL_AreaType is=>"+slAreaType + "--");
			gb.showLogMessage("D","function: TA.selectAuthParm-2 -- sL_CurDate is=>"+slCurDate + "--");


			selectTable();




			if ( "Y".equals(notFound) ) {
				blResult=false;
			}
		}

		if (blResult)  {
			//gb.showLogMessage("D","AuthParmOpenChk=>" + getValue("AuthParmOpenChk"));
			//gb.showLogMessage("D","AuthParmMchtChk=>" + getValue("AuthParmMchtChk"));
			//gb.showLogMessage("D","AuthParmDelinquent=>" + getValue("AuthParmDelinquent"));
			//gb.showLogMessage("D","AuthParmOverseaChk=>" + getValue("AuthParmOverseaChk"));
			//gb.showLogMessage("D","AuthParmAvgConsumeChk=>" + getValue("AuthParmAvgConsumeChk"));
			//gb.showLogMessage("D","AuthParmMonthRiskChk=>" + getValue("AuthParmMonthRiskChk"));

			//gb.showLogMessage("D","AuthParmDayRiskChk=>" + getValue("AuthParmDayRiskChk"));
			//gb.showLogMessage("D","AuthParmOverseaCashPct=>" + getValue("AuthParmOverseaCashPct"));




		}


		return blResult;
	}


	private void getPrdTypeIntr(String spAreaType, String spPrdAttrib, String spCardNote) throws Exception{
		daoTable  = addTableOwner("CCA_PRD_TYPE_INTR");   //產品類別檔
		selectSQL = "NVL(NAT_LMT_AMT_DAY,0) as NAT_LMT_AMT_DAY," //小額日限額
				+ "NVL(NAT_LMT_CNT_DAY,0) as NAT_LMT_CNT_DAY," //小額日限次
				+ "NVL(INT_N_LMT_AMT_MONTH,0) as INT_N_LMT_AMT_MONTH," //一般消費月限額
				+ "NVL(INT_N_LMT_AMT_TIME,0) as INT_N_LMT_AMT_TIME," //一般消費(小額)次限額
				+ "NVL(INT_N_LMT_CNT_MONTH,0) as INT_N_LMT_CNT_MONTH," //一般消費月限次
				+ "NVL(INT_N_LMT_CNT_DAY,0) as INT_N_LMT_CNT_DAY," //一般消費日限次
				+ "NVL(INT_C_LMT_AMT_MONTH,0) as INT_C_LMT_AMT_MONTH," //預借現金月限額
				+ "NVL(INT_C_LMT_AMT_TIME,0) as INT_C_LMT_AMT_TIME," //預借現金次限額
				+ "NVL(INT_C_LMT_CNT_MONTH,0) as INT_C_LMT_CNT_MONTH," //預借現金月限次
				+ "NVL(INT_C_LMT_CNT_DAY,0) as INT_C_LMT_CNT_DAY," //預借現金日限次
				+ "NVL(INT_LMT_AMT_MON,0) as INT_LMT_AMT_MON," //總消費月限額
				+ "NVL(INT_LMT_AMT_TIME,0) as INT_LMT_AMT_TIME," //總消費次限額  (不會用到)
				+ "NVL(INT_LMT_CNT_MON,0) as INT_LMT_CNT_MON," //OEMPAY Token國外交易之管控綁定後幾小時內
				+ "NVL(INT_TOT_LMT_CNT_DAY,0) as INT_TOT_LMT_CNT_DAY "; //OEMPAY Token國外交易之管控綁定後幾小時內交易金額限制

		whereStr  = "WHERE AREA_TYPE=? " /* 國內外交易            -- F.國外交易一般參數, T.小額交易 */
				+ "AND PRD_ATTRIB=? "  /* 產品屬性:             -- P.產品類別設定, T.OEMPAY Token交易設定 */
				+ "AND CARD_NOTE=? ";/* 卡片等級             */


		setString(1,spAreaType);
		setString(2,spPrdAttrib);
		setString(3,spCardNote); 
		selectTable();

	}
	//讀取CCA_PRD_TYPE_INTR
	public boolean selectPrdTypeIntr(String spAmtRule) throws Exception {
		gb.showLogMessage("I","selectPrdTypeIntr()! start");

		String slAreaType=gate.areaType;

		//String sL_PrdTpe=gate.sG_CardProd;//卡號前 10 碼 

		String slCurDate=HpeUtil.getCurDateStr(false);
		boolean blResult =true;



		String slCardNote=getValue("CardBaseCardNote");
		String slPrdAttrib = "P";

		getPrdTypeIntr(slAreaType, slPrdAttrib,slCardNote);
		if ( "Y".equals(notFound) ) {

			/* Howard : marked on 20190104
        	sL_PrdTpe=gate.sG_CardProd.substring(0, 6) + "0000";//de6X + "0000"

            setString(4,sL_PrdTpe);
			 */
			slCardNote = "";
			getPrdTypeIntr(slAreaType, slPrdAttrib,slCardNote);



			if ( "Y".equals(notFound) ) {
				blResult=false;
			}
			/*
            else
            	gate.sG_UsedCardProd = sL_PrdTpe; //Howard:20190104 marked
			 */
		}

		double dlTmpAdjTot = getDouble("INT_LMT_AMT_MON")/100;
		double dlTmpAdjCnt = 0;
		gate.totalLimit = gate.totalLimit * dlTmpAdjTot;//該產品臨調總月限額倍數

		if ("C".equals(spAmtRule)) {
			gate.monthLimit = gate.monthLimit * (getDouble("INT_C_LMT_AMT_MONTH")/100);  //取得臨調總月限額
			gate.timesLimit = gate.timesLimit * (getDouble("INT_C_LMT_AMT_TIME")/100); //取得臨調總次限額

			dlTmpAdjCnt = getDouble("INT_C_LMT_CNT_MONTH")/100;
			gate.monthCntLimit = gate.monthCntLimit * dlTmpAdjCnt;//取得臨調總月限次

			dlTmpAdjCnt = getDouble("INT_C_LMT_CNT_DAY")/100;
			gate.timesCntLimit = gate.timesCntLimit*dlTmpAdjCnt;//取得臨調總日限次
		}
		else {
			gate.monthLimit = gate.monthLimit * (getDouble("INT_N_LMT_AMT_MONTH")/100);  //取得臨調總月限額
			gate.timesLimit = gate.timesLimit * (getDouble("INT_N_LMT_AMT_TIME")/100); //取得臨調總次限額

			dlTmpAdjCnt = getDouble("INT_N_LMT_CNT_MONTH")/100;
			gate.monthCntLimit = gate.monthCntLimit * dlTmpAdjCnt;//取得臨調總月限次

			dlTmpAdjCnt = getDouble("INT_N_LMT_CNT_DAY")/100;
			gate.timesCntLimit = gate.timesCntLimit*dlTmpAdjCnt;//取得臨調總日限次

		}
		return blResult;
	}


	// 讀取 CCA_PRD_TYPE_INTR
	public boolean selectPrdTypeIntr4LowTrade(String spCardNote) throws Exception {

		gb.showLogMessage("I","selectPrdTypeIntr4LowTrade()! start");

		String slPrdTpe= gate.sgCardProd; 

		String slCurDate=HpeUtil.getCurDateStr(false);
		boolean blResult =true;
		daoTable  = addTableOwner("CCA_PRD_TYPE_INTR");  //產品類別檔
		selectSQL = "NVL(NAT_LMT_AMT_DAY,0) as LmtDayAmt4LowTrade," //小額日限額
				+ "NVL(NAT_LMT_CNT_DAY,0) as LmtDayCnt4LowTrade," //小額日限次
				+ "NVL(NAT_LMT_AMT_TIME,0) as LmtTimesAmt4LowTrade"; //小額次限額
		//+ "NVL(INT_N_LMT_AMT_TIME,0) as LmtTimesAmt4LowTrade"; //一般消費(小額)次限額 . Howard:舊系統抓此欄位，新系統應該抓 NAT_LMT_AMT_TIME 欄位 


		whereStr  = "WHERE AREA_TYPE=? "
				+ "AND PRD_ATTRIB=? " 
				+ "AND CARD_NOTE=? "
				+ "AND START_FLAG=? "
				+ "AND START_DATE<=? " //有效日期-起
				+ "AND END_DATE>=? "; //有效日期-迄
		String slAreaType="T";
		String slPrdAttrib="P";
		String slStartFlag = "";
		setString(1,slAreaType);/* 國內外交易            -- F.國外交易一般參數, T.小額交易 */
		setString(2,slPrdAttrib);/* 產品屬性:            -- P.產品類別設定 */
		setString(3,spCardNote); //卡片等級
		setString(4,slStartFlag); //起始旗標
		setString(5,slCurDate);
		setString(6,slCurDate);
		selectTable();

		//gb.showLogMessage("D","Area Type=>"+sL_AreaType+"--");
		//gb.showLogMessage("D","Prd Attrib=>"+sL_PrdAttrib+"--");
		//gb.showLogMessage("D","Card Note=>"+sP_CardNote+"--");
		//gb.showLogMessage("D","Cur Date=>"+sL_CurDate+"--");
		//gb.showLogMessage("D","Start Flag=>"+sL_StartFlag+"--");



		if ( "Y".equals(notFound) ) {
			blResult=false; 
		}
		else {
			//gb.showLogMessage("D","小額日限額=>"+getValue("LmtDayAmt4LowTrade"));
			//gb.showLogMessage("D","小額日限次=>"+getValue("LmtDayCnt4LowTrade"));
			//gb.showLogMessage("D","小額次限額=>"+getValue("LmtTimesAmt4LowTrade"));
		}
		/*
        //BRD: 1.	參數設定修改為依「卡片等級」，刪除「產品類別」設定，「卡片等級」空白視為通用原則。
        if ( "Y".equals(notFound) ) {
        			//用空白 select =>{因為「卡片等級」空白視為通用原則 }
            setString(3," "); 

            selectTable();
            if ( "Y".equals(notFound) ) {
            	bL_Result=false; 
            }
        }
		 */
		/* Howard: 此為 proc 的條件
        whereStr  = "WHERE AREA_TYPE=? "
        		+ "AND PRD_ATTRIB=? " 
        		+ "AND PRD_TYPE=? "
        		+ "AND CARD_START_FLAG=? "
        		+ "AND CARD_START_DATE<=? "
        		+ "AND CARD_END_DATE>=? ";



        setString(1,"T");
        setString(2,"P");
        setString(3,sL_PrdTpe);
        setString(4,"Y");
        setString(5,sL_CurDate);
        setString(6,sL_CurDate);

        selectTable();

        if ( "Y".equals(notFound) ) {

        	sL_PrdTpe = gate.sG_CardProd.substring(0, 6) + "0000";


            setString(3,sL_PrdTpe); 

            selectTable();




            if ( "Y".equals(notFound) ) {
            	bL_Result=false; 
            }
            else {
            	gate.sG_UsedCardProd = sL_PrdTpe;
            }
        }
		 */
		return blResult;
	}


	private boolean isCardAcctMCodeValid() {
		boolean blResult = false;
		try {

			String slCurDate = HpeUtil.getCurDateStr(false);
			String slCardAcctCcasMCode = getValue("CardAcctCcasMCode").trim();
			String slCardAcctMCodeValidDate = getValue("CardAcctMCodeValidDate").trim();

			if (("".equals(slCardAcctCcasMCode)) || ("".equals(slCardAcctMCodeValidDate)) ) {
				blResult = false;
			}
			else {
				//sL_CardAcctMCodeValidDate and sL_CardAcctCcasMCode 都有值，而且 系統日期小於 or 等於 sL_CardAcctMCodeValidDate，則 回傳 true
				if (Integer.parseInt(slCurDate) <= Integer.parseInt(slCardAcctMCodeValidDate)) 
					blResult = true;
			}

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}
	public String getMCode() throws Exception{
		gb.showLogMessage("I","getMCode()! start");

		String slMCode=""; //Db2.CCAS_MCODE==Oracle.MCODE_NOW

		//  JH: -->M-code: check CCA_CARD_ACCT.ccas_mcode, mcode_valid_date; Aselect ACT_ACNO.INT_RATE_MCODE 
		if (isCardAcctMCodeValid()) {
			slMCode = getValue("CardAcctCcasMCode");
		}
		else
			slMCode = getValue("ActAcnoIntRateMCode");

		gate.mCode = slMCode.trim();

		return gate.mCode;
	}

	public String getClassCode() throws Exception{
		gb.showLogMessage("I","getClassCode()! start");

		String slClassCode="";
		String slCurDate = HpeUtil.getCurDateStr(false);


		//  JH: -->dH: check CCA_CARD_ACCT.ccas_class_code, class_valid_date; Aselect ACT_ACNO.class_code
		if ((!"".equals(getValue("CardAcctCcasClassCode"))) && (slCurDate.equals(getValue("CardAcctClassValidDate")))) {
			slClassCode = getValue("CardAcctCcasClassCode"); /* 授權卡人等級                        */
		}
		else {
			if (gate.isDebitCard)
				slClassCode = getValue("DbaAcnoClassCode"); ///* 帳戶卡人等級                   */ -- ' ','A'~'E','1'~'9'
			else
				slClassCode = getValue("ActAcnoClassCode"); ///* 帳戶卡人等級                   */ -- ' ','A'~'E','1'~'9'
		}

		gate.classCode = slClassCode.trim();
		return gate.classCode;
	}

	/**
	 * 取得卡片卡人等級消費限額參數檔
	 * V1.00.38 P3授權額度查核調整
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	private void getRiskLevelParm(String spCardNote, String spAreaType, String spRiskLevel) throws Exception{
		daoTable  = addTableOwner("CCA_RISK_LEVEL_PARM"); 
		selectSQL = "NVL(TOT_AMT_PCT,0) as RiskLevelTotAmtPct, "			//消費總額百分比
				+ "RSP_CODE as RiskLevelRspCode,"  					//On Us 回覆碼
				+ " NVL(ADD_TOT_AMT,0) as RiskLevelAddTotAmt," 	//超額絶對金額 (有用到)
				+ " NVL(MAX_CASH_AMT,0) as RiskLevelMaxCashAmt," 	//預借現金金額
				+ "NVL(INST_MONTH_PCT,0) as RiskLevelInstMonthPct," //分期總月限%
				+ "NVL(MAX_INST_AMT,0) as RiskLevelMaxInstAmt";		//分期最高額度



		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  risk_level = ?";



		setString(1,spCardNote);
		setString(2,spAreaType);
		setString(3,spRiskLevel); 
		selectTable();

	}
	// Ū CCA_RISK_LEVEL_PARM
	/**
	 * 卡片卡人等級消費限額參數檔
	 * V1.00.38 P3授權額度查核調整
	 * @return 臨調後的金額(商務卡)
	 * @throws Exception if any exception occurred
	 */	
	public boolean selectRiskLevelParm() throws Exception {
		gb.showLogMessage("I","selectRiskLevelParm()! start");

		//BRD 1.2.2 額度管理說明提到:
		//(6)	臨調案件之限額判斷以本參數檢核，不再判斷「卡戶等級暨消費限額參數(table: CCA_RISK_LEVEL_PARM)」及其餘臨調參數(如:國外交易一般參數等)。

		//Howard: 非臨調案件，還是要判斷此參數

		//20220105  V1.00.06 臨調公司戶調整，取消此判斷
//		boolean blHasTempAdj = false; 
//
//		if (gate.businessCard)
//			blHasTempAdj = gate.bgHasCompAdj;
//		else
//			blHasTempAdj = gate.bgHasPersonalAdj;
//
//		if(blHasTempAdj)
//			return true;

		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		String slCardNote=getValue("CardBaseCardNote");

		String slAreaType=gate.areaType;
		//kevin:此參數沒有分國內外，一律使用國內:T
		slAreaType="T";
		String slRiskLevel = gate.classCode; //卡人等級   
		boolean blResult =true;
		gate.hasRiskLevelParm=true;


		gb.showLogMessage("D","function: TA.selectRiskLevelParm-1 -- sL_CardNote is=>"+slCardNote + "--");
		gb.showLogMessage("D","function: TA.selectRiskLevelParm-1 -- sL_AreaType is=>"+slAreaType + "--");
		gb.showLogMessage("D","function: TA.selectRiskLevelParm-1 -- sL_RiskLevel is=>"+slRiskLevel + "--");

		getRiskLevelParm(slCardNote, slAreaType, slRiskLevel);

		if ( "Y".equals(notFound) ) {

			//sL_RiskLevel = "J";  
			//sL_RiskLevel = "*";/* DEFAULT RISK LEVEL **/
			slCardNote = "*";

			getRiskLevelParm(slCardNote, slAreaType, slRiskLevel);




			if ( "Y".equals(notFound) ) {
				//getAndSetErrorCode("ERR90");
				gb.showLogMessage("D","function: TA.selectRiskLevelParm-2 -- sL_CardNote is=>"+slCardNote + "--");					
				gb.showLogMessage("D","function: TA.selectRiskLevelParm-2 -- sL_AreaType is=>"+slAreaType + "--");
				gb.showLogMessage("D","function: TA.selectRiskLevelParm-2 -- sL_RiskLevel is=>"+slRiskLevel + "--");

				getAndSetErrorCode("CT");

				blResult=false;
				gate.hasRiskLevelParm=false;

			}


		}

		//gb.showLogMessage("D","傳入CardNote=>" + sL_CardNote);
		//gb.showLogMessage("D","傳入AreaType=>" + sL_AreaType);
		//gb.showLogMessage("D","傳入RiskLevel=>" + sL_RiskLevel);
		//gb.showLogMessage("D","RiskLevelTotAmtPct=>" + getValue("RiskLevelTotAmtPct"));
		//gb.showLogMessage("D","RiskLevelRspCode=>" + getValue("RiskLevelRspCode"));
		//gb.showLogMessage("D","RiskLevelMaxCashAmt=>" + getValue("RiskLevelMaxCashAmt"));
		//gb.showLogMessage("D","RiskLevelInstMonthPct=>" + getValue("RiskLevelInstMonthPct"));
		//gb.showLogMessage("D","RiskLevelMaxInstAmt=>" + getValue("RiskLevelMaxInstAmt"));

		return blResult;
	}

	// 讀取 CCA_RISK_CONSUME_PARM (卡片風險類別消費限額參數檔)
	public boolean selectRiskConsumeParm(String spRiskType, String spRiskLevel, String spCardNote) throws Exception {
		//proc: TB_risk_consume_parm()
		gb.showLogMessage("I","selectRiskConsumeParm()! start");



		String slAreaType=gate.areaType;
		//kevin:此參數沒有分國內外，一律使用國內:T
		slAreaType="T";

		boolean blResult =true;
		daoTable  = addTableOwner("CCA_RISK_CONSUME_PARM"); 
		//daoTable  = "CCA_RISK_CONSUME_PARM_T"; //Howard: 20190111 改由此 table 讀取

		selectSQL = "NVL(LMT_AMT_MONTH_PCT,0) as RiskConsumeLmtAmtMonthPct,"  //月限額百分比
				+"NVL(LMT_CNT_MONTH,0)  as RiskConsumeLmtCntMonth,"		//月限次
				+"NVL(LMT_AMT_TIME_PCT,0) as RiskConsumeLmtAmtTimePct,"		//次限額百分比
				+"NVL(LMT_CNT_DAY,0) as RiskConsumeLmtCntDay,"				//日限次
				+"NVL(ADD_TOT_AMT,0) as RiskConsumeAddTotAmt, "			//超額絶對金額
				+"NVL(RSP_CODE_1,' ') as RiskConsumeRspCode1,"		//月限額回覆碼
				+"NVL(RSP_CODE_2,' ') as RiskConsumeRspCode2,"		//月限次回覆碼
				+"NVL(RSP_CODE_3,' ') as RiskConsumeRspCode3,"		//次限額回覆碼
				+"NVL(RSP_CODE_4,' ') as RiskConsumeRspCode4";		//日限次回覆碼



		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  risk_level = ? and risk_type=?";



		setString(1,spCardNote);//卡片等級*
		setString(2,slAreaType);//通用地區T
		setString(3,spRiskLevel); //卡戶風險等級A
		setString(4,spRiskType); //風險類別Z
		selectTable();

		gb.showLogMessage("D","傳入CardNote[卡片等級]=>" + spCardNote);
		gb.showLogMessage("D","傳入AreaType[通用地區]=>" + slAreaType);
		gb.showLogMessage("D","傳入RiskLevel[卡戶風險等級]=>" + spRiskLevel);
		gb.showLogMessage("D","傳入RiskType[風險類別]=>" + spRiskType);

		gb.showLogMessage("D","月限額百分比[RiskConsumeLmtAmtMonthPct]" + getValue("RiskConsumeLmtAmtMonthPct"));
		gb.showLogMessage("D","月限次[RiskConsumeLmtCntMonth]" + getValue("RiskConsumeLmtCntMonth"));
		gb.showLogMessage("D","次限額百分比[RiskConsumeLmtAmtTimePct]" + getValue("RiskConsumeLmtAmtTimePct"));
		gb.showLogMessage("D","日限次[RiskConsumeLmtCntDay]" + getValue("RiskConsumeLmtCntDay"));


		gb.showLogMessage("D","超額絶對金額[RiskConsumeAddTotAmt]" + getValue("RiskConsumeAddTotAmt"));

		gb.showLogMessage("D","月限額回覆碼[RiskConsumeRspCode1]" + getValue("RiskConsumeRspCode1"));
		gb.showLogMessage("D","月限次回覆碼[RiskConsumeRspCode2]" + getValue("RiskConsumeRspCode2"));
		gb.showLogMessage("D","次限額回覆碼[RiskConsumeRspCode3]" + getValue("RiskConsumeRspCode3"));
		gb.showLogMessage("D","日限次回覆碼[RiskConsumeRspCode4]" + getValue("RiskConsumeRspCode4"));

		if ( "Y".equals(notFound) ) {
			blResult=false;
		}


		return blResult;
	}


	// 讀取 CCA_RISK_CONSUME_PARM (卡片風險類別消費限額參數檔) 
	public boolean selectRiskConsumeParm_ProcSpec(String spRiskType, String spRiskLevel) throws Exception {
		//proc: TB_risk_consume_parm()
		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		gb.showLogMessage("I","selectRiskConsumeParm_ProcSpec()! start");

		String slCardNote=getValue("CardBaseCardNote");

		String slAreaType=gate.areaType;


		boolean blResult =true;
		daoTable  = addTableOwner("CCA_RISK_CONSUME_PARM"); 
		selectSQL = "NVL(LMT_AMT_MONTH_PCT,0) as RiskConsumeLmtAmtMonthPct,"  //月限額百分比
				+"NVL(LMT_CNT_MONTH,0)  as RiskConsumeLmtCntMonth,"		//月限次
				+"NVL(LMT_AMT_TIME_PCT,0) as RiskConsumeLmtAmtTimePct,"		//次限額百分比
				+"NVL(LMT_CNT_DAY,0) as RiskConsumeLmtCntDay,"				//日限次
				+"NVL(ADD_TOT_AMT,0) as RiskConsumeAddTotAmt, "			//超額絶對金額
				+"NVL(RSP_CODE_1,' ') as RiskConsumeRspCode1,"		//月限額回覆碼
				+"NVL(RSP_CODE_2,' ') as RiskConsumeRspCode2,"		//月限次回覆碼
				+"NVL(RSP_CODE_3,' ') as RiskConsumeRspCode3,"		//次限額回覆碼
				+"NVL(RSP_CODE_4,' ') as RiskConsumeRspCode4";		//日限次回覆碼



		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  risk_level = ? and risk_type=?";



		setString(1,slCardNote);//卡片等級
		setString(2,slAreaType);//通用地區
		setString(3,spRiskLevel); //卡戶風險等級
		setString(4,spRiskType); //風險類別
		selectTable();

		if ( "Y".equals(notFound) ) {
			blResult=false;
		}


		return blResult;
	}

	//CCA_ADJ_PARM
	public boolean selectAdjParm(String spRiskType) throws Exception {
		//Howard: 20170707 => table schema
		//proc: TB_adj_parm
		//kevin: 20200510 => new add adj_eff_date check
		gb.showLogMessage("I","selectAdjParm()! start");

		gate.adjParmAmtRate=1;
		gate.adjParmCntRate=1;
		String spCurdate = HpeUtil.getCurDateStr(false);  

		boolean blResult =true;
		daoTable  = addTableOwner("CCA_ADJ_PARM"); 
		selectSQL = "NVL(TIMES_AMT,0) as AdjParmTimesAmt,"      //金額倍數百分比
				+ "NVL(TIMES_CNT,0) as AdjParmTimesCnt,"	    //次數倍數百分比
				+ "NVL(ADJ_MONTH_AMT,0) as AdjParmMonthAmt,"	//調整後的月限額
				+ "NVL(ADJ_MONTH_CNT,0) as AdjParmMonthCnt,"	//調整後的月限次
				+ "NVL(ADJ_DAY_AMT,0) as AdjParmDayAmt,"	    //調整後的日限額
				+ "NVL(ADJ_DAY_CNT,0) as AdjParmDayCnt,"	    //調整後的日限次
				+ "ADJ_EFF_START_DATE as AdjParmStartDate,"     //調整啟用日期
				+ "ADJ_EFF_END_DATE as AdjParmEndDate,"         //調整結束日期
				+ "SPEC_FLAG as AdjParmSpecFlag ";              //專款專用旗標



		whereStr  = "WHERE CARD_ACCT_IDX = ? and risk_type=? AND  ADJ_EFF_START_DATE <= ? and ADJ_EFF_END_DATE >=?";



		//setInt(1,Integer.parseInt(gate.CardAcctIdx));
		setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(gate.cardAcctIdx)));

		setString(2,spRiskType); 
		setString(3,spCurdate);
		setString(4,spCurdate);
		selectTable();

		if ( "Y".equals(notFound) ) {
			blResult=false;
		}
		else {
			gate.adjParmAmtRate= getDouble("AdjParmTimesAmt")/100; //金額倍數百分比
			gate.adjParmCntRate=getDouble("AdjParmTimesCnt")/100;	//次數倍數百分比
		}


		return blResult;
	}

	private void getAdjProdParm(String spCardNote,String spAreaType,String spCurdate,String spMccCode1,String spMccCode2) throws Exception{
		daoTable  = addTableOwner("CCA_ADJ_PROD_PARM"); 
		selectSQL = "NVL(TIMES_AMT,0) as AdjProdParmTimesAmt," //金額倍數百分比
				+"NVL(TIMES_CNT,0) as AdjProdParmTimesCnt," //次數倍數百分比
				+"NVL(TOT_AMT_MONTH,0) as AdjProdParmTotAmtMonth"; //放大總月限額



		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  adj_eff_date1 <= ? and adj_eff_date2 >=? and (mcc_code=? or mcc_code=?)";


		setString(1,spCardNote);
		setString(2,spAreaType);
		setString(3,spCurdate);
		setString(4,spCurdate); 
		setString(5, spMccCode1);
		setString(6,spMccCode2);
		selectTable();

	}

	//取得產品類別臨調檔 CCA_ADJ_PROD_PARM
	public boolean selectAdjProdParm() throws Exception {
		gb.showLogMessage("I","selectAdjProdParm()! start");

		//proc: credit_check_adj_prod()
		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		String slCardNote=getValue("CardBaseCardNote");

		String slAreaType=gate.areaType;
		String slCurdate = HpeUtil.getCurDateStr(false);  

		boolean blResult =true;

		daoTable  = addTableOwner("CCA_ADJ_PROD_PARM"); 
		selectSQL = "NVL(TIMES_AMT,0) as AdjProdParmTimesAmt," //金額倍數百分比
				+"NVL(TIMES_CNT,0) as AdjProdParmTimesCnt," //次數倍數百分比
				+"NVL(TOT_AMT_MONTH,0) as AdjProdParmTotAmtMonth"; //放大總月限額



		whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  adj_eff_date1 <= ? and adj_eff_date2 >=? and (mcc_code=? or mcc_code=?)";

		String slMccCode1="0000";
		String slMccCode2=gate.mccCode;
		getAdjProdParm(slCardNote,slAreaType,slCurdate,slMccCode1,slMccCode2);

		if ( "Y".equals(notFound) ) {
			slCardNote=""; 
			whereStr  = "WHERE card_note = ? AND  area_type  = ? AND  adj_eff_date1 <= ? and adj_eff_date2 >=? and (mcc_code=? or mcc_code=?)";

			getAdjProdParm(slCardNote,slAreaType,slCurdate,slMccCode1,slMccCode2);


			if ( "Y".equals(notFound) ) {
				blResult=false;
			}
		}

		//gb.showLogMessage("D","傳入CardNote[卡片等級]=>" + sL_CardNote);
		//gb.showLogMessage("D","傳入AreaType[適用地區]=>" + sL_AreaType);
		//gb.showLogMessage("D","傳入adj_eff_date1[生效日期(起)]=>" + sL_Curdate);
		//gb.showLogMessage("D","傳入adj_eff_date2[生效日期(迄)]=>" + sL_Curdate);
		//gb.showLogMessage("D","傳入MCC Code=>" + gate.mccCode);

		//gb.showLogMessage("D","TIMES_AMT[金額倍數百分比]=>" +getValue("AdjProdParmTimesAmt"));
		//gb.showLogMessage("D","TIMES_CNT[次數倍數百分比]=>" +getValue("AdjProdParmTimesCnt"));
		//gb.showLogMessage("D","TOT_AMT_MONTH[放大總月限額]=>" +getValue("AdjProdParmTotAmtMonth"));


		return blResult;
	}

	// 自動授權免開卡參數 CCA_AUTH_ACTIVE
	public boolean selectAuthActive() throws Exception {
		gb.showLogMessage("I","selectAuthActive()! start");

		//٨S create table...
		String slTargetMerchantNo = gate.merchantNo.trim();
		String slTargetAcqId = gate.isoField[32].toString().trim();
		String slTargetMccCode = gate.mccCode;
		String slDataSource="";
		//kevin:取消ECSSYSTE檢查
		//kevin:取消service4BatchAuth設定，改為單筆connType = "BATCH"決定
//		if ( (gb.service4BatchAuth) || ("ECSSYSTE".equals(gate.isoField[41].substring(0, 8))) )
		if ("BATCH".equals(gate.connType))
			slDataSource="A";
		else
			slDataSource="C";
		boolean blResult =true;
		daoTable  = addTableOwner("CCA_AUTH_ACTIVE"); //oracle is Auth_Parm_Active 
		selectSQL = "count(*) as AuthActiveCount";



		whereStr  = "WHERE data_source = ? and mcc_code=? and mcht_no=?";
		setString(1,slDataSource);

		setString(2,slTargetMccCode);
		setString(3,slTargetMerchantNo);

		if ("B".equals(slDataSource)) {
			whereStr  = whereStr + " and acq_id=?";
			setString(4,slTargetAcqId);
		}

		selectTable();


		if (getInteger("AuthActiveCount")>0)
			blResult = false;

		//gb.showLogMessage("D","Mcc Code=>" +sL_TargetMccCode);
		//gb.showLogMessage("D","Merchant No=>" +sL_TargetMerchantNo);
		//gb.showLogMessage("D","CCA_AUTH_ACTIVE 資料筆數=>" + getInteger("AuthActiveCount"));

		return blResult;
	}

	private void getDebitParm(String spBinNo) throws Exception{
		daoTable  = addTableOwner("CCA_DEBIT_PARM"); //DEBIT卡參數檔
		selectSQL = "NVL(NO_CONNECT_FLAG,'N') as DebitParmNoConnFlag," //開放非面對面刷卡
				+ "CNT_AMOUNT  as DebitParmCntAmount,"//次限額
				+ "DAY_AMOUNT as DebitParmDayAmount ,"//日限額
				+ "DAY_CNT as DebitParmDayCnt,"//日限次
				+ "MARKUP as DebitParmMarkup,"//匯率轉換率
				+ "MONTH_AMOUNT as DebitParmMonthAmount,"//月限額
				+ "WITHDRAW_FEE as DebitParmWithdrawFee,"//國外手續費
		        + "open_chk as AuthParmOpenChk,"				//是否要做 開卡 檢核
				+ "mcht_chk as AuthParmMchtChk,"				//是否要做 風險特店  檢核 => 2017/11/14 還沒用到此變數
				+ "oversea_chk as AuthParmOverseaChk,"			//是否要做 國外消費 檢核 => 2017/11/14
				+ "avg_consume_chk as AuthParmAvgConsumeChk,"  //是否要做 平均消費 檢核  => 2017/11/14 還沒用到此變數
				+ "month_risk_chk as AuthParmMonthRiskChk," //是否要做 月限次 風險檢核
				+ "day_risk_chk as AuthParmDayRiskChk ";	//是否要做 日限次 風險檢核




		whereStr  = "WHERE bin_no = ? ";



		setString(1,spBinNo);
		selectTable();

	}
	// 讀取 CCA_DEBIT_PARM
	public boolean selectDebitParm() throws Exception {
		gb.showLogMessage("I","selectDebitParm()! start");

		//DEBIT卡參數檔 => CCA_DEBIT_PARM
		//kevin:取消此參數改為有table取得
//		if ("F".equals(gate.areaType))
//			setValue("AuthParmOpenChk", "0"); //]w~ˮֶ}d
//		else {
//			setValue("AuthParmOpenChk", "0");
//			setValue("AuthParmMchtChk", "0");
//			setValue("AuthParmOverseaChk", "0");
//			setValue("AuthParmAvgConsumeChk", "0");
//			setValue("AuthParmMonthRiskChk", "0");
//			setValue("AuthParmDayRiskChk", "0");
//		}

		String slBinNo= gate.binNo; 
		boolean blResult =true;

		getDebitParm(slBinNo);
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("D","function: TA.selectDebitParm-1 -- sL_BinNo is=>"+slBinNo + "--");					
			slBinNo = "000000";
			getDebitParm(slBinNo);



			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D","function: TA.selectDebitParm-2 -- sL_BinNo is=>"+slBinNo + "--");					
				blResult=false;
			}
		}


		//gb.showLogMessage("D","Bin No=>" + sL_BinNo);
		//gb.showLogMessage("D","DebitParmNoConnFlag=>" + getValue("DebitParmNoConnFlag"));
		//gb.showLogMessage("D","DebitParmCntAmount=>" + getValue("DebitParmCntAmount"));

		//gb.showLogMessage("D","DebitParmDayAmount=>" + getValue("DebitParmDayAmount"));
		//gb.showLogMessage("D","DebitParmDayCnt=>" + getValue("DebitParmDayCnt"));
		//gb.showLogMessage("D","DebitParmMarkup=>" + getValue("DebitParmMarkup"));

		//gb.showLogMessage("D","DebitParmMonthAmount=>" + getValue("DebitParmMonthAmount"));
		//gb.showLogMessage("D","DebitParmWithdrawFee=>" + getValue("DebitParmWithdrawFee"));


		return blResult;
	}


	// 讀取 CCA_DEBIT_PARM2
	public boolean selectDebitParm2() throws Exception {
		gb.showLogMessage("I","selectDebitParm2()! start");

		//Debit卡風險類別限額限次參數 => CCA_DEBIT_PARM2
		boolean blResult =true;
		daoTable  = addTableOwner("CCA_DEBIT_PARM2"); //Debit卡風險類別限額限次參數
		selectSQL = "CNT_AMT  as DebitParm2CntAmount,"//次限額
				+ "DAY_AMT as DebitParm2DayAmount ,"//日限額
				+ "DAY_CNT as DebitParm2DayCnt,"//日限次
				+ "MONTH_AMT as DebitParm2MonthAmount,"//月限額
				+ "MONTH_CNT as DebitParm2MonthCnt";//月限次





		whereStr  = "WHERE RISK_TYPE = ? ";

		gb.showLogMessage("D","function: TA.selectDebitParm2 -- mccRiskType is=>"+gate.mccRiskType + "--");					

		//gb.showLogMessage("D","***" + gate.mccRiskType + "===");
		setString(1,gate.mccRiskType);
		//setString(1,"XX");
		selectTable();

		if ( "Y".equals(notFound) ) {
			blResult=false;

		}
		return blResult;
	}

	public boolean getCcaSysParm3(String spSysId, String spSysKey, boolean bpIncludeSysKey, String spSysData3DefaultValue) {
		gb.showLogMessage("I","getCcaSysParm3()! start");
		boolean blResult = true;

		String slSql=""; 

		try {
			daoTable = addTableOwner("CCA_SYS_PARM3");	


			selectSQL ="NVL(SYS_DATA1,'5') as Parm3SysData1, NVL(SYS_DATA2,'1') as Parm3SysData2, NVL(SYS_DATA3," + spSysData3DefaultValue + ") as Parm3SysData3, NVL(SYS_DATA4,'0') as Parm3SysData4";
			if (bpIncludeSysKey) {
				whereStr="SYS_ID=?  AND SYS_KEY=?";
				setString(1,spSysId);
				setString(2,spSysKey);
			}
			else {
				whereStr="SYS_ID=?";
				setString(1,spSysId);
			}
			selectTable();


		}
		catch (Exception e) {
			e.getMessage();
		}

		return blResult;
	}
	public boolean getParm3TranCode(String spSysId, String spSysData2DefaultValue) {
		gb.showLogMessage("I","getParm3TranCode()! start");
		boolean blResult = true;

		String slSql=""; 

		try {
			daoTable = addTableOwner("CCA_SYS_PARM3");	

			selectSQL ="SYS_DATA1 as Parm3TranCode1, SYS_DATA2 as Parm3TranCode2, SYS_DATA3 as Parm3TranCode3, NVL(SYS_DATA4,'Y') as Parm3TranCode4, SYS_DATA5 as Parm3TranCode5 ";
			whereStr="WHERE SYS_ID=?  AND SYS_KEY=?";
			setString(1,spSysId);
			setString(2,spSysData2DefaultValue);
			
			selectTable();


		}
		catch (Exception e) {
			e.getMessage();
		}

		return blResult;
	}
	public boolean getAndSetErrorCode(String spRespCode) {
		gb.showLogMessage("I","getAndSetErrorCode()! start");
		//select * from CCA_sys_parm3 a , CCA_RESP_CODE b where a.SYS_ID='AUTO'  and a.SYS_DATA1=b.RESP_CODE order by a.SYS_KEY

		gate.isoField[92]=spRespCode; //Howard: NCCC 沒有定義 P92，所以用來存放 response code 給可能會使用到的前端程式使用


		gate.authErrId = spRespCode;
		//gb.showLogMessage("D","getAndSetErrorCode.RespCode =>" + sP_RespCode + "===");
		gb.showLogMessage("I","RespCode : "+spRespCode);
		//gb.ddd("errCode="+errCode);

		//select RESP_REMARK, NCCC_P38, NCCC_P39 from CCA_RESP_CODE where RESP_CODE='E3'
		try {
			daoTable = addTableOwner("CCA_RESP_CODE"); //授權回覆碼參數檔	
	
	
			selectSQL ="RESP_REMARK, NCCC_P38, NCCC_P39, VISA_P39, MAST_P39, JCB_P39";
			whereStr="WHERE RESP_CODE = ? ";

			setString(1,spRespCode);


			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","CCA_RESP_CODE NOT FOUND="+spRespCode);
				gate.isoField[39] ="57";
				//gate.isoField[38] ="96    ";
				gate.isoField[38] ="";
				gate.authNo="";
				gate.authStatusCode = "XY";
				return false;
			}
		} catch (Exception e) {
			gb.showLogMessage("I","CCA_RESP_CODE EXCEPTION="+e);
			gate.isoField[39] ="96";
			gate.isoField[38] ="";
			gate.authNo="";
			gate.authStatusCode = "XY";
			return false;
		}

		String slCardType = getValue("CARD_TYPE");


		/*
	   Howard:「P-39回覆碼」新增VISA、MASTER、JCB三個Flag，
	     若欄位為空值，則以NCCC信用卡中心P-39回覆碼值為主。
	   (V依照VMJ回傳不同的回覆碼碼，若VMJ的值為空，則以NCCC的回覆值來回覆)
		 */

		if ("V".equals(slCardType)) {
			if (!"".equals(getValue("VISA_P39"))) 
				gate.isoField[39] =  getValue("VISA_P39");	   
			else
				gate.isoField[39] =  getValue("NCCC_P39");
		}
		else if ("M".equals(slCardType)) {
			if (!"".equals(getValue("MAST_P39"))) 
				gate.isoField[39] =  getValue("MAST_P39");	   
			else
				gate.isoField[39] =  getValue("NCCC_P39");

		}
		else if ("J".equals(slCardType)) {
			if (!"".equals(getValue("JSB_P39"))) 
				gate.isoField[39] =  getValue("JCB_P39");	   
			else
				gate.isoField[39] =  getValue("NCCC_P39");

		}
		else 
			gate.isoField[39] =  getValue("NCCC_P39");

		gate.isoField[38] = getValue("NCCC_P38");
		//gate.auth_no = gate.isoField[38];
//		kevin:回覆碼auth_no是保留授權碼，提供強制授權時可以替換的，所以不應該被放入其他值
//		gate.auth_no = getValue("NCCC_P38");
		gate.authStatusCode = spRespCode; //getValue("auth_status_code");

		return true;
	}
	// 讀取 CRD_CARD
	//V1.00.04 修改子卡月累績消費邏輯-改用原始卡號                                                                           *
	public boolean selectCrdCard() throws Exception {
		gb.showLogMessage("I","selectCrdCard()! start");

		daoTable  = addTableOwner("CRD_CARD"); 
		selectSQL = "ACCT_TYPE,"
				+ "ACNO_P_SEQNO as CrdCardAcnoPSeqNo," //p_seqno=>acno_p_seqno
				+ "P_SEQNO as CrdCardPSeqNo,"   //gp_no => p_seqno
				+ "ID_P_SEQNO,"
				+ "CORP_P_SEQNO,"
				+ "CORP_NO,"
				+ "CORP_NO_CODE,"
				+ "URGENT_FLAG as UrgentFlag,"
				+ "CARD_TYPE,"
				+ "GROUP_CODE,"
				+ "SOURCE_CODE,"
				+ "CHANNEL_CODE,"
				+ "BIN_NO,"
				+ "BIN_TYPE as CardBinType,"
				+ "SUP_FLAG," //-- 0:正卡 1:附卡

                  + "SON_CARD_FLAG as SonCardFlag," //子卡旗標 "Y"=>子卡, 其他都不是子卡
                  + "MAJOR_RELATION,"
                  + "MAJOR_ID_P_SEQNO,"
                  + "MAJOR_CARD_NO,"
                  + "MEMBER_NOTE,"
                  + "MEMBER_ID,"
                  + "CURRENT_CODE," /* 狀態碼                 */ //-- 0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
                  + "FORCE_FLAG,"
                  + "ENG_NAME,"
                  + "REG_BANK_NO,"
                  + "UNIT_CODE,"
                  + "OLD_BEG_DATE,"
                  + "OLD_END_DATE,"
                  + "NEW_BEG_DATE,"
                  + "NEW_END_DATE,"
                  + "ISSUE_DATE as CardIssueDate," 
                  + "EMERGENT_FLAG,"
                  + "REISSUE_DATE,"
                  + "REISSUE_REASON,"
                  + "REISSUE_STATUS,"
                  + "CHANGE_REASON,"
                  + "CHANGE_STATUS,"
                  + "CHANGE_DATE,"
                  + "UPGRADE_STATUS,"
                  + "APPLY_NO,"
                  + "PROMOTE_DEPT,"
                  + "PROMOTE_EMP_NO,"
                  + "INTRODUCE_EMP_NO,"
                  + "INTRODUCE_ID,"
                  + "INTRODUCE_NAME,"
                  + "PROD_NO,"
                  + "REWARD_AMT,"
                  + "INTR_REASON_CODE,"
                  + "CLERK_ID,"
                  + "EMBOSS_DATA,"
                  + "PIN_BLOCK,"
                  + "PVV,"
                  + "CVV,"
                  + "CVV2,"
                  + "PVKI as CardPvki,"
                  + "APPLY_CHT_FLAG,"
                  + "APPLY_ATM_FLAG,"
                  + "BATCHNO,"
                  + "RECNO,"
                  + "OPPOST_REASON,"
                  + "OPPOST_DATE,"
                  //+ "BLOCK_STATUS,"
                  //+ "BLOCK_REASON,"
                  //+ "BLOCK_REASON2,"
                  + "BLOCK_DATE,"
                  + "NEW_CARD_NO,"
                  + "OLD_CARD_NO,"
                  + "STMT_CYCLE,"
                  + "FEE_CODE,"
                  + "CURR_FEE_CODE,"
                  + "SPEC_ANN_FEE_CODE,"
                  + "LOST_FEE_CODE,"
                  + "INDIV_CRD_LMT,"
                  + "INDIV_INST_LMT,"
                  + "EXPIRE_REASON,"
                  + "EXPIRE_CHG_FLAG,"
                  + "EXPIRE_CHG_DATE,"
                  + "CORP_ACT_FLAG," ///* 商務卡總個繳註記       */ -- Y:總繳  N:個繳
                  + "OLD_ACTIVATE_TYPE,"
                  + "OLD_ACTIVATE_FLAG," //1:開卡 2:關閉 =>  12/17 改為 //1:關閉 2:開卡

                  + "OLD_ACTIVATE_DATE,"
                  + "OLD_CLOSE_DATE,"
                  + "ACTIVATE_TYPE,"
                  + "ACTIVATE_FLAG," //1:關閉 2:開卡
                  + "ACTIVATE_DATE,"
                  + "CLOSE_DATE,"
                  + "SET_CODE,"
                  + "MAIL_TYPE,"
                  + "MAIL_NO,"
                  + "MAIL_BRANCH,"
                  + "MAIL_PROC_DATE,"
                  + "MAIL_REJECT_DATE,"
                  + "STOCK_NO,"
                  + "COMBO_ACCT_NO,"
                  + "COMBO_BEG_BAL,"
                  + "COMBO_END_BAL,"
                  + "COMBO_INDICATOR,"
                  + "OLD_BANK_ACTNO,"
                  + "BANK_ACTNO,"
                  + "IC_FLAG,"
                  + "BRANCH,"
                  + "TRANS_CVV2,"
                  + "OLD_TRANS_CVV2,"
                  + "FANCY_LIMIT_FLAG,"
                  + "DESTROY_SEQNO,"
                  + "DROP_AF_FLAG,"
                  + "DROP_AF_DATE,"
                  + "AUTO_INSTALLMENT,"
                  + "EXPIRE_ADDR,"
                  + "WEB_ACS_DATE,"
                  + "WEB_ACS_TYPE,"
                  + "ACS_DATE,"
                  + "ORI_CARD_NO as CrdCardOriCardNo,"   //原始卡號
                  + "ORI_ISSUE_DATE,"
                  + "ORI_APPLY_NO,"
                  + "END_CARD_NO,"
                  + "CURR_CODE,"
                  + "JCIC_SCORE,"
                  + "CARD_MOLD_FLAG,"
                  + "MSG_FLAG as CardMsgFlag,"                 //發簡訊旗標 Y=發簡訊,N=不發簡訊;V1.00.08 新簡訊發送規則
                  + "MSG_PURCHASE_AMT as CardMsgPurchaseAmt, " //發簡訊消費金額;V1.00.08 新簡訊發送規則
				  + "PASSWD_ERR_COUNT as CrdCardPinErrorCnt "; //V1.00.16 預借現金密碼錯誤次數檢查



		whereStr  = "WHERE CARD_NO = ? ";
		setString(1,gate.cardNo);
		selectTable();
		////gb.showLogMessage("D","ACCT_TYPE:" + getValue("ACCT_TYPE") +"--");
		////gb.showLogMessage("D","CARD_TYPE:" + getValue("CARD_TYPE") +"--");
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectCrdCard -- can not find data. CARD_NO is  "+gate.cardNo + "--");
			return false; 
		}
		//gb.showLogMessage("D","Card No=>" + gate.cardNoMask);
		//gb.showLogMessage("D","CardBinType=>" + getValue("CardBinType"));
		//gb.showLogMessage("D","UrgentFlag=>" + getValue("UrgentFlag"));
		//gb.showLogMessage("D","CardGroupCode=>" + getValue("CardGroupCode"));

		setGateValue();

		return true;

	}

	private void setGateValue() {
		gate.groupCode = getValue("group_code");

		if ("Y".equals(getValue("UrgentFlag").trim()))
			gate.urgentFlag = true;
		else
			gate.urgentFlag = false;


		if ("Y".equals(getValue("SonCardFlag").trim()))
			gate.isChildCard = true;
		else
			gate.isChildCard = false;

		gate.binType = getValue("CardBinType").trim();

	}
	// 讀取 DBC_CARD
	public boolean selectDbcCard() throws Exception {
		gb.showLogMessage("I","selectDbcCard()! start");

		daoTable  =  addTableOwner("DBC_CARD"); 
		selectSQL = "ACCT_TYPE,"
				+ "P_SEQNO as DbcCardPSeqNo,"
				//+ "GP_NO," //remove this column
				+ "ID_P_SEQNO,"
				+ "CORP_P_SEQNO,"
				+ "CORP_NO,"
				+ "URGENT_FLAG as UrgentFlag,"
				+ "CARD_TYPE,"
				+ "GROUP_CODE,"
				+ "SOURCE_CODE,"
				+ "CHANNEL_CODE,"
				+ "BIN_NO,"
				+ "BIN_TYPE as CardBinType,"
				+ "SUP_FLAG," //-- 0:正卡 1:附卡
				+ "SON_CARD_FLAG as SonCardFlag," //子卡旗標 "Y"=>子卡, 其他都不是子卡
				+ "MAJOR_RELATION,"
				+ "MAJOR_ID_P_SEQNO,"
				+ "MAJOR_CARD_NO,"
				+ "MEMBER_ID,"
				+ "CURRENT_CODE," /* 狀態碼                 */ //-- 0:正常 1:一般停用 2:掛失 3:強停 4:其他 5: 偽卡
				+ "FORCE_FLAG,"
				+ "ENG_NAME,"
				+ "REG_BANK_NO,"
				+ "UNIT_CODE,"
				+ "OLD_BEG_DATE,"
				+ "OLD_END_DATE,"
				+ "NEW_BEG_DATE,"
				+ "NEW_END_DATE,"
				+ "ISSUE_DATE as CardIssueDate,"
				+ "EMERGENT_FLAG,"
				+ "REISSUE_DATE,"
				+ "REISSUE_REASON,"
				+ "REISSUE_STATUS,"
				+ "CHANGE_REASON,"
				+ "CHANGE_STATUS,"
				+ "CHANGE_DATE,"
				+ "UPGRADE_STATUS,"
				+ "APPLY_NO,"
				+ "PROMOTE_DEPT,"
				+ "PROMOTE_EMP_NO,"
				+ "INTRODUCE_EMP_NO,"
				+ "INTRODUCE_ID,"
				+ "INTRODUCE_NAME,"
				+ "PROD_NO,"
				+ "REWARD_AMT,"
				+ "INTR_REASON_CODE,"
				+ "CLERK_ID,"
				+ "EMBOSS_DATA,"
				+ "PIN_BLOCK,"
				+ "PVV,"
				+ "CVV,"
				+ "CVV2,"
				+ "PVKI,"
				+ "APPLY_CHT_FLAG,"
				+ "APPLY_ATM_FLAG,"
				+ "BATCHNO,"
				+ "RECNO,"
				+ "OPPOST_REASON,"
				+ "OPPOST_DATE,"
				//+ "BLOCK_STATUS,"
				//+ "BLOCK_REASON,"
				//+ "BLOCK_REASON2,"
				+ "BLOCK_DATE,"
				+ "NEW_CARD_NO,"
				+ "OLD_CARD_NO,"
				+ "STMT_CYCLE,"
				+ "FEE_CODE,"
				+ "CURR_FEE_CODE,"
				+ "SPEC_ANN_FEE_CODE,"
				+ "LOST_FEE_CODE,"
				+ "INDIV_CRD_LMT,"
				+ "INDIV_INST_LMT,"
				+ "EXPIRE_REASON,"
				+ "EXPIRE_CHG_FLAG,"
				+ "EXPIRE_CHG_DATE,"
				+ "CORP_ACT_FLAG," ///* 商務卡總個繳註記       */ -- Y:總繳  N:個繳
				+ "OLD_ACTIVATE_TYPE,"
				+ "OLD_ACTIVATE_FLAG,"
				+ "OLD_ACTIVATE_DATE,"
				+ "OLD_CLOSE_DATE,"
				+ "ACTIVATE_TYPE,"
				+ "ACTIVATE_FLAG,"
				+ "ACTIVATE_DATE,"
				+ "CLOSE_DATE,"
				+ "SET_CODE,"
				+ "MAIL_TYPE,"
				+ "MAIL_NO,"
				+ "MAIL_BRANCH,"
				+ "MAIL_PROC_DATE,"
				+ "MAIL_REJECT_DATE,"
				+ "STOCK_NO,"
				+ "OLD_BANK_ACTNO,"
				+ "BANK_ACTNO,"
				+ "IC_FLAG,"
				+ "BRANCH,"
				+ "TRANS_CVV2,"
				+ "OLD_TRANS_CVV2,"
				+ "WEB_ACS_DATE,"
				+ "WEB_ACS_TYPE,"
				+ "ACS_DATE,"
				+ "ORI_CARD_NO,"
				+ "ORI_ISSUE_DATE,"
				+ "ORI_APPLY_NO,"
				+ "ACCT_NO as DbcCardAcctNo,"
				+ "CARD_REF_NUM as DbcCardRefNum,"
				+ "END_CARD_NO, "
                + "MSG_FLAG as CardMsgFlag,"                 //發簡訊旗標 Y=發簡訊,N=不發簡訊;V1.00.08 新簡訊發送規則
                + "MSG_PURCHASE_AMT as CardMsgPurchaseAmt "; //發簡訊消費金額;V1.00.08 新簡訊發送規則


		whereStr  = "WHERE CARD_NO = ? ";
		setString(1,gate.cardNo);

		selectTable();

		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectDbcCard -- can not find data. CARD_NO is  "+gate.cardNoMask + "--");
			return false; 
		}

		//gb.showLogMessage("D","Card No=>" + gate.cardNoMask);
		//gb.showLogMessage("D","CardGroupCode=>" + getValue("CardGroupCode"));
		//gb.showLogMessage("D","CardBinType=>" + getValue("CardBinType"));

		setGateValue();

		return true;
	}

	public boolean selectDebitAndRiskInfo(int npType) throws Exception {
		gb.showLogMessage("I","selectDebitAndRiskInfo()! start");
		boolean blResult = false;
		String slCardAcctIdx = gate.cardAcctIdx;
		String slMccCode = gate.mccCode;

		if (npType==1) {
			String slT1= addTableOwner("CCA_MCC_RISK") + " c";
			String slT2= addTableOwner("CCA_DEBIT_ADJ_PARM") + " a";
			String slT3= addTableOwner("CCA_DEBIT_PARM2") + " b";

			daoTable  = slT1 +" ," + slT2 + " ," + slT3 + " ";
			//daoTable  = "CCA_MCC_RISK c,CCA_DEBIT_ADJ_PARM a, CCA_DEBIT_PARM2 b "; 
			selectSQL = "(b.cnt_amt * a.cnt_amt_pct / 100) as TimesLimitAmtAfterJoin , "		/*次限額*/
					+ "(b.day_amt * a.day_amt_pct / 100) as DayAmtLimitAfterJoin ,"		/*日限額*/
					+ "(b.day_cnt * a.day_cnt_pct / 100) as DayTimesLimitAfterJoin ,"		/*日限次*/
					+ "(b.month_amt * a.month_amt_pct / 100) as MonthAmtLimitAfterJoin , "	/*月限額*/
					+ "(b.month_cnt * a.month_cnt_pct / 100) as MonthTimesLimitAfterJoin ,"	/*月限次*/
					+ "c.risk_type";

			whereStr  = "WHERE a.risk_type = b.risk_type "
					+ "and a.card_acct_idx = ? "
					+ "and a.risk_type     = c.risk_type " 
					+ "and c.mcc_code      = ? ";
			setString(1,slCardAcctIdx);
			setString(2,slMccCode);       
			selectTable();

		}
		else if (npType==2){
			String slT1= addTableOwner("CCA_MCC_RISK") + " c";

			String slT3= addTableOwner("cca_debit_parm2") + " b";

			daoTable  = slT1 +" ," + slT3 + " ";

			//daoTable  = "CCA_MCC_RISK c, cca_debit_parm2 b "; 
			selectSQL = "b.cnt_amt as TimesLimitAmtAfterJoin , "		/*次限額*/
					+ "b.day_amt as DayAmtLimitAfterJoin ,"		/*日限額*/
					+ "b.day_cnt as DayTimesLimitAfterJoin ,"		/*日限次*/
					+ "b.month_amt as MonthAmtLimitAfterJoin , "	/*月限額*/
					+ "b.month_cnt as MonthTimesLimitAfterJoin ,"	/*月限次*/
					+ "c.risk_type";

			whereStr  = "WHERE c.risk_type = b.risk_type "
					+ "and c.mcc_code      = ? ";

			setString(1,slMccCode);       
			selectTable();

		}
		if ( "Y".equals(notFound) ) {
			blResult = false; 
		}
		else {
			blResult = true; 
		}

		return blResult;
	}
	public void selectCrdItemUnit() throws Exception {
		gb.showLogMessage("I","selectCrdItemUnit()! start");

		String slUnitCode = getValue("UNIT_CODE").trim();
		String slCardType = getValue("CARD_TYPE").trim();    	

		daoTable  = addTableOwner("CRD_ITEM_UNIT"); 
		selectSQL = "NVL(VIRTUAL_FLAG, ' ') as CrdItemUnitVirualFlag, "
				  + "ISSUER_CONFIGURATION_ID ";
		whereStr  = "WHERE UNIT_CODE = ? and CARD_TYPE= ? ";
		setString(1,slUnitCode);
		setString(2,slCardType);       
		selectTable();

		//gb.showLogMessage("D","UNIT_CODE=>" + slUnitCode);
		//gb.showLogMessage("D","CARD_TYPE=>" + slCardType);
		//gb.showLogMessage("D","VIRTUAL_FLAG=>" + getValue("CrdItemUnitVirualFlag"));
		//gb.showLogMessage("D","ISSUER_CONFIGURATION_ID=>" + getValue("ISSUER_CONFIGURATION_ID"));

	}
	public void selectPtrGroupCode() throws Exception {
		gb.showLogMessage("I","selectPtrGroupCode()! start");

		daoTable  = addTableOwner("PTR_GROUP_CODE"); 
		selectSQL = "PURCHASE_CARD_FLAG, "
				  + "CCA_GROUP_MCHT_CHK, "
				  + "GROUP_NAME ";

		whereStr  = "WHERE GROUP_CODE = ? ";
		setString(1,gate.groupCode);
		
		selectTable();
	}
	
	public boolean insertCardBaseChild() throws Exception {
		gb.showLogMessage("I","insertCardBaseChild()! start");

		boolean blResult=true;
		daoTable = addTableOwner("CCA_CARDBASE_CHILD");



		setValue("CARD_NO",gate.cardNo);
		setValue("CARD_ACCT_ID",gate.cardAcctIdx);
		setValue("CARD_ACCT_ID_SEQ","0");//Howard: DԣȡAҥH"0" 
		setValue("REJ_AUTH_CNT_DAY","0"); 
		setValue("REJ_AUTH_CNT_MONTH","0"); 
		setValue("TOT_AMT_CONSUME","0");  
		setValue("TOT_AMT_PRECASH","0"); 

		setValue("CRT_DATE",gb.getSysDate());
		setValue("crt_user","AUTH");
		setValue("MOD_USER","AUTH");



		//setTimestamp("MOD_TIME",gate.sG_TransactionStartDateTime);
		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		//setTimestamp("MOD_TIME",HpeUtil.getCurTimestamp());



		setValue("mod_pgm",gb.getSystemName());

		insertTable();



		return blResult;



	}
	//kevin:取消此TABLE改為selectHsmKeys (ptr_hsm_keys)
//	public boolean selectCsseccfg(String sP_DestId) throws Exception {
//		///*讀取RecalBox資料檔
//		daoTable  = addTableOwner("CCA_Csseccfg"); 
//		selectSQL = " ZPK  as CsseccfgZpk,"
//				+"ZMK,"
//				+"PVK1 as CsseccfgPvk1,"
//				+"PVK2 as CsseccfgPvk2,"
//				+"CVKA  as CsseccfgCvka,"
//				+"CVKB  as CsseccfgCvkb,"
//				+"PINFCODE,"
//				+"PVKI as CsseccfgPvki,"
//				+"EFF_FLAG as CsseccfgEffFlag,"
//				+"MK_AC as CsseccfgMkAc,"
//				+"MAC,"
//				+"ZPK_2,"
//				+"ZMK_2,"
//				+"ZPK_3 as CsseccfgZpk3,"
//				+"ZMK_3";
//		whereStr  = "WHERE DEST_ID = ? ";
//		setString(1,sP_DestId);
//		selectTable();
//
//		if ( "Y".equals(notFound) ) {
//			return false;
//		}    		   
//
//
//		return true;
//	}
//	public boolean selectHsmKeys_old(String sP_HsmKeyOrg) throws Exception {
//		///*讀取HSM KEYS資料檔
//		daoTable  = addTableOwner("PTR_HSM_KEYS"); 
//		selectSQL = " VISA_PVKA as PtrHsmVisaPvkA,"
//				+" VISA_PVKB    as PtrHsmVisaPvkB,"
//				+" MASTER_PVKA  as PtrHsmMasterPvkA,"
//				+" MASTER_PVKB  as PtrHsmMasterPvkB,"
//				+" JCB_PVKA     as PtrHsmJcbPvkA,"
//				+" JCB_PVKB     as PtrHsmJcbPvkB,"
//				+" VISA_CVKA    as PtrHsmVisaCvkA,"
//				+" VISA_CVKB    as PtrHsmVisaCvkB,"
//				+" MASTER_CVKA  as PtrHsmMasterCvkA,"
//				+" MASTER_CVKB  as PtrHsmMasterCvkB,"
//				+" JCB_CVKA     as PtrHsmJcbCvkA,"
//				+" JCB_CVKB     as PtrHsmJcbCvkB,"
//				+" VISA_MDK     as PtrHsmVisaMdk,"
//				+" MASTER_MDK   as PtrHsmMasterMdk,"
//				+" JCB_MDK      as PtrHsmJcbMdk,"
//				+" NET_ZPK      as PtrHsmKeysZpk,";
//		whereStr  = "WHERE HSM_KEY_ORG = ? ";
//		setString(1,sP_HsmKeyOrg);
//		selectTable();
//
//		if ( "Y".equals(notFound) ) {
//			return false;
//		}    		   
//		
//		return true;
//	}
	public boolean selectHsmKeys(String spHsmKeyOrg1) throws Exception {
		gb.showLogMessage("I","selectHsmKeys()! start");

		daoTable  = addTableOwner("PTR_HSM_KEYS"); 
		//down, get HSM all keys 
		selectSQL = "VISA_PVKA    as PtrHsmVisaPvkA, "
				  + "VISA_PVKB    as PtrHsmVisaPvkB, "
				  + "MASTER_PVKA  as PtrHsmMasterPvkA, "
				  + "MASTER_PVKB  as PtrHsmMasterPvkB, "
				  + "JCB_PVKA     as PtrHsmJcbPvkA, "
				  + "JCB_PVKB     as PtrHsmJcbPvkB, "
				  + "VISA_CVKA    as PtrHsmVisaCvkA, "
				  + "VISA_CVKB    as PtrHsmVisaCvkB, "
				  + "MASTER_CVKA  as PtrHsmMasterCvkA, "
				  + "MASTER_CVKB  as PtrHsmMasterCvkB, "
				  + "JCB_CVKA     as PtrHsmJcbCvkA, "
				  + "JCB_CVKB     as PtrHsmJcbCvkB, "
				  + "VISA_MDK     as PtrHsmVisaMdk, "
				  + "MASTER_MDK   as PtrHsmMasterMdk, "
				  + "JCB_MDK      as PtrHsmJcbMdk, "
				  + "NET_ZPK      as PtrHsmKeysZpk, "
				  + "ATM_ZPK      as PtrHsmAtmZpk, "
				  + "EBK_ZEK      as PtrHsmAtmZek ";
		whereStr  = "WHERE HSM_KEYS_ORG = ? ";
		setString(1,spHsmKeyOrg1);
		selectTable();

		if ( "Y".equals(notFound) ) {
			return false;
		}
		if (gate.hsmKeyCavv.equals(spHsmKeyOrg1)) {
			gate.visaCavvA   = getValue("PtrHsmVisaCvkA");
			gate.visaCavvB   = getValue("PtrHsmVisaCvkB");
			gate.masterCavvA = getValue("PtrHsmMasterCvkA");
			gate.masterCavvB = getValue("PtrHsmMasterCvkB");
			gate.jcbCavvA    = getValue("PtrHsmJcbCvkA");
			gate.jcbCavvB    = getValue("PtrHsmJcbCvkB");
			return true;
		}
		gate.visaPvkA   = getValue("PtrHsmVisaPvkA");
		gate.visaPvkB   = getValue("PtrHsmVisaPvkB");
		gate.masterPvkA = getValue("PtrHsmMasterPvkA");
		gate.masterPvkB = getValue("PtrHsmMasterPvkB");
		gate.jcbPvkA    = getValue("PtrHsmJcbPvkA");
		gate.jcbPvkB    = getValue("PtrHsmJcbPvkB");
		gate.visaCvkA   = getValue("PtrHsmVisaCvkA");
		gate.visaCvkB   = getValue("PtrHsmVisaCvkB");
		gate.masterCvkA = getValue("PtrHsmMasterCvkA");
		gate.masterCvkB = getValue("PtrHsmMasterCvkB");
		gate.jcbCvkA    = getValue("PtrHsmJcbCvkA");
		gate.jcbCvkB    = getValue("PtrHsmJcbCvkB");
		gate.visaMdk    = getValue("PtrHsmVisaMdk");
		gate.masterMdk  = getValue("PtrHsmMasterMdk");
		gate.jcbMdk     = getValue("PtrHsmJcbMdk");
		gate.keysZpk    = getValue("PtrHsmKeysZpk");
		gate.atmZpk     = getValue("PtrHsmAtmZpk");
		gate.atmZek     = getValue("PtrHsmAtmZek");
		return true;
	}

		//up, get PaidConsumeFee  //結帳-消費
	public boolean selectCardBaseChild() throws Exception {
		gb.showLogMessage("I","selectCardBaseChild()! start");

		//讀取子卡卡片帳務檔
		daoTable  = addTableOwner("CCA_CARDBASE_CHILD"); 
		selectSQL = "NVL(CARD_NO,' ') as CardbaseChildCardNo,"						/* 卡號*/
				+"NVL(REJ_AUTH_CNT_MONTH,0) as CardbaseChildRejAuthCntMonth,"	/* 月拒絕授權次數 */
				+"NVL(CARD_ACCT_ID,' ') as CardbaseChildCardAcctId,"          /* 卡戶證號 */
				+ "NVL(CARD_ACCT_ID_SEQ,'0') as CardbaseChildCardAcctIdSeq,"	/* 卡戶證序號 */
				+ "NVL(TOT_AMT_CONSUME,0) as CardbaseChildTotAmtConsume,"		///* 消費總金額
				+ "NVL(TOT_AMT_PRECASH,0) as CardbaseChildTotAmtPrecash";		///* 預借現金 總金額
		whereStr  = "WHERE CARD_NO = ? ";
		setString(1,gate.cardNo);
		selectTable();

		if ( "Y".equals(notFound) ) {
			insertCardBaseChild();
		}    		   
		return true;
	}

	public boolean updateAtmData(boolean bpIsNewCard) throws Exception {
		gb.showLogMessage("I","updateAtmData()! start");
		//Howard: 要update CCA_CARD_BASE
		boolean blResult = false;
		StringBuffer sblSql = new StringBuffer();
		int nlParmPos=1;
		daoTable = addTableOwner("CCA_CARD_BASE");
		if (bpIsNewCard) {
			String slOldPin = getValue("CardBaseNewPin");
			String slOldPvv = getValue("CardBaseNewPvv");

			sblSql.append("OLD_PIN = ?,");
			setString(nlParmPos,slOldPin);	
			nlParmPos++;

			sblSql.append("OLD_PVV = ?,");
			setString(nlParmPos,slOldPvv);	
			nlParmPos++;
			String slNewPin =gate.newPinFromHsm;
			String slNewPvv = HpeUtil.transPasswd(0, gate.pvv);


			sblSql.append("PIN = ?,");
			setString(nlParmPos,slNewPin);	
			nlParmPos++;

			sblSql.append("PVV = ?,");
			setString(nlParmPos,slNewPvv);	
			nlParmPos++;

			//gb.showLogMessage("D","sL_NewPin=>" + sL_NewPin +"--");
			//gb.showLogMessage("D","sL_NewPvv=>" + sL_NewPvv +"--");


		}
		else {
			String slNewPin = getValue("CardBaseOldPin");
			String slNewPvv = getValue("CardBaseOldPvv");


			sblSql.append("PIN = ?,");
			setString(nlParmPos,slNewPin);	
			nlParmPos++;

			sblSql.append("PVV = ?,");
			setString(nlParmPos,slNewPvv);	
			nlParmPos++;
			String slOldPin = "";
			String slOldPvv = "";

			sblSql.append("OLD_PIN = ?,");
			setString(nlParmPos,slOldPin);	
			nlParmPos++;

			sblSql.append("OLD_PVV = ?,");
			setString(nlParmPos,slOldPvv);	
			nlParmPos++;
			//gb.showLogMessage("D","sL_OldPin=>" + sL_OldPin +"--");
			//gb.showLogMessage("D","sL_OldPvv=>" + sL_OldPvv +"--");

		}

		//down, update 公用欄位
		sblSql.append("MOD_USER = ?,");
		setString(nlParmPos, "AUTH");	
		nlParmPos++;

		sblSql.append("MOD_PGM = ?,");
		setString(nlParmPos, gb.getSystemName());	
		nlParmPos++;


		sblSql.append("MOD_TIME = ? ");
		setTimestamp(nlParmPos,gb.getgTimeStamp()); 
		nlParmPos++;

		updateSQL =  sblSql.toString();

		whereStr  = "WHERE  CARD_NO = ? ";

		setString(nlParmPos,gate.cardNo);

		//gb.showLogMessage("D","cardNo=>" + gate.cardNoMask +"--");


		updateTable();
		//up, update 公用欄位


		blResult = true;


		return blResult;
	}
	public boolean selectCardBase() throws Exception {
		gb.showLogMessage("I","selectCardBase()! start");

		daoTable  = addTableOwner("CCA_CARD_BASE"); 
		selectSQL = "Acno_P_SEQNO as CardBaseAcnoPSeqNo,"
				+ "ID_P_SEQNO as CardBaseIdPSeqNo,"
				+ "ACCT_TYPE as CardBaseAcctType,"    		   
				+ "DEBIT_FLAG as CardBaseDebitFlag,"
				//+ "GP_NO as CardBaseGpNo,"
				+ "P_SEQNO as CardBasePSeqNo,"
				+ "CORP_P_SEQNO as CardBaseCorpPSeqNo,"
				+ "NVL(TOT_AMT_DAY,0) as CardBaseTotAmtDay," //日累積消費金額
				+ "NVL(TOT_CNT_DAY,0) as CardBaseTotCntDay," //日累積消費次數
				+ "CARD_NOTE as CardBaseCardNote,"  //卡片等級
				+ "ACNO_FLAG as CardBaseAcnoFlag," //--1.一般,2.總繳公司,3.商務個繳,Y.總繳個人  [Howard 20180627: 與仁和來哥討論後，移除了 CORP_FLAG，而增加了此欄位 ]
				+ "voice_auth_code as CardBaseVoiceAuthCode," //新卡語音授權密碼
				+ "voice_auth_code2  as CardBaseVoiceAuthCode2," //舊卡語音授權密碼
				+ "PRE_AUTH_CODE_1 as CardBasePreAuthCode1, " //預先授權核准碼(1)
				+ "PRE_AUTH_CODE_2 as CardBasePreAuthCode2, " //預先授權核准碼(2)
				+ "NVL(PRE_AUTH_FLAG,'0') as CardBasePreAuthFlag," //預先授權註記
				+ "NVL(PRE_AUTH_DATE_1,'0000000') as CardBasePreAuthDate1,"//預先授權日期(1)
				+ "NVL(PRE_AUTH_AMT_1,0) as CardBasePreAuthAmt1," //預先授權金額(1)
				+ "NVL(PRE_AUTH_EFF_DATE_END_1,'00000000') as CardBasePreAuthEffDateEnd1,"//預先授權有效日期(1)
				+ "NVL(WRITE_OFF_1,'0') as CardBaseWriteOff1," //預先授權沖消狀態(1) 
				+ "NVL(PRE_AUTH_DATE_2,'00000000') as CardBasePreAuthDate2,"
				+ "NVL(PRE_AUTH_AMT_2,0) as CardBasePreAuthAmt2,"
				+ "NVL(PRE_AUTH_EFF_DATE_END_2,'00000000') as CardBasePreAuthEffDateEnd2,"
				+ "NVL(WRITE_OFF_2,'0') as CardBaseWriteOff2,"
				+ "NVL(SPEC_FLAG,'N') as CardBaseSpecFlag,"
				+ "NVL(SPEC_MST_VIP_AMT,0) as CardBaseSpecMstVipAmt,"
				+ "NVL(SPEC_DEL_DATE,'00000000') as CardBaseSpecDelDate,"
				+ "NVL(CVV_ERROR_CNT,0) as CardBaseCvvErrorCnt,"
				+ "CVV_ERR_TIME as CardBaseCvvErrorTime,"
				+ "OLD_PIN as CardBaseOldPin,"
				+ "OLD_PVV as CardBaseOldPvv,"
				+ "PIN as CardBaseNewPin,"
				+ "PVV as CardBaseNewPvv,"
				
                 + "NVL(LAST_AMT,0) as CardBaseLastAmt,"
                 + "NVL(PREV_AMT,0) as CardBasePrevAmt,"
                 + "NVL(PIN_CNT,0) as CardBasePinCnt,"
                 + "LAST_CONSUME_DATE as CardBaseLastConsumeDate,"
                 + "LAST_CONSUME_TIME as CardBaseLastConsumeTime,"
                 + "LAST_AUTH_CODE as CardBaseLastAuthCode,"
                 + "LAST_AMT as CardBaseLastAmt,"
                 + "LAST_CURRENCY as CardBaseLastCurrency,"
                 + "LAST_COUNTRY as CardBaseLastCountry,"
                 //+ "OLD_TRANS_CVV2 as CardBaseOldTransCvv2," //Howard:改讀 CRD_CARD and DBC_CARD
                 + "voice_open_code as CardBaseVoiceOpenCode,"
                 + "voice_open_code2 as CardBaseVoiceOpenCode2,"

                 + "SPEC_STATUS as CardBaseSpecStatus, "
                 + "SPEC_DEL_DATE as CardBaseSpecDelDate,"
                 + "spec_mst_vip_amt as CardBaseSpecMstVipAmt,"
                 + "card_adj_limit as CardBaseChildCardTmpLimit," /* 子卡臨時額度         */
                 + "card_adj_date1 as CardBaseChildCardEffStartDate," /* 子卡臨調效期起       */
                 + "card_adj_date2 as CardBaseChildCardEffEndDate," /* 子卡臨調效期迄       */
                 + "NVL(CARD_ACCT_IDX,0) as CardBaseCardAcctIdx ";
		//+ "CARD_INDICATOR as CardBaseCardIndicator"; //商務卡註記 => 此欄位不存此table, 採由 PTR_ACCT_TYPE 中取得                 

		whereStr  = "WHERE CARD_NO = ? ";
		setString(1,gate.cardNo);
		selectTable();

		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.selectCardBase -- can not find data. CARD_NO is  "+gate.cardNoMask + "--");
			return false; 
		}

		if ("Y".equals(getValue("CardBaseDebitFlag")))
			gate.isDebitCard = true;

		gate.cardAcctIdx = getValue("CardBaseCardAcctIdx").trim();

		gate.cardBaseTotAmtDay = getInteger("CardBaseTotAmtDay");
		gate.cardBaseTotCntDay = getInteger("CardBaseTotCntDay");

		gate.cardBaseSpecFlag = getValue("CardBaseSpecFlag");
		gate.cardBaseSpecStatus = getValue("CardBaseSpecStatus");
		gate.cardBaseSpecDelDate = getValue("CardBaseSpecDelDate");

		//System.out.println( "Card No=>" + gate.cardNo);
		//System.out.println( "CardAcctIdx=>" + gate.CardAcctIdx);
		//System.out.println( "cardBaseTotAmtDay=>" + gate.cardBaseTotAmtDay);
		//System.out.println( "cardBaseTotCntDay=>" + gate.cardBaseTotCntDay);

		//selectPtrAcctType(getValue("CardBaseAcctType").trim());
		return true;
	}

	//ˮ block code
	public boolean isBlocked() throws Exception {
		gb.showLogMessage("I","isBlocked()! start");

		boolean blResult = false;
		/*
	   act_acno ̭BLOCK_REASON1~BLOCK_REASON5, @ӦȴNnblock.
	   act_acno.SPEC_STATUS and crd_card.SPEC_STATUS OS => Bz޿µ{
		 */



		if ((getValue("CardAcctBlockReason1").trim().length()>0)  || 
				(getValue("CardAcctBlockReason2").trim().length()>0) ||
				(getValue("CardAcctBlockReason3").trim().length()>0) || 
				(getValue("CardAcctBlockReason4").trim().length()>0) ||
				(getValue("CardAcctBlockReason5").trim().length()>0) ){
			//gb.showLogMessage("D","CardAcctBlockReason1~5 任一個有值，所以凍結！");
			blResult = true;
		}
		else if ((getValue("CardAcctBlockReason1OfComp").trim().length()>0)  || 
				(getValue("CardAcctBlockReason2OfComp").trim().length()>0) ||
				(getValue("CardAcctBlockReason3OfComp").trim().length()>0) || 
				(getValue("CardAcctBlockReason4OfComp").trim().length()>0) ||
				(getValue("CardAcctBlockReason5OfComp").trim().length()>0) ){
			//gb.showLogMessage("D","CardAcctBlockReason1OfComp~5 任一個有值，所以凍結！");
			blResult = true;
		}
		else {
			//gb.showLogMessage("D","CardAcctBlockReason1~5 都沒有值，所以沒有凍結！");		   
		}



		return blResult; 
		/*
	   select count(*) from CCS_SPEC_CODE A, CRD_CARD B
	   where spec_code in (B.block_code1,B.block_code2,B.block_code3,B.block_code4,B.block_code5)
	   and nvl(spec_resp_code,'n')='Y'
	   and B.card_no='5412342000009545'
		 */


	}
	// Ū CARD_IDNO
	public boolean selectCardIdNo() throws Exception {
		gb.showLogMessage("I","selectCardIdNo()! start");

		if (gate.isDebitCard) {
			daoTable  = addTableOwner("DBC_IDNO");
		}
		else {
			daoTable  = addTableOwner("CRD_IDNO");
		}
		selectSQL = "ID_P_SEQNO  , "
				+ "ID_NO     as CrdIdNoIdNo  , "
				+ "ID_NO_CODE       , "
				+ "CHI_NAME as CrdIdNoChiName , "
				+ "ENG_NAME as CrdIdNoEngName , "
				+ "BIRTHDAY as CrdIdNoBirthday , "
				+ "OFFICE_TEL_NO1 as CrdIdNoOfficeTelNo1,"
				+ "HOME_TEL_NO1 as CrdIdNoHomeTelNo1,"
				+ "CELLAR_PHONE  as CrdIdNoCellPhone, "
				+ "E_MAIL_ADDR as CrdIdNoEmail, "
                + "MSG_FLAG as CrdIdnoMsgFlag, "                //發簡訊旗標 Y=發簡訊,N=不發簡訊;V1.00.08 新簡訊發送規則
                + "MSG_PURCHASE_AMT as CrdIdnoMsgPurchaseAmt "; //發簡訊消費金額;V1.00.08 新簡訊發送規則

		String slIdPSeqNo = getValue("ID_P_SEQNO");
		whereStr  = "WHERE ID_P_SEQNO = ? ";
		setString(1,slIdPSeqNo);

		selectTable();

		gb.showLogMessage("D","Card NO=>" + gate.cardNoMask);
//		gb.showLogMessage("D","CrdIdNoIdNo=>" + getValue("CrdIdNoIdNo"));
		gb.showLogMessage("D","ID_P_SEQNO=>" + getValue("ID_P_SEQNO"));
//		gb.showLogMessage("D","CrdIdNoChiName=>" + getValue("CrdIdNoChiName"));
//		gb.showLogMessage("D","CrdIdNoEngName=>" + getValue("CrdIdNoEngName"));
		gb.showLogMessage("D","CrdIdNoCellPhone=>" + HpeUtil.getMaskData(getValue("CrdIdNoCellPhone"), 4, "#") );
		gb.showLogMessage("D","CrdIdNoBirthday=>" + getValue("CrdIdNoBirthday"));

		if ( "Y".equals(notFound) )
		{ return false; }

		return true;
	}

	//kevin:取消此作業，改回原selectCardIdNo取得
//	public boolean selectCardIdNoById(String spId) throws Exception {
//
//		daoTable  = addTableOwner("CRD_IDNO");
//		selectSQL = "ID_P_SEQNO  , "
//				+ "ID_NO       , "
//				+ "ID_NO_CODE       , "
//				+ "CHI_NAME as CrdIdNoChiName , "
//				+ "ENG_NAME as CrdIdNoEngName , "
//				+ "BIRTHDAY as CrdIdNoBirthday , "
//				+ "OFFICE_TEL_NO1 as CrdIdNoOfficeTelNo1,"
//				+ "HOME_TEL_NO1 as CrdIdNoHomeTelNo1,"
//				+ "CELLAR_PHONE  as CrdIdNoCellPhone, "
//				+ "E_MAIL_ADDR  ";
//
//		whereStr  = "WHERE ID_P_SEQNO = ? and ID_NO=? ";
//		setString(1,getValue("ID_P_SEQNO"));
//		setString(2, spId);
//		selectTable();
//		if ( "Y".equals(notFound) )
//		{ return false; }
//
//		return true;
//	}
	//Select PRIMARY CARD_IDNO(!!!kevin)
	public boolean selectPrimaryCardIdNo() throws Exception {
		gb.showLogMessage("I","selectPrimaryCardIdNo()! start");

		if (gate.isDebitCard) {
			daoTable  = addTableOwner("DBC_IDNO");
		}
		else {
			daoTable  = addTableOwner("CRD_IDNO");
		}		

		selectSQL = "ID_P_SEQNO  , "
				+ "ID_NO     as CrdIdNoPrimIdNo  , "
				+ "ID_NO_CODE       , "
				+ "CELLAR_PHONE  as CrdIdNoPrimCellPhone, "
				+ "SMS_PRIM_CH_FLAG as CrdIdNoSmsPrimChFlag, "
				+ "E_MAIL_ADDR  ";

		String slIdPSeqNo = getValue("CardAcctIdPSeqNo");
		whereStr  = "WHERE ID_P_SEQNO = ? ";
		setString(1,slIdPSeqNo);

		selectTable();

		gb.showLogMessage("D","CrdIdNoPrimIdNo=>" + getValue("CrdIdNoPrimIdNo"));
		gb.showLogMessage("D","ID_P_SEQNO=>" + getValue("ID_P_SEQNO"));
		gb.showLogMessage("D","CrdIdNoPrimCellPhone=>" + getValue("CrdIdNoPrimCellPhone"));
		gb.showLogMessage("D","CrdIdNoSmsPrimChFlag=>" + getValue("CrdIdNoSmsPrimChFlag"));
		
		if ( "Y".equals(notFound) ) {
			return false; 
		} 
		else if ("Y".equals(getValue("CrdIdNoSmsPrimChFlag"))) {
			return true; 
		}

		return false;
	}

	/* marked by Howard: 20180314
   public void get_okCode(String okCode) throws Exception {
	   //gb.ddd("get-okCode="+okCode);
	   gate.auth_err_id = okCode;

	   //daoTid ="ok.";
	   daoTable ="CCSV_AUTH_RESP";	//View
	   selectSQL ="auth_status_code,"
			   +"nccc_p38, nccc_p39, normal_flag"
			   ;
	   whereStr="WHERE auth_err_id =?";
       setString(1,okCode);
       selectTable();
       if ( "Y".equals(notFound) ){
    	   gate.isoField[39] ="00";
    	   gate.isoField[38] ="00";
    	   return;
       }
       gate.isoField[39] = getValue("nccc_p39");
       gate.auth_status_code = getValue("auth_status_code");


   }
	 */

	public int addCardPasswdErrCount(String spCardNo) throws Exception {
		gb.showLogMessage("I","addCardPasswdErrCount()! start");

		daoTable = addTableOwner("CRD_CARD");
		String slSql = " PASSWD_ERR_COUNT=PASSWD_ERR_COUNT+1 ";
		updateSQL =  slSql.toString();
		whereStr  = "WHERE  CARD_NO = ? ";
		//sqlCmd = "UPDATE "+daoTable+" SET "+updateSQL+" "+whereStr;
		setString(1,spCardNo);

		return updateTable();


	}

	public int resetCardPasswdErrCount(String spCardNo) throws Exception
	{
		gb.showLogMessage("I","resetCardPasswdErrCount()! start");
		daoTable = addTableOwner("CRD_CARD");
		String slSql = " PASSWD_ERR_COUNT=0 ";
		updateSQL =  slSql.toString();
		whereStr  = "WHERE  CARD_NO = ? ";

		setString(1,spCardNo);
		/*
      try {
		setString(1,sP_CardNo);
      } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
      }
		 */

		return updateTable();


	}

	public void insertAuthBitData() throws Exception {
		gb.showLogMessage("I","insertAuthBitData()! start");

		daoTable = addTableOwner("CCA_AUTH_BITDATA");

//		gb.showLogMessage("D","@@@@@CCA_AUTH_BITDATA="+gate.txDate+"CARDNO"+gate.cardNoMask);

		setValue("TX_DATE", gb.getSysDate());
		setValue("TX_TIME", gb.getSysTime());
		setTimestamp("TX_DATETIME", gb.getgTimeStamp());

//		setValue("auth_seqno", getValue("txlog.auth_seqno"));
//		setValue("auth_seqno", getValue("auth_seqno"));
		setValue("auth_seqno", gate.authSeqno);
		setValue("trans_type", gate.transType);
		setValue("card_no", gate.cardNo);
		setValue("bitmap", gate.isoBitMap);
		
		setValue("bit2_acct_no", gate.isoField[2]);		
		setValue("bit3_proc_code", gate.isoField[3]);
		setValue("bit4_tran_amt", gate.isoField[4]);
		setValue("bit6_billing_amt", gate.isoField[6]);
		setValue("bit7_date_time", gate.isoField[7]);
		setValue("bit10_billing_rate", gate.isoField[10]);
		setValue("bit11_audit_no", gate.isoField[11]);
		setValue("bit12_local_time", gate.isoField[12]);
		setValue("bit13_local_date", gate.isoField[13]);
		setValue("bit14_expire_date", gate.isoField[14]);
		setValue("bit15_setl_date", gate.isoField[15]);
		setValue("bit17_cap_date", gate.isoField[17]);
		setValue("bit18_mcht_cat_code", gate.isoField[18]);
		setValue("bit19_acq_country_code", gate.isoField[19]);
		setValue("bit22_pos_entrymode", gate.isoField[22]);
		setValue("bit25_pos_condmode", gate.isoField[25]);
		setValue("bit26_pin_len", gate.isoField[26]);
		setValue("bit28_trans_fee", gate.isoField[28]);
		setValue("bit31_tid", gate.isoField[31]);
		setValue("bit32_code", gate.isoField[32]);
		setValue("bit35_track_ii", gate.isoField[35]);
		setValue("bit37_ref_no", gate.isoField[37]);
		setValue("bit38_appr_code", gate.isoField[38]);
		setValue("bit39_adj_code", gate.isoField[39]);
		setValue("bit41_term_id", gate.isoField[41]);
		setValue("bit42_card_acceptor_code", gate.isoField[42]);
		setValue("bit43_mcht_loc", HpeUtil.removeInvalidChar(gate.isoField[43]));
		setValue("bit45_track_i", gate.isoField[45]);
//		setValue("bit48_add_data", gate.isoField[48]);
		int bit48Len = gate.isoField[48].length();
		if (bit48Len > 106) {
			setValue("bit48_add_data", gate.isoField[48].substring(0, 79));
			setValue("bit44_add_data", gate.isoField[48].substring(79, 106));
		}
		else if (bit48Len > 79) {
			setValue("bit48_add_data", gate.isoField[48].substring(0, 79));
			setValue("bit44_add_data", gate.isoField[48].substring(79, bit48Len));
		}
		else {
			setValue("bit48_add_data", gate.isoField[48]);
		}
		setValue("bit49_trans_cur_code", gate.isoField[49]);
		setValue("bit50_curr_setl", gate.isoField[50]);
		setValue("bit51_curr_bill", gate.isoField[51]);
		setValue("bit52_pin_data", gate.isoField[52]);
//		setValue("bit55_ic_data", gate.isoField[55]);
		int bit55Len = gate.isoField[55].length();
		if (bit55Len > 500) {
			setValue("bit55_ic_data", gate.isoField[55].substring(0, 500));
		}
		else {
			setValue("bit55_ic_data", gate.isoField[55]);
		}
		setValue("bit58_additional_data", gate.isoField[58]);
		setValue("bit60_pos_info", gate.isoField[60]);
//		setValue("bit61_other_data", gate.isoField[61]);
		int bit61Len = gate.isoField[61].length();
		if (bit61Len > 26) {
			setValue("bit61_other_data", gate.isoField[61].substring(0, 26));
		}
		else {
			setValue("bit61_other_data", gate.isoField[61]);
		}
		setValue("bit62_postal_code", gate.isoField[62]);
		setValue("bit63_additional_data", gate.isoField[63]);
		setValue("bit66_setl_code", gate.isoField[66]);
		setValue("bit70_network", gate.isoField[70]);
		setValue("bit73_act_date", gate.isoField[73]);
		setValue("bit90_org_data", gate.isoField[90]);
		setValue("bit91_file_code", gate.isoField[91]);
		setValue("bit95_repl_amt", gate.isoField[95]);
		setValue("bit99_setl_inst", gate.isoField[99]);
		setValue("bit100_receive_inst_code", gate.isoField[100]);
		setValue("bit101_file_name", gate.isoField[101]);
//		setValue("bit120_mess_data", gate.isoField[120]);
		int bit120Len = gate.isoField[120].length();
		if (bit120Len > 500) {
			setValue("bit120_mess_data", gate.isoField[120].substring(0, 500));
		}
		else {
			setValue("bit120_mess_data", gate.isoField[120]);
		}
		setValue("bit121_issuer", gate.isoField[121]);
		setValue("bit122_open_data", gate.isoField[122]);
		setValue("bit123_addr_data", gate.isoField[123]);
		setValue("bit124_token_data", gate.isoField[124]);
		setValue("bit125_supp_data", gate.isoField[125]);
//		setValue("bit126_caf_data", gate.isoField[126]);
		int hex4Bit48Len = gate.isoField[126].length();
		if (hex4Bit48Len > 260) {
			setValue("bit126_atm_add_data", gate.isoField[126].substring(0, 200));
			setValue("bit121_issuer", gate.isoField[126].substring(200, 260));
		}
		else if (hex4Bit48Len > 200) {
			setValue("bit126_atm_add_data", gate.isoField[126].substring(0, 200));
			setValue("bit121_issuer", gate.isoField[126].substring(200, hex4Bit48Len));
		}
		else {
			setValue("bit126_atm_add_data", gate.isoField[126]);
		}
//		setValue("bit127_rec_data", gate.isoField[127]);
		int bit127Len = gate.isoField[127].length();
		if (bit127Len > 200) {
			setValue("bit127_rec_data", gate.isoField[127].substring(0, 200));
		}
		else {
			setValue("bit127_rec_data", gate.isoField[127]);
		}
		insertTable();
		return;
	}

//	public String getPtrSysParmCustIdIbt() throws Exception {
//		gb.showLogMessage("I","getPtrSysParmCustIdIbt()! start");
//
//		String slResult ="";
//		daoTable = addTableOwner("CCS_GENERAL_PARM");
//		selectSQL ="PARM_VALUE as callWsCustID";
//		whereStr ="where PARM_KEY=?";
//
//		setString(1, "WSDLCUST");
//
//		selectTable();
//		if ( "Y".equals(notFound) ){
//			slResult = "DCSUSER";
//
//		}
//		slResult = getValue("callWsCustID");
//
//		return slResult;
//	}

	//-JH-
	public void setAuthSeqno() throws Exception {
		gb.showLogMessage("I","setAuthSeqno()! start");
		/*
  		String sL_Tmp1=gb.sysDate.substring(2);
  		String sL_Tmp2=gb.sysTime;
  		String sL_Tmp = (gb.sysDate.substring(2)+gb.sysTime).substring(0, 12);
  		//System.out.println(sL_Tmp1);
  		//System.out.println(sL_Tmp2);
  		//System.out.println(sL_Tmp);
		 */
		/*

  		if (1==1) { //Howard: 等人和create 此 function 就可以往下走了
  			gate.auth_seqno="123456789011";
  			return;
  		}
		 */
		//daoTid ="txlog.";
		daoTable = addTableOwner("dual");
		//selectSQL ="uf_auth_seqno() as auth_seqno";

		selectSQL = addTableOwner("SEQ_AUTH_SEQNO.nextval as auth_seqno" + " ");


		selectTable();
		if ( "Y".equals(notFound) ){

			gate.authSeqno =(gb.getSysDate().substring(2)+gb.getSysTime()).substring(0, 12);
			return;
		}
		String slOriAuthSeqNo = getValue("auth_seqno");
		slOriAuthSeqNo  = HpeUtil.fillZeroOnLeft(slOriAuthSeqNo, 6);

		gate.authSeqno = gb.getSysDate().substring(2,8) + slOriAuthSeqNo;  
		//RETURN varchar_format(sysdate,'yymmdd')||substr(to_char(seq_auth_seqno.nextval,'000000'),2);

		return;
	}

	//down, updateAuthTxLog 
	/*	
   public int txlog_update_4(String kk1) throws Exception {
   	//-hfupdate ori-txlog-
WC   	int li_cnt=0, ii=1;
   	daoTable ="CCS_AUTH_TXLOG";

       StringBuffer ls_sql = new StringBuffer();

       //
       ls_sql.append("cacu_amount = ?, ");
       ls_sql.append("cacu_cash = ? , ");
       setString(ii,gate.cacu_amount);	ii++;
       setString(ii,gate.cacu_cash); 	ii++;
       ls_sql.append("unlock_flag='Y', ");
       ls_sql.append("MOD_TIME = sysdate");

       updateSQL = ls_sql.toString();
       whereStr  = "WHERE  auth_seqno =? "; 
       setString(ii,kk1);  ii++;

       li_cnt = updateTable();
       return li_cnt ;
   }
	 */
	//up,updateAuthTxLog


	/**
	 * 計算當日、當月;公司、個人、子卡;一般交易、預借現金，累計金額
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
	 * @throws Exception if any exception occurred
	 */

	public double getAlreadyAuthedNotApplyed(int nlType1, int nlType2)throws Exception {
		gb.showLogMessage("I","getAlreadyAuthedNotApplyed("+nlType1+", "+nlType2+")! start");

		double dlResult = 0;
		String slCurDate= HpeUtil.getCurDateStr(false);

		int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx);

		gb.showLogMessage("D","slCurDate="+slCurDate+";CurrYYYYMM="+slCurDate.substring(0, 6)+";txLogAmtDate="+gate.txlogAmtDate);

		if (nlType1==1) {
			//當日累計金額
			if (nlType2==1) {//預借現金select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and tx_date = and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed1";

//				whereStr  = "where CACU_CASH =? and tx_date =? and card_acct_idx =? ";
				whereStr  = "where CACU_CASH =? and tx_date >=? and card_acct_idx =? ";


				setString(1, "Y");
//				setString(2, slCurDate);
				setString(2, gate.txlogAmtDate);
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed1");
				}

			}
			else {//一般消費select sum(nt_amt) from auth_txlog where CACU_AMOUNT =’Y’ and tx_date = and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed2";

//				whereStr  = "where CACU_AMOUNT =? and tx_date =? and card_acct_idx =? ";
				whereStr  = "where CACU_AMOUNT <>? and tx_date >=? and card_acct_idx =? ";

				setString(1, "N");
//				setString(2, slCurDate);
				setString(2, gate.txlogAmtDate);
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed2");
				}

			}
		}
		else if (nlType1==2) {
			//當月累計金額
			if (nlType2==1) {//預借現金select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and tx_date like ‘’ and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed3";

				whereStr  = "where CACU_CASH =? and tx_date like ? and card_acct_idx =? ";

				setString(1, "Y");
				setString(2, slCurDate.substring(0, 6)+"%");
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed3");
				}

			}
			else {//一般消費select sum(nt_amt) from auth_txlog where CACU_AMOUNT =’Y’ and tx_date like ‘’ and card_acct_idx =

				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed4";

				whereStr  = "where CACU_AMOUNT <>? and tx_date like ? and card_acct_idx =? ";

				setString(1, "N");
				setString(2, slCurDate.substring(0,6)+"%");
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed4");
				}

			}
		}
		if (nlType1==3) {
			//當日累計金額(公司)
			if (nlType2==1) {//預借現金select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and tx_date = and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed1";

//				whereStr  = "where CACU_CASH =? and tx_date =? and corp_p_seqno=? ";
				whereStr  = "where CACU_CASH =? and tx_date >=? and corp_p_seqno=? ";


				setString(1, "Y");
//				setString(2, slCurDate);
				setString(2, gate.txlogAmtDate);
				setString(3, getValue("CardBaseCorpPSeqNo")); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed1");
				}

			}
			else {//一般消費select sum(nt_amt) from auth_txlog where CACU_AMOUNT =’Y’ and tx_date = and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed2";

//				whereStr  = "where CACU_AMOUNT =? and tx_date =? and corp_p_seqno=? ";
				whereStr  = "where CACU_AMOUNT <>? and tx_date >=? and corp_p_seqno=? ";

				setString(1, "N");
//				setString(2, slCurDate);
				setString(2, gate.txlogAmtDate);
				setString(3, getValue("CardBaseCorpPSeqNo"));
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed2");
				}

			}
		}
		else if (nlType1==4) {
			//當月累計金額(公司)
			if (nlType2==1) {//預借現金select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and tx_date like ‘’ and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed3";

				whereStr  = "where CACU_CASH =? and tx_date like ? and corp_p_seqno=? ";

				setString(1, "Y");
				setString(2, slCurDate.substring(0, 6)+"%");
				setString(3, getValue("CardBaseCorpPSeqNo"));
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed3");
				}

			}
			else {//一般消費select sum(nt_amt) from auth_txlog where CACU_AMOUNT =’Y’ and tx_date like ‘’ and card_acct_idx =

				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed4";

				whereStr  = "where CACU_AMOUNT <>? and tx_date like ? and corp_p_seqno=? ";

				setString(1, "N");
				setString(2, slCurDate.substring(0,6)+"%");
				setString(3, getValue("CardBaseCorpPSeqNo"));
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed4");
				}

			}
		}
		else if (nlType1==5) {
			//當月累計金額(子卡)
			//V1.00.04 修改子卡月累績消費邏輯-改用原始卡號                                                                           *
			if (nlType2==1) {//預借現金select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and tx_date like ‘’ and card_acct_idx =
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed5";

//				whereStr  = "where CACU_CASH =? and tx_date like ? and card_no=? ";
				whereStr  = "where CACU_CASH =? and tx_date like ? and ORI_CARD_NO=? ";

				setString(1, "Y");
				setString(2, slCurDate.substring(0, 6)+"%");
				setString(3, getValue("CrdCardOriCardNo"));
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed5");
				}

			}
			else {//一般消費select sum(nt_amt) from auth_txlog where CACU_AMOUNT =’Y’ and tx_date like ‘’ and card_acct_idx =

				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed6";

//				whereStr  = "where CACU_AMOUNT =? and tx_date like ?  and card_no=? ";
				whereStr  = "where CACU_AMOUNT <>? and tx_date like ? and ORI_CARD_NO=? ";

				setString(1, "N");
				setString(2, slCurDate.substring(0,6)+"%");
				setString(3, getValue("CrdCardOriCardNo"));
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotApplyed6");
				}

			}
		}
		return dlResult;
	}

	/**
	 * 計算已授權未請款金額 =>個人/公司*預借現金
	 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
	 * @throws Exception if any exception occurred
	 */

	public double getAlreadyAuthedNotApplyed(int nlType)throws Exception {
		gb.showLogMessage("I","getAlreadyAuthedNotApplyed("+nlType+")! start");

		/*
  		 Howard: 計算 TOT_AMT_PRECASH and TOT_AMT_CONSUME
  		 getAlreadyAuthedNotApplyed(5) => //計算 TOT_AMT_PRECASH (個人)
  		 getAlreadyAuthedNotApplyed(6) => //計算 TOT_AMT_PRECASH (公司)
  		 getAlreadyAuthedNotApplyed(7) => //計算 TOT_AMT_CONSUME (個人)
  		 getAlreadyAuthedNotApplyed(8) => //計算 TOT_AMT_CONSUME (公司)
		 * */
		double dlResult = 0;
		int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx); //Howard: 無論公司或者個人，都是從CardBase 中取得 CardAcctIdx

		if (nlType==5) { //計算 TOT_AMT_PRECASH (個人)
			//CardAcct_TOT_AMT_PRECASH已授權未請款預借現金-個人
			/*
			 *select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and mtch_flag = ‘N’ and card_acct_idx = 個人的IDX
			 * */
			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed5";

			whereStr  = "where CACU_CASH =? and mtch_flag = ? and card_acct_idx = ?";

			setString(1, "Y");
			setString(2, "N");
			setInt(3, nlCardAcctIdx); 
			selectTable();
			if ( !"Y".equals(notFound) ) {
				dlResult = getDouble("AuthedNotApplyed5");
			}

		}
		else if (nlType==6) {//計算 TOT_AMT_PRECASH (公司)
			//CardAcctI_TOT_AMT_PRECASH已授權未請款預借現金-公司

			//select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and mtch_flag = ‘N’  and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)

			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed6";

			whereStr  = "where CACU_CASH =? and mtch_flag = ? and card_acct_idx in ";
			//whereStr  += "(select card_acct_idx from cca_card_acct_index where acct_parent_index = ?)";
			whereStr  += "(select card_acct_idx from "+ addTableOwner("cca_card_acct") + " where corp_p_seqno=? and acno_flag in ('3','Y'))";

			setString(1, "Y");
			setString(2, "N");

			setString(3, getValue("CardBaseCorpPSeqNo")); 
			selectTable();
			if ( !"Y".equals(notFound) ) {
				dlResult = getDouble("AuthedNotApplyed6");
			}

		}
		/* old....
		else if (nL_Type==6) {//計算 TOT_AMT_PRECASH (公司)
			//CardAcctI_TOT_AMT_PRECASH已授權未請款預借現金-公司

			 //select sum(nt_amt) from auth_txlog where CACU_CASH =’Y’ and mtch_flag = ‘N’  and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)

			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed6";

			whereStr  = "where CACU_CASH =? and mtch_flag = ? and card_acct_idx in (select card_acct_idx from cca_card_acct_index where acct_parent_index = ?)";

			setString(1, "Y");
			setString(2, "N");
			setInt(3, nL_CardAcctIdx); 
			selectTable();
			if ( !"Y".equals(notFound) ) {
				dL_Result = getDouble("AuthedNotApplyed6");
			}

		}
		 */
		else if (nlType==7) {//計算 TOT_AMT_CONSUME (個人)
			//CardAcct_TOT_AMT_CONSUME已授權未請款含預借現金-個人
			/*
			 * select sum(nt_amt) from auth_txlog where CACU_AMOUNT=’Y’ and mtch_flag = ‘N’ and card_acct_idx = 個人的IDX
			 * */

			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed7";

			whereStr  = "where CACU_AMOUNT <>? and mtch_flag = ? and card_acct_idx =? ";

			setString(1, "N");
			setString(2, "N");
			setInt(3, nlCardAcctIdx); 
			selectTable();
			if ( !"Y".equals(notFound) ) {
				dlResult = getDouble("AuthedNotApplyed7");
			}

		}
		else if (nlType==8) { //計算 TOT_AMT_CONSUME (公司)
			//CardAcctI_TOT_AMT_CONSUME已授權未請款含預借現金-公司

			//select sum(nt_amt) from auth_txlog where CACU_AMOUNT=’Y’ and mtch_flag = ‘N’ and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)


			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed8";

			whereStr  = "where CACU_AMOUNT <>? and mtch_flag = ? and card_acct_idx in ";
			whereStr  += "(select card_acct_idx from " + addTableOwner("cca_card_acct") + " where corp_p_seqno=? and acno_flag in ('3','Y'))";

			setString(1, "N");
			setString(2, "N");
			//setInt(3, nL_CardAcctIdx);
			setString(3, getValue("CardBaseCorpPSeqNo"));
			selectTable();

			if ( !"Y".equals(notFound) ) {
				dlResult = getDouble("AuthedNotApplyed8");
			}

		}
		/* Old
		else if (nL_Type==8) { //計算 TOT_AMT_CONSUME (公司)
			//CardAcctI_TOT_AMT_CONSUME已授權未請款含預借現金-公司

  		   //select sum(nt_amt) from auth_txlog where CACU_AMOUNT=’Y’ and mtch_flag = ‘N’ and card_acct_idx in (select card_acct_idx from card_acct_index where acct_parent_idx = 公司的IDX)


			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthedNotApplyed8";

			whereStr  = "where CACU_AMOUNT =? and mtch_flag = ? and card_acct_idx in (select card_acct_idx from cca_card_acct_index where acct_parent_index = ?)";

			setString(1, "Y");
			setString(2, "N");
			setInt(3, nL_CardAcctIdx); 
			selectTable();
			if ( !"Y".equals(notFound) ) {
				dL_Result = getDouble("AuthedNotApplyed8");
			}

		}
		 */
		return dlResult;
	}

	//CCA_CARD_ACCT
	public boolean loadCcaCardAcct() throws Exception {
		gb.showLogMessage("I","loadCcaCardAcct : started");
		daoTable  = addTableOwner("CCA_CARD_ACCT");
		selectSQL = "acct_type as CardAcctCardAcctType, " //帳戶帳號類別 
				+ " ID_P_SEQNO as CardAcctIdPSeqNo, "
				+ " P_SEQNO       , " // 帳戶流水號(new)       --Howard: 會等於 act_acno.acno_p_seqno or dba_acno.p_seqno  
				+ " corp_p_seqno   as CardAcctCardCorpPSeqno, "
				+ " acno_flag as CardAcctAcnoFlag, " ///* 帳戶繳款類別   */ --1.一般,2.總繳公司,3.商務個繳,Y.總繳個人

    		   + " card_acct_idx  as CardAcctCardAcctIdx  , "
    		   //    		   + " POSITION    		, "  
    		   + " block_status as CardAcctBlockStatus, " //解禁超駐記
    		   + " block_reason1 as CardAcctBlockReason1, " //禁超原因碼
    		   + " block_reason2 as CardAcctBlockReason2, " //禁超原因碼
    		   + " block_reason3 as CardAcctBlockReason3, " //禁超原因碼
    		   + " block_reason4 as CardAcctBlockReason4, " //禁超原因碼
    		   + " block_reason5 as CardAcctBlockReason5, " //禁超原因碼
    		   //    		   + " RISK_LEVEL as CardAcctRiskLevel, " //JH =>Card_acct.risk_level(I) ŪACT_ACNO.class_code
    		   + " BLOCK_DATE    , "    		  //凍結日期
    		   + " UNBLOCK_DATE    , "    		  //解凍日期
    		   + " SPEC_STATUS as CardAcctSpecStatus     , "	          //特殊指示戶狀態碼
    		   + " SPEC_DEL_DATE  as CardAcctSpecDelDate  , "           //特殊指示戶有效日期
    		   + " SPEC_MST_VIP_AMT , "           //特殊指示戶vip金額
    		   + " adj_quota as CardAcctAdjQuota, "          //臨時調高額度註記
    		   + " adj_eff_start_date as CardAcctAdjEffStartDate, "          //臨時調高生效日期(起)
    		   + " adj_eff_end_date as CardAcctAdjEffEndDate , "          //臨時調高生效日期(迄)
    		   + " adj_area as CardAcctAdjArea, "  //臨時調高有效地區
    		   + " tot_amt_month as CardAcctTotAmtMonth, " //臨調放大總月限% => Howard: 仁和說(20180702)要改存金額，而不是%

    		   //  		   + " LMT_TOT_CONSUME  , "  //JH=> Card_Acct.lmt_tot_consume(`vB[d], :a) ŪACT_ACNO.line_of_credit_amt

    		   //+ " tot_amt_consume as CardAcctTotAmtConsume , " //總授權額(已消未請) => Howard: 改由run time 計算
    		   //+ " tot_amt_precash as CardAcctTotAmtPrecash , " //總預現額(已消未請) => Howard: 改由run time 計算
    		   /* 以下欄位搬到 Cca_Consume
    		   + " tx_tot_amt_month as CardAcctTxTotAmtMonth, " //累積月消費額
    		   + " tx_tot_cnt_month  as CardAcctTxTotCntMonth, " //累積月消費次數
    		   + " tx_tot_amt_day as CardAcctTxTotAmtDay, " //累積日消費額
    		   + " tx_tot_cnt_day as CardAcctTxTotCntDay, " //累積日消費次數
    		   + " fn_tot_amt_month as CardAcctFnTotAmtMonth, " //國外一般消費月總額
    		   + " fn_tot_cnt_month as CardAcctFnTotCntMonth, " //國外一般消費月總次
    		   + " fn_tot_amt_day  as CardAcctFnTotAmtDay, " //國外一般消費日總額
    		   + " fn_tot_cnt_day   as CardAcctFnTotCntDay, " //國外一般消費日總次
    		   + " fc_tot_amt_month as  CardAcctFcTotAmtMonth, " //國外預借現金月總額
    		   + " fc_tot_cnt_month as CardAcctFcTotCntMonth, " //國外預借現金月總次
    		   + " fc_tot_amt_day  as  CardAcctFcTotAmtDay, " //國外預借現金日總額
    		   + " fc_tot_cnt_day as  CardAcctFcTotCntDay, " //國外預借現金日總次
    		   + " rej_auth_cnt_month, " //月拒絕授權次數
    		   + " last_consume_date as CardAcctLastConsumeDate, " //最後消費日期
       		   + " TRAIN_TOT_AMT_MONTH as CardAcctTrainTotalAmtMonth, " //高鐵累積月消費額
    		   + " TRAIN_TOT_AMT_DAY as CardAcctTrainTotalAmtDay, " //高鐵累積日消費額
    		    */
    		   //    		   + " LMT_TOT_CASH      , "   //JH=> Card_Acct.lmt_tot_cash(預借現金最大額度) ŪACT_ACNO.line_of_credit_amt_cash
    		   //    		   + " ACCT_NO           , "   //JH=> Card_Acct.acct_no(debit卡金融卡帳戶) OŪACT_ACNO.combo_acct_no, DBA_ACNO.acct_no
    		   + " adj_inst_pct as CardAcctAdjInstPct, "  //臨調分期付款比率 => Howard: 仁和說(20180702)要改存金額，而不是%
    		   //    		   + " AUTO_INSTALLMENT  , "   //JH=> Card_Acct.auto_installment(自動installment) Ū ACT_ACNO.auto_installment
    		   + " organ_id      	 , "   //主織別id
    		   + " notice_flag       , "   //
    		   + " notice_snd_date   , "
    		   //    		   + " pd_rating         , "  //JH=> Card_Acct.pd_rating Ū ACT_ACNO.curr_pd_rating
    		   + " ccas_class_code   as CardAcctCcasClassCode  , " //授權卡人等級
    		   + " class_valid_date  as CardAcctClassValidDate , "//授權卡人等級有效日期
    		   + " ccas_mcode        as CardAcctCcasMCode, " //MCode
    		   + " mcode_valid_date  as CardAcctMCodeValidDate , " //授權Mcode有效日期
    		   //kevin:新增第一階段card link提供的帳務金額 -top
    		   + " jrnl_bal            as CardAcctJrnlBal, "           //帳戶應繳款金額 (可正負)
    		   + " tot_amt_consume     as CardAcctTotAmtConsume, "     //總授權額(已消費未請款)
    		   + " total_cash_utilized as CardAcctTotalCashUtilized, " //預借現金已使用金額
    		   + " unpay_amt           as CardAcctUnpayAmt, "          //逾期未繳款金額
    		   + " adj_risk_type       as CardAcctAdjRiskType, "       //調整的風險分類
    		   + " adj_risk_flag       as CardAcctAdjRiskFlag, "       //管制高風險交易(Y/N)
       		   //kevin:新增第一階段card link提供的帳務金額 -bottom
    		   + " pay_amt              as CardAcctPayAmt, "
    		   + " nocancel_credit_flag as CardAcctNoCancelCreditFlag ";

		whereStr  = "WHERE CARD_ACCT_IDX =? ";

		//setInt(1, Integer.parseInt(gate.CardAcctIdx));
		setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(gate.cardAcctIdx)));

		/*
       whereStr  = "WHERE P_SEQNO = ? and debit_flag=? "; //也可以用 gate.CardAcctIdx 為 key

       ////gb.showLogMessage("D","----" + getValue("p_seqno") + "***" + getValue("CardBaseDebitFlag") + "^^^");
       setString(1, getValue("p_seqno"));
       setString(2, getValue("CardBaseDebitFlag"));
		 */
		selectTable();
		if ( "Y".equals(notFound) ) {
			return false;
		}


		gate.cardAcctTotAmtMonth= getDouble("CardAcctTotAmtMonth"); //臨調放大總月限金額

		/*
       gate.cardAcctTotAmtConsume = getValue("CardAcctTotAmtConsume");
       gate.cardAcctTotAmtPrecash = getValue("CardAcctTotAmtPrecash");
       gate.cardAcctTxTotAmtMonth = getValue("CardAcctTxTotAmtMonth");
       gate.cardAcctTxTotCntMonth = getValue("CardAcctTxTotCntMonth");
       gate.cardAcctTxTotAmtDay = getValue("CardAcctTxTotAmtDay");
       gate.cardAcctTxTotCntDay = getValue("CardAcctTxTotCntDay");
       gate.cardAcctFnTotAmtMonth=getValue("CardAcctFnTotAmtMonth");
       gate.cardAcctFnTotCntMonth=getValue("CardAcctFnTotCntMonth");       
       gate.cardAcctFnTotAmtDay=getValue("CardAcctFnTotAmtDay");
       gate.cardAcctFnTotCntDay=getValue("CardAcctFnTotCntDay");
       gate.cardAcctFcTotAmtMonth=getValue("CardAcctFcTotAmtMonth");
       gate.cardAcctFcTotCntMonth=getValue("CardAcctFcTotCntMonth");
       gate.cardAcctFcTotAmtDay =getValue("CardAcctFcTotAmtDay");
       gate.cardAcctFcTotCntDay=getValue("CardAcctFcTotCntDay");
		 */

		//gb.showLogMessage("D","傳入 P_SEQNO=>" + getPSeqNo());
		//gb.showLogMessage("D","傳入 CardBaseDebitFlag=>" + getValue("CardBaseDebitFlag"));
		//gb.showLogMessage("D","cardAcct TotAmtMonth=>" + gate.cardAcctTotAmtMonth);


		gate.cardAcctAcnoFlag = getValue("CardAcctAcnoFlag"); //"1":一般(個人卡，個繳) or "2":總繳公司() or "3":商務個繳(商務卡，個繳) or "Y":總繳個人(商務卡，公司總繳)
		return true;
	}

	//CCA_CARD_ACCT
	public boolean loadCcaCardAcctOfCompany(String spAcctType, String spCorpPSeqno) throws Exception {
		gb.showLogMessage("I","loadCcaCardAcct Of Company: started");
		daoTable  = addTableOwner("CCA_CARD_ACCT");
		selectSQL = "CARD_ACCT_IDX as  CardAcctIdxOfComp, " 
				+ " block_status as CardAcctBlockStatusOfComp, " //解禁超駐記
				+ " block_reason1 as CardAcctBlockReason1OfComp, " //禁超原因碼
				+ " block_reason2 as CardAcctBlockReason2OfComp, " //禁超原因碼
				+ " block_reason3 as CardAcctBlockReason3OfComp, " //禁超原因碼
				+ " block_reason4 as CardAcctBlockReason4OfComp, " //禁超原因碼
				+ " block_reason5 as CardAcctBlockReason5OfComp, " //禁超原因碼
				+ " BLOCK_DATE  as CardAcctBlockDateOfComp  , "    		  //凍結日期
				+ " UNBLOCK_DATE  as CardAcctUnBlockDateOfComp  , "    		  //解凍日期
				+ " SPEC_STATUS as CardAcctSpecStatusOfComp     , "	          //特殊指示戶狀態碼
				+ " SPEC_DEL_DATE  as CardAcctSpecDelDateOfComp   , "           //特殊指示戶有效日期
				+ " SPEC_MST_VIP_AMT  as CardAcctSpecMstVipAmtOfComp, "           //特殊指示戶vip金額
				+ " adj_quota as CardAcctAdjQuotaOfComp, "          //臨時調高額度註記
				+ " adj_eff_start_date as CardAcctAdjEffStartDateOfComp, "          //臨時調高生效日期(起)
				+ " adj_eff_end_date as CardAcctAdjEffEndDateOfComp , "          //臨時調高生效日期(迄)
				+ " adj_area as CardAcctAdjAreaOfComp, "  //臨時調高有效地區
				+ " tot_amt_month as CardAcctTotAmtMonthOfComp, " //臨調放大總月限%


				//+ " tot_amt_consume as CardAcctTotAmtConsumeOfComp , " //總授權額(已消未請)  Howard: 改由 runtime 運算
				//+ " tot_amt_precash as CardAcctTotAmtPrecashOfComp , " //總預現額(已消未請)	 Howard: 改由 runtime 運算
				/* 以下欄位搬到 Cca_Consume
    		   + " tx_tot_amt_month as CardAcctTxTotAmtMonthOfComp , " //累積月消費額
    		   + " tx_tot_cnt_month as CardAcctTxTotCntMonthOfComp, " //累積月消費次數
    		   + " tx_tot_amt_day as CardAcctTxTotAmtDayOfComp, " //累積日消費額
    		   + " tx_tot_cnt_day as CardAcctTxTotCntDayOfComp, " //累積日消費次數
    		   + " fn_tot_amt_month as CardAcctFnTotAmtMonthOfComp, " //國外一般消費月總額
    		   + " fn_tot_cnt_month as CardAcctFnTotCntMonthOfComp, " //國外一般消費月總次
    		   + " fn_tot_amt_day  as CardAcctFnTotAmtDayOfComp, " //國外一般消費日總額
    		   + " fn_tot_cnt_day as CardAcctFnTotCntDayOfComp, " //國外一般消費日總次
    		   + " fc_tot_amt_month as CardAcctFcTotAmtMonthOfComp , " //國外預借現金月總額
    		   + " fc_tot_cnt_month as CardAcctFcTotCntMonthOfComp, " //國外預借現金月總次
    		   + " fc_tot_amt_day as CardAcctFcTotAmtDayOfComp, " //國外預借現金日總額
    		   + " fc_tot_cnt_day as CardAcctFcTotCntDayOfComp, " //國外預借現金日總次
    		   + " rej_auth_cnt_month as CardAcctRejAuthCntmonthOfComp, " //月拒絕授權次數
    		   + " last_consume_date as CardAcctLastConsumeDateOfComp, " //最後消費日期

    		   + " TRAIN_TOT_AMT_MONTH as CardAcctTrainTotalAmtMonthOfComp, " //高鐵累積月消費額
    		   + " TRAIN_TOT_AMT_DAY as CardAcctTrainTotalAmtDayOfComp, " //高鐵累積日消費額
				 */
				+ " adj_inst_pct as CardAcctAdjInstPctOfComp, "  //臨調分期付款比率
				+ " ccas_class_code   as CardAcctCcasClassCodeOfComp  , " //授權卡人等級
				+ " class_valid_date  as CardAcctClassValidDateOfComp , "//授權卡人等級有效日期
				+ " ccas_mcode        as CardAcctCcasMCodeOfComp, " //MCode
				+ " mcode_valid_date  as CardAcctMCodeValidDateOfComp , " //授權Mcode有效日期
				//kevin:新增第一階段card link提供的帳務金額 -top
				+ " jrnl_bal            as CardAcctJrnlBalOfComp, "           //帳戶應繳款金額 (可正負)
				+ " tot_amt_consume     as CardAcctTotAmtConsumeOfComp, "     //總授權額(已消費未請款)
				+ " total_cash_utilized as CardAcctTotalCashUtilizedOfComp, " //預借現金已使用金額
				+ " unpay_amt           as CardAcctUnpayAmtOfComp, "          //逾期未繳款金額
				+ " adj_risk_type       as CardAcctAdjRiskTypeOfComp, "       //調整的風險分類
				+ " adj_risk_flag       as CardAcctAdjRiskFlagofComp, "       //管制高風險交易(Y/N)
				//kevin:新增第一階段card link提供的帳務金額 -bottom
				+ " pay_amt              as CardAcctPayAmtOfComp, "
				+ " nocancel_credit_flag as CardAcctNoCancelCreditFlagOfComp ";


		//whereStr  = "WHERE acct_type = ? and corp_p_seqno=? and corp_act_flag=? ";  
		whereStr  = "WHERE acct_type = ? and corp_p_seqno=? and acno_flag=? ";


		setString(1, spAcctType);
		setString(2, spCorpPSeqno);
		setString(3, "2"); //2=>總繳公司
		selectTable();
		if ( "Y".equals(notFound) ) {
			return false;
		}

		gate.cardAcctIdxOfComp = getValue("CardAcctIdxOfComp");
		return true;
	}

	public boolean loadCcaConsume(String spCardAcctIdx, int npType) throws Exception {
		//讀取卡戶帳務檔
		gb.showLogMessage("I","loadCcaConsume : started");
		/*
 	   if (1==1)
 		   return true;
		 */



		daoTable  = addTableOwner("CCA_CONSUME");
		selectSQL = " PAID_ANNUAL_FEE            as CcaConsumePaidAnnualFee ," + /*結帳-呆帳*/ 
				" PAID_SRV_FEE                      as CcaConsumePaidSrvFee, " + /*結帳-費用*/ 
				" PAID_LAW_FEE                      as CcaConsumePaidLawFee, " + /*結帳-摧收款*/ 
				" PAID_PUNISH_FEE                   as CcaConsumePaidPunishFee," + /*結帳-違約金*/ 
				" PAID_INTEREST_FEE                 as CcaConsumePaidInterestFee, " + /*結帳-循環息*/ 
				" PAID_CONSUME_FEE                  as CcaConsumePaidConsumeFee, " + /*結帳-消費*///已關帳未繳款不含預借現金 
				" PAID_PRECASH                      as CcaConsumePaidPrecash," + /*結帳-預現*/ //已關帳未繳款預借現金 
				" PAID_CYCLE                        as CcaConsumePaidCycls," +/*未結帳-違約金*/ 
				" PAID_TOT_UNPAY                    as CcaConsumePaidTotUnPay, " + /*PAID_TOT_UNPAY*/ 
				" UNPAID_ANNUAL_FEE                 as CcaConsumeUnPaid , " +/*未結帳-呆帳*/ 
				" UNPAID_SRV_FEE                    as CcaConsumeUnPaidSrvFee    , " +/*未結帳-費用*/ 
				" UNPAID_LAW_FEE                    as CcaConsumeUnPaidLawFee    , " + /*未結帳-摧收款*/ 
				" UNPAID_INTEREST_FEE               as CcaConsumeUnPaidInterestFee    , " +/*未結帳-循環息*/ 
				" UNPAID_CONSUME_FEE                as CcaConsumeUnPaidConsumeFee    , " +/*未結帳-消費*/ //已請款未關帳不含預借現金 
				" UNPAID_PRECASH                    as CcaConsumeUnPaidPrecash    , " +/*未結帳-預現*/ //已請款未關帳預借現金 
				" ARGUE_AMT                         as CcaConsumeArgueAmt    , " +/*爭議金額*/ 
				" PRE_PAY_AMT                       as CcaConsumePrePayAmt    , " + /*預付款金額(溢繳款)*/ 
				" TOT_UNPAID_AMT                    as CcaConsumeTotUnPaidAmt    , " +/*Payment未消*/ //已付款未銷帳  
				" BILL_LOW_LIMIT                    as CcaConsumeBillLowLimit   , " +/*月平均消費額*/ 
				" BILL_LOW_PAY_AMT                  as CcaConsumeBillLowPayAmt , " + /*應繳總額*/ 
				" IBM_RECEIVE_AMT                   as CcaConsumeIbmReceiveAmt , " +/*預借現金指撥金額*///指撥額度 
				" UNPOST_INST_FEE                   as CcaConsumeUnPostInstFee    , " +/*分期未結帳金額*///分期付款未到期 
				//kevin:down for 合庫第一階段過渡處理由CardLink提供
				" TOT_AMT_CONSUME      as CcaConsumeTotAmtConsume ," +  /* 總授權額(已消費未請款)      => 第一階段由CardLink提供   */  
				" TOT_AMT_PRECASH      as CcaConsumeTotAmtPreCash , " + /* 總預現額(已消費未請款)      => 第一階段由CardLink提供   */ 
				//kevin:down for 合庫第一階段過渡處理day1 & day2
				" auth_txlog_amt_1      as CcaConsumeAuthTxlogAmt1, " +//day1授權紀錄通知總金額    
				" auth_txlog_amt_2      as CcaConsumeAuthTxlogAmt2, " +//day2授權紀錄通知總金額  
				" auth_txlog_amt_cash_1 as CcaConsumeAuthTxlogAmtCash1, " +//day1授權紀錄通知預現總金額    
				" auth_txlog_amt_cash_2 as CcaConsumeAuthTxlogAmtCash2, " +//day2授權紀錄通知預現總金額  
				" TX_TOT_AMT_MONTH     as CcaConsumeTxTotAmtMonth , " +/* 累積月消費額                        */ 
				" TX_TOT_CNT_MONTH     as CcaConsumeTxTotCntMonth , " + /* 累積月消費次數                      */ 
				" TX_TOT_AMT_DAY       as CcaConsumeTxTotAmtDay,  " + /*累積日消費額                        */ 
				" TX_TOT_CNT_DAY       as CcaConsumeTxTotCntDay,  " + /* 累積日消費次數                      */ 
				" FN_TOT_AMT_MONTH     as CcaConsumeFnTotAmtMonth , " + /* 國外一般消費月總額                  */ 
				" FN_TOT_CNT_MONTH     as CcaConsumeFnTotCntMonth , " + /* 國外一般消費月總次                  */ 
				" FN_TOT_AMT_DAY       as CcaConsumeFnTotAmtDay , " +/* 國外一般消費日總額                  */ 
				" FN_TOT_CNT_DAY       as CcaConsumeFnTotCntDay , " +/* 國外一般消費日總次                  */ 
				" FC_TOT_AMT_MONTH     as CcaConsumeFcTotAmtMonth , " +/* 國外預借現金月總額                  */  
				" FC_TOT_CNT_MONTH     as CcaConsumeFcTotCntMonth , " +/* 國外預借現金月總次                  */ 
				" FC_TOT_AMT_DAY       as CcaConsumeFcTotAmyDay , " + /* 國外預借現金日總額                  */  
				" FC_TOT_CNT_DAY       as CcaConsumeFcTotCntDay , " +/* 國外預借現金日總次                  */ 
				" REJ_AUTH_CNT_DAY     as CcaConsumeRejAuthCntDay , " +/* 日拒絕授權次數                      */  
				" REJ_AUTH_CNT_MONTH   as CcaConsumeRejAuthCntMonth , " +/* 月拒絕授權次數                      */  
				" TRAIN_TOT_AMT_MONTH  as CcaConsumeTrainTotAmtMonth ," + /* 高鐵累積月消費額                    */  
				" TRAIN_TOT_AMT_DAY    as CcaConsumeTrainTotAmtDay ," +/* 高鐵累積日消費額                    */ 
				" LAST_CONSUME_DATE    as CcaConsumeLastConsumeDate " ;/* 最後消費日期                    */ 


		whereStr  = "WHERE CARD_ACCT_IDX = ? ";



		setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(spCardAcctIdx)));
		//setInt(1, Integer.parseInt(sP_CardAcctIdx));
		selectTable();

		if ( !"Y".equals(notFound) ) {

			if(npType==2) {//nP_Type 1=> Personal, 2=>Company
				gate.ccaConsumePaidAnnualFeeOfComp = getDouble("CcaConsumePaidAnnualFee");
				gate.ccaConsumePaidSrvFeeOfComp = getDouble("CcaConsumePaidSrvFee");
				gate.ccaConsumePaidLawFeeOfComp = getDouble("CcaConsumePaidLawFee");

				gate.ccaConsumePaidPunishFeeOfComp = getDouble("CcaConsumePaidPunishFee");
				gate.ccaConsumePaidInterestFeeOfComp = getDouble("CcaConsumePaidInterestFee");
				gate.ccaConsumePaidConsumeFeeOfComp = getDouble("CcaConsumePaidConsumeFee");


				gate.ccaConsumePaidPrecashOfComp = getDouble("CcaConsumePaidPrecash");
				gate.ccaConsumePaidCyclsOfComp = getDouble("CcaConsumePaidCycls");

				gate.ccaConsumePaidTotUnPayOfComp = getDouble("CcaConsumePaidTotUnPay");
				gate.ccaConsumeUnPaidOfComp = getDouble("CcaConsumeUnPaid");
				gate.ccaConsumeUnPaidSrvFeeOfComp = getDouble("CcaConsumeUnPaidSrvFee");
				gate.ccaConsumeUnPaidLawFeeOfComp = getDouble("CcaConsumeUnPaidLawFee");

				gate.ccaConsumeUnPaidInterestFeeOfComp = getDouble("CcaConsumeUnPaidInterestFee");
				gate.ccaConsumeUnPaidConsumeFeeOfComp = getDouble("CcaConsumeUnPaidConsumeFee");
				gate.ccaConsumeUnPaidPrecashOfComp = getDouble("CcaConsumeUnPaidPrecash");
				gate.ccaConsumeArgueAmtOfComp = getDouble("CcaConsumeArgueAmt");

				gate.ccaConsumePrePayAmtOfComp = getDouble("CcaConsumePrePayAmt");
				gate.ccaConsumeTotUnPaidAmtOfComp = getDouble("CcaConsumeTotUnPaidAmt");
				gate.ccaConsumeBillLowLimitOfComp = getDouble("CcaConsumeBillLowLimit");
				gate.ccaConsumeBillLowPayAmtOfComp = getDouble("CcaConsumeBillLowPayAmt");

				gate.ccaConsumeIbmReceiveAmtOfComp = getDouble("CcaConsumeIbmReceiveAmt");
				gate.ccaConsumeUnPostInstFeeOfComp = getDouble("CcaConsumeUnPostInstFee");

				//kevin:第一階段上線調整
//				gate.totAmtConsumeOfComp = getAlreadyAuthedNotApplyed(8);
//				gate.totAmtPreCashOfComp = getAlreadyAuthedNotApplyed(6);
				gate.totAmtConsumeOfComp = getAlreadyAuthedNotApplyed(3,2) + getDouble("CcaConsumeAuthTxlogAmt1") + getDouble("CcaConsumeAuthTxlogAmt2") 
										 + getDouble("CcaConsumeTotAmtConsume");
				gate.totAmtPreCashOfComp = getAlreadyAuthedNotApplyed(3,1) + getDouble("CcaConsumeAuthTxlogAmtCash1") + getDouble("CcaConsumeAuthTxlogAmtCash2")
										 + getDouble("CcaConsumeTotAmtPreCash");
				gate.ccaConsumeAuthTxlogAmt1OfComp = getDouble("CcaConsumeAuthTxlogAmt1");
				gate.ccaConsumeAuthTxlogAmtCash1OfComp = getDouble("CcaConsumeAuthTxlogAmtCash1");

//				gb.showLogMessage("D","getAlreadyAuthedNotApplyed(3,2)amt  = "+getAlreadyAuthedNotApplyed(3,2));
//				gb.showLogMessage("D","getAlreadyAuthedNotApplyed(3,1)cash = "+getAlreadyAuthedNotApplyed(3,1));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmt1Comp  = "+ getDouble("CcaConsumeAuthTxlogAmt1"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmt2Comp  = "+ getDouble("CcaConsumeAuthTxlogAmt2"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmtCash1Comp = "+getDouble("CcaConsumeAuthTxlogAmtCash1"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmtCash2Comp = "+getDouble("CcaConsumeAuthTxlogAmtCash2"));
				gb.showLogMessage("D","gate.totAmtConsumeComp  = "+ gate.totAmtConsumeOfComp);
				gb.showLogMessage("D","gate.totAmtPreCashComp  = "+ gate.totAmtPreCashOfComp);

				//gate.CcaConsumeTotAmtConsumeOfComp = getDouble("CcaConsumeTotAmtConsume");
				//gate.CcaConsumeTotAmtPreCashOfComp = getDouble("CcaConsumeTotAmtPreCash");
				

				gate.ccaConsumeTxTotAmtMonthOfComp = getDouble("CcaConsumeTxTotAmtMonth");
				gate.ccaConsumeTxTotCntMonthOfComp = getDouble("CcaConsumeTxTotCntMonth");
				gate.ccaConsumeTxTotAmtDayOfComp = getDouble("CcaConsumeTxTotAmtDay");
				gate.ccaConsumeTxTotCntDayOfComp = getDouble("CcaConsumeTxTotCntDay");

				gate.ccaConsumeFnTotAmtMonthOfComp = getDouble("CcaConsumeFnTotAmtMonth");
				gate.ccaConsumeFnTotCntMonthOfComp = getDouble("CcaConsumeFnTotCntMonth");
				gate.ccaConsumeFnTotAmtDayOfComp = getDouble("CcaConsumeFnTotAmtDay");
				gate.ccaConsumeFnTotCntDayOfComp = getDouble("CcaConsumeFnTotCntDay");

				gate.ccaConsumeFcTotAmtMonthOfComp = getDouble("CcaConsumeFcTotAmtmonth");
				gate.ccaConsumeFcTotCntMonthOfComp = getDouble("CcaConsumeFcTotCntMonth");
				gate.ccaConsumeFcTotAmtDayOfComp = getDouble("CcaConsumeFcTotAmyDay");
				gate.ccaConsumeFcTotCntDayOfComp = getDouble("CcaConsumeFcTotCntDay");

				gate.ccaConsumeRejAuthCntDayOfComp = getDouble("CcaConsumeRejAuthCntDay");
				gate.ccaConsumeRejAuthCntMonthOfComp = getDouble("CcaConsumeRejAuthCntMonth");
				gate.ccaConsumeTrainTotAmtMonthOfComp = getDouble("CcaConsumeTrainTotAmtMonth");
				gate.ccaConsumeTrainTotAmtDayOfComp = getDouble("CcaConsumeTrainTotAmtDay");

			}
			else if(npType==1) {//nP_Type 1=> Personal, 2=>Company
				gate.ccaConsumePaidAnnualFee = getDouble("CcaConsumePaidAnnualFee");
				gate.ccaConsumePaidSrvFee = getDouble("CcaConsumePaidSrvFee");
				gate.ccaConsumePaidLawFee = getDouble("CcaConsumePaidLawFee");

				gate.ccaConsumePaidPunishFee = getDouble("CcaConsumePaidPunishFee");
				gate.ccaConsumePaidInterestFee = getDouble("CcaConsumePaidInterestFee");
				gate.ccaConsumePaidConsumeFee = getDouble("CcaConsumePaidConsumeFee");


				gate.ccaConsumePaidPrecash = getDouble("CcaConsumePaidPrecash");
				gate.ccaConsumePaidCycls = getDouble("CcaConsumePaidCycls");

				gate.ccaConsumePaidTotUnPay = getDouble("CcaConsumePaidTotUnPay");
				gate.ccaConsumeUnPaid = getDouble("CcaConsumeUnPaid");
				gate.ccaConsumeUnPaidSrvFee = getDouble("CcaConsumeUnPaidSrvFee");
				gate.ccaConsumeUnPaidLawFee = getDouble("CcaConsumeUnPaidLawFee");

				gate.ccaConsumeUnPaidInterestFee = getDouble("CcaConsumeUnPaidInterestFee");
				gate.ccaConsumeUnPaidConsumeFee = getDouble("CcaConsumeUnPaidConsumeFee");
				gate.ccaConsumeUnPaidPrecash = getDouble("CcaConsumeUnPaidPrecash");
				gate.ccaConsumeArgueAmt = getDouble("CcaConsumeArgueAmt");

				gate.ccaConsumePrePayAmt = getDouble("CcaConsumePrePayAmt");
				gate.ccaConsumeTotUnPaidAmt = getDouble("CcaConsumeTotUnPaidAmt");
				gate.ccaConsumeBillLowLimit = getDouble("CcaConsumeBillLowLimit");
				gate.ccaConsumeBillLowPayAmt = getDouble("CcaConsumeBillLowPayAmt");

				gate.ccaConsumeIbmReceiveAmt = getDouble("CcaConsumeIbmReceiveAmt");
				gate.ccaConsumeUnPostInstFee = getDouble("CcaConsumeUnPostInstFee");

				//kevin:第一階段上線調整
//				gate.totAmtConsume = getAlreadyAuthedNotApplyed(7);
//				gate.totAmtPreCash = getAlreadyAuthedNotApplyed(5);
				gate.totAmtConsume = getAlreadyAuthedNotApplyed(1,2) + getDouble("CcaConsumeAuthTxlogAmt1") + getDouble("CcaConsumeAuthTxlogAmt2")
				                   + getDouble("CcaConsumeTotAmtConsume");
				gb.showLogMessage("D","AuthedNotApplyed="+getAlreadyAuthedNotApplyed(1,2)+"AuthTxlogAmt1="+ getDouble("CcaConsumeAuthTxlogAmt1")+"AuthTxlogAmt2="+ getDouble("CcaConsumeAuthTxlogAmt2")+"TotAmtConsume="+getDouble("CcaConsumeTotAmtConsume"));
				gate.totAmtPreCash = getAlreadyAuthedNotApplyed(1,1) + getDouble("CcaConsumeAuthTxlogAmtCash1") + getDouble("CcaConsumeAuthTxlogAmtCash2")
				                   + getDouble("CcaConsumeTotAmtPreCash");
				gate.ccaConsumeAuthTxlogAmt1 = getDouble("CcaConsumeAuthTxlogAmt1");				
				gate.ccaConsumeAuthTxlogAmtCash1 = getDouble("CcaConsumeAuthTxlogAmtCash1");				

//				gb.showLogMessage("D","getAlreadyAuthedNotApplyed(1,2)amt  = "+getAlreadyAuthedNotApplyed(1,2));
//				gb.showLogMessage("D","getAlreadyAuthedNotApplyed(1,1)cash = "+getAlreadyAuthedNotApplyed(1,1));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmt1  = "+ getDouble("CcaConsumeAuthTxlogAmt1"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmt2  = "+ getDouble("CcaConsumeAuthTxlogAmt2"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmtCash1 = "+getDouble("CcaConsumeAuthTxlogAmtCash1"));
				gb.showLogMessage("D","CcaConsumeAuthTxlogAmtCash2 = "+getDouble("CcaConsumeAuthTxlogAmtCash2"));
				gb.showLogMessage("D","gate.totAmtConsume  = "+ gate.totAmtConsume);
				gb.showLogMessage("D","gate.totAmtPreCash  = "+ gate.totAmtPreCash);

				
				//gate.CcaConsumeTotAmtConsume = getDouble("CcaConsumeTotAmtConsume");
				//gate.CcaConsumeTotAmtPreCash = getDouble("CcaConsumeTxAmtPreCash");
				
				gate.currTotCashAmt = getAlreadyAuthedNotApplyed(2,1);		//取得當月累計預借現金金額	

				gate.ccaConsumeTxTotAmtMonth = getDouble("CcaConsumeTxTotAmtMonth");
				gate.ccaConsumeTxTotCntMonth = getDouble("CcaConsumeTxTotCntMonth");
				gate.ccaConsumeTxTotAmtDay = getDouble("CcaConsumeTxTotAmtDay");
				gate.ccaConsumeTxTotCntDay = getDouble("CcaConsumeTxTotCntDay");

				gate.ccaConsumeFnTotAmtMonth = getDouble("CcaConsumeFnTotAmtMonth");
				gate.ccaConsumeFnTotCntMonth = getDouble("CcaConsumeFnTotCntMonth");
				gate.ccaConsumeFnTotAmtDay = getDouble("CcaConsumeFnTotAmtDay");
				gate.ccaConsumeFnTotCntDay = getDouble("CcaConsumeFnTotCntDay");

				gate.ccaConsumeFcTotAmtMonth = getDouble("CcaConsumeFcTotAmtmonth");
				gate.ccaConsumeFcTotCntMonth = getDouble("CcaConsumeFcTotCntMonth");
				gate.ccaConsumeFcTotAmtDay = getDouble("CcaConsumeFcTotAmyDay");

				gate.ccaConsumeFcTotCntDay = getDouble("CcaConsumeFcTotCntDay");

				gate.ccaConsumeRejAuthCntDay = getDouble("CcaConsumeRejAuthCntDay");
				gate.ccaConsumeRejAuthCntMonth = getDouble("CcaConsumeRejAuthCntMonth");
				gate.ccaConsumeTrainTotAmtMonth = getDouble("CcaConsumeTrainTotAmtMonth");
				gate.ccaConsumeTrainTotAmtDay = getDouble("CcaConsumeTrainTotAmtDay");
				

			}
			gate.lastTxDate = getValue("CcaConsumeLastConsumeDate");

			gb.showLogMessage("D","交易成功次數="+getDouble("CcaConsumeTxTotCntDay"));
			gb.showLogMessage("D","交易失敗次數="+getDouble("CcaConsumeRejAuthCntDay"));

		}


		return true;
	}


	// CCA_CONSUME
	public boolean loadCcaConsume_Old(String spCardAcctIdx) throws Exception {
		//讀取卡戶帳務檔
		gb.showLogMessage("I","loadCcaConsume : started");
		/*
  	   if (1==1)
  		   return true;
		 */

		daoTable  = addTableOwner("CCA_CONSUME");
		selectSQL = "NVL(PRE_PAY_AMT, 0) as CcaConsumePrePayAmt, " //預付款金額(溢繳款)
				+ "NVL(BILL_LOW_LIMIT, 0) as CcaConsumeBillLowLimit, " //月平均消費額  => 授權用不到??!!
				+ "NVL(TOT_UNPAID_AMT,0) as CcaConsumeTotUnpaidAmt, " //Payment未消
				+ "NVL(PAID_PRECASH,0) as CcaConsumePaidPrecash, " //結帳-預現
				+ "NVL(UNPAID_PRECASH,0) as CcaConsumeUnpaidPrecash, " //未結帳-預現
				+ "NVL(PAID_ANNUAL_FEE,0)  , " //結帳-呆帳
				+ "NVL(PAID_SRV_FEE,0)  , " //結帳-費用
				+ "NVL(PAID_LAW_FEE,0)  , " //結帳-摧收款
				+ "NVL(PAID_PUNISH_FEE,0) , " //結帳-違約金
				+ "NVL(PAID_INTEREST_FEE,0), " //結帳-循環息
				+ "NVL(PAID_CONSUME_FEE,0) as CcaConsumePaidConsumeFee , " //結帳-消費
				+ "NVL(PAID_CYCLE,0)  , " //未結帳-違約金 ??
				+ "NVL(UNPAID_ANNUAL_FEE,0)   , " //未結帳-呆帳
				+ "NVL(UNPAID_SRV_FEE,0)  , " //未結帳-費用
				+ "NVL(UNPAID_LAW_FEE,0), " //未結帳-摧收款
				+ "NVL(UNPAID_CONSUME_FEE,0) as CcaConsumeUnpaidConsumeFee, " //未結帳-消費
				+ "NVL(M1_AMT,0)  , " //本期MP餘額
				+ "NVL(MAX_CONSUME_AMT,0)  , " //最近一年最高月消費額
				+ "NVL(YR_MAX_CONSUME_AMT,0) , " //本年最高消費金額
				+ "NVL(MAX_PRECASH_AMT,0), " //最近一年最高月預現額
				+ "NVL(YR_MAX_PRECASH_AMT,0), " //本年最高預借現金
				+ "NVL(IBM_RECEIVE_AMT,0) as CcaConsumeIbmReceiveAmt, " //預借現金指撥金額
				+ "NVL(UNPOST_INST_FEE,0) as CcaConsumeUnpostInstFee," //分期未結帳金額
				//down, Howard:以下欄位從CCA_CARD_ACCT搬過來
				//+ " tot_amt_consume as CcaConsumeTotAmtConsume , " //總授權額(已消未請) =>改為動態計算
				//+ " tot_amt_precash as CcaConsumeTotAmtPreCash , " //總預現額(已消未請) =>改為動態計算
				+ " tx_tot_amt_month as CcaConsumeTxTotAmtMonth, " //累積月消費額
				+ " tx_tot_cnt_month  as CcaConsumeTxTotCntMonth, " //累積月消費次數
				+ " tx_tot_amt_day as CcaConsumeTxTotAmtDay, " //累積日消費額
				+ " tx_tot_cnt_day as CcaConsumeTxTotCntDay, " //累積日消費次數
				+ " fn_tot_amt_month as CcaConsumeFnTotAmtMonth, " //國外一般消費月總額
				+ " fn_tot_cnt_month as CcaConsumeFnTotCntMonth, " //國外一般消費月總次
				+ " fn_tot_amt_day  as CcaConsumeFnTotAmtDay, " //國外一般消費日總額
				+ " fn_tot_cnt_day   as CcaConsumeFnTotCntDay, " //國外一般消費日總次
				+ " fc_tot_amt_month as  CcaConsumeFcTotAmtMonth, " //國外預借現金月總額
				+ " fc_tot_cnt_month as CcaConsumeFcTotCntMonth, " //國外預借現金月總次
				+ " fc_tot_amt_day  as  CcaConsumeFcTotAmtDay, " //國外預借現金日總額
				+ " fc_tot_cnt_day as  CcaConsumeFcTotCntDay, " //國外預借現金日總次
				+ " rej_auth_cnt_day, " //日拒絕授權次數
				+ " rej_auth_cnt_month, " //月拒絕授權次數
				+ " last_consume_date as CcaConsumeLastConsumeDate, " //最後消費日期
				+ " TRAIN_TOT_AMT_MONTH as CcaConsumeTrainTotalAmtMonth, " //高鐵累積月消費額
				+ " TRAIN_TOT_AMT_DAY as CcaConsumeTrainTotalAmtDay "; //高鐵累積日消費額
		//up, Howard:以上欄位從CCA_CARD_ACCT搬過來

		whereStr  = "WHERE CARD_ACCT_IDX = ? ";

		setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(spCardAcctIdx)));
		//setInt(1, Integer.parseInt(sP_CardAcctIdx));
		selectTable();

		gate.ccaConsumeBillLowLimit = getDouble("CcaConsumeBillLowLimit");
		return true;
	}

	public boolean loadCcaConsumeOfCompany(String spPSeqNo) throws Exception {
		//讀取卡戶帳務檔
		gb.showLogMessage("I","loadCcaConsume Of Company : started");
		/*
 	   if (1==1)
 		   return true;
		 */
		//daoTable  = "CCA_CONSUME_H";


		Connection  lDbConn= getDatabaseConnect();
		//

		ResultSet lRS = null;

		//down, get PaidConsumeFee  //結帳-消費
		String slSqlCmd = "select Nvl(sum(decode(acct_code,'CA',0, decode(sign(nvl(a.acct_month,'200407')-b.this_acct_month),1,0,end_bal))) ,0) as CcaConsumePaidConsumeFeeOfComp " + 
				"from act_debt a,ptr_workday b " + 
				"where acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') and p_seqno= ? " ; 


		PreparedStatement lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumePaidConsumeFeeOfComp = lRS.getInt("CcaConsumePaidConsumeFeeOfComp");

		}
		lRS.close();

		//up, get PaidConsumeFee  //結帳-消費

		//down, get UnpaidConsumeFee //未結帳-消費
		slSqlCmd = "select Nvl(sum(decode(acct_code,'CA',0, decode(sign(nvl(a.acct_month,'200407')-b.this_acct_month),1,end_bal,0))) ,0) as CcaConsumeUnpaidConsumeFeeOfComp " + 
				"from act_debt a,ptr_workday b " + 
				"where acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') " + 
				"and p_seqno= ? " ; 



		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumeUnPaidConsumeFeeOfComp = lRS.getInt("CcaConsumeUnpaidConsumeFeeOfComp");

		}
		lRS.close();
		//up, get UnpaidConsumeFee//未結帳-消費


		//down, get PaidPrecash //結帳-預現
		slSqlCmd = "select Nvl(sum(decode(acct_code,'CA', decode(sign(nvl(a.acct_month,'200407')-b.this_acct_month),1,0,end_bal),0)) ,0) as CcaConsumePaidPrecashOfComp " + 
				"from act_debt a,ptr_workday b " + 
				"where acct_code in (select acct_code from ptr_actcode where interest_method = 'Y') " + 
				"and p_seqno= ? " ; 


		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumePaidPrecashOfComp = lRS.getInt("CcaConsumePaidPrecashOfComp");

		}
		lRS.close();
		//up, get PaidPrecash 	   //結帳-預現


		//down, get UnpaidPrecash //未結帳-預現
		slSqlCmd = "select Nvl(sum(decode(acct_code,'CA', decode(sign(nvl(a.acct_month,'200407')-b.this_acct_month),1,end_bal,0),0)) ,0) as  CcaConsumeUnpaidPrecashOfComp " + 
				"from act_debt a,ptr_workday b " + 
				"where acct_code in (select acct_code from ptr_actcode " + 
				"where interest_method = 'Y') " + 
				"and p_seqno= ? " + 
				" " ; 


		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumeUnPaidPrecashOfComp = lRS.getInt("CcaConsumeUnpaidPrecashOfComp");

		}
		lRS.close();
		//up, get UnpaidPrecash //未結帳-預現	   




		//down, get IbmReceiveAmt //預借現金指撥金額
		//sL_SqlCmd = "select Nvl(combo_cash_limit,0) as CcaConsumeIbmReceiveAmtOfComp from act_acno where p_seqno= ? " ; 
		slSqlCmd = "select Nvl(combo_cash_limit,0) as CcaConsumeIbmReceiveAmtOfComp from act_acno where acno_p_seqno= ? " ;


		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumeIbmReceiveAmtOfComp = lRS.getInt("CcaConsumeIbmReceiveAmtOfComp");

		}
		lRS.close();
		//up, get IbmReceiveAmt//預借現金指撥金額 	   




		//down, get UnpostInstFee//分期未結帳金額 
		slSqlCmd = "SELECT Nvl(sum(nvl(a.unit_price,0)*(nvl(a.install_tot_term,0)- nvl(a.install_curr_term,0))+nvl(a.remd_amt,0)+ " + 
				"decode(install_curr_term,0,first_remd_amt,0)) ,0) as CcaConsumeUnpostInstFeeOfComp " + 
				"FROM bil_contract a,bil_merchant b " + 
				//"WHERE a.gp_no = ? AND a.install_tot_term != a.install_curr_term  " + 
				"WHERE a.p_seqno = ? AND a.install_tot_term != a.install_curr_term  " +
				"AND (a.post_cycle_dd > 0 or a.installment_kind = 'F') " + 
				"AND b.mcht_no(+) = a.mcht_no AND (b.loan_flag in ('N','C') or b.loan_flag is null) " ; 



		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();

		while (lRS.next()) {
			gate.ccaConsumeUnPostInstFeeOfComp = lRS.getInt("CcaConsumeUnpostInstFeeOfComp");

		}
		lRS.close();
		//up, get UnpostInstFee//分期未結帳金額 	   

		lPs.close();

		//down, 處理溢繳款-公司
		/*
		 *select end_bal_op+end_bal_lk from act_acct where gp_no =公司gp_no 再加上
select end_bal_op+end_bal_lk from newtable where acno_p_seqno in (select acno_p_seqno from act_acno where gp_no = 公司gp_no)

		 * */
		//up, 處理溢繳款-公司


		int nlValue1=0, nlValue2=0;
		//down, get TotUnpaidAmt //Payment未消 (已付款未銷帳)
		slSqlCmd = "select Nvl(sum(pay_amt),0) as V1 from act_pay_detail where p_seqno = ? " ;

		lPs =getDatabaseConnect().prepareStatement(slSqlCmd);

		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();


		while (lRS.next()) {
			nlValue1 = lRS.getInt("V1");

		}
		lRS.close();

		slSqlCmd = "select Nvl(sum(pay_amt),0) as V2 from act_debt_cancel where nvl(process_flag,'N') != 'Y' and p_seqno = ? " ;

		lPs = getDatabaseConnect().prepareStatement(slSqlCmd);

		lPs.setString(1, spPSeqNo);

		lRS = lPs.executeQuery();


		while (lRS.next()) {
			nlValue2 = lRS.getInt("V2");

		}
		lRS.close();

		gate.ccaConsumeTotUnPaidAmtOfComp = nlValue1 +nlValue2; 
		//up, get TotUnpaidAmt //Payment未消




		/* 
 	  daoTable  = "CCA_CONSUME";
       selectSQL = "NVL(PRE_PAY_AMT, 0) as CcaConsumePrePayAmtOfComp, " //預付款金額(溢繳款)
    		   		+ "NVL(BILL_LOW_LIMIT, 0) as CcaConsumeBillLowLimitOfComp, " //月平均消費額
    		   		+ "NVL(TOT_UNPAID_AMT,0) as CcaConsumeTotUnpaidAmtOfComp, " //Payment未消
    		   		+ "NVL(PAID_PRECASH,0) as CcaConsumePaidPrecashOfComp, " //結帳-預現
    		   		+ "NVL(UNPAID_PRECASH,0) as CcaConsumeUnpaidPrecashOfComp, " //未結帳-預現
    		   		+ "NVL(PAID_ANNUAL_FEE,0) as CcaConsumePaidAnnualFeeOfComp  , " //結帳-呆帳
    		   		+ "NVL(PAID_SRV_FEE,0) as CcaConsumePaidSrvFeeOfComp  , " //結帳-費用
    		   		+ "NVL(PAID_LAW_FEE,0) as CcaConsumePaidLawFeeOfComp  , " //結帳-摧收款
    		   		+ "NVL(PAID_PUNISH_FEE,0)  as CcaConsumePaidPunishFeeOfComp, " //結帳-違約金
    		   		+ "NVL(PAID_INTEREST_FEE,0) as CcaConsumePaidInterestFeeOfComp, " //結帳-循環息
    		   		+ "NVL(PAID_CONSUME_FEE,0) as CcaConsumePaidConsumeFeeOfComp , " //結帳-消費
    		   		+ "NVL(PAID_CYCLE,0)   as CcaConsumePaidCycleOfComp, " //未結帳-違約金 ??
    		   		+ "NVL(UNPAID_ANNUAL_FEE,0)   as CcaConsumeUnPiadAnnualFeeOfComp , " //未結帳-呆帳
    		   		+ "NVL(UNPAID_SRV_FEE,0)  as CcaConsumeUnPiadSrvFeeOfComp , " //未結帳-費用
    		   		+ "NVL(UNPAID_LAW_FEE,0) as CcaConsumeUnPiadLawFeeOfComp, " //未結帳-摧收款
    		   		+ "NVL(UNPAID_CONSUME_FEE,0) as CcaConsumeUnpaidConsumeFeeOfComp, " //未結帳-消費
    		   		+ "NVL(M1_AMT,0) as CcaConsumeM1AmtOfComp  , " //本期MP餘額
    		   		+ "NVL(MAX_CONSUME_AMT,0)  as CcaConsumeMaxConsumeAmtOfComp, " //最近一年最高月消費額
    		   		+ "NVL(YR_MAX_CONSUME_AMT,0)  as CcaConsumeYrMaxConsumeAmtOfComp, " //本年最高消費金額
    		   		+ "NVL(MAX_PRECASH_AMT,0) as CcaConsumeMaxPreCashAmtOfComp, " //最近一年最高月預現額
    		   		+ "NVL(YR_MAX_PRECASH_AMT,0) as CcaConsumeYrMaxPreCashAmtOfComp, " //本年最高預借現金
    		   		+ "NVL(IBM_RECEIVE_AMT,0) as CcaConsumeIbmReceiveAmtOfComp, " //預借現金指撥金額
    		   		+ "NVL(UNPOST_INST_FEE,0) as CcaConsumeUnpostInstFeeOfComp "; //分期未結帳金額


       whereStr  = "WHERE GP_NO = ? ";


       setString(1, sP_GpNo);
       selectTable();

       gate.ccaConsumeBillLowLimitOfComp = getDouble("CcaConsumeBillLowLimitOfComp");

		 */ 
		gb.showLogMessage("I","loadCcaConsume Of Company : completed");


		return true;
	}



	/*
    //ACT_CORP_GP by GP_NO
  	public boolean loadActCorpGpByGpNo(String sP_GpNo) throws Exception {
 	   gb.showLogMessage("D","loadActCorpGpByGpNo : ","started");
	   daoTable  = "ACT_CORP_GP";  
       selectSQL = "NVL(TOT_AMT_CONSUME,0) as ActCorpGpTotAmtConsume,"//總授權額(已消未請)/
    		   		+ "NVL(TOT_AMT_PRECASH,0) as ActCorpGpTotAmtPrecash, "//總預現額(已消未請)/
    		   		+ "NVL(TOT_AMT_MONTH,0) as ActCorpGpTotAmtMonth, "//臨條放大總月限%/
    		   		+ "NVL(ADJ_INST_PCT,0) as ActCorpGpAdjInstPct, "//臨條分期放大總月限%/
    		   		+ "NVL(LMT_TOT_CONSUME,0) as ActCorpGpLmtTotConsume, "//總授權額度(卡戶)/
    		   		+ "NVL(ADJ_AREA,' ') as ActCorpGpAdjArea, "			
    		   		+ "NVL(ADJ_QUOTA,'N') as ActCorpGpAdjQuota, "//臨時調高額度註記/
    		   		+ "NVL(ADJ_EFF_START_DATE,'00000000') as ActCorpGpAdjEffStartDate,"
    		   		+ "NVL(ADJ_EFF_END_DATE,'00000000') as ActCorpGpAdjEffEndDate, "
    		   		+ "ORGAN_ID as ActCorpGpOrganId, " //機關代號 /
    		   		+ "NVL(LMT_TOT_CASH,0) as ActCorpGpLmtTotCash";//預借現金最大額度 /


       whereStr  = "WHERE GP_NO = ? ";


       setString(1, sP_GpNo);
       selectTable();

 	   return true;
  	}
	 */
	/*
  	   // Ū DBA_ACNO
   public boolean selectDbaAcno() throws Exception {

        daoTable  = "DBA_ACNO";
        selectSQL = "ACCT_NO,"                       
                  + "ID_P_SEQNO,"                    
                  + "ACCT_STATUS,"                   
                  + "STATUS_CHANGE_DATE,"            
                  + "STATUS_CHECK_DATE,"             
                  + "NO_DEDUCT_DATE,"                
                  + "DAILY_AMT,"                     
                  + "MONTH_AMT,"                     
                  + "SMS_AMT AS ACNO_SMS_AMT,"       
                  + "SMS_MIN_AMT,"                   
                  + "ADJ_AMT_MM,"                    
                  + "NVL(ADJ_AMT_DATE1,'999999999'),"
                  + "NVL(ADJ_AMT_DATE2,'999999999')";
        whereStr  = "WHERE P_SEQNO = ? ";
        setString(1,getValue("P_SEQNO"));
        selectTable();
        if ( "Y".equals(notFound) )
           { return false; }

        return true;
    }

    // Ū DBA_ACNO_SUP
    public boolean selectDbaAcnoSup() throws Exception {

        daoTable  = "DBA_ACNO_SUP";
        selectSQL = "LOC_AMT_DD,"                    
                  + "LOC_AMT_MM,"                    
                  + "ADJ_AMT_DD AS SUP_ADJ_AMT_DD,"  
                  + "ADJ_AMT_MM AS SUP_ADJ_AMT_MM,"  
                  + "NVL(ADJ_AMT_DATE1,'999999999') AS SUP_ADJ_AMT_DATE1,"  
                  + "NVL(ADJ_AMT_DATE2,'999999999') AS SUP_ADJ_AMT_DATE2,"  
                  + "ADJ_LOC_FLAG ";                 
        whereStr  = "WHERE P_SEQNO = ? ";
        setString(1,getValue("P_SEQNO"));
        selectTable();
        if ( "Y".equals(notFound) )
           { return false; }

        return true;
    }


	 */
	public String getIdPSeqNo() throws Exception {

		return getValue("CardBaseIdPSeqNo");

	}


	public String getAcnoPSeqNo() throws Exception {

		return getValue("CardBaseAcnoPSeqNo");

	}


	public String getPSeqNo() throws Exception {
		return getValue("CardBasePSeqNo");

	}
	/**
	 * 取得公司卡CORP_P_SEQNO
	 * V1.00.38 P3授權額度查核調整
	 * @throws Exception if any exception occurred
	 */
	public String getCorpPSeqNo() throws Exception {
		return getValue("CardBaseCorpPSeqNo");

	}
	//DBA_ACNO
	public boolean loadDbaAcno() throws Exception {


		gb.showLogMessage("I","loadDbaAcno : started");
		daoTable  = addTableOwner("DBA_ACNO"); //credit card 帳戶基本資料主檔
		selectSQL = "class_code as DbaAcnoClassCode, " //(風險等級[帳戶卡人等級])
				+ "ACCT_TYPE as DbaAcnoAcctType," //帳戶帳號類別                   */ -- 01  一般卡02  商務卡03  VISA採購卡05  歡喜卡06  AE卡一般頂/頂級VISA
				+ "CORP_P_SEQNO as DbaAcnoCorpPSeqno," //統一編號流水號碼
				+ "corp_act_flag as DbaAcnoCorpActFlag, " ///* 帳戶繳款類別   */ Y:總繳, N:個繳
				+ "acct_status as DbaAcnoAcctStatus, "//帳戶往來狀態 => 1:正常 2:逾放 3.催收 4.呆帳 5.結清(Write Off)
				+ "NVL(line_of_credit_amt,0) as DbaAcnoLineOfCreditAmt    , " //(總授權額度[卡戶], 單位:仟元)/帳戶循環信用額度 => Howard(20190705): JH 說debit card 額度不是看這裡 
				+ "ACCT_KEY "; //帳戶查詢碼
		//+ "INT_RATE_MCODE as DbaAcnoIntRateMCode "; //計算利率時MCODE   		   



		whereStr  = "WHERE P_SEQNO = ? ";


		setString(1, getPSeqNo());

		selectTable();

		boolean blResult = true;

		if ( "Y".equals(notFound) ){
			blResult = false;

		}




		return blResult;

	}

	public boolean loadDbaAcnoOfCompany(String spAcctType, String spCorpPSeqno) throws Exception {

		gb.showLogMessage("I","loadDbaAcno : started");
		daoTable  = addTableOwner("DBA_ACNO"); //credit card 帳戶基本資料主檔
		selectSQL = "class_code as DbaAcnoClassCodeOfComp, " //(風險等級[帳戶卡人等級])
				+ "corp_act_flag as DbaAcnoCorpActFlagOfComp, " ///* 帳戶繳款類別   */ Y:總繳, N:個繳
				+ "acct_status as DbaAcnoAcctStatusOfComp, "//帳戶往來狀態 => 1:正常 2:逾放 3.催收 4.呆帳 5.結清(Write Off)
				+ "NVL(line_of_credit_amt,0) as DbaAcnoLineOfCreditAmtOfComp  "; //(總授權額度[卡戶], 單位:仟元)/帳戶循環信用額度  //=> Howard(20190705): JH 說debit card 額度不是看這裡 

		//+ "INT_RATE_MCODE as DbaAcnoIntRateMCodeOfComp "; //計算利率時MCODE   		   




		whereStr  = "WHERE acct_type = ? and corp_p_seqno=? and corp_act_flag=? ";  


		setString(1, spAcctType);
		setString(2, spCorpPSeqno);
		//setString(3, "2"); //2=>總繳公司
		setString(3, "Y"); //Y=>總繳公司



		selectTable();

		boolean blResult = true;

		if ( "Y".equals(notFound) ){
			blResult = false;

		}



		return blResult;

	}
	//kevin:新增第一階段CARD LINK提供之相關授權金額
	public boolean loadActAcnoOfCompany(String spAcctType, String spCorpPSeqno, String spPSeqno) throws Exception {
		/*
  		當 corp_act_flag 欄位值是 2 時，表示此筆資料是公司資料；否則則為個人的資料
		 */
		gb.showLogMessage("I","loadActAcno : started");
		daoTable  = addTableOwner("ACT_ACNO"); //信用卡 帳戶基本資料主檔
		selectSQL = "class_code as ActAcnoClassCodeOfComp, " //(風險等級[帳戶卡人等級])
				+ "corp_act_flag as ActAcnoCorpActFlagOfComp, " ///* 帳戶繳款類別   */ -- Y:總繳 N:個繳
				+ "acct_status as ActAcNoAcctStatusOfComp, "//帳戶往來狀態 => 1:正常 2:逾放 3.催收 4.呆帳 5.結清(Write Off)
				+ "ADJ_BEFORE_LOC_AMT as ActAcnoAdjBeforeLocAmtOfComp," //帳戶調整前總月限額         	=>原始額度
				+ "NVL(line_of_credit_amt,0) as ActAcnoLineOfCreditAmtOfComp    , " //(總授權額度[卡戶],/帳戶循環信用額度 => 調整後的額度 
				+ "line_of_credit_amt_cash as ActAcnoLineOfCreditAmtCashOfComp, " //(預借現金最大額度) => 預借現金沒有原始額度  
				+ "INT_RATE_MCODE as ActAcnoIntRateMCodeOfComp, " //計算利率時MCODE
				+ "combo_acct_no as ActAcnoComboAcctNoOfComp, " //COMBO金融卡帳號
				+ "curr_pd_rating as ActAcnoCurrPdRatingOfComp, " //違約預測評等
				+ "auto_installment as ActAcnoAutoInstallmentOfComp," //自動分期付款
				+ "INST_AUTH_LOC_AMT as ActAcnoInstAuthLocAmtOfComp," //帳戶分期付款授權額度
				+ "H_ADJ_LOC_HIGH_DATE as ActAcnoHAdjLocHighDateOfComp," //人工調高信用額度之日期  		=> 有值表示有調高
				+ "H_ADJ_LOC_LOW_DATE as ActAcnoHAdjlocLowDateOfComp,"  //人工調低信用額度之日期		=> 有值表示有調低
				+ "S_ADJ_LOC_HIGH_DATE as ActAcnoSAdjLocHighDateOfComp," //系統調高信用額度之日期    => 新系統不會用到此欄位
				+ "S_ADJ_LOC_LOW_DATE as ActAcnoSAdjLocLowDateOfComp," //系統調低信用額度之日期	=> 新系統不會用到此欄位
				+ "ADJ_LOC_HIGH_T as ActAcnoAdjLocHighTOfComp," //最近調高額度之說明             VALUE:A,B,C,D
				+ "ADJ_LOC_LOW_T as ActAcnoAdjLocLowTOfComp," //最近調低額度之說明             VALUE:A,B,C,D
				+ "AUTH_BILLED_BAL as ActAcnoAuthBilledBal," //帳戶已通知未繳款餘額
				+ "AUTH_UNBILL_BAL as ActAcnoAuthUnbillBal," //帳戶未通知付款餘額 
				+ "AUTH_NOT_DEPOSIT as ActAcnoAuthNotDeposit," //帳戶已授權未請款餘額
				+ "AUTH_CASH_BILLED_BAL as ActAcnoAuthCashBilledBal," //帳戶已通知未繳款預借現金餘額
				+ "AUTH_CASH_UNBILL_BAL as ActAcnoAuthCashUnbillBal," //帳戶未通知付款預借現金餘額
				+ "AUTH_CASH_NOT_DEPOSIT as ActAcnoAuthCashNotDeposit"; //帳戶已授權未請款預借現金餘額

		//kevin:商務卡都會有公司資料，不須再分總繳個繳
//		whereStr  = "WHERE acct_type = ? and corp_p_seqno=? and corp_act_flag=? and P_SEQNO= ? ";  
		whereStr  = "WHERE acct_type = ? and corp_p_seqno=? and acno_flag= ? ";  


		setString(1, spAcctType);
		setString(2, spCorpPSeqno);
		//setString(3, "2"); //2=>總繳公司
//		setString(3, "Y"); //Y=>總繳公司
//		setString(4, sP_PSeqno); 
		setString(3, "2"); 


		selectTable();

		boolean blResult = true;

		if ( "Y".equals(notFound) ){
			blResult = false;

		}
		else {
			//gate.baseLimitOfComp = getDouble("ActAcnoLineOfCreditAmtOfComp"); /*該戶之基本額度*/
			gate.cashBaseOfComp = getDouble("ActAcnoLineOfCreditAmtCashOfComp"); /*該戶之預借現金額度*/
		}
		return blResult;
	}

	// ACT_ACNO
	//kevin:新增第一階段CARD LINK提供之相關授權金額
	public boolean loadActAcno() throws Exception {
		gb.showLogMessage("I","loadActAcno : started");
		daoTable  = addTableOwner("ACT_ACNO"); //信用卡 帳戶基本資料主檔
		selectSQL = "class_code as ActAcnoClassCode, " //(風險等級[帳戶卡人等級])
				+ "ACCT_TYPE as ActAcnoAcctType," //帳戶帳號類別                   */ -- 01  一般卡02  商務卡03  VISA採購卡05  歡喜卡06  AE卡一般頂/頂級VISA
				+ "CORP_P_SEQNO as ActAcnoCorpPSeqno," //統一編號流水號碼
				+ "corp_act_flag as ActAcnoCorpActFlag, " ///* 帳戶繳款類別   */ --Y:總繳, N:個繳
				+ "acct_status as ActAcNoAcctStatus, "//帳戶往來狀態 => 1:正常 2:逾放 3.催收 4.呆帳 5.結清(Write Off)
				+ "ADJ_BEFORE_LOC_AMT as ActAcnoAdjBeforeLocAmt," //帳戶調整前總月限額		=>原始額度    		   
				+ "NVL(line_of_credit_amt,0) as ActAcnoLineOfCreditAmt    , " //(總授權額度[卡戶],帳戶循環信用額度    =>調整後的額度 
				+ "line_of_credit_amt_cash as ActAcnoLineOfCreditAmtCash, " //(預借現金最大額度)  => 預借現金沒有原始額度
				+ "INT_RATE_MCODE as ActAcnoIntRateMCode, " //計算利率時MCODE
				+ "combo_acct_no as ActAcnoComboAcctNo, " //COMBO金融卡帳號
				+ "curr_pd_rating as ActAcnoCurrPdRating, " //違約預測評等
				+ "auto_installment as ActAcnoAutoInstallment," //自動分期付款
				+ "INST_AUTH_LOC_AMT as ActAcnoInstAuthLocAmt," //帳戶分期付款授權額度
				+ "H_ADJ_LOC_HIGH_DATE as ActAcnoHAdjLocHighDate," //人工調高信用額度之日期	=> 有值表示有調高
				+ "H_ADJ_LOC_LOW_DATE as ActAcnoHAdjlocLowDate,"  //人工調低信用額度之日期		=> 有值表示有調低
				+ "S_ADJ_LOC_HIGH_DATE as ActAcnoSAdjLocHighDate," //系統調高信用額度之日期  => 新系統不會用到此欄位
				+ "S_ADJ_LOC_LOW_DATE as ActAcnoSAdjLocLowDate," //系統調低信用額度之日期	=> 新系統不會用到此欄位
				+ "ADJ_LOC_HIGH_T as ActAcnoAdjLocHighT," //最近調高額度之說明             VALUE:A,B,C,D
				+ "ADJ_LOC_LOW_T as ActAcnoAdjLocLowT," //最近調低額度之說明             VALUE:A,B,C,D
				+ "AUTH_BILLED_BAL as ActAcnoAuthBilledBal," //帳戶已通知未繳款餘額
				+ "AUTH_UNBILL_BAL as ActAcnoAuthUnbillBal," //帳戶未通知付款餘額 
				+ "AUTH_NOT_DEPOSIT as ActAcnoAuthNotDeposit," //帳戶已授權未請款餘額
				+ "AUTH_CASH_BILLED_BAL as ActAcnoAuthCashBilledBal," //帳戶已通知未繳款預借現金餘額
				+ "AUTH_CASH_UNBILL_BAL as ActAcnoAuthCashUnbillBal," //帳戶未通知付款預借現金餘額
				+ "AUTH_CASH_NOT_DEPOSIT as ActAcnoAuthCashNotDeposit"; //帳戶已授權未請款預借現金餘額



		//whereStr  = "WHERE P_SEQNO = ? ";
		whereStr  = "WHERE ACNO_P_SEQNO = ? ";

		String slAcnoPSeqNo = getAcnoPSeqNo();
		setString(1, slAcnoPSeqNo);
		//setString(1, "001234"); //for test
		selectTable();

		boolean blResult = true;
		if ( "Y".equals(notFound) ){
			blResult = false;
		}
		else {
			//gate.baseLimit = getDouble("ActAcnoLineOfCreditAmt"); /*該戶之基本額度*/
			gate.cashBase = getDouble("ActAcnoLineOfCreditAmtCash"); /*該戶之預借現金額度*/
		}

		//gb.showLogMessage("D","傳入AcnoPSEQNO=>" + getAcnoPSeqNo());
		//gb.showLogMessage("D","ActAcnoClassCode[風險等級/帳戶卡人等級]=>" + getValue("ActAcnoClassCode"));
		//gb.showLogMessage("D","ActAcnoAdjBeforeLocAmt[帳戶調整前總月限額/原始額度 ]=>" + getValue("ActAcnoAdjBeforeLocAmt"));
		//gb.showLogMessage("D","ActAcnoLineOfCreditAmt[帳戶循環信用額度/調整後的額度]=>" + getValue("ActAcnoLineOfCreditAmt"));
		//gb.showLogMessage("D","ActAcnoInstAuthLocAmt[帳戶分期付款授權額度]=>" + getValue("ActAcnoInstAuthLocAmt"));
		//gb.showLogMessage("D","ActAcnoAdjLocHighT[最近調高額度之說明]=>" + getValue("ActAcnoAdjLocHighT"));
		//gb.showLogMessage("D","ActAcnoAdjLocLowT[最近調低額度之說明]=>" + getValue("ActAcnoAdjLocLowT"));

		return blResult;
	}

	///*讀取卡戶風險分類授權交易統計*/ 

	public boolean loadRiskTradeInfo() throws Exception {
		gb.showLogMessage("I","loadRiskTradeInfo : started");

		daoTable  = addTableOwner("CCA_RISK_T_AMT"); //oracle table is RSK_T_AMT
		selectSQL = "NVL(LAST_CONSUME_DATE,'00000000') as RiskTradeLastConsumeDate," //最後交易日期
				+ "NVL(T_AMT_MONTH,0) as RiskTradeMonthAmt," //本月累積交易金額
				+ "NVL(T_CNT_MONTH,0) as RiskTradeMonthCnt," //本月累積交易筆數
				+ "NVL(T_AMT_DAY,0) as RiskTradeDayAmt, " //本日累積交易金額
				+ "NVL(T_CNT_DAY,0) as RiskTradeDayCnt";  //本日累積交易筆數

		whereStr  = "WHERE RISK_TYPE = ? and CARD_ACCT_IDX=?";

		gb.showLogMessage("D","gate.mccRiskType=>" + gate.mccRiskType + "--");
		gb.showLogMessage("D","gate.CardAcctIdx=>" + gate.cardAcctIdx + "--");

		setString(1, gate.mccRiskType);
		setString(2, gate.cardAcctIdx);
		selectTable();




		if ( "Y".equals(notFound) ){ 
			//insertRiskTradeInfo(); //Howard:改到全部都處理完成後，才insert
			gate.ifHaveRiskTradeInfo = false;
			gate.riskTradeMonthAmt = 0;
			gate.riskTradeMonthCnt = 0;

			gate.riskTradeDayAmt = 0;
			gate.riskTradeDayCnt = 0;
		}
		else {
			gate.ifHaveRiskTradeInfo = true;

			String slRiskTradeLastConsumeDate = getValue("RiskTradeLastConsumeDate");

			gb.showLogMessage("D","sL_RiskTradeLastConsumeDate=>" + slRiskTradeLastConsumeDate + "--");

			//down, 判定是否有同一天的資料
			if (gb.getSysDate().equals(slRiskTradeLastConsumeDate)) {
				gate.riskTradeDayAmt = getInteger("RiskTradeDayAmt");
				gate.riskTradeDayCnt = getInteger("RiskTradeDayCnt");       				
			}
			else {
				gate.riskTradeDayAmt = 0;
				gate.riskTradeDayCnt = 0;       

			}
			//up, 判定是否有同一天的資料

			//down, 判定是否有同一月的資料
			if (gb.getSysDate().substring(0,6).equals(slRiskTradeLastConsumeDate.substring(0,6))) {
				gate.riskTradeMonthAmt = getInteger("RiskTradeMonthAmt");
				gate.riskTradeMonthCnt = getInteger("RiskTradeMonthCnt");

			}
			else {
				gate.riskTradeMonthAmt = 0;
				gate.riskTradeMonthCnt = 0;       

			}
			//up, 判定是否有同一月的資料


			//gb.showLogMessage("D","傳入RiskType=>" + gate.mccRiskType);
			//gb.showLogMessage("D","傳入CardAcctIdx=>" + gate.CardAcctIdx);
			//gb.showLogMessage("D","riskTradeMonthAmt[本月累積交易金額]=>" + gate.riskTradeMonthAmt);
			//gb.showLogMessage("D","riskTradeMonthCnt[本月累積交易筆數]=>" + gate.riskTradeMonthCnt);
			//gb.showLogMessage("D","riskTradeDayAmt[本日累積交易金額]=>" + gate.riskTradeDayAmt);
			//gb.showLogMessage("D","riskTradeDayCnt[本日累積交易筆數]=>" + gate.riskTradeDayCnt);

		}
		return true;

	}

	//*更新卡戶當月之消費總金額*/
	public boolean updateRiskTradeInfo() throws Exception {
		gb.showLogMessage("I","updateRiskTradeInfo(): started");
		//proc is TB_rsk_t_amt(2..)
		boolean blResult=true;

		//kevin:取消service4Manual設定，改為單筆connType 決定
//		if (gb.service4Manual)
		//kevin:人工授權也要累計
//		if ("WEB".equals(gate.connType)) 
//			return true;/*人工授權, 不用新增 (處理)*/
		

		double dlMonthTradeAmt=0, dlMonthTradeCnt=0,dlDayTradeAmt=0, dlDayTradeCnt=0;

//		if ( (gate.normalPurch) || (gate.cashAdvance) || (gate.mailOrder) || (gate.preAuth) ) {
//			if (!gate.forcePosting) {
//
//				dlMonthTradeAmt= gate.riskTradeMonthAmt + gate.ntAmt;  //本月累積交易金額
//
//				dlMonthTradeCnt= gate.riskTradeMonthCnt + 1; //本月累積交易筆數 
//				dlDayTradeAmt=gate.riskTradeDayAmt + gate.ntAmt; //本日累積交易金額
//				dlDayTradeCnt= gate.riskTradeDayCnt  + 1; //本日累積交易筆數
//
//
//
//				/*
//        	  RiskTAmt_T_AMT_MONTH += IsoRec.tx_nt_amt;
//                          RiskTAmt_T_CNT_MONTH++;
//                          RiskTAmt_T_AMT_DAY += IsoRec.tx_nt_amt;
//                          RiskTAmt_T_CNT_DAY++;
//				 * */	
//
//			}
//		}
//		else if ( (gate.refund) || (gate.reversalTrans) || (gate.purchAdjust) || (gate.cashAdjust) ) {
		if ( (gate.refund) || (gate.reversalTrans) || (gate.purchAdjust) || (gate.cashAdjust) ) {

			if (isEqualSysDate(getValue("AuthLogPriorTxDate"))) { //原始交易與系統日期相等
				dlDayTradeAmt = gate.riskTradeDayAmt + gate.balanceAmt;
				//RiskTAmt_T_AMT_DAY += ( IsoRec.repl_nt_amt - IsoRec.tx_nt_amt);
				if (dlDayTradeAmt<0)
					dlDayTradeAmt = 0;
				if (gate.adjustAmount==0) {
					dlDayTradeCnt = gate.riskTradeDayCnt - 1;
					if (dlDayTradeCnt<0) {
						dlDayTradeCnt=0;
					}
				}

			}
			dlMonthTradeAmt = gate.riskTradeMonthAmt + gate.balanceAmt;  //本月累積交易金額
			//proc => RiskTAmt_T_AMT_MONTH += (IsoRec.repl_nt_amt-IsoRec.tx_nt_amt);
			if (dlMonthTradeAmt<0)
				dlMonthTradeAmt = 0;
			if (gate.adjustAmount==0) {
				dlMonthTradeCnt = gate.riskTradeMonthCnt - 1;
				if (dlMonthTradeCnt<0) {
					dlMonthTradeCnt=0;
				}
			}




			/*
        	                           RiskTAmt_T_AMT_DAY += ( IsoRec.repl_nt_amt - IsoRec.tx_nt_amt);
                          if (RiskTAmt_T_AMT_DAY < 0)
                            RiskTAmt_T_AMT_DAY = 0;
                          if (IsoRec.repl_nt_amt == 0){
                            RiskTAmt_T_CNT_DAY--;
                            if (RiskTAmt_T_CNT_DAY < 0)
                                RiskTAmt_T_CNT_DAY = 0;
                          }

			 */

		}
		else if (gate.refundAdjust) {
			if (isEqualSysDate(getValue("AuthLogPriorTxDate"))) { //原始交易與系統日期相等
				dlDayTradeAmt = gate.riskTradeDayAmt + gate.balanceAmt;
				//proc => RiskTAmt_T_AMT_DAY += ( IsoRec.tx_nt_amt - IsoRec.repl_nt_amt);
				if (dlDayTradeAmt<0)
					dlDayTradeAmt = 0;
				if (gate.adjustAmount==0) {
					dlDayTradeCnt = gate.riskTradeDayCnt - 1;
					if (dlDayTradeCnt<0) {
						dlDayTradeCnt=0;
					}
				}
			}
			dlMonthTradeAmt = gate.riskTradeMonthAmt + gate.balanceAmt;  //本月累積交易金額
			//proc => RiskTAmt_T_AMT_MONTH += (IsoRec.tx_nt_amt - IsoRec.repl_nt_amt);
			if (dlMonthTradeAmt<0)
				dlMonthTradeAmt = 0;
			if (gate.adjustAmount==0) {
				dlMonthTradeCnt = gate.riskTradeMonthCnt - 1;
				if (dlMonthTradeCnt<0) {
					dlMonthTradeCnt=0;
				}
			}
		}
		else if ( (gate.normalPurch) || (gate.cashAdvance) || (gate.mailOrder) || (gate.preAuth) ) {
			if (!gate.forcePosting) {

				dlMonthTradeAmt= gate.riskTradeMonthAmt + gate.ntAmt;  //本月累積交易金額

				dlMonthTradeCnt= gate.riskTradeMonthCnt + 1; //本月累積交易筆數 
				dlDayTradeAmt=gate.riskTradeDayAmt + gate.ntAmt; //本日累積交易金額
				dlDayTradeCnt= gate.riskTradeDayCnt  + 1; //本日累積交易筆數



				/*
        	  RiskTAmt_T_AMT_MONTH += IsoRec.tx_nt_amt;
                          RiskTAmt_T_CNT_MONTH++;
                          RiskTAmt_T_AMT_DAY += IsoRec.tx_nt_amt;
                          RiskTAmt_T_CNT_DAY++;
				 * */	

			}
		}
		if (gate.ifHaveRiskTradeInfo) {
			daoTable = addTableOwner("CCA_RISK_T_AMT");//oracle table is RSK_T_AMT
			updateSQL = "T_AMT_MONTH = ?, T_CNT_MONTH=?, T_AMT_DAY=?, T_CNT_DAY=?, LAST_CONSUME_DATE=? ";
			whereStr  = "WHERE RISK_TYPE = ? and CARD_ACCT_IDX=?";

			setDouble(1, dlMonthTradeAmt);
			setDouble(2, dlMonthTradeCnt);
			setDouble(3, dlDayTradeAmt);
			setDouble(4, dlDayTradeCnt);
			setString(5, gate.txDate);

			setString(6, gate.mccRiskType);
			setString(7, gate.cardAcctIdx);
			updateTable();

		}
		else {
			insertRiskTradeInfo(dlMonthTradeAmt, dlMonthTradeCnt,dlDayTradeAmt, dlDayTradeCnt);
		}
		/*
         	   daoTable  = "CCS_AUTH_TXLOG";
	   updateSQL = "BANK_TX_SEQNO = ?, UNLOCK_FLAG=? ";
	   whereStr  = "WHERE AUTH_SEQNO = ? ";

       setString(1, gate.bank_tx_seqno);
       setString(2, "N");
       setString(3, getValue("PriorOfPrior_AUTH_SEQNO"));

       int  cnt = updateTable();

		 */
		/*
        setValue("risk_type",gate.mccRiskType);
        setValue("card_acct_idx",gate.CardAcctIdx);
        setValue("last_consume_date",gb.sysDate); 
        setValue("MONTH_TRADE_AMT","0");  //* 本月累積交易金額
        setValue("MONTH_TRADE_CNT","0"); //* 本月累積交易筆數  
        setValue("DAY_TRADE_AMT","0"); //* 本日累積交易金額      
        setValue("DAY_TRADE_CNT","0"); //* 本日累積交易筆數      
        setValue("CRT_DATE",gb.sysDate);
        setValue("crt_user","AUTH");
        setValue("MOD_USER","AUTH");

        setValue("MOD_TIME",gb.sysDate); //=>oO timestamp , np assign value ?
        setValue("mod_pgm","AUTH");

        insertTable();
		 */


		return blResult;
	}

	private boolean isEqualSysDate(String spTargetDateStr)  throws Exception {
		boolean blResult = false;
		String slCurDate = HpeUtil.getCurDateStr(false);
		if (spTargetDateStr.equals(slCurDate))
			blResult = true;
		return blResult;

	}
	//*新增卡戶當月之消費總金額*/
	public boolean insertRiskTradeInfo(double dpMonthTradeAmt, double dpMonthTradeCnt, double dpDayTradeAmt, double dpDayTradeCnt) throws Exception {
		gb.showLogMessage("I","insertRiskTradeInfo(): started");
		boolean blResult=true;
		daoTable = addTableOwner("CCA_RISK_T_AMT");//oracle table is RSK_T_AMT
		setValue("RISK_TYPE",gate.mccRiskType);

		//setValue("card_acct_idx",gate.CardAcctIdx);
		setValueDouble("CARD_ACCT_IDX", Double.parseDouble(gate.cardAcctIdx));  /* 卡戶INDEX      */

		setValue("LAST_CONSUME_DATE",gb.getSysDate());


		setValueDouble("T_AMT_MONTH",dpMonthTradeAmt);  /* 本月累積交易金額      */
		setValueDouble("T_CNT_MONTH",dpMonthTradeCnt); /* 本月累積交易筆數      */
		setValueDouble("T_AMT_DAY",dpDayTradeAmt); /* 本日累積交易金額      */
		setValueDouble("T_CNT_DAY",dpDayTradeCnt); /* 本日累積交易筆數      */


		//setValue("T_AMT_MONTH",Double.toString(dP_MonthTradeAmt));  /* 本月累積交易金額      */
		//setValue("T_CNT_MONTH",Double.toString(dP_MonthTradeCnt)); /* 本月累積交易筆數      */
		//setValue("T_AMT_DAY",Double.toString(dP_DayTradeAmt)); /* 本日累積交易金額      */
		//setValue("T_CNT_DAY",Double.toString(dP_DayTradeCnt)); /* 本日累積交易筆數      */

		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_TIME",gb.getSysTime());
		setValue("CRT_USER","AUTH");
		setValue("MOD_USER","AUTH");

		setTimestamp("MOD_TIME", gb.getgTimeStamp());
		//setValue("MOD_TIME",gb.sysDate); //=>oO timestamp , np assign value ?


		setValue("MOD_PGM",gb.getSystemName());


		insertTable();



		return blResult;
	}

	//*新增卡戶當月之消費總金額*/
	public boolean initRiskTradeInfo() throws Exception {
		gb.showLogMessage("I","initRiskTradeInfo(): started");
		boolean blResult=true;
		daoTable = addTableOwner("CCA_RISK_T_AMT");//oracle table is RSK_T_AMT
		setValue("risk_type",gate.mccRiskType);
		setValue("card_acct_idx",gate.cardAcctIdx);
		setValue("last_consume_date",gb.getSysDate()); 
		setValue("T_AMT_MONTH","0");  /* 本月累積交易金額      */
		setValue("T_CNT_MONTH","0"); /* 本月累積交易筆數      */
		setValue("T_AMT_DAY","0"); /* 本日累積交易金額      */
		setValue("T_CNT_DAY","0"); /* 本日累積交易筆數      */
		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_TIME",gb.getSysTime());

		setValue("crt_user","AUTH");
		setValue("MOD_USER","AUTH");

		//setValue("MOD_TIME",gb.sysDate); //=>oO timestamp , np assign value ?
		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		setValue("mod_pgm",gb.getSystemName());

		insertTable();



		return blResult;
	}

	public int getSendMsgCount(String spCardNo, String spDate) throws Exception {
		//檢查當日是否已經發過簡訊

		gb.showLogMessage("I","getSendSmsCount : started");
		daoTable  = addTableOwner("CCA_MSG_LOG"); 
		selectSQL = "count(*) as SendMsgCount " ;
		whereStr  = "WHERE CARD_NO = ? and TX_DATE = ? and MSG_ID = ? ";
		setString(1, spCardNo);
		setString(2, spDate);
		setString(3, "1885");

		selectTable();

		int nlSendMsgCount = Integer.parseInt(getValue("SendMsgCount"));

		return nlSendMsgCount;
	}

	public int getAuthCountLimit() throws Exception{
		gb.showLogMessage("I","getAuthCountLimit(): started");
		int nlCount =0 ;
		//計算正負向交易次數

		/*
 				   CARD_NO=:db_curr_card_no
                   AND TX_DATE=:db_wk_sysdate
                   AND SUBSTR(POS_MODE,1,2) = ('81')
                   AND UCAF_IND NOT IN ('1', '2')
                   AND CCAS_AREA_FLAG ='F'
                   AND NT_AMT > 0 
                   AND ISO_RSP_CODE = '00'
                   AND TRANS_TYPE ='0200'
                   AND SUBSTR(PROC_CODE,1,2) = '00'
                                                    OR   CARD_NO=:db_curr_card_no
                                                    AND TX_DATE=:db_wk_sysdate
                                                    AND COND_CODE IN ('59','15')
                                                    AND EC_FLAG NOT IN ('5','6')
                                                    AND CCAS_AREA_FLAG ='F'
                                                    AND NT_AMT > 0 
                                                    AND ISO_RSP_CODE = '00'
                                                    AND TRANS_TYPE ='0200'
                                                    AND SUBSTR(PROC_CODE,1,2) = '00';

		 * */
		gb.showLogMessage("I","getSendSmsCount : started");
		daoTable  = addTableOwner("CCA_AUTH_TXLOG");
		//down, 計算正向交易比數 1      	   
		selectSQL = "count(*) as PositiveTradeCount1 " ;
		whereStr  = "WHERE CARD_NO = ? and TX_DATE = ? and SUBSTR(POS_MODE,1,2) = ? ";
		whereStr  += "AND UCAF_IND NOT IN (?, ?) ";
		whereStr  += "AND CCAS_AREA_FLAG =? ";
		whereStr  += "AND NT_AMT > ? ";
		whereStr  += "AND ISO_RESP_CODE = ?";
		whereStr  += "AND TRANS_TYPE =? ";
		whereStr  += "AND SUBSTR(PROC_CODE,1,2) = ? ";

		setString(1, gate.cardNo);
		setString(2, gate.txDate);
		setString(3, "81");
		setString(4, "1");
		setString(5, "2");
		setString(6, "F");
		setInt(7, 0);
		setString(8, "00");
		setString(9, "0200");
		setString(10, "00");
		selectTable();
		int nlPositiveTradeCount1 = Integer.parseInt(getValue("PositiveTradeCount1"));
		//up, 計算正向交易比數 1



		//down, 計算正向交易比數 2      	   
		selectSQL = "count(*) as PositiveTradeCount2 " ;
		whereStr  = "WHERE CARD_NO = ? and TX_DATE = ? and COND_CODE IN (?,?) ";
		whereStr  += "AND EC_FLAG NOT IN (?,?) ";
		whereStr  += "AND CCAS_AREA_FLAG =? ";
		whereStr  += "AND NT_AMT > ? ";
		whereStr  += "AND ISO_RESP_CODE = ?";
		whereStr  += "AND TRANS_TYPE =? ";
		whereStr  += "AND SUBSTR(PROC_CODE,1,2) = ? ";


		setString(1, gate.cardNo);
		setString(2, gate.txDate);
		setString(3, "59");
		setString(4, "15");
		setString(5, "5");
		setString(6, "6");
		setString(7, "F");
		setInt(8, 0);
		setString(9, "00");
		setString(10, "0200");
		setString(11, "00");


		selectTable();
		int nlPositiveTradeCount2 = Integer.parseInt(getValue("PositiveTradeCount2"));
		//up, 計算正向交易比數 2


		//down, 計算負向交易比數 1
		selectSQL = "count(*) as NegativeTradeCount1 " ;
		whereStr  = "WHERE CARD_NO = ? and TX_DATE = ? and SUBSTR(POS_MODE,1,2) = ? ";
		whereStr  += "AND UCAF_IND NOT IN (?, ?) ";
		whereStr  += "AND CCAS_AREA_FLAG =? ";
		whereStr  += "AND NT_AMT > ? ";
		whereStr  += "AND ISO_RESP_CODE = ?";
		whereStr  += "AND SUBSTR(PROC_CODE,1,2) = ? ";

		setString(1, gate.cardNo);
		setString(2, gate.txDate);
		setString(3, "81");
		setString(4, "1");
		setString(5, "2");
		setString(6, "F");
		setInt(7, 0);
		setString(8, "00");
		setString(9, "20");
		selectTable();
		int nlNegativeTradeCount1 = Integer.parseInt(getValue("NegativeTradeCount1"));

		//up, 計算負向交易比數 1


		//down, 計算負向交易比數 2

		selectSQL = "count(*) as NegativeTradeCount2 " ;
		whereStr  = "WHERE CARD_NO = ? and TX_DATE = ? AND COND_CODE IN (?,?) ";
		whereStr  += "AND EC_FLAG NOT IN (?,?) ";
		whereStr  += "AND CCAS_AREA_FLAG =? ";
		whereStr  += "AND NT_AMT > ? ";
		whereStr  += "AND ISO_RESP_CODE = ?";
		whereStr  += "AND SUBSTR(PROC_CODE,1,2) = ? ";


		setString(1, gate.cardNo);
		setString(2, gate.txDate);
		setString(3, "59");
		setString(4, "15");
		setString(5, "5");
		setString(6, "6");
		setString(7, "F");
		setInt(8, 0);
		setString(9, "00");
		setString(10, "20");
		selectTable();
		int nlNegativeTradeCount2 = Integer.parseInt(getValue("NegativeTradeCount2"));

		//up, 計算負向交易比數 2

		nlCount = ((nlPositiveTradeCount1 + nlPositiveTradeCount2)) -  ((nlNegativeTradeCount1+nlNegativeTradeCount2)) ; 

		return nlCount;
	}
	

	private void getSmsDetl(String spCardNote, String spEntryModeType, String sp3dTranxFlag, String spMccRiskType, String spDataType, String spDataCode1) throws Exception{
		daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
		selectSQL = "DATA_CODE1 as CcaAuthSmsDetlDataCode1 ";			
		whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and apr_flag=? and DATA_TYPE=?  and DATA_CODE1 =?";
		setString(1, spCardNote); //卡片等級(CARD_NOTE)
		setString(2, spEntryModeType ); //entry Mode類別
		setString(3, sp3dTranxFlag); //網路3D交易
		setString(4, spMccRiskType); //風險類別
		setString(5, "Y"); //覆核YN

		setString(6, spDataType); //資料類別(DATA_TYPE), hard code
		setString(7,spDataCode1);
		selectTable();



	}
	private void getSmsAmt1(String spCardNote, String spEntryModeType, String sp3dTranxFlag, String spMccRiskType, String spDataType, double dpDataAmt) throws Exception{
		daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
		selectSQL = "DATA_CODE1 as CcaAuthSmsDetlAmt1Code1, "
				  +	"DATA_CODE2 as CcaAuthSmsDetlAmt1Code2, "
				  +	"DATA_CODE3 as CcaAuthSmsDetlAmt1Code3 ";			
		whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and apr_flag=? and DATA_TYPE=?  and double(DATA_CODE1) <=?";
		setString(1, spCardNote); //卡片等級(CARD_NOTE)
		setString(2, spEntryModeType ); //entry Mode類別
		setString(3, sp3dTranxFlag); //網路3D交易
		setString(4, spMccRiskType); //風險類別
		setString(5, "Y"); //覆核YN

		setString(6, spDataType); //資料類別(DATA_TYPE), hard code
		setDouble(7, dpDataAmt); //交易金額
		selectTable();

	}
	//private boolean checkSmsDetl(String sP_CardNote, String sP_EntryModeType, String sP_3dTranxFlag, String sP_MccRiskType) throws Exception{
	public boolean checkSmsDetl(String spCardNote, String spEntryModeType, String sp3dTranxFlag, String spMccRiskType) throws Exception{
		gb.showLogMessage("I","checkSmsDetl(): started");
		boolean blIgnoreSms = false;
		String slCcaAuthSmsUseFlag = getValue("CcaAuthSmsUseFlag");
		if ("Y".equals(slCcaAuthSmsUseFlag)) {//表示要檢核細項
			//down, 取得 CCA_AUTH_SMSDETL
//			daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
//			selectSQL = "DATA_CODE1 as CcaAuthSmsDetlDataCode1 ";			
//			whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and  apr_flag=? and DATA_TYPE=?  and DATA_CODE1 =?";
//			setString(1, sP_CardNote); //卡片等級(CARD_NOTE)
//			setString(2, sP_EntryModeType ); //entry Mode類別
//			setString(3, sP_3dTranxFlag); //網路3D交易
//			setString(4, "Y"); //覆核YN



			//setString(7, sL_MccCode); //如果找得到資料，就表示要排除，所以就不發簡訊了 



			String slDataType="", slDataCode1="";
			//V1.00.11  Kevin       簡訊發送排除條件修正
			if ("Y".equals(getValue("CcaAuthSmsCond1Mcc"))) {
				slDataType = "MCC1";
				slDataCode1 = gate.mccCode;
				gb.showLogMessage("D","getSmsDetl found="+notFound+",NOTE="+spCardNote+",Mode="+ spEntryModeType+",Trans="+ sp3dTranxFlag+",risk="+ spMccRiskType+",data="+ slDataType+",code1="+  slDataCode1);
				getSmsDetl(spCardNote, spEntryModeType, sp3dTranxFlag, spMccRiskType, slDataType,  slDataCode1);

				if ( "Y".equals(notFound) ){//沒有找到資料表示不排除
					blIgnoreSms = false;//不排除
				}
				else    			   
					blIgnoreSms = true;//排除
				gb.showLogMessage("D","CcaAuthSmsCond1Mcc="+blIgnoreSms);



			}
			if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSmsCond1Mcht")))) {

				slDataType="MCHT1"; //資料類別(DATA_TYPE), hard code
				slDataCode1 = gate.merchantNo;
				gb.showLogMessage("D","getSmsDetl found="+notFound+",NOTE="+spCardNote+",Mode="+ spEntryModeType+",Trans="+ sp3dTranxFlag+",risk="+ spMccRiskType+",data="+ slDataType+",code1="+  slDataCode1);
				getSmsDetl(spCardNote, spEntryModeType, sp3dTranxFlag, spMccRiskType, slDataType,  slDataCode1);

				if ( "Y".equals(notFound) ){//沒有找到資料表示不排除
					blIgnoreSms = false;//不排除
				}
				else    			   
					blIgnoreSms = true;//排除
				gb.showLogMessage("D","CcaAuthSmsCond1Mcht="+blIgnoreSms);

			}
			if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSmsCond1Risk")))) {

				slDataType="RISK1"; //資料類別(DATA_TYPE), hard code
				slDataCode1 = gate.mccRiskType;
				gb.showLogMessage("D","getSmsDetl found="+notFound+",NOTE="+spCardNote+",Mode="+ spEntryModeType+",Trans="+ sp3dTranxFlag+",risk="+ spMccRiskType+",data="+ slDataType+",code1="+  slDataCode1);
				getSmsDetl(spCardNote, spEntryModeType, sp3dTranxFlag, spMccRiskType, slDataType,  slDataCode1);

				if ( "Y".equals(notFound) ){//沒有找到資料表示不排除
					blIgnoreSms = false;//不排除
				}
				else    			   
					blIgnoreSms = true;//排除
				gb.showLogMessage("D","CcaAuthSmsCond1Risk="+blIgnoreSms);

			}

			//up, 取得 CCA_AUTH_SMSDETL


		}
		else {
			blIgnoreSms = false;
		}
		if (!blIgnoreSms) {
			String slDataType="";
			slDataType="AMT1"; //資料類別(DATA_TYPE), hard code
			int ilDataAmt= (int) gate.ntAmt;
			getSmsAmt1(spCardNote, spEntryModeType, sp3dTranxFlag, spMccRiskType, slDataType,  gate.ntAmt);

			gb.showLogMessage("D","分期期數=" + getValue("CcaAuthSmsDetlAmt1Code2")+"利率="+getValue("CcaAuthSmsDetlAmt1Code3"));
		}
		return blIgnoreSms;
	}


	public void getCcaAuthSms(String spCardNote, String spEntryModeType, String sp3dTranxFlag, String spMccRiskType) throws Exception{
		gb.showLogMessage("I","getCcaAuthSms(): started");
		daoTable  = addTableOwner("CCA_AUTH_SMS");  


		selectSQL = "COND1_YN as CcaAuthSmsCond1Yn, "			//是否要 CHECK 條件一 , "Y" or "N"
				+ "TX_AMT as CcaAuthSmsTxAmt, "					//單筆金額門檻 => 大於等於 此金額要發簡訊
				+ "COND1_MCC as CcaAuthSmsCond1Mcc, "			//mcc code旗標-1 =>Y or N, 對應到 detail，取得要排除的 mcc code
				+ "COND1_MCHT as CcaAuthSmsCond1Mcht, "			//特店旗標-1 =>Y or N,對應到 detail，取得要排除的 mcht
				+ "COND1_RISK as CcaAuthSmsCond1Risk, "		    //風險類別 , "Y" or "N"
				+ "MSG_ID1 as CcaAuthSmsMsgId1, "				//簡訊代碼-1 =>條件一成立時，發此簡訊代碼
				+ "USE_FLAG as CcaAuthSmsUseFlag ";


		/*
	    CARD_NOTE       => 卡片等級        
 	    ENTRY_MODE_TYPE => entry Mode類別 
 	    WEB3D_FLAG      => 網路3D交易     
 	    RISK_TYPE       => 風險類別  
	      使用旗標, "Y" or "N"
		 */
		whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and USE_FLAG=? ";

		setString(1, spCardNote); //卡片等級(CARD_NOTE)
		setString(2,spEntryModeType ); //entry Mode類別
		setString(3, sp3dTranxFlag); //網路3D交易
		setString(4, spMccRiskType); //風險類別 
		setString(5, "Y"); //使用旗標

		selectTable();

	}
	// 檢核 簡訊參數檔
//	public boolean checkSmsParm() throws Exception {
//		//table is CCA_AUTH_SMS and CCA_AUTH_SMSDETL
//
//		boolean bL_IgnoreTradingSms = false;
//		gb.showLogMessage("D","loadSmsParm : started");
//
//		String sL_CardNote=getValue("CardBaseCardNote");
//		String sL_EntryModeType = gate.entryModeType;
//		String sL_3dTranxFlag = "";
//		String sL_MccRiskType = gate.mccRiskType;
//
//
//		if (gate.is3DTranx)
//			sL_3dTranxFlag = "Y";
//		else
//			sL_3dTranxFlag = "N";
//		
//		if (gate.isDebitCard)
//			sL_CardNote = "D";
//
//		// 信用卡處理四次，VD卡處理兩次 gate.isDebitCard
//		gb.showLogMessage("D","NOTE="+sL_CardNote+";EM="+sL_EntryModeType+";3D="+sL_3dTranxFlag+";mccRisk="+sL_MccRiskType);
//		getCcaAuthSms(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType); //第一次
//		if ( "Y".equals(notFound) ){  
//			sL_MccRiskType = "*"; // 用通用的 風險類別 再 select 一次			
//			getCcaAuthSms(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType); //第二次
//			if ( "Y".equals(notFound) ){
//				if (gate.isDebitCard) {
//					bL_IgnoreTradingSms = true;
//				}
//				else {
//					sL_CardNote = "*"; // 用通用的 風險類別 再 select 一次
//					sL_MccRiskType = gate.mccRiskType;
//					getCcaAuthSms(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType); //第三次
//					if ( "Y".equals(notFound) ){
//						sL_MccRiskType = "*"; // 用通用的 風險類別 再 select 一次			
//						getCcaAuthSms(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType); //第四次
//						if ( "Y".equals(notFound) ){
//							bL_IgnoreTradingSms = true;
//						}
//						else {
//							int nL_CcaAuthSmsTxAmt = getInteger("CcaAuthSmsTxAmt");
//							if (gate.nt_amt>nL_CcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
//								bL_IgnoreTradingSms = checkSmsDetl(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType);
//						}
//					}
//					else {
//						int nL_CcaAuthSmsTxAmt = getInteger("CcaAuthSmsTxAmt");
//						if (gate.nt_amt>nL_CcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
//							bL_IgnoreTradingSms = checkSmsDetl(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType);
//					}
//				}
//			}
//			else {
//				int nL_CcaAuthSmsTxAmt = getInteger("CcaAuthSmsTxAmt");
//				if (gate.nt_amt>nL_CcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
//					bL_IgnoreTradingSms = checkSmsDetl(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType);
//			}
//		}
//		else {
//			if (gate.nt_amt>getInteger("CcaAuthSmsTxAmt"))//消費金額 > 消費通知參數.單筆金額門檻
//				bL_IgnoreTradingSms = checkSmsDetl(sL_CardNote, sL_EntryModeType, sL_3dTranxFlag, sL_MccRiskType);
//		}
//
//
//		return bL_IgnoreTradingSms;
//
//	}
	//kevin:合庫消費簡訓內容取得
	//透過簡訊MSGID取得讀取簡訊內容	
	public String getSmsContentnt(String spMsgId) throws Exception {
		String slResult = "";
		gb.showLogMessage("I","getSmsContentnt=("+spMsgId+"): started!");
		daoTable  = addTableOwner("SMS_MSG_CONTENT"); //NCCC語音特店控制檔
		selectSQL = "MSG_CONTENT";


		whereStr  = "WHERE MSG_ID = ?";
		setString(1, spMsgId);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gb.showLogMessage("I","function: TA.getSmsContent -- can not find data. msg_id is  "+spMsgId + "--");
			slResult = "";

		}
		else {
		slResult = getValue("MSG_CONTENT");
		}

		return slResult;

	}


	//讀取簡訊參數檔
	public void checkSmsParmBefore20190114() throws Exception {
		//table is CCA_AUTH_SMS and CCA_AUTH_SMSDETL
		//舊程式

		gb.showLogMessage("I","loadSmsParm : started");
		daoTable  = addTableOwner("CCA_AUTH_SMS"); //Master data 
		selectSQL = "cond1_yn as CcaAuthSmsCond1Yn, "				//是否要 CHECK 條件一 , "Y" or "N"
				+ "tx_amt as CcaAuthSmsTxAmt, "					//單筆金額門檻 => 大於等於 此金額要發簡訊
				+ "cond1_mcc as CcaAuthSmsCond1Mcc, "			//mcc code旗標-1 =>Y or N, 對應到 detail，取得要排除的 mcc code
				+ "cond1_mcht as CcaAuthSmsCond1Mcht, "			//特店旗標-1 =>Y or N,對應到 detail，取得要排除的 mcht
				+ "cond2_yn as CcaAuthSmsCond2Yn, "				//是否要 CHECK 條件二 , "Y" or "N"
				+ "day_cnt as CcaAuthSmsDayCnt, "				//日累計筆數門檻 => 每日消費次數 大於等於 幾筆 ，則要發簡訊  
				+ "cond2_mcc as CcaAuthSmsCond2Mcc, "			//mcc code旗標-2 =>Y or N,對應到 detail，取得要排除的 mcc code
				+ "cond2_mcht as CcaAuthSmsCond2Mcht, "			//特店旗標-2 =>Y or N, 對應到 detail，取得要排除的 mcht
				+ "cond2_country as CcaAuthSmsCond2Country, "	//國別旗標-2 =>Y or N, 對應到 detail，取得要排除的 country
				+ "msg_id1 as CcaAuthSmsMsgId1, "				//簡訊代碼-1 =>條件一成立時，發此簡訊代碼
				+ "msg_id2 as CcaAuthSmsMsgId2";				//簡訊代碼-2 =>條件二成立時，發此簡訊代碼




		whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=?";


		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		String slCardNote=getValue("CardBaseCardNote");
		String slEntryModeType = gate.entryModeType;
		String sl3dTranxFlag = "";
		String slMccRiskType = gate.mccRiskType;
		String slMccCode= gate.mccCode;
		String slMchtNo= gate.merchantNo;
		int nlDayTransCount = 0;//日累積交易次數
		String slCountry = gate.merchantCountry;


		if (gate.cashAdvance)
			nlDayTransCount = getInteger("CcaConsumeFcTotCntDay")+1; ////國外預借現金日總次 (gate.cardAcctFcTotCntDay)
		else if ("F".equals(gate.areaType))
			nlDayTransCount = getInteger("CcaConsumeFnTotCntDay")+1;//國外一般消費日總次 (gate.cardAcctFnTotCntDay)
		else 
			nlDayTransCount = getInteger("CcaConsumeTxTotCntDay")+1;//累積日消費次數  (gate.cardAcctTotCntDay)

		if (gate.is3DTranx)
			sl3dTranxFlag = "Y";
		else
			sl3dTranxFlag = "N";

		setString(1, slCardNote); //卡片等級(CARD_NOTE)
		setString(2,slEntryModeType ); //entry Mode類別
		setString(3, sl3dTranxFlag); //網路3D交易
		setString(4, slMccRiskType); //風險類別 

		selectTable();
		if ( "Y".equals(notFound) ){
			return ;
		}
		else {
			// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
			if ("Y".equals(getValue("CcaAuthSmsCond1Yn").toUpperCase(Locale.TAIWAN))) { //要check 條件一 
				if (gate.isoFiled4Value>= getInteger("CcaAuthSmsTxAmt")) {//消費金額大於等於條件一的門檻
					gate.ifSendSms4Cond1 = true;//設定為要發條件一簡訊

					if ("Y".equals(getValue("CcaAuthSmsCond1Mcc"))) {//要 check 條件一 要排除的 MCC
						daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
						selectSQL = "DATA_CODE1 as CcaAuthSmsDetlDataCode1";			
						whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and DATA_TYPE=? and apr_flag=? and data_code1=? ";
						setString(1, slCardNote); //卡片等級(CARD_NOTE)
						setString(2, slEntryModeType ); //entry Mode類別
						setString(3, sl3dTranxFlag); //網路3D交易
						setString(4, slMccRiskType); //風險類別 
						setString(5, "MCC1"); //資料類別, hot code
						setString(6, "Y"); //覆核YN
						setString(7, slMccCode); //如果找得到資料，就表示要排除，所以就不發簡訊了 

						selectTable();
						if ( "N".equals(notFound) ){//有找到資料
							gate.ifSendSms4Cond1 = false;//設定為不要發條件一簡訊
						}
						else    			   
							gate.ifSendSms4Cond1 = true;//設定為要發條件一簡訊

					}
					if ("Y".equals(getValue("CcaAuthSmsCond1Mcht"))) {//要 check 條件一 要排除的  mcht(特店)
						daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
						selectSQL = "DATA_CODE1 as CcaAuthSmsDetlDataCode1";			
						whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and DATA_TYPE=? and apr_flag=? and data_code1=? ";
						setString(1, slCardNote); //卡片等級(CARD_NOTE)
						setString(2, slEntryModeType ); //entry Mode類別
						setString(3, sl3dTranxFlag); //網路3D交易
						setString(4, slMccRiskType); //風險類別 
						setString(5, "MCHT1"); //資料類別, hot code
						setString(6, "Y"); //覆核YN
						setString(7, slMchtNo); //如果找得到資料，就表示要排除，所以就不發簡訊了 

						selectTable();
						if ( "N".equals(notFound) ){//有找到資料
							gate.ifSendSms4Cond1 = false;//設定為不要發條件一簡訊
						}
						else
							gate.ifSendSms4Cond1 = true;//設定為要發條件一簡訊

					}

				}

			}
			// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
			if ("Y".equals(getValue("CcaAuthSmsCond2Yn").toUpperCase(Locale.TAIWAN))) {//要check 條件二
				if (nlDayTransCount>= getInteger("CcaAuthSmsDayCnt")) {
					gate.ifSendSms4Cond2 = true;//設定為要發條件二簡訊

					if ("Y".equals(getValue("CcaAuthSmsCond2Mcc"))) {//要 check 條件二 要排除的 MCC
						daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
						selectSQL = "DATA_CODE2 as CcaAuthSmsDetlDataCode2";			
						whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and DATA_TYPE=? and apr_flag=? and data_code2=? ";
						setString(1, slCardNote); //卡片等級(CARD_NOTE)
						setString(2, slEntryModeType ); //entry Mode類別
						setString(3, sl3dTranxFlag); //網路3D交易
						setString(4, slMccRiskType); //風險類別 
						setString(5, "MCC2"); //資料類別, hot code
						setString(6, "Y"); //覆核YN
						setString(7, slMccCode); //如果找得到資料，就表示要排除，所以就不發簡訊了 

						selectTable();
						if ( "N".equals(notFound) ){//有找到資料
							gate.ifSendSms4Cond2 = false;//設定為不要發條件二簡訊
						}
						else    			   
							gate.ifSendSms4Cond2 = true;//設定為要發條件二簡訊

					}
					if ("Y".equals(getValue("CcaAuthSmsCond2Mcht"))) {//要 check 條件二 要排除的  mcht(特店)
						daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
						selectSQL = "DATA_CODE2 as CcaAuthSmsDetlDataCode2";			
						whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and DATA_TYPE=? and apr_flag=? and data_code2=? ";
						setString(1, slCardNote); //卡片等級(CARD_NOTE)
						setString(2, slEntryModeType ); //entry Mode類別
						setString(3, sl3dTranxFlag); //網路3D交易
						setString(4, slMccRiskType); //風險類別 
						setString(5, "MCHT2"); //資料類別, hot code
						setString(6, "Y"); //覆核YN
						setString(7, slMchtNo); //如果找得到資料，就表示要排除，所以就不發簡訊了 

						selectTable();
						if ( "N".equals(notFound) ){//有找到資料
							gate.ifSendSms4Cond2 = false;//設定為不要發條件二簡訊
						}
						else
							gate.ifSendSms4Cond2 = true;//設定為要發條件二簡訊

					}

					if ("Y".equals(getValue("CcaAuthSmsCond2Country"))) {//要 check 條件二 要排除的  country
						daoTable  = addTableOwner("CCA_AUTH_SMSDETL"); //Detail data 
						selectSQL = "DATA_CODE2 as CcaAuthSmsDetlDataCode2";			
						whereStr  = "WHERE CARD_NOTE = ? and ENTRY_MODE_TYPE=? and WEB3D_FLAG=? and RISK_TYPE=? and DATA_TYPE=? and apr_flag=? and data_code2=? ";
						setString(1, slCardNote); //卡片等級(CARD_NOTE)
						setString(2, slEntryModeType ); //entry Mode類別
						setString(3, sl3dTranxFlag); //網路3D交易
						setString(4, slMccRiskType); //風險類別 
						setString(5, "CNTRY2"); //資料類別, hot code
						setString(6, "Y"); //覆核YN
						setString(7, slCountry); //如果找得到資料，就表示要排除，所以就不發簡訊了 

						selectTable();
						if ( "N".equals(notFound) ){//有找到資料
							gate.ifSendSms4Cond2 = false;//設定為不要發條件二簡訊
						}
						else
							gate.ifSendSms4Cond2 = true;//設定為要發條件二簡訊

					}

				}
			}


		}
		return ;

	}

	public boolean getSmsParmData(String spCardNote, String spTransCode) throws Exception{
		boolean blResult = true;

		gb.showLogMessage("I","loadCCA_AUTH_SMS2_PARM : started");
		daoTable  = addTableOwner("CCA_AUTH_SMS2_PARM"); //Master data 
		selectSQL = "cond1_yn as CcaAuthSms2Cond1Yn, "				//是否要 CHECK 條件一 , "Y" or "N"
				+ "dd_tx_times as CcaAuthSms2DayTxCnt, "				//日累計筆數門檻 => 每日消費次數 大於等於 幾筆 ，則要發簡訊    		   
				+ "cond1_mcc as CcaAuthSms2Cond1Mcc, "			//mcc code旗標-1 =>Y or N, 對應到 detail，取得要排除的 mcc code
				+ "cond1_mcht as CcaAuthSms2Cond1Mcht, "			//特店旗標-1 =>Y or N,對應到 detail，取得要排除的 mcht
				+ "cond1_risk as CcaAuthSms2Cond1Risk, "					
				+ "cond2_yn as CcaAuthSms2Cond2Yn, "				//是否要 CHECK 條件二 , "Y" or "N"
				+ "cond2_RESP1 as CcaAuthSms2Cond2Resp1, "
				+ "cond2_RESP2 as CcaAuthSms2Cond2Resp2, "								
				+ "cond2_mcc as CcaAuthSms2Cond2Mcc, "			//mcc code旗標-2 =>Y or N,對應到 detail，取得要排除的 mcc code
				+ "cond2_mcht as CcaAuthSms2Cond2Mcht, "			//特店旗標-2 =>Y or N, 對應到 detail，取得要排除的 mcht
				+ "cond2_risk as CcaAuthSms2Cond2Risk, "
				+ "msg_id1 as CcaAuthSms2MsgId1, "				//簡訊代碼-1 =>條件一成立時，發此簡訊代碼
				+ "msg_id2 as CcaAuthSms2MsgId2, " 				//簡訊代碼-2 =>條件二成立時，發此簡訊代碼
				+ "cond1_area as CcaAuthSms2Cond1Area";         //國內外-1旗標  



		whereStr  = "WHERE CARD_NOTE = ? and WEB3D_FLAG=?";


		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		String slCardNote=spCardNote;

		setString(1, slCardNote); //卡片等級(CARD_NOTE)
		setString(2, spTransCode); //原WEB3D_FLAG作為交易類別使用 



		selectTable();
		if ( "Y".equals(notFound) ){
			blResult = false;
		}

		return blResult;

	}

//	//檢核 授權特殊消費簡訊參數
//	public void checkSmsParm4SpecialTrading() throws Exception {
//		//table is CCA_AUTH_SMS2_PARM and CCA_AUTH_SMS2_DETL
//
//		String slCardNote=getValue("CardBaseCardNote");
//
//		String slTranCode = "";
//		int nlDayTransCount = 0;//日累積交易次數
//		
//		if (gate.ecTrans) {
//			slTranCode = "OA";    //OA:網路交易 gate.ecTrans
//		}
//		else {
//			slTranCode = gate.transCode;
//		}
//
//		boolean blFindTableData = getSmsParmData(slCardNote, slTranCode);
//
//		if (!blFindTableData) {
//			slCardNote="*";
//			blFindTableData = getSmsParmData(slCardNote, slTranCode);
//		}
//
//		if (blFindTableData) {			
//			if ("3".equals(getValue("CcaAuthSms2Cond1Area"))) {
//				nlDayTransCount	= selectCcaAuthTxLog4SmsInd("3") + 1 ; //交易累計次數須包含本次交易
//				gb.showLogMessage("D","處理國內外"+gate.transCode+getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);
//				checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
//			}
//			else if ("2".equals(getValue("CcaAuthSms2Cond1Area"))) {
//				 if ("T".equals(gate.areaType)) {
//					nlDayTransCount	= selectCcaAuthTxLog4SmsInd("2") + 1 ; //交易累計次數須包含本次交易
//					gb.showLogMessage("D","處理國內"+gate.transCode+getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);
//					checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
//				 }
//			}
//			else if ("1".equals(getValue("CcaAuthSms2Cond1Area"))) {
//				 if ("F".equals(gate.areaType)) {
//					nlDayTransCount	= selectCcaAuthTxLog4SmsInd("1") + 1 ; //交易累計次數須包含本次交易
//					gb.showLogMessage("D","處理國外"+gate.transCode+getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);
//					checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
//				 }
//			}
//		}
//		return ;
//	}

	public void checkSms2Detl(String spCardNote, String spTransCode, int npDayTransCount) throws Exception{
		gb.showLogMessage("I","checkSms2Detl(): started!");
		boolean blIgnoreSms = false;

		String slMccRiskType = gate.mccRiskType;
		String slMccCode= gate.mccCode;
		String slMchtNo= gate.merchantNo;

		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		if ("Y".equals(getValue("CcaAuthSms2Cond1Yn").toUpperCase(Locale.TAIWAN))) { //要check 條件一 
			if (npDayTransCount>= getInteger("CcaAuthSms2DayTxCnt")) {//日累計筆數 > 日累計筆數門檻
				gate.ifSendSms4Cond1 = true;//設定為要發條件一簡訊

				if ("Y".equals(getValue("CcaAuthSms2Cond1Mcc"))) {//要 check 條件一 要排除的 MCC
					daoTable  = addTableOwner("cca_auth_sms2_detl"); //Detail data 
					selectSQL = "DATA_CODE1 as CcaAuthSms2DetlDataCode1";			
					whereStr  = "WHERE CARD_NOTE = ? and WEB3D_FLAG=? and DATA_TYPE=? and apr_flag=? and data_code1=? ";
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "MCC1"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMccCode); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)

				}
				if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSms2Cond1Mcht")))) {//要 check 條件一 要排除的  mcht(特店)
					daoTable  = addTableOwner("cca_auth_sms2_detl"); //Detail data 
					selectSQL = "DATA_CODE1 as CcaAuthSms2DetlDataCode1";			
					whereStr  = "WHERE CARD_NOTE = ? and WEB3D_FLAG=? and DATA_TYPE=? and apr_flag=? and data_code1=? ";
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "MCHT1"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMchtNo); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)
				}


				if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSms2Cond1Risk")))) {//要 check 條件一 要排除的 Risk
					daoTable  = addTableOwner("cca_auth_sms2_detl"); //Detail data 
					selectSQL = "DATA_CODE1 as CcaAuthSms2DetlDataCode1";			
					whereStr  = "WHERE CARD_NOTE = ? and WEB3D_FLAG=? and DATA_TYPE=? and apr_flag=? and data_code1=? ";
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "RISK1"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMccRiskType); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)

				}

				if (blIgnoreSms)
					gate.ifSendSms4Cond1 = false;//設定為不要發條件一簡訊
				else
					gate.ifSendSms4Cond1 = true;//設定為要發條件一簡訊
			}

		}
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		if ("Y".equals(getValue("CcaAuthSms2Cond2Yn").toUpperCase(Locale.TAIWAN))) {//要check 條件二

			blIgnoreSms = false;
			String slRespCode = gate.isoField[39];
			daoTable  = addTableOwner("CCA_AUTH_SMS2_DETL"); //Detail data 
			selectSQL = "DATA_CODE1 as CcaAuthSms2DetlDataCode1";			
			whereStr  = "WHERE CARD_NOTE = ? and WEB3D_FLAG=? and DATA_TYPE=? and apr_flag=? and data_code2=? ";

			if ((slRespCode.equals(getValue("CcaAuthSms2Cond2Resp1"))) ||
					(slRespCode.equals(getValue("CcaAuthSms2Cond2Resp2"))) ){
				gate.ifSendSms4Cond2 = true;//設定為要發條件二簡訊

				if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSms2Cond2Mcc")))) {//要 check 條件二 要排除的 MCC
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "MCC2"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMccCode); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)

				}
				if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSms2Cond2Mcht")))) {//要 check 條件二 要排除的  mcht(特店)
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "MCHT2"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMchtNo); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)

				}
				if ((!blIgnoreSms) && ("Y".equals(getValue("CcaAuthSms2Cond1Risk")))) {//要 check 條件一 要排除的 Risk
					setString(1, spCardNote); //卡片等級(CARD_NOTE)
					setString(2, spTransCode ); //原WEB3D_FLAG作為交易類別使用
					setString(3, "RISK2"); //資料類別, hot code
					setString(4, "Y"); //覆核YN
					setString(5, slMccRiskType); //如果找得到資料，就表示要排除，所以就不發簡訊了 

					selectTable();
					if ( "Y".equals(notFound) ){//沒有找到資料
						blIgnoreSms = true;//排除(不要發送簡訊)
					}
					else    			   
						blIgnoreSms = false;//不排除(要發送簡訊)

				}

				if (blIgnoreSms)
					gate.ifSendSms4Cond2 = false;//設定為不要發條件2簡訊
				else
					gate.ifSendSms4Cond2 = true;//設定為要發條件2簡訊


			}
		}




	}
	
	//讀取消費簡訊門檻參數
	public ResultSet loadSmsMsgParm() throws Exception{
		gb.showLogMessage("I","loadSmsMsgParm(): started!");
		selectSQL = "sms_priority as SmsMsgParmPriority, "      //
				+ "msg_id as SmsMsgParmMsgId, "				    //
				+ "spec_list as SmsMsgParmSpecList, "		    //
				+ "area_type as SmsMsgParmAreaType, "		    //
				+ "spec_list as SmsMsgParmSpecList, "		    //
				+ "cond_country as SmsMsgParmCondCountry, "     //
				+ "cond_curr as SmsMsgParmCondCurr, "		    //
				+ "cond_bin as SmsMsgParmCondBin, "				//
				+ "cond_mcht as SmsMsgParmCondMcht, "	    	//
				+ "cond_mcc as SmsMsgParmCondMcc, "				//
				+ "cond_pos as SmsMsgParmCondPos, "		        //
				+ "cond_trans_type as SmsMsgParmCondTransType, "//
				+ "cond_resp_code as SmsMsgParmCondRespCode, "	//
				+ "cond_amt as SmsMsgParmCondAmt, "				//
				+ "tx_amt as SmsMsgParmTxAmt, "		            //
				+ "cond_cnt1 as SmsMsgParmCondCnt1, "	    	//
				+ "tx_day as SmsMsgParmTxDay, "		            //				
				+ "tx_dat_cnt as SmsMsgParmTxDatCnt, "	        //
				+ "cond_cnt2 as SmsMsgParmCondCnt2, "	    	//
				+ "tx_hour as SmsMsgParmTxHour, "	            //	
				+ "tx_hour_cnt as SmsMsgParmTxhourCnt, "	        //
		    	+ "cond_group as SmsMsgParmCondGroup, "         //
		    	+ "cond_success as SmsMsgParmCondSuccess, "     //
		    	+ "cond_or_and1 as SmsMsgParmConeOrAnd1, "      //
		    	+ "cond_or_and2 as SmsMsgParmCondOrAnd2, "      //
		    	+ "cond1_amt as SmsMsgParmCond1Amt, "           //
		    	+ "cond2_amt as SmsMsgParmCond2Amt ";           //
		
//		gb.showLogMessage("D","SELECT SQL="+selectSQL);
		daoTable = addTableOwner("sms_msg_parm");
		whereStr="WHERE apr_date <> '' order by sms_priority Asc ";

		ResultSet L_RS = getTableResultSet();
		
		return L_RS;
	}
	
	//讀取消費簡訊門檻參數
	public ResultSet loadSmsMsgParmDetl() throws Exception{
		gb.showLogMessage("I","loadSmsMsgParmDetl(): started!");
		selectSQL = "sms_priority as SmsMsgParmDetlPriority, "      //
				+ "data_type as SmsMsgParmDetlDataType, "		    //
				+ "data_code1 as SmsMsgParmDetlDataCode1 ";		    //

		
//		gb.showLogMessage("D","SELECT SQL="+selectSQL);
		daoTable = addTableOwner("sms_msg_parm_detl");
		whereStr="WHERE apr_date <> '' order by sms_priority Asc ";

		ResultSet L_RS = getTableResultSet();
		
		return L_RS;
	}

	///*讀取卡戶當月之消費總金額*/
//	public boolean loadRiskTradeInfoByDecodeMarkedByHoward() throws Exception {
//		//proc => TB_rsk_t_amt()
//		gb.showLogMessage("D","loadRiskTradeInfoByDecode : started");
//		daoTable  = addTableOwner("CCA_RISK_T_AMT"); //oracle table is RSK_T_AMT
//		selectSQL = "decode(last_consume_date,to_char(sysdate,'yyyymmdd'),t_amt_day,0) as WkDayAmt,"
//				+ "decode(last_consume_date,to_char(sysdate,'yyyymmdd'),t_cnt_day,0) as WkDayCnt,"
//				+ "NVL(LAST_CONSUME_DATE,'00000000') as WkLastConsumeDate,"
//				+ "decode(substr(last_consume_date,1,6),to_char(sysdate,'yyyymm'),t_amt_month,0) as WkMonthAmt,"
//				+ "decode(substr(last_consume_date,1,6),to_char(sysdate,'yyyymm'),t_cnt_month,0) as WkMonthCnt";
//
//		whereStr  = "WHERE RISK_TYPE = ? and CARD_ACCT_IDX=?";
//
//
//		setString(1, gate.mccRiskType);
//		setString(2, gate.cardAcctIdx);
//		selectTable();
//		if ( "Y".equals(notFound) ){
//			return false;
//		}
//		return true;
//
//	}

	/*
  	// Ū CCS_CARD_ACCT
   	public boolean loadCardAcct_ibt() throws Exception {
	   gb.showLogMessage("D","loadCardAcct : ","started");
	   if (gate.is_sup_card)
		   gb.showLogMessage("D","is sup card","");

	   //-d-
	   if (gate.is_sup_card) {
	   //if (true) {
		   if (!select_acnoAcct_sup_ibt()) {
			   return false;
		   }
	   }


	   //-d-
	   //daoTid ="acno.";
	   daoTable  = "CCSV_ACNO_ACCT";
       selectSQL = " P_SEQNO , "
    		   + " ID_P_SEQNO       , "
    		   + " SUP_FLAG         , "
    		   + " LOC_AMT_DD       , "
    		   + " LOC_AMT_MM       , "
    		   + " ADJ_AMT_MM       , "
    		   + " ADJ_AMT_DATE1    , "
    		   + " ADJ_AMT_DATE2    , "
    		   + " LIMIT_CHG_REMARK , "
    		   + " LIMIT_CHG_FROM   , "
    		   + " LIMIT_CHG_TIME   , "
    		   + " LIMIT_CHG_USER   , "
    		   + " LAST_CONSUME_DATE, "
    		   + " TX_AMT_DD        , "
    		   + " TX_AMT_MM        , "
    		   + " BANK_AMT_DD      , "
    		   + " BANK_AMT_MM      , "
    		   + " TRAIN_AMT_DD     , "
    		   + " TRAIN_AMT_MM     , "
//    		   + " CURR_AMT_DD      , "
//    		   + " CURR_AMT_MM      , "
    		   + " HAS_CARD_ACCT      "
    		   ;

       whereStr  = "WHERE P_SEQNO = ? and ID_P_SEQNO =? and sup_flag='0'";


       setString(1, getValue("p_seqno"));
       setString(2, getValue("major_id_p_seqno"));
       selectTable();


       gb.showLogMessage("D","loadCardAcct : ","info:" + getValue("p_seqno") + "--" + getValue("major_id_p_seqno"));

       if ( "Y".equals(notFound) )
          { return false; }

       //-b`O-
       //daoTid ="acno.";
       daoTable ="CCSV_ACNO_ACCT";
       selectSQL = " sum(nvl(CURR_AMT_DD,0)) as curr_amt_dd , "
    		   + " sum(nvl(CURR_AMT_MM,0)) as curr_amt_mm  "
    		   ;
       whereStr  = "WHERE P_SEQNO = ?";
       setString(1, getValue("p_seqno"));
       selectTable();
       if ( "Y".equals(notFound) ) {
    	   setValue("curr_amt_dd","0");
    	   setValue("curr_amt_mm","0");
       }

       //-set: auth_txlog-
//     curr_tot_lmt_amt   NUMBER(12,2)               DEFAULT 0,  --B()
//     curr_tot_std_amt   NUMBER(12,2)               DEFAULT 0,  --зB()
//     curr_tot_tx_amt    NUMBER(12,2)               DEFAULT 0,  --`O()
//     curr_dd_lmt_amt    NUMBER(12,2)               DEFAULT 0,  --зB()
//     curr_dd_tot_amt    NUMBER(12,2)               DEFAULT 0,  --`O()
       //-b-
       gate.curr_tot_lmt_amt =getDouble("LOC_AMT_MM");
       gate.curr_tot_std_amt = getDouble("LOC_AMT_MM");
       gate.curr_tot_tx_amt =getDouble("CURR_AMT_MM");



       gate.curr_dd_lmt_amt =getDouble("LOC_AMT_DD");
       gate.curr_dd_tot_amt =getDouble("CURR_AMT_DD");

       //gate.curr_tot_lmt_amt =getDouble("acno.LOC_AMT_MM");
       //gate.curr_tot_std_amt = getDouble("acno.LOC_AMT_MM");
       //gate.curr_tot_tx_amt =getDouble("acno.curr_amt_mm");
       //gate.curr_dd_lmt_amt =getDouble("acno.loc_amt_dd");
       //gate.curr_dd_tot_amt =getDouble("acno.curr_amt_dd");

       //-{-
       if (gb.sysDate.compareTo(getValue("ADJ_AMT_DATE1"))>=0 &&
    		   gb.sysDate.compareTo(getValue("ADJ_AMT_DATE2"))<=0) {
    	   gate.curr_tot_lmt_amt =getDouble("ADJ_AMT_MM");
       }

       //if (gb.sysDate.compareTo(getValue("acno.adj_amt_date1"))>=0 &&
    		   //gb.sysDate.compareTo(getValue("acno.adj_amt_date2"))<=0) {
    	   //gate.curr_tot_lmt_amt =getDouble("acno.ADJ_AMT_MM");
       //}


       //-d-
       if (gate.is_sup_card) {
    	   gate.curr_tot_lmt_amt =getDouble("LOC_AMT_MM");
    	   gate.curr_tot_std_amt =getDouble("LOC_AMT_MM");
    	   gate.curr_tot_tx_amt =getDouble("CURR_AMT_MM");
    	   gate.curr_dd_lmt_amt =getDouble("LOC_AMT_DD");
    	   gate.curr_dd_tot_amt =getDouble("CURR_AMT_DD");
           //-{-
           if (gb.sysDate.compareTo(getValue("ADJ_AMT_DATE1"))>=0 &&
        		   gb.sysDate.compareTo(getValue("ADJ_AMT_DATE2"))<=0) {
        	   gate.curr_tot_lmt_amt =getDouble("ADJ_AMT_MM");
           }
       }

       //       if (gate.is_sup_card) {
    	 //  gate.curr_tot_lmt_amt =getDouble("acno1.LOC_AMT_MM");
//    	   gate.curr_tot_std_amt =getDouble("acno1.LOC_AMT_MM");
  //  	   gate.curr_tot_tx_amt =getDouble("acno1.curr_amt_mm");
    //	   gate.curr_dd_lmt_amt =getDouble("acno1.loc_amt_dd");
    	//   gate.curr_dd_tot_amt =getDouble("acno1.curr_amt_dd");
           //-{-
          // if (gb.sysDate.compareTo(getValue("acno1.adj_amt_date1"))>=0 &&
        	//	   gb.sysDate.compareTo(getValue("acno1.adj_amt_date2"))<=0) {
        	   //gate.curr_tot_lmt_amt =getDouble("acno1.ADJ_AMT_MM");
           //}
       //}


       return true;


    }
	 */ 
	//收單特店資料 CCS_MCHT_BASE
	//kevin:取消CCA_MCHT_BASE，因為TCB無法取得NCCC收單特店資訊
//	public boolean selectMchtBase() throws Exception {
//		daoTable  = addTableOwner("CCA_MCHT_BASE");
//		selectSQL = "MCHT_NAME,"
//				+ "zip_code,"
//				+ "zip_city,"
//				+ "mcht_addr,"
//				+ "tel_no,"
//				+ "contr_type,"
//				+ "bank_no,"
//				+ "POS_FLAG,"
//				+ "EDC_FLAG,"
//				+ "NVL(NCC_CONTR_DATE,'00000000')  as MchtBaseNcccContDate,"
//				+ "NVL(NCC_CONTR_END_DATE,'00000000') as MchtBaseNcccContEndDate,"
//				+ "NCC_RISK_LEVEL as MchtBaseNcccRiskLevel,"
//				+ "NCC_CFLOOR_LIMIT,"
//				+ "NCC_GFLOOR_LIMIT,"
//				+ "NVL(VIS_CONTR_DATE,'00000000') as MchtBaseVisaContDate,"
//				+ "NVL(VIS_CONTR_END_DATE,'00000000')  as MchtBaseVisaContEndDate,"
//				+ "VIS_RISK_LEVEL as MchtBaseVisaRiskLevel,"
//				+ "VIS_CFLOOR_LIMIT,"
//				+ "VIS_GFLOOR_LIMIT,"
//				+ "NVL(MST_CONTR_DATE,'00000000') as MchtBaseMasterContDate,"
//				+ "NVL(MST_CONTR_END_DATE,'00000000') as MchtBaseMasterContEndDate,"
//				+ "MST_RISK_LEVEL as MchtBaseMasterRiskLevel,"
//				+ "MST_CFLOOR_LIMIT,"
//				+ "MST_GFLOOR_LIMIT,"
//				+ "NVL(JCB_CONTR_DATE,'00000000') as MchtBaseJcbContDate,"
//				+ "NVL(JCB_CONTR_END_DATE,'00000000') as MchtBaseJcbContEndDate,"
//				+ "JCB_RISK_LEVEL as MchtBaseJcbRiskLevel,"
//				+ "JCB_CFLOOR_LIMIT,"
//				+ "JCB_GFLOOR_LIMIT,"
//				+ "MCHT_LEVEL,"
//				+ "MCHT_STOP,"
//				+ "mcht_discontr as MchtBaseDiscontractCode,"
//				+ "ic_flag,"
//				+ "current_code";
//
//		whereStr  = "WHERE MCHT_NO = ?  AND ACQ_BANK_ID  = decode(?,'493817','493817','400996') and MCC_CODE = ? ";
//		setString(1,gate.merchantNo);//0101001241
//		setString(2,gate.isoField[32]);//493817
//		String sL_MccCode = gate.mccCode;
//		//sL_MccCode = "9999"; //for test
//		setString(3,sL_MccCode);//mcc code
//		selectTable();
//		if ( "Y".equals(notFound) ) {
//			
//			return false; 
//		}
//
//		gate.merchantName = getValue("MCHT_NAME");
//		return true;
//	}
	


	//本行管制風險特店代碼 CCA_MCHT_RISK_LEVEL
	public boolean selectMchtRiskLevel(String spMchtRiskCode) throws Exception {
		gb.showLogMessage("I","selectMchtRiskLevel(): started!");


		daoTable  = addTableOwner("CCA_MCHT_RISK_LEVEL");
		selectSQL = "NVL(MCHT_RISK_CODE,' '),"
				+ "NVL(RESP_CODE,' ') as MchtRiskLevelRspCode";

		whereStr  = "WHERE MCHT_RISK_CODE = ?";
		setString(1, spMchtRiskCode);

		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 
		}


		return true;
	}


	// 查核此特店是否為NCCC語音特店控制檔
	public boolean selectCcaVoice(String spWkMchtNo) throws Exception {
		gb.showLogMessage("I","selectCcaVoice(): started!");

		daoTable  = addTableOwner("CCA_VOICE"); //NCCC語音特店控制檔
		selectSQL = "VOICE_ID";


		whereStr  = "WHERE VOICE_ID = ?";
		setString(1, spWkMchtNo);

		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 
		}


		return true;
	}

	// Ū CCS_COUNTRY
	//kevin: 取得風險分數 riskFactor
	public boolean selectCountry() throws Exception {
		gb.showLogMessage("I","selectCountry(): started!");

		daoTable  = addTableOwner("CCA_COUNTRY");
		selectSQL = "REJ_CODE as CountryRejCode,"
				  + "COUNTRY_CODE as CountryCode,"
				  + "RISK_FACTOR as CountryRiskFactor";

		if (gate.merchantCountry.length()>2) {
			whereStr  = "WHERE BIN_COUNTRY = ? and CCAS_LINK_TYPE=? ";
		}
		else {
			whereStr  = "WHERE COUNTRY_CODE = ? and CCAS_LINK_TYPE=? ";
		}
//		whereStr  = "WHERE COUNTRY_CODE = ? and CCAS_LINK_TYPE=? ";
		setString(1,gate.merchantCountry); //MZ
		//setString(1,"MZ"); //MZ
//		setString(2,"NCCC"); //Tw 
		setString(2,"FISC");
		selectTable();

		//gb.showLogMessage("D","傳入COUNTRY_CODE=>" + gate.merchantCountry);
		//gb.showLogMessage("D","REJ_CODE=>" + getValue("CountryRejCode"));

		boolean blResult=true;
		if ( "Y".equals(notFound) ) 
			blResult=false;


		return blResult;
	}

	//kevin: 取得風險分數 riskFactor
	public boolean selectBlockCard() throws Exception {
		gb.showLogMessage("I","selectBlockCard(): started!");

		daoTable  = addTableOwner("RSK_BLOCK_CARD");
		selectSQL = "COUNT(*) as Db_Cnt";

		whereStr  = "WHERE card_no = ? ";
		setString(1, gate.cardNo); //卡號
		selectTable();

		gb.showLogMessage("D","傳入黑名單檢查卡號=>" + gate.cardNoMask);

		boolean blResult=false;
		if (getDouble("Db_Cnt") >0) {
			blResult=true;
		}


		return blResult;
	}
	
	//kevin: 取得風險分數 vipFactor
	/**
	 * 查詢是否卡戶是否為免照會VIP
	 * V1.00.45 P3免照會VIP只有定義在ID帳戶
	 * @return 如果檢核通過return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectCcaVip(String spAcctType) throws Exception {
		gb.showLogMessage("I","spAcctType(): started!");

		boolean blResult=false;
		daoTable  = addTableOwner("CCA_VIP");
		selectSQL = "START_DATE ,"
				  + "END_DATE ,"
				  + "WITH_SUP_CARD ";

		whereStr  = "WHERE acno_p_seqno = ?  and acct_type = ?"; 
		setString(1,getAcnoPSeqNo());
		setString(2,spAcctType);

		gb.showLogMessage("D","VIP名單檢查=>" + getAcnoPSeqNo() +"TYPE=" + spAcctType);

		selectTable();
		
		if ( "Y".equals(notFound)) {
			return blResult;
			}
		
		if (getValue("start_date").compareTo(gb.getSysDate())<=0 && gb.getSysDate().compareTo(getValue("end_date"))<=0) {
			if (gate.isSupCard) {
				if ("Y".equals(getValue("with_sup_card"))) {
					blResult=true; 
				}
				else {
					blResult=false; 
				}
			}
			else {
				blResult=true; 
			}
		}
		else {
			blResult=false; 
		}

		gb.showLogMessage("D","是否為VIP期間=>" + getValue("start_date") + '~' + gb.getSysDate() + '~' + getValue("start_date"));
		gb.showLogMessage("D","是否為VIP附卡=>" + getValue("with_sup_card"));
		gb.showLogMessage("D","是否為VIP名單=>" + blResult);

		return blResult;
	}
	
	/**
	 * 查詢對應之MCC風險分類檔CCA_MCC_RISK
	 * V1.00.57 MCC風險分類檔欄位整理
	 * @return 如果檢核通過return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean selectMccRisk() throws Exception {
		gb.showLogMessage("I","selectMccRisk() gate.mccCode="+gate.mccCode);
		//proc => TB_mcc_risk()
		daoTable  = addTableOwner("CCA_MCC_RISK");
		selectSQL = "NVL(RISK_TYPE,'Z') as MccRiskRiskType, "
				+ "MCC_CODE as MccRiskMccCODE,"
				+ "NVL(AMOUNT_RULE,'P') as MccRiskAmtRule,"
				+ "NVL(NCCC_FTP_CODE,'AP') as MccRiskNcccFtpCode,"
				+ "RISK_FACTOR as MccRiskFactor";
		whereStr  = "WHERE MCC_CODE = ? ";

		setString(1, gate.mccCode);

		gb.showLogMessage("D","MCC Code => " + gate.mccCode);

		selectTable();
		if ( "Y".equals(notFound) ) {
			gate.mccRiskType="R"; //若找不到 table data，則預設 RiskType 為 R 風險類別
			gate.mccRiskMccCode = gate.mccCode; 
			gate.mccRiskNcccFtpCode= "AP";        	 
			if (gate.cashAdvance) { 
				gate.mccRiskAmountRule ="C";
			}
			else {
				gate.mccRiskAmountRule ="P";
			}
			gate.mccRiskFactor = 0;
			insertMccRick(gate.mccCode, gate.mccRiskType);
			//and send email....
			return false; 
		
		}


		return true;
	}

	// CCS_POS_ENTRY Ѽ
//	public boolean selectPosEntryIbt() throws Exception {
//		gb.showLogMessage("I","selectPosEntryIbt(): started!");
//
//		daoTable  = addTableOwner("CCS_POS_ENTRY");
//		selectSQL = "REJECT_FLAG as POSENTRY_REJECT_FLAG ";
//		whereStr  = "WHERE GROUP_CODE = ? AND POS_ENTRY = ?";
//		setString(1, gate.groupCode);//0000
//		setString(2, gate.entryMode);//021  == isoField[22]
//		selectTable();
//
//		gb.showLogMessage("D","GroupCode:"+gate.groupCode + ",EntryMode:" + gate.entryMode+"---");       
//
//		if ( "Y".equals(notFound) ) {
//
//			return false; 
//		}
//
//		return true;
//	}

	// CCS_TRAIN_PARM  KѼ
//	public boolean selectTrainParm() throws Exception {
//		gb.showLogMessage("I","selectTrainParm(): started!");
//		//Howard:
//		return true;
//		/*
//        daoTable  = "CCA_TRAIN_PARM"; //howard: db2 table name T{
//        selectSQL = "MIN_TRANS_AMT as TRAIN_MIN_TRANS_AMT,"       // KPINB
//                + "MAX_MONTH_AMT as TRAIN_MAX_MONTH_AMT,"       // KPIN뭭B
//                + "MIN_DAY_AMT as TRAIN_MIN_DAY_AMT,"         // KPIN魭B
//                + "NOPIN_PASSWD_FLAG as TRAIN_NOPIN_PASSWD_FLAG";   // BHWPIN  0.K, 1.PVV, 2.ͤ
//        whereStr  = "WHERE MCHT_NO   = ? "
//                  + "AND   ACQ_ID    = ? "
//                  + "AND   TERM_IND  = ? ";
//        setString(1,"000000000000000");//2016/07/04: JH hard code
//        setString(2,"00000000");//2016/07/04: JH hard code
//        setString(3,"0000");//2016/07/04: JH hard code
//
//        selectTable();
//        if ( "Y".equals(notFound) )
//           { return false; }
//
//        return true;
//		 */
//	}

	// Ū CCA_SYS_PARM2
	public boolean selectSysParm2(String sysId,String sysKey, String spTargetFieldName) throws Exception {
		gb.showLogMessage("I","selectSysParm2(): "+ sysKey+" started!");

		daoTable  = addTableOwner("CCA_SYS_PARM2");
		selectSQL = spTargetFieldName;
		whereStr  = "WHERE  SYS_ID = ? AND SYS_KEY = ? ";
		setString(1, sysId);
		setString(2, sysKey);
		selectTable();
		if ( "Y".equals(notFound) )
		{ return false; }

		return true;
	}

	// Ū CCA_SYS_PARM2
	public boolean selectSysParm2(String sysId,String sysKey) throws Exception {
		gb.showLogMessage("I","selectSysParm2(): "+sysId+", "+ sysKey+") started!");

		daoTable  = addTableOwner("CCA_SYS_PARM2");
		selectSQL = "NVL(SYS_DATA1,' ') as SysParm2Data1," 
				+ "NVL(SYS_DATA2,' ')  as SysParm2Data2,"
				+ "NVL(SYS_DATA3,' ')  as SysParm2Data3";
		whereStr  = "WHERE  SYS_ID = ? AND SYS_KEY = ? ";
		setString(1, sysId);
		setString(2, sysKey);
		selectTable();
		if ( "Y".equals(notFound) )
		{ return false; }

		return true;
	}

	// PTR_CURRENT_RATE  Ѽ
	public boolean selectCurrentRate(String spCurrency) throws Exception {
		gb.showLogMessage("I","selectCurrentRate(): started!");
		//Howard(2017.06.29): lv{OŪ table Curr_Rate  (card_type and currency )Fs{Ū TABLE PTR_CURRENT_RATE 


		daoTable  = addTableOwner("PTR_CURR_RATE");
		selectSQL = "EXCHANGE_RATE"; 

		//whereStr  = "WHERE CURR_CODE = ?  order by MOD_TIME desc ";
		whereStr  = "where CURR_CODE=? and MOD_TIME= (select  MAX(MOD_TIME) as MaxModTime from PTR_CURR_RATE where CURR_CODE=?)";
		//where CURR_CODE='840' and MOD_TIME= (select  MAX(MOD_TIME) as MaxModTime from PTR_CURR_RATE where CURR_CODE='840')

		setString(1,spCurrency);
		setString(2,spCurrency);

		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 
		}

		//gb.showLogMessage("D","EXCHANGE_RATE=>" + getValue("EXCHANGE_RATE"));

		return true;
	}
	
	//kevin:Fisc國外交易時，利用此銀行提供匯率檔計算出台幣金額
	public boolean selectCcaCurrentRate(String spCurrency) throws Exception {
		gb.showLogMessage("I","selectCcaCurrentRate(): started!");

		daoTable  = addTableOwner("CCA_CURR_RATE");
		selectSQL = "RATE as CcaExchangeRate"; 

		whereStr  = "where CURRENCY=?";
		setString(1,spCurrency);

		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 
		}
		return true;
	}
	
	/**
	 * 取得VD交易是否符合Egov加圈手續費
	 * V1.00.23 特店編號取得手續費率，除原先4碼符合之外再新增8碼檢查
	 * @throws Exception if any exception occurred
	 */
	public boolean selectCcaMccEgovFee(String spMcc, String spMerchantNo) throws Exception {
		gb.showLogMessage("I","selectCcaMccEgovFee(): started!");

		daoTable  = addTableOwner("CCA_MCC_EGOV_FEE");
		selectSQL = "INT_MIN_AMT as CcaEgovFeeIntMinAmt, "
				  + "INT_MAX_AMT as CcaEgovFeeIntMaxAmt, "
				  + "INT_FIX_AMT as CcaEgovFeeIntFixAmt, "
				  + "INT_PERCENT as CcaEgovFeeRate,"
				  + "CNTRY_CODE  as CcaEgovFeeCntryCode "; 

		whereStr  = "where MCC_CODE=? and MCC_LINK_ID=? ";
		setString(1,spMcc);
		setString(2,spMerchantNo);

		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 
		}
		return true;
	}

	// CCA_SPEC_CODE 特殊指示戶原因代碼檔
	public boolean selectSpecCode(String spSpecCode) throws Exception {
		gb.showLogMessage("I","selectSpecCode(): started!");
		/*
	   SC_SPEC_CHECK_LEVEL: 作業指示
	   	if 作業指示=0  => 拒絕條件
	   	if 作業指示=1  => 直接回覆
	   	if 作業指示=2  => 額度內100% 可用
	   	if 作業指示=3  => 核准條件
		 * */
		daoTable  = addTableOwner("CCA_SPEC_CODE");
		selectSQL = "RESP_CODE as SC_SPEC_RESP_CODE,"    /* On Us 回覆碼  */
				+ "CHECK_LEVEL as SC_SPEC_CHECK_LEVEL,"  /* 作業指示碼 - Proc  */
				+ "CHECK_FLAG01 as SC_SPEC_CHECK_FLAG01,"  /* 作業指示 01 */
				+ "CHECK_FLAG02 as SC_SPEC_CHECK_FLAG02,"  /* 作業指示 02 */
				+ "CHECK_FLAG03 as SC_SPEC_CHECK_FLAG03,"  /* 作業指示 03 */
				+ "CHECK_FLAG04 as SC_SPEC_CHECK_FLAG04,"  /* 作業指示 04 */
				+ "CHECK_FLAG05 as SC_SPEC_CHECK_FLAG05,"  /* 作業指示 05 */
				+ "CHECK_FLAG06 as SC_SPEC_CHECK_FLAG06,"  /* 作業指示 06 */
				+ "JCB_REASON as SC_SPEC_JCB_REASON,"  /* JCB 原因代碼 */
				+ "VISA_REASON as SC_SPEC_VISA_REASON,"  /* VISA 原因代碼 */
				+ "MAST_REASON as SC_SPEC_MAST_REASON,"  /* MAST 原因代碼 */
				+ "SEND_IBM  as SC_SPEC_SEND_IBM,"       /* VD送主機參數 */
				+ "SPEC_CODE as SC_SPEC_CODE ";
		whereStr  = "WHERE SPEC_CODE = ? ";
		setString(1,spSpecCode);
		selectTable();

		//gb.showLogMessage("D","傳入SPEC_CODE=>" +sP_SpecCode);
		//gb.showLogMessage("D","RESP_CODE[On Us 回覆碼]=>" +getValue("SC_SPEC_RESP_CODE"));
		//gb.showLogMessage("D","CHECK_LEVEL[作業指示碼]=>" +getValue("SC_SPEC_CHECK_LEVEL"));
		//gb.showLogMessage("D","CHECK_FLAG01[作業指示 01]=>" +getValue("SC_SPEC_CHECK_FLAG01"));
		//gb.showLogMessage("D","CHECK_FLAG02[作業指示 02]=>" +getValue("SC_SPEC_CHECK_FLAG02"));
		//gb.showLogMessage("D","CHECK_FLAG03[作業指示 03]=>" +getValue("SC_SPEC_CHECK_FLAG03"));
		//gb.showLogMessage("D","CHECK_FLAG04[作業指示 04]=>" +getValue("SC_SPEC_CHECK_FLAG04"));
		//gb.showLogMessage("D","CHECK_FLAG05[作業指示 05]=>" +getValue("SC_SPEC_CHECK_FLAG05"));
		//gb.showLogMessage("D","CHECK_FLAG06[作業指示 06]=>" +getValue("SC_SPEC_CHECK_FLAG06"));

		if ( "Y".equals(notFound) ) {
			return false; 
		}

		return true;
	}

	// CCA_SPEC_DETL 特殊指示戶原因代碼明細檔
	//V1.00.19 調整特指戶判斷條件
	public boolean selectSpecDetl(String spSpecCode, String spDataType, String spDataCode, String spDataCode2) throws Exception {
		gb.showLogMessage("I","selectSpecDetl(): started!");

		daoTable  = addTableOwner("CCA_SPEC_DETL");
		selectSQL = "count(*) as SC_SPEC_DETL_COUNT ";    
		whereStr  = "WHERE SPEC_CODE = ?  and DATA_TYPE= ? and DATA_CODE=? ";
		if (!"".equals(spDataCode2))
			whereStr  += " and DATA_CODE2=? ";

		setString(1,spSpecCode);
		setString(2,spDataType);
		setString(3,spDataCode);    
		if (!"".equals(spDataCode2))
			setString(4,spDataCode2);    
		selectTable();

		gb.showLogMessage("D","傳入 SPEC_CODE=>" + spSpecCode + "傳入 DATA_TYPE=>" + spDataType+"傳入 DATA_CODE=>" + spDataCode);
		gb.showLogMessage("D","取得CCA_SPEC_DETL 資料筆數=>" + getInteger("SC_SPEC_DETL_COUNT"));

        if (getInteger("SC_SPEC_DETL_COUNT") == 0) {
        	return false; 
        }
        
		return true;
	}

	/*
   //this is  view
   public boolean getCardStatusFromCims() throws Exception {

	   boolean bL_Result = true;
	   //daoTable  = "cimsuser.VW_CARD_MAIN@cims_dblink";
	   //daoTable  = "cimsusr.VW_CARD_MAIN";
	   daoTable  = gb.CimsCardInfoTableName;

       selectSQL = "CARD_STATUS as cims_card_status, "
       		+ "GOOD_THRU as cims_expire_date_mmyy, SC_CVV as cims_sc_cvv, "
       		+ "SC_CVV2 as cims_sc_cvv2, SC_ICVV as cims_sc_icvv ";                      

       whereStr  = "WHERE CARD_NO = ? ";
       setString(1,gate.cardNo);
       selectTable();
       if ( "Y".equals(notFound) )
          { return false; }

       return true;


   }
	 */

	/*
    public  String getLastTransDate()throws Exception {

    	String sL_LastTransDate="";
        daoTable  = "CCS_CARD_ACCT";
        selectSQL = "LAST_CONSUME_DATE as PriorTransDate "
                    +", FN_AMT_MONTH, FN_AMT_DAY, FC_AMT_MONTH, FC_AMT_DAY, TRAIN_AMT_MONTH, TRAIN_AMT_DAY, TX_AMT_MONTH, TX_AMT_DAY, TX_CNT_MONTH,TX_CNT_DAY,FN_CNT_MONTH,FN_CNT_DAY,FC_CNT_MONTH,FC_CNT_DAY ";



        whereStr  = "WHERE P_SEQNO=? and ID_P_SEQNO = ?  ";
        setString(1,getValue("P_SEQNO"));
        setString(2,getValue("ID_P_SEQNO"));
        selectTable();
        if ( "Y".equals(notFound) ) {
        	sL_LastTransDate = gb.sysDate;
        	cardAcct_insert_ibt();
        }
        else
        	sL_LastTransDate = getValue("PriorTransDate");




    	return sL_LastTransDate;

    }
	 */
	/*
   // Ū CCS_DEBIT_PARM
   public boolean selectDebitParm_Ibt() throws Exception {
       gate.debitFee =0;
       gate.debitMakup =1;
       //-ꤺ-
       if ("T".equals(gate.areaType)) {
    	   return true;
       }

        daoTable  = "CCS_DEBIT_PARM";
        selectSQL = "nvl(WITHDRAW_FEE,0) as debit_fee,"    // ~ĥdO
                  + "nvl(MARKUP,1) as debit_makup";        // ײvഫv
        whereStr  = "WHERE BIN_NO = ? ";
        setString(1,gate.binNo);
        selectTable();
        if ( "Y".equals(notFound) ) {
        	return true;

        }

        gate.debitFee = getDouble("debit_fee");
        gate.debitMakup =getDouble("debit_makup");
        if (gate.debitMakup==0) {
        	gate.debitMakup =1;
        }

        return true;


    }
	 */
	// Ū AUTH_TXLOG
	public boolean selectPriorAuthTxLog4PreAuth() throws Exception {
		gb.showLogMessage("I","selectPriorAuthTxLog4PreAuth(): started!");

		daoTable  = addTableOwner("CCA_AUTH_TXLOG");
		//selectSQL = "NT_AMT as SRC_NT_AMT ,AUTH_SEQNO as SRC_AUTH_SEQNO ,BANK_TX_AMT as SRC_BANK_TX_AMT," 
		selectSQL = "NT_AMT as SRC_NT_AMT ,"
				+ " ACNO_P_SEQNO as PRIOR_TX_ACNO_P_SEQNO,ID_P_SEQNO as PRIOR_TX_ID_P_SEQNO ";



		whereStr = "WHERE CARD_NO = ? "
				+ "AND AUTH_NO = ? "
				//+ "AND MCHT_NO = ? "

				+ "AND ISO_RESP_CODE = '00' "
				//+ "AND AUTH_STATUS_CODE = '00' "

				//+ "AND LOGIC_DEL = ?";
				+ "AND AUTH_TYPE = ?";
		//+" and DEBT_FLAG='N' and UNLOCK_FLAG='N' "; Howard(0714) 不確定是否要用此條件
		setString(1, gate.cardNo);
		//setString(2, gate.isoField[38]);
		setString(2, gate.oriAuthNo);
		//setString(3, gate.merchantNo);
		setString(3, "X"); //preAuth  auth_type flag




		selectTable();
		if ( "Y".equals(notFound) ) {
			return false; 

		}

		//gate.src_bank_tx_seqno = getValue("SRC_BANK_TX_SEQNO");
		return true;
	}

	/*
	public void updatePriorOfPriorTx_ibt() throws Exception {
		daoTable  = "CCA_AUTH_TXLOG";
		updateSQL = "BANK_TX_SEQNO = ?, UNLOCK_FLAG=? ";
		whereStr  = "WHERE AUTH_SEQNO = ? ";

		setString(1, gate.bank_tx_seqno);
		setString(2, "N");
		setString(3, getValue("PriorOfPrior_AUTH_SEQNO"));

		int  cnt = updateTable();

		//update
	}
	 */
	//讀取 AUTH_TXLOG : 當執行 reversal 交易時，會用到此 function
//	public boolean getPriorOfPriorInfo4PreAuthCompTxIbt(String spCardNo, String spMerchantNo, String spAuthNo, double dlIsoField4Value) throws Exception{
//		gb.showLogMessage("I","getPriorOfPriorInfo4PreAuthCompTxIbt(): started!");
//
//
//		daoTable  = addTableOwner("CCS_AUTH_TXLOG");
//		selectSQL = "BANK_TX_AMT as PriorOfPrior_BANK_TX_AMT,"
//				+"MCHT_NAME as PriorOfPrior_MCHT_Name,"
//				+"ORI_AUTH_NO as PriorOfPrior_ORI_AUTH_NO,"
//				+"AUTH_SEQNO as PriorOfPrior_AUTH_SEQNO";
//
//
//
//		whereStr = "WHERE CARD_NO = ? "
//				+ "AND AUTH_NO = ? "
//				+ "AND MCHT_NO = ? "
//				+ "AND ISO_RESP_CODE = '00' "
//				//+ "AND LOGIC_DEL = ?";
//				+ "AND AUTH_TYPE = ?";
//		//+" and DEBT_FLAG='N' and UNLOCK_FLAG='N' "; Howard(0714) 不確定是否要用此條件
//		setString(1, spCardNo);
//		setString(2, spAuthNo);
//		setString(3, spMerchantNo);
//		setString(4, "X"); //preAuth  auth_type flag
//
//
//
//
//		selectTable();
//		if ( "Y".equals(notFound) )
//		{ return false; }
//
//		//gate.src_bank_tx_seqno = getValue("SRC_BANK_TX_SEQNO");
//		return true;
//
//	}
//
//	//讀取 AUTH_TXLOG : 當執行 reversal 交易時，會用到此 function
//	public boolean getPriorOfPriorInfo4AdjTxIbt(String spCardNo, String spMerchantNo, String spAuthNo, double dlIsoField4Value) throws Exception{
//		gb.showLogMessage("I","getPriorOfPriorInfo4AdjTxIbt(): started!");
//		boolean blResult = true;
//		daoTable  = addTableOwner("CCS_AUTH_TXLOG"); 
//		selectSQL = "BANK_TX_AMT as PriorOfPrior_BANK_TX_AMT,"
//				+"MCHT_NAME as PriorOfPrior_MCHT_Name,"
//				+"AUTH_SEQNO as PriorOfPrior_AUTH_SEQNO";
//
//		whereStr ="where card_no =?"
//				+" and mcht_no =? and auth_no =?"
//				+" and nt_amt=? " 
//				+" and DEBT_FLAG='N' and UNLOCK_FLAG='Y' ";
//		//+" and tx_date >=to_char(sysdate - ?,'yyyymmdd')";
//
//		/*
//  	 DEBT_FLAG               
//  	 UNLOCK_FLAG             
//  	 有任何一個是Y就表示已經解圈
//
//		 */
//
//		setString(1,spCardNo);
//		setString(2,spMerchantNo);
//		//setString(3,gate.isoField[38].trim());
//		setString(3,spAuthNo);
//
//		//setDouble(4, gate.nt_amt);
//		setDouble(4, dlIsoField4Value);
//		//setInt(5,li_days);
//
//		selectTable();
//		if ( "Y".equals(notFound) ) {
//
//			return false;
//		}
//
//
//
//
//		return blResult;
//	}
	// 讀取 AUTH_TXLOG
//	public boolean selectAuthTxLog4ReversalIbt() throws Exception {
//		gb.showLogMessage("I","selectAuthTxLog4ReversalIbt(): started!");
//
//		daoTable  = addTableOwner("CCS_AUTH_TXLOG");
//		//selectSQL = "NT_AMT as SRC_NT_AMT ,AUTH_SEQNO as SRC_AUTH_SEQNO, "
//		selectSQL = "NT_AMT as SRC_NT_AMT , "
//				+ "MCHT_NAME as SRC_MCHT_NAME, AUTH_TYPE as SRC_AUTH_TYPE, ORI_AMT as SRC_ORI_AMT, "
//				+ "BANK_TX_AMT as SRC_BANK_TX_AMT,BANK_TX_SEQNO as SRC_BANK_TX_SEQNO, "
//				+ "CARD_NO as SRC_CARD_NO, MCHT_NO as SRC_MCHT_NO, ORI_AUTH_NO as SRC_ORI_AUTH_NO ";
//		//
//		whereStr = "WHERE REF_NO = ?  AND ISO_RESP_CODE = '00' AND ( REVERSAL_FLAG IS NULL or REVERSAL_FLAG=? ) and cacu_amount=? and unlock_flag=? ";
//		setString(1, gate.refNo);//用卡號,P37 and 金額
//		setString(2, "N");
//		setString(3, "Y");
//		setString(4, "N");
//
//
//		selectTable();
//		if ( "Y".equals(notFound) ) {
//			gb.showLogMessage("I","function: TA.selectAuthTxLog4Reversal -- can not find data. RefNo is  "+gate.refNo + "--");
//			return false; 
//		}
//
//		//gate.src_bank_tx_seqno = getValue("SRC_BANK_TX_SEQNO");
//
//		return true;
//	}
//
//	// CCS_PTR_MSGID 簡訊參數
//	public boolean selectPtrMsgidIbt(String msgType) throws Exception {
//		gb.showLogMessage("I","selectPtrMsgidIbt(): started!");
//
//		daoTable  = addTableOwner("CCS_PTR_MSGID");
//		selectSQL = "MSG_TYPE,"     // 簡訊種類
//				+ "MSG_ID,"       // 簡訊代碼
//				+ "MSG_CONTENT,"  // 簡訊內容
//				+ "SMS_FLAG,"     // 簡訊旗標
//				+ "EMAIL_FLAG,"   // email 旗標
//				+ "APP_FLAG,"     // APP 旗標
//				+ "MSG_AMT, "     // 簡訊金額
//				+ "USE_FLAG ";     // 使用期標 => equals"Y" 才要發簡訊
//		whereStr  = "WHERE MSG_TYPE = ? ";
//		setString(1, msgType);
//		selectTable();
//		if ( "Y".equals(notFound) )
//		{ return false; }
//
//		return true;
//	}

	/**
	 * 新增CCA_MSG_LOG 消費簡訊紀錄
	 * V1.00.42 授權系統與DB連線交易異常時的處理改善方式
	 * @return
	 * @throws Exception
	 */
	public void insertMsgEven(String spMsgId, String spMsgType, String spCellPhoneNo, String spSmsContent) throws Exception {
		gb.showLogMessage("I","insertMsgEven(): started!");

		//daoTable = "CCS_MSG_EVEN"; //for ibt
		daoTable = addTableOwner("CCA_MSG_LOG");

		//gb.showLogMessage("D","---" +gate.txDate + "===");
		//gb.showLogMessage("D","---" +gate.txTime + "===");
		//gb.showLogMessage("D","---" +gate.cardNoMask + "===");
		//gb.showLogMessage("D","---" +gate.isoField[38] + "===");
		//gb.showLogMessage("D","---" +getValue("CardBaseCardAcctIdx") + "===");
		//gb.showLogMessage("D","---" +getPSeqNo() + "===");
		//gb.showLogMessage("D","---" +getValue("CardBaseIdPSeqNo") + "===");
		//gb.showLogMessage("D","---" +gate.merchantNo + "===");
		//gb.showLogMessage("D","---" +gate.isoField[22] + "===");
		//gb.showLogMessage("D","---" +gate.mccRiskType + "===");
		//gb.showLogMessage("D","---" +gate.isoFiled4Value + "===");
		//gb.showLogMessage("D","---" +gate.ClassCode + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoBirthday") + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoChiName") + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoEngName") + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoHomeTelNo1") + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoOfficeTelNo1") + "===");
		//gb.showLogMessage("D","---" +getValue("CrdIdNoCellPhone") + "===");
		//gb.showLogMessage("D","---" +getValue("SC_SPEC_RESP_CODE") + "===");
		//gb.showLogMessage("D","---" +gate.mesgType + "===");

		setValue("TX_DATE",gate.txDate);
		setValue("TX_TIME",gate.txTime);
		setValue("CARD_NO",gate.cardNo);
		setValue("AUTH_NO",gate.isoField[38]);

		setValue("TRANS_TYPE",gate.mesgType); //

		setValueDouble("CARD_ACCT_IDX", Double.parseDouble(getValue("CardBaseCardAcctIdx")));
		setValue("P_SEQNO", getPSeqNo());
		setValue("ID_P_SEQNO", getValue("CardBaseIdPSeqNo"));
		setValue("ACNO_P_SEQNO", getValue("CardBaseAcnoPSeqNo"));


		setValue("MCHT_NO",gate.merchantNo);
		setValue("MSG_TYPE",spMsgType); 
		setValue("MSG_ID",spMsgId);

		//ENTRY_MODE欄位部顯示，借用給新簡訊邏輯的priority使用
//		setValue("ENTRY_MODE",gate.isoField[22]);
		setValue("ENTRY_MODE",gate.smsPriority);
		setValue("RISK_TYPE",gate.mccRiskType); //風險類別



		setValueDouble("TRANS_AMT",gate.isoFiled4Value);

		setValue("CLASS_CODE", gate.classCode); /* 卡戶等級       */

		setValue("BIRTHDAY", getValue("CrdIdNoBirthday"));
		setValue("CHI_NAME", getValue("CrdIdNoChiName"));
		setValue("ENG_NAME", getValue("CrdIdNoEngName"));
		setValue("TEL_NO_H", getValue("CrdIdNoHomeTelNo1"));
		setValue("TEL_NO_O", getValue("CrdIdNoOfficeTelNo1"));
		setValue("CELLAR_PHONE", spCellPhoneNo);
		setValue("ISO_RESP_CODE", gate.isoField[39]); //ISO回覆碼
		setValue("INST_PERIOD", "0"); //分期期數 => Howard: 新系統用不到這個欄位
		setValue("SMS_CONTENT", spSmsContent); //v1.00.05 新增簡訊內容在簡訊log中


		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_USER", ConstObj.CRT_USER);//放固定值 =>表示是自動授權程式寫入的 data
		setValue("MOD_PGM", gb.getSystemName());//放固定值 =>表示是自動授權程式寫入的 data

		setValue("PROC_CODE","");






		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		insertTable();
		return;
	}
	
	// 更新 CCA_MSG_LOG
	public boolean updateMsgEven(String spMitakeId, String spStatus) throws Exception {
		gb.showLogMessage("I","updateMsgEven(): started!");
		
		boolean blResult = true;
		daoTable  = addTableOwner("CCA_MSG_LOG");
		updateSQL = "SEND_DATE = ?,"
				  + "APPR_PWD = ?,"
				  + "PROC_CODE = ?";
		whereStr  = "WHERE TX_DATE = ? AND CARD_NO = ? AND AUTH_NO = ?";
		setString(1,gate.txDate);
		setString(2,spMitakeId);
		setString(3,spStatus);
		
		setString(4,gate.txDate);
		setString(5,gate.cardNo);
		setString(6,gate.authNo);
		int  cnt = updateTable();
		if (cnt== 0)
			blResult = false;

		return blResult;

	}
	
	// 新增 CCA_IMS_LOG
	public void insertImsEven(String spImsData, boolean bpProcCode) throws Exception {
		gb.showLogMessage("I","insertImsEven(): started!");

		//daoTable = "CCA_IMS_LOG"; //for TCB
		daoTable = addTableOwner("CCA_IMS_LOG");

		//gb.showLogMessage("D","---" +gate.txDate + "===");
		//gb.showLogMessage("D","---" +gate.txTime + "===");
		//gb.showLogMessage("D","---" +gate.cardNoMask + "===");

		setValue("TX_DATE",gate.txDate);
		setValue("TX_TIME",gate.txTime);
		setValue("CARD_NO",gate.cardNo);
		setValue("AUTH_NO",gate.isoField[38]);
		setValue("TRANS_TYPE",gate.mesgType); //
		setValueDouble("CARD_ACCT_IDX", Double.parseDouble(getValue("CardBaseCardAcctIdx")));
		setValue("P_SEQNO", getPSeqNo());
		setValue("ACNO_P_SEQNO", getValue("CardBaseAcnoPSeqNo"));
		setValue("IMS_REVERSAL_DATA",spImsData.substring(1, spImsData.length())); 
		setValue("IMS_RESP_CODE", gate.bankBit39Code);
		setValue("IMS_SEQ_NO", gate.vdTxnSeqNo);
		setValueDouble("TRANS_AMT",gate.isoFiled4Value);
		setValue("ISO_RESP_CODE", gate.authErrId); // 回覆碼
		setValue("CRT_DATE",gb.getSysDate());
		setValue("CRT_USER", ConstObj.CRT_USER);//放固定值 =>表示是自動授權程式寫入的 data
		setValue("MOD_PGM", gb.getSystemName());//放固定值 =>表示是自動授權程式寫入的 data
		if (bpProcCode) {
			setValue("PROC_CODE", "Y");
		}
		else {
			setValue("PROC_CODE", "N");
		}

		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		insertTable();
		return;
	}

	// RESET CCS_CARD_ACCT
	public boolean resetCardAcct(String actionCode) throws Exception {
		//Howard: 功能類似於 proc.TB_card_acct(2,...)
		gb.showLogMessage("I","resetCardAcct : " + actionCode);

		daoTable  = addTableOwner("CCA_CARD_ACCT");
		if ( "M".equals(actionCode) ) {
			updateSQL = "LAST_CONSUME_DATE = ?,"
					+ "TX_AMT_MONTH = 0,"
					+ "TX_CNT_MONTH = 0,"
					+ "TX_AMT_DAY = 0,"
					+ "TX_CNT_DAY = 0,"
					+ "REJ_AUTH_CNT_DAY = 0,"
					+ "REJ_AUTH_CNT_MONTH = 0,"
					+ "BANK_MM_AMT = 0,"
					+ "BANK_MM_CNT = 0,"
					+ "BANK_DD_AMT = 0,"
					+ "BANK_DD_CNT = 0,"
					+ "BANK_REJ_MM_CNT = 0,"
					+ "FN_AMT_MONTH = 0,"
					+ "FN_CNT_MONTH = 0,"
					+ "FN_AMT_DAY = 0,"
					+ "FN_CNT_DAY = 0,"
					+ "FC_AMT_MONTH = 0,"
					+ "FC_CNT_MONTH = 0,"
					+ "FC_AMT_DAY = 0,"
					+ "FC_CNT_DAY = 0,"
					+ "TRAIN_AMT_DAY = 0,"
					+ "TRAIN_AMT_MONTH = 0";
		}
		else {
			updateSQL = "LAST_CONSUME_DATE = ?,"
					+ "TX_AMT_DAY  = 0,"
					+ "TX_CNT_DAY  = 0,"
					+ "BANK_DD_AMT = 0,"
					+ "BANK_DD_CNT = 0,"
					+ "FN_AMT_DAY  = 0,"
					+ "FN_CNT_DAY  = 0,"
					+ "FC_AMT_DAY  = 0,"
					+ "FC_CNT_DAY  = 0,"
					+ "TRAIN_AMT_DAY = 0";
		}

		whereStr  = "WHERE P_SEQNO = ? AND ID_P_SEQNO = ? ";
		setString(1, gb.getSysDate());
		setString(2, getPSeqNo());
		setString(3, getValue("ID_P_SEQNO"));
		/*
        setString(2, getValue("acct.p_seqno"));
        setString(3, getValue("acct.ID_P_SEQNO"));
		 */
		int  cnt = updateTable();
		if ( cnt == 0  ) {
			return false;
		}
		return true;
	}

	public boolean resetCcaConsume(String actionCode) throws Exception {
		//Howard: 功能類似於 proc.TB_card_acct(2,...)
		gb.showLogMessage("I","resetCcaConsume : "+actionCode);

		daoTable  = addTableOwner("CCA_CONSUME");
		if ( "M".equals(actionCode) ) {
			updateSQL = "TX_TOT_AMT_MONTH = 0,"
					+ "TX_TOT_CNT_MONTH = 0,"
					+ "TX_TOT_AMT_DAY = 0,"
					+ "TX_TOT_CNT_DAY = 0,"
					+ "REJ_AUTH_CNT_DAY = 0,"
					+ "REJ_AUTH_CNT_MONTH = 0,"
					/*
					+ "BANK_MM_AMT = 0,"
					+ "BANK_MM_CNT = 0,"
					+ "BANK_DD_AMT = 0,"
					+ "BANK_DD_CNT = 0,"
					+ "BANK_REJ_MM_CNT = 0,"
					 */
					 + "FN_TOT_AMT_MONTH = 0,"
					 + "FN_TOT_CNT_MONTH = 0,"
					 + "FN_TOT_AMT_DAY = 0,"
					 + "FN_TOT_CNT_DAY = 0,"
					 + "FC_TOT_AMT_MONTH = 0,"
					 + "FC_TOT_CNT_MONTH = 0,"
					 + "FC_TOT_AMT_DAY = 0,"
					 + "FC_TOT_CNT_DAY = 0,"
					 + "TRAIN_TOT_AMT_DAY = 0,"
					 + "TRAIN_TOT_AMT_MONTH = 0";
		}
		else {
			updateSQL = "TX_TOT_AMT_DAY  = 0,"
					+ "TX_TOT_CNT_DAY  = 0,"
					/*
					+ "BANK_DD_AMT = 0,"
					+ "BANK_DD_CNT = 0,"
					 */
					 + "FN_TOT_AMT_DAY  = 0,"
					 + "FN_TOT_CNT_DAY  = 0,"
					 + "FC_TOT_AMT_DAY  = 0,"
					 + "FC_TOT_CNT_DAY  = 0,"
					 + "TRAIN_TOT_AMT_DAY = 0";
		}
		whereStr  = "WHERE CARD_ACCT_IDX = ? ";
		setString(1, gate.cardAcctIdx);
		/*
		whereStr  = "WHERE P_SEQNO = ? AND ID_P_SEQNO = ? ";
		setString(1, gb.sysDate);
		setString(2, getPSeqNo());
		setString(3, getValue("ID_P_SEQNO"));
		 */
		/*
        setString(2, getValue("acct.p_seqno"));
        setString(3, getValue("acct.ID_P_SEQNO"));
		 */
		int  cnt = updateTable();
		if ( cnt == 0  ) {
			return false;
		}
		return true;
	}

	/*
   public int updateCardAcct() throws Exception {
	   int nL_Result = 0;

	   return nL_Result;
   }
	 */   
	// 更新 CCS_CARD_ACCT: 消費累計
	public int updateCardAcct() throws Exception {
		gb.showLogMessage("I","updateCardAcct(): started!");

		int liCnt=0;

		daoTable  = addTableOwner("CCA_CARD_ACCT");

		if ( !"00".equals(gate.isoField[39]) ) {
			updateSQL = "REJ_AUTH_CNT_MONTH = REJ_AUTH_CNT_MONTH + 1,"
					  + "REJ_AUTH_CNT_DAY = REJ_AUTH_CNT_DAY + 1";
			whereStr = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";

			setString(1,getPSeqNo());
			setString(2,getValue("ID_P_SEQNO"));
			/*
          setString(1,getValue("acct.P_SEQNO"));
          setString(2,getValue("acct.ID_P_SEQNO"));
			 */
			gb.showLogMessage("W","(1) CCS_CARD_ACCT UPDATE "+ "KEY -- "+ getPSeqNo()+" : "+getValue("ID_P_SEQNO"));
			liCnt = updateTable();
			/*if ( li_cnt == 0 )
             { gb.showLogMessage("W","(1) CCS_CARD_ACCT UPDATE NOT_FOUND ","KEY -- "+getValue("P_SEQNO")+" : "+getValue("ID_P_SEQNO")); }*/
			return liCnt;
		}

		int i=1;
		StringBuffer updBuffer = new StringBuffer();
		updBuffer.append("LAST_CONSUME_DATE = ?,");
		setString(i,gb.getSysDate()); i++;

		//gate.resetCardAcctMonthData = false;
		//gate.resetCardAcctDayData = false;// abcd test...

		// 累積月消費額
		if (gate.resetCcaConsumeMonthData) {
			//if (gate.resetCardAcctMonthData) {
			updBuffer.append("TX_CNT_MONTH = ?,");
			updBuffer.append("TX_AMT_MONTH = ?,");

			setInt(i,1);       
			i++;
			if (gate.balanceAmt<=0)
				setDouble(i,0);
			else
				setDouble(i,gate.balanceAmt); 
			i++;
		}
		else {
			updBuffer.append("TX_CNT_MONTH = TX_CNT_MONTH + ?,");
			setInt(i,gate.transCnt);	i++;

			if ((getDouble("TX_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("TX_AMT_MONTH = ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TX_AMT_MONTH = TX_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

		}




		if (( gate.cashAdvance ) || (gate.cashAdjust)) {

			updBuffer.append("FC_CNT_MONTH = FC_CNT_MONTH + ?,");
			setInt(i,gate.transCnt);	i++;
			updBuffer.append("FC_CNT_DAY = FC_CNT_DAY + ?,");
			setInt(i,gate.transCnt);       i++;

			if ((getDouble("FC_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("FC_AMT_MONTH =  ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("FC_AMT_MONTH = FC_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

			if ((getDouble("FC_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("FC_AMT_DAY =  ?,");
				setDouble(i,0); i++;

			}
			else {
				updBuffer.append("FC_AMT_DAY = FC_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

		if ( gate.speedTrain ) {
			if ((getDouble("TRAIN_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("TRAIN_AMT_MONTH =  ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TRAIN_AMT_MONTH = TRAIN_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

			if ((getDouble("TRAIN_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("TRAIN_AMT_DAY = ?,");
				setDouble(i,0); i++;        		
			}
			else {
				updBuffer.append("TRAIN_AMT_DAY = TRAIN_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

		// 累積日消費額
		if (gate.resetCcaConsumeDayData) {
			//if (gate.resetCardAcctDayData) {
			updBuffer.append("TX_CNT_DAY =  ?,");
			updBuffer.append("TX_AMT_DAY =  ?,");
			setInt(i,1);       
			i++;
			if (gate.balanceAmt<=0)
				setDouble(i,0);
			else
				setDouble(i,gate.balanceAmt); 

			i++;

		}
		else {
			updBuffer.append("TX_CNT_DAY = TX_CNT_DAY + ?,");
			setInt(i,gate.transCnt);		i++;


			if ((getDouble("TX_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("TX_AMT_DAY = ?,");

				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TX_AMT_DAY = TX_AMT_DAY + ?,");

				setDouble(i,gate.balanceAmt); i++;
			}
		}


		//-國外交易-
		if ( "F".equals(gate.areaType) ) {
			updBuffer.append("FN_CNT_DAY = FN_CNT_DAY + ?,");
			setInt(i,gate.transCnt);       i++;

			if ((getDouble("FN_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("FN_AMT_DAY =  ?,");

				//setDouble(i,gate.transAmount); i++;
				setDouble(i,0); i++;

			}
			else {
				updBuffer.append("FN_AMT_DAY = FN_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

		updBuffer.append("MOD_TIME = TO_DATE(?,'YYYYMMDDHH24MISS')");
		setString(i,gb.getSysDate()+gb.getSysTime()); i++;

		updateSQL =  updBuffer.toString();
		whereStr  = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";

		setString(i,getPSeqNo());  i++;
		setString(i,getValue("ID_P_SEQNO"));

		/*
       setString(i,getValue("acct.p_seqno"));  i++;
       setString(i,getValue("acct.ID_P_SEQNO"));
		 */
		gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - SQL -- "+updateSQL);
		gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - KEY -- "+gate.balanceAmt+" : "+ getPSeqNo() +" : "+getValue("ID_P_SEQNO"));
		liCnt = updateTable();
		/*if ( li_cnt == 0 )
           { gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE NOT_FOUND - ","SQL -- "+updateSQL);
          	  gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE NOT_FOUND - ","KEY -- "+gate.balanceAmt+" : "+getValue("p_seqno")+" : "+getValue("ID_P_SEQNO")); }*/
		return liCnt ;

		/*原來的code..
       daoTable  = "CCS_CARD_ACCT";

       // 最後消費日期
       String  lastTxDate = getValue("LAST_CONSUME_DATE");

       if ( !"00".equals(gate.isoField[39]) )
          {
            if ( gb.sysDate.substring(0,6).equals(lastTxDate.substring(0,6)) )
               { updateSQL = "REJ_AUTH_CNT_MONTH = REJ_AUTH_CNT_MONTH + ?"; }
            else
               { updateSQL = "REJ_AUTH_CNT_MONTH = ?"; }
            whereStr = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";
            setInt(1,gate.transCnt);
            setString(2,getValue("P_SEQNO"));
            setString(3,getValue("ID_P_SEQNO"));
            int cnt = updateTable();
            return cnt;
          }

       int i=1;
       StringBuffer updBuffer = new StringBuffer();
       updBuffer.append("LAST_CONSUME_DATE = ?,");
       setString(i,gb.sysDate); i++;

       // 累積月消費額
       updBuffer.append("TX_CNT_MONTH = TX_CNT_MONTH + ?,");
       updBuffer.append("TX_AMT_MONTH = TX_AMT_MONTH + ?,");
       setInt(i,gate.transCnt);       i++;
       setDouble(i,gate.transAmount); i++;

       if ( "F".equals(gate.areaType) )
          {
            updBuffer.append("FN_CNT_MONTH = FN_CNT_MONTH + ?,");
            updBuffer.append("FN_AMT_MONTH = FN_AMT_MONTH + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.cashAdvance )
          {
            updBuffer.append("FC_CNT_MONTH = FC_CNT_MONTH + ?,");
            updBuffer.append("FC_AMT_MONTH = FC_AMT_MONTH + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.speedTrain )
          {
            updBuffer.append("TRAIN_AMT_MONTH = TRAIN_AMT_MONTH + ?,");
            setDouble(i,gate.transAmount); i++;
          }

       // 累積日消費額
       updBuffer.append("TX_CNT_DAY = TX_CNT_DAY + ?,");
       updBuffer.append("TX_AMT_DAY = TX_AMT_DAY + ?,");
       setDouble(i,gate.transCnt);    i++;
       setDouble(i,gate.transAmount); i++;

       if ( "F".equals(gate.areaType) )
          {
            updBuffer.append("FN_CNT_DAY = FN_CNT_DAY + ?,");
            updBuffer.append("FN_AMT_DAY = FN_AMT_DAY + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.cashAdvance )
          {
            updBuffer.append("FC_CNT_DAY = FC_CNT_DAY + ?,");
            updBuffer.append("FC_AMT_DAY = FC_AMT_DAY + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.speedTrain )
          {
            updBuffer.append("TRAIN_AMT_DAY = TRAIN_AMT_DAY + ?,");
            setDouble(i,gate.transAmount); i++;
          }

       updBuffer.append("MOD_TIME = TO_DATE(?,'YYYYMMDDHH24MISS')");
       setString(i,gb.sysDate+gb.sysTime); i++;

       updateSQL =  updBuffer.toString();
       whereStr  = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";

       setString(i,getValue("P_SEQNO"));  i++;
       setString(i,getValue("ID_P_SEQNO"));

       int    cnt2 = updateTable();
       return cnt2 ;
       原來的code*/
	}


	// 更新 CCA_CONSUME: 消費累計
	public int updateCcaConsume() throws Exception {

		int liCnt=0;
		gb.showLogMessage("I","updateCcaConsume="+getDouble("CcaConsumeTxTotAmtMonth"));

		daoTable  = addTableOwner("CCA_CONSUME");

		gb.showLogMessage("D","updateCcaConsume_更新前金額="+getDouble("CcaConsumeTxTotAmtMonth"));
		gb.showLogMessage("D","updateCcaConsume_更新金額="+gate.balanceAmt);
		gb.showLogMessage("D","updateCcaConsume_更新後金額="+getDouble("CcaConsumeTxTotAmtMonth") + gate.balanceAmt);

		int i=1;
		StringBuffer updBuffer = new StringBuffer();
		// 因交易失敗而重製日(月)相關累積消費次數與金額	
		if ( !"00".equals(gate.isoField[39]) ) {
			if (gate.resetCcaConsumeMonthData) {
				updBuffer.append("REJ_AUTH_CNT_MONTH = ?,");
				setInt(i,1);       i++;
				updBuffer.append("REJ_AUTH_CNT_DAY = ?,");
				setInt(i,1);       i++;
				updBuffer.append("TX_TOT_CNT_MONTH = ?,");
				setInt(i,0);       i++;
				updBuffer.append("TX_TOT_CNT_DAY = ?,");
				setInt(i,0);       i++;
				updBuffer.append("FC_TOT_CNT_MONTH = ?,");
				setInt(i,0);       i++;
				updBuffer.append("FC_TOT_CNT_DAY = ?,");
				setInt(i,0);       i++;
				updBuffer.append("FN_TOT_CNT_DAY = ?,");
				setInt(i,0);       i++;
				updBuffer.append("TX_TOT_AMT_MONTH = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("TX_TOT_AMT_DAY = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("FC_TOT_AMT_MONTH = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("FC_TOT_AMT_DAY = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("TRAIN_TOT_AMT_MONTH = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("TRAIN_TOT_AMT_DAY = ?,");
				setDouble(i,0);    i++;
				updBuffer.append("FN_TOT_AMT_DAY = ?,");
				setDouble(i,0);    i++;
			}
			else {
				// 因交易失敗而重製日相關累積消費次數與金額	
				if (gate.resetCcaConsumeDayData) {
					updBuffer.append("REJ_AUTH_CNT_DAY = ?,");
					setInt(i,1);       i++;
					updBuffer.append("TX_TOT_CNT_DAY = ?,");
					setInt(i,0);       i++;
					updBuffer.append("FC_TOT_CNT_DAY = ?,");
					setInt(i,0);       i++;
					updBuffer.append("FN_TOT_CNT_DAY = ?,");
					setInt(i,0);       i++;
					updBuffer.append("TX_TOT_AMT_DAY = ?,");
					setDouble(i,0);    i++;
					updBuffer.append("FC_TOT_AMT_DAY = ?,");
					setDouble(i,0);    i++;
					updBuffer.append("TRAIN_TOT_AMT_DAY = ?,");
					setDouble(i,0);    i++;
					updBuffer.append("FN_TOT_AMT_DAY = ?,");
					setDouble(i,0);    i++;
			}
				// 因交易失敗相關累積拒絕次數	
				else {
					updBuffer.append("REJ_AUTH_CNT_DAY = REJ_AUTH_CNT_DAY + ?,");
					setInt(i,gate.transCnt);		i++;
					updBuffer.append("REJ_AUTH_CNT_MONTH = REJ_AUTH_CNT_MONTH + ?,");
					setInt(i,gate.transCnt);		i++;

				}
			}
		}
		else
			// 因交易成功而重製日(月)相關累積消費次數與金額	
			if (gate.resetCcaConsumeMonthData) {
				updBuffer.append("REJ_AUTH_CNT_MONTH = ?,");
				setInt(i,0);       i++;
				updBuffer.append("REJ_AUTH_CNT_DAY = ?,");
				setInt(i,0);       i++;
				updBuffer.append("TX_TOT_CNT_MONTH = ?,");
				setInt(i,1);       i++;
				updBuffer.append("TX_TOT_CNT_DAY = ?,");
				setInt(i,1);       i++;
				updBuffer.append("TX_TOT_AMT_MONTH = ?,");
				if (gate.balanceAmt<=0)
					setDouble(i,0);
				else
					setDouble(i,gate.balanceAmt); 
				i++;
				updBuffer.append("TX_TOT_AMT_DAY = ?,");
				if (gate.balanceAmt<=0)
					setDouble(i,0);
				else
					setDouble(i,gate.balanceAmt); 
				i++;

				//預借現金相關日(月)次數金額重製
				if (( gate.cashAdvance ) || (gate.cashAdjust)) {
					updBuffer.append("FC_TOT_CNT_MONTH = ?,");
					setInt(i,1);	i++;
					updBuffer.append("FC_TOT_CNT_DAY = ?,");
					setInt(i,1);    i++;
					if (gate.balanceAmt<=0) {
						updBuffer.append("FC_TOT_AMT_MONTH =  ?,");
						setDouble(i,0); i++;
						updBuffer.append("FC_TOT_AMT_DAY =  ?,");
						setDouble(i,0); i++;
					}
					else {
						updBuffer.append("FC_TOT_AMT_MONTH =  ?,");
						setDouble(i,gate.balanceAmt); i++;
						updBuffer.append("FC_TOT_AMT_DAY =  ?,");
						setDouble(i,gate.balanceAmt); i++;
					}
				}
				//高購購票相關金額日(月)重製
				if ( gate.speedTrain ) {
					if (gate.balanceAmt<=0) {
						updBuffer.append("TRAIN_TOT_AMT_MONTH =  ?,");
						setDouble(i,0); i++;
						updBuffer.append("TRAIN_TOT_AMT_DAY =  ?,");
						setDouble(i,0); i++;
					}
					else {
						updBuffer.append("TRAIN_TOT_AMT_MONTH = ?,");
						setDouble(i,gate.balanceAmt); i++;
						updBuffer.append("TRAIN_TOT_AMT_DAY = ?,");
						setDouble(i,gate.balanceAmt); i++;
					}
				}
			}
			// 因交易成功而重製日相關累積消費次數與金額	
			else {
				if (gate.resetCcaConsumeDayData) {
					updBuffer.append("REJ_AUTH_CNT_DAY = ?,");
					setInt(i,0);       i++;
					updBuffer.append("TX_TOT_CNT_DAY = ?,");
					setInt(i,1);       i++;
					updBuffer.append("TX_TOT_AMT_DAY = ?,");
					if (gate.balanceAmt<=0)
						setDouble(i,0);
					else
						setDouble(i,gate.balanceAmt); 
					i++;
					//預借現金相關日次數金額重製
					if (( gate.cashAdvance ) || (gate.cashAdjust)) {
						updBuffer.append("FC_TOT_CNT_DAY = ?,");
						setInt(i,1);    i++;
						if (gate.balanceAmt<=0) {
							updBuffer.append("FC_TOT_AMT_DAY =  ?,");
							setDouble(i,0); i++;
						}
						else {
							updBuffer.append("FC_TOT_AMT_DAY =  ?,");
							setDouble(i,gate.balanceAmt); i++;
						}
					}
					//高鐵購票相關日金額重製
					if ( gate.speedTrain ) {
						if (gate.balanceAmt<=0) {
							updBuffer.append("TRAIN_TOT_AMT_DAY =  ?,");
							setDouble(i,0); i++;
						}
						else {
							updBuffer.append("TRAIN_TOT_AMT_DAY = ?,");
							setDouble(i,gate.balanceAmt); i++;
						}
					}
				} 
				//交易成功而相關累積消費次數與金額
				else {
					updBuffer.append("TX_TOT_CNT_MONTH = TX_TOT_CNT_MONTH + ?,");
					setInt(i,gate.transCnt);	i++;
					updBuffer.append("TX_TOT_CNT_DAY = TX_TOT_CNT_DAY + ?,");
					setInt(i,gate.transCnt);	i++;

					gb.showLogMessage("D","TX_TOT_AMT_MONTH = "+ getDouble("CcaConsumeTxTotAmtMonth")+getDouble("CcaConsumeTxTotAmtMonth"));

					if ((getDouble("CcaConsumeTxTotAmtMonth")+gate.balanceAmt)<0) {
						updBuffer.append("TX_TOT_AMT_MONTH = ?,");
						setDouble(i,0); i++;
					}
					else {
						updBuffer.append("TX_TOT_AMT_MONTH = TX_TOT_AMT_MONTH + ?,");
						setDouble(i,gate.balanceAmt); i++;
					}

					gb.showLogMessage("D","TX_TOT_AMT_DAY = "+ getDouble("CcaConsumeTxTotAmtDay")+";"+getDouble("CcaConsumeTxTotAmtDay"));

					if ((getDouble("CcaConsumeTxTotAmtDay")+gate.balanceAmt)<0) {
						updBuffer.append("TX_TOT_AMT_DAY = ?,");
						setDouble(i,0); i++;
					}
					else {
						updBuffer.append("TX_TOT_AMT_DAY = TX_TOT_AMT_DAY + ?,");
						setDouble(i,gate.balanceAmt); i++;
					}
					//預借現金相關日次數金額累計
					if (( gate.cashAdvance ) || (gate.cashAdjust)) {
						updBuffer.append("FC_TOT_CNT_MONTH = FC_TOT_CNT_MONTH + ?,");
						setInt(i,gate.transCnt);	i++;
						updBuffer.append("FC_TOT_CNT_DAY = FC_TOT_CNT_DAY + ?,");
						setInt(i,gate.transCnt);    i++;

						if ((getDouble("CcaConsumeFcTotAmtMonth")+gate.balanceAmt)<0) {
							updBuffer.append("FC_TOT_AMT_MONTH =  ?,");
							setDouble(i,0); i++;
						}
						else {
							updBuffer.append("FC_TOT_AMT_MONTH = FC_TOT_AMT_MONTH + ?,");
							setDouble(i,gate.balanceAmt); i++;
						}

						if ((getDouble("CcaConsumeFcTotAmyDay")+gate.balanceAmt)<0) {
							updBuffer.append("FC_TOT_AMT_DAY =  ?,");
							setDouble(i,0); i++;

						}
						else {
							updBuffer.append("FC_TOT_AMT_DAY = FC_TOT_AMT_DAY + ?,");
							setDouble(i,gate.balanceAmt); i++;
						}
					}
					//高鐵購票相關日金額累計
					if ( gate.speedTrain ) {
						if ((getDouble("CcaConsumeTrainTotAmtMonth")+gate.balanceAmt)<0) {
							updBuffer.append("TRAIN_TOT_AMT_MONTH =  ?,");
							setDouble(i,0); i++;
						}
						else {
							updBuffer.append("TRAIN_TOT_AMT_MONTH = TRAIN_TOT_AMT_MONTH + ?,");
							setDouble(i,gate.balanceAmt); i++;
						}

						if ((getDouble("CcaConsumeTrainTotAmtDay")+gate.balanceAmt)<0) {
							updBuffer.append("TRAIN_AMT_DAY = ?,");
							setDouble(i,0); i++;        		
						}
						else {
							updBuffer.append("TRAIN_TOT_AMT_DAY = TRAIN_TOT_AMT_DAY + ?,");
							setDouble(i,gate.balanceAmt); i++;
						}
					}
					//-國外交易-
					if ( "F".equals(gate.areaType) ) {
						updBuffer.append("FN_TOT_CNT_DAY = FN_TOT_CNT_DAY + ?,");
						setInt(i,gate.transCnt);       i++;

						if ((getDouble("CcaConsumeFnTotAmtDay")+gate.balanceAmt)<0) {
							updBuffer.append("FN_TOT_AMT_DAY =  ?,");
							setDouble(i,0); i++;

						}
						else {
							updBuffer.append("FN_TOT_AMT_DAY = FN_TOT_AMT_DAY + ?,");
							setDouble(i,gate.balanceAmt); i++;
						}
					}
				}
			}
		//revessal update budget date
		//2022/09/21  V1.00.21 以沖正交易成功與否作為判斷條件，並忽略的原始回覆碼的判斷
		if (gate.reversalBudgetAmt) {
    		gb.showLogMessage("D","reversalBudgetAmt = "+getDouble("CcaConsumeAuthTxlogAmt1")+" ;orignal txn amt"+ getDouble("AuthTxLogNtAmt_SrcTrans"));
			updBuffer.append("auth_txlog_amt_1 = auth_txlog_amt_1 - ?,");
			setDouble(i,getDouble("AuthTxLogNtAmt_SrcTrans")); i++;
			if (gate.reversalBudgetAmtCash) {
				updBuffer.append("auth_txlog_amt_cash_1 = auth_txlog_amt_cash_1 - ?,");
				setDouble(i,getDouble("AuthTxLogNtAmt_SrcTrans")); i++;
			}
		}
	    // 最後消費日期
		updBuffer.append("LAST_CONSUME_DATE = ?,");
		setString(i,gb.getSysDate()); i++;
		
		updBuffer.append("MOD_TIME = TO_DATE(?,'YYYYMMDDHH24MISS')");
		setString(i,gb.getSysDate()+gb.getSysTime()); i++;

		updateSQL =  updBuffer.toString();
		whereStr  = "WHERE  CARD_ACCT_IDX=? ";
		setString(i,gate.cardAcctIdx );
		
		gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - SQL -- "+updateSQL);
		gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - KEY -- "+gate.balanceAmt+" : "+ getPSeqNo() +" : "+getValue("ID_P_SEQNO"));

		liCnt = updateTable();
		return liCnt ;
		}
		

				
		// kevin:舊的金額次數累計移除
		// 累積月消費額
		/*
		if (gate.resetCcaConsumeMonthData) {
			updBuffer.append("TX_TOT_CNT_MONTH = ?,");
			updBuffer.append("TX_TOT_AMT_MONTH = ?,");
			setInt(i,1);       
			i++;
			setInt(i,1);       
			i++;
			if (gate.balanceAmt<=0)
				setDouble(i,0);
			else
				setDouble(i,gate.balanceAmt); 
			i++;
		}
		else {
			updBuffer.append("TX_TOT_CNT_MONTH = TX_TOT_CNT_MONTH + ?,");
			setInt(i,gate.transCnt);	i++;

			if ((getDouble("TX_TOT_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("TX_TOT_AMT_MONTH = ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TX_TOT_AMT_MONTH = TX_TOT_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

		}




		if (( gate.cashAdvance ) || (gate.cashAdjust)) {

			updBuffer.append("FC_TOT_CNT_MONTH = FC_TOT_CNT_MONTH + ?,");
			setInt(i,gate.transCnt);	i++;
			updBuffer.append("FC_TOT_CNT_DAY = FC_TOT_CNT_DAY + ?,");
			setInt(i,gate.transCnt);       i++;

			if ((getDouble("FC_TOT_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("FC_TOT_AMT_MONTH =  ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("FC_TOT_AMT_MONTH = FC_TOT_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

			if ((getDouble("FC_TOT_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("FC_TOT_AMT_DAY =  ?,");
				setDouble(i,0); i++;

			}
			else {
				updBuffer.append("FC_TOT_AMT_DAY = FC_TOT_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

		if ( gate.speedTrain ) {
			if ((getDouble("TRAIN_TOT_AMT_MONTH")+gate.balanceAmt)<0) {
				updBuffer.append("TRAIN_TOT_AMT_MONTH =  ?,");
				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TRAIN_TOT_AMT_MONTH = TRAIN_TOT_AMT_MONTH + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}

			if ((getDouble("TRAIN_TOT_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("TRAIN_AMT_DAY = ?,");
				setDouble(i,0); i++;        		
			}
			else {
				updBuffer.append("TRAIN_TOT_AMT_DAY = TRAIN_TOT_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

		// 累積日消費額
		if (gate.resetCcaConsumeDayData) {
			updBuffer.append("TX_TOT_CNT_DAY =  ?,");
			updBuffer.append("TX_TOT_AMT_DAY =  ?,");
			setInt(i,1);       
			i++;
			if (gate.balanceAmt<=0)
				setDouble(i,0);
			else
				setDouble(i,gate.balanceAmt); 

			i++;

		}
		else {
			updBuffer.append("TX_TOT_CNT_DAY = TX_TOT_CNT_DAY + ?,");
			setInt(i,gate.transCnt);		i++;


			if ((getDouble("TX_TOT_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("TX_TOT_AMT_DAY = ?,");

				setDouble(i,0); i++;
			}
			else {
				updBuffer.append("TX_TOT_AMT_DAY = TX_TOT_AMT_DAY + ?,");

				setDouble(i,gate.balanceAmt); i++;
			}
		}


		//-國外交易-
		if ( "F".equals(gate.areaType) ) {
			updBuffer.append("FN_TOT_CNT_DAY = FN_TOT_CNT_DAY + ?,");
			setInt(i,gate.transCnt);       i++;

			if ((getDouble("FN_TOT_AMT_DAY")+gate.balanceAmt)<0) {
				updBuffer.append("FN_TOT_AMT_DAY =  ?,");

				//setDouble(i,gate.transAmount); i++;
				setDouble(i,0); i++;

			}
			else {
				updBuffer.append("FN_TOT_AMT_DAY = FN_TOT_AMT_DAY + ?,");
				setDouble(i,gate.balanceAmt); i++;
			}
		}

	    // 最後消費日期
		updBuffer.append("LAST_CONSUME_DATE = ?,");
		setString(i,gb.sysDate); i++;
		
		updBuffer.append("MOD_TIME = TO_DATE(?,'YYYYMMDDHH24MISS')");
		setString(i,gb.sysDate+gb.sysTime); i++;

		updateSQL =  updBuffer.toString();
		whereStr  = "WHERE  CARD_ACCT_IDX=? ";
		setString(i,gate.CardAcctIdx );
		*/


		/*
		whereStr  = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";


		setString(i,getPSeqNo());  i++;
		setString(i,getValue("ID_P_SEQNO"));
		 */

		//gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - SQL -- "+updateSQL);
		//gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE - KEY -- "+gate.balanceAmt+" : "+ getPSeqNo() +" : "+getValue("ID_P_SEQNO"));
	
		
		//li_cnt = updateTable();
		/*if ( li_cnt == 0 )
           { gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE NOT_FOUND - ","SQL -- "+updateSQL);
          	  gb.showLogMessage("W","(2) CCS_CARD_ACCT UPDATE NOT_FOUND - ","KEY -- "+gate.balanceAmt+" : "+getValue("p_seqno")+" : "+getValue("ID_P_SEQNO")); }*/
		//return li_cnt ;

		/*原來的code..
       daoTable  = "CCS_CARD_ACCT";

       // 最後消費日期
       String  lastTxDate = getValue("LAST_CONSUME_DATE");

       if ( !"00".equals(gate.isoField[39]) )
          {
            if ( gb.sysDate.substring(0,6).equals(lastTxDate.substring(0,6)) )
               { updateSQL = "REJ_AUTH_CNT_MONTH = REJ_AUTH_CNT_MONTH + ?"; }
            else
               { updateSQL = "REJ_AUTH_CNT_MONTH = ?"; }
            whereStr = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";
            setInt(1,gate.transCnt);
            setString(2,getValue("P_SEQNO"));
            setString(3,getValue("ID_P_SEQNO"));
            int cnt = updateTable();
            return cnt;
          }

       int i=1;
       StringBuffer updBuffer = new StringBuffer();
       updBuffer.append("LAST_CONSUME_DATE = ?,");
       setString(i,gb.sysDate); i++;

       // 累積月消費額
       updBuffer.append("TX_CNT_MONTH = TX_CNT_MONTH + ?,");
       updBuffer.append("TX_AMT_MONTH = TX_AMT_MONTH + ?,");
       setInt(i,gate.transCnt);       i++;
       setDouble(i,gate.transAmount); i++;

       if ( "F".equals(gate.areaType) )
          {
            updBuffer.append("FN_CNT_MONTH = FN_CNT_MONTH + ?,");
            updBuffer.append("FN_AMT_MONTH = FN_AMT_MONTH + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.cashAdvance )
          {
            updBuffer.append("FC_CNT_MONTH = FC_CNT_MONTH + ?,");
            updBuffer.append("FC_AMT_MONTH = FC_AMT_MONTH + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.speedTrain )
          {
            updBuffer.append("TRAIN_AMT_MONTH = TRAIN_AMT_MONTH + ?,");
            setDouble(i,gate.transAmount); i++;
          }

       // 累積日消費額
       updBuffer.append("TX_CNT_DAY = TX_CNT_DAY + ?,");
       updBuffer.append("TX_AMT_DAY = TX_AMT_DAY + ?,");
       setDouble(i,gate.transCnt);    i++;
       setDouble(i,gate.transAmount); i++;

       if ( "F".equals(gate.areaType) )
          {
            updBuffer.append("FN_CNT_DAY = FN_CNT_DAY + ?,");
            updBuffer.append("FN_AMT_DAY = FN_AMT_DAY + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.cashAdvance )
          {
            updBuffer.append("FC_CNT_DAY = FC_CNT_DAY + ?,");
            updBuffer.append("FC_AMT_DAY = FC_AMT_DAY + ?,");
            setInt(i,gate.transCnt);       i++;
            setDouble(i,gate.transAmount); i++;
          }

       if ( gate.speedTrain )
          {
            updBuffer.append("TRAIN_AMT_DAY = TRAIN_AMT_DAY + ?,");
            setDouble(i,gate.transAmount); i++;
          }

       updBuffer.append("MOD_TIME = TO_DATE(?,'YYYYMMDDHH24MISS')");
       setString(i,gb.sysDate+gb.sysTime); i++;

       updateSQL =  updBuffer.toString();
       whereStr  = "WHERE  P_SEQNO = ? AND ID_P_SEQNO = ? ";

       setString(i,getValue("P_SEQNO"));  i++;
       setString(i,getValue("ID_P_SEQNO"));

       int    cnt2 = updateTable();
       return cnt2 ;
       原來的code*/


	// s AUTH_TXLOG 
	public int updateSrcAuthTxLog(int npActionType, String spSrcAuthNo) throws Exception {
		gb.showLogMessage("I","updateSrcAuthTxLog(): started!");
		//-hfupdate ori-txlog-
		//Howard:
		//nP_ActionType ==1 :hf
		//nP_ActionType ==3 :@վ
		//nP_ActionType ==4 :hfվ
		//nP_ActionType ==5 :wɲ{վ    
		//nP_ActionType ==6 :reversal
		int liCnt=0, ii=1;
		daoTable = addTableOwner("CCA_AUTH_TXLOG");
		/*
                     UPDATE   AUTH_TXLOG
                        SET   LOGIC_DEL=:AuTxlog_LOGIC_DEL,
                              CACU_AMOUNT=:AuTxlog_CACU_AMOUNT,
                              CACU_CASH=:AuTxlog_CACU_CASH
                     WHERE  ROWID=CHARTOROWID(:AuTxlog_ROWID);
		 * */

		StringBuffer lsSql = new StringBuffer();

		if (npActionType==1) { //refund
			lsSql.append("cacu_amount = ?, "); //--Y.未沖銷退貨調整, N.已沖銷退貨調整
			lsSql.append("cacu_cash = ? , ");
			setString(ii,"N");	ii++;
			setString(ii,"N"); 	ii++;
			
			lsSql.append("chg_date = ?, ");
			lsSql.append("chg_time = ?, ");
			lsSql.append("chg_user = ?, ");
			setString(ii,gb.getSysDate());	 ii++;
			setString(ii,gb.getSysTime());	 ii++;
			setString(ii,gate.connType); ii++;

			lsSql.append("MOD_TIME = sysdate");

			updateSQL = lsSql.toString();
			whereStr  = "WHERE  auth_seqno =? "; 
			//sP_SrcAuthNo
			setString(ii,spSrcAuthNo);  ii++;
		}
		else if (npActionType==2) { //preAuth complete
			lsSql.append("cacu_amount = ?, ");
			lsSql.append("cacu_cash = ? , ");
			setString(ii,"N");	ii++;
			setString(ii,"N"); 	ii++;
			//VD卡解圈註記Y
			if (gate.isDebitCard) {
				lsSql.append("unlock_flag='Y', ");
			}

			lsSql.append("chg_date = ?, ");
			lsSql.append("chg_time = ?, ");
			lsSql.append("chg_user = ?, ");
			setString(ii,gb.getSysDate());	 ii++;
			setString(ii,gb.getSysTime());	 ii++;
			setString(ii,gate.connType); ii++;

			lsSql.append("MOD_TIME = sysdate");

			updateSQL = lsSql.toString();
			whereStr  = "WHERE  auth_seqno =? "; 
			setString(ii,spSrcAuthNo);  ii++;
		}
		//kevin:調整npActionType= 5 or 6 單獨處理reversal
		if( (npActionType==5) || (npActionType==6) ) { // reversal 5:一般沖正 6:票證沖正
			lsSql.append("cacu_amount = ?, "); 
			lsSql.append("cacu_cash = ? , ");
			lsSql.append("logic_del = ? , ");
			lsSql.append("reversal_flag = ? , ");
			setString(ii,"N");	ii++;
			setString(ii,"N"); 	ii++;
			setString(ii,gate.logicDel); 	ii++;
			setString(ii,gate.reversalFlag); 	ii++;
			//VD卡解圈註記Y
			if (gate.isDebitCard) {
				lsSql.append("unlock_flag='R', ");
			}			
			lsSql.append("chg_date = ?, ");
			lsSql.append("chg_time = ?, ");
			lsSql.append("chg_user = ?, ");
			setString(ii,gb.getSysDate());	 ii++;
			setString(ii,gb.getSysTime());	 ii++;
			setString(ii,gate.connType); ii++;

			lsSql.append("MOD_TIME = sysdate");

			updateSQL = lsSql.toString();
			whereStr  = "WHERE  auth_seqno =? "; 
			//sP_SrcAuthNo
			setString(ii,spSrcAuthNo);  ii++;
		}
		if( (npActionType==3) || (npActionType==4)) { //=>purchAdjust, refundAdjust, =>cashAdjust
			lsSql.append("cacu_amount = ?, ");
			lsSql.append("cacu_cash = ? , ");
			lsSql.append("logic_del = ? , ");
			setString(ii,"N");	ii++;
			setString(ii,"N"); 	ii++;
			//setString(ii,"J"); 	ii++;
			setString(ii,gate.logicDel); 	ii++;

			lsSql.append("chg_date = ?, ");
			lsSql.append("chg_time = ?, ");
			lsSql.append("chg_user = ?, ");
			setString(ii,gb.getSysDate());	 ii++;
			setString(ii,gb.getSysTime());	 ii++;
			setString(ii,gate.connType); ii++;

			lsSql.append("MOD_TIME = sysdate");

			updateSQL = lsSql.toString();
			whereStr  = "WHERE  auth_seqno =? "; 
			//sP_SrcAuthNo
			setString(ii,spSrcAuthNo);  ii++;
		}

		liCnt = updateTable();
		return liCnt ;

	}

	// 更新 AUTH_TXLOG 交易紀錄
//	public void updateAuthTxLog4ReversalIbt() throws Exception {
//
//		int liCnt=0, ii=1;
//
//		StringBuffer lsSql = new StringBuffer();
//
//		daoTable  = addTableOwner("CCS_AUTH_TXLOG");
//
//		lsSql.append("cacu_amount = ?, "); ii++;
//		lsSql.append("cacu_cash = ? , "); ii++;
//		setString(1,"N");	
//		setString(2,"N"); 	
//		lsSql.append("unlock_flag='Y', ");
//
//		lsSql.append("REVERSAL_FLAG='Y', ");
//		lsSql.append("MOD_TIME = sysdate");
//
//		updateSQL = lsSql.toString();
//
//
//
//
//
//
//		whereStr  = "WHERE  AUTH_SEQNO = ? ";
//		setString(ii,getValue("AUTH_SEQNO"));  ii++;
//
//
//		liCnt = updateTable();
//		return;
//	}

	//讀取CCA_SYS_PARM1
	public boolean selectSysParm1(String sysId,String sysKey,String spSelectField) throws Exception {
		gb.showLogMessage("I","selectSysParm1(): started!");
		daoTable  = addTableOwner("CCA_SYS_PARM1");
		selectSQL = spSelectField;
		whereStr  = "WHERE  SYS_ID = ? AND SYS_KEY = ? ";
		setString(1, sysId);
		setString(2, sysKey);
		selectTable();
		if ( "Y".equals(notFound) ) {
			//jjj_notfind("ID="+sysId+", key="+sysKey);
			return false;
		}

		return true;
	}

	/**
	 * 讀取AUTH_TXLOG
	 * V1.00.12 代行交易可以做沖正
	 * V1.00.13 ECS人工沖正處理
	 * V1.00.37 P3紅利兌換處理方式調整
	 * V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理
	 * @throws Exception if any exception occurred
	 */
	public boolean getTxlog(int npTransType) throws Exception {
		gb.showLogMessage("I","getTxlog("+npTransType+"): started!");
		//Howard: 
		//npTransType ==1  : 退貨 
		//npTransType ==2  : 一般交易調整
		//npTransType ==3  : 退貨調整
		//npTransType ==4  : 預借現金調整    	
		//npTransType ==5  : 沖銷reversal
		//kevin:
		//npTransType ==6  : 票證卡片沖銷reversal
		//nPTransType ==7  : 預先授權
		//nPTransType ==8  : ECS人工沖銷專用

		/*
		if (npTransType==3) //Howard: hfվ ݭn select data
			return true;
		 */
		//
		/*	
 	   //-退貨比對天數-
 	   int li_days=0;
 	   if (selectSysParm1("REPORT", "RETURN_05", "nvl(sys_data1,'9') as txback_days")) {
 		   li_days = (int) getInteger("sys1.txback_days");
 	   }
 	   if (li_days<=0) {
 		   li_days =9;
 	   }
		 */

		//-hf-
		//daoTid ="txlog.";
		daoTable  = addTableOwner("CCA_AUTH_TXLOG");
		selectSQL = "nvl(cacu_amount,'N') as AuthTxlogCacuAmount_SrcTrans, "
				+"mcht_no as AuthTxLogOldMchtNo_SrcTrans, "
				+"nvl(train_flag,'N') as AuthTxLogTrainFlag_SrcTrans, "
				+"online_redeem as AuthTxLogOnlineRedeem_SrcTrans, "
				+"nvl(ori_amt,0) as AuthTxLogOriAmt_SrcTrans, "
				+"nvl(nt_amt,0) as AuthTxLogNtAmt_SrcTrans, "
				+"auth_seqno as AuthTxLogAuthSeqNo_SrcTrans, "
				+"nvl(cacu_cash,'N') as AuthTxlogCacuCash_SrcTrans, "
				+"tx_date as AuthTxlogTxDate_SrcTrans, "
				+"mcc_code as AuthTxlogMccCode_SrcTrans, "
				+"consume_country as AuthTxlogConsumeCountry_SrcTrans, "
				+"auth_unit as AuthTxLogAuthUnit_SrcTrans, "
				+"tx_seq as AuthTxLogTxSeq, "
				+"ref_no as AuthTxLogRefNo, "
				+"auth_no as AuthTxLogAuthNo, "
				+"vd_lock_nt_amt as AuthTxLogVdLockNtAmt ";

		/* HU autoAuthIc hfɨS select X
 			   +"nvl(cacu_cash,'N') as cacu_cash, "






 			   +"auth_seqno, "
 			   +"bank_tx_seqno, "
 			   +"nvl(unlock_flag,'N') as unlock_flag, "
 			   +"nvl(debt_flag,'N') as debt_flag,"
  			   +"P_SEQNO as PRIOR_TX_P_SEQNO," //add by Howard
  	   		   +"ID_P_SEQNO as PRIOR_TX_ID_P_SEQNO"; //add by Howard
		 */
		if (npTransType == 7) {
			whereStr ="where card_no =?"
					+" and auth_no =?"
					+" and iso_resp_code =?"
					+ "and auth_type = ?";
			setString(1,gate.cardNo);
			setString(2,gate.oriAuthNo);
			setString(3,"00");
			setString(4,"X");
		} 
		else if (npTransType == 6) {
			whereStr ="where card_no =?"
					+" and trace_no =?"
					+" and iso_resp_code =?"
//					+" AND decode(online_redeem,'1',to_number(nt_amt), '2',to_number(nt_amt),ori_amt ) = to_number(?) ";
					+" AND (to_number(nt_amt) = to_number(?) or to_number(ori_amt) = to_number(?)) ";
			setString(1,gate.cardNo);
			setString(2,gate.oriTraceNo);
			setString(3,"00");
			setString(4,Double.toString(gate.isoFiled4Value));
			setString(5,Double.toString(gate.oriAmount));
		} 
		else if (npTransType == 8) {
			whereStr ="where card_no =?"
					+" and auth_seqno =?"
					+" and iso_resp_code =?"
//					+" AND decode(online_redeem,'1',to_number(nt_amt), '2',to_number(nt_amt),ori_amt ) = to_number(?) ";
					+" AND (to_number(nt_amt) = to_number(?) or to_number(ori_amt) = to_number(?)) ";
			setString(1,gate.cardNo);
			setString(2,gate.isoField[82]);
			setString(3,"00");
			setString(4,Double.toString(gate.isoFiled4Value));
			setString(5,Double.toString(gate.oriAmount));
		} 
		else {
			whereStr ="where card_no =?"
					+" and (auth_no =? or trace_no =?)"
					+" and iso_resp_code =?"
//					+" AND decode(online_redeem,'1',to_number(nt_amt), '2',to_number(nt_amt),ori_amt ) = to_number(?) ";
					+" AND (to_number(nt_amt) = to_number(?) or to_number(ori_amt) = to_number(?)) "
					+" and (trans_type = '0100' or trans_type = '0200' or trans_type = '0120') ";
			setString(1,gate.cardNo);
			setString(2,gate.oriAuthNo);			
			setString(3,gate.oriTraceNo);
			setString(4,"00");
			setString(5,Double.toString(gate.isoFiled4Value));
			setString(6,Double.toString(gate.oriAmount));
		}
		//+" AND decode(online_redeem,'1',to_number(nt_amt), '2',to_number(nt_amt),ori_amt*100 ) = to_number(?) ";
		//+" and tx_date >=to_char(sysdate - ?,'yyyymmdd')";

		/*
 	  DEBT_FLAG               
 	  UNLOCK_FLAG             
 	  有任何一個是Y就表示已經解圈

		 */

//		setString(1,gate.cardNo);
//		setString(2,gate.oriAuthNo);
//
//		setString(3,Double.toString(gate.isoFiled4Value));


		selectTable();
		if ( "Y".equals(notFound) ) {
			//jjj_notfind("mcht="+gate.merchantNo+", authNo="+gate.isoField[38]);
			gb.showLogMessage("I","沖正交易處理失敗=>" + gate.cardNoMask + "---"+gate.oriAuthNo+"---"+Double.toString(gate.isoFiled4Value));
			return false;
		}

		gb.showLogMessage("D","沖正交易處理成功=>" + gate.cardNoMask + "---"+gate.oriAuthNo+"---"+Double.toString(gate.isoFiled4Value));

		if (!"00".equals(gate.isoField[39]) && npTransType != 7) {
			gb.showLogMessage("I","reversal txn gGate.isoField[39] = " +  gate.isoField[39]);
			gate.isoField[39] = "00";
		}
		return true;
	}

//	public boolean adjustPriorTxAmt_Old(Double dpAdjustAmt) throws Exception{
//		gb.showLogMessage("I","adjustPriorTxAmt_Old("+dpAdjustAmt+"): started!");
//		boolean blResult = true;
//		daoTable  = addTableOwner("CCS_CARD_ACCT");
//		updateSQL = "LAST_CONSUME_DATE = ?,"
//				+ "TX_AMT_DAY  = TX_AMT_DAY + ?,"
//				+ "TX_CNT_DAY  = TX_CNT_DAY + 1,"
//				+ "TX_AMT_MONTH = TX_AMT_MONTH + ?,"
//				+ "TX_CNT_MONTH = TX_CNT_MONTH + 1";
//		whereStr  = "WHERE P_SEQNO = ? AND ID_P_SEQNO = ? ";
//		setString(1, gb.getSysDate());
//		setDouble(2,dpAdjustAmt);
//		setDouble(3,dpAdjustAmt);
//
//		setString(4, getValue("PRIOR_TX_P_SEQNO"));
//		setString(5, getValue("PRIOR_TX_ID_P_SEQNO"));
//		int  cnt = updateTable();
//		if (cnt== 0)
//			blResult = false;
//
//		return blResult;
//
//	}

	public boolean getPriorTxLog() throws Exception {
		gb.showLogMessage("I","getPriorTxLog(): started!");

		//daoTid ="txlog.";
		daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
		selectSQL = "nvl(cacu_amount,'N') as src_cacu_amount, "
				+"nvl(cacu_cash,'N') as src_cacu_cash, "
				+"nvl(NT_AMT,0) as SRC_NT_AMT, "
				//+"auth_seqno as SRC_AUTH_SEQNO , "
				//+"BANK_TX_SEQNO as SRC_BANK_TX_SEQNO, "
				//+"BANK_TX_AMT as SRC_BANK_TX_AMT, "
				+"MCHT_NAME as SRC_MCHT_NAME,"
				//+"nvl(unlock_flag,'N') as unlock_flag, "
				//+"nvl(debt_flag,'N') as debt_flag,"
				+"TX_DATE as AuthLogPriorTxDate,"
				+"P_SEQNO as PRIOR_TX_P_SEQNO,"
				+"ID_P_SEQNO as PRIOR_TX_ID_P_SEQNO";
		whereStr ="where card_no =?"
				+" and mcht_no =? and auth_no =?"
				+" and nt_amt=? " 
				+" and DEBT_FLAG='N' and UNLOCK_FLAG='N' ";
		//+" and tx_date >=to_char(sysdate - ?,'yyyymmdd')";

		/*
  	 DEBT_FLAG               
  	 UNLOCK_FLAG             
  	 有任何一個是Y就表示已經解圈

		 */

		setString(1,gate.cardNo);
		setString(2,gate.merchantNo);
		//setString(3,gate.isoField[38].trim());
		setString(3,gate.oriAuthNo);

		//setDouble(4, gate.nt_amt);
		setDouble(4, gate.isoFiled4Value);
		//setInt(5,li_days);

		selectTable();
		if ( "Y".equals(notFound) ) {
			//jjj_notfind("mcht="+gate.merchantNo+", authNo="+gate.isoField[38]);
			return false;
		}

		//gate.src_bank_tx_seqno = getValue("SRC_BANK_TX_SEQNO");
		return true;
	}

	// 更新 CCA_CARD_BASE
	public boolean updateCardBase() throws Exception{
		gb.showLogMessage("I","updateCardBase(): started!");
		//proc is TB_card_base(2..)
		boolean blResult = true;
		daoTable  = addTableOwner("CCA_CARD_BASE");
		updateSQL ="PRE_AUTH_FLAG             = ?,"
				+ "PRE_AUTH_DATE_1           = ?,"
				+ "PRE_AUTH_AMT_1            = ?,"
				+ "PRE_AUTH_CODE_1           = ?,"
				+ "PRE_AUTH_EFF_DATE_END_1   = ?,"
				+ "WRITE_OFF_1               = ?,"
				+ "PRE_AUTH_DATE_2           = ?,"
				+ "PRE_AUTH_AMT_2            = ?,"
				+ "PRE_AUTH_CODE_2           = ?,"
				+ "PRE_AUTH_EFF_DATE_END_2   = ?,"
				+ "WRITE_OFF_2               = ?,"
				+ "CVV_ERROR_CNT             = ?,"
				+ "CVV_ERR_TIME              = ?,"
				+ "TOT_AMT_DAY               = ?,"
				+ "TOT_CNT_DAY               = ?,"
				+ "LAST_CONSUME_DATE         = ?,"
				+ "LAST_CONSUME_TIME         = ?,"
				+ "LAST_CONSUME_DATE_M       = ?,"
				+ "LAST_AUTH_CODE            = ?,"
				+ "LAST_AMT                  = ?,"
				+ "LAST_CURRENCY             = ?,"
				+ "LAST_COUNTRY              = ?,"
				+ "PREV_CONSUME_DATE         = ?,"
				+ "PREV_CONSUME_TIME         = ?,"
				+ "PREV_AUTH_CODE            = ?,"
				+ "PREV_AMT                  = ?,"
				+ "PREV_CURRENCY             = ?,"
				+ "PREV_COUNTRY              = ?"; 

		whereStr  = "WHERE CARD_NO = ?  ";
		setString(1, gate.cardBasePreAuthFlag);//ok
		setString(2,getValue("CardBasePreAuthDate1")); //ok
		setString(3,getValue("CardBasePreAuthAmt1"));//ok
		setString(4,getValue("CardBasePreAuthCode1"));//ok

		setString(5, getValue("CardBasePreAuthEffDateEnd1"));//ok
		setString(6, getValue("CardBaseWriteOff1"));//ok
		setString(7, getValue("CardBasePreAuthDate2"));

		setString(8,getValue("CardBasePreAuthAmt2"));//ok
		setString(9,getValue("CardBasePreAuthCode2"));//ok

		setString(10, getValue("CardBasePreAuthEffDateEnd2"));//ok
		setString(11, getValue("CardBaseWriteOff2"));//ok



		setString(12,getValue("CardBaseCvvErrorCnt"));
		setString(13,getValue("CardBaseCvvErrorTime"));

		setInt(14,gate.cardBaseTotAmtDay);
		setInt(15,gate.cardBaseTotCntDay);
		setString(16,gate.txDate);
		setString(17,gate.txTime);
		setString(18,gate.txDate); //Howard: LAST_CONSUME_DATE_M(最後消費日期-M) 要填啥值=> 這要再確認
		setString(19,gate.isoField[38]); //LAST_AUTH_CODE
		setString(20,gate.cardBaseLastAmt); //LAST AMT
		setString(21,gate.cardBaseLastCurrency);
		setString(22,gate.cardBaseLastCountry);
		setString(23,getValue("CardBaseLastConsumeDate")); //LAST_CONSUME_DATE
		setString(24,getValue("CardBaseLastConsumeTime"));
		setString(25,getValue("CardBaseLastAuthCode"));
		setString(26,getValue("CardBaseLastAmt"));
		setString(27,getValue("CardBaseLastCurrency"));
		setString(28,getValue("CardBaseLastCountry"));
		setString(29, gate.cardNo);

		int  cnt = updateTable();
		if (cnt== 0)
			blResult = false;

		return blResult;




	}

	public boolean insertCcaOnBat() throws Exception {
		gb.showLogMessage("I","insertCcaOnBat(): started!");
		boolean blResult=true;
		daoTable = addTableOwner("ONBAT_2ECS");
		//daoTable = "CCA_ONBAT_INTERFACE";

		setValue("TRANS_TYPE","18"); //傳輸類別
		setValue("TO_WHICH","1");//傳送至 
		setValue("DOG", HpeUtil.getCurDateTimeStr(false, true));  //傳輸日期
		setValue("PROC_MODE","0");  //傳輸模式
		setValue("PROC_STATUS","O"); //傳輸狀態

		setValue("CARD_NO",gate.cardNo); //卡號
		setValue("TRANS_DATE", gate.txDate); //異動日

		setValue("TRANS_AMT",Double.toString(gate.ccaConsumeIbmReceiveAmt)); //異動金額

		setValue("MCC_CODE",gate.mccCode); //特店類別碼

		setValue("CARD_VALID_TO",gate.expireDate); //卡片有效結束日

		setValue("AUTH_NO", gate.isoField[38]); //授權碼
		setValue("IBM_RECEIVE_AMT",Double.toString(gate.ccaConsumeIbmReceiveAmt)); //IBM接數金額

		insertTable();



		return blResult;



	}


	public String getNextSeqVal(String spSequenceName)  throws Exception {
		gb.showLogMessage("I","getNextSeqVal(): started!");
		//get sequence value

		String slSeqVal = "0";
		try {

			daoTable = addTableOwner("dual");	

			selectSQL =  gb.getTableOwner().trim()+"."+spSequenceName  + ".NEXTVAL as SeqVal" ;


			selectTable();




			slSeqVal = getValue("SeqVal");



		} catch (Exception e) {
			// TODO: handle exception
			slSeqVal = "0";
		}
		return slSeqVal;

	}

	public boolean insertCcaIbmReversal(String spToIbmMsgData, AuthTxnGate pAuthGate4Ibm)throws Exception{
		gb.showLogMessage("I","insertCcaIbmReversal(): started!");
		//proc is TB_ibm_reversal()

		boolean blResult=true;

		try {
			gb.showLogMessage("I","insertCcaIbmReversal : start");

			daoTable = addTableOwner("CCA_IBM_REVERSAL");
			String slCurDate= HpeUtil.getCurDateStr(false);
			String slCurTime=HpeUtil.getCurTimeStr();

			setValue("TX_DATE",slCurDate);
			setValue("TX_TIME",slCurTime);
			setValue("AUTH_NO",pAuthGate4Ibm.authNo);
			setValue("CARD_NO",pAuthGate4Ibm.cardNo);
			setValue("TRANS_TYPE",pAuthGate4Ibm.mesgType);
			setValue("CARD_ACCT_IDX",pAuthGate4Ibm.cardAcctIdx);
			setValue("CARD_ACCT_ID",pAuthGate4Ibm.cardAcctId);
			setValue("CARD_ACCT_ID_SEQ","");//Howard: db 中無此欄位
			setValue("CARD_HLDR_ID",getValue("CrdIdNoIdNo")); 
			setValue("CARD_HLDR_ID_SEQ",getValue("ID_NO_CODE"));
			setValue("CARD_LEVEL",getValue("CardAcctCcasClassCode")); 
			setValue("TRACE_NO", pAuthGate4Ibm.isoField[11]);
			setValue("TERM_ID","Auto00"); 
			setValue("RISK_TYPE",pAuthGate4Ibm.mccCode);
			setValue("MCHT_NO",pAuthGate4Ibm.merchantNo);
			setValue("ACQ_ID",pAuthGate4Ibm.isoField[32]);
			setValue("REF_NO",pAuthGate4Ibm.isoField[37]);
			setValue("TRANS_AMT",pAuthGate4Ibm.isoField[4]);

			setValue("MSG_DATA",spToIbmMsgData);
			setValue("PROC_CODE","N");
			setValue("PROC_DATE",null);
			setValue("PROC_UID",null);
			setValue("CRT_DATE",slCurDate);
			setValue("CRT_USER","autoauth");
			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
			blResult=false;
		}

		return blResult;
	}
	// 更新 CCA_CONSUME
	public boolean updateCcaConsume2() throws Exception{
		gb.showLogMessage("I","updateCcaConsume2(): started!");
		//要併入updateCcaConsume()
		//proc is TB_card_base(2..)
		boolean blResult = true;
		daoTable  = addTableOwner("CCA_CONSUME");
		updateSQL ="SET  IBM_RECEIVE_AMT             = ?,"
				+ "MOD_TIME           = ?,"
				+ "MOD_USER            = ?,"
				+ "MOD_PGM           = ?";

		whereStr  = "CARD_ACCT_IDX = ?  ";
		setDouble(1, getDouble("CcaConsumeIbmReceiveAmt"));//預借現金指撥金額
		setString(2,HpeUtil.getCurDateTimeStr(false, false)); //ok
		setString(3,ConstObj.MOD_USER);//ok
		setString(4, gb.getSystemName());//ok


		int  cnt = updateTable();
		if (cnt== 0)
			blResult = false;

		return blResult;




	}

	public boolean insertCardOpen() throws Exception {
		gb.showLogMessage("I","insertCardOpen(): started!");
		boolean blResult = true;
		daoTable = addTableOwner("CCA_CARD_OPEN");

		setValue("CARD_NO", gate.cardNo);


		setValue("NEW_END_DATE", getValue("NEW_END_DATE"));//有效日期(迄)

		gb.showLogMessage("D","CARD_NO=>" + gate.cardNoMask + "---");
		gb.showLogMessage("D","NEW_END_DATE=>" +getValue("NEW_END_DATE") + "---");


		//setValue("NEW_BEG_DATE", gate.cardNo);//有效日期(起) //Howard: 舊系統沒有 insert 此欄位

		String slNewoldFlag="O"; 
		if (gate.IsNewCard)
			slNewoldFlag="N";
		setValue("NEW_OLD_FLAG", slNewoldFlag); //新舊卡別

		String slActivateType="V";//V: VOICE
		if ("0                     TAIPEI CITY  TW TW".equals(gate.isoField[43]))
			slActivateType="O"; //O:ONLINE
		setValue("OPEN_TYPE", slActivateType); //開卡方式

		setValue("OPEN_DATE", gb.getSysDate());

		setValue("OPEN_TIME", gate.txTime);

		gb.showLogMessage("D","sL_NewoldFlag=>" +slNewoldFlag + "---");
		gb.showLogMessage("D","sL_ActivateType=>" +slActivateType + "---");
		gb.showLogMessage("D","gb.sysDate=>" +gb.getSysDate() + "---");
		gb.showLogMessage("D","gb.sysDate=>" +gb.getSysDate() + "---");


		setValue("OPEN_USER", ConstObj.CRT_USER);
		//setValue("MOD_TIME", gb.sysDate);
		setTimestamp("MOD_TIME",gb.getgTimeStamp());

		setValue("MOD_PGM", gb.getSystemName());

		insertTable();
		if ("Y".equals(dupRecord)) {
			gb.showLogMessage("I","insert cca_card_open duplicate="+gate.cardNoMask);
		}
		return blResult;
	}

	public boolean insertOnCcaBat(String spActivateType) throws Exception {
		gb.showLogMessage("I","insertOnCcaBat(): started!");
		//Howard: 2017.12.25 應來哥建議，開卡時將資料 insert into ONBAT_2ECS
		boolean blResult = true;
		daoTable = addTableOwner("ONBAT_2ECS");

		setValue("TRANS_TYPE", "9");

		//setValueInt("TO_WHICH", 1);
		setValue("TO_WHICH", "1");


		setTimestamp("DOG", gb.getgTimeStamp());
		setTimestamp("DOP", gb.getgTimeStamp());
		setValue("PROC_MODE", "");
		setValue("PROC_STATUS", "0");
		setValue("CARD_INDICATOR", "");
		setValue("PAYMENT_TYPE", "");
		setValue("ACCT_TYPE", "");
		setValue("CARD_HLDR_ID", "");
		setValue("ID_P_SEQNO", "");
		setValue("CARD_ACCT_ID", "");
		setValue("ACNP_P_SEQNO", "");
		setValue("CARD_NO", gate.cardNo);
//		gb.showLogMessage("D","gate.cardNo=>" + gate.cardNoMask + "---");

		setValue("OLD_CARD_NO", "");
		setValue("CREDIT_LIMIT", "0");
		setValue("TRANS_DATE", "");
		setValue("TRANS_AMT", "0");
		setValue("MCC_CODE", gate.mccCode);
//		gb.showLogMessage("D","gate.mccCode=>" + gate.mccCode + "---");

		setValue("ISO_RESP_CODE", "");
		setValue("ICBC_RESP_CODE", "");
		setValue("ICBC_RESP_DESC", "");
		setValue("CARD_VALID_FROM", "");

		String slValidTo=gate.expireDate;
		if (gate.expireDate.length()==4) {
			slValidTo= "20" + gate.expireDate;  
		}
		setValue("CARD_VALID_TO", slValidTo);
//		gb.showLogMessage("D","sL_ValidTo=>" + slValidTo + "---");

		setValue("OPP_TYPE", "");
		setValue("OPP_REASON", "");
		setValue("OPP_DATE", "");

		setValue("IS_RENEW", "");
		setValue("IS_EM", "");
		setValue("IS_RC", "");

		setValue("CYCLE_CREDIT_DATE", "0");
		setValue("CURR_TOT_LOST_AMT", "0");

		setValue("PROC_DATE", "");

		setValue("CARD_LAUNCH_TYPE", spActivateType);
		setValue("CARD_LAUNCH_DATE", gb.getSysDate());


		setValue("CVC2_CODE", "");
		setValue("ACTIVE_PIN", "");
		setValue("VOICE_PIN", "");

		setValue("AUTH_CODE", "");
		setValue("TRANS_CODE", "");
		setValue("REF_NO", "");

		setValue("MATCH_FLAG", "");
		setValue("MATCH_DATE", "");

		setValue("TELE_NO", "");
		setValue("CONTRACT_NO", "");

		setValue("BLOCK_REASON1", "");
		setValue("BLOCK_REASON2", "");
		setValue("BLOCK_REASON3", "");
		setValue("BLOCK_REASON4", "");
		setValue("BLOCK_REASON5", "");

		setValue("IBM_RECEIVE_AMT", "0");
		setValue("CREDIT_LIMIT_CASH", "0");

		setValue("ACCT_NO", "");
		setValue("ACCT_NO_OLD", "");


		setValue("MAIL_BRANCH", "");
		setValue("LOST_FEE_FLAG", "");
		setValue("DEBIT_FLAG", gate.debtFlag);
//		gb.showLogMessage("D","gate.debt_flag=>" + gate.debtFlag + "---");
		insertTable();

		return blResult;
	}

	public int updateCardBaseAfterVoiceOpenCard(String spActivateType) throws Exception {
		gb.showLogMessage("I","updateCardBaseAfterVoiceOpenCard(): started!");
		String slTargetTableName = "";

		if (gate.isDebitCard)
			slTargetTableName = addTableOwner("DBC_CARD");
		else
			slTargetTableName = addTableOwner("CRD_CARD");
		daoTable = slTargetTableName;
		//crd_card.activate_flag, crd_card.ACTIVATE_TYPE,  crd_card.ACTIVATE_DATE, crd_card.ACTIVATE_time, crd_card.activate_user

		StringBuffer lSqlBuffer = new StringBuffer();

		if (gate.IsNewCard) {
			lSqlBuffer.append(" ACTIVATE_FLAG=?,");
			setString(1, "2"); //1:關閉 2:開卡
			lSqlBuffer.append("ACTIVATE_TYPE=?,");
			setString(2, spActivateType); //V: VOICE O:ONLINE
			lSqlBuffer.append("ACTIVATE_DATE=?");
			setString(3, HpeUtil.getCurDateStr(false));
		}
		else {
			lSqlBuffer.append(" OLD_ACTIVATE_FLAG=?,");
			setString(1, "1"); //1:關閉 2:開卡  12/17 改為 //1:關閉 2:開卡
			//setString(0, "2"); //1:開卡 2:關閉
			lSqlBuffer.append("OLD_ACTIVATE_TYPE=?,");
			setString(2, spActivateType); //V: VOICE O:ONLINE
			lSqlBuffer.append("OLD_ACTIVATE_DATE=?");
			setString(3, HpeUtil.getCurDateStr(false));
		}


		updateSQL =  lSqlBuffer.toString();

		gb.showLogMessage("D","updateSQL=>" +updateSQL + "----");

		whereStr  = " WHERE  CARD_NO = ? ";
		setString(4, gate.cardNo);




		return updateTable();

	}
	
	public int updateCardBase4Invoice(String spRequestType) throws Exception {
		gb.showLogMessage("I","updateCardBase4Invoice(): started!");
		String slTargetTableName = "";

		if (gate.isDebitCard)
			slTargetTableName = addTableOwner("DBC_CARD");
		else
			slTargetTableName = addTableOwner("CRD_CARD");
		daoTable = slTargetTableName;
		//更新電子發票載具申請A或取消B

		StringBuffer lSqlBuffer = new StringBuffer();

		lSqlBuffer.append("E_INVOICE_DEPOSIT_ACCOUNT=?");
		setString(1, spRequestType); //申請A / 取消B


		updateSQL =  lSqlBuffer.toString();

		gb.showLogMessage("D","updateSQL=>" +updateSQL + "----");

		whereStr  = " WHERE  CARD_NO = ? ";
		setString(2, gate.cardNo);

		return updateTable();

	}

	/**
	 * 新增 AUTH_TXLOG 交易紀錄
	 * V1.00.38 P3授權額度查核調整
	 * V1.00.42 授權系統與DB連線交易異常時的處理改善方式
	 * V1.00.48 P3程式碼整理(TXLOG相關欄位整理)
     * V1.00.58 TXLOG相關欄位整理
	 * @return
	 * @throws Exception
	 */
	public boolean insertAuthTxLog() throws Exception {
		gb.showLogMessage("I","insertAuthTxLog(): started!");

		daoTable = addTableOwner("CCA_AUTH_TXLOG");
		setValue("TX_DATE", gate.txDate); //#0-建檔日期
		setValue("TX_TIME", gate.txTime); //#1-建檔時間
		setTimestamp("TX_DATETIME", gb.getgTimeStamp()); //#2-消費日期時間
		setValue("CARD_NO", gate.cardNo); //#3-卡號
		setValue("AUTH_NO", gate.authNo); //#4-授權碼
		setValue("CORP_FLAG", ""); //#5-此欄位無使用
		if (gate.isSupCard) {  //#6-正附卡別
			setValue("SUP_FLAG", "1");
		}
		else {
			setValue("SUP_FLAG", "0");
		}
		setValue("ACCT_TYPE", getValue("CardAcctCardAcctType")); //#7-帳戶類別
		setValue("ACNO_P_SEQNO", getValue("CardBaseAcnoPSeqNo")); //#8-帳戶流水號
		setValue("ID_P_SEQNO",getValue("CardBaseIdPSeqNo") ); //#9-身分證流水號
		setValue("CORP_P_SEQNO", getValue("ActAcnoCorpPSeqno")); //#10-公司流水號
		setValue("CLASS_CODE", gate.classCode); //#11-卡戶等級	
		setValue("TRANS_TYPE", gate.transType); //#12-ISO交易類別
		setValue("PROC_CODE", gate.isoField[3]); //#13-DE03_交易處理碼
		setValue("TRACE_NO", gate.traceNo); //#14-DE11_追蹤號碼
		setValue("MCC_CODE", gate.mccCode); //#15-DE18_MCC代碼
		setValue("BANK_COUNTRY", gate.isoField[19]); //#16-DE19_收單行國家碼
		setValue("POS_MODE", gate.isoField[22]); //#17-DE22_POS_ENTRY_MODE
		setValue("COND_CODE", gate.isoField[25]); //#18-DE25_POS_CONDITION_MODE
		setValue("STAND_IN",gate.isoField[32]); //#19-DE32_收單行代碼(配合風險特店檢查，所有來源的資料都必須帶入完整的收單行代號，包含人工授權)
		setValue("ISO_RESP_CODE", gate.sgIsoRespCode); //#20-DE39_交易回覆碼
		setValue("ISO_ADJ_CODE", ""); //#21-此欄位無使用
		setValue("POS_TERM_ID", gate.isoField[41]); //#22-DE41_POS/ATM代號
		setValue("TERM_ID", gate.isoField[41]); //#23-DE41_POS/ATM代號(多餘)
		setValue("MCHT_NO", gate.isoField[42]); //#24-特店代號
		setValue("MCHT_NAME", gate.merchantName); //#25-特店名稱
		setValue("MCHT_CITY_NAME", gate.merchantCityName); //#26-特店所在城市名稱
		setValue("MCHT_CITY", gate.merchantCity); //#27-特店所在城市代號
		setValue("MCHT_COUNTRY", getValue("CountryCode")); //#28-特店國家代號_country code 3轉2
		setValue("EFF_DATE_END", gate.effDateEnd); //#29-CCAS_卡片有效日期
		setValue("USER_EXPIRE_DATE", gate.expireDate); //#30-ISO輸入卡片有效日期
		setValue("RISK_TYPE", gate.mccRiskType); //#31-交易風險類別
		setValue("TX_CURRENCY", gate.isoField[49]); //#32-DE49交易幣別
		setValue("CONSUME_COUNTRY", gate.merchantCountry); //#33-消費國家代碼
		setValue("ORI_AMT", ""+gate.oriAmount); //#34-消費金額(原幣)
		setValue("NT_AMT", ""+gate.ntAmt); //#35-消費金額(台幣)
		setValue("AUTH_STATUS_CODE", gate.authStatusCode); //#36-授權結果
		setValue("TX_REMARK", ""); //#37-交易備註_此欄位無使用
		setValue("AUTH_REMARK", gate.authRemark); //#38-授權人員備註說明	
		setValue("AUTH_USER", gate.authUser); //#39-授權人員代碼
		setValue("APR_USER", gate.approveUser); //#40-放行人員代碼     
		setValue("CCAS_AREA_FLAG", gate.areaType); //#41-國內/國外註記
		setValue("AUTH_TYPE", gate.authType); //#42-授權類別
		setValue("CARD_STATUS", gate.cardStatus); //#43-卡片狀態_此欄位無使用
		setValue("LOGIC_DEL", gate.logicDel); //#44-撤掛註記
		setValue("MTCH_FLAG", "N"); //#45-帳單比對註記
		setValue("MTCH_DATE", ""); //#46-帳單比對日期
		setValue("BALANCE_FLAG", ""); //#47-餘額註記_此欄位無使用
		setValue("TO_ACCU_MTCH", ""); //#48-累計帳單比對註記_此欄位無使用
		if (gate.rollbackP2) {
			setValueDouble("CURR_OTB_AMT", gate.otbAmt); //#49-當時可用額度餘額(P2)
			setValue("CURR_TOT_LMT_AMT", Double.toString(gate.finalTotLimit) ); //#50-當時依據總額度(P2)
			setValueInt("CURR_TOT_STD_AMT",getBaseLimit4AllTypeCard()); //#51-當時依據標準額度(P2)
			setValue("CURR_TOT_CASH_AMT", Double.toString(gate.currTotCashAmt)); //#53-累計預借現金金額(P2)
		}
		else {
			if ("C".equals(gate.mccRiskAmountRule)) {  /*預借現金總額度檢查*/
				setValueDouble("CURR_OTB_AMT", gate.otbAmtCash); //#49-當時可用額度餘額(P3預借現金)
				setValue("CURR_TOT_LMT_AMT", Double.toString(gate.realCreditCardBaseLimitOfCash) ); //#50-當時依據總額度(P3預借現金)
				if (gate.isChildCard) {
					if (gate.cbOtb < gate.otbAmtCash) {
						setValueDouble("CURR_OTB_AMT", gate.cbOtb); //#49-當時可用額度餘額(P3預借現金)
						if (gate.isChildAdj) {
							setValue("CURR_TOT_LMT_AMT", getValue("CardBaseChildCardTmpLimit") ); //#50-當時依據總額度(P3子卡調整額度)
						}
						else {
							setValue("CURR_TOT_LMT_AMT", getValue("INDIV_CRD_LMT") ); //#50-當時依據總額度(P3子卡原額度)
						}
					}
				}
			}
			else {
				if (gate.isChildCard) {
					setValueDouble("CURR_OTB_AMT", gate.cbOtb); //#49-當時可用額度餘額(P3子卡額度)
					if (gate.isChildAdj) {
						setValue("CURR_TOT_LMT_AMT", getValue("CardBaseChildCardTmpLimit") ); //#50-當時依據總額度(P3子卡調整額度)
					}
					else {
						setValue("CURR_TOT_LMT_AMT", getValue("INDIV_CRD_LMT") ); //#50-當時依據總額度(P3子卡原額度)
					}
				}
				else {
					setValueDouble("CURR_OTB_AMT", gate.otbAmt); //#49-當時可用額度餘額(P3)
					setValue("CURR_TOT_LMT_AMT", Double.toString(gate.realCreditCardBaseLimit) ); //#50-當時依據總額度(P3)
				}
			}
			setValueInt("CURR_TOT_STD_AMT", getBaseLimit()); //#51-當時依據標準額度(P3)
			setValue("CURR_TOT_CASH_AMT", Double.toString(gate.currTotCashAmt)); //#53-累計預借現金金額(P3)
		}
		setValue("CURR_TOT_TX_AMT", Double.toString(gate.riskTradeMonthAmt)); //#52-當月累計一般消費金額
		setValue("CURR_TOT_UNPAID", Double.toString(gate.curTotalUnpaidOfPersonal)); //#54-當月累計消費金額
		setValue("STAND_IN_REASON", gate.standInReason); //#55-代行原因
		setValue("AUTH_UNIT", gate.authUnit ); //#56-授權單位
		setValue("CACU_AMOUNT", gate.cacuAmount); //#57-計入OTB註記
		setValue("CACU_CASH", gate.cacuCash); //#58-計入OTB預借註記
		setValue("CACU_FLAG", gate.cacuFlag); //#59-專款專用註記
		setValue("STAND_IN_RSPCODE", gate.standInRspcode); //#60-STAND IN 回覆碼(來源)
		setValue("STAND_IN_ONUSCODE", gate.standInOnuscode ); //#61-Stand In 回覆碼(OnUs)
		setValue("TX_AMT_PCT", Double.toString(gate.txAmtPct)); //#62-交易金額%_此欄位無使用
		setValue("TX_CVC2", gate.dualCurr4Bill); //#63-雙幣卡轉換後結帳幣別
		setValue("AE_TRANS_AMT", gate.aeTransAmt); //#64-AE交易金額_欄位無使用
		setValue("ROC", gate.roc); //#65-AE交易金額_此欄位無使用
		setValue("ONLINE_REDEEM", gate.loyaltyTxId); //#66-折抵交易型態
		setValue("IBM_BIT33_CODE", gate.bankBit33Code); //#67-銀行交易碼
		setValue("IBM_BIT39_CODE", gate.bankBit39Code); //#68-銀行回覆碼(主機、ACER、OEMPAY)
		setValue("ACCT_NO", ""); //#69-帳戶帳號_此欄位無使用
		if (gate.isAuthVip) {
			setValue("VIP_CODE", "Y"); //#70-是否符合免照會VIP
		}
		else {
			setValue("VIP_CODE", "N"); //#70-是否符合免照會VIP
		}		
		setValue("EC_FLAG", gate.eci); //#71-ECI旗標
		setValue("CVD_PRESENT", gate.cvdPresent); //#72-CVD
		if ( gate.ecTrans ) {
			setValue("EC_IND", "Y"); //#73-EC識別碼
		}
		setValue("UCAF", gate.f48T42Eci); //#74-UCAF代碼Electronic Commerce Indicator
		setValue("CAVV_RESULT", gate.cavvResult); //#75-CAVV結果
		if ( gate.speedTrain ) {
			setValue("TRAIN_FLAG", "Y"); //#76-高鐵交易註記
		}
		setValue("GROUP_CODE", gate.groupCode); //#77-團體代號
		setValue("FALLBACK", gate.fallback); //#78-Fallback交易註記
		setValue("FRAUD_CHK_RSLT", gate.tpanFraudChk); //#79-偽卡檢查結果
		setValue("AC_VERY_RSLT", gate.tpanAcResult); //#80-AC驗証結果
		setValue("V_CARD_NO", gate.tpanTicketNo); //#81-HCE卡號/TPAN卡號/票證卡號
		setValue("AUTH_SEQNO", gate.authSeqno); //#82-授權流水號(yymmddnnnnnn)
		if (gate.isDebitCard) {  //#83-信用金融卡旗標：C.信用卡,D.金融卡
			setValue("VDCARD_FLAG", "D");
		}
		else {
			setValue("VDCARD_FLAG", "C"); 
		}
		setValue("REVERSAL_FLAG", gate.reversalFlag); //#84-沖銷註記
		setValue("TRANS_CODE", gate.transCode); //#85-交易代碼
		setValue("CARD_ACCT_IDX", gate.cardAcctIdx); //#86-卡戶基本檔Index No
		setValue("REF_NO", gate.refNo); //#87-DE37沖銷原始序號
		setValue("ORI_AUTH_NO", gate.oriAuthNo); //#88-DE38原始授權碼
		setValue("CRT_DATE", gb.getSysDate()); //#89-建檔日期
		setValue("CRT_TIME", gate.txTime); //#90-建檔時間
		setValue("CRT_USER", ConstObj.CRT_USER); //#91-建檔人員
		setValue("CHG_DATE", ""); //#92-最近更新日期_此欄位無使用
		setValue("CHG_TIME", ""); //#93-最近更新時間_此欄位無使用
		setValue("CHG_USER", ""); //#94-最近更新人員_此欄位無使用
		setValue("MOD_USER", ConstObj.MOD_USER); //#95-異動人員
		setTimestamp("MOD_TIME", gb.getgTimeStamp()); //#96-異動時間
		setValue("MOD_PGM", gb.getSystemName()); //#97-異動程式或系統
		setValue("MOD_SEQNO", gate.isoField[11]); //#98-異動註記_無效註記
		setValue("EDC_CODE", gate.edcTradeFunctionCode ); //#99-EDC交易功能碼
		setValue("AUTH_SOURCE", gate.authSource); //#100-授權來源
		setValue("TXN_IDF", gate.traceId); //#101-交易識別碼 MC(Trace ID)
		setValue("TSC_TXN_DATETIME", gate.f58T80); //#102-悠遊卡加值交易需帶入端末設備日期時間
		setValue("TSC_TXN_POS", String.valueOf(gb.durationTime(gate.startTime))); //#103-原悠遊卡端末交易序號_V1.00.06 新增交易Duration Time
		setValue("BONUS_AMT", gate.remainingCreditAmt); //#104-紅利抵扣支付金額
		setValue("CURR_NT_AMT", String.valueOf(gate.dualAmt4Bill)); //#105-雙幣卡轉換後結帳金額
		setValue("CURR_ORI_AMT", String.valueOf(gate.dualRate4OriToUsd)); //#106-雙幣卡本地貨幣對美金匯率
		setValue("CURR_RATE", String.valueOf(gate.dualRate4UsdToTwd)); //#107-雙幣卡美金對台幣匯率
		setValue("RISK_SCORE", Double.toString(gate.riskFactorScore)); //#108-取得風險分數 riskFactor
		setValue("TX_SEQ", gate.imsLockSeqNo); //#109-圈存序號
		setValueDouble("VD_LOCK_NT_AMT", gate.lockAmt); //#110-VD交易之實際圈存金額(含手續費)
		setValue("ADJ_DATE", ""); //#111-調整圈存金額之日期_此欄位無使用
		setValue("ADJ_TIME", ""); //#112-調整圈存金額之時間_此欄位無使用
		setValue("UNLOCK_FLAG", gate.unlockFlag); //#113-解圈註記:M人工解圈
		setValue("STATUS_CODE", ""); //#114-處理階段_此欄位無使用
		setValue("BONUS_POINT", gate.pointBalance); //#115_剩餘點數
		setValue("UNIT_PRICE", gate.everyAmt); //##116_每期金額
		setValue("TOT_TERM", gate.divNum); //#117-分期期數
		setValue("REDEEM_POINT", gate.pointRedemption); //#118-扣抵點數
		setValue("REDEEM_AMT", gate.paidCreditAmt); //#119-抵扣金額
		setValue("INSTALLMENT_TYPE", gate.divMark); //#120-分期種類
		setValue("FIRST_PRICE", gate.firstAmt); //#121-首期金額
		setValue("ORI_AUTH_SEQNO", getValue("AuthTxLogAuthSeqNo_SrcTrans")); //#122-沖正時原始交易碼
		setValue("ORI_TXN_IDF", gate.f58T60); //#123-原始交易識別碼 MC(Trace ID)
		setValue("ORI_CARD_NO", getValue("CrdCardOriCardNo")); //#124-原始卡號(V1.00.04 修改子卡月累績消費邏輯-改用原始卡號)

		if (!insertTable()) { 
			gb.showLogMessage("I","insertAuthTxLog(): error error!");
			return false;
		}
		return true;

	}
	

	public void setAuthStatusCode() {
		gb.showLogMessage("I","setAuthStatusCode(): started!");
		
		String slKey = "";
		if ("00".equals(gate.isoField[39])) {
			if ("".equals(gate.sgKey4OkTrans))
				gate.authStatusCode = gate.isoField[39];
			else {
				String slSql=""; 

				try {

					daoTable = addTableOwner("CCA_SYS_PARM3");	


					selectSQL ="NVL(SYS_DATA1,'00') as Parm3SysData1AsOkCode ";
					whereStr="where SYS_ID=?  AND SYS_KEY=?";
					setString(1,"AUTO");
					setString(2,gate.sgKey4OkTrans);
					selectTable();
					if ( !"Y".equals(notFound) ) {
						String slParm3SysData1AsOkCode = getValue("Parm3SysData1AsOkCode").trim();
						if (slParm3SysData1AsOkCode.length()>=2)
							gate.authStatusCode = slParm3SysData1AsOkCode.substring(0, 2);
						else {
							gate.authStatusCode = "";
							
						}
					}

				}
				catch (Exception e) {
					e.getMessage();
				}


			}
		}


	}
	private String getRealData(int npDataLenSize, String spContent) {

		String slResult = "";

		if (spContent.length()>=npDataLenSize) {
			slResult = spContent.substring(npDataLenSize,spContent.length());
		}
		return slResult;
	}

	// 更新 CCS_STA_TX_UNORMAL
	public boolean updateStaTxUnormal()  {

		gb.showLogMessage("I","updateStaTxUnormal : started");

		boolean blResult = true;
		try {
			daoTable  = addTableOwner("CCA_STA_TX_UNORMAL");
			updateSQL = "TX_CNT =  ?,"
					+ "TX_AMT =  ? ";


			//whereStr="WHERE BIN_NO  = :pBinNo "
			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND RESP_CODE= ? "
					+"AND RISK_TYPE= ? "
					+"AND AREA_TYPE= ? ";
			setInt(1, gate.ngStaTxUnNormalTxCnt);
			setInt(2, gate.ngStaTxUnNormalTxAmt);

			setString(3, gate.sgStaTxUnNormalMccBinNo);
			setString(4, ""+gate.sgStaTxUnNormalGroupCode);

			setInt(5, gate.ngTxSession);


			setString(6, gb.getSysDate());
			setString(7, gate.sgStaTxUnNormalRespCode );
			setString(8, gate.sgStaTxUnNormalRiskType);
			setString(9, "T");

			/*
            daoTable  = "CCA_STA_TX_UNORMAL";
            updateSQL = "TX_CNT =  :pTxCnt,"
                      + "TX_AMT =  :pTxAmt ";


   			whereStr="WHERE BIN_NO  = :pBinNo "
						+"AND GROUP_CODE = :pGroupCode "
						+"AND TX_SESSION= :pTxSession "
						+"AND STA_DATE= :pStaDate "
						+"AND RESP_CODE= :pRespCode "
						+"AND RISK_TYPE= :pRiskType "
						+"AND AREA_TYPE= :pAreaType ";
            setValue("pTxCnt",""+gate.nG_StaTxUnNormalTxCnt);
            setValue("pTxAmt",""+gate.nG_StaTxUnNormalTxAmt);

   			setValue("pBinNo", ""+gate.sG_StaTxUnNormalMccBinNo);
   			setValue("pGroupCode", ""+gate.sG_StaTxUnNormalGroupCode);

   			setValue("pTxSession", ""+gate.nG_TxSession);


   			setValue("pStaDate", gb.sysDate);
   			setValue("pRespCode", gate.sG_StaTxUnNormalRespCode );
   			setValue("pRiskType", gate.sG_StaTxUnNormalRiskType);
   			setValue("pAreaType", "T");
			 */
			updateTable();

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public void insertCcaDebitBil() {


		gb.showLogMessage("I","insertCcaDebitBil : start");

		try {


			String slAcqId = gate.isoField[32];

			String slBillTransType= gate.transType;

			if ( (gate.forcePosting) || (gate.preAuthComp) ) {
				slBillTransType= "0200";
			}
			else if ( (gate.refund) || (gate.purchAdjust)|| (gate.refundAdjust)|| (gate.cashAdjust) ) {
				slBillTransType= "0220";
			}

			daoTable = addTableOwner("CCA_DEBIT_BIL");

			setValue("AUTH_NO", gate.isoField[38]);
			setValue("CARD_NO", gate.cardNo);
			setValue("TRANS_TYPE", slBillTransType);
			setValue("TX_DATE", gb.getSysDate());
			setValue("TX_TIME", gb.getSysTime());
			setValue("ACQ_ID", slAcqId);
			setValue("MCHT_NO", gate.merchantNo);

			setValue("TRACE_NO", gate.isoField[11]);
			setValue("REF_NO", gate.isoField[37]);

			setValue("TRANS_AMT",""+gate.ntAmt);
			setValue("SOURCE_CURR", gate.isoField[49]);
			setValue("PROCESS_FLAG", "N");
			setValue("CCAS_AREA_FLAG", gate.areaType);
			setValue("CONSUME_COUNTRY", gate.merchantCountry);
			setValue("MCC_CODE", gate.mccCode);

			setValue("RISK_TYPE", gate.mccRiskType);
			setValue("POS_TERM_ID", gate.isoField[41]);

			setValue("MOD_USER", ConstObj.MOD_USER);
			setTimestamp("MOD_TIME", gb.getgTimeStamp());






			//setTimestamp("MOD_TIME", HpeUtil.getCurTimestamp());










			//setValue("TX_CURRENCY", gate.isoField[49]);









			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	// 新增 STA_TX_UNORMAL
	public boolean  insertStaTxUnormal() {


		gb.showLogMessage("I","insertStaTxUnormal : start");
		boolean blResult = true;
		try {
			daoTable = addTableOwner("CCA_STA_TX_UNORMAL");

			setValue("BIN_NO", gate.sgStaTxUnNormalMccBinNo);
			setValue("GROUP_CODE", gate.sgStaTxUnNormalGroupCode);
			setValue("TX_SESSION",""+gate.ngTxSession);
			setValue("STA_DATE",gb.getSysDate());
			setValue("AREA_TYPE","T");
			setValue("BIN_TYPE",gate.binType);//By Howard

			setValue("RESP_CODE",gate.sgStaTxUnNormalRespCode );
			setValue("RISK_TYPE",gate.sgStaTxUnNormalRiskType);


			setValue("TX_CNT","1");
			setValue("TX_AMT", ""+gate.ntAmt);

			/*
            setValue("BIN_NO", gate.sG_StaTxUnNormalMccBinNo);
            setValue("GROUP_CODE", gate.sG_StaTxUnNormalGroupCode);
            setValueInt("TX_SESSION",gate.nG_TxSession);
            setValue("STA_DATE",gb.sysDate);
            setValue("AREA_TYPE","T");
            setValue("BIN_TYPE",gate.binType);//By Howard

            setValue("RESP_CODE",gate.sG_StaTxUnNormalRespCode );
            setValue("RISK_TYPE",gate.sG_StaTxUnNormalRiskType);


            setValueInt("TX_CNT",1);
            setValueInt("TX_AMT", (int)gate.nt_amt);
			 */




			insertTable();

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}
	/**
	 * 讀取Bil_Install_Log紅利兌換交易
	 * V1.00.37 P3紅利兌換處理方式調整
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	public void selectBilInstallLog4Redeem(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectBilInstallLog4Redeem(): started!");

		pBilOBase.hInlgPointsAmt = 0;
		pBilOBase.hPointRede = 0;
		pBilOBase.hInlgReversalFlag = "";
		pBilOBase.hInlgRefundFlag = "";

		try {
			daoTable = addTableOwner("bil_install_log");
			selectSQL ="rowid  as rowid,"
					+ "points_amt,"
					+ "points_redeem * (-1) as h_point_rede,"
					+ "reversal_flag,"
					+ "refund_flag ";
			whereStr="WHERE auth_id_resp_38   = ? "
					+ "and card_no    = ?"
					+ "and mcht_id_42   = ?"
					+ "and resp_flag_39    = '00' "
					+ "and amt_tx_4     = ?  "
					+ "and mod_pgm     = 'BilO101' "
					+ "fetch first 1 rows only ";



			setString(1, pBilOBase.hAuthIdResp);
			setString(2, pBilOBase.hCardNo);
			setString(3, pBilOBase.hAccpIdCd);
			setString(4, pBilOBase.hAmtx);

			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "select_bil_install_log not found");
				pBilOBase.hRespCd = "X3";
				return;

			}
			else {
				pBilOBase.hInlgRowid = getValue("rowid");
				pBilOBase.hInlgPointsAmt = getDouble("points_amt");
				pBilOBase.hPointRede = getInteger("h_point_rede");
				pBilOBase.hInlgReversalFlag = getValue("reversal_flag");
				pBilOBase.hInlgRefundFlag = getValue("refund_flag");

			}

		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("select_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "X3";
			return;
		}

		if ("Y".equals(pBilOBase.hInlgReversalFlag) || "Y".equals(pBilOBase.hInlgRefundFlag)) {
			gb.showLogMessage("D", "select_bil_install_log 2 error");
			pBilOBase.hRespCd = "62";
			return;
		}

	}

	public void selectBilInstallLog(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectBilInstallLog(): started!");

		pBilOBase.hTempConfirmFlag = "";
		pBilOBase.hTempX12 = "";
		pBilOBase.hInlgReversalFlag = "";
		pBilOBase.hInlgRefundFlag = "";
		pBilOBase.hInlgInstallResp633 = 0;
		pBilOBase.hInlgInstallResp634 = 0;
		pBilOBase.hInlgInstallResp635 = 0;
		pBilOBase.hInlgInstallResp636 = 0;

		/**


	   selectSQL ="MERCHANTNO, IFVERIFYCUSTID" ;

	   whereStr="WHERE MERCHANTNO =? ";
       setString(1,gate.merchantNo);

       selectTable();
       if ( "Y".equals(notFound) ) {
    	   gb.showLogMessage("D","function: TA.ifMerchant4VerifyCustID -- can not find data. MerchantNo is  ",gate.merchantNo + "--");
    	   sL_Result = "";

       }
       sL_Result = getValue("IFVERIFYCUSTID");

		 */
		try {
			daoTable = addTableOwner("bil_install_log");
			selectSQL ="rowid  as rowid,"
					+ "decode(apr_flag,'','N',apr_flag) h_temp_confirm_flag,"
					+ "decode(amt_tx_4,'','0',amt_tx_4) h_temp_x12,"
					+ "install_resp_63_2,"
					+ "install_resp_63_3,"
					+ "install_resp_63_4,"
					+ "install_resp_63_5,"
					+ "install_resp_63_6,"
					+ "reversal_flag,"
					+ "refund_flag ";
			whereStr="WHERE auth_id_resp_38   = ? "
					+ "and card_no    = ?"
					+ "and mcht_id_42   = ?"
					+ "and resp_flag_39    = '00' "
					+ "and mod_pgm     = 'BilO201' "
					+ "fetch first 1 rows only ";
			setString(1, pBilOBase.hAuthIdResp);
			setString(2, pBilOBase.hCardNo);
			setString(3, pBilOBase.hAccpIdCd);
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "select_bil_install_log not found");
				pBilOBase.hRespCd = "X3";
				return;
			}
			else {
				pBilOBase.hInlgRowid = getValue("rowid");
				pBilOBase.hTempConfirmFlag = getValue("h_temp_confirm_flag");
				pBilOBase.hTempX12 = getValue("h_temp_x12");
				pBilOBase.hInlgInstallResp632 = getValue("install_resp_63_2");
				pBilOBase.hInlgInstallResp633 = Integer.parseInt(getValue("install_resp_63_3"));
				pBilOBase.hInlgInstallResp634 = Double.parseDouble(getValue("install_resp_63_4"));
				pBilOBase.hInlgInstallResp635 = Double.parseDouble(getValue("install_resp_63_5"));
				pBilOBase.hInlgInstallResp636 = Double.parseDouble(getValue("install_resp_63_6"));
				pBilOBase.hInlgReversalFlag = getValue("reversal_flag");
				pBilOBase.hInlgRefundFlag = getValue("refund_flag");

			}


		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("select_bil_install_log 失敗 = [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "X3";
			return;
		}

		if (Double.parseDouble(pBilOBase.hTempX12) != Double.parseDouble(pBilOBase.hAmtx)) {
			//if (CommCrdRoutine.str2long(h_temp_x12) != CommCrdRoutine.str2long(h_amt_x)) {
			gb.showLogMessage("D", "select_bil_install_log 61 error");
			pBilOBase.hRespCd = "61";
			return;
		}

		if ("Y".equals(pBilOBase.hInlgReversalFlag)  || "Y".equals(pBilOBase.hInlgRefundFlag)) {
			gb.showLogMessage("D", "select_bil_install_log 62 error");
			pBilOBase.hRespCd = "62";
			return;
		}

		pBilOBase.hOrderNo = pBilOBase.hInlgInstallResp632;
		pBilOBase.hContInstallTotTerm = pBilOBase.hInlgInstallResp633;
		pBilOBase.hContFirstRemdAmt = pBilOBase.hInlgInstallResp634;
		pBilOBase.hContUnitPrice = pBilOBase.hInlgInstallResp635;
		pBilOBase.hContCltFeesAmt = pBilOBase.hInlgInstallResp636;

		return;

	}

	public void getBilInstallLog(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","getBilInstallLog(): started!");

		pBilOBase.hTempConfirmFlag = "";
		pBilOBase.hTempX12 = "";
		pBilOBase.hInlgReversalFlag = "";
		pBilOBase.hInlgRefundFlag = "";
		pBilOBase.hInlgInstallResp633 = 0;
		pBilOBase.hInlgInstallResp634 = 0;
		pBilOBase.hInlgInstallResp635 = 0;
		pBilOBase.hInlgInstallResp636 = 0;

		/**


	   selectSQL ="MERCHANTNO, IFVERIFYCUSTID" ;

	   whereStr="WHERE MERCHANTNO =? ";
       setString(1,gate.merchantNo);

       selectTable();
       if ( "Y".equals(notFound) ) {
    	   gb.showLogMessage("D","function: TA.ifMerchant4VerifyCustID -- can not find data. MerchantNo is  ",gate.merchantNo + "--");
    	   sL_Result = "";

       }
       sL_Result = getValue("IFVERIFYCUSTID");

		 */
		try {
			daoTable = addTableOwner("bil_install_log");
			selectSQL ="rowid  as rowid,"
					+ "decode(apr_flag,'','N',apr_flag) h_temp_confirm_flag,"
					+ "decode(amt_tx_4,'','0',amt_tx_4) h_temp_x12,"
					+ "install_resp_63_2,"
					+ "install_resp_63_3,"
					+ "install_resp_63_4,"
					+ "install_resp_63_5,"
					+ "install_resp_63_6,"
					+ "reversal_flag,"
					+ "refund_flag ";
			whereStr="WHERE auth_id_resp_38   = ? "
					+ "and card_no    = ?"
					+ "and mcht_id_42   = ?"
					+ "and resp_flag_39    = '00' "
					+ "and mod_pgm     = 'BilO201' "
					+ "fetch first 1 rows only ";
			setString(1, pBilOBase.hAuthIdResp);
			setString(2, pBilOBase.hCardNo);
			setString(3, pBilOBase.hAccpIdCd);
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "select_bil_install_log not found");
				pBilOBase.hRespCd = "X3";
				return;
			}
			else {
				pBilOBase.hInlgRowid = getValue("rowid");
				pBilOBase.hTempConfirmFlag = getValue("h_temp_confirm_flag");
				pBilOBase.hTempX12 = getValue("h_temp_x12");
				pBilOBase.hInlgInstallResp632 = getValue("install_resp_63_2");
				pBilOBase.hInlgInstallResp633 = Integer.parseInt(getValue("install_resp_63_3"));
				pBilOBase.hInlgInstallResp634 = Double.parseDouble(getValue("install_resp_63_4"));
				pBilOBase.hInlgInstallResp635 = Double.parseDouble(getValue("install_resp_63_5"));
				pBilOBase.hInlgInstallResp636 = Double.parseDouble(getValue("install_resp_63_6"));
				pBilOBase.hInlgReversalFlag = getValue("reversal_flag");
				pBilOBase.hInlgRefundFlag = getValue("refund_flag");

			}


		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("select_bil_install_log 失敗 = [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "X3";
			return;
		}



		return;

	}

	public void getBilTxnCode(BilOBase pBilOBase, String spTxnKind, String spErrorCode) throws Exception {
		gb.showLogMessage("I","getBilTxnCode(): started!");
		try {
			daoTable = addTableOwner("bil_txn_code");
			selectSQL ="iso_code, "
                      +"resp_desc";
//			whereStr="WHERE txn_kind = '2' "
			whereStr="WHERE txn_kind = ? "
					+"and resp_flag = ? ";

			setString(1, spTxnKind);
			setString(2, pBilOBase.hRespCd);
			selectTable();
			//kevin:利用h_temp_resp_cd來放置ECS拒絕原因 
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "**** select bil_txn_code not found");
				pBilOBase.hTempRespCd = "其他原因 - " + pBilOBase.hRespCd;
				pBilOBase.hRespCd = spErrorCode;//D3

			}
			else {
//				P_BilOBase.h_temp_resp_cd = getValue("iso_code");
//				P_BilOBase.h_resp_cd = P_BilOBase.h_temp_resp_cd;   
				pBilOBase.hTempRespCd = getValue("resp_desc") + " - " + pBilOBase.hRespCd;
				pBilOBase.hRespCd = getValue("iso_code");   
			}

		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("**** select bil_txn_code error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = spErrorCode;
		}

	}

	public void updateCycBpcd4Redeem(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateCycBpcd4Redeem(): started!");
		try {
			daoTable   = addTableOwner("cyc_bpcd");
			updateSQL  = " use_bp          = use_bp   + ?,";
			updateSQL += " net_ttl_bp      = net_ttl_bp  - ?,";
			updateSQL += " net_ttl_notax_1 = net_ttl_notax_1 - ?,";
			updateSQL += " net_ttl_1       = net_ttl_1  - ?,";
			updateSQL += " mod_user        = 'BIL',";
			updateSQL += " mod_time        = sysdate,";
			updateSQL += " mod_pgm         = ?";
			whereStr   = "where rowid      = ? ";
			setDouble(1, pBilOBase.hPointRede);
			setDouble(2, pBilOBase.hPointRede);
			setDouble(3, pBilOBase.hPointRede);
			setDouble(4, pBilOBase.hPointRede);
			setString(5, pBilOBase.prgmId);
			setRowId( 6, pBilOBase.hBpcdRowid);
			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_cyc_bpcd not found");
				pBilOBase.hRespCd = "22";
				return;
			}
		} catch (Exception ex) {
			gb.showLogMessage("E", String.format("update_cyc_bpcd error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "22";
			return;
		}
	}

	public void updateCycBpcd(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateCycBpcd(): started!");
		try {
			daoTable = addTableOwner("cyc_bpcd");
			updateSQL = "use_bp   = use_bp   + ?,";
			updateSQL += " net_ttl_bp  = net_ttl_bp  - ?,";
			updateSQL += " trans_bp  = ?,";
			updateSQL += " net_ttl_tax_1 = ?,";
			updateSQL += " net_ttl_tax_2 = ?,";
			updateSQL += " net_ttl_tax_3 = ?,";
			updateSQL += " net_ttl_tax_4 = ?,";
			updateSQL += " net_ttl_tax_5 = ?,";
			updateSQL += " net_ttl_notax_1 = ?,";
			updateSQL += " net_ttl_notax_2 = ?,";
			updateSQL += " net_ttl_notax_3 = ?,";
			updateSQL += " net_ttl_notax_4 = ?,";
			updateSQL += " net_ttl_notax_5 = ?,";
			updateSQL += " net_ttl_1  = ?,";
			updateSQL += " net_ttl_2  = ?,";
			updateSQL += " net_ttl_3  = ?,";
			updateSQL += " net_ttl_4  = ?,";
			updateSQL += " net_ttl_5  = ?,";
			updateSQL += " mod_user   = 'BIL',";
			updateSQL += " mod_time   = sysdate,";
			updateSQL += " mod_pgm    = ?";
			whereStr = "where rowid   = ? ";
			setInt(1, pBilOBase.hPointRede);
			setInt(2, pBilOBase.hPointRede);
			setDouble(3, pBilOBase.hBpcdTransBp);
			setDouble(4, pBilOBase.hBpcdNetTtlTax1);
			setDouble(5, pBilOBase.hBpcdNetTtlTax2);
			setDouble(6, pBilOBase.hBpcdNetTtlTax3);
			setDouble(7, pBilOBase.hBpcdNetTtlTax4);
			setDouble(8, pBilOBase.hBpcdNetTtlTax5);
			setDouble(9, pBilOBase.hBpcdNetTtlNotax1);
			setDouble(10, pBilOBase.hBpcdNetTtlNotax2);
			setDouble(11, pBilOBase.hBpcdNetTtlNotax3);
			setDouble(12, pBilOBase.hBpcdNetTtlNotax4);
			setDouble(13, pBilOBase.hBpcdNetTtlNotax5);
			setDouble(14, pBilOBase.hBpcdNetTtlTax1 + pBilOBase.hBpcdNetTtlNotax1);
			setDouble(15, pBilOBase.hBpcdNetTtlTax2 + pBilOBase.hBpcdNetTtlNotax2);
			setDouble(16, pBilOBase.hBpcdNetTtlTax3 + pBilOBase.hBpcdNetTtlNotax3);
			setDouble(17, pBilOBase.hBpcdNetTtlTax4 + pBilOBase.hBpcdNetTtlNotax4);
			setDouble(18, pBilOBase.hBpcdNetTtlTax5 + pBilOBase.hBpcdNetTtlNotax5);
			setString(19, pBilOBase.prgmId);
			setRowId( 20, pBilOBase.hBpcdRowid);
			updateTable();
			if("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_cyc_bpcd not found");
				pBilOBase.hRespCd = "22";
			}

		} catch (Exception e) {
			// TODO: handle exception
			pBilOBase.hRespCd = "22";
		}
	}
	public void updateBilInstallLog3(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstallLog3(): started!");

		pBilOBase.hInlgRefundFlag = "Y";

		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " reversal_flag = 'Y',";
			updateSQL += " resp_flag_39  = '99',";
			whereStr = "where rowid      = ? ";
			setRowId( 1, pBilOBase.hInlgRowid);
			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}

	public void updateBilInstallLog6(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstallLog6(): started!");

		pBilOBase.hInlgRefundFlag = "Y";

		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " install_resp_63_4  = install_resp_63_4 + ? ";

			whereStr = "where rowid      = ? ";
			setDouble(1, pBilOBase.hAmt);
			setRowId( 2, pBilOBase.hInlgRowid);

			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}


	public void updateBilInstallLog5(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstallLog5(): started!");

		pBilOBase.hInlgRefundFlag = "Y";

		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " reversal_flag = 'Y',";
			updateSQL += " refund_flag   = 'Y'";
			whereStr = "where rowid      = ? ";
			setRowId( 1, pBilOBase.hInlgRowid);
			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("I","update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_install_log error= [%s]"+ ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}

	public void updateBilInstalLog4(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstalLog4(): started!");

		pBilOBase.hInlgRefundFlag = "Y";

		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " reversal_flag = 'N',";
			updateSQL += " refund_flag = 'N',";
			updateSQL += " resp_flag_39 = '00' ";

			whereStr = "where auth_id_resp_38   = ?  ";
			whereStr += "and mcht_id_42   = ?  ";
			whereStr += "and card_no    = ?  ";
			whereStr += "and resp_flag_39    = '99'  ";
			whereStr += "and mod_pgm     = 'BilO201' ";

			setString(1, pBilOBase.hAuthIdResp);
			setString(2, pBilOBase.hAccpIdCd);
			setString(3, pBilOBase.hCardNo);

			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("I", "update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}
	public void updateBilInstallLog(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstallLog(): started!");

		pBilOBase.hInlgRefundFlag = "Y";

		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " reversal_flag = 'Y',";
			updateSQL += " resp_flag_39  = '99',";
			updateSQL += " refund_flag   = ?";
			whereStr = "where rowid      = ? ";
			setString(1, pBilOBase.hInlgRefundFlag);
			setRowId( 2, pBilOBase.hInlgRowid);
			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}

	public void updateBilInstallLog2(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilInstallLog2(): started!");


		try {
			daoTable   = addTableOwner("bil_install_log");
			updateSQL  = " install_resp_63_4  = install_resp_63_4 + ? ";
			whereStr = "where rowid      = ? ";
			setDouble(1, pBilOBase.hAmt);
			setRowId( 2, pBilOBase.hInlgRowid);
			updateTable();
			if ("Y".equals(notFound)) {
				gb.showLogMessage("D", "update_bil_install_log not found");
				pBilOBase.hRespCd = "13";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E",String.format("update_bil_install_log error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "13";
		}

	}

	public void insertBilInstallLog(BilOBase pBilOBase, boolean bpResetResp63Value) throws Exception {
		gb.showLogMessage("I","insertBilInstallLog(): started!");
		pBilOBase.hInlgCardNo = pBilOBase.hCardNo;
		pBilOBase.hInlgMsgType = pBilOBase.hMsgType;
		pBilOBase.hInlgProcessCode = pBilOBase.hProcessCd;
		pBilOBase.hInlgTxIndicator = pBilOBase.hTxIndicator;
		pBilOBase.hInlgAmtTx4 = pBilOBase.hAmtx;
		pBilOBase.hInlgDateTime7 = pBilOBase.hDateTime;
		pBilOBase.hInlgTraceNo11 = pBilOBase.hTraceNo;
		pBilOBase.hInlgExpireDate14 = pBilOBase.hExpireDate;
		pBilOBase.hInlgPosEntryMode22 = pBilOBase.hEntryMode;
		pBilOBase.hInlgPosConCode25 = pBilOBase.hConCode;
		pBilOBase.hInlgReferenceNo37 = pBilOBase.hReferenceNo;
		pBilOBase.hInlgAuthIdResp38 = pBilOBase.hAuthIdResp;
		pBilOBase.hInlgRespFlag39 = pBilOBase.hRespCd;
		pBilOBase.hInlgTermId41 = pBilOBase.hAccpTermId;
		pBilOBase.hInlgMchtId42 = pBilOBase.hAccpIdCd;

		pBilOBase.hInlgInstallData63 = pBilOBase.hProdCd;
		pBilOBase.hInlgInstallResp632 = pBilOBase.hOrderNo;
		pBilOBase.hInlgInstallResp631 = pBilOBase.hProdCd;
		pBilOBase.hInlgInstallResp633 = pBilOBase.hContInstallTotTerm;
		pBilOBase.hInlgInstallResp634 = pBilOBase.hContFirstRemdAmt;
		pBilOBase.hInlgInstallResp635 = pBilOBase.hContUnitPrice;
		pBilOBase.hInlgInstallResp636 = pBilOBase.hContCltFeesAmt;

		pBilOBase.hInlgConfirmFlag = "N";

		if (bpResetResp63Value) {
			if(!"00".equals(pBilOBase.hInlgRespFlag39)) {
				pBilOBase.hInlgInstallResp633 = 0;
				pBilOBase.hInlgInstallResp634 = 0;
				pBilOBase.hInlgInstallResp635 = 0;
				pBilOBase.hInlgInstallResp636 = 0;
			}

		}
		try {
			setValue("tx_date", pBilOBase.hInlgTxDate);
			setValue("tx_indicator", pBilOBase.hInlgTxIndicator);
			setValue("card_no", pBilOBase.hInlgCardNo);
			setValue("msg_type", pBilOBase.hInlgMsgType);
			setValue("process_code", pBilOBase.hInlgProcessCode);
			setValue("amt_tx_4", pBilOBase.hInlgAmtTx4);
			setValue("date_time_7", pBilOBase.hInlgDateTime7);
			setValue("trace_no_11", pBilOBase.hInlgTraceNo11);
			setValue("expire_date_14", pBilOBase.hInlgExpireDate14);
			setValue("pos_entry_mode_22", pBilOBase.hInlgPosEntryMode22);
			setValue("pos_con_code_25", pBilOBase.hInlgPosConCode25);
			setValue("reference_no_37", pBilOBase.hInlgReferenceNo37);
			setValue("auth_id_resp_38", pBilOBase.hInlgAuthIdResp38);
			setValue("resp_flag_39", pBilOBase.hInlgRespFlag39);
			setValue("term_id_41", pBilOBase.hInlgTermId41);
			setValue("mcht_id_42", pBilOBase.hInlgMchtId42);
			setValue("install_data_63", pBilOBase.hInlgInstallData63);
			setValue("install_resp_63_1", pBilOBase.hInlgInstallResp631);
			setValue("install_resp_63_2", pBilOBase.hInlgInstallResp632);

			setValue("install_resp_63_3", Integer.toString(pBilOBase.hInlgInstallResp633));
			//setValueInt("install_resp_63_3", P_BilOBase.h_inlg_install_resp_63_3);

			setValue("install_resp_63_4", Double.toString(pBilOBase.hInlgInstallResp634));
			//setValueDouble("install_resp_63_4", P_BilOBase.h_inlg_install_resp_63_4);

			setValue("install_resp_63_5", Double.toString(pBilOBase.hInlgInstallResp635));
			//setValueDouble("install_resp_63_5", P_BilOBase.h_inlg_install_resp_63_5);

			setValue("install_resp_63_6", Double.toString(pBilOBase.hInlgInstallResp636));
			//setValueDouble("install_resp_63_6", P_BilOBase.h_inlg_install_resp_63_6);

			setValue("points_redeem", Integer.toString(pBilOBase.hInlgPointsRedeem));
			//setValueInt("points_redeem", P_BilOBase.h_inlg_points_redeem);

			//setValueDouble("points_balance", P_BilOBase.h_inlg_points_balance);
			setValue("points_balance", Double.toString(pBilOBase.hInlgPointsBalance));

			setValue("acct_type", pBilOBase.hInlgAcctType);
			setValue("p_seqno", pBilOBase.hInlgPSeqno);
			setValue("apr_flag", pBilOBase.hInlgConfirmFlag);
			setValue("apr_date", pBilOBase.hInlgConfirmDate);
			setValue("mod_user", pBilOBase.prgmId);
			//setValue("mod_time", gb.sysDate + gb.sysTime);
			setTimestamp("MOD_TIME",gb.getgTimeStamp());
			setValue("mod_pgm", pBilOBase.prgmId);
			daoTable = addTableOwner("bil_install_log");
			insertTable();
			if ("Y".equals(dupRecord)) {
				gb.showLogMessage("D", "insert bil_install_log 重複");
				pBilOBase.hRespCd = "24";
			}
		} catch (Exception ex) {
			gb.showLogMessage("E", String.format("insert bil_install_log 失敗 = [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "24";
		}
	}
	public void insertBilInstallLog4Redeem2(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","insertBilInstallLog4Redeem2(): started!");
		pBilOBase.hInlgRealCardNo = pBilOBase.hCardNo;
		pBilOBase.hInlgMsgType = pBilOBase.hMsgType;
		pBilOBase.hInlgProcessCode = pBilOBase.hProcessCd;
		pBilOBase.hInlgTxIndicator = pBilOBase.hTxIndicator;
		pBilOBase.hInlgAmtTx4 = pBilOBase.hAmtx;
		pBilOBase.hInlgDateTime7 = pBilOBase.hDateTime;
		pBilOBase.hInlgTraceNo11 = pBilOBase.hTraceNo;
		pBilOBase.hInlgExpireDate14 = pBilOBase.hExpireDate;
		pBilOBase.hInlgPosEntryMode22 = pBilOBase.hEntryMode;
		pBilOBase.hInlgPosConCode25 = pBilOBase.hConCode;
		pBilOBase.hInlgReferenceNo37 = pBilOBase.hReferenceNo;
		pBilOBase.hInlgAuthIdResp38 = pBilOBase.hAuthIdResp;
		pBilOBase.hInlgRespFlag39 = pBilOBase.hRespCd;
		pBilOBase.hInlgTermId41 = pBilOBase.hAccpTermId;
		pBilOBase.hInlgMerchantId42 = pBilOBase.hAccpIdCd;
		pBilOBase.hInlgAcctType = pBilOBase.hCardAcctType;
		pBilOBase.hInlgPSeqno = pBilOBase.hCardPSeqno;
		pBilOBase.hInlgPointsRedeem = 0;
		pBilOBase.hInlgPointsBalance = pBilOBase.hBpcdNetTtlBp;
		pBilOBase.hInlgConfirmFlag = "Y";

		try {
			setValue("tx_date", pBilOBase.hInlgTxDate);
			setValue("tx_indicator", pBilOBase.hInlgTxIndicator);
			setValue("card_no", pBilOBase.hInlgRealCardNo);
			setValue("msg_type", pBilOBase.hInlgMsgType);
			setValue("process_code", pBilOBase.hInlgProcessCode);
			setValue("amt_tx_4", pBilOBase.hInlgAmtTx4);
			setValue("date_time_7", pBilOBase.hInlgDateTime7);
			setValue("trace_no_11", pBilOBase.hInlgTraceNo11);
			setValue("expire_date_14", pBilOBase.hInlgExpireDate14);
			setValue("pos_entry_mode_22", pBilOBase.hInlgPosEntryMode22);
			setValue("pos_con_code_25", pBilOBase.hInlgPosConCode25);
			setValue("reference_no_37", pBilOBase.hInlgReferenceNo37);
			setValue("auth_id_resp_38", pBilOBase.hInlgAuthIdResp38);
			setValue("resp_flag_39", pBilOBase.hInlgRespFlag39);
			setValue("term_id_41", pBilOBase.hInlgTermId41);
			setValue("mcht_id_42", pBilOBase.hInlgMerchantId42);
			setValue("install_data_63", pBilOBase.hInlgInstallData63);
			setValue("install_resp_63_1", pBilOBase.hInlgInstallResp631);
			setValue("install_resp_63_2", pBilOBase.hInlgInstallResp632);


			//setValueInt("install_resp_63_3", P_BilOBase.h_inlg_install_resp_63_3);
			setValue("install_resp_63_3", ""+pBilOBase.hInlgInstallResp633);

			//setValueDouble("install_resp_63_4", P_BilOBase.h_inlg_install_resp_63_4);
			setValue("install_resp_63_4", ""+pBilOBase.hInlgInstallResp634);


			//setValueDouble("install_resp_63_5", P_BilOBase.h_inlg_install_resp_63_5);
			setValue("install_resp_63_5", ""+pBilOBase.hInlgInstallResp635);

			//setValueDouble("install_resp_63_6", P_BilOBase.h_inlg_install_resp_63_6);
			setValue("install_resp_63_6", ""+pBilOBase.hInlgInstallResp636);

			//setValueInt("points_redeem", P_BilOBase.h_inlg_points_redeem);
			setValue("points_redeem", ""+pBilOBase.hInlgPointsRedeem);

			//setValueDouble("points_balance", P_BilOBase.h_inlg_points_balance);
			setValue("points_balance", ""+pBilOBase.hInlgPointsBalance);

			//setValueDouble("points_amt", P_BilOBase.h_inlg_points_amt);
			setValue("points_amt", ""+pBilOBase.hInlgPointsAmt);

			setValue("acct_type", pBilOBase.hInlgAcctType);
			setValue("p_seqno", pBilOBase.hInlgPSeqno);
			setValue("apr_flag", pBilOBase.hInlgConfirmFlag);
			setValue("apr_date", pBilOBase.hInlgConfirmDate);
			setValue("mod_user", pBilOBase.prgmId);
			//setValue("mod_time", gb.sysDate + gb.sysTime);
			setTimestamp("MOD_TIME",gb.getgTimeStamp());
			setValue("mod_pgm", pBilOBase.prgmId);
			daoTable = addTableOwner("bil_install_log");
			insertTable();
			if ("Y".equals(dupRecord)) {
				gb.showLogMessage("D", "insert bil_install_log 重複");
				pBilOBase.hRespCd = "24";
			}
		} catch (Exception ex) {
			gb.showLogMessage("E", String.format("insert bil_install_log 失敗 = [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "24";
		}

	}
	/**
	 * 寫入Bil_Install_Log紅利兌換交易
	 * V1.00.37 P3紅利兌換處理方式調整
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	public void insertBilInstallLog4RedeemProc(BilOBase pBilOBase, boolean bpPlusMinus1) throws Exception 
	{
		gb.showLogMessage("I","insertBilInstallLog4RedeemProc(): started!");


		pBilOBase.hInlgRealCardNo = pBilOBase.hCardNo;
		pBilOBase.hInlgMsgType     = pBilOBase.hMsgType;
		pBilOBase.hInlgProcessCode = pBilOBase.hProcessCd;
		pBilOBase.hInlgTxIndicator = pBilOBase.hTxIndicator;
		pBilOBase.hInlgAmtTx4     = pBilOBase.hAmtx;
		pBilOBase.hInlgDateTime7  = pBilOBase.hDateTime;
		pBilOBase.hInlgTraceNo11  = pBilOBase.hTraceNo;
		pBilOBase.hInlgExpireDate14    = pBilOBase.hExpireDate;
		pBilOBase.hInlgPosEntryMode22 = pBilOBase.hEntryMode;
		pBilOBase.hInlgPosConCode25   = pBilOBase.hConCode;
		pBilOBase.hInlgReferenceNo37   = pBilOBase.hReferenceNo;
		pBilOBase.hInlgAuthIdResp38   = pBilOBase.hAuthIdResp;
		pBilOBase.hInlgRespFlag39   = pBilOBase.hRespCd;
		pBilOBase.hInlgTermId41     = pBilOBase.hAccpTermId;
		pBilOBase.hInlgMerchantId42 = pBilOBase.hAccpIdCd;
		pBilOBase.hInlgAcctType      = pBilOBase.hCardAcctType;
		pBilOBase.hInlgPSeqno        = pBilOBase.hCardPSeqno;
		if (bpPlusMinus1) {
			pBilOBase.hInlgPointsRedeem = pBilOBase.hPointRede * (-1);
			pBilOBase.hInlgPointsBalance = pBilOBase.hBpcdNetTtlBp - pBilOBase.hPointRede;

		}
		else {
			pBilOBase.hInlgPointsRedeem  = pBilOBase.hPointRede;
			pBilOBase.hInlgPointsBalance = pBilOBase.hOldNetTtlBp - pBilOBase.hPointRede;
		}
		pBilOBase.hInlgConfirmFlag = "N";
		if(!"00".equals(pBilOBase.hInlgRespFlag39)) 
		{
			pBilOBase.hInlgPointsRedeem = 0;
			pBilOBase.hInlgPointsBalance = 0;
			pBilOBase.hInlgPointsAmt = 0;
		}

		try {
			setValue("tx_date", pBilOBase.hInlgTxDate);
			setValue("tx_indicator", pBilOBase.hInlgTxIndicator);
			setValue("card_no", pBilOBase.hInlgRealCardNo);
			setValue("msg_type", pBilOBase.hInlgMsgType);
			setValue("process_code", pBilOBase.hInlgProcessCode);
			setValue("amt_tx_4", pBilOBase.hInlgAmtTx4);
			setValue("date_time_7", pBilOBase.hInlgDateTime7);
			setValue("trace_no_11", pBilOBase.hInlgTraceNo11);
			setValue("expire_date_14", pBilOBase.hInlgExpireDate14);
			setValue("pos_entry_mode_22", pBilOBase.hInlgPosEntryMode22);
			setValue("pos_con_code_25", pBilOBase.hInlgPosConCode25);
			setValue("reference_no_37", pBilOBase.hInlgReferenceNo37);
			setValue("auth_id_resp_38", pBilOBase.hInlgAuthIdResp38);
			setValue("resp_flag_39", pBilOBase.hInlgRespFlag39);
			setValue("term_id_41", pBilOBase.hInlgTermId41);
			setValue("mcht_id_42", pBilOBase.hInlgMerchantId42);
			setValue("install_data_63", pBilOBase.hInlgInstallData63);
			setValue("install_resp_63_1", pBilOBase.hInlgInstallResp631);
			setValue("install_resp_63_2", pBilOBase.hInlgInstallResp632);

			//setValueInt("install_resp_63_3", P_BilOBase.h_inlg_install_resp_63_3);
			setValue("install_resp_63_3", ""+pBilOBase.hInlgInstallResp633);

			//setValueDouble("install_resp_63_4", P_BilOBase.h_inlg_install_resp_63_4);
			setValue("install_resp_63_4", ""+pBilOBase.hInlgInstallResp634);

			//setValueDouble("install_resp_63_5", P_BilOBase.h_inlg_install_resp_63_5);
			setValue("install_resp_63_5", ""+pBilOBase.hInlgInstallResp635);

			//setValueDouble("install_resp_63_6", P_BilOBase.h_inlg_install_resp_63_6);
			setValue("install_resp_63_6", ""+pBilOBase.hInlgInstallResp636);

			//setValueInt("points_redeem", P_BilOBase.h_inlg_points_redeem);
			setValue("points_redeem", ""+pBilOBase.hInlgPointsRedeem);

			//setValueDouble("points_balance", P_BilOBase.h_inlg_points_balance);
			setValue("points_balance", ""+pBilOBase.hInlgPointsBalance);

			//setValueDouble("points_amt", P_BilOBase.h_inlg_points_amt);
			setValue("points_amt", ""+pBilOBase.hInlgPointsAmt);

			setValue("acct_type", pBilOBase.hInlgAcctType);
			setValue("acno_p_seqno", pBilOBase.hInlgPSeqno);
			setValue("apr_flag", pBilOBase.hInlgConfirmFlag);
			setValue("apr_date", pBilOBase.hInlgConfirmDate);
			setValue("mod_user", pBilOBase.prgmId);
			//setValue("mod_time", gb.sysDate + gb.sysTime);
			setTimestamp("MOD_TIME",gb.getgTimeStamp());

			setValue("mod_pgm", pBilOBase.prgmId);
			daoTable = addTableOwner("bil_install_log");
			insertTable();
			if ("Y".equals(dupRecord)) {
				gb.showLogMessage("D", "insert bil_install_log 重複");
				pBilOBase.hRespCd = "24";
			}
		} catch (Exception ex) {
			gb.showLogMessage("E",String.format("insert bil_install_log 失敗 = [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "24";
		}

	}

	public void selectBilProd(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectBilProd(): started!");

		String hDtlFlag = "";

		int swCommon = 0, inta;

		pBilOBase.hLimitMin = 0;
		pBilOBase.hContTotAmt = 0;
		pBilOBase.hProdCltFeesFixAmt = 0;
		pBilOBase.hProdCltInterestRate = 0;
		pBilOBase.hProdTransRate = 0;
		hDtlFlag = "";



		pBilOBase.hLimitMin = 0;
		pBilOBase.hContTotAmt = 0;
		pBilOBase.hProdCltFeesFixAmt = 0;
		pBilOBase.hProdCltInterestRate = 0;
		pBilOBase.hProdTransRate = 0;
		hDtlFlag = "";

		try {
			daoTable = addTableOwner("bil_prod_nccc");
			selectSQL ="product_name,"
					+ "dtl_flag,"
					+ "limit_min,"
					+ "tot_amt,"
					+ "clt_fees_fix_amt,"
					+ "clt_interest_rate,"
					+ "seq_no,"
					+ "trans_rate ";

			whereStr="WHERE product_no  = ?   "
					+ "  and mcht_no     = ?  "
					+ "  and start_date <= ?  "
					+ "  and end_date   >= ?  "
					+ "fetch first 1 rows only";

			gb.showLogMessage("D","selectBilProd(): prod="+pBilOBase.hProdCd+"accpidcd="+pBilOBase.hAccpIdCd+"txdate="+pBilOBase.hInlgTxDate);

			setString(1, pBilOBase.hProdCd);
			setString(2, pBilOBase.hAccpIdCd);
			setString(3, pBilOBase.hInlgTxDate);
			setString(4, pBilOBase.hInlgTxDate);
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "select_bil_prod not found");//此分期特店不存在
				pBilOBase.hRespCd = "52";
				swCommon = 1;
				return;
			}
			else {
				pBilOBase.hContProductName = getValue("product_name");
				hDtlFlag = getValue("dtl_flag");
				swCommon = 0;
//				//避免轉檔時會放入Default值"Y   "，造成檢查錯誤
//				if (hDtlFlag.length() == 0) {
//					hDtlFlag = "Y   ";
//				}

				pBilOBase.hLimitMin = getInteger("limit_min");
				pBilOBase.hContTotAmt = getInteger("tot_amt");
				pBilOBase.hProdCltFeesFixAmt = getInteger("clt_fees_fix_amt");
				pBilOBase.hProdCltInterestRate = getInteger("clt_interest_rate");
				pBilOBase.hSeqNo = getInteger("seq_no");
				pBilOBase.hProdTransRate = getInteger("trans_rate");

			}



		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("select_bil_prod error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "74";
			swCommon = 2;
			return;
		}

		if(swCommon == 0) {


//			if("Y".equals(hDtlFlag.substring(0, 1))) {
//			swCommon = 0;
				try {

					daoTable = addTableOwner("bil_prod_nccc_bin");
					selectSQL ="Count(case when (DTL_KIND=?) then 1 else null end) as  kindCnt,"
							  +"Count(case when (DTL_KIND=? AND DTL_VALUE=?) then 1 else null end) as  dtlCnt";
					whereStr="WHERE product_no  = ?   "
							+ "and mcht_no = ?  "
							+ "and seq_no  = ?  ";
					setString(1, "ACCT-TYPE");
					setString(2, "ACCT-TYPE");
					setString(3, pBilOBase.hCardAcctType);

					setString(4, pBilOBase.hProdCd);
					setString(5, pBilOBase.hAccpIdCd);
					setInt(6, pBilOBase.hSeqNo);

					selectTable();
					gb.showLogMessage("D", "hCardAcctType cnt="+pBilOBase.hCardAcctType+";"+getInteger("kindCnt")+getInteger("dtlCnt"));
					if(getInteger("kindCnt")==0) {
						pBilOBase.tempInt=0;
					}
					else {
						if(getInteger("dtlCnt")==0) {
							pBilOBase.tempInt = getInteger("temp_int");
							swCommon = 3;
						}
					}
				} 
				catch(Exception ex) {
					swCommon =2;
					gb.showLogMessage("E", String.format("select bil_prod_nccc_bin 1 error= [%s]", ex.getMessage()));
				}

//				if(pBilOBase.tempInt < 1) {
//					swCommon = 1;
//				}
//			}


//			if("Y".equals(hDtlFlag.substring(1, 2)) && swCommon == 1) {
//				swCommon = 0;
			if(swCommon == 0) {
				try {
					daoTable = addTableOwner("bil_prod_nccc_bin");
					selectSQL ="Count(case when (DTL_KIND=?) then 1 else null end) as  kindCnt,"
							  +"Count(case when (DTL_KIND=? AND DTL_VALUE=?) then 1 else null end) as  dtlCnt";
					whereStr="WHERE product_no  = ?   "
							+ "and mcht_no = ?  "
							+ "and seq_no  = ?  ";
					setString(1, "GROUP-CODE");
					setString(2, "GROUP-CODE");
					setString(3, pBilOBase.hCardGroupCode);

					setString(4, pBilOBase.hProdCd);
					setString(5, pBilOBase.hAccpIdCd);
					setInt(6, pBilOBase.hSeqNo);

					selectTable();
					gb.showLogMessage("D", "hCardGroupCode cnt="+pBilOBase.hCardGroupCode+";"+getInteger("kindCnt")+getInteger("dtlCnt"));
					if(getInteger("kindCnt")==0) {
						pBilOBase.tempInt=0;
					}
					else {
						if(getInteger("dtlCnt")==0) {
							pBilOBase.tempInt = getInteger("temp_int");
							swCommon = 4;
						}
					}
				} 
				catch(Exception ex) {
					swCommon =2;
					gb.showLogMessage("E", String.format("select bil_prod_nccc_bin 2 error= [%s]", ex.getMessage()));
				}
//				if(pBilOBase.tempInt < 1) {
//					swCommon = 1;
//				}
			}

//			if("Y".equals(hDtlFlag.substring(2, 3)) && swCommon == 1) {
//				swCommon = 0;
			if(swCommon == 0) {
				try {
					daoTable = addTableOwner("bil_prod_nccc_bin");
					selectSQL ="Count(case when (DTL_KIND=?) then 1 else null end) as  kindCnt,"
							  +"Count(case when (DTL_KIND=? AND DTL_VALUE=?) then 1 else null end) as  dtlCnt";
					whereStr="WHERE product_no  = ?   "
							+ "and mcht_no = ?  "
							+ "and seq_no  = ?  ";
					setString(1, "CARD-TYPE");
					setString(2, "CARD-TYPE");
					setString(3, pBilOBase.hCardCardType);

					setString(4, pBilOBase.hProdCd);
					setString(5, pBilOBase.hAccpIdCd);
					setInt(6, pBilOBase.hSeqNo);

					selectTable();
					gb.showLogMessage("D", "hCardCardType cnt="+pBilOBase.hCardCardType+";"+getInteger("kindCnt")+getInteger("dtlCnt"));
					if(getInteger("kindCnt")==0) {
						pBilOBase.tempInt=0;
					}
					else {
						if(getInteger("dtlCnt")==0) {
							pBilOBase.tempInt = getInteger("temp_int");
							swCommon = 5;
						}
					}
				} 
				catch(Exception ex) {
					swCommon =2;
					gb.showLogMessage("E", String.format("select bil_prod_nccc_bin 3 error= [%s]", ex.getMessage()));
				}
//				if(pBilOBase.tempInt < 1) {
//					swCommon = 1;
//				}
			}
		}
		switch (swCommon) {
			case  1  : pBilOBase.hRespCd = "76";  
					   gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]"); break; /*1.無此商品2.商品期間不符3.參數不符*/
			case  2  : pBilOBase.hRespCd = "74";  
					   gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]"); break; /*無此參數*/
			case  3  : pBilOBase.hRespCd = "17";  
				 	   gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]"); break; /*ptr_acct_type不存在*/
			case  4  : pBilOBase.hRespCd = "51";  
			    	   gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]"); break; /*此團代不存在*/			
			case  5  : pBilOBase.hRespCd = "71";  
	    	           gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]"); break; /*卡種error*/		
			default  : break;
		}
		
		if(swCommon > 1) {
//			gb.showLogMessage("D", "select  bil_prod_nccc_bin all=[1403]");
//			pBilOBase.hRespCd = "76";
			return;
		}


		if(pBilOBase.hLimitMin > pBilOBase.hAmt) {
			pBilOBase.hRespCd = "75";
			gb.showLogMessage("D", "h_limit_min > h_amt ");
//			gb.showLogMessage("D", String.format("[%d],[%f]",pBilOBase.hLimitMin , pBilOBase.hAmt));
//			gb.showLogMessage("D", String.format("[%s]", pBilOBase.hInlgTxDate) );
			return;
		}


		pBilOBase.hContInstallTotTerm = Integer.parseInt(pBilOBase.hProdCd);


		pBilOBase.hContTotAmt = pBilOBase.hAmt;
		pBilOBase.tempLong = (long) (pBilOBase.hAmt / pBilOBase.hContInstallTotTerm);
		pBilOBase.hContUnitPrice = pBilOBase.tempLong;
		pBilOBase.hContFirstRemdAmt = pBilOBase.hAmt - (pBilOBase.hContUnitPrice * pBilOBase.hContInstallTotTerm);

		if(pBilOBase.hContFirstRemdAmt == -0.0) {
			pBilOBase.hContFirstRemdAmt = 0;
		}

		pBilOBase.tempDouble = pBilOBase.hContTotAmt * pBilOBase.hProdCltInterestRate * 1.0 / 100  + 0.5;
		pBilOBase.tempInt = (int) pBilOBase.tempDouble;
		pBilOBase.hContCltFeesAmt = pBilOBase.hProdCltFeesFixAmt + pBilOBase.tempInt;

		pBilOBase.totInterestAmt = 0;
		if(pBilOBase.hContInstallTotTerm > 1) {
			for (inta = 0; inta < pBilOBase.hContInstallTotTerm; inta++) {
				pBilOBase.doubleAmt = pBilOBase.hContUnitPrice  * (pBilOBase.hContInstallTotTerm - inta);

				if(inta == 0)
					pBilOBase.doubleAmt = pBilOBase.doubleAmt + pBilOBase.hContFirstRemdAmt;
				if(inta == pBilOBase.hContInstallTotTerm - 1)
					pBilOBase.doubleAmt = pBilOBase.doubleAmt + pBilOBase.hContRemdAmt;

				pBilOBase.longAmt = (long) ((pBilOBase.doubleAmt * pBilOBase.hProdTransRate / 1200) + 0.5);
				pBilOBase.totInterestAmt = pBilOBase.totInterestAmt + pBilOBase.longAmt;
			}
		}

		pBilOBase.hInlgInstallResp633 = pBilOBase.hContInstallTotTerm;
		pBilOBase.hInlgInstallResp634 = (int) (pBilOBase.hContUnitPrice + pBilOBase.hContFirstRemdAmt);
		pBilOBase.hInlgInstallResp635 = (int) pBilOBase.hContUnitPrice;
		pBilOBase.hInlgInstallResp636 = (int) pBilOBase.hContCltFeesAmt;

		return;

	}
	public void updateBilMerchant(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","updateBilMerchant(): started!");
		String tempX10="";
		try {

			daoTable = addTableOwner("dual");
//			selectSQL ="substr(to_char(bil_contractseq.nextval,'0000000000'),2,10) temp_x10";
			selectSQL ="substr(to_char("+ addTableOwner("bil_contractseq.nextval,'0000000000')")+",2,10) temp_x10";

			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D", "update_bil_merchant not found");  
			}
			else {
				tempX10 = getValue("temp_x10");	   
			}

		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("update_bil_merchant error= [%s]", ex.getMessage()));
		}
		pBilOBase.hContContractNo = tempX10;
		pBilOBase.hContContractSeqNo = 1;

		return;
	}

	public boolean updateStaRiskType() {
		gb.showLogMessage("I","updateStaRiskType(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_RISK_TYPE");
			updateSQL = "UNNORMAL_CNT   = ?,"
					+"UNNORMAL_AMT   = ?,"
					+"CONSUME_CNT    = ?,"
					+"CONSUME_AMT    = ?,"
					+"AUTH_CNT       = ?,"
					+"DECLINE_CNT    = ?,"
					+"CALLBANK_CNT   = ?,"
					+"PICKUP_CNT     = ?,"
					+"EXPIRED_CNT    = ?,"
					+"AUTH_AMT       = ?,"
					+"DECLINE_AMT    = ?,"
					+"CALLBANK_AMT   = ?,"
					+"PICKUP_AMT     = ?,"
					+"EXPIRED_AMT    = ?,"
					+"GENER_CNT      = ?,"
					+"ADJUST_CNT     = ?,"
					+"REVERSAL_CNT   = ?,"
					+"RETURN_CNT     = ?,"
					+"RETURN_ADJ_CNT = ?,"
					+"FORCE_CNT      = ?,"
					+"PREAUTH_CNT    = ?,"
					+"PREAUTH_OK_CNT = ?,"
					+"MAIL_CNT       = ?,"
					+"CASH_CNT       = ?,"
					+"CASH_ADJ_CNT   = ?,"
					+"VOICE_CNT      = ?,"
					+"EC_CNT         = ?,"
					+"GENER_AMT      = ?,"
					+"ADJUST_AMT     = ?,"
					+"REVERSAL_AMT   = ?,"
					+"RETURN_AMT     = ?,"
					+"RETURN_ADJ_AMT = ?,"
					+"FORCE_AMT      = ?,"
					+"PREAUTH_AMT    = ?,"
					+"PREAUTH_OK_AMT = ?,"
					+"MAIL_AMT       = ?,"
					+"CASH_AMT       = ?,"
					+"CASH_ADJ_AMT   = ?,"
					+"VOICE_AMT      = ?,"
					+"EC_AMT         = ?";
			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND PRD_TYPE= ? "
					+"AND RISK_TYPE= ? "
					+"AND UNNORMAL_CODE= ? ";

			setInt(1, gate.ngStaRiskTypeUnNormalCnt);
			setInt(2, gate.ngStaRiskTypeUnNormalAmt);
			setInt(3, gate.ngStaRiskTypeConsumeCnt);
			setInt(4,  gate.ngStaRiskTypeConsumeAmt);
			setInt(5, gate.ngStaRiskTypeAuthCnt);
			setInt(6, gate.ngStaRiskTypeDeclineCnt);
			setInt(7, gate.ngStaRiskTypeCallBankCnt);
			setInt(8, gate.ngStaRiskTypePickupCnt);
			setInt(9, gate.ngStaRiskTypeExpiredCnt);
			setInt(10, gate.ngStaRiskTypeAuthAmt);
			setInt(11, gate.ngStaRiskTypeDeclineAmt);
			setInt(12, gate.ngStaRiskTypeCallBankAmt);
			setInt(13, gate.ngStaRiskTypePickupAmt);
			setInt(14, gate.ngStaRiskTypeExpiredAmt);
			setInt(15, gate.ngStaRiskTypeGenerCnt);
			setInt(16, gate.ngStaRiskTypeAdjustCnt);
			setInt(17, gate.ngStaRiskTypeReversalCnt);
			setInt(18, gate.ngStaRiskTypeReturnCnt);
			setInt(19, gate.ngStaRiskTypeReturnAdjCnt);
			setInt(20, gate.ngStaRiskTypeForceCnt);
			setInt(21, gate.ngStaRiskTypePreauthCnt);
			setInt(22, gate.ngStaRiskTypePreauthOkCnt);
			setInt(23, gate.ngStaRiskTypeMailCnt);
			setInt(24, gate.ngStaRiskTypeCashCnt);
			setInt(25, gate.ngStaRiskTypeCashAdjCnt);
			setInt(26,0);
			setInt(27, gate.ngStaRiskTypeEcCnt);
			setInt(28, gate.ngStaRiskTypeGenerAmt);
			setInt(29, gate.ngStaRiskTypeAdjustAmt);
			setInt(30, gate.ngStaRiskTypeReversalAmt);
			setInt(31, gate.ngStaRiskTypeReturnAmt);
			setInt(32, gate.ngStaRiskTypeReturnAdjAmt);
			setInt(33, gate.ngStaRiskTypeForceAmt);
			setInt(34, gate.ngStaRiskTypePreauthAmt);
			setInt(35, gate.ngStaRiskTypePreauthOkAmt);
			setInt(36, gate.ngStaRiskTypeMailAmt);
			setInt(37, gate.ngStaRiskTypeCashAmt);
			setInt(38, gate.ngStaRiskTypeCashAdjAmt);
			setInt(39, 0);
			setInt(40, gate.ngStaRiskTypeEcAmt);

			setString(41,gate.sgSRskTypeBinNo);
			setString(42,gate.sgSRskTypeGroupCode);


			setInt(43,gate.ngTxSession);
			setString(44,gb.getSysDate());
			setString(45,gate.sgUsedCardProd);
			setString(46,gate.sgSRskRiskType);
			setString(47,gate.sgSRskCurrRespCode);



			updateTable();
			/*
   			 UNNORMAL_CNT   = :SRskType_UNNORMAL_CNT,
                UNNORMAL_AMT   = :SRskType_UNNORMAL_AMT ,
                CONSUME_CNT    = :SRskType_CONSUME_CNT ,
                CONSUME_AMT    = :SRskType_CONSUME_AMT ,
                AUTH_CNT       = :SRskType_AUTH_CNT ,
                DECLINE_CNT    = :SRskType_DECLINE_CNT ,
                CALLBANK_CNT   = :SRskType_CALLBANK_CNT ,
                PICKUP_CNT     = :SRskType_PICKUP_CNT ,
                EXPIRED_CNT    = :SRskType_EXPIRED_CNT ,
                AUTH_AMT       = :SRskType_AUTH_AMT ,
                DECLINE_AMT    = :SRskType_DECLINE_AMT ,
                CALLBANK_AMT   = :SRskType_CALLBANK_AMT ,
                PICKUP_AMT     = :SRskType_PICKUP_AMT ,
                EXPIRED_AMT    = :SRskType_EXPIRED_AMT ,
                GENER_CNT      = :SRskType_GENER_CNT ,
                ADJUST_CNT     = :SRskType_ADJUST_CNT ,
                REVERSAL_CNT   = :SRskType_REVERSAL_CNT ,
                RETURN_CNT     = :SRskType_RETURN_CNT ,
                RETURN_ADJ_CNT = :SRskType_RETURN_ADJ_CNT ,
                FORCE_CNT      = :SRskType_FORCE_CNT ,
                PREAUTH_CNT    = :SRskType_PREAUTH_CNT ,
                PREAUTH_OK_CNT = :SRskType_PREAUTH_OK_CNT ,
                MAIL_CNT       = :SRskType_MAIL_CNT ,
                CASH_CNT       = :SRskType_CASH_CNT ,
                CASH_ADJ_CNT   = :SRskType_CASH_ADJ_CNT ,
                VOICE_CNT      = :SRskType_VOICE_CNT ,
                EC_CNT         = :SRskType_EC_CNT ,
                GENER_AMT      = :SRskType_GENER_AMT ,
                ADJUST_AMT     = :SRskType_ADJUST_AMT ,
                REVERSAL_AMT   = :SRskType_REVERSAL_AMT ,
                RETURN_AMT     = :SRskType_RETURN_AMT ,
                RETURN_ADJ_AMT = :SRskType_RETURN_ADJ_AMT ,
                FORCE_AMT      = :SRskType_FORCE_AMT ,
                PREAUTH_AMT    = :SRskType_PREAUTH_AMT ,
                PREAUTH_OK_AMT = :SRskType_PREAUTH_OK_AMT ,
                MAIL_AMT       = :SRskType_MAIL_AMT ,
                CASH_AMT       = :SRskType_CASH_AMT ,
                CASH_ADJ_AMT   = :SRskType_CASH_ADJ_AMT ,
                VOICE_AMT      = :SRskType_VOICE_AMT ,
                EC_AMT         = :SRskType_EC_AMT
         WHERE BIN_NO  = :SRskType_BIN_NO
                AND GROUP_CODE = :SRskType_GROUP_CODE
                AND TX_SESSION=:MR_TX_SESSION
                AND STA_DATE=:MR_WK_SYSDATE
                AND PRD_TYPE=:SRskType_PRD_TYPE
                AND RISK_TYPE=:MR_RISK_TYPE
                AND UNNORMAL_CODE=:db_CURR_RESP_CODE;
			 * */

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}


	public boolean updateStaRiskTypeOld() {
		gb.showLogMessage("I","updateStaRiskTypeOld(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_RISK_TYPE");
			updateSQL = "UNNORMAL_CNT   = :p1,"
					+"UNNORMAL_AMT   = :p2,"
					+"CONSUME_CNT    = :p3,"
					+"CONSUME_AMT    = :p4,"
					+"AUTH_CNT       = :p5,"
					+"DECLINE_CNT    = :p6,"
					+"CALLBANK_CNT   = :p7,"
					+"PICKUP_CNT     = :p8,"
					+"EXPIRED_CNT    = :p9,"
					+"AUTH_AMT       = :p10,"
					+"DECLINE_AMT    = :p11,"
					+"CALLBANK_AMT   = :p12,"
					+"PICKUP_AMT     = :p13,"
					+"EXPIRED_AMT    = :p14,"
					+"GENER_CNT      = :p15,"
					+"ADJUST_CNT     = :p16,"
					+"REVERSAL_CNT   = :p17,"
					+"RETURN_CNT     = :p18,"
					+"RETURN_ADJ_CNT = :p19,"
					+"FORCE_CNT      = :p20,"
					+"PREAUTH_CNT    = :p21,"
					+"PREAUTH_OK_CNT = :p22,"
					+"MAIL_CNT       = :p23,"
					+"CASH_CNT       = :p24,"
					+"CASH_ADJ_CNT   = :p25,"
					+"VOICE_CNT      = :p26,"
					+"EC_CNT         = :p27,"
					+"GENER_AMT      = :p28,"
					+"ADJUST_AMT     = :p29,"
					+"REVERSAL_AMT   = :p30,"
					+"RETURN_AMT     = :p31,"
					+"RETURN_ADJ_AMT = :p32,"
					+"FORCE_AMT      = :p33,"
					+"PREAUTH_AMT    = :p34,"
					+"PREAUTH_OK_AMT = :p35,"
					+"MAIL_AMT       = :p36,"
					+"CASH_AMT       = :p37,"
					+"CASH_ADJ_AMT   = :p38,"
					+"VOICE_AMT      = :p39,"
					+"EC_AMT         = :p40";
			whereStr="WHERE BIN_NO  = :p41 "
					+"AND GROUP_CODE = :p42 "
					+"AND TX_SESSION= :p43 "
					+"AND STA_DATE= :p44 "
					+"AND PRD_TYPE= :p45 "
					+"AND RISK_TYPE= :p46 "
					+"AND UNNORMAL_CODE= :p47 ";

			setValueInt("p1", gate.ngStaRiskTypeUnNormalCnt);
			setValueInt("p2", gate.ngStaRiskTypeUnNormalAmt);


			setValueInt("p3", gate.ngStaRiskTypeConsumeCnt);
			setValueInt("p4",  gate.ngStaRiskTypeConsumeAmt);

			setValueInt("p5", gate.ngStaRiskTypeAuthCnt);
			setValueInt("p6", gate.ngStaRiskTypeDeclineCnt);

			setValueInt("p7", gate.ngStaRiskTypeCallBankCnt);
			setValueInt("p8", gate.ngStaRiskTypePickupCnt);
			setValueInt("p9", gate.ngStaRiskTypeExpiredCnt);

			setValueInt("p10", gate.ngStaRiskTypeAuthAmt);
			setValueInt("p11", gate.ngStaRiskTypeDeclineAmt);

			setValueInt("p12", gate.ngStaRiskTypeCallBankAmt);
			setValueInt("p13", gate.ngStaRiskTypePickupAmt);
			setValueInt("p14", gate.ngStaRiskTypeExpiredAmt);
			setValueInt("p15", gate.ngStaRiskTypeGenerCnt);
			setValueInt("p16", gate.ngStaRiskTypeAdjustCnt);

			setValueInt("p17", gate.ngStaRiskTypeReversalCnt);
			setValueInt("p18", gate.ngStaRiskTypeReturnCnt);

			setValueInt("p19", gate.ngStaRiskTypeReturnAdjCnt);
			setValueInt("p20", gate.ngStaRiskTypeForceCnt);
			setValueInt("p21", gate.ngStaRiskTypePreauthCnt);
			setValueInt("p22", gate.ngStaRiskTypePreauthOkCnt);
			setValueInt("p23", gate.ngStaRiskTypeMailCnt);
			setValueInt("p24", gate.ngStaRiskTypeCashCnt);
			setValueInt("p25", gate.ngStaRiskTypeCashAdjCnt);
			setValueInt("p26",0);
			setValueInt("p27", gate.ngStaRiskTypeEcCnt);
			setValueInt("p28", gate.ngStaRiskTypeGenerAmt);
			setValueInt("p29", gate.ngStaRiskTypeAdjustAmt);
			setValueInt("p30", gate.ngStaRiskTypeReversalAmt);
			setValueInt("p31", gate.ngStaRiskTypeReturnAmt);
			setValueInt("p32", gate.ngStaRiskTypeReturnAdjAmt);
			setValueInt("p33", gate.ngStaRiskTypeForceAmt);
			setValueInt("p34", gate.ngStaRiskTypePreauthAmt);
			setValueInt("p35", gate.ngStaRiskTypePreauthOkAmt);
			setValueInt("p36", gate.ngStaRiskTypeMailAmt);
			setValueInt("p37", gate.ngStaRiskTypeCashAmt);
			setValueInt("p38", gate.ngStaRiskTypeCashAdjAmt);
			setValueInt("p39", 0);
			setValueInt("p40", gate.ngStaRiskTypeEcAmt);

			setValue("p41",gate.sgSRskTypeBinNo);
			setValue("p42",gate.sgSRskTypeGroupCode);


			setValueInt("p43",gate.ngTxSession);
			setValue("p44",gb.getSysDate());
			setValue("p45",gate.sgUsedCardProd);
			setValue("p46",gate.sgSRskRiskType);
			setValue("p47",gate.sgSRskCurrRespCode);



			updateTable();
			/*
   			 UNNORMAL_CNT   = :SRskType_UNNORMAL_CNT,
                UNNORMAL_AMT   = :SRskType_UNNORMAL_AMT ,
                CONSUME_CNT    = :SRskType_CONSUME_CNT ,
                CONSUME_AMT    = :SRskType_CONSUME_AMT ,
                AUTH_CNT       = :SRskType_AUTH_CNT ,
                DECLINE_CNT    = :SRskType_DECLINE_CNT ,
                CALLBANK_CNT   = :SRskType_CALLBANK_CNT ,
                PICKUP_CNT     = :SRskType_PICKUP_CNT ,
                EXPIRED_CNT    = :SRskType_EXPIRED_CNT ,
                AUTH_AMT       = :SRskType_AUTH_AMT ,
                DECLINE_AMT    = :SRskType_DECLINE_AMT ,
                CALLBANK_AMT   = :SRskType_CALLBANK_AMT ,
                PICKUP_AMT     = :SRskType_PICKUP_AMT ,
                EXPIRED_AMT    = :SRskType_EXPIRED_AMT ,
                GENER_CNT      = :SRskType_GENER_CNT ,
                ADJUST_CNT     = :SRskType_ADJUST_CNT ,
                REVERSAL_CNT   = :SRskType_REVERSAL_CNT ,
                RETURN_CNT     = :SRskType_RETURN_CNT ,
                RETURN_ADJ_CNT = :SRskType_RETURN_ADJ_CNT ,
                FORCE_CNT      = :SRskType_FORCE_CNT ,
                PREAUTH_CNT    = :SRskType_PREAUTH_CNT ,
                PREAUTH_OK_CNT = :SRskType_PREAUTH_OK_CNT ,
                MAIL_CNT       = :SRskType_MAIL_CNT ,
                CASH_CNT       = :SRskType_CASH_CNT ,
                CASH_ADJ_CNT   = :SRskType_CASH_ADJ_CNT ,
                VOICE_CNT      = :SRskType_VOICE_CNT ,
                EC_CNT         = :SRskType_EC_CNT ,
                GENER_AMT      = :SRskType_GENER_AMT ,
                ADJUST_AMT     = :SRskType_ADJUST_AMT ,
                REVERSAL_AMT   = :SRskType_REVERSAL_AMT ,
                RETURN_AMT     = :SRskType_RETURN_AMT ,
                RETURN_ADJ_AMT = :SRskType_RETURN_ADJ_AMT ,
                FORCE_AMT      = :SRskType_FORCE_AMT ,
                PREAUTH_AMT    = :SRskType_PREAUTH_AMT ,
                PREAUTH_OK_AMT = :SRskType_PREAUTH_OK_AMT ,
                MAIL_AMT       = :SRskType_MAIL_AMT ,
                CASH_AMT       = :SRskType_CASH_AMT ,
                CASH_ADJ_AMT   = :SRskType_CASH_ADJ_AMT ,
                VOICE_AMT      = :SRskType_VOICE_AMT ,
                EC_AMT         = :SRskType_EC_AMT
         WHERE BIN_NO  = :SRskType_BIN_NO
                AND GROUP_CODE = :SRskType_GROUP_CODE
                AND TX_SESSION=:MR_TX_SESSION
                AND STA_DATE=:MR_WK_SYSDATE
                AND PRD_TYPE=:SRskType_PRD_TYPE
                AND RISK_TYPE=:MR_RISK_TYPE
                AND UNNORMAL_CODE=:db_CURR_RESP_CODE;
			 * */

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	/*
   	public boolean insertStaRiskType() {
   		boolean bL_Result = true;

   		try {
   			daoTable = "CCA_STA_RISK_TYPE";

   			setValue("BIN_NO",gate.sG_SRskTypeBinNo);
   			setValue("GROUP_CODE",gate.sG_SRskTypeGroupCode);


   			setValueInt("TX_SESSION",gate.nG_TxSession);
 			setValue("STA_DATE",gb.sysDate);
 			setValue("PRD_TYPE",gate.sG_UsedCardProd);
 			setValue("RISK_TYPE",gate.sG_SRskRiskType);
 			setValue("UNNORMAL_CODE",gate.sG_SRskCurrRespCode);
 			setValue("PRD_GROUP",gate.groupCode);
 			setValue("CARD_TYPE", getValue("CARD_TYPE"));
 			setValue("UNNORMAL_FLAG",gate.sG_StaRiskTypeUnNormalFlag);

 			setValueInt("UNNORMAL_CNT",gate.nG_StaRiskTypeUnNormalCnt);
 			setValueInt("UNNORMAL_AMT", gate.nG_StaRiskTypeUnNormalAmt);
 			setValueInt("CONSUME_CNT",gate.nG_StaRiskTypeConsumeCnt);
 			setValueInt("CONSUME_AMT",gate.nG_StaRiskTypeConsumeAmt);
 			setValueInt("AUTH_CNT", gate.nG_StaRiskTypeAuthCnt);
 			setValueInt("DECLINE_CNT", gate.nG_StaRiskTypeDeclineCnt);
 			setValueInt("CALLBANK_CNT", gate.nG_StaRiskTypeCallBankCnt);
 			setValueInt("PICKUP_CNT",gate.nG_StaRiskTypePickupCnt);
 			setValueInt("EXPIRED_CNT", gate.nG_StaRiskTypeExpiredCnt);
 			setValueInt("AUTH_AMT",gate.nG_StaRiskTypeAuthAmt);
 			setValueInt("DECLINE_AMT", gate.nG_StaRiskTypeDeclineAmt);
 			setValueInt("CALLBANK_AMT", gate.nG_StaRiskTypeCallBankAmt);
 			setValueInt("PICKUP_AMT", gate.nG_StaRiskTypePickupAmt);
 			setValueInt("EXPIRED_AMT", gate.nG_StaRiskTypeExpiredAmt);
 			setValueInt("GENER_CNT", gate.nG_StaRiskTypeGenerAmt);
 			setValueInt("ADJUST_CNT", gate.nG_StaRiskTypeAdjustCnt);
 			setValueInt("REVERSAL_CNT", gate.nG_StaRiskTypeReversalCnt);
 			setValueInt("RETURN_CNT", gate.nG_StaRiskTypeReturnCnt);
 			setValueInt("RETURN_ADJ_CNT", gate.nG_StaRiskTypeReturnAdjCnt);
 			setValueInt("FORCE_CNT", gate.nG_StaRiskTypeForceCnt);
 			setValueInt("PREAUTH_CNT", gate.nG_StaRiskTypePreauthCnt);
 			setValueInt("PREAUTH_OK_CNT", gate.nG_StaRiskTypePreauthOkCnt);
 			setValueInt("MAIL_CNT", gate.nG_StaRiskTypeMailCnt);
 			setValueInt("CASH_CNT", gate.nG_StaRiskTypeCashCnt);
 			setValueInt("CASH_ADJ_CNT", gate.nG_StaRiskTypeCashAdjCnt);
 			setValueInt("VOICE_CNT", 0);
 			setValueInt("EC_CNT", gate.nG_StaRiskTypeEcCnt);
 			setValueInt("CALLBANK_CNTX", gate.nG_StaRiskTypeCallBankCnt);
 			setValueInt("GENER_AMT", gate.nG_StaRiskTypeGenerAmt);
 			setValueInt("ADJUST_AMT", gate.nG_StaRiskTypeAdjustAmt);
 			setValueInt("REVERSAL_AMT", gate.nG_StaRiskTypeReversalAmt);
 			setValueInt("RETURN_AMT", gate.nG_StaRiskTypeReturnAmt);
 			setValueInt("RETURN_ADJ_AMT", gate.nG_StaRiskTypeReturnAdjAmt);
 			setValueInt("FORCE_AMT", gate.nG_StaRiskTypeForceAmt);
 			setValueInt("PREAUTH_AMT", gate.nG_StaRiskTypePreauthAmt);
 			setValueInt("PREAUTH_OK_AMT", gate.nG_StaRiskTypePreauthOkAmt);
 			setValueInt("MAIL_AMT", gate.nG_StaRiskTypeMailAmt);
 			setValueInt("CASH_AMT", gate.nG_StaRiskTypeCashAmt);
 			setValueInt("CASH_ADJ_AMT", gate.nG_StaRiskTypeCashAdjAmt);
 			setValueInt("VOICE_AMT",0);
 			setValueInt("EC_AMT", gate.nG_StaRiskTypeEcAmt);
 			setValueInt("CALLBANK_AMTX", gate.nG_StaRiskTypeCallBankAmt);   	       

   			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
			bL_Result = false;
		}

   		return bL_Result;
   	}
	 */
	public boolean insertStaRiskType() {
		gb.showLogMessage("I","insertStaRiskType(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_RISK_TYPE");

			setValue("BIN_NO",gate.sgSRskTypeBinNo);
			setValue("GROUP_CODE",gate.sgSRskTypeGroupCode);


			setValue("TX_SESSION",""+gate.ngTxSession);
			setValue("STA_DATE",gb.getSysDate());
			setValue("PRD_TYPE",gate.sgUsedCardProd);
			setValue("RISK_TYPE",gate.sgSRskRiskType);
			setValue("UNNORMAL_CODE",gate.sgSRskCurrRespCode);
			setValue("PRD_GROUP",gate.groupCode);
			setValue("CARD_TYPE", getValue("CARD_TYPE"));
			setValue("UNNORMAL_FLAG",gate.sgStaRiskTypeUnNormalFlag);

			setValue("UNNORMAL_CNT",""+gate.ngStaRiskTypeUnNormalCnt);
			setValue("UNNORMAL_AMT", ""+gate.ngStaRiskTypeUnNormalAmt);
			setValue("CONSUME_CNT",""+gate.ngStaRiskTypeConsumeCnt);
			setValue("CONSUME_AMT",""+gate.ngStaRiskTypeConsumeAmt);
			setValue("AUTH_CNT", ""+gate.ngStaRiskTypeAuthCnt);
			setValue("DECLINE_CNT", ""+gate.ngStaRiskTypeDeclineCnt);
			setValue("CALLBANK_CNT", ""+gate.ngStaRiskTypeCallBankCnt);
			setValue("PICKUP_CNT",""+gate.ngStaRiskTypePickupCnt);
			setValue("EXPIRED_CNT", ""+gate.ngStaRiskTypeExpiredCnt);
			setValue("AUTH_AMT",""+gate.ngStaRiskTypeAuthAmt);
			setValue("DECLINE_AMT", ""+gate.ngStaRiskTypeDeclineAmt);
			setValue("CALLBANK_AMT", ""+gate.ngStaRiskTypeCallBankAmt);
			setValue("PICKUP_AMT", ""+gate.ngStaRiskTypePickupAmt);
			setValue("EXPIRED_AMT", ""+gate.ngStaRiskTypeExpiredAmt);
			setValue("GENER_CNT", ""+gate.ngStaRiskTypeGenerAmt);
			setValue("ADJUST_CNT", ""+gate.ngStaRiskTypeAdjustCnt);
			setValue("REVERSAL_CNT", ""+gate.ngStaRiskTypeReversalCnt);
			setValue("RETURN_CNT", ""+gate.ngStaRiskTypeReturnCnt);
			setValue("RETURN_ADJ_CNT", ""+gate.ngStaRiskTypeReturnAdjCnt);
			setValue("FORCE_CNT", ""+gate.ngStaRiskTypeForceCnt);
			setValue("PREAUTH_CNT", ""+gate.ngStaRiskTypePreauthCnt);
			setValue("PREAUTH_OK_CNT", ""+gate.ngStaRiskTypePreauthOkCnt);
			setValue("MAIL_CNT", ""+gate.ngStaRiskTypeMailCnt);
			setValue("CASH_CNT", ""+gate.ngStaRiskTypeCashCnt);
			setValue("CASH_ADJ_CNT", ""+gate.ngStaRiskTypeCashAdjCnt);
			setValue("VOICE_CNT", ""+"0");
			setValue("EC_CNT", ""+gate.ngStaRiskTypeEcCnt);
			setValue("CALLBANK_CNTX", ""+gate.ngStaRiskTypeCallBankCnt);
			setValue("GENER_AMT", ""+gate.ngStaRiskTypeGenerAmt);
			setValue("ADJUST_AMT", ""+gate.ngStaRiskTypeAdjustAmt);
			setValue("REVERSAL_AMT", ""+gate.ngStaRiskTypeReversalAmt);
			setValue("RETURN_AMT", ""+gate.ngStaRiskTypeReturnAmt);
			setValue("RETURN_ADJ_AMT", ""+gate.ngStaRiskTypeReturnAdjAmt);
			setValue("FORCE_AMT", ""+gate.ngStaRiskTypeForceAmt);
			setValue("PREAUTH_AMT", ""+gate.ngStaRiskTypePreauthAmt);
			setValue("PREAUTH_OK_AMT", ""+gate.ngStaRiskTypePreauthOkAmt);
			setValue("MAIL_AMT", ""+gate.ngStaRiskTypeMailAmt);
			setValue("CASH_AMT", ""+gate.ngStaRiskTypeCashAmt);
			setValue("CASH_ADJ_AMT", ""+gate.ngStaRiskTypeCashAdjAmt);
			setValue("VOICE_AMT",""+"0");
			setValue("EC_AMT", ""+gate.ngStaRiskTypeEcAmt);
			setValue("CALLBANK_AMTX", ""+gate.ngStaRiskTypeCallBankAmt);   	       

			insertTable(); 


		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean insertStaRiskTypeOld() {
		gb.showLogMessage("I","insertStaRiskTypeOld(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_RISK_TYPE");

			setValue("BIN_NO",gate.sgSRskTypeBinNo);
			setValue("GROUP_CODE",gate.sgSRskTypeGroupCode);


			setValueInt("TX_SESSION",gate.ngTxSession);
			setValue("STA_DATE",gb.getSysDate());
			setValue("PRD_TYPE",gate.sgUsedCardProd);
			setValue("RISK_TYPE",gate.sgSRskRiskType);
			setValue("UNNORMAL_CODE",gate.sgSRskCurrRespCode);
			setValue("PRD_GROUP",gate.groupCode);
			setValue("CARD_TYPE", getValue("CARD_TYPE"));
			setValue("UNNORMAL_FLAG",gate.sgStaRiskTypeUnNormalFlag);

			setValueInt("UNNORMAL_CNT",gate.ngStaRiskTypeUnNormalCnt);
			setValueInt("UNNORMAL_AMT", gate.ngStaRiskTypeUnNormalAmt);
			setValueInt("CONSUME_CNT",gate.ngStaRiskTypeConsumeCnt);
			setValueInt("CONSUME_AMT",gate.ngStaRiskTypeConsumeAmt);
			setValueInt("AUTH_CNT", gate.ngStaRiskTypeAuthCnt);
			setValueInt("DECLINE_CNT", gate.ngStaRiskTypeDeclineCnt);
			setValueInt("CALLBANK_CNT", gate.ngStaRiskTypeCallBankCnt);
			setValueInt("PICKUP_CNT",gate.ngStaRiskTypePickupCnt);
			setValueInt("EXPIRED_CNT", gate.ngStaRiskTypeExpiredCnt);
			setValueInt("AUTH_AMT",gate.ngStaRiskTypeAuthAmt);
			setValueInt("DECLINE_AMT", gate.ngStaRiskTypeDeclineAmt);
			setValueInt("CALLBANK_AMT", gate.ngStaRiskTypeCallBankAmt);
			setValueInt("PICKUP_AMT", gate.ngStaRiskTypePickupAmt);
			setValueInt("EXPIRED_AMT", gate.ngStaRiskTypeExpiredAmt);
			setValueInt("GENER_CNT", gate.ngStaRiskTypeGenerAmt);
			setValueInt("ADJUST_CNT", gate.ngStaRiskTypeAdjustCnt);
			setValueInt("REVERSAL_CNT", gate.ngStaRiskTypeReversalCnt);
			setValueInt("RETURN_CNT", gate.ngStaRiskTypeReturnCnt);
			setValueInt("RETURN_ADJ_CNT", gate.ngStaRiskTypeReturnAdjCnt);
			setValueInt("FORCE_CNT", gate.ngStaRiskTypeForceCnt);
			setValueInt("PREAUTH_CNT", gate.ngStaRiskTypePreauthCnt);
			setValueInt("PREAUTH_OK_CNT", gate.ngStaRiskTypePreauthOkCnt);
			setValueInt("MAIL_CNT", gate.ngStaRiskTypeMailCnt);
			setValueInt("CASH_CNT", gate.ngStaRiskTypeCashCnt);
			setValueInt("CASH_ADJ_CNT", gate.ngStaRiskTypeCashAdjCnt);
			setValueInt("VOICE_CNT", 0);
			setValueInt("EC_CNT", gate.ngStaRiskTypeEcCnt);
			setValueInt("CALLBANK_CNTX", gate.ngStaRiskTypeCallBankCnt);
			setValueInt("GENER_AMT", gate.ngStaRiskTypeGenerAmt);
			setValueInt("ADJUST_AMT", gate.ngStaRiskTypeAdjustAmt);
			setValueInt("REVERSAL_AMT", gate.ngStaRiskTypeReversalAmt);
			setValueInt("RETURN_AMT", gate.ngStaRiskTypeReturnAmt);
			setValueInt("RETURN_ADJ_AMT", gate.ngStaRiskTypeReturnAdjAmt);
			setValueInt("FORCE_AMT", gate.ngStaRiskTypeForceAmt);
			setValueInt("PREAUTH_AMT", gate.ngStaRiskTypePreauthAmt);
			setValueInt("PREAUTH_OK_AMT", gate.ngStaRiskTypePreauthOkAmt);
			setValueInt("MAIL_AMT", gate.ngStaRiskTypeMailAmt);
			setValueInt("CASH_AMT", gate.ngStaRiskTypeCashAmt);
			setValueInt("CASH_ADJ_AMT", gate.ngStaRiskTypeCashAdjAmt);
			setValueInt("VOICE_AMT",0);
			setValueInt("EC_AMT", gate.ngStaRiskTypeEcAmt);
			setValueInt("CALLBANK_AMTX", gate.ngStaRiskTypeCallBankAmt);   	       

			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean updateStaDailyMcc() {
		gb.showLogMessage("I","updateStaDailyMcc(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_DAILY_MCC");
			updateSQL = "UNNORMAL_CNT   = ?,"
					+"UNNORMAL_AMT   = ?,"
					+"CONSUME_CNT    = ?,"
					+"CONSUME_AMT    = ?,"
					+"AUTH_CNT       = ?,"
					+"DECLINE_CNT    = ?,"
					+"CALLBANK_CNT   = ?,"
					+"PICKUP_CNT     = ?,"
					+"EXPIRED_CNT    = ?,"
					+"AUTH_AMT       = ?,"
					+"DECLINE_AMT    = ?,"
					+"CALLBANK_AMT   = ?,"
					+"PICKUP_AMT     = ?,"
					+"EXPIRED_AMT    = ?,"
					+"GENER_CNT      = ?,"
					+"ADJUST_CNT     = ?,"
					+"REVERSAL_CNT   = ?,"
					+"RETURN_CNT     = ?,"
					+"RETURN_ADJ_CNT = ?,"
					+"FORCE_CNT      = ?,"
					+"PREAUTH_CNT    = ?,"
					+"PREAUTH_OK_CNT = ?,"
					+"MAIL_CNT       = ?,"
					+"CASH_CNT       = ?,"
					+"CASH_ADJ_CNT   = ?,"
					+"VOICE_CNT      = ?,"
					+"EC_CNT         = ?,"
					+"GENER_AMT      = ?,"
					+"ADJUST_AMT     = ?,"
					+"REVERSAL_AMT   = ?,"
					+"RETURN_AMT     = ?,"
					+"RETURN_ADJ_AMT = ?,"
					+"FORCE_AMT      = ?,"
					+"PREAUTH_AMT    = ?,"
					+"PREAUTH_OK_AMT = ?,"
					+"MAIL_AMT       = ?,"
					+"CASH_AMT       = ?,"
					+"CASH_ADJ_AMT   = ?,"
					+"VOICE_AMT      = ?,"
					+"EC_AMT         = ?";
			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND MCC_CODE= ? ";

			setInt(1, gate.ngStaDailyMccUnNormalCnt);
			setInt(2, gate.ngStaDailyMccUnNormalAmt);


			setInt(3, gate.ngStaDailyMccConsumeCnt);
			setInt(4,  gate.ngStaDailyMccConsumeAmt);

			setInt(5, gate.ngStaDailyMccAuthCnt);
			setInt(6, gate.ngStaDailyMccDeclineCnt);

			setInt(7, gate.ngStaDailyMccCallBankCnt);
			setInt(8, gate.ngStaDailyMccPickupCnt);
			setInt(9, gate.ngStaDailyMccExpiredCnt);

			setInt(10, gate.ngStaDailyMccAuthAmt);
			setInt(11, gate.ngStaDailyMccDeclineAmt);

			setInt(12, gate.ngStaDailyMccCallBankAmt);
			setInt(13, gate.ngStaDailyMccPickupAmt);
			setInt(14, gate.ngStaDailyMccExpiredAmt);
			setInt(15, gate.ngStaDailyMccGenerCnt);
			setInt(16, gate.ngStaDailyMccAdjustCnt);
			setInt(17, gate.ngStaDailyMccReversalCnt);
			setInt(18, gate.ngStaDailyMccReturnCnt);
			setInt(19, gate.ngStaDailyMccReturnAdjCnt);
			setInt(20, gate.ngStaDailyMccForceCnt);
			setInt(21, gate.ngStaDailyMccPreauthCnt);
			setInt(22, gate.ngStaDailyMccPreauthOkCnt);
			setInt(23, gate.ngStaDailyMccMailCnt);
			setInt(24, gate.ngStaDailyMccCashCnt);
			setInt(25, gate.ngStaDailyMccCashAdjCnt);
			setInt(26,0);
			setInt(27, gate.ngStaDailyMccEcCnt);
			setInt(28, gate.ngStaDailyMccGenerAmt);
			setInt(29, gate.ngStaDailyMccAdjustAmt);
			setInt(30, gate.ngStaDailyMccReversalAmt);
			setInt(31, gate.ngStaDailyMccReturnAmt);
			setInt(32, gate.ngStaDailyMccReturnAdjAmt);
			setInt(33, gate.ngStaDailyMccForceAmt);
			setInt(34, gate.ngStaDailyMccPreauthAmt);
			setInt(35, gate.ngStaDailyMccPreauthOkAmt);
			setInt(36, gate.ngStaDailyMccMailAmt);
			setInt(37, gate.ngStaDailyMccCashAmt);
			setInt(38, gate.ngStaDailyMccCashAdjAmt);
			setInt(39, 0);
			setInt(40, gate.ngStaDailyMccEcAmt);


			setString(41,gate.sgSDailyMccBinNo);
			setString(42,gate.sgSDailyMccGroupCode);


			setInt(43,gate.ngTxSession);
			setString(44,gb.getSysDate());
			setString(45,gate.mccCode.substring(0,4));

			updateTable();

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean updateStaDailyMccOld2() {
		gb.showLogMessage("I","updateStaDailyMccOld2(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_DAILY_MCC");
			updateSQL = "UNNORMAL_CNT   = :p1,"
					+"UNNORMAL_AMT   = :p2,"
					+"CONSUME_CNT    = :p3,"
					+"CONSUME_AMT    = :p4,"
					+"AUTH_CNT       = :p5,"
					+"DECLINE_CNT    = :p6,"
					+"CALLBANK_CNT   = :p7,"
					+"PICKUP_CNT     = :p8,"
					+"EXPIRED_CNT    = :p9,"
					+"AUTH_AMT       = :p10,"
					+"DECLINE_AMT    = :p11,"
					+"CALLBANK_AMT   = :p12,"
					+"PICKUP_AMT     = :p13,"
					+"EXPIRED_AMT    = :p14,"
					+"GENER_CNT      = :p15,"
					+"ADJUST_CNT     = :p16,"
					+"REVERSAL_CNT   = :p17,"
					+"RETURN_CNT     = :p18,"
					+"RETURN_ADJ_CNT = :p19,"
					+"FORCE_CNT      = :p20,"
					+"PREAUTH_CNT    = :p21,"
					+"PREAUTH_OK_CNT = :p22,"
					+"MAIL_CNT       = :p23,"
					+"CASH_CNT       = :p24,"
					+"CASH_ADJ_CNT   = :p25,"
					+"VOICE_CNT      = :p26,"
					+"EC_CNT         = :p27,"
					+"GENER_AMT      = :p28,"
					+"ADJUST_AMT     = :p29,"
					+"REVERSAL_AMT   = :p30,"
					+"RETURN_AMT     = :p31,"
					+"RETURN_ADJ_AMT = :p32,"
					+"FORCE_AMT      = :p33,"
					+"PREAUTH_AMT    = :p34,"
					+"PREAUTH_OK_AMT = :p35,"
					+"MAIL_AMT       = :p36,"
					+"CASH_AMT       = :p37,"
					+"CASH_ADJ_AMT   = :p38,"
					+"VOICE_AMT      = :p39,"
					+"EC_AMT         = :p40";
			whereStr="WHERE BIN_NO  = :p41 "
					+"AND GROUP_CODE = :p42 "
					+"AND TX_SESSION= :p43 "
					+"AND STA_DATE= :p44 "
					+"AND MCC_CODE= :p45 ";

			setValue("p1", ""+gate.ngStaDailyMccUnNormalCnt);
			setValue("p2", ""+gate.ngStaDailyMccUnNormalAmt);


			setValue("p3", ""+gate.ngStaDailyMccConsumeCnt);
			setValue("p4",  ""+gate.ngStaDailyMccConsumeAmt);

			setValue("p5", ""+gate.ngStaDailyMccAuthCnt);
			setValue("p6", ""+gate.ngStaDailyMccDeclineCnt);

			setValue("p7", ""+gate.ngStaDailyMccCallBankCnt);
			setValue("p8", ""+gate.ngStaDailyMccPickupCnt);
			setValue("p9", ""+gate.ngStaDailyMccExpiredCnt);

			setValue("p10", ""+gate.ngStaDailyMccAuthAmt);
			setValue("p11", ""+gate.ngStaDailyMccDeclineAmt);

			setValue("p12", ""+gate.ngStaDailyMccCallBankAmt);
			setValue("p13", ""+gate.ngStaDailyMccPickupAmt);
			setValue("p14", ""+gate.ngStaDailyMccExpiredAmt);
			setValue("p15", ""+gate.ngStaDailyMccGenerCnt);
			setValue("p16", ""+gate.ngStaDailyMccAdjustCnt);

			setValue("p17", ""+gate.ngStaDailyMccReversalCnt);
			setValue("p18", ""+gate.ngStaDailyMccReturnCnt);

			setValue("p19", ""+gate.ngStaDailyMccReturnAdjCnt);
			setValue("p20", ""+gate.ngStaDailyMccForceCnt);
			setValue("p21", ""+gate.ngStaDailyMccPreauthCnt);
			setValue("p22", ""+gate.ngStaDailyMccPreauthOkCnt);
			setValue("p23", ""+gate.ngStaDailyMccMailCnt);
			setValue("p24", ""+gate.ngStaDailyMccCashCnt);
			setValue("p25", ""+gate.ngStaDailyMccCashAdjCnt);
			setValue("p26",""+"0");
			setValue("p27", ""+gate.ngStaDailyMccEcCnt);
			setValue("p28", ""+gate.ngStaDailyMccGenerAmt);
			setValue("p29", ""+gate.ngStaDailyMccAdjustAmt);
			setValue("p30", ""+gate.ngStaDailyMccReversalAmt);
			setValue("p31", ""+gate.ngStaDailyMccReturnAmt);
			setValue("p32", ""+gate.ngStaDailyMccReturnAdjAmt);
			setValue("p33", ""+gate.ngStaDailyMccForceAmt);
			setValue("p34", ""+gate.ngStaDailyMccPreauthAmt);
			setValue("p35", ""+gate.ngStaDailyMccPreauthOkAmt);
			setValue("p36", ""+gate.ngStaDailyMccMailAmt);
			setValue("p37", ""+gate.ngStaDailyMccCashAmt);
			setValue("p38", ""+gate.ngStaDailyMccCashAdjAmt);
			setValue("p39", ""+"0");
			setValue("p40", ""+gate.ngStaDailyMccEcAmt);


			setValue("p41",gate.sgSDailyMccBinNo);
			setValue("p42",gate.sgSDailyMccGroupCode);


			setValueInt("p43",gate.ngTxSession);
			setValue("p44",gb.getSysDate());
			setValue("p45",gate.mccCode.substring(0,4));

			updateTable();

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean insertStaDailyMccOld2() {
		gb.showLogMessage("I","insertStaDailyMccOld2(): started!");
		boolean blResult = true;

		try {
			daoTable = addTableOwner("CCA_STA_DAILY_MCC");

			setValue("BIN_NO",gate.sgSDailyMccBinNo);
			setValue("GROUP_CODE",gate.sgSDailyMccGroupCode);


			setValueInt("TX_SESSION",gate.ngTxSession);
			setValue("STA_DATE",gb.getSysDate());
			setValue("MCC_CODE",gate.mccCode.substring(0,4));

			setValue("RISK_TYPE",gate.sgSRskRiskType);


			setValueInt("UNNORMAL_CNT",gate.ngStaDailyMccUnNormalCnt);
			setValueInt("UNNORMAL_AMT", gate.ngStaDailyMccUnNormalAmt);
			setValueInt("CONSUME_CNT",gate.ngStaDailyMccConsumeCnt);
			setValueInt("CONSUME_AMT",gate.ngStaDailyMccConsumeAmt);
			setValueInt("AUTH_CNT", gate.ngStaDailyMccAuthCnt);
			setValueInt("DECLINE_CNT", gate.ngStaDailyMccDeclineCnt);

			setValueInt("PICKUP_CNT",gate.ngStaDailyMccPickupCnt);
			setValueInt("EXPIRED_CNT", gate.ngStaDailyMccExpiredCnt);
			setValueInt("AUTH_AMT",gate.ngStaDailyMccAuthAmt);
			setValueInt("DECLINE_AMT", gate.ngStaDailyMccDeclineAmt);

			setValueInt("CALLBANK_AMT", gate.ngStaDailyMccCallBankAmt);
			setValueInt("CALLBANK_CNT", gate.ngStaDailyMccCallBankCnt);
			setValueInt("CALLBANK_CNTX", gate.ngStaDailyMccCallBankCntx);
			setValueInt("CALLBANK_AMTX", gate.ngStaDailyMccCallBankAmtx); 			

			setValueInt("PICKUP_AMT", gate.ngStaDailyMccPickupAmt);
			setValueInt("EXPIRED_AMT", gate.ngStaDailyMccExpiredAmt);
			setValueInt("GENER_CNT", gate.ngStaDailyMccGenerAmt);
			setValueInt("ADJUST_CNT", gate.ngStaDailyMccAdjustCnt);
			setValueInt("REVERSAL_CNT", gate.ngStaDailyMccReversalCnt);
			setValueInt("RETURN_CNT", gate.ngStaDailyMccReturnCnt);
			setValueInt("RETURN_ADJ_CNT", gate.ngStaDailyMccReturnAdjCnt);
			setValueInt("FORCE_CNT", gate.ngStaDailyMccForceCnt);
			setValueInt("PREAUTH_CNT", gate.ngStaDailyMccPreauthCnt);
			setValueInt("PREAUTH_OK_CNT", gate.ngStaDailyMccPreauthOkCnt);
			setValueInt("MAIL_CNT", gate.ngStaDailyMccMailCnt);
			setValueInt("CASH_CNT", gate.ngStaDailyMccCashCnt);
			setValueInt("CASH_ADJ_CNT", gate.ngStaDailyMccCashAdjCnt);
			setValueInt("VOICE_CNT", 0);
			setValueInt("EC_CNT", gate.ngStaDailyMccEcCnt);

			setValueInt("GENER_AMT", gate.ngStaDailyMccGenerAmt);
			setValueInt("ADJUST_AMT", gate.ngStaDailyMccAdjustAmt);
			setValueInt("REVERSAL_AMT", gate.ngStaDailyMccReversalAmt);
			setValueInt("RETURN_AMT", gate.ngStaDailyMccReturnAmt);
			setValueInt("RETURN_ADJ_AMT", gate.ngStaDailyMccReturnAdjAmt);
			setValueInt("FORCE_AMT", gate.ngStaDailyMccForceAmt);
			setValueInt("PREAUTH_AMT", gate.ngStaDailyMccPreauthAmt);
			setValueInt("PREAUTH_OK_AMT", gate.ngStaDailyMccPreauthOkAmt);
			setValueInt("MAIL_AMT", gate.ngStaDailyMccMailAmt);
			setValueInt("CASH_AMT", gate.ngStaDailyMccCashAmt);
			setValueInt("CASH_ADJ_AMT", gate.ngStaDailyMccCashAdjAmt);
			setValueInt("VOICE_AMT",0);
			setValueInt("EC_AMT", gate.ngStaDailyMccEcAmt);
			setValue("AREA_TYPE", "T");

			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean insertStaDailyMcc() {
		gb.showLogMessage("I","insertStaDailyMcc(): started!");
		boolean blResult = true;


		try {
			daoTable = addTableOwner("CCA_STA_DAILY_MCC");

			setValue("BIN_NO",gate.sgSDailyMccBinNo);
			setValue("GROUP_CODE",gate.sgSDailyMccGroupCode);


			setValue("TX_SESSION",""+gate.ngTxSession);
			setValue("STA_DATE",gb.getSysDate());
			setValue("MCC_CODE",gate.mccCode.substring(0,4));

			setValue("RISK_TYPE",gate.sgSRskRiskType);


			setValue("UNNORMAL_CNT",""+gate.ngStaDailyMccUnNormalCnt);
			setValue("UNNORMAL_AMT", ""+gate.ngStaDailyMccUnNormalAmt);
			setValue("CONSUME_CNT",""+gate.ngStaDailyMccConsumeCnt);
			setValue("CONSUME_AMT",""+gate.ngStaDailyMccConsumeAmt);
			setValue("AUTH_CNT", ""+gate.ngStaDailyMccAuthCnt);
			setValue("DECLINE_CNT", ""+gate.ngStaDailyMccDeclineCnt);

			setValue("PICKUP_CNT",""+gate.ngStaDailyMccPickupCnt);
			setValue("EXPIRED_CNT", ""+gate.ngStaDailyMccExpiredCnt);
			setValue("AUTH_AMT",""+gate.ngStaDailyMccAuthAmt);
			setValue("DECLINE_AMT", ""+gate.ngStaDailyMccDeclineAmt);

			setValue("CALLBANK_AMT", ""+gate.ngStaDailyMccCallBankAmt);
			setValue("CALLBANK_CNT", ""+gate.ngStaDailyMccCallBankCnt);
			setValue("CALLBANK_CNTX", ""+gate.ngStaDailyMccCallBankCntx);
			setValue("CALLBANK_AMTX", ""+gate.ngStaDailyMccCallBankAmtx); 			

			setValue("PICKUP_AMT", ""+gate.ngStaDailyMccPickupAmt);
			setValue("EXPIRED_AMT", ""+gate.ngStaDailyMccExpiredAmt);
			setValue("GENER_CNT", ""+gate.ngStaDailyMccGenerAmt);
			setValue("ADJUST_CNT", ""+gate.ngStaDailyMccAdjustCnt);
			setValue("REVERSAL_CNT", ""+gate.ngStaDailyMccReversalCnt);
			setValue("RETURN_CNT", ""+gate.ngStaDailyMccReturnCnt);
			setValue("RETURN_ADJ_CNT", ""+gate.ngStaDailyMccReturnAdjCnt);
			setValue("FORCE_CNT", ""+gate.ngStaDailyMccForceCnt);
			setValue("PREAUTH_CNT", ""+gate.ngStaDailyMccPreauthCnt);
			setValue("PREAUTH_OK_CNT", ""+gate.ngStaDailyMccPreauthOkCnt);
			setValue("MAIL_CNT", ""+gate.ngStaDailyMccMailCnt);
			setValue("CASH_CNT", ""+gate.ngStaDailyMccCashCnt);
			setValue("CASH_ADJ_CNT", ""+gate.ngStaDailyMccCashAdjCnt);
			setValue("VOICE_CNT", "0");
			setValue("EC_CNT", ""+gate.ngStaDailyMccEcCnt);

			setValue("GENER_AMT", ""+gate.ngStaDailyMccGenerAmt);
			setValue("ADJUST_AMT", ""+gate.ngStaDailyMccAdjustAmt);
			setValue("REVERSAL_AMT", ""+gate.ngStaDailyMccReversalAmt);
			setValue("RETURN_AMT", ""+gate.ngStaDailyMccReturnAmt);
			setValue("RETURN_ADJ_AMT", ""+gate.ngStaDailyMccReturnAdjAmt);
			setValue("FORCE_AMT", ""+gate.ngStaDailyMccForceAmt);
			setValue("PREAUTH_AMT", ""+gate.ngStaDailyMccPreauthAmt);
			setValue("PREAUTH_OK_AMT", ""+gate.ngStaDailyMccPreauthOkAmt);
			setValue("MAIL_AMT", ""+gate.ngStaDailyMccMailAmt);
			setValue("CASH_AMT", ""+gate.ngStaDailyMccCashAmt);
			setValue("CASH_ADJ_AMT", ""+gate.ngStaDailyMccCashAdjAmt);
			setValue("VOICE_AMT","0");
			setValue("EC_AMT", ""+gate.ngStaDailyMccEcAmt);

			setValue("AREA_TYPE", "T");

			insertTable();


		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public boolean selectStaTxUnNormal() {
		gb.showLogMessage("I","selectStaTxUnNormal(): started!");
		boolean blResult = true;

		try {
			//kevin:取消service4Manual與service4BatchAuth設定，改為單筆connType 決定
//			if (gb.service4Manual)
			if ("WEB".equals(gate.connType)) 
				gate.sgStaTxUnNormalRespCode = gate.authStatusCode;
			else
				gate.sgStaTxUnNormalRespCode = gate.isoField[39];

			gate.sgStaTxUnNormalMccBinNo = gate.cardNo.substring(0, 6);

			if (gate.groupCode.length()==0)
				gate.sgStaTxUnNormalGroupCode= "0000";
			else
				gate.sgStaTxUnNormalGroupCode= gate.groupCode;

			gate.sgStaTxUnNormalRiskType = gate.mccRiskType; 
			if (gate.sgStaTxUnNormalRiskType.length()==0)
				gate.sgStaTxUnNormalRiskType = "9";


			daoTable = addTableOwner("CCA_STA_TX_UNORMAL");


			selectSQL = "NVL(TX_CNT,0) as StaTxUnNormalTxCnt ,NVL(TX_AMT,0)  as StaTxUnNormalTxAmt ";

			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND RESP_CODE= ? "
					+"AND RISK_TYPE= ? "
					+"AND AREA_TYPE= ? ";


			setString(1, gate.sgStaTxUnNormalMccBinNo);
			setString(2, gate.sgStaTxUnNormalGroupCode);

			setInt(3, gate.ngTxSession);
			setString(4, gb.getSysDate());
			setString(5, gate.sgStaTxUnNormalRespCode );
			setString(6, gate.sgStaTxUnNormalRiskType);
			setString(7, "T");
			/*
   			whereStr="WHERE BIN_NO  = :pBinNo "
						+"AND GROUP_CODE = :pGroupCode "
						+"AND TX_SESSION= :pTxSession "
						+"AND STA_DATE= :pStaDate "
						+"AND RESP_CODE= :pRespCode "
						+"AND RISK_TYPE= :pRiskType "
						+"AND AREA_TYPE= :pAreaType ";


   			setValue("pBinNo", gate.sG_StaTxUnNormalMccBinNo);
   			setValue("pGroupCode", gate.sG_StaTxUnNormalGroupCode);

   			setValueInt("pTxSession", gate.nG_TxSession);
   			setValue("pStaDate", gb.sysDate);
   			setValue("pRespCode", gate.sG_StaTxUnNormalRespCode );
   			setValue("pRiskType", gate.sG_StaTxUnNormalRiskType);
   			setValue("pAreaType", "T");
			 */
			selectTable();
			if ( "Y".equals(notFound) ) {
				blResult = false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}
	public boolean selectStaDailyMcc() {
		gb.showLogMessage("I","selectStaDailyMcc(): started!");
		boolean blResult = true;
		try {
			gate.sgSDailyMccBinNo = gate.cardNo.substring(0, 6);
			gate.sgSDailyMccGroupCode = "";


			if (gate.groupCode.length()==0)
				gate.sgSDailyMccGroupCode= "0000";
			else
				gate.sgSDailyMccGroupCode= gate.groupCode;

			daoTable = addTableOwner("CCA_STA_DAILY_MCC");

			selectSQL = "NVL(TX_SESSION, 0) as StaDailyMccTxSession ,     NVL(STA_DATE, ' ') as StaDailyMccStaDate, "
					+"NVL(MCC_CODE, ' ') as StaDailyMccMccCode,     NVL(RISK_TYPE, ' ') as StaDailyMccRiskType,"
					+"NVL(CONSUME_CNT, 0) as StaDailyMccConsumeCnt,    NVL(CONSUME_AMT, 0) as StaDailyMccConsumtAmt,"
					+"NVL(UNNORMAL_CNT, 0) as StaDailyMccUnNormalCnt,   NVL(UNNORMAL_AMT, 0) as StaDailyMccUnNormalAmt,"
					+"NVL(AUTH_CNT, 0) as StaDailyMccAuthCnt,       NVL(DECLINE_CNT, 0) as StaDailyMccDeclineCnt,"
					+"NVL(CALLBANK_CNT, 0) as StaDailyMccCallBankCnt,   NVL(PICKUP_CNT, 0) as StaDailyMccPickupCnt,"
					+"NVL(EXPIRED_CNT, 0) as StaDailyMccExpiredCnt,    NVL(AUTH_AMT, 0) as StaDailyMccAuthAmt,"
					+"NVL(DECLINE_AMT, 0) as StaDailyMccDeclineAmt,    NVL(CALLBANK_AMT, 0) as StaDailyMccCallBackAmt,"
					+"NVL(PICKUP_AMT, 0) as StaDailyMccPickupAmt,     NVL(EXPIRED_AMT, 0) as StaDailyMccExpiredAmt,"
					+"NVL(GENER_CNT, 0) as StaDailyMccGenerCnt,      NVL(ADJUST_CNT, 0) as StaDailyMccAdjustCnt,"
					+"NVL(REVERSAL_CNT, 0) as StaDailyMccReversalCnt,   NVL(RETURN_CNT, 0) as StaDailyMccReturnCnt,"
					+"NVL(RETURN_ADJ_CNT, 0) as StaDailyMccReturnAdjCnt, NVL(FORCE_CNT, 0) as StaDailyMccForceCnt,"
					+"NVL(PREAUTH_CNT, 0) as StaDailyMccPreauthCnt,    NVL(PREAUTH_OK_CNT, 0) as StaDailyMccPreauthOkCnt,"
					+"NVL(MAIL_CNT, 0) as StaDailyMccMailCnt,       NVL(CASH_CNT, 0) as StaDailyMccCashCnt,"
					+"NVL(CASH_ADJ_CNT, 0) as StaDailyMccCashAdjCnt,   NVL(VOICE_CNT, 0) as StaDailyMccVoiceCnt,"
					+"NVL(EC_CNT, 0) as StaDailyMccEcCnt,         NVL(CALLBANK_CNTX, 0) as StaDailyMccCallBankCntx,"
					+"NVL(GENER_AMT, 0) as StaDailyMccGenerAmt,      NVL(ADJUST_AMT, 0) as StaDailyMccAdjustAmt,"
					+"NVL(REVERSAL_AMT, 0) as StaDailyMccReversalAmt,   NVL(RETURN_AMT, 0) as StaDailyMccReturnAmt,"
					+"NVL(RETURN_ADJ_AMT, 0) as StaDailyMccReturnAdjAmt, NVL(FORCE_AMT, 0) as StaDailyMccForceAmt,"
					+"NVL(PREAUTH_AMT, 0) as StaDailyMccPreauthAmt,    NVL(PREAUTH_OK_AMT, 0) as StaDailyMccPreauthOkAmt,"
					+"NVL(MAIL_AMT, 0) as StaDailyMccMailAmt,       NVL(CASH_AMT, 0) as StaDailyMccCashAmt,"
					+"NVL(CASH_ADJ_AMT, 0) as StaDailyMccCashAdjAmt,   NVL(VOICE_AMT, 0) as StaDailyMccVoiceAmt,"
					+"NVL(EC_AMT, 0) as StaDailyMccEcAmt,         NVL(CALLBANK_AMTX, 0) as StaDailyMccCallBankAmtx";

			/*
   			whereStr="WHERE BIN_NO  = :pBinNo "
						+"AND GROUP_CODE = :pGroupCode "
						+"AND TX_SESSION= :pTxSession "
						+"AND STA_DATE= :pStaDate "
						+"AND MCC_CODE= :pMccCode "
						+"AND AREA_TYPE= :pAreaType ";

   			setValue("pBinNo", gate.sG_SDailyMccBinNo);
   			setValue("pGroupCode", gate.sG_SDailyMccGroupCode);
   			setValue("pTxSession", ""+gate.nG_TxSession);
   			setValue("pStaDate", gb.sysDate);
   			setValue("pMccCode", gate.mccCode.substring(0,4));
   			setValue("pAreaType", "T");
			 */
			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND MCC_CODE= ? "
					+"AND AREA_TYPE= ? ";


			setString(1, gate.sgSDailyMccBinNo);
			setString(2, gate.sgSDailyMccGroupCode);
			setInt(3, gate.ngTxSession);
			setString(4, gb.getSysDate());
			setString(5, gate.mccCode.substring(0,4));
			setString(6, "T");



			selectTable();
			if ( "Y".equals(notFound) ) {
				blResult = false;
			}

		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}
	public boolean selectStaRiskType() {
		gb.showLogMessage("I","selectStaRiskType(): started!");
		boolean blResult = true;
		try {

			//kevin:取消service4Manual設定，改為單筆connType 決定
//			if (gb.service4Manual)
			if ("WEB".equals(gate.connType))
				gate.sgSRskCurrRespCode = gate.authStatusCode;
			else
				gate.sgSRskCurrRespCode = gate.isoField[39];

			gate.sgSRskTypeBinNo = gate.cardNo.substring(0, 6);

			if (gate.groupCode.length()==0)
				gate.sgSRskTypeGroupCode= "0000";
			else
				gate.sgSRskTypeGroupCode= gate.groupCode;


			gate.sgSRskRiskType = gate.mccRiskType; 
			if (gate.sgSRskRiskType.length()==0)
				gate.sgSRskRiskType = "9";

			daoTable = addTableOwner("CCA_STA_RISK_TYPE");

			selectSQL = "NVL(TX_SESSION,0) as StaRiskTypeTxSession,    STA_DATE as StaRiskTypeStaDate, "
					+"PRD_TYPE as StaRiskTypePrdType,             RISK_TYPE as StaRiskTypeRiskType,"
					+"UNNORMAL_CODE as StaRiskTypeUnNormalCode,        PRD_GROUP as StaRiskTypePrdGroup,"
					+"CARD_TYPE as StaRiskTypeCardType,            UNNORMAL_FLAG as StaRiskTypeUnNormalFlag,"
					+"NVL(UNNORMAL_CNT,0) as StaRiskTypeUnNormalCnt,  NVL(UNNORMAL_AMT,0) as StaRiskTypeUnNormalAmt,"
					+"NVL(CONSUME_CNT,0) as StaRiskTypeConsumeCnt,   NVL(CONSUME_AMT,0) as StaRiskTypeConsumeAmt,"
					+"NVL(AUTH_CNT,0) as StaRiskTypeAuthCnt,      NVL(DECLINE_CNT,0) as StaRiskTypeDeclineCnt,"
					+"NVL(CALLBANK_CNT,0) as StaRiskTypeCallBankCnt,  NVL(PICKUP_CNT,0) as StaRiskTypePickupCnt,"
					+"NVL(EXPIRED_CNT,0) as StaRiskTypeExpiredCnt,   NVL(AUTH_AMT,0) as StaRiskTypeAuthAmt,"
					+"NVL(DECLINE_AMT,0) as StaRiskTypeDeclineAmt,   NVL(CALLBANK_AMT,0) as StaRiskTypeCallBankAmt,"
					+"NVL(PICKUP_AMT,0) as StaRiskTypePickupAmt,    NVL(EXPIRED_AMT,0) as StaRiskTypeExpiredAmt,"
					+"NVL(GENER_CNT,0) as StaRiskTypeGenerCnt,     NVL(ADJUST_CNT,0) as StaRiskTypeAdjustCnt,"
					+"NVL(REVERSAL_CNT,0) as StaRiskTypeReversalCnt,  NVL(RETURN_CNT,0) as StaRiskTypeReturnCnt,"
					+"NVL(RETURN_ADJ_CNT,0) as StaRiskTypeReturnAdjCnt,NVL(FORCE_CNT,0) as StaRiskTypeForceCnt,"
					+"NVL(PREAUTH_CNT,0) as StaRiskTypePreauthCnt,   NVL(PREAUTH_OK_CNT,0) as StaRiskTypePreauthOkCnt,"
					+"NVL(MAIL_CNT,0) as StaRiskTypeMailCnt,      NVL(CASH_CNT,0) as StaRiskTypeCashCnt,"
					+"NVL(CASH_ADJ_CNT,0) as StaRiskTypeCashAdjCnt,  NVL(VOICE_CNT,0) as StaRiskTypeVoiceCnt,"
					+"NVL(EC_CNT,0) as StaRiskTypeEcCnt,        NVL(CALLBANK_CNTX,0) as StaRiskTypeCallBankCntx,"
					+"NVL(GENER_AMT,0) as StaRiskTypeGenerAmt,     NVL(ADJUST_AMT,0) as StaRiskTypeAdjustAmt,"
					+"NVL(REVERSAL_AMT,0) as StaRiskTypereversalAmt,  NVL(RETURN_AMT,0) as StaRiskTypeReturnAmt,"
					+"NVL(RETURN_ADJ_AMT,0) as StaRiskTypeReturnAdjAmt,  NVL(FORCE_AMT,0) as StaRiskTypeForceAmt,"
					+"NVL(PREAUTH_AMT,0) as StaRiskTypePreauthAmt,   NVL(PREAUTH_OK_AMT,0) as StaRiskTypePreauthOkAmt,"
					+"NVL(MAIL_AMT,0) as StaRiskTypeMailAmt,      NVL(CASH_AMT,0) as StaRiskTypeCashAmt,"
					+"NVL(CASH_ADJ_AMT,0) as StaRiskTypeCashAdjAmt,  NVL(VOICE_AMT,0) as StaRiskTypeVoiceAmt,"
					+"NVL(EC_AMT,0) as StaRiskTypeEcAmt,        NVL(CALLBANK_AMTX,0) as StaRiskTypeCallBankAmtx,"
					+"ROWID as StaRiskTypeRowId ";
			whereStr="WHERE BIN_NO  = ? "
					+"AND GROUP_CODE = ? "
					+"AND TX_SESSION= ? "
					+"AND STA_DATE= ? "
					+"AND PRD_TYPE=  ? "
					+"AND RISK_TYPE= ? "
					+"AND UNNORMAL_CODE= ? ";
			setString(1, gate.sgSRskTypeBinNo);
			setString(2, gate.sgSRskTypeGroupCode);

			setInt(3, gate.ngTxSession);
			setString(4, gb.getSysDate());
			setString(5, gate.sgUsedCardProd);
			setString(6, gate.sgSRskRiskType);
			setString(7, gate.sgSRskCurrRespCode);

			/*
   			whereStr="WHERE BIN_NO  = :pBinNo "
   								+"AND GROUP_CODE = :pGroupCode "
   								+"AND TX_SESSION= :pTxSession "
   								+"AND STA_DATE= :pStaDate "
   								+"AND PRD_TYPE= :pPrdType "
   								+"AND RISK_TYPE= :pRiskType "
   								+"AND UNNORMAL_CODE= :UnNormalCode ";
   			setValue("pBinNo", gate.sG_SRskTypeBinNo);
   			setValue("pGroupCode", gate.sG_SRskTypeGroupCode);
   			setValueInt("pTxSession", gate.nG_TxSession);
   			setValue("pStaDate", gb.sysDate);
   			setValue("pPrdType", gate.sG_UsedCardProd);
   			setValue("pRiskType", gate.sG_SRskRiskType);
   			setValue("UnNormalCode", gate.sG_SRskCurrRespCode);
			 */   


			selectTable();
			if ( "Y".equals(notFound) ) {
				blResult = false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}
	public boolean selectCycBpcd(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectCycBpcd(): started!");
		boolean blResult = true;
		try {
			String slT1 = addTableOwner("cyc_bpcd");
			String slT2 = addTableOwner("crd_card");

			daoTable = slT1 + " a " + "," + slT2 + " b ";
			//daoTable ="cyc_bpcd a , crd_card b";

			selectSQL = "a.p_seqno,"
					+ "a.use_bp,"
					+ "a.net_ttl_bp,"
					+ "a.net_ttl_tax_1,"
					+ "a.net_ttl_notax_1,"
					+ "a.net_ttl_tax_2,"
					+ "a.net_ttl_notax_2,"
					+ "a.net_ttl_tax_3,"
					+ "a.net_ttl_notax_3,"
					+ "a.net_ttl_tax_4,"
					+ "a.net_ttl_notax_4,"
					+ "a.net_ttl_tax_5,"
					+ "a.net_ttl_notax_5,"
					+ "a.pre_subbp,"
					+ "a.trans_bp,"
					+ "b.acct_type,"
					//+ "(a.net_ttl_notax_1+a.net_ttl_notax_2+a.net_ttl_notax_3+a.net_ttl_notax_4+a.net_ttl_notax_5) net1,"
					//+ "(a.net_ttl_tax_1+a.net_ttl_tax_2+a.net_ttl_tax_3+a.net_ttl_tax_4+a.net_ttl_tax_5) net2,"					   					
					+ "b.group_code,"
					+ "b.card_type,"
					+ "a.rowid  as rowid ";



			whereStr="WHERE b.card_no   = ?   "
					+ "and a.acct_type = b.acct_type  "
					+ "and a.p_seqno   = b.p_seqno  "
					+ "and a.type_code = 'BONU' ";


			setString(1, pBilOBase.hCardNo);



			selectTable();
			if ( "Y".equals(notFound) ) {
				pBilOBase.hRespCd = "R7";
				blResult=false;
			}
			else {
				pBilOBase.hBpcdPSeqno         = getValue("p_seqno");
				pBilOBase.hBpcdUseBp          = getDouble("use_bp");
				pBilOBase.hBpcdNetTtlBp      = getDouble("net_ttl_bp");
				pBilOBase.hBpcdNetTtlTax1   = getDouble("net_ttl_tax_1");
				pBilOBase.hBpcdNetTtlNotax1 = getDouble("net_ttl_notax_1");
				pBilOBase.hBpcdNetTtlTax2   = getDouble("net_ttl_tax_2");
				pBilOBase.hBpcdNetTtlNotax2 = getDouble("net_ttl_notax_2");
				pBilOBase.hBpcdNetTtlTax3   = getDouble("net_ttl_tax_3");
				pBilOBase.hBpcdNetTtlNotax3 = getDouble("net_ttl_notax_3");
				pBilOBase.hBpcdNetTtlTax4   = getDouble("net_ttl_tax_4");
				pBilOBase.hBpcdNetTtlNotax4 = getDouble("net_ttl_notax_4");
				pBilOBase.hBpcdNetTtlTax5   = getDouble("net_ttl_tax_5");
				pBilOBase.hBpcdNetTtlNotax5 = getDouble("net_ttl_notax_5");
				pBilOBase.hBpcdPreSubbp  = getDouble("pre_subbp");
				pBilOBase.hBpcdTransBp   = getDouble("trans_bp");
				pBilOBase.hCardAcctType  = getValue("acct_type");
				pBilOBase.hCardPSeqno    = getValue("p_seqno");
				pBilOBase.hCardGroupCode = getValue("group_code");
				pBilOBase.hCardCardType  = getValue("card_type");
				pBilOBase.hBpcdRowid      = getValue("rowid");

			}

		} catch (Exception e) {
			// TODO: handle exception
			pBilOBase.hRespCd = "R7";
			blResult = false;
		}
		return blResult;

	}
	/**
	 * 取得mkt_bonus_dtl累計紅利點數，有效日期(end)>當天的所有點數加總BP。
	 * V1.00.37 P3紅利兌換處理方式調整
	 * @return boolean false (BP < 0) ; true (BP >= 0)
	 * @throws Exception if any exception occurred
	 */
	public boolean selectMktBonusDtl(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectMktBonusDtl(): started!");
		boolean blResult = true;
		try {
			daoTable = addTableOwner("mkt_bonus_dtl");

			selectSQL = "sum(end_tran_bp + res_tran_bp) "
					+ "as mktBonusDtlEndTranBp ";

			whereStr=" where p_seqno     = ? "
		            + "and bonus_type  = 'BONU' "
					+ "and decode(effect_e_date,'','99999999',effect_e_date) > to_char(sysdate,'yyyymmdd') "
					+ "and apr_flag = 'Y' ";


			setString(1, pBilOBase.hCardPSeqno);



			selectTable();
			pBilOBase.hBpcdNetTtlBp = getDouble("mktBonusDtlEndTranBp");
			pBilOBase.hOldNetTtlBp  = pBilOBase.hBpcdNetTtlBp;
			if (pBilOBase.hBpcdNetTtlBp < 0) {
				pBilOBase.hRespCd = "R7";
				blResult=false;
			}
			else {
				pBilOBase.hBpcdPSeqno    = getValue("p_seqno");
				pBilOBase.hBpcdPreSubbp  = getDouble("pre_subbp");
				pBilOBase.hBpcdTransBp   = getDouble("trans_bp");
				pBilOBase.hCardAcctType  = getValue("acct_type");
				pBilOBase.hCardPSeqno    = getValue("p_seqno");
				pBilOBase.hCardGroupCode = getValue("group_code");
				pBilOBase.hCardCardType  = getValue("card_type");
			}
		} catch (Exception e) {
			// TODO: handle exception
			pBilOBase.hRespCd = "R7";
			blResult = false;
		}
		return blResult;

	}


	public int selectPtrRedeem(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectPtrRedeem(): started!");
		int nlResult = 0;
		try {
			daoTable = addTableOwner("PTR_REDEEM");

			selectSQL = "rdm_seqno,"
					+ "rdm_destamt,"
					+ "rdm_discrate,"
					+ "rdm_discamt,"
					+ "rdm_unitpoint,"
					+ "rdm_unitamt,"
					+ "rdm_binflag ";
			whereStr="where rdm_mchtno = ?  "
					+ "and rdm_strdate <= ?  "
					+ "and rdm_enddate >= ?";



			setString(1, pBilOBase.hAccpIdCd);
			setString(2, pBilOBase.hInlgTxDate);//Howard:這個值需確認
			setString(3, pBilOBase.hInlgTxDate); //Howard:這個值需確認
			selectTable();

			if ( "Y".equals(notFound) ) {
				nlResult  = 1;

			}
			else {
				pBilOBase.hRedeRdmSeqno = getDouble("rdm_seqno");
				pBilOBase.hRedeRdmDestamt = getDouble("rdm_destamt");
				pBilOBase.hRedeRdmDiscrate = getDouble("rdm_discrate");
				pBilOBase.hRedeRdmDiscamt = getDouble("rdm_discamt");
				pBilOBase.hRedeRdmUnitpoint = getDouble("rdm_unitpoint");
				pBilOBase.hRedeRdmUnitamt = getDouble("rdm_unitamt");
				pBilOBase.hRedeRdmBinflag = getValue("rdm_binflag");

			}

		} catch (Exception e) {
			// TODO: handle exception
			nlResult  = 1;
		}
		return nlResult;
	}
	public int selectPtrRedeemDtl1(BilOBase pBilOBase, String spDetailkind, String spdetailValue) throws Exception {
		gb.showLogMessage("I","selectPtrRedeemDtl1(): started!");
		int nlResult = 0;

		try {
			daoTable = addTableOwner("ptr_redeem_dtl1");

			selectSQL = "count(*) as temp_int";
			whereStr="where MERCHANT_NO = ?  "
					+ "and seq_no  = ?   "
					+ "and dtl_kind  = ? "
					+ "and dtl_value = ? ";



			setString(1, pBilOBase.hAccpIdCd);
			setDouble(2, pBilOBase.hRedeRdmSeqno);

			setString(3, spDetailkind);
			setString(4, spdetailValue);
			selectTable();

			if (getInteger("temp_int")==0) {

				nlResult=0;
			}
			else {

				nlResult = getInteger("temp_int");	 
			}





		} catch (Exception e) {
			// TODO: handle exception
			nlResult = 0;
		}
		return nlResult;
	}

	public boolean selectBilRedeem(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","selectBilRedeem(): started!");
		boolean blResult = true;
		try {
			daoTable = addTableOwner("bil_redeem");

			selectSQL = "disc_rate,"
					+"dest_amt,"
					+"disc_amt,"
					+"unit_point,"
					+"unit_amt";
			whereStr="";
			selectTable();
			if ( "Y".equals(notFound) ) {
				blResult = false;
			}
			else {

				blResult = true;	 
			}


		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}
		return blResult;
	}
	
	public boolean getRskFactorParm() throws Exception {
		gb.showLogMessage("I","getRskFactorParm(): started!");
		//kevin: 取得風險參數 riskFactorParm
//		double slRiskScore=0;
		boolean blResult = false;

		daoTable = addTableOwner("rsk_factor_parm");	

		selectSQL ="mcc_code_flag,"
				  +"pos_flag,"
				  +"country_flag,"
				  +"black_mcht_flag,"
				  +"black_card_flag,"
				  +"card_risk_factor,"
				  +"repeat_txn_flag,"
				  +"repeat_factor,"
				  +"in_vip_flag,"
				  +"vip_factor,"
				  +"amt_base_flag,"
				  +"txn_amt_base";

		whereStr="WHERE 1=1";

		selectTable();

		if ( "Y".equals(notFound) ) {
			return blResult;
		}
		blResult = true;
		return blResult;

//		if ("Y".equals(getValue("pos_flag"))) {
//			if (gate.riskFctorInd < 1) {
//					gate.entryModeType = getEntryModeType(gate.entryMode);
//					gate.riskFctorInd  = 1; //kevin: 1. 計算 Pos Entry Mode 風險分數
//				}	
//			slRiskScore += gate.posRiskFactor;
//		}
//		if ("Y".equals(getValue("mcc_code_flag"))) {
//			if (gate.riskFctorInd < 2) {
//				if (selectMccRisk()) {
//					gate.mccRiskFactor = getDouble("MccRiskFactor");
//				}
//				gate.riskFctorInd = 2;     //kevin: 2. 計算 Mcc_code 風險分數
//			}
//			slRiskScore += gate.mccRiskFactor;
//		}
//		if ("Y".equals(getValue("country_flag"))) {
//			if (gate.riskFctorInd < 3) {
//				if (selectCountry()) {
//					gate.countryRiskFactor = getDouble("ountryRiskFactor");
//				}
//				gate.riskFctorInd = 3;     //kevin: 3. 計算 國別  風險分數	
//			}
//			slRiskScore += gate.countryRiskFactor;
//		}
//		if ("Y".equals(getValue("black_mcht_flag"))) {
//			if (gate.riskFctorInd < 4) {
//				checkMchtRisk();
//				gate.riskFctorInd = 4;     //kevin: 4. 計算黑名單特店  風險分數	
//			}
//			slRiskScore += gate.mchtRiskFactor;
//		}
//		if ("Y".equals(getValue("black_card_flag"))) {
//			if (gate.riskFctorInd < 5) {
//				if (selectBlockCard()) {
//					gate.cardRiskFactor = getDouble("card_risk_factor");
//				}
//				gate.riskFctorInd = 5;     //kevin: 5. 計算 黑名單卡號 風險分數	
//			}
//			slRiskScore += gate.cardRiskFactor;
//		}
//		if ("Y".equals(getValue("repeat_txn_flag"))) {
//			if (gate.riskFctorInd < 6) {
//				gate.repeatRiskFactor += ((gate.ccaConsumeTxTotCntDay + gate.ccaConsumeRejAuthCntDay) *
//								           getDouble("repeat_factor"));
//				gb.showLogMessage("D","repeat成功=" +gate.ccaConsumeTxTotCntDay);
//				gb.showLogMessage("D","repeat失敗=" +gate.ccaConsumeRejAuthCntDay);
//				gb.showLogMessage("D","repeat倍數=" +getDouble("repeat_factor"));
//				gb.showLogMessage("D","repeat分數=" +gate.repeatRiskFactor);	
//				gate.riskFctorInd = 6;     //kevin: 6. 計算 重覆交易  風險分數	
//			}
//			slRiskScore += gate.repeatRiskFactor;
//		}
//		if ("Y".equals(getValue("in_vip_flag"))) {
//			if (gate.riskFctorInd < 7) {
////				if (selectCcaVip(getValue("CardBaseAcctType").trim())) {
//				if (gate.isAuthVip) {
//					gate.vipRiskFactor = getDouble("vip_factor");
//					gb.showLogMessage("D","VIP分數=" +gate.vipRiskFactor);
//				}
//				gate.riskFctorInd = 7;     //kevin: 7. 計算 VIP 名單 風險分數	
//			}
//			slRiskScore += gate.vipRiskFactor;
//		}
//		if ("Y".equals(getValue("amt_base_flag"))) {
//			if (gate.riskFctorInd < 8) {
//				gate.amtRiskFactor = Math.round(gate.ntAmt / getDouble("txn_amt_base"));
//				gate.riskFctorInd = 8;     //kevin: 8. 計算 交易金額基數 風險分數	
//			}
//			slRiskScore += gate.amtRiskFactor;
//		}
//		if (slRiskScore > 999) {
//			slRiskScore = 999;
//		}
//		return slRiskScore;
	}

	public void preInsertCycBpjr(BilOBase pBilOBase, String spAdjReason) throws Exception {
		gb.showLogMessage("I","preInsertCycBpjr(): started!");
		pBilOBase.hBpjrAcctDate = HpeUtil.getCurDateStr(false);

		pBilOBase.hBpjrPSeqno = pBilOBase.hBpcdPSeqno;
		pBilOBase.hBpjrAcctType = pBilOBase.hCardAcctType;
		pBilOBase.hBpjrPSeqno = pBilOBase.hCardPSeqno;
		pBilOBase.hBpjrGiftName = pBilOBase.prgmId;
		pBilOBase.hBpjrCardNo = pBilOBase.hCardNo;
		pBilOBase.hBpjrTypeCode = "BONU";
		pBilOBase.hBpjrTransCode = "ADJ";
		pBilOBase.hBpjrReasonCode = "";
		pBilOBase.hBpjrAdjustReason = spAdjReason;

		pBilOBase.hBpjrTransBp = (-1) * pBilOBase.hPointRede;
		pBilOBase.hBpjrNetBp = (int)pBilOBase.hBpcdNetTtlBp + pBilOBase.hBpjrTransBp;
		pBilOBase.hBpjrNetTtlNotaxBef = pBilOBase.hOldNetTtlNotaxAll;
		pBilOBase.hBpjrNetTtlTaxBef = pBilOBase.hOldNetTtlTaxAll;

		pBilOBase.hBpjrGiftNo = "";
		pBilOBase.hBpjrGiftName = "";
		pBilOBase.hBpjrGiftCnt = 0;
		pBilOBase.hBpjrGiftCashValue = 0;
		pBilOBase.hBpjrGiftPayCash = pBilOBase.hPaidAmt; //Howard:h_paid_amt 的值是哪來的呢?


		insertCycBpjr(pBilOBase);

	}
	public void insertCycBpjr(BilOBase pBilOBase) throws Exception {
		gb.showLogMessage("I","insertCycBpjr(): started!");
		try {
			setValue("p_seqno", pBilOBase.hBpjrPSeqno);
			setValue("acct_type", pBilOBase.hBpjrAcctType);
			setValue("type_code", pBilOBase.hBpjrTypeCode);
			setValue("card_no", pBilOBase.hBpjrCardNo);
			setValue("acct_date", pBilOBase.hBpjrAcctDate);
			setValue("trans_code", pBilOBase.hBpjrTransCode);

			//setValueInt("trans_bp", P_BilOBase.h_bpjr_trans_bp);
			setValue("trans_bp", ""+pBilOBase.hBpjrTransBp);

			//setValueInt("net_bp", P_BilOBase.h_bpjr_net_bp);
			setValue("net_bp", ""+pBilOBase.hBpjrNetBp);

			//setValueDouble("trans_bp_tax", P_BilOBase.h_bpjr_trans_bp_tax);
			setValue("trans_bp_tax", "" +pBilOBase.hBpjrTransBpTax);

			//setValueDouble("net_ttl_tax_bef", P_BilOBase.h_bpjr_net_ttl_tax_bef);
			setValue("net_ttl_tax_bef", ""+pBilOBase.hBpjrNetTtlTaxBef);

			//setValueDouble("net_ttl_notax_bef", P_BilOBase.h_bpjr_net_ttl_notax_bef);
			setValue("net_ttl_notax_bef", ""+pBilOBase.hBpjrNetTtlNotaxBef);

			setValue("gift_no", pBilOBase.hBpjrGiftNo);
			setValue("gift_name", pBilOBase.hBpjrGiftName);

			//setValueInt("gift_cnt", P_BilOBase.h_bpjr_gift_cnt);
			setValue("gift_cnt", ""+pBilOBase.hBpjrGiftCnt);

			//setValueDouble("gift_cash_value", P_BilOBase.h_bpjr_gift_cash_value);
			setValue("gift_cash_value", ""+pBilOBase.hBpjrGiftCashValue);

			//setValueDouble("gift_pay_cash", P_BilOBase.h_bpjr_gift_pay_cash);
			setValue("gift_pay_cash", ""+pBilOBase.hBpjrGiftPayCash);

			setValue("reason_code", pBilOBase.hBpjrReasonCode);
			setValue("sub_code", "B");
			setValue("mod_user", "BIL");
			//setValue("mod_time", gb.sysDate + gb.sysTime);
			setTimestamp("MOD_TIME",gb.getgTimeStamp());

			setValue("mod_pgm", pBilOBase.prgmId);
			daoTable = addTableOwner("cyc_bpjr");
			insertTable();
			if ("Y".equals(dupRecord)) {
				gb.showLogMessage("D", "insert " + daoTable + " duplicate!");
				pBilOBase.hRespCd = "23";
			}
		} catch(Exception ex) {
			gb.showLogMessage("E", String.format("insert_cyc_bpjr error= [%s]", ex.getMessage()));
			pBilOBase.hRespCd = "23";
		}

	}

	/**
	 * BONUS紅利折抵方式直接新增到帳戶紅利明細檔 
	 * V1.00.37 P3紅利兌換處理方式調整
	 * @throws Exception if any exception occurred
	 */
	public int insertMktBonusDtl(BilOBase pBilOBase) throws Exception {

		selectSQL = "nextval for " + addTableOwner("MKT_MODSEQ") + " as mod_seqno";
		daoTable = addTableOwner("dual");
		selectTable();
		String tran_seqno = String.format("%010.0f", getDouble("MOD_SEQNO"));
		while (tran_seqno.length() < 10)
			tran_seqno = "0" + tran_seqno;

		setValue("beg_tran_bp", "" + (pBilOBase.hPointRede * -1));
		setValue("end_tran_bp", "" + (pBilOBase.hPointRede * -1));
		setValue("tax_tran_bp", "0");
		setValue("res_tran_bp", "0");
		setValue("move_bp", "0");
		setValue("move_cnt", "0");
		// setValue("active_code" , getValue("active_code"));
		setValue("active_name", "紅利折抵線上作業");
		setValue("p_seqno", pBilOBase.hCardPSeqno);
		setValue("id_p_seqno", pBilOBase.hCardIdPSeqno);
		setValue("acct_type", pBilOBase.hCardAcctType);
		setValue("mod_memo", "卡號:" + pBilOBase.hCardNo + " 符合回饋條件");
		setValue("mod_desc", "紅利折抵線上作業");
		setValue("tax_flag", "N");
		setValue("tran_code", pBilOBase.hTranCode); // 7:紅利扣回 4:紅利使用
		setValue("tran_date", gb.getSysDate());
		setValue("tran_time", gb.getSysTime());
		setValue("tran_seqno", tran_seqno);
		// setValue("effect_e_date" ,
		// comm.nextMonthDate(h_busi_business_date,getValueInt("parm.effect_months",0)));
		setValue("bonus_type", "BONU");
		setValue("acct_date", pBilOBase.hBusiBusinessDate);
		setValue("proc_month", pBilOBase.hBusiBusinessDate.substring(0, 6));
		setValue("tran_pgm", pBilOBase.prgmId);
		setValue("apr_flag", "Y");
		setValue("apr_user", pBilOBase.prgmId);
		setValue("apr_date", gb.getSysDate());
		setValue("crt_user", pBilOBase.prgmId);
		setValue("crt_date", gb.getSysDate());
		setValue("mod_user", "BIL");
		setTimestamp("MOD_TIME", gb.getgTimeStamp());
		setValue("mod_pgm", pBilOBase.prgmId);
		setValue("mod_seqno", "0");
		setValue("major_card_no", pBilOBase.hCardMajorCardNo);

		daoTable = addTableOwner("mkt_bonus_dtl");

		insertTable();

		return (0);
	}

	/**
	 * 取得營業日-紅利明細檔
	 * V1.00.37 P3紅利兌換處理方式調整
	 * @throws Exception if any exception occurred
	 */

	public void selectPtrBusinday(BilOBase pBilOBase) throws Exception {
		selectSQL = "";
		daoTable = addTableOwner("ptr_businday");
		whereStr = "FETCH FIRST 1 ROW ONLY";

		selectTable();

		if (pBilOBase.hBusiBusinessDate.length() == 0)
			pBilOBase.hBusiBusinessDate = getValue("BUSINESS_DATE");

		gb.showLogMessage("I", "本日營業日 : [" + pBilOBase.hBusiBusinessDate + "]");
	}
	
	//kevin:新增三大票證檢查卡號取得real card no和票證狀態
	// 讀取 TSC_CARD
	public boolean selectTscCard(boolean tscVdTableFlag) throws Exception {
		gb.showLogMessage("I","selectTscCard(): started!");

//		daoTable  = addTableOwner("TSC_CARD"); 
		String slCardNoName="";
		if (tscVdTableFlag) {
			slCardNoName = " VD_CARD_NO as TscCardCardNo,";
			daoTable  = addTableOwner("TSC_VD_CARD"); 
		}
		else {
			slCardNoName = " CARD_NO as TscCardCardNo,";
			daoTable  = addTableOwner("TSC_CARD"); 
		}
		selectSQL = slCardNoName
				+ "CURRENT_CODE as TscCardCurrentCode," 
				+ "AUTOLOAD_AMT as TscAutoLoadAmt,"   
				+ "AUTOLOAD_FLAG as TscAutoLoadFlag,"
				+ "NEW_END_DATE as TscCardNewEndDate, "
				+ "RETURN_FLAG as TscCardRuturnFlag, "
				+ "LOCK_FLAG as TscLockFlag, "
				+ "BLACKLT_FLAG as TscBlackltFlag, "
				+ "LAST_ADDVALUE_DATE as TscLastAddValueDate, "
				+ "DAY_CNT as TscDayCnt, "
				+ "DAY_AMT as TscDayAmt" ;   

		whereStr  = "WHERE TSC_CARD_NO = ? ";
//		if (tscVdTableFlag) {
//			daoTable  = addTableOwner("TSC_VD_CARD"); 
//		}
//		else {
//			daoTable  = addTableOwner("TSC_CARD"); 
//		}
		setString(1,gate.isoField[2]);
//		gb.showLogMessage("D","@@@@vd_selectSql"+selectSQL);
		selectTable();

		if ("Y".equals(notFound)) {
			gb.showLogMessage("I","function: TA.selectCrdCard -- can not find data. EASY_CARD_NO is  "+gate.isoField[2] + "--");
			return false;
		}
		else {
			gate.cardNo = getValue("TscCardCardNo");
			if ("".equals(gate.expireDate)) {
				gate.expireDate = getValue("TscCardNewEndDate").substring(2, 6);
			}
			return true;
		}
	}
	//kevin:新增三大票證檢查卡號取得real card no和票證狀態
	// 讀取 IPS_CARD
	public boolean selectIpsCard() throws Exception {
		gb.showLogMessage("I","selectIpsCard(): started!");

		daoTable  = addTableOwner("IPS_CARD"); 
		selectSQL = "CARD_NO as IpsCardCardNo,"
				+ "CURRENT_CODE as IpsCardCurrentCode," 
				+ "AUTOLOAD_AMT as IpsAutoLoadAmt,"   
				+ "AUTOLOAD_FLAG as IpsAutoLoadFlag," 
				+ "NEW_END_DATE as IpsCardNewEndDate, "
				+ "RETURN_FLAG as IpsCardRuturnFlag, "
				+ "LOCK_FLAG as IpsLockFlag, "
				+ "BLACKLT_FLAG as IpsBlackltFlag, "
				+ "LAST_ADDVALUE_DATE as IpsLastAddValueDate, "
				+ "DAY_CNT as IpsDayCnt, "
				+ "DAY_AMT as IpsDayAmt";         


		whereStr  = "WHERE IPS_CARD_NO = ? ";
		setString(1,gate.isoField[2]);
		selectTable();

		if ("Y".equals(notFound)) {
			gb.showLogMessage("I","function: TA.selectCrdCard -- can not find data. IPASS_CARD_NO is  "+gate.isoField[2] + "--");
			return false;
		}
		else {
			gate.cardNo = getValue("IpsCardCardNo");
			if ("".equals(gate.expireDate)) {
				gate.expireDate = getValue("IpsCardNewEndDate").substring(2, 6);
			}
			return true;
		}
	}
	//kevin:新增三大票證檢查卡號取得real card no和票證狀態
	// 讀取 ICH_CARD
	public boolean selectIchCard() throws Exception {
		gb.showLogMessage("I","selectIchCard(): started!");

		daoTable  = addTableOwner("ICH_CARD"); 
		selectSQL = "CARD_NO as IchCardCardNo,"
				+ "CURRENT_CODE as IchCardCurrentCode," 
				+ "AUTOLOAD_AMT as IchAutoLoadAmt,"   
				+ "AUTOLOAD_FLAG as IchAutoLoadFlag,"
				+ "NEW_END_DATE as IchCardNewEndDate, "
				+ "RETURN_FLAG as IchCardRuturnFlag, "
				+ "LOCK_FLAG as IchLockFlag, "
				+ "BLACKLT_FLAG as IchBlackltFlag, "
				+ "LAST_ADDVALUE_DATE as IchLastAddValueDate, "
				+ "DAY_CNT as IchDayCnt, "
				+ "DAY_AMT as IchDayAmt";    


		whereStr  = "WHERE ICH_CARD_NO = ? ";
		setString(1,gate.isoField[2]);
		selectTable();

		if ("Y".equals(notFound)) {
			gb.showLogMessage("I","function: TA.selectCrdCard -- can not find data. ICASH_CARD_NO is  "+gate.isoField[2] + "--");
			return false;
		}
		else {
			gate.cardNo = getValue("IchCardCardNo");
			if ("".equals(gate.expireDate)) {
				gate.expireDate = getValue("IchCardNewEndDate").substring(2, 6);
			}
			return true;
		}
	}
	// 更新 TSC CARD 交易紀錄
	public void updateTscCard(boolean bpFlag) throws Exception {
		gb.showLogMessage("I","updateTscCard(): started!");

		int liCnt=0, ii=1;

		StringBuffer lsSql = new StringBuffer();

		daoTable  = addTableOwner("TSC_CARD");
		
		if (bpFlag ) {
			lsSql.append("AUTOLOAD_FLAG= ?, ");  ii++;
			lsSql.append("AUTOLOAD_DATE = ? ");  ii++;
			setString(1,"Y");
			setString(2,gb.getSysDate()); 	
		}
		else {
			lsSql.append("DAY_CNT= ?, "); ii++;
			lsSql.append("LAST_ADDVALUE_DATE = ?, "); ii++;
			lsSql.append("DAY_AMT= ? "); ii++;
			if (gb.getSysDate().compareTo(getValue("TscLastAddValueDate"))==0) {
				setInt(1,getInteger("TscDayCnt")+1);	
				setString(2,gb.getSysDate()); 	
				setDouble(3,getDouble("TscDayAmt")+gate.isoFiled4Value);	
			}
			else {
				setInt(1,1);	
				setString(2,gb.getSysDate()); 
				setDouble(3,gate.isoFiled4Value);	
			}			
		}
	
		updateSQL = lsSql.toString();

		whereStr  = "WHERE  TSC_CARD_NO = ? ";
		setString(ii,gate.isoField[2]);  ii++;

		liCnt = updateTable();
		return;
	}
	
	// 更新 IPS CARD 交易紀錄
	public void updateIpsCard(boolean bpFlag) throws Exception {
		gb.showLogMessage("I","updateIpsCard(): started!");

		int liCnt=0, ii=1;

		StringBuffer lsSql = new StringBuffer();

		daoTable  = addTableOwner("IPS_CARD");
		
		if (bpFlag ) {
			return;
		}
		else {
			lsSql.append("DAY_CNT= ?, "); ii++;
			lsSql.append("LAST_ADDVALUE_DATE = ?, "); ii++;
			lsSql.append("DAY_AMT= ? "); ii++;
			if (gb.getSysDate().compareTo(getValue("IpsLastAddValueDate"))==0) {
				setInt(1,getInteger("IpsDayCnt")+1);	
				setString(2,gb.getSysDate()); 	
				setDouble(3,getDouble("IpsDayAmt")+gate.isoFiled4Value);	
			}
			else {
				setInt(1,1);	
				setString(2,gb.getSysDate()); 
				setDouble(3,gate.isoFiled4Value);	
			}			
		}
	
		updateSQL = lsSql.toString();

		whereStr  = "WHERE  IPS_CARD_NO = ? ";
		setString(ii,gate.isoField[2]);  ii++;

		liCnt = updateTable();
		return;
	}

		// 更新 ICH CARD 交易紀錄
		public void updateIchCard(boolean bpFlag) throws Exception {
			gb.showLogMessage("I","updateIchCard(): started!");

			int liCnt=0, ii=1;

			StringBuffer lsSql = new StringBuffer();

			daoTable  = addTableOwner("ICH_CARD");
			
			if (bpFlag ) {
				return;
			}
			else {
				lsSql.append("DAY_CNT= ?, "); ii++;
				lsSql.append("LAST_ADDVALUE_DATE = ?, "); ii++;
				lsSql.append("DAY_AMT= ? "); ii++;
				if (gb.getSysDate().compareTo(getValue("IchLastAddValueDate"))==0) {
					setInt(1,getInteger("IchDayCnt")+1);	
					setString(2,gb.getSysDate()); 	
					setDouble(3,getDouble("IchDayAmt")+gate.isoFiled4Value);	
				}
				else {
					setInt(1,1);	
					setString(2,gb.getSysDate()); 
					setDouble(3,gate.isoFiled4Value);	
				}			
			}
		
			updateSQL = lsSql.toString();

			whereStr  = "WHERE  ICH_CARD_NO = ? ";
			setString(ii,gate.isoField[2]);  ii++;

			liCnt = updateTable();
			return;
		}
		public boolean selectPtrSysParm(String spParm, String spKey ) throws Exception {
			gb.showLogMessage("I","selectPtrSysParm("+spKey+";"+spParm+"): started!");
			boolean blResult = true;
//			String slParm = "SYSPARM";
			daoTable = addTableOwner("PTR_SYS_PARM");	
	
			selectSQL ="WF_VALUE as SysValue1, " //
				 	+ "WF_VALUE2 as SysValue2, " // 
				 	+ "WF_VALUE3 as SysValue3, " // 
				 	+ "WF_VALUE4 as SysValue4, " // 
				 	+ "WF_VALUE5 as SysValue5, " // 
				 	+ "WF_VALUE6 as SysDayCnt, " // 
					+ "WF_VALUE7 as SysDayAmt"; // 
	
			whereStr="where wf_parm = ? and wf_key  = ? ";
			setString(1,spParm);
			setString(2,spKey);
	
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectPtrSysParm -- can not find key." + "--");
				blResult = false;
	
			}
	
	
			return blResult;

		}
		public boolean selectIchParm() throws Exception {
			gb.showLogMessage("I","selectIchParm(): started!");
			boolean blResult = true;
			String slParmType = "ICHM0030";
			daoTable = addTableOwner("ICH_00_PARM");	
	
			selectSQL ="CNT02 as IchParmDayCnt," 
					+ "AMT02 as IchParmDayAmt"; 
	
			whereStr="where PARM_TYPE = ? ";
			setString(1,slParmType);
	
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectIchParm -- can not find key." + "--");
				blResult = false;
	
			}
	
	
			return blResult;

		}
		/**
		 * 讀取 cca_auth_txlog取得臨調額度之專款專用累計金額次數
		 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
		 * @throws Exception if any exception occurred
		 */
		public boolean selectAdjTxlog4Risk(String slStartDate, String slEndDate) throws Exception {
			gb.showLogMessage("I","selectAdjTxlog4Risk(): started!");

			boolean blResult =true;
			int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx);

			daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
			selectSQL = "count(*) as TxCount4AdjRiskChecking, NVL(sum(nt_amt),0) as TotalTxAmt4AdjRiskChecking";

			whereStr  = "WHERE card_acct_idx =? "
			+ "AND CACU_AMOUNT<>? "
			+ "AND RISK_TYPE=? "
			+ "AND TX_DATE>=? " //有效日期-起
			+ "AND TX_DATE<=? "; //有效日期-迄

			setInt(1, nlCardAcctIdx); 
			setString(2,"N");
			setString(3,gate.mccRiskType);
			setString(4,slStartDate);
			setString(5,slEndDate);

			selectTable();

			if (getInteger("TxCount4AdjRiskChecking")==0)	{	 
				blResult = false; //表示沒有在臨調期間在該風險類別消費
			}
			return blResult;
		}
		
		/**
		// 讀取 cca_auth_txlog取得已使用到臨調額度之累計金額次數
		 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
		 * @throws Exception if any exception occurred
		 */
		public double selectAdjTxlog(String slStartDate, String slEndDate, String slCacuFlag, String slRiskType) throws Exception {
			gb.showLogMessage("I","selectAdjTxlog(): started!");

			double dlResult =0;
			int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx);

			daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
//			selectSQL = "count(*) as TxCount4AdjNoneRisk, NVL(sum(TX_AMT_PCT),0) as TotalTxAmt4AdjNoneRisk";
			selectSQL = "count(*) as TxCount4AdjNoneRisk, NVL(sum(NT_AMT),0) as TotalTxAmt4AdjNoneRisk";

			if ("Y".equals(slCacuFlag)) {
				whereStr  = "WHERE card_acct_idx =? "
				+ "AND CACU_AMOUNT<>? "
				+ "AND RISK_TYPE LIKE ? "
				+ "AND TX_DATE>=? " //有效日期-起
				+ "AND TX_DATE<=? " //有效日期-迄
				+ "AND CACU_FLAG='Y' ";//專款專用
			}
			else {
				whereStr  = "WHERE card_acct_idx =? "
				+ "AND CACU_AMOUNT<>? "
				+ "AND RISK_TYPE LIKE ? "
				+ "AND TX_DATE>=? " //有效日期-起
				+ "AND TX_DATE<=? " ;//有效日期-迄
			}

			setInt(1, nlCardAcctIdx); 
			setString(2,"N");
			setString(3,slRiskType);
			setString(4,slStartDate);
			setString(5,slEndDate);

			selectTable();

			dlResult = getDouble("TotalTxAmt4AdjNoneRisk"); //表示在臨調期間非專款專用風險類別消費

			return dlResult;
		}
		//CCA_ADJ_PARM 取得專款專用金額
		public double selectAdjParmSpecAmt(String slStartDate, String slEndDate) throws Exception {
			gb.showLogMessage("I","selectAdjParmSpecAmt(): started!");

			String spCurdate = HpeUtil.getCurDateStr(false);  

			double dlResult = 0;
			daoTable  = addTableOwner("CCA_ADJ_PARM"); 
			selectSQL = "NVL(sum(ADJ_MONTH_AMT),0) as AdjParmSpecTotAmt ";   //所有專款專用限定額度加總




			whereStr  = "WHERE CARD_ACCT_IDX = ? and SPEC_FLAG=? AND  ADJ_EFF_START_DATE >= ? and ADJ_EFF_END_DATE <=?";



			//setInt(1,Integer.parseInt(gate.CardAcctIdx));
			setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(gate.cardAcctIdx)));

			setString(2,"Y"); 
			setString(3,slStartDate);
			setString(4,slEndDate);
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("AdjParmSpecTotAmt");
			}


			return dlResult;
		}
		//讀取臨調各個風險分類專款專用金額參數
		public ResultSet loadAdjParmSpecAmt(String slStartDate, String slEndDate) throws Exception{
			gb.showLogMessage("I","loadAdjParmSpecAmt(): started!");
			selectSQL = "risk_type as CcaAdjParmRiskType, "         //
					+ "adj_month_amt as CcaAdjParmAdjMonthAmt, "	//
					+ "adj_eff_start_date as CcaAdjParmAdjEffStartDate, " //
					+ "adj_eff_end_date as CcaAdjParmAdjEffEndDate, "	  //
					+ "spec_flag as CcaAdjParmSpecFlag ";		    //
			
			gb.showLogMessage("D","LoadAdjParmSpecAmt SQL="+selectSQL);
			daoTable = addTableOwner("CCA_ADJ_PARM");
			whereStr="WHERE CARD_ACCT_IDX = ? and  ADJ_EFF_START_DATE >= ? and ADJ_EFF_END_DATE <= ? order by risk_type Asc ";

			setBigDecimal(1, BigDecimal.valueOf(Integer.parseInt(gate.cardAcctIdx)));
			setString(2,slStartDate);
			setString(3,slEndDate);
			ResultSet L_RS = getTableResultSet();
			
			return L_RS;
		}
		//VCCA_RISK_TYPE 取得高風險註記
		public boolean selectHighRiskType() throws Exception {
			gb.showLogMessage("I","selectHighRiskType(): started!");
			boolean blResult =false;
			daoTable  = addTableOwner("VCCA_RISK_TYPE"); 
			selectSQL = "HIGH_RISK_FLAG as VccaHighRiskFlag ";   //高風險註記
			whereStr  = "WHERE RISK_TYPE = ?";
			setString(1,gate.mccRiskType); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				blResult=false;
			}
			else {
				gb.showLogMessage("I","高風險註記 = " + getValue("VccaHighRiskFlag"));
				if ("Y".equals(getValue("VccaHighRiskFlag"))) {
					blResult=true;
				}
				else {
					blResult=false;
				}
			}
			return blResult;
		}
		/**
		 * 取得交易風險分類，日、月累計次數金額
		 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
		 * @throws Exception if any exception occurred
		 */
		public boolean getRiskTradeInfo()throws Exception {
			gb.showLogMessage("I","getRiskTradeInfo(): started!");
			boolean blResult = false;
			String slCurDate= HpeUtil.getCurDateStr(false);
			int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx);

			gb.showLogMessage("D","bef riskTradeMonthAmt[本月累積交易金額]=>" + gate.riskTradeMonthAmt);
			gb.showLogMessage("D","bef riskTradeMonthCnt[本月累積交易筆數]=>" + gate.riskTradeMonthCnt);
			gb.showLogMessage("D","bef riskTradeDayAmt[本日累積交易金額]=>" + gate.riskTradeDayAmt);
			gb.showLogMessage("D","bef riskTradeDayCnt[本日累積交易筆數]=>" + gate.riskTradeDayCnt);
			gb.showLogMessage("D","slCurDate="+slCurDate);

			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "count(*) as TotalTxCnt4RiskTradeDay, NVL(sum(nt_amt),0) as TotalTxAmt4RiskTradeDay";


			whereStr  = "where CACU_AMOUNT <>? and tx_date =? and card_acct_idx =? and risk_type=? ";

			setString(1, "N");
			setString(2, slCurDate);
			setInt(3, nlCardAcctIdx); 
			setString(4, gate.mccRiskType);
			selectTable();
			
			if ("Y".equals(notFound) ){ 
				gate.riskTradeDayAmt = 0;
				gate.riskTradeDayCnt = 0;
			}
			else {
				gate.riskTradeDayAmt = getInteger("TotalTxAmt4RiskTradeDay");
				gate.riskTradeDayCnt = getInteger("TotalTxCnt4RiskTradeDay");    
			}

			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "count(*) as TotalTxCnt4RiskTradeMonth, NVL(sum(nt_amt),0) as TotalTxAmt4RiskTradeMonth";

			whereStr  = "where CACU_AMOUNT <>? and tx_date like ? and card_acct_idx =? and risk_type=? ";

			setString(1, "N");
			setString(2, slCurDate.substring(0,6)+"%");
			setInt(3, nlCardAcctIdx); 
			setString(4, gate.mccRiskType);
			selectTable();
			if ("Y".equals(notFound) ) {
				gate.riskTradeMonthAmt = 0;
				gate.riskTradeMonthCnt = 0;
			}
			else {
				gate.riskTradeMonthAmt = getInteger("TotalTxAmt4RiskTradeMonth");
				gate.riskTradeMonthCnt = getInteger("TotalTxCnt4RiskTradeMonth");    
			}

			gb.showLogMessage("D","riskTradeMonthAmt[本月累積交易金額]=>" + gate.riskTradeMonthAmt);
			gb.showLogMessage("D","riskTradeMonthCnt[本月累積交易筆數]=>" + gate.riskTradeMonthCnt);
			gb.showLogMessage("D","riskTradeDayAmt[本日累積交易金額]=>" + gate.riskTradeDayAmt);
			gb.showLogMessage("D","riskTradeDayCnt[本日累積交易筆數]=>" + gate.riskTradeDayCnt);

			return blResult;
		}
		
		/**
		 * 授權累積金額的指定日期(第一階段)
		 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
		 * @return 如果通過檢核，return true，否則return false
		 * @throws Exception if any exception occurred
		 */
		public boolean selectTxlogAmtDate() throws Exception {
			gb.showLogMessage("I","selectTxlogAmtDate(): started!");
			boolean blResult = true;
			daoTable = addTableOwner("PTR_SYS_PARM");	
			String slParm = "SYSPARM";
			String spKey = "TXLOGAMT_DATE";

			selectSQL ="WF_DESC as PtrSysParmTxlogAmtDate"; 
	
			whereStr="where wf_parm = ? and wf_key  = ? ";
			setString(1,slParm);
			setString(2,spKey);
	
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectPtrSysParm -- can not find key." +spKey+ "--");
				blResult = false;
			}
			else if (getValue("PtrSysParmTxlogAmtDate").length() != 8 ) {
				gb.showLogMessage("I","function: TA.selectPtrSysParm -- PtrSysParmTxlogAmtDate date length error.--");
				blResult = false;
			}
			else if (!HpeUtil.isNumeric(getValue("PtrSysParmTxlogAmtDate"))) {
				gb.showLogMessage("I","function: TA.selectPtrSysParm -- PtrSysParmTxlogAmtDate not date format.--");
				blResult = false;
			}
		
			return blResult;

		}
		//將拒絕回覆碼select拒絕原因並寫入授權留言中，提供授權人員查詢
		public void getAcerRspCode(String spTxnKind, String spErrorCode) throws Exception {
			gb.showLogMessage("D", "acer spTxnKind= "+spTxnKind+"spErrorCode"+spErrorCode);
			gb.showLogMessage("I","getAcerRspCode(): started!");
			try {
				daoTable = addTableOwner("bil_txn_code");
				selectSQL ="iso_code, "
	                      +"resp_desc";
				whereStr="WHERE txn_kind = ? "
						+"and resp_flag = ? ";

				setString(1, spTxnKind);
				setString(2, gate.bankBit39Code);
				selectTable();
				if ( "Y".equals(notFound) ) {
					gb.showLogMessage("D", "**** select bil_txn_code not found");
					gate.authRemark = "其他原因 - " + gate.bankBit39Code;
				}
				else {
					gate.authRemark = getValue("resp_desc") + " - " + gate.bankBit39Code;
				}

			} catch(Exception ex) {
				gb.showLogMessage("E", String.format("**** getAcerRspCode() error= [%s]", ex.getMessage()));
			}

		}
		//--selectTxDays 查詢 cca_auth_txlog , 近 X 天 累積消費
		public boolean selectTxDays(int npTxDay) throws Exception {
			gb.showLogMessage("I","selectTxDays"+npTxDay);
			boolean blResult = true;
			daoTable = addTableOwner("CCA_AUTH_TXLOG");	

			selectSQL ="COUNT(*) as CcaAuthTxlogDayCount, SUM(NT_AMT) as CcaAuthTxlogDayAmount ";
			whereStr="where card_no = ? and tx_date > to_char((sysdate - ? DAYS),'yyyymmdd') and reversal_flag = 'N' and trans_code <> 'RF' "+gate.smsSubQuery;
			setString(1,gate.cardNo);
			setInt(2,npTxDay);
	
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectTxDays -- can not find any txlog for before days." +npTxDay+ "--");
				blResult = false;
			}
		
			return blResult;
		}
		//--selectTxHours 查詢 cca_auth_txlog , 近 X 小時累積消費
		public boolean selectTxHours(int npTxHour) throws Exception {
			gb.showLogMessage("I","selectTxHours"+npTxHour);
			boolean blResult = true;
			daoTable = addTableOwner("CCA_AUTH_TXLOG");	

			selectSQL ="COUNT(*) as CcaAuthTxlogHourCount, SUM(NT_AMT) as CcaAuthTxlogHourAmount ";
			whereStr="where card_no = ? and tx_datetime >  (sysdate - ? HOURS) and reversal_flag = 'N' and trans_code <> 'RF' "+gate.smsSubQuery;
			setString(1,gate.cardNo);
			setInt(2,npTxHour);
	
			selectTable();
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectTxHours -- can not find any txlog for before days." +npTxHour+ "--");
				blResult = false;
			}
		
			return blResult;
		}
		//--selectSmsCountry 查詢 sms_msg_parm_detl data_code1
		public boolean selectSmsParmDetl(String spPriority, String spDataType, String spDataCode) throws Exception {
//			gb.showLogMessage("I","selectSmsParmDetl:"+" Priority="+spPriority+"; DataType="+spDataType+"; DataCode="+spDataCode);
			boolean blResult = true;
			gate.subQuery = "";
			daoTable = addTableOwner("sms_msg_parm_detl");	

			selectSQL ="COUNT(*) as SmsMsgParmDetlCnt ";
	
			whereStr="where sms_priority = ? and data_type = ? and data_code1 = ? ";
			setString(1,spPriority);
			setString(2,spDataType);
			setString(3,spDataCode);

			selectTable();

			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("I","function: TA.selectSmsParmDetl -- can not find any data type & code =." + spDataType+" & " + spDataCode+ "--");
				return false;
			}
//			gb.showLogMessage("D","selectSmsParmDetl:"+" SmsMsgParmDetlCnt="+getInteger("SmsMsgParmDetlCnt"));

			if (getInteger("SmsMsgParmDetlCnt")==0) {
				blResult = false;
			}
			whereStr3=" where sms_priority = "+ spPriority +" and data_type = '"+ spDataType +"'";

			gate.subQuery = "select data_code1 from "+daoTable+whereStr3;
//			gb.showLogMessage("D","selectSmsParmDetl:"+"gate.subQuery="+gate.subQuery);

			return blResult;
		}
//		//--selectSmsCountry 查詢 sms_msg_parm_detl data_code1
//		public boolean selectSmsParmDetlList(String spPriority, String spDataType) throws Exception {
//			gb.showLogMessage("I","selectSmsParmDetl:"+" Priority="+spPriority+"; DataType="+spDataType);
//			boolean blResult = true;
//			daoTable = addTableOwner("sms_msg_parm_detl");	
//
//			selectSQL ="data_code1 as SmsMsgParmDetlDataCode1 ";
//	
//			whereStr="where sms_priority = ? and data_type = ? order by data_code1 ";
//			setString(1,spPriority);
//			setString(2,spDataType);
//
//			selectTable();
//
//			if ( "Y".equals(notFound) ) {
//				gb.showLogMessage("I","function: TA.selectSmsParmDetl -- can not find any data type & code =." + spDataType+ "--");
//				blResult = false;
//			}
//			gb.showLogMessage("I","selectSmsParmDetl1:"+" SmsMsgParmDetlDataCode1="+getValue("SmsMsgParmDetlDataCode1"));
//
//			return blResult;
//		}
		//取得MERCHANT NAME for Line. selectCcaMchtBill
		public String selectCcaMchtBill() throws Exception {
			gb.showLogMessage("I","selectCcaMchtBill(): started!");

			daoTable  = addTableOwner("CCA_MCHT_BILL");
			selectSQL = "MCHT_NAME as CcaMchtBillMchtName, "
					  + "MCHT_ENG_NAME as CcaMchtBillMchtEngName ";

			whereStr="WHERE MCHT_NO = ? and ACQ_BANK_ID = ? ";

			String slAcqBankId =getAcqBankId(gate.isoField[32]); /* 收單行代碼       */
			setString(1,gate.merchantNo);
			setString(2,slAcqBankId);
			selectTable();

			String slResult = "";
			if ( "Y".equals(notFound) ) {
				gb.showLogMessage("D","selectCcaMchtBill NOT FOUND. MCHT_NO = "+gate.merchantNo+"ACQ_BANK_ID = "+slAcqBankId);
				selectSQL = "MCHT_NAME as CcaMchtBillMchtName, "
						  + "MCHT_ENG_NAME as CcaMchtBillMchtEngName ";
				whereStr="WHERE MCHT_NO = ? ";

				setString(1,gate.merchantNo);
				selectTable();
				if ( "Y".equals(notFound) ) {
					gb.showLogMessage("D","selectCcaMchtBill NOT FOUND. MCHT_NO = "+gate.merchantNo);
					return gate.merchantNo;
				}
			}
			if (getValue("CcaMchtBillMchtName").length() > 0) {
				slResult = getValue("CcaMchtBillMchtName");
			}
			else {
				slResult = getValue("CcaMchtBillMchtEngName");
			}

			return slResult;
		}
		//取得CURRENCY CODE NEGLISH NAME for Line. selectPtrCurrcode
		public boolean selectPtrCurrcode(String spCurrCode) throws Exception {
			gb.showLogMessage("I","selectPtrCurrcode(): started!");
			
			boolean blResult = true;
			daoTable  = addTableOwner("PTR_CURRCODE");
			selectSQL = "CURR_ENG_NAME as PtrCurrEngName";

			whereStr  = "WHERE CURR_CODE = ? ";

			setString(1,spCurrCode); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				return false;
			}
			

			return blResult;
		}
		//紀錄LINE推播訊息傳送給AI的LOG
		//V1.00.17 網銀推播-信用卡消費通知介面處理
	    public boolean insertLineMessage(boolean bpLine, String spLineAIJson, boolean bpResult, String spResult, String spIdNo, String spTimeStamp, String spMessageId) throws Exception {
			gb.showLogMessage("I","insertLineMessage(): started!");
	    	boolean blResult=true;

	    		daoTable = addTableOwner("MKT_LINE_MESSAGE");

	    		String slCrtDate = spTimeStamp.substring(0,10).replace("-", "");
	    		String slCrtTime = spTimeStamp.substring(11,19).replace(":", "");
	    		setValue("CRT_DATE",slCrtDate);
	    		setValue("CRT_TIME",slCrtTime);
				setValue("ID_NO",spIdNo);
				if (bpLine) {
					setValue("LINE_SOURCE","ConsumptionNotification");
					setValue("LINE_SOURCE_NAME","LINE-信用卡消費通知");
				}
				else {
					setValue("LINE_SOURCE","TCB_CARD_BUY");
					setValue("LINE_SOURCE_NAME","網銀APP-信用卡/VISA金融卡消費通知 ");
					setValue("LINE_ID",spMessageId);
				}
	    		setValue("LINE_MESSAGE",spLineAIJson);
	    		setValue("LINE_SENDTYPE","0");
	    		setValue("LINE_SENDTIME",spTimeStamp);
	    		if (bpResult) {
		    		setValue("LINE_GW_FLAG","1");
	    		}
	    		else {
		    		setValue("LINE_GW_FLAG","0");
	    		}
	    		setValue("LINE_GW_MESSAGE",spResult); 
	    		setTimestamp("MOD_TIME",gb.getgTimeStamp());
	    		setValue("MOD_PGM",gb.getSystemName());


	    		insertTable();

	    		return blResult;

	    }
		// 特店風險註記 CCA_MCHT_RISK_DETL
		public int getMchtRiskDetl() throws Exception {


			/*
	BRD第2.3.4規則，新增欄位：額度百分比(與金額區間取孰低)
	與新增管制MCC CODE欄位與【排除卡號(白名單)】
	新增維護欄位：開放迄日(DEFAULT當日)及日累計金額。

	故要調整判斷授權規則
	據王sir表示，以前系統只抓收單行及特店代碼相同就管制，
	此次修改可新增多筆同一特店不同mcc code，
	所以授權邏輯要改多判斷一個條件mcc code.

			 * */

			/*
				/*

	Detail :  3050_card (CCA_MCHT_RISK_DETL)


			 * */
			int nL_Result = 0;

			//Howard: 檢核排除卡號(白名單)，如果有資料，就表示為排除卡號
			daoTable  = addTableOwner("CCA_MCHT_RISK_DETL");
			selectSQL = "DATA_CODE2 as MchtRiskDetlDataCode2, " +
					"DATA_CODE3 as MchtRiskDetlDataCode3, " +
					"DATA_AMT as MchtRiskDetlDataAMT ";

			whereStr  = "WHERE  mcht_no = ? and acq_bank_id = ? and data_type=? and data_code=? and ( mcc_code=? or mcc_code=? ) ";

			setString(1,gate.merchantNo);

			String sL_AcqBankId = getAcqBankId(gate.isoField[32]);
			//String sL_AcqBankId =getAcqBankId();
			setString(2,sL_AcqBankId);
			setString(3,"1"); 
			setString(4,gate.cardNo);
			setString(5,gate.mccCode); 
			setString(6, "*"); 
			selectTable();

			if (! notFound.equals("Y") ) {

				String sL_MchtRiskDetlDataCode2 = getValue("MchtRiskDetlDataCode2").trim();
				String sL_MchtRiskDetlDataCode3 = getValue("MchtRiskDetlDataCode3").trim();

				if ((sL_MchtRiskDetlDataCode2.length()>0) && 
						(sL_MchtRiskDetlDataCode3.length()>0) ){

					if (HpeUtil.isCurDateBetweenTwoDays(sL_MchtRiskDetlDataCode2, sL_MchtRiskDetlDataCode3)) {
						//loadTradeInfoByMchtNo();
						int nL_DayTradeTotalAmtOfMcht = getInteger("AuthTxLogDayAmount") + (int) (gate.ntAmt);

						int nL_MchtRiskDetlDataAMT = getInteger("MchtRiskDetlDataAMT");

						if (nL_MchtRiskDetlDataAMT==0) {
							nL_Result= 4;//表示 單日累計金額參數設定為 0

						}
						else if (nL_MchtRiskDetlDataAMT>0) { 
							if (nL_DayTradeTotalAmtOfMcht<=nL_MchtRiskDetlDataAMT)  {
								nL_Result= 5;//表示 日累積金額　沒有超過　單日累計金額參數
							}
							else {
								nL_Result= 6;//表示 日累積金額　超過　單日累計金額參數
							}
						}


					}
					else
						nL_Result= 3;  //表示有設定日期區間，但是已經逾期失效
				}
				else
					nL_Result= 2;  //表示沒有設定日期區間



			}
			else
				nL_Result =1; //表示找不到卡號資料

			return nL_Result;
		}
		
		//V1.00.18 網銀推播-網銀客戶設定啟用通知
		//取得網銀客戶資料檔 MKT_WEB_CUSTOMER. selectMktWebCust
		public boolean selectMktWebCust(String spIdNo) throws Exception {
			gb.showLogMessage("I","selectMktWebCust(): started!");
			
			boolean blResult = true;
			daoTable  = addTableOwner("MKT_WEB_CUSTOMER");
			selectSQL = "COUNT(*) as MktWebCustomerCount";

			whereStr  = "WHERE WEB_CUST_ID = ? ";

			setString(1,spIdNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				return false;
			}
			
			if (getInteger("MktWebCustomerCount")==0) {
				blResult = false;
			}

			return blResult;
		}
		//V1.00.20 修正票證交易日累計自動加值交易金額及次數處理與沖正問題
		/**
		 * 計算三大票證，日累計自動加值交易次數與金額
		 * V1.00.53 專款專用OTB計算錯誤，問題修正-cacu_amount<>'N'
		 * @throws Exception if any exception occurred
		 */
		public boolean getAutoLoad4DayCntAmt(int nlType1)throws Exception {
			gb.showLogMessage("I","getAutoLoad4DayCntAmt("+nlType1+")! start");

			boolean blResult = true;
			daoTable  = addTableOwner("CCA_AUTH_TXLOG");
			selectSQL = "NVL(sum(nt_amt),0) as AuthLoadDayAmount, count(*) as AuthLoadDayCount";
			
			whereStr  = "where card_no=? and cacu_amount <>? and tx_date =? and (trans_code =? OR trans_code =?) ";

			setString(1, gate.cardNo);
			setString(2, "N");
			setString(3, HpeUtil.getCurDateStr(false));
			if (nlType1==1) { 				//悠遊卡自動加值交易，當日累計次數、金額
				setString(4, "TA");
				setString(5, "TS");
			} 
			else if (nlType1==2) {			//一卡通自動加值交易，當日累計次數、金額
				setString(4, "IN");
				setString(5, "IS");
			}
			else if (nlType1==3) {			//愛金卡自動加值交易，當日累計次數、金額
				setString(4, "HN");
				setString(5, "TS");
			}
			
			selectTable();
			
			if (getInteger("AuthLoadDayCount")==0) {
				blResult = false;
			}
			gb.showLogMessage("I",blResult+"=>AuthLoadDayAmount = "+getInteger("AuthLoadDayAmount")+" ; AuthLoadDayCount="+getInteger("AuthLoadDayCount"));

			return blResult;
		}
		/**
		 * 檢核授權邏輯查核-計算結帳消費 and 結帳預現
		 * V1.00.38 P3授權額度查核調整
		 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
		 * @throws Exception if any exception occurred
		 */
		public void computeSpecialConsumeValue() throws Exception{
			//計算結帳-消費 and 結帳-預現
			String slCardBaseAcnoFlag = getValue("CardBaseAcnoFlag");


			String slPSeqNo = getPSeqNo();

			String slAcnoPSeqNo = getAcnoPSeqNo();
			/*
			String slKey = slPSeqNo;
			if ("".equals(slPSeqNo))
				slKey = slAcnoPSeqNo;
			 */
			String slTableName1= addTableOwner("act_debt");
			String slTableName2= addTableOwner("ptr_actcode");
			String slTableName3= addTableOwner("crd_card");


			PreparedStatement lPs = null;
			ResultSet lRS = null;
			int nlTmpPaidConsumeFee=0, nlTmpPaidPrecash=0;
			String slSqlCmd = "";


			if ("1".equals(slCardBaseAcnoFlag)) {
				//一般卡


				slSqlCmd= "select sum(end_bal) as PaidConsumeFee " //--總欠金額(含預借現金)
						+ ", sum(decode(acct_code,'CA',end_bal,0)) as PaidPrecash " //--預借現金
						+ " from " +slTableName1 
						+ " where 1=1 " 
						+ " and acct_code in (select acct_code from " + slTableName2 +" where interest_method='Y') " 
						+ " and p_seqno = ? ";

				lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


				lPs.setString(1, slPSeqNo);


				lRS = lPs.executeQuery();

				while (lRS.next()) {
					nlTmpPaidConsumeFee = lRS.getInt("PaidConsumeFee"); //結帳-消費
					nlTmpPaidPrecash = lRS.getInt("PaidPrecash"); //結帳-預現
					break;
				}
				lRS.close();
				gate.finalPaidConsumeFee =  nlTmpPaidConsumeFee - nlTmpPaidPrecash;
				gate.finalPaidPrecash =  nlTmpPaidPrecash;

				gb.showLogMessage("D", "一般卡.結帳-消費[finalPaidConsumeFee]=>" + gate.finalPaidConsumeFee);
				gb.showLogMessage("D", "一般卡.結帳-預借[finalPaidPrecash]=>" + gate.finalPaidPrecash);
			}
			else if ("3".equals(slCardBaseAcnoFlag)) {
				//商務卡個繳
				slSqlCmd= "select sum(end_bal) as PaidConsumeFee " 
						+ ", sum(decode(acct_code,'CA',end_bal,0)) as PaidPrecash " 
						+ " from " +slTableName1 
						+ " where 1=1 " 
						+ " and acct_code in (select acct_code from " + slTableName2 +" where interest_method='Y') " 
						+ " and p_seqno = ? ";
				lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


				lPs.setString(1, slPSeqNo);


				lRS = lPs.executeQuery();

				while (lRS.next()) {
					nlTmpPaidConsumeFee = lRS.getInt("PaidConsumeFee"); //結帳-消費
					nlTmpPaidPrecash = lRS.getInt("PaidPrecash"); //結帳-預現
					break;
				}
				lRS.close();
				gate.finalPaidConsumeFee =  nlTmpPaidConsumeFee - nlTmpPaidPrecash;
				gate.finalPaidPrecash =  nlTmpPaidPrecash;

				gb.showLogMessage("D", "商務卡個繳.結帳-消費[finalPaidConsumeFee]=>" + gate.finalPaidConsumeFee);
				gb.showLogMessage("D", "商務卡個繳.結帳-預借[finalPaidPrecash]=>" + gate.finalPaidPrecash);


			}
			else if ("Y".equals(slCardBaseAcnoFlag)) {
				//商務卡總繳-個人
				slSqlCmd= "select sum(end_bal) as PaidConsumeFee " 
						+ ", sum(decode(acct_code,'CA',end_bal,0)) as PaidPrecash " 
						+ " from " + slTableName1 
						+ " where 1=1 " 
						+ " and acct_code in (select acct_code from " + slTableName2 + " where interest_method='Y') " 
						+ " and acno_p_seqno = ? ";

				lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


				lPs.setString(1, slAcnoPSeqNo);


				lRS = lPs.executeQuery();

				while (lRS.next()) {
					nlTmpPaidConsumeFee = lRS.getInt("PaidConsumeFee"); //結帳-消費
					nlTmpPaidPrecash = lRS.getInt("PaidPrecash"); //結帳-預現
					break;
				}
				lRS.close();
				gate.finalPaidConsumeFee =  nlTmpPaidConsumeFee - nlTmpPaidPrecash;
				gate.finalPaidPrecash =  nlTmpPaidPrecash;

				gb.showLogMessage("D", "商務卡總繳-個人.結帳-消費[finalPaidConsumeFee]=>" + gate.finalPaidConsumeFee);
				gb.showLogMessage("D", "商務卡總繳-個人.結帳-預借[finalPaidPrecash]=>" + gate.finalPaidPrecash);

			}
			if (gate.businessCard) {
				//商務卡-公司戶
				slSqlCmd= " select sum(end_bal) as PaidConsumeFeeOfComp " 
						+ ", sum(decode(acct_code,'CA',end_bal,0)) as PaidPrecashOfComp " 
						+ " from " +slTableName1 
						+ " where 1=1 " 
						+ " and acct_code in (select acct_code from " + slTableName2 +" where interest_method='Y') " 
						+ " and card_no in (select card_no from " +slTableName3 +" where corp_p_seqno= ? and acct_type= ? ) ";

				String slCardBaseCorpPSeqNo = getValue("CardBaseCorpPSeqNo");
				String slCardBaseAcctType = getValue("CardBaseAcctType");

				lPs = getDatabaseConnect().prepareStatement(slSqlCmd);


				lPs.setString(1, slCardBaseCorpPSeqNo);
				lPs.setString(2, slCardBaseAcctType);


				lRS = lPs.executeQuery();

				while (lRS.next()) {
					nlTmpPaidConsumeFee = lRS.getInt("PaidConsumeFeeOfComp"); //結帳-消費
					nlTmpPaidPrecash = lRS.getInt("PaidPrecashOfComp"); //結帳-預現
					break;
				}
				lRS.close();
				gate.finalPaidConsumeFeeOfComp =  nlTmpPaidConsumeFee - nlTmpPaidPrecash;
				gate.finalPaidPrecashOfComp =  nlTmpPaidPrecash;

				gb.showLogMessage("D", "商務卡-公司戶.結帳-消費[finalPaidConsumeFeeOfComp]=>" + gate.finalPaidConsumeFeeOfComp);
				gb.showLogMessage("D", "商務卡-公司戶.結帳-預借[finalPaidPrecashOfComp]=>" + gate.finalPaidPrecashOfComp);

			}

		}
		/**
		 * 檢核授權邏輯查核-取得 act_acct_sum 指定科目的金額
		 * isCorp true=>corp_p_seqno-false=>acno_p_seqno
		 * spAcctCode 指定科目代碼
		 * V1.00.38 P3授權額度查核調整
		 * @return 指定科目的金額
		 * @throws Exception if any exception occurred
		 */
		public double selectActAcctSum(boolean isCorp, String spAcctCode) throws Exception{
			gb.showLogMessage("I","selectActAcctSum(): started!");

			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("act_acct_sum"); 
			selectSQL = "nvl(sum(nvl(UNBILL_END_BAL,0) "
					  + "+ nvl(BILLED_END_BAL,0)),0) as ActAcctSumAmt ";   //所有未付科目的加總
			
			if (isCorp) {
				whereStr = "where P_SEQNO in (SELECT P_SEQNO FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE CORP_P_SEQNO = ? ) AND ACCT_CODE = ? ";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = "where P_SEQNO in (SELECT P_SEQNO FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE ACNO_P_SEQNO = ? ) AND ACCT_CODE = ? ";
				slPSeqNo = getAcnoPSeqNo();
			}
			
			setString(1,slPSeqNo); 
			setString(2, spAcctCode);
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("ActAcctSumAmt");
			}
			
			return dlResult;
			
		}
		/**
		 * 檢核授權邏輯查核-取得 rsk_problem 列問交的爭議款金額--cancel
		 * isCorp true=>corp_p_seqno-false=>acno_p_seqno
		 * V1.00.38 P3授權額度查核調整
		 * @return 爭議款的金額
		 * @throws Exception if any exception occurred
		 */
		public double selectRskProblem(boolean isCorp) throws Exception{
			gb.showLogMessage("I","selectActAcctSum(): started!");

			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("rsk_problem"); 
			selectSQL = "sum(nvl(prb_amount,0)) as t1_prb_amount ";   //爭議款的加總
					  
			
			if (isCorp) {
				whereStr = "where add_apr_date <> '' and close_apr_date = '' "
						 + "and card_no in (SELECT card_no FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE CORP_P_SEQNO = ? ) "
						 + "and reference_no not in (select reference_no from act_debt where 1=1)";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = "where add_apr_date <> '' and close_apr_date = '' "
						 + "and card_no in (SELECT card_no FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE ACNO_P_SEQNO = ? ) "
						 + "and reference_no not in (select reference_no from act_debt where 1=1)";
				slPSeqNo = getAcnoPSeqNo();
			}
			
			setString(1,slPSeqNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("t1_prb_amount");
			}
			
			return dlResult;
			
		}
		/**
		 * 檢核授權邏輯查核-取得 act_acct_sum 專款專用的金額
		 * isCorp true=>corp_p_seqno-false=>acno_p_seqno
		 * V1.00.38 P3授權額度查核調整
		 * @return 專款專用的金額
		 * @throws Exception if any exception occurred
		 */
		public double selectActAcctSumSpecAmt(boolean isCorp) throws Exception{
			gb.showLogMessage("I","selectActAcctSumSpecAmt(): started!");

			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("act_acct_sum"); 
			selectSQL = "nvl(sum(nvl(END_BAL_SPEC,0)),0) as ActAcctSumSpecAmt ";    //所有專款專用的餘額加總
			
			if (isCorp) {
				whereStr = "where P_SEQNO in (SELECT P_SEQNO FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE CORP_P_SEQNO = ? ) ";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = "where P_SEQNO in (SELECT P_SEQNO FROM "
						 + addTableOwner("CRD_CARD") 
						 + " WHERE ACNO_P_SEQNO = ? ) ";
				slPSeqNo = getAcnoPSeqNo();
			}
			
			setString(1,slPSeqNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("ActAcctSumSpecAmt");
			}
			
			return dlResult;
			
		}
		/**
		 * 檢核授權邏輯查核-取得欠款總額(含專款專用)
		 * V1.00.38 P3授權額度查核調整
		 * @return 欠款總額(含專款專用)
		 * @throws Exception if any exception occurred
		 */
		public double selectAcctJrnlBal(boolean isCorp) throws Exception{
			gb.showLogMessage("I","selectAcctJrnlBal(): started!");

			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("act_acct"); 
			selectSQL = "sum(nvl(ACCT_JRNL_BAL,0)) as ActAcctJrnlBal, "
					+ "sum(nvl(END_BAL_OP + END_BAL_LK ,0)) as AcctPrePayAmt";
			
			if (isCorp) {
				whereStr = "WHERE CORP_P_SEQNO = ? ";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = "WHERE P_SEQNO = ? ";
				slPSeqNo = getAcnoPSeqNo();
			}

			setString(1,slPSeqNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("ActAcctJrnlBal");
			}
			
			return dlResult;
			
		}

		/**
		 * 檢核授權邏輯查核-取得分期未到期金額(不含專款專用)
		 * V1.00.38 P3授權額度查核調整
		 * @return 分期未到期金額(不含專款專用)
		 * @throws Exception if any exception occurred
		 */
		public double selectBilContractInstUpost(boolean isCorp, boolean isSpec) throws Exception{
			gb.showLogMessage("I","selectBilContractInstUpost(): started!");
			
			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("bil_contract"); 
			selectSQL = "sum((install_tot_term - install_curr_term) * unit_price + remd_amt "
					+ "	+ decode(install_curr_term,0,first_remd_amt+extra_fees,0)) as bilContractInstUnpost ,"
					+ "sum(unit_price) as bilContractUnitPrice ";
			
			if (isSpec) {
				whereStr = "where "
						+ "install_tot_term <> install_curr_term  and ((post_cycle_dd >0 or "
						+ "installment_kind ='F') or (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' "
						+ "AND auth_code='DEBT')) and spec_flag = 'Y' ";
			}
			else {
				whereStr = "where "
						+ "install_tot_term <> install_curr_term  and ((post_cycle_dd >0 or "
						+ "installment_kind ='F') or (post_cycle_dd=0 AND DELV_CONFIRM_FLAG='Y' "
						+ "AND auth_code='DEBT')) and spec_flag <> 'Y' ";
			}
			
			if (isCorp) {
				whereStr = whereStr + " and CARD_NO in (select card_no from "
						 + addTableOwner("CRD_CARD") +" crd_card where corp_p_seqno = ? )";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = whereStr + " and ACNO_P_SEQNO = ? ";
				slPSeqNo = getAcnoPSeqNo();
			}

			setString(1,slPSeqNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("bilContractInstUnpost");
			}
			return dlResult;

		}
		
		/**
		 * 檢核授權邏輯查核-取得溢繳款金額
		 * V1.00.38 P3授權額度查核調整
		 * @return 溢繳款金額
		 * @throws Exception if any exception occurred
		 */
		public double selectAcctPrePayAmt(boolean isCorp) throws Exception{
			gb.showLogMessage("I","selectAcctPrePayAmt(): started!");

			double dlResult = 0;
			String slPSeqNo;
			daoTable  = addTableOwner("act_acct"); 
			selectSQL = "sum(nvl(END_BAL_OP + END_BAL_LK ,0)) as AcctPrePayAmt";
			
			if (isCorp) {
				whereStr = "WHERE CORP_P_SEQNO = ? ";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				whereStr = "WHERE P_SEQNO = ? ";
				slPSeqNo = getAcnoPSeqNo();
			}

			setString(1,slPSeqNo); 
			selectTable();

			if ( "Y".equals(notFound) ) {
				dlResult = 0;
			}
			else {
				dlResult = getDouble("AcctPrePayAmt");
			}
			
			return dlResult;
			
		}
		
		/**
		 * 檢核授權邏輯查核-取得已付款未銷帳金額
		 * V1.00.38 P3授權額度查核調整
		 * @return 已付款未銷帳金額
		 * @throws Exception if any exception occurred
		 */
		public double computeUnPaidConsumeFee(boolean isCorp) throws Exception{
			gb.showLogMessage("I","computeUnPaidConsumeFee(): started!");
			
			double dlResult = 0;
			//計算取得已付款未銷帳金額(act_pay_detail(繳款明細檔)、act_debt_cancel(銷帳明細檔)、act_pay_ibm(IBM線上繳款檔) 
			String slPSeqNo;
			String slTableName1= addTableOwner("act_pay_detail");  //繳款明細檔
			String slTableName2= addTableOwner("act_debt_cancel"); //銷帳明細檔
			String slTableName3= addTableOwner("act_pay_ibm");     //IBM線上繳款檔
			String slTableName4= addTableOwner("act_pay_batch");   //繳款明細檔
			String slTableName5= addTableOwner("crd_card");        //卡檔取acno_p_seqno

			PreparedStatement lPs = null;
			ResultSet lRS = null;
			double dlTmpPaidAmt1=0, dlTmpPaidAmt2=0, dlTmpPaidAmt3=0, dlTmpPaidAmt4=0;
			String slSqlCmd = "";
			//已付款未銷帳金額 pay_amt1
			slSqlCmd = "select sum(nvl(A.pay_amt,0)) as pay_amt1 " + "from " + slTableName1 + " A join " + slTableName4
					+ " B on A.batch_no=B.batch_no " 
					+ " where B.batch_tot_cnt >0 ";

			if (isCorp) {
				slSqlCmd = slSqlCmd + "and A.P_SEQNO in (select acno_p_seqno from " + slTableName5 + " where corp_p_seqno = ? )";
				slPSeqNo = getCorpPSeqNo();
			}
			else {
				slSqlCmd = slSqlCmd + "and A.P_SEQNO = ? ";
				slPSeqNo = getAcnoPSeqNo();
			}
			gb.showLogMessage("I","computeUnPaidConsumeFee(): sql1 ="+slSqlCmd);

			lPs = getDatabaseConnect().prepareStatement(slSqlCmd);

			lPs.setString(1, slPSeqNo);

			lRS = lPs.executeQuery();

			while (lRS.next()) {
				dlTmpPaidAmt1 = lRS.getDouble("pay_amt1"); //
				break;
			}
			lRS.close();

			gb.showLogMessage("D", "已付款未銷帳金額[dlTmpPaidAmt1]=>" +dlTmpPaidAmt1);
			
			//已付款未銷帳金額 pay_amt2
			slSqlCmd = "select sum(nvl(pay_amt,0)) as pay_amt2 " + "from " + slTableName2 
					+ " where process_flag <>'Y' ";

			if (isCorp) {
				slSqlCmd = slSqlCmd + "and P_SEQNO in (select acno_p_seqno from " + slTableName5 + " where corp_p_seqno = ? )";
//				slPSeqNo = getCorpPSeqNo();
			}
			else {
				slSqlCmd = slSqlCmd + "and P_SEQNO = ? ";
//				slPSeqNo = getAcnoPSeqNo();
			}
			gb.showLogMessage("I","computeUnPaidConsumeFee(): sql2 ="+slSqlCmd);

			lPs = getDatabaseConnect().prepareStatement(slSqlCmd);

			lPs.setString(1, slPSeqNo);

			lRS = lPs.executeQuery();

			while (lRS.next()) {
				dlTmpPaidAmt2 = lRS.getDouble("pay_amt2"); //
				break;
			}
			lRS.close();

			gb.showLogMessage("D", "已付款未銷帳金額[dlTmpPaidAmt2]=>" +dlTmpPaidAmt2);
			
			//已付款未銷帳金額 pay_amt3
			slSqlCmd = "select sum(nvl(txn_amt,0)) as pay_amt3 " + "from " + slTableName3 
					+ " where nvl(proc_mark,'') <>'Y' and nvl(error_code,'') in "
					+ " ('','0','N') and txn_source not in ('0101', '0102', '0103', '0502')";

			if (isCorp) {
				slSqlCmd = slSqlCmd + "and P_SEQNO in (select acno_p_seqno from " + slTableName5 + " where corp_p_seqno = ? )";
//				slPSeqNo = getCorpPSeqNo();
			}
			else {
				slSqlCmd = slSqlCmd + "and P_SEQNO = ? ";
//				slPSeqNo = getAcnoPSeqNo();
			}
			gb.showLogMessage("I","computeUnPaidConsumeFee(): sql3 ="+slSqlCmd);

			lPs = getDatabaseConnect().prepareStatement(slSqlCmd);

			lPs.setString(1, slPSeqNo);

			lRS = lPs.executeQuery();

			while (lRS.next()) {
				dlTmpPaidAmt3 = lRS.getDouble("pay_amt3"); //
				break;
			}
			lRS.close();
			gb.showLogMessage("D", "已付款未銷帳金額[dlTmpPaidAmt3]=>" +dlTmpPaidAmt3);

			if (isCorp) {
				dlTmpPaidAmt4 = getDouble("CardAcctPayAmtOfComp");
			}
			else {                   
				dlTmpPaidAmt4 = getDouble("CardAcctPayAmt");
			}
			gb.showLogMessage("D", "已付款未銷帳金額[dlTmpPaidAmt4]=>" +dlTmpPaidAmt4);

			dlResult = dlTmpPaidAmt1 + dlTmpPaidAmt2 + dlTmpPaidAmt3 + dlTmpPaidAmt4;

			return dlResult;
		}
		/**
		 * 檢核授權邏輯查核-計算已授權未請款金額
		 * V1.00.38 P3授權額度查核調整
		 * 計算已授權未請款金額 =>個人/公司*預借現金
		 * @throws Exception if any exception occurred
		 */
		public double getAuthedNotMatch(int nlType)throws Exception {
			gb.showLogMessage("I","getAuthedNotMatch("+nlType+")! start");

			/*
	  		 getAuthedNotMatch(1) => //計算 TOT_AMT_PRECASH (個人)
	  		 getAuthedNotMatch(2) => //計算 TOT_AMT_PRECASH (公司)
	  		 getAuthedNotMatch(3) => //計算 TOT_AMT_CONSUME (個人)
	  		 getAuthedNotMatch(4) => //計算 TOT_AMT_CONSUME (公司)
			 * */
			double dlResult = 0;
			int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx); 

			if (nlType==1) { //計算已授權未請款預借現金 TOT_AMT_PRECASH (個人)
				//select sum(decode(cacu_amount, 'Y',nt_amt,0)) tot_Amt from cca_auth_txlog where mtch_flag not in ('Y', 'U') and card_acct_idx = ? and card_acct_idx > 0 and cacu_flag <> 'Y'
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "sum(decode(cacu_amount, 'N',0,nt_amt)) as AuthedNotMatch1";

				whereStr  = "where mtch_flag not in ('Y', 'U') and CACU_CASH =? and cacu_flag <> ? and card_acct_idx = ? ";

				setString(1, "Y");
				setString(2, "Y");
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotMatch1");
				}

			}
			else if (nlType==2) {//計算已授權未請款預借現金 TOT_AMT_PRECASH (公司)
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "sum(decode(cacu_amount, 'N',0,nt_amt)) as AuthedNotMatch2";

				whereStr  = "where mtch_flag not in ('Y', 'U') and CACU_CASH =? and card_acct_idx in ";
				whereStr  += "(select card_acct_idx from " + addTableOwner("cca_card_acct") + " where corp_p_seqno=? and acno_flag in ('3','Y'))";

				setString(1, "Y");
				setString(2, getValue("CardBaseCorpPSeqNo")); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotMatch2");
				}

			}
		else if (nlType==3) {//計算 TOT_AMT_CONSUME (個人)
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "sum(decode(cacu_amount, 'N',0,nt_amt)) as AuthedNotMatch3";

				whereStr  = "where mtch_flag not in ('Y', 'U') and CACU_AMOUNT <> ? and cacu_flag <> ? and card_acct_idx = ? ";

				setString(1, "N");
				setString(2, "Y");
				setInt(3, nlCardAcctIdx); 
				selectTable();
				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotMatch3");
				}

			}
			else if (nlType==4) { //計算 TOT_AMT_CONSUME (公司)
				daoTable  = addTableOwner("CCA_AUTH_TXLOG");
				selectSQL = "sum(decode(cacu_amount, 'N',0,nt_amt)) as AuthedNotMatch4";

				whereStr  = "where mtch_flag not in ('Y', 'U') and CACU_AMOUNT <> ? and card_acct_idx in ";
				whereStr  += "(select card_acct_idx from " + addTableOwner("cca_card_acct") + " where corp_p_seqno=? and acno_flag in ('3','Y'))";

				setString(1, "N");
				setString(2, getValue("CardBaseCorpPSeqNo"));
				selectTable();

				if ( !"Y".equals(notFound) ) {
					dlResult = getDouble("AuthedNotMatch4");
				}

			}
			return dlResult;
		}
		/**
		 * 檢核授權邏輯查核-取得臨調期間專款專用風險類別消費授權已請款金額
		 * V1.00.38 P3授權額度查核調整
		 * @return 臨調期間專款專用風險類別消費授權已請款金額
		 * @throws Exception if any exception occurred
		 */		
		public double selectTxlogSpecNonMatch(String slStartDate, String slEndDate, String slRiskType) throws Exception {
			gb.showLogMessage("I","selectAdjTxlog(): started!");

			double dlResult =0;
			int nlCardAcctIdx = Integer.parseInt(gate.cardAcctIdx);

			daoTable  = addTableOwner("CCA_AUTH_TXLOG"); 
			selectSQL = "NVL(sum(NT_AMT),0) as TotalTxSpecNonMatchAmt";

			whereStr  = "WHERE card_acct_idx =? "
					  + "AND CACU_AMOUNT<>? "
				      + "AND RISK_TYPE LIKE ? "
				      + "AND TX_DATE>=? " //有效日期-起
				      + "AND TX_DATE<=? " //有效日期-迄
				      + "AND MTCH_FLAG in ('Y', 'U') "//比對註記
			          + "AND CACU_FLAG='Y' "; //專款專用

			setInt(1, nlCardAcctIdx); 
			setString(2,"N");
			setString(3,slRiskType);
			setString(4,slStartDate);
			setString(5,slEndDate);

			selectTable();

			dlResult = getDouble("TotalTxSpecNonMatchAmt"); //表示在臨調期間專款專用風險類別消費授權已請款金額

			return dlResult;
		}

		/**
		 * 檢查行動支付手機黑名單
		 * V1.00.40 p3檢查行動支付手機黑名單
		 * @return boolean false 符合手機黑名單
		 * @throws Exception if any exception occurred
		 */
		public boolean selectCcaMobileBlackList()throws Exception {
			gb.showLogMessage("I","selectCcaMobileBlackList()! start");
			boolean blResult = true;

			daoTable  = addTableOwner("cca_mobile_black_list"); 
			selectSQL = "COUNT(*) as MobileBlackCount ";

			whereStr  = "WHERE cellar_phone=? ";

			setString(1, getValue("CrdIdNoCellPhone"));
	

			selectTable();
			
			if (getInteger("MobileBlackCount")>0)
				blResult = false;

			return blResult;
		}

		/**
		 * 檢查行動支付手機黑名單
		 * V1.00.40 p3檢查行動支付手機黑名單
		 * @return boolean false 符合手機號碼異動時間在設定範圍內
		 * @throws Exception if any exception occurred
		 */
		public boolean checkMobileChgTime(int npHours)throws Exception {
			gb.showLogMessage("I","checkMobileChgTime()! start");
			boolean blResult = true;
			
			daoTable  = addTableOwner("cms_chgcolumn_log"); 
			selectSQL = "count(*) as MobileChgTimeCount ";

			whereStr  = "WHERE MOD_TIME > (sysdate - ? HOURS ) AND "
					  + "CHG_TABLE = 'crd_idno' AND CHG_COLUMN = 'cellar_phone' "
					  + "AND ID_P_SEQNO = ? ";

			setInt(1, npHours);
			setString(2, getValue("CardBaseIdPSeqNo"));
	

			selectTable();
			
			if (getInteger("MobileChgTimeCount")>0)
				blResult = false;

			return blResult;		
			}

}
