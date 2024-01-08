/**
 * 授權邏輯查核-網路賭博交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-網路賭博交易檢核處理                 *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckEcGambleTrans extends AuthLogic {
	
	public LogicCheckEcGambleTrans(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckEcGambleTrans : started");

	}
	
	/**
	 * 網路賭博交易檢核
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean checkEcGambleTrans() throws Exception{
		
		boolean blResult = true;
		if ((gGate.ecGamble) && (gGate.ecTrans) ){ ////賭博+網路交易 =>  直接拒絕交易
			//G_Ta.getAndSetErrorCode("ERR14"); //confirmed 0825
			ta.getAndSetErrorCode("Q1");
			blResult=false;
		}
		return blResult;
	}

}
