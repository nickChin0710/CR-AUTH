/**
 * Proc 授權 NEG request 交易轉送 FISC 控制
 * 將交易送出給FISC，並等待回應
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 *
 * @param negOut 透過此 output stream 將資料送給 FISC
 * @param intr 用來將class data 轉為 ISO String的 formatter
 * @throws  Exception if any exception occurred
 * 
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       Proc 授權 NEG request 交易轉送 FISC 控制          *
 * 2023/01/12  V1.00.33  Kevin       財金國內掛卡連線新增Keepalive功能               *
 *                                                                            *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.process;

import java.io.BufferedOutputStream;

import com.tcb.authProg.iso8583.FormatInterChange;
import com.tcb.authProg.main.AuthGlobalParm;

public class ProcNegToFisc extends AuthProcess {

	public ProcNegToFisc(AuthGlobalParm gb, AuthTxnGate gate) {
			this.gGb    = gb;
			this.gGate  = gate;
			
			gb.showLogMessage("I","ProcNegToFisc : started");

	}
	
	public void sendReqToFiscNeg(BufferedOutputStream negOut,FormatInterChange intr) throws Exception {

		if ("Y".equals(gGb.getIfReturnTrueDirectly())) {
			String slTmp = "不做檢核，直接回應交易成功";
			//System.out.println(sL_Tmp);
			gGb.showLogMessage("I", "authMainControl:" + slTmp);
			return;
		}
		String respData = null;
		if (!gGb.isFiscNegConnectFail()) { 
			
			try {
				intr.host2Iso();
				negOut.write(gGate.isoData,0,gGate.totalLen);
				negOut.flush();
			}
			catch ( Exception ex ) {
				gGb.showLogMessage("I","send 0300 to FISC for NEG file failed ! : "+gGate.isoField[14]+" "+gGate.chanNum +"ex:"+ex);
			}
			gGb.setFiscPnt(gGb.getFiscPnt() + 1);
			if ( gGb.getFiscPnt() >= gGb.getMaxFisc() ){
				gGb.setFiscPnt(0); 
			}
			gGb.getFiscRequest().put(gGate.isoField[14],""+gGb.getFiscPnt());
			int k = gGb.getFiscPnt();
	
			gGb.showLogMessage("I","waiting for FISC NEG response : "+gGate.isoField[14]+" "+gGate.chanNum);
			/* 等待 FISC NEG 回覆訊息 */
			gGb.getDoneLock()[k] = new Object();
			synchronized ( gGb.getDoneLock()[k] ) {
				gGb.getDoneLock()[k].wait(6*1000);  
				gGb.getDoneLock()[k] = null; 
			}
	
			gGb.showLogMessage("I","notice NEG message receive ");
			respData = (String)gGb.getFiscResponse().get(gGate.isoField[14]);
			gGb.showLogMessage("I","notice NEG message receive respData = "+respData);
		}
		else {
			gGb.showLogMessage("E","notice FISC NEG connection failed. for 0300 txn="+gGate.isoField[14]+" ,chan number="+gGate.chanNum);
		}
		if ( respData == null ) {
			gGate.isoField[13] = "XX"; 
			return; 
		}
		gGb.getFiscResponse().remove(gGate.isoField[14]);
		gGb.getFiscRequest().remove(gGate.isoField[14]);

		String[] cvtData   = respData.split("@");
		gGate.isoField[38]  = cvtData[0];
		gGate.isoField[13]  = cvtData[1];
		gGate.isoField[120] = cvtData[2];
		gGate.isoField[122] = cvtData[3];
		gGate.isoField[123] = cvtData[4];
		return;
	}
	/**
	 * FISC NEG 回覆訊息 控制
	 * 
	 * @throws  Exception if any exception occurred
	 */
	public void fiscNegResponse() throws Exception {

		String cvtPnt = (String)gGb.getFiscRequest().get(gGate.isoField[14]);
		if ( cvtPnt == null ) {
			gGb.showLogMessage("I","fiscNeg TIME-OUT!"); 
			return; 
		}

		String respData = gGate.isoField[38]+"@"+gGate.isoField[13]+"@"+gGate.isoField[120]+"@"+gGate.isoField[122]+"@"+gGate.isoField[123]+"@"+"N@";
		gGb.getFiscResponse().put(gGate.isoField[14],respData);

		/* FISCNEG 回覆訊息 通知處理完成 */
		int k = Integer.parseInt(cvtPnt);

		gGb.showLogMessage("D","fisc neg response : "+gGate.isoField[14]+" "+gGate.chanNum+" "+gGate.isoField[13]+" "+k);

		synchronized (gGb.getDoneLock()[k]) {
			gGb.getDoneLock()[k].notify();
		}
		return;
	}
}
