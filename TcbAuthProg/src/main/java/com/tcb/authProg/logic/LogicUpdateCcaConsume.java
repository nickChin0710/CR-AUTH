/**
 * 授權邏輯查核-消費累積資料更新處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-消費累積資料更新處理                 *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicUpdateCcaConsume extends AuthLogic {

	public LogicUpdateCcaConsume(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicUpdateCcaConsume : started");

	}
	
	public void processCcaConsume() throws Exception {
		gb.showLogMessage("I","processCcaConsume : started");

		ta.setValue("P_SEQNO", ta.getPSeqNo());
		ta.setValue("LAST_CONSUME_DATE", gb.getSysDate());
		if (gGate.isSupCard) {
			ta.setValue("ID_P_SEQNO",ta.getValue("ID_P_SEQNO"));
			ta.setValue("SUP_FLAG", "1"); //SUP_FLAG => 0:正卡 1:附卡
		}
		else {
			ta.setValue("ID_P_SEQNO",ta.getValue("ID_P_SEQNO"));
			ta.setValue("SUP_FLAG", "0");            //SUP_FLAG => 0:正卡 1:附卡
		}






		// 清 月 累積消費資料
		if ("".equals(gGate.lastTxDate))
			gGate.resetCcaConsumeMonthData=true;
		else if ( (gGate.lastTxDate.length()>=6) &&  (!gGate.lastTxDate.substring(0,6).equals(gb.getSysDate().substring(0,6))) )  {
			gGate.resetCcaConsumeMonthData=true;
		}
		else
			gGate.resetCcaConsumeMonthData=false;

		if ("".equals(gGate.lastTxDate))
			gGate.resetCcaConsumeDayData=true;
		else if ( ( !gGate.lastTxDate.equals(gb.getSysDate()) )){       // 清 日 累積消費資料
			gGate.resetCcaConsumeDayData=true;
		}
		else {
			gGate.resetCcaConsumeDayData=false;
		}
		// 更新當筆消費累積資料

		if (!gGate.convertError) {
			ta.updateCcaConsume();
		}
		
		return;

	}

}
