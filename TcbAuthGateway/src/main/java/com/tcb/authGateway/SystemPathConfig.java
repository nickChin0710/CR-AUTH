package com.tcb.authGateway;

import java.io.IOException;
import java.util.Properties;
import com.tcb.authGateway.utils.SystemUtil;

//import com.sage.util.SystemUtil;


public class SystemPathConfig {

  private static SystemPathConfig config = new SystemPathConfig();

  private Properties prop;

  public SystemPathConfig() {
    this("SAGE_PATH.ini");
  }



  public SystemPathConfig(String fileName) {
    try {
      prop = SystemUtil.loadPropertiesFile(fileName);
    }
    catch (IOException ex) {
    }
  }



  public static SystemPathConfig getInstance() {
    return config;
  }



  public String getSystemLogPath() {
    return prop.getProperty("SYS_LOGS");
  }



  public String getTraxLogPath() {
    return prop.getProperty("TRX_LOGS");
  }



  public String getCommunicationLogPath() {
    return prop.getProperty("COMM_LOGS");
  }



  public String getSystemConfigPath() {
    return prop.getProperty("SYS_CONF");
  }



  public String getCommunicationConfigPath() {
    return prop.getProperty("COMM_CONF");
  }



  public String getTraxConfigPath() {
    return prop.getProperty("TRX_CONF");
  }



  public String getProperty(String name) {
    return prop.containsKey(name) ? prop.getProperty(name) : "";
  }
}
