/**
 * 授權邏輯查核-票證卡號檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-票證卡號檢核處理                    *
 * 2022/01/13  V1.00.01  Kevin       TCB新簡訊發送規則                            *
 * 2022/03/28  V1.00.02  Kevin       馬爺通知所有票證都不用檢查票證卡片為退卡狀態         *
 * 2022/04/18  V1.00.03  Kevin       愛金卡每次限額調整10000                       *
 * 2022/04/19  V1.00.04  Kevin       所有票證都不須檢核鎖卡註記                      *
 * 2022/08/16  V1.00.05  Kevin       修正票證交易日累計自動加值交易金額及次數處理與沖正問題  *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckRealCardNo extends AuthLogic {
	
	public LogicCheckRealCardNo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckRealCardNo : started");

	}
	/**
	 * 票證卡號檢核作業
	 * @return 如果檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkRealCardNo() throws Exception {  //kevin:票證交易所帶進來的卡號，非real card no，所以這邊需要處理並取得real card no
		boolean blResult = true;
		if (gGate.ticketTxn) {
			gGate.tpanTicketNo = gGate.isoField[2];
		}
		if (gGate.easyAutoloadFlag || gGate.easyAutoload  || gGate.easyAutoloadChk || gGate.easyAutoloadVd || gGate.easyStandIn){
			boolean blVdFlag = false;
			if (gGate.easyAutoloadVd) {
				blVdFlag = true;
			}
			
			gGate.mccCode = "4100";//悠遊便利小額連線加值

 			if (!ta.selectTscCard(blVdFlag)) {
				ta.getAndSetErrorCode("KL"); //票證卡片不存在
				return false;
 			}

			if (gGate.reversalTrans) {
				return true;
			}
			if (!("0").equals(ta.getValue("TscCardCurrentCode"))) {
				ta.getAndSetErrorCode("KC"); //票證卡片狀況非正常
				return false;
			}
			if (gGate.easyAutoloadFlag && ("Y").equals(ta.getValue("TscAutoLoadFlag"))) {
				ta.getAndSetErrorCode("KA"); //悠遊卡自動加值功能重複啟用
				return false;
			}
			if (gGate.easyAutoload) {
				if (!("Y").equals(ta.getValue("TscAutoLoadFlag"))) {
					ta.getAndSetErrorCode("KB"); //票證卡片自動加值功能已關閉
					return false;
				}
				//馬爺通知所有票證都不用檢查票證卡片為退卡狀態
//				if (("Y").equals(ta.getValue("TscCardRuturnFlag"))) {
//					ta.getAndSetErrorCode("KD"); //票證卡片為退卡狀態
//					return false;
//				}
				//V1.00.04 所有票證都不須檢核鎖卡註記
//				if (("Y").equals(ta.getValue("TscLockFlag"))) {
//					ta.getAndSetErrorCode("KE"); //票證卡片為鎖卡狀態
//					return false;
//				}
				if (("Y").equals(ta.getValue("TscBlackltFlag"))) {
					ta.getAndSetErrorCode("KF"); //票證卡片為黑名單
					return false;
				}
				//V1.00.05 票證交易檢查當日累計自動加值交易金額及次數，將直接從授權交易記錄檔取得
				if (ta.getAutoLoad4DayCntAmt(1)) {
					if (ta.selectPtrSysParm("SYSPARM", "MKTM0640")) {
						if (ta.getInteger("AuthLoadDayCount")+1 > ta.getInteger("SysDayCnt") )  {
							ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
							return false;
						}			
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > ta.getInteger("SysDayAmt") )  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
					else {
						if (ta.getInteger("AuthLoadDayCount")+1 > 5 )  {
							ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
							return false;
						}
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > 6000)  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
				}
			}			
		}
		else if (gGate.ipassAutoload || gGate.ipassAutoloadChk || gGate.ipassStandIn) {
			gGate.mccCode = "4100";//悠遊便利小額連線加值
			if (!ta.selectIpsCard()) {
				ta.getAndSetErrorCode("KL"); //票證卡片不存在
				return false;
			}
			if (gGate.reversalTrans) {
				return true;
			}
			if (!("0").equals(ta.getValue("IpsCardCurrentCode"))) {
				ta.getAndSetErrorCode("KC"); //票證卡片狀況非正常
				return false;
			}
			if (gGate.ipassAutoload) {
				if (!("Y").equals(ta.getValue("IpsAutoLoadFlag"))) {
					ta.getAndSetErrorCode("KB"); //票證卡片自動加值功能已關閉
					return false;
				}
				//馬爺通知所有票證都不用檢查票證卡片為退卡狀態
//				if (("Y").equals(ta.getValue("IpsCardRuturnFlag"))) {
//					ta.getAndSetErrorCode("KD"); //票證卡片為退卡狀態
//					return false;
//				}
				//V1.00.04 所有票證都不須檢核鎖卡註記
//				if (("Y").equals(ta.getValue("IpsLockFlag"))) {
//					ta.getAndSetErrorCode("KE"); //票證卡片為鎖卡狀態
//					return false;
//				}
				if (("Y").equals(ta.getValue("IpsBlackltFlag"))) {
					ta.getAndSetErrorCode("KF"); //票證卡片為黑名單
					return false;
				}
				//V1.00.05 票證交易檢查當日累計自動加值交易金額及次數，將直接從授權交易記錄檔取得
				if (ta.getAutoLoad4DayCntAmt(2)) {				
					if (ta.selectPtrSysParm("SYSPARM", "IPS_0110")) {
						if (ta.getInteger("AuthLoadDayCount")+1 > ta.getInteger("SysDayCnt") )  {
							ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
							return false;
						}			
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > ta.getInteger("SysDayAmt") )  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
					else {
						if (ta.getInteger("AuthLoadDayCount")+1 > 5 )  {
							ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
							return false;
						}
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > 6000)  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
				}
			}
		}
		else if (gGate.icashAutoload || gGate.icashStandIn) {
			gGate.mccCode = "4100";//悠遊便利小額連線加值
			if (!ta.selectIchCard()) {
				ta.getAndSetErrorCode("KL"); //票證卡片不存在
				return false;
			}
			if (gGate.reversalTrans) {
				return true;
			}
			if (!("0").equals(ta.getValue("IchCardCurrentCode"))) {
				ta.getAndSetErrorCode("KC"); //票證卡片狀況非正常
				return false;
			}
			if (gGate.icashAutoload) {
				if (!("Y").equals(ta.getValue("IchAutoLoadFlag"))) {
					ta.getAndSetErrorCode("KB"); //票證卡片自動加值功能已關閉
					return false;
				}
				//馬爺通知所有票證都不用檢查票證卡片為退卡狀態
//				if (("Y").equals(ta.getValue("IchCardRuturnFlag"))) {
//					ta.getAndSetErrorCode("KD"); //票證卡片為退卡狀態
//					return false;
//				}
				//V1.00.04 所有票證都不須檢核鎖卡註記
//				if (("Y").equals(ta.getValue("IchLockFlag"))) {
//					ta.getAndSetErrorCode("KE"); //票證卡片為鎖卡狀態
//					return false;
//				}
				if (("Y").equals(ta.getValue("IchBlackltFlag"))) {
					ta.getAndSetErrorCode("KF"); //票證卡片為黑名單
					return false;
				}
				//V1.00.03 愛金卡每次限額調整
				if (gGate.isoFiled4Value > 10000 )  {
					ta.getAndSetErrorCode("KI"); //自動加值金額超過每次限額
					return false;
				}
				//V1.00.05 票證交易檢查當日累計自動加值交易金額及次數，將直接從授權交易記錄檔取得
				if (ta.getAutoLoad4DayCntAmt(3)) {				
					if (ta.selectIchParm()) {
						if (ta.getInteger("AuthLoadDayCount")+1 > ta.getInteger("IchParmDayCnt") )  {
								ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
								return false;
						}			
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > ta.getInteger("IchParmDayAmt") )  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
					else {
						if (ta.getInteger("AuthLoadDayCount")+1 > 5 )  {
							ta.getAndSetErrorCode("KG"); //自動加值次數超過每日限制
							return false;
						}
						if (ta.getInteger("AuthLoadDayAmount") + gGate.isoFiled4Value > 6000)  {
							ta.getAndSetErrorCode("KH"); //自動加值金額超過每日限制
							return false;
						}
					}
				}
			}
		} 
		//log顯示卡號，去識別化處理
		if (gGate.cardNo.length()==16) {
			String cardLeft  = gGate.cardNo.substring(0, 4);
			String cardRight = gGate.cardNo.substring(12, 16);
			gGate.cardNoMask = cardLeft+"********"+cardRight;
			gb.showLogMessage("I", "Mask Real Card No = "+gGate.cardNoMask);
		}
		return blResult;
	}

}
