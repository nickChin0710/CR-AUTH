/**
 * Proc 授權 AuthProcess 處理各種授權交易的流程
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
 * 2021/02/08  V1.00.00  Kevin       Proc 授權 AuthProcess 處理各種授權交易的流程     *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      *
 * 2021/08/12  V1.00.01  Kevin       新增lock/unlock功能確保同卡號同時交易時，依序處理。  *   
 * 2022/01/13  V1.00.02  Kevin       TCB新簡訊發送規則                            * 
 * 2022/03/21  V1.00.03  Kevin       VD交易零元不送主機圈存                         *
 * 2022/03/28  V1.00.04  Kevin       調整預先授權邏輯在authLogicCheck()授權邏輯查核    *
 * 2022/04/09  V1.00.05  Kevin	     授權補登交易取消設定，同預先授權完成交易             *
 * 2022/04/09  V1.00.06  Kevin	     VD沖正主機回應失敗不能視為成功                   *
 * 2022/04/20  V1.00.07  Kevin       團代1599政府採購卡不發簡訊及LINE推播             *
 * 2022/06/03  V1.00.08  Kevin       網銀推播-信用卡消費通知介面處理                   *
 * 2022/06/12  V1.00.09  Kevin       LINE信用卡消費通知ccar0220與授權交易紀錄ccaq1032  *
 *                                   的授權碼不一致                               *
 * 2022/09/16  V1.00.10  Kevin       啟用lock/unlock功能確保同卡號同時交易時依序處理。  *
 * 2022/11/14  V1.00.26  Kevin       因票證卡號非實體卡號，暫時關閉lock/unlock功能。     *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/08/08  V1.00.51  Kevin       Line推播發送前，檢查ID是否存在                   *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.logic.LogicCheck3dTransInfo;
import com.tcb.authProg.logic.LogicCheckBeforeTrade;
import com.tcb.authProg.logic.LogicCheckEInvoice;
import com.tcb.authProg.logic.LogicCheckEcGambleTrans;
import com.tcb.authProg.logic.LogicCheckIsoFieldInfo;
import com.tcb.authProg.logic.LogicCheckMcode;
import com.tcb.authProg.logic.LogicCheckOriAuthNo;
import com.tcb.authProg.logic.LogicCheckPreAuth;
import com.tcb.authProg.logic.LogicCheckPurchase;
import com.tcb.authProg.logic.LogicCheckRealCardNo;
import com.tcb.authProg.logic.LogicCheckSpecialAcct;
import com.tcb.authProg.logic.LogicCheckTransBasicInfo;
import com.tcb.authProg.logic.LogicGenerateAuthCode;
import com.tcb.authProg.logic.LogicInitialAmtField;
import com.tcb.authProg.logic.LogicInitialAuthData;
import com.tcb.authProg.logic.LogicInitialTransValuePerTrade;
import com.tcb.authProg.logic.LogicProcAcctVerify;
import com.tcb.authProg.logic.LogicProcAuthLogicCheck;
import com.tcb.authProg.logic.LogicProcBalanceInq;
import com.tcb.authProg.logic.LogicProcDebit;
import com.tcb.authProg.logic.LogicUpdateCardBase;
import com.tcb.authProg.logic.LogicUpdateCcaConsume;
import com.tcb.authProg.logic.LogicProcForcePosting;
import com.tcb.authProg.logic.LogicProcHceChk;
import com.tcb.authProg.logic.LogicProcIdCheckOrg;
import com.tcb.authProg.logic.LogicProcLineToAI;
import com.tcb.authProg.logic.LogicProcOempayChk;
import com.tcb.authProg.logic.LogicProcOpenCardByVoice;
import com.tcb.authProg.logic.LogicProcPurchaseAdjust;
import com.tcb.authProg.logic.LogicProcRefund;
import com.tcb.authProg.logic.LogicProcReversal;
import com.tcb.authProg.logic.LogicSetTransType;
import com.tcb.authProg.logic.LogicProcRiskScore;
import com.tcb.authProg.logic.LogicProcSaveLog;
import com.tcb.authProg.logic.LogicProcSmsToMitake;
import com.tcb.authProg.logic.LogicProcSpecialCode;
import com.tcb.authProg.logic.LogicUpdateTicketInfo;
import com.tcb.authProg.logic.LogicProcTokenizationNotification;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.logic.LogicProcBroadcastApi;


public class ProcAuthTxn extends AuthProcess {


	public ProcAuthTxn(AuthGlobalParm gb,AuthTxnGate gate) {
		this.gGb    = gb;
		this.gGate  = gate;
		
		gb.showLogMessage("I","ProcAuthTxn : started");

	}


	/**
	 * 授權交易的主function
	 * 
	 * @throws  Exception if any exception occurred
	 * @author  Howard Chang
	 * @version 1.0
	 * @since   2017/12/19
	 */
	// 預先授權訊息 01XX ,授權交易訊息 02XX
	public void authMainControl() throws Exception {

		boolean blProcessCorrect = true;

		gGate.isAuthMainControl = true; //確認是否為主要授權邏輯處理程序

		gGb.showLogMessage("I","authMainControl : started, mesg type: " + gGate.mesgType);

		if (null==gTa)
			gTa   = new TableAccess(gGb,gGate);
//		aulg = new AuthLogicCancel(gGb,gGate,gTa);

		//kevin:保留FISC授權 ISO_BIT_DTAT寫入CCA_AUTH_BITDATA
		if ("FISC".equals(gGate.connType)) {
			gTa.insertAuthBitData();        //保留FISC授權 ISO_BIT_DTAT寫入CCA_AUTH_BITDATA
		}
		
		
		//設定授權初始變數
		LogicInitialAuthData logicInit = new LogicInitialAuthData(gGb, gGate, gTa);
		logicInit.initialAuthData();              

		//kevin:新增lock功能確保同卡號同時交易時，依序處理。
		//V1.00.10 啟用lock/unlock功能確保同卡號同時交易時依序處理。
		//V1.00.26 因票證卡號非實體卡號，暫時關閉lock/unlock功能。
//		if (gGate.cardNo.length()>0) {
//			gGb.lock(gGate.cardNo);
//		}
		
		//設定交易類別，應在 checkIsoFieldInfo() 之前執行
		LogicSetTransType logicTrans = new LogicSetTransType(gGb, gGate);
		logicTrans.setTransType();      
		
		//交易類型，計算各種金額欄位，以便交易之用
		LogicInitialAmtField logicAmt = new LogicInitialAmtField(gGb, gGate);
		logicAmt.initAmtField4TxLog();           

		boolean blCheckAuthRule = true;
		boolean blIfCheckDetailRule=true;
		
		//ISO欄位檢查調整各種欄位，以便交易之用
		LogicCheckIsoFieldInfo logicIso = new LogicCheckIsoFieldInfo(gGb, gGate, gTa);
		if (logicIso.checkIsoFieldInfo()) {

			logicIso.reInitValue();      

			if ("Y".equals(gGb.getIfReturnTrueDirectly())) {
				String slTmp = "No verification,return true directly!!";
				gGb.showLogMessage("I", "authMainControl:" + slTmp);

				gGate.isoField[38] = "123456";
				gGate.isoField[39] = "00";

				blProcessCorrect=true;
				blCheckAuthRule = false;
				blIfCheckDetailRule=false;
			}
		}
		else {
			blProcessCorrect = false;
			blIfCheckDetailRule=false;
		}

//		gGb.showLogMessage("D","ISO String is =>" + gGate.isoString + "====");

		if (null != gGate.gDbConn) {
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckRealCardNo logicReal = new LogicCheckRealCardNo(gGb, gGate, gTa);
				//V1.00.42 授權系統與DB連線交易異常時的處理改善方式。票證邏輯判斷拒絕時，仍需要取得主檔資料，避免db出現exception
				if (!logicReal.checkRealCardNo()) { //kevin:票證交易所帶進來的卡號，非real card no，所以這邊需要檢查並取得real card no

					gGb.showLogMessage("D","LogicCheckRealCardNo for ticket card not normal = >" + gGate.tpanTicketNo +" status code = " +gGate.authStatusCode + "====");
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckBeforeTrade logicBefore = new LogicCheckBeforeTrade(gGb, gGate, gTa);
				if (!logicBefore.checkBeforeTrade()) {
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}				
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckTransBasicInfo logicBasic = new LogicCheckTransBasicInfo(gGb, gGate, gTa);
				if (!logicBasic.checkTransBasicInfo()) { //交易基本資料檢核
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckSpecialAcct logicSpec = new LogicCheckSpecialAcct(gGb, gGate, gTa);
				if (!logicSpec.specCheck()) { //spec check
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckMcode logicMcode = new LogicCheckMcode(gGb, gGate, gTa);
				if (!logicMcode.mCodeCheck()) { //mCode 檢查
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicProcDebit logicDebit = new LogicProcDebit(gGb, gGate, gTa);
				if (!logicDebit.processDebit()) { //debit card
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicProcOempayChk logicOempay = new LogicProcOempayChk(gGb, gGate, gTa);
				if (!logicOempay.processOempayChk()) { //OEMPAY STATUS CHK
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicProcHceChk logicHce = new LogicProcHceChk(gGb, gGate, gTa);
				if (!logicHce.processHceChk()) { //HCE card CHK
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckPurchase logicPurchase = new LogicCheckPurchase(gGb, gGate, gTa);
				if (!logicPurchase.purchaseChecking()) { //
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckOriAuthNo logicOriAuthNo = new LogicCheckOriAuthNo(gGb, gGate, gTa);
				if (!logicOriAuthNo.checkOriAuthNo() ) {
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheckEcGambleTrans logicGamble = new LogicCheckEcGambleTrans(gGb, gGate, gTa);
				if (!logicGamble.checkEcGambleTrans() ) {
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}
			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				LogicCheck3dTransInfo logic3d = new LogicCheck3dTransInfo(gGb, gGate, gTa);
				if (!logic3d.check3dTransInfo()) { 
					gTa.getAndSetErrorCode("QX");  
					blProcessCorrect = false;
					blIfCheckDetailRule=false;
				}
			}

			//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(避免預先授權完成交易，因檢查錯誤時，跳過預先授權完成比對工作)
			if (gGate.preAuthComp && !blProcessCorrect && !gGate.isCardNotExit) {
				gGb.showLogMessage("D","preAuthComp before check === "+blProcessCorrect);
				blProcessCorrect = true;
				blIfCheckDetailRule=true;
			}

			if ((blCheckAuthRule) && (blProcessCorrect) && (blIfCheckDetailRule)) {
				if (blIfCheckDetailRule) {


					if ( gGate.refund ) {// 退貨
						//
						LogicProcRefund logicRefund = new LogicProcRefund(gGb, gGate, gTa);
						blProcessCorrect = logicRefund.procRefund();//退貨
					}
					else if ( gGate.refundAdjust ) {// 退貨調整
						LogicProcPurchaseAdjust logicAdjust = new LogicProcPurchaseAdjust(gGb, gGate, gTa);
						blProcessCorrect = logicAdjust.procPurchaseAdjust();// 退貨調整
					}
					else if ( gGate.cashAdjust ) { //預借現金調整
						LogicProcPurchaseAdjust logicAdjust = new LogicProcPurchaseAdjust(gGb, gGate, gTa);
						blProcessCorrect = logicAdjust.procPurchaseAdjust();//預借現金調整

					}
					else if ( gGate.purchAdjust ) { /* 一般交易調整交易 */
						LogicProcPurchaseAdjust logicAdjust = new LogicProcPurchaseAdjust(gGb, gGate, gTa);
						blProcessCorrect = logicAdjust.procPurchaseAdjust();//一般交易調整交易

					}
					//V1.00.04 調整預先授權邏輯在authLogicCheck()授權邏輯查核
//					else if ( gGate.preAuthComp ) { // 查核 預先授權完成
//						LogicCheckPreAuth logicPreAuth = new LogicCheckPreAuth(gGb, gGate, gTa);
//						blProcessCorrect = logicPreAuth.checkPreAuthComp();
//						
//					}
//					else if ( gGate.preAuth ) { //預先授權
//						LogicCheckPreAuth logicPreAuth = new LogicCheckPreAuth(gGb, gGate, gTa);
//						blProcessCorrect = logicPreAuth.checkPreAuth();
//						
//					}
					else if ( gGate.balanceInquiry ) { // 餘額查詢
						LogicProcBalanceInq logicBalance = new LogicProcBalanceInq(gGb, gGate, gTa);
						blProcessCorrect = logicBalance.processBalanceInq();
						
					} else if ( gGate.accountVerify ) { // 帳戶驗證
						LogicProcAcctVerify logicVerify = new LogicProcAcctVerify(gGb, gGate, gTa);
						blProcessCorrect = logicVerify.processAcctVerify();
					//V1.00.05 授權補登交易取消設定，同預先授權完成交易					
//					} else if ( gGate.forcePosting )  { // 授權補登 force posting
//						LogicProcForcePosting logicForce = new LogicProcForcePosting(gGb, gGate, gTa);
//						logicForce.processForcePosting();
//						blProcessCorrect = false;////20160922 Howard:故意設定為 false，避免往下執行多餘的 code
					}
					else if (gGate.txVoice || gGate.atmCardOpen) { // 語音開卡
						LogicProcOpenCardByVoice logicOpen = new LogicProcOpenCardByVoice(gGb, gGate, gTa);
						blProcessCorrect = logicOpen.processOpenCardByVoice();
						gGb.showLogMessage("D","blProcessCorrect aulg.processOpenCardByVoice() === "+blProcessCorrect);
					}
					else if (gGate.isIdCheckOrg) { //信用卡身份輔助認證
						LogicProcIdCheckOrg logicIdChk = new LogicProcIdCheckOrg(gGb, gGate, gTa);
						blProcessCorrect =  logicIdChk.processIdCheckOrg();
						gGb.showLogMessage("D","blProcessCorrect aulg.processIdCheckOrg() === "+blProcessCorrect);
					}
					else if (gGate.isEInvoice) { //電子發票載具申請/取消處理
						LogicCheckEInvoice logicInvoice = new LogicCheckEInvoice(gGb, gGate, gTa);
						blProcessCorrect =  logicInvoice.checkEInvoice();
						gGb.showLogMessage("D","blProcessCorrect aulg.checkEInvoice() === "+blProcessCorrect);
					}
					else if ( gGate.reversalTrans ) { // 沖銷交易(04xx)
						LogicProcReversal logicReversal = new LogicProcReversal(gGb, gGate, gTa);
						blProcessCorrect = logicReversal.procReversal();
						gGb.showLogMessage("D","blProcessCorrect aulg.procReversal() === "+blProcessCorrect);
					}
					else if (gGate.isFiscToken) { //OEMPAY處理
						LogicProcTokenizationNotification logicNotify = new LogicProcTokenizationNotification(gGb, gGate, gTa);
						blProcessCorrect = logicNotify.procTokenizationNotification();
					}
					//kevin:取消
//					else if (gGate.tokenProcess020092) {
//						blProcessCorrect =  aulg.process020092();
//						System.out.println("blProcessCorrect aulg.process020092() === "+blProcessCorrect);
//					}
//					else if (gGate.tokenProcess020093) {
//						blProcessCorrect =  aulg.process020093();
//						System.out.println("blProcessCorrect aulg.process020093() === "+blProcessCorrect);
//					}
					else {
						LogicProcAuthLogicCheck logicAuth = new LogicProcAuthLogicCheck(gGb, gGate, gTa);
						blProcessCorrect = logicAuth.authLogicCheck();// 授權邏輯查核
						gGb.showLogMessage("D","blProcessCorrect authLogicCheck() === "+blProcessCorrect);
					}
				}
				//}
				gGb.showLogMessage("D","blProcessCorrect authMainControl === "+blProcessCorrect);
				
				//G_Gate.isoField[27] = "H";//for test
				if (("WEB".equals(gGate.connType)) && ("N".equals(gGate.isoField[27].trim())) && ("00".equals(gGate.isoField[39]))) { 
					
					//Howard: 人工授權 且 驗證成功時不寫Auth_TxLog

					
					if (blProcessCorrect) {
						gGate.isoField[38]="";
						gGate.isoField[39]="00";
					}
					//kevin:新增lock功能確保同卡號同時交易時，依序處理。
					//V1.00.10 啟用lock/unlock功能確保同卡號同時交易時依序處理。
					//V1.00.26 因票證卡號非實體卡號，暫時關閉lock/unlock功能。
//					if (gGate.cardNo.length()>0) {
//						gGb.unlock(gGate.cardNo);
//					}
					
					return;
				}

			}
			//交易拒絕後，不需再帶授權碼
			if (blProcessCorrect) {
				LogicGenerateAuthCode logicAuthNo = new LogicGenerateAuthCode(gGb, gGate, gTa);
				logicAuthNo.genAuthCode();        
				//// 產生 授權碼
				//kevin:ccasEcsTrans()處理Redeem & installment，第一階段上線redeem還是維持舊的方式連接ACER紅利系統
				//V1.00.37 P3紅利兌換處理方式調整-讀取系統參數檔，確認授權邏輯是否ROLLBACK P2 => Y(表示系統還原至P2階段ACER紅利兌換連線)
				if (gGate.isRedeemTx && gGate.rollbackP2 )  {
					if (gGate.rollbackP2RejectAcer) {
						gTa.getAndSetErrorCode("2E");
						gGate.authRemark = "因授權在P2轉換到P3期間，系統強制拒絕ACER紅利折抵交易";
						blProcessCorrect = false;
					}
					else {
						ProcRedeemToAcer proc2Acer = new ProcRedeemToAcer(gGb, gGate);
						
						if (gGb.isEnableAcerRedemption()) {							
							if (!proc2Acer.sendRedeemToAcer()) {
								gTa.getAndSetErrorCode("2E");
								blProcessCorrect = false;
							}
							else {
								if (!gGate.readFromAcerSuccessful) {
									gTa.getAndSetErrorCode("2A");
									blProcessCorrect = false;
								}
							//將拒絕回覆碼select拒絕原因並寫入授權留言中，提供授權人員查詢
							gTa.getAcerRspCode("3", gGate.bankBit39Code);
							}
						}
						else {
							gTa.getAndSetErrorCode("2E");
							blProcessCorrect = false;
						}
					}
				}
				else {
					ProcInstallRedeem proc2Ecs = new ProcInstallRedeem(gGb, gGate, gTa);
//					proc2Ecs.ccasEcsTrans();
					blProcessCorrect = proc2Ecs.ccasEcsTrans();
				}
			}

			//VD交易IMS Connect傳送帳務主機
//			gGb.showLogMessage("D","@@@@kevin vd test@@@@");
			if ("Y".equals(gGb.getIfEnableIms())) {
				//V1.00.03  VD交易零元不送主機圈存
				if ( (blCheckAuthRule) && (blProcessCorrect) && (gGate.isDebitCard) && (gGate.ifSendTransToIms) && (!gGate.nonPurchaseTxn) && (gGate.lockAmt > 0)){
					String slHeadMsgType = "0200";
					if (gGate.reversalTrans) {
						slHeadMsgType = "0202";
					}
					ProcVisaDebitToIms proc2Ims = new ProcVisaDebitToIms(gGb, gGate, gTa);
					if (proc2Ims.procVdToIms(slHeadMsgType)) {
						if (!gGate.readFromImsSuccessful) {
//							aulg.writeReversalData();//如果無法線上接收 IMS的交易回應，則應做 reversal，所以寫入 table 做批次傳送  
							blProcessCorrect = false;
						}
						else {
							if (!gGate.vdResponseSuccessful) {
								gTa.getAndSetErrorCode("2G");
								blProcessCorrect = false;
							}
						}
					}
					else {
						blProcessCorrect = false;
					}
				}
			}
			//代行通知不須確認主機回復是否正常
			if (gGate.updateSrcTxAfterPreAuthComp) {
				gTa.updateSrcAuthTxLog(2, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));	
			}
	      	//確認簡訊規則
			if (gTa.selectPtrSysParm("SMS_CONNECT", "SMS_FLAG")) {
				gGb.showLogMessage("D","SMS_CONNECT + SYS_FLAG SYS VALUE 1 === "+gTa.getValue("SysValue1"));
				if ("Y".equals(gTa.getValue("SysValue1"))) {
					gGate.isSmsLogic4Tcb = true;
				}
			}
			//一般消費, 預借現金, 郵購, 預先授權, 預先授權完成 才要發簡訊
			gTa.getParm3TranCode("TRANCODE", gGate.transCode);
			
			////System.out.println("DD...");
			gGb.showLogMessage("D","blProcessCorrect ALL === "+blProcessCorrect);
			if (blProcessCorrect) {


				if (gGate.updateSrcTxAfterRefund)      {
					gTa.updateSrcAuthTxLog(1, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));
				}
//				if (gGate.updateSrcTxAfterPreAuthComp) {
//					gTa.updateSrcAuthTxLog(2, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));	
//				}
				if (gGate.updateSrcTxAfterPurchaseAdj) {
					gTa.updateSrcAuthTxLog(3, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));
				}
				if (gGate.updateSrcTxAfterRefundAdj)   {
					gTa.updateSrcAuthTxLog(4, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));
				}
			    if (gGate.updateSrcTxAfterCashAdj)     {
			    	gTa.updateSrcAuthTxLog(5, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));
			    }
		      	if (gGate.updateSrcTxAfterReversal)    {
		      		gTa.updateSrcAuthTxLog(6, gTa.getValue("AuthTxLogAuthSeqNo_SrcTrans"));
		      	}				

				    
				    
					//System.out.println("P38-2" + G_Gate.isoField[38] + "===");
//			    LogicGenerateAuthCode logicAuthNo = new LogicGenerateAuthCode(gGb, gGate, gTa);
//			    logicAuthNo.genAuthCode();                  //// 產生 授權碼
				//System.out.println("P38-3" + G_Gate.isoField[38] + "===");
			    LogicProcSpecialCode logicSpecCode = new LogicProcSpecialCode(gGb, gGate, gTa);
			    logicSpecCode.processSpecCode();              // 處理 回覆碼 CCS_SPEC_CODE
				//System.out.println("P38-4" + G_Gate.isoField[38] + "===");

				if (blCheckAuthRule) {
					ProcUpdateRelatedFile logicUpdFile = new ProcUpdateRelatedFile(gGb, gGate, gTa);
					logicUpdFile.updateRelatedFile();
				}
//				//kevin:ccasEcsTrans()處理Redeem & installment，第一階段上線redeem還是維持舊的方式連接ACER紅利系統
//				if ("FISC".equals(gGate.connType)) {
//					if (gGate.isRedeemTx && gGb.enableAcerRedemption )  {
//						sendRedeemToAcer();
//					}
//					else {
//						ccasEcsTransFisc();
//					}
//				}
//				else {
//					ccasEcsTrans(); //Howard: proc is CCAS_ecs_trans()
//				}

				if (gGate.txVoice) {
					//aulg.sendOut0300Trans4VoiceOpen(); //for test, so marked(2020/01/17)
				}
				//V1.00.09 LINE信用卡消費通知ccar0220與授權交易紀錄ccaq1032的授權碼不一致
				if ("00".equals(gGate.isoField[39]) && gGate.oriAuthNo.length() >0) {    		  
					if ( (gGate.purchAdjust)  || (gGate.cashAdjust) || (gGate.refundAdjust) || (gGate.preAuthComp)  || (gGate.reversalTrans) || (gGate.refund) )  { //20161003, Tony : 這些交易要填回原始授權碼。
						gGate.isoField[38] = gGate.oriAuthNo;
						gGate.authNo = gGate.oriAuthNo;
					}
					gGb.showLogMessage("D","P38-6" + gGate.isoField[38] + "===");
				}
				
			}
			else {
				gGate.isoField[38] = "";
				gGate.authNo = "";
			}




 			if (!"00".equals(gGate.isoField[39])) {//交易失敗
				if ((gGate.ecTrans) && ("01".equals(gGate.isoField[39])) ) { //網路交易 and P39='01' 時，強制改為回覆 05
					gGate.isoField[39] = "57";  
				}
				//卡號不存在系統不需要更新ccaconsume	
				if (!gGate.isCardNotExit) {
					LogicUpdateCcaConsume logicConsume = new LogicUpdateCcaConsume(gGb, gGate, gTa);
					logicConsume.processCcaConsume();
				}
				/* Sylvia(09/09) :先不要執行這一段 
    	  if (( G_Gate.cashAdjust) && ("F".equals(G_Gate.areaType)) ) { // 
        	  G_Ta.getAndSetErrorCode("ERR93"); //0825 confirmed  
          }
				 */
			}

			gGate.sgIsoRespCode =gGate.isoField[39];
			gGate.sgBit38ApprCode = gGate.isoField[38];
			gGb.showLogMessage("D","交易回覆前狀態回覆碼="+gGate.sgIsoRespCode+"授權碼="+gGate.sgBit38ApprCode);
			gGb.showLogMessage("D","交易回覆前取得授權碼="+gGate.authNo);
			/*
			排出(Howard: marked on 2019/05/20)
			if (("0420".equals(gGate.mesgType)) || ("0421".equals(gGate.mesgType)) )
				gGate.isoField[39]="00";// 20160922, Tony: NCCC手冊0420必須回覆P39=00; 20161019, Tony:0421 也要回00
			*/
			gGate.isoField[52]="";//    2016/11/01, Tony:ㄧ律不回52 field

			if (!gGate.isCardNotExit) {
				LogicProcRiskScore logicScore = new LogicProcRiskScore(gGb, gGate, gTa);
				logicScore.procRiskScore(); //kevin: 取得風險分數 riskFactor
			}
			LogicInitialTransValuePerTrade logicTrade = new LogicInitialTransValuePerTrade(gGb, gGate, gTa);
			logicTrade.initTransValuePerTrade();
			
			ProcReAssignData logicAssign = new ProcReAssignData(gGb, gGate, gTa);
			logicAssign.reAssignData();
			
			LogicProcSaveLog logicLog = new LogicProcSaveLog(gGb, gGate, gTa);
			logicLog.saveLog();
			
//			//V1.00.02 TCB新簡訊發送規則; V1.00.07 團代1599政府採購卡不發簡訊及LINE推播、網銀推播; V1.00.42 授權系統與DB連線交易異常時的處理改善方式
			//V1.00.48  P3程式碼整理(附卡註記/附卡消費通知正卡註記)
			//V1.00.51 Line推播發送前，檢查ID是否存在
			if (blCheckAuthRule && (!gGate.isCardNotExit && !"1599".equals(gGate.groupCode))) {
				if (gGate.idNo.isEmpty()) {
					gTa.selectCardIdNo();
					gGate.idNo = gTa.getValue("CrdIdNoIdNo");
				}
				if (gGate.isSupCard) {
					if (gTa.selectPrimaryCardIdNo()) {
						gGate.isAdvicePrimChFlag = true;
						gGb.showLogMessage("D","@@@@符合附卡消費通知正卡註記 = "+gTa.getValue("CrdIdNoSmsPrimChFlag"));
					}
				}
				if (gGate.isSmsLogic4Tcb) {
					gGb.showLogMessage("D","isSmsLogic4Tcb="+gGate.isSmsLogic4Tcb+";CardMsgFlag="+gTa.getValue("CardMsgFlag")+";CrdIdnoMsgFlag="+gTa.getValue("CrdIdnoMsgFlag")+";Parm3TranCode4="+gTa.getValue("Parm3TranCode4"));
					if ("Y".equals(gTa.getValue("CardMsgFlag")) && "Y".equals(gTa.getValue("CrdIdnoMsgFlag")) && "Y".equals(gTa.getValue("Parm3TranCode4").trim())) {
						LogicProcSmsToMitake logicSms = new LogicProcSmsToMitake(gGb, gGate, gTa);
						logicSms.processSmsInfoNew();    
					}
				}
				else {
					LogicProcSmsToMitake logicSms = new LogicProcSmsToMitake(gGb, gGate, gTa);
					logicSms.processSmsInfo();
				}
				if (gGb.getLineAiUrl().length()>0) {
					LogicProcLineToAI logicLine = new LogicProcLineToAI(gGb, gGate, gTa);
					logicLine.processLineInfo();
				}
				LogicProcBroadcastApi logicBroadcast = new LogicProcBroadcastApi(gGb, gGate, gTa);
				logicBroadcast.processBroadcastInfo();
			}

		}
		else {
			gGb.showLogMessage("E","Database is not connected! Tranxaction is rejected.");    	  
			//G_Ta.getAndSetErrorCode("ERR91"); //confirmed 10/26
			gGate.isoField[39] = "96";
			gGate.isoField[38] = "";
			//G_Gate.auth_no = G_Gate.isoField[38];
			gGate.authNo = "";
			gGate.authStatusCode = "";



		}

		if (blCheckAuthRule && !gGate.nonPurchaseTxn) { //V1.00.48 P3程式碼整理(購貨交易才需要統計)
			//產生授權統計資料
			ProcStaticData proc2Static = new ProcStaticData(gGb, gGate, gTa);
			proc2Static.writeStaticData();
			writeToCcaDebitBil(); 
		}
		
		
		return;
	}

	private void writeToCcaDebitBil() {
		//proc is TB_ccas_bil

		if (!gGate.isDebitCard)
			return;

		if ("00".equals(gGate.isoField[39])) {
			gTa.insertCcaDebitBil();
		}

	}

	// 收單對帳請求訊息 05XX reconciliation 訊息
	public void reconControl() throws Exception {

		gGate.isoField[66] = "9";
		gGb.showLogMessage("I","收單對帳訊息 reconciliation 訊息 : "+gGate.mesgType);

		return;
	}

	// 09NN-WEB USER 功能通知訊息
	public void webUserFunction() throws Exception {

		gGb.showLogMessage("I","webUserFunction mesg type: "+gGate.mesgType);

		if ( "001".equals(gGate.isoField[70]) ) {

		}

		return;
	}

}


