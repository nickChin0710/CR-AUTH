/**
 * 授權處理相關工具程式
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  slErrorCode
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       授權處理相關工具程式                          *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      *  
 * 2021/03/26  V1.00.02  Kevin       排除無手機號碼或null值的資料，造成mask長度不足的問題 *
 * 2021/04/09  V1.00.03  Kevin       VISA_CAVV_U3V7檢核處理                     *
 * 2022/10/26  V1.00.04  Kevin       授權連線偵測異常時發送簡訊通知維護人員             *
 * 2022/11/11  V1.00.25  Kevin       弱掃修復：XML External Entity Injection     *
 * 2022/12/21  V1.00.30  Kevin       弱掃修復：Server Identity Verification Disabled
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/08/04  V1.00.49  Kevin       風險特店調整及新增特殊特店名稱檢查(eToro)           *
 ******************************************************************************
 */

package com.tcb.authProg.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
import java.io.PrintWriter;
//import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
//import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Timestamp;
//import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
//import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.XMLConstants;
//import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
//import javax.xml.datatype.DatatypeFactory;
//import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

//import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;


public class HpeUtil {
	
//	Logger logger = LoggerFactory.getLogger(HpeUtil.class);


	public static String removeInvalidChar(String spSrc) {
		//Howard: 將非英數字的字元 replace 為 空白
		String slResult = "", slTmp="";
		for(int n=0; n< spSrc.length(); n++  ) {
			slTmp = spSrc.substring(n,n+1);
			if (!slTmp.matches("[0-9a-zA-Z]*")) {
				slTmp=" ";
			}
			slResult = slResult + slTmp;
		}
		return slResult;
	}

	public static String getCurDateStr(boolean bpIncludeSep) {

		SimpleDateFormat lSDF = null;

		if (bpIncludeSep)
			lSDF = new SimpleDateFormat("yyyy/MM/dd");
		else
			lSDF = new SimpleDateFormat("yyyyMMdd");


		String slResult = lSDF.format(new Date());

		return slResult;
	}

//	public static void writeData2Socket(byte[] pDataAry, BufferedOutputStream pTargetOutputStream) {
//
//		try {
//			pTargetOutputStream.write(pDataAry);
//		} catch (Exception e) {
//			// TODO: handle exception
//			//System.out.println("writeData2Socket exception=>" + e.getMessage() + "---");
//
//		}
//	}

//	public static String genIsoField07() {
//		String slResult = "";
//
//		try {
//			//return MMDDHHmmss， e.g. 0803150000
//			SimpleDateFormat sdFormat=new SimpleDateFormat("MMddHHmmss");
//			Date date = new Date();
//			String strDate = sdFormat.format(date);
//			return strDate;
//
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//		return slResult;
//	}

//	public static String encodedString(String spSrc) {
//
//		String slResult ="";
//		try {
//
//			final Base64.Encoder encoder = Base64.getEncoder();
//
//			final byte[] textByte = spSrc.getBytes("UTF-8");
//			//編碼
//			slResult = encoder.encodeToString(textByte);
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//
//		return slResult;
//
//	}

//	public static String decodedString(String spEncodedStr) {
//
//		String slResult ="";
//		try {
//
//			final Base64.Decoder decoder = Base64.getDecoder();
//
//
//			//解碼
//			slResult =  new String(decoder.decode(spEncodedStr), "UTF-8");
//
//
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//
//		return slResult;
//
//	}
	
	public static String decoded2Ascii(String spEncodedStr) {

		String slResult ="";

		final Base64.Decoder decoder = Base64.getDecoder();

		//解碼
		byte[] bytes =  (decoder.decode(spEncodedStr));

		slResult =  HpeUtil.ebcdic2Str(bytes);
			

		return slResult;

	}


//	public static Timestamp convertStringToTimestamp(String spDateTime) {
//		try {
//			DateFormat formatter;
//			formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//
//			Date date = formatter.parse(spDateTime);
//			java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
//
//			return timeStampDate;
//		} 
//		catch (ParseException e) {
//			//System.out.println("Exception :" + e);
//			return null;
//		}
//	}
//
//	public static Timestamp getCurTimestamp() {
//		java.sql.Timestamp  lCurTimeStamp = new java.sql.Timestamp(new java.util.Date().getTime());
//
//		//System.out.println(L_CurTimeStamp.toString() ); //L_CurTimeStamp.toString() => 2018-01-04 09:27:43.245
//		return lCurTimeStamp;
//	}

//	public static String getNextSeqValOfDb2(Connection pConnection, String spSequenceName)  throws Exception {
//		//get sequence value
//
//		String slSeqVal = "0";
//		try {
//
//
//			String slSql = "VALUES NEXTVAL FOR  "+ spSequenceName  ;
//
//			//System.out.println("getNextSeqVal sql:" + sL_Sql + "==");
//
//			/* worked
//			java.sql.Statement Db2Stmt = P_Connection.createStatement();
//			ResultSet L_ResultSet = Db2Stmt.executeQuery(sL_Sql);
//			System.out.println("a2");
//
//			if (L_ResultSet.next()) {
//				sL_SeqVal = L_ResultSet.getString(1);
//				System.out.println("a3");
//			}
//			 */
//
//
//
//			PreparedStatement db2Stmt = pConnection.prepareStatement(slSql);	
//
//
//			ResultSet lResultSet = db2Stmt.executeQuery();
//
//
//			if (lResultSet.next()) {
//				slSeqVal = lResultSet.getString(1);
//
//			}
//
//
//			lResultSet.close();
//
//			db2Stmt.close();
//
//
//		} catch (Exception e) {
//			// TODO: handle exception
//			//System.out.println("getNextSeqVal exception:" + e.getMessage());
//			slSeqVal = "0";
//		}
//		return slSeqVal;
//
//	}

	public static String getMaskData(String spSrcData, int npKeepLength, String spMaskStr) {
		//call getMaskData("09730253334",4,"#") => return "#######3334"
		//V1.00.02-排除無手機號碼或null值的資料，造成mask長度不足的問題。
//		int nlMaskLength = spSrcData.length()-npKeepLength;
//		String slKeepData = spSrcData.substring(nlMaskLength, spSrcData.length());
//		return HpeUtil.fillCharOnLeft(slKeepData, spSrcData.length() , spMaskStr);
		if (spSrcData.length() <= npKeepLength) {
			return HpeUtil.fillCharOnLeft("", spSrcData.length() , spMaskStr);
		}
		else {
			int nlMaskLength = spSrcData.length()-npKeepLength;
			String slKeepData = spSrcData.substring(nlMaskLength, spSrcData.length());
			return HpeUtil.fillCharOnLeft(slKeepData, spSrcData.length() , spMaskStr);
		}

	}

	// ************************************************************************
	// 0:加密 1:解密
	//
	//
	// ************************************************************************
	public static String transPasswd(int type, String fromPasswd)  {
		long addNum[] = { 7, 34, 295, 4326, 76325, 875392, 2468135, 12357924, 123456789 };
		int int1, int2, datalen;
		long dataint = 1;
		String fdig[] = { "08122730435961748596", "04112633405865798792", "03162439425768718095",
				"04152236415768798390", "09182035435266718497", "01152930475463788296", "07192132465068748593",
		"02172931455660788394" };
		String tmpstr = "";
		String tmpstr1 = "";
		String toPawd = "";

		if (fromPasswd.length() < 1)
			return "";

		if (type == 0) {
			// 加密
			datalen = fromPasswd.length();
			for (int1 = 0; int1 < datalen; int1++) {
				int sbn = Integer.parseInt(fromPasswd.substring(int1, int1 + 1)) * 2 + 1;
				tmpstr += fdig[int1].substring(sbn, sbn + 1);
			}

			for (int1 = 0; int1 < datalen; int1++) {
				dataint = dataint * 10;
			}
			tmpstr1 = String.valueOf(dataint + Long.parseLong(tmpstr) - addNum[datalen - 1]);
			toPawd = tmpstr1.substring(tmpstr1.length() - datalen);

		} else {
			// 解密
			datalen = fromPasswd.length();

			tmpstr1 = String.format("%d", Long.parseLong(fromPasswd) + addNum[datalen - 1]);
			tmpstr = tmpstr1.substring(tmpstr1.length() - datalen);
			for (int1 = 0; int1 < datalen; int1++) {
				for (int2 = 0; int2 < 10; int2++) {
					int po = int2 * 2 + 1;
					if (tmpstr.substring(int1, int1 + 1).equals(fdig[int1].substring(po, po + 1))) {
						po = int2 * 2;
						toPawd += fdig[int1].substring(po, po + 1);
						break;
					}
				}
			}
		}

		return toPawd;
	}
//kevun:弱掃問題 (Often Misused: Authentication) 
//	public static String getLocalIpAddress() throws Exception{
//		String sL_Ip = "";
//		InetAddress addr = InetAddress.getLocalHost();
//		sL_Ip = addr.getHostAddress();
//		return sL_Ip;
//	}
//	public static java.sql.Date getCurDate4Sql() {
//		java.sql.Date lCurDate =  new java.sql.Date(new java.util.Date().getTime());
//		return lCurDate;
//	}

//	public static String byte2ByteMap(String src, int size) {
//		byte[] srcByte = new byte[65];
//		String[] cvt = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
//		String dest = "";
//		int i = 0, ind = 0;
//		srcByte = src.getBytes();
//
//		for (i = 0; i < size; i++) {
//			if (srcByte[i] >= '0' && srcByte[i] <= '9') {
//				ind = (int) (srcByte[i] & 0x0F);
//			} else
//				if (srcByte[i] >= 'A' && srcByte[i] <= 'F') {
//					ind = (int) (srcByte[i] & 0x0F);
//					ind += 9;
//				}
//
//			dest = dest + cvt[ind];
//		}
//		return dest;
//	}

	// combine all bytes array in List collection
//	public static byte[] convertByteAryArrayList2ByteAry(List<byte[]> byteAryList) {
//
//		if (byteAryList == null)
//			throw new NullPointerException("The array has no data");
//
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		for (byte[] item : byteAryList) {
//			try {
//				outputStream.write(item);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		return outputStream.toByteArray();
//	}
//	public static String transNullValue(String spSrc) {
//		String slResult=spSrc;
//		if (null == spSrc)
//			slResult = "";
//
//		return slResult;
//	}

//	public static String zeroPadBinChar(String binChar){
//		int len = binChar.length();
//		if(len == 8) return binChar;
//		String zeroPad = "0";
//		for(int i=1;i<8-len;i++) zeroPad = zeroPad + "0"; 
//		return zeroPad + binChar;
//	}
//	public static String convertBinaryStrToHexStr(String binary) {
//		String hex = "";
//		String hexChar;
//		int len = binary.length()/8;
//		for(int i=0;i<len;i++){
//			String binChar = binary.substring(8*i,8*i+8);
//			int convInt = Integer.parseInt(binChar,2);
//			hexChar = Integer.toHexString(convInt);
//			hexChar = fillZeroOnLeft(hexChar, 2);
//			if(i==0) 
//				hex = hexChar;
//			else 
//				hex = hex+hexChar;
//		}
//		return hex;
//	}
//	public static String convertHexStrToBinaryStr(String spSrcHexStr) {
//		String hexChar,binChar,binary;
//		binary = "";
//		int len = spSrcHexStr.length()/2;
//		for(int i=0;i<len;i++){
//			hexChar = spSrcHexStr.substring(2*i,2*i+2);
//			int convInt = Integer.parseInt(hexChar,16);
//			binChar = Integer.toBinaryString(convInt);
//			binChar = zeroPadBinChar(binChar);
//			if(i==0) binary = binChar; 
//			else binary = binary+binChar;
//			//out.printf("%s %s\n", hex_char,bin_char);
//		}
//		return binary;
//	}
	public static String fillZeroOnLeft(String spSrc, int npTargetLen) {
		String slResult=spSrc;
		for(int i=slResult.length(); i<npTargetLen; i++) {
			slResult = "0" + slResult;
		}

		return  slResult;      
	}

	public static String fillZeroOnLeft(double dpSrc, int npTargetLen) {
		int nlSrc = (int)dpSrc;
		
		return  String.format("%0" + npTargetLen + "d", nlSrc);      
	}

	public static String fillCharOnRight(String spSrc, int npTargetLen, String slTarcharChar) {
		int strLen = spSrc.length();
		if (strLen < npTargetLen) {
			while (strLen < npTargetLen) {
				StringBuffer sb = new StringBuffer();
				sb.append(spSrc).append(slTarcharChar);
				spSrc = sb.toString();
				strLen = spSrc.length();
			}
		}

		return spSrc;       


	}

//	public static String getTransKeyValue() {
//
//		try {
//			return fillCharOnLeft(getCurDateTimeStr(true, false), 20, "0");
//		} catch (DatatypeConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
	public static String fillCharOnLeft(String spSrc, int npTargetLen, String slTarcharChar) {
		int strLen = spSrc.length();
		if (strLen < npTargetLen) {
			while (strLen < npTargetLen) {
				StringBuffer sb = new StringBuffer();
				sb.append(slTarcharChar).append(spSrc);
				spSrc = sb.toString();
				strLen = spSrc.length();
			}
		}

		return spSrc;       

	}

//	public static String bcd2Str(byte[] bytes) {  
//		StringBuffer temp = new StringBuffer(bytes.length * 2);  
//		for (int i = 0; i < bytes.length; i++) {  
//			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));  
//			temp.append((byte) (bytes[i] & 0x0f));  
//		}  
//		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp  
//				.toString().substring(1) : temp.toString();  
//	}  

	//public static byte[] str2Bcd(String asc) {  
//	public static String str2Bcd(String asc) {
//		int len = asc.length();  
//		int mod = len % 2;  
//		if (mod != 0) {  
//			asc = "0" + asc;  
//			len = asc.length();  
//		}  
//		byte abt[] = new byte[len];  
//		if (len >= 2) {  
//			len = len / 2;  
//		}  
//		byte bbt[] = new byte[len];  
//		abt = asc.getBytes();  
//		int j, k;  
//		for (int p = 0; p < asc.length() / 2; p++) {  
//			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {  
//				j = abt[2 * p] - '0';  
//			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {  
//				j = abt[2 * p] - 'a' + 0x0a;  
//			} else {  
//				j = abt[2 * p] - 'A' + 0x0a;  
//			}  
//			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {  
//				k = abt[2 * p + 1] - '0';  
//			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {  
//				k = abt[2 * p + 1] - 'a' + 0x0a;  
//			} else {  
//				k = abt[2 * p + 1] - 'A' + 0x0a;  
//			}  
//			int a = (j << 4) + k;  
//			byte b = (byte) a;  
//			bbt[p] = b;  
//		}  
//		String slResult= "";
//		try {
//			//sL_Result = new String(bbt, "UTF-8");  // Best way to decode using "UTF-8"
//			slResult = new String(bbt, Charset.forName("UTF-8"));  // Best way to decode using "UTF-8"
//		}
//		catch (Exception e) {
//			slResult="";
//		}
//		return slResult;
//		//return bbt;  
//	}  
	
	public static boolean isAmount(String s1) {
	    if (s1 == null || s1.trim().length() == 0)
	      return false;

	    // double lm_val=0;
	    try {
	      Double.parseDouble(s1);
	    } catch (Exception ex) {
	      return false;
	    }

	    return true;
	}
	public static double decimalRate(int ipDecimal, double dpRate) {
		
		double dlExchangeRate = 0;
		switch (ipDecimal) {
		case  0  : dlExchangeRate    = dpRate / 1        ;  break;
		case  1  : dlExchangeRate    = dpRate / 10       ;  break;
		case  2  : dlExchangeRate    = dpRate / 100      ;  break;
		case  3  : dlExchangeRate    = dpRate / 1000     ;  break;
		case  4  : dlExchangeRate    = dpRate / 10000    ;  break;
		case  5  : dlExchangeRate    = dpRate / 100000   ;  break;
		case  6  : dlExchangeRate    = dpRate / 1000000  ;  break; 
		case  7  : dlExchangeRate    = dpRate / 10000000 ;  break; 
		default  : break;
		}
		return dlExchangeRate;
	}
		
	public static boolean isNumberString(String spSource) {





		for (char c : spSource.toCharArray())
		{
			if (!Character.isDigit(c)) 
				return false;
		}
		return true;

	}
	/* Howard:marked on 0225 
    public static XMLGregorianCalendar getNow() throws DatatypeConfigurationException{
		XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		Calendar date = Calendar.getInstance();
		cal.setYear(date.get(Calendar.YEAR));
		cal.setMonth(date.get(Calendar.MONTH)+1);
		cal.setDay(date.get(Calendar.DATE));
		cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
		return cal;
	}
	 */
	public static int binaryToInt(String str){
		double j=0;
		for(int i=0;i<str.length();i++){
			if(str.charAt(i)== '1'){
				j=j+ Math.pow(2,str.length()-1-i);
			}

		}
		return (int) j;
	}
	public static String convertToBinary(String spSrc){
		byte[] lAry = spSrc.getBytes();

		String slTmp ="", slResult="";
		int nlResult1=0, nlResult2=0;
		for(int i=0; i<lAry.length;i++){
			slTmp = Integer.toBinaryString(0x100 + lAry[i]).substring(1);
			//System.out.println(sL_Tmp);


			nlResult1 = binaryToInt(slTmp.substring(0,4));

			nlResult2 = binaryToInt(slTmp.substring(4,8));


			slResult = slResult + Integer.toString(nlResult1) + Integer.toString(nlResult2);


			//System.out.println(Integer.toBinaryString(L_Ary[i]).substring(1));
		}
		return slResult;
	} 

	/*
    public static XMLGregorianCalendar getCurDate() throws DatatypeConfigurationException{
		XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar();
		Calendar date = Calendar.getInstance();
		cal.setYear(date.get(Calendar.YEAR));
		cal.setMonth(date.get(Calendar.MONTH)+1);
		cal.setDay(date.get(Calendar.DATE));

		//cal.setTime(date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), date.get(Calendar.SECOND));
		return cal;
	}
	 */

	/*
    public static String getCurDateStr(String sP_Sep) throws DatatypeConfigurationException{

		Calendar date = Calendar.getInstance();
		String sL_Year = Integer.toString(date.get(Calendar.YEAR));

		String sL_Month = Integer.toString(date.get(Calendar.MONTH)+1);
		sL_Month = fillCharOnLeft(sL_Month, 2, "0");

		String sL_Date = Integer.toString(date.get(Calendar.DATE));
		sL_Date = fillCharOnLeft(sL_Date, 2, "0");

		String sL_Result = sL_Year + sP_Sep + sL_Month+ sP_Sep + sL_Date; 
		return sL_Result;
	}
	 */

	/*
    public static String getCurMonthAndDate(String sP_Sep) throws DatatypeConfigurationException{

		Calendar date = Calendar.getInstance();


		int nL_Month = date.get(Calendar.MONTH)  +1;
		String sL_Result = fillCharOnLeft( Integer.toString(nL_Month), 2, "0") + sP_Sep + fillCharOnLeft( Integer.toString(date.get(Calendar.DATE)), 2, "0"); 
		return sL_Result;
	}
	 */
	public static String getTaiwanDateStr(String spSrcString) {
		//20110909 -> 1000909
		String slResult = spSrcString;
		if (spSrcString.length()>=8)
			slResult = Integer.parseInt(spSrcString.substring(0,4))-1911 + spSrcString.substring(4,8);

		return slResult;
	}

	public static boolean isFirstCharLatter(String spSource) {

		boolean blResult = false;
		char ch=spSource.charAt(0);
		if(ch<='z'&&ch>='a'||ch<='Z'&&ch>='A'){
			blResult=true;
		}
		return blResult;


	}

	public static int compareDateString(String slDate1, String slDate2) {
		int nlResult = 0;
		try {
			Date dlDate1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(slDate1);
			Date dlDate2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(slDate2);

			//System.out.println(dL_Date1);
			//System.out.println(dL_Date2);

			if (dlDate1.compareTo(dlDate2) > 0) {
				nlResult=1; // dL_Date1 > dL_Date2
			} else if (dlDate1.compareTo(dlDate2) < 0) {
				nlResult=-1; // dL_Date1 < dL_Date2
			} else if (dlDate1.compareTo(dlDate2) == 0) {
				nlResult=0; // dL_Date1 = dL_Date2
			} else {
				nlResult=-1;
				//System.out.println("Something weird happened...");
			}

		} catch (Exception e) {
			//e.printStackTrace();
			nlResult=-1;
		}

		return nlResult;
	}


	public static Date addDays(Date dpSrcDate, int npAddDayCount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dpSrcDate);
		cal.add(Calendar.DATE,npAddDayCount);
		Date dlResult =cal.getTime();

		return dlResult;
	}


	/**
	 * 判斷系統日期是否介於 sP_BeginDate 與 sP_EndDate 之間 
	 * @param spBeginDate
	 * @param spEndDate
	 * @return
	 */
	public static boolean isCurDateBetweenTwoDays(String spBeginDate, String spEndDate) {
		boolean blResult = false;

		if ((spBeginDate.trim().equals("")) || 
				(spEndDate.trim().equals("")) ){

			return false;
		}
		try {
			String slCurDate = getCurDateStr(false);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd"); 
			Date dlBeginDate=sdf.parse(spBeginDate);
			Date dlEndDate =sdf.parse(spEndDate); 

			dlBeginDate = addDays(dlBeginDate,-1);
			dlEndDate = addDays(dlEndDate,1);

			Date dlCurDate =sdf.parse(slCurDate);

			if ((dlBeginDate.before(dlCurDate)) && 
					(dlCurDate.before(dlEndDate)) ){ 
				blResult = true; 
			}else{ 
				blResult=false;
			} 
		} catch (Exception e) {
			// TODO: handle exception
			blResult = false;
		}

		return blResult;
	}

	public static int compareDateDiffOfDay(String slDate1, String slDate2) {
		//計算兩個日期差異幾天 => sL_Date2 - sL_Date1  看看相隔幾天
		int nlResult = 0;
		try {
			Date dlDate1 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(slDate1);
			Date dlDate2 = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH).parse(slDate2);

			long lBetweenDate = (dlDate2.getTime() - dlDate1.getTime())/(1000*60*60*24);

			nlResult = Integer.parseInt(Long.toString(lBetweenDate));



		} catch (Exception e) {
			//e.printStackTrace();
			nlResult=-1;
		}

		return nlResult;
	}

	public static int getRandomNumber(int nlMaxNum) 
	{
		int nlResult = 0;

		SecureRandom lRandomGen = new SecureRandom();

		nlResult = lRandomGen.nextInt(nlMaxNum);
		lRandomGen = null;

		return nlResult;
	}

	/*
    public static String getCurHourAndMinStr(String sP_Sep) throws DatatypeConfigurationException{

		Calendar date = Calendar.getInstance();

		String sL_Hour = String.format("%1$tH", date);
		sL_Hour = fillCharOnLeft(sL_Hour, 2, "0");

		String sL_Min = String.format("%1$tM", date);
		sL_Min = fillCharOnLeft(sL_Min, 2, "0");

		String sL_Result = sL_Hour + sP_Sep + sL_Min ; 
		return sL_Result;
	}
	 */

	/*
    public static String getCurHMS(String sP_Sep) throws DatatypeConfigurationException{
    	//取得現在的HHMMSS
		Calendar date = Calendar.getInstance();

		String sL_Hour = String.format("%1$tH", date);
		sL_Hour = fillCharOnLeft(sL_Hour, 2, "0");

		String sL_Min = String.format("%1$tM", date);
		sL_Min = fillCharOnLeft(sL_Min, 2, "0");

		String sL_Sec = String.format("%1$tS", date);
		sL_Sec = fillCharOnLeft(sL_Sec, 2, "0");


		String sL_Result = sL_Hour + sP_Sep + sL_Min + sP_Sep + sL_Sec; 
		return sL_Result;
	}
	 */
//	private static String pad(String str, int size, char padChar) {
//		StringBuffer padded = new StringBuffer(str);
//		while (padded.length() < size) {
//			padded.append(padChar);
//		}
//		return padded.toString();
//	}

//	public static byte[] stringToBytes22(String str) {
//
//		String data;
//
//		data = pad(str, str.length(), ' ');
//
//		return data.getBytes(Charset.forName("UTF-8"));
//		//return data.getBytes("Cp500");
//
//		/*
//		byte[] b = new byte[str.length() / 8];
//		int count = 0;
//
//		for (int i = 0; i < b.length; i++) {
//			b[i] = Byte.parseByte(str.substring(count, count + 8), 2);
//			count += 8;
//		}
//		// Integer.parseInt(c, 2)
//
//		return b;
//		 */
//
//	}


	public static String hextoStr(String spHexString) {
		byte[] bytes;
		String slResult = "";
		try {
			
			/*
			bytes = DatatypeConverter.parseHexBinary(sP_HexString);
			sL_Result= new String(bytes);
			*/
			
			
			bytes = Hex.decodeHex(spHexString.toCharArray());
			//sL_Result = new String(bytes, "UTF-8") ;
			slResult = new String(bytes) ;
			
		} catch (Exception e) {
		// TODO Auto-generated catch block
		}
		
		return slResult;
	}
	
	public static byte[] transHexString2ByteAry(String src) {
	    byte[] biBytes = new BigInteger("10" + src.replaceAll("\\s", ""), 16).toByteArray();
	    return Arrays.copyOfRange(biBytes, 1, biBytes.length);
	}

//	public static String strToHex(String spAsciiString) {
//		char[] chars = spAsciiString.toCharArray();
//		StringBuilder hex = new StringBuilder();
//		for (char ch : chars) {
//			hex.append(Integer.toHexString((int) ch));
//		}
//		return hex.toString();
//	}

	
//	private static String asciiToHex(String asciiStr) {
//		char[] chars = asciiStr.toCharArray();
//		StringBuilder hex = new StringBuilder();
//		for (char ch : chars) {
//			hex.append(Integer.toHexString((int) ch));
//		}
//		return hex.toString();
//	}

//	public static String hexToAscii(String hexStr) {
//		StringBuilder output = new StringBuilder("");
//
//		for (int i = 0; i < hexStr.length(); i += 2) {
//			String str = hexStr.substring(i, i + 2);
//			output.append((char) Integer.parseInt(str, 16));
//		}
//
//		return output.toString();
//	}
//	public static byte[] hexStrToByteArr(String spSrc) {
//
//		int len = spSrc.length();
//		byte[] data = new byte[len / 2];
//		for (int i = 0; i < len; i += 2)
//			data[i / 2] = (byte) ((Character.digit(spSrc.charAt(i), 16) << 4)
//					+ Character.digit(spSrc.charAt(i+1), 16));
//		return data;
//	}


//	public static byte[] strToByteAry(String sPSrc) {
//
//
//		byte[] b = new byte[sPSrc.length() / 8];
//		int count = 0;
//
//		for (int i = 0; i < b.length; i++) {
//			b[i] = Byte.parseByte(sPSrc.substring(count, count + 8), 2);
//			count += 8;
//		}
//		// Integer.parseInt(c, 2)
//
//		return b;
//
//
//
//
//
//	}
//	public static String readDataFromEasyCard(BufferedInputStream pEasyCardBufferedInputStream, BufferedReader pBufferReader) throws Exception{
//		int headLen=0,packetLen=0,inputLen=0;
//		byte[]  authData = new byte[2048];
//		byte[]  lenData  = new byte[3];
//		String slIsoStrFromEasyCard ="";
//
//		try {
//			BufferedReader lEasyCardBufferedReader = new BufferedReader(new InputStreamReader(pEasyCardBufferedInputStream));    	
//			if (1==1) {
//				//BufferedInputStream G_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//
//
//				//while (true) {
//				headLen =  pEasyCardBufferedInputStream.read(lenData, 0, 2);
//				if ( headLen != 2 ) {
//					return "";
//				}  
//				// �q SOCKET Ū�������� 
//				packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);
//
//				inputLen  = pEasyCardBufferedInputStream.read(authData, 0, packetLen);
//
//				slIsoStrFromEasyCard    = new String(authData,0,inputLen);
//
//
//				//System.out.print("readDataFromEasyCard =>" + sL_IsoStrFromEasyCard + "===");
//
//
//				//break;
//				//}
//			}
//			else {
//
//				//BufferedReader in = new BufferedReader(new InputStreamReader(P_Socket.getInputStream()));
//				//BufferedReader in = P_BufferReader;
//
//				while ((slIsoStrFromEasyCard = lEasyCardBufferedReader.readLine()) != null) {
//
//					break;
//				}
//
//				//System.out.print("readDataFromEasyCard :" + sL_IsoStrFromEasyCard + "===");
//
//			}
//		}//end try
//		catch (Exception e) {
//			//System.out.println("readDataFromEasyCard() error=> " + e.getMessage());
//			throw e;
//		}
//		return slIsoStrFromEasyCard;
//	}

//	public static String readDataFromEcs(BufferedInputStream pEcsBufferedInputStream) {
//		int headLen=0,packetLen=0,inputLen=0;
//		byte[]  authData = new byte[2048];
//		byte[]  lenData  = new byte[3];
//		String slIsoStrFromEcs ="";
//
//		try {
//
//			if (1==1) {
//				//BufferedInputStream G_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//
//
//				//while (true) {
//				headLen =  pEcsBufferedInputStream.read(lenData, 0, 2);
//				if ( headLen != 2 ) {
//					return "";
//				}  
//				// �q SOCKET Ū�������� 
//				packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);
//
//				inputLen  = pEcsBufferedInputStream.read(authData, 0, packetLen);
//
//				slIsoStrFromEcs    = new String(authData,0,inputLen);
//
//				//System.out.print("readDataFromEcs :" + sL_IsoStrFromEcs + "===");
//
//
//				//break;
//				//}
//			}
//			else {
//
//			}
//		}//end try
//		catch (Exception e) {
//			System.out.println("readDataFromEcs() error=> " + e.getMessage());
//		}
//		return slIsoStrFromEcs;
//	}

	/*
    public static void writeData2EasyCard(Socket P_Socket, String sP_Data) throws Exception{


    	BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
    	BufferedOutputStream L_SocketWriter = new BufferedOutputStream(P_Socket.getOutputStream());



    	if (1==1) {
        	PrintWriter L_PrintWriter = new PrintWriter(P_Socket.getOutputStream());
    		L_PrintWriter.println("****" + sP_Data + "================");
    		L_PrintWriter.flush();
    	}
    	else {
    		L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);

    		L_SocketWriter.flush();
    	}

    }
	 */
//	public static String exchangeDataWithEasyCard(BufferedOutputStream  pEasyCardBufferedOutputStream,BufferedInputStream  pEasyCardBufferedInputStream,  String spData) throws Exception{
//		String slEasyCardResponseData="";
//
//		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//
//
//
//
//		if (1==2) { 
//			/*
//        	PrintWriter L_PrintWriter = new PrintWriter(P_EasyCardBufferedOutputStream);
//    		L_PrintWriter.println("****" + sP_Data + "================");
//    		L_PrintWriter.flush();
//			 */
//		}
//		else {
//
//			writeData2EasyCard(pEasyCardBufferedOutputStream, spData);
//		}
//		slEasyCardResponseData = readDataFromEasyCard(pEasyCardBufferedInputStream, null);
//
//		return slEasyCardResponseData;
//	}
	public static String getMonthEndDate(String spYear, String spMonth) {
		//傳入 (2016,12) => 回傳 20161231
		//傳入 (2016,2) => 回傳 20160229
		String slResult = "";
		try {
			YearMonth yearMonth = YearMonth.of( Integer.parseInt(spYear), Integer.parseInt(spMonth));
			LocalDate endOfMonth = yearMonth.atEndOfMonth();
			//DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate( FormatStyle.SHORT );
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			formatter = formatter.withLocale( Locale.US );  // Re-assign JVM’s current default Locale that was implicitly applied to the formatter.
			slResult = endOfMonth.format( formatter );

			//System.out.println(sL_Result+"--");

		} catch (Exception e) {
			// TODO: handle exception
			slResult = "";
		}
		return slResult;
	}

//	private static String interpretHostResponseData(byte[] response) throws UnsupportedEncodingException, IOException {
//
//		String ebss; // X(1)
//		String tsqName; // X(16)
//		String filler; // X(20
//		String httpReturnCode = ""; // X(2)
//		String result = "";
//		int iPos = 0;
//		StringBuilder receiveLog = new StringBuilder("");
//
//		try {
//
//			ebss = getHostReturnFiled(response, iPos, 1); // 0~1
//			iPos = iPos + 1;
//			receiveLog.append("clsCWSServerPtl::EBSS::" + ebss + "\r\n");
//
//			tsqName = getHostReturnFiled(response, iPos, 16); // 1~16
//			iPos = iPos + 16;
//			receiveLog.append("clsCWSServerPtl::TSQ_Name::" + tsqName + "\r\n");
//
//			filler = getHostReturnFiled(response, iPos, 20); // 16~36
//			iPos = iPos + 20;
//			receiveLog.append("clsCWSServerPtl::Filler::" + filler + "\r\n");
//
//			httpReturnCode = getHostReturnFiled(response, iPos, 2); // 36~38
//			iPos = iPos + 2;
//			receiveLog.append("clsCWSServerPtl::HttpReturnCode::" + httpReturnCode + "\r\n");
//
//			if (iPos < response.length) {
//				byte[] byteHostFieldData = new byte[4];
//				System.arraycopy(response, iPos, byteHostFieldData, 0, 4);
//				int packetLen = byteToInteger(byteHostFieldData);
//				iPos = iPos + 4;
//				receiveLog.append("clsCWSServerPtl::PacketLen::" + packetLen + "\r\n");
//
//				result = getHostReturnFiled(response, iPos, packetLen);
//				receiveLog.append("clsCWSServerPtl::result::" + result + "\r\n");
//			}
//		} catch (UnsupportedEncodingException e) {
//			//logger.error(this.getClass().getName() + "_interpretHostResponseData() " + e.getClass().getName() + " : " + e.getMessage());
//			//notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(), this.getClass().getName() + "_interpretHostResponseData()");
//			//System.out.println("interpretHostResponseData error 1=>" + e.getMessage());
//			throw e;
//		} catch (IOException e) {
//			//logger.error(this.getClass().getName() + "_interpretHostResponseData() " + e.getClass().getName() + " : " + e.getMessage());
//			//notifyMonitor.sendMonitor(e.getClass().getName() + " : " + e.getMessage(), this.getClass().getName() + "_interpretHostResponseData()");
//			//System.out.println("interpretHostResponseData error 2=>" + e.getMessage());
//			throw e;
//		}
//
//		return httpReturnCode + result;
//	}

//	private static int byteToInteger(byte[] bytes) {
//
//		StringBuilder sb = new StringBuilder();
//
//		if (bytes != null && bytes.length > 0) {
//			// Bytes array to binary string
//			for (int i = 0; i < bytes.length; i++) {
//				sb.append(padLeft(Integer.toBinaryString(bytes[i] & 0xFF), 8, '0'));
//			}
//		}
//
//		return Integer.parseInt(sb.toString(), 2);
//	}
//
//	private static String getHostReturnFiled(byte[] hostReturnData, int start, int dataLen) throws UnsupportedEncodingException, IOException {
//
//		StringBuilder sb = new StringBuilder();
//
//		try {
//
//			if (dataLen > 0) {
//				byte[] byteHostFiled = new byte[dataLen];
//				System.arraycopy(hostReturnData, start, byteHostFiled, 0, dataLen);
//				InputStream is = new ByteArrayInputStream(byteHostFiled);
//				Reader in = new InputStreamReader(is, "Cp500");
//				int buf = -1;
//
//				while ((buf = in.read()) > -1) {
//					sb.append((char) buf);
//				}
//				in.close();
//			}
//		} catch (UnsupportedEncodingException e) {
//			throw e;
//		} catch (IOException e) {
//			throw e;
//		}
//
//		return sb.toString();
//	}

//	public static String exchangeDataWithEcs(BufferedOutputStream  pEcsBufferedOutputStream,BufferedInputStream  pEasyCardBufferedInputStream,  String spData) throws Exception{
//		String slEcsResponseData="";
//
//		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//
//
//		try {
//
//			//P_EasyCardBufferedOutputStream.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);
//
//			byte[] lAry = HpeUtil.genIsoByteAry(spData);
//			pEcsBufferedOutputStream.write(lAry, 0, lAry.length);
//
//			pEcsBufferedOutputStream.flush();
//
//
//			slEcsResponseData = readDataFromEcs(pEasyCardBufferedInputStream);
//		}
//		catch(Exception e) {
//			//System.out.println("exception on exchangeDataWithEcs() =>" + e.getMessage());
//		}
//		return slEcsResponseData;
//	}

	public static byte[] genIsoByteAry(String spIsoData) {
		byte[] lEntireAry = null;
		try {
			lEntireAry     = ("00" + spIsoData).getBytes("IBM-1047");
			
			//System.out.println("---IsoStr is=>"+ gate.isoString + "----");
			int nlTotalLen    = lEntireAry.length;
			int nlDataLen     = nlTotalLen - 2;
			lEntireAry[0]  = (byte)(nlDataLen / 256);
			lEntireAry[1]  = (byte)(nlDataLen % 256);

			/*
    		byte[] L_LenAry = new byte[2];
    		L_LenAry[0]  = (byte)(sP_IsoData.length() / 256);
    		L_LenAry[1]  = (byte)(sP_IsoData.length() % 256);


    		List<byte[]> ByteAryList = new ArrayList<byte[]>();
    		ByteAryList.add(L_LenAry);

    		byte[] L_DataAry = strToByteAry(sP_IsoData);

    		ByteAryList.add(L_DataAry);
    		L_EntireAry = convertByteAryArrayList2ByteAry(ByteAryList);
			 */

		}
		catch (Exception e) {
			lEntireAry = null;
		}
		return lEntireAry;
	}
	
	public static byte[] addLength2HeadOfByteAry(byte[] pSrcByteAry) {
		byte[] lEntireAry = null;
		try {
			int nlDataLen = pSrcByteAry.length;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
			outputStream.write( "00".getBytes() );
			outputStream.write( pSrcByteAry );
			lEntireAry = outputStream.toByteArray( );
			lEntireAry[0]  = (byte)(nlDataLen / 256);
			lEntireAry[1]  = (byte)(nlDataLen % 256);
			
			
			
			
			
			
			}
		catch (Exception e) {
			lEntireAry = null;
		}
		return lEntireAry;
	}

//	public static void writeData2EasyCard(BufferedOutputStream  pEasyCardBufferedOutputStream, String spData) throws Exception{
//		if ("".equals(spData))
//			return;
//
//		byte[] lAry = HpeUtil.genIsoByteAry(spData);
//		pEasyCardBufferedOutputStream.write(lAry, 0, lAry.length);
//
//		pEasyCardBufferedOutputStream.flush();
//		/*
//    	//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//    	BufferedOutputStream L_SocketWriter = P_EasyCardBufferedOutputStream;
//
//
//
//    	if (1==1) { //���ծɡA�Ȧ�post�L�Ӫ���ơA�n�o�ˤ~�i�H�e��easycard
//        	PrintWriter L_PrintWriter = new PrintWriter(P_EasyCardBufferedOutputStream);
//    		L_PrintWriter.println("****" + sP_Data + "================");
//    		L_PrintWriter.flush();
//    	}
//    	else {
//    		L_SocketWriter.write(sP_Data.getBytes(), 0, sP_Data.getBytes().length);
//
//    		L_SocketWriter.flush();
//    	}
//		 */
//	}

//	public static String genBitMap(List pDataFieldList) {
//		String slBitmap="", slFirstByteValue="0";
//		for(int i=2; i<=128; i++) {
//			if (pDataFieldList.indexOf(i) >=0) {
//				slBitmap +="1";
//				if (i>64)
//					slFirstByteValue="1";
//			}
//			else
//				slBitmap +="0";
//		}
//
//		slBitmap = slFirstByteValue + slBitmap;
//
//		String slBitmap1 = slBitmap.substring(0, 64);
//		String slBitmap2 = slBitmap.substring(64, 128);
//
//		String slHexBitmap = convertBinaryStrToHexStr(slBitmap1) + convertBinaryStrToHexStr(slBitmap2);
//
//		return slHexBitmap;
//
//	}

//	public static void writeData2Socket(BufferedOutputStream  pOutputStream, byte[] pData) throws Exception{
//		if (pData==null)
//			return;
//
//		try {
//			//sP_Data = "CCC" +sP_Data;//for test
//			//System.out.println("begin writeData2Auth()");
//			//System.out.println("data string is =>" + sP_Data + "===");
//			byte[] lAry = pData;
//			//byte[] L_Ary = HpeUtil.genIsoByteAry(sP_Data);
//			//System.out.println("data from byte array is =>" + new String(L_Ary) + "===");
//			pOutputStream.write(lAry, 0, lAry.length);
//
//			pOutputStream.flush();
//			//System.out.println("end writeData()");
//
//		} catch (Exception ex) {
//
//			// TODO: handle exception
//			throw ex;
//
//		}
//	}
	public static boolean writeData2Acer(BufferedOutputStream  pOutputStream, byte[] pData, int pLen) {
		boolean blResult = true;
		if (pData==null)
			return false;

		try {
			//sP_Data = "CCC" +sP_Data;//for test
			//System.out.println("begin writeData2Auth()");
			//System.out.println("data string is =>" + sP_Data + "===");
			byte[] lAry = pData;
			//byte[] L_Ary = HpeUtil.genIsoByteAry(sP_Data);
			//System.out.println("data from byte array is =>" + new String(L_Ary) + "===");
			pOutputStream.write(lAry, 0, pLen);

			pOutputStream.flush();
			//System.out.println("end writeData()");

		} catch (Exception ex) {

			// TODO: handle exception
//			throw ex;
			blResult = false;
		}
		return blResult;
	}

//	public static String readDataFromSocket(BufferedInputStream  pInputStream) throws Exception{
//		String slResult = "";
//		try {
//			byte[]  lDataByteAry = new byte[2048];
//			byte[]  lenData  = new byte[3];
//			int headLen =  pInputStream.read(lenData, 0, 2);
//
//			if ( headLen == 2 ) {
//				int packetLen = (lenData[0] & 0xFF) * 256 + (lenData[1] & 0xFF);
//
//				int inputLen  = pInputStream.read(lDataByteAry, 0, packetLen);
//
//				slResult    = new String(lDataByteAry,0, inputLen);
//			}
//
//		} catch (Exception ex) {
//
//			// TODO: handle exception
//			throw ex;
//
//		}
//
//		return slResult;
//	}
	
	public static byte[] readDataFromAcer(BufferedInputStream  pInputStream) throws Exception{
		String slResult = "";
		byte[]  lDataByteAry = new byte[2048];
		byte[]  lenData  = new byte[2];
		try {
//			byte[]  L_DataByteAry = new byte[2048];
//			byte[]  lenData  = new byte[3];
			int headLen =  pInputStream.read(lenData, 0, 2);

			if ( headLen == 2 ) {
//				int packetLen = (lenData[0] & 0xFF) + (lenData[1] & 0xFF);
				int packetLen = Integer.parseInt(byte2HexStr(lenData));
//				System.out.println("@@@@@readDataFromAcer_lenth="+packetLen);
				int inputLen  = pInputStream.read(lDataByteAry, 0, packetLen);

				slResult    = new String(lDataByteAry,0, inputLen);
//				System.out.println("@@@@@readDataFromAcer_Result="+slResult);

			}			
		} catch (Exception ex) {

			// TODO: handle exception
			throw ex;

		}

		return lDataByteAry;
	}

//	public static void writeData2ECS(BufferedOutputStream  pBufferedOutputStream, String spData) throws Exception{
//
//
//		//BufferedInputStream L_SocketReader = new BufferedInputStream(P_Socket.getInputStream());
//
//
//
//
//		if (1==2) {
//			/*
//        	PrintWriter L_PrintWriter = new PrintWriter(P_BufferedOutputStream);
//    		L_PrintWriter.println("****" + sP_Data + "================");
//    		L_PrintWriter.flush();
//			 */
//		}
//		else {
//			byte[] lAry = genIsoByteAry(spData);
//			byte[]  lenData  = new byte[3];
//			byte[]  authData = new byte[2048];
//
//			pBufferedOutputStream.write(lAry, 0, lAry.length);
//			pBufferedOutputStream.flush();
//
//
//		}
//
//
//	}

	public static String getCurTimeStr() throws DatatypeConfigurationException{

		//SimpleDateFormat L_SDF = new SimpleDateFormat("hhmmssSSS");
		SimpleDateFormat lSdf = new SimpleDateFormat("HHmmss");

		String slResult = lSdf.format(new Date());

		return slResult;
	}

	public static String getByteHex(byte[] inputByte){
		StringBuilder str=new StringBuilder();
		for(byte byte1:inputByte){
			str.append(toHex(byte1));
		}
		return str.toString();
	} 

	public static String toHex(byte b){
		return (""+"0123456789ABCDEF".charAt(0xf&b>>4)+"0123456789ABCDEF".charAt(b&0xf));
	}
	public static byte[] transToEBCDIC(String spSrc) throws UnsupportedEncodingException {
		/*
		String data;

		if (this._data.length() > _length)
			data = this._data.substring(0, _length);
		else
			data = pad(this._data, _length, ' ');


		clsTools.recordSendLog(data);
		 */
//		return sP_Src.getBytes("Cp500");
//		System.out.println("ebcdic=Cp1047");
		return spSrc.getBytes("Cp1047");

	}

//	public static String padLeft(String str, int size, char padChar) {
//		StringBuilder padded = new StringBuilder(str);
//		while (padded.length() < size) {
//			padded.insert(0, padChar);
//		}
//		return padded.toString();
//	}

//	public static byte[] stringToBytes(String str) {
//		byte[] b = new byte[str.length() / 8];
//		int count = 0;
//
//		for (int i = 0; i < b.length; i++) {
//			b[i] = Byte.parseByte(str.substring(count, count + 8), 2);
//			count += 8;
//		}
//		// Integer.parseInt(c, 2)
//		//System.out.println("result of stringToBytes()=>" + b.toString() + "---" );
//		return b;
//	}

	public static String getCurDateTimeStr(boolean bpIncludeMSec, boolean blIncludeSep) throws DatatypeConfigurationException{

		SimpleDateFormat lSdf = null;
		if (bpIncludeMSec) {
			if (blIncludeSep)
				lSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
			else
				lSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		}
		else {
			if (blIncludeSep)
				lSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			else
				lSdf = new SimpleDateFormat("yyyyMMddHHmmss");
		}
		String slResult = lSdf.format(new Date());

		return slResult;
	}

	public static boolean isNumeric(String str)
	{
		for (int i = str.length();--i>=0;)
		{
			if (!Character.isDigit(str.charAt(i)))
			{
				return false;
			}
		}
		return true;
	}

//	public static List<String> getAllFileName(String spFullPathFile)
//	{
//		List<String> lFileNames= new ArrayList<String>();
//
//		File[] files = new File(spFullPathFile).listFiles();
//		//If this pathname does not denote a directory, then listFiles() returns null. 
//
//		for (File file : files) {
//			if (file.isFile()) {
//				lFileNames.add(file.getName());
//			}
//		}	
//
//		return lFileNames;
//	}
//	public static List<String> readFileAllLines(String spFullPathFileName)
//	{
//		List<String> lResult=null;
//		Path file = Paths.get(spFullPathFileName);
//
//		try
//		{
//			if(Files.exists(file) && Files.isReadable(file)) {
//				lResult = Files.readAllLines(file, StandardCharsets.UTF_8);
//			}
//		}
//		catch (IOException e) {
//			lResult = null;
//			//e.printStackTrace();
//		}
//		return lResult;
//	}

//	public static boolean deleteFile(String spFullPathFileName)
//	{
//		boolean blResult = true;
//
//		try {
//			Path file = Paths.get(spFullPathFileName);
//			Files.delete(file);
//		}
//		catch (IOException e) {
//			blResult = false;
//			//e.printStackTrace();
//		}
//		return blResult;
//
//	}

	public static String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

//	public static boolean writeToFile(String spFullPathFileName, String spContent)
//	{
//		boolean blResult = true;
//
//		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream(spFullPathFileName), "utf-8"))) {
//			writer.write(spContent);
//		}
//		catch (IOException e) {
//			blResult = false;
//			//e.printStackTrace();
//		}
//		return blResult;
//
//	}
	//kevin:這邊總共有四個方法，分別是將 string/hex 互轉以及 byte array/hex 互轉
	public static String byte2Hex(byte[] b) {
		String result = "";
		for (int i=0 ; i<b.length ; i++)
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		return result;
	}
	 
	public static String string2Hex(String plainText, String charset) throws UnsupportedEncodingException {
		return String.format("%040x", new BigInteger(1, plainText.getBytes(charset)));
	}
	 
	public static byte[] hex2Byte(String hexString) {
		byte[] bytes = new byte[hexString.length() / 2];
		for (int i=0 ; i<bytes.length ; i++)
			bytes[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
		return bytes;
	}
	 
//	public static String hex2String(String hexString) {
//	    StringBuilder str = new StringBuilder();
//	    for (int i=0 ; i<hexString.length() ; i+=2)
//	        str.append((char) Integer.parseInt(hexString.substring(i, i + 2), 16));
//	    return str.toString();
//	}
	
//	public static String str2HexStr(String str) {
//		 
//		char[] chars = "0123456789ABCDEF".toCharArray();
//		StringBuilder sb = new StringBuilder("");
//		byte[] bs = str.getBytes();
//		int bit;
// 
//		for (int i = 0; i < bs.length; i++) {
//			bit = (bs[i] & 0x0f0) >> 4;
//			sb.append(chars[bit]);
//			bit = bs[i] & 0x0f;
//			sb.append(chars[bit]);
//			sb.append(' ');
//		}
//		return sb.toString().trim();
//	}
 
	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;
 
		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}
 
	/**
	 * bytes转换成十六进制字符串
	 * 
	 * @param byte[] b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//			sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
 
	/**
	 * bytes字符串转换为Byte值
	 * 
	 * @param String
	 *            src Byte字符串，每个Byte之间没有分隔符
	 * @return byte[]
	 */
//	public static byte[] hexStr2Bytes(String src) {
//		int m = 0, n = 0;
//		int l = src.length() / 2;
//		System.out.println(l);
//		byte[] ret = new byte[l];
//		for (int i = 0; i < l; i++) {
//			m = i * 2 + 1;
//			n = m + 1;
//			ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
//		}
//		return ret;
//	}
 
	/**
	 * String的字符串转换成unicode的String
	 * 
	 * @param String
	 *            strText 全角字符串
	 * @return String 每个unicode之间无分隔符
	 * @throws Exception
	 */
//	public static String strToUnicode(String strText) throws Exception {
//		char c;
//		StringBuilder str = new StringBuilder();
//		int intAsc;
//		String strHex;
//		for (int i = 0; i < strText.length(); i++) {
//			c = strText.charAt(i);
//			intAsc = (int) c;
//			strHex = Integer.toHexString(intAsc);
//			if (intAsc > 128)
//				str.append("\\u" + strHex);
//			else
//				// 低位在前面补00
//				str.append("\\u00" + strHex);
//		}
//		return str.toString();
//	}
 
	/**
	 * unicode的String转换成String的字符串
	 * 
	 * @param String
	 *            hex 16进制值字符串 （一个unicode为2byte）
	 * @return String 全角字符串
	 */
//	public static String unicodeToString(String hex) {
//		int t = hex.length() / 6;
//		StringBuilder str = new StringBuilder();
//		for (int i = 0; i < t; i++) {
//			String s = hex.substring(i * 6, (i + 1) * 6);
//			// 高位需要补上00再转
//			String s1 = s.substring(2, 4) + "00";
//			// 低位直接转
//			String s2 = s.substring(4);
//			// 将16进制的string转为int
//			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
//			// 将int转换为字符
//			char[] chars = Character.toChars(n);
//			str.append(new String(chars));
//		}
//		return str.toString();
//	}
//	public static byte[] asBytes (String s) {                   
//        String tmp;
//        byte[] b = new byte[s.length() / 2];
//        int i;
//        for (i = 0; i < s.length() / 2; i++) {
//          tmp = s.substring(i * 2, i * 2 + 2);
//          b[i] = (byte)(Integer.parseInt(tmp, 16) & 0xff);
//        }
//        return b;                                            //return bytes
//	}
	public static byte[] genFiscIsoByteAry(String spIsoData, String spBitMap) {
		byte[] lEntireAry = null;
		try {
			lEntireAry     = ("00" + spIsoData).getBytes("IBM-1047");
			
			//System.out.println("---IsoStr is=>"+ gate.isoString + "----");
			int nlTotalLen    = lEntireAry.length;
			int nlDataLen     = nlTotalLen - 2;
			lEntireAry[0]  = (byte)(nlDataLen / 256);
			lEntireAry[1]  = (byte)(nlDataLen % 256);
			byte[] slBitMap = hex2Byte(spBitMap);
			int x = 6;
			int y = 7;
			for(int i=0;i<slBitMap.length;i++) {
//				System.out.println("BITMAP="+slBitMap[i]);
				 Arrays.fill(lEntireAry, x, y, slBitMap[i]);
				 x++; 
				 y++;
			}


		}
		catch (Exception e) {
			lEntireAry = null;
		}
		return lEntireAry;
	}
	//kevin:轉換byte再決定是否轉換ebcdic
	public static byte[] getSubByteAry(byte[] pSrcByteAry, int npBeginPos, int npLength) {
		byte[] lResult = new byte[npLength];

		System.arraycopy(pSrcByteAry, npBeginPos,  lResult, 0, npLength);

		return lResult;

	}
	//kevin:轉換ebcdic to ascii
	public static String ebcdic2Str(byte[] conStr)  {

		byte[] asc = null;
		try {
			asc = "                                                                          [.<(+]&         !$*);^-/        |,%_>?         `:#@'=#0abcdefghi       jklmnopqr       ~stuvwxyz                      {ABCDEFGHI      }JKLMNOPQR      #0STUVWXYZ      0123456789      ".getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			return null;
		}

		byte[] rtn = new byte[conStr.length];

		for(int i = 0; i < conStr.length; i++ ) {  
			int j = byteToUnsignedInt(conStr[i]);  
			rtn[i] = asc[j]; 
		}
		try {
			return new String(rtn, "ASCII");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
//		return null;

	}
	private static int byteToUnsignedInt(byte b) {
		return 0x00 << 24 | b & 0xff;
	}
	/**
	 * curlToken 處理http post傳送與接收作業
	 * V1.00.30 弱掃修復：Server Identity Verification Disabled
	 * @return 如果查核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	//kevin: post message to RS for request token
	public static String curlToken(String urlString, String accepts, String token, String minusD, int timeOutSec) {
	    HttpURLConnection con = null;
	    String host;
		try {
            URL url = new URL(urlString);
            host = url.getHost();
			con = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e1) {
	    	return "ERROR";
		} catch (IOException e1) {
	    	return "ERROR";
		}
	    //kevin:https連線
        SSLSocketFactory oldSocketFactory = null;
        HostnameVerifier oldHostnameVerifier = null;

        boolean useHttps = urlString.startsWith("https");
        if (useHttps) {
            HttpsURLConnection https = (HttpsURLConnection) con;
            oldSocketFactory = trustAllHosts(https);
            oldHostnameVerifier = https.getHostnameVerifier();
            https.setHostnameVerifier( new HostnameVerifier() {
            	
                @Override
                public boolean verify(String hostname, SSLSession sslsession) {

                    if(host.equals(hostname)){//判断域名是否和證書域名相等
                        return true;
                    } else {
                        return false;
                    }
                }});
        }
	    con.setDoOutput(true);
	    con.setReadTimeout(timeOutSec*1000); 
	    con.setConnectTimeout(timeOutSec*1000); 
	    try {
    		con.setRequestMethod("POST");
		} catch (ProtocolException e1) {
	    	return "ERROR";
		}
	    con.setRequestProperty("accept", "*/*");
	    if (token.length() > 0) {
	        con.setRequestProperty("Authorization", token);
	        
	    }
	    con.setRequestProperty("Content-Type",  accepts);
        // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
	    try (java.io.OutputStream output = con.getOutputStream()) {
	    	output.write(minusD.getBytes("UTF-8"));
	    	output.close();
	    } catch (Exception e) {
//	    	System.out.println("curltoken write error="+e);
	    	return "ERROR";
	    }

        // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
	    byte[] respBytes = null;
	    try (ByteArrayOutputStream rspBuff = new ByteArrayOutputStream();
	    InputStream rspStream = con.getInputStream();) {

		    int c;
		    while ((c = rspStream.read()) > 0) {
		        rspBuff.write(c);
		    }
		    rspStream.close();
		    respBytes = rspBuff.toByteArray();
	    } catch (IOException e) {
//	    	System.out.println("curltoken read timeout="+e);
	    	return "TIMEOUT";
	    }

	    return new String(respBytes);
	}
	/**
	 * curlGet 處理http get傳送與接收作業
	 * V1.00.30 弱掃修復：Server Identity Verification Disabled
	 * @return 如果查核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	//kevin: http get for ims health check
	public static String curlGet(String urlString, String accepts) {
	    HttpURLConnection con = null;
	    String host;
		try {
            URL url = new URL(urlString);
            host = url.getHost();
			con = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e1) {
	    	return "ERROR";
		} catch (IOException e1) {
	    	return "ERROR";
		}
        SSLSocketFactory oldSocketFactory = null;
        HostnameVerifier oldHostnameVerifier = null;

        boolean useHttps = urlString.startsWith("https");
        if (useHttps) {
            HttpsURLConnection https = (HttpsURLConnection) con;
            oldSocketFactory = trustAllHosts(https);
            oldHostnameVerifier = https.getHostnameVerifier();
            https.setHostnameVerifier( new HostnameVerifier() {
            	
                @Override
                public boolean verify(String hostname, SSLSession sslsession) {

                    if(host.equals(hostname)){//判断域名是否和證書域名相等
                        return true;
                    } else {
                        return false;
                    }
                }});
        }
	    con.setDoOutput(true);
	    con.setReadTimeout(4*1000); 
	    con.setConnectTimeout(4*1000); 
		try {
			con.setRequestMethod("GET");
		} catch (ProtocolException e1) {
//	    	System.out.println("curlget get request error"+e1);
	    	return "ERROR";
		}

	    con.setRequestProperty("accept", "*/*");
	    con.setRequestProperty("Content-Type",  accepts);
 
		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));){
		    StringBuilder sb = new StringBuilder();
		    int cp;
		    while ((cp = in.read()) != -1) {
		      sb.append((char) cp);
		    }
		    in.close();
		    return sb.toString();
		} catch (IOException e2) {
//		    	System.out.println("curlget read error"+e2);
			return "TIMEOUT";

		}
	}
	//kevin:base64 encoding
	public static String encoded2Base64(byte[] bpSrc) {

		String slResult ="";
//		try {

		final Base64.Encoder encoder = Base64.getEncoder();
		slResult = encoder.encodeToString(bpSrc);

//		} catch (Exception e) {
//			// TODO: handle exception
//			return null;
//		}

		return slResult;
	}
	//kevin:decimalRemove去除小數位
	public static String decimalRemove(double blAmt) {

		String slResult ="";
		DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
		slResult = decimalFormat.format(blAmt);

		return slResult;
	}
	//kevin:編碼UTF-8 、Cp1047
	public static byte[] transByCode(String spSrc, String spCode) {
//		System.out.println("Code =" + spCode);
		try {
			return spSrc.getBytes(spCode);
		} catch (UnsupportedEncodingException e) {

		}
		return null;  
	}
	//kevin:decodeBase64toHex
	public static String decodedBase642Hex(String spEncodedStr) {
		String slBase64 ="";
		String slResult ="";
		try {
			
					
			final Base64.Decoder decoder = Base64.getDecoder();
			
			//還原Base64

			slBase64 =  ebcdic2Str(hex2Byte(spEncodedStr));

//			System.out.println("@@@@@slBase64 = "+ slBase64);
			//解碼
			slResult =  byte2Hex(decoder.decode(slBase64));
			


		} catch (Exception e) {
			// TODO: handle exception
		}

		return slResult;

	}
	public static String replaceIndex(int sIndex, int eIndex, String res, String str){
		StringBuilder sb = new StringBuilder(str);
		return new String(sb.replace(sIndex, eIndex, res));
	}
	
	//SHA-2 cryptographic hash functions
	public static String sha256(String sPInput) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		digest.reset();
		try {
			digest.update(sPInput.getBytes("utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.format("%064x", new BigInteger(1, digest.digest()));
	}
	
	/**
	 * 過濾path特殊字符
	 * fix issue "Path Manipulation" 2020/09/17 Zuwei
	 * @param path
	 * @return
	 */
    public static String verifyPath(String path) {
        String tempStr = Normalizer.normalize(path, Normalizer.Form.NFD);
        while (tempStr.indexOf("..\\") >= 0 || tempStr.indexOf("../") >= 0) {
        	tempStr = tempStr.replace("..\\", ".\\");
        	tempStr = tempStr.replace("../", "./");
        }
        return tempStr;
    }
	//kevin:https 設定
    private static final TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }};


    /**
     * 信任所有
     * @param connection
     * @return
     */
    public static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
//          SSLContext sc = SSLContext.getInstance("TLS");
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
        	return null;
        }
        return oldFactory;
    }

	//kevin:isodata split to Hex String
	public static String[] byte2HexSplit(byte[] b) {
		String sLOutIsoHex = HpeUtil.byte2HexStr(b);
		return (sLOutIsoHex + (sLOutIsoHex.length() % 2 > 0 ? "_" : "")).split("(?<=\\G.{2})");
	}
	
	/**
     * DOM模式的解析器對象，提供防止應用程式遭受 XML 外部實體 (XXE) 攻擊的設定
     * V1.00.04 授權連線偵測異常時發送簡訊通知維護人員
     * V1.00.25 弱掃修復：XML External Entity Injection
     * @return
     * @throws ParserConfigurationException
     */
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
         
        // 調用對象的newDocumentBuilder方法得到 DOM 解析器對象。
        return documentBuilderFactory.newDocumentBuilder();
    }
    
	/**
     * 在字串中尋找比對符合條件的字串
	 * V1.00.49 風險特店調整及新增特殊特店名稱檢查(eToro)
     * @return boolean true:比對符合 ; false:比對不符合
     * @throws ParserConfigurationException
     */
    public static boolean matchString( String parent,String child ) {
        boolean result = false;
    	int count = 0;
        int index = 0;
        while( ( index = parent.indexOf(child, index) ) != -1 )
        {
            index = index+child.length();
            count++;
        }
        if (count > 0) {
        	result = true;
        }
        return result;
    }
}
