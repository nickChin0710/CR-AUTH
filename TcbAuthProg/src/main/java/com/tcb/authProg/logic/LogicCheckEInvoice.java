/**
 * 授權邏輯查核-電子發票交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-電子發票交易檢核處理                 *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckEInvoice extends AuthLogic {

	public LogicCheckEInvoice(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckEInvoice : started");

	}
	
	/**
	 * 電子發票 交易檢核
	 * @return 如果是電子發票，而且身分證號與生日比對失敗，則return false，否則return true
	 * @throws Exception if any exception occurred
	 */
	public boolean checkEInvoice() throws Exception {
		
		//kevin:新增FISC處理，在源頭就決定G_Gate.isEInvoice
		boolean blResult = true;
		if ((("012".equals(gGate.isoField[22].substring(0, 3)))) 
				&& (gGate.isoFiled4Value==1)  
				&& ("72000710".equals(gGate.isoField[41].substring(0, 8))) 
				&& ("0122901213     ".equals(gGate.isoField[42].substring(0, 15)))){
			gGate.isEInvoice = true;
		}
		if (gGate.isEInvoice) {
			String slIdFromIso = getIdFromIsoString();
			String slIdFromDb = ta.getValue("CrdIdNoIdNo");
			String slBirthdayFromDb = ta.getValue("CrdIdNoBirthday");
			if ((!slIdFromDb.substring(6,10).equals(slIdFromIso.substring(4,8))) || 
					(!slBirthdayFromDb.substring(4,8).equals(slIdFromIso.substring(0,4))) ) {
				ta.getAndSetErrorCode("D9");
				return false;
			}
		}
		//電子發票載具申請/取消處理
		/**
		 * 處理電子發票載具交易
		 * @return  如果電子發票載具成功，return true，否則return false
		 * @throws Exception if any exception occurred
		 */
		if (gGate.isReceiptCancel || gGate.isReceiptAdd) {
			gb.showLogMessage("I","processEIvoice : started");

			String slRequestType="A";//A: ADD
			if (gGate.isReceiptCancel)
				slRequestType="B";   //B: CANCEL
			
			ta.updateCardBase4Invoice(slRequestType);
		}

		return blResult;
	}

}
