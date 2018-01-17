package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;

import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarExperimentLauncher;
import jobs.qsar.RScriptLauncher;

import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;
import utils.scripts.ExternalScript;
import visitors.CsvGenerator;
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;
import files.DatabaseFiles;
import files.FileUtils;
import files.formats.csv.QsarExperimentCsvParser;

public class QsarExperimentLauncherTest extends UnitTest{
	
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
        launcher = new QsarExperimentLauncher(experiment.id, factory);
        launcher.setDatabaseFiles(databaseFiles);
        launcher.setFileUtils(fileUtils);
        launcher.setLauncher(rscriptLauncher);
        launcher.setQsarExperimentCsvParser(parser);
        launcher.setMoleculeDatabaseCsvGenerator(databaseCsvGenerator);
        launcher.setCommand(TemplatedConfiguration.get(commandId));
    }
    
    @Test
    public void launcherPlsRscript() {
        getQsarExperimentLauncher("qsar.pls");
        String commandTemplate = TemplatedConfiguration.get("qsar.pls");
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        expect(databaseFiles.getPath(experiment)).andReturn("nothing");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");
        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(rscriptLauncher);
    }

    @Test
    public void launcherSparsePlsRscript(){
        getQsarExperimentLauncher("qsar.sparsePls");
        String commandTemplate = TemplatedConfiguration.get("qsar.sparsePls");
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        expect(databaseFiles.getPath(experiment)).andReturn("nothing");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");

        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(rscriptLauncher);    	
    }
    
    @Test
    public void launcherSvmRscript() {
        getQsarExperimentLauncher("qsar.svmRegression");
        String commandTemplate = TemplatedConfiguration.get("qsar.svmRegression");
        launcher.setCommand(commandTemplate);
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        expect(databaseFiles.getPath(experiment)).andReturn("nothing");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");

        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(rscriptLauncher);
    }

    @Test
    public void launcherSvmClassificationRscript() {
        getQsarExperimentLauncher("qsar.svmClassification");
        String commandTemplate = TemplatedConfiguration.get("qsar.svmRegression");
        launcher.setCommand(commandTemplate);
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        expect(databaseFiles.getPath(experiment)).andReturn("nothing");
        expect(databaseCsvGenerator.getCsvFile()).andReturn(new File("./test-files/file.dpf"));
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");

        replay(factory, rscriptLauncher, databaseCsvGenerator, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(rscriptLauncher);
    }
   
}
