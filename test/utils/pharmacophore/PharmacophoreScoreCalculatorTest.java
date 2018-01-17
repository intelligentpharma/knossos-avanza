package utils.pharmacophore;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.List;

import models.ComparisonExperiment;
import models.MoleculeDatabase;
import models.Pharmacophore;
import models.PharmacophoreKnossos;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import files.DatabaseFiles;

public class PharmacophoreScoreCalculatorTest extends UnitTest{
	
	ComparisonExperiment experiment;
	PharmacophoreKnossos pharmacophore;
	DatabaseFiles dbFiles;
	File pharmacophorePdbqt;
	PharmacophoreKnossosScoreCalculator pharmacophoreScoreCalculator;
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
		engine = createNiceMock(PharmacophoreKnossosEngine.class); 
		
		pharmacophorePdbqt = new File("test-files/pharmacophore/5EH.pdbqt");
		pharmacophoreScoreCalculator = new PharmacophoreKnossosScoreCalculator(pharmacophore, dbFiles);
		pharmacophoreScoreCalculator.setComparisonExperimentId(experiment.id);
//		engine.setPharmacophoreEngine(pharmacophoreScoreCalculator);
	}
	
	@Test
	public void doJobCorrectly() throws Exception{		
//		engine.parsePharmacophorePdbqtAndSaveAtoms(pharmacophore, pharmacophore.database, pharmacophore.similarityThreshold);
		replay(dbFiles, engine);
		pharmacophoreScoreCalculator.doJob();
		verify(dbFiles,engine);
	}
	
}
