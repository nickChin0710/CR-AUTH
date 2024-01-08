/**
 * 授權邏輯查核-交易前置檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-交易前置檢核處理                    *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckBeforeTrade extends AuthLogic {

	public LogicCheckBeforeTrade(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckBeforeTrade : started");

	}
	
	/**
	 * 交易前置檢核
	 * @return 如果交易前置檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkBeforeTrade() throws Exception {
		
		boolean blResult = true;
		if ( (gGate.normalPurch) || (gGate.cashAdvance) || (gGate.mailOrder) || (gGate.preAuth) || (gGate.preAuthComp) ) {
			if (( (gGate.tokenS8FraudChkRslt.length()>0) && (!"00".equals(gGate.tokenS8FraudChkRslt)) )  || 
					( (gGate.tokenS8AcVeryRslt.length()>0) && (!"00".equals(gGate.tokenS8AcVeryRslt)) ) ){
				gGate.isoField[39] ="96";
				blResult = false;

			}

		}

		return blResult;
	}


}
