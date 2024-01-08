/**
 * 收單-分期處理共用物件
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
 * 2017/07/01  V1.00.00  Edson       收單-分期處理共用物件                         *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.bil;


public class InstallmentTxData {

	public String getValue1() {
		return value1;
	}
	public void setValue1(String value1) {
		this.value1 = value1;
	}
	public String getValue2() {
		return value2;
	}
	public void setValue2(String value2) {
		this.value2 = value2;
	}
	public String getValue3() {
		return value3;
	}
	public void setValue3(String value3) {
		this.value3 = value3;
	}
	public String getTotalTerm() {
		return totalTerm;
	}
	public void setTotalTerm(String totalTerm) {
		this.totalTerm = totalTerm;
	}
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	public String getProdNo() {
		return prodNo;
	}
	public void setProdNo(String prodNo) {
		this.prodNo = prodNo;
	}
	public String getMchtNo() {
		return mchtNo;
	}
	public void setMchtNo(String mchtNo) {
		this.mchtNo = mchtNo;
	}
	public String getAuthRemark() {
		return authRemark;
	}
	public void setAuthRemark(String authRemark) {
		this.authRemark = authRemark;
	}
	public String getEntryMode() {
		return entryMode;
	}
	public void setEntryMode(String entryMode) {
		this.entryMode = entryMode;
	}
	public String getReturnMesgType() {
		return returnMesgType;
	}
	public void setReturnMesgType(String returnMesgType) {
		this.returnMesgType = returnMesgType;
	}
	public String getTxIndicator() {
		return txIndicator;
	}
	public void setTxIndicator(String txIndicator) {
		this.txIndicator = txIndicator;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getProcessCode() {
		return processCode;
	}
	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}
	public String getNtAmt() {
		return ntAmt;
	}
	public void setNtAmt(String ntAmt) {
		this.ntAmt = ntAmt;
	}
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public String getTraceNo() {
		return traceNo;
	}
	public void setTraceNo(String traceNo) {
		this.traceNo = traceNo;
	}
	public String getCondCode() {
		return condCode;
	}
	public void setCondCode(String condCode) {
		this.condCode = condCode;
	}
	public String getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getAuthIdResp() {
		return authIdResp;
	}
	public void setAuthIdResp(String authIdResp) {
		this.authIdResp = authIdResp;
	}
	public String getRespCode() {
		return respCode;
	}
	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
	public String getAccpTermId() {
		return accpTermId;
	}
	public void setAccpTermId(String accpTermId) {
		this.accpTermId = accpTermId;
	}
	public InstallmentTxData() {
		// TODO Auto-generated constructor stub
	}

	public String returnMesgType;
	public String txIndicator;
	public String cardNo;
	public String processCode;//isoField[3]
	public String ntAmt;//isoField[4]
	public String dateTime; //isoField[7]
	public String traceNo; //isoField[11]
	public String entryMode; //isoField[22]
	public String condCode; //isoField[25]
	public String expireDate;//G_Gate.expireDate
	public String refNo;	//isoField[37]
	public String authIdResp; //isoField[38]
	public String respCode; //isoField[39]
	public String accpTermId;//isoField[41]
	public String mchtNo;//merchantNo
	public String authRemark;//ECS處理結果放入授權留言
	public String prodNo;//分期數 == TokenC5.divNum
	public String contractNo;
	public String totalTerm;//分期數 == TokenC5.divNum
	public String value1;
	public String value2;
	public String value3;
}
