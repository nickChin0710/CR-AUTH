/**
 * Proc 授權交易完成後，更新table 相關欄位 
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
 * 2021/02/08  V1.00.00  Kevin       Proc 授權交易完成後，更新table 相關欄位         *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.logic.AuthLogic;
import com.tcb.authProg.logic.LogicUpdateCardBase;
import com.tcb.authProg.logic.LogicUpdateCcaConsume;
import com.tcb.authProg.logic.LogicUpdateTicketInfo;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcUpdateRelatedFile extends AuthProcess {

	public ProcUpdateRelatedFile(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gb.showLogMessage("I","ProcUpdateRelatedFile : started");

	}
	
	public void updateRelatedFile()  throws Exception{
		//更新交易紀錄
		//proc is purchase_update_file()

		gTa.updateRiskTradeInfo();//*更新卡戶當月之消費總金額*/
		if (gGate.ifCredit) {
			LogicUpdateCcaConsume logicConsume = new LogicUpdateCcaConsume(gGb, gGate, gTa);
			logicConsume.processCcaConsume();
			LogicUpdateCardBase logicCardBase = new LogicUpdateCardBase(gGb, gGate, gTa);
			logicCardBase.processCardBase();				//準備  更新 CCA_CARD_BASE

			/*
			//**** 佔額度才須UPDATE **
			if (G_Gate.comboTrade) {
				aulg.processCcaConsume(); //準備  更新 CCA_CONSUME

			}
			else {
				aulg.processCardAcct();              // 準備 更新 CCA_CARD_ACCT

				aulg.processCardBase();				//準備  更新 CCA_CARD_BASE

			}
			 */
		}
		LogicUpdateTicketInfo logicTicket = new LogicUpdateTicketInfo(gGb, gGate, gTa);
		logicTicket.processTicketInfo();   //三大票證悠遊卡、一卡通、愛金卡更新資料

	}
}
