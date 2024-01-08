/**
 * 收單-分期/紅利 BASE處理程式
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
 * 2017/07/01  V1.00.00  Edson       收單-分期/紅利 BASE處理程式                   *
 * 2021/02/08  V1.00.01  shiyuqi     updated for project coding standard      *
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.bil;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;


public class BilOBase {

	public BilOBase() {
		// TODO Auto-generated constructor stub
	}
	 AuthTxnGate gGate = null;
	 AuthGlobalParm gGlobalParm=null;

	 TableAccess gTableAccess = null;
	 public InstallmentTxData gInstallTxData = null;
	 public RedeemTxData gRedeemTxData=null;
	 /*
	  int h_852s_sort_type = 0;
 int h_cont_contract_seq_no = 0;
 
 
 double temp_double = 0;
 long temp_long = 0;
 
	  * */
	 public String prgmId="";
	 public String progname="";
	 public int hSeqNo = 0;
	 public int tempInt=0;
	 public String hContProductName = "";
	 public long tempLong=0;
	 public long longAmt=0;
	 public double doubleAmt=0;
	 public double tempDouble=0;
	 public int hLimitMin = 0;
	 //public int h_cont_tot_amt = 0;
	 public double hContTotAmt = 0;
	 public int hProdCltFeesFixAmt = 0;
	 public int hProdCltInterestRate = 0;
	 public int hProdTransRate = 0;
	 public String hContRealCardNo="";
	 public String hInlgRealCardNo="";
	 public int hContContractSeqNo = 0;
	 public String hCardGroupCode="";
	 public String hCardCardType="";
	 public String hCardAcctType="";
	 public String hInlgTxDate = "";
	 public String hTempRespCd = "";
	 public String hRespCd = "";
	 public String hInlgRowid = "";
	 public String hTempConfirmFlag = "";
	 public String hTempX12 = "";
	 public String hInlgInstallResp632 = "";
	 public int hInlgInstallResp633 = 0;
	 public double hInlgInstallResp634 = 0;
	 public double hInlgInstallResp635 = 0;
	 public double hInlgInstallResp636 = 0;
	 public String hInlgReversalFlag = "";
	 public String hAuthIdResp = "";
	 public String hCardNo = "";
	 public String hAccpIdCd = "";
	 public String hInlgRefundFlag = "";
	 public String hInlgPSeqno = "";
	 public String hInlgAcctType = "";
	 public String hInlgCardNo = "";
	 public String hInlgTxIndicator = "";
	 public String hInlgMsgType = "";
	 public String hInlgProcessCode = "";
	 public String hInlgAmtTx4 = "";
	 public String hInlgDateTime7 = "";
	 public String hInlgTraceNo11 = "";
	 public String hInlgExpireDate14 = "";
	 public String hInlgPosEntryMode22 = "";
	 public String hInlgPosConCode25 = "";
	 public String hInlgReferenceNo37 = "";
	 public String hInlgAuthIdResp38 = "";
	 public String hInlgRespFlag39 = "";
	 public String hInlgTermId41 = "";
	 public String hInlgMchtId42 = "";
	 public String hInlgInstallData63 = "";
	 public String hInlgInstallResp631 = "";
	 public int hInlgPointsRedeem = 0;
	 public double hInlgPointsBalance = 0;
	 public String hInlgConfirmFlag = "";
	 public String hInlgConfirmDate = "";

	 public String hProdCd = "";
	 public String hOrderNo = "";
	 public String tempBody = "";
	 public String hMsgType = "";
	 public String hTxIndicator = "";
	 public String tempX100 = "";
	 public String hProcessCd = "";
	 public String hAmtx = "";
	 public String hDateTime = "";
	 public String hTraceNo = "";
	 public String hExpireDate = "";
	 public String hEntryMode = "";
	 public String hConCode = "";
	 public String hReferenceNo = "";
	 public String hAccpTermId = "";
	 public String hContMchtNo = "";
	 public String hContProductNo = "";
	 public String hContContractNo = "";
	 public double hContCltUnitPrice = 0;
	 public double hAmt = 0;
	 
	 public int hContInstallTotTerm = 0;
	 public double hContFirstRemdAmt = 0;
	 public double hContUnitPrice = 0;
	  
	 public double hContCltFeesAmt = 0;
	 public double totInterestAmt = 0;
	 public double hContRemdAmt = 0;

	 public String hCardSupFlag = "";
	 public String hCardCurrentCode = "";
	 public String hCardNewEndDate = "";
	 public String hCardOldEndDate = "";
	 public String hCardMajorCardNo = "";
	 
	 public String sGTxIndicator="";

	 //down, for BilO1~
	
	 public String hBpjrAcctDate = "";
	
	 public String hBpcdPSeqno = "";
	 public double hBpcdUseBp = 0;
	 public double hBpcdNetTtlBp = 0;
	 public double hBpcdNetTtlTax1 = 0;
	 public double hBpcdNetTtlTax2 = 0;
	 public double hBpcdNetTtlTax3 = 0;
	 public double hBpcdNetTtlTax4 = 0;
	 public double hBpcdNetTtlTax5 = 0;
	 public double hBpcdNetTtlNotax1 = 0;
	 public double hBpcdNetTtlNotax2 = 0;
	 public double hBpcdNetTtlNotax3 = 0;
	 public double hBpcdNetTtlNotax4 = 0;
	 public double hBpcdNetTtlNotax5 = 0;
	 public double hBpcdTransBp = 0;
	 public double hBpcdTransOutBp = 0;
	
	 public String hCardPSeqno = "";
	 public String hCardIdPSeqno = "";
	 public String hBusiBusinessDate = "";
	 public String hTranCode = "";
	
	 public String hBpcdRowid = "";
	 public String hRedeRdmBinflag = "";
	 public double hRedeRdmSeqno = 0;
	 public double hRedeRdmDestamt = 0;
	 public double hRedeRdmDiscrate = 0;
	 public double hRedeRdmDiscamt = 0;
	 public double hRedeRdmUnitpoint = 0;
	 public double hRedeRdmUnitamt = 0;
	
	 public int hPointRede = 0;
	 public String hBpjrPSeqno = "";
	 public String hBpjrAcctType = "";
	 public String hBpjrTypeCode = "";
	 public String hBpjrCardNo = "";
	 public String hBpjrTransCode = "";
	 public String hBpjrGiftNo = "";
	 public String hBpjrGiftName = "";
	 public String hBpjrAdjustReason="";
	 public int hBpjrGiftCnt = 0;
	 public String hBpjrReasonCode = "";
	
	 public String hInlgMerchantId42 = "";
	
	 public double hInlgPointsAmt = 0;
	

	 public String buffer = "";
	

	
	 public int currPt = 0;
	 public int bitmapLength = 0;
	 public int pnt = 0;
	 public double hPaidAmt=0;
	 public double hBpcdPreSubbp = 0;
	 public int hBpjrNetBp = 0;
	 public int hBpjrTransBp = 0;
	 public double hOldNetTtlBp = 0;
	 public double hBpjrTransBpTax = 0;
	 public double hBpjrNetTtlTaxBef = 0;
	 public double hBpjrGiftCashValue = 0;
	 public double hBpjrGiftPayCash = 0;
	 public double hOldNetTtlTaxAll = 0;
	 public double hBpjrNetTtlNotaxBef = 0;
	 public double hOldNetTtlNotaxAll = 0;

	  //private double temp_rate_amt = 0;
	 public double tempMini = 0;
	 public double hBpcdNetTtl1 = 0;
	 public double hBpcdNetTtl2 = 0;
	 public double hBpcdNetTtl3 = 0;
	 public double hBpcdNetTtl4 = 0;
	 public double hBpcdNetTtl5 = 0;
	  // up, for BilO1~
	 public void getCrdCardInfo() throws Exception {
		  hContContractSeqNo = 1;
		   
		  hContRealCardNo = hCardNo;
		  hInlgRealCardNo = hCardNo;
		  hContMchtNo = hAccpIdCd;
		  hContProductNo = hProdCd;
		  hCardGroupCode = "";
		  hCardCardType = "";

		  
		 hInlgPSeqno = gTableAccess.getValue("p_seqno");
		 hInlgAcctType = gTableAccess.getValue("acct_type");
		 
		 hCardGroupCode = gTableAccess.getValue("group_code");
		 hCardCardType= gTableAccess.getValue("card_type");
		 hCardAcctType = hInlgAcctType;
		 
		 
		 //down, for BilO1~
		 hCardSupFlag = gTableAccess.getValue("sup_flag");
		 hCardCurrentCode = gTableAccess.getValue("current_code");
		 hCardNewEndDate = gTableAccess.getValue("NEW_END_DATE");
		 hCardOldEndDate = gTableAccess.getValue("OLD_END_DATE");
		 hCardPSeqno     = gTableAccess.getValue("CardBaseAcnoPSeqNo");
		 hCardIdPSeqno   = gTableAccess.getValue("CardBaseIdPSeqNo");
		 hCardMajorCardNo = gTableAccess.getValue("MAJOR_CARD_NO");
		 //up,for BilO1~
	  /*
	  h_inlg_card_no = h_card_no;
	  h_cont_mcht_no = h_accp_id_cd;
	  h_cont_product_no = h_prod_cd;

	  try {
	   sqlCmd = "select p_seqno,";
	   sqlCmd += "acct_type ";
	   sqlCmd += " from crd_card  ";
	   sqlCmd += "where card_no  = ? ";
	   setString(1, h_inlg_card_no);
	   int recordCnt = selectTable();
	   if (recordCnt > 0) {
	    h_inlg_p_seqno = getValue("p_seqno");
	    h_inlg_acct_type = getValue("acct_type");
	   } else {
	    showLogMessage("I","", "select_crd_card not found");
	    h_resp_cd = "14";
	    return;
	   }
	  } catch(Exception ex) {
	   showLogMessage("I","", String.format("select_crd_card error= [%s]", ex.getMessage()));
	   h_resp_cd = "14";
	   return;
	  }
	  */
	 }
	 public void selectCrdCard() throws Exception {
		  /* sup_flag = '0', 正卡  */
		  hCardNewEndDate = "";
		  hCardSupFlag = "";
		  hCardOldEndDate = "";
		  getCrdCardInfo();
		  
		  if (!hCardSupFlag.equals("0")) {
			  gGlobalParm.showLogMessage("I", "select_crd_card 4 error");
			  hRespCd = "R4";
			  return;
		  }
		  
		  gGlobalParm.showLogMessage("I",  String.format("888 exp=[%s],[%s]\n", hExpireDate, hCardNewEndDate));
		  
		  if (hExpireDate.length() > 0  && (!hExpireDate.equals(hCardNewEndDate.substring(2,6)))) {
			  if ((hCardOldEndDate.length() == 0) || (hCardOldEndDate.length() != 0 && (!hExpireDate.equals(hCardOldEndDate.substring(2,6))))) {
				  gGlobalParm.showLogMessage("I", "select_crd_card 5 error");
				  hRespCd = "R5";
				  return;
			  }
		  }
		  if (!hCardCurrentCode.equals("0")) {
			  gGlobalParm.showLogMessage("I", "select_crd_card 6 error");
			  hRespCd = "R6";
			  return;
		  }
		 }

	 public void moveRtn(AuthTxnGate pGate) throws Exception {
		  hMsgType = pGate.mesgType;
		  hTxIndicator = sGTxIndicator;
		  hCardNo = pGate.cardNo;

		  hProcessCd = pGate.isoField[3];


		  hAmtx = pGate.ntAmt+ "00";
		  hAmt = pGate.ntAmt;
		  hDateTime = pGate.isoField[7];
		  
		  hTraceNo = pGate.isoField[11];
		  
		  hExpireDate = pGate.expireDate;
		  
		  hEntryMode  = pGate.entryMode;
		  hConCode = pGate.isoField[25];
		  hReferenceNo = pGate.isoField[37];
		  
		  hAuthIdResp = pGate.isoField[38];
		  
		  hRespCd = pGate.isoField[39];
		  hAccpTermId = pGate.isoField[41];
		  
		  hAccpIdCd = pGate.merchantNo;
		  
		  hProdCd = pGate.divNum;//分期數 => 由 token C5 中取得

	 }

	 public void initBilInstallLog() {
		  hInlgTxDate = "";
		  hInlgMsgType = "";
		  hInlgTxIndicator = "";
		  hInlgCardNo = "";
		  hInlgProcessCode = "";
		  hInlgAmtTx4 = "";
		  hInlgDateTime7 = "";
		  hInlgTraceNo11 = "";
		  hInlgExpireDate14 = "";
		  hInlgPosEntryMode22 = "";
		  hInlgPosConCode25 = "";
		  hInlgReferenceNo37 = "";
		  hInlgAuthIdResp38 = "";
		  hInlgRespFlag39 = "";
		  hInlgTermId41 = "";
		  hInlgMchtId42 = "";
		  hInlgInstallData63 = "";
		  hInlgInstallResp631 = "";
		  hInlgInstallResp632 = "";
		  hInlgInstallResp633 = 0;
		  hInlgInstallResp634 = 0;
		  hInlgInstallResp635 = 0;
		  hInlgInstallResp636 = 0;
		  hInlgAcctType = "";
		  hInlgPSeqno = "";
		  hInlgConfirmFlag = "";
		  hInlgConfirmDate = "";
		 }

	 public void insertCycBpjr(String sPAdjreason) throws Exception {

		  try {
			  gTableAccess.preInsertCycBpjr(this,sPAdjreason);
		   
		  } catch(Exception ex) {
			  gGlobalParm.showLogMessage("I", String.format("insert_cyc_bpjr error= [%s]", ex.getMessage()));
			  hRespCd = "23";
		  }
		 }

	 public void getCycBpcd() throws Exception 
	 {
	     hBpcdUseBp=0;
	     hBpcdNetTtlBp=0;
	     hBpcdNetTtlTax1=0;
	     hBpcdNetTtlTax2=0;
	     hBpcdNetTtlTax3=0;
	     hBpcdNetTtlTax4=0;
	     hBpcdNetTtlTax5=0;
	     hBpcdNetTtlNotax1=0;
	     hBpcdNetTtlNotax2=0;
	     hBpcdNetTtlNotax3=0;
	     hBpcdNetTtlNotax4=0;
	     hBpcdNetTtlNotax5=0;
	     hBpcdPreSubbp=0;
	     hBpcdTransBp=0;
	     hBpcdTransOutBp=0;
	     hCardGroupCode     = "";
	     hCardCardType      = "";
	   
	     if (gTableAccess.selectMktBonusDtl(this)) {
	    	 hOldNetTtlBp = hBpcdNetTtlBp;    	 
	     }
	   


	 }

	 public void getPtrRedeem() throws Exception 
	 {
	  int swCommon = 0;
	  int tempInt = 0;
	  long tempRateAmt = 0;
	  long tempAllPoint = 0;
	  long inputAllPoint = 0;

	  hRedeRdmSeqno = 0;
	  hRedeRdmDestamt = 0;
	  hRedeRdmDiscrate = 0;
	  hRedeRdmDiscamt = 0;
	  hRedeRdmUnitpoint = 0;
	  hRedeRdmUnitamt = 0;
	  hRedeRdmBinflag = "";

	  swCommon = 0;
	  swCommon = gTableAccess.selectPtrRedeem(this);
//	  try{
//	  } catch (Exception ex) {
//	   swCommon = 1;
//	  }

	  if (swCommon == 0) {
		  if (hRedeRdmBinflag.substring(0, 1).equals("Y")) {
//			  try {
			  tempInt = gTableAccess.selectPtrRedeemDtl1(this, "ACCT-TYPE", hCardAcctType);
//			  } catch (Exception ex) { 
//				  
//			  }
			  if (tempInt < 1) {
				  swCommon = 1;
			  }
		  }
		  
		  if (hRedeRdmBinflag.substring(1).equals("Y") && swCommon == 1) {
			  swCommon = 0;
			  try {
				  tempInt = gTableAccess.selectPtrRedeemDtl1(this, "GROUP-CODE", hCardGroupCode);
			  } catch (Exception ex) { 
				  
			  }
			  if (tempInt < 1) {
				  swCommon = 1;
			  }
	   }
	   if (hRedeRdmBinflag.substring(2).equals("Y") && swCommon == 1) {
		   swCommon = 0;
		   try {
			   tempInt = gTableAccess.selectPtrRedeemDtl1(this, "CARD-TYPE", hCardCardType);
		   } catch (Exception ex) { 
			   
		   }
		   if (tempInt < 1) {
			   swCommon = 1;
		   }
	   }
	  }

	  if (swCommon == 1) {
		  try {
			  if (gTableAccess.selectBilRedeem(this)) {
				  hRedeRdmDiscrate = gTableAccess.getDouble("disc_rate");
				  hRedeRdmDestamt = gTableAccess.getDouble("dest_amt");
				  hRedeRdmDiscamt = gTableAccess.getDouble("disc_amt");
				  hRedeRdmUnitpoint = gTableAccess.getDouble("unit_point");
				  hRedeRdmUnitamt = gTableAccess.getDouble("unit_amt");
			  }
			  else {
				  gGlobalParm.showLogMessage("I", "select bil_redeem not found");
				  hRespCd = "25";
			  }

		  } catch (Exception ex) {
			  gGlobalParm.showLogMessage("I", String.format("select bil_redeem error= [%s]", ex.getMessage()));
			  hRespCd = "25";
		  }
	  }
	  if (hAmt < hRedeRdmDestamt) {
		  hRespCd = "26";
	  }
	  tempInt = (int) (hAmt / hRedeRdmUnitamt);
	  inputAllPoint = (long) (tempInt * hRedeRdmUnitpoint);

	  tempRateAmt = (long) (hAmt * hRedeRdmDiscrate / 100);
	  tempMini = tempRateAmt;
	  if (tempRateAmt > hRedeRdmDiscamt) {
	   tempMini = hRedeRdmDiscamt;
	  }

	  tempInt = (int) (tempMini / hRedeRdmUnitamt);
	  tempAllPoint = (long) (tempInt * hRedeRdmUnitpoint);

	  if (tempAllPoint > inputAllPoint)
	   tempAllPoint = inputAllPoint;

	  if (tempAllPoint > hBpcdNetTtlBp) {
	   tempAllPoint = (long) hBpcdNetTtlBp;
	   tempInt = (int) (tempAllPoint / hRedeRdmUnitpoint);
	   hInlgPointsAmt = tempInt * hRedeRdmUnitamt;
	   hPointRede = (int) (tempInt * hRedeRdmUnitpoint);
	  } else {
	   tempInt = (int) (tempMini / hRedeRdmUnitamt);
	   hInlgPointsAmt = tempInt * hRedeRdmUnitamt;
	   hPointRede = (int) (tempInt * hRedeRdmUnitpoint);
	  }

	 }

}
