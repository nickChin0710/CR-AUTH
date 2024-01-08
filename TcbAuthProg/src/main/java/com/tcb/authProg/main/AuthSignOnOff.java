/**
 * 授權使用自動SIGIN ON/SIGN OFF物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用自動SIGIN ON/SIGN OFF物件            *
 * 2022/09/28  V1.00.01  Kevin       授權連線偵測異常時發送簡訊通知維護人員             *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 ******************************************************************************
 */

package com.tcb.authProg.main;

import java.io.BufferedOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.datatype.DatatypeConfigurationException;

import com.tcb.authProg.util.HpeUtil;


public class AuthSignOnOff extends Thread{

	AuthGlobalParm gGb = null;
	Socket  authSd  =  null;
	int chanNum=0;
	final String signOnTriggerFileName="signon";
	final String signOnTriggerOk="signon_Ok";
	final String signOffTriggerFileName="signoff";
	final String signOffTriggerOk="signoff_Ok";
	final String echoTriggerFileName="echo";
	final String echoTriggerOk="echo_Ok";
	final String shutdownTriggerFileName="shutdown";
	final String shutdownTriggerOk="shutdown_Ok";



	public AuthSignOnOff(AuthGlobalParm pGb, Socket authSd, int chanNum) {
		// TODO Auto-generated constructor stub
		gGb = pGb;
		this.authSd = authSd;
		this.chanNum  = chanNum;

	}
	
	private String genCmdStr(int npType) {
		String slResult = "";
		String slMsgType = "0800";
		String slBitMap = HpeUtil.hexStr2Str("C2200000800000020400000500000000");
//		System.out.println("bitMAPS="+slBitMap);
		String slIsoField02 = "0501272";
		String slCurDateTime = null;
		try {
			slCurDateTime = HpeUtil.getCurDateTimeStr(false, false);
		} catch (DatatypeConfigurationException e) {
			slCurDateTime = "00000000000000";
			gGb.showLogMessage("E","AuthSignOnOff getCurDateTime ERROR");
		}
		String slIsoField07 = slCurDateTime.substring(4, 14); //ISO_FIELD[7 ]      : 0515055751
//		String slIsoField11 = slCurDateTime.substring(8, 14); //ISO_FIELD[11]      : 025116
		String slIsoField11 = HpeUtil.fillCharOnLeft(Integer.toString(HpeUtil.getRandomNumber(100000)), 6, "0"); //ISO_FIELD[11]
		String slIsoField33 = "0501272";
		String slIsoField63 = "010MCC 000004";
		String slIsoField70 = "";
		String slIsoField94 = "CA0    ";
		String slIsoField96 = HpeUtil.hextoStr("0000000000000000");
		//kevin:for FISC SIGN-ON/OFF
		if ("Y".equals(gGb.getIfEnableFisc())) {
			if (npType==1) { //sign on
				slIsoField70 = "061";							 
			}  
			else 
			if (npType==2) { //sign off
				slIsoField70 = "062";
			}
			else {
				slIsoField70 = "270";
			}
			slResult = slMsgType    + slBitMap     + slIsoField02 + slIsoField07 + 
					slIsoField11 + slIsoField33 + slIsoField63 + slIsoField70 + 
					slIsoField94 + slIsoField96;
//			System.out.println("sign-on-b="+slResult);
//			System.out.println("sign-on-a="+HpeUtil.string2Hex(slResult, "UTF-8"));
			byte[] test = slResult.getBytes();
//			System.out.println("EBCDIC:" + HpeUtil.getByteHex(test));
			String s = new String(test);
//			System.out.println(s);
			slResult = s;
			
		}
		else {
			
		}
		return slResult;
	}
	private boolean sendSignOnOff(int npType) {
		boolean blResult = true;
		String isoField11="", isoField39="", isoField70="";
		String logMessage="";
		try {
			
			Socket  authSocket= authSd;

			String slCmdStr =  genCmdStr(npType);
			isoField11 = slCmdStr.substring(37,43);
			
			byte[] pDataAry = HpeUtil.genFiscIsoByteAry(slCmdStr, "C2200000800000020400000500000000");
//			System.out.println("send out signon/signoff/echo command:" + slCmdStr + "---");
//			System.out.println("EBCDIC:" + HpeUtil.getByteHex(pDataAry));
			
			BufferedOutputStream lOutStream = new BufferedOutputStream(authSocket.getOutputStream());

			if (npType==1) { //sign on
//				System.out.println("----- begin sign on...channel:"+chanNum);
//				lOutStream.write(pDataAry);
//				lOutStream.flush();
//				gGb.showLogMessage("I", "Sign on to Fisc...channel:"+chanNum);
//				System.out.println("----- end sign on...");
				logMessage = "Sign on to Fisc";
			}
			else if (npType==2) {//sign off
//				System.out.println("----- begin sign off...channel:"+chanNum);
//				lOutStream.write(pDataAry);
//				lOutStream.flush();
//				gGb.showLogMessage("I", "Sign off to Fisc...channel:"+chanNum);
//				System.out.println("----- end sign off...");	
				logMessage = "Sign off to Fisc";
			}
			else if (npType==3) {//echo test
//				System.out.println("----- begin echo test...channel:"+chanNum);
//				lOutStream.write(pDataAry);
//				lOutStream.flush();
//				gGb.showLogMessage("I", "Echo test to Fisc...channel:"+chanNum);
//				System.out.println("----- end echo test...channel:"+chanNum);
				logMessage = "Echo test to Fisc";				
			}
			lOutStream.write(pDataAry);
			lOutStream.flush();
//			gGb.showLogMessage("I", logMessage+"...channel:"+chanNum);
			
			gGb.setFiscPnt(gGb.getFiscPnt() + 1);
			if ( gGb.getFiscPnt() >= gGb.getMaxFisc() ){
				gGb.setFiscPnt(0); 
			}
			gGb.getFiscRequest().put(isoField11,""+gGb.getFiscPnt());
			int k = gGb.getFiscPnt();

			gGb.showLogMessage("D","waiting for 0810 response : "+isoField11+";channel="+chanNum);
			/* 等待 FISC 回覆訊息 */
			gGb.getDoneLock()[k] = new Object();
			synchronized ( gGb.getDoneLock()[k] ) {
				gGb.getDoneLock()[k].wait(6*1000);  
				gGb.getDoneLock()[k] = null; 
			}

			String respData = (String)gGb.getFiscResponse().get(isoField11);
			if ( respData == null ) {
				isoField39 = "99"; 
				return false; 
			}
			gGb.getFiscResponse().remove(isoField11);
			gGb.getFiscRequest().remove(isoField11);

			String[] cvtData   = respData.split("@");
			isoField39  = cvtData[0];
			isoField70  = cvtData[1];
//			gGb.showLogMessage("D","FISC Function -"+isoField70+"- Response code = "+isoField39);
			if (!"00".equals(isoField39)) {
				blResult = false;
				gGb.showLogMessage("I", logMessage+" response failure! -"+isoField70+"- Response code = "+isoField39+"- channel:"+chanNum);
			}
			else {
				gGb.showLogMessage("I", logMessage+" response successful! -"+isoField70+"- Response code = "+isoField39+"- channel:"+chanNum);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
			////System.out.println("Exception on sendSignOnOff()=>" +  e.getMessage() + "====");
		}
		
				
		return blResult;
	}
	 
	
	public void run() {
		try {
			boolean blInitProc = true;
			int reTry = 0;
			while (true) {
				if (blInitProc) {
					if (sendSignOnOff(1)) {           //send sign on
						blInitProc = false;
					}
				}
				String slAuthHome =gGb.getAuthHome();
		        // fix issue "Path Manipulation" 2020/09/16 Zuwei
				slAuthHome = HpeUtil.verifyPath(slAuthHome);
				Path lHomePath = Paths.get(slAuthHome);
				if (Files.exists(lHomePath)) {
					//down, process shutdown AuthService4FISC  
					String slFullPathTriggerFileName = slAuthHome + "/lib/" + shutdownTriggerFileName;
			        // fix issue "Path Manipulation" 2020/09/16 Zuwei
					slFullPathTriggerFileName = HpeUtil.verifyPath(slFullPathTriggerFileName);

					Path lTargetFilePath = Paths.get(slFullPathTriggerFileName);

					if (Files.exists(lTargetFilePath)) {
						Files.delete(lTargetFilePath);
						slFullPathTriggerFileName = HpeUtil.verifyPath(slAuthHome + "/lib/" + shutdownTriggerOk);
						lTargetFilePath = Paths.get(slFullPathTriggerFileName);
						gGb.showLogMessage("I", "OPMENU request shutdown response status = "+shutdownTriggerOk);
						Files.createFile(lTargetFilePath);
						System.exit(-1);//shutdown 
						
					}
					//up, process shutdown AuthService4FISC 
					
					if (!"1".equals(gGb.getThreadConnectionStatusArray()[chanNum])) {
						reTry++;
						//down, process sign on  
						slFullPathTriggerFileName = slAuthHome + "/lib/" + signOnTriggerFileName;
				        // fix issue "Path Manipulation" 2020/09/16 Zuwei
						slFullPathTriggerFileName = HpeUtil.verifyPath(slFullPathTriggerFileName);
	
						lTargetFilePath = Paths.get(slFullPathTriggerFileName);
	
						if (Files.exists(lTargetFilePath)) {
							Files.delete(lTargetFilePath);
							if (sendSignOnOff(1)) {          //send sign on
								slFullPathTriggerFileName = HpeUtil.verifyPath(slAuthHome + "/lib/" + signOnTriggerOk);
								lTargetFilePath = Paths.get(slFullPathTriggerFileName);
								gGb.showLogMessage("I", "OPMENU request Sign On response status = "+lTargetFilePath);
								Files.createFile(lTargetFilePath);
							}
						}
						//up, process sign on 
	
						//down, process sign off  
						slFullPathTriggerFileName = slAuthHome + "/lib/" + signOffTriggerFileName ;
				        // fix issue "Path Manipulation" 2020/09/16 Zuwei
						slFullPathTriggerFileName = HpeUtil.verifyPath(slFullPathTriggerFileName);
	
						lTargetFilePath = Paths.get(slFullPathTriggerFileName);
	
						if (Files.exists(lTargetFilePath)) {
							Files.delete(lTargetFilePath);
							if (sendSignOnOff(2)) {          //send sign off
								slFullPathTriggerFileName = HpeUtil.verifyPath(slAuthHome + "/lib/" + signOffTriggerOk);
								lTargetFilePath = Paths.get(slFullPathTriggerFileName);
								gGb.showLogMessage("I", "OPMENU request Sign off response status = "+lTargetFilePath);
								Files.createFile(lTargetFilePath);
							}	
						}
						
						//up, process sign off 
						//down, process sign off  
						slFullPathTriggerFileName = slAuthHome + "/lib/" + echoTriggerFileName ;
				        // fix issue "Path Manipulation" 2020/09/16 Zuwei
						slFullPathTriggerFileName = HpeUtil.verifyPath(slFullPathTriggerFileName);
	
						lTargetFilePath = Paths.get(slFullPathTriggerFileName);
	
						if (Files.exists(lTargetFilePath)) {
							Files.delete(lTargetFilePath);
							if (sendSignOnOff(3)) {          //send echo test
								slFullPathTriggerFileName = HpeUtil.verifyPath(slAuthHome + "/lib/" + echoTriggerOk);
								lTargetFilePath = Paths.get(slFullPathTriggerFileName);
								gGb.showLogMessage("I", "OPMENU request Echo test response status = "+lTargetFilePath);
								Files.createFile(lTargetFilePath);
							}							
						}
						//up, process sign off 
						if (reTry > 60) {
							//db狀態異常時，不做echo test改為sign off
							//V1.00.01 授權連線偵測異常時發送簡訊通知維護人員
							//V1.00.42 授權系統與DB連線交易異常時的處理改善方式
							//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(DB連線交易異常條件修改)
							gGb.showLogMessage("I","check db Work Connections =" + gGb.getgDataSource().getNumConnections() );
							gGb.showLogMessage("I","check db Idle Connections =" + gGb.getgDataSource().getNumIdleConnections() );
							gGb.showLogMessage("I","check db Busy Connections =" + gGb.getgDataSource().getNumBusyConnections() );
							if (gGb.getgDataSource().getNumConnections() == 0 || gGb.getgDataSource().getNumBusyConnections() == 50) {
								if (!gGb.isFiscSignOff()) { 
									gGb.setFiscSignOff(true);
									sendSignOnOff(2);//send sign off
									gGb.showLogMessage("E", "DB Connection faild, Auth system auto sign off to FISC ");
									gGb.sendSms("授權系統通知!"+gGb.getSystemName()+"授權系統與DB資料庫的連線異常，已通知財金代行，請系統人員檢查網路與DB的連線，必要時可重新啟動授權，謝謝。");
								}
								else {
									if (!sendSignOnOff(3)) { //send sign off
										gGb.showLogMessage("E", "The echo test has no response and the DB connect fail. So stop the FISC socket connection.");
										gGb.setConncetionStatus("FISC", chanNum, "1");
										Thread.currentThread().interrupt();
									}
								}
							}
							else {
								if (gGb.isFiscSignOff()) {
									gGb.setFiscSignOff(false);
									sendSignOnOff(1);//send sign on
									gGb.showLogMessage("E", "DB Connection recovery, Auth system auto sign on to FISC ");
									gGb.sendSms("授權系統通知!"+gGb.getSystemName()+"授權系統與DB資料庫的連線已恢復正常，並恢復自主授權，請客服人員檢查授權交易是否正常，謝謝。");
								}
								else {
									if (!sendSignOnOff(3)) { //send echo test
										gGb.showLogMessage("E", "The echo test has no response. So stop the FISC socket connection.");
										gGb.setConncetionStatus("FISC", chanNum, "1");
										Thread.currentThread().interrupt();
									}
								}
							}
							reTry = 0;
						}
					}
					else {
//						System.out.println("Socket is not connected, could not signon/signoff. ");	
						gGb.showLogMessage("E", "Socket is not connected, could not signon/signoff");
						gGb.setConncetionStatus("FISC", chanNum, "1");
						Thread.currentThread().interrupt();
						
					}
				}
				else {
//					System.out.println("AuthHome 不存在:" +slAuthHome );
					gGb.showLogMessage("E", "AuthHome 不存在:" + slAuthHome);
				}
				
				Thread.sleep(1*1000);

			}
		} catch (Exception e) {
			// TODO: handle exception
			gGb.showLogMessage("E", "Exception on AuthSignOnOff.run() =>" + e.getMessage() + "===");
		}
	}

}
