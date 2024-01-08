/**
 * 授權 Auht Process Thread 共用物件
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
 * 2021/02/08  V1.00.00  Kevin       授權 Auht Process Thread 共用物件           *
 * 2021/08/12  V1.00.01  Kevin       新增lock/unlock功能確保同卡號同時交易時，依序處理。 *   
 * 2022/04/01  V1.00.02	 Kevin       拒絕回應碼調整                               * 
 * 2022/09/16  V1.00.03  Kevin       啟用lock/unlock功能確保同卡號同時交易時依序處理。  *
 * 2022/11/14  V1.00.26  Kevin       因票證卡號非實體卡號，暫時關閉lock/unlock功能。     *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/10/23  V1.00.56  Kevin       避免因特店資料異常時，導致授權系統異常的處理排除        *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import java.io.BufferedOutputStream;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.iso8583.AtmFormat;
import com.tcb.authProg.iso8583.BicFormat;
import com.tcb.authProg.iso8583.FhmFormat;
import com.tcb.authProg.iso8583.FiscFormat;
import com.tcb.authProg.iso8583.FormatInterChange;
import com.tcb.authProg.iso8583.NegFormat;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.DbUtil;
import com.tcb.authProg.util.HpeUtil;


/* 交易處理多工程序 */
public class AuthTxnThread extends Thread {

	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm  gb   = null;
	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate    gate  =  null;
	/**
	 * 單次交易流程處理物件
	 */
	ProcAuthTxn    proc  =  null;
	/**
	 * 資料庫存取物件
	 */
	TableAccess     ta   = null;

	BufferedOutputStream authOut = null;
	BufferedOutputStream fiscFhmNegOut = null;

	FormatInterChange intr = null;

	int    chanNum =0;
	String slReceivedIsoStr = "";

	public AuthTxnThread(AuthGlobalParm gb,String connType,int chanNum,int inputLen,byte[] authData,BufferedOutputStream authOut,BufferedOutputStream fiscFhmNegOut) {
	

		this.gb        =  gb;
		gate  =  new AuthTxnGate();
		proc  =  new ProcAuthTxn(gb,gate);
		gate.connType  =  connType;
		gate.chanNum   =  chanNum;
		gate.dataLen   =  inputLen;
		this.authOut   =  authOut;
		this.fiscFhmNegOut   =  fiscFhmNegOut;
		this.chanNum   =  chanNum;
		for ( int k = 0; k < inputLen; k++) {
			gate.isoData[k] = authData[k]; 
		}
		slReceivedIsoStr    = new String(gate.isoData,0,gate.dataLen);
		
		try {				
//			gb.showLogMessage("I", "AAA-TxnThread().received ISO String=>"+ slReceivedIsoStr + "-------");					
			gate.gDbConn = gb.getgDataSource().getConnection();	
		} catch (Exception e) {
			// TODO: handle exception
			gb.showLogMessage("E","can not get db connectionn in tx thread!!Exception:" + e.getMessage());
			gate.gDbConn = null;
		}
		
	}

	public void run() {    
		try {

//			gb.showLogMessage("I","TxnThread : started");

			/* 交易-開始時間 */
			gb.dateTime();
//			gb.showLogMessage("I","TxnThread start date : "+gb.getSysDate());
			gate.startTime = ""+System.currentTimeMillis();

			gb.setExceptionFlag("");
			gb.setSystemError(false);


			/* 判斷資料來源, 建立格式轉換物件 */
			if ( "FISC".equals(gate.connType) ) {
					intr  =  new  FiscFormat(gb.getLogger(),gate); 
			}
			else if ( "FISCNEG".equals(gate.connType) ) {
					intr  =  new  NegFormat(gb.getLogger(),gate); 
			}
			else if ( "ATM".equals(gate.connType) ) {
					intr  =  new  AtmFormat(gb.getLogger(),gate); 
				}
			else if ( "FHM".equals(gate.connType) ) {
					intr  =  new  FhmFormat(gb.getLogger(),gate); 
			}
			else if ( "NEG".equals(gate.connType) ) {
					intr  =  new  NegFormat(gb.getLogger(),gate); 
			}
			else if ( "TEST".equals(gate.connType) ) {
				    intr  =  new  FiscFormat(gb.getLogger(),gate); 
				    gate.connType = "FISC";
			}
			else {
					intr  =  new  BicFormat(gb.getLogger(),gate);  
			}

			int  i = 999;
			/* 轉換 ISO 格式 為 主機格式 */
			if ( !intr.iso2Host() ) {

				gate.isoField[39] = "30";
				gb.showLogMessage("E","ISO-8583 CONVERT ERROR!");
				DbUtil.closeConn(gate.gDbConn); //kevin:非正常結束時db需要close
				return;
			}
			
			gate.transType = gate.mesgType;
			gb.showLogMessage("D","*******Received mesgType==" + gate.mesgType);

			if (!"08".equals(gate.mesgType.substring(0,2))) {
				String[] split = HpeUtil.byte2HexSplit(gate.isoData);
				String printHex = "";
				for (int p=0; p<gate.dataLen; p++) {
					if ((p+1)%16 == 0 || p+1==gate.dataLen) {
						printHex = printHex +":"+ split[p];
//						gb.showLogMessage("D","AAA-receive iso hexString =>"+printHex);
						printHex = "";
						continue;
					}
					printHex = printHex +":"+ split[p];
				}
				//down, print ISO Fields
				for (int p=0; p<128; p++) {
					if (null != gate.isoField[p]) {
						if (!"".equals(gate.isoField[p]))
//							System.out.println("AAA-received ISO Field " + p + " ==>"+ gate.isoField[p].toString() );
							gb.showLogMessage("D","AAA-received ISO Field " + p + " ==>" + gate.isoField[p].toString());
					}
				}
				//up, print ISO Fields
			}
//			gate.transType = gate.mesgType;
//			gb.showLogMessage("D","*******Received mesgType==" + gate.mesgType);

			if ( "WEB".equals(gate.connType) ) {
				String slSpecialFlag= gate.isoField[27].trim();
				if ("A".equals(slSpecialFlag)) {
					//sL_SpecialFlag==A => 強迫授權成功(不檢核交易邏輯)；要寫統計檔
					gate.forceAuthPassed = true;
					gate.forceAuthRejected = false;
					gate.writeToStatisticTable = true;
				}
				else if ("R".equals(slSpecialFlag)) {
					//sL_SpecialFlag==R => 強迫授權失敗(不檢核交易邏輯)；要寫統計檔
					gate.forceAuthPassed = false;
					gate.forceAuthRejected = true;
					gate.writeToStatisticTable = true;

				}
				else if ("C".equals(slSpecialFlag)) {
					//sL_SpecialFlag==C => 強迫授權失敗(不檢核交易邏輯)；不要寫統計檔
					gate.forceAuthPassed = false;
					gate.forceAuthRejected = true;
					gate.writeToStatisticTable = false;
					
				}
				else {
					gate.forceAuthPassed = false;
					gate.forceAuthRejected = false;
					gate.writeToStatisticTable = true;
					
				}

			}
			i = Integer.parseInt(gate.mesgType.substring(1,2));

			//防止FHM交易轉送FISC時，避免39欄位帶入。
			if ( gate.isoField[39].length() == 0 && !"FHM".equals(gate.connType) ) {
				gate.isoField[39] = "00"; 
			}

			String slIsoField26 = gate.isoField[26].trim(); 
			if (!slIsoField26.isEmpty()) { 
				if ("S".equals(slIsoField26)) {//0907: 暫時用的，將模擬程式的connType設定為NCCC
					gate.connType="NCCC";
//					gb.showLogMessage("D","connType="+slIsoField26);
				}
				else {
					gate.connType="BATCH";  //V1.00.42 授權系統與DB連線交易異常時的處理改善方式。批次授權調整
				}
			}

			if (null == gate.gDbConn) {
			//if (!gb.dbConnected) {
				gb.showLogMessage("E","database is not connected.");
				gate.isoField[38] = "I5";
				gate.isoField[39] = "96";
				gate.isoField[92] = "I5";
				gb.setSystemError(true);
			}
			else {
				switch (i) {
				case 1  : proc.authMainControl();   break;      // 01NN-預先授權訊息
				case 2  : 
					//訊息代碼檢查
					if ("0210".equals(gate.mesgType)) {
						gb.showLogMessage("D","訊息代碼錯誤="+gate.mesgType);
						gate.isoField[39] = "58"; break;      // NNNN-訊息類別錯誤
					}
					else {
						proc.authMainControl();   
						break;      // 02NN-授權交易訊息
					}
					// 03NN-檔案維護(FHM)訊息
				case 3  : 
					if ( "030".equals(gate.mesgType.substring(0, 3))) {
						if ("Y".equals(gb.getIfReturnTrueDirectly())) { 
							if ("NEG".equals(gate.connType)) {     //0300國內掛卡
//								String slTmp = "NEG No verification,return true directly!!";
//								gb.showLogMessage("D",slTmp);
//								gb.showLogMessage("I", "authMainControl--"+ slTmp);
								gate.isoField[13] = "00";
							}
							else {                                 //0302國際掛卡
//								String slTmp = "FHM No verification,return true directly!!";
//								gb.showLogMessage("D",slTmp);
//								gb.showLogMessage("I", "authMainControl--"+ slTmp);
								gate.isoField[38] = "123456";
								gate.isoField[39] = "00";
							}
						}
						else {
							if ("NEG".equals(gate.connType)) { //0300國內掛卡
//								intr  =  new  NegFormat(gb.getLogger(),gate,gb.cvtHash); 
								intr  =  new  NegFormat(gb.getLogger(),gate); 
								ProcNegToFisc proc2Neg = new ProcNegToFisc(gb, gate);
								proc2Neg.sendReqToFiscNeg(fiscFhmNegOut,intr); // 交易轉送 FISC 控制
							}
							else {                             //0302國際掛卡
//								intr  =  new  FiscFormat(gb.getLogger(),gate,gb.cvtHash);
								intr  =  new  FiscFormat(gb.getLogger(),gate);
								ProcFhmToFisc proc2Fhm = new ProcFhmToFisc(gb, gate);
								proc2Fhm.sendReqToFiscFhm(fiscFhmNegOut,intr); // 交易轉送 FISC 控制
							}
						}
					} 
					else if ( "031".equals(gate.mesgType.substring(0, 3)) ) {
						if ("FISCNEG".equals(gate.connType)) { //0300國內掛卡
							ProcNegToFisc proc2Neg = new ProcNegToFisc(gb, gate);
							proc2Neg.fiscNegResponse(); 
						}
						else {
							ProcFhmToFisc proc2Fhm = new ProcFhmToFisc(gb, gate);
							proc2Fhm.fiscFhmResponse();           //0302國際掛卡
						}
							DbUtil.closeConn(gate.gDbConn); //kevin:針對回覆結果，因系統不須特別處理，所以db需要close
							return;// FISC 回覆訊息 控制 
					}                    
					break;
				case 4  : proc.authMainControl();   break;      // 04NN-沖銷交易訊息
				case 5  : proc.reconControl();      break;      // 05NN-收單對帳訊息 reconciliation 訊息
				case 6  : proc.authMainControl();   break;      // 06NN-代碼化交易訊息
				case 8  :
					ProcNetworkManage proc2Network = new ProcNetworkManage(gb, gate);
					if ( "0800".equals(gate.mesgType)) {
						proc2Network.networkManagement();        break;       // 08NN-網路管理通知訊息
					}
					else {
//						gb.showLogMessage("D","Receive 0810 command=>" +gate.mesgType+ gate.isoString + "===");
						proc2Network.networkResponse(); // 0810-網路回覆通知訊息
						DbUtil.closeConn(gate.gDbConn); //kevin:針對回覆結果，因系統不須特別處理，所以db需要close
						return;
					}

				case 9  : proc.webUserFunction();   break;      // 09NN-WEB USER 功能通知訊息
					default : gate.isoField[39] = "58"; break;      // NNNN-訊息類別錯誤
				}

			}
			//gb.showLogMessage("D", "connType:", gate.connType);

		}

		catch ( Exception ex ) {
			//kevin:新增lock/unlock功能確保同卡號同時交易時，依序處理。確保exception時要unlock
			//V1.00.03 啟用lock/unlock功能確保同卡號同時交易時依序處理。
			//V1.00.26 因票證卡號非實體卡號，暫時關閉lock/unlock功能。
			//V1.00.56 避免因特店資料異常時，導致授權系統異常的處理排除
//			if (gate.cardNo.length()>0) {
//				gb.unlock(gate.cardNo);
//			}
			
			gb.expHandle(ex, true);
			try {  
				if (gate.isAuthMainControl) {
					if (null==ta) {
						ta   = new TableAccess(gb,gate);
					}
					if (("0100".equals(gate.mesgType) || "0200".equals(gate.mesgType)) && gate.isoField[38].isEmpty() && "00".equals(gate.isoField[39])) {
						ta.getAndSetErrorCode("I2");
						gate.sgIsoRespCode = gate.isoField[39];
						gate.cacuAmount = "N";
						gate.cacuCash = "N";
					}
					if (!gate.isInsertTxlog) {
						if (ex.toString().length()>60) 
							gate.authRemark = ex.toString().substring(0,60);
						else {
							gate.authRemark = ex.toString();
						}
						ta.insertAuthTxLog();
					}
				}
				DbUtil.rollbackConn(gate.gDbConn);

				gb.transStatistic(0,0);   
			}
			catch ( Exception ex2 ){ 
				gate.isoField[39] = "96";
				gate.isoField[38] = ""; //V1.00.42 授權系統與DB連線交易異常時的處理改善方式。
				if ("NEG".equals(gate.connType)) { //0300國內掛卡
					gate.isoField[13] = "05";
				}
			}
		}

		try {
//			gb.showLogMessage("D","P38-A" + gate.isoField[38] + "===");
			// 授權系統異常

			if ( "Y".equals(gb.getExceptionFlag()) ||  gb.isSystemError() ) { //Howard: 這兩個值不用清掉嗎?
				gb.showLogMessage("D","gb.exceptionFlag=>" + gb.getExceptionFlag() + "===");
				//V1.00.02 拒絕回應碼調整
//				if (!"exp".equals(gate.isoField[38])) {
//					gate.isoField[38] = ""; 
//					gate.isoField[39] = "96";
//				}

				if ("NEG".equals(gate.connType)) { //0300國內掛卡
					gate.isoField[13] = "05";
				}
				DbUtil.rollbackConn(gate.gDbConn);
				gb.showLogMessage("E","AUTH SYSTEM ERROR!");  
			}
			else {
				//kevin:test down
//				if ("990412580500425".equals(gate.merchantNo)) {
//					Thread.sleep(sleepSec5*1000);
//				}
				//kevin:test up
				DbUtil.commitConn(gate.gDbConn);
				gb.setContExcept(0); 
			}


			// 交易資料統計分析,控制 TIME-OUT 及 系統狀態

			// 設定回覆交易訊息類別
			String lastType = gate.mesgType.substring(3,4);

			//kevin:回覆訊息時，mesgType必須特別處理，避免錯誤的回覆
			if (("0".equals(gate.mesgType.substring(2,3))) || ("2".equals(gate.mesgType.substring(2,3)))){
				gate.mesgType = gate.mesgType.substring(0,2)  + (Integer.parseInt(gate.mesgType.substring(2,3)) + 1)  + lastType;
			}
			else {
				gb.showLogMessage("D","system error and Receive response mesgType =>" + gate.mesgType + "===");
				if (null != gate.gDbConn) {
					DbUtil.closeConn(gate.gDbConn); //kevin:針對系統錯誤，訊息結束時，系統不須特別處理，所以db需要close
				}
				return;
			}

			float durSec  = gb.durationTime(gate.startTime);

			gb.transStatistic(durSec,1);
			if ( durSec >=  gb.getWarningSec() ) {
				gb.showLogMessage("W",gate.mesgType+" "+gate.isoField[11]+" TIME-OUT 逾時交易 : "+""+durSec+ " 秒"); 
			}

//			gb.showLogMessage("D",gate.mesgType+" 回覆碼 : "+gate.isoField[39]+" - "+gate.isoField[38]+" 歷時 "+durSec+" 秒");

			//kevin:國內掛卡交易回覆處理
			if ("FHM".equals(gate.connType)) { //0302國外掛卡時，財金FiscFotmat要轉FhmFormat給ECS
				intr  =  new  FhmFormat(gb.getLogger(),gate); 
			}
			/* 主機格式 轉換為 ISO 格式 */
			intr.host2Iso();
//			gb.showLogMessage("D","Send out IsoString hex =>" + gate.isoString);
			if (!"08".equals(gate.mesgType.substring(0,2))) {
				gb.showLogMessage("D",gate.mesgType+" Response Code - Auth No : "+gate.isoField[39]+" - "+gate.isoField[38]+" Duration Time "+durSec+" sec.");
				String[] split = HpeUtil.byte2HexSplit(gate.isoData);
				String printHex = "";
				for (int p=0; p<gate.totalLen; p++) {
					if ((p+1)%16 == 0 || p+1==gate.totalLen) {
						printHex = printHex +":"+ split[p];
//						gb.showLogMessage("D","BBB-send out iso hexString =>"+printHex);
						printHex = "";
						continue;
					}
					printHex = printHex +":"+ split[p];
				}
				for(int p=0; p<gate.isoField.length; p++) {
					if (null != gate.isoField[p]) {
						if (!"".equals(gate.isoField[p])) {
							gb.showLogMessage("D", "BBB-send out iso field " + p + " ==>"+ gate.isoField[p].toString());
						}
					}
				}
			}

			/* 將交易回覆資料傳回 REQUEST 端 */
			authOut.write(gate.isoData, 0, gate.totalLen);
			authOut.flush();

		}
		catch ( Exception ex ) { 				
			gb.expHandle (ex, false);  
		}
		
		finalize();

	}  // end of TxnThread run

	// 釋放物件資原
	public void finalize() {
		intr = null;    
		gate = null;  
		proc = null;
		ta   = null;
		try {
			super.finalize();
		} catch (Throwable e) {
			gb.showLogMessage("E","finalize resourse faild =>"+e);
		}
	}

} // end of TxnThread
