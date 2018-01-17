package jobs;

import jobs.comparison.PegasusExperimentLauncher;
import engine.bidimensional.BidimensionalEngine;
import models.ComparisonExperiment;
import models.User;
import static org.easymock.EasyMock.*;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.experiment.TestDataCreator;

public class PegasusExperimentLauncherTest extends UnitTest {

	TestDataCreator creator;

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		creator = new TestDataCreator();
	}

	@Test
	public void noExperimentsDeletedLaunch(){
		String username = "aperreau";
		User owner = User.findByUserName(username);
		Factory factory = createMock(Factory.class);
		ComparisonExperiment experiment = creator.getSmallExperiment(owner);
		experiment.save();

		PegasusExperimentLauncher el = new PegasusExperimentLauncher(experiment.id, owner, factory);

		experiment.delete();
		replay(factory);
		el.doJob();
		verify(factory);
	}

	@Test
	public void uses2DEngine(){
		String username = "aperreau";
		User owner = User.findByUserName(username);
		Factory factory = createMock(Factory.class);
		ComparisonExperiment experiment = creator.getSmallExperiment(owner);
		experiment.save();
		BidimensionalEngine engine = createMock(BidimensionalEngine.class);
		engine.setExperiment(experiment);
		engine.calculate();
		expect(factory.get2DEngine(experiment)).andReturn(engine);

		PegasusExperimentLauncher el = new PegasusExperimentLauncher(experiment.id, owner, factory);

		replay(factory, engine);
		el.doJob();
		verify(factory, engine);
	}
	
}
