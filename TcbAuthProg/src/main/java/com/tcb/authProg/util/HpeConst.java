/**
 * 授權資料存取物件DAO Util（HpeConst)
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
 * 2022/03/12  V1.00.39  Kevin       db 連線異常修復                              *
 ******************************************************************************
 */

package com.tcb.authProg.util;

public class HpeConst {

	static String SHARED_DATA_SEPERATOR = "#";
	static String SHARED_DATA_CHANNEL_NAME = "SHARED_DATA";
	static String WEB_SERVICE_ERROR_CODE="99";
	static String WEB_SERVICE_SUCCESS_CODE_T24="00000";
	static String WEB_SERVICE_SUCCESS_CODE_CIMS="00";
	public static int    TABLE_COL_LIMIT = 500;
	
	static int    QR_CODE_SERVER = 1;
	static int    EASY_CARD_SERVER = 2;
	static int    ICASH_SERVER = 3;
	static int    IPASS_SERVER = 4;
	static int    FISC_SERVER = 5;
	static int    CHT_SERVER = 6;
	static int    WEB_SERVER = 7;
	static int    ECS_SERVER = 8;
	static int    IVR_SERVER = 9;
}
