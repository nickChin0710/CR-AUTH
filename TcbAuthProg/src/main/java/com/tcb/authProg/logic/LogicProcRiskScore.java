/**
 * 授權邏輯查核-風險分數計算處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-風險分數計算處理                    *
 * 2023/08/04  V1.00.49  Kevin       風險特店調整及新增特殊特店名稱檢查(eToro)           *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthProcess;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcRiskScore extends AuthLogic {

	public LogicProcRiskScore(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcRiskScore : started");

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
	public void procRiskScore() throws Exception {
		//--取得風險分數
		if (ta.getRskFactorParm()) {
			gGate.riskFactorScore = getRiskFactorScore(); 
		}
		else {
			gGate.riskFactorScore = 0; 
		}

		gb.showLogMessage("D","風險分數="+gGate.riskFactorScore);

	}
	
	private double getRiskFactorScore() throws Exception {
		double dlRiskScore = 0;
		if ("Y".equals(ta.getValue("pos_flag"))) {
			if (gGate.riskFctorInd < 1) {
				gGate.entryModeType = ta.getEntryModeType(gGate.entryMode);
				gGate.riskFctorInd  = 1; //kevin: 1. 計算 Pos Entry Mode 風險分數
			}	
			dlRiskScore += gGate.posRiskFactor;
		}
		if ("Y".equals(ta.getValue("mcc_code_flag"))) {
			if (gGate.riskFctorInd < 2) {
				if (ta.selectMccRisk()) {
					gGate.mccRiskFactor = ta.getDouble("MccRiskFactor");
				}
				gGate.riskFctorInd = 2;     //kevin: 2. 計算 Mcc_code 風險分數
			}
			dlRiskScore += gGate.mccRiskFactor;
		}
		if ("Y".equals(ta.getValue("country_flag"))) {
			if (gGate.riskFctorInd < 3) {
				if (ta.selectCountry()) {
					gGate.countryRiskFactor = ta.getDouble("ountryRiskFactor");
				}
				gGate.riskFctorInd = 3;     //kevin: 3. 計算 國別  風險分數	
			}
			dlRiskScore += gGate.countryRiskFactor;
		}
		if ("Y".equals(ta.getValue("black_mcht_flag"))) {
			if (gGate.riskFctorInd < 4) {
				ta.getMchtRiskScore();
				gGate.riskFctorInd = 4;     //kevin: 4. 計算黑名單特店  風險分數	
			}
			dlRiskScore += gGate.mchtRiskFactor;
		}
		if ("Y".equals(ta.getValue("black_card_flag"))) {
			if (gGate.riskFctorInd < 5) {
				if (ta.selectBlockCard()) {
					gGate.cardRiskFactor = ta.getDouble("card_risk_factor");
				}
				gGate.riskFctorInd = 5;     //kevin: 5. 計算 黑名單卡號 風險分數	
			}
			dlRiskScore += gGate.cardRiskFactor;
		}
		if ("Y".equals(ta.getValue("repeat_txn_flag"))) {
			if (gGate.riskFctorInd < 6) {
				gGate.repeatRiskFactor += ((gGate.ccaConsumeTxTotCntDay + gGate.ccaConsumeRejAuthCntDay) *
											ta.getDouble("repeat_factor"));

				gb.showLogMessage("D","repeat成功=" +gGate.ccaConsumeTxTotCntDay);
				gb.showLogMessage("D","repeat失敗=" +gGate.ccaConsumeRejAuthCntDay);
				gb.showLogMessage("D","repeat倍數=" +ta.getDouble("repeat_factor"));
				gb.showLogMessage("D","repeat分數=" +gGate.repeatRiskFactor);	

				gGate.riskFctorInd = 6;     //kevin: 6. 計算 重覆交易  風險分數	
			}
			dlRiskScore += gGate.repeatRiskFactor;
		}
		if ("Y".equals(ta.getValue("in_vip_flag"))) {
			if (gGate.riskFctorInd < 7) {
				if (gGate.isAuthVip) {
					gGate.vipRiskFactor = ta.getDouble("vip_factor");

					gb.showLogMessage("D","VIP分數=" +gGate.vipRiskFactor);

				}
				gGate.riskFctorInd = 7;     //kevin: 7. 計算 VIP 名單 風險分數	
			}
			dlRiskScore += gGate.vipRiskFactor;
		}
		if ("Y".equals(ta.getValue("amt_base_flag"))) {
			if (gGate.riskFctorInd < 8) {
				gGate.amtRiskFactor = Math.round(gGate.ntAmt / ta.getDouble("txn_amt_base"));
				gGate.riskFctorInd = 8;     //kevin: 8. 計算 交易金額基數 風險分數	
			}
			dlRiskScore += gGate.amtRiskFactor;
		}
		if (dlRiskScore > 999) {
			dlRiskScore = 999;
		}
		return dlRiskScore;	
	}
}
