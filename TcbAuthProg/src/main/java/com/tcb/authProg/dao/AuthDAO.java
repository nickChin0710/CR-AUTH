/**
 * 授權資料存取物件DAO（data access object)
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
 * 2021/02/08  V1.00.00  Kevin       授權資料存取物件DAO（data access object)      *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      *
 * 2022/03/12  V1.00.39  Kevin       db 連線異常修復                              *
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *
 * 2023/12/05  V1.00.60  Kevin       SQL EXCEPTION處理                         *
 ******************************************************************************
 */

/*
Timestamp to String:
SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
Timestamp now = new Timestamp(System.currentTimeMillis());
String str = df.format(now);

String to Timestamp:
SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String time = df.format(new Date());
Timestamp ts = Timestamp.valueOf(time);

 * */
package com.tcb.authProg.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;

import com.ibm.db2.jcc.DB2Diagnosable;  // Import packages for DB2       
import com.ibm.db2.jcc.DB2Sqlca;
import com.ibm.db2.jcc.am.SqlDataException;
import com.tcb.authProg.main.AuthGlobalParm;
import com.tcb.authProg.process.AuthTxnGate;
import com.tcb.authProg.util.HpeUtil;

@SuppressWarnings("unchecked")
public class AuthDAO {

	
	/* private variable */

	private  int  tbLimit   = 23;
	private  int  colLimit  = 500;

	private  Object[]    parmData     = new Object[colLimit];
	//private  String[]  parmData     = new String[colLimit];
	private  String[]  parmType     = new String[colLimit];
	private  String[]  columnName   = new String[colLimit];
	private  String[]  insertColumn = new String[colLimit];
	private  String[]  dataType     = new String[colLimit];
	private  Integer[] dataLength   = new Integer[colLimit];
	private  Integer[] dataScale    = new Integer[colLimit];

	private  String    sqlCmd="",columnString = "",valueString="";

	private  String[]  updateType   = new String[colLimit];
	private  String[]  updateColumn = new String[colLimit];
	private  String[]  updateValue  = new String[colLimit];

	private  HashMap   outputHash = new  HashMap();
	private  HashMap   accessHash = new  HashMap();
	private  HashMap[] arrayHash  = new  HashMap[5];

	private  int[]      loadCnt        = {0,0,0,0,0,0,0,0,0};
	private  int[]      loadColumnCnt  = {0,0,0,0,0,0,0,0,0};
	private  String[][] loadColumnName = new String[10][colLimit];

	private  int     ci=0,li=0,retCode=0,parmCount=0,dbCount=0,updateCnt=0;
	private  int     columnCnt  = 0;

	/* public variable */

	public   int     loadRow=1000,loadLimit=100000;

	public   String  dataBase="",whereStr="",whereStr2="",dispMesg="",nullString="",whereStr3="";
	public   String  accessCode="",daoTable="",daoTable2="",notFound="",dupRecord="",specialCode="";
	public   String  unionMinus="",indexHint="",insertSQL="",selectSQL="",selectSQL2="",updateSQL="",debugInsert="";
	public   String  showNotFound="",covertBig5="",saveCode="";

	public   AuthGlobalParm  gb = null;
	public   AuthTxnGate  gate = null;
//	AuthDAO() {
//
//
//		return;
//	}

	/*
	public boolean checkTable(String parmTable) {

		PreparedStatement ps = null;
		ResultSet         rs = null;
		try {

			String checkSql = "select 1 from and rownum < 2"+parmTable;
			ps = gb.conn.prepareStatement(checkSql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);

			rs = ps.executeQuery();
			rs.close();
			ps.close();
			rs = null;
			ps = null;
		}
		catch ( Exception ex ) {
			return false; 
		}

		finally {
			try {
				if ( rs != null ) { rs.close(); }
				if ( ps != null ) { ps.close(); }
			}
			catch ( Exception ex2 ) {

			}
		}

		return true;
	}
	*/
	public boolean insertTable() {

		PreparedStatement ps = null;
		try {

			String insertData="",insertValue="";

			dupRecord = "";
			setConnectIndex();

			processColumnName("");

			StringBuffer colbuf = new StringBuffer();
			StringBuffer valbuf = new StringBuffer();
			columnString = "";
			valueString  = "";
			for ( int i=0; i < columnCnt; i++ )
			{
				insertColumn[i] = null;
				/*
				String ckString = (String)getValue(columnName[i]);
				if ( ckString == null ) {
					continue; 
				}
				*/
				
				insertColumn[i]="Y";
				colbuf.append((columnName[i]+ ","));
				//if ( dataType[i].equals("DATE") && gb.dbType[ci].substring(0,3).equals("ORA") ) {
				if ( dataType[i].equals("DATE")  ) {
					valbuf.append("TO_DATE(?,'YYYYMMDDHH24MISS'),"); 
				}
				else if ( dataType[i].equals("TIMESTAMP")  ) {
					//valbuf.append("TIMESTAMP(?,'YYYYMMDDHH24MISSSSSSSS'),");
					//valbuf.append("TIMESTAMP(CAST (? AS VARCHAR))),");
					valbuf.append("?,"); 
				}
				else {
					valbuf.append("?,");  
				}

				/*
            if ( dataType[i].equals("DATE") && gb.dbType[ci].substring(0,3).equals("ORA") )
               { valbuf.append("TO_DATE(?,'YYYYMMDDHH24MISS'),"); }
            else
               { valbuf.append("?,");  }
				 */
			}

			columnString = colbuf.toString();
			columnString = columnString.substring(0,columnString.length()-1);
			valueString  = valbuf.toString();
			valueString  = valueString.substring(0,valueString.length()-1);

			sqlCmd = "INSERT INTO " + daoTable
					+ " ( " + columnString + " ) VALUES "
					+ " ( " + valueString  + " ) ";
			
			ps = getDatabaseConnect().prepareStatement(sqlCmd);

			debugInsert="Y";
			for( int i=0; i < columnCnt; i++ ) {

				if ( insertColumn[i] == null ) {
					continue; 
				}
				/*
				if ( debugInsert.equals("Y") ) {
					////System.out.println("insertTable=>" + columnName[i]+" : "+getValue(columnName[i])+" : "+getValue(columnName[i]).getBytes().length);
					gb.showLogMessage("D","insertTable",columnName[i]+" : "+getValue(columnName[i])+" : "+getValue(columnName[i]).getBytes().length); 
				}
				*/
				if ( dataType[i].equals("DECIMAL")) {
					//ps.setDouble(i+1,getDouble(columnName[i]));
					 ps.setBigDecimal(i+1, new BigDecimal(getDouble(columnName[i])));
				}
				else if ( dataType[i].equals("TIMESTAMP")) {
					
					ps.setTimestamp(i+1,getTimeStamp(columnName[i])); 
				}
				else {
					ps.setString(i+1,getValue(columnName[i]));  
				}
			}
//			System.out.println("SQL=>" + sqlCmd + "--");




			retCode  = ps.executeUpdate();
			ps.close();
			dataBase = "";
			if ( retCode == 0 ) {
				dispMesg = "executeUpdate return code == 0";  
				return false; 
			}
		}
		catch ( SqlDataException ex3 ) {

			String slMsg = HpeUtil.getStackTrace(ex3);
			////System.out.println("Exp msg=>" + sL_Msg + "---");
			gb.showLogMessage("E","Exp msg=> "+ slMsg + "==");
			if ((gb.getTableOwner().trim()+".CCA_AUTH_BITDATA").equals(daoTable)) {
				gb.showLogMessage("D","CCA_AUTH_BITDATA EX3 ERROR bypass table => "+ daoTable + "==");
				return true; 
			}
			gb.setExpMethod("insertTable");  
			gb.expHandle(ex3, true); 
			return false; 
		}

		catch ( SQLException ex2 ) {

			if ( ex2.getErrorCode() == 1 ) {

				dupRecord = "Y";
				gb.showLogMessage("E","insert "+daoTable.toUpperCase()+"," + ex2.getMessage());
				return true;
			}
			else {
				gb.setExpMethod("insertTable"); 
				gb.expHandle(ex2, true);  
				sqlExceptionChk(ex2);
				return false;  
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("insertTable");  
			gb.expHandle(ex, true); 
			return false; 
		}

		finally {
			try {
				if ( ps != null ) { 
					ps.close(); 
				}
			}
			catch ( Exception ex2 ) {

			}
		}
		return true;
	} // End of insertTable

	public boolean loadTable(int parmCode) {

		gb.dateTime();
		li = parmCode;
		if ( arrayHash[li] == null ) {
			arrayHash[li] = new HashMap();  
		}
		arrayHash[li].clear();
		loadCnt[li] = 0;
		accessCode = "L";
		columnCnt = 0;
		loadCnt[li] = 0;
		selectTable();
		accessCode = "";
		dataBase   = "";
		return true;
	}

	public ResultSet executeSelectCmd(String spSelectSql) {
		PreparedStatement ps = null;
		ResultSet         lRs = null;

		try {
			ps = getDatabaseConnect().prepareStatement(spSelectSql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			lRs = ps.executeQuery();


		} catch (Exception e) {
			// TODO: handle exception
			lRs = null;
		}

		return lRs;
	}
	public boolean selectTable() {

		PreparedStatement ps = null;
		ResultSet         rs = null;

		try {

			notFound  = "";
			setConnectIndex();

			processColumnName(selectSQL);
			processSelectColumn();
			sqlCmd = "SELECT "+indexHint+" "+columnString+" FROM "+daoTable+" "+whereStr;
			if ( unionMinus.length() >= 5 ) {
				String sqlCmd2 = "SELECT "+indexHint+" "+columnString+" FROM "+daoTable2+" "+whereStr2;
				sqlCmd = sqlCmd +" "+unionMinus+" "+sqlCmd2;
			}
			gb.showLogMessage("D","SELECT sql=>" + sqlCmd + "--");
			ps = getDatabaseConnect().prepareStatement(sqlCmd,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			setParmData(ps);

//			System.out.println("sql=>" + sqlCmd + "--");
			rs = ps.executeQuery();

			if ( accessCode.equals("L") ) {
				rs.setFetchSize(loadRow); 
			}
			else {
				rs.setFetchSize(1);  
			}

			retrieveTableData(rs);
			if ( notFound.equals("Y") ) {

				if ( showNotFound.equals("Y") )  {
					gb.showLogMessage("D","select " +  daoTable + ". DATA NOT FOUND! where string is =>"+ whereStr); 
				}
			}

			rs.close();
			ps.close();
			if ( accessCode.equals("L") || daoTable.trim().equals("DUAL") ) {

				loadColumnCnt[li] = columnCnt;
				for( int j=0; j < columnCnt; j++ ) {
					loadColumnName[li][j]= columnName[j]; 
				}
			}

			dataBase   = "";
			accessCode = "";
			whereStr   = "";
			resetParm();
		}
		
		catch ( SQLException ex ) {
			gb.setExpMethod("selectTable");
			gb.expHandle(ex, true); 
			sqlExceptionChk(ex);
			return false; 
		}
		catch ( Exception ex ) {
			gb.setExpMethod("selectTable");
			//System.out.println("Exception sql=>" + sqlCmd + "--");
			gb.expHandle(ex, true); 
			return false; 
		}

		finally {
			try {
				if ( rs != null ) { rs.close(); }
				if ( ps != null ) { ps.close(); }
			}
			catch ( Exception ex2 ) {

			}
		}

		return true;
	} // End of selectTable



	public ResultSet getTableResultSet() {
	
		PreparedStatement ps = null;
		ResultSet         rs = null;

		try {

			notFound  = "";
			setConnectIndex();

			processColumnName(selectSQL);
			processSelectColumn();
			sqlCmd = "SELECT "+indexHint+" "+columnString+" FROM "+daoTable+" "+whereStr;
			if ( unionMinus.length() >= 5 ) {   
				String sqlCmd2 = "SELECT "+indexHint+" "+columnString+" FROM "+daoTable2+" "+whereStr2;
				sqlCmd = sqlCmd +" "+unionMinus+" "+sqlCmd2;
			}
			ps = getDatabaseConnect().prepareStatement(sqlCmd,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			setParmData(ps);
			rs = ps.executeQuery();



			dataBase   = "";
			accessCode = "";
			whereStr   = "";
			resetParm();


		}
		catch (SQLException ex) {
			gb.setExpMethod("selectTable");  
			sqlExceptionChk(ex);
			gb.expHandle(ex, true); 
		}
		catch ( Exception ex ) {
			gb.setExpMethod("selectTable");  
			gb.expHandle(ex, true); 
			return null; 
		}



		return rs;
	} // End of selectTable

	private boolean retrieveTableData(ResultSet rs) {

		try {

			notFound  = "";
			int cntl  = 0;

			byte[] lData = null;
			String slData = "";
			while ( rs.next() ) {

				for( int k=0; k < columnCnt; k++ ) {
					/*
                			byte[] L_Data = P_Rs.getBytes(sP_ColumnName);
			sL_Result = new String(L_Data);

					 * */

					/*
				 L_Data = rs.getBytes(k+1);
				 sL_Data = new String(L_Data);

				 if ( accessCode.equals("L") ) {
					 setArrayData(saveCode+columnName[k],sL_Data,cntl); 
				 }
				 else if ( dataType[k].equals("DATETIME") && gb.dbType[ci].equals("MYSQL") ) {
					 setValue(saveCode+columnName[k],sL_Data.substring(0,19)); 
				 }
				 else {
					 setValue(saveCode+columnName[k],sL_Data); 
				 }
					 */

					////System.out.println(columnName[k] + "***" + rs.getString(k+1) + "***" + k);
					if ( accessCode.equals("L") ) {
						setArrayData(saveCode+columnName[k],rs.getString(k+1),cntl); 
					}
//					else if ( dataType[k].equals("DATETIME") && gb.dbType.equals("MYSQL") ) {
//						setValue(saveCode+columnName[k],rs.getString(k+1).substring(0,19)); 
//					}
					else {
						setValue(saveCode+columnName[k],rs.getString(k+1)); 
					}


				}
				saveCode="";
				cntl++;

				if ( accessCode.equals("L")  ) {
					loadCnt[li] = cntl; 
				}

				if ( accessCode.equals("S") || accessCode.equals("F") ) {
					break; 
				}
				else if ( accessCode.equals("L") && cntl > loadLimit ) {
					break; 
				}
			}

			if ( cntl == 0 ) {
				notFound = "Y";   
				return false;  
			}

		} // End of try

		catch ( Exception ex ) {
			gb.setExpMethod("retrieveTableData");  
			gb.expHandle(ex, true); 
			return false; 
		}

		return true;
	} // End of retrieveTableData

	public int updateTable() {

		PreparedStatement ps = null;
		try {

			setConnectIndex();
			notFound  = "";

			sqlCmd = "UPDATE "+daoTable+" SET "+updateSQL+" "+whereStr;
			gb.showLogMessage("D","UPDATE sql=>" + sqlCmd + "--");
			ps = getDatabaseConnect().prepareStatement(sqlCmd);
			setParmData(ps);
			updateCnt     = ps.executeUpdate();
			ps.close();
			dataBase  = "";
			updateSQL = "";
			whereStr  = "";
			dispMesg  = "updateTable";
			if ( updateCnt == 0 ) {
				notFound = "Y"; 
				dispMesg = "updateCnt == 0"; 
				return 0; 
			}
		}
		catch ( SQLException ex ) {
			gb.setExpMethod("updateTable");  
			sqlExceptionChk(ex);
			gb.expHandle(ex, true); 
			return 0; 
		}
		catch ( Exception ex ) {
			gb.setExpMethod("updateTable");  
			gb.expHandle(ex, true); 
			return 0; 
		}

		finally {
			try {
				if ( ps != null ) { ps.close(); }
			}
			catch ( Exception ex2 ) {

			}
		}
		return updateCnt;
	} // End of updateTable

	public boolean deleteTable() {

		PreparedStatement ps  = null;

		try {

			setConnectIndex();
			notFound  = "";

			sqlCmd  = "DELETE from "+ daoTable;
			sqlCmd  =  sqlCmd  + " "+ whereStr;
			ps      =  getDatabaseConnect().prepareStatement(sqlCmd);
			setParmData(ps);
			retCode =  ps.executeUpdate();
			ps.close();
			whereStr="";

			dispMesg  = "�R������";
			if ( retCode == 0 )
			{ notFound = "Y"; dispMesg = "�R�����~"; }

			dataBase  = "";
		}
		catch ( SQLException ex ) {
			gb.setExpMethod("deleteTable");  
			sqlExceptionChk(ex);
			gb.expHandle(ex, true); 
			return false; 
		}
		catch ( Exception ex ) {
			gb.setExpMethod("deleteTable");  
			gb.expHandle(ex, true); 
			return false; 
		}

		finally {
			try {
				if ( ps != null ) { ps.close(); }
			}
			catch ( Exception ex2 ) {

			}
		}

		return true;
	} // End of deleteTable

	public boolean executeSqlCommand(String sqlStatement) {

		PreparedStatement ps  = null;

		try {

			setConnectIndex();

			sqlCmd  = sqlStatement;
			ps      =  getDatabaseConnect().prepareStatement(sqlCmd);
			setParmData(ps);
			retCode =  ps.executeUpdate();
			ps.close();

			dispMesg  = "SQL ...";
			if ( retCode == 0 )
			{ notFound = "Y"; dispMesg = "SQL..."; }

			dataBase  = "";
			sqlCmd    = "";
		}

		catch ( Exception ex ) {
			gb.setExpMethod("executeSqlCommand");  
			gb.expHandle(ex, true); 
			return false; 
		}

		finally {
			try {
				if ( ps != null ) { ps.close(); }
			}
			catch ( Exception ex2 ) {

			}
		}

		return true;
	} // End of executeSqlCommand

	public boolean processColumnName(String columnData) {

		int      i=0,k=0,pnt=0;
		String   fieldString="",convertSQL="";
		String[] splitString;
		byte[]   cvtData;

		columnCnt=0;

		try {

			if ( columnData.length() == 0 ) {
				processFullColumn(); 
				return true; 
			}

			convertSQL  =  columnData;
//			if ( !gb.dbType.equals("SYBASE_ASE") && !gb.dbType.equals("SYBASE_IQ") ) {
//				convertSQL  =  columnData.toUpperCase(); 
//			}

			convertSQL  =  convertSQL.replaceAll(" as "," AS ");
			cvtData     =  convertSQL.getBytes();
			int  quoCnt =  0;
			for ( i=0; i<cvtData.length; i++ ) {

				if ( cvtData[i] == '(' ) {
					quoCnt++; 
				}
				else if ( cvtData[i] == ')' ) { 
					quoCnt--;  
					cvtData[i]='#'; 
				}

				if ( cvtData[i] == ',' && quoCnt > 0 ) {
					cvtData[i] =  '#'; 
				}
			}

			convertSQL   =  new String(cvtData);
			splitString  =  convertSQL.split(",");
			i=0; k=0;
			for( i=0; i<splitString.length; i++ ) {

				fieldString = splitString[i];
				pnt =  splitString[i].indexOf(" AS ");
				if ( pnt != -1 ) {
					fieldString  = splitString[i].substring(pnt+4); 
				}
				else {
					pnt =  splitString[i].lastIndexOf("(");
					if ( pnt != -1 ) {
						fieldString  = splitString[i].substring(pnt+1);
						pnt =  fieldString.indexOf("#");
						fieldString  = fieldString.substring(0,pnt);
					}
				}
				columnName[k] =  fieldString.trim();
				k++;
			}

			columnCnt  = k;
			for( i=0; i < columnCnt; i++ ) {

				pnt =  columnName[i].indexOf(".");
				columnName[i] = columnName[i].substring(pnt+1);
				dataType[i]   = "";
				////System.out.println(i+" "+columnName[i]);
			}
		}

		catch (Exception ex) {
			gb.setExpMethod("processColumnName");  
			gb.expHandle(ex, true); 
			return false; 
		}

		return true;
	} // End of processColumnName


	public boolean processFullColumn() {

		try {


			if (gb.tableMetaDataObjects.containsKey(daoTable)) {
				//tale meta data 已經存在 cache
				TableMetaDataVo L_TableMetaDataVo = (TableMetaDataVo)gb.tableMetaDataObjects.get(daoTable);
				

				System.arraycopy(L_TableMetaDataVo.columnName, 0, columnName, 0, L_TableMetaDataVo.columnName.length);
				System.arraycopy(L_TableMetaDataVo.dataLength, 0, dataLength, 0, L_TableMetaDataVo.dataLength.length);
				System.arraycopy(L_TableMetaDataVo.dataType, 0, dataType, 0, L_TableMetaDataVo.dataType.length);
				columnCnt = L_TableMetaDataVo.getColumnCount(); 

			}
			else {
				//tale meta data 尚未存在 cache
				Statement    st  = getDatabaseConnect().createStatement();
				ResultSet    rs  = st.executeQuery("SELECT * FROM "+daoTable + " FETCH FIRST 1 ROW ONLY");
				ResultSetMetaData md = rs.getMetaData();
				int i   = 0;
				for ( int k=1; k<=md.getColumnCount(); k++) {
					columnName[i] = md.getColumnName(k);
					dataType[i]   = md.getColumnTypeName(k).toUpperCase();
					dataLength[i] = md.getPrecision(k);
					i++;
				}

				if ( i == 0 ) {
					gb.showLogMessage("D"," NO TABLE NAME ERROR : "+gb.getTableOwner()+"."+daoTable+" "+" "+ci); 
				}
	
				columnCnt = i;
				rs.close();
				st.close();
				rs = null;
				st = null;
				
				//down, add to cache
				TableMetaDataVo L_TableMetaDataVo = new TableMetaDataVo();
				L_TableMetaDataVo.setTableName(daoTable);
				L_TableMetaDataVo.setColumnCount(columnCnt);
				System.arraycopy(columnName, 0, L_TableMetaDataVo.columnName, 0, columnName.length);
				System.arraycopy(dataLength, 0, L_TableMetaDataVo.dataLength, 0, dataLength.length);
				System.arraycopy(dataType, 0, L_TableMetaDataVo.dataType, 0, dataType.length);

				gb.tableMetaDataObjects.put(daoTable, L_TableMetaDataVo);
				
				//up, add to cache
				
			}
		}

		catch (Exception ex) {
			gb.setExpMethod("processMySqlColumn");  
			gb.expHandle(ex, true); 
			return false; 
		}

		return true;
	} // End of processMySqlColumn

	private boolean processSelectColumn() {

		try {

			String  selectColumn="";

			if ( indexHint.length() > 0 ) {
				indexHint = " /*+ "+indexHint+" */ "; 
			}

			if ( selectSQL.length() > 0 ) {
				columnString = selectSQL;  
				return true; 
			}

			StringBuffer strbuf = new StringBuffer();
			columnString = "";
			for( int i=0; i < columnCnt; i++ ) {

//				if ( dataType[i].equals("DATE") && gb.dbType.substring(0,3).equals("ORA") ) {
//					strbuf.append("NVL(TO_CHAR("+columnName[i]+",'YYYYMMDDHH24MISS'),' '),"); 
//				}
//				else if ( gb.dbType.equals("INFORMIX") ) {
//
//					// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
//					if ( dataType[i].length() >= 8 && dataType[i].substring(0,8).toUpperCase(Locale.TAIWAN).equals("DATETIME") ) {
//						strbuf.append("TO_CHAR("+columnName[i]+",'%Y-%m-%d %H:%M:%S'),"); 
//					}
//					// fix issue "Portability Flaw: Locale Dependent Comparison" 2020/09/17 Zuwei
//					else if ( dataType[i].length() >= 4 && dataType[i].substring(0,4).toUpperCase(Locale.TAIWAN).equals("DATE") ) {
//						strbuf.append("TO_CHAR("+columnName[i]+",'%Y-%m-%d'),"); 
//					}
//					else { 
//						strbuf.append(columnName[i]+","); 
//					}
//				}
//				else if ( gb.dbType.equals("SQLserver") ) {
//
//					if ( dataType[i].equals("DATE") || dataType[i].equals("DATETIME") ) {
//						strbuf.append("CONVERT(varchar,"+columnName[i]+",120),");  
//					}
//					else {
//						strbuf.append(columnName[i]+","); 
//					}
//				}
//				else {
					strbuf.append(columnName[i]+","); 
//				}
			}

			columnString = strbuf.toString();
			columnString = columnString.substring(0,columnString.length()-1);
		} // End of try

		catch ( Exception ex ) {
			gb.setExpMethod("processSelectColumn");  
			gb.expHandle(ex, true); 
			return false; 
		}

		return true;
	} // End of processSelectColumn

	public String[]  getTableColumn() {
		return  columnName; 	
	}

	public String[]  getColumnDataType() {
		return  dataType;   
	}

	public Integer[] getColumnLength() {
		return  dataLength; 
	}

	public int getTableColumnCnt() {
		return  columnCnt;  
	}

//	public void switchDatabase(Connection con) {
//
//		try {
//
//			PreparedStatement ps  = null;
//			sqlCmd = "use "+gb.dbName;
//			ps = con.prepareStatement(sqlCmd);
//			retCode = ps.executeUpdate();
//			ps.close();
//		}
//
//		catch (Exception ex) {
//			gb.expMethod = "switchDatabase";  
//			gb.expHandle(ex); 
//			return; 
//		}
//
//		return;
//	} // End of switchDatabase

	public Connection getDatabaseConnect() {

		try {

			//setConnectIndex();
			return gate.gDbConn;
			//return gb.conn;
		}

		catch (Exception ex) {
			gb.setExpMethod("getDatabaseConnect");  
			gb.expHandle(ex, true); 
			return null; 
		}

	} // End of getDatabaseConnect


	public void setConnectIndex() throws Exception {

		ci = 0;
//		for ( int i=0; i<dbCount; i++ ) {
//
//			if ( gb.dbNameCom.equals(dataBase) ) {
//				ci = i; 
//			}
//		}

	} // End of setConnectIndex

	public boolean setString(int pnt,String parmString) throws Exception {

		parmData[pnt] = parmString;
		parmType[pnt] = "S";
		parmCount     = pnt;
		return true;
	}

	public boolean setInt(int pnt,int parmInt) throws Exception {

		parmData[pnt] = ""+parmInt;
		parmType[pnt] = "I";
		parmCount     = pnt;
		return true;
	}
	
	public boolean setBigDecimal(int pnt,BigDecimal parmBD) throws Exception {

		parmData[pnt] = ""+parmBD;
		parmType[pnt] = "B";
		parmCount     = pnt;
		return true;
	}

	public boolean setLong(int pnt,long parmLong) throws Exception {

		parmData[pnt] = ""+parmLong;
		parmType[pnt] = "L";
		parmCount     = pnt;
		return true;
	}

	public boolean setDouble(int pnt,double parmDouble) throws Exception {

		parmData[pnt] = ""+parmDouble;
		parmType[pnt] = "D";
		parmCount     = pnt;
		return true;
	}

	public void setTimestamp(String fieldName,String spDateTimeTs) {

		outputHash.put(fieldName,spDateTimeTs);
		//setValue(fieldName,""+setData,0);
		return;
	}

	public void setTimestamp(String fieldName,java.sql.Timestamp pTs) {

		outputHash.put(fieldName,pTs);
		//setValue(fieldName,""+setData,0);
		return;
	}

	public boolean setTimestamp(int pnt, String spDateTimeTs) throws Exception {
		parmData[pnt] = spDateTimeTs;
		parmType[pnt] = "T";
		parmCount     = pnt;
		return true;
	}

	public boolean setTimestamp(int pnt, java.sql.Timestamp pTs) throws Exception {
		parmData[pnt] = pTs;
		parmType[pnt] = "T";
		parmCount     = pnt;
		return true;
	}
	/*
 public void setTimestamp(String fieldName,Timestamp parmTimestamp) {

	 outputHash.put(fieldName,parmTimestamp);
	 //setValue(fieldName,""+setData,0);
	 return;
 }


 public boolean setTimestamp(int pnt,Timestamp parmTimestamp) throws Exception {
	 parmData[pnt] = parmTimestamp;
	 parmType[pnt] = "T";
	 parmCount     = pnt;
	 return true;
 }
	 */
	public boolean setRowId(int pnt,RowId parmRowId) throws Exception {

		parmData[pnt] = parmRowId;
		parmType[pnt] = "R";
		parmCount     = pnt;
		return true;
	}

	public boolean setParmData(PreparedStatement ps) throws Exception {

		for ( int i=1; i <= parmCount; i++ ) {

			////System.out.println(i+" "+parmData[i]);
			if ( parmType[i].equals("S") ) {
				ps.setObject(i,parmData[i]); 
			}
			//{ ps.setString(i,parmData[i]); }
			else if ( parmType[i].equals("I") ) {
				ps.setDouble(i,Integer.parseInt((String)parmData[i])); 
			}
			//{ ps.setDouble(i,Integer.parseInt(parmData[i])); }
			else if ( parmType[i].equals("L") ) {
				ps.setDouble(i,Long.parseLong((String)parmData[i])); 
			}
			//{ ps.setDouble(i,Long.parseLong(parmData[i])); }
			else if ( parmType[i].equals("D") ) {
				ps.setDouble(i,Double.parseDouble((String)parmData[i])); 
			}
			//{ ps.setDouble(i,Double.parseDouble(parmData[i])); }
			else if ( parmType[i].equals("T") ) {
				ps.setTimestamp(i, (Timestamp)parmData[i]); 
			}
			else if ( parmType[i].equals("R") ) {
				ps.setRowId(i, (RowId)parmData[i]); 
			}
			else if ( parmType[i].equals("B") ) {
				//ps.setBigDecimal(i, (BigDecimal)parmData[i]); 
				ps.setBigDecimal(i, BigDecimal.valueOf(Double.parseDouble((String)parmData[i])));
				
			}

			else {
				
				ps.setObject(i,parmData[i]); 
			}
			//{ ps.setString(i,parmData[i]); }
			parmData[i] = "";
			parmType[i] = "";
		}
		parmCount =0;
		return true;
	}

	public void resetParm() {

		parmCount = 0;
		selectSQL = "";
		indexHint = "";
		return;
	}

	public void resetArrayValue(int pnt) {

		li = pnt;
		if ( arrayHash[li] == null ) {
			arrayHash[li] = new HashMap();  
		}
		arrayHash[li].clear();
		return;
	}

	public void resetValue() {

		outputHash.clear();
		return;
	}

	public void resetField(String fieldName) {

		outputHash.remove(fieldName);
		return;
	}

	public void resetTable() {

		try {

			for ( int i=0; i < columnCnt; i++ ) {
				setValue(columnName[i],""); 
			}
		}
		catch ( Exception ex ) {
			gb.setExpMethod("resetTable");  
			gb.expHandle(ex, true); 
			return; 
		}

		return;
	}

	public String getValue(String fieldName) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			retnStr = (String)outputHash.get(fieldName);
			if ( retnStr == null ) {
				return nullString;   
			}
			retnStr =  retnStr.trim();
			if ( covertBig5.equals("Y") ) {
				retnStr = new String(retnStr.getBytes("iso-8859-1"),"big5"); 
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getValue");  
			gb.expHandle(ex, true); 
		}

		return retnStr;
	}

	public java.sql.Timestamp getTimeStamp(String fieldName) {

		java.sql.Timestamp lTimeStamp=null;
		try {

			fieldName = fieldName.toUpperCase();
			lTimeStamp = (java.sql.Timestamp)outputHash.get(fieldName);
			if ( lTimeStamp == null ) {
				return null;   
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getTimeStamp");  
			gb.expHandle(ex, true); 
		}

		return lTimeStamp;
	}

	public boolean getBoolean(String spValue1,String spValue2) {
		if (spValue1==null || spValue2==null)
			return false;
		if (spValue1.equals(spValue2))
			return true;
		return false;
	}

	public int getInteger(String fieldName) {
		String tmpData="";
		int    cvtNumber=0;
		try {

			fieldName = fieldName.toUpperCase();
			tmpData = (String)outputHash.get(fieldName);
			if ( tmpData == null ) {
				return 0;   
			}
			tmpData = tmpData.trim();
			if ( tmpData.length() == 0 ) {
				return 0; 
			}
			cvtNumber = (int)(Double.parseDouble(tmpData));
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getInteger");  
			gb.expHandle(ex, true); 
		}

		return cvtNumber;
	}

	public long getLong(String fieldName) {

		String tmpData="";
		long   cvtNumber=0;
		try {

			fieldName = fieldName.toUpperCase();
			tmpData   = (String)outputHash.get(fieldName);
			if ( tmpData == null ) {
				return 0;   
			}
			tmpData = tmpData.trim();
			if ( tmpData.length() == 0 ) {
				return 0; 
			}
			cvtNumber = (long)(Double.parseDouble(tmpData));
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getLong");  
			gb.expHandle(ex, true); 
		}

		return cvtNumber;
	}

	public double getDouble(String fieldName) {

		String tmpData="";
		double cvtDouble=0;
		long   cvtLong=0;
		try {

			fieldName = fieldName.toUpperCase();

			Object lObject = outputHash.get(fieldName);
			if (null == lObject)
				return 0;
			
			tmpData = lObject.toString().trim();
			//tmpData = outputHash.get(fieldName);   .toString().trim();
			
			
			if (( tmpData == null ) || (tmpData.length()==0)) {
				return 0;   
			}
			
			cvtDouble = Double.parseDouble(tmpData);
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getDouble");  
			gb.expHandle(ex, true); 
		}

		return cvtDouble;
	}

	public BigDecimal getBigDecimal(String fieldName) {

		String tmpData="";
		BigDecimal cvtBigDecimal=new BigDecimal("0");
		try {
			fieldName = fieldName.toUpperCase();

			tmpData = (String)outputHash.get(fieldName);
			
			tmpData = tmpData.trim();
			
			
			if (( tmpData == null ) || (tmpData.length()==0)) {
				return cvtBigDecimal;   
			}
			
			cvtBigDecimal = new BigDecimal(tmpData);
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getBigDecimal");  
			gb.expHandle(ex, true); 
		}

		return cvtBigDecimal;
	}
	public HashMap getBufferHash() {

		return outputHash;
	}

	public void setReportData(HashMap outputHash) {

		this.outputHash = outputHash;
		return;
	}

	public byte[] hexStrToByteArr(String s) {

		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2)
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		return data;
	}

	public boolean setRowId(int pnt,String parmRowId) throws Exception {

		parmData[pnt] = hexStrToByteArr(parmRowId);
		parmCount     = pnt;
		return true;
	}

	public void setValue(String fieldName,String setData) {

		try {

			fieldName = fieldName.toUpperCase();
			if ( setData == null ) {
				setData = "";   
			}

			setData   = setData.trim();
			outputHash.put(fieldName,setData);
		}
		catch ( Exception ex ) {
			gb.setExpMethod("setValue");  
			gb.expHandle(ex, true); 
		}

		return;
	}

	public void setValueInt(String fieldName,int setData) {
		
		try {

			fieldName = fieldName.toUpperCase();


			outputHash.put(fieldName,setData);
		}
		catch ( Exception ex ) {
			gb.setExpMethod("setValue");  
			gb.expHandle(ex, true); 
		}

		
		//setValue(fieldName,""+setData,0);
		return;
	}

	public void setValueDouble(String fieldName,double setData) {

		fieldName = fieldName.toUpperCase();
		outputHash.put(fieldName,setData);
		//setValue(fieldName,""+setData,0);
		return;
	}


	public String getArrayData(String fieldName,int k) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)arrayHash[li].get(fieldName);
			if ( retnStr == null ) {
				return nullString; 
			}

			retnStr = retnStr.trim();
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getArrayData");  
			gb.expHandle(ex, true); 
		}

		return retnStr;
	}

	public double getArrayNumber(String fieldName,int k) {

		String retnStr="";
		double retnNumber=0;
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)arrayHash[li].get(fieldName);
			if ( retnStr == null ) {
				return 0; 
			}
			if ( retnStr.length() == 0 ) {
				return 0; 
			}

			retnNumber = Double.parseDouble(retnStr.trim());
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getArrayNumber");  
			gb.expHandle(ex, true); 
		}

		return retnNumber;
	}

	public String getList(String fieldName,int k) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)outputHash.get(fieldName);
			if ( retnStr == null ) {
				return nullString; 
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getList");  
			gb.expHandle(ex, true); 
		}

		return retnStr.trim();
	}

	public double getListDouble(String fieldName,int k) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)outputHash.get(fieldName);
			if ( retnStr == null ) {
				return 0; 
			}
			if ( retnStr.length() == 0 ) {
				return 0; 
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getListDouble");  
			gb.expHandle(ex, true); 
		}

		return Double.parseDouble(retnStr.trim());
	}

	public long getListLong(String fieldName,int k) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)outputHash.get(fieldName);
			if ( retnStr == null ){ 
				return 0; 
			}
			if ( retnStr.length() == 0 ) {
				return 0; 
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getListLong");  
			gb.expHandle(ex, true); 
		}

		return Long.parseLong(retnStr.trim());
	}

	public long getListInt(String fieldName,int k) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			fieldName = fieldName+"_"+k;
			retnStr   = (String)outputHash.get(fieldName);
			if ( retnStr == null ) {
				return 0; 
			}
			if ( retnStr.length() == 0 ) {
				return 0; 
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getListInt");  
			gb.expHandle(ex, true); 
		}

		return Integer.parseInt(retnStr.trim());
	}

	public int getLoadCnt(int parmCode) throws Exception {

		li = parmCode;
		return loadCnt[li];
	}

	public void setLoadIndex(int parmCode) throws Exception {

		li = parmCode;
		if ( arrayHash[li] == null ) {
			arrayHash[li] = new HashMap();  
		}
	}

	public void setArrayData(String fieldName,String arrayValue,int k) {

		try {

			fieldName = fieldName.toUpperCase();
			if ( arrayHash[li] == null ) {
				arrayHash[li] = new HashMap();  
			}

			if ( arrayValue == null ) {
				arrayValue = "";   
			}

			arrayValue = arrayValue.trim();
			fieldName  = fieldName+"_"+k;
			arrayHash[li].put(fieldName,arrayValue);
		}
		catch ( Exception ex ) {
			gb.setExpMethod("setArrayData");  
			gb.expHandle(ex, true); 
		}

		return;
	}

	public void setList(String fieldName,String arrayValue,int k) {

		try { 


			fieldName = fieldName.toUpperCase();
			if ( arrayHash[li] == null ) {
				arrayHash[li] = new HashMap();  
			}

			if ( arrayValue == null ) {
				arrayValue = "";   
			}
			arrayValue = arrayValue.trim();
			fieldName  = fieldName+"_"+k;
			outputHash.put(fieldName,arrayValue);
		}
		catch ( Exception ex ) {
			gb.setExpMethod("setList");  
			gb.expHandle(ex, true); 
		}

		return;
	}

	public void removeValue(String fieldName) throws Exception {

		fieldName = fieldName.toUpperCase();
		outputHash.remove(fieldName);
		return;
	}

//	public void setBufferData(String parmKeyField) throws Exception {
//
//		gb.authQueue[0].put(parmKeyField+"-L",""+li);
//
//		String    accessKey="";
//		String[]  colkeyField = parmKeyField.split(",");
//
//		for ( int i=0; i<getLoadCnt(li); i++ ) {
//			for( int j=0; j < columnCnt; j++ ) {
//
//				String queueData = getList(columnName[j],i);
//				gb.authQueue[li].put(columnName[j]+"_"+i,queueData);
//			}
//		}
//
//		for ( int i=0; i<getLoadCnt(li); i++ ) {
//
//			accessKey="";
//			for ( int j=0; j<colkeyField.length; j++ ) {
//				accessKey = accessKey +"#"+ getList(colkeyField[j],i);  
//			}
//			gb.authQueue[li].put(accessKey.substring(1),""+i);
//		}
//		return;
//	}

//	public void getBufferData(String parmKeyField,String parmKeyData) throws Exception {
//
//		String nL = (String)gb.authQueue[0].get(parmKeyField+"-L");
//		int    n  = Integer.parseInt(nL);
//
//		String kP = (String)gb.authQueue[n].get(parmKeyData);
//		int    k  = Integer.parseInt(kP);
//
//		setLoadIndex(n);
//		for ( int j=0; j<loadColumnCnt[n]; j++ ) {
//			String queueData = (String)gb.authQueue[n].get(columnName[j]+"_"+k);
//			setValue(loadColumnName[n][j],queueData);
//		}
//
//		return;
//	}

	public String getUpdate(String fieldName) {

		String retnStr="";
		try {

			fieldName = fieldName.toUpperCase();
			retnStr = (String)outputHash.get(fieldName);
			if ( retnStr == null ) {
				return null;    
			}
			retnStr = retnStr.trim();
		}

		catch ( Exception ex ) {
			gb.setExpMethod("getUpdate");  
			gb.expHandle(ex, true); 
		}

		return retnStr;
	}

	public String convertAmount(String amtField,int dec) {

		String cvtAmount  = String.format("%,14."+dec+"f",Float.parseFloat(amtField));
		return cvtAmount;
	}

	public void commitDataBaseOld() {

		try {

			dbCount=1;
			for( int  i=0; i<dbCount; i++) {

				try {
					if ( getDatabaseConnect() != null ) {
						getDatabaseConnect().commit(); 
						gb.showLogMessage("I","COMMIT"); 
					}
				}
				catch ( Exception ex2 ) {
					continue; 
				}
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("commitDataBase");  
			gb.expHandle(ex, true); 
			return; 
		}
	}

	public void closeConnect() {

		try {

			for( int i=0; i<dbCount; i++) {

				if ( null != getDatabaseConnect()) {
					getDatabaseConnect().close(); 
					//gb.conn = null; 
				}
			}
		}

		catch ( Exception ex ) {
			gb.setExpMethod("closeConnect"); 
			return; 
		}

		return;
	}

	public void rollbackDataBaseOld() {

		try {

			gb.setExceptionFlag("Y");
			for( int  i=0; i<dbCount; i++) {

				if ( getDatabaseConnect() != null ) {
					getDatabaseConnect().rollback(); 
				}
			}

			if ( outputHash != null ) {
				outputHash.clear(); 
				outputHash = null; 
			}
		}

		catch ( Exception ex ) {

			gb.setExpMethod("rollbackDataBase");
			gb.showLogMessage("E","ROLLBACK DATABASE EXCEPTION." + ex.getMessage());
			////System.out.println("ROLLBACK DATABASE EXCEPTION "); 
			return; 
		}

		return;
	}
	/**
	 * AuthDAO Exception Check
	 * V1.00.60 SQL EXCEPTION處理
	 * @return void
	 * @throws Exception if any exception occurred
	 */
	public void sqlExceptionChk(SQLException sqlEx) {
		
		if (sqlEx instanceof DB2Diagnosable) {    
			com.ibm.db2.jcc.DB2Diagnosable diagnosable = (com.ibm.db2.jcc.DB2Diagnosable)sqlEx;               
			String slSqlState="";
			int nlExceptionSqlCode=0;
			String sqlErrMc = "";
			String exceptionInfo = sqlEx.toString();
			DB2Sqlca sqlca = diagnosable.getSqlca();
			if (null != sqlca) {
				slSqlState = sqlca.getSqlState();
				nlExceptionSqlCode = sqlca.getSqlCode();
				sqlErrMc = sqlca.getSqlErrmc();
				try {
					gb.showLogMessage("E","sqlca.getMessage="+sqlca.getMessage());
				} catch (SQLException e) {
					gb.showLogMessage("E","sqlca.getMessage exception="+e);
				}
				if ((nlExceptionSqlCode==-4470) && (slSqlState.equals("08003"))) {
					gate.gDbConn=null;
					gate.authRemark = "DB2 disconnect("+sqlErrMc+")，請DBA檢查。";
				}
				else if ((nlExceptionSqlCode==-911) && (slSqlState.equals("40001"))) {
					gate.authRemark = "DB2 DeadLock("+sqlErrMc+")，請DBA檢查。";
				}
				else {
					gate.authRemark = "DB2 Error:SQLCODE="+nlExceptionSqlCode+", SQLSTATE="+slSqlState+", "+
									sqlErrMc.substring(sqlErrMc.indexOf(".")+1);
				}		
			}
			else {
					gate.authRemark = exceptionInfo;
			}
			if (gate.authRemark.length()>60) {
				gate.authRemark = gate.authRemark.substring(0,60);
			}
		}
	}

	/*
	public Connection getDBconnect() {

		return gb.conn;
	}
	*/
}   // End of class AuthDAO
