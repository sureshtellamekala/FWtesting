package net.boigroup.bdd.framework.Rest.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import net.boigroup.bdd.framework.Rest.Constants.HttpStatus;
import net.boigroup.bdd.framework.Rest.HttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultHttpResponseHandler implements ResponseHandler {
    private static final Pattern CHARSET_PATTERN = Pattern.compile(".*charset=([^ ;]+).*");
    private HttpToolImpl tool;
    private boolean ignoreBody;
    private Stopwatch stopwatch;

    public DefaultHttpResponseHandler(HttpToolImpl tool, boolean ignoreBody, Stopwatch stopwatch) {
        this.tool = tool;
        this.ignoreBody = ignoreBody;
        this.stopwatch = stopwatch;
    }

    public void setIgnoreBody(boolean ignoreBody) {
        this.ignoreBody = ignoreBody;
    }

    @VisibleForTesting
    public HttpResponse handle(org.apache.http.HttpResponse response) throws IOException {
        long responseTimeToEntityNanos = this.stopwatch.elapsed(TimeUnit.NANOSECONDS);
        HttpResponseImpl result = new HttpResponseImpl();
        result.setStatusLine(response.getStatusLine().toString());
        result.setResponseCode(HttpStatus.findByCode(response.getStatusLine().getStatusCode()));
        Header[] allHeaders = response.getAllHeaders();
        Header[] arr$ = allHeaders;
        int len$ = allHeaders.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            Header header = arr$[i$];
            result.getHeaders().put(header.getName(), header.getValue());
        }

        Iterator i$ = this.tool.getContext().getCookieStore().getCookies().iterator();

        while(i$.hasNext()) {
            Cookie cookie = (Cookie)i$.next();
            result.getCookies().put(cookie.getName(), cookie.getValue());
        }

        HttpEntity responseEntity = response.getEntity();
        if (!this.ignoreBody && responseEntity != null) {
            byte[] bytes = EntityUtils.toByteArray(responseEntity);
            result.setContent(bytes);
            result.setEncoding(this.resolveEncoding(responseEntity));
            result.setSize(responseEntity.getContentLength());
        }

        long responseTimeNanos = this.stopwatch.elapsed(TimeUnit.NANOSECONDS);
        result.setResponseTimeToEntityNanos(responseTimeToEntityNanos);
        result.setResponseTimeNanos(responseTimeNanos);
        return result;
    }

    @VisibleForTesting
    protected Charset resolveEncoding(HttpEntity responseEntity) {
        Charset defaultCharset = Charset.forName("utf-8");
        Header contentTypeHeader = responseEntity.getContentType();
        if (contentTypeHeader == null) {
            return defaultCharset;
        } else {
            String contentType = contentTypeHeader.getValue();
            String charset = this.extractCharset(contentType);

            try {
                return Charset.forName(charset);
            } catch (IllegalArgumentException var7) {
                return defaultCharset;
            }
        }
    }

    @VisibleForTesting
    protected String extractCharset(String contentType) {
        if (contentType == null) {
            return null;
        } else {
            Matcher matcher = CHARSET_PATTERN.matcher(contentType);
            return matcher.matches() ? matcher.group(1) : null;
        }
    }
}
