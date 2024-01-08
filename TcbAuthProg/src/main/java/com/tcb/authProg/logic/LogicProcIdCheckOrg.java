/**
 * 授權邏輯查核-信用卡輔助身分驗證處理
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
 * 2021/02/08  V1.00.00  Kevin       授權邏輯查核-信用卡輔助身分驗證處理               *
 *                                                                            *
 ******************************************************************************
 */

package com.tcb.authProg.logic;

import java.util.Arrays;

import com.tcb.authProg.dao.TableAccess;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

public class LogicProcIdCheckOrg extends AuthLogic {

	public LogicProcIdCheckOrg(AuthGlobalParm gb, AuthTxnGate gate, TableAccess ta) {
		this.gb    = gb;
		this.gGate = gate;
		this.ta    = ta;
		
		gb.showLogMessage("I","LogicProcIdCheckOrg : started");

	}
	
	
	/**
	 * 信用卡輔助身分驗證
	 * @return 如果是信用卡輔助身分驗證，而且身分證號、效期、手機、生日比對失敗，則return false，否則return true
	 * @throws Exception if any exception occurred
	 */
	public boolean processIdCheckOrg() throws Exception {
		boolean blResult = true;
		if (gGate.isIdCheckOrg) {
			if (gGate.isoFiled4Value > 0) {     //信用卡輔助身分驗證，金額不能大於0
				ta.getAndSetErrorCode("IP");
				return false;
			}
			gGate.idCheckType = getIdCellBirthdayFromIso();	
			int[] ilIdCheckResult = {0,0,0,0};		
			if (gGate.f58T67Id.length() > 0) {  //信用卡輔助身分驗證ID檢核
				if (!verifyId(gGate.f58T67Id)) {
					ilIdCheckResult[0] = 1;
				}
			}
			else {
				ta.getAndSetErrorCode("D9");    //信用卡輔助身分驗證，沒有帶ID拒絕交易
				return false;
			}
			if ("7321".equals(gGate.mccCode)) {        //信用卡輔助身分驗證，效期檢核
				if (!verifyIssueDare()) {
					ilIdCheckResult[1] = 1;
				}
			}
			if (gGate.f58T87CellPhone.length() > 0) {  //信用卡輔助身分驗證，手機檢核
				if (!verifyCellPhone(gGate.f58T87CellPhone)) {
					ilIdCheckResult[2] = 1;
				}
			}
			if (gGate.f58T87Birthday.length() > 0) {   //信用卡輔助身分驗證，生日檢核
				if (!verifyBirthday(gGate.f58T87Birthday)) {
					ilIdCheckResult[3] = 1;
				}
			}
			String slIdCheckResult = Arrays.toString(ilIdCheckResult);

			gb.showLogMessage("D","slIdCheckResult = " + slIdCheckResult);
			
			if	("0".equals(gGate.idCheckType)) {                  //信用卡輔助身分驗證成功
				if ("[0, 0, 0, 0]".equals(slIdCheckResult)) {
					blResult = true;
				}
				else if ("[1, 0, 0, 0]".equals(slIdCheckResult)) { //信用卡輔助身分驗證，ID失敗
					ta.getAndSetErrorCode("D9");
					blResult = false;
				}
				else if ("[0, 1, 0, 0]".equals(slIdCheckResult)) { //信用卡輔助身分驗證，效期錯誤
					ta.getAndSetErrorCode("E0");
					blResult = false;
				}
				else if ("[1, 1, 0, 0]".equals(slIdCheckResult)) { //信用卡輔助身分驗證，效期錯誤
					ta.getAndSetErrorCode("E0");
					blResult = false;
				}
			}
			else {
				switch (slIdCheckResult) {					
					case "[0, 0, 0, 0]" : blResult = true; break;      //信用卡輔助身分驗證成功					
					case "[1, 0, 0, 0]" : ta.getAndSetErrorCode("IA"); //信用卡輔助身分驗證，ID錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 0, 1, 0]" : ta.getAndSetErrorCode("IB"); //信用卡輔助身分驗證，手機錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 1, 0, 0]" : ta.getAndSetErrorCode("IC"); //信用卡輔助身分驗證，效期錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 0, 0, 1]" : ta.getAndSetErrorCode("ID"); //信用卡輔助身分驗證，生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[1, 0, 1, 0]" : ta.getAndSetErrorCode("IE"); //信用卡輔助身分驗證，ID+手機錯誤
									      gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
									      blResult = false; break;
					case "[1, 1, 0, 0]" : ta.getAndSetErrorCode("IF"); //信用卡輔助身分驗證，ID+效期錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[1, 0, 0, 1]" : ta.getAndSetErrorCode("IG"); //信用卡輔助身分驗證，ID+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 1, 0, 1]" : ta.getAndSetErrorCode("IH"); //信用卡輔助身分驗證，效期+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 0, 1, 1]" : ta.getAndSetErrorCode("II"); //信用卡輔助身分驗證，手機+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[0, 1, 1, 0]" : ta.getAndSetErrorCode("IJ"); //信用卡輔助身分驗證，效期+手機錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[1, 1, 1, 0]" : ta.getAndSetErrorCode("IK"); //信用卡輔助身分驗證，ID+效期+手機錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;
					case "[1, 1, 0, 1]" : ta.getAndSetErrorCode("IL"); //信用卡輔助身分驗證，ID+效期+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;	
					case "[0, 1, 1, 1]" : ta.getAndSetErrorCode("IM"); //信用卡輔助身分驗證，效期+手機+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;	
					case "[1, 0, 1, 1]" : ta.getAndSetErrorCode("IN"); //信用卡輔助身分驗證，ID+手機+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;	
					case "[1, 1, 1, 1]" : ta.getAndSetErrorCode("IO"); //信用卡輔助身分驗證，ID+效期+手機+生日錯誤
										  gGate.isoField[38] = HpeUtil.replaceIndex(0, 1, gGate.idCheckType, ta.getValue("NCCC_P38"));
										  blResult = false; break;	
					default   : break;									
				}
			}
		}
		return blResult;
	}
	
	
	/**
	 * 從ISO8583中取得生日&行動電話
	 * @return 身分證號 
	 */
	private String getIdCellBirthdayFromIso() {
		String slIdCheckType = "";
		if (gGate.f58T87.length() > 0) {
			int lenTot = 0;
			int lenSub = 0;
			String checkTag = "";
			while (lenTot < gGate.f58T87.length()) {
				checkTag = gGate.f58T87.substring(lenTot, lenTot + 2);
				lenTot = lenTot + 2;
				lenSub = Integer.parseInt(gGate.f58T87.substring(lenTot, lenTot+2));
				lenTot = lenTot + 2;

				gb.showLogMessage("D","f58T87檢查 = "+ checkTag +" = "+ gGate.f58T87.substring(lenTot, lenTot+lenSub));

				if ("01".equals(checkTag)) {
					gGate.f58T87Birthday = gGate.f58T87.substring(lenTot, lenTot+lenSub);
				}
				else if ("02".equals(checkTag)) {
					gGate.f58T87CellPhone = gGate.f58T87.substring(lenTot, lenTot+lenSub);

				}
				lenTot = lenTot+lenSub;
				lenSub = 0;
			}
		}
		if ("7321".equals(gGate.mccCode)) { 
			if (gGate.f58T67Id.length() > 0) {
				slIdCheckType = "0";
				if (gGate.f58T87CellPhone.length() > 0) {
					slIdCheckType = "4";
					if (gGate.f58T87Birthday.length() > 0) {
						slIdCheckType = "6";
					}
				}
				else if (gGate.f58T87Birthday.length() > 0) {
					slIdCheckType = "5";
				}
			}
		}
		else {
			if (gGate.f58T67Id.length() > 0) {
				slIdCheckType = "0";
				if (gGate.f58T87CellPhone.length() > 0) {
					slIdCheckType = "1";
					if (gGate.f58T87Birthday.length() > 0) {
						slIdCheckType = "3";
					}
				}
				else if (gGate.f58T87Birthday.length() > 0) {
					slIdCheckType = "2";
				}
			}
		}



		return slIdCheckType;
	}
	
	
	/**
	 * 檢核Issue date是否超過180天
	 * @param spID 將被檢核的ID
	 * @return 如果Issue date超過180天檢核正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyIssueDare() throws Exception {
		boolean blResult = true;

		gb.showLogMessage("D","verifyIssueDare = "+ ta.getValue("CardIssueDate") +"**"+ gb.getSysDate() +"**"+ HpeUtil.compareDateDiffOfDay(ta.getValue("CardIssueDate"), gb.getSysDate()));

		if (HpeUtil.compareDateDiffOfDay(ta.getValue("CardIssueDate"), gb.getSysDate())<180) {
			blResult = false;
		}
		return blResult;
	}
	/**
	 * 檢核手機 是否正確
	 * @param spCell 將被檢核的ID
	 * @return 如果ID檢核正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyCellPhone(String spCell) throws Exception {
		boolean blResult = true;

		gb.showLogMessage("D","verifyCellPhone = "+ ta.getValue("CrdIdNoCellPhone") +"**"+ spCell +"**"+ spCell.equals(ta.getValue("CrdIdNoCellPhone")));

		if (!spCell.equals(ta.getValue("CrdIdNoCellPhone")))
			blResult = false;

		return blResult;
	}
	/**
	 * 檢核生日 是否正確
	 * @param spBirthday 將被檢核的ID
	 * @return 如果生日檢核正確，return true，否則return false
	 * @throws Exception if any exception occurred
	 */
	private boolean verifyBirthday(String spBirthday) throws Exception {
		boolean blResult = true;

		gb.showLogMessage("D","verifyBirthday = "+ ta.getValue("CrdIdNoBirthday") +"**"+ spBirthday +"**"+ spBirthday.equals(ta.getValue("CrdIdNoBirthday")));

		if (!spBirthday.equals(ta.getValue("CrdIdNoBirthday")))
			blResult = false;

		return blResult;
	}


}
