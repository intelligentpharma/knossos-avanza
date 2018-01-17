package engine.bidimensional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import jobs.database.crud.MoleculeDatabaseWorkflowCreatorJob;
import models.ComparisonExperiment;
import models.MoleculeDatabase;
import models.WorkflowExperiment;

import org.apache.log4j.Logger;

import play.db.jpa.JPA;
import utils.Factory;
import utils.FactoryImpl;
import utils.bidimensional.Compound;
import utils.bidimensional.FingerprintFrequencyMatrix;
import utils.bidimensional.SmilesComparisonResult;
import files.DatabaseFiles;
import files.FileFormatTranslator;
import files.FileUtilsImpl;

public abstract class AbstractBidimensionalEngine implements BidimensionalEngine{
	public static final Logger logger = Logger.getLogger(AbstractBidimensionalEngine.class);
		
	protected ComparisonExperiment experiment;
	protected DatabaseFiles dbFiles;
	protected FingerprintFrequencyMatrix targetMatrix;
	protected FingerprintFrequencyMatrix probeMatrix;
	protected FileFormatTranslator translator;
	File outputFile;
	List<SmilesComparisonResult> comparisonResults;
	
	
	
	private boolean isWorkflowExperiment(long experimentId){
		String query = "select 1 from  workflowexperiment e where e.experiment_id = "
				+ experimentId;
		logger.debug(query);
		List results = JPA.em().createNativeQuery(query).getResultList();		
		return !results.isEmpty();
	}
	
	


}
