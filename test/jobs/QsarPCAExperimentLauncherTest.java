package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;

import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarPCAExperimentLauncher;
import jobs.qsar.RScriptLauncher;

import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.DatabaseFiles;
import files.FileUtils;
import files.formats.csv.QsarExperimentCsvParser;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import visitors.CsvGenerator;

public class QsarPCAExperimentLauncherTest extends UnitTest{
    TestDataCreator creator;
    QsarExperiment experiment;
    User user;
    Factory factory;
    RScriptLauncher rscriptLauncher;
    DatabaseFiles databaseFiles;
    QsarExperimentCsvParser parser;
    FileUtils fileUtils;
    AbstractQsarExperimentLauncher launcher;
    CsvGenerator databaseCsvGenerator;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("aperreau");
        creator = new TestDataCreator();
        experiment = creator.createQsarExperiment(user);
        factory = createNiceMock(Factory.class);
        rscriptLauncher = createNiceMock(RScriptLauncher.class);
        databaseFiles = createNiceMock(DatabaseFiles.class);
        parser = createNiceMock(QsarExperimentCsvParser.class);
        fileUtils = createNiceMock(FileUtils.class);
        databaseCsvGenerator = createNiceMock(CsvGenerator.class);
    }

    private void getQsarExperimentLauncher(String commandId) {
        launcher = new QsarPCAExperimentLauncher(experiment.id, factory);
        launcher.setDatabaseFiles(databaseFiles);
        launcher.setFileUtils(fileUtils);
        launcher.setLauncher(rscriptLauncher);
        launcher.setQsarExperimentCsvParser(parser);
        launcher.setMoleculeDatabaseCsvGenerator(databaseCsvGenerator);
        launcher.setCommand(TemplatedConfiguration.get("qsar.pca"));
    }
    
    @Test
    public void launcherPcaRscript() throws IOException {
        getQsarExperimentLauncher("qsar.pca");
        String commandTemplate = TemplatedConfiguration.get("qsar.pca");
        launcher.setCommand(commandTemplate);
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        /*String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id;
        String inputFile = TemplatedConfiguration.get("tmp.dir") + "/qsarDatabase" + experiment.id + ".csv";
        String selectedActivities = "null/selectedActivities.csv";
        String command = String.format(commandTemplate, inputFile, experiment.numPropertiesShown, outputPath, databaseFiles.getPath(experiment) + "/selectedDescriptors.csv", 
        		selectedActivities, experiment.scaled);*/
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        replay(factory, rscriptLauncher, databaseFiles, parser, fileUtils, databaseCsvGenerator);
        launcher.doJob();
        verify(rscriptLauncher);
    }


}
