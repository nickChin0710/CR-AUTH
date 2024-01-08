/**
 * AuthTxnGate 存放單次交易會用到的所有變數
 * 
 *
 * @author  Howard Chang
 * @version 1.0
 * @since   2017/12/19
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2017/12/19  V1.00.00  Howard      授權邏輯查核-共用邏輯處理                       *
 * 2021/10/21  V1.00.01  Tanwei      updated for project coding standard      *
 * 2021/03/22  V1.00.02  Kevin       經常性身分驗證交易不須檢查到期日recurringTrans    *
 * 2021/11/19  V1.00.03  Kevin       VISA 代碼化交易處理調整                       *
 * 2021/12/24  V1.00.04  Kevin       針對POS ENTRY MODE 91，一律拒絕交易           *
 * 2022/01/13  V1.00.05  Kevin       TCB新簡訊發送規則                            *
 * 2022/03/17  V1.00.06  Kevin       麗花襄理要求2447交易需帶入原始RRN               *
 * 2022/03/30  V1.00.07  Kevin       ECS人工沖正處理與沖正成功檢查原交易是否發生在       *
 *                                   budget date之前，須扣出沖正後金額避免佔額        *
 * 2022/05/04  V1.00.08  Kevin       ATM預借現金密碼變更功能開發                    *
 * 2022/05/26  V1.00.09  Kevin       交易類別28xxxx屬於Payment Transaction(PY)   *
 *                                   一律拒絕交易。                               *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/10/23  V1.00.56  Kevin       避免因特店資料異常時，導致授權系統異常的處理排除        *
 * 2023/11/24  V1.00.59  Kevin       修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能 *
 * 2023/12/11  V1.00.61  Kevin       3D交易欄位格式調整                            *
 * 2024/12/27  V1.00.64  Kevin       MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同                                                                           *
 ******************************************************************************
 */

package com.tcb.authProg.process;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import com.tcb.authProg.util.HpeUtil;

import com.tcb.authProg.iso8583.TagObject;


public class AuthTxnGate {
  	

  public    Connection gDbConn  = null;
  public double curTotalUnpaidOfPersonal=0;
  public double curTotalUnpaidOfComp=0;
  public boolean bgToken06RealLengthIs52 = false;
  public String tokenIdC4="";
  public String tokenC4TxnStatInd="";//TokenC4.TxnStatInd
  public String tokenC4TermAttendInd = "", tokenC4TermLocInd = "", tokenC4ChPresetInd = "", tokenC4CrdPresetInd = "", tokenC4CrdCaptrInd = "";
  public String tokenC4TxnSecInd = "", tokenC4ChActvtInd = "", tokenC4TermInputCap = "", tokenC4TxnRtnInd="";
  public String tokenC4Filter1="",tokenC4Filter2="";

  public String txlogAmtDate=""; //授權累積金額的指定日期(第一階段)
  public double cardAcctTotAmtMonth=0;
  public double cardAcctTotAmtMonthOfComp=0;
  public String cardAcctIdxOfComp="";
  public double ccaConsumePaidAnnualFeeOfComp = 0;
  public double ccaConsumePaidSrvFeeOfComp = 0;
  public double ccaConsumePaidLawFeeOfComp = 0;
      		   
  public double ccaConsumePaidPunishFeeOfComp = 0;
  public double ccaConsumePaidInterestFeeOfComp = 0;
  public double ccaConsumePaidConsumeFeeOfComp = 0;
      		   
      		   
  public double ccaConsumePaidPrecashOfComp = 0;
  public double ccaConsumePaidCyclsOfComp = 0;
      		   
  public double ccaConsumePaidTotUnPayOfComp = 0;
  public double ccaConsumeUnPaidOfComp = 0;
  public double ccaConsumeUnPaidSrvFeeOfComp = 0;
  public double ccaConsumeUnPaidLawFeeOfComp = 0;
      		   
  public double ccaConsumeUnPaidInterestFeeOfComp = 0;
  public double ccaConsumeUnPaidConsumeFeeOfComp = 0;
  public double ccaConsumeUnPaidPrecashOfComp = 0;
  public double ccaConsumeArgueAmtOfComp = 0;
      		   
  public double ccaConsumePrePayAmtOfComp = 0;
  public double ccaConsumeTotUnPaidAmtOfComp = 0;
  public double ccaConsumeBillLowPayAmtOfComp = 0;
      		   
  public double ccaConsumeIbmReceiveAmtOfComp = 0;
  public double ccaConsumeUnPostInstFeeOfComp = 0;
  public double totAmtConsumeOfComp = 0;
  public double totAmtPreCashOfComp = 0;
      		   
  public double ccaConsumeTxTotAmtMonthOfComp = 0;
  public double ccaConsumeTxTotCntMonthOfComp = 0;
  public double ccaConsumeTxTotAmtDayOfComp = 0;
  public double ccaConsumeTxTotCntDayOfComp = 0;
      		   
  public double ccaConsumeFnTotAmtMonthOfComp = 0;
  public double ccaConsumeFnTotCntMonthOfComp = 0;
  public double ccaConsumeFnTotAmtDayOfComp = 0;
  public double ccaConsumeFnTotCntDayOfComp = 0;
      			   		 
  public double ccaConsumeFcTotAmtMonthOfComp = 0;
  public double ccaConsumeFcTotCntMonthOfComp = 0;
  public double ccaConsumeFcTotAmtDayOfComp = 0;
  public double ccaConsumeFcTotCntDayOfComp = 0;
    
  public double ccaConsumeRejAuthCntDayOfComp = 0;
  public double ccaConsumeRejAuthCntMonthOfComp = 0;
  public double ccaConsumeTrainTotAmtMonthOfComp = 0;
  public double ccaConsumeTrainTotAmtDayOfComp = 0;
  
  public double ccaConsumeAuthTxlogAmt1OfComp = 0;
  public double ccaConsumeAuthTxlogAmtCash1OfComp = 0;

  public double ccaConsumePaidAnnualFee = 0;
  public double ccaConsumePaidSrvFee = 0;
  public double ccaConsumePaidLawFee = 0;
      		   
  public double ccaConsumePaidPunishFee = 0;
  public double ccaConsumePaidInterestFee = 0;
  public double ccaConsumePaidConsumeFee = 0;
      		   
      		   
  public double ccaConsumePaidPrecash = 0;
  public double ccaConsumePaidCycls = 0;
      		   
  public double ccaConsumePaidTotUnPay = 0;
  public double ccaConsumeUnPaid = 0;
  public double ccaConsumeUnPaidSrvFee = 0;
  public double ccaConsumeUnPaidLawFee = 0;
      		   
  public double ccaConsumeUnPaidInterestFee = 0;
  public double ccaConsumeUnPaidConsumeFee = 0;
  public double ccaConsumeUnPaidPrecash = 0;
  public double ccaConsumeArgueAmt = 0;
      		   
  public double ccaConsumePrePayAmt = 0;
  public double ccaConsumeTotUnPaidAmt = 0;
  public double ccaConsumeBillLowLimit = 0;
  public double ccaConsumeBillLowPayAmt = 0;
      		   
  public double ccaConsumeIbmReceiveAmt = 0;
  public double ccaConsumeUnPostInstFee = 0;
  public double totAmtConsume = 0;
  public double totAmtPreCash = 0;
      		   
  public double ccaConsumeTxTotAmtMonth = 0;
  public double ccaConsumeTxTotCntMonth = 0;
  public double ccaConsumeTxTotAmtDay = 0;
  public double ccaConsumeTxTotCntDay = 0;
      		   
  public double ccaConsumeFnTotAmtMonth = 0;
  public double ccaConsumeFnTotCntMonth = 0;
  public double ccaConsumeFnTotAmtDay = 0;
  public double ccaConsumeFnTotCntDay = 0;
      			   		 
  public double ccaConsumeFcTotAmtMonth = 0;
  public double ccaConsumeFcTotCntMonth = 0;
  public double ccaConsumeFcTotAmtDay = 0;
  public double ccaConsumeFcTotCntDay = 0;
    
  public double ccaConsumeRejAuthCntDay = 0;
  public double ccaConsumeRejAuthCntMonth = 0;
  public double ccaConsumeTrainTotAmtMonth = 0;
  public double ccaConsumeTrainTotAmtDay = 0;
  
  public double ccaConsumeAuthTxlogAmt1 = 0;
  public double ccaConsumeAuthTxlogAmtCash1 = 0;
  
//  public  String   sgDefaultErrorIsoField39 = "99";
  public  int      initPnt=2;
  public  String[] isoField = new String[193];
  public  String[] acerField = new String[128];
//  public  String[] visaF62  = new String[64];
//  public  String[] visaF63  = new String[24];
//  public  String[] visaF126 = new String[24];
//  public  String   chtTpdu="" , chtProcCode="", chtAmount="", chtTraceNo="", chtNetId="", chtTerId="", chtSourceStr="", chtMsgType="", chtBitMap="";
//  public  String   chtAccInfo="", chtExpDate="", chtEntryMode="", chtCondCode="", chtMerId="", chtAppData="";
  
  public  String   bicHead="",errNum="000",mesgType="",hcomTpdu="", isoString="", specialIsoString="";
  // smsMsgType="", smsMsgId="";
  //ArrayList G_TokenQ8ObjArrayList = null;
  public ArrayList<TagObject> gTokenQ8ObjArrayList = null;
//  public  String   otpValue = "";
  public  String   sgTokenQ8SourceStr="", sgTokenQ9SourceStr="", sgTokenF1SourceStr="", sgTokenCZSourceStr="", tokenQ8TagQ9="";
  public  String   tokenQ8TagQA="", tokenQ8Tag27="", tokenQ8Tag51="", tokenQ8Tag50="", tokenQ8Tag07=""; 
  public  boolean  bgTokenQ9FormatIsVisa=false;
  public  boolean  bgHasPersonalAdj=false, bgHasCompAdj=false;  
  public  String    tokenQ9VisaMsgRsnCde="", tokenQ9Fiid="";
  public  String    tokenQ9VisaFiller="", tokenQ9VisaChipTxnInd="", tokenQ9VisaDevTyp="";
  public  String   edcTradeFunctionCode="";
  public  String    tokenQ9MasterDevTyp="", tokenQ9MasterAdviceRsnCde="", tokenQ9MasterAdvcDetlCde="", tokenQ9MasterAuthAgentIdCde="",tokenQ9MasterOnBehalf="", tokenQ9MasterFiller="";
  public  String   tokenQrAdditionalData1 ="", tokenQrAdditionalData2 = "";
  public  String   tokenF4WalletIndFlg = "", tokenF4WalletIndData = "", tokenF4Filler = "";
  public  String   bmsHead="",bmsDestId="",bmsSourceId="",tokenS8AcVeryRslt="", tokenS8FraudChkRslt="", newPin1="", tokenS8AcctNum="",  tokenIdQR="";
  public  String   newPinFrmt="", newPinOfst="", pinCnt="", nwePinSize="", newPin2="", ncccStandinInd="", pvvOnCardFlg="";
  public  String   tokenS8Filler = "", tokenS8ExpDat = "", tokenS8AcctNumInd = "";
  public  String   sgTransactionStartDateTime="";
  public  boolean  convertError=false,isoError=false, IsNewCard=false, isInstallmentTx=false, isRedeemTx=false;
  public  byte[]   isoData = new byte[2048];
  public  byte[]   specialIsoData = new byte[2048];
  public  int      totalLen=0,dataLen=0, specialDataTotalLen=0;
  public  String   orgiReserve="",stationId="",destStation="",srcStation="",terminalType="",termEntryCap="";
  public  String   sgKey4OkTrans="", sgCardProd="";
  public  String   sgUsedCardProd="";   
  public  String   sgIsoRespCode=""; // == proc.AuTxlog_ISO_RSP_CODE
  public  String   sgOnLineRespCode=""; // == proc.ONL_RESP_CODE
  public  String   sgBit38ApprCode="", transType=""; 
  public  String   authUnit=""; 
  public  boolean  ifStandIn=false;
  public  boolean  ifCredit=true; // 是否要佔額度
//  public  boolean  bmsTrans=true;       // @dq
  public  boolean  visaDebit=false;     // VISA DEBIT
  public  boolean  purchaseCard=false;  // 採購卡
  public  boolean  businessCard=false;  // 公司卡/商務卡
  public  boolean  childCard=false;     // 子卡
  public  boolean  comboCard=false;     // combo卡
  public  boolean  urgentCard=false;    // 緊急替代卡
//  public  boolean  specialCard=false;   // 
  public  boolean  adjRiskType=false;   // 臨調有風險類別之旗標
  public  boolean  isAdjSpecAmt=false;  // 臨調專款專用旗標
  public  boolean  isInsertTxlog=false;     //V1.00.56 避免因特店資料異常時，導致授權系統異常的處理排除
  public  boolean  isAuthMainControl=false; //V1.00.56 避免因特店資料異常時，導致授權系統異常的處理排除
  public  String   areaType="";         // "T" : xWꤺ , "F" : ~
  public  String   connCode="";         // "A" : BASE-24 ۰ʱv ,"W" : WEBHuv, "V" : VISA v, "M" : MASTER v
//kevin:多餘的設定，已經有isRedeemTx、installTransTx
//  public  boolean  installTrans=false;  // 
//  public  boolean  redeemTrans=false;   // Q
//  public  boolean  isAcs = false;
  //public  boolean  batchAuth=false;     // ΨƷ~ 妸v
  
  //kevin:FISC TOKEN TXN
  public  boolean  isFiscToken = false; 
  public  boolean  isTokenVTAR = false, isTokenVTNA = false, isTokenVAUT = false; 
  public  boolean  isTokenMAUT = false, isTokenMTER = false, isTokenMTAR = false, isTokenMTCN = false, isTokenMTEN = false; 
  //kevin:FISC OTHER TXN
  public  boolean  isLowTradeAmt = false, isEInvoice = false, isReceiptAdd = false, isReceiptCancel = false, isQRCodeActive = false, isQRCodePassive = false; 
  public  boolean  isNcccOnusPay = false, isReceipt = false, isIdCheckOrg = false, isElectPayTax = false, isOwnerCardOverHalfYear = false, recurringTrans = false ;
  public  String   idCheckType="", idCheckErrCnt="", idCheckErrType="";
  
  public int ngTxSession = 0;
  
  public String sgStaTxUnNormalMccBinNo="", sgStaTxUnNormalGroupCode="", sgStaTxUnNormalRespCode="", sgStaTxUnNormalRiskType="";
  public int ngStaTxUnNormalTxCnt=0;
  public int ngStaTxUnNormalTxAmt=0;
  
  public String sgSDailyMccBinNo="",sgSDailyMccGroupCode="";
  public String sgStaDailyMccUnNormalFlag="N", tokenChPmntTypInd4Master="";
  //public int nG_StaDailyMccTxSession=0; //此值不知道該如何取得..?
  public int ngStaDailyMccAuthCnt = 0;
  public int ngStaDailyMccAuthAmt = 0;

  public int ngStaDailyMccCallBankCnt = 0;
  public int ngStaDailyMccCallBankAmt = 0;
  public int ngStaDailyMccCallBankCntx = 0;
  public int ngStaDailyMccCallBankAmtx = 0;

  public int ngStaDailyMccDeclineCnt = 0;
  public int ngStaDailyMccDeclineAmt = 0;

  public int ngStaDailyMccPickupCnt = 0;
  public int ngStaDailyMccPickupAmt = 0;

  public int ngStaDailyMccExpiredCnt = 0;
  public int ngStaDailyMccExpiredAmt = 0;

      	
  public int ngStaDailyMccConsumeCnt = 0;
  public int ngStaDailyMccConsumeAmt = 0;

  public int ngStaDailyMccGenerCnt = 0;
  public int ngStaDailyMccGenerAmt = 0;

  public int ngStaDailyMccCashCnt = 0;
  public int ngStaDailyMccCashAmt = 0;

  public int ngStaDailyMccReturnCnt = 0;
  public int ngStaDailyMccReturnAmt = 0;

      	
  public int ngStaDailyMccAdjustCnt = 0;
  public int ngStaDailyMccAdjustAmt = 0;

  public int ngStaDailyMccReturnAdjCnt = 0;
  public int ngStaDailyMccReturnAdjAmt = 0;

  public int ngStaDailyMccForceCnt = 0;
  public int ngStaDailyMccForceAmt  =0;

  public int ngStaDailyMccMailCnt = 0;
  public int ngStaDailyMccMailAmt = 0;

  public int ngStaDailyMccPreauthCnt = 0;
  public int ngStaDailyMccPreauthAmt = 0;

  public int ngStaDailyMccPreauthOkCnt = 0;
  public int ngStaDailyMccPreauthOkAmt = 0;

  public int ngStaDailyMccCashAdjCnt = 0;
  public int ngStaDailyMccCashAdjAmt = 0;

  public int ngStaDailyMccReversalCnt = 0;
  public int ngStaDailyMccReversalAmt = 0;

  public int ngStaDailyMccEcCnt = 0;
  public int ngStaDailyMccEcAmt = 0;

      	
  public int ngStaDailyMccUnNormalCnt = 0;
  public int ngStaDailyMccUnNormalAmt = 0;

  
  public String sgSRskTypeBinNo ="", sgSRskTypeGroupCode="";
  public String sgSRskRiskType="", sgSRskCurrRespCode="", sgStaRiskTypeUnNormalFlag="";
  //public int nG_StaRiskTypeTxSession=0; //此值不知道該如何取得..?
  public int ngStaRiskTypeAuthCnt = 0;
  public int ngStaRiskTypeAuthAmt = 0;
      	
  public int ngStaRiskTypeCallBankCnt = 0;
  public int ngStaRiskTypeCallBankAmt = 0;

  public int ngStaRiskTypeCallBankCntx = 0;
  public int ngStaRiskTypeCallBankAmtx = 0;

  public int ngStaRiskTypeDeclineCnt = 0;
  public int ngStaRiskTypeDeclineAmt = 0;

  public int ngStaRiskTypePickupCnt = 0;
  public int ngStaRiskTypePickupAmt = 0;

  public int ngStaRiskTypeExpiredCnt = 0;
  public int ngStaRiskTypeExpiredAmt = 0;

      	
  public int ngStaRiskTypeConsumeCnt = 0;
  public int ngStaRiskTypeConsumeAmt = 0;

  public int ngStaRiskTypeGenerCnt = 0;
  public int ngStaRiskTypeGenerAmt = 0;

  public int ngStaRiskTypeCashCnt = 0;
  public int ngStaRiskTypeCashAmt = 0;

  public int ngStaRiskTypeReturnCnt = 0;
  public int ngStaRiskTypeReturnAmt = 0;

      	
  public int ngStaRiskTypeAdjustCnt = 0;
  public int ngStaRiskTypeAdjustAmt = 0;

  public int ngStaRiskTypeReturnAdjCnt = 0;
  public int ngStaRiskTypeReturnAdjAmt = 0;

  public int ngStaRiskTypeForceCnt = 0;
  public int ngStaRiskTypeForceAmt  =0;

  public int ngStaRiskTypeMailCnt = 0;
  public int ngStaRiskTypeMailAmt = 0;

  public int ngStaRiskTypePreauthCnt = 0;
  public int ngStaRiskTypePreauthAmt = 0;

  public int ngStaRiskTypePreauthOkCnt = 0;
  public int ngStaRiskTypePreauthOkAmt = 0;

  public int ngStaRiskTypeCashAdjCnt = 0;
  public int ngStaRiskTypeCashAdjAmt = 0;

  public int ngStaRiskTypeReversalCnt = 0;
  public int ngStaRiskTypeReversalAmt = 0;

  public int ngStaRiskTypeEcCnt = 0;
  public int ngStaRiskTypeEcAmt = 0;

      	
  public int ngStaRiskTypeUnNormalCnt = 0;
  public int ngStaRiskTypeUnNormalAmt = 0;

  public  boolean  bgAbnormalResp = true;
 /* O */
  public  boolean  normalPurch=false,cashAdvance=false,cashAdvanceCounter=false,cashAdvanceOnus=false,balanceInquiry=false,selfGas=false,pinVerified=false;
  public  boolean  changeAtmPin=false,atmCardOpen=false,isAtmCardActivated=false,verifyAtmPin=false,updateAtmPin=false;
  public  boolean  ecTrans=false,taxTrans=false,txVoice=false,speedTrain=false,creditAccount=false,contactLessByMagnetic=false;
  public  boolean  preAuth=false,preAuthComp=false,reversalTrans=false,atmTrans=false,ecGamble=false;
  public  boolean  forcePosting=false,preAuthAdvice=false,cancelTrans=false,tipsTrans=false;//adviceTrans=false;
  
  public  boolean  requestTrans=false;
  public  boolean  updateSrcTxAfterRefund=false, updateSrcTxAfterPreAuthComp=false, updateSrcTxAfterAdjust=false, updateSrcTxAfterPurchaseAdj=false, updateSrcTxAfterRefundAdj=false, updateSrcTxAfterCashAdj=false, updateSrcTxAfterReversal=false;
  public  String   srcIntfName="",destIntfName="",intfName="",srcFormatType="",destFormatType="", reversalFlag="N";
  public  boolean  convertToken=false,emvTrans=false;
  public  String   originator="0",respondor="0";
  public  boolean  isVip=false, isAuthVip=false, isSpecUse=false, isDebitCard=false, isSpecW0=false, isPrecash=true, isCardNotExit=false;// isCorp=false; //isCorp == proc.szCorpFlag;  isPrecash ==proc.szPrecashFlag
  public  boolean  ticketTxn = false; //三大票證
  public  boolean  easyAutoloadFlag = false, easyAutoload=false, easyAutoloadVd=false, easyAutoloadChk=false, easyStandIn=false; //悠遊卡交易類別
  public  boolean  ipassAutoload=false, ipassAutoloadChk=false, ipassStandIn=false; //一卡通交易類別
  public  boolean  icashAutoload=false, icashStandIn=false; //愛金卡交易類別
  public  boolean  nonPurchaseTxn = false;
  public  boolean  paymentTxn = false; //28xxxx Payment Transaction(PY)
  public  boolean  reversalBudgetAmt = false, reversalBudgetAmtCash = false; //V1.00.07 ECS人工沖正處理與沖正成功檢查原交易是否發生在budget date之前，須扣出沖正後金額避免佔額
  public  String scSpecCode ="";
  public  String corpActFlag="";  //Y:總繳, N:個繳
  public  String cardAcctAcnoFlag=""; //"1":一般(個人卡，個繳) or "2":總繳公司() or "3":商務個繳(商務卡，個繳) or "Y":總繳個人(商務卡，公司總繳)
  //public  boolean hasAdj=false; //O_{

 /* `B */
  public  double   monthTotalAmt=0,monthTotalCnt=0,dayTotalAmt=0,dayTotalCnt=0;
  public  double   supMonthAmt=0,supMonthCnt=0,supDayAmt=0,supDayCnt=0;
  public  double   finalTotLimit=0;//該等級之總額度(A)，proc name is tmpTotLimit1
  public  double   tmpTotLimit2=0;
  
  /*  */
  public  boolean  forceAuthPassed=false; //強制授權成功
  public  boolean  forceAuthRejected=false; //強制授權失敗
  public  boolean  writeToStatisticTable= true; //寫入統計檔
  public  String   connType="",txType="",fromAcctType="",toAcctType="",startTime="",binNo="",transCode="", logicDel="0", authType="", authSource="";
  public  String   acctStatus="",riskLevel="",groupCode="";
  public  boolean  purchAdjust=false,cashAdjust=false,refund=false,refundAdjust=false;
  public  boolean  mailOrder=false,masterEC=false,accountVerify=false, visaEC=false;
  public  boolean  tokenProcess020092=false, tokenProcess020093=false;
  public  int      chanNum=0,transCnt=0;
  public  String   vmjType="";
  public  double	debitMakup=1, debitFee=0;
  public  String   imsLockSeqNo="", vdTxnSeqNo="", imsOriRefNo="";

  //hsm key
  public  String   visaPvkA="", visaPvkB="", masterPvkA="", masterPvkB="", jcbPvkA="", jcbPvkB="";
  public  String   visaCvkA="", visaCvkB="", masterCvkA="", masterCvkB="", jcbCvkA="", jcbCvkB="";
  public  String   visaMdk="",  masterMdk="", jcbMdk="", keysZpk="", atmZpk="", atmZek="";
  public  String   hsmKeyOrg="00000000", hsmKeyCavv="00000001";
  public  String   visaCavvA="", visaCavvB="", masterCavvA="", masterCavvB="", jcbCavvA="", jcbCavvB="";

  /*
  public String    cardAcctTotAmtDay="0"; //累積日消費額
  public String    cardAcctTotCntDay="0"; //累積日消費次數
  public String    cardAcctFnTotAmtDay="0";//國外一般消費日總額
  public String    cardAcctFnTotCntDay="0"; //國外一般消費日總次
  public String    cardAcctFcTotAmtDay ="0";//國外預借現金日總額
  public String    cardAcctFcTotCntDay="0"; //國外預借現金日總次
  public String    cardAcctTrainTotalAmtDay ="0";//高鐵累積日消費額
  
  public String    cardAcctTotAmtMonth="0"; //累積月消費額
  public String    cardAcctTotCntMonth="0"; //累積月消費次數
  public String    cardAcctFnTotAmtMonth="0"; //國外一般消費月總額
  public String    cardAcctFnTotCntMonth="0"; //國外一般消費月總次
  public String    cardAcctFcTotAmtMonth="0"; //國外預借現金月總額
  public String    cardAcctFcTotCntMonth="0"; //國外預借現金月總次
  public String    cardAcctTrainTotalAmtMonth="0"; //高鐵累積月消費額
  public String    cardAcctTotAmtConsume="0"; //總授權額(已消未請)
  
  public String    cardAcctTotAmtPrecash = "0";
  public String    cardAcctTxTotAmtMonth = "0";
  public String    cardAcctTxTotCntMonth = "0";
  public String    cardAcctTxTotAmtDay = "0";
  public String    cardAcctTxTotCntDay = "0";

  public String    cardAcctTotAmtDayOfComp="0"; //累積日消費額
  public String    cardAcctTotCntDayOfComp="0"; //累積日消費次數
  public String    cardAcctFnTotAmtDayOfComp="0";//國外一般消費日總額
  public String    cardAcctFnTotCntDayOfComp="0"; //國外一般消費日總次
  public String    cardAcctFcTotAmtDayOfComp="0";//國外預借現金日總額
  public String    cardAcctFcTotCntDayOfComp="0"; //國外預借現金日總次
  public String    cardAcctTrainTotalAmtDayOfComp="0";//高鐵累積日消費額
  
  public String    cardAcctTotAmtMonthOfComp="0"; //累積月消費額
  public String    cardAcctTotCntMonthOfComp="0"; //累積月消費次數
  public String    cardAcctFnTotAmtMonthOfComp="0"; //國外一般消費月總額
  public String    cardAcctFnTotCntMonthOfComp="0"; //國外一般消費月總次
  public String    cardAcctFcTotAmtMonthOfComp="0"; //國外預借現金月總額
  public String    cardAcctFcTotCntMonthOfComp="0"; //國外預借現金月總次
  public String    cardAcctTrainTotalAmtMonthOfComp="0"; //高鐵累積月消費額
  public String    cardAcctTotAmtConsumeOfComp="0"; //總授權額(已消未請)
  
  public String    cardAcctTotAmtPrecashOfComp= "0";
  public String    cardAcctTxTotAmtMonthOfComp= "0";
  public String    cardAcctTxTotCntMonthOfComp= "0";
  public String    cardAcctTxTotAmtDayOfComp= "0";
  public String    cardAcctTxTotCntDayOfComp= "0";
  */
  //V1.00.38 P3授權額度查核調整-OTB計算處理--start--
  public boolean rollbackP2=false; //確認授權邏輯是否還原到第二階段
  public boolean rollbackP2RejectAcer=false; //授權邏輯在第二階段時，強制拒絕ACER紅利折抵交易註記
  public boolean rejectBonus=false, rejectInstallment=false; //授權系統強制拒絕紅利或分期交易，在非第二階段環境下
  public String  instSpecAddOnDate ="";
  public int     realCreditCardBaseLimit=0;//信用卡基本額度 ( 在 getRealBaseAmt() 中計算)
  public int     realCreditCardBaseLimitOfCash=0;//信用卡預借額度 ( 在 getRealBaseAmt() 中計算)
  public int     realCreditCardBaseLimitOfComp=0;//商務卡基本額度 ( 在 getRealBaseAmt() 中計算)
  public int     realCreditCardBaseLimitOfCashOfComp=0;//商務卡預借額度 ( 在 getRealBaseAmt() 中計算)
  public double  finalPaidConsumeFee=0; //結帳-消費
  public double  finalPaidPrecash=0; //結帳-預現
  public double  finalPaidConsumeFeeOfComp=0; //商務卡-結帳-消費
  public double  finalPaidPrecashOfComp=0; //商務卡-結帳-預現
  public double  totSpecAmtBal=0; //專款專用餘額
  public double  totSpecAmtBalOfComp=0; //商務卡專款專用餘額
  public double  specAmtConsume=0; //專款專用已使用額度
  public double  specAmtTotal=0;   //專款專用設定額度
  public double  SpecAmtMatch=0;   //專款專用已請款額度
  public double  otbAmtCash=0, parentOtbAmtCash=0; //預借現金可用額度
  public double  ccaConsumeUnPostInstSpec=0;  //分期未結帳金額(專款專用)
  public double  curTotalUnpaidOfPersonalOnlyCash=0;
  public double  curTotalUnpaidOfCompOnlyCash=0;
  public double  mccAdjParmTotAmtMonth = 100;//MCC Code 臨調放大總月限額倍數百分比
  public double  mccAdjParmTimesAmt = 100; //MCC Code臨調金額倍數百分比
  public double  mccAdjParmTimesCnt = 100; //MCC Code臨調次數倍數百分比 
  public boolean hasMccAdjParm=false;       //有符合產品類別臨調檔
  public boolean hasRiskLevelParm=false;    //有符合卡片卡人等級消費限額參數檔
  public boolean isSpecUseOfCardAcct=false;
  public boolean isSpecUseOfCardBase=false;
  //V1.00.38 P3授權額度查核調整-OTB計算處理--end--
  
  //V1.00.42 授權系統與DB連線交易異常時的處理改善方式。如為VD圈存交易則再寫入VD沖正異常處理
  public String  vdImsLog; //CCA_IMS_LOG VD授權失敗補沖正異常處理

  
  public String   cardBaseSpecStatus="", cardBaseSpecDelDate="";
  public String   cardBaseSpecFlag="";
  public String    cardBaseLastAmt = "0"; //最後消費金額 
  public int    cardBaseTotAmtDay=0; //日累積消費金額
  public int    cardBaseTotCntDay=0; //日累積消費次數
  public String    cardBaseLastConsumeDate=""; //最後消費日期
  public String    cardBaseLastConsumeTime=""; //最後消費時間
  public String    cardBaseLastAuthCode=""; //最後授權碼
  public String    cardBaseLastCurrency=""; //最後消費幣別
  public String    cardBaseLastCountry=""; //最後消費國家
  public String    cardBasePreAuthFlag="1"; //預先授權註記 => 表示 => /*非預先授權完成*/
  public String    cardBaseWriteOff1="0"; //預先授權沖消狀態(1) => 表示 /*非預先授權完成*/
  
  public int    riskTradeDayAmt=0; //本日累積交易金額
  public int    riskTradeDayCnt=0;  //本日累積交易筆數
  public int    riskTradeMonthAmt=0; //本月累積交易金額
  public int    riskTradeMonthCnt=0; //本月累積交易筆數
  
  public  String   mCode="", cardAcctIdx="", classCode="";
  public  String   traceNo="",oriTraceNo="", termHotkeycontrol="";

  public  String   ecsBatchCode="";
  public  String   mccRiskType="", mccRiskAmountRule="", mccRiskNcccFtpCode="";
  public  String   mccRiskMccCode="" ,merchantNo="",terminalNo="",mccCode="",entryMode="",posConditionCode="", merchantCountry="", merchantName="", entryModeType="", merchantCityName="", merchantCity="";//,country=""
  public  String   posTranInd="", smsPriority="", smsSubQuery="", subQuery="";
  public  String   cardAcctId="",idNo="",cardNo="",cardNoMask="",expireDate="", refNo="", oriAuthNo="", oriRespCode="", traceId="";
  public  String   idPseqno="",cardCode="",cardType="",cardKind="",lastTxDate="";//rejectCode=""
  public  String   txDate="", txTime="", t24ErrorCodes="", t24ErrorCodesFromDb="";//T24ErrorCodesFromDb sample: "5005,5001,5008"
  //public  String   CASH_CODE="CASH";
  public  boolean  resetCcaConsumeMonthData=true, resetCcaConsumeDayData=true, urgentFlag=false, ifCheckOpeningCard=false, isVirtualCard=false, isPurchaseCard=false, isChildCard=false, isChildAdj=false;
  public boolean   lowTradeCheck=false;
  //public boolean  comboTrade=false;
  //public  double   transAmount=0;
  public  double   adjustAmount=0,oriAmount=0,  balanceAmt=0, replAmt=0, oriLockAmount=0;
  public  double   bankTxAmt=0;
  public  double   totMonthLimit=0,totDayLimit=0,supMonthLimit=0,supDayLimit=0;
  public double    otbAmt=0, parentOtbAmt=0;//dlB
  public double   balInqTotal=0;
  public double ccaConsumeBillLowLimitOfComp=0;
  public double cbOtb=0;//
  //public double baseLimit=0;  /*帳戶循環信用額度*/  //Howard proc.CardAcct_LMT_TOT_CONSUME == java.ActAcnoLineOfCreditAmt
  //public double baseLimitOfComp=0;  /*帳戶循環信用額度-公司*/  //Howard proc.CardAcct_LMT_TOT_CONSUME == java.ActAcnoLineOfCreditAmt

  public double cashBase=0;   /*預借現金額度*/  //Howard proc.CardAcct_LMT_TOT_CASH == java.ActAcnoLineOfCreditAmtCash
  public double cashBaseOfComp=0;  //預借現金額度-公司
  //
  public double totalLimit=0; /**/
  public double cashLimit=0;  /**/
  public double monthLimit=0; /**/
  public double timesLimit=0; /**/
  public double monthCntLimit; /**/
  public double timesCntLimit=0;/**/
  public double monthLimitX=0;
  public double timesLimitX=0;
  public double paidPreCash=0;
  public double wkAdjAmt=0;
  public double wkAdjCnt=0;
  public double wkAdjTot=0;
  public double adjParmAmtRate=0; //
  public double adjParmCntRate=0; //
  public double adjTotAmt=0;
  public double exchangRate=0;
  public String wkCashCode = "";
  /*  */
  public  boolean  switchKeyExchange=false,bankKeyExchange=false, readFromImsSuccessful=true, vdResponseSuccessful=false, vdResponseTimeOut=false, readFromAcerSuccessful=true;
  public  boolean  changeKey=false,newKey=false,repeatKey=false,verifyKey=false;// forcePost=false;
  public  String   keyType="",keyDirection="",keyExchangeKey="",workingKeyZPK="",workingKeyLMK="",checkValue;

  /* */
  public  String   servCode="",pvki="",pvv="",cvv="",cvv2="",cvv2Result="", cvdfld="";
  public  String   pinBlock="",arqc="",arpc="",tvr="",cvr="",arc="",cvn="", newPinBlockFromHsm="", newPinFromHsm="";
  //kevin:tcb atm 交易新增
  public  String   atmHead="", atmType="", reqType="", respCode="", pCode="", fCode="",caAtmAmt="", birthday="", arqcLen="", txnDate="";
  
  public  boolean  ifSendSms4Cond1=false, ifSendSms4Cond2=false, ifIgnoreSmsOfTrading=false, isSmsLogic4Tcb=false; 
  public  boolean  is3DTranx=false; //是否為3D交易
  public  String   tokenData="", tokenC0="";
  public  String   eci="",cvv2token="",xid="",cavv="",ucafInd="",ucaf="", authnIndFlg="", cavvResult="", version3Ds="", transId3Ds;
  public  String   tccCode="",keyExchangeBlock="",walletIdentifier="";

  /*  */
  public  String   divMark="",installTxInd="",installTxRespCde="";
  public  String   divNum="",firstAmt="",everyAmt="",procAmt="";

  /*  */
  public  String   loyaltyTxId="",loyaltyTxResp="", c5TxFlag="";
  public  String   pointRedemption="",signBalance="",pointBalance="",paidCreditAmt="" ,remainingCreditAmt="";

  /* Field_48: FISC MESSAGE DATA ELEMENT #48 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200924 , ##START## */
  public  String   f48T23="",f48T26="",f48T30="",f48T33="",f48T37="",f48T40="",f48T41="",f48T42="",f48T43="",f48T44="";
  public  String   f48T66="",f48T71="",f48T72="",f48T74="",f48T77="",f48T79="",f48T82="",f48T83="",f48T87="",f48T88="";
  public  String   f48T89="",f48T90="",f48T92="",f48T95="",f48T42Eci="";
  /* Field_48: FISC MESSAGE DATA ELEMENT #48 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200924 , ##END## */  
  
  /* Field_55: EMV Chip Data */
  public  String   emv57="",emv5A="",emv5F24="",emv5F2A="",emv5F34="",emv71="",emv72="",emv82="",emv84="",emv8A="",emv4F="";
  public  String   emv91="",emv95="",emv9A="",emv9B="",emv9C="",emv9F02="",emv9F03="",emv9F09="",emv9F10="",emv9F1A="";
  public  String   tokenB2IssApplDataLen="", tokenB2IssApplData="";
  public  String   emv9F1E="",emv9F26="",emv9F27="",emv9F33="",emv9F34="",emv9F35="",emv9F36="",emv9F37="";
  public  String   emv9F74="",emv9F63="";
  public  String   emv9F41="",emv9F53="",emv9F5B="",emvDF31="",emvDFED="",emvDFEE="",emvDFEF="",emvD6="";
  public  String   termSerNum="";

  /* Field_58: FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##START## */
  public  String   f58T21="",f58T28="",f58T30="",f58T31="",f58T32="",f58T33="",f58T49="",f58T50="",f58T51="",f58T53="",f58T56="";
  public  String   f58T60="",f58T61="",f58T62="",f58T63="",f58T64="",f58T65="",f58T66="",f58T67Id="",f58T68IdCheckType="",f58T69SpecialTxn="";
  public  String   f58T70="",f58T71="",f58T72="",f58T73TokenType="",f58T80="",f58T81="",f58T82="",f58T83="",f58T84="",f58T85="";
  public  String   f58T86="",f58T87="",f58T87CellPhone="",f58T87Birthday="",f58T90="";
  /* Field_58: FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##END## */

  /* Field_120: FISC MESSAGE DATA ELEMENT #120 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START## */
  public  String   f120MTCN="",f120MTEN="";
  public  String   tokenProvider="",assuranceLevel="",tokenRequetorId="",contactlessUsage="",ecUsage="",mobileEcUsage="";
//public  String   correlationId="",numOfActiveToken="",tokenType="";
  public  String   issueProductId="",consumerLanguage="",deviceName="",finalDecision="",finalInd="",tcIndentifier="",tcDateTime="",activeAttempts="";  
  public  String   tokenUniqueRef="",acctNumberRef="",walletId="",deviceType="",tokenEvent="",tokenEventReason="",eventRequestor="";
  public  String   f120T0302="",f120None="";
  /* Field_127: FISC MESSAGE DATA ELEMENT #120 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##END## */

  /* Field_124: FISC MESSAGE DATA ELEMENT #124 VIAS ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START## */
  public  String   f124T01C0="",f124T01CF="",f124T01D4="";
  public  String   f124T0203="",f124T0204="";
  public  String   f124T1F31="",f124T1F32="",f124T1F33="",f124T0301="",f124T0302="",f124T0303="",f124T0304="",f124T0305="",f124T0306="",f124T0307="";
  public  String   f124T0308="",f124T030A="",f124T030B="",f124T031A="",f124T031B="",f124T031C="",f124T031D="",f124T0310="",f124T0311="",f124T0312="";
  public  String   f124T0313="",f124T0314="";
  public  String   f124T0401="",f124T0402="",f124T0403="",f124T0404="",f124T0405="",f124T0406="",f124T0407="";
  public  String   f124T0502="",f124T0503="",f124T0504="",f124T0505="",f124T0506="",f124T0507="",f124T0508="",f124T0509="",f124T050A="";
  /* Field_124: FISC MESSAGE DATA ELEMENT #124 M/C ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START## */
  public  String   f124TA="",f124TE="";
  public  String   tokenMesgType="",correlationId="",accountSource="",acctInstanceId="",deviceIp="",walletIdHash="",cardholderName="",recommendation=""; 
  public  String   recommendVerison="",deviceScore="",accountScore="",numOfActiveToken="",walletReasonCode="",deviceLocation="",numLast4Digits="",tokenType=""; 
  public  String   numOfInactiveToken="",numOfSuspendedToken="";
  public  String   f124None="";
  public  String   configutationId="";
  /* Field_124: FISC MESSAGE DATA ELEMENT #124 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##END## */
  /* Field_127: FISC MESSAGE DATA ELEMENT #127 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START## */
  public  String   f127T4001="",f127T4002="";
  public  String   f127T4101="",f127T4102="";
  public  String   f127None="";
  /* Field_127: FISC MESSAGE DATA ELEMENT #127 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##END## */


  /* SHARE MEMORY BUUFER  */
  HashMap parmHash = null;

  //down, add by JH

  public int debitParmTableFlag=1;//等於1表示從 table CCA_DEBIT_PARM 取得 debit card 參數; 等於2表示從 table CCA_DEBIT_PARM2 取得 debit card 參數
  public int sendSmsLimitAmt=-1; //
  public boolean ifHaveRiskTradeInfo=true; //是否已經有舊的交易資料了
  public boolean ifSendSmsWhenOverLimit=true; //
  public boolean ifHaveOempayInfo=true; //是否已經有OEMPAY卡片資料了
  public boolean isMobileLast4Digtal=true; //V1.00.64 MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同

  public boolean smsSupLimit=false;	//
  public boolean depositNotEnough=false;	//
  public boolean dayLimitNotEnough=false;	//
  public boolean monthLimitNotEnough=false;	//
  public boolean isSupCard=false, isAdvicePrimChFlag=false;	//V1.00.48  P3程式碼整理(附卡註記/附卡消費通知正卡註記)
  public String binType=""; //CRD_CARD.binType
  public String effDateEnd = "";//CRD_CARD.effDateEnd
  public String cardStatus = "";//

  public double  currTotLmtAmt=0;
  //public double  currTotStdAmt=0;
  public double  currTotTxAmt=0;
  public double  currDdLmtAmt=0;
  public double currDdTotAmt=0;
  public double currTotCashAmt=0;
  public double currTotUnpaid=0;
  //雙幣卡資訊
  public double dualAmt4Twd=0, dualRate4OriToUsd=0, dualRate4UsdToTwd=0, dualAmt4Bill=0; 
  public String dualCurr4Bill="";
  
  //public String srcCacuAmount="Y";
  //public String srcCacuCash="Y";

  public String standInReason="";
  public String cacuAmount="N"; /* 計入OTB註記            */
  public String cacuCash="N";  /* 計入OTB預現註記        */
//  public String bankTxAmt="";
  //public String bankTxSeqno="";
  //public String srcBankTxSeqno="";
  public String unlockFlag="N";
  public String authErrId="";
  public String authStatusCode="00";
  public String authNo="";
  public String authRemark="";
  //public String nt_amt="";
  public double ntAmt=0, isoFiled4Value=0;//OISOField4
  public double isoFiled4ValueAfterMarkup=0;//OISOField4 [WO᪺
  public double cardholderBillAmt=0, cardholderBillAmtMarkup=0;//來自於ISOField6 CARDHOLDER BILLING AMOUNT and MARKUP
  public double adjustAmountAfterMarkup=0;
  public double egovMarkupFee=0;
  public String authSeqno="";
  public String isoBitMap="";
  public String cacuFlag="";
  public String standInRspcode="";
  public String standInOnuscode="";
  public double txAmtPct=0;
  public String aeTransAmt="";
  public String roc="";
  public String idnoVip="";
  public String idnoName="";
  public String cvdPresent="";
  
  public String fallback="N";
  public String authUser="", approveUser="";
  
  public String hceRsnCode="";
  public String tpanTicketNo="",tpanExpire="", tpanPinResult="", tpanAcResult="", tpanFraudChk="", tpanReasonCode="", tpanStatusCode="";

  //MasterCard Google pay information
  public String acctNumInd="", productCode="", tokenAssuranceLevel="", tokenRequestId="", accountRange="", storageTech="";

  public String bankBit33Code="";
  public String bankBit39Code="";
  public String debtFlag="N";
  //kevin:chkeck FHM or NEG 
  public String chkFhmNeg="";

  //public String crt_user="system";
  //public String modUser="system";

  public double lastAmt=0; 
  public boolean prodMode=true;
  public double srcTxNtAmt=0;//
  public double lockAmt=0; //

  public double unLockAmt=0; //
  
  public boolean ifSendTransToIbm=false;
  public boolean ifSendTransToIms=false;

//  public boolean callT24ToLock = false;//OO_ncall T24  s
//  public boolean callT24ToUnLock = false;//OO_ncall T24  Ѱ
//  public boolean callT24ToForceLock = false;//OO_ncall T24  js
//  public String mchtNameToT24 = "";// OsPѰ餧SW
//  public boolean updatePriorOfPriorBankTxSeqNo=false;

  //up, add by JH

  /*
    0:PIN,
	1:PIN+CVVF
	2:ͤF
	3:ͤ+CVV;
	4:CVV
   */
  public String vrfyType="";//

  public String tokenId04="",tokenId06="",tokenId23="",tokenId25="",tokenIdB2="",tokenIdB3="",tokenIdB4="";
  public String tokenIdB5="",tokenIdB6="",tokenIdBJ="",tokenIdC0="",tokenIdC5="",tokenIdC6="";
  public String tokenIdCE="",tokenIdCI="",tokenIdQ2="",tokenIdQ3="",tokenIdW7="",tokenIdW8="",tokenIdWB="";
  public String tokenIdWV="", tokenIdS8="", tokenIdF4="", tokenIdQ8="", tokenIdQ9="", tokenIdF1="", tokenIdCZ="", tokenIdCH="";
  public String visaAddlData="";
  public String tokenDataB2="",tokenDataB3="",tokenDataB4="", tokenData04="";

  //kevin: 取得風險分數 risk_factor
  public int    riskFctorInd=0;
  public double riskFactorScore=0, mccRiskFactor=0, posRiskFactor=0, countryRiskFactor=0, mchtRiskFactor=0;
  public double cardRiskFactor=0, repeatRiskFactor=0, vipRiskFactor=0, amtRiskFactor=0;

  
  public AuthTxnGate() {
	  for ( int k = 0; k < 128; k++) {
		  isoField[k] = "";  
		  acerField[k] = "";
	  }
	  try {
		  sgTransactionStartDateTime = HpeUtil.getCurDateTimeStr(false, false);
	  } catch (Exception e) {
		// TODO: handle exception
	  }
	  

  }

}
