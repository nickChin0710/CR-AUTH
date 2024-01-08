/**
 * 授權邏輯查核-授權交易之帳戶驗證處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-授權交易之帳戶驗證處理               *
 * 2022/03/28  V1.00.01  Kevin       電子化繳費稅處理平台-不須檢驗持卡人身分證ID         *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcAcctVerify extends AuthLogic {

	public LogicProcAcctVerify(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcAcctVerify : started");

	}
	
	/**
	 * 帳戶驗證
	 * @return 帳戶驗證成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean processAcctVerify() throws Exception {

		gb.showLogMessage("I","processAcctVerify : started");
		boolean blResult = true;

		String slId = getIdFromIsoString();
		
		if (gGate.f58T68IdCheckType.length() > 0) {
			switch (gGate.f58T68IdCheckType) {
			case "01" : //：聯卡中心 ON-US 繳費平台
				if (verifyId(slId)){
					gGate.isoField[39]="85";//都回85
				}
				else {
					ta.getAndSetErrorCode("D9");
					blResult = false;
				}    
				break;
			//V1.00.01 電子化繳費稅處理平台-不須檢驗持卡人身分證ID
			case "03" : //電子支付機構/金融機構
				if (verifyId(slId)){
					gGate.isoField[39]="85";//都回85
				}
				else {
					ta.getAndSetErrorCode("D9");
					blResult = false;
				}    
				break;		
			case "04" : //電子化繳費稅處理平台
				gGate.isoField[39]="85";//都回85
				break;		
			default   : break;					}					
		}
		else {
			if (slId.length() >0) {
				if (verifyId(slId)){
					gGate.isoField[39]="85";//都回85
				}
				else {
					ta.getAndSetErrorCode("D9");
					blResult = false;
				}    
			}
			else {
				gGate.isoField[39]="85";//沒有 Id 也回 85
				return blResult;
			}
		}

		return blResult;
	}

}
