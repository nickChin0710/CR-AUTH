package com.tcb.authGateway.sg;

import java.io.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ISGXMLCoder {
  void encode(ISGDocument doc, OutputStream os, String encoding) throws SGXMLCoderException;



  void encode(ISGDocument doc, OutputStream os, String rootTag, String encoding) throws SGXMLCoderException;



  void encode(ISGDocument doc, String filename, String rootTag, String encoding) throws SGXMLCoderException;



  ISGDocument decode(InputStream is) throws SGXMLCoderException;



  ISGDocument decode(InputStream is, String rootTag) throws SGXMLCoderException;



  ISGDocument decode(String filename) throws SGXMLCoderException;
}