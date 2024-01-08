/**
 * 授權邏輯查核-HCE交易的流程與檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-HCE交易的流程與檢核處理              *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcHceChk extends AuthLogic {
	
	public LogicProcHceChk(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcHceChk : started");

	}
	
	
	/* HCE check*/
	/**
	 * handle HCE card 交易的處理流程與檢核
	 * @return 如果正常處理完成return true，否則return false
	 * @throws Exception if any exception
	 */
	public boolean processHceChk() throws Exception {
		//kevin:設定所有HCE TPAN相關信息
		if (gGate.f58T32.length() > 0) {
			if (HpeUtil.isNumberString(gGate.f58T32.substring(0, 2))) {
				int snLen = Integer.parseInt(gGate.f58T32.substring(0, 2));
				snLen = snLen + 2;
				String sltpanTrack   = gGate.f58T32.substring(2, snLen);
				gGate.tpanTicketNo = sltpanTrack.substring(0, 16);
				snLen = snLen + 11; //TSP ID(無使用)
				gGate.tpanPinResult  = gGate.f58T32.substring(snLen, snLen + 1);
				snLen = snLen + 1;
				gGate.tpanAcResult   = gGate.f58T32.substring(snLen, snLen + 1);
				snLen = snLen + 1;
				gGate.tpanFraudChk   = gGate.f58T32.substring(snLen, snLen + 2);
				snLen = snLen + 2;
				gGate.tpanReasonCode = gGate.f58T32.substring(snLen, snLen + 2);
			}

			gb.showLogMessage("D","gGate.tpanAcResult===" + gGate.tpanAcResult + "===");

			if ("1".equals(gGate.tpanPinResult)) {
				ta.getAndSetErrorCode("1E");//HCE mPIN verify error
				return false;
			}
			if ("1".equals(gGate.tpanAcResult)) {
				ta.getAndSetErrorCode("1F");//HCE ARQC verify error
				return false;
			}
			else if ("3".equals(gGate.tpanAcResult)) {
				ta.getAndSetErrorCode("1G");//HCE CVC3 verify error
				return false;
			}
			if ("05".equals(gGate.tpanFraudChk) || "11".equals(gGate.tpanFraudChk)) {
				ta.getAndSetErrorCode("1H");//HCE Fraud check error
				return false;
			}
			if (ta.selectHceCard()) {
				if (!"0".equals(ta.getValue("HceCardStatusCode"))) {
					ta.getAndSetErrorCode("1I");//HCE card status check error
					return false;
				}
			}
		}
		return true;
	}

}
