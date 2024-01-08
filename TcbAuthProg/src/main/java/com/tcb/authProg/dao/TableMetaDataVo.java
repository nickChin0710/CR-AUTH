/**
 * 授權資料存取物件DAO（data TableMetaDataVo object)
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
 * 2022/03/12  V1.00.39  Kevin       db 連線異常修復                              *
 ******************************************************************************
 */

package com.tcb.authProg.dao;

import com.tcb.authProg.util.HpeConst;


public class TableMetaDataVo {

	public int getColumnCount() {
		return columnCount;
	}
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}
	public Integer[] getDataLength() {
		return dataLength;
	}
	public void setDataLength(Integer[] dataLength) {
		this.dataLength = dataLength;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String[] getColumnName() {
		return columnName;
	}
	public void setColumnName(String[] columnName) {
		this.columnName = columnName;
	}
	public String[] getDataType() {
		return dataType;
	}
	public void setDataType(String[] dataType) {
		this.dataType = dataType;
	}
	public TableMetaDataVo() {
		// TODO Auto-generated constructor stub
		columnName   = new String[HpeConst.TABLE_COL_LIMIT];
		dataLength   = new Integer[HpeConst.TABLE_COL_LIMIT];
		dataType   = new String[HpeConst.TABLE_COL_LIMIT];
		
	}

	
	
	public String tableName;
	public int columnCount = 0;
	
	public  String[]  columnName   = null;
	public  Integer[]  dataLength   = null;
	public  String[]  dataType   = null;
	
	
	
}
