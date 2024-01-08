/**
 * Proc 取得風險分數 - 取消此程式改由LogicProcRiskScore統一處理
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
 * 2021/02/08  V1.00.00  Kevin       Proc 取得風險分數                           *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcRiskScore extends AuthProcess {

	public ProcRiskScore(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gb.showLogMessage("I","ProcRiskScore : started");
	}
	/**
	 * //kevin: 取得風險分數
	 * 取得風險參數設定
	 * 1. 計算 Pos Entry Mode 風險分數
	 * 2. 計算 Mcc_code 風險分數
	 * 3. 計算 國別  風險分數
	 * 4. 計算 黑名單特店 風險分數
	 * 5. 計算 黑名單卡號 風險分數
	 * 6. 計算 重覆交易  風險分數
	 * 7. 計算 VIP 名單 風險分數
	 * 8. 計算 交易金額基數 風險分數
	 * 當計算分數 > 999 時 , 以最高分數999設定
	 * @author  Kevin Lin 
	 * @version 1.0
	 * @since   2020/03/19
	 * 
	 * @throws  Exception if any exception occurred 
	 * @return  boolean return True or False
	 */
//	public void procCcaStaRiskScore() throws Exception {
//		//--取得風險分數
//		if (gTa.getRskFactorParm()) {
//			gGate.riskFactorScore = getRiskFactorScore(); 
//		}
//		else {
//			gGate.riskFactorScore = 0; 
//		}
////		gGate.riskFactorScore = gTa.getRskFactorParm(); 
//		System.out.println("風險分數="+gGate.riskFactorScore);
//
//	}
//	
//	private double getRiskFactorScore() throws Exception {
//		double dlRiskScore = 0;
//		if ("Y".equals(gTa.getValue("pos_flag"))) {
//			if (gGate.riskFctorInd < 1) {
//				gGate.entryModeType = gTa.getEntryModeType(gGate.entryMode);
//				gGate.riskFctorInd  = 1; //kevin: 1. 計算 Pos Entry Mode 風險分數
//			}	
//			dlRiskScore += gGate.posRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("mcc_code_flag"))) {
//			if (gGate.riskFctorInd < 2) {
//				if (gTa.selectMccRisk()) {
//					gGate.mccRiskFactor = gTa.getDouble("MccRiskFactor");
//				}
//				gGate.riskFctorInd = 2;     //kevin: 2. 計算 Mcc_code 風險分數
//			}
//			dlRiskScore += gGate.mccRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("country_flag"))) {
//			if (gGate.riskFctorInd < 3) {
//				if (gTa.selectCountry()) {
//					gGate.countryRiskFactor = gTa.getDouble("ountryRiskFactor");
//				}
//				gGate.riskFctorInd = 3;     //kevin: 3. 計算 國別  風險分數	
//			}
//			dlRiskScore += gGate.countryRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("black_mcht_flag"))) {
//			if (gGate.riskFctorInd < 4) {
//				gTa.checkMchtRisk();
//				gGate.riskFctorInd = 4;     //kevin: 4. 計算黑名單特店  風險分數	
//			}
//			dlRiskScore += gGate.mchtRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("black_card_flag"))) {
//			if (gGate.riskFctorInd < 5) {
//				if (gTa.selectBlockCard()) {
//					gGate.cardRiskFactor = gTa.getDouble("card_risk_factor");
//				}
//				gGate.riskFctorInd = 5;     //kevin: 5. 計算 黑名單卡號 風險分數	
//			}
//			dlRiskScore += gGate.cardRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("repeat_txn_flag"))) {
//			if (gGate.riskFctorInd < 6) {
//				gGate.repeatRiskFactor += ((gGate.ccaConsumeTxTotCntDay + gGate.ccaConsumeRejAuthCntDay) *
//											gTa.getDouble("repeat_factor"));
//				System.out.println("repeat成功=" +gGate.ccaConsumeTxTotCntDay);
//				System.out.println("repeat失敗=" +gGate.ccaConsumeRejAuthCntDay);
//				System.out.println("repeat倍數=" +gTa.getDouble("repeat_factor"));
//				System.out.println("repeat分數=" +gGate.repeatRiskFactor);	
//				gGate.riskFctorInd = 6;     //kevin: 6. 計算 重覆交易  風險分數	
//			}
//			dlRiskScore += gGate.repeatRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("in_vip_flag"))) {
//			if (gGate.riskFctorInd < 7) {
//				if (gGate.isAuthVip) {
//					gGate.vipRiskFactor = gTa.getDouble("vip_factor");
//					System.out.println("VIP分數=" +gGate.vipRiskFactor);
//				}
//				gGate.riskFctorInd = 7;     //kevin: 7. 計算 VIP 名單 風險分數	
//			}
//			dlRiskScore += gGate.vipRiskFactor;
//		}
//		if ("Y".equals(gTa.getValue("amt_base_flag"))) {
//			if (gGate.riskFctorInd < 8) {
//				gGate.amtRiskFactor = Math.round(gGate.ntAmt / gTa.getDouble("txn_amt_base"));
//				gGate.riskFctorInd = 8;     //kevin: 8. 計算 交易金額基數 風險分數	
//			}
//			dlRiskScore += gGate.amtRiskFactor;
//		}
//		if (dlRiskScore > 999) {
//			dlRiskScore = 999;
//		}
//		return dlRiskScore;	
//	}
}
