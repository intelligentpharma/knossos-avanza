package files.formats.pdbqt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
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
import files.formats.pdbqt.AlignmentToPdbqtFileGenerator;

import play.Logger;
import play.test.UnitTest;

public class AlignmentToPdbqtFileGeneratorTest extends UnitTest{
	
	@Before
	public void setup(){
		
	}
	
	@Test(expected = RuntimeException.class)
	public void throwExceptionWhenFileDoesNotExist(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.name = "testName_mol12";
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.scorings = new ArrayList<Scoring>();
		
		FileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/hola.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);		
		replay(dbFiles);
	
		pdbqtParser.parse(alignment);
	}

	@Test
	public void generatesPdbqtFromInverseDockingAlignmentWithoutScorings(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.name = "testName_mol12";
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.name="moleculeProbe";
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.targetDeployment.molecule = new Molecule();
		alignment.targetDeployment.molecule.name="moleculeTarget";
		alignment.scorings = new ArrayList<Scoring>();

		FileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);
		
		replay(dbFiles);
		
		pdbqtParser.parse(alignment);

		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());

		File trueTempFile = new File("test-files/mol12_model1_out.pdbqt");
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}
	
	@Test
	public void generatesPdbqtFromInverseDockingAlignmentWithScorings(){
		MapsSimilarities alignment = new MapsSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.name = "testName_mol12";
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.targetDeployment.molecule = new Molecule();
		alignment.targetDeployment.molecule.name = "molecule1";
		alignment.scorings = new ArrayList<Scoring>();
		Scoring scoring = new Scoring();
		scoring.ponderation = new Ponderation();
		scoring.ponderation.name = "inverseAD";
		scoring.score = 12.2;
		alignment.add(scoring);

		FileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);

		replay(dbFiles);
		
		pdbqtParser.parse(alignment);
		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());
		File trueTempFile = new File("test-files/mol12_inverseAD_out.pdbqt");		
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}
	
	@Test
	public void generatesUniquePdbqtFromDockingAlignment(){
		PhysicalSimilarities alignment = new PhysicalSimilarities();
		alignment.probeDeployment = new Deployment();
		alignment.probeDeployment.molecule = new Molecule();
		alignment.probeDeployment.molecule.name = "probeName";
		alignment.probeDeployment.molecule.database = new MoleculeDatabase();
		alignment.probeDeployment.putProperty("PROPR1", "value1");
		alignment.targetDeployment = new Deployment();
		alignment.targetDeployment.name = "testName_mol11";
		alignment.targetDeployment.molecule = new Molecule();
		alignment.targetDeployment.molecule.name = "targetName";
		alignment.energy = 12.2;
		alignment.entropy = 23.3;
		alignment.vinaEnergy = 34.4;
		
		FileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		pdbqtParser.setUnique();
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(alignment)).andReturn(new File("test-files/mol12_out.pdbqt"));
		pdbqtParser.setDatabaseFiles(dbFiles);

		replay(dbFiles);
		
		pdbqtParser.parse(alignment);
		File outputPdbqtFile = pdbqtParser.getOutputFile();
		new FileUtilsImpl().replaceEquivalencesInFile(outputPdbqtFile.getPath(), pdbqtParser.getPonderationEquivalenceMap());
		File trueTempFile = new File("test-files/mol12_selene_unique_out.pdbqt");
		FileAssert.assertEquals(trueTempFile, outputPdbqtFile);
	}

	@Test
	public void hasModelTagDetectsModelTag(){
		AlignmentToPdbqtFileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		File inputPdbqtFile = new File("test-files/mol12_out.pdbqt");
		try {
			boolean hasModelTag = pdbqtParser.hasModelTag(inputPdbqtFile);
			assertTrue(hasModelTag);
		} catch (IOException e) {
			fail();
		}
	}
	
	@Test
	public void hasModelTagDetectsNoModelTag(){
		AlignmentToPdbqtFileGenerator pdbqtParser = new AlignmentToPdbqtFileGenerator();
		File inputPdbqtFile = new File("test-files/mol12_out_withoutModelTag.pdbqt");
		try {
			boolean hasModelTag = pdbqtParser.hasModelTag(inputPdbqtFile);
			assertFalse(hasModelTag);
		} catch (IOException e) {
			fail();
		}
	}
	
}