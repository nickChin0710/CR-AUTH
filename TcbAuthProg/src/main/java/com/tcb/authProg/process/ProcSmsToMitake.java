/**
 * Proc 處理Mitake SMS簡訊處理的流程 - 取消此程式改由LogicProcSmsToMitake統一處理
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
 * 2021/02/08  V1.00.00  Kevin       Proc 處理Mitake SMS簡訊處理的流程             *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.logic.AuthLogicCancel;
import com.tcb.authProg.main.AuthGlobalParm;
//import com.tcb.authProg.sms.SmsProcess;

	/*
	 * Proc 處理Mitake SMS簡訊處理的流程
	 * 
	 *
	 * @author Kevin Lin
	 * @version 1.0
	 * @since   2021/01/11
	 */

public class ProcSmsToMitake extends AuthProcess {

	public ProcSmsToMitake(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta, AuthLogicCancel aulg) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		this.aulg   = aulg;
		
		gb.showLogMessage("I","ProcSmsToMitake : started");

	}
	
//	public void processSmsInfo() throws Exception{
//
//		if ("00".equals(gGate.isoField[39])) {
//			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 才要發簡訊
//			gTa.getParm3TranCode("TRANCODE", gGate.transCode);
//			if ("Y".equals(gTa.getValue("Parm3TranCode4").trim())) {
//				//down, 處理  授權消費簡訊通知參數
//				gGate.ifIgnoreSmsOfTrading = aulg.checkSmsParm(); 
//				System.out.println("@@@@消費簡訊代碼 = "+gTa.getValue("CcaAuthSmsMsgId1"));
//				if (!gGate.ifIgnoreSmsOfTrading) {
//					sendSms(1, gTa.getValue("CcaAuthSmsMsgId1"), gTa.getValue("CrdIdNoCellPhone").trim()); //發出消費簡訊
//					if (gGate.isSupCard) {
//						if (gTa.selectPrimaryCardIdNo()) {
//							System.out.println("@@@@附卡消費通知正卡簡訊代碼 = "+gTa.getValue("CcaAuthSmsMsgId1"));
//							sendSms(4, gTa.getValue("CcaAuthSmsMsgId1"),gTa.getValue("CrdIdNoPrimCellPhone").trim()); //發出消費簡訊(附卡消費通知正卡)
//						}
//					}
//				}
//				//up, 處理  授權消費簡訊通知參數	
//			}
//			//down, 處理 授權特殊消費簡訊1參數
//			aulg.checkSmsParm4SpecialTrading();
//			if (gGate.ifSendSms4Cond1) {
//				sendSms(2, gTa.getValue("CcaAuthSms2MsgId1"), gTa.getValue("CrdIdNoCellPhone").trim()); //發出特殊簡訊一
//			}
//			//up, 處理 授權特殊消費簡訊1參數
//		}
//		//down, 處理 授權特殊消費簡訊2參數
//		if (gGate.ifSendSms4Cond2) {
//			sendSms(3, gTa.getValue("CcaAuthSms2MsgId2"), gTa.getValue("CrdIdNoCellPhone").trim()); //發出特殊簡訊二
//		}
//		//up, 處理 授權特殊消費簡訊2參數
//	}
//	
//	public void sendSms(int npTransType, String spMsgId, String spCellPhoneNo) throws Exception{
//		//kevin: 發送即時簡訊指定電話號碼
//		//nP_TransType=> 1: 消費簡訊, 2:特殊簡訊一, 3:特殊簡訊二, 4:附卡消費通知正卡
//		String slMsgType = "";
//		switch(npTransType) {
//			case 1  : slMsgType = "AUTO" ;  break;//消費簡訊
//			case 2  : slMsgType = "SPEC1";  break;//特殊簡訊一
//			case 3  : slMsgType = "SPEC2";  break;//特殊簡訊二
//			case 4  : slMsgType = "AUTO2";  break;//附卡消費通知正卡
//			default : break;
//		}
//		
//		gTa.insertMsgEven(spMsgId, slMsgType, spCellPhoneNo);
//		if (spCellPhoneNo.length() == 0) {
//			return;
//		}
//		String slSmsContent = aulg.getSmsContent(npTransType, spMsgId);
//		
//		Thread thread = new Thread(new SmsProcess(gGb, spCellPhoneNo, slSmsContent));
//		thread.start();
//
//	}
}
