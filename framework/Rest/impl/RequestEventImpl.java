package net.boigroup.bdd.framework.Rest.impl;

import net.boigroup.bdd.framework.Rest.HttpResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.concurrent.TimeUnit;

class RequestEventImpl implements RequestEvent {
    private final String requestTarget;
    private final String requestType;
    private final String requestData;
    private final long requestSize;
    private int responseCode;
    private long responseTimeToEntityMillis;
    private long responseTimeMillis;
    private String responseData;
    private long responseSize;

    public RequestEventImpl(HttpRequestBase request, long requestSize, HttpResponse response) {
        this.requestTarget = request.getURI().toASCIIString();
        this.requestType = request.getMethod();
        this.requestData = this.getRequestMetadata(request);
        this.requestSize = requestSize;
        if (response != null) {
            this.responseCode = response.getResponseCode().getCode();
            this.responseTimeToEntityMillis = response.getResponseTimeToEntityMillis();
            this.responseTimeMillis = response.getResponseTimeMillis();
            this.responseData = response.getBody();
            this.responseSize = response.getSize();
        }

    }

    private String getRequestMetadata(HttpRequestBase request) {
        StringBuilder sb = new StringBuilder("Headers: ");
        Header[] arr$ = request.getAllHeaders();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Header header = arr$[i$];
            sb.append(header.getName()).append(":").append(header.getValue()).append(",");
        }

        return sb.toString();
    }

    public long getRequestSize() {
        return this.requestSize;
    }

    public String getRequestTarget() {
        return this.requestTarget;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public String getRequestData() {
        return this.requestData;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public long getResponseTimeToEntityNanos() {
        return this.toNanos(this.responseTimeToEntityMillis);
    }

    public long getResponseTimeNanos() {
        return this.toNanos(this.responseTimeMillis);
    }

    public long getResponseTimeToEntityMillis() {
        return this.responseTimeToEntityMillis;
    }

    public long getResponseTimeMillis() {
        return this.responseTimeMillis;
    }

    public String getResponseData() {
        return this.responseData;
    }

    public long getResponseSize() {
        return this.responseSize;
    }

    private long toNanos(long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }
}

