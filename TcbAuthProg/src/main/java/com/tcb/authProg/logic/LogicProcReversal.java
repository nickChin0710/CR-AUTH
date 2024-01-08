/**
 * 授權邏輯查核-沖銷交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-沖銷交易檢核處理                    *
 * 2022/03/30  V1.00.01  Kevin       ECS人工沖正處理與沖正成功檢查原交易是否發生在       *
 *                                   budget date之前，須扣出沖正後金額避免佔額        *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcReversal extends AuthLogic {
	
	public LogicProcReversal(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcReversal : started");

	}
	
	// 沖銷交易訊息 04XX
	/**
	 * 處理沖銷交易
	 * V1.00.37 P3紅利兌換處理方式調整-授權碼補入oriAuthNo
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * @return 如果完成沖銷處理，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean procReversal() throws Exception {
		boolean blResult = true;
		int nlTransType = 5;
		gb.showLogMessage("I","reversalControl 沖銷作業: " + gGate.mesgType);
//		//確認沖銷前原始response code 如果不正常，就不需要做沖正處理。
//		if (gGate.oriRespCode.length()>0 && !"00".equals(gGate.oriRespCode)) {
//			gb.showLogMessage("D","reversal for reject txn response code = "+gGate.oriRespCode);
//			ta.getAndSetErrorCode("D8"); //查無原授權交易(沖銷交易一律回00)
//			return false;
//		}
		
		if ((gGate.ticketTxn) || ("").equals(gGate.oriAuthNo)) {
			nlTransType = 6 ; //
		}
		
		if ("WEB".equals(gGate.connType)) {
			nlTransType = 8 ; //V1.00.01 ECS人工沖正處理與沖正成功檢查原交易是否發生在budget date之前，須扣出沖正後金額避免佔額
		}
		
		if ( !ta.getTxlog(nlTransType) ) {
			//ta.getAndSetErrorCode("ERR44");
			ta.getAndSetErrorCode("D8"); //查無原授權交易(沖銷交易一律回00)
			return false;
		}

		checkOnlineRedeem();

		gb.showLogMessage("D","@@@@check online redeem = "+gGate.authUnit+gGate.isInstallmentTx+gGate.isRedeemTx+gGate.c5TxFlag);

		if ((gGate.isInstallmentTx) || ((gGate.isRedeemTx)) && ("K".equals(gGate.authUnit)) ){
			gb.showLogMessage("I","@@@@gcheck online redeem fales = "+gGate.authUnit+gGate.isInstallmentTx+gGate.isRedeemTx+gGate.c5TxFlag);
			return false;
		}

		gb.showLogMessage("D","@@@@AuthTxlogCacuAmount_SrcTrans = "+ta.getValue("AuthTxlogCacuAmount_SrcTrans"));
		if ("N".equals(ta.getValue("AuthTxlogCacuAmount_SrcTrans"))) {
			ta.getAndSetErrorCode("DI");
			return false;

		}
		if (gGate.oriAuthNo.isEmpty()) {
			gGate.oriAuthNo = ta.getValue("AuthTxLogAuthNo");
		}
		//kevin:調整沖正規則與退貨一致
		double dlChkAmt = 0;
		if (gGate.isDebitCard) {
			dlChkAmt = ta.getDouble("AuthTxLogVdLockNtAmt");
			gGate.oriLockAmount = dlChkAmt;
			gGate.lockAmt = dlChkAmt;
			gGate.imsLockSeqNo = ta.getValue("AuthTxLogTxSeq");

			gb.showLogMessage("D","沖正交易原圈存序號 = "+gGate.imsLockSeqNo+"原交易金額 = "+gGate.oriLockAmount);

			gGate.unlockFlag = "R"; //--N.未解圈, Y.己解圈 R.沖正成功
		}
		ta.setValue("AuthTxlogCacuAmount_SrcTrans", "N");
		ta.setValue("AuthTxlogCacuCash_SrcTrans", "N");
		gGate.reversalFlag = "Y"; //--Y.沖正成功

		if (gGate.isRedeemTx)
			gGate.isoFiled4Value = ta.getDouble("AuthTxLogNtAmt_SrcTrans");
		//kevin:先設定update flag，帶交易成功再更新table
      	gGate.updateSrcTxAfterReversal = true;
      	
      	//V1.00.01 ECS人工沖正處理與沖正成功檢查原交易是否發生在budget date之前，須扣出沖正後金額避免佔額
		gb.showLogMessage("D","original txn date = "+ta.getValue("AuthTxlogTxDate_SrcTrans")+"; budget date = "+ta.getValue("PtrSysParmTxlogAmtDate"));
      	if ((ta.getValue("AuthTxlogTxDate_SrcTrans").compareTo(ta.getValue("PtrSysParmTxlogAmtDate"))<=0) && gGate.rollbackP2) {
//      	ta.setValue("CcaConsumeAuthTxlogAmt1",""+(ta.getDouble("CcaConsumeAuthTxlogAmt1") - ta.getDouble("AuthTxLogNtAmt_SrcTrans")));
    		gb.showLogMessage("D","budget 1 amt = "+ta.getDouble("CcaConsumeAuthTxlogAmt1")+" ;orignal txn amt ="+ ta.getDouble("AuthTxLogNtAmt_SrcTrans"));
    		gGate.reversalBudgetAmt = true;
    		if ("Y".equals(ta.getValue("AuthTxlogCacuCash_SrcTrans"))) {
        		gGate.reversalBudgetAmtCash = true;
    		}
      	}

		return blResult;
		/*
     1. 找到原交易 => 在 ta.getTxlog(5) 中做掉了
     2. 解圈原交易之金額
     3. 將原交易改為已解圈、已沖銷退貨調整
     4. 還原額度
     5. 新增此交易的log
		 */
	}
}
