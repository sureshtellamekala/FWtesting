package net.boigroup.bdd.framework.Rest.impl;

import com.google.common.annotations.VisibleForTesting;
import net.boigroup.bdd.framework.Rest.Constants.HttpStatus;
import net.boigroup.bdd.framework.Rest.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class HttpResponseImpl implements HttpResponse {
    Map<String, String> headers = new HashMap();
    HttpStatus responseCode;
    Map<String, String> cookies = new HashMap();
    String statusLine;
    long responseTimeToEntityNanos;
    long responseTimeNanos;
    long size;
    private byte[] content;
    private String body;
    private Charset encoding;

    HttpResponseImpl() {
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = Collections.unmodifiableMap(headers);
    }

    public HttpStatus getResponseCode() {
        return this.responseCode;
    }

    public String getContentType() {
        return (String)this.headers.get("Content-Type");
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
    }

    public String getBody() {
        if (this.body == null && this.content != null) {
            this.body = new String(this.content, this.encoding);
        }

        return this.body;
    }

    public InputStream getContent() {
        return this.content == null ? null : new ByteArrayInputStream(this.content);
    }

    public Map<String, String> getCookies() {
        return this.cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = Collections.unmodifiableMap(cookies);
    }

    public String getStatusLine() {
        return this.statusLine;
    }

    void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public long getResponseTimeToEntityMillis() {
        return this.toMillis(this.responseTimeToEntityNanos);
    }

    public long getResponseTimeMillis() {
        return this.toMillis(this.responseTimeNanos);
    }

    @VisibleForTesting
    protected long toMillis(long nanos) {
        return nanos / 1000000L;
    }

    public long getResponseTimeNanos() {
        return this.responseTimeNanos;
    }

    public void setResponseTimeNanos(long responseTimeNanos) {
        this.responseTimeNanos = responseTimeNanos;
    }

    public long getResponseTimeToEntityNanos() {
        return this.responseTimeToEntityNanos;
    }

    public void setResponseTimeToEntityNanos(long responseTimeToEntityNanos) {
        this.responseTimeToEntityNanos = responseTimeToEntityNanos;
    }

    public long getSize() {
        return this.size;
    }

    protected void setSize(long size) {
        this.size = size;
    }

    protected void setContent(byte[] content) {
        this.content = content;
    }

    protected void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }
}
