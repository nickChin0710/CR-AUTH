/**
 * 授權邏輯查核-M Code帳齡檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-M Code帳齡檢核處理                 *
 * 2022/02/17  V1.00.01  Kevin       採購卡邏輯變更，視為一般商務卡                   *
 * 2023/06/01  V1.00.46  Kevin       P3批次授權比照一般授權的邏輯，不須特別排除            *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheckMcode extends AuthLogic {

	public LogicCheckMcode(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckMcode : started");

	}
	
	
	/**
	 * 查核 M Code
	 * V1.00.46 P3批次授權比照一般授權的邏輯，不須特別排除
	 * @return 如果M Code查核成功return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	public boolean mCodeCheck() throws Exception {

		if (ifIgnoreProcess3())   //特定交易不做檢核
			return true;
		//V1.00.01 採購卡邏輯變更，視為一般商務卡 
//		if (gGate.isPurchaseCard) { //採購卡不做檢核
//			return true;
//		}

		//是否檢核卡戶判斷(DELINQUENT)
		if ("0".equals(ta.getValue("AuthParmDelinquent"))) {
			gGate.lowTradeCheck = true;
			return true;
		}

		if (isSpecialAcct()) { //
			ta.getAndSetErrorCode("QH");			
			return false;
		}

		String slMCode = gGate.mCode;
		if (slMCode.length()==2)
			slMCode = "M" +slMCode;
		if (("M00".equals(slMCode))  || 
				(" ".equals(slMCode)) ){  //這個if的寫法跟proc不一樣。 proc => if M code = M0 or space , goto CCAS_credit_check
			gGate.lowTradeCheck = true;
			return true;
		}

		//總未付
		double dlCurTotUnpaid = gGate.curTotalUnpaidOfPersonal; 
		
		double dlM1Amt = 0, dlM2Amt = 0;
		//down, Check Mn from SYS_PARM2-M1 parameter*/

		if (ta.selectSysParm2("MPARM", "M1")) {
			String slMCodeOfSysParm2 = ta.getValue("SysParm2Data1");
			dlM1Amt = ta.getDouble("SysParm2Data2");

			if (slMCode.compareTo(slMCodeOfSysParm2)>0) {/*MCODE >= Mn 定義於 M1 Code*/
				if (dlCurTotUnpaid>=dlM1Amt) { /*總未付 >= Mn 定義於 M1 Code 之 M1金額*/
					ta.getAndSetErrorCode(ta.getValue("SysParm2Data3"));
					return false;
				}
			}

		}

		//down, Check Mn from SYS_PARM2-M2 parameter*/
		if (ta.selectSysParm2("MPARM", "M2")) {
			dlM2Amt = ta.getDouble("SysParm2Data2");

			if (dlCurTotUnpaid>dlM2Amt) { /* 總未付 > 定義於 M2 之 M2金額 */
				ta.getAndSetErrorCode(ta.getValue("SysParm2Data3"));
				return false;
			}
		}

		return true;
	}
	
	/**
	 * 判定是否為催呆戶
	 * @return 如果是催呆戶 return true，否則return false
	 * @throws Exception 
	 */
	private boolean isSpecialAcct() throws Exception {
		//判定是否為催呆戶
		String slActAcNoAcctStatus = ta.getValue("ActAcNoAcctStatus");
		boolean blResult= false;
		//kevin:結清戶屬於催呆的一種
		if  (("3".equals(slActAcNoAcctStatus))  ||  //1:正常 2:逾放 3.催收 4.呆帳 5.結清(Write Off) 
				("4".equals(slActAcNoAcctStatus)) || ("5".equals(slActAcNoAcctStatus))){
			blResult = true;

			gb.showLogMessage("D","ActAcNoAcctStatus=>" + slActAcNoAcctStatus+ "，為催呆戶。");

		}
		return blResult;
	}
}
