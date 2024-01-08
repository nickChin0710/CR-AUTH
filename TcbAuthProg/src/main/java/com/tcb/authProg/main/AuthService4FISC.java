/**
 * 授權主程式
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 * TCB與財金連線資訊：                                                            *
 ******************************************************************************
 * nohup java -jar TcbAuthProg.jar 1 &                                        *
 * nohup java -jar TcbAuthProg.jar 2 &                                        *
 * 上述參數 1/2 指的是 FISC channel number                                        *
 * 台北資訊室主機使用之IP為 172.19.229.193 (GW1) 172.19.229.194 (GW2)               *
 * 台中主機使用之IP為 172.19.228.193                                              * 
 * 另外對貴行而言，本公司CARD主機IP/PORT為:                                          *  
 * 172.26.254.252/ 6774(連線授權/國際掛卡)                                        *
 * 172.26.254.252/ 6773(國內掛卡)                                               *                             *
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       授權主程式                                  *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      * 
 * 2022/04/06  V1.00.02  Kevin       財金的國內掛卡不用檢查Length                   *
 * 2022/05/04  V1.00.03  Kevin       ATM預借現金密碼變更功能開發                    *
 * 2022/09/28  V1.00.04  Kevin       授權連線偵測異常時發送簡訊通知維護人員             *
 * 2022/10/18  V1.00.05  Kevin       財金授權連線收到garbage資料時，不須中斷執行緒，直接  *
 * 									 continue。                                *
 * 2023/01/12  V1.00.33  Kevin       財金國內掛卡連線新增Keepalive功能               *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.BasicConfigurator;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.hsm.HsmApi;
import com.tcb.authProg.util.DbUtil;
import com.tcb.authProg.util.HpeUtil;
//import com.tcb.authProg.iso8583.AtmFormat;
//import com.tcb.authProg.iso8583.FhmFormat;
//import com.tcb.authProg.iso8583.BicFormat;
//import com.tcb.authProg.iso8583.FiscFormat;
//import com.tcb.authProg.iso8583.FormatInterChange;
//import com.tcb.authProg.iso8583.NegFormat;
//import com.tcb.authProg.process.ProcAuthTxn;
import com.tcb.authProg.process.AuthTxnThread;

import net.sf.json.JSONObject;


@SuppressWarnings("unchecked")
public class AuthService4FISC {

	AuthGlobalParm   gb  =  new AuthGlobalParm();
	
	TableAccess  gTa =  null;
	//FISC授權連線與國際掛卡
	Socket[]  fiscSd  =  null;
	BufferedInputStream[]  fiscIn  = null;
	BufferedOutputStream[] fiscOut = null;
	//FISC國內掛卡
	Socket[]  fiscNegSd  =  null;
	BufferedInputStream[]  fiscNegIn  = null;
	BufferedOutputStream[] fiscNegOut = null;
	//
	private final int sleepSec1=1, sleepSec5=5, sleepSec30=30;

	
	public static void main(String args[]) throws Exception {


		BasicConfigurator.configure();
		if ( args.length != 1 ) {
//			System.out.println("Please input FISC channel number.");
			return; 
		}

		AuthService4FISC serv = new AuthService4FISC();
		serv.kernelProcess(args);
	}
	
	private void initDatabase() throws Exception{
		// 連接資料庫
		String slAuthHome = gb.getAuthHome();
		String slBeanFile = "";
		slBeanFile  =  slAuthHome + "/parm/bean3.xml";
		gb.showLogMessage("I","FISC Auth Home :"+ slAuthHome);   
		gb.showLogMessage("I","FISC Bean File :"+ slBeanFile);   
	
		gb.setgDataSource(DbUtil.initDataSource(slBeanFile, gb.getSgDbPInfo()));
		if (null == gb.getgDataSource()) {
			gb.showLogMessage("E","DB ERROR:"+ "Init datasource failed!!");   
		}
	}

	public void kernelProcess(String args[]) {

		try {

			String slChannelNum = args[0];

			gb.dateTime();

			//建立 log4j TEXT LOG 物件
			gb.createLogger("P", slChannelNum, ""+gb.getFiscPort());

			// 取得授權系統參數
			gb.loadTextParm("FISC" + slChannelNum);
			
			//建立資料庫連線
			initDatabase();

//			gTa =  new TableAccess(gb);

			//建立連線到FISC 
			if ("Y".equals(gb.getIfEnableFisc())) {
				//FISC授權連線與國際掛卡連線6774
				gb.showLogMessage("I","FISC Server Port :"+ gb.getFiscPort());    	   
				fiscSd  =  new Socket[gb.getFiscChan()];

				fiscIn  =  new BufferedInputStream[gb.getFiscChan()];
				fiscOut =  new BufferedOutputStream[gb.getFiscChan()];
				int nlChannelCount=0; 
				nlChannelCount=gb.getFiscSession();//建立連線數量 
				if (nlChannelCount > 0) {
					ConnectControl fiscThread = new ConnectControl("FISC",fiscSd,fiscIn,fiscOut,gb.getFiscHost(),gb.getFiscPort(),nlChannelCount);
					fiscThread.start();
				}
				Thread.sleep(sleepSec1*1000);
				
				//FISC國內掛卡neg連線6773
				gb.showLogMessage("I","FISC NEG Server Port :"+ gb.getFiscNegPort());    	   
				fiscNegSd  =  new Socket[gb.getFiscNegChan()];

				fiscNegIn  =  new BufferedInputStream[gb.getFiscNegChan()];
				fiscNegOut =  new BufferedOutputStream[gb.getFiscNegChan()];
				int nlChannelNegCount=0; 
				nlChannelNegCount=gb.getFiscNegSession();//建立連線數量 
				if (nlChannelNegCount > 0) {
					ConnectControl negThread = new ConnectControl("FISCNEG",fiscNegSd,fiscNegIn,fiscNegOut,gb.getFiscHost(),gb.getFiscNegPort(),nlChannelNegCount);
					negThread.start();
				}
				Thread.sleep(sleepSec1*1000);
			}

			//check FISC連線是否正常
			//V1.00.04 授權連線偵測異常時發送簡訊通知維護人員
	        connectionControl connectionControlThread = new connectionControl(gb.getFiscSession());
	        connectionControlThread.start();

	        //啟動Internal Server
	        if (gb.isEnableLocalAuthServer4Fisc()) {
				//準備接受線上IMS Connect for ATM PIN change 、cash advance
		        ConnectAtmIms connectAtmImsThread = new ConnectAtmIms();
		        connectAtmImsThread.start();
		        
		        // 準備接受線上掛卡指令, 線上0302國際掛卡	        
		        ConnectFhmManual connectFhmThread = new ConnectFhmManual();
		        connectFhmThread.start();
		        
		        // 準備接受線上掛卡指令, 線上0300國內掛卡	        
		        ConnectNegManual connectNegThread = new ConnectNegManual();
		        connectNegThread.start();
		        
		        // 準備接受TEST指令, 線上模擬財金授權	
				if (gb.isDebugMode()) {
					ConnectTestManual connectTestThread = new ConnectTestManual();
					connectTestThread.start();
				}      
		        // 準備接受人工授權
				connectWebManual();

			}

		} // End try
		catch ( Exception ex ) {
			gb.expHandle (ex, false);  
		}

	}

	private class ConnectControl extends Thread {

		String connType="";
		Socket[] authSd;
		BufferedInputStream authIn[];
		BufferedOutputStream authOut[];
		String ipAddress;
		int portNo=0, chanCount=0; 
		// 連線 FISC( DEDICATE CONNECT )
		ConnectControl(String connType,Socket[] authSd,BufferedInputStream authIn[],BufferedOutputStream authOut[],String ipAddress,int portNo,int chanCount) {
			this.connType = connType;
			this.authSd=authSd;
			this.authIn=authIn;
			this.authOut= authOut;
			this.ipAddress = ipAddress;
			this.portNo=portNo;
			this.chanCount=chanCount;	  
		}


		public void closeSocket(int nlChanNum) {

			try {


				if (authSd[nlChanNum] != null) { 

					authSd[nlChanNum].close();

					authIn[nlChanNum]  = null;
					authOut[nlChanNum]  = null;

					authSd[nlChanNum]  = null; 
				}



			}
			catch ( Exception ex) {   
				gb.showLogMessage("E","Exception Z: Socket closed.Exception:"+ ex.getMessage());

			}
		}

		//public class ConnectControl extends Thread
		//V1.00.04 授權連線偵測異常時發送簡訊通知維護人員
		//V1.00.33 財金國內掛卡連線新增Keepalive功能
		public void run() {  

			boolean blInitProc = true;
			try{
				if ("FISCNEG".equals(connType)) {
					gb.setThreadConnectionNegStatusArray(new String[chanCount]);
					for( int k=0; k<chanCount; k++ ) {
						gb.setConncetionStatus(connType, k, "1");
					}
					while (true) {
						/* 建立 SOCKET I/O 物件 */
						for( int i=0; i<chanCount; i++ ) {
							if ( "1".equals(gb.getThreadConnectionNegStatusArray()[i])) {
								if (!blInitProc) {
									closeSocket(i);
									Thread.sleep(sleepSec5*1000);
								}
								try {
									authSd[i]  =  new  Socket(ipAddress,portNo);
								}
								catch ( Exception ex ) {
									gb.showLogMessage("E","can not new socket ! port:"+ (portNo) + ".Exception:" +ex.getMessage());
//									gb.getThreadConnectionNegStatusArray()[i]="1";
									gb.setConncetionStatus(connType, i, "1");
									closeSocket(i);
									continue; 	
								}
								authSd[i].setSoTimeout(gb.getReadSocketTimeout()*1000);
								authIn[i]  =  new  BufferedInputStream(authSd[i].getInputStream());
								authOut[i] =  new  BufferedOutputStream(authSd[i].getOutputStream());
								gb.setConncetionStatus(connType, i, "2");
						
								/*  多工連線處理 每一個 CHANNEL 一個 THREAD */
								ChannelInThread inp = new ChannelInThread(connType,authSd[i],authIn[i],authOut[i],i);
								inp.start();
								
								Thread.sleep(sleepSec5*1000);
								/*  多工連線處理 每一個 CHANNEL 一個 AuthKeepalive THREAD */
								AuthKeepalive lAuthKeepalive = new AuthKeepalive(gb, authSd[i],i);
								lAuthKeepalive.start();
								
								if (!blInitProc)
									Thread.sleep(sleepSec1*1000);
							}
						}
						if (blInitProc) {
							Thread.sleep(sleepSec30*1000);
							for( int m=0; m<chanCount; m++ ) {
								if (gb.getThreadConnectionNegStatusArray()[m] =="1") {
									gb.setFiscNegConnectFail(true); //Fisc Neg連線失敗
									gb.showLogMessage("F",gb.getSystemName()+" connect to FISC NEG Failed. channel="+gb.getFiscNegSession()+":"+(m+1)+"isFiscNegConnectFail="+gb.isFiscNegConnectFail());
								}
								else {
									blInitProc=false;
									gb.setFiscNegConnectFail(false);//Fisc Neg連線成功
									gb.showLogMessage("F",gb.getSystemName()+" connect to FISC NEG Successful. channel="+gb.getFiscNegSession()+":"+(m+1)+"isFiscNegConnectFail="+gb.isFiscNegConnectFail());

								}
							}
						}
						Thread.sleep(sleepSec30*1000); 
					}
				}
				else {
					gb.setThreadConnectionStatusArray(new String[chanCount]);
					for( int k=0; k<chanCount; k++ ) {
						gb.setConncetionStatus(connType, k, "1");
					}
					while (true) {
						/* 建立 SOCKET I/O 物件 */
						for( int i=0; i<chanCount; i++ ) {
							if ( "1".equals(gb.getThreadConnectionStatusArray()[i])) {
								if (!blInitProc) {
									closeSocket(i);
									Thread.sleep(sleepSec5*1000);
								}
								try {
									authSd[i]  =  new  Socket(ipAddress,portNo);
								}
								catch ( Exception ex ) {
									gb.showLogMessage("E","can not new socket ! port:"+ (portNo) + ".Exception:" +ex.getMessage());
//									gb.getThreadConnectionStatusArray()[i]="1";
									gb.setConncetionStatus(connType, i, "1");
									closeSocket(i);
									continue; 	
								}
								authSd[i].setSoTimeout(gb.getReadSocketTimeout()*1000);
								authIn[i]  =  new  BufferedInputStream(authSd[i].getInputStream());
								authOut[i] =  new  BufferedOutputStream(authSd[i].getOutputStream());
						
								/*  多工連線處理 每一個 CHANNEL 一個 THREAD */
								ChannelInThread inp = new ChannelInThread(connType,authSd[i],authIn[i],authOut[i],i);
								inp.start();
								
								Thread.sleep(sleepSec5*1000);
								/*  多工連線處理 每一個 CHANNEL 一個 AuthSignOnOff THREAD */
								AuthSignOnOff lAuthSignOnOff = new AuthSignOnOff(gb, authSd[i],i);
								lAuthSignOnOff.start();
								
								if (!blInitProc)
									Thread.sleep(sleepSec1*1000);
							}
						}
						if (blInitProc) {
							Thread.sleep(sleepSec30*1000);
							for( int m=0; m<chanCount; m++ ) {
								if ("1".equals(gb.getThreadConnectionStatusArray()[m])) {
									gb.showLogMessage("F",gb.getSystemName()+" Connect to FISC Failed. channel="+gb.getFiscSession()+":"+(m+1));
								}
								else {
									gb.showLogMessage("F",gb.getSystemName()+" Connect to FISC Successful. channel="+gb.getFiscSession()+":"+(m+1));
									blInitProc=false;
								}
							}
						}
						Thread.sleep(sleepSec30*1000); 
					}
				}
			} // End try
			catch ( Exception ex ) {
				//gb.expHandle (ex);
				if (blInitProc) {
					for( int m=0; m<chanCount; m++ ) {
						if ("FISCNEG".equals(connType)) {
							if ("1".equals(gb.getThreadConnectionNegStatusArray()[m])) {
								gb.showLogMessage("E","Exception!! Can not connect to FISC NEG. Auth system was terminted.Exception:" + ex.getMessage());
//								System.exit(-1);
								gb.setFiscNegConnectFail(true); //Fisc Neg連線失敗
							}
						}
						else {						
							if ("1".equals(gb.getThreadConnectionStatusArray()[m])) {
								gb.showLogMessage("E","Exception!! Can not connect to FISC. Auth system was terminted.Exception:" + ex.getMessage());
//								System.exit(-1);
							}
						}
					}
					gb.sendSms("授權系統通知!"+gb.getSystemName()+"授權系統與("+connType+")的連線系統異常，請關閉告警，使用opmenu(a3)強制關閉授權、等待10秒鐘後，(a1)啟動授權，開啟告警，謝謝。");
				}
			}
		}
	}
				
	
	//準備接受ATM經由IMS轉Socket連線
	private class ConnectAtmIms extends Thread {
		
		public void run() {
			// fix issue "Unreleased Resource: Sockets" 2020/09/17 Zuwei  
			try (
				ServerSocket serverAtm  =  new ServerSocket(gb.getInternalAuthServerPort4Atm()); ) {
				while (true) {
					gb.showLogMessage("F",gb.getSystemName()+" Port No:" + gb.getInternalAuthServerPort4Atm()+", start successful and ready for ATM message");

					/* 等待 ATM 授權 連線 */
					Socket atmSd = serverAtm.accept();
	
					/* 建立ATMconnect ATM預借授權 I/O 物件 */
					BufferedInputStream  atmIn  = new BufferedInputStream(atmSd.getInputStream());
					BufferedOutputStream atmOut = new BufferedOutputStream(atmSd.getOutputStream()); 
	
					/*  ATMconnect ATM預借授權 多工處理 */
					ChannelInThread inp = new ChannelInThread("ATM",atmSd,atmIn,atmOut,gb.getWebThreadChanNum());
					inp.start();
				}
			} // End try
			catch ( Exception ex ) {
				gb.showLogMessage("E","Exception on ConnectAtmIms. Exception is :" + ex.getMessage());
				gb.expHandle (ex, false);  
			}
		}
	}
	
	//準備接受FHM經由ECS轉Socket連線
	private class ConnectFhmManual extends Thread {
		
		public void run() {  
			// fix issue "Unreleased Resource: Sockets" 2020/09/17 Zuwei
			try (
				ServerSocket serverFhm  =  new ServerSocket(gb.getInternalAuthServerPort4Fhm());) {
				while (true) {
					gb.showLogMessage("F",gb.getSystemName()+" Port No:" + gb.getInternalAuthServerPort4Fhm()+", start successful and ready for FHM message");

					/* 等待 FHM 授權 連線 */
					Socket fhmSd = serverFhm.accept();
	
					/* 建立FHMconnect FHM預借授權 I/O 物件 */
					BufferedInputStream  fhmIn  = new BufferedInputStream(fhmSd.getInputStream());
					BufferedOutputStream fhmOut = new BufferedOutputStream(fhmSd.getOutputStream()); 
	
					/*  FHMconnect FHM國際掛卡 多工處理 */
					ChannelInThread inp = new ChannelInThread("FHM",fhmSd,fhmIn,fhmOut,gb.getWebThreadChanNum());
					inp.start();
				}
			} // End try
			catch ( Exception ex ) {
				gb.showLogMessage("E","Exception on ConnectEcsFhm. Exception is :" + ex.getMessage());
				gb.expHandle (ex, false);  
			}
		}
	}	
	//準備接受NEG經由ECS轉Socket連線
	private class ConnectNegManual extends Thread {
		
		public void run() {  
			// fix issue "Unreleased Resource: Sockets" 2020/09/17 Zuwei
			try (
				ServerSocket serverNeg  =  new ServerSocket(gb.getInternalAuthServerPort4Neg()); ) {
				while (true) {
					gb.showLogMessage("F",gb.getSystemName()+" Port No:" + gb.getInternalAuthServerPort4Neg()+", start successful and ready for NEG message");

					/* 等待 NEG 授權 連線 */
					Socket negSd = serverNeg.accept();
	
					/* 建立NEGconnect NEG預借授權 I/O 物件 */
					BufferedInputStream  negIn  = new BufferedInputStream(negSd.getInputStream());
					BufferedOutputStream negOut = new BufferedOutputStream(negSd.getOutputStream());

					/*  NEGconnect NEG國內掛卡 多工處理 */
					ChannelInThread inp = new ChannelInThread("NEG",negSd,negIn,negOut,gb.getWebThreadChanNum());
					inp.start();
				}
			} // End try
			catch ( Exception ex ) {
				gb.showLogMessage("E","Exception on ConnectEcsNeg. Exception is :" + ex.getMessage());
				gb.expHandle (ex, false);  
			}
		}
	}
	
	//準備接受AuthTest模擬Fisc交易連線
	private class ConnectTestManual extends Thread {
		
		public void run() {  
			// fix issue "Unreleased Resource: Sockets" 2020/09/17 Zuwei
			try (
				ServerSocket serverTest  =  new ServerSocket(15005); ) {
				while (true) {
					gb.showLogMessage("F",gb.getSystemName()+" Port No:" +15005+", start successful and ready for TEST message");


					/* 等待 Test 授權 連線 */
					Socket testSd = serverTest.accept();
	
					/* 建立TESTconnect FISC TEST 授權 I/O 物件 */
					BufferedInputStream  testIn  = new BufferedInputStream(testSd.getInputStream());
					BufferedOutputStream testOut = new BufferedOutputStream(testSd.getOutputStream());

					/*  TESTconnect TEST FISC AUTH 多工處理 */
					ChannelInThread inp = new ChannelInThread("FISC",testSd,testIn,testOut,gb.getWebThreadChanNum());
					inp.start();
				}
			} // End try
			catch ( Exception ex ) {
				gb.showLogMessage("E","Exception on ConnectEcsFisc. Exception is :" + ex.getMessage());
				gb.expHandle (ex, false);  
			}
		}
	}
	
	//準備接受人工授權與網管指令, Ex: sign on, sign off, change key, verify key, repeat key
	public void connectWebManual() {

		try (
			ServerSocket serverSd  =  new ServerSocket(gb.getInternalAuthServerPort4Online()); ) {
			while (true) {
				gb.showLogMessage("F",gb.getSystemName()+" Port No:" + gb.getInternalAuthServerPort4Online()+", start successful and ready for WEB message");

				/* 等待 WEB 人工授權 連線 */
				Socket manualSd = serverSd.accept();
	
				/* 建立 WEB 人工授權 I/O 物件 */
				BufferedInputStream  manualIn  = new BufferedInputStream(manualSd.getInputStream());
				BufferedOutputStream manualOut = new BufferedOutputStream(manualSd.getOutputStream());

				/* WEB 人工授權 多工處理 */
				ChannelInThread inp = new ChannelInThread("WEB",manualSd,manualIn,manualOut,gb.getWebThreadChanNum());
				inp.start();
			}
		} // End try
		catch ( Exception ex ) {
			gb.showLogMessage("E","Exception on ConnectWebManual. Exception is :" + ex.getMessage());
			gb.expHandle (ex, false);  
		}
	}

	/* CHANNEL INPUT 處理程序 */
	private class ChannelInThread extends Thread {

		String  connType="";
		Socket  authSd  =  null;
		BufferedInputStream  authIn  = null;
		BufferedOutputStream authOut = null;
		BufferedOutputStream fiscFhmNegOut = null;


		int chanNum=0;

		ChannelInThread(String connType,Socket authSd,BufferedInputStream authIn,BufferedOutputStream authOut,int chanNum) { 

			this.connType = connType;
			this.authSd   = authSd;
			this.authIn   = authIn;
			this.authOut  = authOut;
			this.chanNum  = chanNum;
		}

		//V1.00.04 授權連線偵測異常時發送簡訊通知維護人員
		public void run() {
			try {
				int     headLen=0,inputLen = 0,packetLen=0;
				byte[]  authData = new byte[2048];
				byte[]  lenData  = new byte[3];
				
				//FISC要求常連接發送keepalive
				authSd.setKeepAlive(true);

				boolean blKeepRunning=true;
				while (blKeepRunning) {

					try {
						/* 從 SOCKET 讀取交易資料長度 */
//						gb.setConncetionStatus(connType, this.chanNum, "2");
						headLen =  authIn.read(lenData, 0, 2);
						//kevin:test
//						gb.showLogMessage("I","headLen="+headLen);
//						gb.showLogMessage("I","ChanNum="+this.chanNum+"webThareadChanNum="+gb.getWebThreadChanNum());

						if ( headLen != 2 ) {
							closeSocket();
							blKeepRunning = false;
							if ( this.chanNum != gb.getWebThreadChanNum() ) {
								if (ifAllSocketisClosed()) {
									gb.showLogMessage("E","Info G: All of socket are disconnected. Auth system shutdowned.");
								}
							}
							if (!this.isInterrupted()) {
								gb.setConncetionStatus(connType, this.chanNum, "1");
								Thread.currentThread().interrupt();
							}
							return;  
						}
						gb.setConncetionStatus(connType, this.chanNum, "2");

						/* 從 SOCKET 讀取交易資料 */
						packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);
						inputLen  = authIn.read(authData, 0, packetLen);
						if ( inputLen != packetLen  || inputLen == 0) {
							gb.showLogMessage("I","packetLen="+packetLen+";inputLen="+inputLen);
							gb.showLogMessage("E","Exception F: Read data inputLen != packetLen.Channel num:"+ Integer.toString(this.chanNum));
							//V1.00.02 財金的國內掛卡不用檢查Length
							//V1.00.05 財金授權連線收到garbage資料時，不須中斷執行緒，直接continue。
							if ("FISCNEG".equals(connType) || "FISC".equals(connType)) {
								gb.showLogMessage("I","connType="+connType+"packlen error for ChanNum="+this.chanNum+";inputLen="+inputLen);
								continue;
							}
							if ("ATM".equals(connType)) {
								gb.showLogMessage("D","lenData[0]="+lenData[0]+";lenData[1]="+lenData[1]);
								/* 將ATM交易回覆資料傳回 REQUEST 端 */ 
								int errorLen = 18;
								byte[] errorData = new byte[errorLen];
								errorData        = "      ERROR:LENGTH".getBytes("cp1047");
								errorData[0]  = (byte)(errorLen / 256);
								errorData[1]  = (byte)(errorLen % 256);
								errorData[2]  = (byte)(errorLen / 256);
								errorData[3]  = (byte)(errorLen % 256);
								errorData[4]  = (byte)(0);
								errorData[5]  = (byte)(0);
								gb.showLogMessage("D","ATM Length error responsen : ERROR:LENGTH");
								authOut.write(errorData, 0, errorLen);
								authOut.flush();
							}
							closeSocket();
							blKeepRunning = false;
							if (!this.isInterrupted()) {
								gb.setConncetionStatus(connType, this.chanNum, "1");
								Thread.currentThread().interrupt();
							}
							return;
						}


						if ( chanNum != gb.getWebThreadChanNum() )  {
							gb.setConncetionStatus(connType, this.chanNum, "3");
						}

						/* 啟動交易處理多工程序 */
						if ( "FHM".equals(connType)) {
							fiscFhmNegOut = fiscOut[0];
						}
						else if ( "NEG".equals(connType)) {
							fiscFhmNegOut = fiscNegOut[0];
						}
						AuthTxnThread trans = new AuthTxnThread(gb,connType,chanNum,inputLen,authData,authOut,fiscFhmNegOut);
						trans.start();
						
						/* 同步等待ECS多工程序結束 。(gb.warningSec = 6)*/
						if ( "WEB".equals(connType)) {
//							trans.join((int)gb.getWarningSec() * 1000); 
							/* 關閉socket連線，最多等待(gb.warningSec = 6)*/
							//2023/07/03  V1.00.48 P3程式碼整理(避免批次授權timeout時間太短，造成loop)
							trans.join(15 * 1000); 
							closeSocket();
							blKeepRunning = false;
							gb.setConncetionStatus(connType, this.chanNum, "1");
							Thread.currentThread().interrupt();
						}
						//同步等待ATM交易處理完成
						if ( "ATM".equals(connType)) {
							trans.join((int)gb.getWarningSec() * 1000); 
							/* 關閉socket連線，最多等待(gb.warningSec = 6)*/
							closeSocket();
							blKeepRunning = false;
							gb.setConncetionStatus(connType, this.chanNum, "1");
							Thread.currentThread().interrupt();
						}
						//同步等待FHM交易處理完成
						if ( "FHM".equals(connType)) {
//							trans.join((int)gb.getWarningSec() * 1000); 
							/* 關閉socket連線，最多等待(gb.warningSec = 6)*/
							trans.join(15 * 1000); 
							closeSocket();
							blKeepRunning = false;
							gb.setConncetionStatus(connType, this.chanNum, "1");
							Thread.currentThread().interrupt();
						}
						//同步等待NEG交易處理完成
						if ( "NEG".equals(connType)) {
//							trans.join((int)gb.getWarningSec() * 1000); 
							/* 關閉socket連線，最多等待(gb.warningSec = 6)*/
							trans.join(15 * 1000); 
							closeSocket();
							blKeepRunning = false;
							gb.setConncetionStatus(connType, this.chanNum, "1");
							Thread.currentThread().interrupt();
						}
						//同步等待TEST交易處理完成
						if ( "TEST".equals(connType)) {
							trans.join((int)gb.getWarningSec() * 1000); 
							/* 關閉socket連線，最多等待(gb.warningSec = 6)*/
							closeSocket();
							blKeepRunning = false;
							gb.setConncetionStatus(connType, this.chanNum, "1");
							Thread.currentThread().interrupt();
						}
					}
					catch ( Exception ex ) {
						if ( !"WEB".equals(connType) && !"FHM".equals(connType) && !"NEG".equals(connType) && !"ATM".equals(connType) && !"TEST".equals(connType)) {
							gb.showLogMessage("E","Exception "+ connType +" : Chan "+ Integer.toString(this.chanNum) + "==" + ex.getMessage());
						}
	
						closeSocket();
						blKeepRunning = false;
						//return;
						gb.setConncetionStatus(connType, this.chanNum, "1");
						Thread.currentThread().interrupt();

					}
				}
			}

			catch ( Exception ex ) {
				gb.showLogMessage("E","Exception B: Read data from socket failed.Channel num :"+ Integer.toString(this.chanNum) + ".Exception:" + ex.getMessage());

				closeSocket();

				gb.expHandle(ex, false);
				if (!this.isInterrupted()) {
					gb.showLogMessage("E","T-3=> Thread of chan number " + Integer.toString(this.chanNum) + " stopted .Exception;" + ex.getMessage());
					gb.setConncetionStatus(connType, this.chanNum, "1");
					Thread.currentThread().interrupt();
					//this.interrupt();
				}
				gb.setConncetionStatus(connType, this.chanNum, "1");
				Thread.currentThread().interrupt();
				//return;  
			}

		}  // end of ChannelInThread run

		public boolean ifAllSocketisClosed() {
			boolean blAllThreadDied = false;
			int nlDisconnectCount=0;
			for (int p=0; p<gb.getFiscChan(); p++) {
				if ("1".equals(gb.getThreadConnectionStatusArray()[p]) || 
						"1".equals(gb.getThreadConnectionNegStatusArray()[p])) {
					nlDisconnectCount++;
				}
			}

			if (nlDisconnectCount==gb.getFiscChan())
				blAllThreadDied = true;
			else
				blAllThreadDied = false;

			return blAllThreadDied;

		}

		/* 關閉 SOCKET 元件 */
		public void closeSocket() {

			try {

				if ( authSd != null )  { 
					authSd.close();

					authIn  = null;
					authOut = null;
					authSd  = null; 
					//gb.showLogMessage("D","...Close Socket","");
				}
			}
			catch ( Exception ex) {   
				gb.showLogMessage("E","Exception C: Socket closed.Exception:" +ex.getMessage());

			}
		}

	} // end of ChannelInThread



	private class connectionControl extends Thread {

		//HSM API存取物件
		HsmApi gHsmUtil = null;
		
		int ngChannelCount=0;
		
		connectionControl(int npChannelCount) {
			ngChannelCount = npChannelCount;
		}

		private void checkIfStopMainProc(){
			
			if ("Y".equals(gb.getIfEnableFisc())) {
				int nlDisconnectedCount=0;
				for (int i=0; i<gb.getThreadConnectionStatusArray().length; i++) {
					if ("1".equals(gb.getThreadConnectionStatusArray()[i])) {
						nlDisconnectedCount++;
					}	
				}	
				//V1.00.04 授權連線異常偵測時發送簡訊通知維護人員
				if (nlDisconnectedCount > 0 && (nlDisconnectedCount * 2) > ngChannelCount) {
					gb.showLogMessage("E","The number of connections is lower than the standard value.  Please check!");
					if (!gb.isFiscConnectFaild()) { 
						gb.setFiscConnectFaild(true);
						gb.sendSms("授權系統通知!"+gb.getSystemName()+"授權系統與FISC的連線出現異常("+nlDisconnectedCount+":"+ngChannelCount+")，請檢查網路與授權GW的狀態，必要時請關閉告警，下上6773、6774服務，開啟告警，謝謝。");
					}
				}
				else {
					gb.showLogMessage("I","The number of connections reaches the standard value.");
					if (gb.isFiscConnectFaild()) { 
						gb.setFiscConnectFaild(false);
						gb.sendSms("授權系統通知!"+gb.getSystemName()+"授權系統與FISC的連線已恢復正常，請客服人員檢查授權交易是否正常，謝謝。");
					}
				}
			}
		}
		private boolean checkHsmStatus() {
			boolean blResult = false;
			if ("2".equals(gb.getHsmIndicate())) {
				gb.showLogMessage("I","Current HSM Indicate to HSM2");
				this.gHsmUtil = new HsmApi(gb.getHsmHost2(), gb.getHsmPort2());
				try {
					String slHsm2Rc = gHsmUtil.hsmCommandNO().substring(0,2);
					if (!"00".equals(slHsm2Rc)) {
						gb.setHsmIndicate("1");
						gb.showLogMessage("I","HSM2 Connect test Failed ="+ slHsm2Rc);
					}
					else {
						gb.showLogMessage("I","HSM2 Connect test Successful ="+ slHsm2Rc);
						blResult = true;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					gb.setHsmIndicate("1");
					gb.showLogMessage("E","HSM2 SOCKET CONNECT FAILED=" + e);
//					e.printStackTrace();
				}
			}
			else {
				gb.showLogMessage("I","Current HSM Indicate to HSM1");
				this.gHsmUtil = new HsmApi(gb.getHsmHost1(), gb.getHsmPort1());
				try {
					String slHsm1Rc = gHsmUtil.hsmCommandNO().substring(0,2);
					if (!"00".equals(slHsm1Rc)) {
						gb.setHsmIndicate("2");		
						gb.showLogMessage("I","HSM1 Connect test Failed RC="+ slHsm1Rc);
					}
					else {
						gb.showLogMessage("I","HSM1 Connect test Successful RC="+ slHsm1Rc);
						blResult = true;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					gb.setHsmIndicate("2");	
					gb.showLogMessage("E","HSM1 SOCKET CONNECT FAILED =" + e);
//					e.printStackTrace();
				}
			}
			return blResult;
		}
		
		private boolean checkImsToken() {
			boolean blResult = false;
			if (!gb.getTokenExpDate().equals(gb.getSysDate())) {
				gb.showLogMessage("I","Current token expire");
				String applJson = "application/json";
				JSONObject jsonObjectUserPw = new JSONObject();
				jsonObjectUserPw.put("password", gb.getSgImsPInfo());
				jsonObjectUserPw.put("username", gb.getSgImsId());
				String userPass = jsonObjectUserPw.toString();
				String imsIndicate = gb.getsgImsIndicate();
			    String rtnToken = "";
			    String getImsUrl = gb.getSgImsTokenReqUrl() + "auth/token"; 
			    if ("2".equals(imsIndicate)) {
			    	getImsUrl = gb.getSgImsVdTxnUrl() + "auth/token";
			    }
				gb.showLogMessage("D","IMS TOKEN REQUEST indicate/url=" + imsIndicate + " / "+getImsUrl);
					try {
						int nlTimeOutSec = (int) gb.getWarningSec();
						gb.showLogMessage("D","IMS TOKEN REQUEST TimeOut Sec.=" + nlTimeOutSec);
						rtnToken = HpeUtil.curlToken(getImsUrl, applJson, "", userPass, nlTimeOutSec);
						if ("ERROR".equals(rtnToken) || "TIMEOUT".equals(rtnToken)) {
							gb.showLogMessage("E","IMS TOKEN Connect failed=" + rtnToken);
							if ("1".equals(imsIndicate)) {
								gb.setsgImsIndicate("2");
							}
							else {
								gb.setsgImsIndicate("1");
							}
							return false;
						}
					    gb.showLogMessage("D","curlToken = "+rtnToken);
					    JSONObject j = JSONObject.fromObject(rtnToken); 
					    String token = j.getString("token");  
					    gb.setTokenIms(token);
					    gb.setTokenExpDate(gb.getSysDate());		
						gb.showLogMessage("I","IMS connect request token ok. Current Token = " + token);
						blResult = true;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						if ("1".equals(imsIndicate)) {
							gb.setsgImsIndicate("2");
						}
						else {
							gb.setsgImsIndicate("1");
						}
						gb.showLogMessage("E","IMS Connect request token failed=" + e);
//						e.printStackTrace();
					}
			}
			else {
//				gb.showLogMessage("I","IMS connect token no expire");
				String applJson = "application/json";
				blResult = true;
				String imsIndicate = gb.getsgImsIndicate();
			    String rtnToken = "";
			    String getImsUrl = gb.getSgImsTokenReqUrl() + "actuator/health"; 
			    if ("2".equals(imsIndicate)) {
			    	getImsUrl = gb.getSgImsVdTxnUrl() + "actuator/health";
			    }
//			    getImsUrl = getImsUrl.replaceAll("https:", "http:");
//			    getImsUrl = getImsUrl.replaceAll(":8143", ":9080");

				gb.showLogMessage("I","IMS connect token no expire and health check indicate/url=" + imsIndicate + " / "+getImsUrl);
				try {
					rtnToken = HpeUtil.curlGet(getImsUrl, applJson);
					if ("ERROR".equals(rtnToken) || "TIMEOUT".equals(rtnToken)) {
						gb.showLogMessage("E","IMS health check Connect failed=" + rtnToken);
						if ("1".equals(imsIndicate)) {
							gb.setsgImsIndicate("2");
						}
						else {
							gb.setsgImsIndicate("1");
						}
						return false;
					}
				    gb.showLogMessage("D","curlToken health check = "+rtnToken);
				    JSONObject j = JSONObject.fromObject(rtnToken); 
				    String status = j.getString("status");  
				    if (!"UP".equals(status)) {
				    	gb.showLogMessage("I","IMS connect request health check faild status = "+status);
						if ("1".equals(imsIndicate)) {
							gb.setsgImsIndicate("2");
						}
						else {
							gb.setsgImsIndicate("1");
						}
						blResult = false;
				    }
				    else {
				    	gb.showLogMessage("I","IMS connect request health check ok status = "+status);
				    	blResult = true;
				    }
				} catch (Exception e) {
					if ("1".equals(imsIndicate)) {
						gb.setsgImsIndicate("2");
					}
					else {
						gb.setsgImsIndicate("1");
					}
					gb.showLogMessage("E","IMS Connect request health check failed=" + e);
				}
			}
			return blResult;
		}
		
		private boolean checkAcerStatus() {
			boolean blResult = false;
			try (
				//send Test TXN To Acer
				Socket lSocketToAcer  =  new  Socket(gb.getAcerHost() ,  gb.getAcerPort());
				BufferedInputStream lAcerInputStream =  new  BufferedInputStream(lSocketToAcer.getInputStream());
				BufferedOutputStream  lAcerOutputStream = new  BufferedOutputStream(lSocketToAcer.getOutputStream()); ) {
				lSocketToAcer.setSoTimeout(3*1000);//設定 timeout == 3 secs
				byte[] lDataByteAry = HpeUtil.hex2Byte("054160000102010200703c058000C100021640575900100142010000000000000010000000010946010109121200120800C03636323131313131313130303030303132333435363738009538383838383838383838202020202020202020202020202020202020303030303031202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020200371000000000000000000000000000000000000ff02223031202020204e202020202020202020000000000000000000000000000000000000000000000000000000000000202020204e202020202020202020000000000000000000000000000000000000000000000000000000000000202020204e202020202020202020000000000000000000000000000000000000000000000000000000000000202020204e202020202020202020000000000000000000000000000000000000000000000000000000000000202020204e2020202020202020200000000000000000000000000000000000000000000000000000000000000102303243302020000000000010202020200000000000002020202000000000000020202020000000000000202020200000000000002020202000000000000020202020000000000000202020200000000000002020202000000000000020202020000000000000002230330000000000000000000000000000ffffffffffff");
//				gb.showLogMessage("I","acer_isoData HEX= "+HpeUtil.byte2HexStr(lDataByteAry));
//				gb.showLogMessage("I","acer_isoData = "+new String(lDataByteAry));
				HpeUtil.writeData2Acer(lAcerOutputStream, lDataByteAry, 543);	
				
				//receive Redeem TXN From Acer
				boolean blSucFlag = false;
				String slResultOfRecvAcer = "";
				boolean blKeepReceiving = true;
				byte[] lRecvByteAry = null;

				while (blKeepReceiving) {
					blSucFlag = false;
					lRecvByteAry = HpeUtil.readDataFromAcer(lAcerInputStream);
					slResultOfRecvAcer = HpeUtil.byte2HexStr(lRecvByteAry);
					if (!"".equals(slResultOfRecvAcer)) { 
//						gb.showLogMessage("I","acer_isoData HEX receive= "+ slResultOfRecvAcer);
//						gb.showLogMessage("I","acer_isoData receive= "+new String(lRecvByteAry));
						blSucFlag = true;
					}
					else {
						blSucFlag = false;
					}						
					blKeepReceiving = false;
				}
				lSocketToAcer.close();

				if (!blSucFlag) {/** time out **/
					/** 恢復成原值 **/
					blResult = false;
				}
				else {

					blResult = true;
				}
		} catch (Exception e) {
			gb.showLogMessage("I","ACER SOCKET CONNECT FAILED = "+e);
			blResult = false;
		}
			return blResult;
	}
		//public class ConnectControl extends Thread
		public void run() {  
		
			boolean blInitHsm  = true;
			boolean blInitIms  = true;
			boolean blInitAcer = true;

			try{
				while (true) {
					//CHECK HSM CONNECTION STATUS
					if (blInitHsm) {
						if (checkHsmStatus()) {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to HSM Successful");
							blInitHsm = false;
						}
						else {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to HSM Failed");
						}
					}
					else {
						checkHsmStatus();	
					}
					
					//CHECK IMS CONNECTIONS & TOKEN STATUS
					if (blInitIms) {
						if (checkImsToken()) {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to IMS Successful");
							blInitIms = false;
						}
						else {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to IMS Failed");
						}
					}
					else {
						checkImsToken();	
					}
					
					//CHECK ACER CONNECTION STATUS
					if (blInitAcer && gb.isEnableAcerRedemption()) {
						if (checkAcerStatus()) {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to ACER Successful");
							blInitAcer = false;
						}
						else {
							gb.showLogMessage("F",gb.getSystemName()+" Connect to ACER Failed");
						}
					}

					
					Thread.sleep(sleepSec30*1000);
					
					//V1.00.04 授權連線異常偵測時發送簡訊通知維護人員
					checkIfStopMainProc();
				}

			} // End try
			catch ( Exception ex ) {
				gb.expHandle (ex, false);  
			}
		}
	}
	
//	public void finalize() {
//		gb = null;  
//
//	}
} // END OF AuthService

