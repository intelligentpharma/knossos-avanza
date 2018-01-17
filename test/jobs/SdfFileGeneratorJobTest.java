package jobs;

import java.io.File;

import javax.persistence.PersistenceException;

import junitx.framework.FileAssert;
import models.MoleculeDatabase;
import models.User;

import org.junit.BeforeClass;
import org.junit.Test;

import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.DatabaseAccess;
import utils.FactoryImpl;
import utils.TemplatedConfiguration;
import utils.database.DatabasePopulationUtils;
import files.DatabaseFiles;
import files.DatabaseFilesImpl;
import files.FileReadingMoleculeParser;
import files.FileUtilsImpl;
import files.formats.sdf.MoleculeParserSDFException;
import files.formats.sdf.MoleculeParserSdf;

public class SdfFileGeneratorJobTest extends UnitTest {

	MoleculeDatabase database;
	User owner;
    DatabaseFiles databaseFiles = new FactoryImpl().getDatabaseFiles();
	FileReadingMoleculeParser parserSdf;

    DatabasePopulationUtils dbUtils = new FactoryImpl().getDatabasePopulationUtils();
    DatabaseAccess dbAccess = new FactoryImpl().createDatabaseAccess();

    private void setup(){
    	Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        
		owner = User.findByUserName("dbermudez");
        parserSdf = new MoleculeParserSdf();
		database = new MoleculeDatabase();
		database.name = "mol30";
		database.transientFile = new File("test-files/test2.sdf");
		database.originalFileName = "test2.sdf";
		database.owner = owner;
		database.save();
		
		File databaseFile = new File(databaseFiles.getFileName(database));
		new FileUtilsImpl().createDirectory(databaseFile.getAbsolutePath());
		databaseFiles.store(database, databaseFile);
		
		Logger.info(database.id+"...");
		try{
        dbUtils.loadMoleculeDatabaseFromFile(database);}
		catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
        dbAccess.insertMoleculesDeploymentsAndPropertiesInDatabase(database);      
    }
    
	@Test
	public void generatesFullSdf() throws InterruptedException{
		/*
		setup();
        SdfFileGeneratorJob job = new SdfFileGeneratorJob(database.id);
        try{
        	job.doJob();
        }catch(PersistenceException e){
        	Logger.info(e.getMessage());
        }
        File generatedSdfFile = new File(TemplatedConfiguration.get("tmp.dir") + "/sdf_" + database.id + "_" + owner.username + ".sdf");
        File expectedSdfFile = new File("test-files/sdfFile_5.sdf");

        FileAssert.assertEquals(expectedSdfFile, generatedSdfFile);
        */
	}

	@Test
	public void generatesSdfWithSelectedDescriptors() throws InterruptedException{
		/*
		setup();
		SdfFileGeneratorJob job = new SdfFileGeneratorJob(database.id);
        String[] descriptors = {"HELIOS"};
        job.setDescriptors(descriptors);
        try{
        	job.doJob();
        }catch(PersistenceException e){
        	Logger.info(e.getMessage());
        }
        File generatedSdfFile = new File(TemplatedConfiguration.get("tmp.dir") + "/sdf_" + database.id + "_" + owner.username + ".sdf");
        File expectedSdfFile = new File("test-files/sdfFile_10.sdf");

        FileAssert.assertEquals(expectedSdfFile, generatedSdfFile);
        */
	}
	
}
