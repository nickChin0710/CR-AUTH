/**
 * 處理SMS簡訊接收errorCode作業 
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
 * 2021/02/08  V1.00.00  Kevin       處理SMS簡訊接收errorCode作業                 *
 * 2021/02/08  V1.00.01  Tanwei       updated for project coding standard     *  
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.sms;

public class OTPReceive {
		public String getRowId() {
		return rowId;
		
	}
	public void setRowId(String rowId) {
		rowId = rowId;
	}
	public String getCnt() {
		return cnt;
	}
	public void setCnt(String cnt) {
		cnt = cnt;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		errorCode = errorCode;
	}
	/*
		 發送結果說明：
 			0簡訊已發至SMS server
 			1傳入參數有誤
 			2帳號/密碼錯誤
			3電話號碼格式錯誤
 			4帳號已遭暫停使用
 			7預約時間錯誤
 			9簡訊內容為空白
			10資料庫存取或系統錯誤
			11餘額已為0
			12超過長簡訊發送字數
			13電話號碼為黑名單
			14僅接受POST method
			15指定發送代碼無效
			16發送截止時間錯誤
			19查無資料

		 */
	public String rowId;
	public String cnt;
	public String errorCode;
	
}
