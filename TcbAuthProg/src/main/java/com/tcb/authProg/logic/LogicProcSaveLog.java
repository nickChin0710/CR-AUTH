/**
 * 授權邏輯查核-授權LOG處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-授權LOG處理                       *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/10/23  V1.00.56  Kevin       避免因特店資料異常時，導致授權系統異常的處理排除        *
 * 2023/11/20  V1.00.58  Kevin       TXLOG相關欄位整理                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.bil.BilO105;
import com.tcb.authProg.bil.BilO205;
import com.tcb.authProg.bil.InstallmentTxData;
import com.tcb.authProg.bil.RedeemTxData;
import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcSaveLog extends AuthLogic {
	
	public LogicProcSaveLog(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcSaveLog : started");

	}
	
	/**
	 * 儲存各種log
	 * V1.00.42 授權系統與DB連線交易異常時的處理改善方式
	 * V1.00.48 P3程式碼整理(TXLOG相關欄位整理)
     * V1.00.56 避免因特店資料異常時，導致授權系統異常的處理排除
     * V1.00.58 TXLOG相關欄位整理
	 * @throws Exception if any exception occurred
	 */
	public void saveLog() throws Exception {
		ta.setAuthSeqno();
		//V1.00.48 P3程式碼整理(TXLOG相關欄位整理)		
		if ( ("WEB".equals(gGate.connType)) || ("S".equals(gGate.isoField[26].trim())) ) {
			if (gGate.isoField[7].length()>=10) {
				gGate.isoField[7] = gGate.isoField[7].substring(0, 4) + gGate.txTime; 
			}
			if (gGate.isoField[121].length()>=10)
				gGate.approveUser =  gGate.isoField[121].substring(0, 10);
			if (gGate.isoField[127].length()>=10)
				gGate.authUser =  gGate.isoField[127].substring(0, 10);
			if ("WEB".equals(gGate.connType))
				gGate.authUnit = "K"; //人工授權
			else {
				if (gGate.authUnit.length() == 0) {
					gGate.authUnit = "A"; //自動授權
				}
			}
		}
		if ("BATCH".equals(gGate.connType) && gb.durationTime(gGate.startTime) > 6 && "00".equals(gGate.isoField[39])) {
			gb.showLogMessage("E","BATCH AUTH process time out, ISOField[39]00=>98 reject !!! isoField[39] = "+gGate.isoField[39]+" ;cardNo="+gGate.cardNo+" ;AMT="+gGate.isoFiled4Value+" ;AuthNo="+gGate.authNo+" ;IsoRespCode="+gGate.sgIsoRespCode+" ;AuthSeqNo="+gGate.authSeqno);
			gGate.isoField[38] = "";
			gGate.isoField[39] = "98";
			gGate.sgIsoRespCode = gGate.isoField[39];
			gGate.cacuAmount = "N";
			gGate.cacuCash = "N";
			gGate.authNo = "";
			gGate.authRemark = "BATCH AUTH process time out, ISOField[39]00=>98 reject !!!";
		}
		if (!ta.insertAuthTxLog()) {
			gb.showLogMessage("E","insert txlog faild!!! isoField[39] = "+gGate.isoField[39]+" ;cardNo="+gGate.cardNo+" ;AMT="+gGate.isoFiled4Value+" ;AuthNo="+gGate.authNo+" ;IsoRespCode="+gGate.sgIsoRespCode+" ;AuthSeqNo="+gGate.authSeqno);
			if ( "00".equals(gGate.isoField[39]) && (gGate.isDebitCard) && (gGate.ifSendTransToIms) && (!gGate.nonPurchaseTxn) && (gGate.lockAmt > 0)) {
				gb.showLogMessage("E","AUTH SYSTEM ERROR!!! VD TXN need insert CCA_IMS_LOG for reversal = "+ gGate.vdImsLog );
				ta.insertImsEven(gGate.vdImsLog, true); //新增VD交易之IMS LOG，true表示需要異常沖正處裡
			}
			if ((gGate.isRedeemTx || gGate.isInstallmentTx) && "00".equals(gGate.isoField[39]) && "0100".equals(gGate.mesgType) && !gGate.rollbackP2) {
				gGate.oriAuthNo = gGate.isoField[38];
				if (gGate.isRedeemTx) {
					RedeemTxData lRedeemTxData = null;
					BilO105 lBilO105 = new BilO105(gGate, gb, ta, "00000002");
					lRedeemTxData = lBilO105.gRedeemTxData;
					if (lRedeemTxData != null) {
						gb.showLogMessage("D","AUTH SYSTEM ERROR!!! Redeem txn reversal resp = "+ lRedeemTxData.getRespCode() +
						          "Auht Remark = " + lRedeemTxData.getAuthRemark());
					}
					else {
						gb.showLogMessage("D","AUTH SYSTEM ERROR!!! Redeem txn reversal no response");
					}
				}
				else {
					InstallmentTxData lInstallmentTxData = null;
					BilO205 lBilO205 = new BilO205(gGate, gb, ta, "00000001");
					lInstallmentTxData = lBilO205.gInstallTxData;
					if (lInstallmentTxData != null) {
						gb.showLogMessage("D","AUTH SYSTEM ERROR!!! Installment txn reversal resp = "+ lInstallmentTxData.getRespCode() +
								          "Auht Remark = " + lInstallmentTxData.getAuthRemark());
					}
					else {
						gb.showLogMessage("D","AUTH SYSTEM ERROR!!! Installment txn reversal no response");
					}
				}
			}
			gGate.isoField[38] = "";
			gGate.isoField[39] = "96";
			gGate.sgIsoRespCode = gGate.isoField[39];
		}
		else {
			gGate.isInsertTxlog = true;
		}
	}

}
