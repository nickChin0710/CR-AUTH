/**
 * 授權邏輯查核-0800網路檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-0800網路檢核處理                   *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcNetworkManagement extends AuthLogic {

	public LogicProcNetworkManagement(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcNetworkManagement : started");

	}

	
	// 更新 WORKING KEY
	/**
	 * change working key
	 * @throws Exception if any exception occurred
	 */
	public void changeWorkingKey() throws Exception {
		if ( "0800".equals(gGate.mesgType) ) {

			gGate.isoField[12]  = "";
			gGate.isoField[13]  = "";
			gGate.isoField[15]  = "";
			gGate.isoField[38]  = "";
			gGate.isoField[39]  = "";
			gGate.isoField[48]  = "60YNYY2122S009012";
			gGate.keyDirection = gGate.isoField[53].substring(2,4);
			gGate.isoField[120] = "";
			gGate.isoField[123] = "CSM(MCL/RSI RCV/NCCC ORG/BC54 SVR/)";
			gb.showLogMessage("I","send changeWorkingKey request");
			gb.showLogMessage("I","ISO-53 send to nccc:" + gGate.isoField[53] +"**");
		}
		else {
			gb.showLogMessage("I","receive changeWorkingKey response:"+gGate.isoField[39]);
			gb.showLogMessage("I","ISO-53 get from nccc:" + gGate.isoField[53] +"**");
		}

		return;
	}

	// 新 WORKING KEY
	/**
	 * new working key
	 * @throws Exception if any exception occurred
	 */
	public void newWorkingKey() throws Exception {

		gb.showLogMessage("I","newWorkingKey : started");

		if ( gGate.isoField[53].length() < 5 ) {
			gGate.isoField[123] = "05"; 
			return; 
		}

		if ( gGate.isoField[123].length() < 42 ) {
			gGate.isoField[123] = "05"; 
			return; 
		}

		gGate.keyType      = gGate.isoField[53].substring(0,2);
		gGate.keyDirection = gGate.isoField[53].substring(2,4);
		gGate.checkValue   = gGate.isoField[120];
		if ( gGate.isoField[123].length() >= 65 ) {
			gGate.workingKeyZPK = gGate.isoField[123].substring(33,65); 
		}

		gb.showLogMessage("I","newWorkingKey response(ISO-53)    : " + gGate.isoField[53]);       
		gb.showLogMessage("I","ISO-123    : " +gGate.isoField[123]);
		gb.showLogMessage("I","checkValue from nccc: " + gGate.checkValue);
		gb.showLogMessage("I","IWK/ZCMK   : " + gGate.workingKeyZPK);

		// CALL HSM 更新 IWK
		//       G_HsmUtil.changeIWK();

		gGate.isoField[120] = gGate.checkValue;
		gGate.isoField[123] = "";

		gb.showLogMessage("I","newWorkingKey : ended");
		return;
	}

	// 重送 WORKING KEY
	/**
	 * repeat working key
	 * @throws Exception if any exception occurred
	 */
	public void repeatWorkingKey() throws Exception {

		if ( "0800".equals(gGate.mesgType) ) {

			gGate.isoField[12]  = "";
			gGate.isoField[13]  = "";
			gGate.isoField[15]  = "";
			gGate.isoField[38]  = "";
			gGate.isoField[39]  = "";
			gGate.isoField[48]  = "60YNYY2122S009012";
			gGate.keyDirection = gGate.isoField[53].substring(2,4);
			gGate.isoField[120] = "";
			gGate.isoField[123] = "";
			gb.showLogMessage("I","send repeatWorkingKey request");
			gb.showLogMessage("I","send to nccc -repeatWorkingKey ISO-120:" + gGate.isoField[120] + "***");
		}
		else {

			gGate.checkValue = gGate.isoField[120];
			if ( gGate.isoField[123].length() >= 65 ) {
				gGate.workingKeyZPK = gGate.isoField[123].substring(33,65); 
			}
			gb.showLogMessage("I","repeatWorkingKey response" +gGate.checkValue+" "+gGate.isoField[39]+" - "+gGate.workingKeyZPK); //
			gb.showLogMessage("I","check value from nccc --repeatWorkingKey response: " +gGate.checkValue + "--"); //            
			gb.showLogMessage("I","repeatWorkingKey response(ISO-53)    : " + gGate.isoField[53]);
			gb.showLogMessage("I","get from nccc -repeatWorkingKey ISO-120:" + gGate.isoField[120] + "***");            
		}

		return;
	}

	// 驗證 WORKING KEY
	/**
	 * veify working key
	 * @throws Exception if any exception occurred
	 */
	public void verifyWorkingKey() throws Exception {

		if ( "0800".equals(gGate.mesgType) ) {

			gGate.isoField[48]  = "60YNYY2122S009012";
			gGate.keyDirection = gGate.isoField[53].substring(2,4);
			//        G_HsmUtil.verifyIWK();
			gGate.isoField[120] = gGate.checkValue;
			gGate.isoField[123] = "";
			gb.showLogMessage("I","check value --send verifyWorkingKey request:"+gGate.checkValue + "---");
			gb.showLogMessage("I","send to nccc -verifyWorkingKey ISO-120:"+gGate.isoField[120] + "***");
		}
		else {
			gGate.checkValue = gGate.isoField[120];
			gb.showLogMessage("I","verifyWorkingKey checkValue from nccc: " +gGate.checkValue);
			gb.showLogMessage("I","verifyWorkingKey response(p39):" +gGate.isoField[39]);
			gb.showLogMessage("I","verifyWorkingKey response-check value:"+gGate.checkValue+"---");
			gb.showLogMessage("I","verifyWorkingKey response(ISO-53)    :"+gGate.isoField[53]);
			gb.showLogMessage("I","get from nccc -verifyWorkingKey ISO-120:" +gGate.isoField[120] + "***");

		}

		return;
	}


	public void responseEchoTest() throws Exception {


		gGate.isoField[39]  = "00";

		//G_Gate.isoField[70]  = "301";

		return;
	}

	public void responseLogon() throws Exception {

		gGate.isoField[39]  = "00";

		//G_Gate.isoField[70]  = "001";

		return;
	}

	public void responseLogoff() throws Exception {

		gGate.isoField[39]  = "00";

		//G_Gate.isoField[70]  = "001";

		return;
	}

}
