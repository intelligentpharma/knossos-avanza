package jobs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import jobs.database.crud.MoleculeDatabaseUploaderJob;

import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.formats.sdf.MoleculeParserSDFException;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.DatabaseAccess;
import utils.EventWriter;
import utils.database.DatabasePopulationUtils;

public class MoleculeDatabaseUploaderJobTest extends UnitTest {
	
	MoleculeDatabase molecules;
	MoleculeDatabaseUploaderJob job;
	DatabasePopulationUtils dbUtils;
	DatabaseAccess dbAccess;        
	EventWriter eventWriter;
	
	@Before
	public void setUp(){
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
		job = new MoleculeDatabaseUploaderJob();
		dbUtils = createMock(DatabasePopulationUtils.class);
		job.setDatabasePopulationUtils(dbUtils);
		dbAccess = createMock(DatabaseAccess.class);
		job.setDatabaseAccess(dbAccess);
		User user = User.findByUserName("xarroyo");
		List<MoleculeDatabase> list = MoleculeDatabase.findAllOwnedBy(user);
		molecules = list.get(0); 
		job.setMolecules(molecules);
	}

	@Test
	public void loadsMoleculesFromFile(){
		try{
		dbUtils.loadMoleculeDatabaseFromFile(molecules);
		
		replay(dbUtils);
		job.doJob();
		verify(dbUtils);
		}
		catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
	}
	
	@Test
	public void insertsMoleculeDatabase(){
		dbAccess.insertMoleculesDeploymentsAndPropertiesInDatabase(molecules);
		
		replay(dbAccess);
		job.doJob();
		verify(dbAccess);
	}
}
