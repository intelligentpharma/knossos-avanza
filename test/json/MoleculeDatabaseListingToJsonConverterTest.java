package json;

import java.util.ArrayList;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;

import models.*;

import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.Fixtures;

import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class MoleculeDatabaseListingToJsonConverterTest extends UnitTest {

    MoleculeDatabaseListingToJsonConverter converter;
    JsonConverter propertyListConverter;
    JsonConverter experimentListConverter;
    TestDataCreator creator;
    private static User owner;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        converter = new MoleculeDatabaseListingToJsonConverter();
        propertyListConverter = createMock(JsonConverter.class);
        experimentListConverter = createMock(JsonConverter.class);
        converter.setPropertyListConverter(propertyListConverter);
        converter.setExperimentListConverter(experimentListConverter);
        creator = new TestDataCreator();
        owner = User.findByUserName("lnavarro");
    }

    @Test
    public void emptyMoleculeDatabaseToJson() {
        converter.setData(new MoleculeDatabase());
        propertyListConverter.setData(anyObject(List.class));
        experimentListConverter.setData(anyObject(List.class));
        expect(propertyListConverter.getJson()).andReturn("[]");
        replay(propertyListConverter);
        expect(experimentListConverter.getJson()).andReturn("[]");
        replay(experimentListConverter);

        String expectedJson = "{\"id\":\"null\","
                + "\"name\":\"null\","
                + "\"originalFileName\":\"null\","
                + "\"counterIonsRemoved\":\"false\","
                + "\"calculatedProperties\":\"false\","
                + "\"calculatedLigandEfficiency\":\"false\","
                + "\"calculatedDescriptors2D\":\"false\","
                + "\"calculatedDescriptors3D\":\"false\","
                + "\"status\":\"Available\","
                + "\"numMolecules\":\"0\","
                + "\"numDeployments\":\"0\","
                + "\"owner\":\"null\","
                + "\"experimentsUsing\":[],"
                + "\"properties\":[]}";
        assertEquals(expectedJson, converter.getJson());

        verify(propertyListConverter);
        verify(experimentListConverter);
    }

    @Test
    public void moleculeDatabaseWithFilledValuesToJson() {
        MoleculeDatabase molDB = simpleMoleculeDatabase();
        converter.setData(molDB);
        propertyListConverter.setData(anyObject(List.class));
        experimentListConverter.setData(anyObject(List.class));
        expect(propertyListConverter.getJson()).andReturn("[]");
        replay(propertyListConverter);
        expect(experimentListConverter.getJson()).andReturn("[]");
        replay(experimentListConverter);

        String expectedJson = "{\"id\":\"12\","
                + "\"name\":\"database\","
                + "\"originalFileName\":\"file.sdf\","
                + "\"counterIonsRemoved\":\"true\","
                + "\"calculatedProperties\":\"false\","
                + "\"calculatedLigandEfficiency\":\"false\","
                + "\"calculatedDescriptors2D\":\"false\","
                + "\"calculatedDescriptors3D\":\"false\","
                + "\"status\":\"Available\","
                + "\"numMolecules\":\"0\","
                + "\"numDeployments\":\"0\","
                + "\"owner\":\"lnavarro\","
                + "\"experimentsUsing\":[],"
                + "\"properties\":[]}";
        assertEquals(expectedJson, converter.getJson());

        verify(propertyListConverter);
        verify(experimentListConverter);
    }

    private MoleculeDatabase simpleMoleculeDatabase() {
        MoleculeDatabase molDB = new MoleculeDatabase();
        molDB.owner = owner;
        molDB.id = 12L;
        molDB.name = "database";
        molDB.originalFileName = "file.sdf";
        molDB.counterIonsRemoved = true;
        molDB.calculatedProperties = false;
        molDB.calculatedLigandEfficiency = false;
        molDB.status = MoleculeDatabaseStatus.AVAILABLE;
        return molDB;
    }

    @Test
    public void moleculeDatabaseWithFilledValuesAndPropertiesToJson() {
        MoleculeDatabase molDB = simpleMoleculeDatabase();
        Molecule molecule = new Molecule();
        molecule.database = molDB;
        Deployment deployment = new Deployment();
        deployment.putProperty("prop1", "value1");
        deployment.putProperty("prop2", "value1");
        molecule.addDeployment(deployment);
        molDB.addMolecule(molecule);
        converter.setData(molDB);
        propertyListConverter.setData(anyObject(List.class));
        experimentListConverter.setData(anyObject(List.class));
        expect(propertyListConverter.getJson()).andReturn("[\"prop1\",\"prop2\"]");
        replay(propertyListConverter);
        expect(experimentListConverter.getJson()).andReturn("[]");
        replay(experimentListConverter);

        String expectedJson = "{\"id\":\"12\","
                + "\"name\":\"database\","
                + "\"originalFileName\":\"file.sdf\","
                + "\"counterIonsRemoved\":\"true\","
                + "\"calculatedProperties\":\"false\","
                + "\"calculatedLigandEfficiency\":\"false\","
                + "\"calculatedDescriptors2D\":\"false\","
                + "\"calculatedDescriptors3D\":\"false\","
                + "\"status\":\"Available\","
                + "\"numMolecules\":\"1\","
                + "\"numDeployments\":\"1\","
                + "\"owner\":\"lnavarro\","
                + "\"experimentsUsing\":[],"
                + "\"properties\":[\"prop1\",\"prop2\"]}";
        assertEquals(expectedJson, converter.getJson());

        verify(propertyListConverter);
        verify(experimentListConverter);
    }

    @Test
    public void moleculeDatabaseWithCalculatedLigandEfficiency() {
        MoleculeDatabase molDB = simpleMoleculeDatabase();
        molDB.calculatedLigandEfficiency = true;
        converter.setData(molDB);
        propertyListConverter.setData(anyObject(List.class));
        experimentListConverter.setData(anyObject(List.class));
        expect(propertyListConverter.getJson()).andReturn("[]");
        replay(propertyListConverter);
        expect(experimentListConverter.getJson()).andReturn("[]");
        replay(experimentListConverter);

        String expectedJson = "{\"id\":\"12\","
                + "\"name\":\"database\","
                + "\"originalFileName\":\"file.sdf\","
                + "\"counterIonsRemoved\":\"true\","
                + "\"calculatedProperties\":\"false\","
                + "\"calculatedLigandEfficiency\":\"true\","
                + "\"calculatedDescriptors2D\":\"false\","
                + "\"calculatedDescriptors3D\":\"false\","
                + "\"status\":\"Available\","
                + "\"numMolecules\":\"0\","
                + "\"numDeployments\":\"0\","
                + "\"owner\":\"lnavarro\","
                + "\"experimentsUsing\":[],"
                + "\"properties\":[]}";
        assertEquals(expectedJson, converter.getJson());

        verify(propertyListConverter);
        verify(experimentListConverter);
    }

    @Test
    public void moleculeDatabaseWithExperimentsUsing() {
        MoleculeDatabase molDB = simpleMoleculeDatabase();
        molDB.experimentsUsing = new ArrayList<String>();
        molDB.experimentsUsing.add("1");
        molDB.experimentsUsing.add("2");
                
        converter.setData(molDB);
        propertyListConverter.setData(anyObject(List.class));
        experimentListConverter.setData(anyObject(List.class));
        expect(propertyListConverter.getJson()).andReturn("[]");
        replay(propertyListConverter);
        expect(experimentListConverter.getJson()).andReturn("[\"1\",\"2\"]");
        replay(experimentListConverter);

        String expectedJson = "{\"id\":\"12\","
                + "\"name\":\"database\","
                + "\"originalFileName\":\"file.sdf\","
                + "\"counterIonsRemoved\":\"true\","
                + "\"calculatedProperties\":\"false\","
                + "\"calculatedLigandEfficiency\":\"false\","
                + "\"calculatedDescriptors2D\":\"false\","
                + "\"calculatedDescriptors3D\":\"false\","
                + "\"status\":\"Available\","
                + "\"numMolecules\":\"0\","
                + "\"numDeployments\":\"0\","
                + "\"owner\":\"lnavarro\","
                + "\"experimentsUsing\":[\"1\",\"2\"],"
                + "\"properties\":[]}";
        assertEquals(expectedJson, converter.getJson());

        verify(propertyListConverter);
        verify(experimentListConverter);
    }
}
