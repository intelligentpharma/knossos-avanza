package utils;

import java.io.File;
import java.util.List;

import jobs.KnossosJob;
import jobs.comparison.ExperimentErrorsRelauncher;
import jobs.comparison.ExperimentLauncher;
import jobs.comparison.MapsGeneratorJob;
import jobs.comparison.StructureDataFileGeneratorJob;
import jobs.database.calculate.SmileDatabaseCounterIonsRemovalJob;
import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.AbstractRScriptsLauncher;
import jobs.qsar.QsarRModelsPredictor;
import jobs.qsar.QsarRuleBasedModelPredictor;
import jobs.qsar.RScriptLauncher;
import models.ComparisonExperiment;
import models.Experiment;
import models.MoleculeDatabase;
import models.Pharmacophore;
import models.PharmacophoreKnossos;
import models.PharmacophoreSchrodinger;
import models.Ponderation;
import models.QsarExperiment;
import models.User;
import models.WorkflowExperiment;
import play.db.jpa.Blob;
import play.jobs.Job;
import utils.bidimensional.FingerprintFrequencyMatrix;
import utils.bidimensional.FingerprintGenerator;
import utils.bidimensional.Molprint2D;
import utils.bidimensional.Smile;
import utils.cloud.ExternalNodesManager;
import utils.database.ChemicalPropertyAggregator;
import utils.database.CounterIonsRemover;
import utils.database.DatabasePopulationUtils;
import utils.experiment.ComparisonExperimentExporter;
import utils.experiment.ExperimentImporter;
import utils.experiment.QsarExperimentExporter;
import utils.experiment.QsarExperimentImporter;
import utils.experiment.QsarExperimentRefresh;
import utils.pharmacophore.PharmacophoreScoreCalculator;
import utils.queue.PriorityJobQueue;
import utils.queue.PriorityJobQueueImpl;
import utils.queue.PriorityJobQueueItem;
import utils.scripts.ExternalScript;
import utils.scripts.MultiStatistics;
import utils.scripts.pholus.Pholus;
import visitors.StructureDataFileGeneratorOrderedByDeployment;
import visitors.StructureDataFileGeneratorOrderedByPh4;
import visitors.StructureDataFileGeneratorOrderedByPonderation;
import engine.Engine;
import engine.bidimensional.BidimensionalEngine;
import engine.factory.FileNameFactory;
import engine.tridimensional.maps.GrindMapsGenerator;
import engine.tridimensional.maps.RepresentativeMapsGenerator;
import files.DatabaseFiles;
import files.DeploymentPropertyExtractor;
import files.FileFormatTranslator;
import files.FileGenerator;
import files.FileUtils;
import files.MoleculeParser;
import files.ReferenceBoxExtractor;
import files.formats.csv.CsvWriter;
import files.formats.csv.DatabaseActionOutputCsvParser;
import files.formats.csv.QsarExperimentCsvParser;
import files.formats.csv.QsarExperimentCsvParserCoefficient;
import files.formats.pdbqt.PdbClusterDataExtractor;
import files.formats.smiles.SmilesDataExtractor;

public interface Factory {
	

	Job createMoleculeDatabaseWorkflowCreatorJob(MoleculeDatabase molecules, File transientFile,
			WorkflowExperiment workflowExperiment);

	Job createExternalNodeRemoverJob(Experiment experiment);

	ExternalNodesManager getExternalNodesCreator(String provider);

	Job createExternalNodeCreatorJob(Experiment experiment, List<KnossosJob> cloudJobs, PriorityJobQueue priorityJobQueue,
			List<PriorityJobQueueItem> queuedItems, String provider);
}
