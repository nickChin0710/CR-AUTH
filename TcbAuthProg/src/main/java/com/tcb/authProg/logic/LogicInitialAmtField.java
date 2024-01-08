/**
 * 授權邏輯查核-計算各種金額欄位處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-計算各種金額欄位處理                 *
 * 2022/09/07  V1.00.01  Kevin       預先授權完成，不需要帶入原交易金額                *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicInitialAmtField extends AuthLogic {
	
	public LogicInitialAmtField(AuthGlobalParm gb,AuthTxnGate gate) {
		this.gb    = gb;
		this.gGate  = gate;
		
		gb.showLogMessage("I","LogicInitialAmtField : started");

	}
	
	/**
	 * 依據交易類型，計算各種金額欄位，以便交易之用
	 * @throws Exception if any exception occurred
	 */
	public void initAmtField4TxLog()  throws Exception {

		////依據txlog 金額欄位規則_20160901.xlsx 之說明，填入值，準備塞入 txLog
		//kevin:配合財金原始金額放在isoFiled[4]，修改G_Gate.oriAmount = G_Gate.isoFiled[4];
		if (  (gGate.purchAdjust)  || (gGate.cashAdjust)  ) {
			gGate.ntAmt = gGate.adjustAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = gGate.adjustAmount;
			gGate.bankTxAmt = gGate.lockAmt;

		}
		else if (gGate.refundAdjust) {
			gGate.ntAmt = gGate.adjustAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = gGate.adjustAmount;
			gGate.bankTxAmt = 0;

		}
		else if (gGate.normalPurch) {

			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = 0;
			gGate.bankTxAmt = gGate.lockAmt;

		}
		else if (gGate.cashAdvance) {

			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			if (HpeUtil.isAmount(gGate.isoField[4])) {
				gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			}
			else {
				gGate.oriAmount = 0;
			}
			gGate.replAmt = 0;
			gGate.bankTxAmt = gGate.lockAmt;

		}

		else if (gGate.refund) {
			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = 0;
			gGate.bankTxAmt = 0;

		}
		else if (gGate.preAuth) {
			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
			gGate.oriAmount = 0;
			gGate.replAmt = 0;
			gGate.bankTxAmt = gGate.lockAmt;

		}

		else if (gGate.preAuthComp) {
			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
			//V1.00.01 預先授權完成，不需要帶入原交易金額
			gGate.replAmt = gGate.isoFiled4Value;
			gGate.bankTxAmt = gGate.lockAmt;

		}

		else if (gGate.reversalTrans) {
			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
//			G_Gate.oriAmount =G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = 0;
			gGate.bankTxAmt = gGate.lockAmt; 

		}
		else if (gGate.nonPurchaseTxn) {
			gGate.oriAmount = 0;
			gGate.replAmt = 0;
			gGate.bankTxAmt = 0;
		}
		else  {
			gGate.ntAmt = gGate.isoFiled4Value;//G_Gate.transAmount;
//			G_Gate.oriAmount = G_Gate.isoFiled4Value;
			gGate.oriAmount = Double.parseDouble(gGate.isoField[4]) / 100;
			gGate.replAmt = 0;
			gGate.bankTxAmt = gGate.lockAmt;

		}

	}

}
