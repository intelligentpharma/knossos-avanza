package utils.pharmacophore;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import models.Alignment;
import models.ComparisonExperiment;
import models.MapsSimilarities;
import models.MoleculeDatabase;
import models.Pharmacophore;
import models.PharmacophoreKnossos;
import models.PharmacophoreScoring;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;

public class PharmacophoreEngineTest extends UnitTest{

	ComparisonExperiment experiment;
	PharmacophoreKnossos pharmacophore;
	DatabaseFiles dbFiles;
	File pharmacophorePdbqt;
	List<Atom> pharmacophoreAtoms;
	PharmacophoreKnossosEngine engine;
	
	@Before
	public void setup(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
		User user = User.findByUserName("xmaresma");
		TestDataCreator creator = new TestDataCreator();
		MoleculeDatabase database = creator.createSingleMoleculeDatabase(user);
		pharmacophore = new PharmacophoreKnossos(database, 2.0);
		experiment = creator.getSmallEvaluatedExperiment(user);
		dbFiles = createNiceMock(DatabaseFiles.class);
		engine = new PharmacophoreKnossosEngine(dbFiles);
		
		pharmacophorePdbqt = new File("test-files/pharmacophore/5EH.pdbqt");
	}
	
	@Test
	public void extractAtomsFromPharmacophore() throws Exception{		
		expect(dbFiles.retrieve(pharmacophore.database)).andReturn(pharmacophorePdbqt);
		replay(dbFiles);
		engine.parsePharmacophorePdbqtAndSaveAtoms(pharmacophore, pharmacophore.database, pharmacophore.similarityThreshold);
		verify(dbFiles);
		
		pharmacophoreAtoms = engine.getPharmacophoreAtomList();
		assertEquals(21, pharmacophoreAtoms.size());
		assertEquals(0,pharmacophoreAtoms.get(0).calculateDistanceWith(new Atom(14.569  ,31.755  ,20.378,"C")),0.001);
		assertEquals("C",pharmacophoreAtoms.get(0).getAtomType());

		assertEquals(0,pharmacophoreAtoms.get(11).calculateDistanceWith(new Atom(19.149  ,37.384  ,19.580,"A")),0.001);
		assertEquals("A",pharmacophoreAtoms.get(11).getAtomType());

		assertEquals(0,pharmacophoreAtoms.get(20).calculateDistanceWith(new Atom(17.350  ,34.796  ,24.279,"A")),0.001);
		assertEquals("A",pharmacophoreAtoms.get(20).getAtomType());
	}
	
	@Test
	public void calculatePharmacophoreScoreFromAlignment() throws Exception{
		Alignment alignment = new MapsSimilarities();
		experiment.alignments = new ArrayList<Alignment>();
		experiment.alignments.add(alignment);
		alignment.experiment = experiment;
		File alignmentFile = new File("test-files/pharmacophore/mol30.pdbqt");
		expect(dbFiles.retrieve(pharmacophore.database)).andReturn(pharmacophorePdbqt);
		expect(dbFiles.retrieve(alignment)).andReturn(alignmentFile);
		replay(dbFiles);
		engine.parsePharmacophorePdbqtAndSaveAtoms(pharmacophore, pharmacophore.database, pharmacophore.similarityThreshold);
		PharmacophoreScoring score = engine.applyToAlignment(alignment);
		verify(dbFiles);
		
		assertEquals(0, score.score, 0.0001);
	}

}
