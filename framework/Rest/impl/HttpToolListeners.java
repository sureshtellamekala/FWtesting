package net.boigroup.bdd.framework.Rest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpToolListeners {
    private static List<HttpToolListener> httpToolListeners = new ArrayList();

    public static void addListener(HttpToolListener httpToolListener) {
        httpToolListeners.add(httpToolListener);
    }

    public static void removeAllListeners() {
        httpToolListeners.clear();
    }

    public static List<HttpToolListener> getListeners() {
        return Collections.unmodifiableList(httpToolListeners);
    }

    private HttpToolListeners() {
    }
}

