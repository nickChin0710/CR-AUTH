package com.tcb.authGateway.sg;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.*;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SGDocumentUtil {
    private SGDocumentUtil() {
    }



    public static void setDocumentFieldValue(ISGDocument doc, String[] docNames, Object value) {
        String fieldName = getFullDocumentName(docNames);
        doc.setFieldValue(fieldName, value);
    }



    public static Object getDocumentFieldValue(ISGDocument doc, String[] docNames) throws Exception {
        String fieldName = getFullDocumentName(docNames);
        return doc.getFieldValue(fieldName);
    }



    public static boolean containsDocumentField(ISGDocument doc, String[] docNames) {
        String fieldName = getFullDocumentName(docNames);
        return doc.containsField(fieldName);
    }



    public static String getFullDocumentName(String[] docNames) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < docNames.length; i++) {
            sb.append(docNames[i]);
            if (i < docNames.length - 1)
                sb.append(ISGDocument.SEPARATOR);
        }
        return sb.toString();
    }



    public static void addValueIntoArrayField(ISGDocument doc, String arfieldName, Object value) {
        if (!doc.containsField(arfieldName)) {
            doc.setFieldValue(arfieldName, (value instanceof Object[] ? value : new Object[] {value}));
            return;
        }

        Object oriValue = doc.getFieldValue(arfieldName);
        ArrayList newList = new ArrayList();
        if (oriValue instanceof Object[])
            newList.addAll(Arrays.asList( (Object[])oriValue));
        else
            newList.add(oriValue);

        if (value instanceof Object[])
            newList.addAll(Arrays.asList( (Object[])value));
        else
            newList.add(value);

        doc.setFieldValue(arfieldName, newList.toArray());
    }



    public static void addValueIntoArrayField(ISGDocument doc, String arfieldName, Object[] value) {
        addValueIntoArrayField(doc, arfieldName, value);
    }



    public static void addDocumentIntoDocumentArray(ISGDocument doc, String arDocName, ISGDocument addDoc) {
        if (!doc.containsField(arDocName)) {
            doc.setSubDocuments(arDocName, new ISGDocument[] {addDoc});
            return;
        }

        ISGDocument[] oriDocs = doc.getSubDocuments(arDocName);
        ArrayList newList = new ArrayList(Arrays.asList(oriDocs));
        newList.add(addDoc);
        oriDocs = new ISGDocument[newList.size()];
        newList.toArray(oriDocs);

        doc.setSubDocuments(arDocName, oriDocs);
    }



    public static void addDocumentsIntoDocumentArray(ISGDocument doc, String arDocName, ISGDocument[] addDocs) {
        if (!doc.containsField(arDocName)) {
            doc.setSubDocuments(arDocName, addDocs);
            return;
        }

        ISGDocument[] oriDocs = doc.getSubDocuments(arDocName);
        ArrayList newList = new ArrayList(Arrays.asList(oriDocs));
        newList.addAll(Arrays.asList(addDocs));
        oriDocs = new ISGDocument[newList.size()];
        newList.toArray(oriDocs);

        doc.setSubDocuments(arDocName, oriDocs);
    }



    public static ISGDocument[] getDocumentArrayCopy(ISGDocument[] oriDocs) throws Exception {
        ISGDocument[] copies = new ISGDocument[oriDocs.length];
        for (int i = 0; i < oriDocs.length; i++) {
            copies[i] = oriDocs[i].getDeepCopy();
        }

        return copies;
    }



    public static void updateDocument(ISGDocument srcDoc, ISGDocument tarDoc) {
        String[] fieldNames = srcDoc.getFieldNames(null);
        for (int i = 0; i < fieldNames.length; i++) {
            Object tarValue = tarDoc.containsField(fieldNames[i]) ? tarDoc.getFieldValue(fieldNames[i]) : null;
            Object srcValue = srcDoc.getFieldValue(fieldNames[i]);

            if (tarValue instanceof ISGDocument && srcValue instanceof ISGDocument)
                updateDocument(srcDoc.getSubDocument(fieldNames[i]), (ISGDocument)tarValue);
            else
                tarDoc.setFieldValue(fieldNames[i], srcDoc.getFieldValue(fieldNames[i]));
        }
    }



    public static boolean isIdenticalDocument(ISGDocument srcDoc, ISGDocument tarDoc, String[] keyFlds) {
        if (keyFlds == null || keyFlds.length == 0)
            return false;

        for (int i = 0; i < keyFlds.length; i++) {
            if (!srcDoc.containsField(keyFlds[i]) || !tarDoc.containsField(keyFlds[i]))
                return false;

            Object srcValue = srcDoc.getFieldValue(keyFlds[i]);
            Object tarValue = tarDoc.getFieldValue(keyFlds[i]);

            if (! (srcValue != null && tarValue != null && srcValue.equals(tarValue)))
                return false;
        }

        return true;
    }



    public static ISGDocument locateDocument(ISGDocument srcDoc, ISGDocument[] tarDocs, String[] keyFlds) {
        for (int i = 0; i < tarDocs.length; i++) {
            if (isIdenticalDocument(srcDoc, tarDocs[i], keyFlds))
                return tarDocs[i];
        }
        return null;
    }



    public static ISGDocument locateDocument(ISGDocument[] docs, String searchFieldName, Object qualifiedFieldValue) {
        for (int i = 0; i < docs.length; i++) {
            if (!docs[i].containsField(searchFieldName))
                continue;

            Object value = docs[i].getFieldValue(searchFieldName);
            if ( (value == null && qualifiedFieldValue == null) || (value != null && value.equals(qualifiedFieldValue)))
                return docs[i];
        }
        return null;
    }



    public static ISGDocument[] removeDocument(ISGDocument[] docs, String searchFieldName, Object qualifiedFieldValue) {
        ArrayList al = new ArrayList(Arrays.asList(docs));
        int i = 0;
        while (i < al.size()) {
            ISGDocument doc = (ISGDocument)al.get(i);
            if (!doc.containsField(searchFieldName)) {
                i++;
                continue;
            }

            Object value = doc.getFieldValue(searchFieldName);
            if ( (value == null && qualifiedFieldValue == null) || (value != null && value.equals(qualifiedFieldValue)))
                al.remove(i);
            else
                i++;
        }

        if (al.size() == docs.length)
            return docs;

        ISGDocument[] newDocs = new ISGDocument[al.size()];
        al.toArray(newDocs);
        return newDocs;
    }



    public static ISGDocument[] sortDocuments(ISGDocument[] docs, String[] keyFields) {
        Arrays.sort(docs, new DocumentComparator(keyFields));
        return docs;
    }



    private static class DocumentComparator implements Comparator {
        private String[] keyFields;

        DocumentComparator(String[] keyFields) {
            this.keyFields = keyFields;
        }



        public int compare(Object obj1, Object obj2) {
            ISGDocument doc1 = (ISGDocument)obj1;
            ISGDocument doc2 = (ISGDocument)obj2;

            int value = 0;
            int index = 0;
            while (value == 0 && index < keyFields.length) {
                String fldName = keyFields[index++];
                String v1 = doc1.containsField(fldName) && doc1.getFieldValue(fldName) != null ? doc1.getFieldValue(fldName).toString() : "";
                String v2 = doc2.containsField(fldName) && doc2.getFieldValue(fldName) != null ? doc2.getFieldValue(fldName).toString() : "";
                value = v1.compareTo(v2);
            }

            return value;
        }
    }



    public static String toXml(ISGDocument doc) {
        return toXml(doc, "Big5");
    }



    public static String toXml(ISGDocument doc, String encoding) {
        if (doc == null) {
            return "";
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new SGXMLCoder().encode(doc, out, encoding);
        return out.toString();
    }



    public static byte[] toXmlBytes(ISGDocument doc) {
        return toXmlBytes(doc, "Big5");
    }



    public static byte[] toXmlBytes(ISGDocument doc, String encoding) {
        if (doc == null) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new SGXMLCoder().encode(doc, out, encoding);
        return out.toByteArray();
    }



    public static ISGDocument toDocument(String xml) {
        return toDocument(xml, "Big5");
    }



    public static ISGDocument toDocument(String xml, String encoding) {
        if (xml == null) {
            return null;
        }

        ByteArrayInputStream input = null;
        try {
            input = new ByteArrayInputStream(xml.getBytes(encoding));
        }
        catch (UnsupportedEncodingException ex) {
            input = new ByteArrayInputStream(xml.getBytes());
        }
        return new SGXMLCoder().decode(input);
    }



    public static ISGDocument toDocument(byte[] xmlBytes) {
        if (xmlBytes == null) {
            return null;
        }

        ByteArrayInputStream input = new ByteArrayInputStream(xmlBytes);
        return new SGXMLCoder().decode(input);
    }

}
