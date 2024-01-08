/**
 * 授權邏輯查核-HSM相關資料交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-HSM相關資料交易檢核處理              *
 * 2021/04/09  V1.00.01  Kevin       VISA_CAVV_U3V7檢核處理                     *
 * 2021/04/09  V1.00.02  Kevin       MasterCard 晶片ARC不用特別處理               *
 * 2021/12/03  V1.00.03  Kevin       預借現金交易仍維持只能驗預借現金密碼PIN，其餘交易驗證 *
 *                                   PIN規則調整為，只要有帶PIN就驗預借現金密碼PIN或生日的MMDD
 * 2022/03/21  V1.00.04  Kevin       櫃台預借未帶PIN時，不驗PVV                    *                                  
 * 2022/04/06  V1.00.05  Kevin       因聯邦收單特店綠界未帶入3D版本，修改邏輯判斷3D版本   *
 * 2022/05/04  V1.00.06  Kevin       ATM預借現金密碼變更功能開發                    *
 * 2022/06/01  V1.00.07  Kevin       預借現金密碼錯誤次數檢查                       *
 * 2022/10/04  V1.00.08  Kevin       排除部分收單銀行因未帶入5F34導致驗證ARQC錯誤的問題  *
 * 2022/11/10  V1.00.24  Kevin       免照會VIP，可排除因HSM驗證CAVV失敗而拒絕的交易    *
 * 2023/01/16  V1.00.34  Kevin       MasterCard CAVV版本全面升級至2.0             *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/10/23  V1.00.56  Kevin       避免因特店資料異常時，導致授權系統異常的處理排除        *
 * 2023/12/11  V1.00.61  Kevin       3D交易欄位格式調整                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.hsm.HsmApi;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckHsmData extends AuthLogic {
	
	/**
	 * HSM API存取物件
	 */
	HsmApi gHsmUtil = null;
	
	public LogicCheckHsmData(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckHsmData : started");

		try {
			if (!ta.selectHsmKeys(gate.hsmKeyOrg)) {
				if (!"00000000".equals(gate.hsmKeyOrg)) {
					gate.hsmKeyOrg = "00000000";
					if (!ta.selectHsmKeys(gate.hsmKeyOrg)) {
						gb.showLogMessage("E","Select HSM KEY : not found"+gate.hsmKeyOrg);
					}
				}
			}
			if (gate.is3DTranx) {
				if (!ta.selectHsmKeys(gate.hsmKeyCavv)) {
					gb.showLogMessage("E","Select HSM CAVV KEY : not found"+gate.hsmKeyCavv);
					gate.visaCavvA   = gate.visaCvkA;
					gate.visaCavvB   = gate.visaCvkB;
					gate.masterCavvA = gate.masterCvkA;
					gate.masterCavvB = gate.masterCvkB;
					gate.jcbCavvA    = gate.jcbCvkA;
					gate.jcbCavvB    = gate.jcbCvkB;
				}
			}
		} catch (Exception e) {
			gb.showLogMessage("E","Select HSM KEY : Failed");
		}
		if ("2".equals(gb.getHsmIndicate())) {
			this.gHsmUtil = new HsmApi(gb.getHsmHost2(), gb.getHsmPort2());
		}
		else {
			this.gHsmUtil = new HsmApi(gb.getHsmHost1(), gb.getHsmPort1());
		}
		return;
	}
	
	/**
	 * 查核 HSM 資料
	 * V1.00.61 3D交易欄位格式調整
	 * @return 如果查核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	//V1.00.03  驗證PIN規則調整，只要有帶PIN就驗，驗不過就驗生日MMDD，排除預借
	public boolean checkHsmData() throws Exception {

		if(!verifyCvv()) {         // 查核CVV
			return false;
		}

		if (!verifyArqc()) {       // 查核ARQC
			return false;
		}

		String slErrorCode = verifyPvv();        // 查核PVV      
		if (!gGate.pinVerified && slErrorCode.length()>0) {
			if ( (gGate.cashAdvance) || (gGate.cashAdjust) || (gGate.verifyAtmPin) ) { //預借現金、預借現金調整 or CHANGE ATM PIN 才要 往下走
				if (!"DZ".equals(slErrorCode)) {
					ta.addCardPasswdErrCount(gGate.cardNo);
				}
				ta.getAndSetErrorCode(slErrorCode);
				return false;	
			}
//			if ((("4011").equals(gGate.mccCode)) && gGate.pinBlock.length()>0 ) {
//			if ( gGate.pinBlock.length()>0 ) {
			else {
				if (!atmPinGen()) {
					return false;		
				}		
				if (!atmPvvGen()) {
					return false;
				}
				slErrorCode = verifyPvv();        // 用生日查核pvv    
				if (!gGate.pinVerified && slErrorCode.length()>0) {
					if (!"DZ".equals(slErrorCode)) {
						ta.addCardPasswdErrCount(gGate.cardNo);
					}
					ta.getAndSetErrorCode(slErrorCode);
					return false;
				}
			}
//			else {
//				ta.addCardPasswdErrCount(gGate.cardNo);
//				ta.getAndSetErrorCode("DX");
//				return false;
//			}
		}

		// 查核CVC2 
		if (!verifyCvc2() ) {             
			return false;
		}

		// 查核CAVV
		if (gGate.is3DTranx) {
			if(!verifyACSAAV())
				return false;
		}
		
		// 變更ATM 密碼
		if ( gGate.changeAtmPin )  { 
			if(!changeAtmPin())
				return false;
		}
		
		return true;
	}
	
	/**
	 * 驗證 CVC2
	 * @return 如果驗證成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyCvc2() throws Exception {

		if ("N".equals(gb.getIfEnableHsmVerifyCvv())) {
			if (("1".equals(gGate.cvdPresent)) || ((!"".equals(gGate.cvdfld) ))){
				gGate.cvv2Result = "M";
			}
			return true;
		}
		
		boolean blResult = true, blVerifyCvc2=false;		
//		if (gGate.isoField[22].trim().length()>=2) {
//			String slIsoFiled22 = gGate.isoField[22].trim().substring(0,2);
//			if (( "00".equals(slIsoFiled22)) || ( "01".equals(slIsoFiled22)) || ( "81".equals(slIsoFiled22)) ) {
		String slCvc2="";
		if (("1".equals(gGate.cvdPresent)) || ((!"".equals(gGate.cvdfld) ))){
			blVerifyCvc2 = true;
			slCvc2 = gGate.cvdfld;

			if (blVerifyCvc2){
				String slCardNo=gGate.cardNo;
				String slExpireDate = transExpireDate(gGate.expireDate, false);
				String slServiceCode = "000";
				String slCsseccfgCvka = "";
				String slCsseccfgCvkb = "";
				String slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);
				switch (slBinType) {
					case "V" : slCsseccfgCvka = gGate.visaCvkA  ; slCsseccfgCvkb = gGate.visaCvkB;  break; /* VISA CVK*/
					case "M" : slCsseccfgCvka = gGate.masterCvkA; slCsseccfgCvkb = gGate.masterCvkB;  break; /* MASTERCARD CVK*/
					case "J" : slCsseccfgCvka = gGate.jcbCvkA   ; slCsseccfgCvkb = gGate.jcbCvkB;  break; /* JCB CVK*/
					default   : break;
				}

//					gb.showLogMessage("D","verify verifyCvc2 =>" +"BizId:" + "sL_BizId" + ", CVC2:" + slCvc2 +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate + ", ServiceCode:"  + slServiceCode );

				try {
					String slResult = gHsmUtil.hsmCommandCY(slCardNo, slExpireDate, slServiceCode, slCvc2, slCsseccfgCvka, slCsseccfgCvkb);
					if ("00".equals(slResult.substring(0,2)))
					{
						blResult = true;
						gGate.cvv2Result = "M";
					}
					else {
						blResult = false;
						ta.getAndSetErrorCode("DY");
						gGate.tokenData04 = "! 0400020 C           D       ";
						gGate.tokenId04 ="04";
						gGate.cvv2Result = "N";
					}
				}
				catch (Exception e) {
					ta.getAndSetErrorCode("2I");//驗證CVV交易時，HSM機器或網路異常未回應
					gGate.cvv2Result = "P";
					return false;
				}
			}
		}
//		}
		return blResult;
	}
	
	/**
	 * 啟動驗證晶片卡arqc
	 * @return verify ok is true
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyArqc() throws Exception {

 		boolean blResult = true;
 		
		setCvr(); //在取得binType後才能call this function

		//kevin:ARQC驗證開關，關閉時，不需檢驗ARQC
		if ("N".equals(gb.getIfEnableHsmVerifyArqc())) {
			return blResult;
		}
		//kevin:HCE交易時，ARQC驗證改由TWMP，只要判讀TWMP驗證結果
		if (gGate.f58T32.length() > 0) {
			return blResult;
		}
		//kevin:token交易時，ARQC驗證改由VISA，只要判讀VISA驗證結果
		if (gGate.f48T74.length() > 0) {
			gb.showLogMessage("D","VISA已驗證arqc = "+gGate.f48T74);  
			if (gGate.f48T74.equals("50S")) {
				return blResult;
			}
		}

		if (gGate.isoField[22].trim().length()>=2) {
			String slIsoField22=gGate.isoField[22].trim().substring(0,2);
			if ("07".equals(slIsoField22)) { 
				if ("".equals(gGate.emv9F26))
					return true;//ARQC
				else {
					//call HSM ARQC
					blResult = preVerifyArqc();
				}
			}
			else {
				if ("05".equals(slIsoField22)){
					//call HSM ARQC
					blResult = preVerifyArqc();
				}
			}
		}
		return blResult;
	}

	/**
	 * 計算token中emvD6的值，並設定給變數CVR
	 * @throws Exception if any exception occurred
	 */
	private void setCvr() throws Exception {
		if ("V".equals(gGate.binType) && gGate.emv9F10.length() >= 14 ) {
			gGate.emvD6 = gGate.emv9F10.substring(6,14); 
			gGate.cvn = Integer.valueOf(gGate.emv9F10.substring(4,6),16).toString();
		}
		if ("M".equals(gGate.binType) && gGate.emv9F10.length() >= 18 ) {
			if (gGate.emv9F10.length() > 18) {
				gGate.emvD6 = gGate.emv9F10.substring(4,16);  
				gGate.cvn = Integer.valueOf(gGate.emv9F10.substring(2,4),16).toString();
			}
			else {
				gGate.emvD6 = gGate.emv9F10.substring(6,14);  
				gGate.cvn = Integer.valueOf(gGate.emv9F10.substring(4,6),16).toString();
			}
		}
		if ("J".equals(gGate.binType) && gGate.emv9F10.length() >= 16 ) {
			gGate.emvD6 = gGate.emv9F10.substring(6,16);  
			gGate.cvn = Integer.valueOf(gGate.emv9F10.substring(4,6),16).toString();
		}
		if ("17".equals(gGate.cvn)) {
			gGate.emvD6 = gGate.emv9F10.substring(8,10);
		}
		if ("18".equals(gGate.cvn)) {
			gGate.emvD6 = gGate.emv9F10.substring(0,14);
		}
		gGate.cvr = gGate.emvD6;

		gb.showLogMessage("D","VISA EMV CVR CVN = "+gGate.cvr+"=>"+gGate.cvn);

	}
	
	
	/**
	 * 產生驗證晶片卡data
	 * @return 晶片卡data
	 * @throws Exception if any exception occurred
	 */
	private String genCdolData() throws Exception{

		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9F02="+gGate.emv9F02);  //06
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9F03="+gGate.emv9F03);  //06
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9F1A="+gGate.emv9F1A);  //02
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv95="+gGate.emv95);      //05
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv5F2A="+gGate.emv5F2A);  //02
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9A="+gGate.emv9A);      //03
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9C="+gGate.emv9C);      //01
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9F37="+gGate.emv9F37);  //04
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv82="+gGate.emv82);      //02
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.emv9F36="+gGate.emv9F36);  //02
		//gb.showLogMessage("D","@@@@EMV@@@@G_Gate.cvr="+gGate.cvr);          //

		String slCdolData = "";
		if ("17".equals(gGate.cvn)) {
			slCdolData = gGate.emv9F02 + gGate.emv9F37 + gGate.emv9F36 + gGate.cvr+"000000";

//			gb.showLogMessage("D","@@@@EMV@@@@sL_CdolData="+slCdolData);

			return slCdolData;	 
		}
		if (gGate.emv9F03.length()<12) {
			gGate.emv9F03 = "000000000000";
		}
			
		slCdolData = gGate.emv9F02 + gGate.emv9F03 + gGate.emv9F1A + gGate.emv95 + gGate.emv5F2A + gGate.emv9A + gGate.emv9C + gGate.emv9F37 + gGate.emv82 + gGate.emv9F36 + gGate.cvr;

		if ("M".equals(gGate.binType)) {
			slCdolData = slCdolData + "80";
		}
		else if ("V".equals(gGate.binType)) {
			if ("18".equals(gGate.cvn)) {
				slCdolData = slCdolData + "8000000000000000";
			}
			else {
				slCdolData = slCdolData + "000000";
			}
		}
		else if ("J".equals(gGate.binType)) {
			slCdolData = slCdolData + "0000";
		}
		
		//arqc txn data len
		if  (slCdolData.length()<80) {

			gb.showLogMessage("D","@@@@EMV@@@@sL_CdolData length <40="+slCdolData);

			slCdolData = slCdolData + "0000";
		}

//		gb.showLogMessage("D","@@@@EMV@@@@sL_CdolData="+slCdolData);

		return slCdolData;
	}
	
	/**
	 * 產生arc
	 * @return arc
	 * @throws Exception if any exception occurred
	 */
	private String genArc() {
		String slArcHeader = "", slArc="";
//V1.00.02  MasterCard 晶片ARC不用特別處理 
//		if ( "M".equals(gGate.binType) && !"01".equals(gGate.emv9F10.substring(4, 6))) {
//			if ("00".equals(gGate.isoField[39])) {
//				slArcHeader = "0016";
//			}
//			else{
//				slArcHeader = "0006";
//			}
//		}
//		else
			slArcHeader = HpeUtil.convertToBinary(gGate.isoField[39]); //00 -> 3030, 05 -> 3035

		slArc = slArcHeader ;
		return slArc;
	}

	/**
	 * 驗證晶片卡arqc
	 * @return verify ok is true
	 * @throws Exception if any exception occurred
	 */
	private boolean preVerifyArqc()   throws Exception{
		String slArqc="";
		slArqc = gGate.emv9F26;

		gb.showLogMessage("D","@@@@XXX@@@@sL_Arqc_emv9F26="+slArqc);

		if ("".equals(slArqc))
			return true;
		String slCardNo=gGate.cardNo.substring(2,16);
		//hce驗arqc須帶tpan
		if (gGate.tpanTicketNo.length() > 0) {

//			gb.showLogMessage("D","@@@@arqc_hce_tpan = "+gGate.tpanTicketNo);

			slCardNo = gGate.tpanTicketNo.substring(2,16);
		}
//		String slExpireDate = transExpireDate(gGate.expireDate, false);

		String slSchemeId="";
		String slMkAc="";
		String slTxnLen="28";
		if ("17".equals(gGate.cvn)) {
			slTxnLen="10";
			gb.showLogMessage("D","visa arqc verify txn data len = "+gGate.cvn +"="+slTxnLen);
		}
//		String slTxn="";

		if ("V".equals(gGate.binType)) {
			slSchemeId = "0";
			slMkAc     = gGate.visaMdk;
//			sL_TxnLen   = "37";
		}
		else if ("M".equals(gGate.binType)) {
			slSchemeId = "1";
			slMkAc     = gGate.masterMdk;
//			sL_TxnLen   = "40";
		}
		else if ("J".equals(gGate.binType)) {
			slSchemeId = "0";
			slMkAc     = gGate.jcbMdk;
//			sL_TxnLen   = "38";
		}
		String slAtc= gGate.emv9F36;
		String slCdolData = genCdolData();	
		//V1.00.08 排除部分收單銀行因未帶入5F34導致驗證ARQC錯誤的問題
		if (gGate.emv5F34.length()<=0) {
			gGate.emv5F34 = "00";
		}
		String slPanData = slCardNo + gGate.emv5F34;
		String slUn = gGate.emv9F37;

		//gb.showLogMessage("D","@@@@EMV@@@@ATC_emv9F36="+gGate.emv9F36);
		//gb.showLogMessage("D","@@@@XXX@@@@ATC_emv9F36="+slAtc);
		//gb.showLogMessage("D","@@@@XXX@@@@sL_CdolData="+slCdolData);
		//gb.showLogMessage("D","@@@@EMV@@@@CardNo_emv5F34="+gGate.emv5F34);
		//gb.showLogMessage("D","@@@@XXX@@@@sL_PanData="+slPanData);
		//gb.showLogMessage("D","@@@@EMV@@@@sL_Un_emv9F37="+gGate.emv9F37);
		//gb.showLogMessage("D","@@@@XXX@@@@sL_Un="+slUn);

		//call HSM ARQC
		boolean blResult = false;

		String slArc = genArc();
		gGate.arc = slArc;  
		if ( "M".equals(gGate.binType) && !"01".equals(gGate.emv9F10.substring(4, 6))) {
			if ("00".equals(gGate.isoField[39])) {
				slArc = "0016";
			}
			else{
				slArc = "0006";
			}
		}

//		gb.showLogMessage("D","@@@@XXX@@@@sL_Arc="+gGate.arc);

		String slArpc ="";
		if ("18".equals(gGate.cvn)) {
			slSchemeId = "2";
			slTxnLen="30";
			slArpc = gHsmUtil.hsmCommandKW("1", slSchemeId, slMkAc, slPanData, slAtc, 
					slUn, slTxnLen, slCdolData, slArqc, slArc);
		}
		else {
			slArpc = gHsmUtil.hsmCommandKQ("1", slSchemeId, slMkAc, slPanData, slAtc, 
					slUn, slTxnLen, slCdolData, slArqc, slArc);
		}

		//kevin:財金規格arpc須包含arc共10位 ex.sLArpc = "003E6F17C8A05526F2";
		if ("00".equals(slArpc.substring(0,2))) {
			gGate.arpc = slArpc.substring(2,18);
			gGate.emv91 = gGate.arpc + slArc;
//			gGate.emv8A = slArc;
			gGate.emv8A = gGate.arc;
			gGate.f48T74 = "50S";


//			gb.showLogMessage("D","連線HSM驗證ARQC結果=>"+"驗證成功="+slArpc.substring(0,2)+" 產生ARPC="+gGate.arpc);

			gGate.tokenIdB5 ="B5";
			blResult = true;
		}					
		else if ("01".equals(slArpc.substring(0,2))){

			gb.showLogMessage("I","HSM 驗證錯誤! On verifyArpc() error KQ = "+slArpc);

			ta.getAndSetErrorCode("KQ");
			gGate.f48T74 = "50I";
			blResult = false;
		} 
		else if ("mC".equals(slArpc.substring(0,2))){

			gb.showLogMessage("I","HSM 驗證錯誤! On verifyArpc() error 2I = "+slArpc);

			ta.getAndSetErrorCode("2I");
			gGate.f48T74 = "50U";
			blResult = false;
		} 
		else {

			gb.showLogMessage("I","HSM 格式錯誤! On verifyArpc() error KQ = "+slArpc);

			ta.getAndSetErrorCode("KQ");
			gGate.f48T74 = "50F";
			blResult = false;
		}
		return blResult;
	}
	
	/**
	 * 查核 CVV
	 * @return 如果查核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyCvv() throws Exception {

		boolean blResult = true;
		if ("N".equals(gb.getIfEnableHsmVerifyCvv()))
			return true;
		//虛擬卡並非一定是採購卡
		if (gGate.isVirtualCard) { //虛擬卡不做檢核
			return true;
		}
		//ATM開卡通知不查核卡片CVV
		if (gGate.atmCardOpen) {
			return true;
		}
		//人工授權及批次授權不查核卡片CVV
		if (( "WEB".equals(gGate.connType)) ||  "BATCH".equals(gGate.connType))
			return true;
		//kevin:token交易時，CVV和ARQC驗證改由VISA，只要判讀VISA驗證結果
		if (gGate.f48T87.length() > 0) {
			if (gGate.f48T87.equals("M")) {
				gb.showLogMessage("D","VISA已驗證cvv = "+gGate.f48T87);  
				return true;
			}
		}
		if (gGate.f48T74.length() > 0) {
			gb.showLogMessage("D","VISA已驗證CVV & ARQC = "+gGate.f48T74);  
			return true;
		}
		
		if ("".equals(gGate.cvv))
			return true;

		String slCardNo=gGate.cardNo;
		String slExpireDate = transExpireDate(gGate.expireDate, false);
		String slServiceCode = gGate.servCode;
		String slCvv = gGate.cvv;

		//kevin:改為selectHsmKeys (ptr_hsm_keys)，從AuthGlobalParm取的key值
		String slCsseccfgCvka = "";
		String slCsseccfgCvkb = "";
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		String  slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);

//		gb.showLogMessage("D","bin_type"+slBinType+"="+ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN));

		switch (slBinType) {
			case "V" : slCsseccfgCvka = gGate.visaCvkA  ; slCsseccfgCvkb = gGate.visaCvkB;  break; /* VISA CVK*/
			case "M" : slCsseccfgCvka = gGate.masterCvkA; slCsseccfgCvkb = gGate.masterCvkB;  break; /* MASTERCARD CVK*/
			case "J" : slCsseccfgCvka = gGate.jcbCvkA   ; slCsseccfgCvkb = gGate.jcbCvkB;  break; /* JCB CVK*/
			default   : break;
		}
		//kevin:合庫特殊處理，所有ICVV都是使用VISA的KEY，除了HCE以外
		if (gGate.emvTrans && gGate.f58T32.length() == 0) {
			slServiceCode = "999";
		    slCsseccfgCvka = gGate.visaCvkA; 
		    slCsseccfgCvkb = gGate.visaCvkB;
		}

//		gb.showLogMessage("D","verify verifyCvv =>"+"BizId:" + "sL_BizId" + ", CVV:" + slCvv +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate + ", ServiceCode:"  + slServiceCode );

		//kevin:確保call hsm不會出現exception而當掉
		try {
			String slResult = gHsmUtil.hsmCommandCY(slCardNo, slExpireDate, slServiceCode, slCvv, slCsseccfgCvka, slCsseccfgCvkb);
			if ("00".equals(slResult.substring(0,2))) {
				blResult = true;

				gb.showLogMessage("D","連線HSM驗證CVV結果=>"+"驗證成功="+slResult);

			}
			else {
				blResult = false;
				if ("RO".equals(slResult.substring(0,2))){

					gb.showLogMessage("I","HSM 錯誤! On verifyCvv() error 2I = "+slResult);

					ta.getAndSetErrorCode("2I");
				} 
				else {

					gb.showLogMessage("I","CVV 錯誤! On verifyCvv() error DY = "+slResult);

					ta.getAndSetErrorCode("DY");
					gGate.tokenData04 = "! 0400020 C           D       ";
					gGate.tokenId04 ="04";
				}
			}
		} catch (Exception e) {
	
			ta.getAndSetErrorCode("2I");
			return false;
		}
		return blResult;
	}
	
	/**
	 * 查核 CAVV
	 * V1.00.34 MasterCard CAVV版本全面升級至2.0
	 * @return 如果查核通過，return true，否則return false
	 * @throws Exception if any exception occurred
	 */

	private boolean verifyACSAAV() throws Exception {

		boolean blResult = true;
		if ("N".equals(gb.getIfEnableHsmVerifyACSAAV())) {
			if ("FISC".equals(gGate.connType) && (gGate.is3DTranx)) {
				gGate.cavvResult = "2";
			}
			return true;
		}
		//虛擬卡並非一定是採購卡
//		if (gGate.isVirtualCard) { //虛擬卡不做檢核
//			return true;
//		}

		/*人工授權及批次授權不查核卡片CVV*/
		if (( "WEB".equals(gGate.connType)) ||  "BATCH".equals(gGate.connType))
			return true;
		//如VISA已驗證cavv正確_發卡不須再驗證
		if (gGate.cavvResult.length() > 0) {
			gb.showLogMessage("D","VISA驗證cavv = "+gGate.cavvResult);  
			if ("3".equals(gGate.cavvResult) || "0".equals(gGate.cavvResult) || "A".equals(gGate.cavvResult) ||
				"B".equals(gGate.cavvResult) || "8".equals(gGate.cavvResult) || "2".equals(gGate.cavvResult)) {
				return true;
			}
			else {
				ta.getAndSetErrorCode("Q3");
				return false;
			}
		}
		//3D交易未帶UCAF/CAVV
		if ("FISC".equals(gGate.connType) && (gGate.is3DTranx)) {
			if ("".equals(gGate.ucaf)) {
				gGate.cavvResult = "0";
				ta.getAndSetErrorCode("QK");
				return false;
			}		
		}
		//kevin:CAVV與UCAF有值才檢驗CAVV		
		if ("".equals(gGate.cavv) && "".equals(gGate.ucaf)) {
			//3D Secure特店未走ACS認證VISA=>ECI ; MC or JCB=>UCAFIND
			if ( ("1".equals(gGate.eci)) || ("6".equals(gGate.ucafInd)) ) {
				ta.getAndSetErrorCode("1S"); //3D Secure特店未走ACS認證
				return false;
			}
			else {
				return true;
			}
		}
		//kevin:財金規格全部都是UCAF，在這邊分開CAVV
		if ("FISC".equals(gGate.connType) && !"M".equals(gGate.binType) && gGate.ucaf.length()>0) {
			gGate.cavv = gGate.ucaf;
		}

		String slUcaf ="";
		String slCavv = "";
		String slCardNo=gGate.cardNo;
		String slExpireDate = "";
		String slServiceCode = "";
		String slControlByte = "";
		if (!"".equals(gGate.cavv)) {
			if ("7".equals(gGate.ucaf.substring(32, 33)) && "V".equals(gGate.binType)) {
				int nlDateIn  = Integer.parseInt(gb.getJulianDate());
				int nlDateOut = Integer.parseInt(gGate.ucaf.substring(29, 32));
				String slDate = gb.getSysDate().substring(2, 4) + String.format("%03d",nlDateOut);
				gb.showLogMessage("D","CAVV_U3V7_Julian="+nlDateIn+"<"+nlDateOut+"slDate="+slDate);  
				if (nlDateIn < nlDateOut) {
					slDate = String.format("%02d",Integer.parseInt(gb.getSysDate().substring(2, 4)) - 1) + String.format("%03d",nlDateOut);
				}
				String slAmt = String.format("%012d",Integer.valueOf(gGate.ucaf.substring(16, 26), 16));
				gb.showLogMessage("D","CAVV_U3V7_slAmt="+slAmt);  
				gb.showLogMessage("D","CAVV_U3V7_slCardNo before="+slCardNo);  
				slCardNo = slCardNo.substring(0, 15) + "5";
				String slSha = slCardNo + slAmt + gGate.ucaf.substring(26, 29) + slDate;
				gb.showLogMessage("D","CAVV_U3V7_sLSha="+slSha);  
				slCardNo = HpeUtil.sha256(slSha).replaceAll("\\D+","").substring(0, 16);
			}
			gb.showLogMessage("D","AVV_U3V7_slCardNo="+slCardNo);  
			slCavv = gGate.ucaf.substring(9, 12);
			slExpireDate = gGate.ucaf.substring(12, 16);
			slServiceCode = gGate.ucaf.substring(3, 6);
		}
		else {
			slUcaf = HpeUtil.decodedBase642Hex(gGate.ucaf);
			if (slUcaf.isEmpty()) {
				ta.getAndSetErrorCode("QL");
				gGate.cavvResult = "1";
				return false;
			}
			if ("2".equals(gGate.version3Ds)) {
				slCavv = slUcaf.substring(4, 7);
				slServiceCode = slUcaf.substring(7, 10);
				slExpireDate  = gGate.expireDate;

				gb.showLogMessage("D","mastercard avv 2.0 = "+gGate.version3Ds);

			}
			else {				
				slCavv = slUcaf.substring(4, 7);
				slServiceCode = slUcaf.substring(7, 10);
				slExpireDate  = gGate.expireDate;

				gb.showLogMessage("D","mastercard avv version = "+gGate.version3Ds);

			}
		}

		String slCsseccfgCvka = "";
		String slCsseccfgCvkb = "";
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		String  slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);
		switch (slBinType) {
			case "V" : slCsseccfgCvka = gGate.visaCavvA  ; slCsseccfgCvkb = gGate.visaCavvB;  break; /* VISA CAVV CVK*/
			case "M" : slCsseccfgCvka = gGate.masterCavvA; slCsseccfgCvkb = gGate.masterCavvB;  break; /* MASTERCARD CAVV CVK*/
			case "J" : slCsseccfgCvka = gGate.jcbCavvA   ; slCsseccfgCvkb = gGate.jcbCavvB;  break; /* JCB CAVV CVK*/
			default   : break;
		}

//		gb.showLogMessage("D","verify verifyCavv =>" + " CAVV:" + slCavv +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate + ", ServiceCode:"  + slServiceCode );
		String slResult1 = gHsmUtil.hsmCommandCW(slCardNo, slExpireDate, slServiceCode, slCsseccfgCvka, slCsseccfgCvkb);
		gb.showLogMessage("D","verify verifyCavv =>" + " CWCAVV:" + slResult1 + " CAVV:" + slCavv +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate + ", ServiceCode:"  + slServiceCode );
		String slResult = gHsmUtil.hsmCommandCY(slCardNo, slExpireDate, slServiceCode, slCavv, slCsseccfgCvka, slCsseccfgCvkb);
		if ("00".equals(slResult.substring(0,2))) { 

			blResult = true;
			gGate.cavvResult = "2";

			gb.showLogMessage("D","連線HSM驗證CAVV結果=>"+"驗證成功="+slResult);

		}
		else {

			gb.showLogMessage("I","HSM 錯誤! On CAVV error Q3 = "+slResult);
	    	//V1.00.24 免照會VIP，可排除因HSM驗證CAVV失敗而拒絕的交易
			if (gGate.isAuthVip) {
				blResult = true;
				gGate.cavvResult = "2";
				gb.showLogMessage("D","此為免照會VIP，可排除因HSM驗證CAVV失敗而拒絕的交易");
			}
			else {
				ta.getAndSetErrorCode("Q3");
				gGate.tokenData04 = "! 0400020 C           D       ";
				gGate.tokenId04 ="04";
				gGate.cavvResult = "1";
				blResult = false;
			}
		}
		return blResult;
	}
	
	/**
	 * 驗證PVV
	 * @return 如果PVV正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private String verifyPvv() throws Exception {
		String slErrorCode="";
		boolean blRunFunction = false;
		gGate.pinVerified = true;

		if ("N".equals(gb.getIfEnableHsmVerifyPvv()))
			return slErrorCode;//不驗PVV

		if (gGate.isDebitCard)
			return slErrorCode;//不驗PVV
		
		if (gGate.cashAdvanceCounter && gGate.pinBlock.length()<=0) {
			gb.showLogMessage("D","櫃台預借未帶PIN時，不驗PVV=>");
			return slErrorCode;//櫃台預借未帶PIN時，不驗PVV
		}

		//V1.00.03  驗證PIN規則調整，只要有帶PIN就驗，驗不過就驗生日MMDD，排除預借
//		if ((!gGate.isDebitCard) && (gGate.cashAdvance) ) {//不是debit card 而且是 [預借現金(3)] => 才要 往下走
//			blRunFunction = true;
//		}

		//預借現金、預借現金調整(11) and CHANGE ATM PIN(18)
		if ( (gGate.cashAdvance) || (gGate.cashAdjust) || (gGate.changeAtmPin) )//預借現金、預借現金調整 or CHANGE ATM PIN 才要 往下走
			blRunFunction = true;
		
		//高鐵購票交易
//		if ( "4011".equals(gGate.mccCode) && gGate.pinBlock.length()>0 ) {
//			blRunFunction = true;
//		}
		
		if (gGate.pinBlock.length()>0) {
			blRunFunction = true;
		}
		//bL_RunFunction = true;//xyz for test
		if (!blRunFunction)
			return slErrorCode;//不驗PVV

		//虛擬卡並非一定是採購卡
		if (gGate.isVirtualCard) { /*虛擬卡不做檢核*/
			return slErrorCode;//不驗PVV
		}

		if ("WEB".equals(gGate.connType))
			return slErrorCode;//人工授權不驗PVV
		if("NCCC".equals(gGate.connType) && !"ISO01".equals(gGate.bicHead.substring(0, 5))) {
			if (gGate.pinBlock.length()<=0)
				return slErrorCode;
		}
		//V1.00.06 ATM預借現金密碼變更功能開發 
		if (gGate.updateAtmPin) {
			if (!decryptPinBlock()) {
				gGate.newPin1 = gGate.pinBlock;
			}
			return slErrorCode;
		}

		int nlPinErrorCount = ta.getInteger("CrdCardPinErrorCnt");
		if (nlPinErrorCount >= 3) {
			gb.showLogMessage("I","預借密碼錯誤次數超過3次限制 = "+nlPinErrorCount);
//			ta.getAndSetErrorCode("DZ");
			slErrorCode = "DZ";
			gGate.pinVerified = false;
			return slErrorCode;
		}

		String slCardNo=gGate.cardNo;
		String slExpireDate = transExpireDate(gGate.expireDate, false);

		String slServiceCode = gGate.servCode;
		String slPinBlock = gGate.pinBlock;
		
//		boolean bpIsNewCard = gGate.IsNewCard;
		
		String slCsseccfgZpk  = gGate.keysZpk;
		String slPinBlockFormatCode = "01";
		if (gGate.cashAdvanceOnus || gGate.verifyAtmPin) {
			slCsseccfgZpk  = gGate.atmZpk;
			//V1.00.06 ATM預借現金密碼變更功能開發 
			if (decryptPinBlock()) {
				slPinBlock = gGate.newPin1;
				slPinBlockFormatCode = "03";
			}
		}
		String slCsseccfgPvki = gGate.pvki;
		String slCsseccfgPvka = "";
		String slCsseccfgPvkb = "";
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		String  slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);
		switch (slBinType) {
			case "V" : slCsseccfgPvka = gGate.visaPvkA;   slCsseccfgPvkb = gGate.visaPvkB;    break; /* VISA PVK*/
			case "M" : slCsseccfgPvka = gGate.masterPvkA; slCsseccfgPvkb = gGate.masterPvkB;  break; /* MASTERCARD PVK*/
			case "J" : slCsseccfgPvka = gGate.jcbPvkA;    slCsseccfgPvkb = gGate.jcbPvkB;     break; /* JCB PVK*/
			default   : break;
		}
//		String slGatePVV = gGate.pvv;
//		String slDbPvv = ta.getValue("PVV");
//		String slDbOldPvv  = ta.getValue("OLD_PVV");

//		gb.showLogMessage("D","verify verifyPvv => "+"BizId:" + "sL_BizId" + ", PinBlock:" + slPinBlock +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate + ", ServiceCode:"  + slServiceCode );

		String slZpk = "U" + slCsseccfgZpk;
		String slPvk = slCsseccfgPvka + slCsseccfgPvkb;
//		String slPinBlockFormatCode = "01";
		String slPanOrToken = gGate.cardNo.substring(3, 15);
		String slPvki = slCsseccfgPvki;
		String slPVV="";
//		if(bpIsNewCard)
//			slPVV = slDbPvv;
//		else
//			slPVV = slDbOldPvv;
		if (ta.getValue("CardBaseNewPvv").length()>0) {
			slPVV = HpeUtil.transPasswd(1, ta.getValue("CardBaseNewPvv"));
		}
		//kevin:db沒有pvv的資料時，用卡片帶的pvv
		if (slPVV.length() == 0) {
			slPVV = gGate.pvv;
		}
		String slResult = gHsmUtil.hsmCommandEC(slZpk, slPvk, slPinBlock, slPinBlockFormatCode, slPanOrToken, slPvki, slPVV);
		if ("00".equals(slResult.substring(0,2))) {
//			blResult = true;
			gGate.pinVerified = true;
			ta.resetCardPasswdErrCount(gGate.cardNo);

			gb.showLogMessage("D","連線HSM驗證PIN結果=>"+"驗證成功="+slResult.substring(0,2)+" ,驗證結果="+slResult );

		}
		else if ("01".equals(slResult.substring(0,2))){

			gb.showLogMessage("I","連線HSM驗證PIN交易驗證錯誤 =>"+slResult);

			gGate.pinVerified = false;
			slErrorCode = "DX";
		}
		else {

			gb.showLogMessage("I","連線HSM驗證PIN交易驗證其他錯誤 =>"+slResult);

			ta.addCardPasswdErrCount(gGate.cardNo);
//			ta.getAndSetErrorCode("DW");
			gGate.pinVerified = false;
			slErrorCode = "DW";
		}

		return slErrorCode;
	}
	
	/**
	 * 透過HSM 進行 trans ATM PIN
	 * @return 如果 trans ATM PIN 成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean transAtmPin() throws Exception {

		boolean blResult =true;

		if ("N".equals(gb.getIfEnableHsmTransAtmPin()))
			return true;

		if ("WEB".equals(gGate.connType)) { //generate PIN
			return true;
		}

		String slNewPin1 = gGate.newPin1; //G_Gate.newPin1 => q Token06 o

		if (slNewPin1.length()==0) {
			return true;
		}
		
		if (gGate.updateAtmPin && "0202".equals(gGate.mesgType)) {
			gb.showLogMessage("I","Update ATM PIN 的0202沖正交易不須驗證密碼");
			return true;
		}
		
		String slNewPin = "";
//		String slCardNo=gGate.cardNo;
//		String slExpireDate = transExpireDate(gGate.expireDate, false);
//		String slServiceCode = gGate.servCode;

		String slCsseccfgZpk = gGate.keysZpk;
		String slSourcePinBlockFormatCode = "01";
		if ("ATM".equals(gGate.connType)) {
			slCsseccfgZpk = gGate.atmZpk;
			slSourcePinBlockFormatCode = "03";
		}

//		String slSourcePinBlockFormatCode = "01";
		String slAccountNumber = getAccountNumber();
		String slResult = gHsmUtil.hsmCommandJE("U" + slCsseccfgZpk, slNewPin1, slAccountNumber, slSourcePinBlockFormatCode);
		if (!"00".equals(slResult.substring(0,2))) {

			gb.showLogMessage("I","HSM trans ATM PIN驗證失敗 DX"+ slResult);

			ta.getAndSetErrorCode("DX");
			blResult = false;
		}
		else {

			slNewPin = slResult.substring(2, slResult.length());
			if (slNewPin.length()<=0) {

				gb.showLogMessage("I","HSM trans ATM PIN產生失敗 DX"+ slResult);

				ta.getAndSetErrorCode("DX");
				blResult = false;
			}
			gGate.newPinFromHsm = slNewPin; 
			gGate.newPinBlockFromHsm = gGate.newPinFromHsm;
			blResult = true;
		}
		return blResult;
	}

	/**
	 * 透過HSM產生PVV
	 * @return 如果產生PVV成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean atmPvvGen() throws Exception {
		boolean blResult =true;
		if ("N".equals(gb.getIfEnableHsmGenAtmPvv()))
			return true;

		if ("WEB".equals(gGate.connType)) { //generate PIN

			return true;
		}
		
		if (gGate.updateAtmPin && "0202".equals(gGate.mesgType)) {
			gb.showLogMessage("I","Update ATM PIN 的0202沖正交易不須產生PVV");
			return true;
		}

		String slCardNo=gGate.cardNo;
		String slExpireDate = transExpireDate(gGate.expireDate, false);
		String slNewPinBlock = gGate.newPinBlockFromHsm;
		String slNewPvv = "";

		String slCsseccfgPvki = gGate.pvki;		
		String slCsseccfgPvka = "";
		String slCsseccfgPvkb = "";
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		String  slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);
		switch (slBinType) {
			case "V" : slCsseccfgPvka = gGate.visaPvkA;   slCsseccfgPvkb = gGate.visaPvkB;    break; /* VISA PVK*/
			case "M" : slCsseccfgPvka = gGate.masterPvkA; slCsseccfgPvkb = gGate.masterPvkB;  break; /* MASTERCARD PVK*/
			case "J" : slCsseccfgPvka = gGate.jcbPvkA;    slCsseccfgPvkb = gGate.jcbPvkB;     break; /* JCB PVK*/
			default   : break;
		}
		String slPvk = slCsseccfgPvka + slCsseccfgPvkb;
		String slAccountNumber = getAccountNumber();

//		gb.showLogMessage("D","atmPvvGen => "+ "PinBlock:" + slNewPinBlock +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate );

		String slResult = gHsmUtil.hsmCommandDG(slPvk, slNewPinBlock, slAccountNumber, slCsseccfgPvki);


		if (!"00".equals(slResult.substring(0,2))) {

			gb.showLogMessage("I","atmPinGen PVV GEN ERROR DX = " + slResult);

			ta.getAndSetErrorCode("DX");
			blResult = false;
		}
		else {
			slNewPvv = slResult.substring(2, slResult.length());
			gGate.pvv = slNewPvv;
			blResult = true;
		}
		return blResult;
	}
	
	/**
	 * 透過HSM產生PIN
	 * @return 如果產生PIN成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean atmPinGen() throws Exception {
		boolean blResult =true;
		if ("N".equals(gb.getIfEnableHsmGenAtmPvv()))
			return true;

		if ("WEB".equals(gGate.connType)) { //generate PIN
			return true;
		}

		String slCardNo=gGate.cardNo;
		String slExpireDate = transExpireDate(gGate.expireDate, false);
		String slNewPinBlock = "";
		String slCsseccfgPvka = "";
		String slCsseccfgPvkb = "";
		// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
		String  slBinType = ta.getValue("CardBinType").toUpperCase(Locale.TAIWAN);
		switch (slBinType) {
			case "V" : slCsseccfgPvka = gGate.visaPvkA;   slCsseccfgPvkb = gGate.visaPvkB;    break; /* VISA PVK*/
			case "M" : slCsseccfgPvka = gGate.masterPvkA; slCsseccfgPvkb = gGate.masterPvkB;  break; /* MASTERCARD PVK*/
			case "J" : slCsseccfgPvka = gGate.jcbPvkA;    slCsseccfgPvkb = gGate.jcbPvkB;     break; /* JCB PVK*/
			default   : break;
		}
		String slPvk = slCsseccfgPvka + slCsseccfgPvkb;
		String slAccountNumber = getAccountNumber();
		String slPin = ta.getValue("CrdIdNoBirthday").substring(4, 8);

//		gb.showLogMessage("D","atmPvvGen => "+ "PinBlock:" + slNewPinBlock +", CardNo:" +  slCardNo + ", ExpDate:" + slExpireDate );

		String slResult = gHsmUtil.hsmCommandBA(slAccountNumber, slPin);

		if (!"00".equals(slResult.substring(0,2))) {

			gb.showLogMessage("I","atmPinGen PIN GEN ERROR DX = " + slResult);

			ta.getAndSetErrorCode("DX");
			blResult = false;
		}
		else {
			slNewPinBlock = slResult.substring(2, slResult.length());
			gGate.newPinBlockFromHsm = slNewPinBlock;
			blResult = true;
		}
		return blResult;
	}
	
	/**
	 * 從ISO8583中取得Account Number
	 * @return Account Number
	 */
	private String getAccountNumber() {
		return gGate.cardNo.substring(3, 15);
	}
	
	/**
	 * 調整卡片校期
	 * @param spSrcExpireDate 原始卡片效期
	 * @param bpAdjustOrder 調整方式
	 * @return 調整後的效期
	 * 
	 */
	private String transExpireDate(String spSrcExpireDate, boolean bpAdjustOrder) {
		String slResult = "";
		if (bpAdjustOrder)
			slResult = spSrcExpireDate.substring(2,4) + spSrcExpireDate.substring(0,2) ;// 20161014 Howard : gP Mindy and Winson Q׫, dޮĴɡAnഫ
		else
			slResult = spSrcExpireDate;// 20161014 Howard : gP Mindy and Winson Q׫, ǤJHSM ɡAഫ
		return slResult;
	}
	
	/**
	 * 預借現金變更密碼
	 * @return 變更密碼成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean changeAtmPin() throws Exception {

		gb.showLogMessage("I","processpinChange : started");

//		gGate.mccCode = "6011";//

		if  (gGate.isDebitCard) {
			ta.getAndSetErrorCode("B0");
		}

		if ((gGate.pinVerified && "NCCC".equals(gGate.connType)) || (gGate.updateAtmPin)) {
			if (!transAtmPin()) {
				return false;

			}

			if (!atmPvvGen()) {
				return false;
			}

			updateAtmData();  
		}
//		else {
//			ta.addCardPasswdErrCount(gGate.cardNo);
//			ta.getAndSetErrorCode("DX");
//			return false;
//		}

		return true;
	}

	/**
	 * 更改 ATM 相關密碼資料
	 * @return 如果更改 ATM 相關密碼資料成功，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean updateAtmData()  throws Exception{

		boolean blResult = false;
		if (gGate.updateAtmPin && "0202".equals(gGate.mesgType)) {
			gb.showLogMessage("I","Update ATM PIN 的0202沖正交易時，還原PIN與PVV");
			blResult = ta.updateAtmData(false);
		}
		else {
			blResult = ta.updateAtmData(true);
		}
		return blResult;
	}

	/**
	 * V1.00.06 ATM預借現金密碼變更功能開發
	 * @return 解密後的PIN BLOCK
	 * @throws Exception if any exception occurred
	 */
	private boolean decryptPinBlock()  throws Exception{

		boolean blResult =true;
		String slNewPinBlock;
		String slKey = "U"+gGate.atmZek;
		String slIV = HpeUtil.byte2HexStr(HpeUtil.transToEBCDIC("0"+gGate.isoField[37].substring(3)));
		String slResult = gHsmUtil.hsmCommandM2("01", "1", "1", slKey, "", "", slIV, "0010",
				gGate.pinBlock, "00A");

		if (!"00".equals(slResult.substring(0,2))) {

			gb.showLogMessage("I","decrypt PinBlock ERROR DX = " + slResult);

			ta.getAndSetErrorCode("DX");
			blResult = false;
		}
		else {
			slNewPinBlock = slResult.substring(22, 38);
			gGate.newPin1 = slNewPinBlock;
			blResult = true;
		}
		return blResult;
	}
}
