package visitors;

import java.util.ArrayList;
import java.util.List;

import models.*;

import org.junit.Before;

import org.junit.Test;
import play.Logger;
import play.test.Fixtures;
import utils.experiment.TestDataCreator;

public class QsarResultsJsonGeneratorTest extends FailingMethodsVisitorTest {

    private static TestDataCreator testFactory;
    private static User user;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("aperreau");
        testFactory = new TestDataCreator();

    }

    @Override
    public KnossosVisitor getVisitor() {
        return new QsarResultsJsonGenerator();
    }

    @Override
    public int[] getFailingTypes() {
        int[] failingTypes = {MOLECULE_DATABASE, PHYSICAL_SIMILARITIES, MAPS_SIMILARITIES,
            DEPLOYMENT, SCORING, COMPARISON_EXPERIMENT};
        return failingTypes;
    }

    @Test
    public void generatesSingeExperimentCorrectJson() {
        JsonGenerator jsonGenerator = new QsarResultsJsonGenerator();
        QsarExperiment experiment = testFactory.createQsarExperiment(user);
        
        MoleculeDatabase db = testFactory.createSingleMoleculeDatabase(user);
        experiment.molecules = db;
        
        Deployment deployment = experiment.molecules.getAllDeployments().get(0);
        deployment.name = "ZINC01535869";
        deployment.save();
        long deploymentId = deployment.id;

        QsarResult result1 = new QsarResult();
        result1.experiment = experiment;
        result1.molecule = deployment.name;
        result1.partition = "Partition1";
        result1.experimental = "1.1";
        result1.fittedTrain = "1.2";
        result1.looPrediction = "1.3";
        result1.fittedFull = "1.4";
        QsarResult result2 = new QsarResult();
        result2.experiment = experiment;
        result2.molecule = deployment.name;
        result2.partition = "Partition2";
        result2.experimental = "2.1";
        result2.fittedTrain = "2.2";
        result2.looPrediction = "2.3";
        result2.fittedFull = "2.4";
        experiment.results = new ArrayList<QsarResult>();
        experiment.results.add(result1);
        experiment.results.add(result2);

        experiment.save();
        jsonGenerator.visit(experiment);

        String expectedJson = "[{\"molecule\" : \""+deployment.name+"\", \"partition\" : \"Partition1\", \"experimental\" : \"1.1\""
                + ", \"fittedTrain\" : \"1.2\", \"errorTrain\" : \"0.1\", \"looPrediction\" : \"1.3\", \"errorPrediction\" : \"0.2\", " 
        		+ "\"fittedFull\" : \"1.4\", \"errorFull\" : \"0.3\", \"moleculeId\" : \"" + deploymentId + "\"},"
                + "{\"molecule\" : \""+deployment.name+"\", \"partition\" : \"Partition2\", \"experimental\" : \"2.1\", "
                + "\"fittedTrain\" : \"2.2\", \"errorTrain\" : \"0.1\", \"looPrediction\" : \"2.3\", \"errorPrediction\" : \"0.2\","
                + " \"fittedFull\" : \"2.4\", \"errorFull\" : \"0.3\", \"moleculeId\" : \"" + deploymentId + "\"}]";        
        
        Logger.info(expectedJson);
        Logger.info(jsonGenerator.getJson());
        
        assertEquals(expectedJson, jsonGenerator.getJson());
    }

    @Test
    public void generatesMultiExperimentCorrectJson() {
        JsonGenerator jsonGenerator = new QsarResultsJsonGenerator();
        QsarExperiment experiment = testFactory.createQsarExperiment(user);
        
        MoleculeDatabase db = testFactory.createSingleMoleculeDatabase(user);
        experiment.molecules = db;
        
        Deployment deployment = experiment.molecules.getAllDeployments().get(0);
        deployment.name = "ZINC01535869";
        deployment.save();
        long deploymentId = deployment.id;
        experiment.save();
        
        QsarExperiment childExperiment1 = new QsarExperiment();
        childExperiment1.owner = experiment.owner;
        childExperiment1.name = "Test Children Experiment 1";
        childExperiment1.molecules = db;
        childExperiment1.externalSelectionType = "Random";
        childExperiment1.externalPercentage = 20;
        childExperiment1.parent = experiment.id;
        childExperiment1.save();

        QsarExperiment childExperiment2 = new QsarExperiment();
        childExperiment2.owner = experiment.owner;
        childExperiment2.name = "Test Children Experiment 2";
        childExperiment2.molecules = db;
        childExperiment2.externalSelectionType = "Random";
        childExperiment2.externalPercentage = 20;
        childExperiment2.parent = experiment.id;
        childExperiment2.save();

        QsarResult result1 = new QsarResult();
        result1.experiment = childExperiment1;
        result1.molecule = deployment.name;
        result1.partition = "Partition1";
        result1.experimental = "1.1";
        result1.fittedTrain = "1.2";
        result1.looPrediction = "1.3";
        result1.fittedFull = "1.4";
        childExperiment1.results = new ArrayList<QsarResult>();
        childExperiment1.results.add(result1);
        childExperiment1.save();

        QsarResult result2 = new QsarResult();
        result2.experiment = childExperiment2;
        result2.molecule = deployment.name;
        result2.partition = "Partition2";
        result2.experimental = "2.1";
        result2.fittedTrain = "2.2";
        result2.looPrediction = "2.3";
        result2.fittedFull = "2.4";
        childExperiment2.results = new ArrayList<QsarResult>();
        childExperiment2.results.add(result2);
        childExperiment2.save();

        jsonGenerator.visit(experiment);

        String expectedJson = "[{\"molecule\" : \""+deployment.name+"\"," +
        		" \"partition\" : \"Partition1||Partition2\"," +
        		" \"experimental\" : \"1.1||2.1\"," +
                " \"fittedTrain\" : \"1.2||2.2\", \"errorTrain\" : \"0.1||0.1\"," +
                " \"looPrediction\" : \"1.3||2.3\", \"errorPrediction\" : \"0.2||0.2\"," + 
        		" \"fittedFull\" : \"1.4||2.4\", \"errorFull\" : \"0.3||0.3\"," +
        		" \"moleculeId\" : \"" + deploymentId + "\"}]";        
        
        Logger.info(expectedJson);
        Logger.info(jsonGenerator.getJson());
        
        assertEquals(expectedJson, jsonGenerator.getJson());
    }

    
    @Test
    public void emptyExperimentHasEmptyJson() {
        JsonGenerator jsonGenerator = new QsarResultsJsonGenerator();
        QsarExperiment experiment = new QsarExperiment();
        experiment.results = new ArrayList<QsarResult>();

        jsonGenerator.visit(experiment);

        assertEquals("[]", jsonGenerator.getJson());
    }
}
