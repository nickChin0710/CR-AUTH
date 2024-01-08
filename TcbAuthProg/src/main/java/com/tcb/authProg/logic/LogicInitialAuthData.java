/**
 * 授權邏輯查核-授權資料初始值處理
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
 * DATE       Version   AUTHOR      DESCRIPTION                               *
 * ---------  --------  ---------- ------------------------------------------ *
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-M Code帳齡檢核處理                 *
 * 2021/03/22  V1.00.01  Kevin       經常性身分驗證交易不須檢查到期日                  *
 * 2021/03/28  V1.00.02  Kevin       STANDIN交易判斷改由LogicInitialAuht處理。     *
 * 2021/11/24  V1.00.03  Kevin       處理billing amount未帶currency rate        *
 * 2022/02/24  V1.00.04  Kevin       處理Country code改由bin type來判斷           *
 * 2022/03/21  V1.00.05  Kevin       處理Country code判斷邏輯                    *
 * 2022/03/31  V1.00.06  Kevin       雙幣卡清算金額不用四捨五入                      *
 * 2022/04/07  V1.00.07  Kevin       支援人工授權可輸入外幣交易及雙幣卡交易             *
 * 2022/04/11  V1.00.08  Kevin       解決自助加油預先授權走一般授權交易                *
 * 2022/09/07  V1.00.09  Kevin       預先授權完成，不需要帶入原交易金額                *
 * 2022/12/24  V1.00.31  Kevin       處理本行國內QR Code掃碼交易帶入158改為TW         *
 * 2023/04/18  V1.00.43  Kevin       OEMPAY Token國外交易之管控參數:                *
 *                                   綁定之後72小時只能刷8,000元的國外交易              *
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 * 2023/11/24  V1.00.59  Kevin       修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能 *
 * 2023/12/18  V1.00.63  Kevin       VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯 *
 * 									 費」：2440一般圈存一律帶入ID傳送到IMS             *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicInitialAuthData extends AuthLogic {
	
	public LogicInitialAuthData(AuthGlobalParm gb,AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate  = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicInitialAuthData : started");

	}

	// // 處理授權資料初始值
	/**
	 * V1.00.31 處理本行國內QR Code掃碼交易帶入158改為TW
	 * V1.00.38 P3授權額度查核調整-ROLLBACK P2 => Y(表示系統還原至P2階段)
	 * V1.00.59 修正分期資訊欄位位數錯誤的問題與新增強制關閉紅利與分期功能
	 * V1.00.63 VD註記交易屬於「稅款、罰金、罰鍰、滯納金、水費、電費、瓦斯費」：2440一般圈存一律帶入ID傳送到IMS
	 * 初始化授權交易變數值
	 * @throws Exception if any exception occurred 
	 */
	public void initialAuthData() throws Exception {

		gb.showLogMessage("I","initialAuthData : started");

		gGate.rollbackP2 = false; //V1.00.38 P3授權額度查核調整
		if (ta.selectPtrSysParm("SYSPARM", "ROLLBACK_P2")) {
			if ("Y".equals(ta.getValue("SysValue1"))) {
				gGate.rollbackP2 = true;		
				gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(Y) =>授權邏輯-第二階段啟用中");
			}
		}
		if (!gGate.rollbackP2) {
			gGate.instSpecAddOnDate = ta.getValue("SysValue3");
			gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(N) =>授權邏輯-第三階段啟用中 ; 專款專用分期補期金的日期 = "+gGate.instSpecAddOnDate);
			if ("Y".equals(ta.getValue("SysValue4"))) {
				gGate.rejectBonus = true;
				gGate.rejectInstallment = true;
				gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(N) =>授權邏輯-第三階段啟用中 ; Reject ALL ECS BOUNUS & INSTALLMENT Transactions Flag = "+ta.getValue("SysValue4"));
			}
			else if ("B".equals(ta.getValue("SysValue4"))) {
				gGate.rejectBonus = true;
				gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(N) =>授權邏輯-第三階段啟用中 ; Reject ALL ECS BOUNUS  Transactions Flag = "+ta.getValue("SysValue4"));

			}
			else if ("I".equals(ta.getValue("SysValue4"))) {
				gGate.rejectInstallment = true;
				gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(N) =>授權邏輯-第三階段啟用中 ; Reject ALL NCCC INSTALLMENT Transactions Flag = "+ta.getValue("SysValue4"));
			}
		}
		else {
			if ("A".equals(ta.getValue("SysValue4"))) {
				gGate.rollbackP2RejectAcer = true;
				gb.showLogMessage("D","SYSPARM + ROLLBAC_P2(Y) =>授權邏輯-第二階段啟用中 ; ACER紅利折抵交易強制暫停註記 = "+gGate.rollbackP2RejectAcer);
			}
		}

		gGate.mccCode    = gGate.isoField[18].trim();
		getEdcTradeFunctionCode();

		//down, process merchantNo (財金特店代號固定DE42)
		if ("493817".equals(gGate.isoField[32]) && "NCCC".equals(gGate.connType) ) {
			if (gGate.isoField[48].length()>=19) {
				gGate.merchantNo = gGate.isoField[48].substring(0, 19).trim();//NCCC
			}
		}
		else {
			gGate.merchantNo = gGate.isoField[42].trim(); //

			if ("".equals(gGate.merchantNo)) { 
				gGate.merchantNo = gGate.isoField[32].trim();
				//G_Gate.merchantNo = G_Gate.isoField[32].substring(0, 11).trim();
			}
		}
		//up, process merchantNo

		//V1.00.08 解決自助加油預先授權走一般授權交易
		if (("0100".equals(gGate.mesgType)) || ("0120".equals(gGate.mesgType)) || ("0121".equals(gGate.mesgType))) {
			if ("FISC".equals(gGate.connType) && gGate.isoField[61].length() > 0) {
				if ("4".equals(gGate.isoField[61].substring(6, 7))  || ("00".equals(gGate.isoField[25]) && "5542".equals(gGate.mccCode))) {
					gGate.preAuth = true;
				}
				//V1.00.01-經常性身分驗證交易不須檢查到期日
				if ("4".equals(gGate.isoField[61].substring(3, 4))) {
					gGate.recurringTrans = true;
				}
			}
			else {
				if (("06".equals(gGate.isoField[25])) || ("00".equals(gGate.isoField[25]))&& ("5542".equals(gGate.mccCode))) {
					gGate.preAuth = true;
				}
			}
		}

		if ( "0420".equals(gGate.mesgType) || "0421".equals(gGate.mesgType) ){ //沖銷交易 
			gGate.reversalTrans = true; 
			if ("FISC".equals(gGate.connType)) {
				gGate.oriTraceNo = gGate.isoField[90].substring(4,10);
			}
		}
		//V1.00.02 - STANDIN交易判斷改由LogicInitialAuht處理。
		//kevin:交易補登與代行共用isForcePosting - 取消共用
		if (("022".equals(gGate.mesgType.substring(0, 3))) || ("012".equals(gGate.mesgType.substring(0, 3))) ) {	   
			//kevin:交易補登isForcePosting
			if (isForcePosting()) {////判斷是否為補登
//				if ( (gGate.isDebitCard) && ("5542".equals(gGate.mccCode)) ){
//					//轉為 preAuthComp 交易
//					gGate.preAuthComp = true;    /*預先授權 完成  */
//				}
//				else 
					gGate.forcePosting = true; 
			}
			else {
				gGate.ifStandIn = true;
			}
		}
		//kevin:財金公司預先授權完成判斷 並且修改0220->0120
		//V1.00.08 解決自助加油預先授權走一般授權交易
		if ("FISC".equals(gGate.connType) && gGate.isoField[61].length() > 0 && "0120".equals(gGate.mesgType) && "191 ".equals(gGate.f58T66)) {
//			if ((gGate.isoField[61].substring(6, 7).equals("0")) && (!"".equals(gGate.posConditionCode) && gGate.posConditionCode.substring(4, 5).equals("1"))) 
				gGate.preAuthComp = true;    /*財金預先授權 完成  */
		}
		else {
			if (("0120".equals(gGate.mesgType)) && ("06".equals(gGate.isoField[25]))) 
				gGate.preAuthComp = true;    /*預先授權 完成  */
		}
			
		if ((gGate.preAuthComp) && (gGate.forcePosting)&& 
				("5542".equals(gGate.mccCode))) {////如pre-auth-comp 同時又是授權補登的, 且mcc_code是自動加油才去修改授權補登 = false

			gGate.forcePosting = false;
		}

		gGate.traceNo  = gGate.isoField[11];
		
		//kevin:財金公司只有過卡交易才會帶trackII，所以在判斷前先確保卡號到期日都已經取得
		if (gGate.isoField[2].length() > 0) {
			gGate.cardNo = gGate.isoField[2];
		}
		if (gGate.isoField[14].length() > 0) {
			gGate.expireDate = gGate.isoField[14]; 
		}
		
		if ( gGate.isoField[35].length() >= 18 ) {

			String[] cvtcard = parseTrack2(gGate.isoField[35]);
			gGate.cardNo      = cvtcard[0];
			gGate.expireDate  = cvtcard[1].substring(0,4); //Howard : AE
			if ( cvtcard[1].length() >= 15 ) {

				gGate.servCode = cvtcard[1].substring(4,7);
				gGate.pvki     = cvtcard[1].substring(7,8);
				gGate.pvv      = cvtcard[1].substring(8,12);
				gGate.cvv      = cvtcard[1].substring(12,15);
			}
		}

		if ( gGate.cardNo.length() > 0 ) {
			gGate.binNo = gGate.cardNo.substring(0,6);
			gGate.sgCardProd = gGate.cardNo.substring(0,10);
			gGate.sgUsedCardProd = gGate.sgCardProd;
		}

		if ( gGate.isoField[22].length() > 0 ) {

			String checkCode = gGate.isoField[22].substring(0,2);
			if ( "05".equals(checkCode) || "07".equals(checkCode) )
			{ gGate.emvTrans = true; }
		}

		if ( gGate.isoField[43].length() >= 40 ) {

			if ("FISC".equals(gGate.connType)) {
				//kevin:語音開卡特別處理
				if ("CVR".equals(gGate.isoField[43].substring(0,3)) || "AVR".equals(gGate.isoField[43].substring(0,3)) || "   ".equals(gGate.isoField[43].substring(38,40))) {
					gGate.merchantCountry = "TW";
				}
				else {
					if (gGate.isoField[43].substring(37,38).matches("[A-Z]+")) {
						//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(特店國家碼修正)
						if ("4".equals(gGate.cardNo.substring(0,1))) {
							if ("TWN".equals(gGate.isoField[43].substring(0,3))) {
								gGate.merchantCountry = "TW";
							}
							else {
								gGate.merchantCountry = gGate.isoField[43].substring(38,40);
							}
						}
						else {
							gGate.merchantCountry = gGate.isoField[43].substring(37,40);
						}
					}
					else {
						if ("4".equals(gGate.cardNo.substring(0,1)) && gGate.isoField[43].substring(38,39).matches("[A-Z]+")) {
							gGate.merchantCountry = gGate.isoField[43].substring(38,40);
						}
						else {
							gGate.merchantCountry = "TW";
						}
					}
				}
			}
			else {		
				//isofield43="NATIONAL CREDIT CARD  TAIPEI       TW TW"
				gGate.merchantCityName = gGate.isoField[43].substring(22,35); //TAIPEI
				gGate.merchantCity = gGate.isoField[43].substring(35,38); //TW
				gGate.merchantCountry = gGate.isoField[43].substring(38,40);
			}
		}
		else {
			gGate.merchantCountry = "TW";
		}
		
		if ( gGate.isoField[43].length() >=22 ) {
			gGate.merchantName =  gGate.isoField[43].substring(0,22);
		}

		if (gGate.isoField[22].length()>=2) {
			gGate.entryMode  = gGate.isoField[22].trim().substring(0,2);
		}
		else {
			gGate.entryMode = "90";
		}

		gGate.entryModeType = ta.getEntryModeType(gGate.entryMode);
		gGate.terminalNo = gGate.isoField[41].trim();

		gGate.pinBlock   = gGate.isoField[52];

		if ( gGate.isoField[95].length() >= 12 ) {

			gGate.adjustAmount = Double.parseDouble(gGate.isoField[95].substring(0,12)) / 100;

			if ( gGate.adjustAmount == 0 ) {
				gGate.cancelTrans = true; 
			}
			if ( gGate.adjustAmount > gGate.isoFiled4Value ) {
				gGate.tipsTrans   = true; 
			}
		}

		//kevin:取消service4BatchAuth，改為單筆交易來源管道決定
		if (("0200".equals(gGate.mesgType)) && ("BATCH".equals(gGate.connType)) ) {
			gGate.ecsBatchCode="1"; /* 1:公用 2: 郵購 */
			if( ("800030".equals(gGate.isoField[3])) || ("010030".equals(gGate.isoField[3])) )
				gGate.ecsBatchCode="2"; /* 1:公用 2: 郵購 */
		}
		
		//kevin:雙幣卡資訊
		if (gGate.f58T31.length() == 42 ) {
			gGate.dualAmt4Twd = Double.parseDouble((String) gGate.f58T31.subSequence(0, 12)) / 100;
			gGate.dualRate4OriToUsd = Double.parseDouble((String) gGate.f58T31.subSequence(12, 27));
			gGate.dualRate4UsdToTwd = Double.parseDouble((String) gGate.f58T31.subSequence(27, 42));

			gb.showLogMessage("D","雙幣卡交易匯率轉換資訊：台幣金額 = "+gGate.dualAmt4Twd + "外幣對美金匯率 = " + gGate.dualRate4OriToUsd + "美金對台幣匯率 = " + gGate.dualRate4UsdToTwd);

		}
		else {
			gGate.dualRate4UsdToTwd = gGate.exchangRate;
		}
		
		//V1.00.09 預先授權完成，不需要帶入原交易金額
		if ("WEB".equals(gGate.connType) ) {
			if (gGate.isoField[6].length() > 0 && !"901".equals(gGate.isoField[51])) {
				gGate.dualAmt4Twd     = Double.parseDouble(gGate.isoField[5]) / 100;
				gGate.isoFiled4Value  = Math.round(gGate.dualAmt4Twd);
				gb.showLogMessage("D","WEB billing amount no currency rate isoFiled4Value ="+gGate.isoFiled4Value);
				gGate.dualCurr4Bill   = gGate.isoField[51];
				gGate.dualAmt4Bill    = Double.parseDouble(gGate.isoField[6]) / 100;
				gb.showLogMessage("D","WEB dualAmt4Bill = " + gGate.dualAmt4Bill + "dualCurr4Bill = " + gGate.dualCurr4Bill);
			}
			else {
				if (!"901".equals(gGate.isoField[49])) {
					gGate.isoFiled4Value  = Double.parseDouble(gGate.isoField[5]) / 100;
					gb.showLogMessage("D","WEB isoField[49] NOT= 901 , isoFiled4Value = " + gGate.isoFiled4Value);
				}
				else {
					gGate.isoFiled4Value  = Double.parseDouble(gGate.isoField[4]) / 100;
					gb.showLogMessage("D","WEB isoField[49] = 901 , isoFiled4Value ="+gGate.isoFiled4Value);
				}
			}		
		}		
		else {
			if ((gGate.isoField[6].length() > 0 ) && gGate.isoField[51].length() > 0) {
				if (HpeUtil.isAmount(gGate.isoField[6])) {
					if ("901".equals(gGate.isoField[51])) {
						gGate.isoFiled4Value  = Math.round(Double.parseDouble(gGate.isoField[6]) / 100);
						if (gGate.isoField[10].length() > 0 ) {
							int ilDecimal =  Integer.parseInt((String) gGate.isoField[10].subSequence(0, 1));
							double dlRate =  Double.parseDouble((String) gGate.isoField[10].subSequence(1, 8));
							gGate.exchangRate = HpeUtil.decimalRate(ilDecimal, dlRate);
							gGate.dualRate4UsdToTwd = gGate.exchangRate;
							gb.showLogMessage("D","FISC isoField[6] = " + gGate.isoField[6] + "Exchange Rate = " + gGate.exchangRate);
						}
					}
					else {
						if ("901".equals(gGate.isoField[49])) {
							gGate.isoFiled4Value  = Double.parseDouble(gGate.isoField[4]) / 100;
						}
						else {
							gGate.isoFiled4Value  = Math.round(gGate.dualAmt4Twd);
						}
						gGate.dualCurr4Bill   = gGate.isoField[51];
						gGate.dualAmt4Bill    = Double.parseDouble(gGate.isoField[6]) / 100;
						gb.showLogMessage("D","FISC dualAmt4Bill = " + gGate.dualAmt4Bill + "dualCurr4Bill = " + gGate.dualCurr4Bill);
					}
				}
				else {
					gGate.isoFiled4Value = 0;
					gGate.convertError = true;
					ta.getAndSetErrorCode("D2"); //金額錯誤
				}
			}
			else if ( gGate.isoField[4].length() > 0  ) {
				if (HpeUtil.isAmount(gGate.isoField[4])) {
					gGate.isoFiled4Value  = Double.parseDouble(gGate.isoField[4]) / 100;
				}
				else {
					gGate.isoFiled4Value = 0;
					gGate.convertError = true;
					ta.getAndSetErrorCode("D2"); //金額錯誤
				}
			}
		}

		gGate.refNo = gGate.isoField[37].trim();
		gGate.oriAuthNo = gGate.isoField[38].trim();
		gGate.oriRespCode = gGate.isoField[39].trim();
		gGate.isoField[38]="";

		gGate.transCnt = 1;

		if (gGate.reversalTrans) {
			gGate.reversalFlag = "N";//--N.未沖正交易,Y.已沖正交易(沖正成功)
			gGate.debtFlag="Y";      //--N.未請款比對, Y.己請款比對
			gGate.unlockFlag = "R";  //--R.沖正交易, N.未解圈, Y.己解圈
			updatePreAuthData("N");
		}
		else {
			gGate.reversalFlag = "N";//--N.未沖正交易,Y.已沖正交易
			gGate.debtFlag="N";     //--N.未請款比對, Y.己請款比對
			gGate.unlockFlag="N";   //--N.未解圈, Y.己解圈
			updateCacuData("Y", "N");
		}

		gGate.txDate = gb.getSysDate();
		gGate.txTime = HpeUtil.getCurTimeStr();

		setAuthUnit();
		
		return;
	}
	
	/**
	 * 取得 EDC 交易功能碼
	 * @throws Exception if any exception occurred
	 */
	private void getEdcTradeFunctionCode() throws Exception{
		int nlLeng= gGate.isoField[127].length();
		if (nlLeng >= 40) {
			gGate.edcTradeFunctionCode =   gGate.isoField[127].substring(38, 40); //S127. 42~43 bytes //EDC 交易功能碼
		}
	}
	
	/**
	 * 判斷是否為補登交易
	 * @return 如果是補登交易則return true, 否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean isForcePosting() throws Exception{
		boolean blResult = false;
		//kevin:新增代行授權通知交易
		if ("FISC".equals(gGate.connType)) {
			//授權通知來源授權通知來源-950051 POS機離線授權，歸類為授權補登
      		if ( "950051".equals(gGate.f58T63))
      			blResult = true;
		}
      	else {
      		if ("31".equals(gGate.edcTradeFunctionCode))
      			blResult = true;
      	}
		return blResult;
	}
	
	/**
	 * Split Track2
	 * @return Track2 Data
	 * @throws Exception if any exception occurred
	 */
	private String[] parseTrack2(String spTrack2Data) {
		String[] lResult = null;
		if (spTrack2Data.indexOf("=")>0)
			lResult = spTrack2Data.split("="); //G_Gate.isoField[35] is "4xxxxxxxxxxxxxx8=181270111234123"
		else
			lResult = spTrack2Data.split("D"); //G_Gate.isoField[35] is "5xxxxxxxxxxxxxx0D24072012670064500000"
		
		return lResult;
	}
	
	/**
	 * 取得Auth Unit
	 * @throws Exception if any exception occurred
	 */
	private void setAuthUnit() {
		if (gGate.ifStandIn) {
//			if ((!gGate.refund) && (!gGate.purchAdjust) &&(!gGate.refundAdjust)
//				&& (!gGate.cashAdjust) && (!gGate.forcePosting) ){
//				if ("66666000001".equals(gGate.merchantNo))
//					gGate.authUnit = "I";
//			}
//			else if ("6666600000".equals(gGate.merchantNo))
//				gGate.authUnit = "T";
//			else if (((gGate.isoField[127].length()>=74)) && ("MP".equals(((gGate.isoField[127].substring(72, 74))))))
//				gGate.authUnit = "MP";
//			else if (("37".equals(gGate.cardNo.substring(0,2))))
//				gGate.authUnit = "a";
			if ( "950013".equals(gGate.f58T63)) //財金代行
				gGate.authUnit = "F";
			else if ("MP".equals(gGate.f58T69SpecialTxn)) //NCCC小額交易
				gGate.authUnit = "MP";
			else if (gGate.easyStandIn)  //悠遊卡代行加值交易
				gGate.authUnit = "T";
			else if (gGate.ipassStandIn) //一卡通代行加值交易
				gGate.authUnit = "I";
			else if (gGate.icashStandIn) //愛金卡代行加值交易
				gGate.authUnit = "H";
			else if (gGate.preAuthComp)  //預先授權完成是屬於收單代行通知
				gGate.authUnit = "C";
			else if (("3".equals(gGate.cardNo.substring(0,1)))) //JCB代行交易
				gGate.authUnit = "J";
			else if (("4".equals(gGate.cardNo.substring(0,1)))) //VISA代行交易
				gGate.authUnit = "V";
			else if (("5".equals(gGate.cardNo.substring(0,1)))) //MasterCard代行交易
				gGate.authUnit = "M";
		}
	}
}
