package com.tcb.authGateway.sg;

import org.w3c.dom.*;


/**
 * <p>Title: SAGE Generic Library</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SAGE Information System Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class SGXMLDocumentUtil {
  private SGXMLDocumentUtil() {
  }



  public static String displayDocumentNode(Node node) {
    StringBuffer sb = new StringBuffer();
    toString(node, 0, sb);
    return sb.toString();
  }



  public static String displayDocument(Document doc) {
    StringBuffer sb = new StringBuffer();
    toString(doc, 0, sb);
    return sb.toString();
  }



  private static void toString(Node node, int level, StringBuffer buf) {
    NodeList nl = node.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node subNode = nl.item(i);
      if (subNode.getNodeType() != Node.ELEMENT_NODE)
        continue;

      for (int j = 0; j < level; j++)
        buf.append("   ");

      buf.append(subNode.getNodeName());
      buf.append("=");
      buf.append(getNodeValue(subNode));
      buf.append("\r\n");

      if (subNode.hasChildNodes())
        toString(subNode, level + 1, buf);
    }
  }



  public static String getNodeAttribute(Node node, String name) throws DOMException {
    NamedNodeMap attr = node.getAttributes();
    Node rtn = attr.getNamedItem(name);
    return rtn != null ? rtn.getNodeValue() : null;
  }



  public static String convertURLString(String value) {
    // convert "&"
    value = value.replaceAll("\\x26", "&amp;");

    // convert "<"
    value = value.replaceAll("\\x3C", "&lt;");

    // convert ">"
    value = value.replaceAll("\\x3E", "&gt;");

    // convert "'"
    value = value.replaceAll("\\x27", "&apos;");

    // convert """
    value = value.replaceAll("\\x22", "&quot;");

    value = value.replaceAll("\\x00", ".");

    return value;
  }



  public static String getNodeValue(Node node) {
    if (node == null)
      return null;

    NodeList list = node.getChildNodes();
    String value = "";
    for (int i = 0; i < list.getLength(); i++) {
      Node child = list.item(i);
      if (child.getNodeType() == Node.TEXT_NODE)
        value += child.getNodeValue();
    }
    return value.trim();
  }



  public static Node getSingleNode(Document doc, String tagName) {
    NodeList nl = doc.getElementsByTagName(tagName);
    if (nl == null || nl.getLength() == 0)
      return null;

    return nl.item(0);
  }



  public static String getSingleNodeValue(Document doc, String nodeName) {
    NodeList nl = doc.getElementsByTagName(nodeName);
    if (nl == null || nl.getLength() == 0)
      return null;

    return getNodeValue(nl.item(0));
  }



  public static String[] getMultipleNodeValue(Document doc, String nodeName) {
    NodeList nl = doc.getElementsByTagName(nodeName);
    if (nl == null || nl.getLength() == 0)
      return null;

    String[] values = new String[nl.getLength()];
    for (int i = 0; i < nl.getLength(); i++) {
      values[i] = getNodeValue(nl.item(i));
    }

    return values;
  }



  public static Node getSingleNode(Element parent, String tagName) {
    NodeList nl = parent.getElementsByTagName(tagName);
    if (nl == null || nl.getLength() == 0)
      return null;

    return nl.item(0);
  }



  public static String getSingleNodeValue(Element parent, String nodeName) {
    NodeList nl = parent.getElementsByTagName(nodeName);
    if (nl == null || nl.getLength() == 0)
      return null;

    return getNodeValue(nl.item(0));
  }



  public static String[] getMultipleNodeValue(Element parent, String nodeName) {
    NodeList nl = parent.getElementsByTagName(nodeName);
    if (nl == null || nl.getLength() == 0)
      return null;

    String[] values = new String[nl.getLength()];
    for (int i = 0; i < nl.getLength(); i++) {
      values[i] = getNodeValue(nl.item(i));
    }

    return values;
  }



  public static boolean isCompletedXML(String xml) {
    xml = xml.trim();
    if (!xml.startsWith("<?") || getXMLHeader(xml) == null) {
      return false;
    }
    String rootTag = getRootTag(xml);
    if (rootTag == null || !xml.endsWith("</" + rootTag + ">")) {
      return false;
    }

    return true;
  }



  private static String getXMLHeader(String xml) {
    int end = xml.indexOf("?>");
    if (end == -1) {
      return null;
    }

    String header = xml.substring(0, end + 2);
    //System.out.println("Header tag=" + header);
    return header;
  }



  private static String getRootTag(String xml) {
    int endOfHeader = xml.indexOf("?>");
    int beginOfRootTag = xml.indexOf("<", endOfHeader + 2);
    int endOfRootTag = xml.indexOf(">", endOfHeader + 2);
    if (endOfRootTag == -1) {
      return null;
    }

    String rootTag = xml.substring(beginOfRootTag + 1, endOfRootTag);
    if (rootTag.indexOf(" ") > -1) {
      rootTag = rootTag.substring(0, rootTag.indexOf(" "));
    }
    //System.out.println("Root Tag=" + rootTag);
    return rootTag;
  }

}