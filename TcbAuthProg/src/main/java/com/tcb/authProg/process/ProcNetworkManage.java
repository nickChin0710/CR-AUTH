/**
 * Proc 處理各種 0800 交易
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       Proc 處理各種 0800 交易                     *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.logic.AuthLogicCancel;
import com.tcb.authProg.logic.LogicCheckRealCardNo;
import com.tcb.authProg.logic.LogicProcNetworkManagement;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcNetworkManage extends AuthProcess {

	public ProcNetworkManage(AuthGlobalParm gb, AuthTxnGate gate) {
			this.gGb    = gb;
			this.gGate  = gate;
			
			gb.showLogMessage("I","ProcNetworkManage : started");

	}
	/**
	 * 
	 * 處理各種 0800 交易
	 * @throws  Exception if any exception occurred
	 */
	public void networkManagement() throws Exception {

		String  message="";

		gTa   = new TableAccess(gGb,gGate);
//		aulg = new AuthLogic(gGb,gGate,gTa);
//		gGate.parmHash = gTa.getBufferHash();
		LogicProcNetworkManagement logicNet = new LogicProcNetworkManagement(gGb, gGate, gTa);




		int i = Integer.parseInt(gGate.isoField[70]);

		/*
		if ( i >= 161 && i <= 164 ) {


			G_Ta   = new TableAccess(G_Gb,G_Gate);
			aulg = new AuthLogic(G_Gb,G_Gate,G_Ta,shm);
			G_Gate.parmHash = G_Ta.getBufferHash();
		}
		 */

		switch (i) {

		case 1    : 
			message = "Logon  message : ";
			logicNet.responseLogon();
			break;
		case 2    : 
			message = "Logoff message : ";
			logicNet.responseLogoff();
			break;
		case 061  : 
			message = "FISC Logon  message : ";
			logicNet.responseEchoTest();
			break;
		case 062  : 
			message = "FISC Logoff  message : ";
			logicNet.responseEchoTest();
			break;
		case 161  : 
			message = "Change key message : ";
			logicNet.changeWorkingKey();
			break;
		case 162  : 
			message = "New key message : ";
			logicNet.newWorkingKey();
			break;
		case 163  : 
			message = "Repeat key message : ";
			logicNet.repeatWorkingKey();
			break;
		case 164  : 
			message = "Verify key message : ";
			logicNet.verifyWorkingKey();
			break;
		case 201  : 
			message = "Cutover    message : ";
			//shm.loadTableData();
			//Howard: no need to do anything, just reply to NCCC
			break;
		case 301  : 
			message = "NCCC Echo-test  message : ";
			logicNet.responseEchoTest();
			break;
		case 270  : 
			message = "FISC Echo-test  message : ";
			logicNet.responseEchoTest();
			break;
		default   : break;

		}

		gGb.showLogMessage("I",gGate.mesgType + ":"+message + ",isoField70:" + gGate.isoField[70]+" channel "+gGate.chanNum);
		return;
	}
	/**
	 * NETWORK 回覆訊息 控制
	 * 處理各種 0810 交易
	 * @throws  Exception if any exception occurred
	 */
	public void networkResponse() throws Exception {

//		gGb.showLogMessage("D", "RESPONSE gGate.isoField[11] = "+gGate.isoField[11]);
		String cvtPnt = (String)gGb.getFiscRequest().get(gGate.isoField[11]);
//		System.out.println("RESPONSE cvtPnt = "+cvtPnt);
//		gGb.showLogMessage("D", "RESPONSE cvtPnt = "+cvtPnt);
		if ( cvtPnt == null ) {
			gGb.showLogMessage("I","fisc TIME-OUT!"); 
			return; 
		}
//		gGb.showLogMessage("D", "kevin test gGate.isoField[70] = "+ gGate.isoField[70]);
		String respData = gGate.isoField[39]+"@"+gGate.isoField[70]+"@"+"N@";
		gGb.getFiscResponse().put(gGate.isoField[11],respData);

		/* FISC 回覆訊息 通知處理完成 */
		int k = Integer.parseInt(cvtPnt);
//		gGb.showLogMessage("D", "RESPONSE k = "+k);

		gGb.showLogMessage("D","Fiec networkResponse("+gGate.chanNum+")=>cvtPnt="+cvtPnt+";gGate.isoField[11]="+gGate.isoField[11]+";gGate.isoField[70]="+ gGate.isoField[70]+";gGate.isoField[39]="+gGate.isoField[39]);

//		gGb.showLogMessage("D", "RESPONSE gGb.getDoneLock()[k] = "+gGb.getDoneLock()[k]);
		synchronized (gGb.getDoneLock()[k]) {
			gGb.getDoneLock()[k].notify();
		}
		return;
	}
}
