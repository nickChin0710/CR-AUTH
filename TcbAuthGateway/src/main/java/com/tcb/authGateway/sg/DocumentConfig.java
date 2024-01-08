package com.tcb.authGateway.sg;

import java.io.ByteArrayOutputStream;


public class DocumentConfig {
    private ISGDocument configDoc;

    public DocumentConfig(ISGDocument configDoc) {
        this.configDoc = configDoc;
    }



    public DocumentConfig(DocumentConfig config) {
        this.configDoc = config.configDoc;
    }



    public boolean isTrue(String fieldName) {
        if (!configDoc.containsField(fieldName)) {
            return false;
        }
        return new Boolean(configDoc.getStringFieldValue(fieldName)).booleanValue();
    }



    public String getField(String fieldName) {
        return configDoc.getStringFieldValue(fieldName);
    }



    public int getIntField(String fieldName) {
        try {
            return Integer.parseInt(configDoc.getStringFieldValue(fieldName));
        }
        catch (Exception ex) {
            return 0;
        }
    }



    public boolean containsField(String fieldName) {
        return configDoc.containsField(fieldName);
    }



    public DocumentConfig getDocumentConfig(String fieldName) {
        if (!configDoc.containsField(fieldName)) {
            return null;
        }
        return new DocumentConfig(configDoc.getSubDocument(fieldName));
    }



    public ISGDocument getSubDocument(String fieldName) {
        return configDoc.getSubDocument(fieldName);
    }



    public ISGDocument getDocument() {
        return configDoc;
    }



    public DocumentConfig[] getDocumentConfigs(String fieldName) {
        if (!configDoc.containsField(fieldName)) {
            return null;
        }
        ISGDocument[] docs = configDoc.getSubDocuments(fieldName);
        DocumentConfig[] configs = new DocumentConfig[docs.length];

        for (int i = 0; i < configs.length; i++) {
            configs[i] = new DocumentConfig(docs[i]);
        }
        return configs;
    }



    public String toXml() {
        return SGDocumentUtil.toXml(configDoc, "Big5");
    }



    public byte[] toXmlBytes() {
        return SGDocumentUtil.toXmlBytes(configDoc, "Big5");
    }

}
