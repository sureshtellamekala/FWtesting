package net.boigroup.bdd.framework.Rest.impl;


import com.google.common.base.Stopwatch;
import com.google.common.io.Closeables;
import net.boigroup.bdd.framework.Rest.HttpResponse;
import net.boigroup.bdd.framework.Rest.RequestBuilder;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class RequestBuilderImpl implements RequestBuilder {
    private HttpToolImpl tool;
    private List<TempHttpEntity> tempHttpEntities = new ArrayList();
    private HttpEntity requestEntity;
    private FormTempHttpEntity form = new FormTempHttpEntity();
    private boolean ignoreBody = false;
    private List<NameValuePair> params = new ArrayList();
    private UsernamePasswordCredentials credentials;
    private List<Header> headers = new ArrayList();
    private int timeout;
    private boolean explicitlySetMultipart = false;

    RequestBuilderImpl(HttpToolImpl tool) {
        this.tool = tool;
    }

    public RequestBuilderImpl body(String name, String value) {
        this.form.addField(name, value);
        return this;
    }

    public RequestBuilderImpl body(String data) {
        this.tempHttpEntities.add(new StringTempHttpEntity(data));
        return this;
    }

    public RequestBuilderImpl body(InputStream data) {
        this.tempHttpEntities.add(new InputStreamTempHttpEntityImpl(data));
        return this;
    }

    public RequestBuilderImpl body(String name, InputStream data) {
        this.tempHttpEntities.add(new InputStreamTempHttpEntityImpl(name, data));
        return this;
    }

    public RequestBuilderImpl file(File file) {
        this.tempHttpEntities.add(new FileTempHttpEntity(file));
        return this;
    }

    public RequestBuilderImpl file(String name, File file) {
        this.tempHttpEntities.add(new FileTempHttpEntity(name, file));
        return this;
    }

    public RequestBuilderImpl contentType(String contentType) {
        this.header("Content-Type", contentType);
        return this;
    }

    public RequestBuilderImpl ignoreBody(Boolean ignoreBody) {
        this.ignoreBody = ignoreBody;
        return this;
    }

    public RequestBuilderImpl header(String name, String value) {
        if ("Content-Type".equals(name) && "multipart/form-data".equals(value)) {
            this.explicitlySetMultipart = true;
        } else {
            BasicHeader header = new BasicHeader(name, value);
            this.headers.add(header);
        }

        return this;
    }

    public RequestBuilderImpl authenticate(String username, String password) {
        this.credentials = new UsernamePasswordCredentials(username, password);
        return this;
    }

    public RequestBuilderImpl queryParam(String name, String... values) {
        String[] arr$ = values;
        int len$ = values.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            String value = arr$[i$];
            this.params.add(new BasicNameValuePair(name, value));
        }

        return this;
    }

    public RequestBuilderImpl timeout(int sec) {
        this.timeout = sec;
        return this;
    }

    public HttpResponse get(String uri) {
        HttpGet getMethod = new HttpGet(this.buildUri(uri));
        return this.executeMethod(getMethod);
    }

    public HttpResponse post(String uri) {
        HttpPost post = new HttpPost(this.buildUri(uri));
        post.setEntity(this.buildRequestEntity());
        return this.executeMethod(post);
    }

    public HttpResponse delete(String uri) {
        HttpDelete delete = new HttpDelete(this.buildUri(uri));
        return this.executeMethod(delete);
    }

    public HttpResponse put(String uri) {
        HttpPut put = new HttpPut(this.buildUri(uri));
        put.setEntity(this.buildRequestEntity());
        return this.executeMethod(put);
    }

    public HttpResponse head(String uri) {
        HttpHead head = new HttpHead(this.buildUri(uri));
        return this.executeMethod(head);
    }

    @Override
    public List<Header> getHeaders() {
        return this.headers;
    }

    @Override
    public List<String> getBody() {
        List<String> body= new ArrayList<String>();
        if(!this.form.isEmpty()) {
            try {
                String temp =EntityUtils.toString(form.toHttpEntity(), "UTF-8");
                for(String t:temp.split("&")) {
                    body.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!this.tempHttpEntities.isEmpty()) {
            for (TempHttpEntity temp:
                 tempHttpEntities) {
                try {
                    body.add(EntityUtils.toString(temp.toHttpEntity(), "UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return body;
    }

    protected URI buildUri(String uri) {
        try {
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }

            URIBuilder uriBuilder = new URIBuilder(this.tool.getBaseUri() + uri);
            if (this.params.size() > 0) {
                uriBuilder.addParameters(this.params);
            }

            return uriBuilder.build();
        } catch (URISyntaxException var3) {
            throw new IllegalArgumentException("Invalid url");
        }
    }

    protected HttpEntity buildRequestEntity() {
        if (!this.form.isEmpty()) {
            this.tempHttpEntities.add(this.form);
        }

        if (this.tempHttpEntities.isEmpty()) {
            this.requestEntity = null;
            return this.requestEntity;
        } else if (!this.isMultiPartBody()) {
            this.requestEntity = ((TempHttpEntity)this.tempHttpEntities.get(0)).toHttpEntity();
            return this.requestEntity;
        } else {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            Iterator i$ = this.tempHttpEntities.iterator();

            while(i$.hasNext()) {
                TempHttpEntity entity = (TempHttpEntity)i$.next();
                entity.addToMultipartEntity(entityBuilder);
            }

            this.requestEntity = entityBuilder.build();
            return this.requestEntity;
        }
    }

    private boolean isMultiPartBody() {
        return this.explicitlySetMultipart || this.tempHttpEntities.size() > 1 || ((TempHttpEntity)this.tempHttpEntities.get(0)).hasName();
    }

    protected HttpResponse executeMethod(HttpRequestBase request) {
        CloseableHttpResponse response = null;
        HttpResponse result = null;

        try {
            if (this.credentials != null) {
                this.headers.add((new BasicScheme()).authenticate(this.credentials, request, this.tool.getContext()));
            }

            Iterator i$ = this.headers.iterator();

            while(i$.hasNext()) {
                Header header = (Header)i$.next();
                request.addHeader(header);
            }

            this.setRequestTimeout(request, this.timeout);
            Stopwatch stopwatch = Stopwatch.createStarted();
            response = this.tool.getClient().execute(request, this.tool.getContext());
            ResponseHandler responseHandler = (ResponseHandler)this.tool.getResponseHandler().or(this.defaultResponseHandler(stopwatch));
            result = responseHandler.handle(response);
            HttpResponse var6 = result;
            return var6;
        } catch (SocketTimeoutException var17) {
            throw new IllegalStateException("Connection timeout", var17);
        } catch (IOException var18) {
            throw new IllegalStateException("Connection was aborted", var18);
        } catch (AuthenticationException var19) {
            throw new IllegalStateException("Unable to authenticate", var19);
        } finally {
            this.publishRequestEvent(request, result);
            request.releaseConnection();

            try {
                Closeables.close(response, true);
            } catch (IOException var16) {
                ;
            }

        }
    }

    private ResponseHandler defaultResponseHandler(Stopwatch stopwatch) {
        return new DefaultHttpResponseHandler(this.tool, this.ignoreBody, stopwatch);
    }

    private void setRequestTimeout(HttpRequestBase request, int timeout) {
        RequestConfig oldConfig = request.getConfig() == null ? RequestConfig.DEFAULT : request.getConfig();
        RequestConfig newConfig = RequestConfig.copy(oldConfig).setConnectTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        request.setConfig(newConfig);
    }

    private void publishRequestEvent(HttpRequestBase request, HttpResponse response) {
        long requestSize = this.requestEntity == null ? 0L : this.requestEntity.getContentLength();
        RequestEventImpl requestEvent = new RequestEventImpl(request, requestSize, response);
        Iterator i$ = HttpToolListeners.getListeners().iterator();

        while(i$.hasNext()) {
            HttpToolListener httpToolListener = (HttpToolListener)i$.next();
            httpToolListener.onRequest(requestEvent);
        }

    }

    protected HttpEntity getRequestEntity() {
        return this.requestEntity;
    }
}
