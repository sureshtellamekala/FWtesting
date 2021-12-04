package net.boigroup.bdd.framework.Rest.impl;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import net.boigroup.bdd.framework.ConfigLoader;
import net.boigroup.bdd.framework.Rest.HttpResponse;
import net.boigroup.bdd.framework.Rest.RequestBuilder;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpToolImpl implements HttpTool {
    private final String KeyStorePath;
    private final String KeyStorePassword;
    private final SSLSocketFactory sSLSocketFactory;
    private final Scheme sch;
    private URI restHost;
    private DefaultHttpClient client;
    private HttpClientContext context;
    private int defaultTimeout;
    static final String CONTENT_TYPE = "Content-Type";
    private ResponseHandler responseHandler;

    final static Logger LOG = Logger.getLogger(HttpToolImpl.class);

    public HttpToolImpl(URI restHost,String KeyStorePath,String KeyStorePassword, int defaultTimeout) {
        this.restHost = restHost;
        this.KeyStorePath=KeyStorePath;
        this.KeyStorePassword=KeyStorePassword;
        this.defaultTimeout = defaultTimeout;
        this.context = HttpClientContext.create();
        this.context.setCookieStore(new BasicCookieStore());
        this.sSLSocketFactory = getSSLSocketFactory();
        this.sch = new Scheme("https", 443, sSLSocketFactory);
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setRedirectStrategy(DefaultRedirectStrategy.INSTANCE);
        this.client = new DefaultHttpClient();
        this.client.getConnectionManager().getSchemeRegistry().register(sch);
        // add for proxy if proxy enabled
        if("Yes".equalsIgnoreCase(ConfigLoader.config().getString("Rest.ProxyEnabled"))) {
            HttpHost proxy = new HttpHost(ConfigLoader.config().getString("Rest.Proxy.Host"), ConfigLoader.config().getInt("Rest.Proxy.Port"), "http");
            this.client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
    }

    public File getStoreFile(String path){
        String file = new File("src/main/resources/InputOpenBankingCertificates/").getAbsoluteFile().toString();
        File storeFile = new File(file+"/"+path);
        if (!storeFile.exists() || !storeFile.isFile()) {
            LOG.info("Not found or not a file: "
                    + storeFile.getPath());
            return null;
        }
        return storeFile;
    }

    public KeyStore getStore(File store,String storePass) {
        KeyStore store1 = null;
        FileInputStream stream = null;
        try {
            store1 = KeyStore.getInstance(KeyStore
                    .getDefaultType());

            stream = new FileInputStream(store);

            LOG.info("Loading server store from file "
                    + store.getPath());
            store1.load(stream, storePass.toCharArray());
            LOG.info("store certificate count: "
                    + store1.size());
        } catch (Exception ex) {
            System.err.println("Failed to load store: "
                    + ex.toString());
        } finally {
            try {
                stream.close();
            } catch (Exception ignore) {
            }
        }
        return store1;
    }

    public SSLSocketFactory getSSLSocketFactory(){
        SSLSocketFactory socketFactory = null;
        try {
            socketFactory = new SSLSocketFactory(getStore(getStoreFile(this.KeyStorePath),this.KeyStorePassword),
                    this.KeyStorePassword, getStore(getStoreFile(this.KeyStorePath),this.KeyStorePassword));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        return socketFactory;
    }

    DefaultHttpClient getClient() {
        return this.client;
    }

    URI getHost() {
        return this.restHost;
    }

    public HttpClientContext getContext() {
        return this.context;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public Optional<ResponseHandler> getResponseHandler() {
        return this.responseHandler != null ? Optional.of(this.responseHandler) : Optional.absent();
    }

    public String getBaseUri() {
        return String.format("%s://%s:%s", this.restHost.getScheme(), this.restHost.getHost(), this.restHost.getPort());
    }

    public RequestBuilder request() {
        return (new RequestBuilderImpl(this)).timeout(this.defaultTimeout);
    }

    public HttpResponse get(String uri) {
        return this.request().get(uri);
    }

    public HttpResponse post(String uri) {
        return this.request().post(uri);
    }

    public HttpResponse delete(String uri) {
        return this.request().delete(uri);
    }

    public HttpResponse put(String uri) {
        return this.request().put(uri);
    }

    public HttpResponse head(String uri) {
        return this.request().head(uri);
    }

    public void addCookie(String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(this.restHost.getHost());
        cookie.setPath("/");
        this.context.getCookieStore().addCookie(cookie);
    }

    public void addCookies(Map<String, String> cookies) {
        Iterator i$ = cookies.entrySet().iterator();

        while(i$.hasNext()) {
            Entry<String, String> cookie = (Entry)i$.next();
            this.addCookie((String)cookie.getKey(), (String)cookie.getValue());
        }

    }

    public void clearCookies() {
        this.context.getCookieStore().clear();
    }

    public void close() {
        this.client.close();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            HttpToolImpl httpTool = (HttpToolImpl)o;
            return Objects.equal(this.defaultTimeout, httpTool.defaultTimeout) && Objects.equal(this.restHost, httpTool.restHost) && Objects.equal(this.client, httpTool.client) && this.cookiesAreEqual(httpTool);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.restHost, this.client, this.defaultTimeout});
    }

    private boolean cookiesAreEqual(HttpToolImpl httpTool) {
        List<Cookie> cookies = this.context.getCookieStore().getCookies();
        List<Cookie> otherCookies = httpTool.getContext().getCookieStore().getCookies();
        if (cookies.size() != otherCookies.size()) {
            return false;
        } else {
            for(int i = 0; i < cookies.size(); ++i) {
                Cookie cookieA = (Cookie)cookies.get(i);
                Cookie cookieB = (Cookie)otherCookies.get(i);
                boolean cookiesEqual = Objects.equal(cookieA.getComment(), cookieB.getComment()) && Objects.equal(cookieA.getCommentURL(), cookieB.getCommentURL()) && Objects.equal(cookieA.getDomain(), cookieB.getDomain()) && Objects.equal(cookieA.getExpiryDate(), cookieB.getExpiryDate()) && Objects.equal(cookieA.getName(), cookieB.getName()) && Objects.equal(cookieA.getPath(), cookieB.getPath()) && Objects.equal(cookieA.getPorts(), cookieB.getPorts()) && Objects.equal(cookieA.getValue(), cookieB.getValue()) && Objects.equal(cookieA.getVersion(), cookieB.getVersion());
                if (!cookiesEqual) {
                    return false;
                }
            }

            return true;
        }
    }
}
