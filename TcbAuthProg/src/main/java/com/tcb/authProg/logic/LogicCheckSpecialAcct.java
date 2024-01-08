/**
 * 授權邏輯查核-特指戶檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-特指戶檢核處理                     *
 * 2022/04/01  V1.00.01  Kevin       取消海外密集消費檢查                          *
 * 2022/07/07  V1.00.02	 Kevin       調整特指戶判斷條件                            *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/06/01  V1.00.46  Kevin       P3批次授權比照一般授權的邏輯，不須特別排除            *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.math.BigDecimal;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckSpecialAcct extends AuthLogic {

	public LogicCheckSpecialAcct(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckSpecialAcct : started");

	}
	
	/**
	 * 特指戶查核
	 * V1.00.46 P3批次授權比照一般授權的邏輯，不須特別排除
	 * @return 如果特指戶查核成功return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean specCheck() throws Exception {
		boolean blResult = true;

		if (ifIgnoreProcess())
			return true;

		if (gGate.isPurchaseCard) { //採購卡不做檢核
			return true;
		}
		
		String slRealBlockCode="";

		slRealBlockCode = getRealBlockCode();

		//down, proc : 特指戶之設定過期
		String slCurDate = HpeUtil.getCurDateStr(false);
		String slRealSpecFlag = gGate.cardBaseSpecFlag; 
	
		if (  ("Y".equals(gGate.cardBaseSpecFlag))  && ((HpeUtil.compareDateString(slCurDate, gGate.cardBaseSpecDelDate))>0) ){
			slRealSpecFlag = "N";
		}
		if (slRealBlockCode.length()>0) {//有特指
			if (!ta.selectSpecCode(slRealBlockCode)) {
				return true;
			}
			if (gGate.isDebitCard && "Y".equals(ta.getValue("SC_SPEC_SEND_IBM"))) {
				return true;
			}
			if (!checkSpecLevel()) { //Howard => Proc 中 FALSE繼續往下走,TRUE為拒絕, 但這做法不一樣
				if ("61".equals(slRealBlockCode.substring(0, 2))) { //proc line 5066
					gGate.authRemark = "特指參數回覆碼" + getSpecRespCode();	
				}
				return false; 
			}

			gGate.scSpecCode = slRealBlockCode;
		}
		//V1.00.01 取消海外密集消費檢查
//		if (  ("F".equals(gGate.areaType)) && 
//				(!("9".equals(gGate.classCode))) && 
//				(("02".equals(gGate.entryMode.substring(0,2))) || ("90".equals(gGate.entryMode.substring(0,2))) )){
//			if ( (!gGate.isDebitCard) || 
//					((gGate.isDebitCard) && (!"6010".equals(gGate.mccCode)) && (!"6011".equals(gGate.mccCode)) ) ) {
//
//				ta.selectCcaAuthTxLog4ForeignTrade();
//			
//				if (ta.getInteger("AuthTxLogCount4ForeignTrade")>=3) {
//					gGate.isSpecW0 = true;
//					slRealSpecFlag = "Y";
//					gGate.cardBaseSpecDelDate= HpeUtil.getCurDateStr(false);
//					gGate.cardBaseSpecStatus   = "W0";
//					gGate.scSpecCode = gGate.cardBaseSpecStatus;
//				}
//			}
//		}


		if ("N".equals(slRealSpecFlag)) //不是特指戶
			return true;

		if ((HpeUtil.compareDateString(HpeUtil.getCurDateStr(false), gGate.cardBaseSpecDelDate)) >0 )
			return true;

		if ("Y".equals(slRealSpecFlag)) {
			gGate.scSpecCode = gGate.cardBaseSpecStatus;
		}

		if (!("".equals(gGate.cardBaseSpecStatus))) {

			if (!ta.selectSpecCode(gGate.cardBaseSpecStatus))
				return true;
			if (gGate.isDebitCard && "Y".equals(ta.getValue("SC_SPEC_SEND_IBM"))) {
				return true;
			}
			blResult = checkSpecLevel();
		}

		return blResult;
	}
	
	/**
	 * 取得block code
	 * V1.00.38 P3授權額度查核調整-帳戶凍結不含公司戶統編
	 * @return block code
	 * @throws Exception if any exception occurred
	 */
	private String getRealBlockCode() throws Exception{
		String slRealBlockCode = "", slSpecDelDate="";
		slRealBlockCode = ta.getValue("CardAcctSpecStatus");
		slSpecDelDate = ta.getValue("CardAcctSpecDelDate");

		if (slRealBlockCode.length()>0) {
			if (slSpecDelDate.length()>0) {
				//down, 判斷  特殊指示戶有效日期 應該要 > 今天，特指才有效
	    		String slCurDate = HpeUtil.getCurDateStr(false);
	    		int nlResult = HpeUtil.compareDateString(slCurDate, slSpecDelDate);
	    		if (nlResult>0)
	    			slRealBlockCode = ""; //亦即讓 特指失效
			}
		}
		return slRealBlockCode;
	}
	
	/**
	 * 取得Spec 錯誤回覆碼RESP_CODE
	 * @return 回覆碼RESP_CODE
	 * @throws Exception if any exception occurred
	 */
	private String getSpecRespCode() {
		return ta.getValue("SC_SPEC_RESP_CODE").substring(0,2);
	}
	
	/*特指戶檢核*/
	/**
	 * 特指戶檢核
	 * @return 如果特指戶檢核查核成功return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean checkSpecLevel() throws Exception {
		if (  ("11".equals(ta.getValue("SC_SPEC_VISA_REASON")))  && 
				("V".equals(ta.getValue("CARD_TYPE"))) ){  //VISA VIP 客戶
			ta.getAndSetErrorCode(getSpecRespCode());
			gGate.isVip = true; //需佔額度 
			return false;
		}
		String slCurrency = "840";//美金幣別
		String slCardType = ta.getValue("CARD_TYPE");
		BigDecimal dlExchangeRate = new BigDecimal("1");

		//MasterCard VIP and tx amt must lower than MC_VIP_AMT*exchange rate
		if (  ("V".equals(ta.getValue("SC_SPEC_MAST_REASON")))  && 
				("M".equals(slCardType)) ){ //MasterCard VIP 客戶

			if(ta.selectCurrentRate(slCurrency)) {
				dlExchangeRate = new BigDecimal(ta.getValue("EXCHANGE_RATE"));
			}

			BigDecimal dlSpecMstVipAmt = new BigDecimal(ta.getValue("CardBaseSpecMstVipAmt"));
			BigDecimal dlTargetAmt = dlSpecMstVipAmt.multiply(dlExchangeRate);

			BigDecimal dlIsoField4Value = new BigDecimal(Double.toString(gGate.isoFiled4Value));
			double dlDiff = (dlIsoField4Value.subtract(dlTargetAmt)).doubleValue(); 
			if (dlDiff<=0) {
				//金額超過VIP設定
				ta.getAndSetErrorCode(getSpecRespCode());
				gGate.isVip = true; //需佔額度
				return false;
			}
			else 
				return true;
		}

		boolean blCheckSpecDetl4RejectTrade=false,blCheckSpecDetl4ApproveTrade=false;
		String slSpecCheckLevel = ta.getValue("SC_SPEC_CHECK_LEVEL"); //作業指示碼

		if ("0".equals(slSpecCheckLevel)) {
			//檢核條件，若符合，則拒絕交易
			//gb.showLogMessage("D","作業指示碼=>" + "0，表示拒絕交易!");
			blCheckSpecDetl4RejectTrade=true;
		}
		else if ("1".equals(slSpecCheckLevel)) {
			//直接回覆
			String slSpecRespCode = getSpecRespCode();

			gb.showLogMessage("D","作業指示碼=>" + "1，表示直接回覆!");
			gb.showLogMessage("D","On Us 回覆碼=>" + slSpecRespCode);

			ta.getAndSetErrorCode(slSpecRespCode); 
			return false;
		}
		else if ("2".equals(slSpecCheckLevel)) {
			//作業指示=2  => 額度內100% 可用
			gGate.isSpecUse = true; 
			//return false;
			return true;
		}
		else if ("3".equals(slSpecCheckLevel)) {
			//檢核條件，若符合，則核准交易
			blCheckSpecDetl4ApproveTrade=true;
		}

		//down, 檢核 CCA_SPEC_DETL 是否有值
		//V1.00.02 調整特指戶判斷條件
		boolean blResult = true;
		if ((blCheckSpecDetl4ApproveTrade) || (blCheckSpecDetl4RejectTrade)) {
			String slScSpecCheckFlag01 = ta.getValue("SC_SPEC_CHECK_FLAG01").trim();
			String slScSpecCheckFlag02 = ta.getValue("SC_SPEC_CHECK_FLAG02").trim();
			String slScSpecCheckFlag03 = ta.getValue("SC_SPEC_CHECK_FLAG03").trim();
			String slScSpecCheckFlag04 = ta.getValue("SC_SPEC_CHECK_FLAG04").trim();
			String slScSpecCheckFlag05 = ta.getValue("SC_SPEC_CHECK_FLAG05").trim();
			String slScSpecCheckFlag06 = ta.getValue("SC_SPEC_CHECK_FLAG06").trim();
			String slSpecCode= ta.getValue("SC_SPEC_CODE");
			String slDataType="";
			String slDataCode="";
			String slDataCode2="";
			boolean blMatched=false;
			if (!"00".equals(slScSpecCheckFlag01)) {
				slDataType = slScSpecCheckFlag01;
				slDataCode = gGate.mccRiskType;
				ta.selectSpecDetl(slSpecCode, slDataType, slDataCode,slDataCode2);
				if (Integer.parseInt(ta.getValue("SC_SPEC_DETL_COUNT"))>0)//Howard:如果有資料，則繼續交易
					blMatched = true;
			}
			if ((!blMatched) && (!"00".equals(slScSpecCheckFlag02))) {
				slDataType = slScSpecCheckFlag02;
				slDataCode = gGate.mccCode;
				ta.selectSpecDetl(slSpecCode, slDataType, slDataCode,slDataCode2);
				if (Integer.parseInt(ta.getValue("SC_SPEC_DETL_COUNT"))>0) //Howard:如果有資料，則繼續交易
					blMatched = true;
			}
			if ((!blMatched) && (!"00".equals(slScSpecCheckFlag03)) ) {
				slDataType = slScSpecCheckFlag03;
				slDataCode = gGate.eci;
				ta.selectSpecDetl(slSpecCode, slDataType, slDataCode,slDataCode2);
				if (Integer.parseInt(ta.getValue("SC_SPEC_DETL_COUNT"))>0)//Howard:如果有資料，則繼續交易
					blMatched = true;
			}
			if ((!blMatched) && (!"00".equals(slScSpecCheckFlag04))) {
				slDataType = slScSpecCheckFlag04;
				slDataCode = gGate.ucafInd;
				ta.selectSpecDetl(slSpecCode, slDataType, slDataCode,slDataCode2);
				if (Integer.parseInt(ta.getValue("SC_SPEC_DETL_COUNT"))>0)//Howard:如果有資料，則繼續交易
					blMatched = true;
			}
			//調整特指戶判斷條件-網路交易檢查
			if ((!blMatched) &&("Y".equals(slScSpecCheckFlag05))) {
				if (gGate.ecTrans) {
					blMatched = true;
				}
			}
			if ((!blMatched) &&(!"00".equals(slScSpecCheckFlag06))) {
				slDataType = slScSpecCheckFlag06;
				slDataCode = ta.getValue("CountryCode");
				slDataCode2 = gGate.entryMode;
				ta.selectSpecDetl(slSpecCode, slDataType, slDataCode,slDataCode2);
				if (Integer.parseInt(ta.getValue("SC_SPEC_DETL_COUNT"))>0)//Howard:如果有資料，則繼續交易
					blMatched = true;
			}
			if (blCheckSpecDetl4ApproveTrade) {
				if (blMatched) {
						return true;
				}
				else {
					ta.getAndSetErrorCode(getSpecRespCode()); 
					return false;
				}
			}
			else {
				if (blMatched) {
					ta.getAndSetErrorCode(getSpecRespCode()); 
					return false;
				}
				else {
					return true;
				}
			}
		}
		//down, 檢核 CCA_SPEC_DETL 是否有值		
		return blResult;
	}
}
