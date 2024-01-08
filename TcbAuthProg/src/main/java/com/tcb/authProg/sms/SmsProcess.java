/**
 * 處理SMS簡訊連線作業 
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  slErrorCode
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       處理SMS簡訊連線與接收處理作業                   *
 * 2021/02/08  V1.00.01  Tanwei       updated for project coding standard     *  
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.sms;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.DbUtil;

public class SmsProcess implements Runnable{
	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm  gGb   = null;

	/**
	 * 資料庫存取物件
	 */
	TableAccess     pGlobalTa = null;
	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate     gGate = null;
	
	String sgSmsId="";
	String sgSmsPInfo="";
	String sgSmsServerUrl="";
	String sgCellPhoneNo="";
	String sgSmsContent = "";
	String sgTxDate="", sgCardNo="", sgAuthNo="";


	public SmsProcess(AuthGlobalParm pGlobalParm, String spCellPhoneNo, String spSmsContent, String spTxDate, String spCardNo, String spAuthNo) {
		// TODO Auto-generated constructor stub
		gGb = pGlobalParm;
		gGate  =  new AuthTxnGate();
		pGlobalTa   = new TableAccess(gGb,gGate);
		sgSmsId = gGb.getSgSmsId();
		sgSmsPInfo = gGb.getSgSmsPInfo();
		sgSmsServerUrl = gGb.getSgSmsServerUrl();
		sgCellPhoneNo = spCellPhoneNo;
		sgSmsContent = spSmsContent;
		gGate.txDate = spTxDate;
		gGate.cardNo = spCardNo;
		gGate.authNo = spAuthNo;
		
//		try {				
//			gGb.showLogMessage("I", "SmsProcess start send SMS=>"+ spCellPhoneNo + "-------");					
//			gGate.gDbConn = gGb.getgDataSource().getConnection();	
//		} catch (Exception e) {
//			// TODO: handle exception
//			gGb.showLogMessage("E","can not get db connectionn in SmsProcess thread!!Exception:" + e.getMessage());
//			gGate.gDbConn = null;
//		}
	}

	@Override
	public void run() {
//		if (null == gGate.gDbConn) {
//		//if (!gb.dbConnected) {
//			gGb.showLogMessage("E","database is not connected.");
//			gGb.setSystemError(true);
//		}
//		else {
		try {
			sendSms();
		} catch (Exception e) {
			gGb.showLogMessage("I", "send SMS Mitake error.");
		}
//			DbUtil.closeConn(gGate.gDbConn); //kevin:db需要close
//		}
	}

	private String sendSms( ) {
		String slErrorCode = "";
		String[] smsResp = new String[3];

		try {
			OTPSend data = new OTPSend();
			data.uid = sgSmsId;
//			String pinfo = sgSmsPInfo;
			data.pwd = sgSmsPInfo;
			data.da = sgCellPhoneNo;
			data.sm = sgSmsContent;
			String slServerUrl = sgSmsServerUrl;//"http://stgsmsb2c.mitake.com.tw:8001/b2c/mtk/SmSend?";
			gGb.showLogMessage("I", "smsserverurl= "+ slServerUrl );
			gGb.showLogMessage("I", "smsdata= "+ data );
//			String[] smsResp = new String[3];
			smsResp = OTPSend.sendService(data, slServerUrl);
			gGb.showLogMessage("D", "smsdata Response= "+smsResp[0]+";"+smsResp[1]+";"+smsResp[2]);
//			pGlobalTa.updateMsgEven(smsResp[0], smsResp[1]);
//			DbUtil.commitConn(gGate.gDbConn);
//			gGate = null;						
		} catch (Exception e) {
			// TODO: handle exception
			slErrorCode = "99";
			smsResp[1] = slErrorCode;
			gGb.showLogMessage("D", "smsdata exception= "+e);
//			DbUtil.rollbackConn(gGate.gDbConn);

		}
		// up, new socket to send to Mitake
		try {
			gGb.showLogMessage("I", "SmsProcess start send Line=>" + gGate.cardNo + "-------");
			gGate.gDbConn = gGb.getgDataSource().getConnection();
			if (null == gGate.gDbConn) {
				gGb.showLogMessage("E", "UpdateSmsMessage error for database is not connected.");
			}
			else {
				pGlobalTa.updateMsgEven(smsResp[0], smsResp[1]);
				DbUtil.commitConn(gGate.gDbConn);
				DbUtil.closeConn(gGate.gDbConn); //kevin:db需要close
			}
		} catch (Exception e) {
			if (null != gGate.gDbConn) {
				DbUtil.closeConn(gGate.gDbConn); //kevin:db需要close
			}
			gGb.showLogMessage("E", "SMS Update message log error=" + e);
		}
		gGate = null;						
		return slErrorCode;

	}
}