/**
 * 授權邏輯查核-票證相關資料更新處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-票證相關資料更新處理                 *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicUpdateTicketInfo extends AuthLogic {
	
	public LogicUpdateTicketInfo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicUpdateTicketInfo : started");

	}
	
	
	public void processTicketInfo() throws Exception {
		
		if (gGate.easyAutoloadFlag) {
			ta.updateTscCard(true);
		} 
		if (gGate.easyAutoload || gGate.easyStandIn) {
			ta.updateTscCard(false);
		}
		if (gGate.ipassAutoload || gGate.ipassStandIn){
			ta.updateIpsCard(false);
		}
		if (gGate.icashAutoload || gGate.icashStandIn){
			ta.updateIchCard(false);
		}
		
		return;

	}

}
