/**
 * 取得DB連線池參數
 * 
 *
 * @author  Kevin
 * @version 1.0
 * @since   2021/02/08
 * 
 * @throws  Exception if any exception occurred
 * @return  slErrorCode
 ******************************************************************************
 *                                                                            *
 *                              MODIFICATION LOG                              *
 *                                                                            *
 * DATE        Version   AUTHOR      DESCRIPTION                              *
 * ----------  --------  ----------  -----------------------------------------*
 * 2021/02/08  V1.00.00  Kevin       取得DB連線池參數                             *
 * 2021/02/08  V1.00.01  Tanwei      updated for project coding standard      *    
 * 2023/07/03  V1.00.48  Kevin       P3程式碼整理                                *                                                                            * 
 ******************************************************************************
 */

package com.tcb.authProg.util;

import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.mchange.v2.c3p0.ComboPooledDataSource;
//import com.tcb.ap4.tool.Decryptor;

public class DbUtil {

	private static ComboPooledDataSource dataSource=null; 

	public DbUtil() {
		// TODO Auto-generated constructor stub
	}

	
	public static ComboPooledDataSource initDataSource(String spDsConfigFileName, String spDbPInfo) {
		ComboPooledDataSource lDs = null;

		try {
//			System.out.println("bean3="+spDsConfigFileName);
			ApplicationContext context = new FileSystemXmlApplicationContext("file:"+spDsConfigFileName);


			lDs =  (ComboPooledDataSource)context.getBean("dataSource");

			lDs.setPassword(spDbPInfo);

			
			//down, 基本設定					
			lDs.setInitialPoolSize(3);
			lDs.setMinPoolSize(3);
			lDs.setMaxPoolSize(50);
			lDs.setAcquireIncrement(1);
			//up, 基本設定

			//down, reconnect 設定
			//https://www.mchange.com/projects/c3p0/#configuring_recovery
			lDs.setAcquireRetryAttempts(0);// 一直重連
			lDs.setAcquireRetryDelay(2000); //每隔 2 秒 嘗試重連 1 次
			lDs.setBreakAfterAcquireFailure(false);//重連失敗後 data source 還會保持有效


			//up, reconnect 設定

			//down, connection test
			//https://www.mchange.com/projects/c3p0/#configuring_connection_testing
			lDs.setIdleConnectionTestPeriod(60*1); //每 1 分鐘測試一次
			
			//L_Ds.setPreferredTestQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
			lDs.setTestConnectionOnCheckin(false);
			lDs.setTestConnectionOnCheckout(false);
			//kevin:確保idle的連線不會占用缓冲池，並缓冲池自動清除連接--top
			//最大空閒时間設置
			lDs.setMaxIdleTime(180);
			//自動超時回收，根据最大空閒時間設置，超過maxIdleTime则缓冲池自動清除連接 
			lDs.setUnreturnedConnectionTimeout(190);
			//跟踪泄露
			lDs.setDebugUnreturnedConnectionStackTraces(true);
			//kevin:確保idle的連線不會占用缓冲池，並缓冲池自動清除連接-end


			dataSource = lDs;
		} catch (Exception e) {
			// TODO: handle exception
			lDs = null;
			e.printStackTrace(System.out);
		
		}

		return lDs;
	}
	public static void commitConn(Connection conn){
		try {
			if((conn!=null) && (!conn.isClosed())){
				conn.commit();
				closeConn(conn);
//				System.out.println("commit connection...");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void rollbackConn(Connection conn){
		try {
			if((conn!=null) && (!conn.isClosed())){
				conn.rollback();
				closeConn(conn);
//				System.out.println("rollback connection...");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public static void closeConn(Connection conn){
		try {
			if((conn!=null) && (!conn.isClosed())){

				conn.close();
//				System.out.println("close connection...");
			}
			//howard marked: 不可以這樣做 dataSource.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static Connection getConnection(){  
		Connection conn=null;  
		try {  
			conn=dataSource.getConnection();  
		} catch (SQLException e) {
			conn=null;
			e.printStackTrace();  
		}  
		return conn;  
	}  

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}

}
