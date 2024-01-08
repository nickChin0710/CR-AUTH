/**
 * 處理Line訊息連線作業 
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/12/27
 * 
 * @throws  Exception if any exception occurred
 * @return  slErrorCode
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/12/27  V1.00.00  Kevin       處理Line訊息連線與接收處理作業                  *
 * 2022/03/18  V1.00.01  Kevin       LINE MESSAGE LOG放入傳送時間                *
 * 2022/06/03  V1.00.02  Kevin       網銀推播-信用卡消費通知介面處理                  *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.sms;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.DbUtil;
import com.tcb.authProg.util.HpeUtil;

import net.sf.json.JSONObject;

public class LineProcess implements Runnable {
	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm gGb = null;

	/**
	 * 資料庫存取物件
	 */
	TableAccess pGlobalTa = null;
	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate gGate = null;

	String sgLineId = "";
	String sgLinePInfo = "";
	String sgLineServerUrl = "";
	String sgCellPhoneNo = "";
	String sgLineContent = "";
	String sgTxDate = "", sgCardNo = "";
	String sgIdNo = "", sgGroupName = "", sgCardNo4 = "", sgTxnAmt = "", sgCurrName = "", sgTxnCodeType = "",
			sgMchtName = "", sgAuthNo = "", sgTimeStamp = "";

	public LineProcess(AuthGlobalParm pGlobalParm, String spIdNo, String spGroupName, String spCardNo, String spTxnAmt,
			String spCurrName, String spTxnCodeType, String spMchtName, String spAuthNo, String spTimeStamp) {
		// TODO Auto-generated constructor stub
		gGb = pGlobalParm;
		gGate = new AuthTxnGate();
		pGlobalTa = new TableAccess(gGb, gGate);
//		sgLineServerUrl = "https://10.0.172.8/node-red/CDNotice";
		sgLineServerUrl = gGb.getLineAiUrl();
		sgIdNo = spIdNo;
		sgGroupName = spGroupName;
		sgTxnAmt = spTxnAmt;
		sgCurrName = spCurrName;
		sgTxnCodeType = spTxnCodeType;
		sgMchtName = spMchtName;
		sgAuthNo = spAuthNo;
		sgTimeStamp = spTimeStamp;

//		//取出卡號末四碼
		gGate.cardNo = spCardNo;
		if (gGate.cardNo.length() >= 4)
			sgCardNo4 = gGate.cardNo.substring(gGate.cardNo.length() - 4, gGate.cardNo.length());
		gGate.authNo = spAuthNo;
	}

	@Override
	public void run() {
		try {
			sendLine();
		} catch (Exception e) {
			gGb.showLogMessage("I", "send LINE AI error.");
		}
	}

	private boolean sendLine() throws Exception {
		boolean blResult = true;

		JSONObject jsonObjectLineTxn = new JSONObject();
		jsonObjectLineTxn.put("id", sgIdNo);
		jsonObjectLineTxn.put("cardName", sgGroupName);
		jsonObjectLineTxn.put("endDigits", sgCardNo4);
		jsonObjectLineTxn.put("amount", sgTxnAmt);
		jsonObjectLineTxn.put("ccy", sgCurrName);
		jsonObjectLineTxn.put("type", sgTxnCodeType);
		jsonObjectLineTxn.put("store", sgMchtName);
		jsonObjectLineTxn.put("accessCode", sgAuthNo);
		jsonObjectLineTxn.put("time", sgTimeStamp);

		String reqLineAI = jsonObjectLineTxn.toString();
		gGb.showLogMessage("D", "@@@reqLineAI@@@=" + reqLineAI);

		String applJson = "application/json";
		String rtnLine = "";
		String statusDesc = "";
		String statusCode = "";

		try {
			int nlTimeOutSec = (int) gGb.getWarningSec();
			gGb.showLogMessage("D", "@@@rtnLine@@@ Time out Sec.="+nlTimeOutSec);
			rtnLine = HpeUtil.curlToken(sgLineServerUrl, applJson, "", reqLineAI, nlTimeOutSec);
			if ("ERROR".equals(rtnLine) || "TIMEOUT".equals(rtnLine)) {
				gGb.showLogMessage("E","LINE AI Connect failed=" + rtnLine);
				blResult = false;
			}

			gGb.showLogMessage("D", "@@@rtnLine_rtnLine@@@=" + rtnLine);
			JSONObject jsonIn = JSONObject.fromObject(rtnLine);
			gGb.showLogMessage("D", "@@@rtnLine_jsonIn@@@=" + jsonIn);
			statusDesc = jsonIn.getString("statusDesc");
			statusCode = jsonIn.getString("statusCode");
			gGb.showLogMessage("D", "rtnLine_statusDesc=" + statusDesc+ "_statusCode=" + statusCode);
		} catch (Exception e) { // 傳送LINE主機錯誤
			// kevin:測試時先改成true，並且不要直接return，傳送一個假的rtnLine ###top
			gGb.showLogMessage("E", "LINE AI send failed=" + rtnLine + e);
			blResult = false;
		}
		// up, new socket to send to Line

		if (statusCode.length() <= 0) {
			statusCode = "9999";
			statusDesc = "傳送LINE主機錯誤";
		}

		if ("4001".equals(statusCode)) {
			blResult = true;
		} else {
			blResult = false;
		}

		try {
//			gGb.showLogMessage("D", "LineProcess start send Line=>" + gGate.cardNo + "-------");
			gGate.gDbConn = gGb.getgDataSource().getConnection();
			if (null == gGate.gDbConn) {
				gGb.showLogMessage("E", "insertLineMessage error for database is not connected.");
			} 
			else {
				pGlobalTa.insertLineMessage(true, reqLineAI, blResult, statusCode, sgIdNo, sgTimeStamp, "");
				DbUtil.commitConn(gGate.gDbConn);
				DbUtil.closeConn(gGate.gDbConn); //kevin:db需要close
			}
		} catch (Exception e) {
			if (null != gGate.gDbConn) {
				DbUtil.closeConn(gGate.gDbConn); //kevin:db需要close
			}
			gGb.showLogMessage("E", "LINE AI insert message log error=" + e);
		}
		gGate = null;	
		return blResult;
	}
	
}