package visitors;

import java.util.Map;

import models.Alignment;
import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.Molecule;
import models.MoleculeDatabase;
import models.MoleculePair;
import models.PhysicalSimilarities;
import models.Ponderation;
import models.Score;
import models.Scoring;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.db.jpa.JPA;
import play.test.Fixtures;
import utils.experiment.TestDataCreator;
import files.FileDataExtractor;

public class BestAlignmentsJsonGeneratorTest extends FailingMethodsVisitorTest {

	BestAlignmentsJsonGenerator generator;
	
	@Before
	public void setup(){
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		generator = new BestAlignmentsJsonGenerator(null, null);
	}
	
	@Override
	public KnossosVisitor getVisitor() {
		return new BestAlignmentsJsonGenerator(null, null);
	}

	@Override
	public int[] getFailingTypes() {
		int[] failingTypes =  {MOLECULE_DATABASE, DEPLOYMENT, QSAR_EXPERIMENT, QSAR_RESULT};
		return failingTypes;
	}
	
	@Test
	public void emptyExperimentConvertedCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		generator.visit(experiment);
		assertEquals("[]", generator.getJson());
	}
	
	@Test
	public void scoringParsedCorrectly() {
		Scoring scoring = createScoring(1, 2, "inverseAD", 12.1122);
		generator.initScoringData();
		generator.visit(scoring);
		MoleculePair pair = scoring.getMoleculePair();
		Map<String, Score> scores = generator.retrieveScores(pair);
		assertNotNull(scores);
		assertEquals(new Double(12.1122), new Double(scores.get("inverseAD").score));
	}

	@Test
	public void scoringsWithDifferentPonderationAreAllStored(){
		Scoring scoring = createScoring(1, 2, "inverseAD", 12.1122);
		Scoring scoring2 = createScoring(1, 2, "inverseAD2", 1.12);
		generator.initScoringData();
		generator.visit(scoring);
		generator.visit(scoring2);
		MoleculePair pair = scoring.getMoleculePair();
		Double actualScore = new Double(generator.scoringData.get(pair).get("inverseAD").score);
		Double actualScore2 = new Double(generator.scoringData.get(pair).get("inverseAD2").score);
		assertEquals(new Double(12.1122), actualScore);
		assertEquals(new Double(1.12), actualScore2);
	}
	
	@Test 
	public void bestScoreIsStoredForScoringsWithSamePonderation(){
		Scoring scoring = createScoring(1, 2, "inverseAD", 12.1122);
		Scoring scoring2 = createScoring(1, 2, "inverseAD", 1.12);
		Scoring scoring3 = createScoring(1, 2, "inverseAD", 12.12);
		generator.initScoringData();
		generator.visit(scoring);
		generator.visit(scoring2);
		generator.visit(scoring3);
		MoleculePair pair = scoring.getMoleculePair();
		assertEquals(new Double(12.12), new Double(generator.scoringData.get(pair).get("inverseAD").score));
	}

	@Test 
	public void bestScoreIsStoredForPhysicalSimilarities(){
		PhysicalSimilarities similarity = createPhysicalSimilarities(1, 2, -12.12, 10,20);
		PhysicalSimilarities similarity2 = createPhysicalSimilarities(1, 2,  -1.12,10,20);
		PhysicalSimilarities similarity3 = createPhysicalSimilarities(1, 2, -12.12, 30,20);
		generator.initScoringData();
		generator.visit(similarity);
		generator.visit(similarity2);
		generator.visit(similarity3);
		MoleculePair pair = similarity.getMoleculePair();
		assertEquals(new Double(-12.12), new Double(generator.scoringData.get(pair).get(FileDataExtractor.ENERGY).score));
		assertEquals(new Double(10), new Double(generator.scoringData.get(pair).get(FileDataExtractor.ENTROPY).score));
	}
	
	@Test
	public void experimentWithSingleScoring(){
		JPA.setRollbackOnly();
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = creator.getNxMEvaluatedExperiment(owner);
		for(Alignment alignment : experiment.alignments){
			alignment.id = 0L;
		}
		generator.visit(experiment);
		String json = "[{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000005\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000004\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000019\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000007\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000008\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000005\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000008\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000006\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000007\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000019\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC02\",\"deploymentOrder\":\"2\"},\"probe\":{\"name\":\"MCMC00000006\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01\",\"deploymentOrder\":\"1\"},\"probe\":{\"name\":\"MCMC00000004\"},\"scores\":{\"inverseAD\":{\"value\":\"17.5722\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}]";
		Logger.info("Json: " + generator.getJson());
		assertEquals(json,generator.getJson());
	}
	
	@Test
	public void dockingExperimentIsCorrectlyConverted(){
		JPA.setRollbackOnly();
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = creator.getSmallEvaluatedDockingExperiment(owner);
		for(Alignment alignment : experiment.alignments){
			alignment.id = 0L;
		}
		generator.visit(experiment);
		String json = "[{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000004\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000005\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}" +
				",{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000006\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000007\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000008\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}," +
				"{\"target\":{\"name\":\"ZINC01535869\"},\"probe\":{\"name\":\"MCMC00000019\"},\"scores\":{\"energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"vinaEnergy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energyBigC (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"entropy\":{\"value\":\"0.4560\",\"alignmentId\":\"0\"},\"vinaEnergy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy (1-S)\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"Mean energy\":{\"value\":\"NaN\",\"alignmentId\":\"0\"},\"energy\":{\"value\":\"0.1230\",\"alignmentId\":\"0\"},\"Mean energyBigC\":{\"value\":\"NaN\",\"alignmentId\":\"0\"}},\"pharmacophoreScores\":{}}]";
		assertEquals(json,generator.getJson());
	}
	
	@Test
	public void visitingPhysicalSimilarityStoresEnergyAndEntropy() {
		PhysicalSimilarities scoring = createPhysicalSimilarities(1,2,12.12,0.12,1.22);
		generator.initScoringData();
		generator.visit(scoring);
		MoleculePair pair = scoring.getMoleculePair();
		Double energyScore = generator.scoringData.get(pair).get("energy").score;
		Double entropyScore = generator.scoringData.get(pair).get("entropy").score;
		assertEquals(new Double(12.12), energyScore);
		assertEquals(new Double(0.12), entropyScore);
	}

	private Scoring createScoring(long targetId, long probeId, String ponderationName, double score) {
		Ponderation ponderation = new Ponderation();
		ponderation.name = ponderationName;
		MoleculeDatabase database = new MoleculeDatabase();
		Molecule targetM = new Molecule();
		targetM.database = database;
		targetM.id = targetId;
		Deployment targetD = new Deployment();
		targetD.molecule = targetM;
		targetM.addDeployment(targetD);
		Molecule probeM = new Molecule();
		probeM.database = database;
		probeM.id = probeId;
		Deployment probeD = new Deployment();
		probeD.molecule = probeM;
		probeM.addDeployment(probeD);
		Alignment maps = new MapsSimilarities();
		maps.targetDeployment = targetD;
		maps.probeDeployment = probeD;
		Scoring scoring = new Scoring();
		scoring.ponderation = ponderation;
		scoring.score = score;
		scoring.maps = maps;
		return scoring;
	}
	
	private PhysicalSimilarities createPhysicalSimilarities(long targetId, long probeId, double energy, double entropy,
			double vinaEnergy) {
		PhysicalSimilarities scoring = new PhysicalSimilarities();
		scoring.energy = energy;
		scoring.entropy = entropy;
		scoring.vinaEnergy = vinaEnergy;

		MoleculeDatabase database = new MoleculeDatabase();
		Molecule targetM = new Molecule();
		targetM.database = database;
		targetM.id = targetId;
		Deployment targetD = new Deployment();
		targetM.addDeployment(targetD);
		targetD.molecule = targetM;
		Molecule probeM = new Molecule();
		probeM.database = database;
		probeM.id = probeId;
		Deployment probeD = new Deployment();
		probeM.addDeployment(probeD);

		scoring.targetDeployment = targetD;
		scoring.probeDeployment = probeD;
		return scoring;
	}
	
	@Test
	//This should not happen. Similarities with error shouldn't have scorings
	public void allMapsSimilaritiesWithErrorAndScoringsReturnsEmptyScores(){
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = creator.getNxMEvaluatedExperiment(owner);
		for(Alignment alignment : experiment.alignments){
			alignment.error = true;
		}

		generator.visit(experiment);
		String json = "[]";
		assertEquals(json,generator.getJson());
	}
	
	@Test
	public void allMapsSimilaritiesWithErrorAndNoScoringsReturnsEmpty(){
		TestDataCreator creator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = creator.getNxMEvaluatedExperiment(owner);
		for(Alignment alignment : experiment.alignments){
			alignment.error = true;
			((MapsSimilarities)alignment).scorings.clear();
		}

		generator.visit(experiment);
		String json = "[]";
		assertEquals(json,generator.getJson());
	}
	
	
}
