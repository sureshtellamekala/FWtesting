package net.boigroup.bdd.framework.Rest.impl;

import net.boigroup.bdd.framework.Rest.HttpResponse;
import net.boigroup.bdd.framework.Rest.RequestBuilder;

import java.util.Map;

public interface HttpTool {
    String getBaseUri();

    RequestBuilder request();

    HttpResponse get(String var1);

    HttpResponse post(String var1);

    HttpResponse delete(String var1);

    HttpResponse put(String var1);

    HttpResponse head(String var1);

    void addCookie(String var1, String var2);

    void addCookies(Map<String, String> var1);

    void clearCookies();

    void close();
}

