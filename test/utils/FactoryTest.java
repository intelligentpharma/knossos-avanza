package utils;

import java.util.ArrayList;
import java.util.List;

import jobs.KnossosJob;
import jobs.comparison.ExperimentErrorsRelauncher;
import jobs.comparison.ExperimentLauncher;
import jobs.comparison.FingerprintPegasusExperimentLauncher;
import jobs.comparison.FullExperimentLauncher;
import jobs.comparison.PegasusExperimentLauncher;
import jobs.database.analyze.BoxPlotJob;
import jobs.database.analyze.FingerprintMatrixJob;
import jobs.database.analyze.MoleculeDatabaseClusterizationJob;
import jobs.database.analyze.MoleculeDatabaseCorrelationJob;
import jobs.database.calculate.LigandEfficiencyCalculatorJob;
import jobs.database.calculate.MoleculeDatabaseCalculatorJob;
import jobs.database.calculate.RcdkDescriptorsCalculatorJob;
import jobs.database.calculate.SmileDatabaseCounterIonsRemovalJob;
import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarExperimentValidationJob;
import jobs.qsar.QsarPreprocessJob;
import jobs.qsar.QsarRModelsPredictor;
import jobs.qsar.QsarRuleBasedModelPredictor;
import jobs.qsar.RScriptLauncher;
import models.ChemicalProperty;
import models.ComparisonExperiment;
import models.MoleculeDatabase;
import models.PharmacophoreKnossos;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Play;
import play.db.jpa.Blob;
import play.jobs.Job;
import play.test.UnitTest;
import utils.bidimensional.FingerprintFrequencyMatrix;
import utils.bidimensional.FingerprintFrequencyMatrixImpl;
import utils.bidimensional.FingerprintGenerator;
import utils.bidimensional.Molprint2D;
import utils.bidimensional.Smile;
import utils.database.ChemicalPropertyAggregator;
import utils.database.DatabasePopulationUtils;
import utils.database.DatabasePopulationUtilsImpl;
import utils.database.StatelessDatabaseAccess;
import utils.experiment.ComparisonExperimentExporter;
import utils.experiment.ExperimentImporter;
import utils.experiment.TestDataCreator;
import utils.pharmacophore.PharmacophoreKnossosScoreCalculator;
import utils.pharmacophore.PharmacophoreScoreCalculator;
import utils.queue.PriorityJobQueue;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;
import utils.scripts.MultiStatistics;
import utils.scripts.SlurmExternalScript;
import utils.scripts.pholus.EvolutionaryPholus;
import utils.scripts.pholus.Pholus;
import utils.scripts.pholus.PholusImpl;
import visitors.StructureDataFileGeneratorOrderedByPonderation;
import engine.bidimensional.BidimensionalEngine;
import engine.bidimensional.LingosComparisonEngine;
import engine.factory.FileNameFactory;
import engine.factory.FileNameFactoryImpl;
import engine.tridimensional.maps.GrindMapsGenerator;
import files.DatabaseFilesImpl;
import files.FileFormatTranslator;
import files.FileFormatTranslatorImpl;
import files.FileGenerator;
import files.ReferenceBoxExtractor;
import files.formats.csv.CsvWriter;
import files.formats.csv.CsvWriterImpl;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.PredictionCsvParser;
import files.formats.other.RcdkDescriptorsParser;
import files.formats.pdbqt.AlignmentToPdbqtFileGenerator;
import files.formats.pdbqt.MoleculeParserPdbqt;
import files.formats.sdf.MoleculeParserSdf;
import files.formats.smiles.MoleculeParserSmile;

public class FactoryTest extends UnitTest {

    private Factory factory;
    private User user;

    @Before
    public void setup() {
        factory = new FactoryImpl();
        user = new User("aperreau", "hola", "adeu");
        user.save();
    }

    @Test
    public void experimentChargeTypesToString() {
        assertEquals("ORIGINAL", Factory.ORIGINAL_NAME);
        assertEquals("EEM (not evaluated)", Factory.EEM_NAME);
        assertEquals("GASTEIGER (not evaluated)", Factory.GASTEIGER_NAME);
        assertEquals("MMFF94 (not evaluated)", Factory.MMFF94_NAME);
    }

    @Test
    public void getExternalScriptLauncherReturnsSlurm() {
        ExternalScript launcher = factory.getExternalScriptLauncher();
        assertTrue(launcher instanceof SlurmExternalScript);
        
        //1st call initializes factory.launcher, so the behaviour is slightly different!
        launcher = factory.getExternalScriptLauncher();
        assertTrue(launcher instanceof SlurmExternalScript);
    }

    @Test
    public void defaultExternalScriptIsReturnedForDevelopmentEnvironment() {
        Play.id = "dev";
        ExternalScript launcher = factory.getExternalScriptLauncher();
        assertTrue(launcher instanceof ExternalScriptViaCommandLine);
        Play.id = "test";
    }

    @Test
    public void fileUtilsIsASingleton() {
        assertEquals(factory.getFileUtils(), factory.getFileUtils());
    }

    @Test
    public void createsTheAppropriateMoleculeFileParser() {
        assertTrue(factory.getMoleculeFileParser("somefile.sdf") instanceof MoleculeParserSdf);
        assertTrue(factory.getMoleculeFileParser("somefile.pdbqt") instanceof MoleculeParserPdbqt);
        assertTrue(factory.getMoleculeFileParser("somefile.smi") instanceof MoleculeParserSmile);
    }

    @Test
    public void failsWhenCreatingAParserForUnsupportedMoleculeFiles() {
        try {
            factory.getMoleculeFileParser("somefile.uns");
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertEquals("Unknown database file type somefile.uns", e.getMessage());
        }
    }

    @Test
    public void createsFileFormatTranslatorCorrectly() {
        FileFormatTranslator translator = factory.getFileFormatTranslator();
        assertTrue(translator instanceof FileFormatTranslatorImpl);
        assertTrue(((FileFormatTranslatorImpl) translator).launcher instanceof SlurmExternalScript);
    }

    @Test
    public void createsDatabasePopulationUtilsCorrectly() {
        DatabasePopulationUtils dbUtils = factory.getDatabasePopulationUtils();
        assertTrue(dbUtils instanceof DatabasePopulationUtilsImpl);
        assertEquals(factory, ((DatabasePopulationUtilsImpl) dbUtils).toolsFactory);
        assertTrue(((DatabasePopulationUtilsImpl) dbUtils).dbFiles instanceof DatabaseFilesImpl);
    }

    @Test
    public void createsFileGeneratorCorrectly() {
        FileGenerator fileGenerator = factory.getAlignmentToPdbqtFileGenerator();
        assertTrue(fileGenerator instanceof AlignmentToPdbqtFileGenerator);
        assertFalse(((AlignmentToPdbqtFileGenerator) fileGenerator).isUnique);
    }

    @Test
    public void createsUniqueFileGeneratorCorrectly() {
        FileGenerator fileGenerator = factory.getUniqueFileGenerator();
        assertTrue(fileGenerator instanceof AlignmentToPdbqtFileGenerator);
        assertTrue(((AlignmentToPdbqtFileGenerator) fileGenerator).isUnique);
    }

    @Test
    public void pholusPlsCreatedCorrectly() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        Pholus pholus = factory.getPholus("pholusName", experiment, false, "activity", "active",
                "clusterColumn", "clusterValue", Factory.LOGISTIC_REGRESSION);
        assertTrue(pholus instanceof PholusImpl);
        assertTrue(((PholusImpl) pholus).launcher instanceof SlurmExternalScript);
    }

    @Test
    public void evolutionaryPholusCreatedCorrectly() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        Pholus pholus = factory.getPholus("pholusName", experiment, false, "activity", "active",
                "clusterColumn", "clusterValue", Factory.PHOLUS);
        assertTrue(pholus instanceof EvolutionaryPholus);
        assertTrue(((EvolutionaryPholus) pholus).launcher instanceof SlurmExternalScript);
        //Can't check fileUtils is set correctly with current API
    }

    @Test
    public void chargeTypeNamesAreCorrect() {
        assertEquals(Factory.ORIGINAL_NAME, factory.getChargeTypeName(Factory.ORIGINAL));
        assertEquals(Factory.GASTEIGER_NAME, factory.getChargeTypeName(Factory.GASTEIGER));
        assertEquals(Factory.EEM_NAME, factory.getChargeTypeName(Factory.EEM));
        assertEquals(Factory.MMFF94_NAME, factory.getChargeTypeName(Factory.MMFF94));
    }

    @Test
    public void chargeTypeNameFailsForInexistentType() {
        try {
            factory.getChargeTypeName(-1);
            fail("Should not allow non-existent charge type");
        } catch (RuntimeException e) {
            assertEquals("ChargeType does not exist", e.getMessage());
        }
    }

    @Test
    public void similarityCalculationNamesAreCorrect() {
        assertEquals(Factory.TYPE_OR_NAME, factory.getSimilarityCalculationName(Factory.TYPE_OR));
        assertEquals(Factory.TYPE_AND_NAME, factory.getSimilarityCalculationName(Factory.TYPE_AND));
        assertEquals(Factory.TYPE_TARGET_NAME, factory.getSimilarityCalculationName(Factory.TYPE_TARGET));
        assertEquals(Factory.TYPE_PROBE_NAME, factory.getSimilarityCalculationName(Factory.TYPE_PROBE));
    }

    @Test
    public void similarityCalculationNameFailsForInexistentType() {
        try {
            factory.getSimilarityCalculationName(-1);
            fail("Should not allow non-existent similarity calculation type");
        } catch (RuntimeException e) {
            assertEquals("SimilarityCalculationType does not exist", e.getMessage());
        }
    }

    @Test
    public void createsStructureDataFileGeneratorCorrectly() {
        StructureDataFileGeneratorOrderedByPonderation generator = factory.getStructureDataFileGenerator(true, 0L, "FALSE");
        assertTrue(generator instanceof StructureDataFileGeneratorOrderedByPonderation);
    }

    @Test
    public void createsStructureDataFilWithoutSystemPropertieseGeneratorCorrectly() {
        StructureDataFileGeneratorOrderedByPonderation generator = factory.getStructureDataFileWithoutSystemPropertiesGenerator(true, null);
        assertTrue(generator instanceof StructureDataFileGeneratorOrderedByPonderation);
    }

    @Test
    public void priorityQueueIsASingleton() {
        PriorityJobQueue queue1 = factory.getPriorityJobQueue();
        PriorityJobQueue queue2 = factory.getPriorityJobQueue();
        assertEquals(queue1, queue2);
        assertNotNull(queue1);
    }

    @Test
    public void createsAStatelessDatabaseAccessObject() {
        DatabaseAccess dbAccess = factory.createDatabaseAccess();
        assertNotNull(dbAccess);
        assertTrue(dbAccess instanceof StatelessDatabaseAccess);
    }

    @Test
    public void createsMoleculeDatabaseCalculatorJob() {
        Job job = factory.createMoleculeDatabaseCalculatorJob(12);
        assertNotNull(job);
        assertTrue(job instanceof MoleculeDatabaseCalculatorJob);
    }

    @Test
    public void creates2DPegassusEngine() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        experiment.engineId = Factory.LINGO_SIM;
        BidimensionalEngine engine = factory.get2DEngine(experiment);
        assertNotNull(engine);
        assertTrue(engine instanceof LingosComparisonEngine);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionOnFakeEngineId() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        experiment.engineId = -12;
        factory.get2DEngine(experiment);
    }

    @Test
    public void createsMultiStatistics() {
        List<ComparisonExperiment> experiments = new ArrayList<ComparisonExperiment>();
        MultiStatistics statistics = factory.createMultiStatistics(experiments, "activityColumn", "activeValue", "clusterColumn", "1");
        assertNotNull(statistics);
    }

    @Test
    public void getsExperimentImporter() {
        ExperimentImporter importer = factory.getExperimentImporter();
        assertNotNull(importer);
    }

    @Test
    public void createsExperimentExporter() {
        ComparisonExperimentExporter exporter = factory.createExperimentExporter();
        assertNotNull(exporter);
    }

    @Test
    public void getsCompressionUtils() {
        CompressionUtils utils = factory.getCompressionUtils();
        assertNotNull(utils);
        assertTrue(utils instanceof CompressionUtilsImpl);
    }

    @Test
    public void createsSmileDatabaseCounterIonsRemovalJob() {
        Job job = factory.createSmileDatabaseCounterIonsRemovalJob(12);
        assertNotNull(job);
        assertTrue(job instanceof SmileDatabaseCounterIonsRemovalJob);
    }

    @Test
    public void createsFullExperimentLauncher() {
        ComparisonExperiment experiment = new ComparisonExperiment();
        experiment.id = -1L;
        experiment.engineId = Factory.AUTODOCK4;
        ExperimentLauncher launcher = factory.createExperimentLauncher(experiment);
        assertNotNull(launcher);
        assertTrue(launcher instanceof FullExperimentLauncher);
    }

    @Test
    public void createsPegasusExperimentLauncher() {
        TestDataCreator creator = new TestDataCreator();
        ComparisonExperiment experiment = creator.createFingerprintExperiment(Factory.LINGO_SIM, Factory.LINGO_SIM_NAME);
        ExperimentLauncher launcher = factory.createExperimentLauncher(experiment);
        assertNotNull(launcher);
        assertTrue(launcher instanceof PegasusExperimentLauncher);
    }

    @Test
    public void createFingerprintExperimentLauncherGRAPH() {
        TestDataCreator creator = new TestDataCreator();
        ComparisonExperiment experiment = creator.createFingerprintExperiment(Factory.FINGERPRINTS_GRAPH, Factory.FINGERPRINTS_GRAPH_NAME);
        ExperimentLauncher launcher = factory.createExperimentLauncher(experiment);
        assertNotNull(launcher);
        assertTrue(launcher instanceof FingerprintPegasusExperimentLauncher);
    }

    @Test
    public void createFingerprintExperimentLauncherMACCS() {
        TestDataCreator creator = new TestDataCreator();
        ComparisonExperiment experiment = creator.createFingerprintExperiment(Factory.FINGERPRINTS_MACCS, Factory.FINGERPRINTS_MACCS_NAME);
        ExperimentLauncher launcher = factory.createExperimentLauncher(experiment);
        assertNotNull(launcher);
        assertTrue(launcher instanceof FingerprintPegasusExperimentLauncher);
    }
    
    @Test
    public void createFingerprintExperimentLauncherHYBRIDIZATION() {
        TestDataCreator creator = new TestDataCreator();
        ComparisonExperiment experiment = creator.createFingerprintExperiment(Factory.FINGERPRINTS_HYBRIDIZATION, Factory.FINGERPRINTS_HYBRIDIZATION_NAME);
        ExperimentLauncher launcher = factory.createExperimentLauncher(experiment);
        assertNotNull(launcher);
        assertTrue(launcher instanceof FingerprintPegasusExperimentLauncher);
    }

    @Test
    public void createQsarPlsExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_PLS;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }

    @Test
    public void createQsarSparsePlsExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_SPARSE_PLS;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }

    @Test
    public void createPcaQsarExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_PCA;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }

    @Test
    public void createQsarSVMRegressionExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_SVM_REGRESSION;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }

    @Test
    public void createQsarSVMClassificationExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_SVM_CLASSIFICATION;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }
    
    @Test
    public void createQsarRuleBasedExperimentLauncher() {
        QsarExperiment experiment = new QsarExperiment();
        experiment.id = -1L;
        experiment.qsarType = Factory.QSAR_RULE_BASED;
        AbstractQsarExperimentLauncher launcher = factory.createQsarExperimentLauncher(experiment);
        assertNotNull(launcher);
    }

    @Test
    public void createRcdkDescriptorsCalculatorJob() {
        MoleculeDatabase database = new MoleculeDatabase();
        database.id = -1L;
        Job job = factory.createRcdkDescriptorsCalculatorJob(database, "2d");
        assertNotNull(job);
        assertTrue(job instanceof RcdkDescriptorsCalculatorJob);
    }

    @Test
    public void createLingoFrequencyMatrix() {
        FingerprintFrequencyMatrix matrix = factory.createFingerprintFrequencyMatrix();
        assertNotNull(matrix);
        assertTrue(matrix instanceof FingerprintFrequencyMatrixImpl);
    }

    @Test
    public void createSmile() {
        Smile smile = factory.createSmile("CCC", "molecule name");
        assertNotNull(smile);
        assertEquals("CCC", smile.getCode());
        assertEquals("molecule name", smile.getName());
    }

    @Test
    public void createMolprint2d() {
        Molprint2D compound = factory.createMolprint2d("CCC", "molecule name");
        assertNotNull(compound);
        assertEquals("CCC", compound.getCode());
        assertEquals("molecule name", compound.getName());
    }

    @Test
    public void engineNames() {
        assertEquals(Factory.AUTODOCK4_NAME, factory.getEngineName(Factory.AUTODOCK4));
        assertEquals(Factory.AUTODOCK_VINA_NAME, factory.getEngineName(Factory.AUTODOCK_VINA));
        assertEquals(Factory.SELENE_NAME, factory.getEngineName(Factory.SELENE));
        assertEquals(Factory.SELENE_AUTODOCK4_2_3_NAME, factory.getEngineName(Factory.SELENE_AUTODOCK4_2_3));
        assertEquals(Factory.SELENE_VINA_NAME, factory.getEngineName(Factory.SELENE_VINA));
        assertEquals(Factory.LINGO_SIM_NAME, factory.getEngineName(Factory.LINGO_SIM));
        assertEquals(Factory.FINGERPRINTS_MACCS_NAME, factory.getEngineName(Factory.FINGERPRINTS_MACCS));
        assertEquals(Factory.FINGERPRINTS_GRAPH_NAME, factory.getEngineName(Factory.FINGERPRINTS_GRAPH));
        assertEquals(Factory.FINGERPRINTS_HYBRIDIZATION_NAME, factory.getEngineName(Factory.FINGERPRINTS_HYBRIDIZATION));
        assertEquals(Factory.INVERSE_AD_IMPROVED_NAME, factory.getEngineName(Factory.INVERSE_AD_IMPROVED));
        assertEquals(Factory.FAKE_DOCKING_ENGINE_NAME, factory.getEngineName(Factory.FAKE_DOCKING));
        assertEquals(Factory.FAKE_INVERSE_DOCKING_ENGINE_NAME, factory.getEngineName(Factory.FAKE_INVERSE_DOCKING));
        try{
        	factory.getEngineName(666);
        	fail();
        }
        catch(RuntimeException e){
        }
    }

    @Test
    public void createPropertiesCsvParser() {
        DatabaseActionOutputCsvParser parser = factory.createPropertiesCsvParser(new Blob(), ChemicalProperty.NHEA, ChemicalProperty.NHEA);
        assertNotNull(parser);
        assertTrue(parser instanceof DatabaseActionOutputCsvParser);
    }

    @Test
    public void createLigandEfficiencyCalculatorJob() {
        Job job = factory.createLigandEfficiencyCalculatorJob(-1, "PKI Column");
        assertNotNull(job);
        assertTrue(job instanceof LigandEfficiencyCalculatorJob);
    }

    @Test
    public void createRcdkDescriptorsParser() {
        MoleculeDatabase database = new MoleculeDatabase();
        DatabaseActionOutputCsvParser parser = factory.createRcdkDescriptorsParser(database);
        assertNotNull(parser);
        assertTrue(parser instanceof RcdkDescriptorsParser);
    }

    @Test
    public void createQsarPreprocessJob() {
        MoleculeDatabase database = new MoleculeDatabase();
        database.id = -1L;
        String [] emptyArray = {};
        Job job = factory.createQsarPreprocessJob(database, emptyArray, emptyArray, 0, 0, 0.0);
        assertNotNull(job);
        assertTrue(job instanceof QsarPreprocessJob);
    }

    @Test
    public void createCsvWriter() {
        CsvWriter parser = factory.createCsvWriter("output", "directory");
        assertNotNull(parser);
        assertTrue(parser instanceof CsvWriterImpl);
    }

    @Test
    public void createQsarRModelsPredictionLauncher() {
        QsarRModelsPredictor launcher = factory.createQsarRModelsPredictionLauncher(-1L, -1L);
        assertNotNull(launcher);
        assertTrue(launcher instanceof QsarRModelsPredictor);
    }

    @Test
    public void createQsarRuleBasedModelsPredictionLauncher() {
        QsarRuleBasedModelPredictor launcher = factory.createQsarRuleBasedModelPredictionLauncher(-1L, -1L);
        assertNotNull(launcher);
        assertTrue(launcher instanceof QsarRuleBasedModelPredictor);
    }
    
    @Test
    public void createPredictionCsvParser() {
        DatabaseActionOutputCsvParser parser = factory.createPredictionCsvParser(-1L, -1L);
        assertNotNull(parser);
        assertTrue(parser instanceof PredictionCsvParser);
    }

    @Test
    public void getDBUtils() {
        DBUtils utils = factory.getDBUtils();
        assertNotNull(utils);
        assertTrue(utils instanceof DBUtilsImpl);
    }

    @Test
    public void createsKnossosInitializer() {
        KnossosInitializer initializer = factory.createKnossosInitializer();
        assertNotNull(initializer);
        assertTrue(initializer instanceof KnossosInitializerImpl);
    }

    @Test
    public void createsExperimentErrorsRelauncher() {
        User owner = User.findByUserName("aperreau");
        ExperimentErrorsRelauncher relauncher = factory.createExperimentErrorsRelauncher(1, owner);
        assertNotNull(relauncher);
        assertTrue(relauncher instanceof ExperimentErrorsRelauncher);
    }

    @Test
    public void createMoleculeDatabaseClusterizationJob() {
        MoleculeDatabase database = new MoleculeDatabase();
        database.id = -1L;
        Job job = factory.createMoleculeDatabaseClusterizationJob(database);
        assertNotNull(job);
        assertTrue(job instanceof MoleculeDatabaseClusterizationJob);
    }

    @Test
    public void createMoleculeDatabaseClusterizationParser() {
        MoleculeDatabase database = new MoleculeDatabase();
        database.id = -1L;
        DatabaseActionOutputCsvParser parser = factory.createMoleculeDatabaseClusterizationParser(database);
        assertNotNull(parser);
        assertTrue(factory.createMoleculeDatabaseClusterizationParser(database) instanceof DatabaseActionOutputCsvParser);
    }

    @Test
    public void getMaxSmilesToConvertAnSmiToSdf(){
    	assertEquals(Factory.MAX_SMILES_TO_SDF, factory.getMaxSmilesToSdf());
    }

    @Test
    public void createsBoxReferenceCalculator() {
    	MoleculeDatabase database = new MoleculeDatabase();
        ReferenceBoxExtractor referenceBoxExtractor = factory.createReferenceBoxCalculator(database, factory.getDatabaseFiles(), new ComparisonExperiment());
        assertNotNull(referenceBoxExtractor);
        assertTrue(referenceBoxExtractor instanceof ReferenceBoxExtractor);
    }
    
    @Test
    public void createsGrindMapsGenerator(){
    	MoleculeDatabase database = new MoleculeDatabase();
    	database.id = -1L;
    	FileNameFactory fileNameFactory = new FileNameFactoryImpl();
    	GrindMapsGenerator generator = factory.createGrindMapsGenerator(database, fileNameFactory);
    	assertNotNull(generator);
    	assertTrue(generator instanceof GrindMapsGenerator);
    }
 
    @Test
    public void createMoleculeDatabaseCorrelationJob() {
    	MoleculeDatabase database = new MoleculeDatabase();
    	database.id = -1L;
        Job job = factory.createMoleculeDatabaseCorrelationJob(database);
        assertNotNull(job);
        assertTrue(job instanceof MoleculeDatabaseCorrelationJob);
    }
    
    @Test
    public void createFingerprintGeneratorECFP(){
    	MoleculeDatabase database = new MoleculeDatabase();
    	database.id = -1L;
    	int searchDepth = 2;
    	int atomLabelType = 3;
    	FingerprintGenerator fingerprintGenerator = factory.createFingerprintGenerator(database.id, factory.getDatabaseFiles(), searchDepth, Factory.FINGERPRINTS_ECFP, atomLabelType);
    	assertNotNull(fingerprintGenerator);
    	assertTrue(fingerprintGenerator instanceof FingerprintGenerator);
    }

    @Test
    public void createFingerprintGeneratorECFPVariant(){
    	MoleculeDatabase database = new MoleculeDatabase();
    	database.id = -1L;
    	int searchDepth = 2;
    	int atomLabelType = 3;
    	FingerprintGenerator fingerprintGenerator = factory.createFingerprintGenerator(database.id, factory.getDatabaseFiles(), searchDepth, Factory.FINGERPRINTS_ECFPVARIANT, atomLabelType);
    	assertNotNull(fingerprintGenerator);
    	assertTrue(fingerprintGenerator instanceof FingerprintGenerator);
    }

    @Test
    public void createFingerprintGeneratorMolprint2D(){
    	MoleculeDatabase database = new MoleculeDatabase();
    	database.id = -1L;
    	int searchDepth = 2;
    	int atomLabelType = 3;
    	FingerprintGenerator fingerprintGenerator = factory.createFingerprintGenerator(database.id, factory.getDatabaseFiles(), searchDepth, Factory.FINGERPRINTS_MOLPRINT2D, atomLabelType);
    	assertNotNull(fingerprintGenerator);
    	assertTrue(fingerprintGenerator instanceof FingerprintGenerator);
    }

    @Test
   	public void createFingerPrintMatrixJob(){
	   	MoleculeDatabase database = new MoleculeDatabase();
	   	database.id = -1L;
		int searchDepth = 2;
		int atomLabelType = 3;
		Job job = factory.createFingerprintMatrixJob(database.id, searchDepth, atomLabelType, Factory.FINGERPRINTS_ECFP);   
	    assertNotNull(job);
	    assertTrue(job instanceof FingerprintMatrixJob);
	}
    
    @Test
    public void createBoxPlotJob(){
    	long databaseId = -1L;
    	String[] selectedDescriptors = {"prop1","prop2"};
    	String activity = "activity";
    	String username = "xmaresma";
    	Job boxplotJob = factory.createBoxPlotJob(databaseId, selectedDescriptors, activity, username);
    	assertNotNull(boxplotJob);
    	assertTrue(boxplotJob instanceof BoxPlotJob);
    	
    }
    
    @Test
    public void createRscriptLauncher(){
    	RScriptLauncher launcher = factory.createRScriptLauncher(TemplatedConfiguration.get("qsar.plsIteration"));
    	assertNotNull(launcher);    	
    }
    
    @Test
    public void createQsarExperimentValidationJob(){
    	KnossosJob job = factory.createQsarExperimentValidationJob(-1L, 0, "qsar.pls");
    	assertNotNull(job);
    	assertTrue(job instanceof QsarExperimentValidationJob);
    }
    
    @Test
    public void createChemicalPropertyAggregator(){
    	ChemicalPropertyAggregator aggregator = factory.createChemicalPropertyAggregator(-1L, "newProperty","x+y");
    	assertNotNull(aggregator);
    	assertTrue(aggregator instanceof ChemicalPropertyAggregator);
    }
    
    @Test
    public void createPharmacophoreScoreCalculator(){
    	MoleculeDatabase database = new MoleculeDatabase();
    	PharmacophoreKnossos pharmacophore = new PharmacophoreKnossos(database, 1.0);
    	PharmacophoreScoreCalculator calculator = factory.createPharmacophoreKnossosScoreCalculator(pharmacophore, 0L);    			    	    	
    	assertNotNull(calculator);
    	assertTrue(calculator instanceof PharmacophoreScoreCalculator);
    }
    
}