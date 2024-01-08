/**
 * 授權邏輯查核-語音開卡交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-語音開卡交易檢核處理                 *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcOpenCardByVoice extends AuthLogic {

	public LogicProcOpenCardByVoice(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcOpenCardByVoice : started");

	}
	
	
	//語音開卡
	/**
	 * 處理語音開卡交易
	 * @return  如果語音開卡成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean processOpenCardByVoice() throws Exception {

		gb.showLogMessage("I","processOpenCardByVoice : started");

		String slTmpVoiceCode = "";
		if (gGate.IsNewCard) {
			slTmpVoiceCode = ta.getValue("CardBaseVoiceOpenCode");
		}
		else 
			slTmpVoiceCode = ta.getValue("CardBaseVoiceOpenCode2");

		if (  ("*".equals(slTmpVoiceCode)) ||
				(" ".equals(slTmpVoiceCode)) || 
				(slTmpVoiceCode.length()==0) ) {
			slTmpVoiceCode = HpeUtil.getTaiwanDateStr(ta.getValue("CrdIdNoBirthday"));
		}
		else 
			slTmpVoiceCode = HpeUtil.transPasswd(2, slTmpVoiceCode);

		if (!slTmpVoiceCode.equals(getPasswdFromIsoString4VoiceOpenCard())) {
			ta.getAndSetErrorCode("CO");
			return false;
		}

		gGate.authNo = "AT0000";

		String slActivateType="V";//V: VOICE
		if ("0                     TAIPEI CITY  TW TW".equals(gGate.isoField[43]))
			slActivateType="O"; //O:ONLINE

		if (!gGate.isAtmCardActivated) { //ATM COMBO卡啟用時，如檢查已開卡，將不需要再更新開卡資訊
			ta.updateCardBaseAfterVoiceOpenCard(slActivateType);
			ta.insertCardOpen();
			ta.insertOnCcaBat(slActivateType);
		}

		return true;
	}

}
