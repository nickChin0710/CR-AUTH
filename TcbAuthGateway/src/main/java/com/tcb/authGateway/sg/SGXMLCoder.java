package com.tcb.authGateway.sg;

import java.io.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
//import com.sage.util.*;
//import com.sage.roger.*;
import org.xml.sax.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SGXMLCoder implements ISGXMLCoder {
  public static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"ENCODING\"?>";

  private static String CR = System.getProperty("line.separator");

  private static String TAB = "\t";

  private boolean isFormatted = true;

  public SGXMLCoder() {
  }



  public void encode(ISGDocument doc, String filename, String rootTag, String encoding) throws SGXMLCoderException {
    try {
      backupFile(filename);
      try( FileOutputStream fos = new FileOutputStream(filename);){
    	     encode(doc, fos, rootTag, encoding);
    	      fos.close();
      }
    }
    catch (IOException ex) {
      throw new SGXMLCoderException("Encoing Document to XML failed.", ex);
    }
  }



  private void backupFile(String fileName) {
    File f = new File(fileName);
    if (!f.exists())
      return;

    try {
    	try(FileInputStream fis = new FileInputStream(f);
    			FileOutputStream fos = new FileOutputStream(fileName + ".bak");){
    	    byte[] data = new byte[fis.available()];
    	      while(fis.available()>0)
    	    	  fis.read(data);
    	      fis.close();   	      
    	      fos.write(data);
    	      fos.close();
    	}
    }
    catch (Exception ex) {
    }
  }



  public ISGDocument decode(String filename) throws SGXMLCoderException {
    try(FileInputStream fis = new FileInputStream(filename);) {    
      ISGDocument doc = decode(fis);
      fis.close();
      return doc;
    }
    catch (IOException ex) {
      throw new SGXMLCoderException("Decoding XML file to Document failed.", ex);
    }
  }



  public void encode(ISGDocument doc, OutputStream os, String encoding) throws SGXMLCoderException {
    encode(doc, os, "xml", encoding);
  }



  public ISGDocument decode(InputStream is) throws SGXMLCoderException {
    return decode(is, null);
  }



  public void encode(ISGDocument doc, OutputStream os, String rootTag, String encoding) throws SGXMLCoderException {
    String header = XML_HEADER.replaceAll("ENCODING", encoding);

    try {
      os.write(header.getBytes());
      OutputStreamWriter out = new OutputStreamWriter(os, encoding);
      addEndOfLine(out);
      generateNode(rootTag, doc, out, 0);
      out.flush();
    }
    catch (IOException ex) {
      throw new SGXMLCoderException("Encoding Document to XML failed.", ex);
    }

  }



  private void generateNode(String nodeName, ISGDocument doc, OutputStreamWriter out, int level) throws IOException {
    addIndent(out, level);
    addBeginTag(out, nodeName);

    addEndOfLine(out);
    String[] fieldNames = (String[])doc.getFieldNames(null);
    for (int i = 0; i < fieldNames.length; i++) {
      Object value = doc.getFieldValue(fieldNames[i]);
      if (value instanceof ISGDocument) {
        generateNode(fieldNames[i], (ISGDocument)value, out, level + 1);
      }
      else if (value instanceof ISGDocument[]) {
        generateNodeArray(fieldNames[i], (ISGDocument[])value, out, level);
      }
      else {
        if (value instanceof Object[]) {
          generateLeafArray(fieldNames[i], (Object[])value, out, level + 1);
        }
        else {
          generateLeaf(fieldNames[i], value, out, level + 1);
        }
      }
    }

    addIndent(out, level);
    addEndTag(out, nodeName);
    addEndOfLine(out);
  }



  private void generateNodeArray(String nodeName, ISGDocument[] docs, OutputStreamWriter out,
      int level) throws IOException {
    for (int i = 0; i < docs.length; i++) {
      generateNode(nodeName, docs[i], out, level + 1);
    }
  }



  private void generateLeaf(String nodeName, Object value, OutputStreamWriter out, int level) throws IOException {
    if (value == null)
      return;

    addIndent(out, level);
    addBeginTag(out, nodeName);

    out.write(SGXMLDocumentUtil.convertURLString(value.toString()));

    addEndTag(out, nodeName);
    out.write(CR);
  }



  private void generateLeafArray(String nodeName, Object[] values, OutputStreamWriter out,
      int level) throws IOException {
    for (int i = 0; i < values.length; i++) {
      if (values[i] == null) {
        continue;
      }

      addIndent(out, level);
      addBeginTag(out, nodeName);

      out.write(SGXMLDocumentUtil.convertURLString(values[i].toString()));

      addEndTag(out, nodeName);
      out.write(CR);
    }
  }



  public ISGDocument decode(InputStream is, String rootTag) throws SGXMLCoderException {
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      // fix issue "XML External Entity Injection" 2021/01/13 Zuwei
      docFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      docFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      docFactory.setXIncludeAware(false);
      docFactory.setExpandEntityReferences(false);
      DocumentBuilder db = docFactory.newDocumentBuilder();
      Document doc = db.parse(is);
      return decode(doc, rootTag);
    }
    catch (IOException ex) {
      throw new SGXMLCoderException("Decoding XML or Json file to Document failed.", ex);
    }
    catch (SAXException ex) {
      throw new SGXMLCoderException("Decoding XML or Json file to Document failed.", ex);
    }
    catch (FactoryConfigurationError ex) {
      throw new SGXMLCoderException("Decoding XML or Json file to Document failed.", ex);
    }
    catch (ParserConfigurationException ex) {
      throw new SGXMLCoderException("Decoding XML or Json file to Document failed.", ex);
    }
  }



  public ISGDocument decode(Document doc, String rootTag) throws SGXMLCoderException {
	  
	  
	  
    Node root = doc.getDocumentElement();
    if (rootTag != null && !root.getNodeName().equals(rootTag)) {
      throw new SGXMLCoderException("Wrong definition file. (" + root.getNodeName() + ")");
    }

    ISGDocument outDoc = SGDocumentFactory.createDocument(root.getNodeName());
    generateISDocument(root, outDoc);

    return outDoc;
  }



  private void generateISDocument(Node parent, ISGDocument parentDoc) {
    NodeList nl = parent.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node node = nl.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }

      if (!isLeafNode(node)) {
        generateISDocument(node, generateSubDocument(node, parentDoc));
      }
      else {
        setDocumentField(node, parentDoc);
      }
    }
  }



  private boolean isLeafNode(Node node) {
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
        return false;
      }
    }

    String id = SGXMLDocumentUtil.getNodeAttribute(node, "id");
    if (node.getAttributes().getLength() > (id != null ? 1 : 0)) {
      return false;
    }

    return true;
  }



  private ISGDocument generateSubDocument(Node node, ISGDocument parentDoc) {
    String docName = getFieldName(node);
    ISGDocument doc = SGDocumentFactory.createDocument(docName);
    generateAttributeFields(node, doc);

    if (parentDoc.containsField(docName)) {
      Object value = parentDoc.getFieldValue(docName);
      if (value instanceof ISGDocument || value instanceof ISGDocument[]) {
        SGDocumentUtil.addDocumentIntoDocumentArray(parentDoc, docName, doc);
      }
      else {
        parentDoc.setSubDocument(docName, doc);
      }
    }
    else {
      parentDoc.setSubDocument(docName, doc);
    }

    return doc;
  }



  private void generateAttributeFields(Node node, ISGDocument doc) {
    for (int i = 0; i < node.getAttributes().getLength(); i++) {
      String nodeName = node.getAttributes().item(i).getNodeName();
      if (!nodeName.equals("id")) {
        doc.setFieldValue(nodeName, node.getAttributes().item(i).getNodeValue());
      }
    }

    String value = SGXMLDocumentUtil.getNodeValue(node);
    if (value != null && value.trim().length() > 0)
      doc.setFieldValue("value", value.trim());
  }



  private void setDocumentField(Node node, ISGDocument parentDoc) {
    String value = SGXMLDocumentUtil.getNodeValue(node);
    String fieldName = getFieldName(node);
    if (parentDoc.containsField(fieldName)) {
      Object field = parentDoc.getFieldValue(fieldName);
      if (! ( (field instanceof ISGDocument) || (field instanceof ISGDocument[]))) {
        SGDocumentUtil.addValueIntoArrayField(parentDoc, fieldName, value);
      }
    }
    else {
      parentDoc.setFieldValue(fieldName, value);
    }
  }



  private String getFieldName(Node node) {

    String nodeName = SGXMLDocumentUtil.getNodeAttribute(node, "id");
    //if (nodeName == null)
    //  nodeName = SGXMLDocumentUtil.getNodeAttribute(node, "name");
    if (nodeName == null) {
      nodeName = node.getNodeName();
      nodeName = nodeName.replaceAll("\\x2E", "&dot"); //JDK 1.4+
      //nodeName = RCFunc.replaceString(nodeName, ".", "&dot"); //Before JDK 1.3
    }

    return nodeName;
  }



  private void addIndent(OutputStreamWriter out, int level) throws IOException {
    if (!isFormatted) {
      return;
    }

    for (int j = 0; j < level; j++) {
      out.write(TAB);
    }
  }



  private void addEndOfLine(OutputStreamWriter out) throws IOException {
    if (!isFormatted) {
      return;
    }

    out.write(CR);
  }



  private void addBeginTag(OutputStreamWriter out, String tagName) throws IOException {
    out.write("<");
    tagName = tagName.replaceAll("&dot", ".");
    out.write(tagName);
    out.write(">");
  }



  private void addEndTag(OutputStreamWriter out, String tagName) throws IOException {
    if (tagName.indexOf(" ") > -1) {
      tagName = tagName.substring(0, tagName.indexOf(" "));
    }

    out.write("</");
    tagName = tagName.replaceAll("&dot", ".");
    out.write(tagName);
    out.write(">");
  }

}
