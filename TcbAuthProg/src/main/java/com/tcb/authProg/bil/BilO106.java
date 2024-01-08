/**
 * 收單-紅利 Refund 處理
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
 * 2017/07/01  V1.00.00  Edson       收單-紅利 Refund 處理                       *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.bil;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.process.AuthTxnGate;


/*收單-紅利 Refund處理*/
public class BilO106 extends BilOBase {

 
 public BilO106(AuthTxnGate pGate, AuthGlobalParm pGlobalParm, TableAccess pTableAccess, String sPTxIndicator) throws Exception{

	 progname = "收單-紅利 Refund處理 106/07/01 V1.00.00";
	 
	 prgmId = "BilO106";

  /*
  00     正常回覆
  R1     主機忙線中
  R2     點數不足
  R3     卡號錯誤(卡號不存在)
  R4     該卡號非持卡人之正卡
  R5     卡號效期錯誤
  R6     該卡號己被停卡
  R7     無紅利主檔(cyc_pbcd不存在)
  */
  
  // ====================================
	 gGate = pGate;
	 gGlobalParm = pGlobalParm;
	 gTableAccess = pTableAccess;
	 sGTxIndicator = sPTxIndicator;
  
	 moveRtn(pGate);

	 hRespCd = "00";

	 initBilInstallLog();
	 hInlgTxDate = HpeUtil.getCurDateStr(false);
	 if (hRespCd.equals("00")) {
		 selectCrdCard();
	 }

	 if (hRespCd.equals("00")) {
		 getCycBpcd();

	 }

	 if (hRespCd.equals("00")) {
		 
		 gTableAccess.selectBilInstallLog4Redeem(this);
   
	 }

	 if (hRespCd.equals("00")) {
		 updateCycBpcd();
		 if (hRespCd.equals("00")) {
			 hBpjrAcctDate = HpeUtil.getCurDateStr(false);

			 hBpjrTransBpTax = 0;
			 hBpjrNetTtlTaxBef = 0;
			 hBpjrPSeqno = hBpcdPSeqno;
			 hBpjrAcctType = hCardAcctType;
			 hBpjrPSeqno = hCardPSeqno;
			 hBpjrGiftName = prgmId;
			 hBpjrCardNo = hCardNo;
			 hBpjrTypeCode = "BONU";
			 hBpjrTransCode = "ADJ";
			 hBpjrReasonCode = "12";
			 hBpjrGiftNo = "";
			 hBpjrGiftName = "";
			 hBpjrGiftCnt = 0;
			 hBpjrGiftCashValue = 0;
			 hBpjrGiftPayCash = 0;
			 if (hOldNetTtlNotaxAll > hPointRede) {
				 hBpjrTransBp = (-1) * hPointRede;
				 hBpjrNetTtlNotaxBef = hOldNetTtlNotaxAll;
				 hBpjrNetBp = (int)hBpcdNetTtlBp - hPointRede;
				 hBpjrTransBpTax = 0;
				 hBpjrNetTtlTaxBef = hOldNetTtlTaxAll;
				 gTableAccess.insertCycBpjr(this);
				 //	insert_cyc_bpjr();
			 } else {
				 hBpjrTransBp = (int) ((-1) * hOldNetTtlNotaxAll);
				 hBpjrNetTtlNotaxBef = hOldNetTtlNotaxAll;
				 hBpjrTransBpTax = (-1)
						 * (hPointRede - hOldNetTtlNotaxAll);
				 hBpjrNetTtlTaxBef = hOldNetTtlTaxAll;
				 hBpjrNetBp = (int)hBpcdNetTtlBp - hPointRede;
				 gTableAccess.insertCycBpjr(this);
				 //		insert_cyc_bpjr();
			 }
		 }
	 }

  if (hRespCd.equals("00")) {
	  gTableAccess.updateBilInstallLog5(this);
	  //down, Howard: 為何還要 update 呢? 與 update_bil_install_log5() 為何不一起做?? 還不知道答案,暫時 marked
	  /*
	  try {
		  daoTable = "bil_install_log";
		  updateSQL = "reversal_flag = '',";
		  updateSQL += " refund_flag = '' ";
		  whereStr = "where auth_id_resp_38   = ?  ";
		  whereStr += "and mcht_id_42   = ?  ";
		  whereStr += "and card_no    = ?  ";
		  whereStr += "and mod_pgm     = 'BilO101' ";
		  setString(1, h_auth_id_resp);
		  setString(2, h_accp_id_cd);
		  setString(3, h_card_no);
		  updateTable();
		  if (notFound.equals("Y")) {
			  showLogMessage("I","", "**** update bil_install_log 2 not found");
			  h_resp_cd = "13";
		  }
	  } catch(Exception ex) {
		  showLogMessage("I","", String.format("**** update bil_install_log 2 error= [%s]", ex.getMessage()));
		  h_resp_cd = "13";
	  }
	  */
  }
  //Up, Howard: 為何還要 update 呢? 與 update_bil_install_log5() 為何不一起做?? 還不知道答案,暫時 marked

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

 /***********************************************************************/
 /***********************************************************************/
 /***********************************************************************/
  /***********************************************************************/
 /***********************************************************************/

 /***********************************************************************/
 private void updateCycBpcd() throws Exception {
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

  try {
	  gTableAccess.updateCycBpcd4Redeem(this);
  } catch (Exception ex) {
	  gGlobalParm.showLogMessage("E", String.format("update_cyc_bpcd error= [%s]", ex.getMessage()));
	  hRespCd = "22";
  }

 }

 /***********************************************************************/
 private int calBonusNet(double pTransBp) {
  int i;
  double[] tempNetTtlNotax = new double[10];
  double[] tempNetTtlTax = new double[10];

  if (pTransBp <= 0)
   return (2);
  if (hBpcdNetTtlBp < pTransBp)
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

  hBpcdNetTtlBp = (int) (hBpcdNetTtl1 + hBpcdNetTtl2
    + hBpcdNetTtl3 + hBpcdNetTtl4 + hBpcdNetTtl5);

  return 0;
 }

 /***********************************************************************/
 /***********************************************************************/

 /***********************************************************************/
 private void genReturnData() throws Exception {

  String tempX12 = "";
  String tempX08 = "";
  String tempX38 = "";
  long tempLong = 0;
  int int1 = 0;
  gRedeemTxData = new RedeemTxData();
  gRedeemTxData.setReturnMesgType("0430");

  gRedeemTxData.setTxIndicator(sGTxIndicator);

  gRedeemTxData.setCardNo(hCardNo);

  gRedeemTxData.setProcessCode(hProcessCd);

  gRedeemTxData.setNtAmt(hAmtx);
  gRedeemTxData.setDateTime(hDateTime);

  gRedeemTxData.setTraceNo(hTraceNo);
  
  gRedeemTxData.setExpireDate(hExpireDate);
  gRedeemTxData.setEntryMode(hEntryMode);

  gRedeemTxData.setCondCode(hConCode);

  gRedeemTxData.setRefNo(hReferenceNo);

  gRedeemTxData.setAuthIdResp(hAuthIdResp);

  gRedeemTxData.setRespCode(hRespCd);

  gRedeemTxData.setAccpTermId(hAccpTermId);

  gRedeemTxData.setMchtNo(hAccpIdCd);

  /*
  buffer = comc.cmpStr(buffer, curr_pt, "061", 3); //這似乎可以不用assign
  curr_pt = curr_pt + 3;
  */
  
  gRedeemTxData.setPointRede(Integer.toString(hPointRede));


  tempLong = (long) (hOldNetTtlBp - hPointRede);
  tempX12 = String.format("%012d", tempLong);
  gRedeemTxData.setValue1(tempX12);
  

  return;
 }


}
