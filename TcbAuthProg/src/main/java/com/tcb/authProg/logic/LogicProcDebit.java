/**
 * 授權邏輯查核-Debit交易的流程與檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-Debit交易的流程與檢核處理            *
 * 2023/05/24  V1.00.44  Kevin       VD退貨交易不佔參數限額檢查                      *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcDebit extends AuthLogic {

	public LogicProcDebit(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcDebit : started");

	}
	
	/* debit check*/
	/**
	 * handle debit card 交易的處理流程與檢核
	 * V1.00.44 VD退貨交易不佔參數限額檢查
	 * @return 如果正常處理完成return true，否則return false
	 * @throws Exception if any exception
	 */
	public boolean processDebit() throws Exception {
		//debit txn send to IMS
		gGate.ifSendTransToIms = true;
		if (gGate.isDebitCard) {
			if ( (gGate.forcePosting) && (!"5542".equals(gGate.mccCode)) ){
				return true;
			}
			if (gGate.isInstallmentTx) {  
				ta.getAndSetErrorCode("1D");//不可做分期付款
				return false;
			}

			//down, 檢核限額限次及有無臨調 
			if (!ifIgnoreProcess4()) {	//V1.00.44 VD退貨交易不佔參數限額檢查(排除退貨、沖正交易)
				if (!debitCheck())
					return false;
			}
		}								
		return true;
	}
	
	
	/**
	 * debit card 交易檢核 -檢核DEBIT CARD限額,限次及有無臨調 
	 * @return 如果檢核通過return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean debitCheck() throws Exception{
		if ("N".equals(ta.getValue("DebitParmNoConnFlag"))) {
			String slEntryMode = gGate.entryMode.substring(0, 2);

			boolean blCFlag = false;
			if (("02".equals(slEntryMode)) 	|| ("05".equals(slEntryMode)) || ("07".equals(slEntryMode))
					|| ("90".equals(slEntryMode)) || ("91".equals(slEntryMode))) {
				blCFlag = true;
			}
			if (!blCFlag) {//不為以上ENTRY MODE則拒絕 
				if (!ta.selectCcaMchtNotOnLine()) {
					ta.getAndSetErrorCode("4D"); //ENTRY MODE Cannot continue
					return false;
				}
			}
		}
		boolean blAdj = gGate.bgHasPersonalAdj; //checkAcctAdj(1);//proc is => check_acct_adj
		BigDecimal bdAdjRate = new BigDecimal("1");
		BigDecimal bdPercentage = new BigDecimal("100");

		if (blAdj) { //有個人臨調
			if (ta.getDouble("CardAcctTotAmtMonth")>0)
				bdAdjRate = ta.getBigDecimal("CardAcctTotAmtMonth").divide(bdPercentage);
		}

		gb.showLogMessage("D","blAdj="+blAdj+"@@@@dlAdjRate="+ bdAdjRate );

		BigDecimal bdTmpMonthAmt = new BigDecimal("0");
		BigDecimal bdTmpDayAmt = new BigDecimal("0");
		BigDecimal bdTmpCntAmt = new BigDecimal("0");
		BigDecimal bdTmpDayCnt = new BigDecimal("0");

		if (gGate.debitParmTableFlag <= 2) { //debit card 一般限額限次檢查
			bdTmpMonthAmt = ta.getBigDecimal("DebitParmMonthAmount").multiply(bdAdjRate);
			bdTmpDayAmt = ta.getBigDecimal("DebitParmDayAmount").multiply(bdAdjRate);
			bdTmpCntAmt = ta.getBigDecimal("DebitParmCntAmount").multiply(bdAdjRate);
			bdTmpDayCnt = ta.getBigDecimal("DebitParmDayCnt").multiply(bdAdjRate);
			
			//-月限額-
			BigDecimal bdCurrAmtMm = new BigDecimal(Double.toString(gGate.isoFiled4Value)).add(getMonthlyTradeAmt());
			/*bdCurrAmtMm = 本次交易金額 + 月累計消費金額 */

			BigDecimal bdMonthUseLimit =bdTmpMonthAmt.subtract(bdCurrAmtMm);

			gb.showLogMessage("D","checkLimit_all : lm_loc_lmt(總可用額度): " + bdTmpMonthAmt);
			gb.showLogMessage("D","checkLimit_all : curr_amt_mm(月已用額度):" + getMonthlyTradeAmt());
			gb.showLogMessage("D","checkLimit_all : dL_MonthUseLimit(月剩餘額度):" + bdMonthUseLimit);
			gb.showLogMessage("D","checkLimit_all : 刷卡金額:" + gGate.isoFiled4Value + "--調整金額:" + gGate.adjustAmount);
			gb.showLogMessage("D","@@@@dlCurrAmtMm="+ bdCurrAmtMm + " <= dL_TmpMonthAmt="+bdTmpMonthAmt);

			if (bdCurrAmtMm.compareTo(bdTmpMonthAmt) > 0) {
				ta.  getAndSetErrorCode("5D");
				gGate.monthLimitNotEnough =true;
				return false;
			}

			//-日限額-
			BigDecimal bdDayUseLimit = new BigDecimal(Double.toString(gGate.isoFiled4Value)).add(getDailyTradeAmt()); 

			gb.showLogMessage("D","@@@@dlDayUseLimit="+ bdDayUseLimit   + " <= dlTmpDayAmt="+bdTmpDayAmt);

			if (bdDayUseLimit.compareTo(bdTmpDayAmt) > 0) {
				ta.getAndSetErrorCode("6D");
				gGate.dayLimitNotEnough =true;
				return false;
			}
			
			//-次限額-
			gb.showLogMessage("D","@@@@isoFiled4Value="+ gGate.isoFiled4Value + " <= dlTmpCntAmt="+bdTmpCntAmt);

			if (new BigDecimal(Double.toString(gGate.isoFiled4Value)).compareTo(bdTmpCntAmt) > 0) {
				ta.getAndSetErrorCode("7D"); 
				gGate.dayLimitNotEnough =true;
				return false;
			}
			
			//-日限次-
			int ilCurrentTxCnt = getDailyTradeCnt(); 

			gb.showLogMessage("D","@@@@ilCurrentTxCnt="+ ilCurrentTxCnt +1 + " <= nlTmpDayCnt="+bdTmpDayCnt.setScale(0,RoundingMode.FLOOR).intValue());

			if (ilCurrentTxCnt + 1 > bdTmpDayCnt.setScale(0,RoundingMode.FLOOR).intValue()) {
				ta.getAndSetErrorCode("8D");
				gGate.dayLimitNotEnough =true;
				return false;
			}
		}
		if (gGate.debitParmTableFlag >= 2) { //debit card 風險分類限額限次檢查
			if (blAdj) { //有臨調 and 風險類別族群
				if (!ta.selectDebitAndRiskInfo(1)) {
					ta.getAndSetErrorCode("9C");
					return false;
				}
			}
			else { //
				if (!ta.selectDebitAndRiskInfo(2)) {
					ta.getAndSetErrorCode("9C");
					return false;
				}
			}

			BigDecimal bdWkDayAmt = new BigDecimal(Double.toString(gGate.riskTradeDayAmt)).add(new BigDecimal(Double.toString(gGate.isoFiled4Value)));
			int nlWkDayCnt = gGate.riskTradeDayCnt + 1;
			BigDecimal bdWkMonthAmt = new BigDecimal(Double.toString(gGate.riskTradeMonthAmt)).add(new BigDecimal(Double.toString(gGate.isoFiled4Value)));
			int nlWkMonthCnt = gGate.riskTradeMonthCnt + 1;
			
			gb.showLogMessage("D","是否臨調=>" + blAdj);
			gb.showLogMessage("D","是否檢查日限次=>" + ta.getValue("AuthParmDayRiskChk"));
			gb.showLogMessage("D","是否檢查月限次=>" + ta.getValue("AuthParmMonthRiskChk"));
			gb.showLogMessage("D","消費金額=>" + gGate.isoFiled4Value);
			gb.showLogMessage("D","@@@@dlWkMonthAmt="+ bdWkMonthAmt + " <= dL_TmpMonthAmt="+ta.getBigDecimal("MonthAmtLimitAfterJoin"));
			gb.showLogMessage("D","@@@@dlWkDayAmt="+ bdWkDayAmt   + " <= dL_TmpDayAmt="+ta.getBigDecimal("DayAmtLimitAfterJoin"));
			gb.showLogMessage("D","@@@@isoFiled4Value="+ gGate.isoFiled4Value + " <= dL_TmpCntAmt="+ta.getBigDecimal("TimesLimitAmtAfterJoin"));
			gb.showLogMessage("D","@@@@dlWkMonthCnt="+ nlWkMonthCnt + " <= dL_TmpMonthAmt="+ta.getBigDecimal("MonthTimesLimitAfterJoin"));
			gb.showLogMessage("D","@@@@dlWkDayCnt="+ nlWkDayCnt + " <= dL_TmpDayCnt="+ta.getBigDecimal("DayTimesLimitAfterJoin"));
			
			if (new BigDecimal(Double.toString(gGate.isoFiled4Value)).compareTo(ta.getBigDecimal("TimesLimitAmtAfterJoin")) > 0) { //  消費金額 > 次限額
				ta.getAndSetErrorCode("9C");
				return false;
			}
			
			BigDecimal bdDayAmtLimitAfterJoin = ta.getBigDecimal("DayAmtLimitAfterJoin"); //
			if ( (bdDayAmtLimitAfterJoin.compareTo(BigDecimal.ZERO) > 0) && (bdWkDayAmt.compareTo(bdDayAmtLimitAfterJoin) > 0 )) { //  卡戶風險分類授權交易之 本日累積交易金額 > 日限額
				ta.getAndSetErrorCode("9B");
				return false;
			}

			int nlDayTimesLimitAfterJoin = ta.getBigDecimal("DayTimesLimitAfterJoin").setScale(0,RoundingMode.FLOOR).intValue(); //
			if("1".equals(ta.getValue("AuthParmDayRiskChk"))) {
				if ( (nlDayTimesLimitAfterJoin > 0) && (nlWkDayCnt>nlDayTimesLimitAfterJoin) ) { //  卡戶風險分類授權交易之 本日累積交易次數 > 日限次
					ta.getAndSetErrorCode("9E");
					return false;
				}
			}

			BigDecimal bdMonthAmtLimitAfterJoin = ta.getBigDecimal("MonthAmtLimitAfterJoin");//
			if ( (bdMonthAmtLimitAfterJoin.compareTo(BigDecimal.ZERO) > 0) && (bdWkMonthAmt.compareTo(bdMonthAmtLimitAfterJoin) > 0 )) { //  卡戶風險分類授權交易之 本月累積交易金額 > 月限額
				ta.getAndSetErrorCode("9A");
				return false;
			}

			int nlMonthTimesLimitAfterJoin = ta.getBigDecimal("MonthTimesLimitAfterJoin").setScale(0,RoundingMode.FLOOR).intValue();//
			if ("1".equals(ta.getValue("AuthParmMonthRiskChk"))) {
				if ( (nlMonthTimesLimitAfterJoin>0) && (nlWkMonthCnt>nlMonthTimesLimitAfterJoin) ) { //  卡戶風險分類授權交易之 本月累積交易次數 > 月限次
					ta.getAndSetErrorCode("9D");
					return false;
				}
			}
		}
		return true;
	}
}
