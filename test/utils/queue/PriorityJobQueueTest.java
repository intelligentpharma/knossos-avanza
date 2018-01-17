package utils.queue;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import jobs.KnossosJob;

import org.junit.Test;

import play.test.UnitTest;
import utils.FactoryImpl;

public class PriorityJobQueueTest extends UnitTest {

	@Test
	public void initializedAsEmptyQueue(){
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), 0, 10);
		assertTrue(queue.getQueuedItems().isEmpty());
	}

	@Test
	public void addingAnEmptyItemThrowsException(){
		try{
			int runningQueueSize = 1;
			PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
			expect(item.isFinished()).andReturn(true).anyTimes();
			replay(item);
			
			PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
			queue.add(item);
			fail("Should have thrown exception");
		}catch(RuntimeException e){
			assertEquals("Tried to add an item with no jobs in the queue", e.getMessage());
		}
	}

	@Test
	public void addedItemIsLastItem(){
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();

		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), 0, 10);
		replay(item);
		
		queue.add(item);
		
		List<PriorityJobQueueItem> items = queue.getQueuedItems();
		
		assertEquals(item, items.get(items.size()-1));		
	}
	
	@Test
	public void popsAsManyJobsAsNeededToFillRunningListWhenAddingNewJob(){
		int runningQueueSize = 5;
		KnossosJob job = createMock(KnossosJob.class);
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();
//		expect(item.popNextJob()).andReturn(job).times(runningQueueSize);
		expect(item.getExperiment()).andReturn(null).anyTimes();
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item);
		
		queue.add(item);
		
		verify(item);
	}
	
	@Test
	public void stopsPoppingJobsWhenNewAddedItemIsEmptied(){
		int runningQueueSize = 5;
		KnossosJob job = createMock(KnossosJob.class);
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).times(2);
		expect(item.popNextJob()).andReturn(job).once();
		expect(item.isFinished()).andReturn(true).anyTimes();
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item);
		
		queue.add(item);
		
		verify(item);
	}
	
	@Test
	public void addingMoreItemsKeepsFillingTheRunningListWhenNotFull() {
		int runningQueueSize = 5;
		KnossosJob job = createMock(KnossosJob.class);
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).times(4);
		expect(item1.popNextJob()).andReturn(job).times(3);
		expect(item1.isFinished()).andReturn(true).anyTimes();
		
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.popNextJob()).andReturn(job).times(2);

		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2);
		
		queue.add(item1);
		queue.add(item2);
		
		verify(item1, item2);
	}

	@Test
	public void noJobsPoppedWhenRunningListFull() {
		int runningQueueSize = 0;
		PriorityJobQueueItem item = createNiceMock(PriorityJobQueueItem.class);

		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		
		replay(item);
		
		queue.add(item);
	}
	
	@Test
	public void ifAllJobsAreAddedToRunningListItemIsRemovedFromQueuedItems(){
		int runningQueueSize = 5;
		KnossosJob job = createMock(KnossosJob.class);
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).times(4);
		expect(item.popNextJob()).andReturn(job).times(3);
		expect(item.isFinished()).andReturn(true).anyTimes();
		
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item);
		
		queue.add(item);
		assertFalse(queue.getQueuedItems().contains(item));
		
		verify(item);
	}

	@Test
	public void whenAJobIsAddedToRunningListItIsLaunched(){
		int runningQueueSize = 1;
		KnossosJob job = createMock(KnossosJob.class);
		job.launch();
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();
		expect(item.popNextJob()).andReturn(job).times(1);
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);

		replay(item);
		
		queue.add(item);
		
		verify(item);
	}

	@Test
	public void whenARunningJobIsFinishedItsRemovedFromTheRunningList(){
		int runningQueueSize = 1;
		KnossosJob job = createMock(KnossosJob.class);

		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
	
		String queueName = job.getPartition();
		
		if (!queue.runningJobs.containsKey(queueName)) {			
			queue.runningJobs.put(queueName, new ArrayList<KnossosJob>());
		}
		queue.runningJobs.get(queueName).add(job);

		queue.finishedJob(job);

		assertFalse(queue.runningJobs.get(queueName).contains(job));
	}

	@Test
	public void whenARunningJobIsFinishedThrowsExceptionIfNotInRunningList(){
		try{
			int runningQueueSize = 1;
			KnossosJob job = createMock(KnossosJob.class);
	
			PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
			
			replay(job);
			
			queue.finishedJob(job);
			
			fail("should have thrown exception");
		}
		catch(Exception e){
			assertTrue(e instanceof RuntimeException);
			assertEquals("Job was not runnning", e.getMessage());
		}
	}
	
	@Test
	public void whenARunningJobIsFinishedTheFirstQueuedJobIsAddedToTheRunningList(){
		int runningQueueSize = 1;
		KnossosJob job = createMock(KnossosJob.class);
		job.launch();
		expectLastCall().times(2);
		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();
		expect(item.popNextJob()).andReturn(job).times(2);
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);

		replay(item, job);
		
		queue.add(item);
		queue.finishedJob(job);
		
		verify(item);
	}
	
	@Test
	public void whenARunningJobIsFinishedTheFirstQueuedJobIsPopped(){
		int runningQueueSize = 1;
		KnossosJob job1 = createMock(KnossosJob.class);
		job1.launch();
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).times(2);
		expect(item1.popNextJob()).andReturn(job1).once();
		expect(item1.isFinished()).andReturn(true).anyTimes();
		
		KnossosJob job2 = createMock(KnossosJob.class);
		job2.launch();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.popNextJob()).andReturn(job2).once();
		PriorityJobQueue queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);

		replay(item1, job1, item2, job2);
		
		queue.add(item1);
		queue.add(item2);
		queue.finishedJob(job1);
		
		verify(item1, job1, item2, job2);
	}
	
	@Test
	public void doesNotFailWhenAJobFinishesAndThereAreNoMoreInQueue() {
		int runningQueueSize = 1;
		KnossosJob job1 = createMock(KnossosJob.class);
		job1.launch();
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).times(2);
		expect(item1.isFinished()).andReturn(true).anyTimes();
		expect(item1.popNextJob()).andReturn(job1).times(1);
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, job1);

		queue.add(item1);
		queue.finishedJob(job1);

		assertEquals(0, queue.getQueuedItems().size());
		verify(item1, job1);
	}
	
	@Test
	public void movingItemToNegativePositionMovesItToFirstPosition(){
		int runningQueueSize = 0;
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).anyTimes();
		expect(item1.getId()).andReturn(1L).anyTimes();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.getId()).andReturn(2L).anyTimes();
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2);

		queue.add(item1);
		queue.add(item2);
		
		queue.reprioritize(item2.getId(), -1);

		assertEquals(0, queue.getQueuedItems().indexOf(item2));
		verify(item1, item2);
	}
	
	@Test
	public void attemptingToMoveItemBeyondLastPositionMovesItemToLastPosition() {
		int runningQueueSize = 0;
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).anyTimes();
		expect(item1.getId()).andReturn(1L).anyTimes();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.getId()).andReturn(2L).anyTimes();
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2);

		queue.add(item1);
		queue.add(item2);
		
		queue.reprioritize(item1.getId(), 12);

		assertEquals(1, queue.getQueuedItems().indexOf(item1));
		verify(item1, item2);
	}
	
	@Test
	public void movedItemIsAtCorrectPosition(){
		int runningQueueSize = 0;
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).anyTimes();
		expect(item1.getId()).andReturn(1L).anyTimes();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.getId()).andReturn(2L).anyTimes();
		PriorityJobQueueItem item3 = createMock(PriorityJobQueueItem.class);
		expect(item3.isFinished()).andReturn(false).anyTimes();
		expect(item3.getId()).andReturn(3L).anyTimes();
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2, item3);

		queue.add(item1);
		queue.add(item2);
		queue.add(item3);
		
		int newPosition = 1;
		queue.reprioritize(item1.getId(), newPosition);

		assertEquals(newPosition, queue.getQueuedItems().indexOf(item1));
		assertEquals(0,queue.getQueuedItems().indexOf(item2));
		assertEquals(1,queue.getQueuedItems().indexOf(item1));
		assertEquals(2,queue.getQueuedItems().indexOf(item3));
		
		verify(item1, item2, item3);
	}
	
	@Test
	public void movedItemIsRemovedFromPreviousPosition(){
		int runningQueueSize = 0;
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).anyTimes();
		expect(item1.getId()).andReturn(1L).anyTimes();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.getId()).andReturn(2L).anyTimes();
		PriorityJobQueueItem item3 = createMock(PriorityJobQueueItem.class);
		expect(item3.isFinished()).andReturn(false).anyTimes();
		expect(item3.getId()).andReturn(3L).anyTimes();
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2, item3);

		queue.add(item1);
		queue.add(item2);
		queue.add(item3);
		
		int newPosition = 1;
		queue.reprioritize(item1.getId(), newPosition);

		assertEquals(3, queue.getQueuedItems().size());
		verify(item1, item2, item3);
	}
	
	@Test
	public void movingItemNotInQueueThrowsException(){
		int runningQueueSize = 0;
		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).anyTimes();
		expect(item1.getId()).andReturn(1L).anyTimes();
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).anyTimes();
		expect(item2.getId()).andReturn(2L).anyTimes();
		PriorityJobQueueItem item3 = createMock(PriorityJobQueueItem.class);
		expect(item3.isFinished()).andReturn(false).anyTimes();
		expect(item3.getId()).andReturn(3L).anyTimes();
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2, item3);

		queue.add(item1);
		queue.add(item2);
		
		try{
			int newPosition = 1;
			queue.reprioritize(item3.getId(), newPosition);
			fail("Should have thrown exception");
		}catch(Exception e){
			assertTrue(e instanceof RuntimeException);
			assertEquals("Cannot reprioritize items not in queue", e.getMessage());
		}

	}
	
	@Test
	public void increasingLimitPopsAllNeeded(){
		int runningQueueSize = 1;

		KnossosJob job = createMock(KnossosJob.class);
		job.launch();
		expectLastCall().times(3);

		PriorityJobQueueItem item1 = createMock(PriorityJobQueueItem.class);
		expect(item1.isFinished()).andReturn(false).times(4);
		expect(item1.isFinished()).andReturn(true).anyTimes();
		expect(item1.popNextJob()).andReturn(job).times(2);
		
		PriorityJobQueueItem item2 = createMock(PriorityJobQueueItem.class);
		expect(item2.isFinished()).andReturn(false).times(2);
		expect(item2.isFinished()).andReturn(true).anyTimes();
		expect(item2.popNextJob()).andReturn(job).times(1);
		
		PriorityJobQueueItem item3 = createMock(PriorityJobQueueItem.class);
		expect(item3.isFinished()).andReturn(false).times(1);
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		replay(item1, item2, item3, job);

		queue.add(item1);
		queue.add(item2);
		queue.add(item3);
		
		queue.resize(3);
		
		verify(item1, item2, item3, job);

	}
	
	@Test
	public void decreasingLimitKeepsQueueIntact(){
		int runningQueueSize = 3;

		KnossosJob job = createMock(KnossosJob.class);
		job.launch();
		expectLastCall().times(3);

		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();
		expect(item.popNextJob()).andReturn(job).times(3);
		
		replay(job, item);
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		queue.add(item);
		int initialSize = queue.runningJobs.size();
		queue.resize(1);
		int finalSize = queue.runningJobs.size();

		verify(job, item);
		
		assertEquals(initialSize, finalSize);
	}

	@Test
	public void afterDecreasingLimitNoJobsArePoppedWhenAJobFinishes(){
		int runningQueueSize = 3;

		KnossosJob job = createMock(KnossosJob.class);
		job.launch();
		expectLastCall().times(3);

		PriorityJobQueueItem item = createMock(PriorityJobQueueItem.class);
		expect(item.isFinished()).andReturn(false).anyTimes();
		expect(item.popNextJob()).andReturn(job).times(3);
		
		replay(job, item);
		
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), runningQueueSize, 10);
		queue.add(item);
		queue.resize(1);
		queue.finishedJob(job);

		verify(job, item);
	}
	
	@Test(expected = RuntimeException.class)
	public void throwsExceptionIfPoolSizeIsLargerThanMaxUponCreation(){
		new PriorityJobQueueImpl(new FactoryImpl(), 5000, 10);
	}
	
	@Test(expected = RuntimeException.class)
	public void throwsExceptionIfPoolSizeIsLargerThanMaxUponResize(){
		PriorityJobQueueImpl queue = new PriorityJobQueueImpl(new FactoryImpl(), 5, 10);
		queue.resize(11);
	}
	
}
