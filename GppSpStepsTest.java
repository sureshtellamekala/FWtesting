package net.boigroup.gppsp;

import net.boigroup.bdd.framework.BddRunner;
import net.boigroup.bdd.framework.SequenceBddRunner;
import org.apache.log4j.Logger;

import java.util.List;

import static net.boigroup.bdd.framework.ConfigLoader.config;

public class GppSpStepsTest extends BddRunner {
	final static Logger LOG = Logger.getLogger(GppSpStepsTest.class);
	SequenceBddRunner sequenceStory = new SequenceBddRunner();
	@Override
	protected List<String> storyPaths() {
		return getFeatures();
	}

	public GppSpStepsTest() throws Throwable{

		setLogs();

		String getCutOffMeta = config().getString("bdd.SequenceMetaFilter");
		if (!getCutOffMeta.equals("")){
			LOG.info("Testing=" + config().getString("bdd.SequenceMetaFilter"));
			//super.sequenceTest();
			sequenceStory.run();
		}
	}

	public void setLogs(){
		System.out.println("Calling setLogs");
		Logger.getLogger("org.openqa.selenium").setLevel(org.apache.log4j.Level.OFF);
		Logger.getLogger("org.apache.http.headers").setLevel(org.apache.log4j.Level.OFF);
		Logger.getLogger("org.apache.http.wire").setLevel(org.apache.log4j.Level.OFF);
		Logger.getLogger("httpclient.wire.content").setLevel(org.apache.log4j.Level.OFF);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
	}

}