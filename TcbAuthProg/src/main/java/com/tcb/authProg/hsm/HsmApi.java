/**
 * 授權使用HSM_API存取物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用HSM_API存取物件                      *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 * 2021/02/08  V1.00.02  shiyuqi     updated for project coding standard      *                                                                         *
 ******************************************************************************
 */

package com.tcb.authProg.hsm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.SecureRandom;

import javax.net.SocketFactory;

import com.tcb.authProg.util.HpeUtil;

public class HsmApi {
	String sGResponseCode = "";
	String sGReturnCode = "";
	String sGReturnMsg = "";
	String sGHsmServerIp = "";
	int nGHsmServerPort = 0;

	public HsmApi(String sPHsmServerIp, int nPHsmServerPort) {
		this.sGHsmServerIp = sPHsmServerIp;
		this.nGHsmServerPort = nPHsmServerPort;
	}

	public String hsmCommandM4(String sPSourceModeFlag, String sPDestModeFlag, String sPInputFormatFlag,
			String sPOutputFormatFlag, String sPSourceKeyType, String sPSourceKey, String sPSourceKsnDesc,
			String sPSourceKeySerialNumber, String sPDestKeyType, String sPDestKey, String sPDestKsnDesc,
			String sPDestKeySerialNumber, String sPSourceIv, String sPDestIv, String sPMesgLength,
			String sPEncryptedMesg) {
		String sLResult = "";

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "M4";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPSourceModeFlag;
		sLHsmCommand = sLHsmCommand + sPDestModeFlag;
		sLHsmCommand = sLHsmCommand + sPInputFormatFlag;
		sLHsmCommand = sLHsmCommand + sPOutputFormatFlag;
		sLHsmCommand = sLHsmCommand + sPSourceKeyType;
		sLHsmCommand = sLHsmCommand + sPSourceKey;
		sLHsmCommand = sLHsmCommand + sPSourceKsnDesc;
		sLHsmCommand = sLHsmCommand + sPSourceKeySerialNumber;
		sLHsmCommand = sLHsmCommand + sPDestKeyType;

		sLHsmCommand = sLHsmCommand + sPDestKey;
		sLHsmCommand = sLHsmCommand + sPDestKsnDesc;
		sLHsmCommand = sLHsmCommand + sPDestKeySerialNumber;
		sLHsmCommand = sLHsmCommand + sPSourceIv;
		sLHsmCommand = sLHsmCommand + sPDestIv;

		sLHsmCommand = sLHsmCommand + sPMesgLength;
		sLHsmCommand = sLHsmCommand + sPEncryptedMesg;

		String sLHsmResponse = null;
		try {
			sLHsmResponse = executeHsmCmd(sLHsmCommand);
		} catch (Exception e) {
			return "hsmCommandM4 error";
		}
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("M5".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public byte[] hsmCommandLQReturnByteAry(String sPHashIdentifier, String sPHmacLen, String sPHmacKeyFormat,
			String sPHmacKeyLen, String sPHmacKey, String sPDelimiter, String sPMesgLen, String sPMesgData) {
		byte[] lResult = null;

		ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		try {
			lByteAryOutputStream.write(sLMsgHeader.getBytes());

			String sLCommandCode = "LQ";
			sLHsmCommand = sLHsmCommand + sLCommandCode;
			lByteAryOutputStream.write(sLCommandCode.getBytes());

			sLHsmCommand = sLHsmCommand + sPHashIdentifier;
			lByteAryOutputStream.write(sPHashIdentifier.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacLen;
			lByteAryOutputStream.write(sPHmacLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
			lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
			lByteAryOutputStream.write(sPHmacKeyLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKey;
			byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
			lByteAryOutputStream.write(lHmacKeyByteAry);

			sLHsmCommand = sLHsmCommand + sPDelimiter;
			lByteAryOutputStream.write(sPDelimiter.getBytes());

			sLHsmCommand = sLHsmCommand + sPMesgLen;
			lByteAryOutputStream.write(sPMesgLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPMesgData;
			lByteAryOutputStream.write(sPMesgData.getBytes());
		} catch (IOException e) {
			return null;
		}
		byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
		byte[] lHsmResponseByteAry = null;
//		try {
			lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);
//		} catch (Exception e) {
//			return null;
//		}

		String sLHsmResponse = new String(lHsmResponseByteAry);

		String sLResponseCode = "";
//		String sLHmac = "";
		String sLHmacLen = "";
		int nLHmacBeginPos = 0;
//		int nLHmacEndPos = 0;
		if (sLHsmResponse.length() > 6) {
			sLResponseCode = sLHsmResponse.substring(4, 6);
			sLHmacLen = sLHsmResponse.substring(6, 10);
			nLHmacBeginPos = 10;
		}
		if (sLHsmResponse.length() >= 8) {
			sLResponseCode = sLHsmResponse.substring(6, 8);
			sLHmacLen = sLHsmResponse.substring(8, 12);
			nLHmacBeginPos = 12;
		}
		int nLHmacDataLen = Integer.parseInt(sLHmacLen);
//		nLHmacEndPos = nLHmacBeginPos + nLHmacDataLen;
		byte[] lHmacData = new byte[nLHmacDataLen];

		System.arraycopy(lHsmResponseByteAry, nLHmacBeginPos, lHmacData, 0, nLHmacDataLen);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {
			outputStream.write(sLResponseCode.getBytes());
			if ("00".equals(sLResponseCode)) {
				outputStream.write(sLHmacLen.getBytes());
				outputStream.write(lHmacData);
			}
		} catch (IOException e) {
			return null;
		}

		lResult = outputStream.toByteArray();

		return lResult;
	}

	public String hsmCommandLQ(String sPHashIdentifier, String sPHmacLen, String sPHmacKeyFormat, String sPHmacKeyLen,
			String sPHmacKey, String sPDelimiter, String sPMesgLen, String sPMesgData) {
		String sLResult = "";

		ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		try {
			lByteAryOutputStream.write(sLMsgHeader.getBytes());

			String sLCommandCode = "LQ";
			sLHsmCommand = sLHsmCommand + sLCommandCode;
			lByteAryOutputStream.write(sLCommandCode.getBytes());

			sLHsmCommand = sLHsmCommand + sPHashIdentifier;
			lByteAryOutputStream.write(sPHashIdentifier.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacLen;
			lByteAryOutputStream.write(sPHmacLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
			lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
			lByteAryOutputStream.write(sPHmacKeyLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPHmacKey;
			byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
			lByteAryOutputStream.write(lHmacKeyByteAry);

			sLHsmCommand = sLHsmCommand + sPDelimiter;
			lByteAryOutputStream.write(sPDelimiter.getBytes());

			sLHsmCommand = sLHsmCommand + sPMesgLen;
			lByteAryOutputStream.write(sPMesgLen.getBytes());

			sLHsmCommand = sLHsmCommand + sPMesgData;
			lByteAryOutputStream.write(sPMesgData.getBytes());
		} catch (IOException e) {
			return "hsmCommandLQ error";
		}
		byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
		byte[] lHsmResponseByteAry = null;
		try {
			lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);
		} catch (Exception e) {
			return "hsmCommandLQ error";
		}

		String sLHsmResponse = new String(lHsmResponseByteAry);

		String sLResponseCode = "";
//		String sLHmac = "";
		String sLHmacLen = "";
		int nLHmacBeginPos = 0;
//		int nLHmacEndPos = 0;
		if (sLHsmResponse.length() > 6) {
			sLResponseCode = sLHsmResponse.substring(4, 6);
			sLHmacLen = sLHsmResponse.substring(6, 10);
			nLHmacBeginPos = 10;
		}
		if (sLHsmResponse.length() >= 8) {
			sLResponseCode = sLHsmResponse.substring(6, 8);
			sLHmacLen = sLHsmResponse.substring(8, 12);
			nLHmacBeginPos = 12;
		}
		int nLHmacDataLen = Integer.parseInt(sLHmacLen);
//		nLHmacEndPos = nLHmacBeginPos + nLHmacDataLen;
		byte[] lHmacData = new byte[nLHmacDataLen];

		System.arraycopy(lHsmResponseByteAry, nLHmacBeginPos, lHmacData, 0, nLHmacDataLen);

		sLResult = sLResponseCode;
		if ("00".equals(sLResponseCode)) {
			sLResult = sLResult + sLHmacLen;
			String sLHmacHex = HpeUtil.getByteHex(lHmacData);
			sLResult = sLResult + sLHmacHex;
		}
		return sLResult;
	}

	public String hsmCommandLS(String sPHashIdentifier, String sPHmacLen, String sPHmac, String sPHmacKeyFormat,
			String sPHmacKeyLen, String sPHmacKey, String sPDelimiter, String sPDataLen, String sPMesgData)
			{
		String sLResult = "";
		byte[] lHmacByteAry = HpeUtil.transHexString2ByteAry(sPHmac);

		ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		try {
			lByteAryOutputStream.write(sLMsgHeader.getBytes());

			String sLCommandCode = "LS";
			sLHsmCommand = sLHsmCommand + sLCommandCode;
			lByteAryOutputStream.write(sLCommandCode.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPHashIdentifier;
			lByteAryOutputStream.write(sPHashIdentifier.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPHmacLen;
			lByteAryOutputStream.write(sPHmacLen.getBytes());
	
			lByteAryOutputStream.write(lHmacByteAry);
	
			sLHsmCommand = sLHsmCommand + sPHmacKeyFormat;
			lByteAryOutputStream.write(sPHmacKeyFormat.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPHmacKeyLen;
			lByteAryOutputStream.write(sPHmacKeyLen.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPHmacKey;
			byte[] lHmacKeyByteAry = HpeUtil.transHexString2ByteAry(sPHmacKey);
			lByteAryOutputStream.write(lHmacKeyByteAry);
	
			sLHsmCommand = sLHsmCommand + sPDelimiter;
			lByteAryOutputStream.write(sPDelimiter.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPDataLen;
			lByteAryOutputStream.write(sPDataLen.getBytes());
	
			sLHsmCommand = sLHsmCommand + sPMesgData;
			lByteAryOutputStream.write(sPMesgData.getBytes());
		} catch (IOException e) {
			return "hsmCommandLS error";
		}

		byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
		byte[] lHsmResponseByteAry = null;
		try {
			lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);
		} catch (Exception e) {
			return "hsmCommandLS error";
		}

		String sLHsmResponse = new String(lHsmResponseByteAry);
//		System.out.println("###" + sLHsmResponse + "###" + lHsmResponseByteAry.length + "--");

		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("LT".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandCW(String sPCardNo, String sPExpireDate, String sPServiceCode, String sPCsseccfgCvka,
			String sPCsseccfgCvkb) {
		String sLResultCVV = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "CW";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPCsseccfgCvka + sPCsseccfgCvkb;

		sLHsmCommand = sLHsmCommand + sPCardNo;

		sLHsmCommand = sLHsmCommand + ";";
		sLHsmCommand = sLHsmCommand + sPExpireDate + sPServiceCode;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("CX".equals(this.sGResponseCode))) {
			sLResultCVV = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResultCVV = this.sGReturnCode;
		}
		return sLResultCVV;
	}

	public String hsmCommandJE(String sPZpk, String sPSourcePinBlock, String sPAccountNumber,
			String sPSourcePinBlockFormatCode) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "JE";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPZpk;

		sLHsmCommand = sLHsmCommand + sPSourcePinBlock;

		sLHsmCommand = sLHsmCommand + sPSourcePinBlockFormatCode;

		sLHsmCommand = sLHsmCommand + sPAccountNumber;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("JF".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandM0(String sPModeFlag, String sPInputFormat, String sPOutputFormat, String sPKeyType,
			String sPKey, String sPKeyDescriptor, String sPKeySerialNumber, String sPIv, String sPMsgLength,
			String sPMsgToBeEncrypted) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLKeyType = "FFF";
		if (!"".equals(sPKeyType)) {
			sLKeyType = sPKeyType;
		}
		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "M0";
		sLHsmCommand = sLMsgHeader + sLCommandCode + sPModeFlag + sPInputFormat + sPOutputFormat + sLKeyType + sPKey
				+ sPKeyDescriptor + sPKeySerialNumber + sPIv + sPMsgLength + sPMsgToBeEncrypted;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("M1".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandM2(String sPModeFlag, String sPInputFormat, String sPOutputFormat, String sPKey,
			String sPKsnDescriptor, String sPKeySerialNumber, String sPIV, String sPMsgLength,
			String sPMsgToBeDecrypted, String sPKeyType) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLKeyType = "609";
		if (sPKeyType.length() > 0) {
			sLKeyType = sPKeyType;
		}
		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "M2";
		sLHsmCommand = sLMsgHeader + sLCommandCode + sPModeFlag + sPInputFormat + sPOutputFormat + sLKeyType + sPKey
				+ sPKsnDescriptor + sPKeySerialNumber + sPIV + sPMsgLength + sPMsgToBeDecrypted;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("M3".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandDG(String sPPvk, String sPPin, String sPAccountNumber, String sPPvki) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "DG";
		sLHsmCommand = sLMsgHeader + sLCommandCode + sPPvk + sPPin + sPAccountNumber + sPPvki;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("DH".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandRY(String sPMode, String sPFlag, String sPCsck, String sPAccountNumber, String sPExpireDate,
			String sPServiceCode, String sPZmk, String sP5DigitCsc, String sP4DigitCsc, String sP3DigitCsc) {
		
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "RY";
		if ("0".equals(sPMode)) {
			sLHsmCommand = sLMsgHeader + sLCommandCode;
		} else if ("1".equals(sPMode)) {
			sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck + sPAccountNumber + sPExpireDate
					+ sPServiceCode;
		} else if ("2".equals(sPMode)) {
			sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck + sPAccountNumber + sPExpireDate
					+ sPServiceCode;
		} else if ("3".equals(sPMode)) {
			sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck + sPAccountNumber + sPExpireDate
					+ sPServiceCode;
		} else if ("4".equals(sPMode)) {
			sLHsmCommand = sLMsgHeader + sLCommandCode + sPMode + sPFlag + sPCsck + sPAccountNumber + sPExpireDate
					+ sPServiceCode + sP5DigitCsc + sP4DigitCsc + sP3DigitCsc;
		}
		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("RZ".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandJA(String sPAccountNumber, String sPPinLength) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "JA";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPAccountNumber + sPPinLength;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("JB".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandFA(String sPZmk, String sPZpk) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "FA";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPZmk + sPZpk;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("FB".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandEC(String sPZpk, String sPPvk, String sPPinBlock, String sPPinBlockFormatCode,
			String sPPanOrToken, String sPPvki, String sPPVV) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "EC";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPZpk + sPPvk + sPPinBlock + sPPinBlockFormatCode + sPPanOrToken
				+ sPPvki + sPPVV;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("ED".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandCC(String sPSrcZpk, String sPDestZpk, String sPMaxPinLength, String sPSrcPinBlock,
			String sPSrcPinBlockFormatCode, String sPDestPinBlockFormatCode, String sPPanOrToken) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "CC";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPSrcZpk + sPDestZpk + sPMaxPinLength + sPSrcPinBlock
				+ sPSrcPinBlockFormatCode + sPDestPinBlockFormatCode + sPPanOrToken;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("CD".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	private int getRandomNumber(int nLMaxNum) {
		int nLResult = 0;

		SecureRandom lRandomGen = new SecureRandom();

		nLResult = lRandomGen.nextInt(nLMaxNum);
		lRandomGen = null;

		return nLResult;
	}

	private String fillZeroOnLeft(double dPSrc, int nPTargetLen) {
		int nLSrc = (int) dPSrc;

		return String.format("%0" + nPTargetLen + "d", new Object[] { Integer.valueOf(nLSrc) });
	}

	private String getMsgHeader() {
		String sLTmpNo = Integer.toString(getRandomNumber(999) + getRandomNumber(999));

		String sLMsgHeader = fillZeroOnLeft(Double.parseDouble(sLTmpNo), 6);

		sLMsgHeader = sLMsgHeader.substring(2, 6);

		return sLMsgHeader;
	}

	public String hsmCommandCY(String sPCardNo, String sPExpireDate, String sPServiceCode, String sPCvv,
			String sPCsseccfgCvka, String sPCsseccfgCvkb) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "CY";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPCsseccfgCvka + sPCsseccfgCvkb;

		sLHsmCommand = sLHsmCommand + sPCvv + sPCardNo;

		sLHsmCommand = sLHsmCommand + ";";
		sLHsmCommand = sLHsmCommand + sPExpireDate + sPServiceCode;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("CZ".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandBE(String sPZpk, String sPPinBlock, String sPPinBlockFormatCode, String sPAccountNumber,
			String sPPin) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "BE";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPZpk + sPPinBlock + sPPinBlockFormatCode + sPAccountNumber + sPPin;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("BF".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandPA(String sPData) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "PA";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPData;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("PB".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandPI(String sPDocType, String sPAccountNumber, String sPPin, String sPAllPrintField) {
		
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		sLHsmCommand = sLMsgHeader;

		String sLCommandCode = "PI";
		sLHsmCommand = sLHsmCommand + sLCommandCode;

		sLHsmCommand = sLHsmCommand + sPDocType + sPAccountNumber + sPPin + sPAllPrintField;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("PJ".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandNG(String sPAccountNumber, String sPPin) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "NG";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPAccountNumber + sPPin;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("NH".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	public String hsmCommandNO() {
		String sLResult = "";
		String sLHsmCommand = "";
		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		String sLCommandCode = "NO00";//
		sLHsmCommand = sLHsmCommand + sLCommandCode;
		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);

		if (("00".equals(sGReturnCode)) && ("NP".equals(sGResponseCode)))
			sLResult = sGReturnCode + sGReturnMsg;
		else
			sLResult = sGReturnCode;
		return sLResult;
	}

	public String hsmCommandKQX(String sLModeFlag, String sLSchemeId, String sLMkAc, String sLPanData, String sLAtc,
			String sLUn, String sLTxnLen, String sLTxn, String sLInput, String sLArc) {
		String sLResult = "";
		String sLHsmCommand = "";
		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		String sLCommandCode = "KQ";
		if ("0".equals(sLModeFlag)) {
			sLHsmCommand = sLHsmCommand + sLCommandCode + sLModeFlag + sLSchemeId + "U" + sLMkAc + sLPanData + sLAtc
					+ sLUn + sLTxnLen + sLTxn + ";" + sLInput;
		} else {
			sLHsmCommand = sLHsmCommand + sLCommandCode + sLModeFlag + sLSchemeId + "U" + sLMkAc + sLPanData + sLAtc
					+ sLUn + sLInput + sLArc;
		}

		;
//		System.out.println("@@@@HSM_KQ@@@@RESPONSE=" + sLHsmCommand);
		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
//		System.out.println("@@@@HSM_KR@@@@RESPONSE=" + sLHsmResponse);

		if (("00".equals(sGReturnCode)) && ("KR".equals(sGResponseCode)))
			sLResult = sGReturnCode + sGReturnMsg;
		else
			sLResult = sGReturnCode;
		return sLResult;
	}

	public String hsmCommandKQ(String sLModeFlag, String sLSchemeId, String sLMkAc, String sLPanData, String sLAtc,
			String sLUn, String sLTxnLen, String sLTxn, String sLInput, String sLArc) {
		String sLResult = "";

		ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		try {
			lByteAryOutputStream.write(sLMsgHeader.getBytes());

			String sLCommandCode = "KQ";
			sLHsmCommand = sLHsmCommand + sLCommandCode;
			lByteAryOutputStream.write(sLCommandCode.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLModeFlag;
			lByteAryOutputStream.write(sLModeFlag.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLSchemeId;
			lByteAryOutputStream.write(sLSchemeId.getBytes());
	
			String sLMkacInd = "U";
			sLHsmCommand = sLHsmCommand + sLMkacInd;
			lByteAryOutputStream.write(sLMkacInd.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLMkAc;
			lByteAryOutputStream.write(sLMkAc.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLPanData;
			byte[] lPanDataByteAry = HpeUtil.transHexString2ByteAry(sLPanData);
			lByteAryOutputStream.write(lPanDataByteAry);
	
			sLHsmCommand = sLHsmCommand + sLAtc;
			byte[] lAtcByteAry = HpeUtil.transHexString2ByteAry(sLAtc);
			lByteAryOutputStream.write(lAtcByteAry);
	
			sLHsmCommand = sLHsmCommand + sLUn;
			byte[] lUnByteAry = HpeUtil.transHexString2ByteAry(sLUn);
			lByteAryOutputStream.write(lUnByteAry);
	
			sLHsmCommand = sLHsmCommand + sLTxnLen;
			lByteAryOutputStream.write(sLTxnLen.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLTxn;
			byte[] lTxnByteAry = HpeUtil.transHexString2ByteAry(sLTxn);
			lByteAryOutputStream.write(lTxnByteAry);
	
			String sLSplit = ";";
			sLHsmCommand = sLHsmCommand + sLSplit;
			lByteAryOutputStream.write(sLSplit.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLInput;
			byte[] lInputByteAry = HpeUtil.transHexString2ByteAry(sLInput);
			lByteAryOutputStream.write(lInputByteAry);
	
			if ("1".equals(sLModeFlag)) {
				sLHsmCommand = sLHsmCommand + sLArc;
				byte[] lArcByteAry = HpeUtil.transHexString2ByteAry(sLArc);
				lByteAryOutputStream.write(lArcByteAry);
			}
		} catch (IOException e) {
			return "hsmCommandKQ error";
		}
//		System.out.println("@@@@HSM_KQ=" + lByteAryOutputStream);
		byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
//		System.out.println("@@@@HSM_KQ=" + HpeUtil.byte2Hex(lCmdByteAry));
		byte[] lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);

		String sLHsmResponse = new String(lHsmResponseByteAry);
//		System.out.println("@@@@HSM_KR=" + sLHsmResponse);

		String sLResponseCode = "";
//	    String sLArpc = "";
//	    int nLArpcBeginPos = 0;
//	    int nLArpcEndPos = 0;
		if (sLHsmResponse.length() >= 8) {
			sLResponseCode = sLHsmResponse.substring(6, 8);
			if ((sLHsmResponse.length() > 8) && ("00".equals(sLResponseCode))) {
				int nLArpcBeginPos = 8;
				int nLArpcDataLen = 8;
//	    		int nLArpcEndPos = nLArpcBeginPos + nLArpcDataLen;
				byte[] lArpcData = new byte[nLArpcDataLen];
				System.arraycopy(lHsmResponseByteAry, nLArpcBeginPos, lArpcData, 0, nLArpcDataLen);
				sLResult = sLResponseCode;
				String sLArpcHex = HpeUtil.getByteHex(lArpcData);
				sLResult = sLResult + sLArpcHex;
			} else {
				sLResult = sLResponseCode;
			}
		}
		return sLResult;
	}

	public String hsmCommandKW(String sLModeFlag, String sLSchemeId, String sLMkAc, String sLPanData, String sLAtc,
			String sLUn, String sLTxnLen, String sLTxn, String sLInput, String sLArc) {
		String sLResult = "";

		ByteArrayOutputStream lByteAryOutputStream = new ByteArrayOutputStream();

		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();
		sLHsmCommand = sLMsgHeader;
		try {
			lByteAryOutputStream.write(sLMsgHeader.getBytes());


			String sLCommandCode = "KW";
			sLHsmCommand = sLHsmCommand + sLCommandCode;
			lByteAryOutputStream.write(sLCommandCode.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLModeFlag;
			lByteAryOutputStream.write(sLModeFlag.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLSchemeId;
			lByteAryOutputStream.write(sLSchemeId.getBytes());
	
			String sLMkacInd = "U";
			sLHsmCommand = sLHsmCommand + sLMkacInd;
			lByteAryOutputStream.write(sLMkacInd.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLMkAc;
			lByteAryOutputStream.write(sLMkAc.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLPanData;
			byte[] lPanDataByteAry = HpeUtil.transHexString2ByteAry(sLPanData);
			lByteAryOutputStream.write(lPanDataByteAry);
	
			sLHsmCommand = sLHsmCommand + sLAtc;
			byte[] lAtcByteAry = HpeUtil.transHexString2ByteAry(sLAtc);
			lByteAryOutputStream.write(lAtcByteAry);
	
	//	    sLHsmCommand = sLHsmCommand + sLUn;
	//	    byte[] lUnByteAry = HpeUtil.transHexString2ByteAry(sLUn);
	//	    lByteAryOutputStream.write(lUnByteAry);
	
			sLHsmCommand = sLHsmCommand + sLTxnLen;
			lByteAryOutputStream.write(sLTxnLen.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLTxn;
			byte[] lTxnByteAry = HpeUtil.transHexString2ByteAry(sLTxn);
			lByteAryOutputStream.write(lTxnByteAry);
	
			String sLSplit = ";";
			sLHsmCommand = sLHsmCommand + sLSplit;
			lByteAryOutputStream.write(sLSplit.getBytes());
	
			sLHsmCommand = sLHsmCommand + sLInput;
			byte[] lInputByteAry = HpeUtil.transHexString2ByteAry(sLInput);
			lByteAryOutputStream.write(lInputByteAry);
	
			if ("1".equals(sLModeFlag)) {
				sLHsmCommand = sLHsmCommand + sLArc;
				byte[] lArcByteAry = HpeUtil.transHexString2ByteAry(sLArc);
				lByteAryOutputStream.write(lArcByteAry);
			}
		} catch (IOException e) {
			return "hsmCommandKW error";
		}
//		System.out.println("@@@@HSM_KW=" + lByteAryOutputStream);
		byte[] lCmdByteAry = HpeUtil.addLength2HeadOfByteAry(lByteAryOutputStream.toByteArray());
//		System.out.println("@@@@HSM_KW=" + HpeUtil.byte2Hex(lCmdByteAry));
		byte[] lHsmResponseByteAry = executeHsmCmd(lCmdByteAry);

		String sLHsmResponse = new String(lHsmResponseByteAry);
//		System.out.println("@@@@HSM_KX=" + sLHsmResponse);

		String sLResponseCode = "";
//	    String sLArpc = "";
//	    int nLArpcBeginPos = 0;
//	    int nLArpcEndPos = 0;
		if (sLHsmResponse.length() >= 8) {
			sLResponseCode = sLHsmResponse.substring(6, 8);
			if ((sLHsmResponse.length() > 8) && ("00".equals(sLResponseCode))) {
				int nLArpcBeginPos = 8;
				int nLArpcDataLen = 8;
//	    		int nLArpcEndPos = nLArpcBeginPos + nLArpcDataLen;
				byte[] lArpcData = new byte[nLArpcDataLen];
				System.arraycopy(lHsmResponseByteAry, nLArpcBeginPos, lArpcData, 0, nLArpcDataLen);
				sLResult = sLResponseCode;
				String sLArpcHex = HpeUtil.getByteHex(lArpcData);
				sLResult = sLResult + sLArpcHex;
			} else {
				sLResult = sLResponseCode;
			}
		}
		return sLResult;
	}

	public String hsmCommandBA(String sPAccountNumber, String sPPin) {
		String sLResult = "";
		String sLHsmCommand = "";

		String sLMsgHeader = getMsgHeader();

		String sLCommandCode = "BA";

		sLHsmCommand = sLMsgHeader + sLCommandCode + sPPin + "F" + sPAccountNumber;

		String sLHsmResponse = executeHsmCmd(sLHsmCommand);
		getHsmExecuteResult(sLHsmResponse);
		if (("00".equals(this.sGReturnCode)) && ("BB".equals(this.sGResponseCode))) {
			sLResult = this.sGReturnCode + this.sGReturnMsg;
		} else {
			sLResult = this.sGReturnCode;
		}
		return sLResult;
	}

	private void getHsmExecuteResult(String sPHsmResponse) {
		if (sPHsmResponse.length() > 6) {
			this.sGResponseCode = sPHsmResponse.substring(4, 6);
		}
		if (sPHsmResponse.length() >= 8) {
			this.sGReturnCode = sPHsmResponse.substring(6, 8);
		}
		String sLResponseHeader = sPHsmResponse.substring(0, 8);
		if ("00".equals(this.sGReturnCode)) {
			this.sGReturnMsg = sPHsmResponse.substring(sLResponseHeader.length(), sPHsmResponse.length());
		} else {
			this.sGReturnMsg = this.sGReturnCode;
		}
	}

	private byte[] executeHsmCmd(byte[] pCommandByteAry) {
		Socket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
//		String response = null;
		byte[] respData = new byte[1024];
		try {
			socket = new Socket(this.sGHsmServerIp, this.nGHsmServerPort);
			if (socket != null) {
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				out.write(pCommandByteAry);

				out.flush();

				byte[] lenData = new byte[3];

				int nLHeadLen = in.read(lenData, 0, 2);
				if (nLHeadLen == 2) {
					int packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);

					if (packetLen > respData.length) {
//						throw new RuntimeException("packet length is too long.");
						respData = null;
					}
					in.read(respData, 0, packetLen);
				}
				else {
					//head length error
					respData = null;
				}
				socket.close();
			}

		} catch (Exception ex) {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					respData = null;
				}
			}
//			System.out.println("HSM hsmCmd Error." + ex);
//			throw ex;
			respData = null;
		}
		return respData;
	}

	private String executeHsmCmd(String command) {
		Socket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		String response = null;
		try {
			socket = SocketFactory.getDefault().createSocket();

//      socket = new Socket(this.sG_HsmServerIp, this.nG_HsmServerPort);
			if (socket != null) {
				SocketAddress remoteaddr = new InetSocketAddress(this.sGHsmServerIp, this.nGHsmServerPort);
				socket.setSoTimeout(2000);
				socket.connect(remoteaddr, 2000); // 就在connect時設timeout
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

				out.writeUTF(command);

//				System.out.println("To HSM:[" + command + "]");

				out.flush();
				response = in.readUTF();

				socket.close();
			}
		} catch (Exception ex) {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {

				}
			}
//      System.out.println("HSM hsmCmd Error." + ex);
//      throw ex;
			response = "HSM hsmCmd Error.";
		}
		return response;
	}

//  public static void main(String[] args) {
//    try {
//      HsmUtil lHsmUtil = new HsmUtil("134.251.83.223", 1500);
//
//
//      String sLHashIdentifier = "08";
//      String sLHmacLen = "0032";
//      String sLHmacKeyFormat = "00";
//      String sLHmacKeyLen = "0040";
//      String sLHmacKey =
//          "C1C51B1535778FF511929BDFC5EBC82018C142DFB48FDE1E506AF45831D9F9CC0E8BA5D8FADF60EB";
//      String sLDelimiter = ";";
//      String sLMesgLen = "00020";
//      String sLMesgData = "0010CAFC6D3323644EB3";
//
//
//      byte[] arrayOfByte =
//          lHsmUtil.hsmCommandLQReturnByteAry(sLHashIdentifier, sLHmacLen, sLHmacKeyFormat,
//              sLHmacKeyLen, sLHmacKey, sLDelimiter, sLMesgLen, sLMesgData);
//    } catch (Exception localException) {
//    }
//  }
}
