/**
 * 授權邏輯查核-3D交易驗證處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-3D交易驗證處理                     *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicCheck3dTransInfo extends AuthLogic {
	
	public LogicCheck3dTransInfo(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicCheck3dTransInfo : started");

	}
	
	/**
	 * 檢核NCCC送來的3d交易驗證結果
	 * @return 如果3d交易驗證成功，return true，否則return false
	 * @throws Exception if any exception ccurred
	 */
	//kevin:合庫自驗CAVV，所以不須查驗結果
	public boolean check3dTransInfo() throws Exception {
		
		boolean blResult = true;
		if ("FISC".equals(gGate.connType)) {
			return blResult;
		}
		String slCavvResultFromNCCC = gGate.cavvResult.trim(); 
		gb.showLogMessage("D","check3dTransInfo() cavvResult="+slCavvResultFromNCCC);
		if (!"".equals(slCavvResultFromNCCC)) {
			if ((" ").equals(slCavvResultFromNCCC))
				blResult=true; //不用驗CAVV => ECS 正常進行授權流程
			else if (("0").equals(slCavvResultFromNCCC))
				blResult=false; //未帶CAVV/AAV
			else if (("1").equals(slCavvResultFromNCCC))
				blResult=false; //查核CAVV/AAV失敗
			else if (("2").equals(slCavvResultFromNCCC)) {
				blResult=true; //驗CAVV 通過 => ECS 正常進行授權流程
				gGate.is3DTranx = true;
			}
			else if (("3").equals(slCavvResultFromNCCC))
				blResult=false; //無法進行CAVV驗證
			else if (("4").equals(slCavvResultFromNCCC))
				blResult=false; //系統問題無法進行CAVV驗證
			else if (("5").equals(slCavvResultFromNCCC))
				blResult=false; //收單行加入3D,但發卡行未加入3D
			else if (("6").equals(slCavvResultFromNCCC))
				blResult=false; //發卡行 BIN 未加入3D
			else if (("7").equals(slCavvResultFromNCCC))
				blResult=false; //CAVV重覆
			else 
				blResult=false; //填錯data
		}
		return blResult;
	}
}
