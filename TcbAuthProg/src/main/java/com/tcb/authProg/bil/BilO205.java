/**
 * 收單-分期付款 Reversal Sales 處理程式
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
 * 2017/07/01  V1.00.00  Edson       收單-分期付款 Reversal Sales 處理程式          *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.bil;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.process.AuthTxnGate;

/*收單-分期付款 Reversal Sales 處理程式*/
public class BilO205 extends BilOBase {

// **************************************************************************
 public BilO205(AuthTxnGate pGate, AuthGlobalParm pGlobalParm, TableAccess pTableAccess, String sPTxIndicator) throws Exception {
	 progname = "收單-分期付款 Reversal Sales 處理程式  106/07/01 V1.00.00";
	 
	 prgmId = "BilO205";

 
  // ====================================
	 gGate = pGate;
	 gGlobalParm = pGlobalParm;
	 gTableAccess = pTableAccess;
	 sGTxIndicator = sPTxIndicator;

  moveRtn(pGate);
  hAuthIdResp = gGate.oriAuthNo;
  hRespCd = "00";

  initBilInstallLog();
  hInlgTxDate = HpeUtil.getCurDateStr(false);

  getCrdCardInfo();

  selectBilInstallLog();
  if (hRespCd.equals("00")) {
	  gTableAccess.updateBilInstallLog3(this);
  }

  gTableAccess.insertBilInstallLog(this, false);

  hTempRespCd = "";
  gTableAccess.getBilTxnCode(this, "2", "D3");
  
  genReturnData();
  return;
  /*

  gen_buf();

  commitDataBase();
  closeConnect();
  
  return buffer;
  */
 }

 /***********************************************************************/

 /***********************************************************************/

 /***********************************************************************/

 /***********************************************************************/
 private void selectBilInstallLog() throws Exception {
	 gTableAccess.getBilInstallLog(this);
	 if (Double.parseDouble(hTempX12) != Double.parseDouble(hAmtx)) {
		   //if (CommCrdRoutine.str2long(h_temp_x12) != CommCrdRoutine.str2long(h_amt_x)) {
			 gGlobalParm.showLogMessage("I", "select_bil_install_log 61 error");
			 hRespCd = "61";
			 return;
		 }

	 if (hInlgReversalFlag.equals("Y")  || hInlgRefundFlag.equals("Y")) {
		 gGlobalParm.showLogMessage("I", "select_bil_install_log 62 error");
		 hRespCd = "62";
		 return;
	 }

	 hOrderNo = hInlgInstallResp632;
	 hContInstallTotTerm = hInlgInstallResp633;
	 hContFirstRemdAmt = hInlgInstallResp634;
	 hContUnitPrice = hInlgInstallResp635;
	 hContCltFeesAmt = hInlgInstallResp636;

  return;

 }

 /***********************************************************************/

 /***********************************************************************/

 /***********************************************************************/
 private void genReturnData() throws Exception {

  String tempX12 = "";
  String tempX02 = "";
  int int1 = 0;

  gInstallTxData = new InstallmentTxData();
  gInstallTxData.setReturnMesgType("0430");
  gInstallTxData.setTxIndicator(sGTxIndicator);

  gInstallTxData.setCardNo(hCardNo);
  gInstallTxData.setProcessCode(hProcessCd);

  gInstallTxData.setNtAmt(hAmtx);

  gInstallTxData.setDateTime(hDateTime);

  gInstallTxData.setTraceNo(hTraceNo);

  gInstallTxData.setExpireDate(hExpireDate);

  gInstallTxData.setEntryMode(hEntryMode);

  gInstallTxData.setCondCode(hConCode);

  gInstallTxData.setRefNo(hReferenceNo);

  gInstallTxData.setAuthIdResp(hAuthIdResp);

  gInstallTxData.setRespCode(hRespCd);

  gInstallTxData.setAccpTermId(hAccpTermId);


  gInstallTxData.setMchtNo(hAccpIdCd);

  /*
  buffer = comc.cmpStr(buffer, curr_pt, "061", 3); //這似乎可以不用assign 
  curr_pt = curr_pt + 3;
  */
  gInstallTxData.setProdNo(hContProductNo);

  gInstallTxData.setContractNo(hContContractNo);


  tempX02 = String.format("%02d", hContInstallTotTerm);
  gInstallTxData.setTotalTerm(tempX02);
  

  tempX12 = String.format("%010.0f00", hContFirstRemdAmt + hContUnitPrice);
  gInstallTxData.setValue1(tempX12); //Howard:應該是首期金額?
  
  tempX12 = String.format("%010.0f00", hContUnitPrice);
  gInstallTxData.setValue2(tempX12); //Howard: 應該是每期金額?
  

  tempX12 = String.format("%010.0f00", hContCltUnitPrice);
  

  gInstallTxData.setValue3(tempX12); //Howard: 這是啥?

  return;
 }

}
