package models;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class PharmacophoreScoringTest extends UnitTest{

	MapsSimilarities similarities1;
	MapsSimilarities similarities2;
	Pharmacophore pharmacophore1;
	Pharmacophore pharmacophore2;
	ComparisonExperiment experiment;
	User owner;
	
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		
		TestDataCreator dataCreator = new TestDataCreator();
		owner = User.findByUserName("aperreau");
		experiment = dataCreator.getSmallEvaluatedExperiment(owner);
		MoleculeDatabase database = MoleculeDatabase.findAllOwnedBy(owner).get(0);
		pharmacophore1 = new PharmacophoreKnossos(database, 1.0);
		pharmacophore2 = new PharmacophoreKnossos(database, 2.0);
		pharmacophore1.save();
		pharmacophore2.save();
					
		similarities1 = (MapsSimilarities)experiment.alignments.get(0);
		similarities1.pharmacophoreScorings = new ArrayList<PharmacophoreScoring>();
		PharmacophoreScoring scoring11 = new PharmacophoreScoring(similarities1, pharmacophore2);
		scoring11.setScore(0.3);
		similarities1.pharmacophoreScorings.add(scoring11);
		PharmacophoreScoring scoring12 = new PharmacophoreScoring(similarities1, pharmacophore2);
		scoring12.setScore(0.65);
		similarities1.pharmacophoreScorings.add(scoring12);
		PharmacophoreScoring scoring111 = new PharmacophoreScoring(similarities1, pharmacophore1);
		scoring111.setScore(0.2);
		similarities1.pharmacophoreScorings.add(scoring111);
		PharmacophoreScoring scoring121 = new PharmacophoreScoring(similarities1, pharmacophore1);
		scoring121.setScore(0.55);
		similarities1.pharmacophoreScorings.add(scoring121);

		similarities2 = (MapsSimilarities)experiment.alignments.get(1);
		similarities2.pharmacophoreScorings = new ArrayList<PharmacophoreScoring>();
		PharmacophoreScoring scoring21 = new PharmacophoreScoring(similarities2, pharmacophore2);
		scoring21.setScore(0.1);
		similarities2.pharmacophoreScorings.add(scoring21);
		PharmacophoreScoring scoring22 = new PharmacophoreScoring(similarities2, pharmacophore2);
		scoring22.setScore(0.78);
		similarities2.pharmacophoreScorings.add(scoring22);
		PharmacophoreScoring scoring23 = new PharmacophoreScoring(similarities2, pharmacophore2);
		scoring23.setScore(0.16);
		similarities2.pharmacophoreScorings.add(scoring23);

		PharmacophoreScoring scoring211 = new PharmacophoreScoring(similarities2, pharmacophore1);
		scoring211.setScore(0.05);
		similarities2.pharmacophoreScorings.add(scoring211);
		PharmacophoreScoring scoring221 = new PharmacophoreScoring(similarities2, pharmacophore1);
		scoring221.setScore(0.73);
		similarities2.pharmacophoreScorings.add(scoring221);
		PharmacophoreScoring scoring231 = new PharmacophoreScoring(similarities2, pharmacophore1);
		scoring231.setScore(0.1116);
		similarities2.pharmacophoreScorings.add(scoring231);

		experiment.save();
	}
	
	@Test
	public void findScoresByAlignment(){
		List<PharmacophoreScoring> scorings1 = PharmacophoreScoring.findByAlignment(similarities1);
		List<PharmacophoreScoring> scorings2 = PharmacophoreScoring.findByAlignment(similarities2);
		
		assertEquals(4,scorings1.size());		
		
		assertEquals(similarities1,scorings1.get(0).maps);
		assertEquals(pharmacophore2,scorings1.get(0).pharmacophore);
//		assertEquals(2,scorings1.get(0).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.3,scorings1.get(0).score,0.001);
		
		assertEquals(similarities1,scorings1.get(1).maps);
		assertEquals(pharmacophore2,scorings1.get(1).pharmacophore);
//		assertEquals(2,scorings1.get(1).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.65,scorings1.get(1).score,0.001);
		
		assertEquals(similarities1,scorings1.get(2).maps);
		assertEquals(pharmacophore1,scorings1.get(2).pharmacophore);
//		assertEquals(1,scorings1.get(2).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.2,scorings1.get(2).score,0.001);

		assertEquals(similarities1,scorings1.get(3).maps);
		assertEquals(pharmacophore1,scorings1.get(3).pharmacophore);
//		assertEquals(1,scorings1.get(3).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.55,scorings1.get(3).score,0.001);
		
		
		assertEquals(6,scorings2.size());		
		
		assertEquals(similarities2,scorings2.get(0).maps);
		assertEquals(pharmacophore2,scorings2.get(0).pharmacophore);
//		assertEquals(2,scorings2.get(0).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.1,scorings2.get(0).score,0.001);
		
		assertEquals(similarities2,scorings2.get(1).maps);
		assertEquals(pharmacophore2,scorings2.get(1).pharmacophore);
//		assertEquals(2,scorings2.get(1).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.78,scorings2.get(1).score,0.001);
		
		assertEquals(similarities2,scorings2.get(2).maps);
		assertEquals(pharmacophore2,scorings2.get(2).pharmacophore);
//		assertEquals(2,scorings2.get(2).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.16,scorings2.get(2).score,0.001);

		assertEquals(similarities2,scorings2.get(3).maps);
		assertEquals(pharmacophore1,scorings2.get(3).pharmacophore);
//		assertEquals(1,scorings2.get(3).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.05,scorings2.get(3).score,0.001);
		
		assertEquals(similarities2,scorings2.get(4).maps);
		assertEquals(pharmacophore1,scorings2.get(4).pharmacophore);
//		assertEquals(1,scorings2.get(4).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.73,scorings2.get(4).score,0.001);
		
		assertEquals(similarities2,scorings2.get(5).maps);
		assertEquals(pharmacophore1,scorings2.get(5).pharmacophore);
//		assertEquals(1,scorings2.get(5).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.1116,scorings2.get(5).score,0.001);

	}
	
	@Test
	public void findScoreByAlignmentPharmacophoreAndThreshold(){
		List<PharmacophoreScoring> scorings = PharmacophoreScoring.findByAlignmentAndPharmacophore(similarities1,pharmacophore2);
		
		assertEquals(2,scorings.size());
		
		assertEquals(similarities1,scorings.get(0).maps);
		assertEquals(pharmacophore2,scorings.get(0).pharmacophore);
//		assertEquals(2,scorings.get(0).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.3,scorings.get(0).score,0.001);
		
		assertEquals(similarities1,scorings.get(1).maps);
		assertEquals(pharmacophore2,scorings.get(1).pharmacophore);
//		assertEquals(2,scorings.get(1).pharmacophore.similarityThreshold,0.001);
		assertEquals(0.65,scorings.get(1).score,0.001);

		
	}

}
