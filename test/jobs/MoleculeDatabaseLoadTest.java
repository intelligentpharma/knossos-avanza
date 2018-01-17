package jobs;

import jobs.database.crud.MoleculeDatabaseLoad;
import models.MoleculeDatabase;
import models.User;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import files.formats.sdf.MoleculeParserSDFException;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.database.DatabasePopulationUtils;

public class MoleculeDatabaseLoadTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}

	@Test
	public void populationObjectIsCalledCorrectly(){
		User owner = User.findByUserName("aperreau");
		MoleculeDatabase molecules = MoleculeDatabase.findAllOwnedBy(owner).get(0);
		
		DatabasePopulationUtils utils = EasyMock.createMock(DatabasePopulationUtils.class);
		try{
		utils.loadMoleculeDatabaseFromFile(molecules);
		}
		catch (MoleculeParserSDFException E){
			//Do nothing, loaded DBs will not have non-allowed chats
		}
		
		Factory factory = EasyMock.createMock(Factory.class);
		EasyMock.expect(factory.getDatabasePopulationUtils()).andReturn(utils);

		MoleculeDatabaseLoad loader = new MoleculeDatabaseLoad(molecules.id, factory);
		
		EasyMock.replay(factory, utils);
		loader.doJob();
		EasyMock.verify(factory, utils);
		
		molecules.delete();
	}

}
