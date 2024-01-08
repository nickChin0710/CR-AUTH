/**
 * 授權邏輯查核-調整交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-調整交易檢核處理                    *
 * 2022/03/17  V1.00.01  Kevin       麗花襄理要求2447交易需帶入原始RRN               *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcPurchaseAdjust extends AuthLogic {
	
	public LogicProcPurchaseAdjust(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcPurchaseAdjust : started");

	}
	/* 調整交易 */
	/**
	 * 驗證退貨調整、預借現金調整與一般交易調整交易 
	 * @return 驗證正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean procPurchaseAdjust() throws Exception {
		//退貨調整, 預借現金調整, 一般交易調整交易 => 都會進到此 function
		boolean blSetLockFlag=false, blGetPriorTx=false;
		gb.showLogMessage("I","procPurchaseAdjust : started");
		//proc => ISOpurchasing(5) //一般交易調整交易
		//proc => ISOpurchasing(6) //退貨調整
		//proc => ISOpurchasing(11) //預借現金調整

		boolean blResult = true;

		if (gGate.refundAdjust) {//退貨調整
			//VD退貨調整交易不處理也不送主機解圈
			if (gGate.isDebitCard) {
				gGate.ifSendTransToIms = false;
				gGate.unlockFlag = "R"; //--N.未解圈, Y.己解圈 R.沖正成功
				return true;
			}
			if ( !ta.getTxlog(3) ) {
				//ta.getAndSetErrorCode("ERR44");
				ta.getAndSetErrorCode("DH");
				return false;
			}
			ta.setValue("AuthTxlogCacuAmount_SrcTrans", "N");
			ta.setValue("AuthTxlogCacuCash_SrcTrans", "N");

			double dlChkAmt = 0;
			if (gGate.isRedeemTx) 
				dlChkAmt = gGate.ntAmt;
			else if (gGate.isDebitCard) {
				dlChkAmt = ta.getDouble("AuthTxLogVdLockNtAmt");
				gGate.oriLockAmount = dlChkAmt;
				gGate.imsLockSeqNo = ta.getValue("AuthTxLogTxSeq");
				gGate.imsOriRefNo = ta.getValue("AuthTxLogRefNo");


				gb.showLogMessage("D","退貨調整原圈存序號 = "+gGate.imsLockSeqNo+"原交易金額 = "+gGate.oriLockAmount+"原交易RRN = "+gGate.imsOriRefNo);

			}
			else
				dlChkAmt = ta.getDouble("AuthTxLogOriAmt_SrcTrans");

			if ((dlChkAmt-gGate.isoFiled4Value)<0) {
				ta.getAndSetErrorCode("DK");
				return false;
			}


			gGate.logicDel = "W";
			//kevin:先設定flag，帶交易成功後再更新table
	      	gGate.updateSrcTxAfterRefundAdj = true;
		}
		else if (gGate.purchAdjust) { //一般交易調整
			if ( !ta.getTxlog(2) ) {
				ta.getAndSetErrorCode("DH");
				return false;
			}
			checkOnlineRedeem();

			double dlChkAmt = 0;
			if (gGate.isRedeemTx) 
				dlChkAmt = gGate.ntAmt;
			else if (gGate.isDebitCard) {
				dlChkAmt = ta.getDouble("AuthTxLogVdLockNtAmt");
				gGate.oriLockAmount = dlChkAmt;
				gGate.imsLockSeqNo = ta.getValue("AuthTxLogTxSeq");

				gb.showLogMessage("D","一般交易調整原圈存序號 = "+gGate.imsLockSeqNo+"原交易金額 = "+gGate.oriLockAmount);

			}
			else
				dlChkAmt = ta.getDouble("AuthTxLogOriAmt_SrcTrans");

			if ((dlChkAmt-gGate.isoFiled4Value)<0) {
				ta.getAndSetErrorCode("DK");
				return false;
			}

			/** CACU_AMOUNT -- 計入OTB註記**/
			if ("N".equals(ta.getValue("AuthTxlogCacuAmount_SrcTrans"))) {
				ta.getAndSetErrorCode("DI");
				return false;
			}

			ta.setValue("AuthTxlogCacuAmount_SrcTrans", "N");
			ta.setValue("AuthTxlogCacuCash_SrcTrans", "N");

			if (gGate.isRedeemTx)
				gGate.ntAmt = ta.getDouble("AuthTxLogNtAmt_SrcTrans");

			gGate.logicDel = "J";
			if (gGate.replAmt==0)
				gGate.logicDel = "B";
			//kevin:先設定flag，帶交易成功後再更新table
	      	gGate.updateSrcTxAfterPurchaseAdj = true;
		}
		else if (gGate.cashAdjust) {//預借現金調整

			if (!gGate.pinVerified) {
				ta.addCardPasswdErrCount(gGate.cardNo);
				ta.getAndSetErrorCode("DX");
				return false;
			}
			if ((gGate.isoFiled4Value - gGate.adjustAmount)<0) {
				ta.getAndSetErrorCode("DK");
				return false;
			}
			if ( !ta.getTxlog(4) ) {
				ta.getAndSetErrorCode("DH");
				return false;
			}
			if ("N".equals(ta.getValue("AuthTxlogCacuAmount_SrcTrans"))) {
				ta.getAndSetErrorCode("DI");
				return false;
			}
			ta.setValue("AuthTxlogCacuAmount_SrcTrans", "N");
			ta.setValue("AuthTxlogCacuCash_SrcTrans", "N");

			/* ATM 預借現金調整交易須於次三日完成 */
			String slSrcTransTxDate = ta.getValue("AuthTxlogTxDate_SrcTrans");
			String slCurDate = HpeUtil.getCurDateStr(false);

			int nlBetweenDate = HpeUtil.compareDateDiffOfDay(slSrcTransTxDate, slCurDate);

			if (nlBetweenDate>3) {
				ta.getAndSetErrorCode("DJ");
				return false;
			}

			gGate.logicDel = "A";
			//kevin:先設定flag，帶交易成功後再更新table
	      	gGate.updateSrcTxAfterCashAdj = true;
		}
		return true;
	}
}
