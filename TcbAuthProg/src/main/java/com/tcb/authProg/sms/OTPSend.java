/**
 * 處理SMS簡訊傳送與接收作業 
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
 * 2021/02/08  V1.00.00  Kevin       處理SMS簡訊傳送與接收作業                      *
 * 2021/02/08  V1.00.01  Tanwei       updated for project coding standard     *  
 * 2022/12/21  V1.00.30  Kevin       弱掃修復：Server Identity Verification Disabled
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.sms;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
//import com.google.gson.Gson;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.tcb.authProg.util.HpeUtil;

public class OTPSend {
	public String uid;
	public String pwd;
	public String da;
	public String sm;
//  private int icnt = 0;
//  private int iiAmount = 0;
//	private String isMsgId = "";
//  private String iiRespStatus = "";
    
//	public static String sendServiceOld( OTPSend data, String spServerUrl ) throws Exception {
//		String slResult = "";
//        OTPReceive[] recvVO = null;
//        Gson gson = new Gson();
//        try {
//        	  //System.out.println(data.toParameters());
//        	String urlstr = spServerUrl;
//            URL url = new URL(urlstr);
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setRequestMethod("POST");
//            con.setDoOutput(true);
//            
//            // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
//            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//            		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));) {
//            wr.write(data.toParameters().getBytes("utf-8"));
//            String line;
//            StringBuilder sb = new StringBuilder();
//            while ((line = rd.readLine()) != null) {
//                sb.append(line);
//            }
//            //recvVO = gson.fromJson(sb.toString(), OTPReceive[].class);
//            //System.out.println("XML 回傳值 : " +  recvVO[0].getErrorCode() );
//            
//            
//            slResult = sb.toString();
//            }
//            //System.out.println("http result =>" + sL_Result + "---");
//        }catch (Exception e) {
//                throw (e);
//        }
//        return slResult;
//	}

//	public static OTPReceive sendService( OTPSend data, String spServerUrl ) throws Exception {
////		String slResult = "";
//        OTPReceive recvVO = new OTPReceive();
////      Gson gson = new Gson();
//        try {
//        	
//        	String urlstr = spServerUrl+data.toParameters();
//            URL url = new URL(urlstr);
//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            SSLSocketFactory oldSocketFactory = null;
//            HostnameVerifier oldHostnameVerifier = null;
//            HttpsURLConnection https = (HttpsURLConnection) con;
//            oldSocketFactory = HpeUtil.trustAllHosts(https);
//            oldHostnameVerifier = https.getHostnameVerifier();
//            https.setHostnameVerifier(HpeUtil.getDoNotVerify());	 	 	  	  
//            con.setDoOutput(true);
//            con.setRequestMethod("POST"); 
//            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////            con.setRequestMethod("GET");
////            con.setDoOutput(true);
//            // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
//            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());            
//            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"Big5"));) {
//	            String sLine, isMsgId;
//	            int iiRespStatus, iiAmount;
//	            int icnt = 0;
//	            while ((sLine = rd.readLine()) != null)
//	            
//	            { System.out.println("@@@@sLine="+sLine);
//					icnt = icnt + 1;
//					switch (icnt) {				
//					case 2:
//						isMsgId = sLine.substring(6, sLine.length()).trim();					
//						break;
//					case 3:
//						iiRespStatus = Integer.parseInt(sLine.substring(11, sLine.length()).trim());					
//				      break;
//					case 4:
//						iiAmount = Integer.parseInt(sLine.substring(13, sLine.length()).trim());
//						break;	
//					}
//				}      
//            }
//            
//        }catch (Exception e) {
//                throw (e);
//        }
//        return recvVO;
//	}
	/**
	 * sendService 處理SMS簡訊傳送與接收作業
	 * V1.00.30 弱掃修復：Server Identity Verification Disabled
	 * @return 如果查核通過，return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	public static String[] sendService( OTPSend data, String spServerUrl ) throws Exception {
//		String slResult = "";
//      OTPReceive recvVO = new OTPReceive();
//      Gson gson = new Gson();
		String[] smsInfo = new String[3];
        try {
        	
        	String urlstr = spServerUrl+data.toParameters();
            URL url = new URL(urlstr);
            String host = url.getHost();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            SSLSocketFactory oldSocketFactory = null;
            HostnameVerifier oldHostnameVerifier = null;
            HttpsURLConnection https = (HttpsURLConnection) con;
            oldSocketFactory = HpeUtil.trustAllHosts(https);
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
  	  
            con.setDoOutput(true);
            con.setRequestMethod("POST"); 
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            con.setRequestMethod("GET");
//            con.setDoOutput(true);
            // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream());            
            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"Big5"));) {
	            String sLine, isMsgId;
	            int iiRespStatus, iiAmount;
	            int icnt = 0;
	            while ((sLine = rd.readLine()) != null)
	            
	            { 
					icnt = icnt + 1;
					switch (icnt) {				
					case 2:
						smsInfo[0] = sLine.substring(6, sLine.length()).trim();	
//						System.out.println("smsInfo[0]="+smsInfo[0]);
						break;
					case 3:
						smsInfo[1] = sLine.substring(11, sLine.length()).trim();
//						System.out.println("smsInfo[1]="+smsInfo[1]);
						break;
					case 4:
						smsInfo[2] = sLine.substring(13, sLine.length()).trim();
//						System.out.println("smsInfo[2]="+smsInfo[2]);
						break;	
					}
				}      
            }
            
        }catch (Exception e) {
                throw (e);
        }
        return smsInfo;
	}

	public String toParameters(){
		String lsTempBody = "" ;
        try {
			lsTempBody = URLEncoder.encode(this.sm,"Big5");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//return "User="+this.User+"&pwd="+this.Pwd+"&MobileNumber="+this.mobileNumber+"&Smsbody="+this.smsbody;
		return "username="+this.uid+"&password="+this.pwd+"&dstaddr="
				+this.da+"&smbody="+lsTempBody;
		
		
		// UID=apiuser&PWD=dGVzdDEyMzQ=&DA=0944000001&STOPTIME=2017/01/27 17:29&SM=SmsContent&EMPLATEVAR ={" data1":"信用卡系統","data2":"催收業務"}
		
		
		}
//	  private static final TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
//	      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//	          return new java.security.cert.X509Certificate[]{};
//	      }
//
//	      public void checkClientTrusted(X509Certificate[] chain, String authType)
//	              throws CertificateException {
//	      }
//
//	      public void checkServerTrusted(X509Certificate[] chain, String authType)
//	              throws CertificateException {
//	      }
//	  }
//	  };
	  
	  /**
	   * 設置不驗證主機
	   */
//	  private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
//	      public boolean verify(String hostname, SSLSession session) {
//	          return true;
//	      }
//	  };
	  
//	  private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
//	      SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
//	      try {
////	          SSLContext sc = SSLContext.getInstance("TLS");
//	          SSLContext sc = SSLContext.getInstance("TLSv1.2");
//	          sc.init(null, trustAllCerts, new java.security.SecureRandom());
//	          SSLSocketFactory newFactory = sc.getSocketFactory();
//	          connection.setSSLSocketFactory(newFactory);
//	      } catch (Exception e) {
//	          e.printStackTrace();
//	      }
//	      return oldFactory;
//	  }

}