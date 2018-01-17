package engine.tridimensional.aligner;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import models.AlignmentBox;
import models.ComparisonExperiment;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;
import engine.tridimensional.aligner.AlignmentBoxCalculator;
import files.DatabaseFiles;
import files.FileFormatTranslator;
import files.ReferenceBoxExtractor;

public class ReferenceBoxExtractorTest extends UnitTest{
	
	ReferenceBoxExtractor boxExtractor;
	MoleculeDatabase database;

    private static User user;
    private static MoleculeDatabase db;
    private static TestDataCreator testFactory;
    private static ComparisonExperiment experiment;
	
	@Before
	public void setup(){
		boxExtractor = new ReferenceBoxExtractor();
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("aperreau");
        testFactory = new TestDataCreator();
        db = testFactory.createSmallDatabaseWithoutProperties(user);
        experiment = testFactory.createExperiment();
	}
	
	@Test(expected = RuntimeException.class)
	public void throwExceptionWhenDatabaseIsEmpty(){
		database = new MoleculeDatabase();
		boxExtractor.setDatabase(database);
		boxExtractor.getBox();
	}

	@Test(expected = RuntimeException.class)
	public void throwExceptionWhenDatabaseIsNull(){
		boxExtractor.getBox();
	}

	@Test
	public void launchesPdbbox(){
		AlignmentBoxCalculator boxCalculator = createMock(AlignmentBoxCalculator.class);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);

		boxExtractor.setBoxCalculator(boxCalculator);
		boxExtractor.setDatabase(db);
		boxExtractor.setDatabaseFiles(dbFiles);
		boxExtractor.setExperiment(experiment);
		boxExtractor.setFileFormatTranslator(translator);

		String fileName = "file";
		expect(dbFiles.getFileName(db.getAllDeployments().get(0))).andReturn(fileName);
		translator.convertSdfToStaticPdbqt(fileName, fileName+".pdbqt", experiment.chargeType);
		boxCalculator.calculateAlignmentBox(fileName + ".pdbqt", null);
		AlignmentBox box = new AlignmentBox();
		expect(boxCalculator.getAlignmentBox()).andReturn(box);
		
		replay(boxCalculator, dbFiles, translator);
		boxExtractor.getBox();
		verify(boxCalculator, dbFiles, translator);
	}
	
	@Test
	public void launchesPdbboxWithCorrectFile(){
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		AlignmentBoxCalculator boxCalculator = createMock(AlignmentBoxCalculator.class);
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		
		boxExtractor.setBoxCalculator(boxCalculator);
		boxExtractor.setDatabase(db);
		boxExtractor.setDatabaseFiles(dbFiles);
		boxExtractor.setExperiment(experiment);
		boxExtractor.setFileFormatTranslator(translator);

		String fileName = "test-files/1hnw_prodock_easy.pdbqt";
		expect(dbFiles.getFileName(db.getAllDeployments().get(0))).andReturn(fileName);		
		translator.convertSdfToStaticPdbqt(fileName, fileName+".pdbqt", experiment.chargeType);
		boxCalculator.calculateAlignmentBox(fileName+".pdbqt", null);
		AlignmentBox box = new AlignmentBox();
		expect(boxCalculator.getAlignmentBox()).andReturn(box);
				
		replay(boxCalculator, dbFiles, translator);
		
		boxExtractor.getBox();
		
		verify(boxCalculator, dbFiles, translator);
	}
	
	@Test
	public void launchWorksCorrectly(){
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		AlignmentBoxCalculator boxCalculator = createMock(AlignmentBoxCalculator.class);
		FileFormatTranslator translator = createMock(FileFormatTranslator.class);
		
		boxExtractor.setBoxCalculator(boxCalculator);
		boxExtractor.setDatabase(db);
		boxExtractor.setDatabaseFiles(dbFiles);
		boxExtractor.setExperiment(experiment);
		boxExtractor.setFileFormatTranslator(translator);

		String fileName = "test-files/1hnw_prodock_easy.pdbqt";
		expect(dbFiles.getFileName(db.getAllDeployments().get(0))).andReturn(fileName);
		translator.convertSdfToStaticPdbqt(fileName, fileName+".pdbqt", experiment.chargeType);
		boxCalculator.calculateAlignmentBox(fileName+".pdbqt", null);
		AlignmentBox box = new AlignmentBox(79.466, 40.787, 7.728, 1324, 1029, 801);
		
		expect(boxCalculator.getAlignmentBox()).andReturn(box);

		replay(boxCalculator, dbFiles, translator);

		AlignmentBox alignmentBox = boxExtractor.getBox();
		
		assertEquals(79.466, alignmentBox.centerX,0.00001);
		assertEquals(40.787, alignmentBox.centerY,0.00001);
		assertEquals(7.728, alignmentBox.centerZ,0.00001);

		assertEquals(1324, alignmentBox.sizeX);
		assertEquals(1029, alignmentBox.sizeY);
		assertEquals(801, alignmentBox.sizeZ);

		verify(boxCalculator, dbFiles, translator);
	}
	
}
