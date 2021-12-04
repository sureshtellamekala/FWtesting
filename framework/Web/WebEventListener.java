package net.boigroup.bdd.framework.Web;

import net.boigroup.bdd.framework.LogUtil;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

class WebEventListener extends AbstractWebDriverEventListener {
    final static Logger LOG = Logger.getLogger(WebEventListener.class);

    public void beforeNavigateTo(String url, WebDriver driver) {
        LOG.info("Before navigating to: '" + url + "'");
    }

    public void afterNavigateTo(String url, WebDriver driver) {
        LogUtil.nestedLogStart("Web Page URL");
        LogUtil.log(url);
        LogUtil.nestedLogClose();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOG.info("After navigating to: '" + url + "'");
        //LogUtil.attachScreenshot("Navigated to url ");
    }



    public void beforeClickOn(WebElement element, WebDriver driver) {
        LOG.info("Trying to click on: " + element.toString());
        //LogUtil.attachScreenshot("Before Click on "+element.getText());
    }



    public void afterClickOn(WebElement element, WebDriver driver) {
        LOG.info("Clicked on: " + element.toString());
    }

    public void onException(Throwable error, WebDriver driver) {
        LOG.info("Error occurred: " + error);
    }
}