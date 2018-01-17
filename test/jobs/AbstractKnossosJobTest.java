package jobs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;

import jobs.comparison.PholusLaunch;

import models.Ponderation;

import org.junit.Test;

import play.test.UnitTest;
import utils.Factory;
import utils.queue.PriorityJobQueue;
import utils.scripts.pholus.Pholus;

public class AbstractKnossosJobTest extends UnitTest {

	//@Test
	public void removesItselfFromPriorityQueueWhenFinished(){
		Factory factory = createMock(Factory.class);
		PriorityJobQueue priorityJobQueue = createNiceMock(PriorityJobQueue.class);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);

		replay(factory);
		
		PholusLaunch launcher = new PholusLaunch(null, factory);	
		priorityJobQueue.finishedJob(launcher);
		
		replay(priorityJobQueue);
	
		launcher._finally();
		
		verify(factory, priorityJobQueue);
	}

	@Test
	public void knossosJobGetsPriorityQueueFromFactory() {

		Pholus pholus = createNiceMock(Pholus.class);
		expect(pholus.getPonderations()).andReturn(new ArrayList<Ponderation>());
		Factory factory = createMock(Factory.class);
		PriorityJobQueue priorityJobQueue = createMock(PriorityJobQueue.class);
		expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue);

		replay(pholus, factory);

		PholusLaunch job = new PholusLaunch(pholus, factory);
		
		job.doJob();
		
		verify(pholus, factory);
	}
}
