package utils.queue;

import java.util.List;

import jobs.KnossosJob;

import org.easymock.EasyMock;
import org.junit.Test;

import play.test.UnitTest;
import utils.queue.PriorityJobQueueItem;
import utils.queue.PriorityJobQueueItemImpl;

public class PriorityJobQueueItemTest extends UnitTest {

	@Test
	public void initializedCorrectly(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		assertEquals(1, item.getType());
		assertEquals("name", item.getName());
		assertTrue(item.isFinished());
		assertEquals(0, item.getInitialSize());
		assertEquals(0, item.getCurrentSize());
	}
	
	@Test
	public void itemIdsAreIncremental(){
		PriorityJobQueueItem item1 = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		PriorityJobQueueItem item2 = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		assertEquals(item1.getId() +1, item2.getId());
	}
	
	@Test
	public void addsKnossosJobToTheEndOfTheQueue(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job = EasyMock.createMock(KnossosJob.class);
		item.addJob(job);
		List<KnossosJob> jobsList = item.getJobs();
		assertEquals(job, jobsList.get(jobsList.size()-1));
	}
	
	@Test
	public void incrementsInitialSizeForEachAddedJob(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job = EasyMock.createMock(KnossosJob.class);
		int initialSize = item.getInitialSize();
		item.addJob(job);
		assertEquals(initialSize+1, item.getInitialSize());
		
	}
	
	@Test
	public void isNotFinishedAfterAddingJob(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job = EasyMock.createMock(KnossosJob.class);
		item.addJob(job);
		assertFalse(item.isFinished());
	}
	
	@Test
	public void becomesFinishedAfterPopingTheLastJob(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job = EasyMock.createMock(KnossosJob.class);
		item.addJob(job);
		item.popNextJob();
		assertTrue(item.isFinished());
	}

	@Test
	public void isNotFinishedAfterPopingNotLastJob(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job1 = EasyMock.createMock(KnossosJob.class);
		KnossosJob job2 = EasyMock.createMock(KnossosJob.class);
		item.addJob(job1);
		item.addJob(job2);
		item.popNextJob();
		assertFalse(item.isFinished());
	}
	
	
	@Test
	public void poppedJobWasTheFirstJobInTheList(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job1 = EasyMock.createMock(KnossosJob.class);
		KnossosJob job2 = EasyMock.createMock(KnossosJob.class);
		item.addJob(job1);
		item.addJob(job2);
		KnossosJob poppedJob = item.popNextJob();
		assertEquals(job1, poppedJob);
	}

	@Test
	public void poppingAJobRemovesItFromTheList(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job1 = EasyMock.createMock(KnossosJob.class);
		KnossosJob job2 = EasyMock.createMock(KnossosJob.class);
		item.addJob(job1);
		item.addJob(job2);
		KnossosJob poppedJob = item.popNextJob();
		assertFalse(item.getJobs().contains(poppedJob));
	}
	
	@Test
	public void poppingAJobDoesNotReduceInitialSize(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job1 = EasyMock.createMock(KnossosJob.class);
		KnossosJob job2 = EasyMock.createMock(KnossosJob.class);
		item.addJob(job1);
		item.addJob(job2);
		item.popNextJob();
		assertEquals(2, item.getInitialSize());
	}
	
	@Test
	public void poppingAJobFromAFinishedItemReturnsNull(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob poppedJob = item.popNextJob();
		assertNull(poppedJob);
	}

	@Test
	public void addingAJobIncrementsCurrentSize(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job = EasyMock.createMock(KnossosJob.class);
		int lastCurrentSize = item.getCurrentSize();
		item.addJob(job);
		assertEquals(lastCurrentSize+1, item.getCurrentSize());
	}
	
	@Test
	public void poppingAJobDecrementsCurrentSize(){
		PriorityJobQueueItem item = new PriorityJobQueueItemImpl(1, "name", "dbermudez", 1l);
		KnossosJob job1 = EasyMock.createMock(KnossosJob.class);
		item.addJob(job1);
		int lastCurrentSize = item.getCurrentSize();
		item.popNextJob();
		assertEquals(lastCurrentSize-1, item.getCurrentSize());
	}


}
