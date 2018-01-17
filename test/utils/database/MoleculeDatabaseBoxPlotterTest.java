package utils.database;

import jobs.qsar.RScriptLauncher;
import models.MoleculeDatabase;

import static org.easymock.EasyMock.*;
import org.junit.Test;

import play.test.UnitTest;
import utils.database.MoleculeDatabaseBoxPlotter;
import utils.database.MoleculeDatabaseBoxPlotterImpl;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import visitors.CsvGenerator;
import visitors.MoleculeDatabaseCsvGenerator;

public class MoleculeDatabaseBoxPlotterTest extends UnitTest {

	@Test
	public void boxPlotterBehavesCorrectly(){
		TestDataCreator dataCreator = new TestDataCreator();
		MoleculeDatabase database = dataCreator.createSingleDeploymentDatabaseWithActivity();
		String username = "dbermudez";
		String activityColumn = "activity";
		String[] selectedDescriptors = {"cluster"};

		CsvGenerator generator = createMock(CsvGenerator.class);

		generator.visit(database);
		expect(generator.getPropertyPosition(activityColumn, database)).andReturn(1);
		expect(generator.getCsv()).andReturn("1,2,3,4,5,6");
		
		ExternalScript launcher = createMock(ExternalScript.class);
		
		String command="/usr/local/bin/R --vanilla --slave -f ./scripts/moleculeDBBoxPlot.R --args ./scripts /tmp/prueba_null.csv 1 /tmp/selectedDescriptors_null_dbermudez.csv /tmp/boxPlot_null_dbermudez.pdf";
		expect(launcher.launch(command)).andReturn("");
		
		MoleculeDatabaseBoxPlotter databaseBoxPlotter = new MoleculeDatabaseBoxPlotterImpl();
		databaseBoxPlotter.setGenerator(generator);
		databaseBoxPlotter.setLauncher(launcher);
	
		databaseBoxPlotter.setDatabase(database);
		databaseBoxPlotter.setUsername(username);
		databaseBoxPlotter.setActivityColumn(activityColumn);
		databaseBoxPlotter.setSelectedDescriptors(selectedDescriptors);
		
		replay(generator, launcher);
		
		databaseBoxPlotter.generateBoxPlotPDF();
			
		verify(generator, launcher);
	}
}