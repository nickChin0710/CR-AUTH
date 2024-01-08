/**
 * 授權使用格式轉換INTERFACE物件
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
 * 2021/02/08  V1.00.00  Kevin       授權使用格式轉換INTERFACE物件                 *
 * 2021/02/08  V1.00.01  Zuwei       updated for project coding standard      * 
 ******************************************************************************
 */

package com.tcb.authProg.iso8583;

public interface FormatInterChange {
	public boolean host2Iso();

	public boolean iso2Host();

}