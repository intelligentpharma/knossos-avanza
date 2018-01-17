package visitors;

import java.util.ArrayList;
import java.util.HashMap;

import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.MoleculeDatabase;
import models.PharmacophoreKnossos;
import models.PharmacophoreScoring;
import models.PhysicalSimilarities;
import models.Ponderation;
import models.Scoring;

import org.junit.Before;
import org.junit.Test;

public class AllAlignmentsJsonGeneratorTest extends FailingMethodsVisitorTest {

	AllAlignmentsJsonGenerator generator;
	
	@Before
	public void setup(){
		generator = new AllAlignmentsJsonGenerator();
	}
	
	@Override
	public KnossosVisitor getVisitor() {
		return new AllAlignmentsJsonGenerator();
	}

	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void emptyExperimentConvertedCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		generator.visit(experiment);
		assertEquals("[]", generator.getJson());
	}
	
	@Test
	public void fullExperimentConvertedCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.alignments.add(createSimilarity());
		experiment.alignments.add(createSimilarity());
		generator.visit(experiment);
		assertEquals("[{\"id\" : null, \"target\" : {\"name\":\"test\"}, \"probe\" : {\"name\":\"test\"}, \"scores\" : {\"inverseAD\":{\"value\":\"12.1122\",\"alignmentId\":\"null\"}}," +
				" \"pharmacophoreScores\" : {\"1\":{\"bestClusterValue\":\"13.1330\",\"bestClusterEnergy\":\"NaN\",\"value\":\"13.1330\",\"alignmentId\":\"null\",\"threshold\":\"0.0000\",\"pharmacophoreName\":\"testPharmacophore_2.0\"}}},{\"id\" : null, \"target\" : {\"name\":\"test\"}," +
				" \"probe\" : {\"name\":\"test\"}, \"scores\" : {\"inverseAD\":{\"value\":\"12.1122\",\"alignmentId\":\"null\"}}, \"pharmacophoreScores\" : {\"1\":{\"bestClusterValue\":\"13.1330\",\"bestClusterEnergy\":\"NaN\",\"value\":\"13.1330\",\"alignmentId\":\"null\",\"threshold\":\"0.0000\",\"pharmacophoreName\":\"testPharmacophore_2.0\"}}}]",generator.getJson());
	}

	@Test
	public void physicalSimilaritiesConvertedCorrectly(){
		PhysicalSimilarities similarity = createPhysicalSimilarity();
		generator.alignmentsJson = new ArrayList<String>();
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(similarity);
		assertEquals(1, generator.alignmentsJson.size());
		assertEquals("{\"target\" : {\"name\":\"test\"}, \"probe\" : {\"name\":\"test\"}, \"scores\" : "+
				"{\"energy\":{\"value\":\"12.0000\",\"alignmentId\":\"null\"}"+
				",\"entropy\":{\"value\":\"1.0000\",\"alignmentId\":\"null\"}"+
				",\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}"+
				",\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"null\"}}, \"pharmacophoreScores\" : {}}",
				generator.alignmentsJson.get(0));
	}
	
	private PhysicalSimilarities createPhysicalSimilarity() {
		PhysicalSimilarities similarity = new PhysicalSimilarities();
		Deployment deployment = new Deployment();
		deployment.name = "test";
		similarity.probeDeployment = deployment;
		similarity.targetDeployment = deployment;
		similarity.energy = 12.0;
		similarity.entropy = 1.0;
		similarity.pharmacophoreScorings = new ArrayList<PharmacophoreScoring>();
		return similarity;
	}

	@Test
	public void mapSimilaritiesConvertedCorrectly(){
		MapsSimilarities similarity = createSimilarity();
		generator.alignmentsJson = new ArrayList<String>();
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(similarity);
		assertEquals(1, generator.alignmentsJson.size());
		assertEquals("{\"id\" : null, \"target\" : {\"name\":\"test\"}, \"probe\" : {\"name\":\"test\"}, \"scores\" : {\"inverseAD\":{\"value\":\"12.1122\",\"alignmentId\":\"null\"}}, \"pharmacophoreScores\" : {\"1\":{\"bestClusterValue\":\"13.1330\",\"bestClusterEnergy\":\"NaN\",\"value\":\"13.1330\",\"alignmentId\":\"null\",\"threshold\":\"0.0000\",\"pharmacophoreName\":\"testPharmacophore_2.0\"}}}",
				generator.alignmentsJson.get(0));
	}

	private MapsSimilarities createSimilarity() {
		MapsSimilarities similarity = new MapsSimilarities();
		Deployment deployment = new Deployment();
		deployment.name = "test";
		similarity.probeDeployment = deployment;
		similarity.targetDeployment = deployment;
		Scoring scoring = createScoring("inverseAD", 12.1122);
		PharmacophoreScoring pharmacophoreScoring = createPharmacophoreScoring("testPharmacophore", 13.133, 2, "knossos");
		similarity.add(scoring);
		similarity.add(pharmacophoreScoring);
		return similarity;
	}

	private PharmacophoreScoring createPharmacophoreScoring(String databaseName, double score, double threshold, String engine) {
		MoleculeDatabase database = new MoleculeDatabase();
		database.name = databaseName;
		Alignment maps = new MapsSimilarities();
		PharmacophoreKnossos pharmacophore = new PharmacophoreKnossos(database, threshold);
		pharmacophore.id = 1L;
		PharmacophoreScoring scoring = new PharmacophoreScoring(maps, pharmacophore);
		scoring.score = score;
		scoring.bestClusterEnergy = Double.NaN;
		scoring.bestClusterValue = score;
		return scoring;
	}

	@Test
	public void DeploymentWithoutPropertiesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "test";
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(deployment);
		assertEquals("{\"name\":\"test\"}", generator.deploymentsJson.get(deployment));
	}
	
	@Test
	public void DeploymentWithPropertiesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "test";
		deployment.putProperty("property1", "value1");
		deployment.putProperty("property2", "value2");
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(deployment);
		assertEquals("{\"name\":\"test\",\"property1\":\"value1\",\"property2\":\"value2\"}", generator.deploymentsJson.get(deployment));
	}

	@Test
	public void scoringConvertedCorrectly(){
		Scoring scoring = createScoring("inverseAD", 12.1122);
		generator.scoringsJson = new ArrayList<String>();
		generator.visit(scoring);
		assertEquals(1, generator.scoringsJson.size());
		assertEquals("\"inverseAD\":{\"value\":\"12.1122\",\"alignmentId\":\"null\"}", generator.scoringsJson.get(0));
	}

	@Test
	public void multipleScoringsConvertedCorrectly(){
		Scoring scoring = createScoring("inverseAD", 12.1122);
		Scoring scoring2 = createScoring("inverseAD2", 1.12);
		generator.scoringsJson = new ArrayList<String>();
		generator.visit(scoring);
		generator.visit(scoring2);
		assertEquals(2, generator.scoringsJson.size());
		assertEquals("\"inverseAD\":{\"value\":\"12.1122\",\"alignmentId\":\"null\"}", generator.scoringsJson.get(0));
		assertEquals("\"inverseAD2\":{\"value\":\"1.1200\",\"alignmentId\":\"null\"}", generator.scoringsJson.get(1));
	}

	private Scoring createScoring(String ponderationName, double score) {
		Ponderation ponderation = new Ponderation();
		ponderation.name = ponderationName;
		Alignment maps = new MapsSimilarities();
		Scoring scoring = new Scoring();
		scoring.ponderation = ponderation;
		scoring.score = score;
		scoring.maps = maps;
		return scoring;
	}

	
	@Test
	public void propertyWithQuotesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "deployMent";
		deployment.putProperty("prop1", "value1");
		deployment.putProperty("prop2", "something \"value2\" something else");
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(deployment);
		assertEquals("{\"name\":\"deployMent\",\"prop1\":\"value1\",\"prop2\":\"something \\\"value2\\\" something else\"}", 
				generator.deploymentsJson.get(deployment));
	}

	@Test
	public void propertyWithNewlinesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "deployMent";
		deployment.putProperty("prop1", "value1\nvalue11");
		deployment.putProperty("prop2", "value2");
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(deployment);
		assertEquals("{\"name\":\"deployMent\",\"prop1\":\"value1\\nvalue11\",\"prop2\":\"value2\"}", 
				generator.deploymentsJson.get(deployment));
	}
	
	@Test
	public void errorAlignmentsAreShownInAllPonderations(){
		MapsSimilarities similarity = createSimilarity();
		similarity.error = true;
		generator.alignmentsJson = new ArrayList<String>();
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(similarity);
		assertEquals(1, generator.alignmentsJson.size());
		assertEquals("{\"id\" : null, \"target\" : {\"name\":\"test\"}, \"probe\" : {\"name\":\"test\"}, \"scores\" : {}, \"pharmacophoreScores\" : {}}",
				generator.alignmentsJson.get(0));
	}

	@Test
	public void errorAlignmentsAreShownInAllPhysicalData(){
		PhysicalSimilarities similarity = createPhysicalSimilarity();
		similarity.error = true;
		generator.alignmentsJson = new ArrayList<String>();
		generator.deploymentsJson = new HashMap<Deployment, String>();
		generator.visit(similarity);
		assertEquals(1, generator.alignmentsJson.size());
		assertEquals("{\"target\" : {\"name\":\"test\"}, \"probe\" : {\"name\":\"test\"}, \"scores\" : {}, \"pharmacophoreScores\" : {}}",
				generator.alignmentsJson.get(0));
	}

}
