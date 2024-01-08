/**
 * 授權邏輯查核-原始交易授權碼檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-原始交易授權碼檢核處理               *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckOriAuthNo extends AuthLogic {
	
	public LogicCheckOriAuthNo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckOriAuthNo : started");

	}
	
	
	/**
	 * 原始交易授權碼檢核
	 * @return 如果要忽略交易流程，return true；否則return false
	 */
	public boolean checkOriAuthNo() throws Exception{
		boolean blResult = true;
		//票證交易並不會提供原始交易授權碼，所以不需檢查
		if (gGate.ticketTxn)
			return blResult;
		//如果一般交易調整、預借現金調整、退貨調整、preAuth Comp、沖銷交易 的oriAuthNo (原始授權碼) 是空的，則拒絕交易
		//kevin:排除沖銷交易
		if ( (gGate.purchAdjust)  || (gGate.cashAdjust) || (gGate.refundAdjust) || (gGate.preAuthComp) )  {
		if ("".equals(gGate.oriAuthNo.trim())) {
				ta.getAndSetErrorCode("A2");
				blResult=false;
			}
		}
		return blResult;
	}
}
