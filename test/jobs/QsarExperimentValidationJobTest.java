package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.qsar.QsarExperimentValidationJob;
import jobs.qsar.RScriptLauncher;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;

public class QsarExperimentValidationJobTest extends UnitTest{
	
	QsarExperimentValidationJob validationJob;
	Factory factory;
	RScriptLauncher rscriptLauncher;
	QsarExperiment experiment;
	
	@Before
	public void setup(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
		User user = User.findByUserName("xmaresma");
		TestDataCreator creator = new TestDataCreator();
		QsarExperiment experiment = creator.createQsarExperiment(user); 
		factory = createNiceMock(Factory.class);
		rscriptLauncher = createNiceMock(RScriptLauncher.class);
		validationJob = new QsarExperimentValidationJob(factory, experiment.id,"qsar.pls");
		validationJob.setLauncher(rscriptLauncher);
		validationJob.setValidationStep(0);
	}
	
	@Test
	public void launcheExperimentValidationIteration(){
		replay(rscriptLauncher);
		validationJob.launch();
		verify(rscriptLauncher);
	}

}
