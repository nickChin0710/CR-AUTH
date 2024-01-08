/**
 * 授權使用格式轉換TAG物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用格式轉換TAG物件                       *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

public class TagObject {

	public String getObjId() {
		return objID;
	}

	public void setObjId(String objId) {
		objID = objId;
	}

	public String getObjData() {
		return objDATA;
	}

	public void setObjData(String objData) {
		objDATA = objData;
	}

	public TagObject() {
		// TODO Auto-generated constructor stub
	}

	public String objID = "";
	public String objDATA = "";
}
