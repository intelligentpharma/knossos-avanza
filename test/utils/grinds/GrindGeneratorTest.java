package utils.grinds;

import static org.easymock.EasyMock.createMock;
import models.AlignmentBox;
import models.GrindVector;
import models.MoleculeDatabase;
import models.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.experiment.TestDataCreator;
import utils.grinds.GrindGenerator;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueImpl;
import utils.queue.PriorityJobQueueItem;
import utils.queue.PriorityJobQueueItemImpl;

public class GrindGeneratorTest extends UnitTest{

	private AlignmentBox box;
	private Factory factory;
	private MoleculeDatabase database;
	private GrindGenerator grindGenerator;
	private TestDataCreator dataCreator;
	private float binSize;
	
	@Before
	public void setup(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
		
		factory = createMock(Factory.class);

		dataCreator = new TestDataCreator();
		database = dataCreator.createSingleDeploymentDatabase();
		User owner = User.findByUserName("dbermudez");
		database.owner = owner;
		database.save();

		binSize = 0.8f;
		grindGenerator = new GrindGenerator(factory, database.id, binSize);
	}

	@Test
	public void generateQueuesAItemInThePriorityQueue(){
		PriorityJobQueue priorityJobQueue = createMock(PriorityJobQueue.class);
		
		EasyMock.expect(factory.getPriorityJobQueue()).andReturn(priorityJobQueue).anyTimes();
		priorityJobQueue.add(EasyMock.anyObject(PriorityJobQueueItem.class));
		
		GrindVector grindVector = new GrindVector("", "", "", database.molecules.get(0).deployments.get(0).id, database.id, "", binSize);
		grindVector.save();
		
		EasyMock.replay(factory, priorityJobQueue);
		
		grindGenerator.generate();
		
		EasyMock.verify(factory, priorityJobQueue);
	}
	
}
