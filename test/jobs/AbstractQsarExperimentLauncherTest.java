package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarExperimentLauncher;
import jobs.qsar.QsarPCAExperimentLauncher;
import jobs.qsar.RScriptLauncher;

import models.ExperimentStatus;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import visitors.CsvGenerator;
import files.DatabaseFiles;
import files.FileUtils;
import files.formats.csv.QsarExperimentCsvParser;

public class AbstractQsarExperimentLauncherTest extends UnitTest {

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
        if (commandId.equalsIgnoreCase("qsar.pca")) {
            launcher = new QsarPCAExperimentLauncher(experiment.id, factory);
        } else {
            launcher = new QsarExperimentLauncher(experiment.id, factory);
        }
        launcher.setDatabaseFiles(databaseFiles);
        launcher.setFileUtils(fileUtils);
        launcher.setLauncher(rscriptLauncher);
        launcher.setQsarExperimentCsvParser(parser);
        launcher.setMoleculeDatabaseCsvGenerator(databaseCsvGenerator);
        launcher.setCommand(TemplatedConfiguration.get(commandId));
    }

    @Test
    public void changesExperimentStatusToFinished() throws IOException {
        getQsarExperimentLauncher("qsar.pls");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        replay(factory, databaseCsvGenerator, rscriptLauncher, databaseFiles, parser, fileUtils);
        launcher.doJob();
        QsarExperiment experimentFinished = QsarExperiment.findById(experiment.id);
        assertEquals(ExperimentStatus.FINISHED, experimentFinished.status);
    }

    @Test
    public void setsExperimentStartDate() {
        getQsarExperimentLauncher("qsar.pls");
        replay(factory, rscriptLauncher, databaseFiles, parser, fileUtils);
        launcher.doJob();
        assertTrue(experiment.startingDate > (new Date()).getTime() - 1000);
    }

    @Test
    public void createsDirectoryUsingFileUtils() {
        getQsarExperimentLauncher("qsar.pls");
        String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id;
        fileUtils.createDirectory(outputPath);
        replay(factory, rscriptLauncher, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(fileUtils);
    }

    @Test
    public void movesDirectoryGeneratedByRScript() {
        getQsarExperimentLauncher("qsar.pls");
        String sourcePath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id;
        String destinationPath = "outputPath";
        expect(databaseFiles.getPath(experiment)).andReturn(destinationPath).anyTimes();
        fileUtils.copyDirectory(sourcePath, destinationPath);
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));        
        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(fileUtils, databaseFiles);
    }

    @Test
    public void plsCallsParseFileOnParser() {
        getQsarExperimentLauncher("qsar.pls");
        parser.parseFile();
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(parser);
    }

    @Test
    public void pcaCallsParseFileOnEmptyParser() {
        getQsarExperimentLauncher("qsar.pca");
        launcher.setCommand(TemplatedConfiguration.get("qsar.pca"));
        parser.parseFile();
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(parser);
    }
}
