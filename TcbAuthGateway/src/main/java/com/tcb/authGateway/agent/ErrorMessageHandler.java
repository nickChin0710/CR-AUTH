package com.tcb.authGateway.agent;

import java.util.Properties;



public class ErrorMessageHandler {

  private static boolean hasInitiated = false;
  private static Properties errTable = new Properties();

  private ErrorMessageHandler() {

  }

  public final static String emOpenFailed = "1001";
  public final static String emCloseFailed = "1002";
  public final static String emAddNewConnectionFailed = "1003";
  public final static String emGetIDFailed = "1004";
  public final static String emUndefinedConnection = "1005";
  public final static String emRemoveConnectionFailed = "1006";
  public final static String emReceiveDataFromUndefinedConnection = "1007";
  public final static String emSentDataToUndefinedConnecton = "1008";
  public final static String emSentDataFailed = "1009";
  public final static String emReceiveDataFailed = "1010";
  public final static String emPutMessageIntoQueueFailed = "1011";
  public final static String emForwardMessageFailed = "1012";
  public final static String emMessageHandlerError = "1013";
  public final static String emConnectionNotOpenYet = "1014";
  public final static String emDuplicatedOpenOperation = "1015";
  public final static String emDuplicatedCloseOperation = "1016";
  public final static String emAddMessagetoResendManagerFailed = "1017";
  public final static String emSendingMessageNotPermitted = "1018";

  private static void initTable() {
    errTable.setProperty(emOpenFailed, "Open connection failed. ");
    errTable.setProperty(emCloseFailed, "Close connection failed. ");
    errTable.setProperty(emAddNewConnectionFailed, "Add new connection failed. ");
    errTable.setProperty(emGetIDFailed, "Get Client ID failed. ");
    errTable.setProperty(emUndefinedConnection, "Connection is not allowed. ");
    errTable.setProperty(emRemoveConnectionFailed, "Remove connection failed. ");
    errTable.setProperty(emReceiveDataFromUndefinedConnection, "Receive data from a undefined connection. ");
    errTable.setProperty(emSentDataToUndefinedConnecton, "Sent data to a undefined connection. ");
    errTable.setProperty(emSentDataFailed, "Sent data failed. ");
    errTable.setProperty(emReceiveDataFailed, "Receive data failed. ");
    errTable.setProperty(emForwardMessageFailed, "Forward message failed. ");
    errTable.setProperty(emPutMessageIntoQueueFailed, "Put message into MessageQueue failed. ");
    errTable.setProperty(emMessageHandlerError, "Error occurred while MessageHandler is parsing message. ");
    errTable.setProperty(emConnectionNotOpenYet, "Connection is not opened for client to be able to connect. ");
    errTable.setProperty(emDuplicatedOpenOperation, "Unnecessary duplicated open operation. ");
    errTable.setProperty(emDuplicatedCloseOperation, "Unnecessary duplicated close operation. ");
    errTable.setProperty(emAddMessagetoResendManagerFailed, "Add message to MessageResendManager failed. ");
    errTable.setProperty(emSendingMessageNotPermitted, "Client is not permitted to send message. ");
    hasInitiated = true;
  }

  public static String getErrorMessage(String errCode) {
    if (!hasInitiated)
      initTable();
    String msg = errTable.getProperty(errCode);
    return msg != null ? msg : "Unexcepted error. ";
  }

}