package com.tcb.authGateway.sg;

//import com.sage.roger.*;
import java.util.*;
import com.tcb.authGateway.utils.RCFunc;


public class SGSocketPackage {

  private byte Header[] = new byte[90];

  private int DataLength = 0;

  private int MessageLength = 0;

  private int RemoteID = 0;

  private int Command = 0;

  private byte[] Message;

  private int Position_DestinationID = 20;

  private int Position_PackageType = 62;

  // Package Type
  public static final int ptData = 0x01;

  public static final int ptResponse = 0x02;

  public static final int ptReport = 0x03;

  public static final int ptFailure = 0x04;

  // Command
  public static final int cmdDataFromGateway = 0x01;

  public static final int cmdOpenConnection = 0x11;

  public static final int cmdCloseConnection = 0x12;

  public static final int cmdResetConnection = 0x13;

  public static final int cmdQueryStatus = 0x14;

  public static final int cmdDataFromNonGateway = 0x21;

  public static final int cmdSetTimer = 0x51;

  public static final int cmdCancelTimer = 0x52;

  public static final int cmdQueryTimer = 0x53;

  public static final int cmdReportTimeout = 0x54;

  public SGSocketPackage() {
    setPackageType(this.ptData);
  }



  public void setHeader(byte header[]) {
    Header = header;
  }



  public void setRemoteID(int id) {
    RemoteID = id;
  }



  public int getRemoteID() {
    return RemoteID;
  }



  public void setDestinationID(int id) {
    Header[Position_DestinationID] = (byte)id;
  }



  public int getDestinationID() {
    return (char)Header[Position_DestinationID];
  }



  public void setPackageType(int type) {
    Header[Position_PackageType] = (byte)type;
  }



  public int getPackageType() {
    return Header[Position_PackageType];
  }



  public void setCommand(int cmd) {
    Command = cmd;
  }



  public int getCommand() {
    return Command;
  }



  public byte[] getHeader() {
    return Header;
  }



  public void setMessage(byte msg[]) {
    Message = msg;
  }



  public byte[] getMessage() {
    return Message;
  }



  public int getDataLength() {
    return DataLength;
  }



  public void loadPackage(byte Indata[]) throws Exception {
    byte buf[] = new byte[2];
    System.arraycopy(Indata, 0, Header, 0, Header.length);
    System.arraycopy(Indata, Header.length, buf, 0, 2);
    DataLength = RCFunc.bytetoIntLoByteFirst(buf);
    System.arraycopy(Indata, Header.length + 3, buf, 0, 2);
    MessageLength = RCFunc.bytetoIntLoByteFirst(buf);
    RemoteID = (char)Indata[Header.length + 5];
    Command = (char)Indata[Header.length + 2];

    byte msgdata[] = new byte[MessageLength - 1];
    System.arraycopy(Indata, Header.length + 6, msgdata, 0, msgdata.length);
    Message = msgdata;
  }



  public byte[] generatePackage() {
    byte outdata[] = new byte[Header.length + 6 + Message.length];
    byte buf[] = new byte[2];

    Arrays.fill(outdata, (byte)0);
    MessageLength = Message.length + 1;
    DataLength = Message.length + 4;
    System.arraycopy(Header, 0, outdata, 0, Header.length);
    buf = RCFunc.InttoByte_LoByteFirst(DataLength, 2);
    System.arraycopy(buf, 0, outdata, Header.length, 2);
    buf = RCFunc.InttoByte_LoByteFirst(MessageLength, 2);
    System.arraycopy(buf, 0, outdata, Header.length + 3, 2);
    outdata[Header.length + 2] = (byte)Command;
    outdata[Header.length + 5] = (byte)RemoteID;
    System.arraycopy(Message, 0, outdata, Header.length + 6, Message.length);

    return outdata;
  }

}
