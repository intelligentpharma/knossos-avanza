package jobs;

import java.util.ArrayList;
import java.util.List;

import jobs.comparison.PholusLaunch;

import models.Ponderation;
import models.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.queue.PriorityJobQueue;
import utils.scripts.pholus.Pholus;

public class PholusLaunchTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}

	@Test
	public void launchesPholusAndSavesAllPonderatrions(){
		User owner = User.findByUserName("aperreau");
		Pholus pholus = EasyMock.createMock(Pholus.class);
		pholus.calculate();
		Ponderation p1 = new Ponderation();
		p1.name = "p1";
		p1.weights.A = 1;
		p1.owner = owner;
		Ponderation p2 = new Ponderation();
		p2.name = "p2";
		p2.weights.A = 1;
		p2.owner = owner;
		List<Ponderation> ponderations = new ArrayList<Ponderation>();
		ponderations.add(p1);
		ponderations.add(p2);
		EasyMock.expect(pholus.getPonderations()).andReturn(ponderations);
		
		Factory factory = EasyMock.createMock(Factory.class);
		PriorityJobQueue priorityJobQueue = EasyMock.createNiceMock(PriorityJobQueue.class);
		EasyMock.expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);
		
		EasyMock.replay(pholus, factory, priorityJobQueue);
		
		PholusLaunch launcher = new PholusLaunch(pholus, factory);
		launcher.doJob();
		
		assertTrue(p1.isPersistent());
		assertTrue(p2.isPersistent());
		
		EasyMock.verify(pholus, factory, priorityJobQueue);
	}

}
