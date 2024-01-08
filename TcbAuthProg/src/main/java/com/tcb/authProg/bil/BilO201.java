/**
 * 分期付款 Sales 處理程式
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
 * 2017/07/01  V1.00.00  Edson       分期付款 Sales 處理程式                       *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 * 2023/11/24  V1.00.59  Kevin       修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能 *
 ******************************************************************************
 */

package com.tcb.authProg.bil;


import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.process.AuthTxnGate;

/*分期付款 Sales 處理程式*/
public class BilO201 extends BilOBase {
 


// **************************************************************************
 public BilO201(AuthTxnGate pGate, AuthGlobalParm pGlobalParm, TableAccess pTableAccess, String sPTxIndicator) throws Exception {
	 progname = "分期付款 Sales 處理程式  106/07/01 V1.00.00";
	 prgmId = "BilO201";
	 gGate = pGate;
	 gGlobalParm = pGlobalParm;
	 gTableAccess = pTableAccess;
	 sGTxIndicator = sPTxIndicator;

	 moveRtn(pGate);
  hRespCd = "00";

  initBilInstallLog();
  hInlgTxDate = HpeUtil.getCurDateStr(false);

  if(hRespCd.equals("00")) {
   getCrdCardInfo();
  }

  if(hRespCd.equals("00")) {
   gTableAccess.selectBilProd(this);
  }

  if(hRespCd.equals("00")) {
   gTableAccess.updateBilMerchant(this);
  }

  gTableAccess.insertBilInstallLog(this, true);

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
 
 /*
 public String bil_o101_old(String rcvbuf) throws Exception
 {
  // ====================================
  // 固定要做的
  dateTime();
  setConsoleMode("Y");
  javaProgram = this.getClass().getName();
  showLogMessage("I", "", javaProgram + " " + PROGNAME);
  // =====================================

  // 固定要做的
  if(!connectDataBase()) {
   showLogMessage("I","", "connect DataBase error");
   return buffer;
  }

  temp_body = rcvbuf;

  move_rtn();
  h_resp_cd = "00";

  init_bil_install_log();

  try {
   sqlCmd = "select to_char(sysdate,'yyyymmdd') h_inlg_tx_date ";
   sqlCmd += " from dual ";
   int recordCnt = selectTable();
   if(recordCnt > 0) {
    h_inlg_tx_date = getValue("h_inlg_tx_date");
   } else {
    showLogMessage("I","", "**** select dual not found");
    h_resp_cd = "20";
   }
  } catch(Exception ex) {
   showLogMessage("I","", String.format("**** select dual error= [%s]", ex.getMessage()));
   h_resp_cd = "20";
  }

  if(h_resp_cd.equals("00")) {
   select_crd_card();
  }

  if(h_resp_cd.equals("00")) {
   select_bil_prod();
  }

  if(h_resp_cd.equals("00")) {
   update_bil_merchant();
  }

  insert_bil_install_log();

  h_temp_resp_cd = "";
  try {
   sqlCmd = "select iso_code ";
   sqlCmd += " from bil_txn_code  ";
   sqlCmd += "where txn_kind = '2'  ";
   sqlCmd += "and resp_flag = ? ";
   setString(1, h_resp_cd);
   int recordCnt = selectTable();
   if(recordCnt > 0) {
    h_temp_resp_cd = getValue("iso_code");
    h_resp_cd = h_temp_resp_cd;
   } 
   else {
    showLogMessage("I","", "**** select bil_txn_code not found");
    h_resp_cd = "D3";
   }
  } catch(Exception ex) {
   showLogMessage("I","", String.format("**** select bil_txn_code error= [%s]", ex.getMessage()));
   h_resp_cd = "D3";
  }

  gen_buf_old();

  commitDataBase();
  closeConnect();
  
  return buffer;
 }
*/
 /***********************************************************************/
 /***********************************************************************/
 /***********************************************************************/
 /***********************************************************************/
 /**********************************************************************/
 /***********************************************************************/
 private void genReturnData() throws Exception {

  String tempX12 = "";
  String tempX02 = "";
  String tempX08 = "";
  String tempX06 = "";
  int int1 = 0;

  gInstallTxData = new InstallmentTxData();
  gInstallTxData.setReturnMesgType("0210");
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
  //kevin:新增G_InstallTxData.setAuthRemark(h_temp_resp_cd)
  gInstallTxData.setAuthRemark(hTempRespCd);
  
  gInstallTxData.setProdNo(hContProductNo);

  gInstallTxData.setContractNo(hContContractNo);

  //V1.00.59 修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能
  tempX02 = String.format("%02d", hContInstallTotTerm);
  gInstallTxData.setTotalTerm(tempX02); //分期數
  
  tempX08 = String.format("%08d", (int) Math.round(hContFirstRemdAmt + hContUnitPrice));
  gInstallTxData.setValue1(tempX08); //首期金額

  tempX08 = String.format("%08d", (int) Math.round(hContUnitPrice));
  gInstallTxData.setValue2(tempX08); //每期金額

  tempX06= String.format("%06d", (int) Math.round(hContCltFeesAmt + totInterestAmt));
  gInstallTxData.setValue3(tempX06);//手續費

  return;
 }
 
 /*
 private void gen_buf_old() throws Exception {

	  String temp_x12 = "";
	  String temp_x02 = "";
	  int int1 = 0;

	  buffer = "0210";
	  curr_pt = 4;

	  buffer = comc.cmpStr(buffer, curr_pt, h_tx_indicator,
	    h_tx_indicator.length());
	  curr_pt = curr_pt + 8;

	  buffer = comc.cmpStr(buffer, curr_pt, h_card_no, h_card_no.length());
	  curr_pt = curr_pt + h_card_no.length();

	  for (int1 = 0; int1 < (19 - h_card_no.length()); int1++) {
	   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
	   curr_pt = curr_pt + 1;
	  }

	  buffer = comc.cmpStr(buffer, curr_pt, h_process_cd, 6);
	  curr_pt = curr_pt + 6;

	  buffer = comc.cmpStr(buffer, curr_pt, h_amt_x, 12);
	  curr_pt = curr_pt + 12;

	  buffer = comc.cmpStr(buffer, curr_pt, h_date_time, 10);
	  curr_pt = curr_pt + 10;

	  buffer = comc.cmpStr(buffer, curr_pt, h_trace_no, 6);
	  curr_pt = curr_pt + 6;

	  buffer = comc.cmpStr(buffer, curr_pt, h_expire_date, 4);
	  curr_pt = curr_pt + 4;

	  buffer = comc.cmpStr(buffer, curr_pt, h_entry_mode, 3);
	  curr_pt = curr_pt + 3;

	  buffer = comc.cmpStr(buffer, curr_pt, h_con_code, 2);
	  curr_pt = curr_pt + 2;

	  buffer = comc.cmpStr(buffer, curr_pt, h_reference_no, 12);
	  curr_pt = curr_pt + 12;

	  buffer = comc.cmpStr(buffer, curr_pt, h_auth_id_resp, 6);
	  curr_pt = curr_pt + 6;

	  buffer = comc.cmpStr(buffer, curr_pt, h_resp_cd, 2);
	  curr_pt = curr_pt + 2;

	  buffer = comc.cmpStr(buffer, curr_pt, h_accp_term_id, 16);
	  curr_pt = curr_pt + h_accp_term_id.length();

	  for (int1 = 0; int1 < (16 - h_accp_term_id.length()); int1++) {
	   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
	   curr_pt = curr_pt + 1;
	  }

	  buffer = comc.cmpStr(buffer, curr_pt, h_accp_id_cd, h_accp_id_cd.length());
	  curr_pt = curr_pt + h_accp_id_cd.length();

	  for (int1 = 0; int1 < (15 - h_accp_id_cd.length()); int1++) {
	   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
	   curr_pt = curr_pt + 1;
	  }

	  buffer = comc.cmpStr(buffer, curr_pt, "061", 3);
	  curr_pt = curr_pt + 3;

	  buffer = comc.cmpStr(buffer, curr_pt, h_cont_product_no, 8);
	  curr_pt = curr_pt + h_cont_product_no.length();
	  for (int1 = 0; int1 < (8 - h_cont_product_no.length()); int1++) {
	   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
	   curr_pt = curr_pt + 1;
	  }

	  buffer = comc.cmpStr(buffer, curr_pt, h_cont_contract_no, 12);
	  curr_pt = curr_pt + h_cont_contract_no.length();
	  for (int1 = 0; int1 < (12 - h_cont_contract_no.length()); int1++) {
	   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
	   curr_pt = curr_pt + 1;
	  }

	  temp_x02 = String.format("%02d", h_cont_install_tot_term);
	  buffer = comc.cmpStr(buffer, curr_pt, temp_x02, 2);
	  curr_pt = curr_pt + 2;

	  temp_x12 = String.format("%010.0f00", h_cont_first_remd_amt
	    + h_cont_unit_price);
	  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
	  curr_pt = curr_pt + 12;

	  temp_x12 = String.format("%010.0f00", h_cont_unit_price);
	  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
	  curr_pt = curr_pt + 12;

	  temp_x12 = String.format("%010.0f00", h_cont_clt_fees_amt
	    + tot_interest_amt);
	  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
	  curr_pt = curr_pt + 12;


	  return;
	 }
*/
// **************************************************************************
}
