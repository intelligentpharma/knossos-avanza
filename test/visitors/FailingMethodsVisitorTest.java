package visitors;

import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.MoleculeDatabase;
import models.PhysicalSimilarities;
import models.QsarExperiment;
import models.QsarResult;
import models.Scoring;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public abstract class FailingMethodsVisitorTest extends UnitTest {
	
	public abstract KnossosVisitor getVisitor();
	
	public abstract int[] getFailingTypes();
	
	public final static int COMPARISON_EXPERIMENT = 1;
	public final static int PHYSICAL_SIMILARITIES = 2;
	public final static int MAPS_SIMILARITIES = 3;
	public final static int DEPLOYMENT = 4;
	public final static int SCORING = 5;
	public final static int MOLECULE_DATABASE = 6;
	public final static int QSAR_EXPERIMENT = 7;
	public final static int QSAR_RESULT = 8;
	
	@Test
	public void notImplementedMethodsShouldThrowException(){
		int[] failingTypes = getFailingTypes();
		for(int failingType : failingTypes){
			Logger.debug("Checking visiting %s fails", failingType);
			checkFailingType(failingType);
		}
	}

	private void checkFailingType(int failingType) {
		KnossosVisitor visitor = getVisitor();
		try{
			switch(failingType){
			case COMPARISON_EXPERIMENT:
				visitor.visit(new ComparisonExperiment());
				fail("Visiting a comparison experiment should throw exception");
			case PHYSICAL_SIMILARITIES:
				visitor.visit(new PhysicalSimilarities());
				fail("Visiting a physical similarity should throw exception");
			case MAPS_SIMILARITIES:
				visitor.visit(new MapsSimilarities());
				fail("Visiting a maps similarity should throw exception");
			case DEPLOYMENT:
				visitor.visit(new Deployment());
				fail("Visiting a deployment should throw exception");
			case SCORING:
				visitor.visit(new Scoring());
				fail("Visiting a scoring should throw exception");
			case MOLECULE_DATABASE:
				visitor.visit(new MoleculeDatabase());
				fail("Visiting a molecule database should throw exception");
			case QSAR_EXPERIMENT:
				visitor.visit(new QsarExperiment());
				fail("Visiting a qsar experiment should throw exception");
			case QSAR_RESULT:
				visitor.visit(new QsarResult());
				fail("Visiting a qsar result should throw exception");
			default:
				fail("Wrong model type code");
			}
		}catch(VisitingUnsupportedModelException e){
			Logger.debug("Failed!!");
			//It worked correctly
		}
	}
	
	
	
}
