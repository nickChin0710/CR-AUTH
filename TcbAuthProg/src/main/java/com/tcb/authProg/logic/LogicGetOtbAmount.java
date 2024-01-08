/**
 * 授權邏輯查核-OTB計算處理
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2023/02/14
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.sql.ResultSet;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicGetOtbAmount extends AuthLogic {

	public LogicGetOtbAmount(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb = gb;
		this.gGate = gate;
		this.ta = ta;

		gb.showLogMessage("I", "LogicGetOtbAmount : started");

	}
	
	/*
	 * 檢核授權邏輯查核-信用卡OTB計算處理 computeOtb
	 * V1.00.38 P3授權額度查核調整
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public void computeOtb() throws Exception {
		//計算信用卡個人的可用餘額
		gb.showLogMessage("I", "＊＊＊開始計算信用卡Personal OTB可用額度＊＊＊");
		//取得相關額度計算金額-個人
		gGate.ccaConsumeArgueAmt = ta.selectActAcctSum(false, "DP"); //爭議款金額
		gGate.totSpecAmtBal  = ta.selectActAcctSumSpecAmt(false);    //取得專款專用欠款總額
		gGate.finalPaidConsumeFee = ta.selectAcctJrnlBal(false);     //欠款金額(含專款專用、含溢付款金額)
		gGate.finalPaidPrecash = ta.selectActAcctSum(false, "CA");   //欠款金額(預借現金)
		gGate.ccaConsumeUnPostInstFee = ta.selectBilContractInstUpost(false, false); //取得分期未到期金額(不含專款專用)	
		gGate.ccaConsumeUnPostInstSpec = ta.selectBilContractInstUpost(false, true); //取得分期未到期金額(專款專用)	
		gGate.ccaConsumePrePayAmt = ta.selectAcctPrePayAmt(false);   //溢繳款金額(不用放入OTP計算)
		gGate.ccaConsumeTotUnPaidAmt = ta.computeUnPaidConsumeFee(false); //已付款未銷帳金額
		gGate.totAmtPreCash = ta.getAuthedNotMatch(1); //已授權未請款預借金額(不含專款專用)
		gGate.totAmtConsume = ta.getAuthedNotMatch(3); //已授權未請款金額(不含專款專用)
		
		gb.showLogMessage("D","Personal-BaseLimit信用卡基本額度=>" + gGate.realCreditCardBaseLimit);
		gb.showLogMessage("D","Personal-BaseLimitOfCash信用卡預借額度=>" + gGate.realCreditCardBaseLimitOfCash);
		gb.showLogMessage("D","Personal-ArgueAmt[爭議款金額]=>" + gGate.ccaConsumeArgueAmt);
		gb.showLogMessage("D","Personal-totSpecAmtBal[專款專用欠款總額]=>" + gGate.totSpecAmtBal);
		gb.showLogMessage("D","Personal-finalPaidConsumeFee[欠款金額(含專款專用)]=>" + gGate.finalPaidConsumeFee);
		gb.showLogMessage("D","Personal-finalPaidPrecash[欠款金額(預借現金)]=>" + gGate.finalPaidPrecash);
		gb.showLogMessage("D","Personal-UnPostInstFee[分期未到期金額(不含專款專用)]=>" + gGate.ccaConsumeUnPostInstFee);
		gb.showLogMessage("D","Personal-UnPostInstSpec[分期未到期金額(專款專用)]=>" + gGate.ccaConsumeUnPostInstSpec);
		gb.showLogMessage("D","Personal-PrePayAmt[溢繳款金額]=>" + gGate.ccaConsumePrePayAmt);
		gb.showLogMessage("D","Personal-UnPaidConsumeFee[已付款未銷帳金額]=>" + gGate.ccaConsumeTotUnPaidAmt);
		gb.showLogMessage("D","Personal-totAmtPreCash[已授權未請款預借金額]=>" + gGate.totAmtPreCash);
		gb.showLogMessage("D","Personal-totAmtConsume[已授權未請款金額]=>" + gGate.totAmtConsume);
		gb.showLogMessage("D","Personal-specAmtConsume[專款專用已使用額度]=>" + gGate.specAmtConsume);
		gb.showLogMessage("D","Personal-specAmtTotal[專款專用指定額度]=>" + gGate.specAmtTotal);
		
		gGate.curTotalUnpaidOfPersonal = gGate.ccaConsumeArgueAmt 
									   + (gGate.finalPaidConsumeFee - gGate.totSpecAmtBal) 
									   + gGate.ccaConsumeUnPostInstFee 
									   + gGate.totAmtConsume;
		gb.showLogMessage("D",
				"Personal-curTotalUnpaid欠款金額#"+gGate.curTotalUnpaidOfPersonal+" = ArgueAmt爭議款金額#"+gGate.ccaConsumeArgueAmt+
				                                                               " + (finalPaidConsumeFee欠款金額#"+gGate.finalPaidConsumeFee+" - AmtBal專款專用欠款總額#"+gGate.totSpecAmtBal+")"+
				                                                               " + UnPostInstFee分期未到期金額#"+gGate.ccaConsumeUnPostInstFee+
				                                                               " + totAmtConsume已授權未請款金額#"+gGate.totAmtConsume);
		gGate.currTotCashAmt = gGate.totAmtPreCash 
				             + gGate.finalPaidPrecash;
		gb.showLogMessage("D",
				"Personal-currTotCashAmt欠款金額#"+gGate.currTotCashAmt+" = totAmtPreCash已授權未請款預借金額(不含專款專用)#"+gGate.totAmtPreCash+
				                                                     " + finalPaidPrecash欠款金額(預借現金)#"+gGate.finalPaidPrecash);
		
		//取得額度金額-個人
		getRealBaseAmt();
		if (gGate.SpecAmtMatch > 0 && gGate.SpecAmtMatch > gGate.specAmtTotal) {
			if ((gGate.totSpecAmtBal + gGate.ccaConsumeUnPostInstSpec)< gGate.specAmtTotal) {
				gGate.specAmtConsume = (gGate.specAmtConsume - gGate.SpecAmtMatch) 
						             + gGate.specAmtTotal;
				gb.showLogMessage("D",
						"Personal-specAmtConsume專款專用指定額度#"+gGate.specAmtConsume+" = (specAmtConsume專款專用指定額度#"+gGate.specAmtConsume+" - SpecAmtMatch專款專用已請款額度#"+gGate.SpecAmtMatch+")"+
						                                                           " + specAmtTotal專款專用設定額度#"+gGate.specAmtTotal);
			}
		}
		//TCB轉換當月的專款專用分期期金因為重複佔額的問題，啟用判斷在專款專用分期補期金的日期內，一律補當期分期期金到otb
		String slCurdate = HpeUtil.getCurDateStr(false);  
		double dlSpecAmt4InstAddOn = 0;
		if (slCurdate.compareTo(gGate.instSpecAddOnDate) <= 0 && gGate.ccaConsumeUnPostInstSpec > 0) {
			dlSpecAmt4InstAddOn = ta.getDouble("bilContractUnitPrice");

		}
		//臨調期間且非臨調的風險分類旗標，將專款專用超額使用到原額度的分期已入帳部分，還額給otb使用
		double dlSpecAmt4Installment = 0;
		if (gGate.bgHasPersonalAdj && !gGate.adjRiskType) {
			if ((gGate.totSpecAmtBal - gGate.ccaConsumeUnPostInstSpec) > gGate.specAmtTotal) {
				dlSpecAmt4Installment = (gGate.totSpecAmtBal - gGate.ccaConsumeUnPostInstSpec) - gGate.specAmtTotal;
			}
		}
		//計算OTB可用餘額-個人
		gGate.otbAmt = gGate.realCreditCardBaseLimit 
					 + gGate.ccaConsumeTotUnPaidAmt 
					 - gGate.specAmtConsume
				     - gGate.curTotalUnpaidOfPersonal
				     + dlSpecAmt4Installment
				     + dlSpecAmt4InstAddOn;
		gb.showLogMessage("D","Personal-otbAmt一般可用餘額計算#"+gGate.otbAmt+" = BaseLimit信用卡基本額度#"+gGate.realCreditCardBaseLimit+
						  " + UnPaidConsumeFee已付款未銷帳金額#"+gGate.ccaConsumeTotUnPaidAmt+
						  " - specAmtConsume專款專用已使用額度#"+gGate.specAmtConsume+
						  " - curTotalUnpaidOfPersonal欠款金額#"+gGate.curTotalUnpaidOfPersonal+
						  " + dlSpecAmt4Installment[專款專用超額分期使用到原額度的部分還額給otb]"+dlSpecAmt4Installment+
						  " + dlSpecAmt4InstAddOn[專款專用分期上線當月補當期分期期金到otb]"+dlSpecAmt4InstAddOn);

		//計算OTB預借可用餘額-個人
		gGate.otbAmtCash = gGate.realCreditCardBaseLimitOfCash - gGate.currTotCashAmt;
		gb.showLogMessage("D","Personal-OTB預借可用餘額計算=>" + gGate.otbAmtCash);	

		gb.showLogMessage("I", "＊＊＊信用卡Personal OTB可用額度計算完畢＊＊＊");

	}
	/*
	 * 檢核授權邏輯查核-公司卡OTB計算處理 computeBusinessCardOtb
	 * V1.00.38 P3授權額度查核調整
	 * @return 如果preAuth complete 交易檢核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public void computeBusinessCardOtb() throws Exception {
		//計算商務卡公司的可用餘額
		if (!gGate.businessCard) {
			return; //不是商務卡，就不用計算
		}
		gb.showLogMessage("I", "＊＊＊開始計算商務卡Business OTB可用額度＊＊＊");	
		//取得相關額度計算金額-公司
		gGate.ccaConsumeArgueAmtOfComp = ta.selectActAcctSum(true, "DP"); //爭議款金額
		gGate.totSpecAmtBalOfComp = ta.selectActAcctSumSpecAmt(true); // 取得專款專用欠款總額
		gGate.finalPaidConsumeFeeOfComp = ta.selectAcctJrnlBal(true); // 欠款金額(含專款專用、含溢付款金額)
		gGate.finalPaidPrecashOfComp = ta.selectActAcctSum(true, "CA"); // 欠款金額(預借現金)
		gGate.ccaConsumeUnPostInstFeeOfComp = ta.selectBilContractInstUpost(true, false); // 取得分期未到期金額(不含專款專用)
		gGate.ccaConsumePrePayAmtOfComp = ta.selectAcctPrePayAmt(true); // 溢繳款金額(不用放入OTP計算)
		gGate.ccaConsumeTotUnPaidAmtOfComp = ta.computeUnPaidConsumeFee(true); // 已付款未銷帳金額
		gGate.totAmtPreCashOfComp = ta.getAuthedNotMatch(2); // 已授權未請款預借金額(商務)
		gGate.totAmtConsumeOfComp = ta.getAuthedNotMatch(4); // 已授權未請款金額(商務)

		//取得額度金額-公司
		getRealBaseAmtOfComp();
		gGate.parentOtbAmt = gGate.realCreditCardBaseLimitOfComp 
						   + gGate.ccaConsumeTotUnPaidAmtOfComp - gGate.ccaConsumeArgueAmtOfComp
			               -(gGate.finalPaidConsumeFeeOfComp - gGate.totSpecAmtBalOfComp) - gGate.ccaConsumeUnPostInstFeeOfComp
				           - gGate.totAmtConsumeOfComp;
		//計算OTB預借可用餘額-公司
		gGate.parentOtbAmtCash = gGate.realCreditCardBaseLimitOfCashOfComp
							   - gGate.totAmtPreCashOfComp - gGate.finalPaidPrecashOfComp;

		gb.showLogMessage("D", "Business-信用卡基本額度=>" + gGate.realCreditCardBaseLimitOfComp);
		gb.showLogMessage("D", "Business-信用卡預借額度=>" + gGate.realCreditCardBaseLimitOfCashOfComp);
		gb.showLogMessage("D", "Business-ArgueAmt[爭議款金額]=>" + gGate.ccaConsumeArgueAmtOfComp);
		gb.showLogMessage("D", "Business-AmtBal[專款專用欠款總額]=>" + gGate.totSpecAmtBalOfComp);
		gb.showLogMessage("D", "Business-finalPaidConsumeFee[欠款金額(含專款專用)]=>" + gGate.finalPaidConsumeFeeOfComp);
		gb.showLogMessage("D", "Business-finalPaidPrecash[欠款金額(預借現金)]=>" + gGate.finalPaidPrecashOfComp);
		gb.showLogMessage("D", "Business-UnPostInstFee[分期未到期金額(不含專款專用)]=>" + gGate.ccaConsumeUnPostInstFeeOfComp);
		gb.showLogMessage("D", "Business-PrePayAmt[溢繳款金額]=>" + gGate.ccaConsumePrePayAmtOfComp);
		gb.showLogMessage("D", "Business-UnPaidConsumeFee[已付款未銷帳金額]=>" + gGate.ccaConsumeTotUnPaidAmtOfComp);
		gb.showLogMessage("D", "Business-totAmtPreCash[已授權未請款預借金額]=>" + gGate.totAmtPreCashOfComp);
		gb.showLogMessage("D", "Business-totAmtConsume[已授權未請款金額]=>" + gGate.totAmtConsumeOfComp);
		gb.showLogMessage("D", "Business-OTB一般可用餘額計算=>" + gGate.parentOtbAmt);
		gb.showLogMessage("D", "Business-OTB預借可用餘額計算=>" + gGate.parentOtbAmtCash);
		gb.showLogMessage("I", "＊＊＊商務卡Business OTB可用額度計算完畢＊＊＊");

	}

	/**
	 * 檢核授權邏輯查核-取得基本額度(含零調總額度)
	 * V1.00.38 P3授權額度查核調整
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	private void getRealBaseAmt() throws Exception {
		/*
		PS: [gGate.cashBase and gGate.baseAmt ]算是臨調後的額度了 
		1. 先算出信用卡基本額度。
		2. 國外預借現金額度 = 信用卡基本額度* 國外預借現金%
		3. 國內預借現金額度 = ta.ActAcnoLineOfCreditAmtCash
		 * */

		int nlResult=0, nlRealBaseLimit=0, nlRealBaseLimitOfCash=0;

		//down, 計算 信用卡基本額度

		if (gGate.bgHasPersonalAdj) {
			nlRealBaseLimit = (int)getAdjAmt(gGate.mccRiskType); //信用卡零調額度
		}
		else {
			nlResult = getBaseAmt(); //信用卡基本額度		
			
				getMccAdjParm();
	
				if (gGate.hasMccAdjParm) {
					gb.showLogMessage("I", "有 MCC 臨調，所以要計算 MCC 臨調參數");
					//有 MCC臨調
					double dlProdAdjParmTotAmtMonth = (double)(gGate.mccAdjParmTotAmtMonth/100);
					nlRealBaseLimit = (int)(nlResult*dlProdAdjParmTotAmtMonth);
				}
				else {
					gb.showLogMessage("I", "沒有 MCC 臨調...");
					//沒有 MCC臨調，計算RiskLevel
					double dlRiskLevelParm = getRiskLevelParm();
					
					if ("T".equals(gGate.areaType) ) {// 國內交易  //( (要考慮RiskLevel的上限值參數))
						nlRealBaseLimit = computeFinalMonthLimitAmtByRiskLevel(nlResult, dlRiskLevelParm, true); //RiskLevel 參數只與月限額有關
					}
					else {
						int nlTmp1 = computeFinalMonthLimitAmtByRiskLevel(nlResult, dlRiskLevelParm, false);
						int nlTmp2 = nlResult + ta.getInteger("RiskLevelAddTotAmt"); 
						if (nlTmp1 > nlTmp2) {
							nlRealBaseLimit = nlTmp1;
						}
						else {
							nlRealBaseLimit = nlTmp2;
						}
					}
				}
		}
		//符合P2的額度使用邏輯
		gGate.finalTotLimit = nlRealBaseLimit;
		
		gGate.realCreditCardBaseLimit = nlRealBaseLimit;
		//up, 計算 信用卡基本額度

		//down, 計算 預借現金 基本額度
		if ("F".equals(gGate.areaType)) {
			//國外交易 預借現金天花板=> 信用卡的額度*國外預借現金%
			nlRealBaseLimitOfCash = (int)(nlRealBaseLimit*(double)(ta.getDouble("AuthParmOverseaCashPct")/100)); //該戶之國外預借現金額度。AuthParmOverseaCashPct => 國外預借現金%
			gb.showLogMessage("I", "個人國外交易 預借現金天花板=個人信用卡的額度*國外預借現金 百分比=" +nlRealBaseLimitOfCash);
		}
		else {
			//國內交易 預借現金天花板=> 預借現金額度的設定值
			nlRealBaseLimitOfCash = ta.getInteger("ActAcnoLineOfCreditAmtCash"); //該戶之預借現金額度
			gb.showLogMessage("I", "個人國內交易 預借現金天花板=個人信用卡預借現金額度的設定值=" +nlRealBaseLimitOfCash);
		}
		//up, 計算 預借現金基本額度		
		//符合P2的額度使用邏輯
		gGate.cashLimit = computeCashLimit((double)nlRealBaseLimitOfCash);		

		gGate.realCreditCardBaseLimitOfCash = (int)gGate.cashLimit;
	}
	/**
	 * 檢核授權邏輯查核-取得基本額度(含零調總額度)
	 * V1.00.38 P3授權額度查核調整
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	private void getRealBaseAmtOfComp() throws Exception {
		/*
		PS: [gGate.cashBase and gGate.baseAmt ]算是臨調後的額度了 
		1. 先算出信用卡基本額度。
		2. 國外預借現金額度 = 信用卡基本額度* 國外預借現金%
		3. 國內預借現金額度 = ta.ActAcnoLineOfCreditAmtCash
		 * */

		int nlRealBaseLimitOfComp=0, nlRealBaseLimitOfCashOfComp=0;

		//down, 計算 信用卡基本額度

		if (gGate.bgHasCompAdj) {
			nlRealBaseLimitOfComp = getAdjAmtOfComp(); //信用卡零調額度 (商務卡)
		}
		else {
			nlRealBaseLimitOfComp = getBaseAmtOfComp(); //信用卡基本額度 (商務卡)
		}
		//符合P2的額度使用邏輯
		gGate.tmpTotLimit2 = nlRealBaseLimitOfComp;
		
		gGate.realCreditCardBaseLimitOfComp = nlRealBaseLimitOfComp;
		//up, 計算 信用卡基本額度

		//down, 計算 預借現金 基本額度
		if ("F".equals(gGate.areaType)) {
			//國外交易 預借現金天花板=> 信用卡的額度*國外預借現金%
			nlRealBaseLimitOfCashOfComp = (int)(nlRealBaseLimitOfComp*(double)(ta.getDouble("AuthParmOverseaCashPct")/100)); //該戶之國外預借現金額度。AuthParmOverseaCashPct => 國外預借現金%
			gb.showLogMessage("I", "公司國外交易 預借現金天花板=公司信用卡的額度*國外預借現金 百分比=" +nlRealBaseLimitOfCashOfComp);
		}
		else {
			//國內交易 預借現金天花板=> 預借現金額度的設定值
			nlRealBaseLimitOfCashOfComp = ta.getInteger("ActAcnoLineOfCreditAmtCashOfComp");  //*該戶之預借現金額度-公司
			gb.showLogMessage("I", "公司國內交易 預借現金天花板=公司信用卡預借現金額度的設定值=" +nlRealBaseLimitOfCashOfComp);
		}
	
		gGate.realCreditCardBaseLimitOfCashOfComp = nlRealBaseLimitOfCashOfComp;

		//up, 計算 預借現金基本額度		
	}
	/**
	 * 檢核授權邏輯查核-取得臨調後的金額
	 * V1.00.38 P3授權額度查核調整
	 * @return 臨調後的金額
	 * @throws Exception if any exception occurred
	 */	
	private double getAdjAmt(String spRiskType) throws Exception {

		double dL_AdjAmt=0;
		if ( (gGate.isVirtualCard) &&  (gGate.isInstallmentTx) ){
			//if(gGate.isInstallmentTx) {
			if (ta.getInteger("CardAcctAdjInstPct")>0) {
				gGate.wkAdjTot = ta.getInteger("CardAcctAdjInstPct"); /* 臨調分期付款金額*/
				gb.showLogMessage("I", "****** 有個人臨調 + 是虛擬卡 + 分期。臨調後額度:" + gGate.wkAdjTot + "******");
			}
		}
		else {
			if (ta.getInteger("CardAcctTotAmtMonth")>0) {
				gGate.wkAdjTot = ta.getInteger("CardAcctTotAmtMonth");/* 臨調放大總月限金額 */
				gb.showLogMessage("I", "****** 有個人臨調。臨調後額度:" +gGate.wkAdjTot + "******");
			}
		}
//		/***** 讀取臨調專款專用資料 **/   
		ResultSet adjRS = ta.loadAdjParmSpecAmt(ta.getValue("CardAcctAdjEffStartDate"), ta.getValue("CardAcctAdjEffEndDate"));
		gb.showLogMessage("D","check CCA ADJ PARM adjRS="+adjRS);
		double dlSpecAmt = 0, dlTmpAmt = 0, dlRiskAmt=0, dlSpecAmtConsume=0, dlSpecAmtTotal=0, dlSpecAmtMatch=0;
		boolean blMatchRisk = false;
		String slCurdate = HpeUtil.getCurDateStr(false);  

		if (adjRS == null) {
			dL_AdjAmt =  getBaseAmt();
		}
		else {
			while (adjRS.next()) {
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmRiskType="+adjRS.getString("CcaAdjParmRiskType"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjMonthAmt="+adjRS.getString("CcaAdjParmAdjMonthAmt"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjEffStartDate="+adjRS.getString("CcaAdjParmAdjEffStartDate"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmAdjEffEndDate="+adjRS.getString("CcaAdjParmAdjEffEndDate"));
				gb.showLogMessage("D","check CCA ADJ PARM CcaAdjParmSpecFlag="+adjRS.getString("CcaAdjParmSpecFlag"));
				double dlSpecAmt1 = ta.selectAdjTxlog(adjRS.getString("CcaAdjParmAdjEffStartDate"), adjRS.getString("CcaAdjParmAdjEffEndDate"), "N", adjRS.getString("CcaAdjParmRiskType")+"%");
				double dlSpecAmt2 = ta.selectTxlogSpecNonMatch(adjRS.getString("CcaAdjParmAdjEffStartDate"), adjRS.getString("CcaAdjParmAdjEffEndDate"), adjRS.getString("CcaAdjParmRiskType")+"%");
				if (spRiskType.equals(adjRS.getString("CcaAdjParmRiskType")) && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffStartDate")) >= 0  && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffEndDate")) <= 0) {
					if ("Y".equals(adjRS.getString("CcaAdjParmSpecFlag"))) {
						gGate.isAdjSpecAmt = true;
						dlTmpAmt = adjRS.getDouble("CcaAdjParmAdjMonthAmt");
						gb.showLogMessage("D","check CCA ADJ PARM SPEC_FLAG "+spRiskType+" = "+adjRS.getString("CcaAdjParmRiskType")+" dlTmpAmt = "+dlTmpAmt);
					}
					else {
						blMatchRisk = true;
						if (dlSpecAmt1 + gGate.isoFiled4Value > adjRS.getDouble("CcaAdjParmAdjMonthAmt")) {
							dlRiskAmt =  gGate.curTotalUnpaidOfPersonal;
						}
						else {
							if ((gGate.curTotalUnpaidOfPersonal + gGate.isoFiled4Value + gGate.specAmtConsume) > gGate.wkAdjTot) {
								dlRiskAmt =  gGate.curTotalUnpaidOfPersonal;
							}
							else {
								dlRiskAmt =  gGate.curTotalUnpaidOfPersonal + adjRS.getDouble("CcaAdjParmAdjMonthAmt") + gGate.specAmtConsume - dlSpecAmt1;

							}
						}
						gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE "+spRiskType+" = "+adjRS.getString("CcaAdjParmRiskType")+" dlRiskAmt = "+dlRiskAmt);
					}
				}
				else {
					if (dlSpecAmt1 < adjRS.getDouble("CcaAdjParmAdjMonthAmt") && "Y".equals(adjRS.getString("CcaAdjParmSpecFlag"))) {
						dlSpecAmt = dlSpecAmt + (adjRS.getDouble("CcaAdjParmAdjMonthAmt") - dlSpecAmt1);
					}
					gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE = "+adjRS.getString("CcaAdjParmRiskType")+" dlSpecAmt = "+dlSpecAmt);
				}
				if ("Y".equals(adjRS.getString("CcaAdjParmSpecFlag")) && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffStartDate")) >= 0  && slCurdate.compareTo(adjRS.getString("CcaAdjParmAdjEffEndDate")) <= 0) {
					dlSpecAmtConsume = dlSpecAmtConsume + dlSpecAmt1;
					dlSpecAmtTotal   = dlSpecAmtTotal + adjRS.getDouble("CcaAdjParmAdjMonthAmt");
					dlSpecAmtMatch   = dlSpecAmtMatch + dlSpecAmt2;
				}
				gb.showLogMessage("D","check CCA ADJ PARM RISK_TYPE = "+adjRS.getString("CcaAdjParmRiskType")+" dlSpecAmt = "+dlSpecAmt);
			}
			gGate.specAmtConsume = dlSpecAmtConsume;
			gGate.specAmtTotal   = dlSpecAmtTotal;
			gGate.SpecAmtMatch   = dlSpecAmtMatch;
			gb.showLogMessage("D","check gGate.specAmtConsume = "+gGate.specAmtConsume);
			gb.showLogMessage("D","check gGate.specAmtTotal   = "+gGate.specAmtTotal);
			gb.showLogMessage("D","check gGate.SpecAmtMatch   = "+gGate.SpecAmtMatch);

			if (gGate.adjRiskType) { //個人有設定臨調的風險分類旗標，檢查是否對應到專款專用之限制額度旗標
				if (gGate.isAdjSpecAmt) {
					gGate.cacuFlag = "Y";
					dL_AdjAmt = gGate.wkAdjTot - dlSpecAmt;
					gb.showLogMessage("D","專款專用類別_臨調金額，臨調期間額度dL_AdjAmt="+dL_AdjAmt);
				}
				else if (blMatchRisk) {
					dL_AdjAmt = dlRiskAmt;
					gb.showLogMessage("D","風險限制類別_臨調金額，臨調期間額度dL_AdjAmt="+dL_AdjAmt);
				}
				else {
					dL_AdjAmt =  gGate.wkAdjTot - dlSpecAmt;
					gb.showLogMessage("D","已過期之(專款專用/風險限制)類別，臨調期間額度dL_AdjAmt="+dL_AdjAmt);
				}			
			}
			else {
				dL_AdjAmt =  gGate.wkAdjTot - dlSpecAmt;
				gb.showLogMessage("D","非(專款專用/風險限制)類別，臨調期間額度dL_AdjAmt="+dL_AdjAmt);			
			}
		}
		return dL_AdjAmt;
	}

	/**
	 * 檢核授權邏輯查核-取得臨調後的金額(商務卡)
	 * V1.00.38 P3授權額度查核調整
	 * @return 臨調後的金額(商務卡)
	 * @throws Exception if any exception occurred
	 */	
	private int getAdjAmtOfComp() {

		int nL_AdjAmt=0;
		if ( (gGate.isVirtualCard) &&  (gGate.isInstallmentTx) ){
			if (ta.getInteger("CardAcctAdjInstPctOfComp")>0) {
				nL_AdjAmt = ta.getInteger("CardAcctAdjInstPctOfComp"); /* 臨調分期付款金額  -本來是 %         */
				gb.showLogMessage("I", "****** 有公司臨調 + 是虛擬卡 + 分期。臨調後額度:" + nL_AdjAmt + "******");
			}
		}
		else {
			if (ta.getInteger("CardAcctTotAmtMonthOfComp")>0) {
				nL_AdjAmt = ta.getInteger("CardAcctTotAmtMonthOfComp");/* 臨調放大總月限金額  -本來是 %       */
				gb.showLogMessage("I", "****** 有公司臨調 。臨調後額度:" + nL_AdjAmt + "******");
			}
		}
		return nL_AdjAmt;
	}
	/**
	 * 卡片風險類別消費限額參數檔(CCA_RISK_CONSUME_PARM)計算預借現金總額度
	 * V1.00.38 P3授權額度查核調整
	 * @param dpCashAmt 預借現金額度
	 * @throws Exception in any exception occurred
	 */
	private double computeCashLimit(double dpCashAmt) throws Exception {

		//計算　預借現金總額度
		double dlCashAmt = 0;
		//由卡戶之 card acct risk level 取得該等級之預借現金總月限百分比及回覆碼參數
		String slRiskLevel = gGate.classCode;
		String slCardNote= ta.getValue("CardBaseCardNote");

		if (!ta.selectRiskConsumeParm("C", slRiskLevel, slCardNote)) {
			gGate.wkAdjAmt= 1;
		}
		else {
			gGate.wkCashCode = ta.getValue("RiskConsumeRspCode1");
			gGate.wkAdjAmt= ta.getDouble("RiskConsumeLmtAmtMonthPct")/100;
		}
		dlCashAmt = dpCashAmt*gGate.wkAdjAmt;//預借現金總額度 (C)

		return dlCashAmt;
	}
	/**
	 * 取得RiskLevel該等級之總月限百分比
	 * V1.00.38 P3授權額度查核調整
	 * @return 取得該等級之總月限百分比
	 * @throws Exception in any exception occurred
	 */
	private double getRiskLevelParm() {
		//down, 計算RiskLevel
		double dL_RiskLevelParm = 1;

		if (gGate.hasRiskLevelParm) {
			/*由卡戶之 card acct risk level 取得該等級之總月限百分比及回覆碼參數*/
			/*及預現最高限額*/
			if (gGate.isInstallmentTx)
				dL_RiskLevelParm = (double)ta.getDouble("RiskLevelInstMonthPct")/100; //分期總月限%
			else {
				dL_RiskLevelParm = (double)ta.getDouble("RiskLevelTotAmtPct")/100; //消費總月限%
			}
		}

		if (gGate.balanceInquiry) {
			gb.showLogMessage("I", "餘額查詢不計算shadow/RiskLevel，所以設定shadow/RiskLevel=1");
			dL_RiskLevelParm = 1;
		}

		return dL_RiskLevelParm;

	}
	
	/**
	 * 取得"依MCC Code 臨調"參數
	 * V1.00.38 P3授權額度查核調整
	 * @return boolean
	 * @throws Exception in any exception occurred
	 */
	private boolean getMccAdjParm()  throws Exception{
		//proc line 6061, function credit_check_adj_prod();
		if (gGate.balanceInquiry) {
			gb.showLogMessage("I", "餘額查詢不計算產品類別臨調檔");
			return false;
		}
		if (!ta.selectAdjProdParm()) //
			return false;

		int nlProdAdjParmTotAmtMonth = ta.getInteger("AdjProdParmTotAmtMonth");//MCC臨調放大總月限額
		int nlProdAdjParmTimesAmt = ta.getInteger("AdjProdParmTimesAmt"); //MCC臨調金額倍數百分比 Howard: 不能改用 int
		int nlProdAdjParmTimesCnt = ta.getInteger("AdjProdParmTimesCnt"); //MCC臨調次數倍數百分比 Howard: 不能改用 int

		gGate.hasMccAdjParm=true;
		gGate.mccAdjParmTotAmtMonth = nlProdAdjParmTotAmtMonth;//MCC 臨調放大總月限額
		gGate.mccAdjParmTimesAmt = nlProdAdjParmTimesAmt; //MCC 臨調金額倍數百分比 Howard: 不能改用 int
		gGate.mccAdjParmTimesCnt = nlProdAdjParmTimesCnt; //MCC臨調次數倍數百分比 Howard: 不能改用 int

		gb.showLogMessage("I", "依MCC ADJ臨調放大總月限額AmtMonth參數:"+nlProdAdjParmTotAmtMonth+"臨調金額倍數百分比TimesAmt參數:"+nlProdAdjParmTimesAmt+"臨調次數倍數百分比TimesCnt參數:" +nlProdAdjParmTimesCnt);


		return true;


	}
	/**
	 * 取得該等級之總額度-RiskLevel 的設定只會影響 信用卡天花板
	 * V1.00.38 P3授權額度查核調整
	 * @return int 該等級之總額度
	 * @throws Exception in any exception occurred
	 */
	private int computeFinalMonthLimitAmtByRiskLevel(int dP_Base, double dP_AdjPct, boolean bP_CacuIncreaseAmt) {

		gb.showLogMessage("I", "依據 RiskLevel參數調整前的額度:"+ dP_Base);
		gb.showLogMessage("I", "RiskLevel 調整倍數:"+ dP_AdjPct);

		int nlResult =(int)(dP_Base*dP_AdjPct); //取得該等級之總額度(A)
		//gGate.finalTotLimitAfterRiskLevel = (int)(dP_Base*dP_AdjPct); //取得該等級之總額度(A)

		if (bP_CacuIncreaseAmt) {
			int dL_IncreaseAmt = nlResult - dP_Base; //增加的金額

			int dL_RiskLevelAddTotAmt = ta.getInteger("RiskLevelAddTotAmt");//可以增加的最多金額

			gb.showLogMessage("I", "RiskLevel參數(金額上限):"+ dL_RiskLevelAddTotAmt);

			if (dL_IncreaseAmt>dL_RiskLevelAddTotAmt)
				nlResult = (int)(dP_Base + dL_RiskLevelAddTotAmt);
			else
				nlResult = (int)(dP_Base + dL_IncreaseAmt);

		}
		gb.showLogMessage("I", "依據 RiskLevel參數調整後的額度:"+ nlResult);

		return nlResult;
	}
}
