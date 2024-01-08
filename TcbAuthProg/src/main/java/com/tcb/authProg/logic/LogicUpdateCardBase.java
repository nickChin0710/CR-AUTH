/**
 * 授權邏輯查核-更新卡片資料處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-更新卡片資料處理                    *
 * 2022/07/07  V1.00.02	 Kevin       調整特指戶判斷條件                            *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicUpdateCardBase extends AuthLogic {

	public LogicUpdateCardBase(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicUpdateCardBase : started");

	}
	
	
	// 準備 更新 CCA_CARD_BASE
	/**
	 * 更新卡片資料檔
	 * @throws Exception if any exception occurred
	 */
	public void processCardBase() throws Exception{
		if( (gGate.normalPurch) || (gGate.cashAdvance) || (gGate.mailOrder) || (gGate.preAuth) || (gGate.preAuthComp) ) {
			gGate.cardBaseLastAmt = Double.toString(gGate.isoFiled4Value);
			if (!gGate.forcePosting) {
				gGate.cardBaseTotAmtDay +=  gGate.isoFiled4Value;
				gGate.cardBaseTotCntDay = gGate.cardBaseTotCntDay + 1;

			}

		}
		else if ((gGate.purchAdjust) || (gGate.cashAdjust) || (gGate.refundAdjust) ) {

			gGate.cardBaseLastAmt = Double.toString(gGate.adjustAmount);
			gGate.cardBaseTotAmtDay =  (int)(gGate.cardBaseTotAmtDay - gGate.isoFiled4Value);

			gGate.cardBaseTotAmtDay +=  gGate.adjustAmount;

			if (gGate.cardBaseTotAmtDay<0) {
				gGate.cardBaseTotAmtDay=0;
			}
			if (gGate.adjustAmount==0) {
				gGate.cardBaseTotCntDay = gGate.cardBaseTotCntDay -1;
			}

			if (gGate.cardBaseTotCntDay<0) {
				gGate.cardBaseTotCntDay=0;

			}

		}
		gGate.cardBaseLastConsumeDate = gGate.txDate;
		gGate.cardBaseLastConsumeTime = gGate.txTime;
		gGate.cardBaseLastAuthCode = gGate.isoField[38];
		gGate.cardBaseLastCurrency = gGate.isoField[49];
		gGate.cardBaseLastCountry  = gGate.merchantCountry;

		if ("WEB".equals(gGate.connType)) /*人工授權, 不用新增*/
			return ;

		ta.updateCardBase();
		return ;


	}

}
