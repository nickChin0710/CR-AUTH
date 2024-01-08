package com.tcb.authGateway.utils;

import java.io.*;
import java.util.*;


public class SystemUtil {
  private SystemUtil() {
  }



  public static Properties loadPropertiesFile(String filename) throws IOException {
    File f = getFileFromSystemLibraryPath(filename);
    if (f == null) {
      throw new FileNotFoundException(filename);
    }
    	 try( FileInputStream fis = new FileInputStream(f);){
    		 Properties prop = new Properties();
    		    prop.load(fis);
    		 fis.close();
        	 return prop;  
    	 }  
  }



  private static File getFileFromSystemLibraryPath(String filename) {
    String libPath = System.getProperty("java.library.path");
    StringTokenizer st = new StringTokenizer(libPath, ";");
    while (st.hasMoreTokens()) {
      File file = new File(st.nextToken().trim() + File.separator + filename);
      //System.out.println("searching " + file.getAbsolutePath());
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }



  public static Properties loadPropertiesFileFromClassPath(String filename) throws IOException {
	  try(InputStream is = ClassLoader.getSystemResourceAsStream(filename);){
		    if (is == null) {
		        throw new FileNotFoundException(filename);
		      }
		    Properties prop = new Properties();
		    prop.load(is);
		    return prop;
	  }
  }

}