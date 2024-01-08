/**
 * 授權邏輯查核-代碼化交易處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-代碼化交易處理                     *
 * 2021/11/16  V1.00.01  Kevin       VISA 代碼化交易處理借用欄位顯示在授權LOG          *
 * 2023/03/15  V1.00.40  Kevin       P3檢查行動支付手機黑名單                        *
 * 2024/12/27  V1.00.64  Kevin       MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同                                                                           *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcTokenizationNotification extends AuthLogic {

	public LogicProcTokenizationNotification(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcTokenizationNotification : started");

	}
	
	
	/**
	 * 處理FISC代碼化交易 0620 0100
	 * V1.00.40 P3檢查行動支付手機黑名單
	 * V1.00.64 MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同
	 * @throws Exception
	 */
	public boolean procTokenizationNotification() throws Exception {
		//kevin:交易要寫auth_txlog
		boolean blResult = true;
		String slReasonCode = "";
		String slStatus ="0";
		if (!gGate.isTokenVAUT && !gGate.isTokenMAUT) {
			ta.insertOempayApplyData();
		}
		//V1.00.40 P3檢查行動支付手機黑名單
		if ((gGate.isTokenVTAR || gGate.isTokenMTAR) && ta.getValue("CrdIdNoCellPhone").length()>0) {
			if (!checkMobileBlack()) {
				blResult = false;
			}
		}
		//V1.00.64 MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同
		if ( gGate.isTokenMTAR && !"327".equals(gGate.walletIdentifier)) {
			if (!checkMobileLast4Digtal()) {
				gGate.isMobileLast4Digtal = false;
			}
		}
		if (gGate.bgTokenQ9FormatIsVisa) {
			//是VISA card
			//kevin:調整為FISC規格
			slReasonCode = gGate.f58T70;
			//V1.00.01 VISA代碼化交易處理借用gGate.bankBit39Code欄位顯示在授權LOG
			gGate.bankBit39Code =  gGate.f58T70;
			gb.showLogMessage("I","TOKEN FOR VISA REASON CODE =" + slReasonCode);

			if (    ("3700".equals(slReasonCode)) ||  //3700 =>Token Create
					("3701".equals(slReasonCode)) ||  //3701 =>Token Deactivate
					("3702".equals(slReasonCode)) ||  //3702 =>Token Suspend
					("3703".equals(slReasonCode)) ||  //3703 =>Token Resume
					("3711".equals(slReasonCode)) ||  //3711 => Token Provisioned
					("3712".equals(slReasonCode)) ||  //3712 =>OTP validation result
					("3713".equals(slReasonCode)) ||  //3713 =>Call center Activate/Token Activation
					("3714".equals(slReasonCode)) ||  //3714 =>APP Authentication result
					("3715".equals(slReasonCode))     //3715 => LUK Provisioned			 
					){  
					//代碼化通知訊息電文
					ta.insertOempayApplyData();
			}

			if (    ("3700".equals(slReasonCode)) ||  //3700 =>Token Create
					("3701".equals(slReasonCode)) ||  //3701 =>Token Deactivate
					("3702".equals(slReasonCode)) ||  //3702 =>Token Suspend
					("3703".equals(slReasonCode)) ||  //3703 =>Token Resume
					("3711".equals(slReasonCode)) ||  //3711 => Token Provisioned
					("3712".equals(slReasonCode)) ||  //3712 =>OTP validation result
					("3713".equals(slReasonCode)) ||  //3713 =>Call center Activate/Token Activation
					("3714".equals(slReasonCode)) ||  //3714 =>APP Authentication result
					("3715".equals(slReasonCode))     //3715 => LUK Provisioned			 

					){
				if  ("3700".equals(slReasonCode))
					{slStatus = "0";}
				else if ("3701".equals(slReasonCode))
					{slStatus = "2";}
				else if ("3702".equals(slReasonCode))
					{slStatus = "1";}
				else if ("3703".equals(slReasonCode))
					{slStatus = "0";}
				else if ("3711".equals(slReasonCode))
					{slStatus = "4";}
				else if ("3712".equals(slReasonCode))
					{slStatus = "0";}
				else if ("3713".equals(slReasonCode))
					{slStatus = "0";}
				else if ("3714".equals(slReasonCode))
					{slStatus = "0";}
				else if ("3715".equals(slReasonCode))
					{slStatus = "0";}
				if  (gGate.ifHaveOempayInfo)
					{ta.updateOempayCard(slStatus);}
				else 
					{ta.insertOempayCard(slStatus);}
			}

		}
		else {
			//是Master card
			//kevin:調整為FISC規格
			slReasonCode = gGate.f58T72;
			//V1.00.01 M/C代碼化交易處理借用gGate.bankBit39Code欄位顯示在授權LOG
			gGate.bankBit39Code =  gGate.f58T72;
			if( 	("0251".equals(slReasonCode))  ||
					("0252".equals(slReasonCode)) 
					){
				if  ("0251".equals(slReasonCode)) {
					//通知TOKENIZATION完成 => 寫log
					slStatus = "0";
				}
				else {
					//通知TOKENIZATION事件 => 寫log
//					slStatus = "1";
					if ("3".equals(gGate.tokenEvent)) {
						slStatus = "2";
					}
					else if ("6".equals(gGate.tokenEvent)) {
						slStatus = "1";						
					}
					else if ("7".equals(gGate.tokenEvent)) {
						slStatus = "0";						
					}
					else if ("8".equals(gGate.tokenEvent)) {
						slStatus = "1";						
					}
					else {
						slStatus = "1";						
					}
				}
				ta.insertOempayApplyData();
				
				if  (gGate.ifHaveOempayInfo)
					{ta.updateOempayCard(slStatus);}
				else 
					{ta.insertOempayCard(slStatus);}

			}
		}
		return blResult;
	}
	
	/**
	 * 檢查行動支付手機黑名單
	 * V1.00.40 P3檢查行動支付手機黑名單
	 * @throws Exception
	 */
	public boolean checkMobileBlack() throws Exception {
		boolean blResult = true;
		int nlHours = 0;
		if (!ta.selectCcaMobileBlackList()) {
			ta.getAndSetErrorCode("1O"); //手機門號已被設定為黑名單
			return false;
		}
		if (ta.selectPtrSysParm("SYSPARM", "CHG_CELLAR_PHONE_TIME")) { 
			nlHours = ta.getInteger("SysValue1"); //取得手機號碼變更X小時不可申請行動支付(HCE、OEMPAY、EPAY)
		}
		if (!ta.checkMobileChgTime(nlHours)) {
			ta.getAndSetErrorCode("1P"); //手機門號異動與綁定時間太接近
			return false;
		}

		return blResult;
	}
	/**
	 * MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同
	 * V1.00.64 MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同
	 * @throws Exception
	 */
	public boolean checkMobileLast4Digtal() throws Exception {
		boolean blResult = true;
		if (ta.getValue("CrdIdNoCellPhone").length()>4) {
			String slLast4Digtal = ta.getValue("CrdIdNoCellPhone").substring(ta.getValue("CrdIdNoCellPhone").length()-4);
			if (!slLast4Digtal.equals(gGate.numLast4Digits)) {
				blResult = false;
				gGate.authRemark = "MTAR:行動電話後四碼"+gGate.numLast4Digits+"與系統不符";
			}
		} else {
			blResult = false;
			gGate.authRemark = "MTAR:行動電話後四碼"+gGate.numLast4Digits+"與系統無號碼不符";
		}
		return blResult;
	}
}
