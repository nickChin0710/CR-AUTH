/**
 * 授權使用Keep alive物件
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2023/01/12
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2023/01/12  V1.00.33  Kevin       財金國內掛卡連線新增Keepalive功能                *                                                                            * 
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


public class AuthKeepalive extends Thread{

	AuthGlobalParm gGb = null;
	Socket  authSd  =  null;
	int chanNum=0;


	public AuthKeepalive(AuthGlobalParm pGb, Socket authSd, int chanNum) {
		// TODO Auto-generated constructor stub
		gGb = pGb;
		this.authSd = authSd;
		this.chanNum  = chanNum;

	}
	
	private boolean sendKeepalive() {
		boolean blResult = true;
		try {
			
			Socket  authSocket= authSd;	
			byte[] pDataAry = HpeUtil.hex2Byte("0087303330303231353435323032303031353134373130312020204C20322020202020202020202020202030303030303030303030303032343132202020202020202020202020202020202020323937373634202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020");
			pDataAry = "".getBytes();

			BufferedOutputStream lOutStream = new BufferedOutputStream(authSocket.getOutputStream());

			lOutStream.write(pDataAry);
			lOutStream.flush();

			
		} catch (Exception e) {
			blResult = false;
		}
		
				
		return blResult;
	}
	 
	
	public void run() {
		try {
			boolean blInitProc = true;
			int reTry = 121; 
			while (true) {
					if (!"1".equals(gGb.getThreadConnectionNegStatusArray()[chanNum])) {
						reTry++;
						//up, process sign off 
						if (reTry > 120) { //每兩分鐘傳送一次keepalive
									if (!sendKeepalive()) { //send echo test
										gGb.showLogMessage("E", "The Keepalive test has no response. So stop the FISCNEG socket connection.");
										gGb.setConncetionStatus("FISCNEG", chanNum, "1");
										Thread.currentThread().interrupt();
									}
							reTry = 0;
						}
					}
					else {
						gGb.showLogMessage("E", "FISCNEG Socket is not connected, could not keepalive");
						gGb.setConncetionStatus("FISCNEG", chanNum, "1");
						Thread.currentThread().interrupt();
						
					}
					Thread.sleep(1*1000);
				}
			

		} catch (Exception e) {
			// TODO: handle exception
			gGb.showLogMessage("E", "Exception on AuthKeepalive.run() =>" + e.getMessage() + "===");
		}
	}

}
