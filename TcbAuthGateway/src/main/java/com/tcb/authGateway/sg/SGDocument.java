package com.tcb.authGateway.sg;

import java.util.*;
import com.tcb.authGateway.utils.RCFunc;

//import com.sage.roger.RCFunc;
//import com.sage.expression.*;


/**
 * <p>本Class為新的版本的Document，支援陣列欄位的操作。 (ver 1.1) </p>
 * <p>於1.2版新增在取得欄位時所傳入的欄位名稱中，陣列索引值可以變數取代或一運算式。</p>
 * <p>Title: SAGE Generic Library</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004-2005</p>
 * <p>Company: SAGE Information System Corp.</p>
 * @author Roger Chen
 * @version 1.2
 */
public class SGDocument implements ISGDocument, Cloneable {
    private Map doc;

    private String name;

    public static String DOCUMENT_SEPARATOR = ".";

    public static String ARRAY_BEGIN = "[";

    public static String ARRAY_END = "]";

    private static String CR = System.getProperty("line.separator");

    private static String TAB = "\t";

    private static String EMPTY_DOCUMENT = "<Empty Document>";

    private static String THIS = "this";

    protected SGDocument() {
        doc = new HashMap(50);
    }



    protected SGDocument(Map doc) {
        this.doc = doc;
    }



    protected SGDocument(SGDocument doc) {
        this.doc = doc.doc;
    }



    protected SGDocument(String docName, Map doc) {
        this(doc);
        this.name = docName;
    }



    protected SGDocument(String docName) {
        this();
        this.name = docName;
    }



    protected Object clone() throws CloneNotSupportedException {
        SGDocument newDoc = (SGDocument)super.clone();
        newDoc.doc = new HashMap();
        String[] fieldNames = getFieldNames(null);
        for (int i = 0; i < fieldNames.length; i++) {
            Object obj = doc.get(fieldNames[i]);
            try {
                if (obj instanceof ISGDocument) {
                    newDoc.setFieldValue(fieldNames[i], ( (SGDocument)obj).getDeepCopy());
                }
                else if (obj instanceof ISGDocument[]) {
                    newDoc.setFieldValue(fieldNames[i], getDocumentArrayCopy( (ISGDocument[])obj));
                }
                else
                    newDoc.setFieldValue(fieldNames[i], obj);
            }
            catch (Exception ex) {
            }
        }
        return newDoc;
    }



    private ISGDocument[] getDocumentArrayCopy(ISGDocument[] oriDocs) {
        ISGDocument[] docs = new ISGDocument[oriDocs.length];
        for (int i = 0; i < docs.length; i++) {
            try {
                docs[i] = oriDocs[i].getDeepCopy();
            }
            catch (Exception ex) {
            }
        }
        return docs;
    }



    public String getDocumentName() {
        return name;
    }



    public void setDocumentName(String docName) {
        this.name = docName;
    }



    public int getFieldCount() {
        return doc.size();
    }



    public boolean containsField(String fullFieldName) {
        if (fullFieldName == null) {
            return false;
        }

        if (!isFullFieldName(fullFieldName)) {
            if (isArrayFieldName(fullFieldName)) {
                try {
                    Object value = getArrayField(fullFieldName);
                    return true;
                }
                catch (ArrayIndexOutOfBoundsException ex) {
                    return false;
                }
            }

            return doc.containsKey(fullFieldName);
        }

        try {
            SGDocument subDoc = getSubDocByNS(fullFieldName, false);
            return subDoc.containsField(getLastFieldName(fullFieldName));
        }
        catch (RuntimeException ex) {
            return false;
        }
    }



    public String[] getFieldNames(String fullDocName) throws SGFieldNotFoundException {
        if (fullDocName == null || fullDocName.length() == 0) {
            String[] fieldNames = new String[doc.size()];
            doc.keySet().toArray(fieldNames);
            Arrays.sort(fieldNames);
            return fieldNames;
        }

        ISGDocument subDoc = getSubDocument(fullDocName);
        return subDoc.getFieldNames(null);
    }



    public Object[] getFieldValues(String fullDocName) throws SGFieldNotFoundException {
        String[] fieldNames = getFieldNames(fullDocName);
        Object[] fieldValues = new Object[fieldNames.length];
        for (int i = 0; i < fieldValues.length; i++) {
            fieldValues[i] = getFieldValue(fullDocName + DOCUMENT_SEPARATOR + fieldNames[i]);
        }

        return fieldValues;
    }



    /**
     * 設定欄位值
     * @param fullFieldName 完整的欄位名稱或單一欄位名稱
     * @param fieldValue 欄位值
     */
    public void setFieldValue(String fullFieldName, Object fieldValue) {

        // 若不是完整欄位名稱，則以單一欄位處理
        if (!isFullFieldName(fullFieldName)) {
            setField(fullFieldName, fieldValue);
            return;
        }

        SGDocument subDoc = getSubDocByNS(fullFieldName, true);
        if (subDoc != null) {
            subDoc.setField(getLastFieldName(fullFieldName), fieldValue);
        }

    }



    private void setField(String fieldName, Object fieldValue) {
        // 若為陣列欄位，則設定陣列欄位值
        if (isArrayFieldName(fieldName)) {
            if (fieldValue instanceof ISGDocument) {
                setArrayDocument(fieldName, (SGDocument)fieldValue);
            }
            else {
                setArrayField(fieldName, fieldValue);
            }
        }
        else {
            doc.put(fieldName, fieldValue);
        }
    }



    public String getStringFieldValue(String fullFieldName) throws SGFieldNotFoundException {
        Object value = getFieldValue(fullFieldName);
        if (value instanceof String) {
            return (String)value;
        }
        else {
            throw new SGTypeMismatchException(fullFieldName);
        }
    }



    public int getIntFieldValue(String fullFieldName) throws SGFieldNotFoundException {
        try {
            return Integer.parseInt(getStringFieldValue(fullFieldName));
        }
        catch (NumberFormatException ex) {
            throw new SGTypeMismatchException(fullFieldName);
        }
    }



    /**
     * <p>取得某一欄位值。</p>
     * 傳入的欄位名稱可以是完整的欄位名稱或是單一欄位名稱，如為完整欄位名稱，則循著名稱一層一層往下找到<BR>
     * 最後一個目標欄位，如果單一欄位名稱，則找出這份Document下層的這個欄位。
     * @param fullFieldName 完整的欄位名稱，包含所有上層欄位名稱，如(Person.Name.FirstName)，或單一欄位名稱，如(Name)。
     * @return 欄位值
     * @throws SGFieldNotFoundException 欄位不存在
     */
    public Object getFieldValue(String fullFieldName) throws SGFieldNotFoundException {
        //若傳入的名稱為this，則直接傳回這份Document。
        if (fullFieldName.equals(THIS)) {
            return this;
        }

        // 檢查傳入的欄位名稱是否為完整名稱，用於處理單一欄位
        if (!isFullFieldName(fullFieldName)) {
            return getField(fullFieldName);
        }

        // 傳入名稱為完整名稱，故取出欲處理之欄位所在的Document
        SGDocument subDoc = getSubDocByNS(fullFieldName, false);
        if (subDoc != null) {
            String fieldName = getLastFieldName(fullFieldName);
            return subDoc.getField(fieldName);
        }

        throw new SGFieldNotFoundException(fullFieldName);
    }



    /**
     * 取得這份Document下層某一欄位值
     * @param fieldName 欄位名稱
     * @return 欄位值
     */
    private Object getField(String fieldName) {

        // 檢查欄位名稱中是否包含陣列欄位名稱，若是，則使用取得陣列欄位的方法來取得欄位(getArrayField)
        if (isArrayFieldName(fieldName)) {
            return getArrayField(fieldName);
        }
        else {
            // 若該欄位存在，則回傳欄位值，否則丟出欄位不存在的例外。
            if (doc.containsKey(fieldName)) {
                return doc.get(fieldName);
            }
            else {
                throw new SGFieldNotFoundException(fieldName);
            }
        }

    }



    /**
     * 檢查傳入的欄位名稱是否為完整欄位名稱
     * @param fieldName 欄位名稱
     * @return true 是 : false 否
     */
    private boolean isFullFieldName(String fieldName) {
        return fieldName.indexOf(DOCUMENT_SEPARATOR) > -1;
    }



    /**
     * 檢查傳入的欄位名稱中是否包含陣列名稱
     * @param fieldName 欄位名稱
     * @return true 是，有包含陣列名稱。 false 否，不包含。
     */
    private boolean isArrayFieldName(String fieldName) {
        return fieldName.endsWith(ARRAY_END) & fieldName.lastIndexOf(ARRAY_BEGIN, fieldName.length()) > -1;
    }



    /**
     * 取得陣列欄位中的某一欄位<br>
     * 這個方法只能用在處理本份Doc下層的某一相對欄位，不支援處理完整名稱的絕對欄位名稱。
     * @param fieldName 欄位名稱
     * @return 欄位值
     */
    private Object getArrayField(String fieldName) {
        String realFieldName = getArrayFieldName(fieldName);

        Object obj = doc.get(realFieldName);
        if (obj instanceof Object[]) {
            int arrayIndex = getArrayIndexValue(fieldName);
            if (arrayIndex >= ( (Object[])obj).length) {
                throw new ArrayIndexOutOfBoundsException(arrayIndex);
            }
            return ( (Object[])obj)[arrayIndex];
        }
        else {
            throw new SGDocumentAccessException("Cannot get an array element from a non-Array field.");
        }
    }



    private String getArrayFieldName(String docName) {
        int beginPos = docName.lastIndexOf(ARRAY_BEGIN);
        int endPos = docName.lastIndexOf(ARRAY_END);

        return docName.substring(0, beginPos);
    }



    /**
     * 取得陣列欄位名稱中的索引值
     * @param fieldName 陣列欄位名稱或運算式
     * @return 索引值
     */
    private int getArrayIndexValue(String fieldName) {
        int beginPos = fieldName.lastIndexOf(ARRAY_BEGIN);
        int endPos = fieldName.lastIndexOf(ARRAY_END);

        String index = fieldName.substring(beginPos + 1, endPos);
        int arrayIndex = 0;
        try {
            arrayIndex = Integer.parseInt(index);
        }
        catch (NumberFormatException ex) {
            throw ex;
        }
        return arrayIndex;
    }



    /**
     * 設定陣列欄位中的某一欄位值<BR>
     * 這個方法只能用在處理本份Doc下層的某一相對欄位，不支援處理完整名稱的絕對欄位名稱。
     * @param fieldName 欄位名稱
     * @param value 欄位值
     */
    private void setArrayField(String fieldName, Object value) {
        String realFieldName = getArrayFieldName(fieldName);
        int arrayIndex = getArrayIndexValue(fieldName);

        Object oriValue = null;
        if (doc.containsKey(realFieldName)) {
            oriValue = doc.get(realFieldName);
        }
        else {
            oriValue = new Object[0];
        }

        Object[] objArr = null;
        // 若目標欄位不是陣列欄位或欲處理的索引值大於或等於目前陣列的最大長度，則重新調整陣列大小
        if (! (oriValue instanceof Object[]) || arrayIndex >= ( (Object[])oriValue).length) {
            objArr = adjustArraySize(arrayIndex + 1, oriValue);
        }
        else {
            objArr = (Object[])oriValue;
        }

        objArr[arrayIndex] = value;
        doc.put(realFieldName, objArr);
    }



    /**
     * 根據傳入的新的陣列大小值調整陣列大小，並將已存在的陣列值存入新陣列
     * @param newSize 新的陣列大小
     * @param oriValue 已存在的陣列值
     * @return 新的陣列
     */
    private Object[] adjustArraySize(int newSize, Object oriValue) {
        ArrayList valueList = new ArrayList();
        if (oriValue instanceof Object[]) {
            valueList.addAll(Arrays.asList( (Object[])oriValue));
        }
        else {
            valueList.add(oriValue);
        }

        for (int i = valueList.size(); i < newSize; i++) {
            valueList.add(null);
        }

        return valueList.toArray();
    }



    private String removeArraySuffix(String fieldName) {
        int beginPos = fieldName.lastIndexOf(ARRAY_BEGIN);
        if (beginPos == -1)
            return fieldName;

        int endPos = fieldName.lastIndexOf(ARRAY_END);
        return fieldName.substring(0, beginPos);
    }



    public void removeField(String fullFieldName) throws SGFieldNotFoundException {
        if (!isFullFieldName(fullFieldName)) {
            doc.remove(fullFieldName);
            return;
        }

        SGDocument subDoc = getSubDocByNS(fullFieldName, false);
        if (subDoc != null) {
            subDoc.removeField(getLastFieldName(fullFieldName));
        }

    }



    /**
     * 取得完整欄位名稱中，最末端一個欄位名稱
     * @param fullFieldName 完整欄位名稱
     * @return 最末端一個欄位名稱
     */
    private String getLastFieldName(String fullFieldName) {
        if (isFullFieldName(fullFieldName)) {
            return fullFieldName.substring(fullFieldName.lastIndexOf(DOCUMENT_SEPARATOR) + 1, fullFieldName.length());
        }

        return fullFieldName;
    }



    private SGDocument getSubDocByNS(String fullFieldName, boolean createNewDoc) {
        int index = fullFieldName.lastIndexOf(DOCUMENT_SEPARATOR);
        if (index > -1) {
            String docName = fullFieldName.substring(0, index);
            return getSubDocument(new StringTokenizer(docName, DOCUMENT_SEPARATOR), createNewDoc);
        }
        else
            return this;
    }



    private SGDocument getSubDocument(StringTokenizer fullDocName, boolean createNewDoc) {
        if (!fullDocName.hasMoreTokens()) {
            return this;
        }

        String docName = fullDocName.nextToken();
        Object subDoc = null;
        // 是否為陣列欄位，若是，則取出陣列欄位，取出欄位時若發生例外且無需產生新的Doc時，則丟出例外
        if (isArrayFieldName(docName)) {
            try {
                subDoc = getArrayField(docName);
            }
            catch (RuntimeException ex) {
                if (!createNewDoc)
                    throw ex;
            }
        }
        else {
            subDoc = doc.get(docName);
        }

        // 若順利取得Doc，則繼續往下層取得下層的Doc
        if (subDoc != null && subDoc instanceof ISGDocument) {
            return ( (SGDocument)subDoc).getSubDocument(fullDocName, createNewDoc);
        }

        // 若無法取得Doc且需建立新的Doc，則建立後，繼續往下建立新的Doc，否則回傳Null
        if (createNewDoc) {
            return createNewSubDocument(docName).getSubDocument(fullDocName, createNewDoc);
        }

        return null;
    }



    private SGDocument createNewSubDocument(String docName) {
        if (isArrayFieldName(docName)) {
            SGDocument lastDoc = new SGDocument();
            setArrayDocument(docName, lastDoc);
            return lastDoc;
        }

        SGDocument newDoc = new SGDocument();
        doc.put(docName, newDoc);
        return newDoc;
    }



    /**
     * 將一份Document放置到一個DocumentArray欄位中
     * @param docName 陣列Document的欄位名稱
     * @param subDoc 要存放的Document
     */
    private void setArrayDocument(String docName, SGDocument subDoc) {
        String realDocName = getArrayFieldName(docName);
        int arrayIndex = getArrayIndexValue(docName);

        ISGDocument[] subDocs = containsField(realDocName) ? getSubDocuments(realDocName) : new ISGDocument[0];
        ISGDocument[] newDocs = null;
        if (arrayIndex < subDocs.length) {
            subDocs[arrayIndex] = subDoc;
            newDocs = subDocs;
        }
        else {
            newDocs = new ISGDocument[arrayIndex + 1];
            for (int i = 0; i < newDocs.length; i++) {
                newDocs[i] = new SGDocument();
            }

            System.arraycopy(subDocs, 0, newDocs, 0, subDocs.length);
            newDocs[arrayIndex] = subDoc;
        }

        doc.put(realDocName, newDocs);
    }



    public void setSubDocument(String docName, ISGDocument subDoc) {
        setFieldValue(docName, subDoc);
    }



    public void setSubDocuments(String docName, ISGDocument[] docs) {
        setFieldValue(docName, docs);
    }



    public ISGDocument[] getSubDocuments(String docName) throws SGFieldNotFoundException {
        Object value = getFieldValue(docName);

        if (value instanceof ISGDocument) {
            return new ISGDocument[] { (ISGDocument)value};
        }
        else if (value instanceof ISGDocument[]) {
            return (ISGDocument[])value;
        }

        throw new SGFieldNotFoundException(docName);
    }



    public ISGDocument getSubDocument(String docName) throws SGFieldNotFoundException {
        Object value = getFieldValue(docName);
        if (value instanceof ISGDocument) {
            return (ISGDocument)value;
        }
        else if (value instanceof ISGDocument[] && ( (ISGDocument[])value).length > 0) {
            return ( (ISGDocument[])value)[0];
        }

        throw new SGFieldNotFoundException(docName);
    }



    public ISGDocument getDeepCopy() throws Exception {
        return (ISGDocument)clone();
    }



    public ISGDocument getShallowCopy() throws Exception {
        return new SGDocument(doc);
    }



    public void removeAllFields() {
        doc.clear();
    }



    public String toString() {
        try {
            return toString(0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return doc.toString();

        }
    }



    public String toString(int lvl) throws Exception {
        StringBuffer sb = new StringBuffer();

        if (doc.size() == 0) {
            addIndent(sb, lvl);
            sb.append(EMPTY_DOCUMENT);
            sb.append(CR);
            return sb.toString();
        }

        String[] fieldNames = getFieldNames(null);
        for (int i = 0; i < fieldNames.length; i++) {

            addIndent(sb, lvl);
            sb.append(fieldNames[i]);
            sb.append('=');
            Object value = doc.get(fieldNames[i]);
            if (value instanceof SGDocument) {
                addEndOfLine(sb);
                sb.append( ( (SGDocument)value).toString(lvl + 1));
            }
            else if (value instanceof ISGDocument[]) {
                ISGDocument[] data = (ISGDocument[])value;
                for (int k = 0; k < data.length; k++) {
                    addEndOfLine(sb);
                    sb.append(data[k] != null ? data[k].toString(lvl + 1) : "<Null>\r\n");
                }
            }
            else {
                sb.append(getStringValue(value));
                addEndOfLine(sb);
            }

        }
        return sb.toString();
    }



    private String getStringValue(Object value) {
        String rtnValue = null;

        if (value instanceof byte[]) {
            StringBuffer sb = new StringBuffer();
            sb.append('[');
            sb.append(RCFunc.encodeBased16( (byte[])value));
            sb.append(']');

            rtnValue = sb.toString();
        }
        else if (value instanceof Object[]) {
            StringBuffer sb = new StringBuffer();
            Object[] rtn = (Object[])value;
            for (int i = 0; i < rtn.length; i++) {
                sb.append('[');
                sb.append(rtn[i] != null ? rtn[i].toString() : "");
                sb.append(']');
            }
            rtnValue = sb.toString();
        }
        else {
            rtnValue = value != null ? value.toString() : "";
        }

        if (rtnValue != null && rtnValue.length() > 256)
            rtnValue = rtnValue.substring(0, 256) + "....";

        return rtnValue;
    }



    private void addIndent(StringBuffer out, int level) {
        for (int j = 0; j < level; j++) {
            out.append(TAB);
        }
    }



    private void addEndOfLine(StringBuffer out) {
        out.append(CR);
    }

}
