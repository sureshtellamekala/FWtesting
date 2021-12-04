package net.boigroup.bdd.framework;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.core.IsNull;
import java.util.List;

public class Asserts {
	private static final List<AssertionError> errors = Lists.newArrayList();
	private static final boolean soft = ConfigLoader.config().getBoolean("assert.soft",false);

    private static AllureReporter allureReporter = new AllureReporter();
	private static Thread hook = null;

	final static Logger LOG = Logger.getLogger(Asserts.class);
	private synchronized static void addError(AssertionError e){
		LOG.error("Error in test ",e);
		errors.add(e);
		if (hook == null){
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {				
				@Override
				public void run() {
					checkForErrors();				
				}
			}));
		}	
	}

	public static void assertThat(String reason, boolean assertion) {
		originalAssertThat(reason, assertion, null,null, false);
	}

	public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
		originalAssertThat(reason, false, actual, matcher, false);
	}

    public static void assertContinue(String reason, boolean assertion) {
        originalAssertThat(reason, assertion, null,null, true);
    }

	public synchronized static void checkForErrors(){
		if (! errors.isEmpty()){
			throw new AssertionError("Errors during test execution: " + Joiner.on("\n").join(errors));
		}
	}

	private static final <T> void originalAssertThat(String reason, boolean assertion, T actual,Matcher<? super T> matcher, boolean doContinue){
		try {
			if (actual == null &&  !(matcher instanceof IsNull) ){
				if (!assertion) {
					LogUtil.log(reason);
					Description description = new StringDescription();

					throw new AssertionError(reason);
				}
			} else {
				if (!matcher.matches(actual)) {
					Description description = new StringDescription();
					description.appendText(reason).appendText("\n Expected ").appendDescriptionOf(matcher).appendText("\n Actual ");
					matcher.describeMismatch(actual, description);
					LogUtil.log(reason);
					//LogUtil.log(description.toString());
					throw new AssertionError(description.toString());
				}
			}
		} catch (Throwable e){
            if(doContinue){
                allureReporter.isStepFailed = true;
                allureReporter.stepFailedCause = e;
            }else{
                Throwables.propagate(e);
            }
		}
	}
}

