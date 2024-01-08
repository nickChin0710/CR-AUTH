/**
 * 處理網銀推播訊息連線與接收處理作業 
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2022/06/03
 * 
 * @throws  Exception if any exception occurred
 * @return  slErrorCode
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2022/06/03  V1.00.00  Kevin       網銀推播-信用卡消費通知介面處理                  *
 * 2022/10/12  V1.00.01  Kevin       網銀推播取消TITLE訊息欄位                     * 
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.sms;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.DbUtil;
import com.tcb.authProg.util.HpeUtil;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class BroadcastProcess implements Runnable {
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

//	String sgBroadcastId = "";
//	String sgBroadcastPInfo = "";
	String sgBroadcastServerUrl = "";
//	String sgCellPhoneNo = "";
	String sgBroadcastContent = "";
//	String sgTxDate = "", sgCardNo = "";
	String sgIdNo = "";
	String sgGroupName = "", sgCardNo4 = "", sgTxnAmt = "", sgCurrName = "", sgTxnCodeType = "", sgMchtName = "",
			sgAuthNo = "";
	String sgTimeStamp = "";
	String viaAccount = "TCB_CARD_BUY";
	String account = "TCB_CARD_BUY";
	String roleId = "";
	String guid = "";
	String title = "信用卡/VISA金融卡消費通知";

	public static String PARAM_UNIQUE_ID = "UniqueId";
	public static String PARAM_APP_ID = "AppId";
	public static String PARAM_ACCOUNT = "Account";
	public static String PARAM_GUID = "Guid";
	public static String PARAM_EXPIRY_TIME = "ExpiryTime";
	public static String PARAM_ROLE_ID = "RoleId";
	public static String PARAM_VIA_ACCOUNT = "ViaAccount";
	public static String PARAM_RECEIVERS = "Receivers";
	public static String PARAM_CONTENT = "Content";
	public static String PARAM_CONTENT_TYPE = "ContentType";
	public static String PARAM_TITLE = "Title";
	public static String PARAM_RECEIVER_GROUP = "ReceiverGroup";
	public static String PARAM_LINK_APP = "LinkApp";
	public static String PARAM_EXTRA_DATA = "ExtraData";
	public static String PARAM_EXTRA_MSG = "ExtraMsg";
	public static String PARAM_PRESET_TYPE = "PresetType";
	public static String PARAM_BEGIN_TIME = "BeginTime";
	public static String PARAM_END_TIME = "EndTime";
	public static String PARAM_ORDER = "Order";
	public static String PARAM_APPOINTMENT_TIME = "AppointmentTime";

	public BroadcastProcess(AuthGlobalParm pGlobalParm, String spIdNo, String spGroupName, String spCardNo,
			String spTxnAmt, String spCurrName, String spTxnCodeType, String sPBroadcastContent, String spAuthNo,
			String spTimeStamp, String spUrl) {
		gGb = pGlobalParm;
		gGate = new AuthTxnGate();
		pGlobalTa = new TableAccess(gGb, gGate);
//		sgBroadcastServerUrl = "https://10.0.172.11/string/Management/Broadcast";
		sgBroadcastServerUrl = spUrl;
		sgIdNo = spIdNo;
		sgBroadcastContent = sPBroadcastContent;
		sgTxnAmt = spTxnAmt;
		sgCurrName = spCurrName;
		sgTxnCodeType = spTxnCodeType;
		sgGroupName = spGroupName;
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
			sendBroadcast();
		} catch (Exception e) {
			gGb.showLogMessage("I", "send Broadcast API error.");
		}
	}

	private boolean sendBroadcast() throws Exception {
		boolean blResult = true;

		JSONObject jsonObjectBroadcastTxn = transform2BrodCastJsObj(sgIdNo, title, sgBroadcastContent);
		String reqBroadcastApi = jsonObjectBroadcastTxn.toString();
		gGb.showLogMessage("D", "@@@reqBroadcastApi@@@=" + reqBroadcastApi);

		String applJson = "application/json";
		String rtnBroadcast = "";
		String errMsg = "";
		String errCode = "";
		String errProc = "";
		String messageId = "";

		try {
//			if (pGlobalTa.selectPtrSysParm("BROADCAST_API", "URL")) {
//				sgBroadcastServerUrl = pGlobalTa.getValue("WF_VALUE");
//				gGb.showLogMessage("D","BROADCAST_API + URL === "+sgBroadcastServerUrl);
//			}
//			else {
//				gGb.showLogMessage("D","BROADCAST_API + URL not found");
//				blResult = false;
//			}
			int nlTimeOutSec = (int) gGb.getWarningSec();
			gGb.showLogMessage("D", "@@@rtnBroadcast@@@ Time out Sec.=" + nlTimeOutSec);
			rtnBroadcast = HpeUtil.curlToken(sgBroadcastServerUrl, applJson, "", reqBroadcastApi, nlTimeOutSec);
			if ("ERROR".equals(rtnBroadcast) || "TIMEOUT".equals(rtnBroadcast)) {
				gGb.showLogMessage("E", "Broadcast API Connect failed=" + rtnBroadcast);
				blResult = false;
			}

			gGb.showLogMessage("D", "@@@rtnBroadcast_rtnBroadcast@@@=" + rtnBroadcast);
			JSONObject jsonIn = JSONObject.fromObject(rtnBroadcast);
			gGb.showLogMessage("D", "@@@rtnBroadcast_jsonIn@@@=" + jsonIn);
			errCode = jsonIn.getString("errCode");
			gGb.showLogMessage("D", "@@@rtnBroadcast_errCode@@@=" + errCode);
			errMsg = jsonIn.getString("errMsg");
			gGb.showLogMessage("D", "@@@rtnBroadcast_errMsg@@@=" + errMsg);
			if (errCode.length() <= 0) {
				errCode = "4001";
				errMsg = "傳送網銀推播主機成功";
				messageId = jsonIn.getString("MessageId");
				gGb.showLogMessage("I", "Broadcast API response successful=" + messageId);
			} else {
				errMsg = "傳送網銀推播主機錯誤";
				errProc = jsonIn.getString("errProc");
				gGb.showLogMessage("E", "Broadcast API no response_errProc=" + errProc);
			}

		} catch (Exception e) { // 傳送Broadcast主機錯誤
			gGb.showLogMessage("I", "Broadcast API send failed=" + rtnBroadcast + e);
			errCode = "9999";
			errMsg = "傳送網銀推播主機失敗";
			blResult = false;
		}
		// up, new socket to send to Broadcast

		if ("4001".equals(errCode)) {
			blResult = true;
		} else {
			blResult = false;
		}

		try {
//			gGb.showLogMessage("D", "BroadcastProcess start send Broadcast=>" + gGate.cardNo + "-------");
			gGate.gDbConn = gGb.getgDataSource().getConnection();
			if (null == gGate.gDbConn) {
				gGb.showLogMessage("E", "insertBroadcastMessage error for database is not connected.");
			} else {
				JSONObject jsonObjectLineTxn = new JSONObject();
				jsonObjectLineTxn.put("id", sgIdNo);
				jsonObjectLineTxn.put("cardName", sgGroupName);
				jsonObjectLineTxn.put("endDigits", sgCardNo4);
				jsonObjectLineTxn.put("amount", sgTxnAmt);
				jsonObjectLineTxn.put("ccy", sgCurrName);
				jsonObjectLineTxn.put("type", sgTxnCodeType);
				jsonObjectLineTxn.put("store", sgBroadcastContent);
				jsonObjectLineTxn.put("accessCode", sgAuthNo);
//				jsonObjectLineTxn.put("time", sgTimeStamp);

				String reqLineAI = jsonObjectLineTxn.toString();
				gGb.showLogMessage("D", "@@@Broadcast for insertLineMessage@@@=" + reqLineAI);
				pGlobalTa.insertLineMessage(false, reqLineAI, blResult, errCode, sgIdNo, sgTimeStamp, messageId);
				DbUtil.commitConn(gGate.gDbConn);
				DbUtil.closeConn(gGate.gDbConn); // kevin:db需要close
			}
		} catch (Exception e) {
			if (null != gGate.gDbConn) {
				DbUtil.closeConn(gGate.gDbConn); // kevin:db需要close
			}
			gGb.showLogMessage("E", "網銀推播 API insert message log error=" + e);
		}
		gGate = null;
		return blResult;
	}

	private JSONObject transform2BrodCastJsObj(String personalId, String otpCode, String optContent)
			throws JSONException {

		JSONObject job = new JSONObject();
		job.put(PARAM_UNIQUE_ID, viaAccount);
		job.put(PARAM_APP_ID, "");
		job.put(PARAM_ACCOUNT, account);
		job.put(PARAM_GUID, guid);
		job.put(PARAM_EXPIRY_TIME, "");
		job.put(PARAM_ROLE_ID, roleId);
		job.put(PARAM_VIA_ACCOUNT, viaAccount);
		job.put(PARAM_RECEIVERS, personalId);
		job.put(PARAM_CONTENT, optContent);
		job.put(PARAM_CONTENT_TYPE, "0");
		//V1.00.01 網銀推播取消TITLE訊息欄位		
		job.put(PARAM_TITLE, "");
		job.put(PARAM_RECEIVER_GROUP, "0");
		job.put(PARAM_LINK_APP, "");
		job.put(PARAM_EXTRA_DATA, "");
		job.put(PARAM_EXTRA_MSG, "");
		job.put(PARAM_BEGIN_TIME, "");
		job.put(PARAM_END_TIME, "");
		job.put(PARAM_APPOINTMENT_TIME, "");
		return job;
	}

}