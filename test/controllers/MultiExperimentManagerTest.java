package controllers;

import java.util.ArrayList;
import java.util.List;

import models.ComparisonExperiment;
import models.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class MultiExperimentManagerTest extends UnitTest {

	String username;
	TestDataCreator dataCreator;
	ExperimentManager experimentManager;
	
	@Before
	public void setup() {
		this.username = "aperreau";
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		this.dataCreator = new TestDataCreator();
		this.experimentManager = new ExperimentManager();
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void emptyExperimentListReturnsEmptyCommonProperties() {
		List<String> commonDatabaseProperties = MultiExperimentManager.getCommonDatabasePropertiesInternal(new ArrayList<Long>());
		
		assertEquals(0, commonDatabaseProperties.size());
	}

	@Test
	public void experimentsWithNoPropertiesReturnEmtpyCommonProperties() {
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = this.dataCreator.getSmallExperiment(owner);
		
		List<Long> experimentIds = new ArrayList<Long>();
		experimentIds.add(experiment.id);
		
		List<String> commonDatabaseProperties = MultiExperimentManager.getCommonDatabasePropertiesInternal(experimentIds);
		
		assertEquals(0, commonDatabaseProperties.size());
	}

	@Test
	public void experimentsWithCommonAndDistinctPropertiesReturnOnlyCommonProperties() {

		User owner = User.findByUserName("aperreau");
		List<String> commonPropertyNames = new ArrayList<String>();
		commonPropertyNames.add("C1");
		commonPropertyNames.add("C2");
		List<String> propertyNamesType1 = new ArrayList<String>();
		propertyNamesType1.add("T1.0");
		propertyNamesType1.add("T1.1");
		propertyNamesType1.addAll(commonPropertyNames);
		List<String> propertyNamesType2 = new ArrayList<String>();
		propertyNamesType2.add("T2.0");
		propertyNamesType2.add("T2.1");
		propertyNamesType2.addAll(commonPropertyNames);

		ComparisonExperiment experimentCommonAndT1 = this.dataCreator.getSmallExperimentWithGivenProperties(owner, propertyNamesType1);
		ComparisonExperiment experimentCommonAndT2 = this.dataCreator.getSmallExperimentWithGivenProperties(owner, propertyNamesType2);

		List<Long> experimentIds = new ArrayList<Long>();
		experimentIds.add(experimentCommonAndT1.id);
		experimentIds.add(experimentCommonAndT2.id);

		List<String> commonDatabaseProperties = MultiExperimentManager.getCommonDatabasePropertiesInternal(experimentIds);

		assertEquals(2, commonDatabaseProperties.size());
		assertTrue(commonDatabaseProperties.contains("C1"));
		assertTrue(commonDatabaseProperties.contains("C1"));
		assertFalse(commonDatabaseProperties.contains("T1.0"));
		assertFalse(commonDatabaseProperties.contains("T1.1"));
		assertFalse(commonDatabaseProperties.contains("T2.0"));
		assertFalse(commonDatabaseProperties.contains("T2.1"));
	}

	@Test
	public void aExperimentWithoutPropertiesCausesReturnEmptyCommonProperties() {
		User owner = User.findByUserName("aperreau");
		List<String> commonPropertyNames = new ArrayList<String>();
		commonPropertyNames.add("C1");
		commonPropertyNames.add("C2");
		List<String> propertyNamesType1 = new ArrayList<String>();
		propertyNamesType1.add("T1.0");
		propertyNamesType1.add("T1.1");
		propertyNamesType1.addAll(commonPropertyNames);
		List<String> propertyNamesType2 = new ArrayList<String>();
		propertyNamesType2.add("T2.0");
		propertyNamesType2.add("T2.1");
		propertyNamesType2.addAll(commonPropertyNames);

		ComparisonExperiment experimentCommonAndT1 = this.dataCreator.getSmallExperimentWithGivenProperties(owner, propertyNamesType1);
		ComparisonExperiment experimentCommonAndT2 = this.dataCreator.getSmallExperimentWithGivenProperties(owner, propertyNamesType2);
		ComparisonExperiment experimentWithoutProperties = this.dataCreator.getSmallExperiment(owner);
		
		List<Long> experimentIds = new ArrayList<Long>();
		experimentIds.add(experimentCommonAndT1.id);
		experimentIds.add(experimentCommonAndT2.id);
		experimentIds.add(experimentWithoutProperties.id);

		List<String> commonDatabaseProperties = MultiExperimentManager.getCommonDatabasePropertiesInternal(experimentIds);

		assertEquals(0, commonDatabaseProperties.size());
	}
	
}
