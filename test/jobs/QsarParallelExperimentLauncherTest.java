package jobs;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import jobs.qsar.AbstractQsarExperimentLauncher;
import jobs.qsar.QsarParallelExperimentLauncher;
import jobs.qsar.RScriptLauncher;
import models.QsarExperiment;
import models.User;

import org.junit.Before;
import org.junit.Test;

import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;

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

public class QsarParallelExperimentLauncherTest{
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
        launcher = new QsarParallelExperimentLauncher(experiment.id, factory);
        launcher.setDatabaseFiles(databaseFiles);
        launcher.setFileUtils(fileUtils);
        launcher.setLauncher(rscriptLauncher);
        launcher.setQsarExperimentCsvParser(parser);
        launcher.setMoleculeDatabaseCsvGenerator(databaseCsvGenerator);
        launcher.setCommand(TemplatedConfiguration.get(commandId));
    }
    
    @Test
    public void launcherParallelPlsRscript() {
        getQsarExperimentLauncher("qsar.plsPreparation");
        String commandTemplate = TemplatedConfiguration.get("qsar.plsPreparation");
        String databaseFile = "database file";
        expect(databaseFiles.getFileName(experiment.molecules)).andReturn(databaseFile);
        /*String activityColumn = experiment.activityProperty;
        String outputPath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.id;
        String inputFile = TemplatedConfiguration.get("tmp.dir") + "/qsarDatabase" + experiment.id + ".csv";
        int percentage = 20;
        String inputDatabasePath = TemplatedConfiguration.get("tmp.dir") + "/" + experiment.molecules.id + ".input";
        String command = String.format(commandTemplate, inputFile, activityColumn, outputPath, percentage, databaseFiles.getPath(experiment) + "/selectedDescriptors.csv", 
        		experiment.numberOfLatentValues, experiment.valuesParameter, inputDatabasePath, experiment.scaled, experiment.searchDepth+"",
        		AtomLabelType.values()[experiment.atomLabelType], experiment.validationType);*/
        expect(rscriptLauncher.launchRScript(commandTemplate)).andReturn("nothing");

        replay(factory, rscriptLauncher, databaseFiles, parser, fileUtils);
        launcher.doJob();
        verify(rscriptLauncher);
    }


}
