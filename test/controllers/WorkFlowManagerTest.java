package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ComparisonExperiment;
import models.ExperimentStatus;
import models.ModelForListing;
import models.MoleculeDatabase;
import models.Workflow;
import models.WorkflowExperiment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.Factory;
import utils.experiment.TestDataCreator;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class WorkFlowManagerTest extends FunctionalTest {

	String username;
	TestDataCreator dataCreator;
	Map<String, String> addStepParams;

	@Before
	public void setup() {
		username = "aperreau";
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		dataCreator = new TestDataCreator();
		
		addStepParams = new HashMap<>();
		List<MoleculeDatabase> databases = ModelReadingUtils.getMoleculeDatabasesOwnedBy(username);
		List<ModelForListing> engines = ModelReadingUtils.getListOfEngines();
		addStepParams = new HashMap<String, String>();
		addStepParams.put("username", username);
		addStepParams.put("experimentName", "Tester");
		addStepParams.put("chargeType", Factory.GASTEIGER + "");
		addStepParams.put("targetDatabaseId", "" + databases.get(0).id);
		addStepParams.put("probeDatabaseId", "" + databases.get(0).id);
		addStepParams.put("engineId", "" + engines.get(0).id);
		addStepParams.put("filterType", "percentage");
		addStepParams.put("filterThreshold", String.valueOf(1));
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}
	
	@Test
	public void addStepWithIdAddsToWorkflow() {
		Workflow wf = Workflow.findByName("test");
		
		Map<String, String> params = new HashMap<>(addStepParams);
		params.put("workflowId", wf.id.toString());
		params.put("workflowName", wf.name);
		
		Response response = POST("/workflow/step", params);
		assertIsOk(response);
		assertContentType("application/json", response);		
		List<WorkflowExperiment> wes = getWorkflowExperiments(wf);
		
		JsonElement root = new JsonParser().parse(getContent(response));
		String id = root.getAsJsonObject().get("workflow").getAsJsonObject().get("id").getAsString();
		
		
		assertEquals(wes.get(wes.size()-1).experiment.name, params.get("experimentName"));		
		assertEquals(id, String.valueOf(wf.id));
	}
	
	@Test
    public void addStepWithNullIdCreatesWorkflow() {
		Map<String, String> params = new HashMap<>(addStepParams);
		params.put("workflowId", "null");
		params.put("workflowName", "a_name");
		
		Response response = POST("/workflow/step", params);
		assertIsOk(response);
		assertContentType("application/json", response);
		
		JsonElement root = new JsonParser().parse(getContent(response));
		String id = root.getAsJsonObject().get("workflow").getAsJsonObject().get("id").getAsString();
		
		// Will throw an exception if it is not a number
		Integer.parseInt(id);
	}
		
	@Test
    public void addStepCreatesComparisonExperiment() {
		Workflow wf = Workflow.findByName("test");
		String experimentName = "a_comparison_experiment";
		
		Map<String, String> params = new HashMap<>(addStepParams);
		params.put("workflowId", wf.id.toString());
		params.put("workflowName", wf.name);
		params.put("experimentName", experimentName);
		
		POST("/workflow/step", params);
		
		List<ComparisonExperiment> ces = getComparisonExperiments(experimentName);
		assertTrue(ces.size() > 0);
	}
	
    @Test
    public void addStepSetsExperimentStatusToWaiting() {
    	Workflow wf = Workflow.findByName("test");
    	String experimentName = "a_comparison_experiment";
    	
    	Map<String, String> params = new HashMap<>(addStepParams);
    	params.put("workflowId", wf.id.toString());
    	params.put("workflowName", wf.name);
    	params.put("experimentName", experimentName);
    	
    	POST("/workflow/step", params);
    	
    	List<ComparisonExperiment> ces = getComparisonExperiments(experimentName);
    	assertEquals(ExperimentStatus.WAITING, ces.get(0).status);
	}	
    
	@Test
    public void runWorkflowSetsFirstExperimentStatusToQueued() throws InterruptedException {
		Workflow wf = Workflow.findByName("test");
    	addExperimentsToWorkflow(wf);
    	runWorkflow(wf);
    	ComparisonExperiment ce = wf.workflowExperiments.get(0).experiment;
    	assertEquals(ExperimentStatus.QUEUED, ce.status);
	}
	
	@Test
    public void runWorkflowLeavesNonFirstExperimentStatusAsWaiting() {
		Workflow wf = Workflow.findByName("test");
		addExperimentsToWorkflow(wf);
    	runWorkflow(wf);
    	
    	for(WorkflowExperiment we: getWorkflowExperiments(wf)) {
    		if (we.index == 0) {
    			continue;
    		}
    		
    		assertEquals(ExperimentStatus.WAITING, we.experiment.status);
    	}
	}
	
	private void addExperimentsToWorkflow(Workflow wf) {
		Map<String, String> addSteparams = new HashMap<>(addStepParams);
    	addSteparams.put("workflowId", wf.id.toString());
    	addSteparams.put("workflowName", wf.name);
    	
    	POST("/workflow/step", addSteparams);
    	POST("/workflow/step", addSteparams);
    	POST("/workflow/step", addSteparams);
	}
	
	private void runWorkflow(Workflow wf) {
		Map<String, String> runWorkflowParams = new HashMap<>();
    	runWorkflowParams.put("id", String.valueOf(wf.id));
    	runWorkflowParams.put("name", wf.name);
    	runWorkflowParams.put("username", username);
    	POST("/workflow", runWorkflowParams);
	}
	
	private List<ComparisonExperiment> getComparisonExperiments(String name) {
		return ComparisonExperiment.find("byName", name).fetch();
	}
	
	private List<WorkflowExperiment> getWorkflowExperiments(Workflow wf) {
		return WorkflowExperiment.find("byWorkflow_id", wf.id).fetch();
	}
	

}