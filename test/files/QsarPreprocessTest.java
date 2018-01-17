package files;

import java.io.File;

import jobs.qsar.QsarPreprocessJob;
import junitx.framework.FileAssert;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import utils.TemplatedConfiguration;
import utils.scripts.ExternalScript;
import utils.scripts.ExternalScriptViaCommandLine;

public class QsarPreprocessTest extends UnitTest {

    QsarPreprocessJob preprocessor;
    String outputFilePath;
    String commandTemplate;
    TemplatedConfiguration configuration = new TemplatedConfiguration();
    ExternalScript launcher;
    String notToRemoveEmpty = "test-files/qsar/preprocess/notToRemoveEmpty.csv";
    String notToRemoveC = "test-files/qsar/preprocess/notToRemoveC.csv";
    String notToRemoveAC = "test-files/qsar/preprocess/notToRemoveAC.csv";
    String notToRemoveA = "test-files/qsar/preprocess/notToRemoveA.csv";
    String notToCorrelate = "test-files/qsar/preprocess/notToCorrelate.csv";

    @Before
    public void setup() {
        preprocessor = new QsarPreprocessJob(0);
        commandTemplate = TemplatedConfiguration.get("qsarPreprocess.pls");
        outputFilePath = configuration.get("tmp.dir") + "/qsarPreprocessOutputTest.csv";
        launcher = new ExternalScriptViaCommandLine();
    }

    @Test
    public void constantPropertyIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsConstantProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 20, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void constantPropertyWithMissingsIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsConstantPropWithNA.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 20, 100, 1.9);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void emptyPropertyOverColumnThresholdIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsEmptyProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 20, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void emptyPropertyUnderColumnThresholdIsNotDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsEmptyProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 90, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsEmptyPropNas.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void allowedCorrelatedPropertyOverThresholdIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsNotSoCorrelatedProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 100, 100, 0.9);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void allowedCorrelatedPropertyWith100PercentCorrelationDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 100, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void allowedCorrelatedPropertyUnderThresholdIsNotDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsNotSoCorrelatedProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 100, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsNotSoCorrelatedProp.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void notAllowedCorrelatedPropertyIsNotDeletedAndThePairIs() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv", outputFilePath, notToRemoveEmpty, notToCorrelate, 100, 100, 0.99);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));

        command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 100, 0.99);
        launcher.launch(command);
        trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsWithC.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));

        command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv", outputFilePath, notToRemoveA, notToCorrelate, 100, 100, 0.99);
        launcher.launch(command);
        trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));

        command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsCorrelatedProp.csv", outputFilePath, notToRemoveAC, notToCorrelate, 100, 100, 0.99);
        launcher.launch(command);
        trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsWithAC.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

//Row operations
    @Test
    public void moleculeWithMissingPropertyOverThresholdIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsMissingProp.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 10, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarOutputMissingProp1.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void moleculeWithMissingPropertyUnderThresholdIsNotDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsMissingProp.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 80, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarOutputMissingProp2.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void moleculeWithAllNAWithAllNAPropertyNotToRemoveIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsAllNAProp.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 0, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptorsMolecleNAAllNAProp.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void duplicatedMoleculeNameIsDeleted() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsDuplicatedEntry.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File("test-files/qsar/preprocess/qsarDescriptors.csv");
        FileAssert.assertEquals(trueTempFile, new File(outputFilePath));
    }

    @Test
    public void createAndStoreLogFile() {
        String command = String.format(commandTemplate, "test-files/qsar/preprocess/qsarDescriptorsAllNAProp.csv", outputFilePath, notToRemoveC, notToCorrelate, 100, 100, 1);
        launcher.launch(command);
        File trueTempFile = new File(outputFilePath + ".log");
        assertTrue(trueTempFile.exists());
    }
}
