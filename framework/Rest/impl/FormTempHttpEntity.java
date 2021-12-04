package net.boigroup.bdd.framework.Rest.impl;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.*;

class FormTempHttpEntity implements TempHttpEntity {
    private Map<String, String> form = new HashMap();

    FormTempHttpEntity() {
    }

    public void addField(String name, String value) {
        this.form.put(name, value);
    }

    public boolean isEmpty() {
        return this.form.isEmpty();
    }

    public HttpEntity toHttpEntity() {
        try {
            return new UrlEncodedFormEntity(this.toNameValueList(this.form));
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalStateException("Unsupported Encoding");
        }
    }

    public void addToMultipartEntity(MultipartEntityBuilder builder) {
        Iterator i$ = this.form.entrySet().iterator();

        while(i$.hasNext()) {
            Map.Entry<String, String> field = (Map.Entry)i$.next();
            builder.addTextBody((String)field.getKey(), (String)field.getValue());
        }

    }

    public boolean hasName() {
        return false;
    }

    private List<NameValuePair> toNameValueList(Map<String, String> data) {
        List<NameValuePair> result = new ArrayList();
        Iterator i$ = data.entrySet().iterator();

        while(i$.hasNext()) {
            Map.Entry<String, String> stringStringEntry = (Map.Entry)i$.next();
            result.add(new BasicNameValuePair((String)stringStringEntry.getKey(), (String)stringStringEntry.getValue()));
        }

        return result;
    }
}

