/**
 * 授權邏輯查核-OEMPAY交易檢核處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-OEMPAY交易檢核處理                 *
 * 2021/11/23  V1.00.01  Kevin       新增非購貨交易不須檢查OEMPAY卡片狀態             *
 * 2022/12/01  V1.00.27  Kevin       OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD  *
 * 2023/04/18  V1.00.43  Kevin       OEMPAY Token國外交易之管控參數:                *
 *                                   綁定之後72小時只能刷8,000元的國外交易              *
 * 2023/06/05  V1.00.47  Kevin       OEMPAY Token國外交易之管控參數:欄位型態改為long    *                                 
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicProcOempayChk extends AuthLogic {
	
	public LogicProcOempayChk(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcOempayChk : started");

	}
	
	/* OEMPAY check*/
	/**
	 * handle OEMPAY card 交易的處理流程與檢核
	 * V1.00.27 OEMPAY虛擬卡片不存在時，新增一筆資料到OEMPAY_CARD
	 * V1.00.43 OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
	 * V1.00.47 OEMPAY Token國外交易之管控參數:欄位型態改為long
	 * @return 如果正常處理完成return true，否則return false
	 * @throws Exception if any exception
	 */
	public boolean processOempayChk() throws Exception {
		//kevin:查詢Oempay TPAN status code
		if (gGate.tokenS8AcctNum.length() > 0) {
			if (ta.selectOempayCard()) {
				gGate.ifHaveOempayInfo = true;
				//V1.00.01 新增非購貨交易不須檢查OEMPAY卡片狀態
				if (!ta.getValue("OempayCardStatusCode").equals("0")) {
					if (!gGate.nonPurchaseTxn) {
						ta.getAndSetErrorCode("1L");//OEMPAY TPAN status verify error
						return false;
					}
					else {
						gb.showLogMessage("D","Oempay Card none purchase and Status Code = "+ta.getValue("OempayCardStatusCode"));
					}
				}
			}
			else {
				gGate.ifHaveOempayInfo = false;
				if (!gGate.nonPurchaseTxn) {
					gb.showLogMessage("D","Oempay Card not found. Error code = (1M). OEMPAY_CARD add New TPAN = "+gGate.tokenS8AcctNum);
					ta.insertOempayCard("0");
				}
			}
			if (gGate.f48T71.length()>0) {
				int lenTot = 0;
				int lenSub = 0;
				String checkTag = "";
				while (lenTot < gGate.f48T71.length()) {
					checkTag = gGate.f48T71.substring(lenTot, lenTot + 2);
					lenTot = lenTot + 2;
					lenSub = 2;
					if ("50".equals(checkTag)) {
						gGate.tpanFraudChk = gGate.f48T71.substring(lenTot, lenTot+1);
					}
					else if ("51".equals(checkTag)) {
						gGate.tpanAcResult = gGate.f48T71.substring(lenTot, lenTot+1);
					}
					else if ("61".equals(checkTag)) {
						gGate.tpanPinResult = gGate.f48T71.substring(lenTot, lenTot+1);
					}
					lenTot = lenTot+lenSub;
				}
				if ("I".equals(gGate.tpanAcResult)) {
					ta.getAndSetErrorCode("1N");//OEMPAY TPAN AC CHECK ERROR
					return false;
				}			
			}
		}
		if ("F".equals(gGate.areaType) && (gGate.isTokenVAUT || gGate.isTokenMAUT) && gGate.ifHaveOempayInfo && "07".equals(gGate.entryMode)) {
			if (ta.selectPrdTypeIntrOempay() && !ta.getValue("OempayCredCreateDate").isEmpty() && !ta.getValue("OempayCredCreateTime").isEmpty()) {
				gb.showLogMessage("D","Oempay Oversea txn limit. Hour = "+ta.getInteger("INT_LMT_CNT_MON")+"Amount = "+ta.getInteger("INT_TOT_LMT_CNT_DAY"));
				long hour2millisecond = ta.getLong("INT_LMT_CNT_MON")*60*60*1000;
				gb.showLogMessage("D","Oempay Oversea txn limit. DATE = "+ta.getValue("OempayCredCreateDate")+"TIME = "+ta.getValue("OempayCredCreateTime"));
				Date createDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(ta.getValue("OempayCredCreateDate")+ta.getValue("OempayCredCreateTime"));
				Date afterDate = new Date(createDate .getTime() + hour2millisecond);//72小後的時間
				if (afterDate.after(gb.getgTimeStamp())) {
					if (!ta.selectCcaAuthTxLog4OempayOversea(new Timestamp(createDate.getTime()))) {
						if (gGate.isAuthVip) {
							gb.showLogMessage("D","Auth Vip bypass OEMPAY Oversea txn Limit($8000/72 hours) of reject");							
						}
						else {
							ta.getAndSetErrorCode("1Q");//OEMPAY Token國外交易之管控參數: 綁定之後72小時只能刷8,000元的國外交易
							return false;
						}
					}
				}
			}
		}
		return true;
	}

}
