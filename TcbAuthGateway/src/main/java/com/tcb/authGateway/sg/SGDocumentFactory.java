package com.tcb.authGateway.sg;

/**
 * <p>Title: SAGE Generic Library</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: SAGE Information System Corp.</p>
 * @author Roger Chen
 * @version 1.0
 */

public class SGDocumentFactory {
  private SGDocumentFactory() {
  }



  public static ISGDocument createDocument() {
    return createDocument("unnamed");
  }



  public static ISGDocument createDocument(boolean allowDuplicatedKey) {
    return createDocument("unnamed", allowDuplicatedKey);
  }



  public static ISGDocument createDocument(String docName) {
    return new SGDocument(docName);
  }



  public static ISGDocument createDocument(String docName, boolean allowDuplicatedKey) {
    if (allowDuplicatedKey)
      return new SGDocument(docName, new SGArrayListMap());
    else
      return createDocument(docName);
  }

}