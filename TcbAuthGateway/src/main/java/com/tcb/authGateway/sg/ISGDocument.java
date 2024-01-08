package com.tcb.authGateway.sg;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ISGDocument {
    String SEPARATOR = ".";

    Object getFieldValue(String fullFieldName) throws SGFieldNotFoundException;



    String getStringFieldValue(String fullFieldName) throws SGFieldNotFoundException;



    int getIntFieldValue(String fullFieldName) throws SGFieldNotFoundException;



    int getFieldCount();



    String getDocumentName();



    void setDocumentName(String docName);



    boolean containsField(String fullFieldName);



    String[] getFieldNames(String docName) throws SGFieldNotFoundException;



    Object[] getFieldValues(String docName) throws SGFieldNotFoundException;



    ISGDocument getSubDocument(String docName) throws SGFieldNotFoundException;



    ISGDocument[] getSubDocuments(String docName) throws SGFieldNotFoundException;



    ISGDocument getDeepCopy() throws Exception;



    ISGDocument getShallowCopy() throws Exception;



    void setFieldValue(String fullFieldName, Object fieldValue);



    //void addFieldValue(String fullFieldName, Object fieldValue);

    void setSubDocument(String docName, ISGDocument doc);



    //void addSubDocument(String docName, ISGDocument doc);

    void setSubDocuments(String docName, ISGDocument[] doc);



    //void addSubDocuments(String docName, ISGDocument[] doc);

    void removeField(String fullFieldName) throws SGFieldNotFoundException;



    void removeAllFields();



    String toString(int lvl) throws Exception;

}