/**
 * 授權邏輯查核-餘額查詢處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-餘額查詢處理                       *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcBalanceInq extends AuthLogic {

	public LogicProcBalanceInq(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcBalanceInq : started");

	}
	
	
	/**
	 * 餘額查詢
	 * @return 餘額查詢完成，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean processBalanceInq() throws Exception {
		gb.showLogMessage("I","processBalanceInq : started");
		boolean blResult = true;
		//V1.00.38 P3授權額度查核調整
		if (!gGate.rollbackP2) {
			gGate.balInqTotal = gGate.otbAmtCash;
			if (gGate.balInqTotal>gGate.otbAmt) {
				gGate.balInqTotal=gGate.otbAmt;
			}
			genIsoField44();

			gb.showLogMessage("D","卡號=>" + gGate.cardNoMask + "--");
			gb.showLogMessage("D","餘額1=>" + gGate.balInqTotal + "--");
			gb.showLogMessage("D","餘額2=>" + gGate.otbAmt + "--");
			gb.showLogMessage("D","isoField(44)=>" + gGate.isoField[44] + "--");
			return blResult;
		}
		String slIsoAuthCode = getPasswdFromIsoString();
		String slIsoField41= gGate.isoField[41].trim();
		if ((!("WEB".equals(slIsoField41))) || (slIsoAuthCode.length()>0) ) {
			if (!checkVoiceAuthCode()) {
				return false;
			}
		}

		//down, 判斷是否有臨時調高額度
		//double dL_AdjRate= getAdjRate();
		boolean blAdj = gGate.bgHasPersonalAdj; //checkAcctAdj(1);//ˮ֭ӤHO_{
		double dlCreditLimit= 0;
		if (blAdj) {
			dlCreditLimit= getAdjAmt();//臨調後的額度
		}
		else {
			dlCreditLimit = ta.getDouble("ActAcnoLineOfCreditAmt");//沒有臨調的額度
		}
		
		computeOtb(1, dlCreditLimit); //計算卡戶餘額 OTB
		computeCashLimit();
		computeBalanceAmt();

		gb.showLogMessage("D","balInqTotal and OtbAmt=>" + gGate.balInqTotal + "===" + gGate.otbAmt);

		if (gGate.balInqTotal>gGate.otbAmt)
			gGate.balInqTotal=gGate.otbAmt;

		genIsoField44();

		gb.showLogMessage("D","卡號=>" + gGate.cardNoMask + "--");
		gb.showLogMessage("D","餘額1=>" + gGate.balInqTotal + "--");
		gb.showLogMessage("D","餘額2=>" + gGate.otbAmt + "--");
		gb.showLogMessage("D","isoField(44)=>" + gGate.isoField[44] + "--");

		return blResult;
	}
	
	
	/**
	 * 檢核語音密碼
	 * @return 如果語音密碼檢核正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkVoiceAuthCode() throws Exception {
		boolean blResult = true;

		String slTmpVoiceAuthCode="";
		if (gGate.IsNewCard) {
			slTmpVoiceAuthCode = ta.getValue("CardBaseVoiceAuthCode").trim();
		}
		else {
			slTmpVoiceAuthCode = ta.getValue("CardBaseVoiceAuthCode2").trim();
		}
		String slVoiceCodeAfterTrans = "", slIsoVoiceCode="";
		if ( (!"BATCH".equals(gGate.connType)) && (!"WEB".equals(gGate.connType)) ){
			slVoiceCodeAfterTrans = HpeUtil.transPasswd(2, slTmpVoiceAuthCode);
			slIsoVoiceCode = getPasswdFromIsoString();
			if ( (!slVoiceCodeAfterTrans.equals(slIsoVoiceCode)) ||
					("".equals(slIsoVoiceCode))) {
				//ta.getAndSetErrorCode("ERR55");
				ta.getAndSetErrorCode("B9");
				blResult = false;
			}
		}
		return blResult;
	}
	
	/**
	 * 計算 卡戶餘額
	 * @param npType 1:個人卡；2:公司卡
	 * @param dP_AdjRate 臨調比率
	 */
	private void computeOtb(Integer npType, double dpCreditLimit) throws Exception{
		/* 卡戶餘額 OTB = [基本額度 + (預付 + 總已付Payment未消)]
        - (累計消費 + 結帳消費 + 未結消費
        +  結帳預現 + 未結預現 + 指撥金額
		+  分期未結金額)
		 */
		//如果是 debit ，則dP_CreditLimit是比率，否則dP_CreditLimit是金額 
		if (npType==1) {
			double dlCurTotUnpaid = gGate.curTotalUnpaidOfPersonal; //個人的總消費金額
			gGate.otbAmt = dpCreditLimit + 
//					ta.getDouble("CcaConsumePrePayAmt") +
					gGate.ccaConsumePrePayAmt +
					gGate.ccaConsumeTotUnPaidAmt - dlCurTotUnpaid;

			gGate.otbAmt = (int)(gGate.otbAmt + 0.5);//四捨五入
		}
		else {
			double dlCurTotUnpaid = gGate.curTotalUnpaidOfComp;

			gGate.parentOtbAmt = dpCreditLimit + 
//					ta.getDouble("CcaConsumePrePayAmtOfComp") + 
					gGate.ccaConsumePrePayAmtOfComp +
					gGate.ccaConsumeTotUnPaidAmtOfComp - dlCurTotUnpaid;

			gGate.parentOtbAmt = (int)(gGate.parentOtbAmt + 0.5);//四捨五入
		}
	}
	
	/**
	 * 取得臨調後的金額
	 * @return 臨調後的金額
	 */
	private double getAdjAmt() {
		double dlAdjAmt=0;
		if(gGate.isInstallmentTx) {
			if (ta.getDouble("CardAcctAdjInstPct")>0)
				dlAdjAmt = ta.getDouble("CardAcctAdjInstPct"); /* 臨調分期付款金額  -本來是 %         */
		}
		else {
			if (ta.getDouble("CardAcctTotAmtMonth")>0)
				dlAdjAmt = ta.getDouble("CardAcctTotAmtMonth");/* 臨調放大總月限金額  -本來是 %       */
		}
		return dlAdjAmt;
	}
	
	private boolean computeCashLimit() throws Exception {

		//計算　預借現金總額度
		boolean blConsumeFlag=false;
		//由卡戶之 card acct risk level 取得該等級之預借現金總月限百分比及回覆碼參數
		String slRiskLevel = gGate.classCode;
		//String sL_CardNote=getValue("PtrCardTypeCardNote");
		String slCardNote= ta.getValue("CardBaseCardNote");

		if (!ta.selectRiskConsumeParm("C", slRiskLevel, slCardNote)) {
			gGate.wkAdjAmt= 1;
			blConsumeFlag = false;
		}
		else {
			gGate.wkCashCode = ta.getValue("RiskConsumeRspCode1");
			gGate.wkAdjAmt= ta.getDouble("RiskConsumeLmtAmtMonthPct")/100;
			blConsumeFlag = true;
		}
		gGate.cashLimit = getCashBase()*gGate.wkAdjAmt;//預借現金總額度 (C)

		return blConsumeFlag;
	}
	
	
	/**
	 * 計算可用餘額 
	 * @throws Exception if any exception occurred
	 */
	private void computeBalanceAmt() throws Exception { /*17.檢查是否超過總月限額*/
		//檢核其他額度檢核(語音)17-22項
		//double dL_ComboAmt = ta.getDouble("CcaConsumeIbmReceiveAmt");
		/*總消費金額 = 本次交易金額 + 已授權未請款
      					+ 總未付本金
       						(結帳消費+未結帳消費+
        					結帳預現+未結帳預現)
      					+ 分期未結帳金額*/

		if (gGate.isPrecash)
			gGate.paidPreCash = gGate.ccaConsumePaidPrecash;
		else
			gGate.paidPreCash=0;

		/*總預現金額 = 本次預現金額 + 已授權未請款(預現)
      							+ 預現總未付(結帳-預現 + 未結帳-預現) */

		gb.showLogMessage("D","dL_TmpAmt1=" + gGate.totAmtPreCash  + "--" + gGate.paidPreCash + "--"+ gGate.ccaConsumeUnPaidPrecash + "--"+ gGate.ccaConsumeIbmReceiveAmt);

		double dlTmpAmt1 = gGate.totAmtPreCash + gGate.paidPreCash + gGate.ccaConsumeUnPaidPrecash;

		/*總預借月限額 = 額度 * 月限額調整倍數 + 預付款金額 + payment末消 */
		/*Combo卡比較預現總額度(含臨調後)及卡戶原授權額度, 取其較小值*/

		if( ("Y".equals(ta.getValue("COMBO_INDICATOR")))  && (gGate.cashLimit>ta.getBaseLimit()) ){
			gGate.cashLimit = ta.getBaseLimit();
		}

		/*比較預現總額度(含臨調後)及卡戶等級預現總最高額度, 取其較小值*/
		if ( (gGate.cashLimit>ta.getDouble("RiskLevelMaxCashAmt")) && (ta.getDouble("RiskLevelMaxCashAmt")>0)) {
			gGate.cashLimit = ta.getDouble("RiskLevelMaxCashAmt");
		}

		double dlTmpAmt2 = gGate.cashLimit + gGate.ccaConsumePrePayAmt + gGate.ccaConsumeTotUnPaidAmt;

		gb.showLogMessage("D","dL_TmpAmt2=" + gGate.cashLimit + "--" + ta.getDouble("CcaConsumePrePayAmt") + "--"+ gGate.ccaConsumeTotUnPaidAmt);

		if (dlTmpAmt1>dlTmpAmt2) {
			gb.showLogMessage("I", "checkCreditLimit2:超出預借現金總額");
		}
		gGate.balInqTotal = dlTmpAmt2-dlTmpAmt1;
		return ;
	}

	
	private void genIsoField44() {
		gGate.isoField[44] = "253" + HpeUtil.fillZeroOnLeft(gGate.balInqTotal,12) + HpeUtil.fillZeroOnLeft(gGate.otbAmt,12);

	}

}
