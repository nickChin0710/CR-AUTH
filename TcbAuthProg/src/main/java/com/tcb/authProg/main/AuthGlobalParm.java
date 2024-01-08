/**
 * 授權使用之全域變數物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用之全域變數物件                         *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      * 
 * 2021/04/09  V1.00.02  Kevin       VISA_CAVV_U3V7檢核處理                     *
 * 2021/08/12  V1.00.03  Kevin       新增lock/unlock功能確保同卡號同時交易時，依序處理。 *   
 * 2021/12/27  V1.00.04  Kevin       Debug Mode ON時，不寫LOG                   *
 * 2022/07/19  V1.00.05  Kevin       Log Forging漏洞校驗                        *
 * 2022/09/28  V1.00.06  Kevin       授權連線偵測異常時發送簡訊通知維護人員             *
 * 2023/01/12  V1.00.33  Kevin       財金國內掛卡連線新增Keepalive功能               *
 * 2022/03/12  V1.00.39  Kevin       db 連線異常修復                              *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 ******************************************************************************
 */

package com.tcb.authProg.main;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.DocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.logging.log4j.ThreadContext;

import com.tcb.ap4.tool.Decryptor;
import com.tcb.authProg.sms.OTPSend;
import com.tcb.authProg.util.HpeUtil;

public class AuthGlobalParm{

	//System
	public  Map<String, Object> tableMetaDataObjects = new HashMap<String, Object>();
 	private   boolean execUnitTest=false, systemError=false, fiscSignOff=false, fiscConnectFaild=false; //V1.00.06 授權連線偵測異常時發送簡訊通知維護人員
	private   ComboPooledDataSource gDataSource = null;
	private   Logger   logger    = null;
	private   String   expMethod="",newLine="",exceptionFlag="",tokenIms="";
	private   String[] threadConnectionStatusArray=null;
	private   String[] threadConnectionNegStatusArray=null;
	private   final int webThreadChanNum=1001;
	private   final String   sgDefaultAuthHome="/cr/Auth";
	private   final String   sgDefaultAcdpHome="/PKI";
	private   final String   sgDefaultEcsHome="/cr/ecs";
	private   int   contTimout=0,contExcept=0;

	//Timestamp
	private   byte[]   carriage= new byte[3];
	private   java.sql.Timestamp gTimeStamp; 
	private   String   sysDate="",sysTime="",sqlTime="",chinDate="",dispDate="",dispTime="",millSecond="",durTime="", timeStamp="", tokenExpDate="";
	private   String   julianDate="";
	//Authorize configure parameter
	private   String   confFile,acdpFile,devMode,tableOwner,systemName,fiscHost,acerHost,ecsAcdpFile;
	private   int      fiscSession=0, fiscNegSession=0;
	private   int      maxContTimout=0,maxContExcept=0, readSocketTimeout=0;
	private   int      fiscPort=0, fiscNegPort=0, acerPort=0;
	private   int      internalAuthServerPort4Online=0, internalAuthServerPort4Atm=0;
	private   int      internalAuthServerPort4Fhm=0, internalAuthServerPort4Neg=0;
	private   int      fiscChan=0,fiscNegChan=0;
	private   boolean  debugMode=false, enableLocalAuthServer4Fisc=false, enableAcerRedemption=false, fiscNegConnectFail=false;
	private   String   ifEnableFisc="N", ifEnableEcsTrans="N", ifReturnTrueDirectly="N", ifEnableIms="N";
	private   float    warningSec=30;
	//DB  Connection information
	private   String   sgDbPInfo="";
	//SMS Connection information
	private   String   sgSmsId="", sgSmsPInfo="", sgSmsServerUrl="";
	//IMS Connection information
	private   String   sgImsId="", sgImsPInfo="", sgImsTokenReqUrl="", sgImsVdTxnUrl="", sgImsIndicate="1";
	//HSM Connection information
	private   String   hsmHost1="", hsmHost2="", hsmIndicate="";
	private   int      hsmPort1=0, hsmPort2=0;
	private   String   ifEnableHsmVerifyCvv="N", ifEnableHsmVerifyArqc="N", ifEnableHsmChangeIwk="N", ifEnableHsmVerifyIwk="N", ifEnableHsmVerifyPvv="N", ifEnableHsmVerifyPinBlock="N", ifEnableHsmVerifyACSAAV="N", ifEnableHsmTransPinBlock="N", ifEnableHsmGenAtmPvv="N", ifEnableHsmTransAtmPin="N"; 
	//LINE Connection information
	private   String   LineAiUrl="";
	
	//FISC掛卡同步處理
	private final int maxFisc   = 100;
	private int       fiscPnt   = 0;
	private Object[]  doneLock = new Object[getMaxFisc()];	
	private HashMap<String,String>  fiscRequest  = new HashMap<String,String>();
	private HashMap<String,String>  fiscResponse = new HashMap<String,String>();

	//V1.00.03 新增lock/unlock功能確保同卡號同時交易時，依序處理。
    // 保存所有鎖定的KEY及其信號量
    private final ConcurrentMap<String, Semaphore> map = new ConcurrentHashMap<String, Semaphore>();
    // 保存Threads鎖定的KEY及其鎖定計數
    private final ThreadLocal<Map<String, LockInfo>> local = new ThreadLocal<Map<String, LockInfo>>() {
        @Override
        protected Map<String, LockInfo> initialValue() {
            return new HashMap<String, LockInfo>();
        }
    };

	AuthGlobalParm()
	{
		return;
	}
	
	//get Authorize home
	public String getAuthHome() {
//		String slTargetProjHome = System.getenv("AUTH_HOME");
//		String slAuthHome = slTargetProjHome; 
		
//		if ((null  == slTargetProjHome) || ("".equals(slTargetProjHome)))
		String slAuthHome = sgDefaultAuthHome;
		
		return slAuthHome;
	}
	
	//get Authorize configure file name
	public String getAuthConfigFileName() {
		String slConfigFileName = "";
		String slAuthHome = getAuthHome();
		slConfigFileName  = slAuthHome+"/parm/Auth_Parm.txt"; 
		
		showLogMessage("I","Auth Config file name is :"+ slConfigFileName);   

		return slConfigFileName;
	}
	
	//get Acdp home
	public String getAcdpHome() {
//		String slTargetProjHome = System.getenv("ACDP_HOME");
//		String slAcdpHome = slTargetProjHome; 
//		
//		if ((null  == slTargetProjHome) || ("".equals(slTargetProjHome)))
		String slAcdpHome = sgDefaultAcdpHome;
		
		return slAcdpHome;
	}
	
	//get Acdp configure file name
	public String getAcdpConfigFileName() {
		String slConfigFileName = "";
		String slAcdpHome = getAcdpHome();
		slConfigFileName  = slAcdpHome+"/acdp.properties"; 
		
		showLogMessage("I","Acdp Config file name is :"+ slConfigFileName);   

		return slConfigFileName;
	}
	//get Acdp home
	public String getEcsHome() {
//		String slTargetProjHome = System.getenv("ECS_HOME");
//		String slAcdpHome = slTargetProjHome; 
//		
//		if ((null  == slTargetProjHome) || ("".equals(slTargetProjHome)))
		String slAcdpHome = sgDefaultEcsHome;
		
		return slAcdpHome;
	}
	
	//get EcsAcdp configure file name
	public String getEcsAcdpConfigFileName() {
		String slConfigFileName = "";
		String slEcsHome = getEcsHome();
		slConfigFileName  = slEcsHome+"/conf/ecsAcdp.properties"; 
		
		showLogMessage("I","Ecs Acdp Config file name is :"+ slConfigFileName);   

		return slConfigFileName;
	}
	/* load authorize configuration parameter */
	public void loadTextParm(String spChannelName) throws Exception {
//		spChannelName = HpeUtil.fillCharOnRight(spChannelName, 10, " ");

		String  cvtString="", slSystemName="";
		//取得本機資料
		InetAddress localhost=InetAddress.getLocalHost();
		slSystemName = localhost.getHostName();
		//避免HOSTNAME長度超過20，寫入TXLOG.MOD_PGM錯誤
		if (slSystemName.length() > 20) {
			slSystemName = slSystemName.substring(0,20);
		}
		showLogMessage("I","LOCAL HOST :"+ slSystemName);   

		//取得系統參數檔
		confFile  = getAuthConfigFileName();
        // fix issue "Path Manipulation" 2020/09/16 Zuwei
		confFile = HpeUtil.verifyPath(confFile);
		showLogMessage("I","AUTH Parameter File :"+ confFile);   
		//讀取系統參數檔
		Properties       props  =  new Properties();
        // fix issue "Unreleased Resource: Streams" 2020/09/16 Zuwei
		try (FileInputStream  fis    =  new FileInputStream(confFile);) {
			props.load(fis);
			fis.close(); 
		}

		//down, 取得 系統 設定參數
		//識別Debug Mode
		cvtString   = (props.getProperty("DEBUG_MODE").trim());
		if ("Y".equals(cvtString))
			debugMode = true;
		else
			debugMode = false;
		showLogMessage("I","AUTH Parameter DEBUG_MODE :"+ isDebugMode());   
		//識別開發環境
		devMode     = (props.getProperty("DEV_MODE").trim());
		showLogMessage("I","AUTH Parameter DEV_MODE :"+ getDevMode());   
		//db schema
		tableOwner      =  props.getProperty("TABLE_OWNER").trim();
		showLogMessage("I","AUTH Parameter TABLE_OWNER :"+ getTableOwner());   
		
		//取得 SMS 設定參數
		sgSmsServerUrl =  props.getProperty("SMS_SERVER_URL").trim();
		showLogMessage("I","AUTH Parameter SMS_SERVER_URL :"+ getSgSmsServerUrl());   
		sgSmsId      =  props.getProperty("SMS_ID").trim();
		sgSmsPInfo      =  props.getProperty("SMS_WORD").trim();

		//取得 IMS Connect 設定參數 sgImsTokenReqUrl:crap1p(t) sgImsVdTxnUrl:crap2p(t)
		sgImsTokenReqUrl      =  props.getProperty("IMS_TOKEN_REQ_URL").trim();
		showLogMessage("I","AUTH Parameter IMS_TOKEN_REQ_URL :"+ getSgImsTokenReqUrl());   
		sgImsId      =  props.getProperty("IMS_ID").trim();
		sgImsPInfo      =  props.getProperty("IMS_WORD").trim();
		sgImsVdTxnUrl      =  props.getProperty("IMS_VD_TXN_URL").trim();
		showLogMessage("I","AUTH Parameter IMS_VD_TXN_URL :"+ getSgImsVdTxnUrl());   

		//取得 SMS 設定參數
		LineAiUrl =  props.getProperty("LINE_AI_URL").trim();
		showLogMessage("I","AUTH Parameter LINE_AI_URL :"+ getLineAiUrl());   
		
		//Authorize timer setting
		cvtString     =  props.getProperty("TIME_OUT_SEC").trim();
		warningSec    =  Float.parseFloat(cvtString);
		showLogMessage("I","AUTH Parameter TIME_OUT_SEC :"+ getWarningSec());   
		cvtString     =  props.getProperty("CONT_TIME_OUT").trim();
		maxContTimout =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter CONT_TIME_OUT :"+ maxContTimout);   
		cvtString     =  props.getProperty("CONT_EXCEPTION").trim();
		maxContExcept =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter CONT_EXCEPTION :"+ maxContExcept);   
		cvtString     =  props.getProperty("READ_SOCKET_TIMEOUT").trim();
		readSocketTimeout    =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter READ_SOCKET_TIMEOUT :"+ getReadSocketTimeout());   

		//取得 HSM 設定參數
		setHsmIndicate(props.getProperty("HSM_INDICATE").trim());
		showLogMessage("I","AUTH Parameter HSM_INDICATE :"+ getHsmIndicate());   
		hsmHost1      =  props.getProperty("HSM_HOST_1").trim();
		showLogMessage("I","AUTH Parameter HSM_HOST_1 :"+ getHsmHost1());   
		cvtString     =  props.getProperty("HSM_CONNECT_PORT_1").trim();
		hsmPort1      =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter HSM_CONNECT_PORT_1 :"+ getHsmPort1());   
		hsmHost2      =  props.getProperty("HSM_HOST_2").trim();
		showLogMessage("I","AUTH Parameter HSM_HOST_2 :"+ getHsmHost2());   
		cvtString     =  props.getProperty("HSM_CONNECT_PORT_2").trim();
		hsmPort2      =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter HSM_CONNECT_PORT_2 :"+ getHsmPort2());   
		ifEnableHsmVerifyCvv   =  props.getProperty("ENABLE_HSM_VERIFY_CVV").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_CVV :"+ getIfEnableHsmVerifyCvv());   
		ifEnableHsmVerifyPvv   =  props.getProperty("ENABLE_HSM_VERIFY_PVV").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_PVV :"+ getIfEnableHsmVerifyPvv());   
		ifEnableHsmVerifyPinBlock   =  props.getProperty("ENABLE_HSM_VERIFY_PIN_BLOCK").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_PIN_BLOCK :"+ ifEnableHsmVerifyPinBlock);   
		ifEnableHsmVerifyACSAAV   =  props.getProperty("ENABLE_HSM_VERIFY_ACSAAV").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_ACSAAV :"+ getIfEnableHsmVerifyACSAAV());   
		ifEnableHsmTransPinBlock   =  props.getProperty("ENABLE_HSM_TRANS_PINBLOCK").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_TRANS_PINBLOCK :"+ ifEnableHsmTransPinBlock);   
		ifEnableHsmGenAtmPvv   =  props.getProperty("ENABLE_HSM_GEN_ATM_PVV").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_GEN_ATM_PVV :"+ getIfEnableHsmGenAtmPvv());   
		ifEnableHsmTransAtmPin =  props.getProperty("ENABLE_HSM_TRANS_ATM_PIN").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_TRANS_ATM_PIN :"+ getIfEnableHsmTransAtmPin());   
		ifEnableHsmVerifyArqc   =  props.getProperty("ENABLE_HSM_VERIFY_ARQC").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_ARQC :"+ getIfEnableHsmVerifyArqc());   
		ifEnableHsmChangeIwk   =  props.getProperty("ENABLE_HSM_CHANGE_IWK").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_CHANGE_IWK :"+ ifEnableHsmChangeIwk);   
		ifEnableHsmVerifyIwk   =  props.getProperty("ENABLE_HSM_VERIFY_IWK").trim();
		showLogMessage("I","AUTH Parameter ENABLE_HSM_VERIFY_IWK :"+ ifEnableHsmVerifyIwk);   

		//取得交易功能參數
		systemName  = (props.getProperty("SYSTEM_NAME").trim());
		systemName  = slSystemName;
		showLogMessage("I","AUTH Parameter SYSTEM_NAME :"+ getSystemName());  
		
		ifEnableFisc = props.getProperty("ENABLE_FISC").trim();
		showLogMessage("I","AUTH Parameter ENABLE_FISC :"+ getIfEnableFisc());   
		ifEnableEcsTrans   =  props.getProperty("ENABLE_ECS_TRANS").trim();
		showLogMessage("I","AUTH Parameter ENABLE_ECS_TRANS :"+ getIfEnableEcsTrans());   
		ifEnableIms  =  props.getProperty("ENABLE_IMS").trim();
		showLogMessage("I","AUTH Parameter ENABLE_IMS :"+ getIfEnableIms());   
		cvtString    =  props.getProperty("FISC_CHAN_NO").trim();
		fiscChan     =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter FISC_CHAN_NO :"+ getFiscChan());   
		cvtString    =  props.getProperty("FISC_NEG_CHAN_NO").trim();
		fiscNegChan  =  Integer.parseInt(cvtString);
		showLogMessage("I","AUTH Parameter FISC_NEG_CHAN_NO :"+ getFiscNegChan());   

		//取得FISC連線設定參數		
		String slParamKey="",  slChannelNum="";
		if ("FISC".equals(spChannelName.substring(0,4))) {
			slChannelNum = spChannelName.substring(4,5);

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_IP";
			cvtString     =  props.getProperty(slParamKey).trim();
			fiscHost =  cvtString;
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getFiscHost());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			fiscPort =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getFiscPort());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_CONNECT_SESSION";
			cvtString     =  props.getProperty(slParamKey).trim();
			fiscSession =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getFiscSession());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_NEG_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			fiscNegPort =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getFiscNegPort());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_NEG_CONNECT_SESSION";
			cvtString     =  props.getProperty(slParamKey).trim();
			fiscNegSession =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getFiscNegSession());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_ENABLE_INTERNAL_AUTH_SERVER";
			cvtString     =  props.getProperty(slParamKey).trim();
			if ("Y".equals(cvtString))
				enableLocalAuthServer4Fisc = true;
			else
				enableLocalAuthServer4Fisc = false;
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ isEnableLocalAuthServer4Fisc());   
			
			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_INTERNAL_WEB_SOCKET_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			internalAuthServerPort4Online =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getInternalAuthServerPort4Online());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_INTERNAL_ATM_SOCKET_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			internalAuthServerPort4Atm =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getInternalAuthServerPort4Atm());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_INTERNAL_FHM_SOCKET_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			internalAuthServerPort4Fhm =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getInternalAuthServerPort4Fhm());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_INTERNAL_NEG_SOCKET_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			internalAuthServerPort4Neg =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getInternalAuthServerPort4Neg());   

			slParamKey = "CHANNEL_" + slChannelNum + "_FISC_RETURN_TRUE_DIRECTLY";
			cvtString     =  props.getProperty(slParamKey).trim();
			ifReturnTrueDirectly = cvtString; 
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getIfReturnTrueDirectly());   
			//up, 取得 FISC 連線設定參數		
			
			//取得ACER Redemption的連線資訊
			slParamKey = "CHANNEL_" + slChannelNum + "_ENABLE_ACER_Redemption";
			cvtString     =  props.getProperty(slParamKey).trim();
			if ("Y".equals(cvtString))
				enableAcerRedemption = true;
			else
				enableAcerRedemption = false;
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ isEnableAcerRedemption());   

			slParamKey = "CHANNEL_" + slChannelNum + "_ACER_IP";
			cvtString     =  props.getProperty(slParamKey).trim();
			acerHost =  cvtString;
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getAcerHost());   

			slParamKey = "CHANNEL_" + slChannelNum + "_ACER_PORT";
			cvtString     =  props.getProperty(slParamKey).trim();
			acerPort =  Integer.parseInt(cvtString);
			showLogMessage("I","AUTH Parameter "+slParamKey+" :"+ getAcerPort());   
			//UP, 取得 系統 設定參數
			
			}
		//取得DB密碼參數檔
		acdpFile  = getAcdpConfigFileName();
		acdpFile = HpeUtil.verifyPath(acdpFile);
		showLogMessage("I","ACDP Parameter File :"+ acdpFile);   
		//讀取DB密碼參數檔
		Properties       acdpProps  =  new Properties();
		try (FileInputStream  acdpFis    =  new FileInputStream(acdpFile);) {
			acdpProps.load(acdpFis);
			acdpFis.close(); 
		}		
		//取得ECS密碼參數檔
		ecsAcdpFile  = getEcsAcdpConfigFileName();
		ecsAcdpFile = HpeUtil.verifyPath(ecsAcdpFile);
		showLogMessage("I","ECS ACDP Parameter File :"+ ecsAcdpFile);   
		//讀取ECS密碼參數檔
		Properties       ecsAcdpProps  =  new Properties();
		try (FileInputStream  ecsAcdpFis    =  new FileInputStream(ecsAcdpFile);) {
			ecsAcdpProps.load(ecsAcdpFis);
			ecsAcdpFis.close(); 
		}
		Decryptor decryptor = new Decryptor(); //TCB專用解密碼時使用
		if ("X".equals(getDevMode())) { 
			sgImsPInfo      =  ecsAcdpProps.getProperty("cr.ims").trim();
			sgDbPInfo       =  acdpProps.getProperty("cr.db").trim();
			sgSmsPInfo      =  ecsAcdpProps.getProperty("cr.sms").trim();
//			cvtString       =  acdpProps.getProperty("cr.credit.aid").trim();
		}
		else {
			sgImsPInfo      =  decryptor.doDecrypt(ecsAcdpProps.getProperty("cr.ims").trim());
			sgDbPInfo       =  decryptor.doDecrypt(acdpProps.getProperty("cr.db").trim());
			sgSmsPInfo      =  decryptor.doDecrypt(ecsAcdpProps.getProperty("cr.sms").trim());
//			cvtString       =  decryptor.doDecrypt(acdpProps.getProperty("cr.credit.aid").trim());
		}
		return;
	}


	/* Monitor 交易狀態 */
	public synchronized void transStatistic(float durSec,int authCnt) throws Exception
	{
		if ( durSec >=  getWarningSec() ) {
			contTimout++; 
		}
		else {
			contTimout=0; 
		}

		if ( !"Y".equals(getExceptionFlag()) ) {
			setContExcept(0); 
		}

		if ( contTimout >= maxContTimout ) {
			setSystemError(true); 
			showLogMessage("E","systemError : contTimout "+contTimout + " >= maxContTimout " + maxContTimout); 
		}
		
		if ( getContExcept() >= maxContExcept ) {
			setSystemError(true); 
			showLogMessage("E","systemError : contExcept "+getContExcept() + " >= maxContTimout " + maxContTimout); 
		}

		if ( contTimout >= maxContTimout ) {
			showLogMessage("I","maxContTimout : "+maxContTimout + " " + durSec+ " sec"); 
		}

		if ( getContExcept() >= maxContExcept ) {
			showLogMessage("I","maxContExcept : "+maxContExcept+" "+durSec+ " sec"); 
		}
		return;
	}
	//get current TimeStamp
	private static java.sql.Timestamp getCurrentTimeStamp() {

		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());

	}
	//get current date time
	public void dateTime() {
		String  dateStr="",dispStr="";
		Date    currDate = new Date();
		SimpleDateFormat form1 = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		SimpleDateFormat form2 = new SimpleDateFormat("yyyy/MM/ddHH:mm:ss");
		SimpleDateFormat form3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		dateStr     = form1.format(currDate);
		dispStr     = form2.format(currDate);
		sqlTime     = form3.format(currDate);
		gTimeStamp = getCurrentTimeStamp();
		timeStamp   = form1.format(currDate) + "000";
		sysDate     = dateStr.substring(0,8);
		chinDate    = (Integer.parseInt(dateStr.substring(0,4)) -1911) + dateStr.substring(4,8);
		sysTime     = dateStr.substring(8,14);
		millSecond  = dateStr.substring(14,17);
		dispDate    = dispStr.substring(0,10);
		dispTime    = dispStr.substring(10,18);
		carriage[0] = 0x0D;
		carriage[1] = 0x0A;
		newLine     = new String(carriage,0,2);
		DateFormat d = new SimpleDateFormat("D");
		julianDate  = d.format(currDate);
		return;
	}

	//get duration time
	public synchronized float durationTime(String startMillis) {
		long  startNum=0,endNum=0,duration=0,milsec=0;
		float floatSec=0;
		startNum  = Long.parseLong(startMillis);
		endNum    = System.currentTimeMillis();

		floatSec  = ((float)(endNum - startNum)) / 1000;
		duration  = (endNum - startNum) / 1000;
		milsec    = (endNum - startNum) % 1000;
		durTime   = duration / 3600 +" : "+ (duration % 3600) / 60 +" : "+ (duration % 60)+" : "+ milsec;
		return  floatSec;
	}

	//create log4j
	public void createLogger(String idCode, String spChannelNo, String spPortNo) throws Exception {
		String slAuthHome = getAuthHome();
		showLogMessage("D","getAuthHome="+ slAuthHome);
		String slConfigFileName="";
		slConfigFileName = slAuthHome + "/parm/log4j2.xml";

		System.setProperty("log4j.configurationFile", slConfigFileName);

		ThreadContext.put("channelno", spChannelNo);

		//LoggerContext.getContext(false).reconfigure();
		LoggerContext lLoggerContext= LoggerContext.getContext(false);

		//L_LoggerContext.putObject("channelno", sP_ChannelNo);
		lLoggerContext.reconfigure();

		//Configurator.setLevel("com.tcb.auth.main", Level.TRACE); // 改為debug level

		setLogger(LogManager.getLogger(AuthGlobalParm.class.getName()));
		getLogger().fatal("==="+ "fatal level...." + "===");
		getLogger().error("==="+ "error level...." + "===");
		getLogger().warn("==="+ "warn level...." + "===");
		getLogger().info("==="+ "info level...." + "===");
		getLogger().debug("==="+ "debug level...." + "===");
		getLogger().trace("==="+ "trace level...." + "===");

		return;
	}

	public void showLogMessage(String spMsgType, String spLogMsgContent) {
		if (null == getLogger())
			return;
		//V1.00.04 Debug Mode ON時，不寫LOG
		if ("D".equals(spMsgType) && !isDebugMode()) {
			return;
		}
		//Log Forging漏洞校驗
//		spLogMsgContent = vaildLog(spLogMsgContent);
		
		//Logger輸出的等級 : FATAL > ERROR > WARN > INFO > DEBUG > TRACE
		//Logger輸出的等級大於等於Logger設定的等級時才會輸出訊息。
		if ("F".equals(spMsgType)) {
			getLogger().fatal("==="+ spLogMsgContent + "===");
		}
		else if ("E".equals(spMsgType)) {
			getLogger().error("==="+ spLogMsgContent + "===");
		}
		else if ("W".equals(spMsgType)) {
			getLogger().warn("==="+ spLogMsgContent + "===");
		}
		else if ("I".equals(spMsgType)) {
			getLogger().info("==="+ spLogMsgContent + "===");
		}
		else if ("D".equals(spMsgType)) {
			getLogger().debug("==="+ spLogMsgContent + "===");				
		}
		else if ("T".equals(spMsgType)) {
			getLogger().trace("==="+ spLogMsgContent + "===");
		}
		else {
			getLogger().info("==="+ spLogMsgContent + "===");
		}
	}
	
	/**
	 * V1.00.05 Log Forging漏洞校驗
	 * @param log
	 * @return
	 */
	public static String vaildLog(String log) {
		List<String> list = new ArrayList<String>();
		list.add("%0d");
		list.add("\r");
		list.add("0a");
		list.add("\n");
		String encode = Normalizer.normalize(log, Normalizer.Form.NFKC);
		for (int i = 0; i < list.size(); i++) {
			encode = encode.replace(list.get(i), "");
		}
		return encode;
	}
	
	//handle Authorize Exception write log4j
	public void expHandle(Exception ex, boolean bpExFlag) {
		if (ex==null)
			return;

		String fatalMesg="";
		if (bpExFlag ) {
			setExceptionFlag("Y");
			setContExcept(getContExcept() + 1);
			fatalMesg = "####### AuthDAO Exception for Auth system and that will and reject Transaction";
		}
		else {
			fatalMesg = "####### AuthService Exception for Auth system ";
		}
		getLogger().fatal(" >> ####### AUTH Exception MESSAGE STARTED ######"+newLine);
		getLogger().fatal(fatalMesg);
		getLogger().fatal("Exception_Message : ", ex);
		getLogger().fatal(" >> ####### AUTH system Exception MESSAGE   ENDED ######"+newLine);

		setExpMethod("");

		return;
	}
	
    /**
     * 鎖定key，其他等待此key的thread將進入等待，直到調用{@link #unlock(K)}
     * 使用hash code和equals來判斷key是否相同，因此key必须實現{@link #hashCode()}和
     * {@link #equals(Object)}方法
     * 
     * @param key
     */
    public void lock(String key) {
        if (key == null)
            return;
        LockInfo info = local.get().get(key);
        if (info == null) {
            Semaphore current = new Semaphore(1);
            current.acquireUninterruptibly();
            Semaphore previous = map.put(key, current);
            if (previous != null)
                previous.acquireUninterruptibly();
            local.get().put(key, new LockInfo(current));
//          gb.showLogMessage("D","get lock key="+local.get().get(key));
        } else {
            info.lockCount++;
        }
    }
    /**
     * 釋放key，喚醒其他等待此key的thread
     * @param key
     */
    public void unlock(String key) {
        if (key == null)
            return;
        LockInfo info = local.get().get(key);
        showLogMessage("D","get unlock key="+local.get().get(key));
        if (info != null && --info.lockCount == 0) {
            info.current.release();
            map.remove(key, info.current);
            local.get().remove(key);
        }
    }
    
    /**
     * AuthGlobalParm sendSms
     * V1.00.06 授權連線偵測異常時發送簡訊通知維護人員
     * @param content
     * @return boolean
     */
	public boolean sendSms(String content) {
		boolean blResult = true;
		String[] smsResp = new String[3];
		try {
			String slFullPathTriggerFileName = getAuthHome() + "/lib/SMS.xml";
			slFullPathTriggerFileName = HpeUtil.verifyPath(slFullPathTriggerFileName);
			Path lTargetFilePath = Paths.get(slFullPathTriggerFileName);
			if (Files.exists(lTargetFilePath)) {
	            File file = new File(slFullPathTriggerFileName);
	            DocumentBuilder db = HpeUtil.newDocumentBuilder();
	            Document document = db.parse(file);
	            document.getDocumentElement().normalize();
//	            System.out.println("Root Element :" + document.getDocumentElement().getNodeName());
	            NodeList nList = document.getElementsByTagName("list");
//	            System.out.println("----------------------------");
	            for (int temp = 0; temp < nList.getLength(); temp++) {
	                Node nNode = nList.item(temp);
//	                System.out.println("\nCurrent Element :" + nNode.getNodeName());
	                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	                    Element eElement = (Element) nNode;
//	                    System.out.println("CHI Name  : " + eElement.getElementsByTagName("name").item(0).getTextContent());
//	                    System.out.println("E-Mail    : " + eElement.getElementsByTagName("email").item(0).getTextContent());
	    				String slphone = eElement.getElementsByTagName("phone").item(0).getTextContent();
//	                    System.out.println("Cellphone : " + slphone);
						OTPSend data = new OTPSend();
						data.uid = getSgSmsId();
						data.pwd = getSgSmsPInfo();
						data.da = slphone;
						data.sm = content;
						String slServerUrl = getSgSmsServerUrl();//"http://stgsmsb2c.mitake.com.tw:8001/b2c/mtk/SmSend?";
						smsResp = OTPSend.sendService(data, slServerUrl);
						showLogMessage("D", "Send SMS to TCB duty. for the AUTH system "+getSystemName()+" connection status. SMS Response= "+smsResp[0]+";"+smsResp[1]+";"+smsResp[2]);
	                }
	            }
			}
			else {
				showLogMessage("D", "Read SMS list not found. file path = ("+slFullPathTriggerFileName+")");
				return false;
			}

		} catch (Exception e) {
			showLogMessage("D", "Send SMS to duty. for AUTH system "+getSystemName()+" connect to FISC faild. SMS exception= "+e);
			blResult = false;
		}
		return blResult;
	}
    /**
     * AuthGlobalParm setConncetionStatus
     * V1.00.06 授權連線偵測異常時發送簡訊通知維護人員
     * @param 
     * @return void
     */
	public void setConncetionStatus(String connType, int chanNum, String status ) {
		if ( chanNum != getWebThreadChanNum() ) {
			if ("FISCNEG".equals(connType)) {
				getThreadConnectionNegStatusArray()[chanNum]= status;
				if ("1".equals(status)) {
					setFiscNegConnectFail(true);
				}
				else {
					setFiscNegConnectFail(false);
				}
			}
			else {
				getThreadConnectionStatusArray()[chanNum]= status;
			}
		}
	}

	public int getWebThreadChanNum() {
		return webThreadChanNum;
	}

	public boolean isExecUnitTest() {
		return execUnitTest;
	}


	public boolean isSystemError() {
		return systemError;
	}

	public void setSystemError(boolean systemError) {
		this.systemError = systemError;
	}

	public boolean isFiscSignOff() {
		return fiscSignOff;
	}

	public void setFiscSignOff(boolean fiscSignOff) {
		this.fiscSignOff = fiscSignOff;
	}
	//V1.00.06 授權連線偵測異常時發送簡訊通知維護人員
	public boolean isFiscConnectFaild() {
		return fiscConnectFaild;
	}

	public void setFiscConnectFaild(boolean fiscConnectFaild) {
		this.fiscConnectFaild = fiscConnectFaild;
	}

	public ComboPooledDataSource getgDataSource() {
		return gDataSource;
	}

	public void setgDataSource(ComboPooledDataSource gDataSource) {
		this.gDataSource = gDataSource;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getExpMethod() {
		return expMethod;
	}

	public void setExpMethod(String expMethod) {
		this.expMethod = expMethod;
	}

	public String[] getThreadConnectionNegStatusArray() {
		return threadConnectionNegStatusArray;
	}

	public void setThreadConnectionNegStatusArray(String[] threadConnectionNegStatusArray) {
		this.threadConnectionNegStatusArray = threadConnectionNegStatusArray;
	}

	public String[] getThreadConnectionStatusArray() {
		return threadConnectionStatusArray;
	}

	public void setThreadConnectionStatusArray(String[] threadConnectionStatusArray) {
		this.threadConnectionStatusArray = threadConnectionStatusArray;
	}

	public String getExceptionFlag() {
		return exceptionFlag;
	}

	public void setExceptionFlag(String exceptionFlag) {
		this.exceptionFlag = exceptionFlag;
	}

	public int getFiscPnt() {
		return fiscPnt;
	}

	public void setFiscPnt(int fiscPnt) {
		this.fiscPnt = fiscPnt;
	}

	public Object[] getDoneLock() {
		return doneLock;
	}

	public void setDoneLock(Object[] doneLock) {
		this.doneLock = doneLock;
	}

	public int getMaxFisc() {
		return maxFisc;
	}

	public String getDevMode() {
		return devMode;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public String getSystemName() {
		return systemName;
	}

	public String getTableOwner() {
		return tableOwner;
	}

	public String getFiscHost() {
		return fiscHost;
	}

	public String getTokenIms() {
		return tokenIms;
	}

	public void setTokenIms(String tokenIms) {
		this.tokenIms = tokenIms;
	}

	public String getAcerHost() {
		return acerHost;
	}

	public int getFiscSession() {
		return fiscSession;
	}

	public int getFiscNegSession() {
		return fiscNegSession;
	}

	public int getContExcept() {
		return contExcept;
	}

	public void setContExcept(int contExcept) {
		this.contExcept = contExcept;
	}

	public int getFiscPort() {
		return fiscPort;
	}

	public int getReadSocketTimeout() {
		return readSocketTimeout;
	}

	public int getInternalAuthServerPort4Online() {
		return internalAuthServerPort4Online;
	}

	public int getInternalAuthServerPort4Atm() {
		return internalAuthServerPort4Atm;
	}

	public int getInternalAuthServerPort4Fhm() {
		return internalAuthServerPort4Fhm;
	}

	public int getInternalAuthServerPort4Neg() {
		return internalAuthServerPort4Neg;
	}

	public int getFiscNegPort() {
		return fiscNegPort;
	}

	public int getFiscNegChan() {
		return fiscNegChan;
	}

	public int getFiscChan() {
		return fiscChan;
	}

	public int getAcerPort() {
		return acerPort;
	}

	public boolean isEnableLocalAuthServer4Fisc() {
		return enableLocalAuthServer4Fisc;
	}

	public boolean isEnableAcerRedemption() {
		return enableAcerRedemption;
	}

	public float getWarningSec() {
		return warningSec;
	}

	public String getSgImsPInfo() {
		return sgImsPInfo;
	}

	public String getSgImsId() {
		return sgImsId;
	}

	public String getSgImsTokenReqUrl() {
		return sgImsTokenReqUrl;
	}

	public String getSgImsVdTxnUrl() {
		return sgImsVdTxnUrl;
	}
	
	public String getsgImsIndicate() {
		return sgImsIndicate;
	}
	
	public void setsgImsIndicate(String sgImsIndicate) {
		this.sgImsIndicate = sgImsIndicate;
	}

	public String getSgSmsId() {
		return sgSmsId;
	}

	public String getSgSmsPInfo() {
		return sgSmsPInfo;
	}

	public String getSgSmsServerUrl() {
		return sgSmsServerUrl;
	}

	public String getHsmIndicate() {
		return hsmIndicate;
	}

	public void setHsmIndicate(String hsmIndicate) {
		this.hsmIndicate = hsmIndicate;
	}

	public String getIfEnableFisc() {
		return ifEnableFisc;
	}

	public String getHsmHost2() {
		return hsmHost2;
	}

	public int getHsmPort2() {
		return hsmPort2;
	}

	public String getHsmHost1() {
		return hsmHost1;
	}

	public int getHsmPort1() {
		return hsmPort1;
	}

	public String getIfReturnTrueDirectly() {
		return ifReturnTrueDirectly;
	}

	public String getIfEnableIms() {
		return ifEnableIms;
	}

	public String getIfEnableEcsTrans() {
		return ifEnableEcsTrans;
	}

	public String getIfEnableHsmVerifyCvv() {
		return ifEnableHsmVerifyCvv;
	}

	public String getIfEnableHsmVerifyArqc() {
		return ifEnableHsmVerifyArqc;
	}

	public String getIfEnableHsmVerifyACSAAV() {
		return ifEnableHsmVerifyACSAAV;
	}

	public String getIfEnableHsmVerifyPvv() {
		return ifEnableHsmVerifyPvv;
	}

	public String getIfEnableHsmTransAtmPin() {
		return ifEnableHsmTransAtmPin;
	}

	public String getIfEnableHsmGenAtmPvv() {
		return ifEnableHsmGenAtmPvv;
	}

	public HashMap<String,String> getFiscRequest() {
		return fiscRequest;
	}

	public HashMap<String,String> getFiscResponse() {
		return fiscResponse;
	}

	public String getTokenExpDate() {
		return tokenExpDate;
	}

	public void setTokenExpDate(String tokenExpDate) {
		this.tokenExpDate = tokenExpDate;
	}

	public String getSysDate() {
		return sysDate;
	}

	public java.sql.Timestamp getgTimeStamp() {
		return gTimeStamp;
	}

	public String getSysTime() {
		return sysTime;
	}

	public String getSgDbPInfo() {
		return sgDbPInfo;
	}

	public String getJulianDate() {
		return julianDate;
	}


    private static class LockInfo {
        private final Semaphore current;
        private int lockCount;

        private LockInfo(Semaphore current) {
            this.current = current;
            this.lockCount = 1;
        }
    }
    
	public String getLineAiUrl() {
		return LineAiUrl;
	}
	
	public boolean isFiscNegConnectFail() {
		return fiscNegConnectFail;
	}

	public void setFiscNegConnectFail(boolean fiscNegConnectFail) {
		this.fiscNegConnectFail = fiscNegConnectFail;
	}
}
