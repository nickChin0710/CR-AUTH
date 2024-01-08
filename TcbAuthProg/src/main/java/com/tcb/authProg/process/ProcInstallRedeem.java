/**
 * Proc 處理Ecs Installment & redeem交易的流程
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
 * 2021/02/08  V1.00.00  Kevin       Proc 處理Ecs Installment & redeem交易的流程  *
 * 2022/02/22  V1.00.01  Kevin       ECS計算分期資料後，避免null導致資料位移           *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/11/24  V1.00.59  Kevin       修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能 *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.bil.BilO101;
import com.tcb.authProg.bil.BilO102;
import com.tcb.authProg.bil.BilO105;
import com.tcb.authProg.bil.BilO201;
import com.tcb.authProg.bil.BilO202;
import com.tcb.authProg.bil.BilO205;
import com.tcb.authProg.bil.InstallmentTxData;
import com.tcb.authProg.bil.RedeemTxData;
import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcInstallRedeem extends AuthProcess {
	
	public ProcInstallRedeem(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gb.showLogMessage("I","ProcInstallRedeem : started");

	}
	/**
	 * ECS紅利折抵功能與NCCC分期付款功能
	 * V1.00.59 修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能
	 * 初始化授權交易變數值
	 * @throws Exception if any exception occurred 
	 */
	public boolean ccasEcsTrans() throws Exception {

		if ("N".equals(gGb.getIfEnableEcsTrans()))
			return true;
		//第一階段只要處理分期付款(NCCC)493817
		if (((gGate.isInstallmentTx) && "493817".equals(gGate.isoField[32])) || (gGate.isRedeemTx)) {

			if (gGate.isRedeemTx)  {
				if (gGate.rejectBonus) {
					gTa.getAndSetErrorCode("ER");
					gGate.authRemark = "授權系統強制關閉ECS紅利折抵功能，拒絕所有紅利折抵交易";
					return false;
				}
			}
			if (gGate.isInstallmentTx)  {
				if (gGate.rejectInstallment) {
					gTa.getAndSetErrorCode("ER");
					gGate.authRemark = "授權系統強制關閉NCCC分期付款功能，拒絕NCCC分期付款交易";
					return false;
				}
			}			
			RedeemTxData lRedeemTxData = null;
			InstallmentTxData lInstallmentTxData = null;

			String slTxIndicator="";
			String slMsgType = gGate.mesgType;
			
			String slSubProcessCode = gGate.isoField[3].substring(0, 2);
			if("0100".equals(slMsgType)  && "00".equals(slSubProcessCode)) {
				if(gGate.isRedeemTx) {
					slTxIndicator = "00000002";
					BilO101 lBilO101 = new BilO101(gGate, gGb, gTa, slTxIndicator);
					lRedeemTxData = lBilO101.gRedeemTxData;

				} 
				else if(gGate.isInstallmentTx) { 
					slTxIndicator = "00000001";
					BilO201 lBilO201 = new BilO201(gGate, gGb, gTa, slTxIndicator);
					lInstallmentTxData = lBilO201.gInstallTxData; 
				}
			}
			else if("0100".equals(slMsgType) || "0120".equals(slMsgType) && "20".equals(slSubProcessCode)) {

				if(gGate.isRedeemTx) {
					slTxIndicator = "00000002";
					BilO102 lBilO102 = new BilO102(gGate, gGb, gTa, slTxIndicator);
					lRedeemTxData = lBilO102.gRedeemTxData;


				} 
				else if(gGate.isInstallmentTx) { 
					slTxIndicator = "00000001";
					BilO202 lBilO202 = new BilO202(gGate, gGb, gTa, slTxIndicator);
					lInstallmentTxData = lBilO202.gInstallTxData;
				}
			}
			else if("0420".equals(slMsgType) && "00".equals(slSubProcessCode)) {

				if(gGate.isRedeemTx) { 
					slTxIndicator = "00000002";
					BilO105 lBilO105 = new BilO105(gGate, gGb, gTa, slTxIndicator);
					lRedeemTxData = lBilO105.gRedeemTxData;

				} 
				else if(gGate.isInstallmentTx) { 
					slTxIndicator = "00000001";
					BilO205 lBilO205 = new BilO205(gGate, gGb, gTa, slTxIndicator);
					lInstallmentTxData = lBilO205.gInstallTxData;

				}
			}

			String slReturnMsgType = "";
			if (gGate.isRedeemTx) {
				// fix issue "Null Dereference" 2020/09/16 Zuwei
				if (lRedeemTxData != null) {
					gGate.isoField[39] = lRedeemTxData.getRespCode();
					gGate.loyaltyTxResp = lRedeemTxData.getRespCode();
					slReturnMsgType = lRedeemTxData.getReturnMesgType();
					gGate.authRemark = lRedeemTxData.getAuthRemark();
				}
			}
			else if (gGate.isInstallmentTx) {
				// fix issue "Null Dereference" 2020/09/16 Zuwei
				if (lInstallmentTxData != null) {
					gGate.isoField[39] = lInstallmentTxData.getRespCode();
					gGate.installTxRespCde = lInstallmentTxData.getRespCode();
					slReturnMsgType = lInstallmentTxData.getReturnMesgType();
					gGate.authRemark = lInstallmentTxData.getAuthRemark();
				}
			}

			if (!"00".equals(gGate.isoField[39])) {
				//kevin:AuthRemark調整
//				G_Gate.AuthRemark = "  ECS回覆碼 " + G_Gate.isoField[39];
				//G_Ta.getAndSetErrorCode("ECS04");/* rejected by ECS */
				gTa.getAndSetErrorCode("ER");/* rejected by ECS */
				return false;
			}
			if (!slReturnMsgType.isEmpty()) {
				if (!"1".equals(slReturnMsgType.substring(2,3))) {
					return true;
				}
			}
			else {
				gTa.getAndSetErrorCode("ER");/* rejected by ECS */
				return false;
			}
			
			if (gGate.isRedeemTx) {
				//處理 ECS 的 return
				/*
            	Howard: 這裡要assign token C5 的值...
				 */
				// fix issue "Null Dereference" 2020/09/16 Zuwei
				if (lRedeemTxData != null) {
					gGate.pointRedemption = lRedeemTxData.getPointRede();//扣抵點數
	
					gGate.pointBalance = lRedeemTxData.getValue1(); //剩餘點數
					gGate.paidCreditAmt = lRedeemTxData.getPointsAmt(); //可折抵金額 
					gGate.remainingCreditAmt =  lRedeemTxData.getValue2(); //支付金額
				}


				gGate.loyaltyTxId = "1";
				if (Double.parseDouble(gGate.paidCreditAmt) < gGate.ntAmt) {
					gGate.loyaltyTxId = "3";
				} else {
					gGate.loyaltyTxId = "2";
				}
				gGate.signBalance = "P";
				/** P 正數 **/
				if (Integer.parseInt(gGate.pointBalance) < 0) {
					gGate.signBalance = "N";/** N 負數 **/
				}
				gGate.f58T21 = "000000" + gGate.loyaltyTxId + gGate.isoField[39] + gGate.signBalance + gGate.pointBalance + gGate.pointRedemption + gGate.remainingCreditAmt;
				gGb.showLogMessage("D","G_Gate.loyaltyTxId ="+ gGate.loyaltyTxId
									  +";G_Gate.signBalance ="+ gGate.signBalance
									  +";G_Gate.pointBalance ="+ gGate.pointBalance
									  +";G_Gate.pointRedemption ="+ gGate.pointRedemption
									  +";G_Gate.paidCreditAmt ="+ gGate.paidCreditAmt
									  +";G_Gate.remainingCreditAmt ="+ gGate.remainingCreditAmt);
				gGb.showLogMessage("D",";G_Gate.f58t21 ="+ gGate.f58T21);

			}
			else if (gGate.isInstallmentTx) {
				//處理 ECS 的 return
				/*
            	Howard: 這裡要assign token C5 的值...
				 */

				// fix issue "Null Dereference" 2020/09/16 Zuwei
				//V1.00.01 ECS計算分期資料後，避免null導致資料位移
				if (lInstallmentTxData != null) {
					if (lInstallmentTxData.getTotalTerm() != null) {
						gGate.divNum = lInstallmentTxData.getTotalTerm(); //分期數
					}
					if (lInstallmentTxData.getValue1() != null) {
						gGate.firstAmt = lInstallmentTxData.getValue1();//Howard:首期金額
					}
					if (lInstallmentTxData.getValue2() != null) {
						gGate.everyAmt = lInstallmentTxData.getValue2();//Howard:每期金額
					}
					if (lInstallmentTxData.getValue3() != null) {
						gGate.procAmt = lInstallmentTxData.getValue3(); //手續費
					}
				}
				gGb.showLogMessage("D","Gate.divNum ="+ gGate.divNum
						  +";Gate.firstAmt ="+ gGate.firstAmt
						  +";Gate.everyAmt ="+ gGate.everyAmt
						  +";Gate.procAmt ="+ gGate.procAmt);
			}

			gGate.tokenIdC5 ="C5";
		}
		return true;
	}
}
