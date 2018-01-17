package utils;

import java.io.File;
import java.util.List;

import jobs.DatabaseCategorizationJob;
import jobs.KnossosJob;
import jobs.cloud.ExternalNodeCreatorJob;
import jobs.cloud.ExternalNodeRemoverJob;
import jobs.comparison.ExperimentErrorsRelauncher;
import jobs.comparison.ExperimentLauncher;
import jobs.comparison.FingerprintPegasusExperimentLauncher;
import jobs.comparison.FullExperimentLauncher;
import jobs.comparison.MapsGeneratorJob;
import jobs.comparison.PegasusExperimentLauncher;
import jobs.comparison.StructureDataFileGeneratorJob;
import jobs.database.analyze.BoxPlotJob;
import jobs.database.analyze.DiversitySelectionJob;
import jobs.database.analyze.FingerprintMatrixJob;
import jobs.database.analyze.LingosFrequencyTableJob;
import jobs.database.analyze.MoleculeDatabaseClusterizationJob;
import jobs.database.analyze.MoleculeDatabaseCorrelationJob;
import jobs.database.calculate.GenerateConformationsJob;
import jobs.database.calculate.LigandEfficiencyCalculatorJob;
import jobs.database.calculate.MoleculeDatabaseCalculatorJob;
import jobs.database.calculate.PropertyCalculatorJob;
import jobs.database.calculate.ProtonateJob;
import jobs.database.calculate.RcdkDescriptorsCalculatorJob;
import jobs.database.calculate.SmileDatabaseCounterIonsRemovalJob;
import jobs.database.crud.MoleculeDatabaseCSVGeneratorJob;
import jobs.database.crud.MoleculeDatabaseDeleteJob;
import jobs.database.crud.MoleculeDatabaseDuplicationJob;
import jobs.database.crud.MoleculeDatabaseUploaderJob;
import jobs.database.crud.MoleculeDatabaseWorkflowCreatorJob;
import jobs.database.crud.PropertiesUpdaterJob;
import jobs.database.crud.SdfFileGeneratorJob;
import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.AbstractRScriptsLauncher;
import jobs.qsar.QsarExperimentLauncher;
import jobs.qsar.QsarExperimentValidationJob;
import jobs.qsar.QsarPCAExperimentLauncher;
import jobs.qsar.QsarParallelExperimentLauncher;
import jobs.qsar.QsarPreprocessJob;
import jobs.qsar.QsarRModelsPredictor;
import jobs.qsar.QsarRuleBasedExperimentLauncher;
import jobs.qsar.QsarRuleBasedModelPredictor;
import jobs.qsar.RScriptLauncher;
import jobs.qsar.RScriptLauncherImpl;
import jobs.workflow.MedeaPredictionExperimentLauncher;
import jobs.workflow.PythiaExperimentLauncher;
import json.AbstractExperimentJsonExporter;
import json.ComparisonExperimentJsonExporter;
import json.ComparisonExperimentJsonImporter;
import json.ExperimentJsonImporter;
import json.QsarExperimentJsonExporter;
import json.QsarExperimentJsonImporter;
import models.ComparisonExperiment;
import models.Experiment;
import models.ExperimentStatus;
import models.MoleculeDatabase;
import models.Pharmacophore;
import models.PharmacophoreKnossos;
import models.PharmacophoreSchrodinger;
import models.Ponderation;
import models.QsarExperiment;
import models.User;
import models.WorkflowExperiment;

import org.apache.log4j.Logger;

import play.Play;
import play.db.jpa.Blob;
import play.jobs.Job;
import utils.bidimensional.FingerprintFrequencyMatrix;
import utils.bidimensional.FingerprintFrequencyMatrixImpl;
import utils.bidimensional.FingerprintGenerator;
import utils.bidimensional.FingerprintGeneratorImpl;
import utils.bidimensional.Molprint2D;
import utils.bidimensional.Smile;
import utils.cloud.AWSNodesManager;
import utils.cloud.AzureNodesManager;
import utils.cloud.ExternalNodesManager;
import utils.cloud.GcloudNodesManager;
import utils.database.ChemicalPropertyAggregator;
import utils.database.CounterIonsRemover;
import utils.database.CounterIonsRemoverImpl;
import utils.database.DatabasePopulationUtils;
import utils.database.DatabasePopulationUtilsImpl;
import utils.database.MoleculeDatabaseBoxPlotter;
import utils.database.MoleculeDatabaseBoxPlotterImpl;
import utils.database.StatelessDatabaseAccess;
import utils.experiment.ComparisonExperimentExporter;
import utils.experiment.ExperimentImporter;
import utils.experiment.QsarExperimentExporter;
import utils.experiment.QsarExperimentImporter;
import utils.experiment.QsarExperimentRefresh;
import utils.pharmacophore.PharmacophoreKnossosEngine;
import utils.pharmacophore.PharmacophoreKnossosScoreCalculator;
import utils.pharmacophore.PharmacophoreSchrodingerScoreCalculator;
import utils.pharmacophore.PharmacophoreScoreCalculator;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueImpl;
import utils.queue.PriorityJobQueueItem;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;
import utils.scripts.MultiStatistics;
import utils.scripts.MultiStatisticsOutput;
import utils.scripts.SlurmExternalScript;
import utils.scripts.pholus.EvolutionaryPholus;
import utils.scripts.pholus.Pholus;
import utils.scripts.pholus.PholusImpl;
import utils.scripts.pholus.PholusOutputImpl;
import visitors.MoleculeDatabaseCsvGenerator;
import visitors.MultiStatisticsInputGenerator;
import visitors.PholusInputGenerator;
import visitors.StructureDataFileGeneratorOrderedByDeployment;
import visitors.StructureDataFileGeneratorOrderedByPh4;
import visitors.StructureDataFileGeneratorOrderedByPonderation;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2D;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DECFP;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DECFPVariant;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DMolprint;
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import engine.Engine;
import engine.bidimensional.BidimensionalEngine;
import engine.factory.EngineFactory;
import engine.factory.EngineFactoryImpl;
import engine.factory.FileNameFactory;
import engine.factory.FileNameFactoryImpl;
import engine.tridimensional.aligner.AlignmentBoxCalculatorImpl;
import engine.tridimensional.maps.GrindMapsGenerator;
import engine.tridimensional.maps.MapsGenerator;
import engine.tridimensional.maps.RepresentativeMapsGenerator;
import files.DatabaseFiles;
import files.DatabaseFilesImpl;
import files.DeploymentPropertyExtractor;
import files.DeploymentPropertyExtractorImpl;
import files.FileFormatTranslator;
import files.FileFormatTranslatorImpl;
import files.FileGenerator;
import files.FileReadingMoleculeParser;
import files.FileUtils;
import files.FileUtilsImpl;
import files.MoleculeParser;
import files.PredictionNoCsvParser;
import files.ReferenceBoxExtractor;
import files.formats.csv.CsvWriter;
import files.formats.csv.CsvWriterImpl;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.MoleculeDatabaseClusterizationParser;
import files.formats.csv.MoleculeDatabaseCsvParser;
import files.formats.csv.MoleculeParserCsv;
import files.formats.csv.PredictionCsvParser;
import files.formats.csv.PropertiesCsvParser;
import files.formats.csv.QsarExperimentCsvParser;
import files.formats.csv.QsarExperimentCsvParserCoefficient;
import files.formats.csv.QsarExperimentCsvParserEmptyImpl;
import files.formats.csv.QsarExperimentCsvParserResultImpl;
import files.formats.other.RcdkDescriptorsParser;
import files.formats.pdbqt.AlignmentToPdbqtFileGenerator;
import files.formats.pdbqt.AlignmentToPdbqtFileWithoutSystemPropertiesGenerator;
import files.formats.pdbqt.BestByPh4AlignmentToPdbqtFileGenerator;
import files.formats.pdbqt.ClusterAlignmentToPdbqtFileGenerator;
import files.formats.pdbqt.MoleculeParserPdbqt;
import files.formats.pdbqt.PdbClusterDataExtractor;
import files.formats.sdf.MoleculeParserSdf;
import files.formats.smiles.MoleculeParserSmile;
import files.formats.smiles.SmilesDataExtractor;

public class FactoryImpl implements Factory {
	public static final Logger logger = Logger.getLogger(FactoryImpl.class);

	private ExternalScript launcher;
	private FileUtils fileUtils;
	private EngineFactory engineFactory;
	private static PriorityJobQueue priorityJobQueue;
	private String username = "";

	public FactoryImpl() {
		EngineFactoryImpl engineFactory = new EngineFactoryImpl();
		engineFactory.setFactory(this);
		this.engineFactory = engineFactory;
		this.fileUtils = new FileUtilsImpl();
	}

	@Override
	public ExternalScript getExternalScriptLauncher() {
		if (launcher == null) {
			if (Play.id.equalsIgnoreCase("dev")) {
				launcher = new ExternalScriptViaCommandLine();
			} else {
				launcher = new SlurmExternalScript();
			}
		}
		launcher.setUsername(this.username);
		return launcher;
	}

	@Override
	public FileUtils getFileUtils() {
		return this.fileUtils;
	}

	@Override
	public Job createMoleculeDatabaseWorkflowCreatorJob(MoleculeDatabase molecules, File transientFile,
			WorkflowExperiment workflowExperiment) {
		MoleculeDatabaseWorkflowCreatorJob job = new MoleculeDatabaseWorkflowCreatorJob();
		job.setDatabasePopulationUtils(getDatabasePopulationUtils());
		job.setDatabaseAccess(createDatabaseAccess());
		job.setMolecules(molecules.id);
		job.setMoleculesFile(transientFile);
		job.setFactory(this);
		job.setWorkflowExperiment(workflowExperiment);
		return job;
	}

	@Override
	public Job createExternalNodeRemoverJob(Experiment experiment) {
		ExternalNodeRemoverJob job = new ExternalNodeRemoverJob();
		job.setExperiment(experiment);
		job.setExternalScriptLauncher(getExternalScriptLauncher());
		job.setExternalNodesCreator(
				getExternalNodesCreator(ExternalNodesManager.getProviderNameFromExperiment(experiment)));
		return job;
	}

	@Override
	public Job createExternalNodeCreatorJob(Experiment experiment, List<KnossosJob> cloudJobs,
			PriorityJobQueue priorityJobQueue, List<PriorityJobQueueItem> queuedItems, String provider) {
		ExternalNodeCreatorJob job = new ExternalNodeCreatorJob();
		job.setExperiment(experiment);
		job.setExternalScriptLauncher(getExternalScriptLauncher());
		job.setListOfCloudJobs(cloudJobs);
		job.setPriorityJobQueue(priorityJobQueue);
		job.setQueuedItems(queuedItems);
		job.setExternalNodesCreator(getExternalNodesCreator(provider));
		return job;
	}

}