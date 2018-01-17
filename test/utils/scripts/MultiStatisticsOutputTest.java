package utils.scripts;
import java.util.Set;

import org.junit.Test;

import play.test.UnitTest;
import utils.scripts.MultiStatisticsOutput;


public class MultiStatisticsOutputTest extends UnitTest {

	@Test
	public void singleLineContainsOneExperiment(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133");
		Set<String> experimentNames = output.getExperimentNames();
		assertEquals(1, experimentNames.size());
		assertTrue(experimentNames.contains("exp1"));
	}
	
	@Test
	public void singleLineContainsStatistics(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133");
		Set<String> statisticsValues = output.getStatisticNames();
		assertEquals(3, statisticsValues.size());
		assertTrue(statisticsValues.contains("rer"));
		assertTrue(statisticsValues.contains("numClus"));
		assertTrue(statisticsValues.contains("wauac"));
	}
	
	@Test
	public void singleLineContainsValues(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133");
		assertEquals("12.122",output.getValue("exp1","rer"));
		assertEquals("32",output.getValue("exp1","numClus"));
		assertEquals("22.133",output.getValue("exp1","wauac"));
	}
	
	@Test
	public void multiLineContainsManyExperiments(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133\n" +
				"exp2,rer:12.122,numClus:32,wauac:22.133\n" +
				"exp3,rer:12.122,numClus:32,wauac:22.133\n");
		Set<String> experimentNames = output.getExperimentNames();
		assertEquals(3, experimentNames.size());
		assertTrue(experimentNames.contains("exp1"));
		assertTrue(experimentNames.contains("exp2"));
		assertTrue(experimentNames.contains("exp3"));
	}

	@Test
	public void multiLineContainsStatistics(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133\n" +
				"exp2,rer:12.122,numClus:32,wauac:22.133\n" +
				"exp3,rer:12.122,numClus:32,wauac:22.133\n");
		Set<String> statisticsValues = output.getStatisticNames();
		assertEquals(3, statisticsValues.size());
		assertTrue(statisticsValues.contains("rer"));
		assertTrue(statisticsValues.contains("numClus"));
		assertTrue(statisticsValues.contains("wauac"));
	}
	
	@Test
	public void multiLineContainsValues(){
		MultiStatisticsOutput output = new MultiStatisticsOutput();
		output.parse("exp1,rer:12.122,numClus:32,wauac:22.133\n" +
				"exp2,rer:13.122,numClus:33,wauac:23.133\n" +
				"exp3,rer:14.122,numClus:34,wauac:24.133\n");
		assertEquals("12.122",output.getValue("exp1","rer"));
		assertEquals("33",output.getValue("exp2","numClus"));
		assertEquals("24.133",output.getValue("exp3","wauac"));
	}
	
}
