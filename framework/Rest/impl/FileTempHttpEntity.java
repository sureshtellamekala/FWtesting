package net.boigroup.bdd.framework.Rest.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;

class FileTempHttpEntity implements TempHttpEntity {
    private String name = "FILE_CONTENT";
    private File data;

    FileTempHttpEntity(File data) {
        this.data = data;
    }

    FileTempHttpEntity(String name, File data) {
        this.data = data;
        this.name = name;
    }

    public HttpEntity toHttpEntity() {
        return new FileEntity(this.data);
    }

    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        builder.addBinaryBody(this.name, this.data, ContentType.DEFAULT_BINARY, this.data.getName());
    }

    public boolean hasName() {
        return !"".equals(this.name);
    }
}

