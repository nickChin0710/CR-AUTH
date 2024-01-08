/**
 * 授權邏輯查核-交易類別及種類處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-交易類別及種類處理                  *
 * 2021/03/28  V1.00.01  Kevin       MAUT、VAUT屬於授權交易，非屬於代碼化交易處理。     *
 * 2021/03/28  V1.00.02  Kevin       STANDIN交易判斷改由LogicInitialAuht處理。     *
 * 2021/11/16  V1.00.03  Kevin       VISA VTAR代碼化驗證訊息調整。                 *
 * 2021/12/24  V1.00.04  Kevin       針對POS ENTRY MODE 91，一律拒絕交易           *
 * 2022/03/21  V1.00.05  Kevin       櫃台預借未帶PIN時，不驗PVV                    * 
 * 2022/04/11  V1.00.06  Kevin       授權補登交易取消設定，同預先授權完成交易            *                                 
 * 2022/05/03  V1.00.07  Kevin       調整語音開卡判斷邏輯                          *
 * 2022/05/04  V1.00.08  Kevin       ATM預借現金密碼變更功能開發                    *
 * 2022/05/26  V1.00.09  Kevin       交易類別28xxxx屬於Payment Transaction(PY)   *
 *                                   一律拒絕交易。                               *
 * 2022/08/16  V1.00.10  Kevin       修正票證交易日累計自動加值交易金額及次數處理與沖正問題  *
 * 2022/09/21  V1.00.11  Kevin       以沖正交易成功與否作為判斷條件，並忽略的原始回覆碼的判斷*
 * 2023/02/14  V1.00.38  Kevin       P3授權額度查核調整                            *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/09/13  V1.00.52  Kevin       OEMPAY綁定成功後發送通知簡訊和格式整理             *
 * 2023/10/12  V1.00.54  Kevin       OEMPAY綁定Mastercard Token成功通知僅限行動裝置  *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;

public class LogicSetTransType extends AuthLogic {
	
	public LogicSetTransType(AuthGlobalParm gb,AuthTxnGate gate) {
		this.gb    = gb;
		this.gGate  = gate;
		
		gb.showLogMessage("I","LogicSetTransType : started");

	}
	
	//// 處理交易類別 及 交易種類
	/**
	 * 判斷是哪一種交易類別/交易種類
	 * V1.00.38 P3授權額度查核調整
	 * V1.00.48 P3程式碼整理
	 * V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理
	 * V1.00.54 OEMPAY綁定Mastercard Token成功通知僅限行動裝置
	 * @throws Exception if any exception occurred
	 */
	public void setTransType() throws Exception {

		gb.showLogMessage("I","setTransType : started");

		if (gGate.isoField[3].length() > 0) {
			gGate.txType       = gGate.isoField[3].substring(0,2);
			gGate.fromAcctType = gGate.isoField[3].substring(2,4);
			gGate.toAcctType   = gGate.isoField[3].substring(4,6);
		}
		//V1.00.02 - STANDIN交易判斷改由LogicInitialAuht處理。
		//STANDIN
//		if (("022".equals(gGate.mesgType.substring(0, 3))) || ("012".equals(gGate.mesgType.substring(0, 3))) ) {	   
//			gGate.ifStandIn = true;
//		}
		//V1.00.10 修正票證交易日累計自動加值交易金額及次數處理與沖正問題
		if ((!gGate.forcePosting ) && (!gGate.preAuth ) && (!gGate.preAuthComp ) && (!gGate.reversalTrans)) {     /* not 授權補登 也不是 preAuth 也不是 preAuthComp,也不是reversalTrans  Howard 加的 code*/
			if ("WEB".equals(gGate.connType)) {
				int i = Integer.parseInt(gGate.txType);
	
				switch (i) {
				case  0  : gGate.normalPurch    = true;  break; /* 一般交易*/
				case  1  : gGate.cashAdvance    = true;  break; /* 預借現金*/
				case  2  : gGate.purchAdjust    = true;  break; /* 一般交易調整 */
				case 14  : gGate.cashAdjust     = true;  break; /* 預借現金調整*/
				case 20  : gGate.refund         = true;  break; /* 退貨*/
				case 22  : gGate.refundAdjust   = true;  break; /* 退貨調整*/
				case 31  : gGate.balanceInquiry = true;  break; /* 餘額查詢*/
				case 80  : gGate.mailOrder      = true;  break; /* 郵購*/
				case 81  : gGate.accountVerify  = true;  break; /* 帳戶驗證 */
				case 92  : gGate.tokenProcess020092  = true;  break; /* Tokenization request (代碼啟用訊息) */
				case 93  : gGate.tokenProcess020093  = true;  break; /*代碼化通知訊息 (Tokenization Notification) */
				case 96  : gGate.changeAtmPin   = true;  break; /* 變更 ATM密碼 */
				//                    case 99  : G_Gate.batchAuth      = true;  break; /*批次授權*/
				default  : break;
				}
			}
		}
		//kevin:FISC規格自行判斷PROCESS CODE
		//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理(PROCESSING CODE整理)
		if ("FISC".equals(gGate.connType) && (gGate.isoField[3].length() > 0) ) {
			int i = Integer.parseInt(gGate.isoField[3].substring(0,6));
			
			switch (i) {
			case      0  : gGate.normalPurch      = true;  break; /* 一般交易*/
			case 170000  : gGate.cashAdvance      = true;  break; /* 預借現金*/
			case 200000  : gGate.refund           = true;  break; /* 退貨授權(VISA ONLY*/
			case 349998  : gGate.isEInvoice       = true;         /* 查詢發票中獎戶同意註記資訊*/
						   gGate.nonPurchaseTxn   = true;  break; 
			case 349999  : gGate.isEInvoice       = true;         /* 申請/取消發票中獎戶同意註記*/
			   			   gGate.nonPurchaseTxn   = true;  break; 
			case 810799  : 
			case 810399  : gGate.easyAutoloadFlag = true;         /* 悠遊卡自動加值功能開啟*/
						   gGate.ticketTxn        = true;         
			   			   gGate.nonPurchaseTxn   = true;  break; 
			case 820799  : gGate.easyAutoloadVd   = true;         /* VD悠遊卡自動加值交易*/
			               gGate.ticketTxn        = true;  break;         
			case 820999  : if (gGate.ifStandIn) {
							   gGate.easyStandIn  = true;         /* 悠遊卡自動加值代行交易*/
			               }
                           else {
							   gGate.easyAutoload = true;         /* 悠遊卡自動加值交易*/
                           }
			               gGate.ticketTxn        = true;  break;  
			case 812799  : 
			case 812999  : gGate.easyAutoloadChk  = true;         /* 悠遊卡授權狀態查詢*/
						   gGate.ticketTxn        = true;         
			   			   gGate.nonPurchaseTxn   = true;  break; 
			case 810000  : if (gGate.ifStandIn) {
         	   				   gGate.ipassStandIn  = true;         /* 一卡通自動加值代行交易*/
						   }
			               else {
							   gGate.ipassAutoload = true;         /* 一卡通自動加值交易*/
			               }
						   gGate.ticketTxn        = true;  break;       
			case 820000  : gGate.ipassAutoloadChk = true;         /* 一卡通授權狀態查詢*/
						   gGate.ticketTxn        = true;         
			   			   gGate.nonPurchaseTxn   = true;  break; 
			case 990174  : gGate.icashAutoload    = true;         /* 愛金卡自動加值交易*/
			               gGate.ticketTxn        = true;  break;       
			case 990175  : gGate.icashStandIn     = true;         /* 愛金卡代行授權*/
						   gGate.ticketTxn        = true;  break;       
			default  : break;
			}
			//V1.00.09  交易類別28xxxx屬於Payment Transaction(PY)一律拒絕交易
			if ("28".equals(gGate.txType)) {
				gGate.paymentTxn = true;
			} 
			else if ("00".equals(gGate.txType)) {
				gGate.normalPurch = true;
			}
			else if ("17".equals(gGate.txType)) {
				gGate.cashAdvance = true;
			}
		}
		//kevin:ATM規格自行判斷PROCESS CODE
		if ("ATM".equals(gGate.connType)) {
			String slPcode = gGate.isoField[3].substring(0,6);
			
			switch (slPcode) {
			case "2622VC"  : gGate.cashAdvanceOnus  = true;         /* 預借現金*/
							 gGate.cashAdvance      = true;  break;	
			case "2632MC"  : gGate.cashAdvanceOnus  = true;         /* 預借現金*/
							 gGate.cashAdvance      = true;  break;	
			case "2640JC"  : gGate.cashAdvanceOnus  = true;         /* 預借現金*/
							 gGate.cashAdvance      = true;  break;	
			case "2471P5"  : gGate.changeAtmPin     = true;         /* 密碼變更舊密碼驗證類別P5*/
							 gGate.verifyAtmPin     = true;
						     gGate.nonPurchaseTxn   = true;  break; 
			case "2471P6"  : gGate.changeAtmPin     = true;         /* 密碼變更新密碼更新類別P6*/
							 gGate.updateAtmPin     = true;
			   			     gGate.nonPurchaseTxn   = true;  break; 
			case "OPENSC"  : gGate.atmCardOpen      = true;         /* combo卡啟用*/
							 gGate.nonPurchaseTxn   = true;  break; 
			default  : break;
			}
			if (("SC").equals(gGate.fCode) || ("3").equals(gGate.reqType)) {
				gGate.atmCardOpen      = true;         /* ATM舊卡啟用新卡時，若為Combo卡時同步開卡通知*/
  			    gGate.nonPurchaseTxn   = true;  
			}
		}
		if (!"".equals(gGate.mccCode) ) {
			int j = Integer.parseInt(gGate.mccCode);
			switch (j) {
			case 4011 : gGate.speedTrain = true;  break; /* 高鐵交易*/
			case 5542 : gGate.selfGas    = true;  break; /* 自助加油 */
			case 7995 : gGate.ecGamble   = true;  break; /* 網路賭博*/
			case 9311 : gGate.taxTrans   = true;  break; /* 繳稅 */
			case 6010 : gGate.cashAdvanceCounter = true; break; /* 櫃台預借現金 */
			case 6011 : gGate.cashAdvance = true; break; /* ATM預借現金 */
			default   : break;
			}
		}
		if (!"".equals(gGate.isoField[25]) ) {
			int k = Integer.parseInt(gGate.isoField[25]);
			switch (k) {
			/*
                    case 6  : G_Gate.preAuth     = false;
                              G_Gate.preAuthComp = true;  break;  // 預先授權 完成
			 */
			case 8  : gGate.mailOrder   = true;  break;  // 郵購   ---- 20170510把郵購的判斷移到交易類別的判斷
			case 15 : gGate.masterEC    = true;  break;  /* MASTER CARD */
			case 59 : gGate.visaEC      = true;  break;  /* VISA CARD */
			default   : break;
			}
		}


		if ( "161".equals(gGate.isoField[70]) ) {
			gGate.changeKey = true; 
		}

		if ( "30".equals(gGate.fromAcctType) || "10".equals(gGate.fromAcctType) || "20".equals(gGate.fromAcctType) ) {
			gGate.atmTrans = true; 
		}

		//kevin:NCCC語音開卡0122900410
		if ( "0122900410".equals(gGate.merchantNo) && "NCCC".equals(gGate.connType)) {
			gGate.txVoice  = true; 
		}
		//kevin:FISC語音開卡95005001 & CVR
		if ( "95005001".equals(gGate.merchantNo) && ("95005001".equals(gGate.isoField[41].substring(0, 8))) &&
				"CVR".equals(gGate.isoField[43].substring(0,3)) && "FISC".equals(gGate.connType)) {
			gGate.txVoice  = true; 
			
		}
		if ( "158".equals(gGate.isoField[19]) || "TW".equals(gGate.merchantCountry) )  { 
			//if (G_Gate.merchantCountry.equals("TW") )  { //20160922. Tony: 改為只看 merchantCountry
			gGate.areaType = "T";/* 國內交易 */ 
		}
		else if (("0420".equals(gGate.mesgType)) || ("0421".equals(gGate.mesgType))) {
			gGate.areaType = "T";/* 國內交易 *///20160922: 0420 and 0421 一律視為 國內交易

		}
		else {

			gGate.areaType = "F";/* 國外交易 */
		}


		/* EC 網路交易 */
		if (gGate.isoField[22].length()>=2) {
			String slValue =gGate.isoField[22].substring(0, 2); 
			if ("81".equals(slValue))
				gGate.ecTrans = true;
			//V1.00.04 針對POS ENTRY MODE 91，拒絕交易
			if ("91".equals(slValue))
				gGate.contactLessByMagnetic = true;
		}
		if (gGate.f48T42.length()>0) { //Electronic Commerce Indicator
			gGate.ecTrans = true;
		}


		if ("59".equals(gGate.isoField[25]) || "15".equals(gGate.isoField[25]) ) { 
			gGate.ecTrans = true; 
		}
		

		/* 分期交易 */
//		if ( G_Gate.divMark.equals("I") || G_Gate.divMark.equals("E") ) {
//			G_Gate.installTrans = true; 
//		}
		//kevin:Fisc設定分期註記
		if (gGate.isoField[112].length()>0) {
			gGate.isInstallmentTx=true; //分期交易
		}

		/* 紅利交易 */
//		if ( G_Gate.loyaltyTxId.equals("1") || G_Gate.loyaltyTxId.equals("2") || G_Gate.loyaltyTxId.equals("3") ||
//				G_Gate.loyaltyTxId.equals("4") || G_Gate.loyaltyTxId.equals("6") || G_Gate.loyaltyTxId.equals("7") ) {
//			G_Gate.redeemTrans = true; 
//		}
		//kevin:Fisc設定紅利註記
		if (gGate.f58T21.length()>0) {
			gGate.isRedeemTx=true; //紅利交易
		}
		
		//kevin:設定所有財金交易類別
		//V1.00.01 - VMAUT、VAUT屬於授權交易，非屬於代碼化交易處理。
		if ("FISC".equals(gGate.connType)) {
			//kevin:設定FISC TOKEN訊息類型
			if ( gGate.f58T73TokenType.length() > 0 ) {
				gGate.isFiscToken = true; 
				/* VTAR：VISA Token Activation Request */
				if ( "VTAR".equals(gGate.f58T73TokenType)) {
					gGate.isTokenVTAR = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "VTAR:VISA Token Activation Request";
				}
				/* VTNA：VISA Token Notification Advice */
				if ( "VTNA".equals(gGate.f58T73TokenType)) {
					gGate.isTokenVTNA = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "VTNA:VISA Token Notification Advice";
				}
				/* VAUT：VISA Token 授權交易/授權通知交易/取消交易  */
				if ( "VAUT".equals(gGate.f58T73TokenType)) {
					gGate.isTokenVAUT = true; 
					gGate.isFiscToken = false; 
					gGate.authRemark = "VAUT:VISA Token 授權交易";
				}
				/* MAUT：MasterCard Token 授權交易/授權通知交易/取消交易  */
				if ( "MAUT".equals(gGate.f58T73TokenType)) {
					gGate.isTokenMAUT = true; 
					gGate.isFiscToken = false; 
					gGate.authRemark = "MAUT:MasterCard Token 授權交易";
				}
				/* MTER：MasterCard Token Eligibility Request */
				if ( "MTER".equals(gGate.f58T73TokenType)) {
					gGate.isTokenMTER = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "MTER:MasterCard Token Eligibility Request";
				}
				/* MTAR：MasterCard Token Authorization Request */
				if ( "MTAR".equals(gGate.f58T73TokenType)) {
					gGate.isTokenMTAR = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "MTAR:MasterCard Token Authorization Request";
				}
				/* MTCN：MasterCard Token Complete Notification */
				if ( "MTCN".equals(gGate.f58T73TokenType)) {
					gGate.isTokenMTCN = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "MTCN:MasterCard Token Complete Notification";
				}
				/* MTEN：MasterCard Token Event Notification */
				if ( "MTEN".equals(gGate.f58T73TokenType)) {
					gGate.isTokenMTEN = true; 
					gGate.nonPurchaseTxn = true;
					gGate.authRemark = "MTEN:MasterCard Token Event Notification";
				}
			}
			//kevin:判斷小額交易
			if ( "MP".equals(gGate.f58T69SpecialTxn)) {
				gGate.isLowTradeAmt = true; 
			}
			//kevin:申請信用卡載具發票中獎入戶
			if ( "EA".equals(gGate.f58T69SpecialTxn)) {
				gGate.isReceiptAdd = true; 
				gGate.nonPurchaseTxn = true;
			}
			//kevin:取消信用卡載具發票中獎入戶
			if ( "EC".equals(gGate.f58T69SpecialTxn)) {
				gGate.isReceiptCancel = true; 
				gGate.nonPurchaseTxn = true;
			}
			//kevin:信用卡 QR Code 主掃授權
			if ( "QA".equals(gGate.f58T69SpecialTxn)) {
				gGate.isQRCodeActive = true; 
			}
			//kevin:信用卡 QR Code 被掃授權
			if ( "QB".equals(gGate.f58T69SpecialTxn)) {
				gGate.isQRCodePassive = true; 
			}
			//kevin:聯卡中心ON-US繳費平台
			if ( "01".equals(gGate.f58T68IdCheckType)) {
				gGate.isNcccOnusPay = true; 
			}
			//kevin:財政中心電子化發票平台
			if ( "02".equals(gGate.f58T68IdCheckType)) {
				gGate.isReceipt  = true; 	
				gGate.isEInvoice = true; 
				gGate.nonPurchaseTxn = true;
			}
			//kevin:電子化支付機構/金融機構
			if ( "03".equals(gGate.f58T68IdCheckType)) {
				gGate.isIdCheckOrg = true; 
				gGate.nonPurchaseTxn = true;
			}
			//kevin:電子化繳費稅處理平台
			if ( "04".equals(gGate.f58T68IdCheckType)) {
				gGate.isElectPayTax = true; 
			}
		}
			
		//其他非身分輔助驗證之EC網路交易account verify
		if (!gGate.isIdCheckOrg) {
			if ((gGate.ecTrans) && (Double.parseDouble(gGate.isoField[4])<=0) && (gGate.cvv2.length() > 0)) {
				gGate.accountVerify  = true;
			}
		}
		


		//新增G_Gate.authSource，提供txlog紀錄使用
		gGate.authSource = "P";
		//V1.00.10 修正票證交易日累計自動加值交易金額及次數處理與沖正問題
		//V1.00.11 以沖正交易成功與否作為判斷條件，並忽略的原始回覆碼的判斷
		if (("0220").equals(gGate.mesgType) ||("0120").equals(gGate.mesgType)) {
			gGate.authSource = "B";
			if (!"00".equals(gGate.oriRespCode.substring(0, 2)))
				gGate.ifCredit = false;//設為不佔額度
		}
		if (gGate.ecTrans) {
			gGate.authSource = "E";
		}
		if ( "WEB".equals(gGate.connType))  {               /*人工授權*/ 
			gGate.transCode = "MA"; 
			if (gGate.cashAdvance) {
				gGate.normalPurch = false;
				gGate.authType ="C";
				gGate.logicDel = "C";
			}
			else {
				gGate.authType ="Z";
			}
			gGate.sgKey4OkTrans="OKAE1";
			gGate.authSource="T";
			if ( gGate.reversalTrans ) {                    /*人工沖正交易 */
				gGate.transCode = "WO"; 
				gGate.authType ="R";
				gGate.logicDel = "Z";
				gGate.sgKey4OkTrans="OKAM3";
			}
			//V1.00.38 P3授權額度查核調整
			else if ( gGate.balanceInquiry ) {               /* 餘額查詢 */
				gGate.transCode = "BQ"; 
				gGate.authType ="Q";
				gGate.logicDel = "Q";
				gGate.sgKey4OkTrans="OKA01";
			}
		}
		else if ( "BATCH".equals(gGate.connType))  {               /*批次授權*/ 
			gGate.transCode = "BA"; 
			gGate.authType ="Z";
			gGate.sgKey4OkTrans="OKA20";
			gGate.authSource="T";
		}
		else if (gGate.ticketTxn ) {                             /*三大票證公司*/
			if ( gGate.easyAutoloadFlag ) {                 /* 悠遊卡自動加值功能開啟*/
				 gGate.transCode = "TN"; 
				 gGate.authSource="O";				  
				 gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.easyAutoload ) {                
				if	( gGate.reversalTrans)
					{ gGate.transCode = "TR";               /* 悠遊卡自動加值沖正交易*/
					  gGate.authType ="R";
					  gGate.logicDel = "x";
					  gGate.sgKey4OkTrans="OKA13";
					}
				else{ gGate.transCode = "TA"; }             /* 悠遊卡自動加值交易*/
					  gGate.authSource = "L";
					  gGate.sgKey4OkTrans="OKA12";
			}		
			else if ( gGate.easyAutoloadVd ) {                
				if	( gGate.reversalTrans)
					{ gGate.transCode = "VR";               /* 悠遊卡VD自動加值沖正交易*/
					  gGate.authType ="R";
					  gGate.logicDel = "x";
					  gGate.sgKey4OkTrans="OKA13";
					}
				else{ gGate.transCode = "VA"; }             /* 悠遊卡VD自動加值交易*/
					  gGate.authSource = "L";
					  gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.easyAutoloadChk ) {             /* 悠遊卡授權狀態查詢*/
				      gGate.transCode = "TQ"; 
				      gGate.authSource = "H";
					  gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.easyStandIn ) {                /* 悠遊卡代行授權交易*/
					  gGate.transCode = "TS"; 
				      gGate.authSource = "W";
				      gGate.sgKey4OkTrans="OKA12";		  
			}
			else if ( gGate.ipassAutoload ) {               /* 一卡通自動加值交易*/
				if	( gGate.reversalTrans)
					{ gGate.transCode = "IR";               /* 一卡通自動加值沖正交易*/
					  gGate.authType ="R";
					  gGate.logicDel = "x";
					  gGate.sgKey4OkTrans="OKA13";
					}
				else{ gGate.transCode = "IN"; }             /* 一卡通自動加值交易*/
				      gGate.authSource = "L";
					  gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.ipassAutoloadChk ) {            /* 一卡通授權狀態查詢*/
					  gGate.transCode = "IQ"; 
					  gGate.authSource = "H";
					  gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.ipassStandIn ) {                /* 一卡通代行授權交易*/
					  gGate.transCode = "IS"; 
					  gGate.authSource = "W";
					  gGate.sgKey4OkTrans="OKA12";		  
			}
			else if ( gGate.icashAutoload ) {               /* 愛金卡自動加值交易*/
				if	( gGate.reversalTrans)
					{ gGate.transCode = "HR";              	/* 愛金卡自動加值沖正交易*/
					  gGate.authType ="R";
					  gGate.logicDel = "x";
					  gGate.sgKey4OkTrans="OKA13";
					}
				else{ gGate.transCode = "HN"; }         	 /* 愛金卡自動加值交易*/
				      gGate.authSource = "L";
					  gGate.sgKey4OkTrans="OKA12";
			}
			else if ( gGate.icashStandIn ) {                /* 愛金卡代行授權交易*/
				      gGate.transCode = "HS"; 
				      gGate.authSource = "W";
					  gGate.sgKey4OkTrans="OKA12";
			}
		}
			
		else if ( gGate.isLowTradeAmt ) {                        /* 小額付款交易*/
			gGate.transCode = "MP"; 
   	        gGate.sgKey4OkTrans="OKA07";

		}
		//V1.00.09  交易類別28xxxx屬於Payment Transaction(PY)一律拒絕交易
		else if ( gGate.paymentTxn ) {
			gGate.transCode = "PY"; 
		}
		//V1.00.03  VISA VTAR代碼化驗證訊息調整。
		else if ( gGate.isTokenMTAR || gGate.isTokenMTER || gGate.isTokenVTAR ) {     /* 代碼持卡人驗證訊息*/
			gGate.transCode = "CV"; 
			gGate.sgKey4OkTrans="OKA19";
		}
		//V1.00.52 OEMPAY綁定成功後發送通知簡訊和格式整理
		//V1.00.54 OEMPAY綁定Mastercard Token成功通知僅限行動裝置
		else if ( (gGate.isTokenMTCN && "0".equals(gGate.ecUsage)) || "3712".equals(gGate.f58T70) || "3713".equals(gGate.f58T70)) {     /* 代碼化啟用通知訊息*/
			gGate.transCode = "CM"; 
		}
		else if ( gGate.isTokenVTNA || gGate.isTokenMTEN || gGate.isTokenMTCN) {     /* 代碼化通知訊息*/
			gGate.transCode = "DN"; 
		}		
		else if ( gGate.isReceiptAdd ) {                 /* 申請信用卡載具發票中獎入戶*/
			gGate.transCode = "EA"; 
		}
		else if ( gGate.isReceiptCancel ) {              /* 取消信用卡載具發票中獎入戶*/
			gGate.transCode = "EC"; 
		}
		else if ( gGate.isReceipt ) {                    /* 電子發票身分驗證*/
			gGate.transCode = "EI"; 
		}
		else if ( gGate.isEInvoice ) {                   /* 查詢發票中獎戶同意註記資訊*/
			gGate.transCode = "WI"; 
		}
		else if ( gGate.isIdCheckOrg ) {                 /* 電子化支付機構/金融機構，身分驗證輔助驗證*/
			gGate.transCode = "ID"; 
		}
		else if ( gGate.reversalTrans ) {                /* 授權沖正交易 */
//			G_Gate.transCode = "x"; 
			gGate.transCode = "WO"; 
			gGate.authType ="R";
			gGate.logicDel = "R";
			gGate.sgKey4OkTrans="OKA13";
		}
		else if ( gGate.isQRCodeActive ) {               /* 信用卡 QR Code 主掃授權。(0100/0420 使用) */
			gGate.transCode = "QA";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.isQRCodePassive ) {              /* 信用卡 QR Code 被掃授權。(0100/0420 使用) */
			gGate.transCode = "QP";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if( "NCCC".equals(gGate.connType) && gGate.forcePosting ) {                 /* 授權補登  */
//			G_Gate.transCode = "F"; 
			gGate.transCode = "BU"; 
			gGate.authType ="F";
			gGate.logicDel = "F";
			gGate.sgKey4OkTrans="OKA07";
		}
		else if ("FISC".equals(gGate.connType) && gGate.forcePosting ) {                /*授權補登 V1.00.06 授權補登交易取消設定，同預先授權完成交易 */ 
//			G_Gate.transCode = "0"; 
			gGate.transCode = "BU"; 
			gGate.authType ="F";
			gGate.logicDel = "F";
			gGate.sgKey4OkTrans="OKA07";
		}
//		else if ((gGate.connType.equals("NCCC") || gGate.connType.equals("FISC"))  && (gGate.reversalTrans )) {/*  自動授權沖銷  */
////			G_Gate.transCode = "R"; 
//			gGate.transCode = "WO"; 
//			gGate.authType ="R";
//			gGate.logicDel = "R";
//		}
		else if ( gGate.changeAtmPin ) {                 /*變更密碼*/
//			G_Gate.transCode = "1"; 
			gGate.transCode = "AP"; 
			gGate.authType ="1";
			gGate.logicDel = "1";
			gGate.sgKey4OkTrans="OKA18";
		}
		else if ( gGate.changeKey ) {                    /*線上換KEY */
//			G_Gate.transCode = "2"; 
			gGate.transCode = "CK"; 
			gGate.authType ="2";
			gGate.logicDel = "9";
			gGate.sgKey4OkTrans="OKA17";
		}
		else if ( gGate.cashAdjust ) {                   /*預借現金調整 */
//			G_Gate.transCode = "A"; 
			gGate.transCode = "CJ"; 
			gGate.authType ="A";
			gGate.logicDel = "A";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.refund )   {                     /*退貨*/
//			G_Gate.transCode = "B"; 
			gGate.transCode = "RF"; 
			gGate.authType ="B";
			gGate.logicDel = "B";
			gGate.sgKey4OkTrans="OKA04";
		}
		else if ( gGate.cashAdvanceOnus ) {              /* 自行ATM預借現金 */
			gGate.transCode = "CO"; 
			gGate.authType ="C";
			gGate.logicDel = "C";
			gGate.sgKey4OkTrans="OKA03";
		}
		else if ( gGate.cashAdvance ) {                  /* 預借現金 */
//			G_Gate.transCode = "C"; 
			if ("6011".equals(gGate.mccCode)) {
				gGate.transCode = "AC"; 
			}
			else {
				gGate.transCode = "CA"; 
			}
			gGate.authType ="C";
			gGate.logicDel = "C";
			gGate.sgKey4OkTrans="OKA03";
		}
//		else if ( G_Gate.mailOrder && G_Gate.purchAdjust ) {/* 郵購 */
//			G_Gate.transCode = "D"; 
//			G_Gate.auth_type ="D";
//			G_Gate.logic_del = "D";
//		}
		else if ( gGate.purchAdjust ) {                  /* 一般調整*/
//			G_Gate.transCode = "J"; 
			gGate.transCode = "PJ"; 
			gGate.authType ="J";
			gGate.logicDel = "J";
			gGate.sgKey4OkTrans="OKA05";
		}
//		else if ( gGate.mailOrder  )  {                  /* 郵購 */
////			G_Gate.transCode = "M";
//			gGate.transCode = "MO"; 
//			gGate.authType ="M";
//			gGate.logicDel = "M";
//			gGate.sgKey4OkTrans="OKA08";
//		}
		else if ( gGate.balanceInquiry ) {               /* 餘額查詢 */
//			G_Gate.transCode = "Q"; 
			gGate.transCode = "BQ"; 
			gGate.authType ="Q";
			gGate.logicDel = "Q";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if (  gGate.txVoice  ) {                    /*語音開卡 */
//			G_Gate.transCode = "V"; 
			gGate.transCode = "VO"; 
			gGate.authType ="V";
			gGate.logicDel = "V";
			gGate.sgKey4OkTrans="OKA02";
		}
		else if ( gGate.atmCardOpen ) {                 /*ATM COMBO開卡啟用*/
			gGate.transCode = "AO"; 
			gGate.authType ="V";
			gGate.logicDel = "V";
			gGate.sgKey4OkTrans="OKA02";
		}
		else if ( gGate.refundAdjust ) {                /*退貨調整*/
//			G_Gate.transCode = "W"; 
			gGate.transCode = "RJ"; 
			gGate.authType ="W";
			gGate.logicDel = "W";
			gGate.sgKey4OkTrans="OKA06";
		}
		else if ( gGate.preAuthComp )  {                  /*預先授權完成*/
//			G_Gate.transCode = "Y"; 
			gGate.transCode = "PC"; 
			gGate.authType ="Y";
			gGate.logicDel = "Y";
			gGate.sgKey4OkTrans="OKA10";
		}
		else if ( gGate.preAuth )  {                     /*預先授權*/
//			G_Gate.transCode = "X"; 
			gGate.transCode = "PA"; 
			gGate.authType ="X";
			gGate.logicDel = "X";
			gGate.sgKey4OkTrans="OKA09";
		}
		else if ( gGate.accountVerify ) {                /* 帳戶驗證交易*/
//			G_Gate.transCode = "U"; 
			gGate.transCode = "CK"; 
			gGate.authType ="U";
			gGate.logicDel = "U";
		}		
		else if ( gGate.isRedeemTx ) {                  /*紅利積點抵扣交易 */ 
//			G_Gate.transCode = "0"; 
			gGate.transCode = "BD"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.isInstallmentTx ) {             /*分期付款交易 */ 
//			G_Gate.transCode = "0"; 
			gGate.transCode = "IP"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.speedTrain ) {                 /*高鐵交易 */ 
			gGate.transCode = "HI"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.ecGamble ) {                   /*網路賭博 */ 
			gGate.transCode = "GB"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.taxTrans ) {                   /*繳稅 */ 
			gGate.transCode = "PT"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else if ( gGate.mailOrder  )  {                  /* 郵購 */
			gGate.transCode = "MO"; 
			gGate.authType ="M";
			gGate.logicDel = "M";
			gGate.sgKey4OkTrans="OKA08";
		}
		else if ( gGate.ifStandIn ) {                /*一般代行通知交易 V1.00.06 授權補登交易取消設定，同預先授權完成交易*/
			gGate.transCode = "SI"; 
			gGate.authType ="F";
			gGate.logicDel = "F";
			gGate.sgKey4OkTrans="OKA07";
		}
		else if ( gGate.normalPurch ) {                 /*一般交易 */ 
//			G_Gate.transCode = "0"; 
			gGate.transCode = "NP"; 
			gGate.authType ="0";
			gGate.logicDel = "0";
			gGate.sgKey4OkTrans="OKA01";
		}
		else {
			gGate.transCode = "XX"; 
		}
		

	}
}
