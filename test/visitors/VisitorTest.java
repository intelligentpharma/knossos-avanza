package visitors;

import models.ComparisonExperiment;
import models.Deployment;
import models.MapsSimilarities;
import models.MoleculeDatabase;
import models.PhysicalSimilarities;
import models.QsarExperiment;
import models.QsarResult;
import models.Scoring;

import org.easymock.EasyMock;
import org.junit.Test;

import play.test.UnitTest;

public class VisitorTest extends UnitTest {

	@Test
	public void experimentCallsVisitorCorrectly(){
		ComparisonExperiment experiment = new ComparisonExperiment();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(experiment);
		EasyMock.replay(visitor);
		
		experiment.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	@Test
	public void deploymentCallsVisitorCorrectly(){
		Deployment deployment = new Deployment();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(deployment);
		EasyMock.replay(visitor);
		
		deployment.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	@Test
	public void mapsSimilarityCallsVisitorCorrectly(){
		MapsSimilarities maps = new MapsSimilarities();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(maps);
		EasyMock.replay(visitor);
		
		maps.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	@Test
	public void physicalSimilaritiesCallsVisitorCorrectly(){
		PhysicalSimilarities sim = new PhysicalSimilarities();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(sim);
		EasyMock.replay(visitor);
		
		sim.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	@Test
	public void scoringCallsVisitorCorrectly(){
		Scoring scoring = new Scoring();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(scoring);
		EasyMock.replay(visitor);
		
		scoring.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	@Test
	public void moleculeDatatabaseCallsVisitorCorrectly(){
		MoleculeDatabase moleculeDatabase = new MoleculeDatabase();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(moleculeDatabase);
		EasyMock.replay(visitor);
		
		moleculeDatabase.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	public void qsarExperimentCallsVisitorCorrectly(){
		QsarExperiment experiment = new QsarExperiment();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(experiment);
		EasyMock.replay(visitor);
		
		experiment.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

	public void qsarResultCallsVisitorCorrectly(){
		QsarResult result = new QsarResult();
		KnossosVisitor visitor = EasyMock.createMock(KnossosVisitor.class);
		visitor.visit(result);
		EasyMock.replay(visitor);
		
		result.acceptVisitor(visitor);
		
		EasyMock.verify(visitor);
	}

}
