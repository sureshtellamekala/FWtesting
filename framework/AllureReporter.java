package net.boigroup.bdd.framework;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import net.boigroup.bdd.framework.Jira.JiraZephyrTestCaseManagement;
import net.boigroup.bdd.framework.Web.BrowserDriver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.jbehave.core.model.*;
import org.jbehave.core.reporters.StoryReporter;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.config.AllureModelUtils;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.model.Description;
import ru.yandex.qatools.allure.model.DescriptionType;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.report.AllureReportBuilder;
import ru.yandex.qatools.allure.report.AllureReportBuilderException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static net.boigroup.bdd.framework.ConfigLoader.config;

public class AllureReporter implements StoryReporter {

    final static Logger LOG = Logger.getLogger(AllureReporter.class);

    private JiraZephyrTestCaseManagement jiraClient = new JiraZephyrTestCaseManagement();
	
    public Allure allure = Allure.LIFECYCLE;
    private final Map<String, String> suites = new HashMap<String, String>();

    public static boolean isStepFailed = false;
    public static Throwable stepFailedCause = new Throwable();

    private Map<String, String> scenarioTitle2Description = new HashMap<String, String>();
        
    public AllureReporter(){
    	cleanPreviousReport();
    }
    
    private static final ThreadLocal<String> currentSuite = new ThreadLocal<>();
	private static final ThreadLocal<String> currentSuiteName = new ThreadLocal<>();
	
	public static String storyName(){
		return currentSuiteName.get();
	}
	
	@Override
    public void beforeStory(Story story, boolean givenStory) {
    	String uuid = UUID.randomUUID().toString();
        currentSuite.set(uuid);
        
        TestSuiteStartedEvent event = new TestSuiteStartedEvent(uuid, story.getName());
        Label[] labels = constructLabels(story);
        
        for (Scenario scenario : story.getScenarios()) {
            scenarioTitle2Description.put(scenario.getTitle(), scenario.getMeta().getProperty("description"));
        }
        event.withLabels(AllureModelUtils.createTestFrameworkLabel("JBehave"), labels);
        event.withTitle(story.getName());
        allure.fire(event);
    }

    private Label[] constructLabels(Story story) {
        String path = story.getPath();
        String[] dirs = path.split("/");
       /* if (dirs.length > 3) {
            Label featureLabel = AllureModelUtils.createFeatureLabel(normalizeLabel(dirs[2]));
            Label storyLabel = AllureModelUtils.createStoryLabel(normalizeLabel(dirs[3]));
            return new Label[] { featureLabel, storyLabel };
        } else*/ if (dirs.length > 2) {
            Label featureLabel = AllureModelUtils.createFeatureLabel(normalizeLabel(dirs[dirs.length - 3]));
            Label storyLabel = AllureModelUtils.createStoryLabel(normalizeLabel(dirs[dirs.length - 2]));
            return new Label[] { featureLabel, storyLabel };
        } else if (dirs.length > 1) {
            Label featureLabel = AllureModelUtils.createFeatureLabel(normalizeLabel(dirs[dirs.length - 2]));
            Label storyLabel = AllureModelUtils.createStoryLabel(normalizeLabel(story.getName().replace(".story", "")));
            return new Label[] { featureLabel, storyLabel };

        }
        return new Label[] {};
    }

    private String normalizeLabel(String name) {
        return name;
    }

    @Override
    public void afterStory(boolean givenStory) {
        allure.fire(new TestSuiteFinishedEvent(currentSuite.get()));
    }

    public static final ThreadLocal<String> currentScenario = new ThreadLocal<>();
	private static final ThreadLocal<String> originalThreadName = new ThreadLocal<String>();
	private static final ThreadLocal<Throwable> stepFailed = new ThreadLocal<Throwable>();

    @Override
    public void beforeScenario(String scenarioTitle) {
        isStepFailed = false;
        stepFailedCause = new Throwable();
        LOG.info("Starting scenario { " + scenarioTitle + " }");
        originalThreadName.set(Thread.currentThread().getName());
        currentScenario.set(scenarioTitle);
//		Thread.currentThread().setName(scenarioTitle);

        stepFailed.set(null);
        TestCaseStartedEvent event = new TestCaseStartedEvent(currentSuite.get(), scenarioTitle);
        Description description = new Description();
        description.setValue(scenarioTitle2Description.get(scenarioTitle).toString());
        description.setType(DescriptionType.TEXT);
        event.setDescription(description);
        event.setTitle(scenarioTitle);
        allure.fire(event);
        allure.fire(new ClearStepStorageEvent());

        jiraClient.readJiraCycleMap();
    }

    static ThreadLocal<Map<String,String>> currentExample = new ThreadLocal<Map<String,String>>();
	//static ThreadLocal<Boolean> notFirstExample = new ThreadLocal<>();
    
    @Override
    public void beforeStep(String step) {
    	    
    	/**
    	int pos = step.indexOf(":");
        if (pos > -1) {
            step = step.substring(0, pos + 1);
        }
        allure.fire(new StepStartedEvent(step).withTitle(step));
        **/
        isStepFailed = false;
        stepFailedCause = new Throwable();

    	List<String> stepParts = Splitter.onPattern("<|>").splitToList(step);
		StringBuilder dataStepName = new StringBuilder();
		for (String stepPart : stepParts){
			if (currentExample.get() != null && currentExample.get().containsKey(stepPart)){
				dataStepName.append("[").append(currentExample.get().get(stepPart)).append("]");
			} else {
				dataStepName.append(stepPart);
			}
		}
		
		String dataStep = dataStepName.toString();
		
        int pos = dataStepName.indexOf(":");
        if (pos > -1) {        	
        	dataStep = dataStepName.substring(0, pos + 1);
        }
        //allure.fire(new StepStartedEvent(step).withTitle(step));
        allure.fire(new StepStartedEvent(dataStep).withTitle(dataStep));

        
    }
    
    public void addTextAttachment(String fileName, String text) {
            allure.fire(new MakeAttachmentEvent(text.getBytes(), fileName, "text/plain"));
    }
    
    
    public void addTextMessage(String message){
    	
    }
    
    @Override
    public void successful(String step) {
        if(isStepFailed){
            continueFailedStep(step, stepFailedCause);
        } else {
            allure.fire(new StepFinishedEvent());
        }
    }

    @Override
    public void ignorable(String step) {
        allure.fire(new StepCanceledEvent());
    }

    @Override
    public void notPerformed(String step) {
        allure.fire(new StepCanceledEvent());
    }
    
//    private static Throwable stepFailed = null;
    
    @Override
    public void failed(String step, Throwable cause) {
    	Throwable trueCause = cause.getCause();
    	stepFailed.set(trueCause);
    					
		StepFailureEvent event = new StepFailureEvent();		
		event.setThrowable(trueCause);		
		Allure.LIFECYCLE.fire(event);
        LogUtil.attachScreenshot("Test Failed");
        allure.fire(new StepFailureEvent().withThrowable(cause.getCause()));
        allure.fire(new StepFinishedEvent());
        allure.fire(new TestCaseFailureEvent().withThrowable(cause.getCause()));
    }

    public void continueFailedStep(String step, Throwable cause) {
        Throwable trueCause = cause.fillInStackTrace();
        stepFailed.set(trueCause);

        StepFailureEvent event = new StepFailureEvent();
        event.setThrowable(trueCause);
        Allure.LIFECYCLE.fire(event);

        allure.fire(new StepFailureEvent().withThrowable(cause.fillInStackTrace()));
        allure.fire(new StepFinishedEvent());
        allure.fire(new TestCaseFailureEvent().withThrowable(cause.fillInStackTrace()));
    }


    public void pending(String step) {
        allure.fire(new StepCanceledEvent());
        allure.fire(new TestCasePendingEvent().withMessage("PENDING"));
    }

    public void afterScenario() {
//    	Thread.currentThread().setName(originalThreadName.get());
        if (stepFailed.get() != null) {
            isStepFailed = true;
            Allure.LIFECYCLE.fire(new TestCaseFailureEvent().withThrowable(stepFailed.get()));
        }
        populateVideos();
        allure.fire(new TestCaseFinishedEvent());

        String failedReason = isStepFailed != false ? stepFailed.get().getLocalizedMessage() : "";
        jiraClient.prepareExecutionResults(currentScenario.get(), scenarioTitle2Description.get(currentScenario.get()), isStepFailed, failedReason);
    }

    public void storyNotAllowed(Story story, String filter) {
        // TODO
        // Allure doesn't support this
    }

    public void storyCancelled(Story story, StoryDuration storyDuration) {

    }

    @Override
    public void narrative(Narrative narrative) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void scenarioMeta(Meta meta) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        // TODO
        // Allure doesn't support this
    }
    
    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        // TODO
        // Allure doesn't support this
    }

    @Override
	public void example(Map<String, String> tableRow) {
        /*if(notFirstExample.get()) {
            if (stepFailed.get() != null) {
                Allure.LIFECYCLE.fire(new TestCaseFailureEvent().withThrowable(stepFailed.get()));
            }
            allure.fire(new TestCaseFinishedEvent());
        }else{
            notFirstExample.set(true);
        }*/
		currentExample.set(tableRow);
        /*stepFailed.set(null);
        String ScenarioKey=ConfigLoader.config().getString("Allure.scenarioKey","Scenario");
        String ScenarioNoKey=ConfigLoader.config().getString("Allure.scenarioNumberKey","TCNO");
        String descriptionKey=ConfigLoader.config().getString("Allure.descriptionKey","Description");
        String replaceScenario=currentExample.get().get(ScenarioKey);
        String ScenarioNo=currentExample.get().get(ScenarioNoKey);
        String replaceDescription=currentExample.get().get(descriptionKey);
        String title =  currentScenario.get().replace("<"+ScenarioKey+">",replaceScenario!=null?replaceScenario:"").replace("<"+ScenarioNoKey+">",ScenarioNo!=null?ScenarioNo+" : ":"");
        String descriptionText=replaceDescription!=null?replaceDescription:scenarioTitle2Description.get(currentScenario.get()).toString();
        TestCaseStartedEvent event = new TestCaseStartedEvent(currentSuite.get(), title);
        Description description = new Description();
        description.setValue(descriptionText);
        description.setType(DescriptionType.TEXT);
        event.setDescription(description);
        event.setTitle(title);
        allure.fire(event);
        allure.fire(new ClearStepStorageEvent());*/
	}

	@Override
    public void afterExamples() {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {

    }

    @Override
    public void restarted(String step, Throwable cause) {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void dryRun() {
        // TODO
        // Allure doesn't support this
    }

    @Override
    public void pendingMethods(List<String> methods) {
        // TODO
        // Allure doesn't support this
    }

    public String generateSuiteUid(Story story) {
        String uId = UUID.randomUUID().toString();
        synchronized (getSuites()) {
            getSuites().put(uId, story.getPath());
        }
        return uId;
    }

    public Map<String, String> getSuites() {
        return suites;
    }

    @Override
    public void restartedStory(Story arg0, Throwable arg1) {

    }

//	public void comment(String arg0) {
//		// TODO Auto-generated method stub		
//	}
	


	private static Properties populateProperty(String name,String key, Properties initial){
		if (ConfigLoader.config().containsKey(key)){
			initial.setProperty(name, ConfigLoader.config().getString(key));
		}
		return initial;
	}

	private static Properties populateProperty(String name, Properties initial) {
        if (ConfigLoader.config().containsKey(name)) {
            initial.setProperty(name, ConfigLoader.config().getString(name));
        }

        return initial;
    }
	
	private static void generateProperties(String directory){

        LOG.info("Generating properties for report");
		Properties environmentToShow = new Properties();
        environmentToShow = populateProperty("Project.Name", environmentToShow);
		environmentToShow = populateProperty("Environment","test.environment", environmentToShow);
		String env = ConfigLoader.config().getString("test.environment");
		environmentToShow = populateProperty("Endpoint",env + ".uri", environmentToShow);
		environmentToShow = populateProperty("webdriver.gridurl", environmentToShow);
		LOG.info("Properties ready");
		try {
			environmentToShow.store(Files.newWriter(Paths.get(directory,"environment.properties").toFile(), Charset.defaultCharset()), "execution properties");
			LOG.info("Properties written");
		} catch (IOException e) {
			LOG.debug("Cannot generate properties");
		}
	}

    private static Properties populateResourceProperty(Properties initial) {
        Iterator<String> keys = ConfigLoader.config().getKeys();
        while (keys.hasNext()){
            String key=keys.next();
            if(key.contains("rest.")){
                initial.setProperty(key, ConfigLoader.config().getString(key));
            }
        }
        return initial;
    }

    public static void main(String[] a){
        Properties prop = new Properties();
        populateResourceProperty(prop);
    }


    private static Optional<File> resultsDir = null;
	
	private static Optional<File> getResultsDir(){

		Optional<File> dire = Optional.absent();
		if (resultsDir == null || ! dire.isPresent()){
			Collection<File> dirs = FileUtils.listFilesAndDirs(new File(new File("").getAbsolutePath()), FalseFileFilter.INSTANCE, TrueFileFilter.INSTANCE); 
			dire = Iterables.tryFind(dirs, new Predicate<File>() {

				@Override
				public boolean apply(File input) {
					return input.getName().endsWith("allure-results");
				}
			});
			resultsDir = dire;
		}
		return resultsDir;
	}
	private static File reportDir = null;
	private static File getReportDir(){
		if (reportDir == null){
			if (getResultsDir().isPresent())
				reportDir = Paths.get(getResultsDir().get().getParent(),"allure-report").toFile();
		}
		return reportDir;
	}
	
	public static void cleanPreviousReport(){
		if (getResultsDir().isPresent()){
			try {
				FileUtils.forceDelete(getReportDir());
				FileUtils.forceDelete(getResultsDir().get());
			} catch (IOException e) {
				LOG.warn("Deleting previous results failed");
			}
		}
	}
	public static void tearDownBrowser(){
        BrowserDriver.closeDriver();
    }
	public static void generateReport(){
		String userSettings = String.format("%s/.m2/settings.xml", System.getProperty("user.home"));
		if (! Paths.get(userSettings).toFile().canRead()){
			String globalSettings = String.format("%s/conf/settings.xml", ConfigLoader.config().getString("M2_HOME"));
			if (! Paths.get(globalSettings).toFile().canRead()){
				LOG.error("Maven settings are not configured. Report cannot be generated");
			} else {
				try {
					FileUtils.copyFile(Paths.get(globalSettings).toFile(), Paths.get(userSettings).toFile());
				} catch (IOException e) {
					LOG.error("Cannot copy file due to ",e);

				}

			}
		}

		if (getResultsDir().isPresent()){
			generateProperties(getResultsDir().get().getAbsolutePath());
			AllureReportBuilder bl;
			try {
				bl = new AllureReportBuilder("1.5.4", getReportDir());
				bl.processResults(getResultsDir().get());
				bl.unpackFace();
				URI report = Paths.get(getReportDir().getAbsolutePath(),"index.html").toUri();
				LOG.info("Report generated to { " + report + " }");
				
			} catch (AllureReportBuilderException  cantCreateReport) {
				LOG.error("Report is not generated due to ",cantCreateReport);
			}
		}

	}

	@Override
	public void comment(String step) {
		// TODO Auto-generated method stub
		
	}
    private static CloseableHttpClient cl;

    private static void populateVideo(String link, String title) {
        try {
            HttpResponse videoResponse = cl.execute(new HttpGet(link));
            if (videoResponse.getStatusLine().getStatusCode() == 200) {
                InputStream videoFile = videoResponse.getEntity().getContent();
                Paths.get("target/allure-report/data/").toFile().mkdirs();
                java.nio.file.Files.copy(videoFile, Paths.get("target/allure-report/data/" + title+".mp4"), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                byte[] data = FileUtils.readFileToByteArray(new File("target/allure-report/data/" + title+".mp4"));
                LogUtil.logVideoattachment(title,data);
                ConfigLoader.config().clearProperty("webdriver.videos");
                //initial.setProperty(title, "data/" + title + ".mp4");
            }
        } catch (IOException | UnsupportedOperationException var5) {
            LOG.error("Video cannot be added { " + var5.getMessage() + " }");
        }

    }

    private static void populateVideos() {
        tearDownBrowser();
        Set<?> videos = ImmutableSet.copyOf(ConfigLoader.config().getList("webdriver.videos", Lists.newArrayList()));
        if (!videos.isEmpty()) {
            cl = HttpClients.createDefault();
            Iterator i$ = videos.iterator();

            while (i$.hasNext()) {
                Object video = i$.next();
                Map.Entry<String, String> videoData = (Map.Entry) ((Map) video).entrySet().iterator().next();
                populateVideo((String) videoData.getValue(), (String) videoData.getKey());
            }

            try {
                cl.close();
            } catch (IOException var5) {
                ;
            }
        }
    }

    public static void copyTargetFolderToReportBackupFolder(){
        try {
            if((!config().getString("JIRA.REPORTS_DIR").isEmpty()) && (!config().getString("JIRA.REPORTS_DIR").equalsIgnoreCase("NA")) ){
                File dir = new File(config().getString("JIRA.REPORTS_DIR")+"/"+config().getString("JIRA.BUILD_NUMBER"));
                LOG.info("Path: " + dir.getAbsolutePath());
                if(dir.exists()){
                    LOG.info("Folder already exists. Target folder not copied to the path: "+ dir.getAbsolutePath());
                } else {
                    boolean fileFolder = dir.mkdirs();
                    if(fileFolder){
                        String reportLoc = new File("src/").getAbsolutePath();
                        File reportLocFile = new File(reportLoc + "../../target");
                        File[] listOfFiles = reportLocFile.listFiles();
                        for (File file : listOfFiles) {
                            if(file.getName().equals("allure-report")){
                                FileUtils.copyDirectory(file, new File(dir + "/target/" + file.getName()));
                            }
                        }
                        LOG.info("Successfully copied target folder to backup folder at location = "+ dir.getAbsolutePath());
                    }else {
                        LOG.info("Failed to create the directory. Check the Permissions for: "+ dir.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error in copyTargetFolderToReportFolder method, Error = " + e.getMessage());
        }
    }
	
}
