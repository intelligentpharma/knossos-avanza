package files.formats.smiles;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import models.MoleculeDatabase;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.formats.smiles.MoleculeParserSmile;

import play.test.UnitTest;
import utils.Factory;
import utils.bidimensional.FingerprintFrequencyMatrix;

public class MoleculeParserSmileTest extends UnitTest {

	MoleculeParserSmile parser;
	MoleculeDatabase database;

	@Before
	public void setup() {
		parser = new MoleculeParserSmile();
		database = new MoleculeDatabase();
		database.name = "smileDb_Test";
		database.originalFileName = "smileDb_Test.smi";
	}
	
	@Test
	public void parserTypeIsCorrect() {
		assertEquals("SMI", parser.getType());
	}
	
	@Test(expected = RuntimeException.class)
	public void throwExceptionIfFileDoesNotExists(){
		Factory factory = createMock(Factory.class);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(database)).andReturn(new File("test-files/hola.smi"));
		
		replay(factory, dbFiles);
		
		MoleculeParserSmile parser = new MoleculeParserSmile();
		parser.setMoleculeDatabase(database);
		parser.setFactory(factory);
		parser.setDatabaseFiles(dbFiles);
		
		parser.extractSmilesData();
	}
	
	@Test(expected = RuntimeException.class)
	public void throwExceptionIfCanNotCanonize() throws IOException{
		Factory factory = createMock(Factory.class);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		File file = createMock(File.class);
		expect(dbFiles.retrieve(database)).andReturn(new File("test-files/hola.smi"));
		expect(file.getCanonicalPath()).andReturn("test.fukes/hola.smi");
		replay(factory, dbFiles,file);
		
		MoleculeParserSmile parser = new MoleculeParserSmile();
		parser.setMoleculeDatabase(database);
		parser.setFactory(factory);
		parser.setDatabaseFiles(dbFiles);

		parser.parseFileAndLoadMolecules();
	}
	
	@Test
	public void createsLingoFrequencyMatrixWithAllSmiles(){
		Factory factory = createMock(Factory.class);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(database)).andReturn(new File("test-files/easySmileDbUncanonized.smi"));
		FingerprintFrequencyMatrix matrix = createMock(FingerprintFrequencyMatrix.class);
		expect(factory.createFingerprintFrequencyMatrix()).andReturn(matrix);
		
		matrix.addCompound("CC(=O)OC1","uncanonical01");
		matrix.addCompound("CC(C)CC1","uncanonical02");
		matrix.addCompound("CCC","uncanonical03");
		
		replay(factory, matrix, dbFiles);
		
		MoleculeParserSmile parser = new MoleculeParserSmile();
		parser.setMoleculeDatabase(database);
		parser.setFactory(factory);
		parser.setDatabaseFiles(dbFiles);
		
		parser.extractSmilesData();

		assertNotNull(parser.getFingerprintFrequencyMatrix());
		
		verify(factory,matrix);
	}	

	@Test
	public void lingosWithoutNamesHasSmileAsName(){
		Factory factory = createMock(Factory.class);
		DatabaseFiles dbFiles = createMock(DatabaseFiles.class);
		expect(dbFiles.retrieve(database)).andReturn(new File("test-files/easySmileDbUncanonizedWithoutNames.smi"));
		FingerprintFrequencyMatrix matrix = createMock(FingerprintFrequencyMatrix.class);
		expect(factory.createFingerprintFrequencyMatrix()).andReturn(matrix);
		
		matrix.addCompound("CC(=O)OC1","CC(=O)OC1");
		expectLastCall();
		matrix.addCompound("CC(C)CC1","CC(C)CC1");
		expectLastCall();
		matrix.addCompound("CCC","CCC");
		expectLastCall();
		
		replay(factory, matrix, dbFiles);
		
		MoleculeParserSmile parser = new MoleculeParserSmile();
		parser.setMoleculeDatabase(database);
		parser.setFactory(factory);
		parser.setDatabaseFiles(dbFiles);
		
		parser.extractSmilesData();
		
		verify(factory,matrix);
	}	


}

