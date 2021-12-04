package net.boigroup.bdd.framework.Web;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import net.boigroup.bdd.framework.StorySteps;


@StorySteps
public class BrowserDriver {

    public static void open(String url) {
        WebDriver driver = DriverFactory.getDriver();
        driver.manage().window().maximize();
        driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL,"0"));
        driver.get(url);
        ((JavascriptExecutor) driver).executeScript("window.focus();");
    }

    public static WebDriver getDriver(){
      return DriverFactory.getDriver();
    }

    public static void closeDriver() {
        DriverFactory.closeDriver();
    }

    public static boolean hasInstance() {
        return DriverFactory.hasInstance();
    }
}
