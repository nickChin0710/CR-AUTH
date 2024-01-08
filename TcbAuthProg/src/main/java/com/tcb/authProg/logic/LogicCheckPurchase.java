/**
 * 授權邏輯查核-一般授權交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-一般授權交易檢核處理                 *
 * 2022/04/13  V1.00.01  Kevin       NCCC保發交易需檢核查卡人ID                    *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckPurchase extends AuthLogic {

	public LogicCheckPurchase(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckPurchase : started");

	}
	
	/**
	 * 一般授權交易檢核作業
	 * @return 如果檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean purchaseChecking() throws Exception {
		if (ifIgnoreProcess2())
			return true;

		gb.showLogMessage("I","purchaseChecking : started");
		gb.showLogMessage("I","iso 39-1 :" + gGate.isoField[39]);

		boolean blResult = true;

    	//down, 所有交易身分驗證檢核, Kevin:將搬到 processAcctVerify() 中
		String slMerchantNo = "";

		if (gGate.isoField[127].length()>=32)
			slMerchantNo = gGate.isoField[127].substring(21,31).trim();

		if ("".equals(slMerchantNo))
			slMerchantNo = gGate.merchantNo;

		String slTmpCardHolderId = getIdFromIsoString();
		if (slMerchantNo.length()>0) {
			if (ta.selectCcaVoice(slMerchantNo)) {
				//如果特店(merchant no )為NCCC語音特店控制檔，需要檢核身分證號
				if (!slTmpCardHolderId.equals(ta.getValue("CrdIdNoIdNo"))) {
					ta.getAndSetErrorCode("CM");
					return false;
				}
			}
		}
		gb.showLogMessage("I","iso 39-2 : "+ gGate.isoField[39]);
		//down,	卡繳稅檢核		
		if (gGate.taxTrans) { //卡繳稅檢核
			if (slTmpCardHolderId.length()>0) {
				if (!HpeUtil.isFirstCharLatter(slTmpCardHolderId)){ //若第一個字母不是A~Z
					//ta.getAndSetErrorCode("ERR21");
					ta.getAndSetErrorCode("D9");
					return false;
				}

				if (!slTmpCardHolderId.equals(ta.getValue("CrdIdNoIdNo"))) {
					//ta.getAndSetErrorCode("ERR21");
					ta.getAndSetErrorCode("D9");
					return false;
				}
			}
		}
		//up,卡繳稅檢核
		gb.showLogMessage("I","iso 39-3 : "+ gGate.isoField[39]);
		//down, Mail Order Checking /CardHolder ID
		boolean blCheckMailOrder = false; // Proc =>郵購交易暫不檢核ID
		if ((gGate.mailOrder) && (blCheckMailOrder) ){ //郵購交易暫不檢核ID 
			if (slTmpCardHolderId.length()>0) {
				if (!slTmpCardHolderId.equals(ta.getValue("CrdIdNoIdNo"))) {
					//ta.getAndSetErrorCode("ERR54");
					ta.getAndSetErrorCode("CM");
					return false;
				}
			}
			String slIsoBirthdayCode = getPasswdFromIsoString();
			if (!ta.getValue("CrdIdNoBirthday").equals(slIsoBirthdayCode)) {
				//ta.getAndSetErrorCode("ERR55");
				ta.getAndSetErrorCode("B9");
				return false;
			}
		}

		gb.showLogMessage("I","iso 39-4 : "+ gGate.isoField[39]);

		//V1.00.01 NCCC保發交易需檢核查卡人ID <down>
		if ("FISC".equals(gGate.connType) && gGate.f58T68IdCheckType.length() <= 0 && gGate.f58T67Id.length()> 0) {
			if (!gGate.f58T67Id.equals(ta.getValue("CrdIdNoIdNo"))) {
				gb.showLogMessage("I","NCCC Insurance TXN for ID check = "+gGate.f58T67Id+" not equals CH ID = "+ ta.getValue("CrdIdNoIdNo"));
				ta.getAndSetErrorCode("CM");
				return false;
			}
		}
		
		gb.showLogMessage("I","iso 39-5 : "+ gGate.isoField[39]);
		//V1.00.01 NCCC保發交易需檢核查卡人ID <up>
		
		if ((gGate.isInstallmentTx) || (gGate.isRedeemTx)) {
			if (gGate.purchAdjust) {//一般調整交易
				return true;
			}
		}

		if (gGate.preAuthComp) {
			updatePreAuthData("N");
		}
		
		return blResult;
	}    

}
