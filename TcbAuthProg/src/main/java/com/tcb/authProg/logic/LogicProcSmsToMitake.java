/**
 * 授權邏輯查核-三竹SMS簡訊處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-三竹SMS簡訊處理                    *
 * 2021/12/23  V1.00.01	 Kevin       新增簡訊內容在簡訊log中                       *
 * 2022/01/13  V1.00.02  Kevin       TCB新簡訊發送規則                            *
 * 2022/02/16  V1.00.03  Kevin       TCB新簡訊發送規則-單筆金額判斷為>=設定的金額       *
 * 2022/04/01  V1.00.04  Kevin       設定雙幣卡簡訊，美金(9840)，日幣(9392)          *
 * 2022/06/03  V1.00.05  Kevin       網銀推播-信用卡消費通知介面處理                  *
 * 2022/06/14  V1.00.06  Kevin       特殊消費簡訊1，需符合ccam8030消費簡訊是否發送條件     * 
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.sms.SmsProcess;
import com.tcb.authProg.util.HpeUtil;
import java.sql.ResultSet;


public class LogicProcSmsToMitake extends AuthLogic {

	public LogicProcSmsToMitake(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcSmsToMitake : started");

	}
	/**
	 * 舊簡訊邏輯處理，讀取消費簡訊門檻參數
	 * V1.00.48  P3程式碼整理(附卡註記/附卡消費通知正卡註記)
	 * @throws Exception
	 */
	public void processSmsInfo() throws Exception{

		if ("00".equals(gGate.isoField[39])) {
			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 才要發簡訊
//			ta.getParm3TranCode("TRANCODE", gGate.transCode);
			if ("Y".equals(ta.getValue("Parm3TranCode4").trim())) {
				//down, 處理  授權消費簡訊通知參數
				gGate.ifIgnoreSmsOfTrading = checkSmsParm(); 

				gb.showLogMessage("D","@@@@消費簡訊代碼 = "+ta.getValue("CcaAuthSmsMsgId1"));

				if (!gGate.ifIgnoreSmsOfTrading) {
					sendSms(1, ta.getValue("CcaAuthSmsMsgId1"), ta.getValue("CrdIdNoCellPhone").trim()); //發出消費簡訊
					if (gGate.isSupCard) {
						if (gGate.isAdvicePrimChFlag) {

							gb.showLogMessage("D","@@@@附卡消費通知正卡簡訊代碼 = "+ta.getValue("CcaAuthSmsMsgId1"));

							sendSms(4, ta.getValue("CcaAuthSmsMsgId1"),ta.getValue("CrdIdNoPrimCellPhone").trim()); //發出消費簡訊(附卡消費通知正卡)
						}
					}
				}
				//up, 處理  授權消費簡訊通知參數	
				//V1.00.06 特殊消費簡訊1，需符合ccam8030消費簡訊是否發送條件
				//down, 處理 授權特殊消費簡訊1參數
				else {
					checkSmsParm4SpecialTrading();
					if (gGate.ifSendSms4Cond1) {
						sendSms(2, ta.getValue("CcaAuthSms2MsgId1"), ta.getValue("CrdIdNoCellPhone").trim()); //發出特殊簡訊一
					}
				}
				//up, 處理 授權特殊消費簡訊1參數
			}

		}
		//down, 處理 授權特殊消費簡訊2參數
		if (gGate.ifSendSms4Cond2) {
			sendSms(3, ta.getValue("CcaAuthSms2MsgId2"), ta.getValue("CrdIdNoCellPhone").trim()); //發出特殊簡訊二
		}
		//up, 處理 授權特殊消費簡訊2參數
	}
	
	/**
	 * 新簡訊邏輯處理，讀取消費簡訊門檻參數
	 * V1.00.02 新簡訊發送規則
	 * V1.00.48  P3程式碼整理(附卡註記/附卡消費通知製卡註記)
	 * @throws Exception
	 */
	public void processSmsInfoNew() throws Exception {
		gb.showLogMessage("D","讀取所有消費簡訊門檻參數");

		ResultSet smsRS = ta.loadSmsMsgParm();
//		gb.showLogMessage("D","check SMS smsRS="+smsRS);
		while (smsRS.next()) {
			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmPriority="+smsRS.getString("SmsMsgParmPriority"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmMsgId="+smsRS.getString("SmsMsgParmMsgId"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmSpecList="+smsRS.getString("SmsMsgParmSpecList"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmAreaType="+smsRS.getString("SmsMsgParmAreaType"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondCountry="+smsRS.getString("SmsMsgParmCondCountry"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondCurr="+smsRS.getString("SmsMsgParmCondCurr"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondBin="+smsRS.getString("SmsMsgParmCondBin"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondMcht="+smsRS.getString("SmsMsgParmCondMcht"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondMcc="+smsRS.getString("SmsMsgParmCondMcc"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondPos="+smsRS.getString("SmsMsgParmCondPos"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondTransType="+smsRS.getString("SmsMsgParmCondTransType"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondRespCode="+smsRS.getString("SmsMsgParmCondRespCode"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondAmt="+smsRS.getString("SmsMsgParmCondAmt"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmTxAmt="+smsRS.getString("SmsMsgParmTxAmt"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCpmdCmt1="+smsRS.getString("SmsMsgParmCondCnt1"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmTxDay="+smsRS.getString("SmsMsgParmTxDay"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmTxDatCnt="+smsRS.getString("SmsMsgParmTxDatCnt"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCpmdCmt2="+smsRS.getString("SmsMsgParmCondCnt2"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmTxHour="+smsRS.getString("SmsMsgParmTxHour"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmTxHourCnt="+smsRS.getString("SmsMsgParmTxHourCnt"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondGroup="+smsRS.getString("SmsMsgParmCondGroup"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondSuccess="+smsRS.getString("SmsMsgParmCondSuccess"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmConeOrAnd1="+smsRS.getString("SmsMsgParmConeOrAnd1"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCond1Amt="+smsRS.getString("SmsMsgParmCond1Amt"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCondOrAnd2="+smsRS.getString("SmsMsgParmCondOrAnd2"));
//			gb.showLogMessage("D","check SMS MSG PARM SmsMsgParmCond2Amt="+smsRS.getString("SmsMsgParmCond2Amt"));

			gGate.ifIgnoreSmsOfTrading = checkSmsParmNew(smsRS); 
			
			if (!gGate.ifIgnoreSmsOfTrading) {
				gGate.smsPriority = smsRS.getString("SmsMsgParmPriority");
				sendSms(1, smsRS.getString("SmsMsgParmMsgId"), ta.getValue("CrdIdNoCellPhone").trim()); //發出消費簡訊
				if (gGate.isSupCard) {
					if (gGate.isAdvicePrimChFlag) {

						gb.showLogMessage("D","@@@@附卡消費通知正卡簡訊代碼 = "+ta.getValue("CcaAuthSmsMsgId1"));

						sendSms(4, smsRS.getString("SmsMsgParmMsgId"),ta.getValue("CrdIdNoPrimCellPhone").trim()); //發出消費簡訊(附卡消費通知正卡)
					}
				}
				return;
			}
		}
	}
	
	private void sendSms(int npTransType, String spMsgId, String spCellPhoneNo) throws Exception{
		//kevin: 發送即時簡訊指定電話號碼
		//nP_TransType=> 1: 消費簡訊, 2:特殊簡訊一, 3:特殊簡訊二, 4:附卡消費通知正卡
		String slMsgType = "";
		switch(npTransType) {
			case 1  : slMsgType = "AUTO" ;  break;//消費簡訊
			case 2  : slMsgType = "SPEC1";  break;//特殊簡訊一
			case 3  : slMsgType = "SPEC2";  break;//特殊簡訊二
			case 4  : slMsgType = "AUTO2";  break;//附卡消費通知正卡
			default : break;
		}
		//v1.00.01 新增簡訊內容在簡訊log中
		String slSmsContent = getSmsContent(npTransType, spMsgId);
		ta.insertMsgEven(spMsgId, slMsgType, spCellPhoneNo, slSmsContent);
		if (spCellPhoneNo.length() == 0) {
			return;
		}
//		String slSmsContent = getSmsContent(npTransType, spMsgId);
		
		Thread thread = new Thread(new SmsProcess(gb, spCellPhoneNo, slSmsContent, gGate.txDate, gGate.cardNo, gGate.isoField[38]));
		thread.start();

	}
	//V1.00.05 網銀推播-信用卡消費通知介面處理
//	private String getSmsContent(int npTransType, String spMsgId) throws Exception{
//		String slSmsContent = "" ;
//		String slReplaceSms = "" ;
//		//down, 依據是否為 debit card 給予不同的簡訊內容
//		String slTransType1Field1 = "";
//		if (gGate.isDebitCard) {
//			slTransType1Field1 ="VISA金融卡";
//		}
//		else {
//			slTransType1Field1 ="信用卡";
//		}
//
//
//		//up, 依據是否為 debit card 給予不同的簡訊內容
//
//
//		//down, 取出卡號末四碼
//		String slLast4CardNo = "";
//		if (gGate.cardNo.length()>=4)
//			slLast4CardNo = gGate.cardNo.substring(gGate.cardNo.length()-4, gGate.cardNo.length());
//		//up, 取出卡號末四碼		
//
//
//		//down, 取出月日與時間
//		String slCurDate = HpeUtil.getCurDateStr(false);
//		String slMonth = slCurDate.substring(4, 6);
//		String slDate = slCurDate.substring(6, 8);
//		String slTime = HpeUtil.getCurTimeStr().substring(0, 2) + ":" + HpeUtil.getCurTimeStr().substring(2, 4);
//		//up, 取出月日與時間
//
//
//		//down, 判斷國內外交易
//		boolean blIsForeignTrands = false;
//		String slTransType1Field2="", slTransType1Field3="";
//		double dlBillAmount = gGate.ntAmt;
//		if ("F".equals(gGate.areaType))
//			blIsForeignTrands = true;
//
//		if (blIsForeignTrands) {
//			slTransType1Field2 = "國外交易";
//			slTransType1Field3 = "，若為外幣交易以帳單金額為準。祝順心!";
//			if ("392".equals(gGate.dualCurr4Bill) || "840".equals(gGate.dualCurr4Bill)) {
//				dlBillAmount = gGate.dualAmt4Bill;
//				//V1.00.04 設定雙幣卡簡訊，美金(9840)，日幣(9392)
//				if ("392".equals(gGate.dualCurr4Bill)) {
//					spMsgId = "9392";
//				}
//				if ("840".equals(gGate.dualCurr4Bill)) {
//					spMsgId = "9840";
//				}
//				gb.showLogMessage("D","dual currency amount for billing. CURRENCY = "+gGate.dualCurr4Bill+" ; AMOUNT = "+ dlBillAmount+" ; spMsgId = "+spMsgId);
//			}
//		}
//		else {
//			slTransType1Field2 = "國內交易";
//			slTransType1Field3 = "。祝順心!";
//		}
//
//
//		//up, 判斷國內外交易
//
//
//		if (npTransType==1) { //消費簡訊
//			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 要回傳下列訊息內容
//			/*
//    		1.	感謝使用合庫VISA金融卡末四碼8639於12月04日 15:14國外交易台幣1630元，若為外幣交易以帳單金額為準。祝順心!
//			2.	感謝使用合庫VISA金融卡末四碼2333於12月04日 15:13國內交易台幣5880元。祝順心!
//			3.	感謝使用合庫信用卡末四碼3538於12月04日 15:14國內交易台幣7510元。祝順心!
//			4.	感謝使用合庫信用卡末四碼1042於12月04日 15:14國外交易台幣17123元，若為外幣交易以帳單金額為準。祝順心!
//
//			 * */
//
//
////			sL_SmsContent = "感謝使用合庫 " +sL_TransType1Field1 + " 末四碼" + sL_Last4CardNo + "於" + sL_Month + "月" + sL_Date + "日 " + sL_Time +   sL_TransType1Field2 + "台幣 " +G_Gate.nt_amt + "元" + sL_TransType1Field3;
//			//kevin:取得消費簡訊內容，並放入簡訊變數。
//			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
//			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
//			slSmsContent = ta.getSmsContentnt(spMsgId);
//
//			gb.showLogMessage("D","GET SMS_MSG_ID=" + spMsgId);
//			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);
//
//			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
//			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
//			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
//			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
//			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
//			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
//			slSmsContent = slReplaceSms6;
//
//			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);
//
//		}
//		else if (npTransType==2) {//特殊簡訊一
//			//kevin:取得特殊簡訊一內容，並放入簡訊變數。
//			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
//			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
//			slSmsContent = ta.getSmsContentnt(spMsgId);
//
//			gb.showLogMessage("D","GET SMS2_MSG_ID1=" + spMsgId);
//			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);
//
//			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
//			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
//			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
//			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
//			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
//			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
//			slSmsContent = slReplaceSms6;
//
//			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);
//
//		}
//		else if (npTransType==3) {//特殊簡訊二
//			//kevin:取得特殊簡訊二內容，並放入簡訊變數。
//			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
//			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
//			slSmsContent = ta.getSmsContentnt(spMsgId);
//
//			gb.showLogMessage("D","GET SMS2_MSG_ID2=" + spMsgId);
//			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);
//
//			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
//			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
//			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
//			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
//			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
//			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
//			slSmsContent = slReplaceSms6;
//
//			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);
//
//		}
//		else {
//			//kevin:取得附卡消費通知正卡簡訊一內容，並放入簡訊變數。
//			String slTerm = ta.getValue("CcaAuthSmsDetlAmt1Code2");
//			String slRate = ta.getValue("CcaAuthSmsDetlAmt1Code3");
//			slSmsContent = ta.getSmsContentnt(spMsgId);
//
//			gb.showLogMessage("D","GET SMS_MSG_ID=" + spMsgId);
//			gb.showLogMessage("D","Before SMS_CONTENT=" + slSmsContent);
//
//			String slReplaceSms1 =  slSmsContent.replaceAll("<#0>", slLast4CardNo); //卡號末四碼
//			String slReplaceSms2 = slReplaceSms1.replaceAll("<#1>", slMonth + "月" + slDate + "日 " ); //消費日期
//			String slReplaceSms3 = slReplaceSms2.replaceAll("<#2>", slTime); //消費時間
//			String slReplaceSms4 = slReplaceSms3.replaceAll("<#3>", HpeUtil.decimalRemove(dlBillAmount)); //消費金額
//			String slReplaceSms5 = slReplaceSms4.replaceAll("<#4>", slTerm); //分期期數
//			String slReplaceSms6 = slReplaceSms5.replaceAll("<#5>", slRate); //分期利率
//			slSmsContent = slReplaceSms6;
//
//			gb.showLogMessage("D","After SMS_CONTENT=" + slSmsContent);
//		}
//
//		return slSmsContent;
//
//	}
	
	
	// 處理簡訊資料
	// 檢核消費通知簡訊檢
	private boolean checkSmsParm() throws Exception {
		//table is CCA_AUTH_SMS and CCA_AUTH_SMSDETL

		boolean blIgnoreTradingSms = true;

		gb.showLogMessage("D","loadSmsParm : started");

		String slCardNote=ta.getValue("CardBaseCardNote");
		String slEntryModeType = gGate.entryModeType;
		String sl3dTranxFlag = "";
		String slMccRiskType = gGate.mccRiskType;

		if (gGate.is3DTranx) {
			sl3dTranxFlag = "2";
		}
		else if (gGate.ecTrans) {
			sl3dTranxFlag = "3";
		}
		else {
			sl3dTranxFlag = "1";
		}
		
		if (gGate.isDebitCard)
			slCardNote = "V";

		// 信用卡處理四次，VD卡處理兩次 gate.isDebitCard

		gb.showLogMessage("D","NOTE="+slCardNote+";EM="+slEntryModeType+";3D="+sl3dTranxFlag+";mccRisk="+slMccRiskType);

		ta.getCcaAuthSms(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType); //第一次
		if ( "Y".equals(ta.notFound) ){  
			slMccRiskType = "*"; // 用通用的 風險類別 再 select 一次			
			ta.getCcaAuthSms(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType); //第二次
			if ( "Y".equals(ta.notFound) ){
				if (gGate.isDebitCard) {
					blIgnoreTradingSms = true;
				}
				else {
					slCardNote = "*"; // 用通用的 風險類別 再 select 一次
					slMccRiskType = gGate.mccRiskType;
					ta.getCcaAuthSms(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType); //第三次
					if ( "Y".equals(ta.notFound) ){
						slMccRiskType = "*"; // 用通用的 風險類別 再 select 一次			
						ta.getCcaAuthSms(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType); //第四次
						if ( "Y".equals(ta.notFound) ){
							blIgnoreTradingSms = true;
						}
						else {
							int nlCcaAuthSmsTxAmt = ta.getInteger("CcaAuthSmsTxAmt");
							if (gGate.ntAmt>nlCcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
								blIgnoreTradingSms = ta.checkSmsDetl(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType);
						}
					}
					else {
						int nlCcaAuthSmsTxAmt = ta.getInteger("CcaAuthSmsTxAmt");
						if (gGate.ntAmt>nlCcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
							blIgnoreTradingSms = ta.checkSmsDetl(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType);
					}
				}
			}
			else {
				int nlCcaAuthSmsTxAmt = ta.getInteger("CcaAuthSmsTxAmt");
				if (gGate.ntAmt>nlCcaAuthSmsTxAmt) //消費金額 > 消費通知參數.單筆金額門檻
					blIgnoreTradingSms = ta.checkSmsDetl(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType);
			}
		}
		else {
			if (gGate.ntAmt > ta.getInteger("CcaAuthSmsTxAmt"))//消費金額 > 消費通知參數.單筆金額門檻
				blIgnoreTradingSms = ta.checkSmsDetl(slCardNote, slEntryModeType, sl3dTranxFlag, slMccRiskType);
		}


		return blIgnoreTradingSms;

	}
	/**
	 * 檢核 授權特殊消費簡訊參數
	 * V1.00.42 授權系統與DB連線交易異常時的處理改善方式-交易累計次數已包含本次交易
	 * @throws Exception
	 */
	private void checkSmsParm4SpecialTrading() throws Exception {
		//table is CCA_AUTH_SMS2_PARM and CCA_AUTH_SMS2_DETL

		String slCardNote=ta.getValue("CardBaseCardNote");

		String slTranCode = "";
		int nlDayTransCount = 0;//日累積交易次數
		
		if (gGate.ecTrans) {
			slTranCode = "OA";    //OA:網路交易 gate.ecTrans
		}
		else {
			slTranCode = gGate.transCode;
		}

		boolean blFindTableData = ta.getSmsParmData(slCardNote, slTranCode);

		if (!blFindTableData) {
			slCardNote="*";
			blFindTableData = ta.getSmsParmData(slCardNote, slTranCode);
		}

		if (blFindTableData) {			
			if ("3".equals(ta.getValue("CcaAuthSms2Cond1Area"))) {
				nlDayTransCount	= ta.selectCcaAuthTxLog4SmsInd("3"); //交易累計次數已包含本次交易

				gb.showLogMessage("D","處理國內外" + slTranCode + ta.getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);

				ta.checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
			}
			else if ("2".equals(ta.getValue("CcaAuthSms2Cond1Area"))) {
				 if ("T".equals(gGate.areaType)) {
					nlDayTransCount	= ta.selectCcaAuthTxLog4SmsInd("2"); //交易累計次數已包含本次交易

					gb.showLogMessage("D","處理國內" + slTranCode + ta.getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);

					ta.checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
				 }
			}
			else if ("1".equals(ta.getValue("CcaAuthSms2Cond1Area"))) {
				 if ("F".equals(gGate.areaType)) {
					nlDayTransCount	= ta.selectCcaAuthTxLog4SmsInd("1"); //交易累計次數已包含本次交易

					gb.showLogMessage("D","處理國外" + slTranCode + ta.getValue("Parm3TranCode1")+",交易筆數 = " + nlDayTransCount);

					ta.checkSms2Detl(slCardNote, slTranCode, nlDayTransCount);
				 }
			}
		}
		return ;
	}
	/**
	 * 新簡訊發送規則參數
	 * V1.00.02 新簡訊發送規則
	 * V1.00.42 授權系統與DB連線交易異常時的處理改善方式-交易累計次數金額已包含本次交易
	 * @throws Exception
	 */
	private boolean checkSmsParmNew(ResultSet smsRS) throws Exception {

		boolean blIgnoreTradingSms = true;
		gGate.smsSubQuery = "";
		int nlCurrentCnt = 0;
		double dlCurrentAmt = 0;
//		gb.showLogMessage("D","loadSmsParmNew : started");
		
		//--[1] 指定名單
		if( ("Y").equals(smsRS.getString("SmsMsgParmSpecList"))) {
			gb.showLogMessage("D","CardMsgFlag :"+ta.getValue("CardMsgFlag")+"CrdIdnoMsgFlag :"+ta.getValue("CrdIdnoMsgFlag"));
			blIgnoreTradingSms = true;
			if (!"00".equals(gGate.isoField[39])) {
				return true;
			}
			if (ta.getDouble("CardMsgPurchaseAmt") > 0 ) {
				if (gGate.ntAmt >= ta.getDouble("CardMsgPurchaseAmt")) {
					blIgnoreTradingSms = false;
				}
			}
			if (ta.getDouble("CrdIdnoMsgPurchaseAmt") > 0) {
				if (gGate.ntAmt >= ta.getDouble("CrdIdnoMsgPurchaseAmt")) {
					blIgnoreTradingSms = false;
				}
			}
			return blIgnoreTradingSms;
		}
		else {
			//--[11] 是否檢核回覆碼 0.不檢核 1.指定 2.排除 (調整順序，先檢查交易成功或失敗)
			if( ("1").equals(smsRS.getString("SmsMsgParmCondRespCode"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "RESP", gGate.sgIsoRespCode)) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and ISO_RESP_CODE in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondRespCode"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "RESP", gGate.sgIsoRespCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and ISO_RESP_CODE not in ("+gGate.subQuery+") ";
			}
			
			//--[2] 適用地區 0.不檢核 1.國外 2.國內 
			if( ("1").equals(smsRS.getString("SmsMsgParmAreaType"))) {
				if (!"F".equals(gGate.areaType)) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and CCAS_AREA_FLAG = 'F'";
			}   
			else if (("2").equals(smsRS.getString("SmsMsgParmAreaType"))) {
				if (!"T".equals(gGate.areaType)) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and CCAS_AREA_FLAG = 'T' ";
			}
	
			//--[3] 是否檢核國別 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondCountry"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "COUNTRY", ta.getValue("CountryCode"))) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCHT_COUNTRY in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondCountry"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "COUNTRY", ta.getValue("CountryCode"))) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCHT_COUNTRY not in ("+gGate.subQuery+") ";
			}       
	
			//--[4] 是否檢核幣別 0.不檢核 1.指定 2.排除
	
			if( ("1").equals(smsRS.getString("SmsMsgParmCondCurr"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "CURR", gGate.isoField[49])) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and TX_CURRENCY in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondCurr"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "CURR", gGate.isoField[49])) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and TX_CURRENCY not in ("+gGate.subQuery+") ";
			}
	
			//--[5] 是否檢核BIN  0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondBin"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "BIN", gGate.cardNo.substring(0,6))) {
					return true;
				}		
				blIgnoreTradingSms = false;				
				gGate.smsSubQuery = gGate.smsSubQuery+"and LEFT(CARD_NO , 6) in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondBin"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "BIN", gGate.cardNo.substring(0,6))) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and LEFT(CARD_NO , 6) not in ("+gGate.subQuery+") ";
			}

			//--[6] 是否檢核團代 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondGroup"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "GROUP", gGate.groupCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and GROUP_CODE in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondGroup"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "GROUP", gGate.groupCode)) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and GROUP_CODE not in ("+gGate.subQuery+") ";
			}
			
			//--[7] 是否檢核特店 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondMcht"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "MCHT", gGate.merchantNo)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCHT_NO in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondMcht"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "MCHT", gGate.merchantNo)) {
					return true;
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCHT_NO not in ("+gGate.subQuery+") ";
			}
	
			//--[8] 是否檢核MCC CODE 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondMcc"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "MCC", gGate.mccCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCC_CODE in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondMcc"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "MCC", gGate.mccCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and MCC_CODE not in ("+gGate.subQuery+") ";
			}
	
			//--[9] 是否檢核POS ENTRY MODE 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondPos"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "POS", gGate.entryMode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and LEFT(POS_MODE , 2) in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondPos"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "POS", gGate.entryMode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and LEFT(POS_MODE , 2) not in ("+gGate.subQuery+") ";
			}
	
			//--[10] 是否檢核交易類別 0.不檢核 1.指定 2.排除
			if( ("1").equals(smsRS.getString("SmsMsgParmCondTransType"))) {
				if (!ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "TRANS_TYPE", gGate.transCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and TRANS_CODE in ("+gGate.subQuery+") ";
			}   
			else if( ("2").equals(smsRS.getString("SmsMsgParmCondTransType"))) {
				if (ta.selectSmsParmDetl(smsRS.getString("SmsMsgParmPriority"), "TRANS_TYPE", gGate.transCode)) {
					return true;
				}		
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and TRANS_CODE not in ("+gGate.subQuery+") ";
			}
	
			//--[12] 是否檢核金額     N.不檢核 Y.檢核
			if( ("Y").equals(smsRS.getString("SmsMsgParmCondAmt"))) {
				if (gGate.ntAmt < smsRS.getDouble("SmsMsgParmTxAmt")) {
					return true;			
				}
				blIgnoreTradingSms = false;
				gGate.smsSubQuery = gGate.smsSubQuery+"and NT_AMT >= "+smsRS.getDouble("SmsMsgParmTxAmt")+" "; //v1.00.03  Kevin TCB新簡訊發送規則-單筆金額判斷為>=設定的金額
			}
	
			//--[13] 是否檢核佔額 or MCC     N.不檢核 Y.檢核佔額  M.檢核MCC A.檢核佔額+MCC
			if( !("N").equals(smsRS.getString("SmsMsgParmCondSuccess"))) {
				blIgnoreTradingSms = false;
				if( ("Y").equals(smsRS.getString("SmsMsgParmCondSuccess")) || ("A").equals(smsRS.getString("SmsMsgParmCondSuccess"))) {
					gGate.smsSubQuery = gGate.smsSubQuery+"and CACU_AMOUNT = 'Y' ";
					if (!"00".equals(gGate.sgIsoRespCode)) {
						nlCurrentCnt = 0;
						dlCurrentAmt = 0;
					}
				}
				if( ("M").equals(smsRS.getString("SmsMsgParmCondSuccess")) || ("A").equals(smsRS.getString("SmsMsgParmCondSuccess"))) {
					gGate.smsSubQuery = gGate.smsSubQuery+"and MCC_CODE = '"+gGate.mccCode+"' ";
				}
			}

			//**--select sms_consume_msg 資料再進行累積筆數判斷
	
			//--[14] 是否檢核近日累積交易筆數或金額 N.不檢核 Y.檢核
			if( ("Y").equals(smsRS.getString("SmsMsgParmCondCnt1")) || ("Y").equals(smsRS.getString("SmsMsgParmConeOrAnd1"))) {
				gb.showLogMessage("D","[14] 是否檢核近日累積交易="+ smsRS.getInt("SmsMsgParmTxDay"));
			    ta.selectTxDays(smsRS.getInt("SmsMsgParmTxDay"));
			    if( ("Y").equals(smsRS.getString("SmsMsgParmCondCnt1"))) {
					gb.showLogMessage("D","[14] 檢核近日累積交易次數<設定次數="+ ta.getInteger("CcaAuthTxlogDayCount")+" : "+smsRS.getInt("SmsMsgParmTxDatCnt"));
				    if(ta.getInteger("CcaAuthTxlogDayCount")+nlCurrentCnt < smsRS.getInt("SmsMsgParmTxDatCnt")) {
						return true;			
				    }
			    }
			    if( ("Y").equals(smsRS.getString("SmsMsgParmConeOrAnd1"))) {
					gb.showLogMessage("D","[14] 檢核近日累積交易金額<設定金額="+ ta.getDouble("CcaAuthTxlogDayAmount")+" : "+smsRS.getDouble("SmsMsgParmCond1Amt"));
				    if(ta.getDouble("CcaAuthTxlogDayAmount")+dlCurrentAmt < smsRS.getDouble("SmsMsgParmCond1Amt")) {
						return true;			
				    }
			    }
				blIgnoreTradingSms = false;
			}
	
			//--[15] 是否檢核幾小時內累積交易筆數 N.不檢核 Y.檢核
			if( ("Y").equals(smsRS.getString("SmsMsgParmCondCnt2")) || ("Y").equals(smsRS.getString("SmsMsgParmCondOrAnd2"))) {
				gb.showLogMessage("D","[15] 是否檢核幾小時內累積交易="+ smsRS.getInt("SmsMsgParmTxHour"));
			    ta.selectTxHours(smsRS.getInt("SmsMsgParmTxHour"));
				if( ("Y").equals(smsRS.getString("SmsMsgParmCondCnt2"))) {
					gb.showLogMessage("D","[15] 檢核幾小時內累積交易次數<設定次數="+ ta.getInteger("CcaAuthTxlogHourCount")+" : "+smsRS.getInt("SmsMsgParmTxHourCnt"));
				    if(ta.getInteger("CcaAuthTxlogHourCount")+nlCurrentCnt < smsRS.getInt("SmsMsgParmTxHourCnt")) {
						return true;			
				    }
				}
				if( ("Y").equals(smsRS.getString("SmsMsgParmCondOrAnd2"))) {
					gb.showLogMessage("D","[15] 檢核幾小時內累積交易金額<設定金額="+ ta.getDouble("CcaAuthTxlogHourAmount")+" : "+smsRS.getDouble("SmsMsgParmCond2Amt"));
				    if(ta.getDouble("CcaAuthTxlogHourAmount")+dlCurrentAmt < smsRS.getDouble("SmsMsgParmCond2Amt")) {
						return true;			
				    }
				}
				blIgnoreTradingSms = false;
			}
		}
		
		gb.showLogMessage("D","New SMS Logic Ignore trading Sms="+ blIgnoreTradingSms);

		return blIgnoreTradingSms;

	}
}
