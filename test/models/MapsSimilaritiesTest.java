package models;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class MapsSimilaritiesTest extends UnitTest {

	MapsSimilarities similarities1, similarities2;
	Ponderation ponderation1, ponderation2, defaultPonderation;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		TestDataCreator dataCreator = new TestDataCreator();
		User owner = User.findByUserName("aperreau");
		ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		
		defaultPonderation = Ponderation.getDefaultPonderations().get(0);

		ponderation1 = new Ponderation();
		ponderation1.name = "ponderation1";
		ponderation1.weights.A = 1;
		ponderation1.owner = User.findByUserName("aperreau");
		ponderation1.save();

		ponderation2 = new Ponderation();
		ponderation2.name = "ponderation2";
		ponderation2.weights.A = 2;
		ponderation2.owner = User.findByUserName("aperreau");
		ponderation2.save();
		
		experiment.rescore(ponderation1);
		experiment.rescore(ponderation2);
		
		similarities1 = (MapsSimilarities)experiment.alignments.get(0);
		Scoring scoring11 = similarities1.scorings.get(0);
		scoring11.score = scoring11.ponderation.id;
		Scoring scoring12 = similarities1.scorings.get(1);
		scoring12.score = scoring12.ponderation.id;
		Scoring scoring13 = similarities1.scorings.get(2);
		scoring13.score = scoring13.ponderation.id;

		similarities2 = (MapsSimilarities)experiment.alignments.get(1);
		Scoring scoring21 = similarities2.scorings.get(0);
		scoring21.score = scoring21.ponderation.id*2;
		Scoring scoring22 = similarities2.scorings.get(1);
		scoring22.score = scoring22.ponderation.id*2;
		Scoring scoring23 = similarities2.scorings.get(2);
		scoring23.score = scoring23.ponderation.id*2;

		experiment.save();
	}

	@Test
	public void defaultPonderation() {
		MapsSimilarities maps = getAllOnesMapSimilarities();
		maps.applyDefaultPonderations();
		assertEquals(1, maps.scorings.size());
	}

	public static MapsSimilarities getAllOnesMapSimilarities() {
		MapsSimilarities maps = new MapsSimilarities();
		Set<String> mapNames = PhysicalMaps.getMapNames();
		for(String mapName : mapNames){
			maps.similarities.setMapValue(mapName, 1.0);
		}
		return maps;
	}

	@Test
	public void uniformScoring(){
		List<Ponderation> ponderations = Ponderation.getDefaultPonderations();
		assertEquals(1, ponderations.size());
		Ponderation ponderation = ponderations.get(0);
		MapsSimilarities maps = getAllOnesMapSimilarities();
		maps.ponderate(ponderation);
		assertEquals(1, maps.scorings.size());
		assertEquals(1.0, maps.scorings.get(0).score, 0.0001);
	}

	@Test
	public void getsCorrectMoleculePair() {
		MapsSimilarities similarities = new MapsSimilarities();
		similarities.targetDeployment = new Deployment();
		Molecule probe = new Molecule();
		similarities.targetDeployment.molecule = probe;
		similarities.probeDeployment = new Deployment();
		Molecule target = new Molecule();
		similarities.probeDeployment.molecule = target;
		MoleculePair pair = new MoleculePair(target,probe);
		assertEquals(pair, similarities.getMoleculePair());
	}
	
	@Test
	public void getsCorrectScore(){	
		assertEquals(3, similarities1.scorings.size());
	
		assertEquals(similarities1.getScore(defaultPonderation), defaultPonderation.id, 0.01);
		assertEquals(similarities1.getScore(ponderation1), ponderation1.id, 0.01);
		assertEquals(similarities1.getScore(ponderation2), ponderation2.id, 0.01);
	}

	@Test
	public void comparisionToItselfIs0(){
		assertEquals(0,similarities1.compare(similarities1, ponderation1),0.001);
		assertEquals(0,similarities1.compare(similarities1, ponderation2),0.001);
		assertEquals(0,similarities1.compare(similarities1, defaultPonderation),0.001);
	}
	
	@Test
	public void comparissonIsCorrect(){
		assertEquals(-ponderation1.id,similarities1.compare(similarities2, ponderation1),0.001);
		assertEquals(-ponderation2.id,similarities1.compare(similarities2, ponderation2),0.001);
		assertEquals(-defaultPonderation.id,similarities1.compare(similarities2, defaultPonderation),0.001);
	}
}
