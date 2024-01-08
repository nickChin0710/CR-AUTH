/**
 * 授權邏輯查核-預先授權交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-預先授權交易檢核處理                 *
 * 2022/03/17  V1.00.01  Kevin       麗花襄理要求2447交易需帶入原始RRN               *
 * 2022/03/28  V1.00.02  Kevin       VD自助加油一般授權交易圈存1500                 *
 * 2022/05/04  V1.00.03  Kevin       預先授權完成，成功取消原預先授權交易，判斷原交易是    *
 *                                   否發生在budget date之前，須扣出預先授權金額避免佔額 *
 * 2022/09/07  V1.00.04  Kevin       預先授權完成，不需要帶入原交易金額                *
 * 2022/12/07  V1.00.28  Kevin       VD卡加油站交易(5541、5542) ，只要收單銀行送入是0元交易
 *                                   不論是一般或預先授權都一律以系統設定的自助加油金額1500元來圈存。
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckPreAuth extends AuthLogic {
	
	public LogicCheckPreAuth(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckPreAuth : started");

	}
	
	
	// 處理 預先授權完成
	/**
	 * 檢核 preAuth 交易
	 * V1.00.28 VD卡加油站交易(5541、5542) ，只要收單銀行送入是0元交易，不論是一般或預先授權都一律以系統設定的自助加油金額1500元來圈存。
	 * @return 如果 preAuth 交易通過檢核，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkPreAuth() throws Exception {

		//kevin:VD自助加油預先授權交易圈存1500
		if ( gGate.selfGas || ("5541".equals(gGate.mccCode) && gGate.isoFiled4Value == 0)) {
			if (gGate.isDebitCard) {
				gGate.lockAmt = getPreAuthAmt4SelfGas();
				gGate.ntAmt = gGate.lockAmt;
				gGate.isoFiled4Value =gGate.lockAmt;
				computeBalanceAmt(gGate.isoFiled4Value,0);//kevin:VD自助加油預先授權交易圈存金額isoFiled4Value 去check VD日、月限額
			}
		}
		else {
			if (gGate.isDebitCard) {
				gGate.lockAmt = getFinalIsoField4Value();//G_Gate.isoFiled4Value;
			}
		}
		return true;
	}
	
	// 處理 預先授權完成
	/**
	 * 檢核preAuth complete 交易
	 * V1.00.38 P3授權額度查核調整-新增ROLLBACK_P2檢查
	 * V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(避免預先授權完成交易，因檢查錯誤時，跳過預先授權完成比對工作)
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkPreAuthComp() throws Exception {

		gb.showLogMessage("I","checkPreAuthComp : started");

		gGate.lockAmt = getFinalIsoField4Value(); //G_Gate.isoFiled4Value;

		if ( !ta.getTxlog(7) ) {//預授比照退貨處理，沒有找到 preAuth 交易資料			
			ta.getAndSetErrorCode("DD");//沒有找到 preAuth 交易資料
			return false;
		}
		else { 
			gGate.srcTxNtAmt = ta.getInteger("AuthTxLogNtAmt_SrcTrans");

			gb.showLogMessage("D","預先授權完成原交易金額 = "+gGate.srcTxNtAmt);

			if (gGate.isDebitCard) {
				gGate.oriLockAmount = ta.getDouble("AuthTxLogVdLockNtAmt");
				gGate.imsLockSeqNo = ta.getValue("AuthTxLogTxSeq");
				gGate.imsOriRefNo = ta.getValue("AuthTxLogRefNo");

				gb.showLogMessage("D","VD預先授權完成交易原圈存序號 = "+gGate.imsLockSeqNo+"VD預先授權完成原交易金額 = "+gGate.oriLockAmount+"VD預先授權完成原交易RRN = "+gGate.imsOriRefNo);

			}
			
			//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(避免預先授權完成交易，因檢查錯誤時，跳過預先授權完成比對工作)
          	gGate.updateSrcTxAfterPreAuthComp = true;

			if ((gGate.isoFiled4Value>gGate.srcTxNtAmt*1.15) && (!"5542".equals(gGate.mccCode)) ) {
				//Howard: mcc_code == 5542 => 自助加油

				gGate.totAmtConsume = gGate.totAmtConsume - gGate.srcTxNtAmt; 
				gGate.cardBaseWriteOff1="1";
				gGate.cardBasePreAuthFlag="0";

				//ta.getAndSetErrorCode("ERR40");
				ta.getAndSetErrorCode("DC");

//				return false;
			}
//			else {
				gGate.totAmtConsume = gGate.totAmtConsume - gGate.srcTxNtAmt ; 
				gGate.cardBaseWriteOff1="1";
				gGate.cardBasePreAuthFlag="0";
            	gGate.unLockAmt =gGate.srcTxNtAmt;
                gGate.balanceAmt = gGate.lockAmt - gGate.srcTxNtAmt;
                //V1.00.04 預先授權完成，不需要帶入原交易金額
              	
              	//V1.00.03 預先授權完成，成功取消原預先授權交易，判斷原交易是否發生在budget date之前，須扣出預先授權金額避免佔額
        		gb.showLogMessage("D","preauth complete original txn date = "+ta.getValue("AuthTxlogTxDate_SrcTrans")+"; budget date = "+ta.getValue("PtrSysParmTxlogAmtDate"));
              	if ((ta.getValue("AuthTxlogTxDate_SrcTrans").compareTo(ta.getValue("PtrSysParmTxlogAmtDate"))<=0) && gGate.rollbackP2) {
            		gb.showLogMessage("D","budget 1 amt = "+ta.getDouble("CcaConsumeAuthTxlogAmt1")+" ;preauth complete for orignal txn amt ="+ ta.getDouble("AuthTxLogNtAmt_SrcTrans"));
            		gGate.reversalBudgetAmt = true;
            		if ("Y".equals(ta.getValue("AuthTxlogCacuCash_SrcTrans"))) {
                		gGate.reversalBudgetAmtCash = true;
            		}
              	}
			
				return true;
//			}
		}
	}
	
	
	/**
	 * 取得自助加油的圈存金額
	 * @return 自助加油的圈存金額
	 * 	@throws Exception if any exception occurred 
	 * 移至AuthLogic.java處理 * 2022/03/28  V1.00.08 VD自助加油一般授權交易圈存1500 
	 */
//	private double getPreAuthAmt4SelfGas() throws Exception {
//
//		gb.showLogMessage("I","getPreAuthAmt4SelfGas : started");
//		
//		double dlResult = 0;
//		//kevin:自助加油預先授權金額與參數GAS-SELF-AMT超過CCA_SYS_PARM2的長度限制，改成GAS-AMT
//		if ( !ta.selectSysParm2("VD-GAS-AMT","LOCK","Nvl(SYS_DATA2,'1500')") ) {
//			ta.setValue("SYS_DATA2","1500"); 
//		}
//		dlResult = ta.getDouble("SYS_DATA2");//sB
//		return dlResult;
//	}
}
