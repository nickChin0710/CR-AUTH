/**
 * Proc 處理VISA DEBIT交易連線到主機的流程 
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       Proc 處理VISA DEBIT交易連線到主機的流程        *
 * 2022/03/09  V1.00.01  Kevin       沖正只要有回應就算成功                         *
 * 2022/03/17  V1.00.02  Kevin       麗花襄理要求2447交易需帶入原始RRN               *
 * 2022/04/06  V1.00.03  Kevin       麗花襄理要求2442悠遊卡加值交易需帶入端末設備日期時間  *
 * 2022/04/11  V1.00.04  Kevin       弘奇說VD沖正主機回應失敗不能視為成功              *
 * 2022/04/13  V1.00.05  Kevin       主機IMS Connect出現exception時，需要沖正處理，  *
 *                                   如主機回覆拒絕時，仍要產生沖正異常檔提供後續處理      *
 * 2022/05/18  V1.00.06  Kevin       自動授權啟動之線上沖正時原圈存金額等於charge amount*
 *                                   之圈存金額                                  * 
 * 2023/04/13  V1.00.42  Kevin       授權系統與DB連線交易異常時的處理改善方式             *
 * 2023/12/18  V1.00.63  Kevin       VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯 *
 * 									 費」：2440一般圈存一律帶入ID傳送到IMS             *
 ******************************************************************************
 */

package com.tcb.authProg.process;


import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;
import net.sf.json.JSONObject;

public class ProcVisaDebitToIms extends AuthProcess {

	public ProcVisaDebitToIms(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gGb.showLogMessage("I","ProcVisaDebitToIms : started");

	}
	
	public boolean procVdToIms(String spHeadMsgType) throws Exception{
		//圈存與解圈
		boolean blResult = true;
		String slHeadMsgType = spHeadMsgType;
		String slHeadPcode   = "    ";
		String slBodyFcode   = "  ";
		String slHeadTxType  = " ";
		String slBodyTxAmt   = String.format("%010d", (int)gGate.isoFiled4Value);
		String slBodyLockAmt = String.format("%010d", (int)gGate.lockAmt);
		String slBodyOriAmt  = String.format("%010d", (int)gGate.oriLockAmount);

		if (gGate.easyAutoloadVd) {
			slHeadPcode = "2442";
			slBodyFcode = "V2";
			slHeadTxType = "6";	
		}
		else if (gGate.refund){
			slHeadPcode = "VDRT";
			slBodyFcode = "VR";
			if ("WEB".equals(gGate.connType)) {
				slHeadTxType = "B";
				slBodyTxAmt   = String.format("%010d", 0);
			}
			else {
				slHeadTxType = "7";
			}
		}
		else if (gGate.preAuthComp){
			slHeadPcode = "2447";
			slBodyFcode = "V7";
			slHeadTxType = "8";
		}
		else if (gGate.purchAdjust) {
			slHeadPcode = "2447";
			slBodyFcode = "V7";
			slHeadTxType = "8";
		}
		else {
			slHeadPcode = "2440";
			slBodyFcode = "V0";
			if ("WEB".equals(gGate.connType)) {
				slHeadTxType = "A";
//				slHeadTxType = "6"; //配合測試先將人工圈存模擬線上交易
				slBodyTxAmt   = String.format("%010d", 0);
			}
			else {
				slHeadTxType = "6";
			}
		}
		
		if ("WEB".equals(gGate.connType)) {
			slHeadTxType = "A";
		}

		String slHeadDest = "ATM ";
		String slCurDateTime = HpeUtil.getCurDateTimeStr(false, false);
		String slHeadDate = slCurDateTime.substring(0, 8); //ISO_FIELD[7 ]      : 0515055751
		String slHeadTime = slCurDateTime.substring(8, 14); //ISO_FIELD[11]      : 025116
		String slVdTxnSeqNo = "";
		String slVdOriSeqNo = "";
		String slBodyRefNo  = gGate.isoField[37];
		
		if (gGate.imsLockSeqNo.length() > 0) {
			if ((gGate.preAuthComp) || (gGate.refund) || (gGate.purchAdjust)) {
				slVdTxnSeqNo     = HpeUtil.fillCharOnLeft(gTa.getNextSeqVal("SEQ_SEND_IBMSEQNO"), 6, "0");
				gGate.vdTxnSeqNo = "CRD0" + slVdTxnSeqNo;
				slVdOriSeqNo     = "CRD0" + gGate.imsLockSeqNo.substring(4,10);
				gGate.imsLockSeqNo = slCurDateTime.substring(4, 8) + slVdTxnSeqNo;
				slBodyRefNo      = gGate.imsOriRefNo;
			}
			else {
				slVdTxnSeqNo     = gGate.imsLockSeqNo.substring(4,10);
				gGate.vdTxnSeqNo = "CRD0" + slVdTxnSeqNo;
				slVdOriSeqNo     = gGate.vdTxnSeqNo;
			}
		}
		else {
			slVdTxnSeqNo     = HpeUtil.fillCharOnLeft(gTa.getNextSeqVal("SEQ_SEND_IBMSEQNO"), 6, "0");
			gGate.vdTxnSeqNo = "CRD0" + slVdTxnSeqNo;
			slVdOriSeqNo     = gGate.vdTxnSeqNo;
			gGate.imsLockSeqNo = slCurDateTime.substring(4, 8) + slVdTxnSeqNo;
		}
		gGb.showLogMessage("D","G_Gate.imsLockSeqNo = "+ gGate.imsLockSeqNo);
		gGb.showLogMessage("D","G_Gate.vdTxnSeqNo = "+ gGate.vdTxnSeqNo);
		gGb.showLogMessage("D","G_Gate.imsOriRefNo = "+ gGate.imsOriRefNo);

//		String slVdTxnSeqNo = HpeUtil.fillCharOnLeft(gTa.getNextSeqVal("SEQ_SEND_IBMSEQNO"), 6, "0");
//		gGate.vdTxnSeqNo = "CRD0"+ slVdTxnSeqNo;
//		gGb.showLogMessage("I","G_Gate.vdTxnSeqNo = "+ gGate.vdTxnSeqNo);
		String slTraceNo = slHeadMsgType + slHeadPcode + slHeadDest + gGate.vdTxnSeqNo;
//		String slVdOriSeqNo  = "";
//		if (gGate.imsLockSeqNo.length() <= 0) {
//			gGate.imsLockSeqNo = gGate.vdTxnSeqNo;
//			slVdOriSeqNo  = gGate.imsLockSeqNo;
//		}
//		else {
//			slVdOriSeqNo  = "CRD0" + gGate.imsLockSeqNo.substring(4,10);
//		}
		
		String slHeadTermId  = "CLIENTID";
		String slHeadType    = "1";
		String slHeadRtnCode = "    ";
		String slBodyTrack2  = "                                     ";
		if (gGate.isoField[35].length() > 0) {
			if (gGate.isoField[35].length() >= 37) {
				slBodyTrack2 = gGate.isoField[35].substring(0, 37);
			}
			else {
				slBodyTrack2 = slBodyTrack2.substring(gGate.isoField[35].length(), 37); 
				slBodyTrack2 = gGate.isoField[35].substring(0, gGate.isoField[35].length()) + slBodyTrack2;
			}
		}
		else {
			slBodyTrack2 = gGate.cardNo +"="+ gGate.expireDate + "                "; 
		}
		gGb.showLogMessage("D","@@@@ims_sL_BodyTrack2="+slBodyTrack2);
		String slBodyBankAct = gTa.getValue("DbcCardAcctNo");
		String slBodyBankSeq = gTa.getValue("DbcCardRefNum");
//		String slVdOriSeqNo  = gGate.imsLockSeqNo;
		String slBodyTrace   = gGate.isoField[11];
		String slBodyTxTime  = gGate.isoField[12];
		String slBodyTxDate  = gGate.isoField[13];
		//V1.00.03 麗花襄理要求2442悠遊卡加值交易需帶入端末設備日期時間
		if (gGate.easyAutoloadVd && gGate.f58T80.length() == 14) {
			slBodyTxTime = gGate.f58T80.substring(8, 14);
			slBodyTxDate = gGate.f58T80.substring(4, 8);
		}
//		String slBodyRefNo   = gGate.isoField[37];
		String slBodyScvsNo  = "                    ";
		if (gGate.easyAutoloadVd ) {
			slBodyScvsNo = slBodyScvsNo.substring(gGate.isoField[2].length(), 20); 
			slBodyScvsNo  = gGate.isoField[2].substring(0, gGate.isoField[2].length())+slBodyScvsNo;
		}
		//V1.00.63 VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯費」：2440一般圈存一律帶入ID傳送到IMS
		String slBodySpecificFlag = " ";
		String slBodySpecificId = "";
		if ("2440".equals(slHeadPcode)) {
			slBodySpecificId = gGate.idNo;
			if (checkSpecificTxn()) {
				slBodySpecificFlag = "Y";
				gGb.showLogMessage("D","TXN for Specific, ID="+slBodySpecificId+";merchant name="+gGate.merchantName+";Mcc="+gGate.mccCode);
			}
		}
		String  slData = slHeadPcode+slHeadDest+gGate.vdTxnSeqNo+
				slVdTxnSeqNo+slHeadDate+slHeadTime+slHeadTxType+slHeadTermId+slHeadType+
				slHeadRtnCode+" "+slBodyFcode+slBodyTrack2+slBodyTxAmt+slBodyLockAmt+
				slBodyOriAmt+slBodyBankAct+slBodyBankSeq+slVdOriSeqNo+slBodyTrace+
				slBodyTxTime+slBodyTxDate+slBodyRefNo+slBodyScvsNo+slBodySpecificFlag;
		
		String  slImsData = " " + slHeadMsgType + slData;
		//V1.00.42 授權系統與DB連線交易異常時的處理改善方式。如為VD圈存交易則再寫入VD沖正異常處理
		if ("0200".equals(slHeadMsgType)) {
			gGate.vdImsLog = " " + "0202" + slData;
		}
		
		boolean blInsert = false;
		//V1.00.63 VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯費」：2440一般圈存一律帶入ID傳送到IMS
		if (sendVdToIms(slTraceNo, slImsData+slBodySpecificId)) {
			if (!gGate.readFromImsSuccessful) {
				blResult = false;
				if (gGate.vdResponseTimeOut) {
					gTa.getAndSetErrorCode("2F"); //VD交易，IMS主機回應time out
				    gGb.showLogMessage("I","@@@rtnImsVd_rtnImsVd TIMEOUT = 2F @@@="+gGate.vdTxnSeqNo);
				}
				else {
					gTa.getAndSetErrorCode("2H"); //VD交易，IMS主機回應格式錯誤
				    gGb.showLogMessage("I","@@@rtnImsVd_rtnImsVd TIMEOUT = 2H @@@="+gGate.vdTxnSeqNo);
				}
				if ("0200".equals(slHeadMsgType)) {
					slHeadMsgType =  "0202";
					//自動授權啟動之線上沖正時原圈存金額等於charge amount之圈存金額
					slData = slHeadPcode+slHeadDest+gGate.vdTxnSeqNo+
							slVdTxnSeqNo+slHeadDate+slHeadTime+slHeadTxType+slHeadTermId+slHeadType+
							slHeadRtnCode+" "+slBodyFcode+slBodyTrack2+slBodyTxAmt+slBodyLockAmt+
							slBodyLockAmt+slBodyBankAct+slBodyBankSeq+slVdOriSeqNo+slBodyTrace+
							slBodyTxTime+slBodyTxDate+slBodyRefNo+slBodyScvsNo+" ";
					slImsData = " " + slHeadMsgType + slData;
					slTraceNo = slHeadMsgType + slHeadPcode + slHeadDest + gGate.vdTxnSeqNo;
					//麗花襄理說人工的不能做線上reversal，只針對2440-6和2442-6，其餘沖正都改成批次處理
					if (slHeadTxType.contentEquals("6")) { 
						sendVdToIms(slTraceNo, slImsData);
						if (!gGate.vdResponseSuccessful) {
							gGb.showLogMessage("I","@@@@ IMS REVERSAL DATA = "+slImsData);
							blInsert = true;
						}
					}
					else {
						blInsert = true;
					}

				}
				else {
					blInsert = true;
					blResult = false;
				}
			}
			else {
				blResult = true;
				//針對2447-8強圈失敗時，仍要送通知批次處理
				if (slHeadTxType.contentEquals("8")) { 
					if (!gGate.vdResponseSuccessful) {
						gGb.showLogMessage("I","@@@@ IMS 強圈交易主機回應非成功時，仍要送通知批次處理 DATA = "+slImsData);
						blInsert = true;
					}
				}			
				//V1.00.05 主機IMS Connect出現exception時，需要沖正處理，如主機回覆拒絕時，仍要產生沖正異常檔提供後續處理
				else if ("0202".equals(slHeadMsgType)) {
					if (!gGate.vdResponseSuccessful) {
						gGb.showLogMessage("I","@@@@ IMS 財金沖正交易主機回應非成功時，仍要送通知批次處理 DATA = "+slImsData);
						blInsert = true;
					}
				}
			}
		}
		else {
			gTa.getAndSetErrorCode("2D");	//VISA DEBIT 送主機錯誤		
		    gGb.showLogMessage("I","@@@rtnImsVd_rtnImsVd error = 2D @@@="+gGate.vdTxnSeqNo);
			blResult = false;
			//針對2447-8強圈失敗時，仍要送沖正批次處理
			if (slHeadTxType.contentEquals("8")) { 
				gGb.showLogMessage("I","@@@@ IMS 強圈交易傳送主機失敗時，仍要送沖正批次處理 DATA = "+slImsData);
				blInsert = true;
			}
			else if (slHeadTxType.contentEquals("6")) { 
				if (!gGate.vdResponseSuccessful) {
					gGb.showLogMessage("I","@@@@ IMS 一般交易主機回應非成功時，仍要送沖正批次處理 DATA = "+slImsData);
					blInsert = true;
				}					
			}
		}

		gTa.insertImsEven(slImsData, blInsert); //新增VD交易之IMS LOG，true表示需要異常沖正處裡
		return blResult;
	}
	private boolean sendVdToIms(String spTraceNo, String spImsData) throws Exception{
		gGate.readFromImsSuccessful = true;
		gGate.vdResponseSuccessful  = false;
		gGate.vdResponseTimeOut     = false;
		boolean blResult = true;
		gGb.showLogMessage("D","@@@@IMS_VD_TXN_1=@@@@"+spImsData);
		byte[] lDataByteAry = HpeUtil.transByCode(spImsData, "Cp1047");
		gGb.showLogMessage("D","@@@@IMS_VD_TXN_2=@@@@"+lDataByteAry);
		String reqIms = HpeUtil.encoded2Base64(lDataByteAry);
		gGb.showLogMessage("D","@@@@IMS_VD_TXN_3=@@@@"+reqIms);
				
		JSONObject jsonObjectVdTxn = new JSONObject();
		jsonObjectVdTxn.put("seqNo", spTraceNo);
		jsonObjectVdTxn.put("message", reqIms);
		String reqImsVd = jsonObjectVdTxn.toString();
	    gGb.showLogMessage("D","@@@reqImsVd@@@="+reqImsVd);

		String applJson = "application/json";
	    String rtnIms = "";
	    String rtnImsVd = "";
	    String imsUrl = gGb.getSgImsTokenReqUrl() + "ims-auth/vdTxn";
	    if ("2".equals(gGb.getsgImsIndicate())) {
		    imsUrl = gGb.getSgImsVdTxnUrl() + "ims-auth/vdTxn";
	    }

		try {
			int nlTimeOutSec = 3;
		    gGb.showLogMessage("D","@@@rtnImsVd set time out sec.@@@="+nlTimeOutSec);
			rtnIms = HpeUtil.curlToken(imsUrl, applJson,"Bearer "+gGb.getTokenIms(), reqImsVd, nlTimeOutSec);
			if ("ERROR".equals(rtnIms)) {
				gGb.showLogMessage("E","IMS VD Connect failed=" + rtnIms);
				return false;
			}
		    gGb.showLogMessage("D","@@@rtnImsVd_rtnIms@@@="+rtnIms);
		    if (!"TIMEOUT".equals(rtnIms)) {
		    	JSONObject jsonIn = JSONObject.fromObject(rtnIms); 
		    	gGb.showLogMessage("D","@@@rtnImsVd_jsonIn@@@="+jsonIn);
		    	rtnImsVd = HpeUtil.decoded2Ascii(jsonIn.getString("message"));     
		    	gGb.showLogMessage("D","@@@rtnImsVd_rtnImsVd@@@="+ rtnImsVd);
		    }

		    
		} catch (Exception e) {   //VISA DEBIT 送主機錯誤	
			//kevin:測試時先改成true，並且不要直接return，傳送一個假的rtnImsVd ###top
		    gGb.showLogMessage("I","@@@傳送主機錯誤@@@="+ rtnImsVd+ e);
			blResult = false;
		    return blResult;
		    
//			blResult = true;
//			if ("0202".equals(sP_ImsData.substring(1, 5))) {
//				rtnImsVd="11111111 02122440ATM 0060000034884857202009120017256CLIENTID19999 ";//正常回覆
//				rtnImsVd="1 ";//正常回覆
//
//			}
//			else {
////				rtnImsVd="11111111 02102440ATM 0060000034884857202009120017256CLIENTID10000 ";//正常回覆
////				rtnImsVd="11111111 02102440ATM 0060000034884857202009120017256CLIENTID19999 ";//主機回錯誤
//				rtnImsVd="1";//格式錯誤
//			}
			//kevin:測試時先改成true，並且不要直接return，傳送一個假的rtnImsVd ###bottom
		}
		
		boolean blSucFlag = false;
		
//		if (!"".equals(rtnImsVd.substring(9,13)) && !"".equals(rtnImsVd.substring(61,65))) { 
		if (rtnImsVd.length() > 0) { 
			blSucFlag = true;
			if (rtnImsVd.length() > 65 && rtnImsVd.substring(21,31).equals(gGate.vdTxnSeqNo)) { 
				if ("0210".equals(rtnImsVd.substring(9,13))) {
					gGate.bankBit39Code = rtnImsVd.substring(61,65);
					gGb.showLogMessage("I","@@@@rtnImsVd_0210_REPS="+gGate.bankBit39Code);
					if (("0000").equals(gGate.bankBit39Code)) {
						gGate.vdResponseSuccessful = true;
					}
					return blResult;
				}
				else if ("0212".equals(rtnImsVd.substring(9,13))) {
					gGate.bankBit39Code = rtnImsVd.substring(61,65);
					gGb.showLogMessage("I","@@@@rtnImsVd_0212_REPS="+gGate.bankBit39Code);
					if (("0000").equals(gGate.bankBit39Code)) { //v1.00.01  麗花襄理說：沖正只要有回應就算成功(V1.00.04 弘奇說VD沖正主機回應失敗不能視為成功)
						gGate.vdResponseSuccessful = true;
					}		
					return blResult;
				}
			}
		}
		
		gGate.readFromImsSuccessful = false;
		if (!blSucFlag) {/** VISA DEBIT 主機回應time out **/
			gGate.vdResponseTimeOut = true;
		}		

	    return blResult;

		//up, new socket to send to IMS
	}
	/**
	 * 檢查VD交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯費」
	 * V1.00.63 VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯費」：2440一般圈存一律帶入ID傳送到IMS
	 * @return
	 * @throws Exception
	 */
	private boolean checkSpecificTxn() {
		boolean result = false;
		if ("T".equals(gGate.areaType)) {
			if ("9311".equals(gGate.mccCode)) {
				return true;
			}
			if ("9399".equals(gGate.mccCode)) {
				if ("455742".equals(gGate.isoField[32])) { //FISC
					if (gGate.merchantName.toUpperCase().indexOf("TAIWAN POWER COMPANY") >= 0) {   //台灣電力公司
						return true;
					}
				}
				if ("493817".equals(gGate.isoField[32]) ) { //NCCC
					if (gGate.merchantName.toUpperCase().indexOf("TAIWAN WATER CORPORATI") >= 0    //台灣省自來水公司
						|| gGate.merchantName.toUpperCase().indexOf("TAIPEI WATER DEPARTMEN") >= 0 //台北市自來水公司
						|| gGate.merchantName.toUpperCase().indexOf("POLICE") >= 0     //警察局
						|| gGate.merchantName.toUpperCase().indexOf("MOTOR") >= 0      //監理所
						|| gGate.merchantName.toUpperCase().indexOf("HOUSEHOLD") >= 0  //戶政事務所
						|| gGate.merchantName.toUpperCase().indexOf("TRAFFIC") >= 0    //交通裁決所
						|| gGate.merchantName.toUpperCase().indexOf("LOCAL TAX") >= 0  //地方財政稅務局
						|| gGate.merchantName.toUpperCase().indexOf("TAXATION") >= 0)  //財政部稅務入口網、財政部國稅局、地方財政稅務局
					{
						return true;
					}
				}
			}
			if ("4900".equals(gGate.mccCode)) {
				if ("455742".equals(gGate.isoField[32])) { //FISC
					if (gGate.merchantName.toUpperCase().indexOf("TAI WAN ZI LAI SHUI GU") >= 0     //台灣省自來水公司
						|| gGate.merchantName.toUpperCase().indexOf("TAIPEI WATER DEPARTMEN") >= 0  //台北市自來水公司
						|| gGate.merchantName.toUpperCase().indexOf("TAIWAN POWER COMPANY") >= 0    //台灣電力公司
						|| gGate.merchantName.toUpperCase().indexOf("YANG MING SHAN WA SI G") >= 0  //陽明山瓦斯公司
						|| gGate.merchantName.toUpperCase().indexOf("XIN HAI WA SI GU FEN Y") >= 0) //新海瓦斯公司
					{
						return true;
					}
				}
				if ("493817".equals(gGate.isoField[32]) ) { //NCCC
					if (gGate.merchantName.toUpperCase().indexOf("HSIN KAO GAS CO,LTD") >= 0)        //欣高石油氣股份有限公司
					{
						return true;
					}
				}
				if ("442511".equals(gGate.isoField[32]) ) { //台北富邦銀行
					if (gGate.merchantName.toUpperCase().indexOf("TAIPEI WATER DEPARTMEN") >= 0)     //台北市自來水公司
					{
						return true;
					}
				}
			}
		}
		return result;			
	}

}