/**
 * 授權邏輯查核-特店風險驗證處理
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2022/03/26
 * 
 * @throws  Exception if any exception occurred
 * @return  boolean return True or False
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2022/03/26  V1.00.00  Kevin       授權邏輯查核-特店風險驗證處理                     *
 * 2023/12/12  V1.00.62  Kevin       新增指定單一特店代碼或指定單一收單行註記風險特店        *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckMchtRisk extends AuthLogic {
	
	public LogicCheckMchtRisk(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckMchtRisk : started");

	}
	
	/**
	 * 檢核特店風險驗證結果
	 * @return 如果特店風險驗證成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 * @return  boolean return True or False
	 */
	public boolean checkMchtRisk() throws Exception {

		boolean blResult = true;
		
		if ("0".equals(ta.getValue("AuthParmMchtChk"))) { //

			gb.showLogMessage("D","function checkMchtInfo()，AuthParmMchtChk==0，所以不檢核 Mcht Info");

			return true;
		}
		//kevin:非購貨交易或預借現金，例如三大票證來源，不需檢查特店
		if (gGate.nonPurchaseTxn || gGate.ticketTxn) { //

			gb.showLogMessage("D","function checkMchtInfo()，nonPurchaseTxn，所以不檢核 Mcht Info");

			return true;
		}

		String slMchtNo= gGate.merchantNo; /* 特店代碼         */

		String slAcqBankId = ta.getAcqBankId(gGate.isoField[32]); /* 收單行代碼       */

		boolean bLFindMchtRiskData=true;

		String slMccCode= gGate.mccCode;

		ta.getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
		gb.showLogMessage("D","function checkMchtRisk()-1，MCHT_NO="+ gGate.merchantNo+"，ACQ_BANK_ID="+slAcqBankId+"，MCC_CODE="+slMccCode);
		if (ta.notFound.equals("Y")) {
			gb.showLogMessage("D", "function checkMchtRisk()-1，沒找到資料，用MCC_CODE=*，再找一次");
			slMccCode = "*";
			ta.getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
			gb.showLogMessage("D","function checkMchtRisk()-2，MCHT_NO="+ gGate.merchantNo+"，ACQ_BANK_ID="+slAcqBankId+"，MCC_CODE="+slMccCode);
			if (ta.notFound.equals("Y")) {
				gb.showLogMessage("D", "function checkMchtRisk()-2，沒找到資料，用ACQ_BANK_ID=*、MCC_CODE=*，再找一次");
				slAcqBankId = "*";
				ta.getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
				gb.showLogMessage("D","function checkMchtRisk()-3，MCHT_NO="+ gGate.merchantNo+"，ACQ_BANK_ID="+slAcqBankId+"，MCC_CODE="+slMccCode);
				if (ta.notFound.equals("Y")) {
					gb.showLogMessage("D", "function checkMchtRisk()-3，沒找到資料，用MCHT_CODE=*、MCC_CODE=*，再找一次");
					slMchtNo = "*";
					slAcqBankId = ta.getAcqBankId(gGate.isoField[32]);
					ta.getMchtRisk(slMchtNo, slAcqBankId, slMccCode);
					gb.showLogMessage("D","function checkMchtRisk()-4，MCHT_NO="+ gGate.merchantNo+"，ACQ_BANK_ID="+slAcqBankId+"，MCC_CODE="+slMccCode);
					if (ta.notFound.equals("Y")) {
						bLFindMchtRiskData = false;
						gb.showLogMessage("D", "function checkMchtRisk()-4，沒找到資料，表示沒有管制，所以允許交易!");
						blResult = true; // 沒找到資料，表示沒有管制，所以允許交易
					}
				}
			}
			//up, 用 MCC_CODE='*' search

		}
    	//免照會VIP不須檢查風險特店
		if (gGate.isAuthVip) {
			gb.showLogMessage("D","function checkMchtInfo()，isAuthVip，所以不檢核 Mcht Info");
			bLFindMchtRiskData = false;
		}
		//排除部分交易不需檢查風險特店
		if (ifIgnoreProcess()) {
			bLFindMchtRiskData = false;
		}
		
		if (bLFindMchtRiskData) {

			ta.selectCcaAuthTxLog();


			String slMchtRiskRiskStartDate = ta.getValue("MchtRiskRiskStartDate");
			String slMchtRiskRiskEndDate = ta.getValue("MchtRiskRiskEndDate");

			int nlMchtRiskAuthAmtRate = ta.getInteger("MchtRiskAuthAmtRate"); //總額度 * 管制金額%
			int nlMchtRiskAuthAmtS = ta.getInteger("MchtRiskAuthAmtS"); /* 金額區間-s       */
			int nlMchtRiskAuthAmtE = ta.getInteger("MchtRiskAuthAmtE"); /* 金額區間-E       */
			String slMchtRiskEdcPosNo1 = ta.getValue("MchtRiskEdcPosNo1");
			String slMchtRiskEdcPosNo2 = ta.getValue("MchtRiskEdcPosNo2");
			String slMchtRiskEdcPosNo3 = ta.getValue("MchtRiskEdcPosNo3");
			String slMchtRiskMchtRiskCode = ta.getValue("MchtRiskMchtRiskCode");


			if ((HpeUtil.isCurDateBetweenTwoDays(slMchtRiskRiskStartDate, slMchtRiskRiskEndDate)) &&
					(slMchtRiskMchtRiskCode.length()>0) ){
				//介於管制期間，所以要進行檢核
				String slMchtRiskLevelRspCode = "";
				if(ta.selectMchtRiskLevel(slMchtRiskMchtRiskCode)) {
					slMchtRiskLevelRspCode = ta.getValue("MchtRiskLevelRspCode");
				}

				boolean blPassMasterChecking = true;

				if((slMchtRiskEdcPosNo1.length()==0) && (slMchtRiskEdcPosNo2.length()==0) &&
						(slMchtRiskEdcPosNo3.length()==0) && (nlMchtRiskAuthAmtE==0) &&
						(nlMchtRiskAuthAmtS==0) && (nlMchtRiskAuthAmtRate==0) ) {
					blPassMasterChecking = false;
					//所有欄位都是0，所以拒絕交易
				}

				//down, check 本行自定控管特店-拒絕交易

				String slTerminal= gGate.isoField[41].trim();

				int nlTransAmt = (int) gGate.ntAmt;



				//down, 檢查端末機 是否有管制
				if (blPassMasterChecking) {
					if ( 	((slMchtRiskEdcPosNo1.length() >0) && (slTerminal.equals(slMchtRiskEdcPosNo1)) ) ||
							((slMchtRiskEdcPosNo2.length() >0) && (slTerminal.equals(slMchtRiskEdcPosNo2)) ) ||
							((slMchtRiskEdcPosNo3.length() >0) && (slTerminal.equals(slMchtRiskEdcPosNo3)) )
							){
						blPassMasterChecking = false;
						//getAndSetErrorCode(slMchtRiskLevelRspCode);
						//return false;
					}
				}
				//up, 檢查端末機 是否有管制

				//down, 檢查交易筆數
				if (blPassMasterChecking) {
					int nlAuthTxLogCount = ta.getInteger("AuthTxLogDayCount");
					int nlMchtRiskDayLimitCnt = ta.getInteger("MchtRiskDayLimitCnt"); 
					if ( (nlMchtRiskDayLimitCnt>0) && 
							(nlMchtRiskDayLimitCnt<(nlAuthTxLogCount+1)) ) {
						blPassMasterChecking = false;
					}
				}
				//up, 檢查交易筆數
				
				gb.showLogMessage("D","function checkMchtRisk()，ntamt:"+nlTransAmt+" >= Ranger for MchtRiskAuthAmtS:"+nlMchtRiskAuthAmtS);
				gb.showLogMessage("D","function checkMchtRisk()，ntamt:"+nlTransAmt+" <= Ranger for MchtRiskAuthAmtE:"+nlMchtRiskAuthAmtE);

				//down, 檢查金額區間
				if (blPassMasterChecking) {
					if ((nlMchtRiskAuthAmtS==0) && (nlMchtRiskAuthAmtE==0)){
						//起訖金額都是0所以拒絕交易
						blPassMasterChecking = false;
					}
					else if ( (nlTransAmt>=nlMchtRiskAuthAmtS ) && 
							(  nlTransAmt>nlMchtRiskAuthAmtE ) )  {
						blPassMasterChecking = false;
					}
				}
				//up, 檢查金額區間

				//down, 檢核消費金額
				//新作法 
				if (blPassMasterChecking) {
					if (nlMchtRiskAuthAmtRate>0) {
						//double dlBase =gGate.finalTotLimitAfterRiskLevel;
						double dlBase = ta.getBaseLimit();

						double nlLimit1 = dlBase*(double)nlMchtRiskAuthAmtRate/100; //總額度 * 管制金額%


						if ((nlLimit1>0) && (gGate.ntAmt>=nlLimit1)) {
							gb.showLogMessage("D","function checkMchtRisk()，dlBase=>"+ dlBase);
							gb.showLogMessage("D","function checkMchtRisk()，MchtRiskAuthAmtRate=>"+ ta.getInteger("MchtRiskAuthAmtRate"));
							gb.showLogMessage("D","function checkMchtRisk()，nlLimit1=dlBase*1000*MchtRiskAuthAmtRate，  nlLimit1=>"+ nlLimit1);


							gb.showLogMessage("D","function checkMchtRisk()，消費金額(isoFiled4Value) > 可消費的最高額度(nlLimit1)，所以拒絕交易");
							blPassMasterChecking = false;
							//getAndSetErrorCode(slMchtRiskLevelRspCode); 舊系統無此 error
							//return false;// 消費金額 >= 可消費的最高額度，則拒絕交易
						}

					}
				}

				//up, check 本行自定控管特店-拒絕交易

				int nlLimit3 = ta.getInteger("MchtRiskDayTotAmt");/* 管制日累計金額   */ 

				int nlTotalTxAmt4RiskChecking = ta.getInteger("AuthTxLogDayAmount");; //當日、在某特店的已消費金額 
				int nlTxCount4RiskChecking = ta.getInteger("AuthTxLogDayCount");; //當日、在某特店的已消費次數


				//down, 檢核 日累計金額 
				if (blPassMasterChecking) {
					if  ((nlLimit3>0) && ((gGate.ntAmt+nlTotalTxAmt4RiskChecking) >=  nlLimit3)) {
						blPassMasterChecking = false;
					}
				}
				//up, 檢核 日累計金額


				int nlLimit4 = ta.getInteger("MchtRiskDayLimitCnt"); /* 日累積限制筆數   */

				if (blPassMasterChecking) {
					//down, 檢核 日累積限制筆數
					if  ((nlLimit4>0) && ((1+nlTxCount4RiskChecking) >  nlLimit4)) {
						blPassMasterChecking = false;
						//getAndSetErrorCode(slMchtRiskLevelRspCode); 
						//return false;
					}
				}
				//up, 檢核 日累積限制筆數

				int nlMchtRiskDetl =  ta.getMchtRiskDetl();
				gb.showLogMessage("D","function1 getMchtRiskDetl()，nlMchtRiskDetl="+nlMchtRiskDetl+";blPassMasterChecking="+blPassMasterChecking);

				/*
				nlMchtRiskDetl= 1; 表示找不到卡號資料
				nlMchtRiskDetl= 2; 表示沒有設定日期區間
				nlMchtRiskDetl= 3; 表示有設定日期區間，但是已經逾期失效
				nlMchtRiskDetl= 4; 表示 單日累計金額參數設定為 0
				nlMchtRiskDetl= 5; 表示 日累積金額　沒有超過　單日累計金額參數
				nlMchtRiskDetl= 6; 表示 日累積金額　超過　單日累計金額參數
			    */
				if (blPassMasterChecking) {
					if (nlMchtRiskDetl==6)
						blResult = false;
				}
				else {

					if ((nlMchtRiskDetl!=4) && (nlMchtRiskDetl!=5)) {
						gb.showLogMessage("D","function2 getMchtRiskDetl()，nlMchtRiskDetl="+nlMchtRiskDetl+";blPassMasterChecking="+blPassMasterChecking);
						blResult = false;
					}
				}

				if (!blResult) {
					ta.getAndSetErrorCode(slMchtRiskLevelRspCode);	
				}
			}
		}
		gb.showLogMessage("D","End function checkMchtRisk()!");
		return blResult;
	}
}
