package net.boigroup.bdd.framework;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.thoughtworks.paranamer.NullParanamer;
import org.apache.log4j.Logger;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.failures.RethrowingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.AbsolutePathCalculator;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.*;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.boigroup.bdd.framework.ConfigLoader.config;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class SequenceBddRunner extends ConfigurableEmbedder {

	private Configuration configuration;
    static String packageName = config().getString("bdd.steps.package");
    final static Logger LOG = Logger.getLogger(SequenceBddRunner.class);

    public SequenceBddRunner() {
        super();
        configuration = new Configuration() {
        };
        configuration.useStoryReporterBuilder(new StoryReporterBuilder().withReporters(new AllureReporter()));
        configuration.useFailureStrategy(new RethrowingFailure());
        configuration.useKeywords(new LocalizedKeywords(Locale.ENGLISH));
        configuration.usePathCalculator(new AbsolutePathCalculator());
        configuration.useParameterControls(new ParameterControls());
        configuration.useParameterConverters(new ParameterConverters(null));
        configuration.useParanamer(new NullParanamer());
        configuration.usePendingStepStrategy(new PassingUponPendingStep());
        configuration.useStepCollector(new MarkUnmatchedStepsAsPending());
        configuration.useStepdocReporter(new PrintStreamStepdocReporter());
        configuration.useStepFinder(new StepFinder());
        configuration.useStepMonitor(new SilentStepMonitor());
        configuration.useStepPatternParser(new RegexPrefixCapturingPatternParser());
        configuration.useStoryControls(new StoryControls());
       // configuration.useStoryParser(new RegexStoryParser(configuration.keywords()));
        configuration.useStoryPathResolver(new UnderscoredCamelCaseResolver());
        configuration.useViewGenerator(new FreemarkerViewGenerator());
        configuration.useStoryLoader(new LoadFromClasspath());
        configuration.storyControls().useStoryMetaPrefix("story_").useScenarioMetaPrefix("scenario_");
        
        //configuration.storyControls().us
        //configuration.storyControls().m
        //configuration.storyControls().doMetaByRow(true);

        EmbedderControls embedderControls = configuredEmbedder().embedderControls();
        embedderControls.doBatch(false);
        embedderControls.doGenerateViewAfterStories(true);
        embedderControls.doIgnoreFailureInStories(false);
        embedderControls.doIgnoreFailureInView(false);
        embedderControls.doSkip(false);
        embedderControls.doVerboseFailures(false);
        embedderControls.doVerboseFiltering(false);
        embedderControls.useStoryTimeoutInSecs(1000);
        //embedderControls.useStoryTimeouts(600);
        embedderControls.useThreads(1);        
    }

    @Override
    public Configuration configuration() {
        return configuration;
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        // varargs, can have more that one steps classes
    	return new InstanceStepsFactory(configuration(), discoverSteps(packageName));
    	//return new InstanceStepsFactory(configuration(), new PaymentSteps());
    }
    private static final List stepClasses = Lists.newArrayList(); 
    private List<?> discoverSteps(String packageName){
    	if (stepClasses.isEmpty()){

			Reflections reflections = new Reflections(packageName);
			Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(StorySteps.class);
			for (Class<?> logic : annotated){
				try {
					LOG.info("packageName" + packageName);
					LOG.info("Found steps class {" + logic + " }");
					stepClasses.add(logic.newInstance());
				} catch (Exception e) {
					LOG.error("Injecting logic for {" + logic +"} failed due to {" + e + "}");
				}
			}
		}
		return stepClasses;
	}
    
	 public static Class<?> getCallerClass()  {
		 	StackTraceElement[] st = Thread.currentThread().getStackTrace();
	        StackTraceElement rawFQN = Iterables.find(Lists.newArrayList(st), new Predicate<StackTraceElement>() {

				@Override
				public boolean apply(StackTraceElement input) {
					return ! (input.toString().contains("gppsp")||
							input.toString().contains("internal") ||
							input.toString().startsWith("java.lang")
							);
				}
			});
	        try {
	        	String fqn = rawFQN.toString().split("\\(")[0].split("<")[0];
				return Class.forName(fqn.substring(0, fqn.lastIndexOf('.')));
			} catch (ClassNotFoundException e) {
				return null;
			}
	    }

    private Class<?> codeLocation(){
    	LOG.info("packageName" + packageName);
    	if (discoverSteps(packageName).isEmpty()){
			return getCallerClass();
		} else {
			return discoverSteps(packageName).get(0).getClass();
		}
	}
    private List<String> asList(String testing) {		
    	List<String> lst = Arrays.asList(testing.split(";"));
		return lst;		
	}
    
    public void run() throws Throwable {
        try {
            Embedder embedder = configuredEmbedder();
            embedder.useMetaFilters(asList(config().getString("bdd.SequenceMetaFilter")));
            
            try {
                String story = config().getString("jbehave.story");
                List<String> stories = new StoryFinder().findPaths(codeLocationFromClass(codeLocation()),
                        "**/features/**" + story, "");
                LOG.info("----------------------------------------------stories" + stories);
                embedder.runStoriesAsPaths(stories);
            } finally {
                embedder.generateCrossReference();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
