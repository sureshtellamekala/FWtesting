package net.boigroup.bdd.framework.Rest;

import org.apache.http.Header;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface RequestBuilder {
    RequestBuilder body(String var1, String var2);

    RequestBuilder body(String var1);

    RequestBuilder body(InputStream var1);

    RequestBuilder body(String var1, InputStream var2);

    RequestBuilder file(File var1);

    RequestBuilder file(String var1, File var2);

    RequestBuilder contentType(String var1);

    RequestBuilder ignoreBody(Boolean var1);

    RequestBuilder header(String var1, String var2);

    RequestBuilder authenticate(String var1, String var2);

    RequestBuilder queryParam(String var1, String... var2);

    RequestBuilder timeout(int var1);

    HttpResponse get(String var1);

    HttpResponse post(String var1);

    HttpResponse delete(String var1);

    HttpResponse put(String var1);

    HttpResponse head(String var1);

    List<Header> getHeaders();

    List<String> getBody();
}

