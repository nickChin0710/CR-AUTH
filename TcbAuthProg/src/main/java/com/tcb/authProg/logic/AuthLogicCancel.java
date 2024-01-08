/**
 * 各種交易邏輯的實做 - cancel
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
 * 2021/02/08  V1.00.00  Kevin       各種交易邏輯的實做 - cancel                   *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;


import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.hsm.HsmApi;


/**
 * AuthLogic 各種交易邏輯的實做
 * 
 *
 * @author  Howard Chang
 * @version 1.0
 * @since   2017/12/19
 */
/**
 * @author changcho
 *
 */
/**
 * @author changcho
 *
 */
public class AuthLogicCancel  {


	/**
	 * 系統全域變數物件
	 */
	AuthGlobalParm  gb    = null;

	/**
	 * 單次交易變數物件
	 */
	AuthTxnGate        gGate  = null;

	/**
	 * 資料庫存取物件
	 */
	TableAccess     ta    = null;


	/**
	 * Share Memory 物件
	 */



	/**
	 * HSM API存取物件
	 */
	HsmApi gHsmUtil = null;


	/**
	 * AuthLogic contructor
	 * @param gb AuthGlobalParm
	 * @param gate AuthGate
	 * @param ta TableAccess
	 * @param shm AuthShareMemory
	 */
	public AuthLogicCancel(AuthGlobalParm gb,AuthTxnGate gate,TableAccess ta) {
		this.gb   = gb;
		this.gGate = gate;
		this.ta   = ta;
	}

} // end of class AuthLogic
