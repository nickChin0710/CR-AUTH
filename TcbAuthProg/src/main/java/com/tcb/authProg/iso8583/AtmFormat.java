/**
 * 授權使用ATM ISO8583格式轉換物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用ATM ISO8583格式轉換物件               *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 * 2022/05/04  V1.00.02  Kevin       ATM預借現金密碼變更功能開發
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

//import org.apache.log4j.*;
import org.apache.logging.log4j.Logger;

import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class AtmFormat extends ConvertMessage implements FormatInterChange {

	public String byteMap = "", isoString = "", retCode = "";
	public String zeros = "", spaces = "", fiid = "", dpcNum = "", lNet = "";
//    public  String[] iso117 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
//    public  String[] iso120 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
//    public  String[] iso121 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
//    public  String[] iso122 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
//    public  String[] iso125 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
//    public  String[] iso126 = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
	private int offset = 0, k = 0;

//    public AtmFormat(Logger logger,AuthGate gate,HashMap cvtHash) {
	public AtmFormat(Logger logger, AuthTxnGate gate) {
		super.logger = logger;
		super.gate = gate;
//      super.cvtHash = cvtHash;
	}

	/* ATM change to ECS format */
	public boolean iso2Host() {
		try {

//        String cvtString = "";
//        int cnt = 0;

//    	System.arraycopy(gate.isoData, 0, gate.atmHead, 0, gate.atmHead.length);
			isoString = new String(gate.isoData, 0, gate.dataLen);

			offset = 0;
			gate.isoString = isoString;
			logger.debug("AtmFormat isoString=" + gate.isoString);

			offset = 0;
			String sLFiller = "", sLAtmLen = "", sLCaAmt = "";
			sLAtmLen = getIsoFixLenStrToHost(4, false);
			gate.atmHead = getIsoFixLenStrToHost(9, true);
			gate.mesgType = getIsoFixLenStrToHost(4, true);
			logger.debug("AtmFormat mesgType=" + gate.mesgType);
			gate.pCode = getIsoFixLenStrToHost(4, true);
			gate.isoField[32] = getIsoFixLenStrToHost(4, true);
			gate.isoField[37] = getIsoFixLenStrToHost(10, true);
			gate.isoField[11] = getIsoFixLenStrToHost(6, true);
			gate.txnDate = getIsoFixLenStrToHost(8, true);
			gate.isoField[12] = gate.txnDate.substring(4, 8);
			gate.isoField[13] = getIsoFixLenStrToHost(6, true);
			gate.atmType = getIsoFixLenStrToHost(1, true);
			gate.isoField[41] = getIsoFixLenStrToHost(8, true);
			gate.reqType = getIsoFixLenStrToHost(1, true);
			gate.respCode = getIsoFixLenStrToHost(4, true);
			sLFiller = getIsoFixLenStrToHost(1, true);
			gate.fCode = getIsoFixLenStrToHost(2, true);
			gate.isoField[35] = getIsoFixLenStrToHost(37, true);
			gate.caAtmAmt = getIsoFixLenStrToHost(10, true);
			gate.isoField[4] = gate.caAtmAmt + "00";
			gate.isoField[52] = getIsoFixLenStrToHost(16, true).trim();
			gate.pinBlock = gate.isoField[52];
			gate.birthday = getIsoFixLenStrToHost(8, true);
//      gate.arqcLen      = getIsoFixLenStrToHost(2, true);
//      gate.arqc         = getIsoFixLenStrToHost(185, true);
//      hostVarArqc(2, true);

			gate.isoField[3] = gate.pCode + gate.fCode;

			// kevin:補充授權所需的資料
			if ("OPEN".equals(gate.pCode) || "2471".equals(gate.pCode)) {
				gate.isoField[18] = "0000";
				gate.isoField[22] = "901";
			} else {
				hostVarArqc(2, true);
				gate.isoField[18] = "6011";
				gate.isoField[22] = "051";
			}
			gate.merchantCountry = "TW";
			gate.isoField[42] = gate.isoField[32]+gate.pCode+gate.fCode;

//        cvtString = isoString.substring(offset, offset + 16);
//        byteMap = byte2ByteMap(cvtString, 16);
//        gate.iso_bitMap =byteMap;
//        offset += 16;

//        if (byteMap.charAt(0) == '1') {
//            cvtString = isoString.substring(offset, offset + 16);
//            byteMap = byteMap + byte2ByteMap(cvtString, 16);
//            offset += 16;
//            cnt = 128;
//        } else {
//            cnt = 64;
//        }

//        for (k = 2; k <= cnt; k++) {
//            if (byteMap.charAt(k - 1) == '1') {
//                switch (k) {
//                case 2:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 3:
//                    gate.isoField[k] = hostFixField(6);
//                    break;
//                case 4:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 5:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 6:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 7:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 8:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 9:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 10:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 11:
//                    gate.isoField[k] = hostFixField(6);
//                    break;
//                case 12:
//                    gate.isoField[k] = hostFixField(6);
//                    break;
//                case 13:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 14:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 15:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 16:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 17:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 18:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 19:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 20:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 21:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 22:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 23:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 24:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 25:
//                    gate.isoField[k] = hostFixField(2);
//                    break;
//                case 26:
//                    gate.isoField[k] = hostFixField(2);
//                    break;
//                case 27:
//                    gate.isoField[k] = hostFixField(1);
//                    break;
//                case 28:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 29:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 30:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 31:
//                    gate.isoField[k] = hostFixField(8);
//                    break;
//                case 32:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 33:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 34:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 35:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 36:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 37:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 38:
//                    gate.isoField[k] = hostFixField(6);
//                    break;
//                case 39:
//                    gate.isoField[k] = hostFixField(2);
//                    retCode = gate.isoField[k];
//                    break;
//                case 40:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 41:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 42:
//                    gate.isoField[k] = hostFixField(15);
//                    break;
//                case 43:
//                    gate.isoField[k] = hostFixField(40);
//                    break;
//                case 44:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 45:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 46:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 47:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 48:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 49:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 50:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 51:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 52:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 53:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 54:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 55:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 56:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 57:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 58:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 59:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 60:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 61:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 62:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 63:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 64:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 65:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 66:
//                    gate.isoField[k] = hostFixField(1);
//                    break;
//                case 67:
//                    gate.isoField[k] = hostFixField(2);
//                    break;
//                case 68:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 69:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 70:
//                    gate.isoField[k] = hostFixField(3);
//                    break;
//                case 71:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 72:
//                    gate.isoField[k] = hostFixField(4);
//                    break;
//                case 73:
//                    gate.isoField[k] = hostFixField(6);
//                    break;
//                case 74:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 75:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 76:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 77:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 78:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 79:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 80:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 81:
//                    gate.isoField[k] = hostFixField(10);
//                    break;
//                case 82:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 83:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 84:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 85:
//                    gate.isoField[k] = hostFixField(12);
//                    break;
//                case 86:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 87:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 88:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 89:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 90:
//                    gate.isoField[k] = hostFixField(42);
//                    break;
//                case 91:
//                    gate.isoField[k] = hostFixField(1);
//                    break;
//                case 92:
//                    gate.isoField[k] = hostFixField(2);
//                    break;
//                case 93:
//                    gate.isoField[k] = hostFixField(5);
//                    break;
//                case 94:
//                    gate.isoField[k] = hostFixField(7);
//                    break;
//                case 95:
//                    gate.isoField[k] = hostFixField(42);
//                    break;
//                case 96:
//                    gate.isoField[k] = hostFixField(16);
//                    break;
//                case 97:
//                    gate.isoField[k] = hostFixField(17);
//                    break;
//                case 98:
//                    gate.isoField[k] = hostFixField(25);
//                    break;
//                case 99:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 100:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 101:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 102:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 103:
//                    gate.isoField[k] = hostVariable(2);
//                    break;
//                case 104:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 105:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 106:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 107:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 108:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 109:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 110:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 111:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 113:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 114:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 115:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 116:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 117:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 118:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 119:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 120:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 121:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 122:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 123:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 124:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 125:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 126:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 127:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                case 128:
//                    gate.isoField[k] = hostVariable(3);
//                    break;
//                default:
//                    break;
//                }
//            }
//        }

//        if (gate.isoField[101].equals("CF")) {
//            formatInCAF();
//        } else
//        if (gate.isoField[101].equals("DA")) {
//            formatInPBF();
//        } else
//        if (gate.isoField[101].equals("NF")) {
//            formatInNEG();
//        } else
//        if (gate.isoField[101].equals("VP")) {
//            formatInVISA();
//        }

//          convertBicField("C"); //mark by Howard

		} // end of try
		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	}

	// kevin:取iso固定長欄位並轉碼
	private String getIsoFixLenStrToHost(int len, boolean bPIsEbcdic) throws Exception {
		String fieldData = "";

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);
		if (bPIsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			fieldData = new String(lTmpAry, 0, lTmpAry.length);

		// fieldData = isoStringOfFisc.substring(offset, offset + len);
		offset += len;
		return fieldData;
	}

	private String hostFixBcdTest(int fieldLen) {

		String dest = "", lByte = "", rByte = "";
		int i = 0, cvt = 0, subOffset = 0;
		subOffset = offset;

		for (i = 0; i < fieldLen; i++) {
			cvt = (gate.isoData[subOffset] & 0xFF);
			lByte = Integer.toHexString(cvt / 16);
			lByte = lByte.toUpperCase();
			rByte = Integer.toHexString(cvt % 16);
			rByte = rByte.toUpperCase();
			dest = dest + lByte + rByte;
			subOffset++;
		}
		return dest;
	}

	private String hostVarBinBcd(int size) {
		String fieldData = "";
		int fieldLen = 0;

		if (size == 1) {
			fieldLen = gate.isoData[offset] & 0xFF;
			offset++;
		} else {
			fieldLen = (gate.isoData[offset] & 0xFF) * 256 + (gate.isoData[offset + 1] & 0xFF);
			offset += 2;
		}

		fieldData = hostFixBcd(fieldLen);
		return fieldData;
	}

	private String hostFixBcd(int size) {
		String dest = "", lByte = "", rByte = "";
		int i = 0, cvt = 0, left = 0, right = 0;

		for (i = 0; i < size; i++) {
			cvt = (gate.isoData[offset] & 0xFF);
			lByte = Integer.toHexString(cvt / 16);
			lByte = lByte.toUpperCase();
			rByte = Integer.toHexString(cvt % 16);
			rByte = rByte.toUpperCase();
			dest = dest + lByte + rByte;
			offset++;
		}

		return dest;
	}

//ATM MESSAGE FOR ARQC ADDITIONAL DATA – PRIVATE USE. write by Kevin 20200304 , ##START##    
	private void hostVarArqc(int size, boolean bP_IsEbcdic) throws Exception {
		logger.debug("AtmFormat @@@@@hostVarArqc");
		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);
		int arqcLen = (lTmpAry[0] & 0xFF) * 256 + (lTmpAry[1] & 0xFF);
//    	if (bP_IsEbcdic)
//    		lenData = HpeUtil.ebcdic2Str(L_TmpAry);
//    	else
//    		lenData = new String(L_TmpAry, 0, L_TmpAry.length);
		logger.debug("AtmFormat @@@@@hostVarArqc-len=" + arqcLen);
//    	fieldLen = Integer.parseInt(lenData);
		fieldLen = arqcLen;
		offset += size;

		gate.isoField[55] = hostFixBcdTest(fieldLen);

		fieldData = new String(gate.isoData, offset, fieldLen);
		// kevin:DE55不等於emvTrans
		gate.emvTrans = true;
		int checkPnt = offset + fieldLen;
		while (offset < checkPnt) {
			if (gate.isoData[offset] == (byte) 0x57) {
				offset++;
				gate.emv57 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x5A) {
				offset++;
				gate.emv5A = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x5F && gate.isoData[offset + 1] == (byte) 0x24) {
				offset += 2;
				gate.emv5F24 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x5F && gate.isoData[offset + 1] == (byte) 0x2A) {
				offset += 2;
				gate.emv5F2A = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x5F && gate.isoData[offset + 1] == (byte) 0x34) {
				offset += 2;
				gate.emv5F34 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x71) {
				offset++;
				gate.emv71 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x72) {
				offset++;
				gate.emv72 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x82) {
				offset++;
				gate.emv82 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x84) {
				offset++;
				gate.emv84 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x8A) {
				offset++;
				gate.emv8A = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x91) {
				offset++;
				gate.emv91 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x95) {
				offset++;
				gate.emv95 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9A) {
				offset++;
				gate.emv9A = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9B) {
				offset++;
				gate.emv9B = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9C) {
				offset++;
				gate.emv9C = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x02) {
				offset += 2;
				gate.emv9F02 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x03) {
				offset += 2;
				gate.emv9F03 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x09) {
				offset += 2;
				gate.emv9F09 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x10) {
				offset += 2;
				gate.emv9F10 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x1A) {
				offset += 2;
				gate.emv9F1A = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x1E) {
				offset += 2;
				gate.emv9F1E = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x26) {
				offset += 2;
				gate.emv9F26 = hostVarBinBcd(1);
				logger.debug("AtmFormat @@@@@hostVarArqc-gate.emv9F26=" + gate.emv9F26);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x27) {
				offset += 2;
				gate.emv9F27 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x33) {
				offset += 2;
				gate.emv9F33 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x34) {
				offset += 2;
				gate.emv9F34 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x35) {
				offset += 2;
				gate.emv9F35 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x36) {
				offset += 2;
				gate.emv9F36 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x37) {
				offset += 2;
				gate.emv9F37 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x41) {
				offset += 2;
				gate.emv9F41 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x53) {
				offset += 2;
				gate.emv9F53 = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0x9F && gate.isoData[offset + 1] == (byte) 0x5B) {
				offset += 2;
				gate.emv9F5B = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0xDF && gate.isoData[offset + 1] == (byte) 0xED) {
				offset += 2;
				gate.emvDFED = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0xDF && gate.isoData[offset + 1] == (byte) 0xEE) {
				offset += 2;
				gate.emvDFEE = hostVarBinBcd(1);
			} else if (gate.isoData[offset] == (byte) 0xDF && gate.isoData[offset + 1] == (byte) 0xEF) {
				offset += 2;
				gate.emvDFEF = hostVarBinBcd(1);
			} else {
				offset += 2;
				hostVarBinBcd(1);
			}
		}

		return;
	}
//    private String byte2ByteMap(String src, int size) {
//        byte[] srcByte = new byte[65];
//        String[] cvt = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
//        String dest = "";
//        int i = 0, ind = 0;
//        srcByte = src.getBytes();
//
//        for (i = 0; i < size; i++) {
//            if (srcByte[i] >= '0' && srcByte[i] <= '9') {
//                ind = (int) (srcByte[i] & 0x0F);
//            } else
//            if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
//                ind = (int) (srcByte[i] & 0x0F);
//                ind += 9;
//            }
//
//            dest = dest + cvt[ind];
//        }
//        return dest;
//    }
//
//    private String hostVariable(int len) {
//        String lenData = "", fieldData = "";
//        int fieldLen = 0;
//
//        lenData = isoString.substring(offset, offset + len);
//        fieldLen = Integer.parseInt(lenData);
//        offset += len;
//        fieldData = isoString.substring(offset, offset + fieldLen);
//        offset += fieldLen;
//        return fieldData;
//    }
//
//    private String hostFixField(int len) {
//        String fieldData = "";
//        fieldData = isoString.substring(offset, offset + len);
//        offset += len;
//        return fieldData;
//    }

//    private void formatOutCAF() {
//        if (gate.isoField[91].equals("5")) return;
//        gate.isoField[117] = iso117[1] +
//                        iso117[2] +
//                        iso117[3] +
//                        iso117[4] +
//                        iso117[5] +
//                        iso117[6] +
//                        iso117[7] +
//                        iso117[8] +
//                        iso117[9] +
//                        iso117[10] +
//                        iso117[11] +
//                        iso117[12];
//
//        gate.isoField[120] = iso120[1] +
//                        iso120[2] +
//                        iso120[3] +
//                        iso120[4] +
//                        iso120[5] +
//                        iso120[6] +
//                        iso120[7] +
//                        iso120[8] +
//                        iso120[9] +
//                        iso120[10];
//
//        gate.isoField[121] = iso121[1] +
//                        iso121[2] +
//                        iso121[3] +
//                        iso121[4] +
//                        iso121[5] +
//                        iso121[6];
//
//        gate.isoField[122] = iso122[1] +
//                        iso122[2] +
//                        iso122[3] +
//                        iso122[4] +
//                        iso122[5] +
//                        iso122[6] +
//                        iso122[7] +
//                        iso122[8] +
//                        iso122[9] +
//                        iso122[10];
//
//        gate.isoField[126] = iso126[1] +
//                        iso126[2] +
//                        iso126[3] +
//                        iso126[4] +
//                        iso126[5] +
//                        iso126[6];
//
//        if (gate.isoField[120].length() < 95) retCode = "120";
//        else if (gate.isoField[121].length() < 60) retCode = "121";
//        else if (gate.isoField[122].length() < 101)retCode = "122";
//        else if (gate.isoField[126].length() < 44) retCode = "126";
//    }
//
//    private void formatOutPBF() {
//        if (gate.isoField[91].equals("5")) return;
//        gate.isoField[120] = iso120[1] +
//                        iso120[2] +
//                        iso120[3] +
//                        iso120[4] +
//                        iso120[5] +
//                        iso120[6] +
//                        iso120[7] +
//                        iso120[8] +
//                        iso120[9] +
//                        iso120[10];
//
//        gate.isoField[122] = iso122[1] +
//                        iso122[2];
//
//        gate.isoField[125] = iso125[1] +
//                        iso125[2] +
//                        iso125[3] +
//                        iso125[4] +
//                        iso125[5];
//
//        if (gate.isoField[120].length() < 111) retCode = "120";
//        else if (gate.isoField[122].length() < 30) retCode = "122";
//        else if (gate.isoField[125].length() < 10) retCode = "125";
//    }
//
//    private void formatOutNEG() {
//        if (gate.isoField[91].equals("5")) return;
//
//        gate.isoField[120] = iso120[1] +
//                        iso120[2] +
//                        iso120[3] +
//                        iso120[4] +
//                        iso120[5];
//
//        if (gate.isoField[120].length() < 15) retCode = "120";
//    }
//
//    private void formatOutVISA() {
//        if (gate.isoField[91].equals("5")) {
//            return;
//        }
//
//        if (gate.isoField[91].equals("3")) {
//            gate.isoField[73] = "      ";
//        }
//
//        gate.isoField[120] = iso120[1] +
//                        iso120[2] +
//                        iso120[3] +
//                        iso120[4] +
//                        iso120[5] +
//                        iso120[6] +
//                        iso120[7];
//
//        for (k = 0; k < 10; k++) {
//            gate.isoField[120] = gate.isoField[120] + "          ";
//        }
//
//        if (gate.isoField[120].length() < 150) retCode = "120";
//    }
//
//    private void formatInCAF() {
//        if (gate.isoField[117].length() >= 24) {
//            iso117[1] = gate.isoField[117].substring(0, 4);
//            iso117[2] = gate.isoField[117].substring(4, 5);
//            iso117[3] = gate.isoField[117].substring(5, 6);
//            iso117[4] = gate.isoField[117].substring(6, 10);
//            iso117[5] = gate.isoField[117].substring(10, 11);
//            iso117[6] = gate.isoField[117].substring(11, 12);
//            iso117[7] = gate.isoField[117].substring(12, 16);
//            iso117[8] = gate.isoField[117].substring(16, 20);
//            iso117[9] = gate.isoField[117].substring(20, 21);
//            iso117[10] = gate.isoField[117].substring(21, 22);
//            iso117[11] = gate.isoField[117].substring(22, 23);
//            iso117[12] = gate.isoField[117].substring(23, 24);
//        }
//
//        if (gate.isoField[120].length() >= 95) {
//            iso120[1] = gate.isoField[120].substring(0, 2);
//            iso120[2] = gate.isoField[120].substring(2, 3);
//            iso120[3] = gate.isoField[120].substring(3, 19);
//            iso120[4] = gate.isoField[120].substring(19, 31);
//            iso120[5] = gate.isoField[120].substring(31, 43);
//            iso120[6] = gate.isoField[120].substring(43, 55);
//            iso120[7] = gate.isoField[120].substring(55, 67);
//            iso120[8] = gate.isoField[120].substring(67, 79);
//            iso120[9] = gate.isoField[120].substring(79, 91);
//            iso120[10] = gate.isoField[120].substring(91, 95);
//        }
//
//        if (gate.isoField[121].length() >= 60) {
//            iso121[1] = gate.isoField[121].substring(0, 4);
//            iso121[2] = gate.isoField[121].substring(4, 16);
//            iso121[3] = gate.isoField[121].substring(16, 28);
//            iso121[4] = gate.isoField[121].substring(28, 40);
//            iso121[5] = gate.isoField[121].substring(40, 52);
//            iso121[6] = gate.isoField[121].substring(52, 60);
//        }
//
//        if (gate.isoField[122].length() >= 101) {
//            iso122[1] = gate.isoField[122].substring(0, 12);
//            iso122[2] = gate.isoField[122].substring(12, 24);
//            iso122[3] = gate.isoField[122].substring(24, 48);
//            iso122[4] = gate.isoField[122].substring(48, 60);
//            iso122[5] = gate.isoField[122].substring(60, 72);
//            iso122[6] = gate.isoField[122].substring(72, 76);
//            iso122[7] = gate.isoField[122].substring(76, 88);
//            iso122[8] = gate.isoField[122].substring(88, 100);
//            iso122[9] = gate.isoField[122].substring(100, 101);
//        }
//
//        if (gate.isoField[126].length() >= 44) {
//            iso126[1] = gate.isoField[126].substring(0, 2);
//            iso126[2] = gate.isoField[126].substring(2, 4);
//            iso126[3] = gate.isoField[126].substring(4, 32);
//            iso126[4] = gate.isoField[126].substring(32, 33);
//            iso126[5] = gate.isoField[126].substring(33, 43);
//            iso126[6] = gate.isoField[126].substring(43, 44);
//        }
//    }
//
//    private void formatInPBF() {
//        if (gate.isoField[120].length() >= 111) {
//            iso120[1] = gate.isoField[120].substring(0, 1);
//            iso120[2] = gate.isoField[120].substring(1, 20);
//            iso120[3] = gate.isoField[120].substring(20, 39);
//            iso120[4] = gate.isoField[120].substring(39, 58);
//            iso120[5] = gate.isoField[120].substring(58, 69);
//            iso120[6] = gate.isoField[120].substring(69, 75);
//            iso120[7] = gate.isoField[120].substring(75, 90);
//            iso120[8] = gate.isoField[120].substring(90, 96);
//            iso120[9] = gate.isoField[120].substring(96, 111);
//        }
//
//        if (gate.isoField[122].length() >= 30) {
//            iso122[1] = gate.isoField[122].substring(0, 15);
//            iso122[2] = gate.isoField[122].substring(15, 30);
//        }
//
//        if (gate.isoField[125].length() >= 10) {
//            iso125[1] = gate.isoField[122].substring(0, 2);
//            iso125[2] = gate.isoField[122].substring(2, 4);
//            iso125[3] = gate.isoField[122].substring(4, 6);
//            iso125[4] = gate.isoField[122].substring(6, 8);
//            iso125[5] = gate.isoField[122].substring(8, 10);
//        }
//    }
//
//    private void formatInNEG() {
//        if (gate.isoField[120].length() >= 15) {
//            iso120[1] = gate.isoField[120].substring(0, 2);
//            iso120[2] = gate.isoField[120].substring(2, 4);
//            iso120[3] = gate.isoField[120].substring(4, 5);
//            iso120[4] = gate.isoField[120].substring(5, 11);
//            iso120[5] = gate.isoField[120].substring(11, 15);
//        }
//    }
//
//    private void formatInVISA() {
//        if (gate.isoField[120].length() >= 50) {
//            iso120[1] = gate.isoField[120].substring(0, 4);
//            iso120[2] = gate.isoField[120].substring(4, 6);
//            iso120[3] = gate.isoField[120].substring(6, 34);
//            iso120[4] = gate.isoField[120].substring(34, 37);
//            iso120[5] = gate.isoField[120].substring(37, 39);
//            iso120[6] = gate.isoField[120].substring(39, 41);
//            iso120[7] = gate.isoField[120].substring(41, 50);
//        }
//    }

	/* ECS change to ATM format */
	public boolean host2Iso() {

		try {

//        convertBicField("I");

			String cvtString = "";
			int cnt = 0;

			StringBuffer cvtZeros = new StringBuffer();
			StringBuffer cvtSpace = new StringBuffer();
			for (int i = 0; i < 30; i++) {
				cvtZeros.append("0000000000");
				cvtSpace.append("          ");
			}
			zeros = cvtZeros.toString();
			spaces = cvtSpace.toString();
			cvtZeros = null;
			cvtSpace = null;

			k = 0;
			isoString = "   ";
//        if (gate.isoField[101].equals("CF"))      formatOutCAF();
//        else if (gate.isoField[101].equals("DA")) formatOutPBF();
//        else if (gate.isoField[101].equals("NF")) formatOutNEG();
//        else if (gate.isoField[101].equals("VP")) formatOutVISA();
//        setHeaderMap();
			String sLAuthCode = gate.isoField[38];
			if (gate.isoField[39].equals("00")) {
				gate.respCode = "0000";
				if (gate.updateAtmPin) {
					int x = 25;
					int y = gate.isoField[35].length();
					logger.debug("AtmFormat updateAtmPin after isoField[35]=" + gate.isoField[35]);
					gate.isoField[35] = gate.isoField[35].substring(0, x) + gate.pvv + gate.isoField[35].substring(x+4, y);
					logger.debug("AtmFormat updateAtmPin after isoField[35]=" + gate.isoField[35]);
				}
			} else {
//        	gate.respCode = "9999";
				gate.respCode = gate.isoField[39] + gate.authErrId;
				sLAuthCode = "      ";
			}
			gate.reqType = "2";
			isoString = isoString + gate.mesgType + gate.pCode + "ATM " + gate.isoField[37] + gate.isoField[11]
					+ gate.txnDate + gate.isoField[13] + gate.atmType + gate.isoField[41] + gate.reqType + gate.respCode
					+ " ";
			offset += 60;
			logger.debug("AtmFormat mesgheader=" + isoString + "LEN1=" + isoString.length());
			
			isoString = isoString + gate.fCode + gate.isoField[35] + gate.caAtmAmt + sLAuthCode;
			offset += 55;
			logger.debug("AtmFormat mesgheaderbody=" + isoString + " isoString_len=" + isoString.length());

			byte[] inData = isoString.getBytes("cp1047");

			fiscVarF55();
			logger.debug("AtmFormat inData_len=" + inData.length + "specialIsoData_len=" + offset);
			gate.isoData = new byte[inData.length + offset];

			for (int i = 0; i < inData.length; i++) {
				gate.isoData[i] = inData[i];
			}

			for (int i = 0; i < offset; i++) {
				gate.isoData[i + inData.length] = gate.specialIsoData[i];
			}

			logger.debug("AtmFormat isoData_len=" + gate.isoData.length);

//			for (int i = 0; i < gate.isoData.length; i++) {
//				logger.debug("AtmFormat isoData[" + i + "]=" + gate.isoData[i] + "="
//						+ Integer.toHexString(gate.isoData[i] & 0xFF).toUpperCase());
//			}

			gate.totalLen = gate.isoData.length;
			gate.dataLen = gate.totalLen - 2;
			gate.isoData[0] = (byte) (gate.dataLen / 256);
			gate.isoData[1] = (byte) (gate.dataLen % 256);
			logger.debug("AtmFormat isoData = " + byteArrayToHexStr(gate.isoData));

			gate.isoString = isoString;
		} // end of try
		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;
	}

	public static String byteArrayToHexStr(byte[] byteArray) {
		if (byteArray == null) {
			return null;
		}
		char[] hexArray = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[byteArray.length * 2];
		for (int j = 0; j < byteArray.length; j++) {
			int v = byteArray[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	private void setHeaderMap() {
		int i = 0, k = 0;
		char[] map = new char[128];
		for (i = 0; i < 128; i++) {
			map[i] = '0';
		}

		if (gate.bicHead.length() != 12) {
			gate.bicHead = "ISO026000000";
		}

		if (gate.mesgType.length() != 4) {
			gate.mesgType = "XXXX";
		}

		isoString = spaces.substring(0, gate.initPnt) + gate.bicHead;
		offset = 14;

		for (k = 2; k < 128; k++) {

			if (gate.isoField[k].length() > 0) {
				map[k - 1] = '1';
			}

			if (gate.isoField[k].length() > 0 && k > 64) {
				map[0] = '1';
			}
		}

		byteMap = String.valueOf(map);
	}

	private void fiscVarF55() throws UnsupportedEncodingException {
		int len = 0, mod = 0;
		String tmpStr = "";
		byte[] tmpByte;
		int checkPnt = 0, nLArqcLen = 0;
		offset = 0;
//    checkPnt=offset;
		offset += 2;

		// if ( !gate.requestTrans )
		// { fiscTag55("91",gate.emv91); }

		// if ( gate.emv57.length() > 0 )
		// { fiscTag55("57",gate.emv57); }

		// if ( gate.emv5A.length() > 0 )
		// { fiscTag55("5A",gate.emv5A); }
		//
		// if ( gate.emv5F24.length() > 0 )
		// { fiscTag55("5F24",gate.emv5F24); }
		//
		// if ( gate.emv5F2A.length() > 0 )
		// { fiscTag55("5F2A",gate.emv5F2A); }
		//
		// if ( gate.emv5F34.length() > 0 )
		// { fiscTag55("5F34",gate.emv5F34); }
		//
		// if ( gate.emv71.length() > 0 )
		// { fiscTag55("71",gate.emv71); }
		//
		// if ( gate.emv72.length() > 0 )
		// { fiscTag55("72",gate.emv72); }
		//
		// if ( gate.emv82.length() > 0 )
		// { fiscTag55("82",gate.emv82); }
		//
		// if ( gate.emv84.length() > 0 )
		// { fiscTag55("84",gate.emv84); }

		if (gate.emv8A.length() > 0) {
			fiscTag55("8A", gate.emv8A);
		}

		if (gate.emv91.length() > 0) {
			fiscTag55("91", gate.emv91);
		}

		// if ( gate.emv95.length() > 0 )
		// { fiscTag55("95",gate.emv95); }
		//
		// if ( gate.emv9A.length() > 0 )
		// { fiscTag55("9A",gate.emv9A); }
		//
		// if ( gate.emv9B.length() > 0 )
		// { fiscTag55("9B",gate.emv9B); }
		//
		// if ( gate.emv9C.length() > 0 )
		// { fiscTag55("9C",gate.emv9C); }
		//
		// if ( gate.emv9F02.length() > 0 )
		// { fiscTag55("9F02",gate.emv9F02); }
		//
		// if ( gate.emv9F03.length() > 0 )
		// { fiscTag55("9F03",gate.emv9F03); }
		//
		// if ( gate.emv9F09.length() > 0 )
		// { fiscTag55("9F09",gate.emv9F09); }
		//
		// if ( gate.emv9F10.length() > 0 )
		// { fiscTag55("9F10",gate.emv9F10); }
		//
		// if ( gate.emv9F1A.length() > 0 )
		// { fiscTag55("9F1A",gate.emv9F1A); }
		//
		// if ( gate.emv9F1E.length() > 0 )
		// { fiscTag55("9F1E",gate.emv9F1E); }
		//
		// if ( gate.emv9F26.length() > 0 )
		// { fiscTag55("9F26",gate.emv9F26); }
		//
		// if ( gate.emv9F27.length() > 0 )
		// { fiscTag55("9F27",gate.emv9F27); }
		//
		// if ( gate.emv9F33.length() > 0 )
		// { fiscTag55("9F33",gate.emv9F33); }
		//
		// if ( gate.emv9F34.length() > 0 )
		// { fiscTag55("9F34",gate.emv9F34); }
		//
		// if ( gate.emv9F35.length() > 0 )
		// { fiscTag55("9F35",gate.emv9F35); }
		//
		// if ( gate.emv9F36.length() > 0 )
		// { fiscTag55("9F36",gate.emv9F36); }
		//
		// if ( gate.emv9F37.length() > 0 )
		// { fiscTag55("9F37",gate.emv9F37); }
		//
		// if ( gate.emv9F41.length() > 0 )
		// { fiscTag55("9F41",gate.emv9F41); }
		//
		// if ( gate.emv9F53.length() > 0 )
		// { fiscTag55("9F53",gate.emv9F53); }
		//
		// if ( gate.emv9F5B.length() > 0 )
		// { fiscTag55("9F5B",gate.emv9F5B); }
		//
		// if ( gate.emvDFED.length() > 0 )
		// { fiscTag55("DFED",gate.emvDFED); }
		//
		// if ( gate.emvDFEE.length() > 0 )
		// { fiscTag55("DFEE",gate.emvDFEE); }
		//
		// if ( gate.emvDFEF.length() > 0 )
		// { fiscTag55("DFEF",gate.emvDFEF); }

//    tmpStr= (offset - checkPnt -2)+"";
//    if ( tmpStr.length()  == 1  )
//       { tmpStr = "00" + tmpStr;   }
//    else
//    if ( tmpStr.length()  == 2  )
//       { tmpStr = "0" + tmpStr; }
//    tmpByte = tmpStr.getBytes("Cp1047");
//    for( int i=0; i<2; i++ )
//       { gate.isoData[checkPnt+i] = tmpByte[i]; }
		nLArqcLen = (offset - checkPnt - 2);
		logger.debug("AtmFormat @@@@ARQC_LEN=" + nLArqcLen);
		gate.specialIsoData[0] = (byte) (nLArqcLen / 256);
		gate.specialIsoData[1] = (byte) (nLArqcLen % 256);
		return;
	}

	private void fiscTag55(String tagData, String emvData) {
		int len = tagData.length() / 2;
		fiscFixBcd(tagData, len);

//    if ( (emvData.length() % 2) != 0 )
//       { emvData = "0" + emvData;  }
		len = emvData.length() / 2;
//    gate.specialIsoData[offset] = (byte)len;
//    offset++;
		String lenData = len2Hex(len);
		int modLen = lenData.length() / 2;
		convertBcd(lenData, modLen);
		convertBcd(emvData, len);
		return;
	}

	private String len2Hex(int len) {
		String lenData = Integer.toHexString(len);
		if ((lenData.length() % 2) != 0) {
			lenData = "0" + lenData;
		}
		return lenData.toUpperCase();
	}

	private void fiscFixBcd(String fieldData, int size) {
		int mod = fieldData.length() % 2;
		if (mod != 0) {
			fieldData = "0" + fieldData;
		}

		int len = fieldData.length() / 2;

		if (len != size) {
			retCode = "F" + k;
			return;
		}

		convertBcd(fieldData, len);
		return;
	}

	private void convertBcd(String src, int size) {
		int i = 0, left = 0, right = 0, pnt = 0;
		byte[] tmp = src.getBytes();
		for (i = 0; i < size; i++) {
			left = tmp[pnt] - 48;
			pnt++;
			if (left > 40) {
				left = left - 39;
			} else if (left > 10) {
				left = left - 7;
			}
			right = tmp[pnt] - 48;
			pnt++;
			if (right > 40) {
				right = right - 39;
			} else if (right > 10) {
				right = right - 7;
			}
			gate.specialIsoData[offset] = (byte) (left * 16 + right);
			offset++;
		}
		return;
	}

//    private String byteMap2Byte(String src, int size) {
//        char[] destChar = new char[33];
//        char[] cvt = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
//        int i = 0, j = 0, ind = 0;
//        String dest = "", tmp = "";
//
//        for (i = 0; i < size; i++) {
//            tmp = "";
//            tmp = src.substring(j, j + 4);
//            ind = Integer.parseInt(tmp, 2);
//            destChar[i] = cvt[ind];
//            j += 4;
//        }
//
//        dest = String.valueOf(destChar);
//        dest = dest.substring(0, size);
//
//        return dest;
//    }

//    private void B24Variable(String fieldData, int len) {
//        String zeros = "00000000", tempStr = "";
//        int fieldLen = 0;
//
//        fieldLen = fieldData.length();
//        tempStr  = String.valueOf(fieldLen);
//        if (tempStr.length() < len) {
//            tempStr = zeros.substring(0, len - tempStr.length()) + tempStr;
//        }
//        isoString = isoString + tempStr + fieldData;
//        offset = offset + len + fieldLen;
//    }
//
//    private void B24FixField(String fieldData, int len) {
//        if (fieldData.length() < len) {
//            fieldData = fieldData + spaces.substring(0, len - fieldData.length());
//        }
//
//        isoString = isoString + fieldData.substring(0, len);
//        offset += len;
//    }

// public boolean convertBicField(String cvtCode)
// {
//    if ( cvtCode.equals("C") )
//       {
//         if (( gate.isoField[63].length() > 0 ) || (gate.isoField[126].length()>0) )
//            {
//              TokenObject token = new TokenObject();
//              token.decodeTokenData(gate);
//              if ( token.tokenIdB2.equals("B2") || token.tokenIdB5.equals("B5") )
//                 { gate.emvTrans = true; }
//            }
//       }
//    else
//       {
//    	 if (gate.ProdMode) {
//    		 if ( gate.bicHead.length() != 12 )
//    		 { gate.bicHead = "ISO026000000"; }
//    		 TokenObject token = new TokenObject();
//         
//    		 if (gate.isoField[126].length()>0) {
//    			 gate.isoField[126] = token.createTokenData(gate);
//    			 gate.isoField[63]="";
//    		 }
//    		 else {
//    			 gate.isoField[63] = token.createTokenData(gate);
//    			 gate.isoField[126]="";
//    		 }
//    	 }
//       }
//
//    
//    return true;
// }

	public void expHandle(Exception ex) {
		logger.fatal(" >> ####### AtmFormat Exception MESSAGE STARTED ######");
		logger.fatal("AtmFormat Exception_Message : ", ex);
		logger.fatal(" >> ####### AtmFormat system Exception MESSAGE   ENDED ######");
		return;
	}

} // Class BA24 End