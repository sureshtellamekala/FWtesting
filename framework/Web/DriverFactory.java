package net.boigroup.bdd.framework.Web;

import com.codeborne.selenide.Configuration;
import com.google.common.base.Strings;
import net.boigroup.bdd.framework.AllureReporter;
import net.boigroup.bdd.framework.ConfigLoader;
import net.boigroup.bdd.framework.LogUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sun.security.krb5.Config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.boigroup.bdd.framework.Asserts.assertThat;

class DriverFactory {
    private static Capabilities capabilities;
    private static final ThreadLocal<WebDriver> instances = new InheritableThreadLocal();
    final static Logger LOG = Logger.getLogger(DriverFactory.class);
    private static final String GRID_URL = ConfigLoader.config().getString("webdriver.gridurl", (String) null);
    private static final String FIREFOX = "firefox";
    private static final String CHROME = "chrome";
    private static final String IE = "ie";
    private static final String EDGE = "edge";
    private static final String YES= "true";

    private static WebDriver instance() {
        return instances.get();
    }
    public static boolean hasInstance() {
        if (instance() != null) {
            return true;
        }else {
            return false;
        }
    }
    private static void setCapabilities(Capabilities capabilities) {
        capabilities = capabilities;
    }

    private static boolean isGridAvailable(String gridUrl) {
        boolean result = true;

        try {
            CloseableHttpClient cl = HttpClients.createDefault();
            CloseableHttpResponse gridResponse = cl.execute(new HttpGet(new URI(gridUrl)));
            if (gridResponse.getStatusLine().getStatusCode() == 404) {
                //log.warn("Response from contacting grid {}", IOUtils.toString(gridResponse.getEntity().getContent()));
                result = false;
            }

            gridResponse.close();
            cl.close();
        } catch (Throwable var4) {
            result = false;
            LOG.error("Selenium grid not available due to { " + var4.getMessage() + " }");
        }

        return result;
    }

    private static void checkGridExtras(String gridUrl, RemoteWebDriver driver) {
        String gridExtras = GridUtils.getNodeExtras(gridUrl, driver);
        if (gridExtras == null) {
            //log.info("No grid extras foud");
        } else {
            //log.info("Grid extras available at {}", gridExtras);
        }

    }

    private static WebDriver createRemoteDriver() {
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.BROWSER, java.util.logging.Level.OFF);

        Configuration.timeout = 10000;
        int retryCount = 5, retryAttempt = 1;
        RemoteWebDriver result = null;
        try {
            if (!Strings.isNullOrEmpty(GRID_URL) && isGridAvailable(GRID_URL)) {

                //log.info("Using Selenium grid at {}", GRID_URL);
                String browserType = ConfigLoader.config().getString("webdriver.browser", "ie");

                //log.info("Creating driver for browser {}", browserType);
                URL gridUrl = new URL(GRID_URL);

                try{
                    if(BrowserDriver.hasInstance()){
                        LOG.info("BrowserDriver has instance. Closing it..");
                        BrowserDriver.closeDriver();
                        Thread.sleep(10000);
                    }
                    result = new RemoteWebDriver(gridUrl, getCapabilities(browserType));
                }catch (UnreachableBrowserException ex){
                    assertThat("Error: UnreachableBrowserException." + ex.getLocalizedMessage(), false);
                }


                /*while(retryAttempt<=retryCount){
                    try{
                        result = new RemoteWebDriver(gridUrl, getCapabilities(browserType));
                        //break;
                    } catch(UnreachableBrowserException e){
                        Thread.sleep(10000);
                        //if(retryAttempt > retryCount){
                            LOG.info("Remote Web Driver cannot be reached at this moment" + e.getLocalizedMessage());
                            LogUtil.log("Remote Web Driver cannot be reached at this moment");
                        //}
                    }
                    retryCount++;
                }*/

                //RemoteWebDriver result = new RemoteWebDriver(gridUrl, getCapabilities(browserType));

                //log.info("Driver instance created {}", result);
                //log.info("Test session id {}", result.getSessionId());
                //log.info("Executing on node {}", GridUtils.getNode(GRID_URL, result));
                if (isVideoRecording()) {
                    String videoLink = String.format("%s/download_video/%s.mp4", GridUtils.getNodeExtras(gridUrl.toString(), result), result.getSessionId().toString());
                    ConfigLoader.config().addProperty("webdriver.video", videoLink);
                    List<Object> videos = ConfigLoader.config().getList("webdriver.videos", new ArrayList());
                    String title = AllureReporter.storyName();

                    if (Strings.isNullOrEmpty(title)) {
                        title = "video_recording " + result.getSessionId();
                    }

                    //log.info("Session for story {}", title);
                    videos.add(Collections.singletonMap(title, videoLink));
                    ConfigLoader.config().setProperty("webdriver.videos", videos);
                    checkGridExtras(GRID_URL, result);
                }
                return result;
            } else {
                //log.info("Grid not detected");
                if (ConfigLoader.config().containsKey("noui")) {
                    return new HtmlUnitDriver(true);
                } else if ("ie".equalsIgnoreCase(ConfigLoader.config().getString("webdriver.browser"))) {

                    // Get local Iexplorer driver
                    String file = new File("src/main/resources/").getAbsoluteFile().toString();
                    file = file.replace("src\\main\\resources", "libs\\");

                    String path = file + "IEDriverServer.exe";
                    System.setProperty("webdriver.ie.driver", path);
                    DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
                    caps.setCapability("ignoreZoomSetting", true);
                    caps.setCapability("silent", true);
                    caps.setCapability(InternetExplorerDriver.SILENT, true);
                    caps.setCapability(InternetExplorerDriver.LOG_LEVEL, "INFO");
                    caps.setCapability("unexpectedAlertBehaviour", "ignore");
                    caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "ignore");
                    caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

                    return new InternetExplorerDriver(caps);
                } else if ("chrome".equalsIgnoreCase(ConfigLoader.config().getString("webdriver.browser"))) {

                    // Get local Chrome driver
                    String file = new File("src/main/resources/").getAbsoluteFile().toString();
                    file = file.replace("src\\main\\resources", "libs\\");

                    String path = file + "chromedriver.exe";
                    System.setProperty("webdriver.chrome.driver", path);

                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("start-maximized");
                    options.addArguments("--disable-extensions");

                    return new ChromeDriver(options);

                } else if ("edge".equalsIgnoreCase(ConfigLoader.config().getString("webdriver.browser"))){

                    //Get local Edge driver
                    String file = new File("src/main/resources/").getAbsoluteFile().toString();
                    file = file.replace("src\\main\\resources", "libs\\");

                    String path = file + "msedgedriver.exe";
                    System.setProperty("webdriver.edge.driver", path);
                    EdgeOptions options = new EdgeOptions();

                    return new EdgeDriver(options);
                } else {
                    FirefoxProfile profile = new FirefoxProfile();
                    profile.setPreference("plugin.state.java", 2);
                    return new FirefoxDriver(profile);
                }
            }
        } catch (MalformedURLException var6) {
            throw new RuntimeException("Cannot connect to grid", var6);
        } catch (Exception ex) {
            //assertThat("Error in creating the Remote driver." + ex.getLocalizedMessage(), false);resu
            getDriver().quit();
            LOG.error("Error in creating the Remote driver: " + ex.getLocalizedMessage());
            throw new RuntimeException("Error in creating the Remote driver - Cannot connect to grid", ex);
        }
    }

    private static boolean isVideoRecording() {
        return YES.equalsIgnoreCase(ConfigLoader.config().getString("webdriver.video","false"));
    }

    public static void closeDriver() {
        if (instance() != null) {
            try {
                instance().close();
                instance().quit();
            } catch (Throwable ignored) {
            }
            instances.set(null);
        }

    }

    public static void quitDriver() {
        if (instance() != null) {
            try {
                instance().quit();
            } catch (Throwable ignored) {
            }
            instances.set(null);
        }

    }

    private static boolean dontUseQuit() {
        return ConfigLoader.config().getBoolean("webdriver.close.only", true);
    }

    private static Capabilities getCapabilities(String driverType) {
        DesiredCapabilities result = DesiredCapabilities.htmlUnit();
        byte var3 = -1;
        switch (driverType.hashCode()) {
            case -1361128838:
                if (driverType.equals(CHROME)) {
                    var3 = 1;
                }
                break;
            case -849452327:
                if (driverType.equals(FIREFOX)) {
                    var3 = 0;
                }
                break;
            case 3356:
                if (driverType.equals(IE)) {
                    var3 = 2;
                }
                break;
            case 3108285:
                if (driverType.equals(EDGE)) {
                    var3 = 3;
                }
        }

        switch (var3) {
            case 0:
                result = DesiredCapabilities.firefox();
                FirefoxProfile profile = new FirefoxProfile();
                profile.setPreference("plugin.state.java", 2);
                result.setCapability("firefox_profile", profile);
                break;
            case 1:
                result = DesiredCapabilities.chrome();
                break;
            case 2:
                DesiredCapabilities caps = DesiredCapabilities.internetExplorer();

                LoggingPreferences logs = new LoggingPreferences();
                logs.enable(LogType.DRIVER, java.util.logging.Level.OFF);
                logs.enable(LogType.BROWSER, java.util.logging.Level.OFF);
                logs.enable(LogType.CLIENT, java.util.logging.Level.OFF);
                logs.enable(LogType.DRIVER, java.util.logging.Level.OFF);
                logs.enable(LogType.PERFORMANCE, java.util.logging.Level.OFF);
                logs.enable(LogType.PROFILER, java.util.logging.Level.OFF);
                logs.enable(LogType.SERVER, java.util.logging.Level.OFF);

                caps.setCapability(CapabilityType.LOGGING_PREFS, logs);

                caps.setCapability("ignoreZoomSetting", true);
                caps.setCapability("unexpectedAlertBehaviour", "ignore");
                caps.setCapability("silent", true);
                caps.setCapability(InternetExplorerDriver.LOG_LEVEL, Level.ERROR);
                caps.setCapability(InternetExplorerDriver.SILENT, true);
                caps.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, "ignore");
                caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);

                result = caps.internetExplorer();
                break;
            case 3:
                result = DesiredCapabilities.edge();
        }

        return concatenate(capabilities, result);
    }

    private static Capabilities concatenate(Capabilities... toJoin) {
        return new DesiredCapabilities(toJoin);
    }

    private static Thread closeDriver(final WebDriver driver) {
        return new Thread(() -> {
            if (driver != null && !ConfigLoader.config().getBoolean("webdriver.dontclose", false)) {
                try {
                    driver.close();
                    if (!DriverFactory.dontUseQuit()) {
                        driver.quit();
                    }
                } catch (Exception ignored) {

                }
            }

        });
    }

    static EventFiringWebDriver e_driver;
    static WebEventListener eventListener;

    public static WebDriver getDriver() {
        String browserType = ConfigLoader.config().getString("webdriver.browser", "ie");
        if (instance() == null) {
            setCapabilities(new DesiredCapabilities());
            Runtime.getRuntime().addShutdownHook(closeDriver(instance()));
            e_driver = new EventFiringWebDriver(createRemoteDriver());
            eventListener = new WebEventListener();
            e_driver.register(eventListener);
            instances.set(e_driver);
        }
        return instance();
    }


    private static WebDriverWait delay(long timeInSeconds) {
        return new WebDriverWait(getDriver(), timeInSeconds);
    }


}

