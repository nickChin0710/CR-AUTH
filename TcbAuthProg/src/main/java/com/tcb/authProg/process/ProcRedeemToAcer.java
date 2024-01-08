/**
 * Proc 處理ACER redeem交易的流程
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
 * 2021/02/08  V1.00.00  Kevin       Proc 處理ACER redeem交易的流程              *
 * 2022/04/13  V1.00.01  Kevin       ACER 回應Time out時間調整為5秒               *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

//import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.iso8583.AcerFormat;
import com.tcb.authProg.iso8583.FormatInterChange;
//import com.tcb.authProg.logic.AuthLogic;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;

public class ProcRedeemToAcer extends AuthProcess {

	public ProcRedeemToAcer(AuthGlobalParm gb,AuthTxnGate gate) {
		this.gGb    = gb;
		this.gGate  = gate;
		
		gb.showLogMessage("I","ProcRedeemToAcer : started");


	}
	
	public boolean sendRedeemToAcer() throws Exception{
		boolean blResult = true;
		String slSrcRrn   = ("000000000000000000000000");
		String slSrcApprovalCode = ("000000000000");
		String slSrcResponseCode = ("FF");
		String slTable01 = ("202020204e202020202020202020000000000000000000000000000000000000000000000000000000000000");
		String slSrcTable01 = ("02223031")+slTable01+slTable01+slTable01+slTable01+slTable01;
		String slPoint02 = "43302020"+ gGate.isoField[4];
		String slTable02 = ("20202020000000000000");
		String slSrcTable02 = ("01023032")+slPoint02+slTable02+slTable02+slTable02+slTable02+slTable02+slTable02+slTable02+slTable02+slTable02;
		String slSrcTable03 = ("002230330000000000000000000000000000FFFFFFFFFFFF");		
		String slSrcMesgType = gGate.mesgType;
		gGb.showLogMessage("D","@@@@sL_SrcTable01="+slSrcTable01);
		gGb.showLogMessage("D","@@@@sL_SrcTable02="+slSrcTable02);
		gGb.showLogMessage("D","@@@@sL_SrcTable03="+slSrcTable03);

		
		gGate.acerField[2]  = gGate.isoField[2];
		gGate.acerField[3]  = "000000";
		gGate.acerField[4]  = gGate.isoField[4];
		gGate.acerField[11] = gGate.isoField[11];
		gGate.acerField[12] = gGate.isoField[12];
		gGate.acerField[13] = gGate.isoField[13];
		gGate.acerField[14] = gGate.isoField[14];
		gGate.acerField[22] = "0012";
		gGate.acerField[24] = "0800";
		gGate.acerField[25] = "C0";
		gGate.acerField[41] = gGate.isoField[41];
		gGate.acerField[42] = gGate.isoField[42];
		gGate.acerField[48] = gGate.acerField[42]+"             "+"000001"+"          "+"                                                   ";
		gGb.showLogMessage("D","@@@@acer_acerField[48]="+gGate.acerField[48]);
		
		if (gGate.reversalTrans) {
			gGate.mesgType = "0400";  //Reversal交易轉為0400交易，ACER mesgType固定0400
			gGate.acerField[11] = gGate.oriTraceNo;
			
		} else {
			gGate.mesgType = "0200";  //轉為0200交易，ACER mesgType固定0200
			gGate.acerField[63] = slSrcRrn+slSrcApprovalCode+slSrcResponseCode+slSrcTable01+slSrcTable02+slSrcTable03;
			gGb.showLogMessage("D","@@@@acer_acerField[63]="+gGate.acerField[63]);
		}
		int nlTraceNo = HpeUtil.getRandomNumber(1000000);
//		String slDataToAcer= nlTraceNo + "...data from G_Gate..."; //proc.EDS_Iso2Bit() 組合出送給 ACER 的data
		//down, new socket to send to ACER
		String slAcerSocketIp= gGb.getAcerHost();
		int nlAcerSocketPort = gGb.getAcerPort();
		// fix issue "Unreleased Resource: Sockets" 2020/09/17 Zuwei
		try (
			//send Redeem TXN To Acer
			Socket lSocketToAcer  =  new  Socket(slAcerSocketIp ,  nlAcerSocketPort);
			BufferedInputStream lAcerInputStream =  new  BufferedInputStream(lSocketToAcer.getInputStream());
			BufferedOutputStream  lAcerOutputStream = new  BufferedOutputStream(lSocketToAcer.getOutputStream()); ) {
			lSocketToAcer.setSoTimeout(5*1000);//設定 timeout == 5 secs

			FormatInterChange intr = null;
			intr  =  new  AcerFormat(gGb.getLogger(),gGate);
			intr.host2Iso();

			byte[] lDataByteAry = gGate.isoData;
			gGb.showLogMessage("D","acer_isoData HEX= "+HpeUtil.byte2HexStr(lDataByteAry));
			gGb.showLogMessage("D","acer_isoData = "+new String(lDataByteAry));
			HpeUtil.writeData2Acer(lAcerOutputStream, lDataByteAry, gGate.totalLen);	
			
			//receive Redeem TXN From Acer
			boolean blSucFlag = false;
			String slResultOfRecvAcer = "";
			boolean blKeepReceiving = true;
			byte[] lRecvByteAry = null;

			while (blKeepReceiving) {
				blSucFlag = false;
//				slAcerRespCode = "";
				lRecvByteAry = HpeUtil.readDataFromAcer(lAcerInputStream);
				//kevin: G_Gate.isoData是紅利測試用，直接用原來request的電文G_Gate.isoData訊息測試
//				L_RecvByteAry = HpeUtil.hex2Byte("60020100010210203801000E81000200000000022411473710160800313030303030303237323035303030303231303031303031303039310095303036313233363030393031303031202020202020202020202020203030303030312020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202020202003713130303030303032373230353030303032310002223031303030305932303231303232382B000009160000000000000000000000000000000000067000000000013400000000004E00000000000000002B000000000000000000000000000000000000000000000000000000000000000000004E00000000000000002B000000000000000000000000000000000000000000000000000000000000000000004E00000000000000002B000000000000000000000000000000000000000000000000000000000000000000004E00000000000000002B0000000000000000000000000000000000000000000000000000000000000102303243302020000000044900202020200000000000002020202000000000000020202020000000000000202020200000000000002020202000000000000020202020000000000000202020200000000000002020202000000000000020202020000000000000002230330000000000000000000000000000FFFFFFFFFFFF");
				gGate.isoData = lRecvByteAry;
				intr.iso2Host();
				gGb.showLogMessage("D","acer_response code="+gGate.acerField[39]);
				slResultOfRecvAcer = HpeUtil.byte2HexStr(lRecvByteAry);
				if (!"".equals(slResultOfRecvAcer)) { 
					gGb.showLogMessage("D","acer_isoData HEX receive= "+ slResultOfRecvAcer);
					gGb.showLogMessage("D","acer_isoData receive= "+new String(lRecvByteAry));
					if ("0210".equals(gGate.mesgType)){
						//kevin:比對從 ACER 取回的 data
						if (!"00".equals(gGate.acerField[39])) {
							/** 暫定錯誤碼2F **/
//							gTa.getAndSetErrorCode("2A");
							gGate.readFromAcerSuccessful = false;
						}
						else {
							gGb.showLogMessage("D","acerField[38]="+gGate.acerField[38]);
							gGb.showLogMessage("D","acerField[39]="+gGate.acerField[39]);
							gGb.showLogMessage("D","acerField[41]="+gGate.acerField[41]);
							gGb.showLogMessage("D","acerField[48]="+gGate.acerField[48]);
							gGb.showLogMessage("D","acerField[63]="+gGate.acerField[63]);
							gGate.signBalance="P";/** P 正數 **/
							if ("2D".equals(gGate.acerField[63].substring(72, 74))) {
								gGate.signBalance="N";/** N 負數 **/
							}
							gGate.pointRedemption = gGate.acerField[63].substring(112, 122);//扣抵點數
							gGate.pointBalance = gGate.acerField[63].substring(76, 86); //剩餘點數
							gGate.paidCreditAmt = gGate.acerField[63].substring(122, 134);; //支付金額
							gGate.loyaltyTxId = "1";
							
							if (Integer.parseInt(gGate.paidCreditAmt) < Integer.parseInt(gGate.acerField[4])) {
								gGate.loyaltyTxId = "3";
								gGate.remainingCreditAmt = String.format("%012d", (int)(Integer.parseInt(gGate.acerField[4])-Integer.parseInt(gGate.paidCreditAmt)));
							}
							else {
								gGate.loyaltyTxId = "2";
								gGate.remainingCreditAmt = String.format("%012d", 0);
							}
							gGate.f58T21 = "000000" + gGate.loyaltyTxId + gGate.acerField[39] + gGate.signBalance + gGate.pointBalance + gGate.pointRedemption + gGate.remainingCreditAmt;
							gGb.showLogMessage("D","G_Gate.loyaltyTxId ="+ gGate.loyaltyTxId);
							gGb.showLogMessage("D","G_Gate.signBalance ="+ gGate.signBalance);
							gGb.showLogMessage("D","G_Gate.pointBalance ="+ gGate.pointBalance);
							gGb.showLogMessage("D","G_Gate.pointRedemption ="+ gGate.pointRedemption);
							gGb.showLogMessage("D","G_Gate.paidCreditAmt ="+ gGate.paidCreditAmt);
							gGb.showLogMessage("D","G_Gate.remainingCreditAmt ="+ gGate.remainingCreditAmt);
							gGb.showLogMessage("D","G_Gate.f58t21 ="+ gGate.f58T21);
						}
						gGate.bankBit39Code = gGate.acerField[39];
						blSucFlag = true;
					}
					else if ("0410".equals(gGate.mesgType)){
						//比對從 ACER 取回的 data
							if (!"00".equals(gGate.acerField[39])) {
								/** 暫定錯誤碼2F **/
//								gTa.getAndSetErrorCode("2A");
								gGate.readFromAcerSuccessful = false;
							}
							gGate.bankBit39Code = gGate.acerField[39];
							blSucFlag = true;
					}
					blKeepReceiving = false;
				}

			}//end while


			lSocketToAcer.close();

			if (!blSucFlag) {/** time out **/
				/** 恢復成原值 **/
//				gGate.readFromAcerSuccessful = false;
				blResult = false;
			}
			else {

//				gGate.readFromAcerSuccessful = true;
				blResult = true;
			}
		} catch (Exception e) {
//			gGate.readFromAcerSuccessful = false;
			blResult = false;
		}
//		if (!gGate.readFromAcerSuccessful) {
//			/** 暫定錯誤碼2D **/
//			gTa.getAndSetErrorCode("2E");
//		}
			/** 恢復成原值 **/
		gGate.mesgType = slSrcMesgType;

		return blResult;

	}
}
