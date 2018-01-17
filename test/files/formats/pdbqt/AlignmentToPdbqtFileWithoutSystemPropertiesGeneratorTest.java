package files.formats.pdbqt;

import java.io.File;
import java.util.ArrayList;

import junitx.framework.FileAssert;
import models.Deployment;
import models.MapsSimilarities;
import models.Molecule;
import models.MoleculeDatabase;
import models.PhysicalSimilarities;
import models.Ponderation;
import models.Scoring;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.FileGenerator;
import files.FileUtilsImpl;
import files.formats.pdbqt.AlignmentToPdbqtFileWithoutSystemPropertiesGenerator;

import static org.easymock.EasyMock.*;

import play.Logger;
import play.test.UnitTest;

public class AlignmentToPdbqtFileWithoutSystemPropertiesGeneratorTest extends UnitTest{
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void generatesPdbqtFromInverseDockingAlignmentWithoutScorings(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.name = "testName_mol12";
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("DeploymentOrder", "1");
		alignment.probeDeployment.putProperty("MoleculeOrder", "1");
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.scorings = new ArrayList<Scoring>();

		FileGenerator pdbqtParser = new AlignmentToPdbqtFileWithoutSystemPropertiesGenerator();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);
		
		replay(dbFiles);
		
		pdbqtParser.parse(alignment);

		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());

		File trueTempFile = new File("test-files/mol12_withoutSystemProperties_out.pdbqt");
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}
	
	@Test
	public void generatesPdbqtFromInverseDockingAlignmentWithScorings(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.name = "testName_mol12";
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("DeploymentOrder", "1");
		alignment.probeDeployment.putProperty("MoleculeOrder", "1");
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.scorings = new ArrayList<Scoring>();
		Scoring scoring = new Scoring();
		scoring.ponderation = new Ponderation();
		scoring.ponderation.name = "DEFAULT";
		scoring.score = 12.2;
		alignment.add(scoring);

		FileGenerator pdbqtParser = new AlignmentToPdbqtFileWithoutSystemPropertiesGenerator();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);

		replay(dbFiles);
		
		pdbqtParser.parse(alignment);
		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());
		File trueTempFile = new File("test-files/mol12_withoutSystemProperties_inverseAD.pdbqt");
		Logger.info("Compare " + trueTempFile.getAbsolutePath() + " and " + outputPdbqtFile.getAbsolutePath());
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}
	
	@Test
	public void generatesUniquePdbqtFromDockingAlignment(){
		PhysicalSimilarities alignment = new PhysicalSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.name = "probeName";
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("DeploymentOrder", "1");
		alignment.probeDeployment.putProperty("MoleculeOrder", "1");
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.targetDeployment.molecule = new Molecule();
		alignment.targetDeployment.molecule.name = "targetName";
		alignment.energy = 12.2;
		alignment.entropy = 23.3;
		alignment.vinaEnergy = 34.4;
		
		FileGenerator pdbqtParser = new AlignmentToPdbqtFileWithoutSystemPropertiesGenerator();
		pdbqtParser.setUnique();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);

		replay(dbFiles);
		
		pdbqtParser.parse(alignment);
		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());
		File trueTempFile = new File("test-files/mol12_withoutSystemProperties_selene_unique_out.pdbqt");
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}

}
