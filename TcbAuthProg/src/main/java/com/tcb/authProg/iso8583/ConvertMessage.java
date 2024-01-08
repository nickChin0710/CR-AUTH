/**
 * 授權使用格式轉換共用物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用格式轉換共用物件                       *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

import java.util.HashMap;

//import org.apache.log4j.*;
import org.apache.logging.log4j.Logger;

import com.tcb.authProg.process.AuthTxnGate;

public class ConvertMessage {
	public String[] headField = new String[15];

	Logger logger = null;
	AuthTxnGate gate = null;
	HashMap cvtHash = null;

	public ConvertMessage() {
	}

	/* VISA OR MASTER */
	public boolean convertToCommon() {
		if (!convertMesgType("C")) {
			return false;
		}

		if (!gate.srcFormatType.equals("BIC")) {
			if (gate.requestTrans) {
				convertIsoField("C");
			} else {
				restoreIsoField("C");
			}
		}

		return true;
	}

	public boolean convertToInterChange() {
		String cvtType = "", cvtIsoField = "";

		if (!convertMesgType("I")) {
			return false;
		}

		if (!gate.destFormatType.equals("BIC")) {
			if (gate.requestTrans) {
				convertIsoField("I");
			} else {
				restoreIsoField("I");
			}
		}

		return true;
	}

	public boolean convertMesgType(String cvtCode) {
		String cvtType = (String) cvtHash.get(cvtCode + "-0-" + gate.mesgType);
		if (cvtType == null) {
			logger.error("convert to message type Error " + cvtCode + " " + gate.mesgType);
			return false;
		}

		String[] cvtData = cvtType.split("-");
		gate.mesgType = cvtData[0];
		gate.txType = cvtData[1];
		if (gate.txType.equals("0")) {
			gate.requestTrans = true;
		} else {
			gate.requestTrans = false;
		}

		return true;
	}

	public boolean convertIsoField(String cvtCode) {
		String cvtIsoField = "";

		gate.originator = "6";
		headField[7] = "00";
		headField[12] = "00";
		if (cvtCode.equals("I") && gate.destIntfName.equals("VISA")) {
			gate.destStation = "000000";
			gate.srcStation = gate.stationId;
		}

		gate.orgiReserve = gate.destStation + "-" + gate.srcStation + "-" + headField[7] + "-" + headField[12] + "-"
				+ gate.isoField[3] + "-" + gate.isoField[22] + "-" + gate.isoField[25] + "-" + "#";

		if (gate.isoField[3].length() > 0) {
			cvtIsoField = (String) cvtHash.get(cvtCode + "-3-" + gate.isoField[3]);
			if (cvtIsoField != null) {
				gate.isoField[3] = cvtIsoField;
			}
		}

		if (gate.isoField[22].length() > 0) {
			cvtIsoField = (String) cvtHash.get(cvtCode + "-22-" + gate.isoField[22]);
			if (cvtIsoField != null) {
				gate.isoField[22] = cvtIsoField;
			}
		}

		if (gate.isoField[25].length() > 0) {
			cvtIsoField = (String) cvtHash.get(cvtCode + "-25-" + gate.isoField[25]);
			if (cvtIsoField != null) {
				gate.isoField[25] = cvtIsoField;
			}
		}

		return true;
	}

	public boolean restoreIsoField(String cvtCode) {
		gate.respondor = "6";

		String[] tmpString = gate.orgiReserve.split("-");
		gate.destStation = tmpString[0];
		gate.srcStation = tmpString[1];
		headField[7] = tmpString[2];
		headField[12] = tmpString[3];
		gate.isoField[3] = tmpString[4];
		gate.isoField[22] = tmpString[5];
		gate.isoField[25] = tmpString[6];

		String cvtStation = gate.destStation;
		gate.destStation = gate.srcStation;
		gate.srcStation = cvtStation;
		return true;
	}

} // end of class ConvertMessage