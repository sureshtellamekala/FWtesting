package net.boigroup.bdd.framework.Rest;

import net.boigroup.bdd.framework.LogUtil;
import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import static net.boigroup.bdd.framework.Asserts.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class RestService {

    public RequestBuilder onuri(String uri, String keyStore, String password) {
        return RestActions.onUri(uri, keyStore, password);
    }

    public String getJsonValue(String data, String key) {
        String[] req = key.split("/");
        JSONObject childObj = new JSONObject(data);
        for (int i = 0; i < req.length; i++) {
            data = childObj.getString(req[i]);
            if (i == req.length - 1) {
                break;
            }
            childObj = new JSONObject(data);
        }
        return data;
    }

    public void logRequestDetails(RequestBuilder request) {
        LogUtil.nestedLogStart("Headers of Request");
        List<Header> reqheader = request.getHeaders();
        for (Header head : reqheader) {
            LogUtil.log(head.getName() + " : " + head.getValue());
        }
        LogUtil.nestedLogClose();

        List<String> body = request.getBody();
        if (!(body.size() == 0)) {
            LogUtil.nestedLogStart("Body of Request");
            for (String temp : body)
                LogUtil.log(temp);
            LogUtil.nestedLogClose();
        }
    }

    public void logResponseDetails(HttpResponse response) {
        LogUtil.nestedLogStart("Headers of Response");
        Map<String, String> resheader = response.getHeaders();
        for (String name : resheader.keySet()) {
            String key = name.toString();
            String value = resheader.get(name).toString();
            LogUtil.log(key + " : " + value);
        }
        LogUtil.nestedLogClose();

        LogUtil.log("Response Status : " + response.getResponseCode().toString());
        LogUtil.log("Response Code : " + response.getResponseCode().getCode());
        if (!response.getBody().isEmpty()) {
            LogUtil.nestedLogStart("Response Body");
            LogUtil.log(response.getBody().toString());
            LogUtil.nestedLogClose();
        }
    }

    public HttpResponse executeGet(RequestBuilder request, String endpoint) {
        HttpResponse response = null;
        LogUtil.log("Endpoint Used : " + endpoint);

        logRequestDetails(request);
        int retryCount = 0;
        while (retryCount != 5 && response == null) {
            try {
                response = request.get(endpoint);
                logResponseDetails(response);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.log("Error while sending request Resending..");
            }
            retryCount++;
        }
        if (response == null) {
            assertThat("Error while sending request Resending..", false);
        }
        return response;
    }

    public HttpResponse executeDelete(RequestBuilder request, String endpoint) {
        HttpResponse response = null;
        LogUtil.log("Endpoint Used : " + endpoint);
        logRequestDetails(request);
        int retryCount = 0;
        while (retryCount != 5 && response == null) {
            try {
                response = request.delete(endpoint);

                logResponseDetails(response);

            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.log("Error while sending request Resending..");
            }
            retryCount++;
        }
        if (response == null) {
            assertThat("Error while sending request Resending..", false);
        }
        return response;
    }

    public HttpResponse executePost(RequestBuilder request, String endpoint) {
        HttpResponse response = null;
        LogUtil.log("Endpoint Used : " + endpoint);

        logRequestDetails(request);
        int retryCount = 0;
        while (retryCount != 5 && response == null) {
            try {
                response = request.post(endpoint);

                logResponseDetails(response);
            } catch (Exception e) {
              e.printStackTrace();
                LogUtil.log("Error while sending request Resending..");

            }
            retryCount++;
        }
        if (response == null) {
            assertThat("Error while sending request Resending..", false);
        }
        return response;
    }

    public HttpResponse executePut(RequestBuilder request, String endpoint) {
        HttpResponse response = null;
        LogUtil.log("Endpoint Used : " + endpoint);

        logRequestDetails(request);

        int retryCount = 0;
        while (retryCount != 10 && response == null) {
            try {
                response = request.put(endpoint);

                logResponseDetails(response);
            } catch (Exception e) {
              e.printStackTrace();
                LogUtil.log("Error while sending request Resending..");
            }
            retryCount++;
        }
        if (response == null) {
            assertThat("Error while sending request Resending..", false);
        }
        return response;
    }

    public static String getXMLTagwithValue(String fieldName, String value) {
        return String.format("<%s>%s</%s>", new Object[]{fieldName, value, fieldName});
    }

    public static String getJsonNodewithValue(String fieldName, String value) {
        return String.format("%s : %s", new Object[]{fieldName, value});
    }

    public void verifyResponseCode(HttpResponse response, int code) {
        assertThat("Mismatch in Response Code! : " + code, response.getResponseCode().getCode(), is(code));
    }

    public String getAnyJsonValue(String body, String key) {
        String value = "";
        if (body.contains(key)) {
            int start = body.indexOf(key) + key.length() + 3;
            int end = body.indexOf(",", start);
            value = body.substring(start, end - 1);
        }

        return value;
    }

    public String getJsonTagandValue(String tag, String value) {
        return "\"" + tag + "\":\"" + value + "\"";
    }
}
