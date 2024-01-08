/**
 * 授權使用FISC ISO8583格式轉換物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用FISC ISO8583格式轉換物件              *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 * 2021/03/24  V1.00.02  Kevin       物件與TokenObject一樣，故取消TokenObjectFisc  *
 * 2021/11/19  V1.00.03  Kevin       VISA 代碼化交易處理調整                       *
 * 2021/12/15  V1.00.04  Kevin       VISA 代碼化交易處理調整2                      *
 * 2022/02/22  V1.00.05  Kevin       ECS計算分期資料後，避免null導致資料位移           *
 * 2022/03/22  V1.00.06  Kevin       HCE交易時，ARQC驗證由TWMP，故不用回覆DE55       *
 * 2022/04/06  V1.00.07  Kevin       M/C 代碼化交易處理調整                       *
 * 2022/04/18  V1.00.08  Kevin       fix f48T42 f120 Length error             *          
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 * 2023/10/12  V1.00.54  Kevin       OEMPAY綁定Mastercard Token成功通知僅限行動裝置  *
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

import java.io.UnsupportedEncodingException;
//import java.util.*;
import org.apache.logging.log4j.Logger;

import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class FiscFormat extends ConvertMessage implements FormatInterChange {

	public String byteMap = "", retCode = "", rejectMesg = "", errFlag = "";
	public String zeros = "", spaces = "";

	public int offset = 0, k = 0;

//public  FiscFormat(Logger logger,AuthGate gate,HashMap cvtHash)
	public FiscFormat(Logger logger, AuthTxnGate gate) {
		super.logger = logger;
		super.gate = gate;
//super.cvtHash = cvtHash;
	}

	/* �N FISC ISO8583 �榡�ର�D���榡��� */
	public boolean iso2Host() {
//String  cvtStr="";
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

//gate.mesgType = hostFixAns(4);
			gate.mesgType = getIsoFixLenStrToHost(4, true);
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
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 3:
						gate.isoField[k] = getIsoFixLenStrToHost(6, true);
						break;
					case 4:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 5:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 6:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 7:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 8:
						gate.isoField[k] = getIsoFixLenStrToHost(8, true);
						break;
					case 9:
						gate.isoField[k] = getIsoFixLenStrToHost(8, true);
						break;
					case 10:
						gate.isoField[k] = getIsoFixLenStrToHost(8, true);
						break;
					case 11:
						gate.isoField[k] = getIsoFixLenStrToHost(6, true);
						break;
					case 12:
						gate.isoField[k] = getIsoFixLenStrToHost(6, true);
						break;
					case 13:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 14:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 15:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 16:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 17:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 18:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 19:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 20:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 21:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 22:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 23:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 24:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 25:
						gate.isoField[k] = getIsoFixLenStrToHost(2, true);
						break;
					case 26:
						gate.isoField[k] = getIsoFixLenStrToHost(2, true);
						break;
					case 27:
						gate.isoField[k] = getIsoFixLenStrToHost(1, true);
						break;
					case 28:
						gate.isoField[k] = getIsoFixLenStrToHost(9, true);
						break;
					case 29:
						gate.isoField[k] = getIsoFixLenStrToHost(9, true);
						break;
					case 30:
						gate.isoField[k] = getIsoFixLenStrToHost(9, true);
						break;
					case 31:
						gate.isoField[k] = getIsoFixLenStrToHost(9, true);
						break;
					case 32:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 33:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 34:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 35:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 36:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 37:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 38:
						gate.isoField[k] = getIsoFixLenStrToHost(6, true);
						break;
					case 39:
						gate.isoField[k] = getIsoFixLenStrToHost(2, true);
						break;
					case 40:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 41:
						gate.isoField[k] = getIsoFixLenStrToHost(8, true);
						break;
					case 42:
						gate.isoField[k] = getIsoFixLenStrToHost(15, true);
						break;
					case 43:
						gate.isoField[k] = getIsoFixLenStrToHost(40, true);
						break;
					case 44:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 45:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 46:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 47:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 48:
						hostVarF48(3, true);
						break;
					case 49:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 50:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 51:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 52:
						gate.isoField[k] = hostFixBcd(8);
						break;
					case 53:
						gate.isoField[k] = getIsoFixLenStrToHost(8, true);
						break;
					case 54:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 55:
						hostVarF55(3, true);
						break;
					case 56:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 57:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 58:
						hostVarF58(3, true);
						break;
					case 59:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 60:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 61:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 62:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 63:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 64:
						gate.isoField[k] = hostFixBcd(8);
						break;
					case 65:
						gate.isoField[k] = hostFixBcd(8);
						break;
					case 66:
						gate.isoField[k] = getIsoFixLenStrToHost(1, true);
						break;
					case 67:
						gate.isoField[k] = getIsoFixLenStrToHost(2, true);
						break;
					case 68:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 69:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 70:
						gate.isoField[k] = getIsoFixLenStrToHost(3, true);
						break;
					case 71:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 72:
						gate.isoField[k] = getIsoFixLenStrToHost(4, true);
						break;
					case 73:
						gate.isoField[k] = getIsoFixLenStrToHost(6, true);
						break;
					case 74:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 75:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 76:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 77:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 78:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 79:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 80:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 81:
						gate.isoField[k] = getIsoFixLenStrToHost(10, true);
						break;
					case 82:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 83:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 84:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 85:
						gate.isoField[k] = getIsoFixLenStrToHost(12, true);
						break;
					case 86:
						gate.isoField[k] = getIsoFixLenStrToHost(16, true);
						break;
					case 87:
						gate.isoField[k] = getIsoFixLenStrToHost(16, true);
						break;
					case 88:
						gate.isoField[k] = getIsoFixLenStrToHost(16, true);
						break;
					case 89:
						gate.isoField[k] = getIsoFixLenStrToHost(16, true);
						break;
					case 90:
						gate.isoField[k] = getIsoFixLenStrToHost(42, true);
						break;
					case 91:
						gate.isoField[k] = getIsoFixLenStrToHost(1, true);
						break;
					case 92:
						gate.isoField[k] = getIsoFixLenStrToHost(2, true);
						break;
					case 93:
						gate.isoField[k] = getIsoFixLenStrToHost(5, true);
						break;
					case 94:
						gate.isoField[k] = getIsoFixLenStrToHost(7, true);
						break;
					case 95:
						gate.isoField[k] = getIsoFixLenStrToHost(42, true);
						break;
					case 96:
						gate.isoField[k] = hostFixBcd(8);
						break;
					case 97:
						gate.isoField[k] = getIsoFixLenStrToHost(17, true);
						break;
					case 98:
						gate.isoField[k] = getIsoFixLenStrToHost(25, true);
						break;
					case 99:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 100:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 101:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 102:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 103:
						gate.isoField[k] = getIsoVarLenStrToHost(2, true);
						break;
					case 104:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 105:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
//		    case 112: gate.isoField[k] = getIsoFixLenStrToHost(27, true);     break;
					case 112:
						hostFixF112(27, true);
						break;
					case 113:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 114:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 115:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 116:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 117:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 118:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 119:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 120:
						hostVarF120(3, true);
						break;
					case 121:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 122:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 123:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 124:
						hostVarF124(3, true);
						break;
					case 125:
						gate.isoField[k] = hostFixBcd(8);
						break;
					case 126:
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 127:
						//Kevin:20220111 VISA掛卡測試失敗fix
//						hostVarF127(3, true);
						gate.isoField[k] = getIsoVarLenStrToHost(3, true);
						break;
					case 128:
						gate.isoField[k] = hostFixBcd(8);
						break;
					default:
						break;
					}
				}
			}

			/* FISC �榡 �ഫ�� �@�P�榡 */
//kevin:remove by fail
// if ( !convertToCommon() )
//    { return false; }
			convertFiscField("C");

		} // end of try

		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	} // end of iso2Host

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

//kevin:針對fisc客製化取欄位
	private String hostVarAns(int size, int subOffset, String fieldData) {
		String subFieldData = "";
		int fieldLen = 0;
//		logger.debug("hostVarAns-size=" + size);
//		logger.debug("hostVarAns-subOffset=" + subOffset);
//		logger.debug("hostVarAns-fieldData=" + fieldData);
		fieldLen = Integer.parseInt(fieldData.substring(subOffset, subOffset + size));
//		logger.debug("hostVarAns-fieldLen=" + fieldLen);
		subOffset += size;
		subFieldData = fieldData.substring(subOffset, subOffset + fieldLen);

		subOffset += fieldLen;
		return subFieldData;
	}

	private String hostVarAns(int size) {
		String fieldData = "";
		int fieldLen = 0;

		fieldLen = Integer.parseInt(new String(gate.isoData, offset, size));
		offset += size;
		fieldData = new String(gate.isoData, offset, fieldLen);

		offset += fieldLen;
		return fieldData;
	}

	private String hostFixAns(int Len) {
		String fieldData = "";
		fieldData = new String(gate.isoData, offset, Len);
		offset += Len;
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

//kevin:取iso變動長欄位並轉碼
	private String getIsoVarLenStrToHost(int len, boolean bPIsEbcdic) throws Exception {
		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, len);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		offset += len;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

		if (bPIsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			fieldData = new String(lTmpAry, 0, lTmpAry.length);

		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
		offset += fieldLen;
		return fieldData;
	}

//kevin:取iso變動長欄位並轉碼
	private String getIsoVarLenStrToHostTest(int len, boolean bPIsEbcdic) throws Exception {
		String lenData = "", fieldData = "";
		int fieldLen = 0, subOffset = 0;
		subOffset = offset;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, subOffset, len);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		subOffset += len;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, subOffset, fieldLen);

		if (bPIsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			fieldData = new String(lTmpAry, 0, lTmpAry.length);

		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
		subOffset += fieldLen;
		return fieldData;
	}

//kevin:取iso變動長欄位並轉HEX
	private String getIsoVarLenStrToHostHex(int len, boolean bPIsEbcdic) throws Exception {
		String lenData = "", fieldData = "";
		int fieldLen = 0, subOffset = 0;
		subOffset = offset;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, subOffset, len);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		subOffset += len;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, subOffset, fieldLen);

//	if (bPIsEbcdic)
//		fieldData = HpeUtil.ebcdic2Str(lTmpAry);
//	else
//		fieldData = new String(lTmpAry, 0, lTmpAry.length);
		fieldData = HpeUtil.byte2Hex(lTmpAry);

		// fieldData = isoStringOfFisc.substring(offset, offset + fieldLen);
		subOffset += fieldLen;
		return fieldData;
	}

//kevin:取iso固定長欄位並轉碼
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

//private void hostVarF48(int size, boolean bP_IsEbcdic) throws UnsupportedEncodingException
//{
//try {
//	gate.isoField[48] = getIsoVarLenStrToHostTest(3, true);
//} catch (Exception e) {
// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//String fieldData="",checkCode1="",checkCode2="",unUseData="",lenData="";
//int    fieldLen=0 ,subOffset=0;
//
//byte[] L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, size);
//
//if (bP_IsEbcdic)
//	lenData = HpeUtil.ebcdic2Str(L_TmpAry);
//else
//	lenData = new String(L_TmpAry, 0, L_TmpAry.length);
////lenData = isoStringOfFisc.substring(offset, offset + len);
//fieldLen = Integer.parseInt(lenData);
//offset += size;
//
//L_TmpAry=HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);
//
//if (bP_IsEbcdic)
//	fieldData = HpeUtil.ebcdic2Str(L_TmpAry);
//else
//	fieldData = new String(L_TmpAry, 0, L_TmpAry.length);
//
//int checkPnt = offset+fieldLen;
//checkCode1   = fieldData.substring(subOffset,subOffset+1);
//
//if (
//	     checkCode1.equals("A") ||
//	     checkCode1.equals("C") ||
//	     checkCode1.equals("F") ||
//	     checkCode1.equals("H") ||
//	     checkCode1.equals("O") ||
//	     checkCode1.equals("P") ||
//	     checkCode1.equals("R") ||
//	     checkCode1.equals("T") ||
//	     checkCode1.equals("U") ||
//	     checkCode1.equals("X") ||
//	     checkCode1.equals("Z")
//	   )
//	   { offset++; gate.tccCode = checkCode1; subOffset++; }
//
//while ( offset < checkPnt )
//{
//  checkCode2 = fieldData.substring(subOffset,subOffset+2);
//  logger.debug("hostVarF48-checkCode2=" + checkCode2);
//  offset +=2; subOffset +=2;
//  if ( checkCode2.equals("11") )
//     { gate.keyExchangeBlock = hostVarAns(2, subOffset, fieldData);  
//       subOffset += gate.keyExchangeBlock.length()+2;
//       offset += gate.keyExchangeBlock.length()+2;}
//  else
//  if ( checkCode2.equals("26") )
//     { gate.walletIdentifier = hostVarAns(2, subOffset, fieldData);  
//       subOffset += gate.walletIdentifier.length()+2;
//       offset += gate.walletIdentifier.length()+2;}
//  else
//  if ( checkCode2.equals("42") ) 
//     { gate.f48t42 = hostVarAns(2, subOffset, fieldData); //kevin:整個tag都收進來再處理，避免欄位算錯
//  	   gate.ucafInd = gate.f48t42.substring(6);
//       subOffset += gate.f48t42.length()+2; 
//       offset += gate.f48t42.length()+2;}
//  else
//  if ( checkCode2.equals("43") )
//     { gate.ucaf = hostVarAns(2, subOffset, fieldData); 
//       subOffset += gate.ucaf.length()+2;
//       offset += gate.ucaf.length()+2;}
//  else
//  if ( checkCode2.equals("44") )
//     { gate.xid  = hostVarAns(2, subOffset, fieldData); 
//       subOffset += gate.xid.length()+2; 
//       offset += gate.xid.length()+2;}
//  else
//  if ( checkCode2.equals("61") )
//     { gate.posConditionCode  = hostVarAns(2, subOffset, fieldData); 
//       subOffset += gate.posConditionCode.length()+2; 
//       offset += gate.posConditionCode.length()+2;}
//  else	  
//  if ( checkCode2.equals("63") )
//     { gate.traceId  = hostVarAns(2, subOffset, fieldData); 
//       subOffset += gate.traceId.length()+2; 
//       offset += gate.traceId.length()+2;}
//  else	  	  
//  if ( checkCode2.equals("92") )
//     { gate.cvv2 = hostVarAns(2, subOffset, fieldData);
//       subOffset += gate.cvv2.length()+2; 
//       offset += gate.cvv2.length()+2;}
//  else
//     { unUseData = hostVarAns(2, subOffset, fieldData); 
//       logger.debug("hostVarF48-unUseData="+unUseData+"len="+subOffset);
//       subOffset += unUseData.length()+2; 
//       offset += unUseData.length()+2;
//       logger.debug("hostVarF48-len="+subOffset);}
//}
//offset = checkPnt;
//return;
//}

	private void hostVarF48(int size, boolean bPIsEbcdic) throws Exception {

		gate.isoField[48] = getIsoVarLenStrToHostTest(3, true);

		gate.isoField[126] = getIsoVarLenStrToHostHex(3, true);

		String fieldData = "", checkCode1 = "", checkCode2 = "", unUseData = "", lenData = "";
		int fieldLen = 0, subOffset = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);
//		logger.debug("@@@@@F48_LEN_HEX = " + HpeUtil.byte2HexStr(lTmpAry));

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
//lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		offset += size;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);
//		logger.debug("@@@@@F48_LEN_DATA = " + HpeUtil.byte2HexStr(lTmpAry));

		if (bPIsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			fieldData = new String(lTmpAry, 0, lTmpAry.length);

		int checkPnt = offset + fieldLen;
		checkCode1 = fieldData.substring(subOffset, subOffset + 1);

		if (checkCode1.equals("A") || checkCode1.equals("C") || checkCode1.equals("F") || checkCode1.equals("H") ||
			checkCode1.equals("O") || checkCode1.equals("P") || checkCode1.equals("R") || checkCode1.equals("T") ||
			checkCode1.equals("U") || checkCode1.equals("X") || checkCode1.equals("Z")) {
			offset++;
			gate.tccCode = checkCode1;
			subOffset++;
		}

		while (offset < checkPnt) {
			checkCode2 = fieldData.substring(subOffset, subOffset + 2);
//			logger.debug("hostVarF48-checkCode2=" + checkCode2);
			offset += 2;
			subOffset += 2;
			if (checkCode2.equals("10")) {
				gate.keyExchangeKey = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.keyExchangeKey.length() + 2;
				offset += gate.keyExchangeKey.length() + 2;
			} else if (checkCode2.equals("11")) {
				gate.keyExchangeBlock = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.keyExchangeBlock.length() + 2;
				offset += gate.keyExchangeBlock.length() + 2;
			} else if (checkCode2.equals("23")) {
				gate.f48T23 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T23.length() + 2;
				offset += gate.f48T23.length() + 2;
			} else if (checkCode2.equals("26")) {
				gate.walletIdentifier = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.walletIdentifier.length() + 2;
				offset += gate.walletIdentifier.length() + 2;
			} else if (checkCode2.equals("30")) {
				gate.f48T30 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T30.length() + 2;
				offset += gate.f48T30.length() + 2;
			} else if (checkCode2.equals("33")) {
				gate.f48T33 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T33.length() + 2;
				offset += gate.f48T33.length() + 2;
			} else if (checkCode2.equals("37")) {
				gate.f48T37 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T37.length() + 2;
				offset += gate.f48T37.length() + 2;
			} else if (checkCode2.equals("40")) {
				gate.f48T40 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T40.length() + 2;
				offset += gate.f48T40.length() + 2;
			} else if (checkCode2.equals("41")) {
				gate.f48T41 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T41.length() + 2;
				offset += gate.f48T41.length() + 2;
			} else if (checkCode2.equals("42")) {
				gate.f48T42 = hostVarAns(2, subOffset, fieldData); // kevin:整個tag都收進來再處理，避免欄位算錯
// 	   gate.ucafInd = gate.f48t42.substring(4);
				logger.debug("@@@@@ECI or UCAF IND=" + gate.f48T42.substring(4));
				subOffset += gate.f48T42.length() + 2;
				offset += gate.f48T42.length() + 2;
			} else if (checkCode2.equals("43")) {
				gate.f48T43 = hostVarAns(2, subOffset, fieldData);
				subOffset += 2;
				offset += 2;
				logger.debug("@@@@gate.f48t43 len = " + gate.f48T43.length());
				gate.ucaf = hostFixBcd(gate.f48T43.length());
				logger.debug("@@@@gate.ucaf = " + gate.ucaf);
				subOffset += gate.f48T43.length();
			}
//     offset += gate.f48t43.length();}
			else if (checkCode2.equals("44")) {
				gate.f48T44 = hostVarAns(2, subOffset, fieldData);
				subOffset += 2;
				offset += 2;
				gate.xid = hostFixAns(gate.f48T44.length());
				logger.debug("@@@@gate.xid = " + gate.xid);
				subOffset += (gate.f48T44.length());
			}
//     offset += (gate.f48t44.length());}
			else if (checkCode2.equals("45")) {
				gate.cavvResult = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.cavvResult.length() + 2;
				offset += gate.cavvResult.length() + 2;
			} else if (checkCode2.equals("61")) {
				gate.posConditionCode = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.posConditionCode.length() + 2;
				offset += gate.posConditionCode.length() + 2;
			} else if (checkCode2.equals("63")) {
				gate.traceId = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.traceId.length() + 2;
				offset += gate.traceId.length() + 2;
			} else if (checkCode2.equals("66")) {
				gate.f48T66 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T66.length() + 2;
				offset += gate.f48T66.length() + 2;
			} else if (checkCode2.equals("71")) {
				gate.f48T71 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T71.length() + 2;
				offset += gate.f48T71.length() + 2;
			} else if (checkCode2.equals("72")) {
				gate.f48T72 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T72.length() + 2;
				offset += gate.f48T72.length() + 2;
			} else if (checkCode2.equals("74")) {
				gate.f48T74 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T74.length() + 2;
				offset += gate.f48T74.length() + 2;
			} else if (checkCode2.equals("77")) {
				gate.f48T77 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T77.length() + 2;
				offset += gate.f48T77.length() + 2;
			} else if (checkCode2.equals("79")) {
				gate.f48T79 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T79.length() + 2;
				offset += gate.f48T79.length() + 2;
			} else if (checkCode2.equals("82")) {
				gate.f48T82 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T82.length() + 2;
				offset += gate.f48T82.length() + 2;
			} else if (checkCode2.equals("83")) {
				gate.f48T83 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T83.length() + 2;
				offset += gate.f48T83.length() + 2;
			} else if (checkCode2.equals("87")) {
				gate.f48T87 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T87.length() + 2;
				offset += gate.f48T87.length() + 2;
			} else if (checkCode2.equals("88")) {
				gate.f48T88 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T88.length() + 2;
				offset += gate.f48T88.length() + 2;
			} else if (checkCode2.equals("89")) {
				gate.f48T89 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T89.length() + 2;
				offset += gate.f48T89.length() + 2;
			} else if (checkCode2.equals("90")) {
				gate.f48T90 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T90.length() + 2;
				offset += gate.f48T90.length() + 2;
			} else if (checkCode2.equals("92")) {
				gate.cvv2 = hostVarAns(2, subOffset, fieldData);
				logger.debug("@@@@gate.cvv2 = " + gate.cvv2);
				subOffset += gate.cvv2.length() + 2;
				offset += gate.cvv2.length() + 2;
			} else if (checkCode2.equals("95")) {
				gate.f48T95 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f48T95.length() + 2;
				offset += gate.f48T95.length() + 2;
			} else {
				unUseData = hostVarAns(2, subOffset, fieldData);
				logger.debug("hostVarF48-unUseData=" + unUseData + "len=" + subOffset);
				subOffset += unUseData.length() + 2;
				offset += unUseData.length() + 2;
				logger.debug("hostVarF48-len=" + subOffset);
			}
		}
		offset = checkPnt;
		return;
	}
//fieldLen     = Integer.parseInt(new String(gate.isoData,offset,size));
//offset      += size;
//fieldData    = new String(gate.isoData,offset,fieldLen);
//int checkPnt = offset+fieldLen;
//checkCode1   = new String(gate.isoData,offset,1);

//if (
//     checkCode1.equals("A") ||
//     checkCode1.equals("C") ||
//     checkCode1.equals("F") ||
//     checkCode1.equals("H") ||
//     checkCode1.equals("O") ||
//     checkCode1.equals("P") ||
//     checkCode1.equals("R") ||
//     checkCode1.equals("T") ||
//     checkCode1.equals("U") ||
//     checkCode1.equals("X") ||
//     checkCode1.equals("Z")
//   )
//   { offset++; gate.tccCode = checkCode1; }

//while ( offset < checkPnt )
// {
//   checkCode2 = new String(gate.isoData,offset,2);
//   offset +=2;
//   if ( checkCode2.equals("11") )
//      { gate.keyExchangeBlock = hostVarAns(2);     }
//   else
//   if ( checkCode2.equals("42") )
//      { gate.ucafInd = hostVarAns(2).substring(6); }
//   else
//   if ( checkCode2.equals("43") )
//      { gate.ucaf = hostVarAns(2); }
//   else
//   if ( checkCode2.equals("44") )
//      { gate.xid  = hostVarBcd(2); }
//   else
//   if ( checkCode2.equals("92") )
//      { gate.cvv2 = hostVarAns(2); }
//   else
//      { unUseData = hostVarAns(2); }
// }
//
//offset = checkPnt;
//return;
//}

	private String hostVarBcd(int size) {
		String fieldData = "";
		int fieldLen = 0;

		fieldLen = Integer.parseInt(new String(gate.isoData, offset, size));
		offset += size;

		fieldData = hostFixBcd(fieldLen);
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

	private void hostVarF55(int size, boolean bPIsEbcdic) throws Exception {
		gate.isoField[55] = getIsoVarLenStrToHostHex(3, true);

		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		fieldLen = Integer.parseInt(lenData);
		offset += size;

		gate.isoField[55] = hostFixBcdTest(fieldLen);

		fieldData = new String(gate.isoData, offset, fieldLen);
		// kevin:DE55不等於emvTrans
//	gate.emvTrans = true;
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
			} else if (gate.isoData[offset] == (byte) 0x4F) {
				offset++;
				gate.emv4F = hostVarBinBcd(1);
			} else {
				offset += 2;
				hostVarBinBcd(1);
			}
		}

		return;
	}

//FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##START##
	private void hostVarF58(int size, boolean bPIsEbcdic) throws Exception {
		gate.isoField[58] = getIsoVarLenStrToHostTest(3, true);

		String fieldData = "", checkCode = "", unUseData = "", lenData = "";
		int fieldLen = 0, subOffset = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		// lenData = isoStringOfFisc.substring(offset, offset + len);
		fieldLen = Integer.parseInt(lenData);
		offset += size;

		lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, fieldLen);

		if (bPIsEbcdic)
			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			fieldData = new String(lTmpAry, 0, lTmpAry.length);

		// gate.emvTrans = true;
		int checkPnt = offset + fieldLen;
		while (offset < checkPnt) {
			checkCode = fieldData.substring(subOffset, subOffset + 2);
			offset += 2;
			subOffset += 2;
			// Tag21- 紅利扣抵資訊 (0100/0110;0420/0430)
			if (checkCode.equals("21")) {
				gate.f58T21 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T21.length() + 2;
				offset += gate.f58T21.length() + 2;
			} else
			// Tag28- 電子化繳費稅處理平台發卡參加機構代碼 (0100/0110;0420/0430)
			if (checkCode.equals("28")) {
				gate.f58T28 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T28.length() + 2;
				offset += gate.f58T28.length() + 2;
			} else
			// Tag30- 大賣場收單處理單位代號 (0100/0110;0420/0430)
			if (checkCode.equals("30")) {
				gate.f58T30 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T30.length() + 2;
				offset += gate.f58T30.length() + 2;
			} else
			// Tag31- 雙幣卡匯率轉換資訊 (0100/0110;0120/0130;0420/0430)
			if (checkCode.equals("31")) {
				gate.f58T31 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T31.length() + 2;
				offset += gate.f58T31.length() + 2;
			} else
			// Tag32- TSP Transaction Data (0100/0110;0120/0130;0420/0430)
			if (checkCode.equals("32")) {
				gate.f58T32 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T32.length() + 2;
				offset += gate.f58T32.length() + 2;
			} else
			// Tag33- 電子化繳費稅處理平台代收行 (0100;0420)
			if (checkCode.equals("33")) {
				gate.f58T33 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T33.length() + 2;
				offset += gate.f58T33.length() + 2;
			} else
			// Tag49- 信用卡載具資訊 (0100/0110)
			if (checkCode.equals("49")) {
				gate.f58T49 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T49.length() + 2;
				offset += gate.f58T49.length() + 2;
			} else
			// Tag50- 銀聯優計劃 Coupon資訊 (0100/0110;0420/0430)
			if (checkCode.equals("50")) {
				gate.f58T50 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T50.length() + 2;
				offset += gate.f58T50.length() + 2;
			} else
			// Tag51- 銀 聯 QR Code Voucher Number (0100/0110;0420/0430)
			if (checkCode.equals("51")) {
				gate.f58T51 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T51.length() + 2;
				offset += gate.f58T51.length() + 2;
			} else
			// Tag53- 銀聯實時立減折扣資訊 (0100/0110)
			if (checkCode.equals("53")) {
				gate.f58T53 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T53.length() + 2;
				offset += gate.f58T53.length() + 2;
			} else
			// Tag56-Payment Account Reference (PAR) (0100/0110;0120;0420/0430)
			if (checkCode.equals("56")) {
				gate.f58T56 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T56.length() + 2;
				offset += gate.f58T56.length() + 2;
			} else
			// Tag60- 交易識別碼 (0100/0110;0120;0420)
			if (checkCode.equals("60")) {
				gate.f58T60 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T60.length() + 2;
				offset += gate.f58T60.length() + 2;
			} else
			// Tag61-授權欄位驗證碼 (0110)
			if (checkCode.equals("61")) {
				gate.f58T61 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T61.length() + 2;
				offset += gate.f58T61.length() + 2;
			} else
			// Tag62-卡片級別識別碼 (0100/0110;0120/0130;0420/0430)
			if (checkCode.equals("62")) {
				gate.f58T62 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T62.length() + 2;
				offset += gate.f58T62.length() + 2;
			} else
			// Tag63-授權通知來源 (0120)
			if (checkCode.equals("63")) {
				gate.f58T63 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T63.length() + 2;
				offset += gate.f58T63.length() + 2;
			} else
			// Tag64-銀聯交易傳輸日期時間 (0110)
			if (checkCode.equals("64")) {
				gate.f58T64 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T64.length() + 2;
				offset += gate.f58T64.length() + 2;
			} else
			// Tag65-銀聯交易序號 (0110)
			if (checkCode.equals("65")) {
				gate.f58T65 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T65.length() + 2;
				offset += gate.f58T65.length() + 2;
			} else
			// Tag66-代收通知理由碼 (0120;0620)
			if (checkCode.equals("66")) {
				gate.f58T66 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T66.length() + 2;
				offset += gate.f58T66.length() + 2;
			} else
			// Tag67-卡號比對資訊ID (0100;0420)
			if (checkCode.equals("67")) {
				gate.f58T67Id = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T67Id.length() + 2;
				offset += gate.f58T67Id.length() + 2;
			} else
			// Tag68-卡號比對資訊/繳費稅交易傳送機構 (0100;0420)
			if (checkCode.equals("68")) {
				gate.f58T68IdCheckType = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T68IdCheckType.length() + 2;
				offset += gate.f58T68IdCheckType.length() + 2;
			} else
			// Tag69-信用卡特殊平台交易識別碼 (0100/0110;0120/0130;0420/0430)
			if (checkCode.equals("69")) {
				gate.f58T69SpecialTxn = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T69SpecialTxn.length() + 2;
				offset += gate.f58T69SpecialTxn.length() + 2;
			} else
			// Tag70-VISA國 際組織訊息理 由碼 (0100;0120;0302)
			if (checkCode.equals("70")) {
				gate.f58T70 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T70.length() + 2;
				offset += gate.f58T70.length() + 2;
			} else
			// Tag71-VISA國際組織檔案維護訊息錯誤 碼 (0312)
			if (checkCode.equals("71")) {
				gate.f58T71 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T71.length() + 2;
				offset += gate.f58T71.length() + 2;
			} else
			// Tag72-FiscerCard國際組織通知細部理由碼 (0120;0620)
			if (checkCode.equals("72")) {
				gate.f58T72 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T72.length() + 2;
				offset += gate.f58T72.length() + 2;
			} else
			// Tag73-Token交易類型 (0100/0110;0120/0130;0420/0430;0620/0630)
			if (checkCode.equals("73")) {
				gate.f58T73TokenType = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T73TokenType.length() + 2;
				offset += gate.f58T73TokenType.length() + 2;
			} else
			// Tag80-悠遊卡端末設備交易日期時間 (0100;0120)
			if (checkCode.equals("80")) {
				gate.f58T80 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T80.length() + 2;
				offset += gate.f58T80.length() + 2;
			} else
			// Tag81-悠遊卡端末設備交易序號 (0100;0120)
			if (checkCode.equals("81")) {
				gate.f58T81 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T81.length() + 2;
				offset += gate.f58T81.length() + 2;
			} else
			// Tag82-一 卡 通NTID序號 (0100/0110;0120/0130;0420/0430)
			if (checkCode.equals("82")) {
				gate.f58T82 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T82.length() + 2;
				offset += gate.f58T82.length() + 2;
			} else
			// Tag83-悠遊卡主機端交易日期時間 0312
			if (checkCode.equals("83")) {
				gate.f58T83 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T83.length() + 2;
				offset += gate.f58T83.length() + 2;
			} else
			// Tag84-愛金卡交易 資訊 (0100/0110;0120/0130;0312;0420/0430
			if (checkCode.equals("84")) {
				gate.f58T84 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T84.length() + 2;
				offset += gate.f58T84.length() + 2;
			} else
			// Tag85-愛金卡掛卡/ 取消掛卡回覆碼 (0312)
			if (checkCode.equals("85")) {
				gate.f58T85 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T85.length() + 2;
				offset += gate.f58T85.length() + 2;
			} else
			// Tag86-信用卡載具中獎入戶同意註記資訊 (0110)
			if (checkCode.equals("86")) {
				gate.f58T86 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T86.length() + 2;
				offset += gate.f58T86.length() + 2;
			} else
			// Tag87-輔助身分驗證資訊 (0110)
			if (checkCode.equals("87")) {
				gate.f58T87 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T87.length() + 2;
				offset += gate.f58T87.length() + 2;
			} else
			// Tag90-Merchant PAN (0100;0120;0420)
			if (checkCode.equals("90")) {
				gate.f58T90 = hostVarAns(2, subOffset, fieldData);
				subOffset += gate.f58T90.length() + 2;
				offset += gate.f58T90.length() + 2;
			} else {
				unUseData = hostVarAns(2, subOffset, fieldData);
				subOffset += unUseData.length() + 2;
				offset += unUseData.length() + 2;
			}
		}

		return;
	}

//FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200304 , ##END##
	private void hostFixF112(int size, boolean bP_IsEbcdic) throws Exception {

		gate.isoField[112] = getIsoFixLenStrToHost(size, bP_IsEbcdic);

		gate.divMark = gate.isoField[112].substring(0, 1);// 分期種類
		gate.divNum = gate.isoField[112].substring(1, 3);// 分期數
		gate.firstAmt = gate.isoField[112].substring(3, 11);// 首期金額
		gate.everyAmt = gate.isoField[112].substring(11, 19);// 每期金額
		gate.procAmt  = gate.isoField[112].substring(19, 25);// 手續費
		

	}

	/**
	 * FISC MESSAGE DATA ELEMENT #120 ADDITIONAL DATA – PRIVATE USE
	 * V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理
	 * @throws Exception if any exception
	 */
	private void hostVarF120(int size, boolean bPIsEbcdic) throws Exception {

		gate.isoField[120] = getIsoVarLenStrToHostTest(3, true);

		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		fieldLen = Integer.parseInt(lenData);
		offset += size;

		fieldData = new String(gate.isoData, offset, fieldLen);
		// kevin:DE120不等於emvTrans
//	gate.emvTrans = true;
		logger.debug("msg_type = " + gate.mesgType + ";gate.f58t73 =>" + gate.f58T73TokenType);
		int checkPnt = offset + fieldLen;
		while (offset < checkPnt) {
			if (gate.mesgType.equals("0620")) {
				if (gate.f58T73TokenType.equals("MTCN")) {
					gate.f120MTCN = getIsoFixLenStrToHost(fieldLen, true);
					logger.debug("gate.f120MTCN = " + gate.f120MTCN);
					gate.tokenS8AcctNum = gate.f120MTCN.substring(0, 16);
					gate.tpanTicketNo = gate.tokenS8AcctNum;
					String slExpire = gate.f120MTCN.substring(19, 23);
					gate.tpanExpire = HpeUtil.getMonthEndDate("20" + slExpire.substring(0, 2),
							slExpire.substring(2, 4));
					gate.expireDate = slExpire; // MTCN交易不會帶PAN卡片expire date，所以改用TPAN expire date代替
					gate.tokenProvider = gate.f120MTCN.substring(46, 47);// TOKEN_PROVIDER
					gate.assuranceLevel = gate.f120MTCN.substring(47, 49);// TOKEN_ASSURANCE_LEVEL
					gate.tokenRequetorId = gate.f120MTCN.substring(49, 60);// TOKEN_REQUESTOR_ID
					gate.contactlessUsage = gate.f120MTCN.substring(60, 61);// CONTACTLESS_USAGE
					gate.ecUsage = gate.f120MTCN.substring(61, 62);// EC_USAGE
					gate.mobileEcUsage = gate.f120MTCN.substring(62, 63);// MOBILE_EC_USAGE
					gate.correlationId = gate.f120MTCN.substring(63, 77);// CORRELATION_ID
					gate.numOfActiveToken = gate.f120MTCN.substring(77, 79); // ACTIVE_TOKENS
					gate.issueProductId = gate.f120MTCN.substring(79, 89); // ISSUE_PRODUCT_ID
					gate.consumerLanguage = gate.f120MTCN.substring(89, 91); // CONSUMER_LANGUAGE
					gate.deviceName = gate.f120MTCN.substring(91, 111); // DEVICE_NAME
					gate.finalDecision = gate.f120MTCN.substring(111, 112); // FINAL_TOKENIZATION_DECISION
					gate.finalInd = gate.f120MTCN.substring(112, 113); // FINAL_TOKENIZATION_IND
					gate.tcIndentifier = gate.f120MTCN.substring(113, 145); // T_C_IDENTIFIER
					gate.tcDateTime = gate.f120MTCN.substring(145, 155); // T_C_DATE_TIME
					gate.activeAttempts = gate.f120MTCN.substring(155, 156); // ACTIVATION_ATTEMPTS
					gate.tokenUniqueRef = gate.f120MTCN.substring(156, 204); // TOKEN_UNIQUE_REF
					gate.acctNumberRef = gate.f120MTCN.substring(204, 252); // ACCOUNT_NUMBER_REF
					gate.tokenType = gate.f120MTCN.substring(252, 253); // TOKEN_TYPE
					gate.walletId = gate.f120MTCN.substring(253, 256); // WALLET_ID
					if (fieldLen > 256) {
						gate.deviceType = gate.f120MTCN.substring(256, 258); // DEVICE_TYPE
					}
				} else if (gate.f58T73TokenType.equals("MTEN")) {
					gate.f120MTEN = getIsoFixLenStrToHost(fieldLen, true);
					logger.debug("gate.f120MTEN = " + gate.f120MTEN);
					gate.tokenS8AcctNum = gate.f120MTEN.substring(23, 39); //V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理
					gate.tpanTicketNo = gate.tokenS8AcctNum;
					String slExpire = gate.f120MTEN.substring(42, 46);
					gate.tpanExpire = HpeUtil.getMonthEndDate("20" + slExpire.substring(0, 2),
							slExpire.substring(2, 4));
					gate.expireDate = slExpire; // MTEN交易不會帶PAN卡片expire date，所以改用TPAN expire date代替
					gate.tokenProvider = gate.f120MTEN.substring(46, 47);// TOKEN_PROVIDER
					gate.correlationId = gate.f120MTEN.substring(47, 61);// CORRELATION_ID
					gate.tokenEvent = gate.f120MTEN.substring(61, 62);// TOKEN_EVENT 3 = Deactivate;6 = Suspend;7 =
																		// Resume;8 = Exception Event
					gate.tokenEventReason = gate.f120MTEN.substring(62, 64);// TOKEN_EVENT_REASON
					gate.contactlessUsage = gate.f120MTEN.substring(64, 65);// CONTACTLESS_USAGE
					gate.ecUsage = gate.f120MTEN.substring(65, 66);// EC_USAGE
					gate.mobileEcUsage = gate.f120MTEN.substring(66, 67);// MOBILE_EC_USAGE
					gate.eventRequestor = gate.f120MTEN.substring(67, 68);// EVENT_REQUESTOR
					gate.tokenRequetorId = gate.f120MTEN.substring(68, 79);// TOKEN_REQUESTOR_ID
					gate.walletId = gate.f120MTEN.substring(79, 82); // WALLET_ID
					if (fieldLen > 82) {
						gate.deviceType = gate.f120MTEN.substring(82, 84); // DEVICE_TYPE
					}
				}
			} else if (gate.mesgType.equals("0302")) {
				gate.f120T0302 = getIsoFixLenStrToHost(fieldLen, true);
				logger.debug("gate.f120T0302 = " + gate.f120T0302);
			} else {
				gate.f120None = getIsoFixLenStrToHost(fieldLen, true);
				logger.debug("gate.f120None = " + gate.f120None);
			}
			return;
		}
	}

//FISC MESSAGE DATA ELEMENT #124 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START##
	private void hostVarF124(int size, boolean bPIsEbcdic) throws Exception {

		gate.isoField[124] = getIsoVarLenStrToHostHex(3, true);

		String lenData = "", fieldData = "";
		int fieldLen = 0;

		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);

		if (bPIsEbcdic)
			lenData = HpeUtil.ebcdic2Str(lTmpAry);
		else
			lenData = new String(lTmpAry, 0, lTmpAry.length);
		fieldLen = Integer.parseInt(lenData);
		offset += size;

//	fieldData = new String(gate.isoData,offset,fieldLen);
		// kevin:DE124不等於emvTrans
//	gate.emvTrans = true;
		int checkPnt = offset + fieldLen;
		while (offset < checkPnt) {
			if (gate.isoData[offset] == (byte) 0x01) {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					if (gate.isoData[offset] == (byte) 0xC0) {
						offset++;
						gate.f124T01C0 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t01C0 = " + gate.f124T01C0);
					} else if (gate.isoData[offset] == (byte) 0xCF) {
						offset++;
						gate.f124T01CF = hostVarBinEbcdic(1);
						logger.debug("gate.f124t01CF = " + gate.f124T01CF);
					} else if (gate.isoData[offset] == (byte) 0xD4) {
						offset++;
						gate.f124T01D4 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t01D4 = " + gate.f124T01D4);
					} else {
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			} else if (gate.isoData[offset] == (byte) 0x02) {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					if (gate.isoData[offset] == (byte) 0x03) {
						offset++;
						gate.f124T0203 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0203 = " + gate.f124T0203);
					} else if (gate.isoData[offset] == (byte) 0xCF) {
						offset++;
						gate.f124T0204 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0204 = " + gate.f124T0204);
					} else {
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			} else if (gate.isoData[offset] == (byte) 0x03) {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					if (gate.isoData[offset] == (byte) 0x1F && gate.isoData[offset + 1] == (byte) 0x31) {
						offset++;
						gate.f124T1F31 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t1F31 = " + gate.f124T1F31);
					} else if (gate.isoData[offset] == (byte) 0x1F && gate.isoData[offset + 1] == (byte) 0x32) {
						offset++;
						gate.f124T1F32 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t1F32 = " + gate.f124T1F32);
					} else if (gate.isoData[offset] == (byte) 0x1F && gate.isoData[offset + 1] == (byte) 0x33) {
						offset++;
						gate.f124T1F33 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t1F33 = " + gate.f124T1F33);
					} else if (gate.isoData[offset] == (byte) 0x01) {
						offset++;
						gate.f124T0301 = hostVarBinEbcdic(1);
						gate.tokenS8AcctNum = gate.f124T0301;
						gate.tpanTicketNo = gate.f124T0301;
						gate.bgTokenQ9FormatIsVisa = true;
						logger.debug("gate.f124t0301 = " + gate.f124T0301);
					} else if (gate.isoData[offset] == (byte) 0x02) {
						offset++;
						gate.f124T0302 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0302 = " + gate.f124T0302);
						gate.assuranceLevel = gate.f124T0302;
					} else if (gate.isoData[offset] == (byte) 0x03) {
						offset++;
						gate.f124T0303 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0303 = " + gate.f124T0303);
						gate.tokenRequetorId = gate.f124T0303;
					} else if (gate.isoData[offset] == (byte) 0x04) {
						offset++;
						gate.f124T0304 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0304 = " + gate.f124T0304);
					} else if (gate.isoData[offset] == (byte) 0x05) {
						offset++;
						gate.f124T0305 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0305 = " + gate.f124T0305);
						gate.tcIndentifier = gate.f124T0305;
					} else if (gate.isoData[offset] == (byte) 0x06) {
						offset++;
						gate.f124T0306 = hostVarBinEbcdic(1);
						String slExpire = gate.f124T0306;
						gate.tpanExpire = HpeUtil.getMonthEndDate("20" + slExpire.substring(0, 2),
								slExpire.substring(2, 4));
						logger.debug("gate.tpanExpire = " + gate.tpanExpire);
					} else if (gate.isoData[offset] == (byte) 0x07) {
						offset++;
						gate.f124T0307 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0307 = " + gate.f124T0307);
						gate.tokenType = gate.f124T0307;
					} else if (gate.isoData[offset] == (byte) 0x08) {
						offset++;
						gate.f124T0308 = hostVarBinEbcdic(1);
						gate.tpanStatusCode = gate.f124T0308;
						logger.debug("gate.tpanStatusCode = " + gate.tpanStatusCode);
					} else if (gate.isoData[offset] == (byte) 0x0A) {
						offset++;
						gate.f124T030A = hostVarBinEbcdic(1);
						logger.debug("gate.f124t030A = " + gate.f124T030A);
					} else if (gate.isoData[offset] == (byte) 0x0B) {
						offset++;
						gate.f124T030B = hostVarBinEbcdic(1);
						gate.acctNumberRef = gate.f124T030B;
						logger.debug("gate.f124t030B = " + gate.f124T030B);
					} else if (gate.isoData[offset] == (byte) 0x1A) {
						offset++;
						gate.f124T031A = hostVarBinEbcdic(1);
						logger.debug("gate.f124t031A = " + gate.f124T031A);
					} else if (gate.isoData[offset] == (byte) 0x1B) {
						offset++;
						gate.f124T031B = hostVarBinEbcdic(1);
						logger.debug("gate.f124t031B = " + gate.f124T031B);
					} else if (gate.isoData[offset] == (byte) 0x1C) {
						offset++;
						gate.f124T031C = hostVarBinEbcdic(1);
						gate.activeAttempts = gate.f124T031C;
						logger.debug("gate.f124t031C = " + gate.f124T031C);
					} else if (gate.isoData[offset] == (byte) 0x1D) {
						offset++;
						gate.f124T031D = hostVarBinEbcdic(1);
						logger.debug("gate.f124t031D = " + gate.f124T031D);
					} else if (gate.isoData[offset] == (byte) 0x10) {
						offset++;
						gate.f124T0310 = hostVarBinEbcdic(1);
						gate.accountScore = gate.f124T0310;
						logger.debug("gate.f124t0310 = " + gate.f124T0310);
					} else if (gate.isoData[offset] == (byte) 0x11) {
						offset++;
						gate.f124T0311 = hostVarBinEbcdic(1);
						gate.finalDecision = gate.f124T0311;
						logger.debug("gate.f124t0311 = " + gate.f124T0311);
					} else if (gate.isoData[offset] == (byte) 0x12) {
						offset++;
						gate.f124T0312 = hostVarBinEbcdic(1);
						gate.numOfActiveToken = gate.f124T0312;
						logger.debug("gate.f124t0312 = " + gate.f124T0312);
					} else if (gate.isoData[offset] == (byte) 0x13) {
						offset++;
						gate.f124T0313 = hostVarBinEbcdic(1);
						gate.numOfInactiveToken = gate.f124T0313;
						logger.debug("gate.f124t0313 = " + gate.f124T0313);
					} else if (gate.isoData[offset] == (byte) 0x14) {
						offset++;
						gate.f124T0314 = hostVarBinEbcdic(1);
						gate.numOfSuspendedToken = gate.f124T0314;
						logger.debug("gate.f124t0314 = " + gate.f124T0314);
					} else {
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			} else if (gate.isoData[offset] == (byte) 0x04) {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					if (gate.isoData[offset] == (byte) 0x01) {
						offset++;
						gate.f124T0401 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0401 = " + gate.f124T0401);
					} else if (gate.isoData[offset] == (byte) 0x02) {
						offset++;
						gate.f124T0402 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0402 = " + gate.f124T0402);
					} else if (gate.isoData[offset] == (byte) 0x03) {
						offset++;
						gate.f124T0403 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0403 = " + gate.f124T0403);
					} else if (gate.isoData[offset] == (byte) 0x04) {
						offset++;
						gate.f124T0404 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0404 = " + gate.f124T0404);
					} else if (gate.isoData[offset] == (byte) 0x05) {
						offset++;
						gate.f124T0405 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0405 = " + gate.f124T0405);
					} else if (gate.isoData[offset] == (byte) 0x06) {
						offset++;
						gate.f124T0406 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0406 = " + gate.f124T0406);
					} else if (gate.isoData[offset] == (byte) 0x07) {
						offset++;
						gate.f124T0407 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0407 = " + gate.f124T0407);
					} else {
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			} else if (gate.isoData[offset] == (byte) 0x05) {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					if (gate.isoData[offset] == (byte) 0x02) {
						offset++;
						gate.f124T0502 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0502 = " + gate.f124T0502);
					} else if (gate.isoData[offset] == (byte) 0x03) {
						offset++;
						gate.f124T0503 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0503 = " + gate.f124T0503);
					} else if (gate.isoData[offset] == (byte) 0x04) {
						offset++;
						gate.f124T0504 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0504 = " + gate.f124T0504);
					} else if (gate.isoData[offset] == (byte) 0x05) {
						offset++;
						gate.f124T0505 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0505 = " + gate.f124T0505);
					} else if (gate.isoData[offset] == (byte) 0x06) {
						offset++;
						gate.f124T0506 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0506 = " + gate.f124T0506);
					} else if (gate.isoData[offset] == (byte) 0x07) {
						offset++;
						gate.f124T0507 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0507 = " + gate.f124T0507);
					} else if (gate.isoData[offset] == (byte) 0x08) {
						offset++;
						gate.f124T0508 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0508 = " + gate.f124T0508);
					} else if (gate.isoData[offset] == (byte) 0x09) {
						offset++;
						gate.f124T0509 = hostVarBinEbcdic(1);
						logger.debug("gate.f124t0509 = " + gate.f124T0509);
					} else if (gate.isoData[offset] == (byte) 0x0A) {
						offset++;
						gate.f124T050A = hostVarBinEbcdic(1);
						logger.debug("gate.f124t050A = " + gate.f124T050A);
					} else {
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			} else if (gate.isoData[offset] == (byte) 0xE3 && gate.isoData[offset + 1] == (byte) 0xC1) { // MASTERCARD
																											// FORMAT
				gate.f124TA = getIsoFixLenStrToHost(fieldLen, true);
				logger.debug("gate.f124TA = " + gate.f124TA);
			} else if (gate.isoData[offset] == (byte) 0xE3 && gate.isoData[offset + 1] == (byte) 0xC5) { // MASTERCARD
																											// FORMAT
				gate.f124TE = getIsoFixLenStrToHost(fieldLen, true);
				logger.debug("gate.f124TE  = " + gate.f124TE);
			} else {
				offset++;
				int checkTnt = offset + hostVarBinLen(2);
				while (offset < checkTnt) {
					{
						offset++;
						gate.f124None = hostVarBinEbcdic(1);
						logger.debug("gate.f124tNone = " + gate.f124None);
					}
				}
			}
		}

		return;
	}

//FISC MESSAGE DATA ELEMENT #127 ADDITIONAL DATA – PRIVATE USE write by Kevin 20200812 , ##START##
//	private void hostVarF127(int size, boolean bPIsEbcdic) throws Exception {
//
//		gate.isoField[127] = getIsoVarLenStrToHostHex(3, true);
//
//		String lenData = "", fieldData = "";
//		int fieldLen = 0;
//
//		byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, offset, size);
//
//		if (bPIsEbcdic)
//			lenData = HpeUtil.ebcdic2Str(lTmpAry);
//		else
//			lenData = new String(lTmpAry, 0, lTmpAry.length);
//		fieldLen = Integer.parseInt(lenData);
//		offset += size;
//
//		fieldData = new String(gate.isoData, offset, fieldLen);
//
//		// kevin:DE127不等於emvTrans
////	gate.emvTrans = true;
//		int checkPnt = offset + fieldLen;
//		while (offset < checkPnt) {
//			if (gate.isoData[offset] == (byte) 0x40) {
//				offset++;
//				int checkTnt = offset + hostVarBinLen(2);
//				while (offset < checkTnt) {
//					if (gate.isoData[offset] == (byte) 0x01) {
//						offset++;
//						gate.f127T4001 = hostVarBinEbcdic(1);
//						logger.debug("gate.f127T4001 = " + gate.f127T4001);
//					} else if (gate.isoData[offset] == (byte) 0x02) {
//						offset++;
//						gate.f127T4002 = hostVarBinEbcdic(1);
//						logger.debug("gate.f127T4002 = " + gate.f127T4002);
//					} else {
//						offset++;
//						gate.f127None = hostVarBinEbcdic(1);
//						logger.debug("gate.f127None = " + gate.f127None);
//					}
//				}
//			} else if (gate.isoData[offset] == (byte) 0x41) {
//				offset++;
//				int checkTnt = offset + hostVarBinLen(2);
//				while (offset < checkTnt) {
//					if (gate.isoData[offset] == (byte) 0x01) {
//						offset++;
//						gate.f127T4101 = hostVarBinEbcdic(1);
//						logger.debug("gate.f127t4101 = " + gate.f127T4101);
//					} else if (gate.isoData[offset] == (byte) 0x02) {
//						offset++;
//						gate.f127T4102 = hostVarBinEbcdic(1);
//						logger.debug("gate.f127t4102 = " + gate.f127T4102);
//					} else {
//						offset++;
//						gate.f127None = hostVarBinEbcdic(1);
//						logger.debug("gate.f127tNone = " + gate.f127None);
//					}
//				}
//			} else {
//				offset++;
//				int checkTnt = offset + hostVarBinLen(2);
//				while (offset < checkTnt) {
//					{
//						offset++;
//						gate.f127None = hostVarBinEbcdic(1);
//						logger.debug("gate.f127tNone = " + gate.f127None);
//					}
//				}
//			}
//		}
//
//		return;
//	}

	private int hostVarBinLen(int size) {
		int fieldLen = 0;

		if (size == 1) {
			fieldLen = gate.isoData[offset] & 0xFF;
			offset++;
		} else {
			fieldLen = (gate.isoData[offset] & 0xFF) * 256 + (gate.isoData[offset + 1] & 0xFF);
			offset += 2;
		}

		return fieldLen;
	}

	private String hostVarBinEbcdic(int size) throws Exception {
		String fieldData = "";
		int fieldLen = 0;

		if (size == 1) {
			fieldLen = gate.isoData[offset] & 0xFF;
			offset++;
		} else {
			fieldLen = (gate.isoData[offset] & 0xFF) * 256 + (gate.isoData[offset + 1] & 0xFF);
			offset += 2;
		}

		fieldData = getIsoFixLenStrToHost(fieldLen, true);

		return fieldData;
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

	public boolean host2Iso() {
		try {

//kevin:remove by fail
//if ( !convertToInterChange() )
//   { return false; }
			convertFiscField("I");

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
			fiscFixAns(gate.mesgType, 4);
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
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 3:
						fiscFixAns(gate.isoField[k], 6);
						break;
					case 4:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 5:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 6:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 7:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 8:
						fiscFixAns(gate.isoField[k], 8);
						break;
					case 9:
						fiscFixAns(gate.isoField[k], 8);
						break;
					case 10:
						fiscFixAns(gate.isoField[k], 8);
						break;
					case 11:
						fiscFixAns(gate.isoField[k], 6);
						break;
					case 12:
						fiscFixAns(gate.isoField[k], 6);
						break;
					case 13:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 14:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 15:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 16:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 17:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 18:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 19:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 20:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 21:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 22:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 23:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 24:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 25:
						fiscFixAns(gate.isoField[k], 2);
						break;
					case 26:
						fiscFixAns(gate.isoField[k], 2);
						break;
					case 27:
						fiscFixAns(gate.isoField[k], 1);
						break;
					case 28:
						fiscFixAns(gate.isoField[k], 9);
						break;
					case 29:
						fiscFixAns(gate.isoField[k], 9);
						break;
					case 30:
						fiscFixAns(gate.isoField[k], 9);
						break;
					case 31:
						fiscFixAns(gate.isoField[k], 9);
						break;
					case 32:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 33:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 34:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 35:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 36:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 37:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 38:
						fiscFixAns(gate.isoField[k], 6);
						break;
					case 39:
						fiscFixAns(gate.isoField[k], 2);
						break;
					case 40:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 41:
						fiscFixAns(gate.isoField[k], 8);
						break;
					case 42:
						fiscFixAns(gate.isoField[k], 15);
						break;
					case 43:
						fiscFixAns(gate.isoField[k], 40);
						break;
					case 44:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 45:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 46:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 47:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 48:
						fiscVarF48();
						break;
					case 49:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 50:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 51:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 52:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					case 53:
						fiscFixAns(gate.isoField[k], 8);
						break;
					case 54:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 55:
						fiscVarF55();
						break;
					case 56:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 57:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 58:
						fiscVarF58();
						break;
					case 59:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 60:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 61:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 62:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 63:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 64:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					case 65:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					case 66:
						fiscFixAns(gate.isoField[k], 1);
						break;
					case 67:
						fiscFixAns(gate.isoField[k], 2);
						break;
					case 68:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 69:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 70:
						fiscFixAns(gate.isoField[k], 3);
						break;
					case 71:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 72:
						fiscFixAns(gate.isoField[k], 4);
						break;
					case 73:
						fiscFixAns(gate.isoField[k], 6);
						break;
					case 74:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 75:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 76:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 77:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 78:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 79:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 80:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 81:
						fiscFixAns(gate.isoField[k], 10);
						break;
					case 82:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 83:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 84:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 85:
						fiscFixAns(gate.isoField[k], 12);
						break;
					case 86:
						fiscFixAns(gate.isoField[k], 16);
						break;
					case 87:
						fiscFixAns(gate.isoField[k], 16);
						break;
					case 88:
						fiscFixAns(gate.isoField[k], 16);
						break;
					case 89:
						fiscFixAns(gate.isoField[k], 16);
						break;
					case 90:
						fiscFixAns(gate.isoField[k], 42);
						break;
					case 91:
						fiscFixAns(gate.isoField[k], 1);
						break;
					case 92:
						fiscFixAns(gate.isoField[k], 2);
						break;
					case 93:
						fiscFixAns(gate.isoField[k], 5);
						break;
					case 94:
						fiscFixAns(gate.isoField[k], 7);
						break;
					case 95:
						fiscFixAns(gate.isoField[k], 42);
						break;
					case 96:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					case 97:
						fiscFixAns(gate.isoField[k], 17);
						break;
					case 98:
						fiscFixAns(gate.isoField[k], 25);
						break;
					case 99:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 100:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 101:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 102:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 103:
						fiscVarAns(gate.isoField[k], 2);
						break;
					case 104:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 105:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 112:
						fiscFixAns(gate.isoField[k], 27);
						break;
					case 113:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 114:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 115:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 116:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 117:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 118:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 119:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 120:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 121:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 122:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 123:
						fiscVarAns(gate.isoField[k], 3);
						break;
					// V1.00.03 VISA 代碼化交易處理調整
					// V1.00.07 M/C 代碼化交易處理調整
					//case 124: fiscVarAns(gate.isoField[k],3);      break;
					case 124:
						if (gate.isTokenMTAR || gate.isTokenMTER) {
							fiscVarAns(gate.isoField[k], 3);
						}
						else {
							fiscVarF124();
						}
						break;
					case 125:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					case 126:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 127:
						fiscVarAns(gate.isoField[k], 3);
						break;
					case 128:
						fiscFixBcd(gate.isoField[k], 8);
						break;
					default:
						break;
					}
				}
			}

			gate.totalLen = offset;
			gate.dataLen = offset - gate.initPnt;

			gate.isoData[0] = (byte) (gate.dataLen / 256);
			gate.isoData[1] = (byte) (gate.dataLen % 256);

		} // end of try

		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;

	} // end of host2Iso

	public boolean convertFiscField(String cvtCode) {
		if (cvtCode.equals("C")) {
//        if ( gate.isoField[55].length() > 0 ) {
//             TokenObjectFisc token = new TokenObjectFisc();
//             token.decodeTokenData(gate);
//             if ( gate.emv9F26.length() > 0 )
//                { gate.emvTrans = true; }
//        }
			if (gate.cvv2.length() > 0) {
				gate.cvdPresent = "1";
				gate.cvdfld = gate.cvv2;
			}
			// kevin:MASTER TOKEN DATA DE48
			if (gate.f48T33.length() > 0) {
				int checkPnt = gate.f48T33.length();
				String fieldData = gate.f48T33, checkCode = "", unUseData = "";
				int subOffset = 0;
				while (subOffset < checkPnt) {
					checkCode = fieldData.substring(subOffset, subOffset + 2);
					logger.debug("hostVarF48t33-checkCode=" + checkCode);
					subOffset += 2;
					if (checkCode.equals("01")) {
						gate.acctNumInd = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.acctNumInd.length() + 2;
					} else if (checkCode.equals("02")) {
						gate.tpanTicketNo = hostVarAns(2, subOffset, fieldData);
						gate.tokenS8AcctNum = gate.tpanTicketNo;
						subOffset += gate.tpanTicketNo.length() + 2;
					} else if (checkCode.equals("03")) {
						String slExpire = hostVarAns(2, subOffset, fieldData);
						gate.tpanExpire = HpeUtil.getMonthEndDate("20" + slExpire.substring(0, 2),
								slExpire.substring(2, 4));
						subOffset += slExpire.length() + 2;
					} else if (checkCode.equals("04")) {
						gate.productCode = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.productCode.length() + 2;
					} else if (checkCode.equals("05")) {
						gate.tokenAssuranceLevel = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.tokenAssuranceLevel.length() + 2;
					} else if (checkCode.equals("06")) {
						gate.tokenRequestId = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.tokenRequestId.length() + 2;
					} else if (checkCode.equals("07")) {
						gate.accountRange = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.accountRange.length() + 2;
					} else if (checkCode.equals("08")) {
						gate.storageTech = hostVarAns(2, subOffset, fieldData);
						subOffset += gate.storageTech.length() + 2;
					} else {
						unUseData = hostVarAns(2, subOffset, fieldData);
						logger.debug("hostVarF48t33-unUseData=" + unUseData + "len=" + subOffset);
						subOffset += unUseData.length() + 2;
						logger.debug("hostVarF48t33-len=" + subOffset);
					}
				}
			}
			if (gate.f48T42.length() > 0) {
				int checkPnt = gate.f48T42.length();
				String fieldData = gate.f48T42, checkCode = "", unUseData = "";
				int subOffset = 0;
				while (subOffset < checkPnt) {
					checkCode = fieldData.substring(subOffset, subOffset + 2);
					logger.debug("hostVarf48T42-checkCode=" + checkCode);
					subOffset += 2;
					if (checkCode.equals("01")) {
						gate.f48T42Eci = hostVarAns(2, subOffset, fieldData);
						logger.debug("hostVarf48T42Eci=" + gate.f48T42Eci + "len=" + subOffset);
						subOffset += gate.f48T42Eci.length() + 2;
					} 
					else if (checkCode.equals("00")) {
						gate.f48T42Eci = fieldData.substring(4);
						logger.debug("hostVarf48T42Eci=" + gate.f48T42Eci + "len=" + subOffset);
						subOffset += gate.f48T42Eci.length() + 4;
					}
					else {
						unUseData = hostVarAns(2, subOffset, fieldData);
						logger.debug("hostVarf48T42-unUseData=" + unUseData + "len=" + subOffset);
						subOffset += unUseData.length() + 2;
						logger.debug("hostVarf48T42-len=" + subOffset);
					}
				}
			}
			if (gate.f48T66.length() > 0) {
				int checkPnt = gate.f48T66.length();
				String fieldData = gate.f48T66, checkCode = "", unUseData = "";
				int subOffset = 0;
				while (subOffset < checkPnt) {
					checkCode = fieldData.substring(subOffset, subOffset + 2);
					logger.debug("hostVarf48T66-checkCode=" + checkCode);
					subOffset += 2;
					if (checkCode.equals("01")) {
						gate.version3Ds = hostVarAns(2, subOffset, fieldData);
						logger.debug("hostVarf48T66-version3Ds=" + gate.version3Ds + "len=" + subOffset);
						subOffset += gate.version3Ds.length() + 2;
					} else {
						unUseData = hostVarAns(2, subOffset, fieldData);
						logger.debug("hostVarf48T66-unUseData=" + unUseData + "len=" + subOffset);
						subOffset += unUseData.length() + 2;
						logger.debug("hostVarf48T66-len=" + subOffset);
					}
				}
			}
			// kevin:MASTER TOKEN DATA DE124
			if (gate.f124TA.length() > 0) { // MTAR
				gate.tokenMesgType = gate.f124TA.substring(0, 2);
				gate.correlationId = gate.f124TA.substring(2, 16);
				gate.accountSource = gate.f124TA.substring(16, 17);
				gate.acctInstanceId = gate.f124TA.substring(17, 65);
				gate.deviceIp = gate.f124TA.substring(65, 77);
				gate.walletIdHash = gate.f124TA.substring(77, 141);
				gate.cardholderName = gate.f124TA.substring(141, 168);
				gate.recommendation = gate.f124TA.substring(168, 169);
				gate.recommendVerison = gate.f124TA.substring(169, 171);
				gate.deviceScore = gate.f124TA.substring(171, 172);
				gate.accountScore = gate.f124TA.substring(172, 173);
				gate.numOfActiveToken = gate.f124TA.substring(173, 175);
				gate.walletReasonCode = gate.f124TA.substring(175, 181);
				gate.deviceLocation = gate.f124TA.substring(181, 190);
				gate.numLast4Digits = gate.f124TA.substring(190, 194);
				gate.tokenType = gate.f124TA.substring(194, 195);
			}
			if (gate.f124TE.length() > 0) { // MTCN
				gate.tokenMesgType = gate.f124TE.substring(0, 2);
				gate.correlationId = gate.f124TE.substring(2, 16);
				gate.accountSource = gate.f124TE.substring(16, 17);
				gate.acctInstanceId = gate.f124TE.substring(17, 65);
				gate.numOfActiveToken = gate.f124TE.substring(65, 67);
				gate.walletIdHash = gate.f124TE.substring(67, 131);
				gate.cardholderName = gate.f124TE.substring(131, 158);
				gate.tokenType = gate.f124TE.substring(158, 159);
			}
		} else {
//	     V1.00.02 - 物件與TokenObject一樣，故取消TokenObjectFisc
//   	 TokenObjectFisc token = new TokenObjectFisc();
			if (!gate.connType.equals("FISC")) {
				TokenObject token = new TokenObject();
				gate.isoField[55] = token.createTokenData(gate);
			}

		}

		return true;
	}

	private void setByteMap() {
		int i = 0, k = 0;
		char map[] = new char[129];
		String tmpStr = "";

		for (i = 0; i <= 128; i++) {
			map[i] = '0';
		}

//	if  ( gate.tccCode.length() > 0 || gate.keyExchangeBlock.length() > 0 || gate.ucafInd.length() > 0 ||
//	      gate.eci.length() > 0 || gate.ucaf.length() > 0 || gate.xid.length() > 0 || gate.cvv2.length() > 0 )
		// V1.00.03 VISA 代碼化交易處理調整
		if (gate.tccCode.length() > 0 || gate.keyExchangeBlock.length() > 0 || gate.cavvResult.length() > 0
				|| gate.cvv2Result.length() > 0 || gate.f48T74.length() > 0) {
			gate.isoField[48] = "A";
		}

		if (gate.emvTrans) {
			//HCE交易時，ARQC驗證由TWMP，故不用回覆DE55
			if (gate.f58T32.length() > 0 ) {
				gate.isoField[55] = "";
			}
			else {
				gate.isoField[55] = "A";
			}
		}

		if (gate.isInstallmentTx) {
			gate.isoField[112] = gate.divMark + gate.divNum + gate.firstAmt + gate.everyAmt + gate.procAmt
					+ gate.isoField[39];
			logger.debug("分期回覆資訊=" + gate.isoField[112]);
		}
		//V1.00.04  VISA 代碼化交易處理調整2 (國際掛卡及悠遊卡連線拒授權名單維護才需要帶入isoField[92])
		if (gate.isoField[92].length() > 0 && !"FHM".equals(gate.connType)) {
			logger.debug("系統回覆碼=" + gate.isoField[92]);
			gate.isoField[92] = "";
		}
		if (gate.isoField[126].length() > 0) {
			logger.debug("isoField[48]借用[126]=" + gate.isoField[126]);
			gate.isoField[126] = "";
		}

		for (k = 2; k < 128; k++) {
			if (gate.isoField[k].length() > 0) {
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

	private void fiscFixAns(String fieldData, int len) throws UnsupportedEncodingException {
		int i = 0;
		if (fieldData.length() < len) {
			fieldData = fieldData + spaces.substring(0, len - fieldData.length());
		} else if (fieldData.length() > len) {
			fieldData = fieldData.substring(0, len);
		}

		byte[] tmp = fieldData.getBytes("Cp1047");
		for (i = 0; i < len; i++) {
			gate.isoData[offset] = tmp[i];
			offset++;
		}

		return;
	}

	private void fiscVarAns(String fieldData, int size) throws UnsupportedEncodingException {
		String tmpStr = "";
		byte[] tmpByte;
		int i = 0;

		tmpStr = String.valueOf(fieldData.length());
		if ((size - tmpStr.length()) == 1) {
			tmpStr = "0" + tmpStr;
		} else if ((size - tmpStr.length()) == 2) {
			tmpStr = "00" + tmpStr;
		}

		tmpByte = tmpStr.getBytes("Cp1047");
		for (i = 0; i < size; i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		tmpByte = fieldData.getBytes("Cp1047");
		for (i = 0; i < fieldData.length(); i++) {
			gate.isoData[offset] = tmpByte[i];
			offset++;
		}

		return;
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
			gate.isoData[offset] = (byte) (left * 16 + right);
			offset++;
		}
		return;
	}

	private void fiscVarF48() throws UnsupportedEncodingException {
		int len = 0, mod = 0;
		String tmpStr = "";
		byte[] tmpByte;
		int checkPnt = 0;

		checkPnt = offset;
		offset += 3;
		gate.isoField[48] = "";

		if (gate.tccCode.length() == 1) {
			fiscFixAns(gate.tccCode, 1);
			gate.isoField[48] = gate.isoField[48] + gate.tccCode;
		}

		// if ( gate.keyExchangeBlock.length() > 0 )
		// {
		// fiscFixAns("11",2);
		// fiscVarAns(gate.keyExchangeBlock,2);
		// }
		//
		// if ( gate.ucafInd.length() == 1 )
		// {
		// fiscFixAns("42",2);
		// fiscVarAns(gate.ucafInd,2);
		// }
		//
		// if ( gate.ucaf.length() > 0 )
		// {
		// fiscFixAns("43",2);
		// fiscVarAns(gate.ucaf,2);
		// }
		//
		// if ( gate.xid.length() > 0 )
		// {
		// fiscFixAns("44",2);
		// fiscFixBcd(gate.xid,(gate.xid.length()/2));
		// }
		// MASTERCARD 通過放2
//	if ( gate.ucafInd.length() > 0  )
//		{
//		fiscFixAns("45",2);
//		if (!gate.isoField[39].equals("00")) {
//			fiscVarAns("1",2);
//			gate.isoField[48] = gate.isoField[48] + "4501" + "1";
//		}
//		else {
//			fiscVarAns("2",2);
//			gate.isoField[48] = gate.isoField[48] + "4501" + "2";
//		}
//	}
		// VISA、JCB 通過放2
//	else if (gate.eci.length() > 0)
//		{
//		fiscFixAns("45",2);
//		if (!gate.isoField[39].equals("00")) {
//			fiscVarAns("1",2);
//			gate.isoField[48] = gate.isoField[48] + "4501" + "1";
//		}
//		else {
//			fiscVarAns("2",2);
//			gate.isoField[48] = gate.isoField[48] + "4501" + "2";
//		}
//	}
//	if ((gate.ucafInd.length() > 0)  || (gate.eci.length() > 0)) {
//	if ((gate.ucafInd.length() > 0)  || ("5".equals(gate.eci))) {
		// V1.00.03 VISA 代碼化交易處理調整
		if (gate.cavvResult.length() > 0) {
			fiscFixAns("45", 2);
			fiscVarAns(gate.cavvResult, 2);
			gate.isoField[48] = gate.isoField[48] + "4501" + gate.cavvResult;
		}

		// 晶片驗證結果
		if (gate.f48T74.length() > 0) {
			fiscFixAns("74", 2);
			fiscVarAns(gate.f48T74, 2);
			gate.isoField[48] = gate.isoField[48] + "74" + String.format("%02d", (int) (gate.f48T74.length()))
					+ gate.f48T74;

		}
		// CVC2驗證結果
		if (gate.cvv2.length() > 0) {
			fiscFixAns("87", 2);
			fiscVarAns(gate.cvv2Result, 2);
			gate.isoField[48] = gate.isoField[48] + "8701" + gate.cvv2Result;
		}
		logger.debug("FiscFormat gate.isoField[48] =" + gate.isoField[48]);

		// if ( gate.ucafInd.length() == 1 )
		// {
		// fiscFixAns("92",2);
		// fiscVarAns(gate.cvv2,2);
		// }

		tmpStr = (offset - checkPnt - 3) + "";
		if (tmpStr.length() == 1) {
			tmpStr = "00" + tmpStr;
		} else if (tmpStr.length() == 2) {
			tmpStr = "0" + tmpStr;
		}
		tmpByte = tmpStr.getBytes("Cp1047");
		for (int i = 0; i < 3; i++) {
			gate.isoData[checkPnt + i] = tmpByte[i];
		}

		return;
	}

	private void fiscVarF55() throws UnsupportedEncodingException {

		if (gate.isoField[55].length() == 0) {
			return;
		}
		int len = 0, mod = 0;
		String tmpStr = "";
		byte[] tmpByte;
		int checkPnt = 0;

		checkPnt = offset;
		offset += 3;
		gate.isoField[55] = "";

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
			gate.isoField[55] = gate.isoField[55] + "8A" + len2Hex(gate.emv8A.length() / 2) + gate.emv8A;
		}

		if (gate.emv91.length() > 0) {
			fiscTag55("91", gate.emv91);
			gate.isoField[55] = gate.isoField[55] + "91" + len2Hex(gate.emv91.length() / 2) + gate.emv91;
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
		if (gate.emv9F26.length() > 0) {
			fiscTag55("9F26", gate.emv9F26);
			gate.isoField[55] = gate.isoField[55] + "9F26" + len2Hex(gate.emv9F26.length() / 2) + gate.emv9F26;
		}

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

		tmpStr = (offset - checkPnt - 3) + "";
		if (tmpStr.length() == 1) {
			tmpStr = "00" + tmpStr;
		} else if (tmpStr.length() == 2) {
			tmpStr = "0" + tmpStr;
		}
		gate.isoField[55] = tmpStr + gate.isoField[55];
		tmpByte = tmpStr.getBytes("Cp1047");
		for (int i = 0; i < 3; i++) {
			gate.isoData[checkPnt + i] = tmpByte[i];
		}

		return;
	}

	private void fiscTag55(String tagData, String emvData) {
		int len = tagData.length() / 2;
		fiscFixBcd(tagData, len);

//	if ( (emvData.length() % 2) != 0 )
//	   { emvData = "0" + emvData;  }
		len = emvData.length() / 2;
//	gate.isoData[offset] = (byte)len;
//	offset++;
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

	private void fiscVarF58() throws UnsupportedEncodingException {
		logger.debug("isoField[58]="+gate.isoField[58]);
		if (gate.isoField[58].length() == 0) {
			return;
		}
		int len = 0, mod = 0;
		String tmpStr = "";
		byte[] tmpByte;
		int checkPnt = 0;

		checkPnt = offset;
		offset += 3;
		gate.isoField[58] = "";

		if (gate.f58T21.length() > 0) {
			fiscFixAns("21", 2);
			fiscVarAns(gate.f58T21, 2);
			gate.isoField[58] = gate.isoField[58] + "2142" + gate.f58T21;
		}
		if (gate.f58T32.length() > 0) {
			fiscFixAns("32", 2);
			fiscVarAns(gate.f58T32, 2);
			gate.isoField[58] = gate.isoField[58] + "3256" + gate.f58T32;
		}
		if (gate.f58T70.length() > 0) {
			fiscFixAns("70", 2);
			fiscVarAns(gate.f58T70, 2);
			gate.isoField[58] = gate.isoField[58] + "7004" + gate.f58T70;
		}
		if (gate.f58T82.length() > 0) {
			fiscFixAns("82", 2);
			fiscVarAns(gate.f58T82, 2);
			gate.isoField[58] = gate.isoField[58] + "8248" + gate.f58T82;
		}
		if (gate.f58T84.length() > 0) {
			fiscFixAns("84", 2);
			fiscVarAns(gate.f58T84, 2);
			gate.isoField[58] = gate.isoField[58] + "84" + String.format("%02d", (int) (gate.f58T84.length()))
					+ gate.f58T84;
		}
		logger.debug("FiscFormat gate.isoField[58] =" + gate.isoField[58]);

		tmpStr = (offset - checkPnt - 3) + "";
		if (tmpStr.length() == 1) {
			tmpStr = "00" + tmpStr;
		} else if (tmpStr.length() == 2) {
			tmpStr = "0" + tmpStr;
		}
		tmpByte = tmpStr.getBytes("Cp1047");
		for (int i = 0; i < 3; i++) {
			gate.isoData[checkPnt + i] = tmpByte[i];
		}

		return;
	}

//V1.00.03 VISA 代碼化交易處理調整
	private void fiscVarF124() throws UnsupportedEncodingException {
		if (gate.isoField[124].length() == 0) {
			return;
		}
		int len = 0;
		String tmpStr = "";
		byte[] tmpByte;
		int checkPnt = 0;

		checkPnt = offset;
		offset += 3;
		len = gate.isoField[124].length() / 2;
		convertBcd(gate.isoField[124], len);

		tmpStr = (offset - checkPnt - 3) + "";
		if (tmpStr.length() == 1) {
			tmpStr = "00" + tmpStr;
		} else if (tmpStr.length() == 2) {
			tmpStr = "0" + tmpStr;
		}
		gate.isoField[124] = tmpStr + gate.isoField[124];
		tmpByte = tmpStr.getBytes("Cp1047");
		for (int i = 0; i < 3; i++) {
			gate.isoData[checkPnt + i] = tmpByte[i];
		}

		return;
	}

	public void expHandle(Exception ex) {
		logger.fatal(" >> ####### MastFormat Exception MESSAGE STARTED ######");
		logger.fatal("MastFormat Exception_Message : ", ex);
		logger.fatal(" >> ####### Mast system Exception MESSAGE   ENDED ######");
		return;
	}

} // Class MastFormat End
