package net.boigroup.bdd.framework.Rest.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.InputStream;

class InputStreamTempHttpEntityImpl implements TempHttpEntity {
    private String name = "STREAM_CONTENT";
    private InputStream data;

    InputStreamTempHttpEntityImpl(InputStream data) {
        this.data = data;
    }

    InputStreamTempHttpEntityImpl(String name, InputStream data) {
        this.data = data;
        this.name = name;
    }

    public HttpEntity toHttpEntity() {
        return new InputStreamEntity(this.data);
    }

    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody(this.name, this.data);
    }

    public boolean hasName() {
        return !"".equals(this.name);
    }
}

