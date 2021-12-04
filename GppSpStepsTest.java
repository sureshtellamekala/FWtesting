package net.boigroup.gppsp;

import net.boigroup.bdd.framework.BddRunner;
import net.boigroup.bdd.framework.SequenceBddRunner;

import java.util.List;

import static net.boigroup.bdd.framework.ConfigLoader.config;

public class GppSpStepsTest extends BddRunner {

	SequenceBddRunner sequenceStory = new SequenceBddRunner();
	@Override
	protected List<String> storyPaths() {
		return getFeatures();
	}

	public GppSpStepsTest() throws Throwable{
		String getCutOffMeta = config().getString("bdd.SequenceMetaFilter");
		if (!getCutOffMeta.equals("")){
			System.out.println("Testing=" + config().getString("bdd.SequenceMetaFilter"));
			//super.sequenceTest();
			sequenceStory.run();
		}
	}

}