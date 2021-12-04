package net.boigroup.bdd.framework;

import net.boigroup.bdd.framework.Web.BrowserDriver;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.events.MakeAttachmentEvent;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogUtil {

	final static Logger LOG = Logger.getLogger(LogUtil.class);
	private static boolean isnestedLog;

	private LogUtil() {
    }
	public static void nestedLogStart(final String message){
		LOG.info("Logger : " + message);
		StepStartedEvent event = new StepStartedEvent(message);
		Description description = new Description();
		description.setValue(message);
		description.setType(DescriptionType.MARKDOWN);
		event.withTitle(message);
		Allure.LIFECYCLE.fire(event);
	}


	public static void nestedLogClose(){
		Allure.LIFECYCLE.fire(new StepFinishedEvent());
	}

    @Step("{0}")
    public static void log(final String message){
    	LOG.info("Logger : " + message);
    	
		StepStartedEvent event = new StepStartedEvent(message);    	
		Description description = new Description();
		description.setValue(message);
		description.setType(DescriptionType.MARKDOWN);
		event.withTitle(message);
				 
		Allure.LIFECYCLE.fire(event);  	    	
    	Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

    
    public static void logAttachment(String fileName, String text){
    	LOG.info("Logger : " + text);

    	Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/plain"));
    	//AllureReporter.addTextAttachment(fileName, text);
    }
    
    public static void logAttachmentHTML(String fileName, String text){
    	LOG.info("Logger : " + text);
    	Allure.LIFECYCLE.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "html/plain"));
    	//AllureReporter.addTextAttachment(fileName, text);
    }

	public static void logCSVAttachment(String sql, String csvFormat) {
		LOG.info("Logger : " + sql);
		Allure.LIFECYCLE.fire(new MakeAttachmentEvent(csvFormat.getBytes(), sql, "text/csv"));
	}

	public static void attachScreenshot(String message) {
		
		if (BrowserDriver.hasInstance()) {
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.simple()).takeScreenshot(BrowserDriver.getDriver());
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(fpScreenshot.getImage(),"png",bos);
				Allure.LIFECYCLE.fire(new MakeAttachmentEvent(bos.toByteArray(), message + " at " + getTimeStamp(), "Image/png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
	}

	public static void attachScreenshotOnWebElement(String message, WebElement element) {

		if (BrowserDriver.hasInstance()) {
			Screenshot fpScreenshot = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(100)).takeScreenshot(BrowserDriver.getDriver(), element);
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(fpScreenshot.getImage(),"png",bos);
				Allure.LIFECYCLE.fire(new MakeAttachmentEvent(bos.toByteArray(), message + " at " + getTimeStamp(), "Image/png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
	}

	public static void logVideoattachment(String fileName,byte[] video) {
		//StepStartedEvent event = new StepStartedEvent("Attaching Video");
		Allure.LIFECYCLE.fire(new MakeAttachmentEvent(video, fileName, "video/mp4"));
		//Allure.LIFECYCLE.fire(new StepFinishedEvent());
	}
}
