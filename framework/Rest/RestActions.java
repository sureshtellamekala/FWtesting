package net.boigroup.bdd.framework.Rest;

import org.apache.log4j.Logger;

import java.net.URI;

public class RestActions {
    public static final String BASE_URI_PARAM = "rest.base.uri";

    final static Logger LOG = Logger.getLogger(RestActions.class);

    public RestActions() {
    }

    public static HttpToolBuilder newBuilder(URI baseHost,String KeystorePath,String KeyStorePass) {
        return HttpToolBuilder.newBuilder(baseHost,KeystorePath,KeyStorePass);
    }

    public static RequestBuilder onUri(String uri,String KeystorePath,String KeyStorePass) {
        return newBuilder(URI.create(uri),KeystorePath,KeyStorePass).build().request();
    }
}

