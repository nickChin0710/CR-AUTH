/**
 * 授權邏輯查核-AI社群信用卡消費通知介面處理
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/12/27
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/12/27  V1.00.00  Kevin       授權邏輯查核-AI社群信用卡消費通知介面處理            *
 * 2022/02/04  V1.00.36  Kevin       處理Line消費推播商店名稱以授權的英文特店名稱顯示       *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.sms.LineProcess;
import com.tcb.authProg.util.HpeUtil;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;


public class LogicProcLineToAI extends AuthLogic {

	public LogicProcLineToAI(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcLineToAI : started");

	}
	/**
	 * 發送即時LINE推撥到指定ID
	 * V1.00.48  P3程式碼整理(附卡註記/附卡消費通知正卡註記)
	 * @throws Exception
	 */
	public void processLineInfo() throws Exception{

		if ("00".equals(gGate.isoField[39]) && !gGate.idNo.isEmpty()) {
			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 才要發簡訊
//			ta.getParm3TranCode("TRANCODE", gGate.transCode);
			if ("Y".equals(ta.getValue("Parm3TranCode5").trim())) {
				gb.showLogMessage("D","@@@@LINE消費簡訊代碼 = "+ta.getValue("CcaAuthLineMsgId1"));
				sendLine(1); //發出消費簡訊
				if (gGate.isSupCard) {
					if (gGate.isAdvicePrimChFlag) {
						gb.showLogMessage("D","@@@@附卡消費通知正卡Line訊息");
						sendLine(4); //發出消費訊息(附卡消費通知正卡)
					}
				}
			}
		}
	}
	/**
	 * 發送即時LINE推撥到指定ID
	 * V1.00.36 處理Line消費推播商店名稱以授權的英文特店名稱顯示
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	private void sendLine(int npTransType) throws Exception{
		//kevin: 發送即時LINE推撥到指定ID
		//nP_TransType=> 1: 消費通知持卡人LINE, 2:特殊LINE通知一, 3:特殊LINE通知二, 4:附卡消費通知正卡LINE
		//取出持卡人 ID
		String slIdNo = "";
		switch(npTransType) {
		case 1  : slIdNo = gGate.idNo;  break;//消費通知持卡人LINE
		case 2  : slIdNo = gGate.idNo;  break;//特殊LINE通知一
		case 3  : slIdNo = gGate.idNo;  break;//特殊LINE通知二
		case 4  : slIdNo = ta.getValue("CrdIdNoPrimIdNo").trim();  break;//附卡消費通知正卡LINE
		default : slIdNo = gGate.idNo;  break;
		}
//		slIdNo = "D174869911";
		//取出卡片名稱
		String slGroupName =ta.getValue("GROUP_NAME").trim();
		//取出交易幣別
		String slCurrCode = "901";
		String slCurrName = "TWD";
		String slTxnAmt   = HpeUtil.decimalRemove(gGate.ntAmt); 
		if (gGate.isoField[49].length() > 0) {
			slCurrCode = new String(gGate.isoField[49]);
		}
		if (!"901".equals(slCurrCode)) {
			if (ta.selectPtrCurrcode(slCurrCode)) { 
				slTxnAmt   = Double.toString(gGate.oriAmount);
				slCurrName = ta.getValue("PtrCurrEngName").trim();
			}
			else {
				slCurrName = slCurrCode;
			}
		}
		//取出交易類別
		String slTxnCodeType = ta.getValue("Parm3TranCode1");
		//取出特店名稱
		String slMchtName = gGate.merchantName;
		if (slMchtName.isEmpty()) {
			slMchtName = gGate.merchantNo;
		}
		
		String slTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gb.getgTimeStamp());
		
		
		Thread thread = new Thread(new LineProcess(gb, slIdNo, slGroupName, gGate.cardNo, slTxnAmt, slCurrName, slTxnCodeType, slMchtName, gGate.isoField[38], slTimeStamp));
		thread.start();

	}
}
