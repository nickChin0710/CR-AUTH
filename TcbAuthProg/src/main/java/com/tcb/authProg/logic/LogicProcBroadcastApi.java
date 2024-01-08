/**
 * 授權邏輯查核-網銀推播信用卡消費通知介面處理
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
 * 2022/06/03  V1.00.00  Kevin       網銀推播-信用卡消費通知介面處理                    *
 * 2022/06/21  V1.00.01  Kevin       網銀推播-網銀客戶設定啟用通知                     *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.sms.BroadcastProcess;
import com.tcb.authProg.util.HpeUtil;
import java.text.SimpleDateFormat;


public class LogicProcBroadcastApi extends AuthLogic {

	public LogicProcBroadcastApi(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcBroadcastApi : started");

	}
	/**
	 * 發送即時網銀推撥到指定ID
	 * V1.00.48  P3程式碼整理(附卡註記/附卡消費通知正卡註記)
	 * @throws Exception
	 */
	public void processBroadcastInfo() throws Exception{

		if ("00".equals(gGate.isoField[39]) && !gGate.idNo.isEmpty()) {
			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 才要發簡訊
//			ta.getParm3TranCode("TRANCODE", gGate.transCode);
			if ("Y".equals(ta.getValue("Parm3TranCode2").trim())) {
				gb.showLogMessage("D","@@@@網銀推播消費簡訊代碼 = "+ta.getValue("CcaAuthLineMsgId1"));
				sendBroadcast(1); //發出消費簡訊
				if (gGate.isSupCard) {
					if (gGate.isAdvicePrimChFlag) {
						gb.showLogMessage("D","@@@@附卡消費通知正卡網銀推播訊息");
						sendBroadcast(4); //發出消費訊息(附卡消費通知正卡)
					}
				}
			}
		}
	}
	
	private void sendBroadcast(int npTransType) throws Exception{
		//檢查網銀推播url是否啟用
		//V1.00.01 網銀推播-網銀客戶設定啟用通知
		String slUrl = "";
		if (!ta.selectPtrSysParm("BROADCAST_API", "URL")) {
			gb.showLogMessage("D","BROADCAST_API + URL not found =>網銀推播功能未啟用");
			return;
		}
		else {
			slUrl = ta.getValue("SysValue1");
			gb.showLogMessage("D","BROADCAST_API + URL === "+ slUrl);
		}
		//kevin: 發送即時網銀推播推播到指定ID
		//nP_TransType=> 1: 消費通知持卡人網銀推播, 2:特殊網銀推播通知一, 3:特殊網銀推播通知二, 4:附卡消費通知正卡網銀推播
		//取出持卡人 ID
		String slIdNo = "";
		switch(npTransType) {
		case 1  : slIdNo = gGate.idNo;  break;//消費通知持卡人網銀推播
		case 2  : slIdNo = gGate.idNo;  break;//特殊網銀推播通知一
		case 3  : slIdNo = gGate.idNo;  break;//特殊網銀推播通知二
		case 4  : slIdNo = ta.getValue("CrdIdNoPrimIdNo").trim();  break;//附卡消費通知正卡網銀推播
		default : slIdNo = gGate.idNo;  break;
		}
		//檢查是否為網銀客戶
		//V1.00.01 網銀推播-網銀客戶設定啟用通知
		if (!ta.selectMktWebCust(slIdNo)) {
			gb.showLogMessage("D","MKT_WEB_CUSTOMER ID not found =>非網銀客戶推播功能未啟用");
			return;
		}
		//取得推播訊息內容
		String slSmsContent = getSmsContent(npTransType, "9999");
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
		String slMchtName = ta.selectCcaMchtBill();
		
		String slTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(gb.getgTimeStamp());

		
		Thread thread = new Thread(new BroadcastProcess(gb, slIdNo, slGroupName, gGate.cardNo, slTxnAmt, slCurrName, slTxnCodeType, slSmsContent, gGate.isoField[38], slTimeStamp, slUrl));
		thread.start();
	}
}
