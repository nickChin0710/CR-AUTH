/**
 * Proc 處理統計處理作業 
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
 * 2021/02/08  V1.00.00  Kevin       Proc 處理統計處理作業                        *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcStaticData extends AuthProcess {

	public ProcStaticData(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gb.showLogMessage("I","ProcStaticData : started");

	}

	public void writeStaticData() {
		
		/**-----------  統計處理作業 -------------**/
		setTxSessionValue();
		procCcaStaRiskType();
		procCcaStaDailyMcc();
		procCcaStaTxUnNormal();
	}
	private void procCcaStaTxUnNormal() {
		if (!gGate.bgAbnormalResp)
			return;
	
		boolean blHasData = gTa.selectStaTxUnNormal();    	
	
		if (blHasData) {
			gGate.ngStaTxUnNormalTxCnt = gTa.getInteger("StaTxUnNormalTxCnt") + 1;
	
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) || (gGate.refundAdjust))
				gGate.ngStaTxUnNormalTxAmt =  gTa.getInteger("StaTxUnNormalTxAmt") + (int)gGate.replAmt;
			else
				gGate.ngStaTxUnNormalTxAmt = gTa.getInteger("StaTxUnNormalTxAmt") + (int)gGate.ntAmt;
	
			gTa.updateStaTxUnormal();
	
		}
		else {
			gTa.insertStaTxUnormal();
		}
	}
	/*
	 * 處理統計處理作業-取得授權來源port number
	 * V1.00.48 P3程式碼整理(購貨交易才需要做統計處理資料)
	 * @throws Exception if any exception occurred
	 */
	private void setTxSessionValue() {
		String slTmp="";
		int nlBeginIndex=0;
		if (gGate.connType.equals("FISC")) {
			slTmp= "FISC"+ gGb.getFiscPort();
	
		}
		else {
			slTmp = "WEB"+gGb.getInternalAuthServerPort4Online(); 
	
		}
	
		if (slTmp.length()>4) {
			nlBeginIndex = slTmp.length()-4;
			slTmp = slTmp.substring(nlBeginIndex, nlBeginIndex+4);
		}
	
		gGate.ngTxSession = Integer.parseInt(slTmp);

		if ("00".equals(gGate.isoField[39])) {
			gGate.bgAbnormalResp = false;
		}
	
	}    
	/*
	 * 處理統計處理作業-MCC授權交易統計檔
	 * V1.00.48 P3程式碼整理(購貨交易才需要做統計處理資料)
	 * @throws Exception if any exception occurred
	 */
	private void procCcaStaDailyMcc() {
		boolean blHasData = gTa.selectStaDailyMcc();
	
		if (blHasData) {
			gGate.ngStaDailyMccAuthCnt = gTa.getInteger("StaDailyMccAuthCnt");
			gGate.ngStaDailyMccAuthAmt = gTa.getInteger("StaDailyMccAuthAmt");
	
			gGate.ngStaDailyMccCallBankCntx = gTa.getInteger("StaDailyMccCallBankCntx");
			gGate.ngStaDailyMccCallBankAmtx = gTa.getInteger("StaDailyMccCallBankAmtx");
			gGate.ngStaDailyMccCallBankCnt = gTa.getInteger("StaDailyMccCallBankCnt");
			gGate.ngStaDailyMccCallBankAmt = gTa.getInteger("StaDailyMccCallBankAmt");
	
			gGate.ngStaDailyMccDeclineCnt = gTa.getInteger("StaDailyMccDeclineCnt");
			gGate.ngStaDailyMccDeclineAmt = gTa.getInteger("StaDailyMccDeclineAmt");
	
			gGate.ngStaDailyMccPickupCnt = gTa.getInteger("StaDailyMccPickupCnt");
			gGate.ngStaDailyMccPickupAmt = gTa.getInteger("StaDailyMccPickupAmt");
	
			gGate.ngStaDailyMccExpiredCnt = gTa.getInteger("StaDailyMccExpiredCnt");
			gGate.ngStaDailyMccExpiredAmt = gTa.getInteger("StaDailyMccExpiredAmt");
	
	
			gGate.ngStaDailyMccConsumeCnt = gTa.getInteger("StaDailyMccConsumeCnt");
			gGate.ngStaDailyMccConsumeAmt = gTa.getInteger("StaDailyMccConsumeAmt");
	
			gGate.ngStaDailyMccGenerCnt = gTa.getInteger("StaDailyMccGenerCnt");
			gGate.ngStaDailyMccGenerAmt = gTa.getInteger("StaDailyMccGenerAmt");
	
			gGate.ngStaDailyMccCashCnt = gTa.getInteger("StaDailyMccCashCnt");
			gGate.ngStaDailyMccCashAmt = gTa.getInteger("StaDailyMccCashAmt");
			gGate.ngStaDailyMccReturnCnt = gTa.getInteger("StaDailyMccReturnCnt");
			gGate.ngStaDailyMccReturnAmt = gTa.getInteger("StaDailyMccReturnAmt");
	
	
			gGate.ngStaDailyMccAdjustCnt = gTa.getInteger("StaDailyMccAdjustCnt");
			gGate.ngStaDailyMccAdjustAmt = gTa.getInteger("StaDailyMccAdjustAmt");
	
			gGate.ngStaDailyMccReturnAdjCnt = gTa.getInteger("StaDailyMccReturnAdjCnt");
			gGate.ngStaDailyMccReturnAdjAmt = gTa.getInteger("StaDailyMccReturnAdjAmt");
	
			gGate.ngStaDailyMccForceCnt = gTa.getInteger("StaDailyMccForceCnt");
			gGate.ngStaDailyMccForceAmt = gTa.getInteger("StaDailyMccForceAmt");
	
			gGate.ngStaDailyMccMailCnt = gTa.getInteger("StaDailyMccMailCnt");
			gGate.ngStaDailyMccMailAmt = gTa.getInteger("StaDailyMccMailAmt");
	
			gGate.ngStaDailyMccPreauthCnt = gTa.getInteger("StaDailyMccPreauthCnt");
			gGate.ngStaDailyMccPreauthAmt = gTa.getInteger("StaDailyMccPreauthAmt");
	
			gGate.ngStaDailyMccPreauthOkCnt = gTa.getInteger("StaDailyMccPreauthOkCnt");
			gGate.ngStaDailyMccPreauthOkAmt = gTa.getInteger("StaDailyMccPreauthOkAmt");
	
			gGate.ngStaDailyMccCashAdjCnt = gTa.getInteger("StaDailyMccCashAdjCnt");
			gGate.ngStaDailyMccCashAdjAmt = gTa.getInteger("StaDailyMccCashAdjAmt");
	
			gGate.ngStaDailyMccReversalCnt = gTa.getInteger("StaDailyMccReversalCnt");
			gGate.ngStaDailyMccReversalAmt = gTa.getInteger("StaDailyMccReversalAmt");
	
			gGate.ngStaDailyMccEcCnt = gTa.getInteger("StaDailyMccEcCnt");
			gGate.ngStaDailyMccEcAmt = gTa.getInteger("StaDailyMccEcAmt");
	
	
			gGate.ngStaDailyMccUnNormalCnt = gTa.getInteger("StaDailyMccUnNormalCnt");
			gGate.ngStaDailyMccUnNormalAmt = gTa.getInteger("StaDailyMccUnNormalAmt");
	
	
		}
		String slIsoRepCode= getIsoRespCode();
	
		if ("00".equals(slIsoRepCode)) {
			gGate.ngStaDailyMccAuthCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaDailyMccAuthAmt += gGate.replAmt;
			else
				gGate.ngStaDailyMccAuthAmt += gGate.ntAmt;
	
	
		}
		else if ("01".equals(slIsoRepCode)) {
			gGate.ngStaDailyMccCallBankCntx++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaDailyMccCallBankAmt += gGate.replAmt;
			else
				gGate.ngStaDailyMccCallBankAmt += gGate.ntAmt;
	
		}
		else if ("54".equals(slIsoRepCode)) {
			gGate.ngStaDailyMccExpiredCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaDailyMccExpiredAmt += gGate.replAmt;
			else
				gGate.ngStaDailyMccExpiredAmt += gGate.ntAmt;
		}
		else if ("41".equals(slIsoRepCode) || "43".equals(slIsoRepCode)) {
			gGate.ngStaDailyMccPickupCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaDailyMccPickupAmt += gGate.replAmt;
			else
				gGate.ngStaDailyMccPickupAmt += gGate.ntAmt;
	
		}
		else  {
			gGate.ngStaDailyMccDeclineCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaDailyMccDeclineAmt += gGate.replAmt;
			else
				gGate.ngStaDailyMccDeclineAmt += gGate.ntAmt;
	
		}
	
		gGate.ngStaDailyMccConsumeCnt++;
	
		if ( (gGate.purchAdjust) || (gGate.cashAdjust) || (gGate.refundAdjust))
			gGate.ngStaDailyMccConsumeAmt += gGate.replAmt;
		else
			gGate.ngStaDailyMccConsumeAmt += gGate.ntAmt;
	
	
	
		if (gGate.normalPurch){
			gGate.ngStaDailyMccGenerCnt++;
			gGate.ngStaDailyMccGenerAmt += gGate.ntAmt;
		}
		else if (gGate.cashAdvance){
			gGate.ngStaDailyMccCashCnt++;
			gGate.ngStaDailyMccCashAmt += gGate.ntAmt;
		}
		else if (gGate.refund){
			gGate.ngStaDailyMccReturnCnt++;
			gGate.ngStaDailyMccReturnAmt += gGate.ntAmt;
		}
		else if (gGate.purchAdjust){
			gGate.ngStaDailyMccAdjustCnt++;
			gGate.ngStaDailyMccAdjustAmt += gGate.ntAmt;
		}
		else if (gGate.refundAdjust){
			gGate.ngStaDailyMccReturnAdjCnt++;
			gGate.ngStaDailyMccReturnAdjAmt += gGate.replAmt;
		}
		else if (gGate.forcePosting){
			gGate.ngStaDailyMccForceCnt++;
			gGate.ngStaDailyMccForceAmt += gGate.ntAmt;
		}
		else if (gGate.mailOrder){
			gGate.ngStaDailyMccMailCnt++;
			gGate.ngStaDailyMccMailAmt += gGate.ntAmt;
		}
		else if (gGate.preAuth){
			gGate.ngStaDailyMccPreauthCnt++;
			gGate.ngStaDailyMccPreauthAmt += gGate.ntAmt;
		}
		else if (gGate.preAuthComp){
			gGate.ngStaDailyMccPreauthOkCnt++;
			gGate.ngStaDailyMccPreauthOkAmt += gGate.ntAmt;
		}
		else if (gGate.cashAdjust){
			gGate.ngStaDailyMccCashAdjCnt++;
			gGate.ngStaDailyMccCashAdjAmt += gGate.ntAmt;
		}
		else if (gGate.reversalTrans){
			gGate.ngStaDailyMccReversalCnt++;
			gGate.ngStaDailyMccReversalAmt += gGate.ntAmt;
		}
	
		if (gGate.ecTrans) {
			gGate.ngStaDailyMccEcCnt++;    		
			gGate.ngStaDailyMccEcAmt += gGate.ntAmt;
		}
	
		if (gGate.bgAbnormalResp) {
			gGate.sgStaDailyMccUnNormalFlag="Y";
			gGate.ngStaDailyMccUnNormalCnt++;
			gGate.ngStaDailyMccUnNormalAmt += gGate.ntAmt;
	
		}
	
		if (blHasData) {
			gTa.updateStaDailyMcc();
		}
		else {
			gTa.insertStaDailyMcc();
		}
	
	
	}
	
	private String getIsoRespCode() {
		String slIsoRepCode="";
		if ( (!"".equals(gGate.isoField[39].trim())) && (gGate.isoField[39].trim().length()>=2) )
			slIsoRepCode= gGate.isoField[39].substring(0, 2);    	
	
		return slIsoRepCode;
	}
	/*
	 * 處理統計處理作業-風險分類授權交易統計檔
	 * V1.00.48 P3程式碼整理(購貨交易才需要做統計處理資料)
	 * @throws Exception if any exception occurred
	 */
	private void procCcaStaRiskType() {
		//proc is TB_sta_rsk_type
	
		boolean blHasData = gTa.selectStaRiskType(); 
	
	
	
		if (blHasData) {
			gGate.ngStaRiskTypeAuthCnt = gTa.getInteger("StaRiskTypeAuthCnt");
			gGate.ngStaRiskTypeAuthAmt = gTa.getInteger("StaRiskTypeAuthAmt");
	
			gGate.ngStaRiskTypeCallBankCnt = gTa.getInteger("StaRiskTypeCallBankCnt");
			gGate.ngStaRiskTypeCallBankAmt = gTa.getInteger("StaRiskTypeCallBankAmt");
	
			gGate.ngStaRiskTypeCallBankCntx = gTa.getInteger("StaRiskTypeCallBankCntx");
			gGate.ngStaRiskTypeCallBankAmtx = gTa.getInteger("StaRiskTypeCallBankAmtx");
	
			gGate.ngStaRiskTypeDeclineCnt = gTa.getInteger("StaRiskTypeDeclineCnt");
			gGate.ngStaRiskTypeDeclineAmt = gTa.getInteger("StaRiskTypeDeclineAmt");
	
			gGate.ngStaRiskTypePickupCnt = gTa.getInteger("StaRiskTypePickupCnt");
			gGate.ngStaRiskTypePickupAmt = gTa.getInteger("StaRiskTypePickupAmt");
	
			gGate.ngStaRiskTypeExpiredCnt = gTa.getInteger("StaRiskTypeExpiredCnt");
			gGate.ngStaRiskTypeExpiredAmt = gTa.getInteger("StaRiskTypeExpiredAmt");
	
	
			gGate.ngStaRiskTypeConsumeCnt = gTa.getInteger("StaRiskTypeConsumeCnt");
			gGate.ngStaRiskTypeConsumeAmt = gTa.getInteger("StaRiskTypeConsumeAmt");
	
			gGate.ngStaRiskTypeGenerCnt = gTa.getInteger("StaRiskTypeGenerCnt");
			gGate.ngStaRiskTypeGenerAmt = gTa.getInteger("StaRiskTypeGenerAmt");
	
			gGate.ngStaRiskTypeCashCnt = gTa.getInteger("StaRiskTypeCashCnt");
			gGate.ngStaRiskTypeCashAmt = gTa.getInteger("StaRiskTypeCashAmt");
			gGate.ngStaRiskTypeReturnCnt = gTa.getInteger("StaRiskTypeReturnCnt");
			gGate.ngStaRiskTypeReturnAmt = gTa.getInteger("StaRiskTypeReturnAmt");
	
	
			gGate.ngStaRiskTypeAdjustCnt = gTa.getInteger("StaRiskTypeAdjustCnt");
			gGate.ngStaRiskTypeAdjustAmt = gTa.getInteger("StaRiskTypeAdjustAmt");
	
			gGate.ngStaRiskTypeReturnAdjCnt = gTa.getInteger("StaRiskTypeReturnAdjCnt");
			gGate.ngStaRiskTypeReturnAdjAmt = gTa.getInteger("StaRiskTypeReturnAdjAmt");
	
			gGate.ngStaRiskTypeForceCnt = gTa.getInteger("StaRiskTypeForceCnt");
			gGate.ngStaRiskTypeForceAmt = gTa.getInteger("StaRiskTypeForceAmt");
	
			gGate.ngStaRiskTypeMailCnt = gTa.getInteger("StaRiskTypeMailCnt");
			gGate.ngStaRiskTypeMailAmt = gTa.getInteger("StaRiskTypeMailAmt");
	
			gGate.ngStaRiskTypePreauthCnt = gTa.getInteger("StaRiskTypePreauthCnt");
			gGate.ngStaRiskTypePreauthAmt = gTa.getInteger("StaRiskTypePreauthAmt");
	
			gGate.ngStaRiskTypePreauthOkCnt = gTa.getInteger("StaRiskTypePreauthOkCnt");
			gGate.ngStaRiskTypePreauthOkAmt = gTa.getInteger("StaRiskTypePreauthOkAmt");
	
			gGate.ngStaRiskTypeCashAdjCnt = gTa.getInteger("StaRiskTypeCashAdjCnt");
			gGate.ngStaRiskTypeCashAdjAmt = gTa.getInteger("StaRiskTypeCashAdjAmt");
	
			gGate.ngStaRiskTypeReversalCnt = gTa.getInteger("StaRiskTypeReversalCnt");
			gGate.ngStaRiskTypeReversalAmt = gTa.getInteger("StaRiskTypeReversalAmt");
	
			gGate.ngStaRiskTypeEcCnt = gTa.getInteger("StaRiskTypeEcCnt");
			gGate.ngStaRiskTypeEcAmt = gTa.getInteger("StaRiskTypeEcAmt");
	
	
			gGate.ngStaRiskTypeUnNormalCnt = gTa.getInteger("StaRiskTypeUnNormalCnt");
			gGate.ngStaRiskTypeUnNormalAmt = gTa.getInteger("StaRiskTypeUnNormalAmt");
	
	
		}
	
		String slIsoRepCode= getIsoRespCode();
		if ("00".equals(slIsoRepCode)) {
			gGate.ngStaRiskTypeAuthCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaRiskTypeAuthAmt += gGate.replAmt;
			else
				gGate.ngStaRiskTypeAuthAmt += gGate.ntAmt;
	
	
		}
		else if ("01".equals(slIsoRepCode)) {
			gGate.ngStaRiskTypeCallBankCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaRiskTypeCallBankAmt += gGate.replAmt;
			else
				gGate.ngStaRiskTypeCallBankAmt += gGate.ntAmt;
	
		}
		else if ("54".equals(slIsoRepCode)) {
			gGate.ngStaRiskTypeExpiredCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaRiskTypeExpiredAmt += gGate.replAmt;
			else
				gGate.ngStaRiskTypeExpiredAmt += gGate.ntAmt;
		}
		else if ("41".equals(slIsoRepCode) || "43".equals(slIsoRepCode)) {
			gGate.ngStaRiskTypePickupCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaRiskTypePickupAmt += gGate.replAmt;
			else
				gGate.ngStaRiskTypePickupAmt += gGate.ntAmt;
	
		}
		else  {
			gGate.ngStaRiskTypeDeclineCnt++;
	
			if ( (gGate.purchAdjust) || (gGate.cashAdjust) )
				gGate.ngStaRiskTypeDeclineAmt += gGate.replAmt;
			else
				gGate.ngStaRiskTypeDeclineAmt += gGate.ntAmt;
	
		}
	
		gGate.ngStaRiskTypeConsumeCnt++;
		if ( (gGate.purchAdjust) || (gGate.cashAdjust) || (gGate.refundAdjust))
			gGate.ngStaRiskTypeConsumeAmt += gGate.replAmt;
		else
			gGate.ngStaRiskTypeConsumeAmt += gGate.ntAmt;
	
	
	
		if (gGate.normalPurch){
			gGate.ngStaRiskTypeGenerCnt++;
			gGate.ngStaRiskTypeGenerAmt += gGate.ntAmt;
		}
		else if (gGate.cashAdvance){
			gGate.ngStaRiskTypeCashCnt++;
			gGate.ngStaRiskTypeCashAmt += gGate.ntAmt;
		}
		else if (gGate.refund){
			gGate.ngStaRiskTypeReturnCnt++;
			gGate.ngStaRiskTypeReturnAmt += gGate.ntAmt;
		}
		else if (gGate.purchAdjust){
			gGate.ngStaRiskTypeAdjustCnt++;
			gGate.ngStaRiskTypeAdjustAmt += gGate.ntAmt;
		}
		else if (gGate.refundAdjust){
			gGate.ngStaRiskTypeReturnAdjCnt++;
			gGate.ngStaRiskTypeReturnAdjAmt += gGate.replAmt;
		}
		else if (gGate.forcePosting){
			gGate.ngStaRiskTypeForceCnt++;
			gGate.ngStaRiskTypeForceAmt += gGate.ntAmt;
		}
		else if (gGate.mailOrder){
			gGate.ngStaRiskTypeMailCnt++;
			gGate.ngStaRiskTypeMailAmt += gGate.ntAmt;
		}
		else if (gGate.preAuth){
			gGate.ngStaRiskTypePreauthCnt++;
			gGate.ngStaRiskTypePreauthAmt += gGate.ntAmt;
		}
		else if (gGate.preAuthComp){
			gGate.ngStaRiskTypePreauthOkCnt++;
			gGate.ngStaRiskTypePreauthOkAmt += gGate.ntAmt;
		}
		else if (gGate.cashAdjust){
			gGate.ngStaRiskTypeCashAdjCnt++;
			gGate.ngStaRiskTypeCashAdjAmt += gGate.ntAmt;
		}
		else if (gGate.reversalTrans){
			gGate.ngStaRiskTypeReversalCnt++;
			gGate.ngStaRiskTypeReversalAmt += gGate.ntAmt;
		}
	
		if (gGate.ecTrans) {
			gGate.ngStaRiskTypeEcCnt++;    		
			gGate.ngStaRiskTypeEcAmt += gGate.ntAmt;
		}
	
		if (gGate.bgAbnormalResp) {
			gGate.sgStaRiskTypeUnNormalFlag="Y";
			gGate.ngStaRiskTypeUnNormalCnt++;
			gGate.ngStaRiskTypeUnNormalAmt += gGate.ntAmt;
	
		}
	
		if (blHasData) {
			gTa.updateStaRiskType();
		}
		else {
			gTa.insertStaRiskType();
		}
	}
}