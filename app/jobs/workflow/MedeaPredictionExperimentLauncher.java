package jobs.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import files.DatabaseFiles;
import files.FileUtils;
import jobs.comparison.ExperimentLauncher;
import jobs.database.calculate.RcdkDescriptorsCalculatorJob;
import jobs.qsar.AbstractRScriptsLauncher;
import jobs.qsar.QsarPreprocessJob;
import jobs.qsar.QsarRModelsPredictor;
import models.ChemicalProperty;
import models.Deployment;
import models.ExperimentStatus;
import models.Filter;
import models.Molecule;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.User;
import models.WorkflowExperiment;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.Job;
import play.libs.F.Promise;
import utils.Factory;
import utils.FactoryImpl;

public class MedeaPredictionExperimentLauncher extends ExperimentLauncher {

	public static final Logger logger = Logger.getLogger(MedeaPredictionExperimentLauncher.class);

	QsarRModelsPredictor qsarRModelsPredictor;

	DatabaseFiles dbFiles;

	FileUtils fileUtils;

	public MedeaPredictionExperimentLauncher(long experimentId, User owner, Factory factory) {
		super(experimentId, owner, factory);
	}

	public void setQsarRModelsPredictor(QsarRModelsPredictor qsarRModelsPredictor) {
		this.qsarRModelsPredictor = qsarRModelsPredictor;
	}

	public void setDbFiles(DatabaseFiles dbFiles) {
		this.dbFiles = dbFiles;
	}

	public void setFileUtils(FileUtils fileUtils) {
		this.fileUtils = fileUtils;
	}

	@Override
	protected void launch() {
		
		MoleculeDatabase weDatabase = this.experiment.probeMolecules;
		
		if (isWorkflowExperiment(this.experiment.id)){	
			logger.info("Preprocess database starts" + weDatabase.id);
			//Factory for creating jobs
			Factory factory = new FactoryImpl();
	    	factory.setUsername(this.experiment.owner.username);
			//Value 5 is hardcoded as per GUI value	    
			AbstractRScriptsLauncher calculatorJob = factory.createRcdkDescriptorsCalculatorJob(weDatabase, "5");
			//Runs job now and syncs (e.g., blocks execution until job is over) via promise
			Promise pr = calculatorJob.now();
			try {
				pr.get();
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//We comment preprocess as it does not seem to be required and can cause problems with very small databases. Uncomment if required (approved by LN 2017-11-03)
			//Default value for preprocess
			//QsarPreprocessJob qsarPreprocessJob = (QsarPreprocessJob) factory.createQsarPreprocessJob(weDatabase, new String[]{}, new String[]{}, 20, 0, 0.9);
//			//Runs job now and syncs (e.g., blocks execution until job is over) via promise
//			pr = qsarPreprocessJob.now();
//			try {
//				pr.get();
//			} catch (InterruptedException | ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			logger.info("Preprocess database finishes" + weDatabase.id);
		}
		qsarRModelsPredictor.launch();
	}

	private List<String> filter(WorkflowExperiment we, Map<String, Float> moleculeMap) {

		Filter filter = we.filter;

		int numberOfMolecules = 0;

		List<String> molSet = new ArrayList<>();

		Comparator<String> comparator = new ValueComparator(moleculeMap);

		TreeMap<String, Float> orderedMoleculeMap = new TreeMap<String, Float>(comparator);
		orderedMoleculeMap.putAll(moleculeMap);
		if (we.filter.decreasing){
			if (filter.type.equals(Filter.PERCENTAGE)) {
				numberOfMolecules = Math.round(moleculeMap.keySet().size() * filter.threshold / 100l);
				molSet = filterNumberOfMolecules(numberOfMolecules, molSet, orderedMoleculeMap);
			} else if (filter.type.equals(Filter.NUMBER_OF_MOLECULES)) {
				numberOfMolecules = Math.round(filter.threshold);
				molSet = filterNumberOfMolecules(numberOfMolecules, molSet, orderedMoleculeMap);
			} else {
				for (String name : orderedMoleculeMap.descendingKeySet()) {				
					if (orderedMoleculeMap.get(name) > filter.threshold) {
						molSet.add(name);
					}else {
						break;
					}
				}
			}
		}
		else{
			comparator = new ValueComparator(moleculeMap,true);
			orderedMoleculeMap = new TreeMap<String, Float>(comparator);
			orderedMoleculeMap.putAll(moleculeMap);
			if (filter.type.equals(Filter.PERCENTAGE)) {
				numberOfMolecules = Math.round(moleculeMap.keySet().size() * filter.threshold / 100l);
				molSet = filterNumberOfMolecules(numberOfMolecules, molSet, orderedMoleculeMap);
			} else if (filter.type.equals(Filter.NUMBER_OF_MOLECULES)) {
				numberOfMolecules = Math.round(filter.threshold);
				molSet = filterNumberOfMolecules(numberOfMolecules, molSet, orderedMoleculeMap);
			} else {
				for (String name : orderedMoleculeMap.descendingKeySet()) {				
					if (orderedMoleculeMap.get(name) < filter.threshold) {
						molSet.add(name);
					}else {
						break;
					}
				}
			}
		}
		
		return molSet;
	}

	private List<String> filterNumberOfMolecules(int numberOfMolecules, List<String> molSet,
			TreeMap<String, Float> orderedMoleculeMap) {
		molSet.addAll(orderedMoleculeMap.keySet());
		int size = molSet.size(); 
		if (numberOfMolecules > size){
			return molSet.subList(0,size);
		}
		return molSet.subList(size-numberOfMolecules, size);
	}

	private Map<String, Float> parseResults(WorkflowExperiment we) {
		Map<String, Float> moleculeMap = new HashMap<>();
		MoleculeDatabase moleculeDatabase = experiment.probeMolecules;
		QsarExperiment qsarExperiment = QsarExperiment.findById(experiment.modelId);
		String activityPropertyName = qsarExperiment.activityProperty;
		String predictedPropertyName = activityPropertyName + "_" + qsarExperiment.id + "_Full";

		for (Molecule molecule : moleculeDatabase.molecules) {
			for (Deployment deployment : molecule.deployments) {
				ChemicalProperty predictedProperty = ChemicalProperty.findByDeploymentAndName(deployment,
						predictedPropertyName);
				moleculeMap.put(deployment.name, Float.parseFloat(predictedProperty.value));
			}
		}

		return moleculeMap;
	}
	
	
	private boolean isWorkflowExperiment(long experimentId){
		String query = "select 1 from  workflowexperiment e where e.experiment_id = "
				+ experimentId;
		logger.debug(query);
		List results = JPA.em().createNativeQuery(query).getResultList();		
		return !results.isEmpty();
	}
	
	private void uploadDatabase(WorkflowExperiment we, List<String> molSet) {
		
		MoleculeDatabase weDatabase = MoleculeDatabase.findById(we.database.id);						
		String structureFileName = dbFiles.getFileName(experiment.probeMolecules);
		String outputFile = structureFileName + "_filtered.sdf";
		fileUtils.copyAndFilterSdfFile(structureFileName, outputFile, molSet);
		File structureFile = new File(outputFile);		
		Job dbUploadJob = factory.createMoleculeDatabaseWorkflowCreatorJob(weDatabase, structureFile, we);
		dbUploadJob.in(2);

	}

	public void after() {		
		WorkflowExperiment we = WorkflowExperiment.findByExperimentId(experiment.id);
		Map<String, Float> moleculeMap = parseResults(we);
		List<String> molSet = filter(we, moleculeMap);
		logger.info("MedeaExperiment After: Uploading database");
		uploadDatabase(we, molSet);
		logger.info("MedeaExperiment After: Database uploaded");
		experiment.status=ExperimentStatus.FINISHED;
		experiment.save();
	}
}
