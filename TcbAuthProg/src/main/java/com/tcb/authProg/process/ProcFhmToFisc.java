/**
 * Proc 授權 FHM request 交易轉送 FISC 控制
 * 將交易送出給FISC，並等待回應
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 *
 * @param fhmOut 透過此 output stream 將資料送給 FISC
 * @param intr 用來將class data 轉為 ISO String的 formatter
 * @throws  Exception if any exception occurred
 * 
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       Proc 授權 FHM request 交易轉送 FISC 控制     *
 * 2023/01/12  V1.00.33  Kevin       財金國內掛卡連線新增Keepalive功能               *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import java.io.BufferedOutputStream;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.iso8583.FormatInterChange;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.util.HpeUtil;

public class ProcFhmToFisc extends AuthProcess{

	public ProcFhmToFisc(AuthGlobalParm gb, AuthTxnGate gate) {
			this.gGb    = gb;
			this.gGate  = gate;
			
			gb.showLogMessage("I","ProcFhmToFisc : started");

	}
	// FHM request 交易轉送 FISC 控制
	/**
	 * 將交易送出給FISC，並等待回應
	 * 
	 * @param fhmOut 透過此 output stream 將資料送給 FISC
	 * @param intr 用來將class data 轉為 ISO String的 formatter
	 * @throws  Exception if any exception occurred
	 */
	public void sendReqToFiscFhm(BufferedOutputStream fhmOut,FormatInterChange intr) throws Exception {

		if ("Y".equals(gGb.getIfReturnTrueDirectly())) {
			String slTmp = "不做檢核，直接回應交易成功";
			//System.out.println(sL_Tmp);
			gGb.showLogMessage("I", "sendReqToFiscFhm:" + slTmp);
			return;
		}

		intr.host2Iso();
		String[] split = HpeUtil.byte2HexSplit(gGate.isoData);
		String printHex = "";
		for (int p=0; p<gGate.totalLen; p++) {
			if ((p+1)%16 == 0 || p+1==gGate.totalLen) {
				printHex = printHex +":"+ split[p];
				gGb.showLogMessage("D","FHM-send out iso hexString =>"+printHex);
				printHex = "";
				continue;
			}
			printHex = printHex +":"+ split[p];
		}
		fhmOut.write(gGate.isoData,0,gGate.totalLen);
		fhmOut.flush();
		gGb.showLogMessage("D", "REQUEST0 gGate.isoField[11] = "+gGate.isoField[11]);
		gGb.showLogMessage("D", "REQUEST1 gGb.getFiscPnt() = "+gGb.getFiscPnt());
		gGb.setFiscPnt(gGb.getFiscPnt() + 1);
		if ( gGb.getFiscPnt() >= gGb.getMaxFisc() ){
			gGb.setFiscPnt(0); 
		}
		gGb.getFiscRequest().put(gGate.isoField[11],""+gGb.getFiscPnt());

		gGb.showLogMessage("D", "REQUEST2 fiscRequest.get(gGate.isoField[11]) = "+gGb.getFiscRequest().get(gGate.isoField[11]));
//		gGb.setFhmPnt(gGb.getFhmPnt() + 1);
		int k = gGb.getFiscPnt();
		gGb.showLogMessage("D", "REQUEST3 gGb.getFiscPnt() = "+gGb.getFiscPnt());

		gGb.showLogMessage("I","waiting for FISC response : "+gGate.isoField[11]+" "+gGate.chanNum);
		/* 等待 FISC 回覆訊息 */
		gGb.showLogMessage("D", "REQUEST4 [K] = "+k);
		gGb.getDoneLock()[k] = new Object();
		gGb.showLogMessage("D", "REQUEST gGb.getDoneLock()[k] = "+gGb.getDoneLock()[k]);
		synchronized ( gGb.getDoneLock()[k] ) {
			gGb.getDoneLock()[k].wait(6*1000);  
			gGb.getDoneLock()[k] = null; 
		}

		gGb.showLogMessage("I","notice message receive ");
		String respData = (String)gGb.getFiscResponse().get(gGate.isoField[11]);
		if ( respData == null ) {
			gGate.isoField[39] = "XX"; 
			return; 
		}
		gGb.getFiscResponse().remove(gGate.isoField[11]);
		gGb.getFiscRequest().remove(gGate.isoField[11]);

		String[] cvtData   = respData.split("@");
		gGate.isoField[38]  = cvtData[0];
		gGate.isoField[39]  = cvtData[1];
		gGate.isoField[120] = cvtData[2];
		gGate.isoField[122] = cvtData[3];
		gGate.isoField[123] = cvtData[4];
		return;
	}
	/* FiscFhm 回覆訊息 控制 */
	/**
	 * FiscFhm 回覆訊息 控制
	 * 
	 * @throws  Exception if any exception occurred
	 */
	public void fiscFhmResponse() throws Exception {

		String cvtPnt = (String)gGb.getFiscRequest().get(gGate.isoField[11]);
		gGb.showLogMessage("D", "RESPONSE cvtPnt = "+cvtPnt);
		if ( cvtPnt == null ) {
			gGb.showLogMessage("I","fisc TIME-OUT!"); 
			return; 
		}
		gGb.showLogMessage("D", "kevin test gGate.isoField[122] = "+ gGate.isoField[122]);
		String respData = gGate.isoField[38]+"@"+gGate.isoField[39]+"@"+gGate.isoField[120]+"@"+gGate.isoField[122]+"@"+gGate.isoField[123]+"@"+"N@";
		gGb.getFiscResponse().put(gGate.isoField[11],respData);

		/* FISC 回覆訊息 通知處理完成 */
		int k = Integer.parseInt(cvtPnt);
		gGb.showLogMessage("D", "RESPONSE k = "+k);

		gGb.showLogMessage("D","fisc response : "+gGate.isoField[11]+" "+gGate.chanNum+" "+gGate.isoField[39]+" "+k);

		gGb.showLogMessage("D", "RESPONSE gGb.getDoneLock()[k] = "+gGb.getDoneLock()[k]);
		synchronized (gGb.getDoneLock()[k]) {
			gGb.getDoneLock()[k].notify();
		}
		return;
	}
}
