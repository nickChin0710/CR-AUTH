/**
 * 授權邏輯查核-授權交易佔額變數值處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-授權交易佔額變數值處理               *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicInitialTransValuePerTrade extends AuthLogic {
	
	public LogicInitialTransValuePerTrade(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicInitialTransValuePerTrade : started");

	}
	
	
	/**
	 * 初始化交易佔額變數值
	 * @throws Exception if any exception occurred
	 */
	public void initTransValuePerTrade() throws Exception {
//		gb.showLogMessage("D","gGate.normalPurch = "+ gGate.normalPurch);
//		gb.showLogMessage("D","gGate.mailOrder = "+ gGate.mailOrder);
//		gb.showLogMessage("D","gGate.preAuth = " + gGate.preAuth);
//		gb.showLogMessage("D","gGate.preAuthComp = "+ gGate.preAuthComp);
//		gb.showLogMessage("D","gGate.cashAdvance = "+ gGate.cashAdvance);
//		gb.showLogMessage("D","gGate.cashAdjust = "+ gGate.cashAdjust);
//		gb.showLogMessage("D","gGate.refundAdjust = "+ gGate.refundAdjust);
//		gb.showLogMessage("D","gGate.reversalTrans = "+ gGate.reversalTrans);

		if ( "00".equals(gGate.isoField[39] )) {
			if ((gGate.normalPurch)  ||
					(gGate.mailOrder) || (gGate.preAuth) || 
					(gGate.preAuthComp)){
				updateCacuData("Y", "N");
			}
			else if ( (gGate.cashAdvance)) {
					
				updateCacuData("Y", "Y");
			}

			else if (gGate.cashAdjust) {
				if(gGate.adjustAmount==0)
					updateCacuData("N", "N");
				else {
					if ("C".equals(gGate.mccRiskAmountRule))
						updateCacuData("Y", "Y");
					else
						updateCacuData("Y", "N");
				}
			}
			
			else if (gGate.refundAdjust) {
				if ((gGate.isoFiled4Value-gGate.adjustAmount)==0) {
					updateCacuData("N", "N");	
				}
				else {
					if ("F".equals(gGate.mccRiskAmountRule))
						updateCacuData("Y", "Y");
					else
						updateCacuData("Y", "N");
				}
			}
			//kevin:沖正交易、退貨交易、非購貨交易、金額為零，本身並不包含在消費累計上
			if (gGate.reversalTrans) {
				updateCacuData("N", "N");	
			}
			else if (gGate.refund) {
				updateCacuData("N", "N");
			}
			else if (gGate.nonPurchaseTxn) {
				updateCacuData("N", "N");
			} 
			else if (gGate.isoFiled4Value == 0) {
				updateCacuData("N", "N");
			} 
		}
		else {
			//交易失敗
			updateCacuData("N", "N");
		}
	}
}
