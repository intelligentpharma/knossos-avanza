package jobs.workflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import files.DatabaseFiles;
import files.FileFormatTranslator;
import jobs.comparison.ExperimentLauncher;
import jobs.database.crud.MoleculeDatabaseWorkflowCreatorJob;
import models.ExperimentStatus;
import models.MoleculeDatabase;
import models.User;
import models.WorkflowExperiment;
import utils.Factory;
import utils.FactoryImpl;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class PythiaExperimentLauncher extends ExperimentLauncher {

	protected ExternalScript launcher;

	public PythiaExperimentLauncher(long experimentId, User owner, Factory factory) {
		super(experimentId, owner, factory);
	}

	@Override
	protected void launch() {
		try {
			// Create temporal directory to hold result
			int tmpDirId = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			String tmpDirPath = String.format("./pythiaEmbedded/%s/", tmpDirId);

			this.launcher = new ExternalScriptViaCommandLine();
			// Run pythia
			calculate(tmpDirPath);
			// Get full table CSV
			List<MoaEntry> results = parseResults(tmpDirPath);
			// Apply experiment filter
			List<MoaEntry> filteredResults = filterResults(results);
			// Generate smiles file from filtered results
			String smilesPath = generateSmileFile(filteredResults, tmpDirPath);
			// Generate SDF file from smile file
			String SDFPath = convertSmiToSDF3D(smilesPath, tmpDirPath);
			// Upload SDF file
			uploadResultDatabase(SDFPath);

			setExperimentFinished();
		} catch (Exception e) {
			experiment.addErrorEvent("Pythia Failed");
			experiment.status = ExperimentStatus.ERROR;
			experiment.save();
			logger.info(e.getMessage());
		}
	}

	// Builds command to execute pythia.jar with selected parameters
	private void runPythia(String sdfFile, Float simThres, Float actThres, String simMethod, String database,
			String csvFile) throws IOException {
		String commandTemplate = TemplatedConfiguration.get("pythiaKnossos");
		String pythiaCommand = String.format(commandTemplate, sdfFile, simThres, actThres, simMethod, database,
				csvFile);
		this.launcher.launch(pythiaCommand);
	}

	// Ready files for executing pythia and run execution
	private void calculate(String tmpDirPath) throws IOException {

		// logger.info(tmpDirPath);
		File f = new File(tmpDirPath);
		f.mkdir();
		// Export Database
		DatabaseFiles databaseFiles = new FactoryImpl().getDatabaseFiles();
		String downloadedFileName = databaseFiles.getFileName(this.experiment.probeMolecules);

		String pythiaResultFileName = tmpDirPath + "pythiaOutput.csv";
		// Runs pythia with specified parameters
		String descriptor = this.experiment.descriptor;
		if (descriptor.equalsIgnoreCase("molprint2d")) {
			descriptor = descriptor + " -sd 2";
		}
		runPythia(downloadedFileName, this.experiment.similarity, this.experiment.activity, descriptor,
				this.experiment.chemblVersion, pythiaResultFileName);

	}

	
	// Parse pythia results. parseResults("./pythiaEmbedded/1153/pythiaOutput.csv")
	private List<MoaEntry> parseResults(String tmpDirPath) throws IOException {
		// Parse csv result file
		Map<String, Float> pythiaResults = new HashMap<String, Float>();
		List<MoaEntry> pythiaResultsOrdered = new ArrayList<MoaEntry>();
		String resultFile = tmpDirPath + "/pythiaOutput.csv";
		logger.info(resultFile);
		boolean firstLine = true;
				
		Float moaScore;
		String smile;
		Reader scanner = new FileReader(resultFile);
		CSVParser records = CSVFormat.EXCEL.parse(scanner);
						
		for (CSVRecord record : records) {			
			if (firstLine) {
				firstLine = false;
				continue;
			} 
			moaScore = Float.parseFloat(record.get(6));
			smile = record.get(7);
			if (!pythiaResults.containsKey(smile)) {
				pythiaResults.put(smile,moaScore);
			}else {
				pythiaResults.put(smile,Math.max(moaScore, pythiaResults.get(smile)));
			}
		}
		scanner.close();
		
		for (Entry<String, Float> entry : pythiaResults.entrySet())
		{
		    MoaEntry moaEntry = new MoaEntry(entry.getKey(), entry.getValue());
		    pythiaResultsOrdered.add(moaEntry);
		}

		Collections.sort(pythiaResultsOrdered);
		logger.info("Size not duplicated: " + pythiaResultsOrdered.size());
		return pythiaResultsOrdered;

	}

	// Generates a smaller version of the ordered set containing filtered results
	private List<MoaEntry> filterResults(List<MoaEntry> pythiaResults) {
		List<MoaEntry> filteredResults = new ArrayList<MoaEntry>();

		WorkflowExperiment we = WorkflowExperiment.findByExperimentId(experimentId);	
		
		switch (we.filter.type) {
		case "percentage":
			filteredResults = filterPythiaPercent(pythiaResults, we.filter.threshold);
			break;
		case "number":
			filteredResults = filterPythiaNumber(pythiaResults, we.filter.threshold);
			break;
		case "threshold":
			filteredResults = filterPythiaThreshold(pythiaResults, we.filter.threshold);
			break;
		default:
			logger.error("filterMolSet : invalid filter type " + we.filter.type);
		}
		return filteredResults;
	}

	private List<MoaEntry> filterPythiaPercent(List<MoaEntry> pythiaResults, float threshold) {
		int limit = Math.min((int) Math.ceil((threshold * pythiaResults.size()) / 100), pythiaResults.size());
		return pythiaResults.subList(0, limit);
	}

	private List<MoaEntry> filterPythiaNumber(List<MoaEntry> pythiaResults, float threshold) {
		int limit = Math.min((int) threshold, pythiaResults.size());
		return pythiaResults.subList(0, limit);
	}

	private List<MoaEntry> filterPythiaThreshold(List<MoaEntry> pythiaResults, float threshold) {
		List<MoaEntry> result = new ArrayList<MoaEntry>();
		for (MoaEntry moa : pythiaResults) {
			if (moa.moaScore >= threshold) {
				result.add(moa);
			}
			// Once moa is smaller that threshold we stop searching, list is ordered for
			// percent and number filters
			else {
				break;
			}
		}
		return result;
	}

	private String generateSmileFile(List<MoaEntry> pythiaResults, String tmpDirPath) throws FileNotFoundException {
		String resultFile = tmpDirPath + "/pythiaOutput.smi";
		logger.info("Smile intermediate file: " + resultFile);
		PrintWriter out = new PrintWriter(resultFile);
		for (MoaEntry moa : pythiaResults) {
			// Smile file includes smiles and mol name splitted by tab, we put smile as mol
			// name (smile twice)
			out.write(moa.smile + "\t" + moa.smile + "\n");
		}
		out.close();
		return resultFile;
	}

	private String convertSmiToSDF3D(String smilePath, String tmpDirPath) {
		String resultFile = tmpDirPath + "/pythiaOutput.sdf";
		FileFormatTranslator translator = this.factory.getFileFormatTranslator();
		translator.convertSmiToSdf3d(smilePath, resultFile);
		return resultFile;

	}

	private void uploadResultDatabase(String SDFPath) {
		WorkflowExperiment we = WorkflowExperiment.findByExperimentId(this.experimentId);
		MoleculeDatabase weDatabase = MoleculeDatabase.findById(we.database.id);
		File structureFile = new File(SDFPath);
		MoleculeDatabaseWorkflowCreatorJob dbUploadJob = (MoleculeDatabaseWorkflowCreatorJob) factory
				.createMoleculeDatabaseWorkflowCreatorJob(weDatabase, structureFile, we);
		dbUploadJob.in(1);
	}

	private void setExperimentFinished() {
		experiment.addInfoEvent("Experiment " + experiment.name + " finished");
		experiment.endDate = Calendar.getInstance().getTimeInMillis();
		experiment.runTime = experiment.endDate - experiment.startingDate;
		experiment.status = ExperimentStatus.FINISHED;
		experiment.save();
	}

}