package net.boigroup.bdd.framework.Rest.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.UnsupportedEncodingException;

class StringTempHttpEntity implements TempHttpEntity {
    private String body;
    public static final String STRING_NAME = "STRING_CONTENT";

    public StringTempHttpEntity(String body) {
        this.body = body;
    }

    public HttpEntity toHttpEntity() {
        try {
            return new StringEntity(this.body);
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalStateException("Unsupported Encoding");
        }
    }

    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody("STRING_CONTENT", this.body.getBytes());
    }

    public boolean hasName() {
        return false;
    }
}
