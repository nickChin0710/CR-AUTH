/**
 * 授權使用ACER ISO8583格式轉換物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用ACER ISO8583格式轉換物件              *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

//import org.apache.log4j.*;
import org.apache.logging.log4j.Logger;

import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class AcerFormat extends ConvertMessage implements FormatInterChange {

	public String byteMap = "", isoString = "", retCode = "";
	public String zeros = "", spaces = "", fiid = "", dpcNum = "", lNet = "";
	private int offset = 0, k = 0;

//    public AcerFormat(Logger logger,AuthGate gate,HashMap cvtHash) {
	public AcerFormat(Logger logger, AuthTxnGate gate) {
		super.logger = logger;
		super.gate = gate;
//      super.cvtHash = cvtHash;
	}

	/* ACER ISO FORMAT TO ECS */
	public boolean iso2Host() {
//  String  cvtStr="";
		int cnt = 0;

		try {

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

			offset = 0;

			// gate.mesgType = hostFixAns(4);
			gate.bicHead = hostFixBcd(5);
			gate.mesgType = hostFixBcd(2);
			byteMap = bitMapToByteMap(8);

			if (byteMap.charAt(1 - 1) == '1') {
				byteMap = byteMap + bitMapToByteMap(8);
				cnt = 128;
			} else {
				cnt = 64;
			}

			for (k = 2; k <= cnt; k++) {
				if (byteMap.charAt(k - 1) == '1') {
					switch (k) {
					case 2:
						gate.acerField[k] = hostFixBcd(9);
						break;
					case 3:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 4:
						gate.acerField[k] = hostFixBcd(6);
						break;
					case 5:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 6:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 7:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 8:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 9:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 10:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 11:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 12:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 13:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 14:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 15:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 16:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 17:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 18:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 19:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 20:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 21:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 22:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 23:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 24:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 25:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 26:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 27:
						gate.acerField[k] = hostFixBcd(1);
						break;
					case 28:
						gate.acerField[k] = hostFixBcd(9);
						break;
					case 29:
						gate.acerField[k] = hostFixBcd(9);
						break;
					case 30:
						gate.acerField[k] = hostFixBcd(9);
						break;
					case 31:
						gate.acerField[k] = hostFixBcd(9);
						break;
					case 32:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 33:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 34:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 35:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 36:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 37:
						gate.acerField[k] = getIsoFixLenStrToHost(12, false);
						break;
					case 38:
						gate.acerField[k] = getIsoFixLenStrToHost(6, false);
						break;
					case 39:
						gate.acerField[k] = getIsoFixLenStrToHost(2, false);
						break;
					case 40:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 41:
						gate.acerField[k] = getIsoFixLenStrToHost(8, false);
						break;
					case 42:
						gate.acerField[k] = hostFixBcd(15);
						break;
					case 43:
						gate.acerField[k] = hostFixBcd(40);
						break;
					case 44:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 45:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 46:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 47:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 48:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 49:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 50:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 51:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 52:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 53:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 54:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 55:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 56:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 57:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 58:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 59:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 60:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 61:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 62:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 63:
						gate.acerField[k] = getAcerVarLenF63ToHost(2);
						break;
					case 64:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 65:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 66:
						gate.acerField[k] = hostFixBcd(1);
						break;
					case 67:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 68:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 69:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 70:
						gate.acerField[k] = hostFixBcd(3);
						break;
					case 71:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 72:
						gate.acerField[k] = hostFixBcd(4);
						break;
					case 73:
						gate.acerField[k] = hostFixBcd(6);
						break;
					case 74:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 75:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 76:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 77:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 78:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 79:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 80:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 81:
						gate.acerField[k] = hostFixBcd(10);
						break;
					case 82:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 83:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 84:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 85:
						gate.acerField[k] = hostFixBcd(12);
						break;
					case 86:
						gate.acerField[k] = hostFixBcd(16);
						break;
					case 87:
						gate.acerField[k] = hostFixBcd(16);
						break;
					case 88:
						gate.acerField[k] = hostFixBcd(16);
						break;
					case 89:
						gate.acerField[k] = hostFixBcd(16);
						break;
					case 90:
						gate.acerField[k] = hostFixBcd(42);
						break;
					case 91:
						gate.acerField[k] = hostFixBcd(1);
						break;
					case 92:
						gate.acerField[k] = hostFixBcd(2);
						break;
					case 93:
						gate.acerField[k] = hostFixBcd(5);
						break;
					case 94:
						gate.acerField[k] = hostFixBcd(7);
						break;
					case 95:
						gate.acerField[k] = hostFixBcd(42);
						break;
					case 96:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 97:
						gate.acerField[k] = hostFixBcd(17);
						break;
					case 98:
						gate.acerField[k] = hostFixBcd(25);
						break;
					case 99:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 100:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 101:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 102:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 103:
						gate.acerField[k] = getAcerVarLenStrToHost(2);
						break;
					case 104:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 105:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 112:
						gate.acerField[k] = hostFixBcd(27);
						break;
					case 113:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 114:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 115:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 116:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 117:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 118:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 119:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 120:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 121:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 122:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 123:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 124:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 125:
						gate.acerField[k] = hostFixBcd(8);
						break;
					case 126:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 127:
						gate.acerField[k] = getAcerVarLenStrToHost(3);
						break;
					case 128:
						gate.acerField[k] = hostFixBcd(8);
						break;
					default:
						break;
					}
				}
			}

//     convertFiscField("C");

		} // end of try
		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	}

	private String byte2ByteMap(String src, int size) {
		byte[] srcByte = new byte[65];
		String[] cvt = { "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
				"1100", "1101", "1110", "1111" };
		String dest = "";
		int i = 0, ind = 0;
		srcByte = src.getBytes();

		for (i = 0; i < size; i++) {
			if (srcByte[i] >= '0' && srcByte[i] <= '9') {
				ind = (int) (srcByte[i] & 0x0F);
			} else if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
				ind = (int) (srcByte[i] & 0x0F);
				ind += 9;
			}

			dest = dest + cvt[ind];
		}
		return dest;
	}

	private String bitMapToByteMap(int size) {
		String cvtMap = "", tmp = "", zeros = "00000000";
		int i = 0, cvt = 0;
		for (i = 0; i < size; i++) {
			cvt = (gate.isoData[offset] & 0xFF);
			tmp = Integer.toBinaryString(cvt);
			if (tmp.length() < 8) {
				tmp = zeros.substring(0, 8 - tmp.length()) + tmp;
			}
			cvtMap = cvtMap + tmp;
			offset++;
		}
		return cvtMap;
	}

	private String getAcerVarLenStrToHost(int len) throws Exception {
		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);
		lenData = HpeUtil.byte2Hex(lTmpAry);
//    	lenData = new String(L_TmpAry, 0, L_TmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
//		System.out.println("acer_field(48)_len=" + fieldLen);
		offset += len;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

		fieldData = new String(lTmpAry, 0, lTmpAry.length);
//		System.out.println("acer_field(48)_data=" + fieldData);

		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
		offset += fieldLen;
		return fieldData;
	}

	private String getAcerVarLenF63ToHost(int len) throws Exception {
		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] L_TmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);
		lenData = HpeUtil.byte2Hex(L_TmpAry);
//    	lenData = new String(L_TmpAry, 0, L_TmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
//		System.out.println("acer_field(63)_len=" + fieldLen);
		offset += len;

//    	L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);
		//
// 		fieldData = new String(L_TmpAry, 0, L_TmpAry.length);
		fieldData = hostFixBcd(fieldLen);
//		System.out.println("acer_field(63)_data=" + fieldData);

		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
//    	offset += fieldLen;
		return fieldData;
	}

//	private String getIsoVarLenStrToHost(int len, boolean bPIsEbcdic) throws Exception {
//		String lenData = "", fieldData = "";
//		int fieldLen = 0;
//
//		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);
//
//		if (bPIsEbcdic)
//			lenData = HpeUtil.ebcdic2Str(lTmpAry);
//		else
//			lenData = new String(lTmpAry, 0, lTmpAry.length);
//		// lenData = isoStringOfFisc.substring(offset, offset + len);
//		fieldLen = Integer.parseInt(lenData);
//		offset += len;
//
//		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);
//
//		if (bPIsEbcdic)
//			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
//		else
//			fieldData = new String(lTmpAry, 0, lTmpAry.length);
//
//		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
//		offset += fieldLen;
//		return fieldData;
//	}

	private String getIsoFixLenStrToHost(int len, boolean bP_IsEbcdic) throws Exception {
		String fieldData = "";

		byte[] L_TmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);
		if (bP_IsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
		else
			fieldData = new String(L_TmpAry, 0, L_TmpAry.length);

		// fieldData = isoStringOfFisc.substring(offset, offset + len);
		offset += len;
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

	private String hostVariable(int len) {
		String lenData = "", fieldData = "";
		int fieldLen = 0;

		lenData = isoString.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		offset += len;
		fieldData = isoString.substring(offset, offset + fieldLen);
		offset += fieldLen;
		return fieldData;
	}

	private String hostFixField(int len) {
		String fieldData = "";
		fieldData = isoString.substring(offset, offset + len);
		offset += len;
		return fieldData;
	}

	public boolean host2Iso() {
		try {
			String cvtStr = "";
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

			offset = gate.initPnt;
			gate.bicHead = "6000010201";
			acerFixBcd(gate.bicHead, 5);
			acerFixBcd(gate.mesgType, 2);
			setByteMap();
			cvtStr = byteMap.substring(0, 64);
			byteMapToBitMap(cvtStr, 8);

			if (byteMap.charAt(1 - 1) == '0') {
				cnt = 64;
			} else if (byteMap.charAt(1 - 1) == '1') {
				cvtStr = byteMap.substring(64, 128);
				byteMapToBitMap(cvtStr, 8);
				cnt = 128;
			}

			for (k = 2; k <= cnt; k++) {
				if (byteMap.charAt(k - 1) == '1') {
					switch (k) {
					case 2:
						acerVarBcdFix(gate.acerField[k], 1);
						break;
					case 3:
						acerFixBcd(gate.acerField[k], 3);
						break;
					case 4:
						acerFixBcd(gate.acerField[k], 6);
						break;
					case 5:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 6:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 7:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 8:
						acerFixAns(gate.acerField[k], 8);
						break;
					case 9:
						acerFixAns(gate.acerField[k], 8);
						break;
					case 10:
						acerFixAns(gate.acerField[k], 8);
						break;
					case 11:
						acerFixBcd(gate.acerField[k], 3);
						break;
					case 12:
						acerFixBcd(gate.acerField[k], 3);
						break;
					case 13:
						acerFixBcd(gate.acerField[k], 2);
						break;
					case 14:
						acerFixBcd(gate.acerField[k], 2);
						break;
					case 15:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 16:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 17:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 18:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 19:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 20:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 21:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 22:
						acerFixBcd(gate.acerField[k], 2);
						break;
					case 23:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 24:
						acerFixBcd(gate.acerField[k], 2);
						break;
					case 25:
						acerFixBcd(gate.acerField[k], 1);
						break;
					case 26:
						acerFixAns(gate.acerField[k], 2);
						break;
					case 27:
						acerFixAns(gate.acerField[k], 1);
						break;
					case 28:
						acerFixAns(gate.acerField[k], 9);
						break;
					case 29:
						acerFixAns(gate.acerField[k], 9);
						break;
					case 30:
						acerFixAns(gate.acerField[k], 9);
						break;
					case 31:
						acerFixAns(gate.acerField[k], 9);
						break;
					case 32:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 33:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 34:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 35:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 36:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 37:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 38:
						acerFixAns(gate.acerField[k], 6);
						break;
					case 39:
						acerFixAns(gate.acerField[k], 2);
						break;
					case 40:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 41:
						acerFixAns(gate.acerField[k], 8);
						break;
					case 42:
						acerFixAns(gate.acerField[k], 15);
						break;
					case 43:
						acerFixAns(gate.acerField[k], 40);
						break;
					case 44:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 45:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 46:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 47:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 48:
						acerVarAnsFix(gate.acerField[k]);
						break;
					case 49:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 50:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 51:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 52:
						acerFixBcd(gate.acerField[k], 8);
						break;
					case 53:
						acerFixAns(gate.acerField[k], 8);
						break;
					case 54:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 55:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 56:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 57:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 58:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 59:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 60:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 61:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 62:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 63:
						acerVarBcdFix(gate.acerField[k], 2);
						break;
					case 64:
						acerFixBcd(gate.acerField[k], 8);
						break;
					case 65:
						acerFixBcd(gate.acerField[k], 8);
						break;
					case 66:
						acerFixAns(gate.acerField[k], 1);
						break;
					case 67:
						acerFixAns(gate.acerField[k], 2);
						break;
					case 68:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 69:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 70:
						acerFixAns(gate.acerField[k], 3);
						break;
					case 71:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 72:
						acerFixAns(gate.acerField[k], 4);
						break;
					case 73:
						acerFixAns(gate.acerField[k], 6);
						break;
					case 74:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 75:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 76:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 77:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 78:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 79:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 80:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 81:
						acerFixAns(gate.acerField[k], 10);
						break;
					case 82:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 83:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 84:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 85:
						acerFixAns(gate.acerField[k], 12);
						break;
					case 86:
						acerFixAns(gate.acerField[k], 16);
						break;
					case 87:
						acerFixAns(gate.acerField[k], 16);
						break;
					case 88:
						acerFixAns(gate.acerField[k], 16);
						break;
					case 89:
						acerFixAns(gate.acerField[k], 16);
						break;
					case 90:
						acerFixAns(gate.acerField[k], 42);
						break;
					case 91:
						acerFixAns(gate.acerField[k], 1);
						break;
					case 92:
						acerFixAns(gate.acerField[k], 2);
						break;
					case 93:
						acerFixAns(gate.acerField[k], 5);
						break;
					case 94:
						acerFixAns(gate.acerField[k], 7);
						break;
					case 95:
						acerFixAns(gate.acerField[k], 42);
						break;
					case 96:
						acerFixBcd(gate.acerField[k], 8);
						break;
					case 97:
						acerFixAns(gate.acerField[k], 17);
						break;
					case 98:
						acerFixAns(gate.acerField[k], 25);
						break;
					case 99:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 100:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 101:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 102:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 103:
						acerVarAns(gate.acerField[k], 2);
						break;
					case 104:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 105:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 112:
						acerFixAns(gate.acerField[k], 27);
						break;
					case 113:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 114:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 115:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 116:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 117:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 118:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 119:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 120:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 121:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 122:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 123:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 124:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 125:
						acerFixBcd(gate.acerField[k], 8);
						break;
					case 126:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 127:
						acerVarAns(gate.acerField[k], 3);
						break;
					case 128:
						acerFixBcd(gate.acerField[k], 8);
						break;
					default:
						break;
					}
				}
			}
//			System.out.println("ACER_OFFSET=" + offset);
			gate.totalLen = offset;
			gate.dataLen = offset - gate.initPnt;
//			System.out.println("ACER_gate.totalLen=" + gate.totalLen);
//			System.out.println("ACER_gate.dataLen=" + gate.dataLen);

//    		gate.isoData[0]   = (byte)(gate.dataLen / 256);
//    		gate.isoData[1]   = (byte)(gate.dataLen % 256);
			String sL_TxLen = String.format("%04d", gate.dataLen);
			gate.isoData[0] = (byte) Integer.parseInt(sL_TxLen.substring(0, 2), 16);
			gate.isoData[1] = (byte) Integer.parseInt(sL_TxLen.substring(2, 4), 16);

		} // end of try

		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	} // end of host2Iso

	private void setByteMap() {
		int i = 0, k = 0;
		char map[] = new char[129];
		String tmpStr = "";

		for (i = 0; i <= 128; i++) {
			map[i] = '0';
		}

		for (k = 2; k < 128; k++) {
			if (gate.acerField[k].length() > 0) {
				map[k - 1] = '1';
			}

			if (map[k - 1] == '1' && k > 64) {
				map[1 - 1] = '1';
			}
		}

		byteMap = String.valueOf(map);
		return;
	}

	private void byteMapToBitMap(String src, int size) {
		String tmp = "";
		int i = 0, pnt = 0;

		for (i = 0; i < size; i++) {
			tmp = src.substring(pnt, pnt + 8);
			gate.isoData[offset] = (byte) (Integer.parseInt(tmp, 2));
			pnt += 8;
			offset++;
		}

		return;
	}

	private void acerFixAns(String fieldData, int len) throws UnsupportedEncodingException {
		int i = 0;
		if (fieldData.length() < len) {
			fieldData = fieldData + spaces.substring(0, len - fieldData.length());
		} else if (fieldData.length() > len) {
			fieldData = fieldData.substring(0, len);
		}

		byte[] tmp = fieldData.getBytes();
		for (i = 0; i < len; i++) {
			gate.isoData[offset] = tmp[i];
			offset++;
		}

		return;
	}

	private void acerVarAns(String fieldData, int size) throws UnsupportedEncodingException {
		String tmpStr = "";
		byte[] tmpByte;
		int i = 0;

		tmpStr = String.valueOf(fieldData.length());
		if ((size - tmpStr.length()) == 1) {
			tmpStr = "0" + tmpStr;
		} else if ((size - tmpStr.length()) == 2) {
			tmpStr = "00" + tmpStr;
		}

		tmpByte = tmpStr.getBytes();
		for (i = 0; i < size; i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		tmpByte = fieldData.getBytes();
		for (i = 0; i < fieldData.length(); i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		return;
	}

	private void acerVarAnsFix(String fieldData) throws UnsupportedEncodingException {
		byte[] tmpByte;
		int i = 0;

		String sL_TxLen = String.format("%04d", fieldData.length());
		gate.isoData[offset] = (byte) Integer.parseInt(sL_TxLen.substring(0, 2), 16);
		offset++;
		gate.isoData[offset] = (byte) Integer.parseInt(sL_TxLen.substring(2, 4), 16);
		offset++;

		tmpByte = fieldData.getBytes();
		for (i = 0; i < fieldData.length(); i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		return;
	}

	private void acerVarBcdFix(String fieldData, int size) throws UnsupportedEncodingException {
		String tmpStr = "";
		byte[] tmpByte;
		int i = 0;

		int mod = fieldData.length() % 2;
		if (mod != 0) {
			fieldData = "0" + fieldData;
		}
		int len = fieldData.length() / 2;
		tmpStr = String.valueOf(len);
		if (size == 1) {
			tmpStr = String.valueOf(fieldData.length());
		}
		size = size * 2;
		if ((size - tmpStr.length()) == 1) {
			tmpStr = "0" + tmpStr;
		} else if ((size - tmpStr.length()) == 2) {
			tmpStr = "00" + tmpStr;
		}

//		tmpByte = tmpStr.getBytes();
		for (i = 0; i < size; i += 2)
//		   { gate.isoData[offset] = tmpByte[i];   offset++;   }
		{
			gate.isoData[offset] = (byte) Integer.parseInt(tmpStr.substring(i, i + 2), 16);
			offset++;
		}

//		tmpByte = fieldData.getBytes();
//		for ( i=0; i<fieldData.length(); i++ )
//		   { gate.isoData[offset] = tmpByte[i];   offset++;   }
		convertBcd(fieldData, len);

		return;
	}

	private void acerVarBcd(String fieldData, int size) throws UnsupportedEncodingException {
		String tmpStr = "";
		byte[] tmpByte;
		int i = 0;

		int mod = fieldData.length() % 2;
		if (mod != 0) {
			fieldData = "0" + fieldData;
		}
		int len = fieldData.length() / 2;

		tmpStr = String.valueOf(len);
		if ((size - tmpStr.length()) == 1) {
			tmpStr = "0" + tmpStr;
		} else if ((size - tmpStr.length()) == 2) {
			tmpStr = "00" + tmpStr;
		}

		tmpByte = tmpStr.getBytes();
		for (i = 0; i < size; i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		convertBcd(fieldData, len);

		return;
	}

	private void acerFixBcd(String fieldData, int size) {
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
			gate.isoData[offset] = (byte) (left * 16 + right);
			offset++;
		}
		return;
	}

	private void setHeaderMap() {
		int i = 0, k = 0;
		char[] map = new char[128];
		for (i = 0; i < 128; i++) {
			map[i] = '0';
		}

		if (gate.bicHead.length() != 5)
//          { gate.bicHead = HpeUtil.strToHex("6000010201"); }
		{
			gate.bicHead = "6000010201";
		}

		if (gate.mesgType.length() != 4) {
			gate.mesgType = "XXXX";
		}

		isoString = spaces.substring(0, gate.initPnt) + gate.bicHead;
		offset = +12;

		for (k = 2; k < 128; k++) {

			if (gate.acerField[k].length() > 0) {
				map[k - 1] = '1';
			}

			if (gate.acerField[k].length() > 0 && k > 64) {
				map[0] = '1';
			}
		}

		byteMap = String.valueOf(map);
	}

	private String byteMap2Byte(String src, int size) {
		char[] destChar = new char[33];
		char[] cvt = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int i = 0, j = 0, ind = 0;
		String dest = "", tmp = "";

		for (i = 0; i < size; i++) {
			tmp = "";
			tmp = src.substring(j, j + 4);
			ind = Integer.parseInt(tmp, 2);
			destChar[i] = cvt[ind];
			j += 4;
		}

		dest = String.valueOf(destChar);
		dest = dest.substring(0, size);

		return dest;
	}

	private void acerVariable(String fieldData, int len) {
		String zeros = "00000000", tempStr = "";
		int fieldLen = 0;

		fieldLen = fieldData.length();
		tempStr = String.valueOf(fieldLen);
		if (tempStr.length() < len) {
			tempStr = zeros.substring(0, len - tempStr.length()) + tempStr;
		}
		isoString = isoString + tempStr + fieldData;
		offset = offset + len + fieldLen;
	}

	private void acerFixField(String fieldData, int len) {
		if (fieldData.length() < len) {
			fieldData = fieldData + spaces.substring(0, len - fieldData.length());
		}

		isoString = isoString + fieldData.substring(0, len);
		offset += len;
	}

	public void expHandle(Exception ex) {
		logger.fatal(" >> ####### BicFormat Exception MESSAGE STARTED ######");
		logger.fatal("BicFormat Exception_Message : ", ex);
		logger.fatal(" >> ####### BicFormat system Exception MESSAGE   ENDED ######");
		return;
	}

}
