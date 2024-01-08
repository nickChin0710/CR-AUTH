/**
 * 授權邏輯查核-退貨交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-退貨交易檢核處理                    *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcRefund extends AuthLogic {
	
	public LogicProcRefund(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcRefund : started");

	}
	
	//處理退貨交易
	/**
	 * 處理退貨交易
	 * @return 如果正確處理完成退貨交易，return true，否則return false
	 * @throws Exception if any exception occurred 
	 * TCB退貨條件放寬，未帶授權碼之交易，不需比對原交易直接退貨。
	 */
	public boolean procRefund() throws Exception {
		boolean blResult = true;
		//VD退貨交易不處理也不送主機解圈
		if (gGate.isDebitCard) {
			gGate.ifSendTransToIms = false;
			gGate.unlockFlag = "R"; //--N.未解圈, Y.己解圈 R.沖正成功
			return true;
		}
		//TCB退貨條件不須再比對原始交易。
//		if (gGate.oriAuthNo.length()==0) {
//			return true;
//		}
//		else {			
//			if ( !ta.getTxlog(1) ) {
//				ta.getAndSetErrorCode("DH");
//				return false;
//			}
//		}
//		checkOnlineRedeem();
//
//		double dlChkAmt = 0;
//		if (gGate.isRedeemTx) 
//			dlChkAmt = gGate.ntAmt;
//		else if (gGate.isDebitCard) {
//			dlChkAmt = ta.getDouble("AuthTxLogVdLockNtAmt");
//			gGate.imsLockSeqNo = ta.getValue("AuthTxLogTxSeq");
//			System.out.println("退貨交易原圈存序號 = "+gGate.imsLockSeqNo+"原交易金額 = "+gGate.oriLockAmount);
//		}
//		else
//			dlChkAmt = ta.getDouble("AuthTxLogOriAmt_SrcTrans");
//
//		if ((dlChkAmt-gGate.isoFiled4Value)<0) {
//			ta.getAndSetErrorCode("DK");
//			return false;
//		}
//
//		/** CACU_AMOUNT --  計入OTB註記            */
//		/** CACU_AMOUNT -- 檢核是否已佔額度**/
//		if ("N".equals(ta.getValue("AuthTxlogCacuAmount_SrcTrans"))) { //--Y.未沖銷退貨調整, N.已沖銷退貨調整
//			ta.getAndSetErrorCode("DI");
//			return false;
//		}
//		ta.setValue("AuthTxlogCacuAmount_SrcTrans", "N");
//		ta.setValue("AuthTxlogCacuCash_SrcTrans", "N");
//
//		if (gGate.isRedeemTx)
//			gGate.isoFiled4Value = ta.getDouble("AuthTxLogNtAmt_SrcTrans");
//
//		gGate.logicDel = "B";
//		//kevin:取消service4Manual設定，改為單筆connType 決定		
//		if ("WEB".equals(gGate.connType)) /* 退貨 -- 改為沖正交易(人工授權) */
//			gGate.logicDel = "Z";
//		//kevin:先設定flag，帶交易成功後再更新table
//     	gGate.updateSrcTxAfterRefund = true;
//     	blResult = true;
		return blResult;
	}
}
