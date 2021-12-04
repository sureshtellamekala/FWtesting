package net.boigroup.bdd.framework.Rest.impl;

import net.boigroup.bdd.framework.Rest.HttpResponse;

import java.io.IOException;

public interface ResponseHandler {
    HttpResponse handle(org.apache.http.HttpResponse var1) throws IOException;
}
