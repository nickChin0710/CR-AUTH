/**
 * 授權邏輯查核-補登通知交易處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-補登通知交易處理                    *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcForcePosting extends AuthLogic {

	public LogicProcForcePosting(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcForcePosting : started");

	}
	
	
	//-補登交易 force post
	/**
	 * 授權補登做業處理
	 * @return 如果正確處理完成，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean processForcePosting() throws Exception {
		gb.showLogMessage("I","processForcePost : started");
		
		if (("").equals(gGate.oriAuthNo.trim())) {
			gGate.isoField[38] = "A";
			gGate.authNo = "A";
		}
		else {
			gGate.isoField[38] = gGate.oriAuthNo;
			gGate.authNo = gGate.oriAuthNo;
		}

		return true;
	}

}
