/**
 * 授權邏輯查核-產生授權碼處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-產生授權碼處理                     *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicGenerateAuthCode extends AuthLogic {
	
	public LogicGenerateAuthCode(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicGenerateAuthCode : started");

	}

	
	// 產生授權碼
	/**
	 * 產生授權碼
	 * @throws Exception if any exception occurred
	 */
	public void genAuthCode() throws Exception {

		String  zeros="000000",authCode="";
		int     random1=0,random2=0;

		/*
        Random     randomGen = new Random();
        random1 =  randomGen.nextInt(1000);
        random2 =  randomGen.nextInt(1000);
		 */
		random1 = HpeUtil.getRandomNumber(1000);
		random2 = HpeUtil.getRandomNumber(1000);

		authCode = (""+random1) + random2;
		if ( authCode.length() < 6 )
		{ authCode = zeros.substring(0,6-authCode.length()) + authCode; }

		if (gGate.isIdCheckOrg) {
			if ("7375".equals(gGate.mccCode)) {
				if ("0".equals(gGate.idCheckType)) {      // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 1, "A", authCode);
				}
				else if ("1".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "AA", authCode);
				}
				else if ("2".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "AB", authCode);
				}
				else if ("3".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "AC", authCode);
				}
			}
			else if ("7321".equals(gGate.mccCode)) {
				if ("0".equals(gGate.idCheckType)) {      // 0:ID+效期 4:ID+效期、手機 5:ID+效期、生日 6:ID+效期、手機、生日
					authCode = HpeUtil.replaceIndex(0, 1, "B", authCode);
				}
				else if ("4".equals(gGate.idCheckType)) { // 0:ID+效期 4:ID+效期、手機 5:ID+效期、生日 6:ID+效期、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "BA", authCode);
				}
				else if ("5".equals(gGate.idCheckType)) { // 0:ID+效期 4:ID+效期、手機 5:ID+效期、生日 6:ID+效期、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "BB", authCode);
				}
				else if ("6".equals(gGate.idCheckType)) { // 0:ID+效期 4:ID+效期、手機 5:ID+效期、生日 6:ID+效期、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "BC", authCode);
				}
			}
			else if ("6300".equals(gGate.mccCode)) {
				if ("0".equals(gGate.idCheckType)) {      // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 1, "C", authCode);
				}
				else if ("1".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "CA", authCode);
				}
				else if ("2".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "CB", authCode);
				}
				else if ("3".equals(gGate.idCheckType)) { // 0:ID 1:ID、手機 2:ID、生日 3:ID、手機、生日
					authCode = HpeUtil.replaceIndex(0, 2, "CC", authCode);
				}
			}	
		}
		gGate.isoField[38] = authCode;
		gGate.authNo = authCode;
		//randomGen = null;
		return;
	}
}
