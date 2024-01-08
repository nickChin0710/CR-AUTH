/**
 * 授權使用FMH ISO8583格式轉換物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用FMH ISO8583格式轉換物件               *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

import java.util.HashMap;

//import org.apache.log4j.*;
import org.apache.logging.log4j.Logger;

import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class FhmFormat extends ConvertMessage implements FormatInterChange {

	public String byteMap = "", isoString = "", retCode = "";
	public String zeros = "", spaces = "", fiid = "", dpcNum = "", lNet = "";
	private int offset = 0, k = 0;

//  public FhmFormat(Logger logger,AuthGate gate,HashMap cvtHash) {
	public FhmFormat(Logger logger, AuthTxnGate gate) {
		super.logger = logger;
		super.gate = gate;
//      super.cvtHash = cvtHash;
	}

	/* �N FISC FHM �榡�ର�D���榡��� */
	public boolean iso2Host() {
		try {

			String cvtString = "";
			int cnt = 0;

			logger.debug("FhmFormat receive 0302 HEX:" + HpeUtil.getByteHex(gate.isoData));


			isoString = new String(gate.isoData, 0, gate.dataLen);

			offset = 0;
			gate.isoString = isoString;

//        gate.fhmHead = isoString.substring(0, 12);
//        offset  = 12;

			gate.mesgType = isoString.substring(offset, offset + 4);
			offset += 4;

			cvtString = isoString.substring(offset, offset + 16);
			byteMap = byte2ByteMap(cvtString, 16);
			gate.isoBitMap = byteMap;
			offset += 16;

			if (byteMap.charAt(0) == '1') {
				cvtString = isoString.substring(offset, offset + 16);
				byteMap = byteMap + byte2ByteMap(cvtString, 16);
				offset += 16;
				cnt = 128;
			} else {
				cnt = 64;
			}

			for (k = 2; k <= cnt; k++) {
				if (byteMap.charAt(k - 1) == '1') {
					switch (k) {
					case 2:
						gate.isoField[k] = hostVariable(2);
						break;
					case 3:
						gate.isoField[k] = hostFixField(6);
						break;
					case 4:
						gate.isoField[k] = hostFixField(12);
						break;
					case 5:
						gate.isoField[k] = hostFixField(12);
						break;
					case 6:
						gate.isoField[k] = hostFixField(12);
						break;
					case 7:
						gate.isoField[k] = hostFixField(10);
						break;
					case 8:
						gate.isoField[k] = hostFixField(8);
						break;
					case 9:
						gate.isoField[k] = hostFixField(8);
						break;
					case 10:
						gate.isoField[k] = hostFixField(8);
						break;
					case 11:
						gate.isoField[k] = hostFixField(6);
						break;
					case 12:
						gate.isoField[k] = hostFixField(6);
						break;
					case 13:
						gate.isoField[k] = hostFixField(4);
						break;
					case 14:
						gate.isoField[k] = hostFixField(4);
						break;
					case 15:
						gate.isoField[k] = hostFixField(4);
						break;
					case 16:
						gate.isoField[k] = hostFixField(4);
						break;
					case 17:
						gate.isoField[k] = hostFixField(4);
						break;
					case 18:
						gate.isoField[k] = hostFixField(4);
						break;
					case 19:
						gate.isoField[k] = hostFixField(3);
						break;
					case 20:
						gate.isoField[k] = hostFixField(3);
						break;
					case 21:
						gate.isoField[k] = hostFixField(3);
						break;
					case 22:
						gate.isoField[k] = hostFixField(3);
						break;
					case 23:
						gate.isoField[k] = hostFixField(3);
						break;
					case 24:
						gate.isoField[k] = hostFixField(3);
						break;
					case 25:
						gate.isoField[k] = hostFixField(2);
						break;
					case 26:
						gate.isoField[k] = hostFixField(2);
						break;
					case 27:
						gate.isoField[k] = hostFixField(1);
						break;
					case 28:
						gate.isoField[k] = hostFixField(9);
						break;
					case 29:
						gate.isoField[k] = hostFixField(9);
						break;
					case 30:
						gate.isoField[k] = hostFixField(9);
						break;
					case 31:
						gate.isoField[k] = hostFixField(9);
						break;
					case 32:
						gate.isoField[k] = hostVariable(2);
						break;
					case 33:
						gate.isoField[k] = hostVariable(2);
						break;
					case 34:
						gate.isoField[k] = hostVariable(2);
						break;
					case 35:
						gate.isoField[k] = hostVariable(2);
						break;
					case 36:
						gate.isoField[k] = hostVariable(3);
						break;
					case 37:
						gate.isoField[k] = hostFixField(12);
						break;
					case 38:
						gate.isoField[k] = hostFixField(6);
						break;
					case 39:
						gate.isoField[k] = hostFixField(2);
						retCode = gate.isoField[k];
						break;
					case 40:
						gate.isoField[k] = hostFixField(3);
						break;
					case 41:
						gate.isoField[k] = hostFixField(8);
						break;
					case 42:
						gate.isoField[k] = hostFixField(15);
						break;
					case 43:
						gate.isoField[k] = hostFixField(40);
						break;
					case 44:
						gate.isoField[k] = hostVariable(2);
						break;
					case 45:
						gate.isoField[k] = hostVariable(2);
						break;
					case 46:
						gate.isoField[k] = hostVariable(3);
						break;
					case 47:
						gate.isoField[k] = hostVariable(3);
						break;
					case 48:
						gate.isoField[k] = hostVariable(3);
						break;
					case 49:
						gate.isoField[k] = hostFixField(3);
						break;
					case 50:
						gate.isoField[k] = hostFixField(3);
						break;
					case 51:
						gate.isoField[k] = hostFixField(3);
						break;
					case 52:
						gate.isoField[k] = hostFixField(16);
						break;
					case 53:
						gate.isoField[k] = hostFixField(8);
						break;
					case 54:
						gate.isoField[k] = hostVariable(3);
						break;
					case 55:
						gate.isoField[k] = hostVariable(3);
						break;
					case 56:
						gate.isoField[k] = hostVariable(3);
						break;
					case 57:
						gate.isoField[k] = hostVariable(3);
						break;
					case 58:
//						gate.isoField[k] = hostVariable(3);
						hostVarF58(3, false);
						break;
					case 59:
						gate.isoField[k] = hostVariable(3);
//						hostVarF58(3, false);
						break;
					case 60:
						gate.isoField[k] = hostVariable(3);
						break;
					case 61:
						gate.isoField[k] = hostVariable(3);
						break;
					case 62:
						gate.isoField[k] = hostVariable(3);
						break;
					case 63:
						gate.isoField[k] = hostVariable(3);
						break;
					case 64:
						gate.isoField[k] = hostFixField(16);
						break;
					case 65:
						gate.isoField[k] = hostFixField(16);
						break;
					case 66:
						gate.isoField[k] = hostFixField(1);
						break;
					case 67:
						gate.isoField[k] = hostFixField(2);
						break;
					case 68:
						gate.isoField[k] = hostFixField(3);
						break;
					case 69:
						gate.isoField[k] = hostFixField(3);
						break;
					case 70:
						gate.isoField[k] = hostFixField(3);
						break;
					case 71:
						gate.isoField[k] = hostFixField(4);
						break;
					case 72:
						gate.isoField[k] = hostFixField(4);
						break;
					case 73:
						gate.isoField[k] = hostFixField(6);
						break;
					case 74:
						gate.isoField[k] = hostFixField(10);
						break;
					case 75:
						gate.isoField[k] = hostFixField(10);
						break;
					case 76:
						gate.isoField[k] = hostFixField(10);
						break;
					case 77:
						gate.isoField[k] = hostFixField(10);
						break;
					case 78:
						gate.isoField[k] = hostFixField(10);
						break;
					case 79:
						gate.isoField[k] = hostFixField(10);
						break;
					case 80:
						gate.isoField[k] = hostFixField(10);
						break;
					case 81:
						gate.isoField[k] = hostFixField(10);
						break;
					case 82:
						gate.isoField[k] = hostFixField(12);
						break;
					case 83:
						gate.isoField[k] = hostFixField(12);
						break;
					case 84:
						gate.isoField[k] = hostFixField(12);
						break;
					case 85:
						gate.isoField[k] = hostFixField(12);
						break;
					case 86:
						gate.isoField[k] = hostFixField(16);
						break;
					case 87:
						gate.isoField[k] = hostFixField(16);
						break;
					case 88:
						gate.isoField[k] = hostFixField(16);
						break;
					case 89:
						gate.isoField[k] = hostFixField(16);
						break;
					case 90:
						gate.isoField[k] = hostFixField(42);
						break;
					case 91:
						gate.isoField[k] = hostFixField(1);
						break;
					case 92:
						gate.isoField[k] = hostFixField(2);
						break;
					case 93:
						gate.isoField[k] = hostFixField(5);
						break;
					case 94:
						gate.isoField[k] = hostFixField(7);
						break;
					case 95:
						gate.isoField[k] = hostFixField(42);
						break;
					case 96:
						gate.isoField[k] = hostFixField(16);
						break;
					case 97:
						gate.isoField[k] = hostFixField(17);
						break;
					case 98:
						gate.isoField[k] = hostFixField(25);
						break;
					case 99:
						gate.isoField[k] = hostVariable(2);
						break;
					case 100:
						gate.isoField[k] = hostVariable(2);
						break;
					case 101:
						gate.isoField[k] = hostVariable(2);
						break;
					case 102:
						gate.isoField[k] = hostVariable(2);
						break;
					case 103:
						gate.isoField[k] = hostVariable(2);
						break;
					case 104:
						gate.isoField[k] = hostVariable(3);
						break;
					case 105:
						gate.isoField[k] = hostVariable(3);
						break;
					case 106:
						gate.isoField[k] = hostVariable(3);
						break;
					case 107:
						gate.isoField[k] = hostVariable(3);
						break;
					case 108:
						gate.isoField[k] = hostVariable(3);
						break;
					case 109:
						gate.isoField[k] = hostVariable(3);
						break;
					case 110:
						gate.isoField[k] = hostVariable(3);
						break;
					case 111:
						gate.isoField[k] = hostVariable(3);
						break;
					case 113:
						gate.isoField[k] = hostVariable(3);
						break;
					case 114:
						gate.isoField[k] = hostVariable(3);
						break;
					case 115:
						gate.isoField[k] = hostVariable(3);
						break;
					case 116:
						gate.isoField[k] = hostVariable(3);
						break;
					case 117:
						gate.isoField[k] = hostVariable(3);
						break;
					case 118:
						gate.isoField[k] = hostVariable(3);
						break;
					case 119:
						gate.isoField[k] = hostVariable(3);
						break;
					case 120:
						gate.isoField[k] = hostVariable(3);
						break;
					case 121:
						gate.isoField[k] = hostVariable(3);
						break;
					case 122:
						gate.isoField[k] = hostVariable(3);
						break;
					case 123:
						gate.isoField[k] = hostVariable(3);
						break;
					case 124:
						gate.isoField[k] = hostVariable(3);
						break;
					case 125:
						gate.isoField[k] = hostFixField(16);
						break;
					case 126:
						gate.isoField[k] = hostVariable(3);
						break;
					case 127:
						gate.isoField[k] = hostVariable(3);
						break;
					case 128:
						gate.isoField[k] = hostFixField(16);
						break;
					default:
						break;
					}
				}
			}

			/* FHM �榡 �ഫ�� �@�P�榡 */
			/*
			 * Jack : �Τ����o... if ( !convertToCommon() ) { return false; }
			 */
//          convertFhmField("C"); //mark by Howard

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

	// FISC MESSAGE DATA ELEMENT #58 ADDITIONAL DATA – PRIVATE USE write by Kevin
	// 20200304 , ##START##
	private void hostVarF58(int size, boolean bPIsEbcdic) throws Exception {
		try {
			gate.isoField[58] = getIsoVarLenStrToHostTest(3, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

//		if (bPIsEbcdic)
//			fieldData = HpeUtil.ebcdic2Str(lTmpAry);
//		else
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
				logger.debug("FhmFormat gate.f58T70="+gate.f58T70);
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

	// kevin:針對fisc客製化取欄位
	private String hostVarAns(int size, int subOffset, String fieldData) {
		String subFieldData = "";
		int fieldLen = 0;
		logger.debug("hostVarAns-size=" + size);
		logger.debug("hostVarAns-subOffset=" + subOffset);
		logger.debug("hostVarAns-fieldData=" + fieldData);
		fieldLen = Integer.parseInt(fieldData.substring(subOffset, subOffset + size));
		logger.debug("hostVarAns-fieldLen=" + fieldLen);
		subOffset += size;
		subFieldData = fieldData.substring(subOffset, subOffset + fieldLen);

		subOffset += fieldLen;
		return subFieldData;
	}
	//kevin:取iso變動長欄位並轉碼
		private String getIsoVarLenStrToHostTest(int len, boolean bPIsEbcdic) throws Exception {
			String lenData = "", fieldData = "";
			int fieldLen = 0, subOffset = 0;
			subOffset = offset;

			byte[] lTmpAry = HpeUtil.getSubByteAry(gate.isoData, subOffset, len);

//			if (bPIsEbcdic)
//				lenData = HpeUtil.ebcdic2Str(lTmpAry);
//			else
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


	/* �N�D���榡����ର FISC FHM �榡 */
	public boolean host2Iso() {

		try {
			/*
			 * Jack : �Τ����o... if ( !convertToInterChange() ) { return false; }
			 */
//        convertFhmField("I");

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
			isoString = "";
//        if (gate.isoField[101].equals("CF"))      formatOutCAF();
//        else if (gate.isoField[101].equals("DA")) formatOutPBF();
//        else if (gate.isoField[101].equals("NF")) formatOutNEG();
//        else if (gate.isoField[101].equals("VP")) formatOutVISA();

			setHeaderMap();
			isoString = isoString + gate.mesgType;
			offset += 4;

			cvtString = byteMap.substring(0, 64);
			isoString = isoString + byteMap2Byte(cvtString, 16);
			offset += 16;

			if (byteMap.charAt(0) == '1') {
				cvtString = byteMap.substring(64, 128);
				isoString = isoString + byteMap2Byte(cvtString, 16);
				offset += 16;
				cnt = 128;
			} else {
				cnt = 64;
			}
			for (k = 2; k <= cnt; k++) {
				if (byteMap.charAt(k - 1) == '1') {
					switch (k) {
					case 2:
						b24Variable(gate.isoField[k], 2);
						break;
					case 3:
						b24FixField(gate.isoField[k], 6);
						break;
					case 4:
						b24FixField(gate.isoField[k], 12);
						break;
					case 5:
						b24FixField(gate.isoField[k], 12);
						break;
					case 6:
						b24FixField(gate.isoField[k], 12);
						break;
					case 7:
						b24FixField(gate.isoField[k], 10);
						break;
					case 8:
						b24FixField(gate.isoField[k], 8);
						break;
					case 9:
						b24FixField(gate.isoField[k], 8);
						break;
					case 10:
						b24FixField(gate.isoField[k], 8);
						break;
					case 11:
						b24FixField(gate.isoField[k], 6);
						break;
					case 12:
						b24FixField(gate.isoField[k], 6);
						break;
					case 13:
						b24FixField(gate.isoField[k], 4);
						break;
					case 14:
						b24FixField(gate.isoField[k], 4);
						break;
					case 15:
						b24FixField(gate.isoField[k], 4);
						break;
					case 16:
						b24FixField(gate.isoField[k], 4);
						break;
					case 17:
						b24FixField(gate.isoField[k], 4);
						break;
					case 18:
						b24FixField(gate.isoField[k], 4);
						break;
					case 19:
						b24FixField(gate.isoField[k], 3);
						break;
					case 20:
						b24FixField(gate.isoField[k], 3);
						break;
					case 21:
						b24FixField(gate.isoField[k], 3);
						break;
					case 22:
						b24FixField(gate.isoField[k], 3);
						break;
					case 23:
						b24FixField(gate.isoField[k], 3);
						break;
					case 24:
						b24FixField(gate.isoField[k], 3);
						break;
					case 25:
						b24FixField(gate.isoField[k], 2);
						break;
					case 26:
						b24FixField(gate.isoField[k], 2);
						break;
					case 27:
						b24FixField(gate.isoField[k], 1);
						break;
					case 28:
						b24FixField(gate.isoField[k], 8);
						break;
					case 29:
						b24FixField(gate.isoField[k], 8);
						break;
					case 30:
						b24FixField(gate.isoField[k], 8);
						break;
					case 31:
						b24FixField(gate.isoField[k], 8);
						break;
					case 32:
						b24Variable(gate.isoField[k], 2);
						break;
					case 33:
						b24Variable(gate.isoField[k], 2);
						break;
					case 34:
						b24Variable(gate.isoField[k], 2);
						break;
					case 35:
						b24Variable(gate.isoField[k], 2);
						break;
					case 36:
						b24Variable(gate.isoField[k], 3);
						break;
					case 37:
						b24FixField(gate.isoField[k], 12);
						break;
					case 38:
						b24FixField(gate.isoField[k], 6);
						break;
					case 39:
						b24FixField(gate.isoField[k], 2);
						break;
					case 40:
						b24FixField(gate.isoField[k], 3);
						break;
					case 41:
						b24FixField(gate.isoField[k], 8);
						break;
					case 42:
						b24FixField(gate.isoField[k], 15);
						break;
					case 43:
						b24FixField(gate.isoField[k], 40);
						break;
					case 44:
						b24Variable(gate.isoField[k], 2);
						break;
					case 45:
						b24Variable(gate.isoField[k], 2);
						break;
					case 46:
						b24FixField(gate.isoField[k], 3);
						break;
					case 47:
						b24FixField(gate.isoField[k], 3);
						break;
					case 48:
						b24Variable(gate.isoField[k], 3);
						break;
					case 49:
						b24FixField(gate.isoField[k], 3);
						break;
					case 50:
						b24FixField(gate.isoField[k], 3);
						break;
					case 51:
						b24FixField(gate.isoField[k], 3);
						break;
					case 52:
						b24FixField(gate.isoField[k], 16);
						break;
					case 53:
						b24FixField(gate.isoField[k], 8);
						break;
					case 54:
						b24Variable(gate.isoField[k], 3);
						break;
					case 55:
						b24Variable(gate.isoField[k], 3);
						break;
					case 56:
						b24Variable(gate.isoField[k], 3);
						break;
					case 57:
						b24Variable(gate.isoField[k], 3);
						break;
					case 58:
						b24Variable(gate.isoField[k], 3);
						break;
					case 59:
						b24Variable(gate.isoField[k], 3);
						break;
					case 60:
						b24Variable(gate.isoField[k], 3);
						break;
					case 61:
						b24Variable(gate.isoField[k], 3);
						break;
					case 62:
						b24Variable(gate.isoField[k], 3);
						break;
					case 63:
						b24Variable(gate.isoField[k], 3);
						break;
					case 64:
						b24FixField(gate.isoField[k], 16);
						break;
					case 65:
						b24FixField(gate.isoField[k], 16);
						break;
					case 66:
						b24FixField(gate.isoField[k], 1);
						break;
					case 67:
						b24FixField(gate.isoField[k], 2);
						break;
					case 68:
						b24FixField(gate.isoField[k], 3);
						break;
					case 69:
						b24FixField(gate.isoField[k], 3);
						break;
					case 70:
						b24FixField(gate.isoField[k], 3);
						break;
					case 71:
						b24FixField(gate.isoField[k], 4);
						break;
					case 72:
						b24FixField(gate.isoField[k], 4);
						break;
					case 73:
						b24FixField(gate.isoField[k], 6);
						break;
					case 74:
						b24FixField(gate.isoField[k], 10);
						break;
					case 75:
						b24FixField(gate.isoField[k], 10);
						break;
					case 76:
						b24FixField(gate.isoField[k], 10);
						break;
					case 77:
						b24FixField(gate.isoField[k], 10);
						break;
					case 78:
						b24FixField(gate.isoField[k], 10);
						break;
					case 79:
						b24FixField(gate.isoField[k], 10);
						break;
					case 80:
						b24FixField(gate.isoField[k], 10);
						break;
					case 81:
						b24FixField(gate.isoField[k], 10);
						break;
					case 82:
						b24FixField(gate.isoField[k], 12);
						break;
					case 83:
						b24FixField(gate.isoField[k], 12);
						break;
					case 84:
						b24FixField(gate.isoField[k], 12);
						break;
					case 85:
						b24FixField(gate.isoField[k], 12);
						break;
					case 86:
						b24FixField(gate.isoField[k], 16);
						break;
					case 87:
						b24FixField(gate.isoField[k], 16);
						break;
					case 88:
						b24FixField(gate.isoField[k], 16);
						break;
					case 89:
						b24FixField(gate.isoField[k], 16);
						break;
					case 90:
						b24FixField(gate.isoField[k], 42);
						break;
					case 91:
						b24FixField(gate.isoField[k], 1);
						break;
					case 92:
						b24FixField(gate.isoField[k], 2);
						break;
					case 93:
						b24FixField(gate.isoField[k], 5);
						break;
					case 94:
						b24FixField(gate.isoField[k], 7);
						break;
					case 95:
						b24FixField(gate.isoField[k], 42);
						break;
					case 96:
						b24FixField(gate.isoField[k], 16);
						break;
					case 97:
						b24FixField(gate.isoField[k], 17);
						break;
					case 98:
						b24FixField(gate.isoField[k], 25);
						break;
					case 99:
						b24Variable(gate.isoField[k], 2);
						break;
					case 100:
						b24Variable(gate.isoField[k], 2);
						break;
					case 101:
						b24Variable(gate.isoField[k], 2);
						break;
					case 102:
						b24Variable(gate.isoField[k], 2);
						break;
					case 103:
						b24Variable(gate.isoField[k], 2);
						break;
					case 104:
						b24Variable(gate.isoField[k], 3);
						break;
					case 105:
						b24Variable(gate.isoField[k], 3);
						break;
					case 106:
						b24Variable(gate.isoField[k], 3);
						break;
					case 107:
						b24Variable(gate.isoField[k], 3);
						break;
					case 108:
						b24Variable(gate.isoField[k], 3);
						break;
					case 109:
						b24Variable(gate.isoField[k], 3);
						break;
					case 110:
						b24Variable(gate.isoField[k], 3);
						break;
					case 111:
						b24Variable(gate.isoField[k], 3);
						break;
					case 113:
						b24Variable(gate.isoField[k], 3);
						break;
					case 114:
						b24Variable(gate.isoField[k], 3);
						break;
					case 115:
						b24Variable(gate.isoField[k], 3);
						break;
					case 116:
						b24Variable(gate.isoField[k], 3);
						break;
					case 117:
						b24Variable(gate.isoField[k], 3);
						break;
					case 118:
						b24Variable(gate.isoField[k], 3);
						break;
					case 119:
						b24Variable(gate.isoField[k], 3);
						break;
					case 120:
						b24Variable(gate.isoField[k], 3);
						break;
					case 121:
						b24Variable(gate.isoField[k], 3);
						break;
					case 122:
						b24Variable(gate.isoField[k], 3);
						break;
					case 123:
						b24Variable(gate.isoField[k], 3);
						break;
					case 124:
						b24Variable(gate.isoField[k], 3);
						break;
					case 125:
						b24FixField(gate.isoField[k], 16);
						break;
					case 126:
						b24Variable(gate.isoField[k], 3);
						break;
					case 127:
						b24Variable(gate.isoField[k], 3);
						break;
					case 128:
						b24FixField(gate.isoField[k], 16);
						break;
					default:
						break;
					}
				}
			}

			gate.isoData = isoString.getBytes(); // Howard: isoString 的前兩碼是空白

			// System.out.println("---IsoStr is=>"+ gate.isoString + "----");
			gate.totalLen = gate.isoData.length;
			gate.dataLen = gate.totalLen - gate.initPnt;
			gate.isoData[0] = (byte) (gate.dataLen / 256);
			gate.isoData[1] = (byte) (gate.dataLen % 256);

			gate.isoString = isoString;
		} // end of try
		catch (Exception ex) {
			expHandle(ex);
			return false;
		}
		return true;
	}

	private void setHeaderMap() {
		int i = 0, k = 0;
		char[] map = new char[128];
		for (i = 0; i < 128; i++) {
			map[i] = '0';
		}

//        if ( gate.fhmHead.length() != 12 )
//           { gate.fhmHead = "ISO026000000"; }

		if (gate.mesgType.length() != 4) {
			gate.mesgType = "XXXX";
		}

		isoString = spaces.substring(0, gate.initPnt);
		offset = 2;

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

	private void b24Variable(String fieldData, int len) {
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

	private void b24FixField(String fieldData, int len) {
		if (fieldData.length() < len) {
			fieldData = fieldData + spaces.substring(0, len - fieldData.length());
		}

		isoString = isoString + fieldData.substring(0, len);
		offset += len;
	}

	public void expHandle(Exception ex) {
		logger.fatal(" >> ####### FhmFormat Exception MESSAGE STARTED ######");
		logger.fatal("FhmFormat Exception_Message : ", ex);
		logger.fatal(" >> ####### FhmFormat system Exception MESSAGE   ENDED ######");
		return;
	}

} // Class FISC End
