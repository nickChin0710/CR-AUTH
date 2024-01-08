/**
 * 授權邏輯查核-ISO欄位值檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-ISO欄位值檢核處理                  *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/12/11  V1.00.61  Kevin       3D交易欄位格式調整                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicCheckIsoFieldInfo extends AuthLogic {
	
	public LogicCheckIsoFieldInfo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheckIsoFieldInfo : started");

	}
	
	//
	/**
	 * ISO欄位值檢核
	 * @return ISO欄位值正確return true，否則 return false
	 * @throws Exception if any exception occurred
	 */
	public boolean checkIsoFieldInfo() throws Exception {

		//check expire date
		if (!HpeUtil.isNumberString(gGate.expireDate)) {
			ta.getAndSetErrorCode("D4");
			return false;
		}

		//check card no
		if (!HpeUtil.isNumberString(gGate.cardNo)) {
			//ta.getAndSetErrorCode("ERR01");//卡號欄位不為數字型態
			ta.getAndSetErrorCode("D1");
			gGate.isCardNotExit = true;
			return false;
		}

		//check iso field4 (Amt)
		if(!gGate.balanceInquiry) {
			//if (!HpeUtil.isNumberString(Double.toString(G_Gate.isoFiled4Value))) {
			if (!HpeUtil.isNumberString(gGate.isoField[4].trim())) {
				//ta.getAndSetErrorCode("ERR02");//BIT4金額不為數字
				ta.getAndSetErrorCode("D2");
				return false;
			}
		}

		/*檢核 NCCC特店的代碼 是否正確 ------------------------------------*/
		/*如果此筆交易來自NCCC之特店(493817為NCCC特店代碼)，*/
		/*則bit48_add_data之前10碼應該為該特店之真實代碼----------*/
		if ("493817".equals(gGate.isoField[32]) && "NCCC".equals(gGate.connType)) {
			if (gGate.isoField[48].length()>=10) {
				String slRealMerchantNo = gGate.isoField[48].substring(0, 10).trim();
				if ((!HpeUtil.isNumberString(slRealMerchantNo)) &&
						(!"01".equals(gGate.isoField[3].substring(0, 2))) ){
					//ta.getAndSetErrorCode("ERR03");//NCCC特店代碼不正確
					ta.getAndSetErrorCode("D3");
					return false;
				}
			}
		}

		//檢核 ISO8583 格式是否正確
		if (gGate.isoField[49].length()==0) {

			gGate.isoField[49] = "901";
		}
		
		//kevin:財金公司卡號欄位為必帶欄位，非過卡交易是不會送TrackII，因此有帶TrackII才檢查
		if (gGate.isoField[35].length() > 0) {
			if (gGate.isoField[35].indexOf("=")<0) {
				if (gGate.isoField[35].indexOf("D")<0) {
					ta.getAndSetErrorCode("D4");
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 依據交易類型，初始化變數值
	 * V1.00.48 P3程式碼整理
	 * V1.00.61 3D交易欄位格式調整
	 */
	public void reInitValue() throws Exception{


		if  ( "0110".equals(gGate.mesgType) 
				|| "0120".equals(gGate.mesgType) 
				|| "0130".equals(gGate.mesgType) ) {///*ATM交易*/

			if ("".equals(gGate.mccCode))  
				gGate.mccCode = "6011";


			if ("".equals(gGate.merchantNo)) {
				if (gGate.isoField[32].length()>=11)
					gGate.merchantNo = gGate.isoField[32].substring(0, 11).trim();
			}
		}
		//FISC ACS設定方式
		if (gGate.f48T42Eci.length() > 0) {
			if ("M".equals(gGate.binType)) {
				gGate.ucafInd = gGate.f48T42Eci.substring(2,3);
			}
			gGate.eci = gGate.f48T42Eci.substring(2,3);
			if (!"0".equals(gGate.eci)) {
				gGate.is3DTranx = true;
				gb.showLogMessage("D","CHECK 3D TXN = "+gGate.is3DTranx);
			}
		}

		gGate.c5TxFlag = gGate.loyaltyTxId;
		setInstallmentFlag();
		setRedeemFlag();

	}
	
}
