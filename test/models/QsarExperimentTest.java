package models;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.List;

import junitx.framework.FileAssert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import visitors.KnossosVisitor;
import files.DatabaseFiles;
import utils.Factory;
import utils.TemplatedConfiguration;
import utils.experiment.TestDataCreator;

public class QsarExperimentTest extends UnitTest {

    private User owner;
    private TestDataCreator dataCreator;
    private QsarExperiment experiment;
    private String outputPath;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        owner = User.findByUserName("test");
        dataCreator = new TestDataCreator();
        experiment = dataCreator.createQsarExperiment(owner);
        outputPath = TemplatedConfiguration.get("tmp.dir");
    }

    @Test
    public void findsAllQsarExperiments() {
        List<QsarExperiment> experiments = QsarExperiment.findAllOwnedBy(owner);
        assertEquals(1, experiments.size());
    }

    @Test
    public void findsAllParentQsarExperiments() {
        List<QsarExperiment> experiments = QsarExperiment.findAllParentsOwnedBy(owner);
        assertEquals(1, experiments.size());
    }

    @Test
    public void findsAllChildsExperiementFromParentId() {
        List<QsarExperiment> experiments = QsarExperiment.findAllParentsOwnedBy(owner);
        QsarExperiment childExperiment1 = new QsarExperiment();
        childExperiment1.owner = owner;
        childExperiment1.name = "Test Children Experiment";
        childExperiment1.molecules = dataCreator.createSmallDatabaseWithoutProperties(owner);
        childExperiment1.externalSelectionType = "Random";
        childExperiment1.externalPercentage = 20;
        childExperiment1.parent = experiments.get(0).id;
        childExperiment1.save();

        QsarExperiment childExperiment2 = new QsarExperiment();
        childExperiment2.owner = owner;
        childExperiment2.name = "Test Children Experiment 2";
        childExperiment2.molecules = dataCreator.createSmallDatabaseWithoutProperties(owner);
        childExperiment2.externalSelectionType = "Random";
        childExperiment2.externalPercentage = 20;
        childExperiment2.parent = experiments.get(0).id;
        childExperiment2.save();

        List<QsarExperiment> childrenExperiments = QsarExperiment.findChildrenExperimentsFromParentExperiment(experiments.get(0));

        assertEquals(2, childrenExperiments.size());
    }

    @Test
    public void callsTheVisitorCorrectly() {
        QsarExperiment experiment = new QsarExperiment();
        KnossosVisitor visitor = createMock(KnossosVisitor.class);
        visitor.visit(experiment);

        replay(visitor);
        experiment.acceptVisitor(visitor);
        verify(visitor);
    }

    @Test
    public void generatesSelectedPartitionsFileCorrectly() {
        FileUtils.deleteQuietly(new File(outputPath + "/selectedPartitions.csv"));

        String selectedPartitions = "2142571,-Structure50_i001_c003,Training:2142559,-Structure50_i001_c002,Training:2142547,-Structure50_i001_c001,External:2142534,-Structure49_i001_c005,External:2142522,-Structure49_i001_c004,None";
        DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
        expect(databaseFiles.getPath(experiment)).andReturn(outputPath);

        replay(databaseFiles);

        experiment.writeSelectedPartitionsFile(selectedPartitions, databaseFiles);

        FileAssert.assertEquals(new File("test-files/qsar/selectedPartitions.csv"), new File(outputPath + "/selectedPartitions.csv"));

        verify(databaseFiles);
    }

    @Test
    public void generatesEmptySelectedPartitionsFile() {
        FileUtils.deleteQuietly(new File(outputPath + "/selectedPartitions.csv"));

        String selectedPartitions = "";
        DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
        //Don't expect any call on databaseFiles!

        replay(databaseFiles);

        experiment.writeSelectedPartitionsFile(selectedPartitions, databaseFiles);

        assertEquals(0, new File(outputPath + "/selectedPartitions.csv").length());

        verify(databaseFiles);
    }

    @Test
    public void generatesSelectedDescriptorsFileCorrectly() {
        FileUtils.deleteQuietly(new File(outputPath + "/selectedDescriptors.csv"));

        String[] selectedDescriptors = {"GCUT_SLOGP_0", "PEOE_VSA-4", "Q_VSA_HYD", "SMR_VSA1", "a_aro", "a_nC", "b_rotN", "deploymentOrder", "moleculeOrder", "mr", "vsa_pol"};

        DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
        expect(databaseFiles.getPath(experiment)).andReturn(outputPath);

        replay(databaseFiles);

        experiment.writeSelectedDescriptorsFile(selectedDescriptors, databaseFiles, "selectedDescriptors");

        FileAssert.assertEquals(new File("test-files/qsar/selectedDescriptors.csv"), new File(outputPath + "/selectedDescriptors.csv"));

        verify(databaseFiles);
    }

    @Test
    public void generatesEmptySelectedDescriptorsFile() {
        FileUtils.deleteQuietly(new File(outputPath + "/selectedDescriptors.csv"));

        String[] selectedDescriptors = new String[0];
        DatabaseFiles databaseFiles = createMock(DatabaseFiles.class);
        //Don't expect any call on databaseFiles!

        replay(databaseFiles);

        experiment.writeSelectedDescriptorsFile(selectedDescriptors, databaseFiles, "selectedDescriptors");

        assertEquals(0, new File(outputPath + "/selectedDescriptors.csv").length());

        verify(databaseFiles);
    }

    @Test
    public void findChildrenQsarExperimentsFromParentExperimentId() {
        dataCreator.createChildQsarExperiment(experiment.id, owner);
        dataCreator.createChildQsarExperiment(experiment.id, owner);
        assertEquals(2, QsarExperiment.findChildrenExperimentsFromParentExperiment(experiment).size());
    }

    @Test
    public void listAllUsingWorks() {
        List<QsarExperiment> experiments = QsarExperiment.findAllOwnedBy(owner);
        MoleculeDatabase database = experiments.get(0).molecules;        
        List<String> experimentsIds = QsarExperiment.findAllUsingIds(database);
        assertEquals(1, experimentsIds.size());

    }
    
    @Test
    public void getFingerprintsUsed(){
    	experiment.hasECFP = true;
    	experiment.hasECFPVariant = true;
    	experiment.hasMolprint2D = true;
    	String fingerprintUsed = experiment.getFingerprintsUsedToString();
    	
    	assertEquals("ECFP, ECFP Variant, MOLPRINT2D", fingerprintUsed);
    }
    
}
