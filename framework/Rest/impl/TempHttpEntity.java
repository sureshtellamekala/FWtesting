package net.boigroup.bdd.framework.Rest.impl;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

interface TempHttpEntity {
    HttpEntity toHttpEntity();

    void addToMultipartEntity(MultipartEntityBuilder var1);

    boolean hasName();
}
