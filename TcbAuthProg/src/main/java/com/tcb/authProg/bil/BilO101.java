/**
 * 收單-紅利 Sales 處理
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
 * 2017/07/01  V1.00.00  Edson       收單-紅利 Sales 處理                        *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.bil;



import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.process.AuthTxnGate;

/*收單-紅利 Sales 處理*/
public class BilO101 extends BilOBase  
{
  
 public BilO101(AuthTxnGate pGate, AuthGlobalParm pGlobalParm, TableAccess pTableAccess, String sPTxIndicator) throws Exception 
 {
	  
	 progname = "收單-紅利 Sales 處理  106/07/01 V1.00.00";


	 prgmId = "BilO101";

	 gGate = pGate;
	 gGlobalParm = pGlobalParm;
	 gTableAccess = pTableAccess;
	 sGTxIndicator = sPTxIndicator;
  moveRtn(pGate);

  hRespCd = "00";

  initBilInstallLog();
  hInlgTxDate = HpeUtil.getCurDateStr(false);
    if(hRespCd.equals("00")) {
     selectCrdCard();
    }

  if(hRespCd.equals("00")) {
	  getCycBpcd();
      //select_cyc_bpcd();
  }

  if(hRespCd.equals("00")) {
	  getPtrRedeem();
  }

  if (hRespCd.equals("00")) {
	  if (hPointRede < 1) {
		  gGlobalParm.showLogMessage("I", String.format("point_rede = 0 point=[%d] ",hPointRede));
		  hRespCd = "R2";
	  }
	  if ((hBpcdNetTtlBp - hPointRede - hBpcdPreSubbp) < 0) {
		  gGlobalParm.showLogMessage("I", String.format("    net =[%f]",hBpcdNetTtlBp));
		  gGlobalParm.showLogMessage("I", String.format("< 0 point=[%f]",hBpcdNetTtlBp - hPointRede - hBpcdPreSubbp));
		  hRespCd = "R2";
	  }

	  if (hRespCd.equals("00")) {
		  gTableAccess.selectPtrBusinday(this);
		  hTranCode = "4"; // 7:紅利扣回 4:紅利使用
		  gTableAccess.insertMktBonusDtl(this);
	  }
  	}
  gTableAccess.insertBilInstallLog4RedeemProc(this, false);
  
  //insert_bil_install_log();

  hTempRespCd = "";
  gTableAccess.getBilTxnCode(this, "1", "57"); //ok
  
  
  genReturnData();
  /*
  gen_buf();

  commitDataBase();
  closeConnect();
  
  return buffer;
  */
  return;
 }

// **************************************************************************
/***********************************************************************/
 
 /***********************************************************************/
/***********************************************************************/
/***********************************************************************/
/***********************************************************************/
 /***********************************************************************/
 private void updateCycBpcd() throws Exception 
 {
  double dTransBp = 0;

  dTransBp = hPointRede;

  if (calBonusNet(dTransBp) != 0) {
   hRespCd = "R2";
   return;
  }

  hBpcdTransBp = hBpcdTransBp - hPointRede;
  if (hBpcdTransBp < 0)
   hBpcdTransBp = 0;

  if (!hRespCd.equals("00"))
   return;

  gTableAccess.updateCycBpcd(this);
  
 }
/***********************************************************************/
 private int calBonusNet(double pTransBp) 
 {
  int i;
  double[] tempNetTtlNotax = new double[10];
  double[] tempNetTtlTax = new double[10];

  if (pTransBp <= 0)
   return (2);
  if ((hBpcdNetTtlBp - hBpcdPreSubbp) < pTransBp)
   return (1);

  tempNetTtlNotax[1] = hBpcdNetTtlNotax1;
  tempNetTtlNotax[2] = hBpcdNetTtlNotax2;
  tempNetTtlNotax[3] = hBpcdNetTtlNotax3;
  tempNetTtlNotax[4] = hBpcdNetTtlNotax4;
  tempNetTtlNotax[5] = hBpcdNetTtlNotax5;

  tempNetTtlTax[1] = hBpcdNetTtlTax1;
  tempNetTtlTax[2] = hBpcdNetTtlTax2;
  tempNetTtlTax[3] = hBpcdNetTtlTax3;
  tempNetTtlTax[4] = hBpcdNetTtlTax4;
  tempNetTtlTax[5] = hBpcdNetTtlTax5;

  for (i = 5; i > 0; i--) {
   if (pTransBp <= 0)
    break;
   if (pTransBp > tempNetTtlTax[i]) {
    pTransBp = pTransBp - tempNetTtlTax[i];
    tempNetTtlTax[i] = 0;
   } else {
    tempNetTtlTax[i] = tempNetTtlTax[i] - pTransBp;
    pTransBp = 0;
   }
   if (pTransBp <= 0)
    break;

   if (pTransBp > tempNetTtlNotax[i]) {
    pTransBp = pTransBp - tempNetTtlNotax[i];
    tempNetTtlNotax[i] = 0;
   } else {
    tempNetTtlNotax[i] = tempNetTtlNotax[i] - pTransBp;
    pTransBp = 0;
   }
  }

  hBpcdNetTtlNotax1 = tempNetTtlNotax[1];
  hBpcdNetTtlNotax2 = tempNetTtlNotax[2];
  hBpcdNetTtlNotax3 = tempNetTtlNotax[3];
  hBpcdNetTtlNotax4 = tempNetTtlNotax[4];
  hBpcdNetTtlNotax5 = tempNetTtlNotax[5];

  hBpcdNetTtlTax1 = tempNetTtlTax[1];
  hBpcdNetTtlTax2 = tempNetTtlTax[2];
  hBpcdNetTtlTax3 = tempNetTtlTax[3];
  hBpcdNetTtlTax4 = tempNetTtlTax[4];
  hBpcdNetTtlTax5 = tempNetTtlTax[5];

  hBpcdNetTtl1 = hBpcdNetTtlNotax1 + hBpcdNetTtlTax1;
  hBpcdNetTtl2 = hBpcdNetTtlNotax2 + hBpcdNetTtlTax2;
  hBpcdNetTtl3 = hBpcdNetTtlNotax3 + hBpcdNetTtlTax3;
  hBpcdNetTtl4 = hBpcdNetTtlNotax4 + hBpcdNetTtlTax4;
  hBpcdNetTtl5 = hBpcdNetTtlNotax5 + hBpcdNetTtlTax5;

  hBpcdNetTtlBp = hBpcdNetTtl1 + hBpcdNetTtl2
    + hBpcdNetTtl3 + hBpcdNetTtl4 + hBpcdNetTtl5;
  return 0;
 }
/**********************************************************************/
/***********************************************************************/
 private void genReturnData() throws Exception 
 {
  String tempX12 = "";
  String tempX26 = "";
  String tempX08 = "";
  String tempx10 = "";
  int int1 = 0;

  gRedeemTxData = new RedeemTxData();
  gRedeemTxData.setReturnMesgType("0210");
  //buffer = "0210";
  //curr_pt = 4;

  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_tx_indicator,
    h_tx_indicator.length());
  curr_pt = curr_pt + 8;
  */
  gRedeemTxData.setTxIndicator(sGTxIndicator);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_card_no, h_card_no.length());
  curr_pt = curr_pt + h_card_no.length();

  for (int1 = 0; int1 < (19 - h_card_no.length()); int1++) {
   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
   curr_pt = curr_pt + 1;
  }
  */
  
  
  gRedeemTxData.setCardNo(hCardNo);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_process_cd, 6);
  curr_pt = curr_pt + 6;
  */
  gRedeemTxData.setProcessCode(hProcessCd);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_amt_x, 12);
  curr_pt = curr_pt + 12;
  */
  gRedeemTxData.setNtAmt(hAmtx);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_date_time, 10);
  curr_pt = curr_pt + 10;
  */
  gRedeemTxData.setDateTime(hDateTime);
  
  gRedeemTxData.setTraceNo(hTraceNo);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_expire_date, 4);
  curr_pt = curr_pt + 4;
  */
  gRedeemTxData.setExpireDate(hExpireDate);
  gRedeemTxData.setEntryMode(hEntryMode);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_entry_mode, 3);
  curr_pt = curr_pt + 3;
  */
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_con_code, 2);
  curr_pt = curr_pt + 2;
  */
  gRedeemTxData.setCondCode(hConCode);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_reference_no, 12);
  curr_pt = curr_pt + 12;
  */
  gRedeemTxData.setRefNo(hReferenceNo);
  
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_auth_id_resp, 6);
  curr_pt = curr_pt + 6;
  */
  gRedeemTxData.setAuthIdResp(hAuthIdResp);
  
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_resp_cd, 2);
  curr_pt = curr_pt + 2;
  */
  gRedeemTxData.setRespCode(hRespCd);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_accp_term_id, 16);
  curr_pt = curr_pt + h_accp_term_id.length();
  
  
  
  for (int1 = 0; int1 < (16 - h_accp_term_id.length()); int1++) {
   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
   curr_pt = curr_pt + 1;
  }
  */
  gRedeemTxData.setAccpTermId(hAccpTermId);
  
  /*
  buffer = comc.cmpStr(buffer, curr_pt, h_accp_id_cd, h_accp_id_cd.length());
  curr_pt = curr_pt + h_accp_id_cd.length();

  for (int1 = 0; int1 < (15 - h_accp_id_cd.length()); int1++) {
   buffer = comc.cmpStr(buffer, curr_pt, " ", 1);
   curr_pt = curr_pt + 1;
  }
  */
  gRedeemTxData.setMchtNo(hAccpIdCd);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, "061", 3); //這似乎可以不用assign
  curr_pt = curr_pt + 3;
  */

  gRedeemTxData.setAuthRemark(hTempRespCd);
  
  /*
  temp_x08 = String.format("%08d", h_point_rede);
  buffer = comc.cmpStr(buffer, curr_pt, temp_x08, 8);
  curr_pt = curr_pt + 8;
  */
  tempx10 = String.format("%010d", hPointRede);
  gRedeemTxData.setPointRede(tempx10);
  
  tempX12 = String.format("%010.0f00", hInlgPointsAmt);
  gRedeemTxData.setPointsAmt(tempX12);

  long tempLong = (long) (hOldNetTtlBp - hPointRede);
  tempx10 = String.format("%010d", tempLong);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
  curr_pt = curr_pt + 12;
  */
  gRedeemTxData.setValue1(tempx10);
  
  tempX12 = String.format("%010.0f00", hAmt - hInlgPointsAmt);
  /*
  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
  curr_pt = curr_pt + 12;
  */
  gRedeemTxData.setValue2(tempX12);
  
  /*
  temp_x26 = String.format("%26.26s", " ");
  buffer = comc.cmpStr(buffer, curr_pt, temp_x26, 12);
  curr_pt = curr_pt + 26;
  */
  return;
 }

 /*
 private void gen_buf() throws Exception 
 {
  String temp_x12 = "";
  String temp_x26 = "";
  String temp_x08 = "";
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

  temp_x08 = String.format("%08d", h_point_rede);
  buffer = comc.cmpStr(buffer, curr_pt, temp_x08, 8);
  curr_pt = curr_pt + 8;

  long temp_long = (long) (h_old_net_ttl_bp - h_point_rede);
  temp_x12 = String.format("%012d", temp_long);
  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
  curr_pt = curr_pt + 12;

  temp_x12 = String.format("%010.0f00", h_amt - h_inlg_points_amt);
  buffer = comc.cmpStr(buffer, curr_pt, temp_x12, 12);
  curr_pt = curr_pt + 12;

  temp_x26 = String.format("%26.26s", " ");
  buffer = comc.cmpStr(buffer, curr_pt, temp_x26, 12);
  curr_pt = curr_pt + 26;

  return;
 }
 */
/**********************************************************************/
}
