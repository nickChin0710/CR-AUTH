/**
 * 授權使用FISC TOKEN格式轉換物件 - 取消
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
 * 2021/02/08  V1.00.00  Kevin       此物件與TokenObject一樣，故取消               *
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

//import java.util.ArrayList;

//
//import com.tcb.authProg.process.AuthTxnGate;
//import com.tcb.authProg.util.HpeUtil;

public class TokenObjectFisc {

//	String tokenData = "", tokenError = "", reserv = "";
//	String tokenId = "";
//	String zeros = "", spaces = "";
//	int tokenCount = 0, tokenTotalLength = 0, tokenLength = 0;
//
//	AuthTxnGate gate = null;
//
//	/* Token QR */
//	String tokenIdQR = "";
//	String additionalData1 = "",additionalData2 = "";
//			
//	/* Token S8 */
//	String tokenIdS8 = "";
//	String acctNumInd = "", acctNum = "", expDat = "", acVeryRslt = "", fraudChkRslt = "", filler = "";
//
//	/* Token CZ */
//	String tokenIdCZ = "";
//	String tokenCzAtc = "", tokenCzFormFactrInd = "", tokenCzAtcValidInd = "", tokenCzAtcDisCr = "";
//	
//	/* Token F1 */
//	String tokenIdF1 = "";
//	String tokenF1SchemeMerchId = "", tokenF1CrdHldrAuthnInd = "", tokenF1Filler = "";
//
//	
//	/* Token 04 */
//	String tokenId04 = "";
//	String errorflg = "", cvv2Resut = "", compTk2Data = "";
//
//	/* Token Q8 */
//	String tokenIdQ8 = "";
//
//	/* Token Q9 */
//	String tokenIdQ9 = "";
//	String visaDevTyp="", visaChipTxnInd="", visaMsgRsnCde="", visaFiller="";
//	String masterDevTyp="", masterAdviceRsnCde="", masterAdvcDetlCde="", masterAuthAgentIdCde="", masterOnBehalf="", masterFiller="";
//	
//	/* Token 06 */
//	String tokenId06 = "";
//	String newPinFrmt = "", newPinOfst = "", pinCnt = "", nwePinSize = "", newPin1 = "", newPin2 = "",
//			ncccStandinInd = "", pvvOnCardFlg = "";
//
//	/* Token 23 */
//	String tokenId23 = "";
//	String formatCode = "", pan = "", fieldSep = "", cardHolderName = "", expirationDate = "";
//	String serviceCode = "", pvki = "", pvv = "", discretionData = "", cvvToken23 = "", endSentinel = "";
//
//	/* Token 25 */
//	String tokenId25 = "";
//	String tranFee = "", origFee = "";
//
//	/* Token B2 */
//	String tokenIdB2 = "";
//
//	/* Token B3 */
//	String tokenIdB3 = "";
//	String dfName = "";
//
//	/* Token B4 */
//	String tokenIdB4 = "";
//	String ptSrvEntryMode = "", lastEmvStat = "", dataSuspect = "", devInfoCamFlg = "", arqcVerify = "";
//
//	/* Token B5 */
//	String tokenIdB5 = "";
//	String visaAddlData = "";
//
//	/* Token B6 */
//	String tokenIdB6 = "";
//	String issscriptData = "";
//
//	/* Token BJ */
//	String tokenIdBJ = "";
//	String numIssScriptRslt = "";
//	String[] issScriptProcRslt = new String[8];
//	String[] issuerScriptSeq = new String[8];
//	String[] issScriptId = new String[8];
//
//	/* Token C0 */
//	String tokenIdC0 = "";
//	String cvdFldPresent = "", cavvResult = "", cvdFld = "";
//
//	/* Token C4 */
//	String tokenIdC4 = "";
//	String termAttendInd = "", termLocInd = "", chPresetInd = "", crdPresetInd = "", crdCaptrInd = "", txnStatInd = "";
//	String txnSecInd = "", chActvtInd = "", termInputCAP = "", txnRtnInd="";
//
//	/* Token C5 */
//	String tokenIdC5 = "";
//	String merchantId = "", storeId = "", transId = "", referenceId = "", goodsId = "", personal = "", pin = "", ecFlag = "";
//	
//	/* ���� */
//	String orderNumI = "";
//	/* ���Q */
//	String settleFlg = "", orderNum1 = "";
//
//	/* Token C6 */
//	String tokenIdC6 = "";
//
//	/* Token CH */
//	String tokenIdCH = "",
//	tokenChRespSrcRsnCde4Visa = "", 	tokenChFilter14Visa  = "", tokenChRecurPmntInd4Visa  = "",
//	tokenChFilter24Visa  = "", tokenChRvslRsnInd4Visa  = "", 	tokenChFilter34Visa  = "",
//	tokenChAuthMsgInd4Visa  = "", tokenChTermTyp4Visa  = "", 	tokenChFilter44Visa  = "";
//	
//	String tokenChFilter14Master="",tokenChRecurPmntInd4Master  = "",tokenChFilter24Master="",
//			tokenChPmntTypInd4Master="", tokenChFilter34Master="", tokenChRvslRsnInd4Master  = "",
//			tokenChFilter44Master="", tokenChAuthMsgInd4Master="",tokenChTermTypMaster="",tokenChFilter54Master=""; 
//
//	
//	/* Token CE */
//	String tokenIdCE = "";
//	String authnIndFlg = "";
//
//	/* Token CI */
//	String tokenIdCI = "";
//	String mcElecAccptInd = "";
//
//	/* Token F4 */
//	String tokenIdF4 = "";
//	String walletIndFlg = "", walletIndData = "", tokenF4Filler = "";
//	/* Token Q2 */
//	String tokenIdQ2 = "";
//	String offLineInd = "", terminalSerNum = "", authRespCode = "", iCCAppVersion = "";
//
//	/* Token Q3 */
//	String tokenIdQ3 = "";
//	String jcbStipInst = "", jcbStipReason = "", jcbStipRjeReason = "";
//
//	/* Token W7 */
//	String tokenIdW7 = "";
//	String chSerNumber = "";
//
//	/* Token W8 */
//	String tokenIdW8 = "";
//	String mchtSerNumber = "";
//
//	/* Token WB */
//	String tokenIdWB = "";
//	String bnetEcCertRqst = "", bnetEcCertResp = "";
//
//	/* Token WV */
//	String tokenIdWV = "";
//	String visaEcCertRqst = "";
//
//	public void decodeTokenData(AuthTxnGate gate) {
//		this.gate = gate;
//
//		if (gate.bicHead.substring(3, 5).equals("01")) {
//			tokenData = gate.isoField[126];
//			if (tokenData.equals(""))
//				tokenData = gate.isoField[63];
//
//		} else
//			tokenData = gate.isoField[63];
//
//		/*
//		 * if ( gate.bicHead.substring(3,5).equals("01") &&
//		 * gate.isoField[126].substring(0,1).equals("&") &&
//		 * gate.isoField[126].length() > 20 ) { tokenData = gate.isoField[126];
//		 * } else { tokenData = gate.isoField[63]; }
//		 */
//
//		tokenCount = Integer.parseInt(tokenData.substring(2, 7));
//		tokenTotalLength = Integer.parseInt(tokenData.substring(7, 12));
//		tokenData = tokenData.substring(12);
//
//		while (tokenData.length() > 10) {
//			tokenId = tokenData.substring(2, 4);
//			if (tokenId.trim().equals(""))
//				break;
//
//			// CE00030 01jAoHnaRTHgLDCBgAAAAnBRUAAAA=!
//			tokenLength = Integer.parseInt(tokenData.substring(4, 9));
//			tokenData = tokenData.substring(10, tokenData.length());
//
//			if (tokenId.equals("04")) {
//				decodeToken04();
//			} else if (tokenId.equals("06")) {
//				if (tokenLength==52) {
//					tokenLength = 54;
//					tokenData = tokenData + "00";//Howard(20190730) : ATM 只會傳入 52 bytes，所以補上 00，使長度變成 54 bytes
//					gate.bgToken06RealLengthIs52 = true;
//				}
//				decodeToken06();
//			} else if (tokenId.equals("23")) {
//				decodeToken23();
//			} else if (tokenId.equals("25")) {
//				decodeToken25();
//			} else if (tokenId.equals("B2")) {
//				decodeTokenB2();
//			} else if (tokenId.equals("B3")) {
//				decodeTokenB3();
//			} else if (tokenId.equals("B4")) {
//				decodeTokenB4();
//			} else if (tokenId.equals("B5")) {
//				decodeTokenB5();
//			} else if (tokenId.equals("B6")) {
//				decodeTokenB6();
//			} else if (tokenId.equals("BJ")) {
//				decodeTokenBJ();
//			} else if (tokenId.equals("C0")) {
//				decodeTokenC0();
//			} else if (tokenId.equals("C4")) {
//				decodeTokenC4();
//			} else if (tokenId.equals("C5")) {
//				decodeTokenC5();
//			} else if (tokenId.equals("CH")) {
//				decodeTokenCH();
//				
//			} else if (tokenId.equals("C6")) {
//				decodeTokenC6();
//			} else if (tokenId.equals("CE")) {
//				decodeTokenCE();
//			} else if (tokenId.equals("CI")) {
//				decodeTokenCI();
//			} else if (tokenId.equals("F4")) {
//				decodeTokenF4();
//			} else if (tokenId.equals("Q2")) {
//				decodeTokenQ2();
//			} else if (tokenId.equals("Q3")) {
//				decodeTokenQ3();
//			} else if (tokenId.equals("W7")) {
//				decodeTokenW7();
//			} else if (tokenId.equals("W8")) {
//				decodeTokenW8();
//			} else if (tokenId.equals("WB")) {
//				decodeTokenWB();
//			} else if (tokenId.equals("WV")) {
//				decodeTokenWV();
//			} else if (tokenId.equals("S8")) {
//				decodeTokenS8();
//			}
//			else if (tokenId.equals("Q8")) {
//				decodeTokenQ8();
//			}
//			else if (tokenId.equals("Q9")) {
//				decodeTokenQ9();
//			}
//			else if (tokenId.equals("QR")) {
//				decodeTokenQR();
//			}			
//			else if (tokenId.equals("F1")) {
//				decodeTokenF1();
//			}
//			else if (tokenId.equals("CZ")) {
//				decodeTokenCZ();
//			}
//
//			if (tokenData.length() > tokenLength) {
//				tokenData = tokenData.substring(tokenLength, tokenData.length());
//			} else {
//				tokenData = "";
//			}
//		}
//	}
//
//	public void decodeTokenS8() {
//		gate.tokenIdS8 = tokenId;
//		tokenIdS8 = tokenId;
//		acctNumInd = tokenData.substring(0, 1);
//		acctNum = tokenData.substring(1, 20);
//		expDat = tokenData.substring(20, 24);
//		acVeryRslt = tokenData.substring(24, 26);
//		fraudChkRslt = tokenData.substring(26, 28);
//		filler = tokenData.substring(28, 30);
//
//		gate.tokenS8AcctNum = acctNum; // 代碼化交易會用到
//		gate.tokenS8AcVeryRslt = acVeryRslt;
//		gate.tokenS8FraudChkRslt = fraudChkRslt;
//		gate.tokenS8Filler = filler;
//		gate.tokenS8ExpDat = expDat;
//		gate.tokenS8AcctNumInd = acctNumInd;
//		return;
//	}
//
//	private String getVarTokenData(String sPTokenData) {
//		String sLObjId = sPTokenData.substring(0, 2);
//		int nLObjLength = Integer.parseInt(sPTokenData.substring(2, 5));
//		
//		int nLObjDataEndPos = 5+nLObjLength;
//		String sLResult = sLObjId + HpeUtil.fillZeroOnLeft(""+nLObjLength, 3) + sPTokenData.substring(5, nLObjDataEndPos);
//		
//		return sLResult;
//		
//	}
//	public void decodeTokenQR() {
//		gate.tokenIdQR = tokenId;
//		tokenIdQR = tokenId;
//		
//		//tokenData=> "010250132450928     73500005  02025735000050132450928       "
//		//System.out.println("------------------Token QR data=>" +tokenData + "---");
//		//Howard: 還要確認 要怎麼 拆...
//	
//		String sLTmpData = tokenData;
//		additionalData1 = getVarTokenData(sLTmpData);
//		
//		
//		sLTmpData = tokenData.substring(additionalData1.length(), tokenData.length());
//		additionalData2 = getVarTokenData(sLTmpData);
//		
//		
//
//		gate.tokenQrAdditionalData1 = additionalData1;
//		gate.tokenQrAdditionalData2 = additionalData2;
//		
//		return;
//	}
//	
//	private String getTagData(String sPTokenData, ArrayList<TagObject> pTargetTagObjectAryList) {
//
//			String sLResult = "";
//			String sLObjId="", sLObjData="";
//			int nLObjLength=0;
//			
//			sLObjId = sPTokenData.substring(0, 2);
//			nLObjLength = Integer.parseInt(sPTokenData.substring(2, 5));
//			
//			int nLObjDataEndPos = 5+nLObjLength;
//			sLObjData = sPTokenData.substring(5, nLObjDataEndPos);
//			
//			sLResult = sPTokenData.substring(nLObjDataEndPos, sPTokenData.length());
//			
//			TagObject lTagObject = new TagObject();
//			lTagObject.setObjId(sLObjId);
//			lTagObject.setObjData(sLObjData);
//			pTargetTagObjectAryList.add(lTagObject);
//			
//			if ("Q9".equals(sLObjId))
//				gate.tokenQ8TagQ9 = sLObjData;//Token Status
//			else if ("QA".equals(sLObjId))
//				gate.tokenQ8TagQA = sLObjData;//OTP authentication indicator flag： 01=Get Cardholder Verification Method，02=Send OTP(passcode)
//			else if ("27".equals(sLObjId))
//				gate.tokenQ8Tag27 = sLObjData;//Message Type
//			else if ("07".equals(sLObjId))
//				gate.tokenQ8Tag07 = sLObjData;//Activation Code(AC)
//			else if ("50".equals(sLObjId))
//				gate.tokenQ8Tag50 = sLObjData;
//			return sLResult;
//	}
//	public void decodeTokenQ8() {
//		tokenIdQ8 = tokenId;
//		
//		if (null == gate.gTokenQ8ObjArrayList)
//			gate.gTokenQ8ObjArrayList = new ArrayList<TagObject>();
//		
//		
//		gate.sgTokenQ8SourceStr = tokenData;
//		String sLTmpData = tokenData;
//		
//		while (sLTmpData.length()>2) {
//			sLTmpData = getTagData(sLTmpData, gate.gTokenQ8ObjArrayList);
//		}
//
//
//		return;
//	}
//
//	public void decodeTokenQ9() {
//		tokenIdQ9 = tokenId;
//
//		String sLFormatType= tokenData.substring(0, 4);
//		gate.tokenQ9Fiid = sLFormatType;
//		
//		if ("VISA".equals(sLFormatType)) { //VISA
//			gate.bgTokenQ9FormatIsVisa=true;
//			visaDevTyp=tokenData.substring(4, 6);
//			visaChipTxnInd=tokenData.substring(6, 7);
//			visaMsgRsnCde=tokenData.substring(7,11);
//			visaFiller=tokenData.substring(11, 50);
//			
//			gate.tokenQ9VisaFiller=visaFiller;
//			gate.tokenQ9VisaChipTxnInd=visaChipTxnInd;
//			gate.tokenQ9VisaDevTyp=visaDevTyp;
//			gate.tokenQ9VisaMsgRsnCde = visaMsgRsnCde;
//		}
//		else if ("BNET".equals(sLFormatType)) { //Master Card
//			gate.bgTokenQ9FormatIsVisa=false;
//			masterDevTyp=tokenData.substring(4, 6);
//			masterAdviceRsnCde=tokenData.substring(6, 9);
//			masterAdvcDetlCde=tokenData.substring(9, 13);
//			masterAuthAgentIdCde=tokenData.substring(13, 19);
//			masterOnBehalf=tokenData.substring(19, 49);
//			masterFiller=tokenData.substring(49, 50);
//			
//			
//			gate.tokenQ9MasterDevTyp=masterDevTyp;
//			gate.tokenQ9MasterAdviceRsnCde=masterAdviceRsnCde;
//			gate.tokenQ9MasterAdvcDetlCde=masterAdvcDetlCde;
//			gate.tokenQ9MasterAuthAgentIdCde=masterAuthAgentIdCde;
//			gate.tokenQ9MasterOnBehalf=masterOnBehalf;
//			gate.tokenQ9MasterFiller=masterFiller;
//			
//
//		}
//
//		return;
//	}
//
//	
//	public void decodeTokenF1() {
//		tokenIdF1 = tokenId;
//
//		gate.sgTokenF1SourceStr=tokenData;
//		tokenF1SchemeMerchId = tokenData.substring(0, 15);
//		tokenF1CrdHldrAuthnInd = tokenData.substring(15, 16);
//		tokenF1Filler = tokenData.substring(16, 30);
//		
//		
//
//		return;
//	}
//
//	
//	public void decodeTokenCZ() {
//		tokenIdCZ = tokenId;
//
//		gate.sgTokenCZSourceStr=tokenData;
//		tokenCzAtc = tokenData.substring(0, 4);
//		tokenCzFormFactrInd = tokenData.substring(4, 12);
//		tokenCzAtcValidInd = tokenData.substring(12, 13);
//		tokenCzAtcDisCr = tokenData.substring(13, 18);
//		
//		return;
//	}
//
//	public void decodeToken04() {
//		tokenId04 = tokenId;
//
//		errorflg = tokenData.substring(0, 1);
//		reserv = tokenData.substring(1, 12);
//		cvv2Resut = tokenData.substring(12, 13);//Crd-very-flg
//		reserv = tokenData.substring(13, 18);
//		compTk2Data = tokenData.substring(18, 19);
//		reserv = tokenData.substring(19, 20);
//
//		return;
//	}
//
//	public void decodeToken06() {
//		gate.tokenId06 = tokenId;
//		tokenId06 = tokenId;
//
//		newPinFrmt = tokenData.substring(0, 1);
//		gate.newPinFrmt = newPinFrmt;
//		
//		newPinOfst = tokenData.substring(1, 17);
//		gate.newPinOfst = newPinOfst;
//		
//		pinCnt = tokenData.substring(17, 18);
//		gate.pinCnt = pinCnt;
//		
//		nwePinSize = tokenData.substring(18, 20);
//		gate.nwePinSize = nwePinSize;
//		
//		newPin1 = tokenData.substring(20, 36);
//		gate.newPin1 = newPin1;
//		
//		newPin2 = tokenData.substring(36, 52);
//		gate.newPin2 = newPin2;
//		
//		ncccStandinInd = tokenData.substring(52, 53);
//		gate.ncccStandinInd = ncccStandinInd;
//		
//		pvvOnCardFlg = tokenData.substring(53, 54);
//		gate.pvvOnCardFlg = pvvOnCardFlg;
//
//		return;
//	}
//
//	public void decodeToken23() {
//		tokenId23 = tokenId;
//
//		formatCode = tokenData.substring(0, 1);
//		pan = tokenData.substring(1, 17);
//		fieldSep = tokenData.substring(17, 18);
//		int i = tokenData.lastIndexOf("^");
//		cardHolderName = tokenData.substring(18, i);
//		fieldSep = tokenData.substring(i, i + 1);
//		expirationDate = tokenData.substring(i + 1, i + 5);
//		serviceCode = tokenData.substring(i + 5, i + 8);
//		pvki = tokenData.substring(i + 8, i + 9);
//		pvv = tokenData.substring(i + 9, i + 13);
//		discretionData = tokenData.substring(i + 13, i + 21);
//		reserv = tokenData.substring(i + 21, i + 23);
//		cvvToken23 = tokenData.substring(i + 23, i + 26);
//		reserv = tokenData.substring(i + 26, i + 32);
//		endSentinel = tokenData.substring(i + 32, i + 33);
//
//		return;
//	}
//
//	public void decodeToken25() {
//		tokenId25 = tokenId;
//
//		tranFee = tokenData.substring(0, 19);
//		origFee = tokenData.substring(19, 38);
//		reserv = tokenData.substring(38, 70);
//		return;
//	}
//
//	public void decodeTokenB2() {
//		gate.tokenIdB2 = tokenId;
//
//		reserv = tokenData.substring(4, 8);
//		gate.emv9F27 = tokenData.substring(8, 10);
//		gate.emv95 = tokenData.substring(10, 20);
//		gate.emv9F26 = tokenData.substring(20, 36);
//		gate.emv9F02 = tokenData.substring(36, 48);
//		gate.emv9F03 = tokenData.substring(48, 60);
//		gate.emv82 = tokenData.substring(60, 64);
//		gate.emv9F36 = tokenData.substring(64, 68);
//		gate.emv9F1A = tokenData.substring(68, 71);
//		gate.emv9F1A = "0" + gate.emv9F1A;
//		gate.emv5F2A = tokenData.substring(71, 74);
//		gate.emv5F2A = "0" + gate.emv5F2A;
//		gate.emv9A = tokenData.substring(74, 80);
//		gate.emv9C = tokenData.substring(80, 82);
//		gate.emv9F37 = tokenData.substring(82, 90);
//
//		gate.emv9F10 = tokenData.substring(94, 158);
//		gate.tokenB2IssApplDataLen = gate.emv9F10.substring(0, 4);
//		gate.tokenB2IssApplData = gate.emv9F10.substring(4, gate.emv9F10.length());
//
//		gate.tokenDataB2 = tokenData.substring(0, tokenLength);
//
//		return;
//	}
//
//	public void decodeTokenB2Source() {
//		tokenIdB2 = tokenId;
//
//		String bitMap = tokenData.substring(0, 4);
//		String byteMap = byte2ByteMap(bitMap, 4);
//		if (byteMap.charAt(1) == '1') {
//			reserv = tokenData.substring(4, 8);
//		}
//		if (byteMap.charAt(2) == '1') {
//			gate.emv9F27 = tokenData.substring(8, 10);
//		}
//		if (byteMap.charAt(3) == '1') {
//			gate.emv95 = tokenData.substring(10, 20);
//		}
//		if (byteMap.charAt(4) == '1') {
//			gate.emv9F26 = tokenData.substring(20, 36);
//		}
//		if (byteMap.charAt(5) == '1') {
//			gate.emv9F02 = tokenData.substring(36, 48);
//		}
//		if (byteMap.charAt(6) == '1') {
//			gate.emv9F03 = tokenData.substring(48, 60);
//		}
//		if (byteMap.charAt(7) == '1') {
//			gate.emv82 = tokenData.substring(60, 64);
//		}
//		if (byteMap.charAt(8) == '1') {
//			gate.emv9F36 = tokenData.substring(64, 68);
//		}
//		if (byteMap.charAt(9) == '1') {
//			gate.emv9F1A = tokenData.substring(68, 71);
//			gate.emv9F1A = "0" + gate.emv9F1A;
//		}
//		if (byteMap.charAt(10) == '1') {
//			gate.emv5F2A = tokenData.substring(71, 74);
//			gate.emv5F2A = "0" + gate.emv5F2A;
//		}
//		if (byteMap.charAt(11) == '1') {
//			gate.emv9A = tokenData.substring(74, 80);
//		}
//		if (byteMap.charAt(12) == '1') {
//			gate.emv9C = tokenData.substring(80, 82);
//		}
//		if (byteMap.charAt(13) == '1') {
//			gate.emv9F37 = tokenData.substring(82, 90);
//		}
//
//		if (byteMap.charAt(14) == '1') {
//			int iadLength = Integer.parseInt(tokenData.substring(90, 94));
//			gate.emv9F10 = tokenData.substring(94, 94 + iadLength);
//		}
//
//		gate.arqc = gate.emv9F26;
//		if (gate.binType.equals("V") && gate.emv9F10.length() >= 64) {
//			gate.emvD6 = gate.emv9F10.substring(6, 14);
//		} else if (gate.binType.equals("M") && gate.emv9F10.length() >= 64) {
//			gate.emvD6 = gate.emv9F10.substring(6, 14);
//		} else if (gate.binType.equals("J") && gate.emv9F10.length() >= 64) {
//			gate.emvD6 = gate.emv9F10.substring(6, 16);
//		}
//		gate.cvr = gate.emvD6;
//		return;
//	}
//
//	private String byte2ByteMap(String src, int size) {
//		byte[] srcByte = new byte[65];
//		String[] cvt = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
//				"1100", "1101", "1110", "1111" };
//		String dest = "";
//		int i = 0, ind = 0;
//		srcByte = src.getBytes();
//
//		for (i = 0; i < size; i++) {
//			if (srcByte[i] >= '0' && srcByte[i] <= '9') {
//				ind = (int) (srcByte[i] & 0x0F);
//			} else if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
//				ind = (int) (srcByte[i] & 0x0F);
//				ind += 9;
//			}
//
//			dest = dest + cvt[ind];
//		}
//		return dest;
//	}
//
//	public void decodeTokenB3() {
//		// tokenIdB3 = tokenId;
//		gate.tokenIdB3 = tokenId;
//
//		String bitMap = tokenData.substring(0, 4);
//		String byteMap = byte2ByteMap(bitMap, 4);
//
//		if (byteMap.charAt(0) == '1') {
//			gate.termSerNum = tokenData.substring(4, 12);
//		}
//		if (byteMap.charAt(1) == '1') {
//			gate.emv9F33 = tokenData.substring(12, 18);
//		}
//		reserv = tokenData.substring(18, 24);
//		reserv = tokenData.substring(24, 32);
//		if (byteMap.charAt(4) == '1') {
//			gate.emv9F35 = tokenData.substring(32, 34);
//		}
//		if (byteMap.charAt(5) == '1') {
//			gate.emv9F09 = tokenData.substring(34, 38);
//		}
//		if (byteMap.charAt(6) == '1') {
//			gate.emv9F34 = tokenData.substring(38, 44);
//		}
//		if (byteMap.charAt(7) == '1') {
//			String lenData = tokenData.substring(44, 48);
//			int cvtLength = Integer.parseInt(lenData);
//			dfName = tokenData.substring(48, 48 + cvtLength);
//		}
//		gate.tokenDataB3 = tokenData.substring(0, tokenLength);
//		return;
//	}
//
//	public void decodeTokenB4() {
//		// tokenIdB4 = tokenId;
//		gate.tokenIdB4 = tokenId;
//
//		ptSrvEntryMode = tokenData.substring(0, 3);
//		gate.emvDFEE = tokenData.substring(3, 4); //TERM-ENTRY-CAP
//		lastEmvStat = tokenData.substring(4, 5);
//		dataSuspect = tokenData.substring(5, 6);
//		gate.emv5F34 = tokenData.substring(6, 8);//APPL-PAN-SEQ-NUM
//		devInfoCamFlg = tokenData.substring(8, 14);//DEV-INFO or CAM-FLG
//		gate.emvDFEF = tokenData.substring(14, 18);//RSN-ON-LINE-CDE
//		arqcVerify = tokenData.substring(18, 19);
//		reserv = tokenData.substring(19, 20); //USR-FLD1
//
//		gate.tokenDataB4 = tokenData.substring(0, tokenLength);
//		return;
//	}
//
//	public void decodeTokenB5() {
//		tokenIdB5 = tokenId;
//
//		String lenData = tokenData.substring(0, 4);
//		int cvtLength = Integer.parseInt(lenData);
//		gate.emv91 = tokenData.substring(4, 20).trim();
//		gate.visaAddlData = tokenData.substring(20, 36);
//		reserv = tokenData.substring(36, 42);
//		gate.arpc = gate.emv91;
//
//		return;
//	}
//
//	public void decodeTokenB6() {
//		tokenIdB6 = tokenId;
//
//		String lenData = tokenData.substring(0, 4);
//		int cvtLen = Integer.parseInt(lenData);
//		issscriptData = tokenData.substring(4, 4 + cvtLen);
//		return;
//	}
//
//	public void decodeTokenBJ() {
//		tokenIdBJ = tokenId;
//
//		numIssScriptRslt = tokenData.substring(0, 1);
//		reserv = tokenData.substring(1, 2);
//		String cvtData = tokenData.substring(2);
//		for (int i = 0; i < 8; i++) {
//			issScriptProcRslt[i] = cvtData.substring(0, 1);
//			issuerScriptSeq[i] = cvtData.substring(1, 2);
//			issScriptId[i] = cvtData.substring(2, 10);
//			cvtData = cvtData.substring(10);
//		}
//		return;
//	}
//
//	public void decodeTokenC0() {
//		tokenIdC0 = tokenId;
//
//		gate.tokenIdC0 = "C0";
//
//		gate.tokenC0 = tokenData.substring(0, tokenLength);
//		
//		gate.cvv2 = tokenData.substring(0, 4).trim();
//		gate.cvdfld = tokenData.substring(0, 4).trim();
//
//		reserv = tokenData.substring(4, 18);
//		gate.eci = tokenData.substring(18, 19); // MO/TO or EC flag
//		reserv = tokenData.substring(19, 21);
//		cvdFldPresent = tokenData.substring(21, 22);
//		gate.cvdPresent = cvdFldPresent;
//		
//		reserv = tokenData.substring(22, 23);
//		gate.ucafInd = tokenData.substring(23, 24);
//		reserv = tokenData.substring(24, 25);
//		cavvResult = tokenData.substring(25, 26);
//		gate.cavvResult = cavvResult;
//		return;
//	}
//
//	public void decodeTokenC4() {
//		gate.tokenIdC4 = tokenId;
//		tokenIdC4 = tokenId;
//
//		termAttendInd = tokenData.substring(0, 1);
//		gate.tokenC4Filter1 = tokenData.substring(1, 2);
//		termLocInd = tokenData.substring(2, 3);
//		chPresetInd = tokenData.substring(3, 4);
//		crdPresetInd = tokenData.substring(4, 5);
//		crdCaptrInd = tokenData.substring(5, 6);
//		txnStatInd = tokenData.substring(6, 7);
//		txnSecInd = tokenData.substring(7, 8);
//		
//		txnRtnInd = tokenData.substring(8, 9); //TXN-RTN-IND
//		
//		chActvtInd = tokenData.substring(9, 10);
//		termInputCAP = tokenData.substring(10, 11);
//		gate.tokenC4Filter2 = tokenData.substring(11, 12);
//
//		gate.tokenC4TermAttendInd = termAttendInd;
//		
//		gate.tokenC4TermLocInd = termLocInd;
//		gate.tokenC4ChPresetInd = chPresetInd;
//		gate.tokenC4CrdPresetInd = crdPresetInd;
//		gate.tokenC4CrdCaptrInd = crdCaptrInd;
//		gate.tokenC4TxnStatInd = txnStatInd;
//		gate.tokenC4TxnSecInd = txnSecInd;
//		gate.tokenC4TxnRtnInd = txnRtnInd;
//		gate.tokenC4ChActvtInd = chActvtInd;
//		gate.tokenC4TermInputCap = termInputCAP;
//
//
//		
//		
//		return;
//	}
//
//	public void decodeTokenC5() {
//		tokenIdC5 = tokenId;
//
//		merchantId = tokenData.substring(0, 10);
//		storeId = tokenData.substring(10, 16);
//		transId = tokenData.substring(16, 18);
//		referenceId = tokenData.substring(18, 29);
//		goodsId = tokenData.substring(29, 35);
//		personal = tokenData.substring(35, 45);
//		pin = tokenData.substring(45, 49);
//		ecFlag = tokenData.substring(49, 50);
//
//		gate.divMark = tokenData.substring(90, 91);
//		if (gate.divMark.equals("I") || gate.divMark.equals("E")) {
//			orderNumI = tokenData.substring(50, 90);
//			gate.divMark = tokenData.substring(90, 91);
//			gate.installTxRespCde = tokenData.substring(91, 93);
//			reserv = tokenData.substring(93, 120);
//			gate.divNum = tokenData.substring(120, 122);// 分期數
//			gate.firstAmt = tokenData.substring(122, 130);// 首期金額
//			gate.everyAmt = tokenData.substring(130, 138);// 每期金額
//			gate.procAmt = tokenData.substring(138, 144);
//		} else {
//			settleFlg = tokenData.substring(50, 52);
//			orderNum1 = tokenData.substring(52, 90);
//			gate.loyaltyTxId = tokenData.substring(90, 91);
//			gate.loyaltyTxResp = tokenData.substring(91, 93);
//			gate.pointRedemption = tokenData.substring(93, 101);
//			gate.signBalance = tokenData.substring(101, 102);
//			gate.pointBalance = tokenData.substring(102, 110);
//			gate.paidCreditAmt = tokenData.substring(110, 120);
//		}
//
//		return;
//	}
//
//	public void decodeTokenCH() {
//		gate.tokenIdCH = tokenId;
//		tokenIdCH = tokenId;
//		
//		boolean bLIsVisa=true;
//		if (gate.isoField[22].length()>=2) {
//			if (gate.isoField[22].substring(0, 2).equals("81"))
//				bLIsVisa=false;
//		}
//
//		if (bLIsVisa) {
//			tokenChRespSrcRsnCde4Visa = tokenData.substring(0, 1);
//			tokenChFilter14Visa  = tokenData.substring(1, 19);
//			tokenChRecurPmntInd4Visa  = tokenData.substring(19, 20);
//			tokenChFilter24Visa  = tokenData.substring(20, 33);
//			tokenChRvslRsnInd4Visa  = tokenData.substring(33,34);
//			tokenChFilter34Visa  = tokenData.substring(34, 36);
//			tokenChAuthMsgInd4Visa  = tokenData.substring(36,37);
//			tokenChTermTyp4Visa  = tokenData.substring(37,38);
//			tokenChFilter44Visa  = tokenData.substring(38,40);
//				
//		}
//		else  {
//
//			
//
//
//			tokenChFilter14Master = tokenData.substring(0, 19);
//			tokenChRecurPmntInd4Master  = tokenData.substring(19, 20);
//			tokenChFilter24Master  = tokenData.substring(20, 24);
//			tokenChPmntTypInd4Master  = tokenData.substring(24, 27);
//			
//			
//			tokenChFilter34Master  = tokenData.substring(27,33);
//			
//			tokenChRvslRsnInd4Master  = tokenData.substring(33, 34);
//			tokenChFilter44Master  = tokenData.substring(34,36);
//			
//			tokenChAuthMsgInd4Master  = tokenData.substring(36,37);
//			tokenChTermTypMaster  = tokenData.substring(37,38);
//			tokenChFilter54Master  = tokenData.substring(40);
//		}
//		
//		
//		
//		return;
//	}
//
//	public void decodeTokenC6() {
//		tokenIdC6 = tokenId;
//
//		gate.xid = tokenData.substring(0, 40).trim();
//		gate.cavv = tokenData.substring(40, 80).trim();
//
//		return;
//	}
//
//	public void decodeTokenCE() {
//		tokenIdCE = tokenId;
//
//		authnIndFlg = tokenData.substring(0, 2);
//		gate.authnIndFlg = authnIndFlg;
//
//		// "CE00030 01jAoHnaRTHgLDCBgAAAAnBRUAAAA=! "
//		// tokenData => "01jAoHnaRTHgLDCBgAAAAnBRUAAAA=! "
//		gate.ucaf = tokenData.substring(2, tokenLength).trim();
//
//		// gate.ucaf = tokenData.substring(2,202).trim();
//		return;
//	}
//
//	public void decodeTokenCI() {
//		tokenIdCI = tokenId;
//
//		reserv = tokenData.substring(0, 52);
//		mcElecAccptInd = tokenData.substring(52, 53);
//		reserv = tokenData.substring(53, 70);
//		return;
//	}
//
//	public void decodeTokenQ2() {
//		tokenIdQ2 = tokenId;
//
//		offLineInd = tokenData.substring(0, 1);
//		terminalSerNum = tokenData.substring(1, 9);
//		gate.emvDFED = tokenData.substring(9, 10);
//		authRespCode = tokenData.substring(10, 12);
//		gate.emv9B = tokenData.substring(12, 16);
//		int cvtLength = Integer.parseInt(tokenData.substring(16, 18));
//		gate.emv91 = tokenData.substring(18, 18 + cvtLength);
//		gate.emv9F34 = tokenData.substring(50, 56);
//		gate.emv9F09 = tokenData.substring(56, 60);
//		gate.emv9F41 = tokenData.substring(60, 70);
//		iCCAppVersion = tokenData.substring(70, 74);
//		return;
//	}
//
//	public void decodeTokenQ3() {
//		tokenIdQ3 = tokenId;
//
//		jcbStipInst = tokenData.substring(0, 1);
//		jcbStipReason = tokenData.substring(1, 2);
//		jcbStipRjeReason = tokenData.substring(2, 4);
//		reserv = tokenData.substring(4, 10);
//		return;
//	}
//
//	public void decodeTokenF4() {
//		tokenIdF4 = tokenId;
//		walletIndFlg = tokenData.substring(0, 2);
//		walletIndData = tokenData.substring(2, 5);
//		tokenF4Filler = tokenData.substring(5, 14);
//		
//		gate.tokenF4WalletIndFlg = walletIndFlg;
//		gate.tokenF4WalletIndData = walletIndData;
//		gate.tokenF4Filler = tokenF4Filler;
//		return;
//	}
//
//	public void decodeTokenW7() {
//		tokenIdW7 = tokenId;
//
//		int w7Length = Integer.parseInt(tokenData.substring(0, 2));
//		chSerNumber = tokenData.substring(2, 34);
//		return;
//	}
//
//	public void decodeTokenW8() {
//		tokenIdW8 = tokenId;
//
//		int w8Length = Integer.parseInt(tokenData.substring(0, 2));
//		mchtSerNumber = tokenData.substring(2, 34);
//		return;
//	}
//
//	public void decodeTokenWB() {
//		tokenIdWB = tokenId;
//
//		bnetEcCertRqst = tokenData.substring(0, 95);
//		bnetEcCertResp = tokenData.substring(95, 120);
//		return;
//	}
//
//	public void decodeTokenWV() {
//		tokenIdWV = tokenId;
//
//		// //System.out.println(tokenData.length());
//		// //System.out.println(tokenData);
//		visaEcCertRqst = tokenData.substring(0, 120);
//		return;
//	}
//
//	public String createTokenData(AuthTxnGate gate) {
//		this.gate = gate;
//
//		gate.tokenData = "";
//
//		tokenData = "";
//		reserv = "";
//		spaces = "";
//		zeros = "";
//		for (int k = 0; k < 20; k++) {
//			spaces = spaces + "               ";
//			zeros = zeros + "000000000000000";
//		}
//
//		if (gate.tokenId04.equals("04")) {
//			gate.tokenData = gate.tokenData + createToken04();
//		}
//
//		if (gate.tokenId06.equals("06")) {
//			gate.tokenData = gate.tokenData + createToken06();
//		}
//
//		if (gate.tokenId23.equals("23")) {
//			gate.tokenData = gate.tokenData + createToken23();
//		}
//
//		if (gate.tokenId25.equals("25")) {
//			gate.tokenData = gate.tokenData + createToken25();
//		}
//
//		if (gate.tokenIdB2.equals("B2")) {
//			gate.tokenData = gate.tokenData + createTokenB2();
//		}
//
//		if (gate.tokenIdB3.equals("B3")) {
//			gate.tokenData = gate.tokenData + createTokenB3();
//		}
//
//		if (gate.tokenIdB4.equals("B4")) {
//			gate.tokenData = gate.tokenData + createTokenB4();
//		}
//
//		if (gate.tokenIdB5.equals("B5")) {
//			gate.tokenData = gate.tokenData + createTokenB5();
//		}
//
//		if (gate.tokenIdB6.equals("B6")) {
//			gate.tokenData = gate.tokenData + createTokenB6();
//		}
//
//		if (gate.tokenIdBJ.equals("BJ")) {
//			gate.tokenData = gate.tokenData + createTokenBJ();
//		}
//
//		if (gate.tokenIdC0.equals("C0")) {
//			gate.tokenData = gate.tokenData + createTokenC0();
//		}
//
//		if (gate.tokenIdC4.equals("C4")) {
//			gate.tokenData = gate.tokenData + createTokenC4();
//		}
//
//		if (gate.tokenIdC5.equals("C5")) {
//			gate.tokenData = gate.tokenData + createTokenC5();
//		}
//
//		if (gate.tokenIdC6.equals("C6")) {
//			gate.tokenData = gate.tokenData + createTokenC6();
//		}
//
//		if (gate.tokenIdCE.equals("CE")) {
//			gate.tokenData = gate.tokenData + createTokenCE();
//		}
//
//		if (gate.tokenIdCH.equals("CH")) {
//			gate.tokenData = gate.tokenData + createTokenCH();
//		}
//
//		if (gate.tokenIdCI.equals("CI")) {
//			gate.tokenData = gate.tokenData + createTokenCI();
//		}
//
//		if (gate.tokenIdQ2.equals("Q2")) {
//			gate.tokenData = gate.tokenData + createTokenQ2();
//		}
//
//		if (gate.tokenIdQ3.equals("Q3")) {
//			gate.tokenData = gate.tokenData + createTokenQ3();
//		}
//
//		if (gate.tokenIdW7.equals("W7")) {
//			gate.tokenData = gate.tokenData + createTokenW7();
//		}
//
//		if (gate.tokenIdW8.equals("W8")) {
//			gate.tokenData = gate.tokenData + createTokenW8();
//		}
//
//		if (gate.tokenIdWB.equals("WB")) {
//			gate.tokenData = gate.tokenData + createTokenWB();
//		}
//
//		if (gate.tokenIdWV.equals("WV")) {
//			gate.tokenData = gate.tokenData + createTokenWV();
//		}
//
//		if (gate.tokenIdS8.equals("S8")) {
//			gate.tokenData = gate.tokenData + createTokenS8();
//		}
//
//		if (gate.tokenIdF4.equals("F4")) {
//			gate.tokenData = gate.tokenData + createTokenF4();
//		}
//
//		if (gate.tokenIdQ8.equals("Q8")) {
//			gate.tokenData = gate.tokenData + createTokenQ8();
//		}
//
//		if (gate.tokenIdQ9.equals("Q9")) {
//			gate.tokenData = gate.tokenData + createTokenQ9();
//		}
//		if (gate.tokenIdQR.equals("QR")) {
//			gate.tokenData = gate.tokenData + createTokenQR();
//		}
//		
//		if (gate.tokenIdF1.equals("F1")) {
//			gate.tokenData = gate.tokenData + createTokenF1();
//		}
//		
//		if (gate.tokenIdCZ.equals("CZ")) {
//			gate.tokenData = gate.tokenData + createTokenCZ();
//		}
//
//		if (tokenCount == 0) {
//			return "";
//		}
//
//		return createHeaderToken(gate.tokenData) + gate.tokenData;
//
//	}
//
//	public String createHeaderToken(String cvtTokenData) {
//		tokenCount++;
//		String lenCount = "" + tokenCount;
//		String lenTotal = "" + (cvtTokenData.getBytes().length + 12);
//		String tokenHeader = "& " + fillZero(lenCount, 5) + fillZero(lenTotal, 5);
//
//		return tokenHeader;
//	}
//
//	public String createTokenHeader(String tokenIdno, String cvtTokenData) {
//		String lenData = "" + cvtTokenData.length();
//		String tokenHeader = "! " + tokenIdno + fillZero(lenData, 5) + " ";
//
//		return tokenHeader;
//	}
//
//	public String createToken04() {
//		String cvtData = spaces.substring(0, 20);
//
//		cvtData = formatString(cvtData, errorflg, 0, 1);
//		cvtData = formatString(cvtData, reserv, 1, 12);
//		cvtData = formatString(cvtData, cvv2Resut, 12, 13);
//		cvtData = formatString(cvtData, reserv, 13, 18);
//		cvtData = formatString(cvtData, compTk2Data, 18, 19);
//		cvtData = formatString(cvtData, reserv, 19, 20);
//
//		cvtData = gate.tokenData04;
//		tokenCount++;
//		return createTokenHeader("04", cvtData) + cvtData;
//	}
//
//	public String createTokenS8() {
//		String cvtData = spaces.substring(0, 30);
//		
//		//cvtData = formatString(cvtData, acctNumInd, 0, 1);
//		cvtData = formatString(cvtData, gate.tokenS8AcctNumInd, 0, 1);
//		
//		cvtData = formatString(cvtData, gate.tokenS8AcctNum, 1, 20);
//		cvtData = formatString(cvtData, gate.tokenS8ExpDat , 20, 24);
//		cvtData = formatString(cvtData, gate.tokenS8AcVeryRslt, 24, 26);
//		cvtData = formatString(cvtData, gate.tokenS8FraudChkRslt, 26, 28);
//		cvtData = formatString(cvtData, gate.tokenS8Filler, 28, 30);
//		
//		//cvtData = formatString(cvtData, expDat, 20, 24);
//		//cvtData = formatString(cvtData, acVeryRslt, 24, 26);
//		//cvtData = formatString(cvtData, fraudChkRslt, 26, 28);
//		//cvtData = formatString(cvtData, filler, 28, 30);
//		tokenCount++;
//		
//		////System.out.println("S8=>" + cvtData + "===");
//		return createTokenHeader("S8", cvtData) + cvtData;
//	}
//
//	public String createTokenQR() {
//		
//		String cvtData = gate.tokenQrAdditionalData1 + gate.tokenQrAdditionalData2 ;
//		
//		tokenCount++;
//		
//		String sLTokenHeader = createTokenHeader("QR", cvtData);
//		String sLFullToken = sLTokenHeader + cvtData;
//		
//		//System.out.println("Token QR=>" + sL_FullToken + "===");
//		return  sLFullToken;
//	}
//
//	public String createToken06() {
//		int nLTokenLen = 54;
//		if (gate.bgToken06RealLengthIs52)
//			nLTokenLen = 52;
//		String cvtData = spaces.substring(0, nLTokenLen);
//
//		cvtData = formatString(cvtData, gate.newPinFrmt, 0, 1);
//		if ("".equals(gate.newPinFromHsm))
//			cvtData = formatString(cvtData, gate.newPinOfst, 1, 17);
//		else
//			cvtData = formatString(cvtData, gate.newPinFromHsm, 1, 17);
//
//		cvtData = formatString(cvtData, gate.pinCnt, 17, 18);
//		cvtData = formatString(cvtData, gate.nwePinSize, 18, 20);
//		cvtData = formatString(cvtData, gate.newPin1, 20, 36);
//		cvtData = formatString(cvtData, gate.newPin2, 36, 52);
//		
//		if (!gate.bgToken06RealLengthIs52) {
//			cvtData = formatString(cvtData, gate.ncccStandinInd, 52, 53);
//			cvtData = formatString(cvtData, gate.pvvOnCardFlg, 53, 54);
//		}
//		tokenCount++;
//
//		return createTokenHeader("06", cvtData) + cvtData;
//	}
//
//	public String createToken23() {
//		String cvtData = spaces.substring(0, 51 + cardHolderName.length());
//
//		cvtData = formatString(cvtData, formatCode, 0, 1);
//		cvtData = formatString(cvtData, pan, 1, 17);
//		cvtData = formatString(cvtData, "^", 17, 18);
//		int i = 18 + cardHolderName.length();
//		cvtData = formatString(cvtData, cardHolderName, 18, i);
//		cvtData = formatString(cvtData, "^", i, i + 1);
//		cvtData = formatString(cvtData, expirationDate, i + 1, i + 5);
//		cvtData = formatString(cvtData, serviceCode, i + 5, i + 8);
//		cvtData = formatString(cvtData, pvki, i + 8, i + 9);
//		cvtData = formatString(cvtData, pvv, i + 9, i + 13);
//		cvtData = formatString(cvtData, discretionData, i + 13, i + 21);
//		cvtData = formatString(cvtData, reserv, i + 21, i + 23);
//		cvtData = formatString(cvtData, cvvToken23, i + 23, i + 26);
//		cvtData = formatString(cvtData, reserv, i + 26, i + 32);
//		cvtData = formatString(cvtData, endSentinel, i + 32, i + 33);
//		tokenCount++;
//
//		return createTokenHeader("23", cvtData) + cvtData;
//	}
//
//	public String createToken25() {
//		String cvtData = spaces.substring(0, 70);
//
//		cvtData = formatString(cvtData, tranFee, 0, 19);
//		cvtData = formatString(cvtData, origFee, 19, 38);
//		cvtData = formatString(cvtData, reserv, 38, 70);
//		tokenCount++;
//
//		return createTokenHeader("25", cvtData) + cvtData;
//	}
//
//	public String createTokenF4() {
//		String cvtData = spaces.substring(0, 14);
//
//
//		cvtData = formatString(cvtData, gate.tokenF4WalletIndFlg, 0, 2);
//		cvtData = formatString(cvtData, gate.tokenF4WalletIndData, 2, 5);
//		cvtData = formatString(cvtData, gate.tokenF4Filler, 5, 14);
//		tokenCount++;
//
//		return createTokenHeader("F4", cvtData) + cvtData;
//	}
//
//	private String genTokenQ8Value() {
//		String sLTagId="", sLTagData="", sLResult="", sLTmp="", sLTagDataLength="";
//		for (int i=0; i<gate.gTokenQ8ObjArrayList.size(); i++) {
//			sLTagId = gate.gTokenQ8ObjArrayList.get(i).getObjId();
//			sLTagData = gate.gTokenQ8ObjArrayList.get(i).getObjData();
//			
//			sLTagDataLength = HpeUtil.fillCharOnLeft(Integer.toString(sLTagData.length()), 3, "0");
//			sLTmp = sLTagId + sLTagDataLength + sLTagData;
//			
//			sLResult += sLTmp;
//		}
//		
//		return sLResult;
//	}
//
//	public String createTokenQ8() {
//		
//		String cvtData = genTokenQ8Value();
//		/*
//		String cvtData = gate.sG_TokenQ8SourceStr;
//		
//		String sL_TmpData="", sL_TmpDataLength="";
//		if (!"".equals(gate.tokenQ8Tag50)) {
//			
//			sL_TmpDataLength = HpeUtil.fillZeroOnLeft(Integer.toString(gate.tokenQ8Tag50.length()), 3);
//			sL_TmpData = "50" + sL_TmpDataLength + gate.tokenQ8Tag50; 
//			cvtData += sL_TmpData;
//		}
//
//		if (!"".equals(gate.tokenQ8Tag51)) {
//			
//			sL_TmpDataLength = HpeUtil.fillZeroOnLeft(Integer.toString(gate.tokenQ8Tag51.length()), 3);
//			sL_TmpData = "51" + sL_TmpDataLength + gate.tokenQ8Tag51; 
//			cvtData += sL_TmpData;
//		}
//		*/
//		//down, ＊若Token Q8總長度不會偶數時，會於最後補一個空白最為filler使token長度必定為偶數。
//		if (cvtData.length()%2 != 0)
//			cvtData = cvtData + " ";
//		//up, ＊若Token Q8總長度不會偶數時，會於最後補一個空白最為filler使token長度必定為偶數。
//		
//		tokenCount++;
//
//		return createTokenHeader("Q8", cvtData) + cvtData;
//	}
//
//	public String createTokenQ9() {
//		
//		
//		String cvtData = gate.sgTokenQ9SourceStr;
//		tokenCount++;
//
//		return createTokenHeader("Q9", cvtData) + cvtData;
//	}
//
//	public String createTokenF1() {
//		
//		
//		String cvtData = gate.sgTokenF1SourceStr;
//		tokenCount++;
//
//		return createTokenHeader("F1", cvtData) + cvtData;
//	}
//
//	public String createTokenCZ() {
//		
//		
//		String cvtData = gate.sgTokenCZSourceStr;
//		tokenCount++;
//
//		return createTokenHeader("CZ", cvtData) + cvtData;
//	}
//
//	
//	public String createTokenB2() {
//		String cvtData = zeros.substring(0, 158);
//
//		byte[] byteMap = new byte[16];
//		for (int k = 0; k < 16; k++) {
//			byteMap[k] = '0';
//		}
//
//		cvtData = formatNumber(cvtData, "0000", 4, 8);
//		byteMap[0] = '0';
//		if (gate.emv9F27.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F27, 8, 10);
//			byteMap[1] = '1';
//		}
//		if (gate.emv95.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv95, 10, 20);
//			byteMap[2] = '1';
//		}
//		if (gate.emv9F26.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F26, 20, 36);
//			byteMap[3] = '1';
//		}
//		if (gate.emv9F02.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F02, 36, 48);
//			byteMap[4] = '1';
//		}
//		if (gate.emv9F03.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F03, 48, 60);
//			byteMap[5] = '1';
//		}
//		if (gate.emv82.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv82, 60, 64);
//			byteMap[6] = '1';
//		}
//		if (gate.emv9F36.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F36, 64, 68);
//			byteMap[7] = '1';
//		}
//		if (gate.emv9F1A.length() > 0) {
//			gate.emv9F1A = gate.emv9F1A.substring(1);
//			cvtData = formatNumber(cvtData, gate.emv9F1A, 68, 71);
//			byteMap[8] = '1';
//		}
//		if (gate.emv5F2A.length() > 0) {
//			gate.emv5F2A = gate.emv5F2A.substring(1);
//			cvtData = formatNumber(cvtData, gate.emv5F2A, 71, 74);
//			byteMap[9] = '1';
//		}
//		if (gate.emv9A.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9A, 74, 80);
//			byteMap[10] = '1';
//		}
//		if (gate.emv9C.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9C, 80, 82);
//			byteMap[11] = '1';
//		}
//		if (gate.emv9F37.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F37, 82, 90);
//			byteMap[12] = '1';
//		}
//
//		int cvtLength = gate.emv9F10.length();
//		cvtData = formatNumber(cvtData, (64 + ""), 90, 94);
//		if (gate.emv9F10.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F10, 94, 94 + cvtLength);
//			byteMap[15] = '1';
//		}
//
//		String tmpString = new String(byteMap, 0, 16);
//		String bitMap = byteMap2Byte(tmpString, 4);
//		cvtData = formatNumber(cvtData, bitMap, 0, 4);
//
//		cvtData = gate.tokenDataB2;
//		tokenCount++;
//		return createTokenHeader("B2", cvtData) + cvtData;
//	}
//
//	private String byteMap2Byte(String src, int size) {
//		char[] destChar = new char[33];
//		char[] cvt = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
//		int i = 0, j = 0, ind = 0;
//		String dest = "", tmp = "";
//
//		for (i = 0; i < size; i++) {
//			tmp = "";
//			tmp = src.substring(j, j + 4);
//			ind = Integer.parseInt(tmp, 2);
//			destChar[i] = cvt[ind];
//			j += 4;
//		}
//
//		dest = String.valueOf(destChar);
//		dest = dest.substring(0, size);
//
//		return dest;
//	}
//
//	public String createTokenB3() {
//		String cvtData = spaces.substring(0, 80);
//
//		byte[] byteMap = new byte[16];
//		for (int k = 0; k < 16; k++) {
//			byteMap[k] = '0';
//		}
//
//		if (gate.termSerNum.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.termSerNum, 8, 10);
//			byteMap[0] = '1';
//		}
//		if (gate.emv9F33.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F33, 10, 20);
//			byteMap[1] = '1';
//		}
//		if (gate.emv9F35.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F35, 20, 36);
//			byteMap[4] = '1';
//		}
//		if (gate.emv9F09.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F09, 36, 48);
//			byteMap[5] = '1';
//		}
//		if (gate.emv9F34.length() > 0) {
//			cvtData = formatNumber(cvtData, gate.emv9F34, 48, 60);
//			byteMap[6] = '1';
//		}
//		if (dfName.length() > 0) {
//			cvtData = formatNumber(cvtData, dfName, 60, 64);
//			byteMap[7] = '1';
//		}
//
//		cvtData = formatString(cvtData, gate.termSerNum, 4, 12);
//		cvtData = formatString(cvtData, gate.emv9F33, 12, 18);
//		cvtData = formatString(cvtData, reserv, 18, 24);
//		cvtData = formatString(cvtData, reserv, 24, 32);
//		cvtData = formatString(cvtData, gate.emv9F35, 32, 34);
//		cvtData = formatString(cvtData, gate.emv9F09, 34, 38);
//		cvtData = formatString(cvtData, gate.emv9F34, 38, 44);
//		int cvtLength = dfName.length();
//		cvtData = formatNumber(cvtData, (cvtLength + ""), 44, 48);
//		cvtData = formatString(cvtData, dfName, 48, 80);
//
//		String tmpString = new String(byteMap, 0, 16);
//		String bitMap = byteMap2Byte(tmpString, 4);
//		cvtData = formatNumber(cvtData, bitMap, 0, 4);
//
//		cvtData = gate.tokenDataB3;
//		tokenCount++;
//		return createTokenHeader("B3", cvtData) + cvtData;
//	}
//
//	public String createTokenB4() {
//		String cvtData = spaces.substring(0, 20);
//
//		cvtData = formatString(cvtData, ptSrvEntryMode, 0, 3);
//		cvtData = formatString(cvtData, gate.emvDFEE, 3, 4);
//		cvtData = formatString(cvtData, lastEmvStat, 4, 5);
//		cvtData = formatString(cvtData, dataSuspect, 5, 6);
//		cvtData = formatString(cvtData, gate.emv5F34, 6, 8);
//		cvtData = formatString(cvtData, devInfoCamFlg, 8, 14);
//		cvtData = formatString(cvtData, gate.emvDFEF, 14, 18);
//		cvtData = formatString(cvtData, arqcVerify, 18, 19);
//		cvtData = formatString(cvtData, reserv, 19, 20);
//
//		cvtData = gate.tokenDataB4;
//		tokenCount++;
//
//		return createTokenHeader("B4", cvtData) + cvtData;
//	}
//
//	public String createTokenB5() {
//		String cvtData = spaces.substring(0, 42);
//
//		// int cvtLength = gate.emv91.length() + gate.visaAddlData.length();
//		int cvtLength = 10;// 20170110 by Tony
//
//		gate.emv91 = gate.arpc;
//
//		cvtData = formatNumber(cvtData, (cvtLength + ""), 0, 4);
//		cvtData = formatString(cvtData, gate.emv91, 4, 20);
//		String respCode = unpackToString(gate.isoField[39].getBytes(), 2);
//		if (gate.arc.length() > 4) {
//			respCode = gate.arc.substring(0, 4);
//		} // modify by JackLiao 2016/01/23
//		cvtData = formatString(cvtData, respCode, 20, 24);
//		cvtData = formatString(cvtData, "000000000000", 24, 36);
//		cvtData = formatString(cvtData, "N    3", 36, 42);
//		tokenCount++;
//
//		return createTokenHeader("B5", cvtData) + cvtData;
//	}
//
//	public String createTokenB6() {
//		String cvtData = spaces.substring(0, 4 + issscriptData.length());
//
//		String lenData = "" + issscriptData.length();
//		cvtData = formatNumber(cvtData, lenData, 0, 4);
//		cvtData = formatString(cvtData, issscriptData, 4, issscriptData.length() + 4);
//		tokenCount++;
//
//		return createTokenHeader("B6", cvtData) + cvtData;
//	}
//
//	public String createTokenBJ() {
//		String cvtData = spaces.substring(0, 2);
//
//		cvtData = formatString(cvtData, numIssScriptRslt, 0, 1);
//		cvtData = formatString(cvtData, reserv, 1, 2);
//
//		for (int i = 0; i < 8; i++) {
//			String tmpData = spaces.substring(0, 10);
//			tmpData = formatString(tmpData, issScriptProcRslt[i], 0, 1);
//			tmpData = formatString(tmpData, issuerScriptSeq[i], 1, 2);
//			tmpData = formatString(tmpData, issScriptId[i], 2, 10);
//			cvtData = cvtData + tmpData;
//		}
//		tokenCount++;
//
//		return createTokenHeader("BJ", cvtData) + cvtData;
//	}
//
//	public String createTokenC0() {
//		String cvtData = spaces.substring(0, 26);
//
//		cvtData = formatNumber(cvtData, gate.cvv2, 0, 4);
//		cvtData = formatString(cvtData, reserv, 4, 18);
//		cvtData = formatString(cvtData, gate.eci, 18, 19);
//		cvtData = formatString(cvtData, reserv, 19, 21);
//		cvtData = formatString(cvtData, cvdFldPresent, 21, 22);
//		cvtData = formatString(cvtData, reserv, 22, 23);
//		cvtData = formatString(cvtData, gate.ucafInd, 23, 24);
//		cvtData = formatString(cvtData, reserv, 24, 25);
//		cvtData = formatString(cvtData, cavvResult, 25, 26);
//
//		cvtData = gate.tokenC0;
//
//		tokenCount++;
//		return createTokenHeader("C0", cvtData) + cvtData;
//	}
//
//	public String createTokenC4() {
//		String cvtData = spaces.substring(0, 12);
//
//		
//		cvtData = formatString(cvtData, gate.tokenC4TermAttendInd, 0, 1);
//		cvtData = formatString(cvtData, gate.tokenC4Filter1, 1, 2);
//		cvtData = formatString(cvtData, gate.tokenC4TermLocInd, 2, 3);
//		cvtData = formatString(cvtData, gate.tokenC4ChPresetInd, 3, 4);
//		cvtData = formatString(cvtData, gate.tokenC4CrdPresetInd, 4, 5);
//		cvtData = formatString(cvtData, gate.tokenC4CrdCaptrInd, 5, 6);
//		cvtData = formatString(cvtData, gate.tokenC4TxnStatInd, 6, 7);
//		cvtData = formatString(cvtData, gate.tokenC4TxnSecInd, 7, 8);
//		cvtData = formatString(cvtData, gate.tokenC4TxnRtnInd, 8, 9);
//		cvtData = formatString(cvtData, gate.tokenC4ChActvtInd, 9, 10);
//		cvtData = formatString(cvtData, gate.tokenC4TermInputCap, 10, 11);
//		cvtData = formatString(cvtData, gate.tokenC4Filter2, 11, 12);
//		
//		//System.out.println("======token C4=>" + cvtData + "--------------------");
//
//		/*
//		cvtData = formatString(cvtData, termAttendInd, 0, 1);
//		cvtData = formatString(cvtData, reserv, 1, 2);
//		cvtData = formatString(cvtData, termLocInd, 2, 3);
//		cvtData = formatString(cvtData, chPresetInd, 3, 4);
//		cvtData = formatString(cvtData, crdPresetInd, 4, 5);
//		cvtData = formatString(cvtData, crdCaptrInd, 5, 6);
//		cvtData = formatString(cvtData, txnStatInd, 6, 7);
//		cvtData = formatString(cvtData, txnSecInd, 7, 8);
//		cvtData = formatString(cvtData, reserv, 8, 9);
//		cvtData = formatString(cvtData, chActvtInd, 9, 10);
//		cvtData = formatString(cvtData, termInputCAP, 10, 11);
//		cvtData = formatString(cvtData, reserv, 11, 12);
//		*/
//		tokenCount++;
//		return createTokenHeader("C4", cvtData) + cvtData;
//	}
//
//	public String createTokenC5() {
//		String cvtData = "";
//		if (gate.installTxInd.equals("I") || gate.installTxInd.equals("E")) {
//			cvtData = spaces.substring(0, 144);
//		} else {
//			cvtData = spaces.substring(0, 120);
//		}
//
//		cvtData = formatString(cvtData, merchantId, 0, 10);
//		cvtData = formatString(cvtData, storeId, 10, 16);
//		cvtData = formatString(cvtData, transId, 16, 18);
//		cvtData = formatString(cvtData, referenceId, 18, 29);
//		cvtData = formatString(cvtData, goodsId, 29, 35);
//		cvtData = formatString(cvtData, personal, 35, 45);
//		cvtData = formatString(cvtData, pin, 45, 49);
//		cvtData = formatString(cvtData, ecFlag, 49, 50);
//
//		if (gate.divMark.equals("I") || gate.divMark.equals("E")) { // 分期交易
//			cvtData = formatString(cvtData, orderNumI, 50, 90);
//			cvtData = formatString(cvtData, gate.divMark, 90, 91);
//			cvtData = formatString(cvtData, gate.installTxRespCde, 91, 93);
//			cvtData = formatString(cvtData, reserv, 93, 120);
//			cvtData = formatString(cvtData, gate.divNum, 120, 122); // 分期數
//			cvtData = formatString(cvtData, gate.firstAmt, 122, 130);// 首期金額
//			cvtData = formatString(cvtData, gate.everyAmt, 130, 138); // 每期金額
//			cvtData = formatString(cvtData, gate.procAmt, 138, 144); // 手續費
//		} else { // 紅利交易
//			cvtData = formatString(cvtData, settleFlg, 50, 52);
//			cvtData = formatString(cvtData, orderNum1, 52, 90);
//			cvtData = formatString(cvtData, gate.loyaltyTxId, 90, 91);
//			cvtData = formatString(cvtData, gate.loyaltyTxResp, 91, 93);
//			cvtData = formatString(cvtData, gate.pointRedemption, 93, 101); // 扣抵點數
//			cvtData = formatString(cvtData, gate.signBalance, 101, 102);
//			cvtData = formatString(cvtData, gate.pointBalance, 102, 110); // 剩餘點數
//			cvtData = formatString(cvtData, gate.paidCreditAmt, 110, 120); // 支付金額
//		}
//
//		tokenCount++;
//		return createTokenHeader("C5", cvtData) + cvtData;
//	}
//
//	public String createTokenCH() {
//		String cvtData = spaces.substring(0, 40);
//
//		boolean bLIsVisa=true;
//		if (gate.isoField[22].length()>=2) {
//			if (gate.isoField[22].substring(0, 2).equals("81"))
//				bLIsVisa=false;
//		}
//
//		if (bLIsVisa) {
//			cvtData = formatString(cvtData, tokenChRespSrcRsnCde4Visa, 0, 1);
//			cvtData = formatString(cvtData, tokenChFilter14Visa, 1, 19);
//			cvtData = formatString(cvtData, tokenChRecurPmntInd4Visa, 19, 20);
//			cvtData = formatString(cvtData, tokenChFilter24Visa, 20, 33);
//			cvtData = formatString(cvtData, tokenChRvslRsnInd4Visa, 33, 34);
//			cvtData = formatString(cvtData, tokenChFilter34Visa, 34, 36);
//			cvtData = formatString(cvtData, tokenChAuthMsgInd4Visa, 36, 37);
//			cvtData = formatString(cvtData, tokenChTermTyp4Visa, 37, 38);
//			cvtData = formatString(cvtData, tokenChFilter44Visa, 38, 40);
//			
//		}
//		else  {
//			cvtData = formatString(cvtData, tokenChFilter14Master , 0, 19);
//			cvtData = formatString(cvtData, tokenChRecurPmntInd4Master  , 19, 20);
//			
//			cvtData = formatString(cvtData, tokenChFilter24Master  , 20, 24);
//			
//			
//			cvtData = formatString(cvtData, gate.tokenChPmntTypInd4Master  , 24, 27);
//			
//			cvtData = formatString(cvtData, tokenChFilter34Master, 27, 33);
//			cvtData = formatString(cvtData, tokenChRvslRsnInd4Master , 33, 34);
//			cvtData = formatString(cvtData, tokenChFilter44Master , 34, 36);
//			
//			
//			cvtData = formatString(cvtData, tokenChAuthMsgInd4Master, 36, 37);
//			cvtData = formatString(cvtData, tokenChTermTypMaster  , 37, 38);
//			cvtData = formatString(cvtData, tokenChFilter54Master    , 38, 40);
//
//		}
//
//		
//		tokenCount++;
//		return createTokenHeader("CH", cvtData) + cvtData;
//	}
//
//	public String createTokenC6() {
//		String cvtData = spaces.substring(0, 80);
//
//		cvtData = formatString(cvtData, gate.xid, 0, 40);
//		cvtData = formatString(cvtData, gate.cavv, 40, 80);
//		tokenCount++;
//
//		return createTokenHeader("C6", cvtData) + cvtData;
//	}
//
//	public String createTokenCE() {
//		String cvtData = spaces.substring(0, 202);
//
//		cvtData = formatString(cvtData, authnIndFlg, 0, 2);
//		cvtData = formatString(cvtData, gate.ucaf, 2, 202);
//		tokenCount++;
//
//		return createTokenHeader("CE", cvtData) + cvtData;
//	}
//
//	public String createTokenCI() {
//		String cvtData = spaces.substring(0, 70);
//
//		cvtData = formatString(cvtData, reserv, 0, 52);
//		cvtData = formatString(cvtData, mcElecAccptInd, 52, 53);
//		cvtData = formatString(cvtData, reserv, 53, 70);
//		tokenCount++;
//
//		return createTokenHeader("CI", cvtData) + cvtData;
//	}
//
//	public String createTokenQ2() {
//		String cvtData = spaces.substring(0, 74);
//
//		cvtData = formatString(cvtData, offLineInd, 0, 1);
//		cvtData = formatString(cvtData, terminalSerNum, 1, 9);
//		cvtData = formatString(cvtData, gate.emvDFED, 9, 10);
//		cvtData = formatString(cvtData, gate.isoField[39], 10, 12);
//		cvtData = formatString(cvtData, gate.emv9B, 12, 16);
//		int cvtLength = gate.emv91.length();
//		String lengthData = "" + cvtLength;
//		if (cvtLength < 10) {
//			lengthData = "0" + cvtLength;
//		}
//		cvtData = formatString(cvtData, (lengthData + gate.emv91), 16, 50);
//		cvtData = formatString(cvtData, gate.emv9F34, 50, 56);
//		cvtData = formatString(cvtData, gate.emv9F09, 56, 60);
//		cvtData = formatString(cvtData, gate.emv9F41, 60, 70);
//		cvtData = formatString(cvtData, iCCAppVersion, 70, 74);
//		tokenCount++;
//
//		return createTokenHeader("Q2", cvtData) + cvtData;
//	}
//
//	public String createTokenQ3() {
//		String cvtData = spaces.substring(0, 10);
//
//		cvtData = formatString(cvtData, jcbStipInst, 0, 1);
//		cvtData = formatString(cvtData, jcbStipReason, 1, 2);
//		cvtData = formatString(cvtData, jcbStipRjeReason, 2, 4);
//		cvtData = formatString(cvtData, reserv, 4, 10);
//		tokenCount++;
//
//		return createTokenHeader("Q3", cvtData) + cvtData;
//	}
//
//	public String createTokenW7() {
//		String cvtData = spaces.substring(0, 34);
//
//		int cvtLength = chSerNumber.length();
//		cvtData = formatNumber(cvtData, (cvtLength + ""), 0, 2);
//		cvtData = formatString(cvtData, chSerNumber, 2, 34);
//		tokenCount++;
//
//		return createTokenHeader("W7", cvtData) + cvtData;
//	}
//
//	public String createTokenW8() {
//		String cvtData = spaces.substring(0, 34);
//
//		int cvtLength = mchtSerNumber.length();
//		cvtData = formatNumber(cvtData, (cvtLength + ""), 0, 2);
//		cvtData = formatString(cvtData, mchtSerNumber, 2, 34);
//		tokenCount++;
//
//		return createTokenHeader("W8", cvtData) + cvtData;
//	}
//
//	public String createTokenWB() {
//		String cvtData = spaces.substring(0, 120);
//
//		cvtData = formatString(cvtData, bnetEcCertRqst, 0, 95);
//		cvtData = formatString(cvtData, bnetEcCertResp, 95, 120);
//		tokenCount++;
//
//		return createTokenHeader("WB", cvtData) + cvtData;
//	}
//
//	public String createTokenWV() {
//		String cvtData = spaces.substring(0, 120);
//
//		cvtData = formatString(cvtData, visaEcCertRqst, 0, 120);
//		tokenCount++;
//
//		return createTokenHeader("WV", cvtData) + cvtData;
//	}
//
//	private String formatNumber(String destValue, String cvtValue, int startPnt, int endPnt) {
//
//		if (cvtValue == null) {
//			cvtValue = "";
//		}
//
//		int len = endPnt - startPnt;
//
//		cvtValue = fillZero(cvtValue, len);
//		destValue = destValue.substring(0, startPnt) + cvtValue + destValue.substring(endPnt);
//
//		return destValue;
//	}
//
//	private String formatString(String destValue, String cvtValue, int startPnt, int endPnt) {
//
//		if (cvtValue == null) {
//			cvtValue = "";
//		}
//
//		int len = endPnt - startPnt;
//
//		cvtValue = fillSpace(cvtValue, len);
//		destValue = destValue.substring(0, startPnt) + cvtValue + destValue.substring(endPnt);
//
//		return destValue;
//	}
//
//	private String formatLeftString(String destValue, String cvtValue, int startPnt, int endPnt) {
//
//		if (cvtValue == null) {
//			cvtValue = "";
//		}
//
//		int len = endPnt - startPnt;
//
//		cvtValue = fillLeftSpace(cvtValue, len);
//		destValue = destValue.substring(0, startPnt) + cvtValue + destValue.substring(endPnt);
//
//		return destValue;
//	}
//
//	private String fillZero(String value, int length) {
//		if (value == null)
//			return zeros.substring(0, length);
//		int len = value.length();
//		if (len == length)
//			return value;
//		else if (len < length)
//			return zeros.substring(0, length - len) + value;
//		else
//			return value.substring(0, length);
//	}
//
//	private String fillSpace(String value, int length) {
//		if (value == null)
//			return spaces.substring(0, length);
//		int len = value.length();
//		if (len == length)
//			return value;
//		else if (len < length)
//			return value + spaces.substring(0, length - len);
//		else
//			return value.substring(0, length);
//	}
//
//	private String fillLeftSpace(String value, int length) {
//		if (value == null)
//			return spaces.substring(0, length);
//		int len = value.length();
//		if (len == length)
//			return value;
//		else if (len < length)
//			return spaces.substring(0, length - len) + value;
//		else
//			return value.substring(0, length);
//	}
//
//	static public String unpackToString(byte[] bcdData, int size) {
//		String dest = "", lByte = "", rByte = "";
//		int i = 0, cnt = 0, cvt = 0, left = 0, right = 0, offset = 0;
//
//		for (i = 0; i < size; i++) {
//			cvt = bcdData[offset];
//			if (cvt < 0) {
//				cvt += 256;
//			}
//			lByte = Integer.toHexString(cvt / 16);
//			lByte = lByte.toUpperCase();
//			rByte = Integer.toHexString(cvt % 16);
//			rByte = rByte.toUpperCase();
//			dest = dest + lByte + rByte;
//			offset++;
//		}
//
//		if ((size % 2) != 0) {
//			dest = dest.substring(1, dest.length());
//		}
//
//		return dest;
//	}

}