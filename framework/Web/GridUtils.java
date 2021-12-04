package net.boigroup.bdd.framework.Web;

import net.boigroup.bdd.framework.ConfigLoader;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class GridUtils {
    final static Logger LOG = Logger.getLogger(GridUtils.class);
    private static RedirectionCalculator calculator = null;

    public GridUtils() {
    }

    public static String getNode(String gridUrl, WebDriver driver) {
        String nodeUrl = null;
        CloseableHttpClient cl = HttpClients.createDefault();
        CloseableHttpResponse gridResponse = null;

        try {
            URI gridUri;
            try {
                gridUri = new URI(gridUrl);
                gridUri = new URI(String.format("%s://%s:%s/grid/api/testsession?session=%s", gridUri.getScheme(), gridUri.getHost(), gridUri.getPort(), ((RemoteWebDriver)driver).getSessionId()));
                LOG.debug("Using session uri { " + gridUri + " }");
                gridResponse = cl.execute(new HttpPost(gridUri));
                String response = IOUtils.toString(gridResponse.getEntity().getContent());
                LOG.debug("Response from contacting grid { "+response+" }");
                Map<String, String> result = (Map)(new Gson()).fromJson(response, HashMap.class);
                nodeUrl = (String)result.get("proxyId");
            } catch (URISyntaxException | JsonSyntaxException | IOException var19) {
                try {
                    gridUri = new URI(gridUrl);
                    if (cl.execute(new HttpGet(new URI(String.format("%s://%s:%s/", gridUri.getScheme(), gridUri.getHost(), 3000)))).getStatusLine().getStatusCode() == 200) {
                        nodeUrl = gridUrl;
                    }
                } catch (Exception var18) {
                    LOG.error("Issue with getting node from grid { " +var19.getMessage()+" }");
                }
            }
        } finally {
            try {
                gridResponse.close();
                cl.close();
            } catch (IOException var17) {
                ;
            }

        }

        return nodeUrl;
    }

    public static String getNodeExtras(String gridUrl, WebDriver driver) {
        String extrasUrl = null;
        String nodeUrl = getNode(gridUrl, driver);
        String extrasPort = ConfigLoader.config().getString("grid.extras.port", "3000");
        if (nodeUrl != null) {
            try {
                extrasUrl = String.format("%s://%s:%s", (new URI(nodeUrl)).getScheme(), (new URI(nodeUrl)).getHost(), extrasPort);
                if (calculator != null) {
                    extrasUrl = calculator.calculate(new URI(extrasUrl)).toString();
                }
            } catch (URISyntaxException var6) {
                LOG.debug("Issue with selenium grid extras uri", var6);
            }
        }

        return extrasUrl;
    }

    public static synchronized void registerRedirectionCalculator(RedirectionCalculator calculator) {
        calculator = calculator;
    }
}
