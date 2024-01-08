/**
 * Proc 處理各種交易回覆前，回覆資料欄位調整
 * 必須在 saveLog() 前執行
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       Proc 處理各種交易回覆前，回覆資料欄位調整          *
 * 2021/11/16  V1.00.01  Kevin       VISA代碼化申請核准，回覆碼對應P-39 ="00" OR "85"*
 * 2022/04/11  V1.00.02  Kevin       授權補登交易取消設定，同預先授權完成交易            *
 * 2022/05/31  V1.00.03  Kevin       授權回覆碼錯誤修正                            *
 * 2022/06/09  V1.00.04  Kevin       身分驗證交易拒絕時授權回覆碼與授權紀錄不一致         *
 * 2023/10/12  V1.00.54  Kevin       OEMPAY綁定Mastercard Token成功通知僅限行動裝置  *
 * 2024/12/27  V1.00.64  Kevin       MasterCard Oempay申請需檢查行動電話後四碼是否與資料相同                                                                           *
 ******************************************************************************
 */

package com.tcb.authProg.process;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.logic.AuthLogic;
import com.tcb.authProg.logic.LogicGenerateAuthCode;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;

public class ProcReAssignData extends AuthProcess {

	public ProcReAssignData(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gGb    = gb;
		this.gGate  = gate;
		this.gTa    = ta;
		
		gb.showLogMessage("I","ProcReAssignData : started");

	}
	
	public void reAssignData(){
		//必須在 saveLog() 前執行
		try {
			//System.out.println("P38-9" + G_Gate.isoField[38] + "===");
			gTa.setAuthStatusCode();
			
			
			if (!"00".equals(gGate.isoField[39]) && !"85".equals(gGate.isoField[39])) {
				//授權回覆碼錯誤修正
//				/* Setup abnormal transaction flag */
//				String slSelectField = " NVL(SYS_DATA2,' ') as Parm1StsData2,NVL(SYS_DATA3,' ') as Parm1StsData3, NVL(SYS_DATA4,'N') as Parm1StsData4,NVL(SYS_DATA5,' ') as Parm1StsData5 ";
//				if (gTa.selectSysParm1("RESP", gGate.isoField[39], slSelectField)) { // Howard(20190104) : 從 cca_sys_parm1 中，似乎找不到資料?! 錯誤碼都從 CCA_RESP_CODE 中抓了，是不是就不需要這樣做了呢?
//
//					String slTmpCountry = gTa.getValue("Parm1StsData4");
//					if (!"Y".equals(slTmpCountry))
//						gGate.bgAbnormalResp = false;
//
//					//proc.AuTxlog_ISO_RSP_CODE == java.realTxResultOnP39
//					gGate.sgIsoRespCode = gTa.getValue("Parm1StsData3").substring(0,2);
//
//					//proc.AuTxlog_BIT38_APPR_CODE == java.sG_Bit38ApprCode
//					//proc.AuTxlog_BIT39_RESP_CODE == java.sG_Bit39RespCode
//					//G_Gate.sG_Bit39RespCode = G_Gate.isoField[39]; Howard: 這變數用不到
//					if ("00".equals(gGate.sgIsoRespCode)) {
//						gGate.sgBit38ApprCode = gGate.authNo;
//						
//					}
//					else {
//						gGate.sgBit38ApprCode = gTa.getValue("Parm1StsData2");
//						if ("  ".equals(gGate.sgBit38ApprCode))
//							gGate.sgBit38ApprCode = "86    ";
////						gGate.authNo = "**";
//					}
//				}
//				else {
//					/*原定 B5*/
//					gGate.sgBit38ApprCode = "86    ";
//					//G_Gate.sG_Bit39RespCode = "05";Howard: 這變數用不到
//					gGate.isoField[39] = "05";
////					gGate.authNo = "**";
//				}

				if (gGate.ifStandIn) {
					gGate.standInReason= gGate.oriRespCode;
					gGate.standInRspcode=gGate.sgIsoRespCode;
//					gGate.standInOnuscode=gGate.sgIsoRespCode;
					gGate.standInOnuscode=gGate.authErrId;
				}
			}



//			if  ((gGate.ifStandIn) || ("0120".equals(gGate.mesgType.substring(0, 4)) ) ){
			//V1.00.02 授權補登交易取消設定，同預先授權完成交易
			if  (gGate.ifStandIn || gGate.forcePosting) {
				gGate.sgBit38ApprCode = gGate.oriAuthNo;
				gGate.authNo = gGate.oriAuthNo;
				gGate.sgIsoRespCode = gGate.oriRespCode;
				gGate.isoField[39] = gGate.oriRespCode;
				gGate.isoField[38] = gGate.oriAuthNo;
//				if ("00".equals(gGate.oriRespCode) && "N".equals(gGate.cacuAmount)) {
				if ("00".equals(gGate.oriRespCode)) { //核准－代行/補登
					gGate.cacuAmount = "Y";					
				}
				else {                                //拒絕－代行/補登
					gGate.cacuAmount = "N";
					gGate.authStatusCode = "F9"; 
				}
			}

			if (( "WEB".equals(gGate.connType)) && ("N".equals(gGate.isoField[27].trim())) ){
				return;
			}
			if ( "WEB".equals(gGate.connType)) /*人工授權 */ {
				//kevin:TranType = 改由MSGTYPE放入
//				G_Gate.TransType = G_gTa.getValue("MccRiskNcccFtpCode");
				gGb.showLogMessage("D", "@@@@@人工授權mesgType="+gGate.mesgType);
				gGate.transType = gGate.mesgType;
				gGate.authType="1";

				if (gGate.refund) { /* 退貨 -- 改為沖正交易(人工授權) */
					gGate.logicDel = "R"; //x->R
					gGate.authType="R";   //2=>R
				}

				if (("F".equals(gGate.isoField[27].trim())) || ("R".equals(gGate.isoField[27].trim())) || ("A".equals(gGate.isoField[27].trim()))) {
					/** 強制交易 or 沖正交易**/
					gGb.showLogMessage("D", "強制授權檢查1="+gGate.isoField[27].trim()+" AuthRemark="+gGate.isoField[122]
							+" OnlineRespCode="+gGate.isoField[39]);
					gGate.authRemark = gGate.isoField[122];
					gGate.sgOnLineRespCode = gGate.isoField[39];
				}

				
				if ("F".equals(gGate.isoField[27].trim())){
					gGb.showLogMessage("D", "強制授權檢查2="+gGate.isoField[27].trim()+" IsoRespCode="+gGate.sgIsoRespCode
							+" OnlineRespCode="+gGate.isoField[39]);
					if (!"00".equals(gGate.sgOnLineRespCode)) {
						gGate.authStatusCode = gGate.sgOnLineRespCode; 
					}

					if (!"00".equals(gGate.sgIsoRespCode)) {
						gGb.showLogMessage("D", "強制授權檢查3="+gGate.isoField[27].trim()+" auth_no="+gGate.authNo
								+" RespCode="+gGate.isoField[39]);
						ProcUpdateRelatedFile logicUpdFile = new ProcUpdateRelatedFile(gGb, gGate, gTa);
						logicUpdFile.updateRelatedFile();
						//kevin:交易被拒絕系統不會產生授權碼，但因要強制授權需要，所以產生授權碼，更新ISO回覆碼及系統回覆碼
					    LogicGenerateAuthCode logicAuthNo = new LogicGenerateAuthCode(gGb, gGate, gTa);
					    logicAuthNo.genAuthCode();                  //// 產生 授權碼
						gGate.authStatusCode = "AL";
						gGate.sgIsoRespCode = "00";
						gGate.sgBit38ApprCode = gGate.authNo;
						gGate.isoField[39] = "00";
						gGate.cacuAmount = "Y";
						gGate.currTotTxAmt += gGate.ntAmt;
						gGate.currTotUnpaid += gGate.ntAmt;
					}

				}

			}
			if (("01".equals(gGate.sgIsoRespCode)) &&
					(((gGate.mailOrder) && ("F".equals(gGate.areaType)))  || ("6011".equals(gGate.mccCode) || (gGate.ecTrans))  )){
				gGate.sgIsoRespCode = "57";
				


			}
			if ( ("00".equals(gGate.sgIsoRespCode)) && (gGate.ntAmt==0) 
					&&( (gGate.tokenC4TxnStatInd.length()>0) && ("8".equals(gGate.tokenC4TxnStatInd.substring(0, 1)))  || ( "51".equals(gGate.isoField[25])) || ("810000".equals(gGate.isoField[3])) ) ) {
				gGate.sgIsoRespCode = "85";
				
			}
			//代碼化申請核准，回覆碼對應P-39 = "00" OR "85"
			
			if ((gGate.ntAmt==0) && (gGate.authStatusCode.length()>0) && (gGate.isTokenMTAR || gGate.isTokenMTER))  {		
				if ("00".equals(gGate.sgIsoRespCode) || "85".equals(gGate.sgIsoRespCode)) {
					gTa.getAndSetErrorCode(gGate.authStatusCode);
					gGate.sgIsoRespCode = gGate.isoField[39];	
					String slCallCenterNum = "(04)22273131";	
					gGb.showLogMessage("D","CrdIdNoCellPhone : "+gTa.getValue("CrdIdNoCellPhone")+";length ="+gTa.getValue("CrdIdNoCellPhone").length()+";isMobileLast4Digtal="+gGate.isMobileLast4Digtal);
					if (gTa.getValue("CrdIdNoCellPhone").length()>0 && gGate.isMobileLast4Digtal) {
						String slMaskedCellPhoneNum = HpeUtil.getMaskData(gTa.getValue("CrdIdNoCellPhone"),4,"#");
						gGate.isoField[124] = gGate.configutationId + "000" + "1" + slMaskedCellPhoneNum + "|" + "4" + slCallCenterNum + "||";
					}
					else {
						gGate.isoField[124] = gGate.configutationId + "000" + "4" + slCallCenterNum + "||";
					}
				}
				else {
					gGate.isoField[124] = "";
				}
			}
			//V1.00.01 kevin:VISA代碼化申請核准，回覆碼對應P-39 = "00" OR "85"
			if ((gGate.ntAmt==0) && (gGate.authStatusCode.length()>0) && gGate.isTokenVTAR)  {		
				if ("00".equals(gGate.sgIsoRespCode) || "85".equals(gGate.sgIsoRespCode)) {
					gTa.getAndSetErrorCode(gGate.authStatusCode);
					gGate.sgIsoRespCode = gGate.isoField[39];	
				}
			}
			//V1.00.04 身分驗證交易拒絕時授權回覆碼與授權紀錄不一致
//			if ( (!"00".equals(gGate.sgIsoRespCode)) && (!"85".equals(gGate.sgIsoRespCode)) && (gGate.ntAmt==0) 
//					&&( (gGate.tokenC4TxnStatInd.length()>0) && ("8".equals(gGate.tokenC4TxnStatInd.substring(0, 1)))  || ( "51".equals(gGate.isoField[25])) || ("810000".equals(gGate.isoField[3])) ) ) {
//				gGate.sgIsoRespCode = "05";
//			}
			
			//0420/0421必須回覆P39=00
			if (("0420".equals(gGate.mesgType)) || ("0421".equals(gGate.mesgType))) {
				if (gGate.oriRespCode.length()>0 && !"00".equals(gGate.oriRespCode)) {
					gGate.authNo = gGate.oriAuthNo;
					gGate.sgIsoRespCode = gGate.oriRespCode;
					gGate.isoField[38]  = gGate.oriAuthNo;
				}
				gGate.isoField[39]="00";
			}

			
			//ATM開卡通知，強迫回覆P39=00
			if (gGate.atmCardOpen && !"00".equals(gGate.isoField[39])) {
				gGb.showLogMessage("D", "Atm COMBO Card Open Faild. return code = "+gGate.isoField[39] +"-"+ gGate.authErrId);
				gGate.isoField[39]="00";
			}

		} catch (Exception e) {
			// TODO: handle exception
			gGb.showLogMessage("E", "reAssignData:"+ e.getMessage());
		}
	}


}
