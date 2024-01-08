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
 * 2023/02/08  V1.00.37  Kevin       P3紅利兌換處理方式調整                         *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.bil;


import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import com.tcb.authProg.process.AuthTxnGate;

/*收單-紅利 Refund處理*/
public class BilO105 extends BilOBase {
 
 public BilO105(AuthTxnGate pGate, AuthGlobalParm pGlobalParm, TableAccess pTableAccess, String sPTxIndicator) throws Exception{
	 progname = "收單-紅利 Refund處理  106/07/01 V1.00.00";
	 
	 prgmId = "BilO105";
 
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
  hAuthIdResp = gGate.oriAuthNo;
  hRespCd = "00";

  initBilInstallLog();

  hInlgTxDate = HpeUtil.getCurDateStr(false);
  

  if (hRespCd.equals("00")) {
   selectCrdCard();
  }

  if (hRespCd.equals("00")) {
	  gTableAccess.selectBilInstallLog4Redeem(this);
  }

  if (hRespCd.equals("00")) {
	  gTableAccess.selectPtrBusinday(this);
	  hTranCode = "7"; // 7:紅利扣回 4:紅利使用
	  gTableAccess.insertMktBonusDtl(this);
  }

  if (hRespCd.equals("00")) {
	  gTableAccess.updateBilInstallLog5(this);
   
  }
  gTableAccess.insertBilInstallLog4RedeemProc(this, true);
  

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
 /**********************************************************************/

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


  tempLong = (long) (hBpcdNetTtlBp - hPointRede);
  tempX12 = String.format("%012d", tempLong);
  gRedeemTxData.setValue1(tempX12);
  
  
  return;
 }


}
