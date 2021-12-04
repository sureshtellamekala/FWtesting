package net.boigroup.bdd.framework.Rest.impl;

public interface RequestEvent {
    String getRequestTarget();

    String getRequestType();

    String getRequestData();

    long getRequestSize();

    int getResponseCode();

    long getResponseTimeToEntityNanos();

    long getResponseTimeNanos();

    long getResponseTimeToEntityMillis();

    long getResponseTimeMillis();

    String getResponseData();

    long getResponseSize();
}
