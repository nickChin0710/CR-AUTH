/**
 * 授權 AuhtProcess 共用物件
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
 * 2021/02/08  V1.00.00  Kevin       授權 AuhtProcess 共用物件                   *
 *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.logic.AuthLogicCancel;
import com.tcb.authProg.main.AuthGlobalParm;

public class AuthProcess {
	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm  gGb   = null;

	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate     gGate = null;

	/**
	 * 資料庫存取物件
	 */
	TableAccess     gTa   = null;

	/**
	 *交易邏輯處理物件 
	 */
	AuthLogicCancel       aulg = null;
	
}
