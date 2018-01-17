package models;

import controllers.MoleculeDatabaseManager;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.database.DatabasePopulationUtils;
import utils.experiment.TestDataCreator;

public class DeploymentTest extends UnitTest {

    static User user;
    static Deployment deployment;
    static DatabasePopulationUtils utils;
    static TestDataCreator dataCreator;

    @BeforeClass
    public static void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("aperreau");
        deployment = new Deployment();
        Factory toolsFactory = new FactoryImpl();
        utils = toolsFactory.getDatabasePopulationUtils();
        dataCreator = new TestDataCreator();
    }

    @Test
    public void nameWithoutDeploymentDescriptors() {
        String originalName = "ZINC12492";
        String name = deployment.parseName(originalName);
        assertEquals(originalName, name);
    }

    @Test
    public void nameOnlyWithConformation() {
        String originalName = "ZINC_12893_c001";
        String name = deployment.parseName(originalName);
        assertEquals("ZINC_12893", name);
        assertEquals(1, deployment.conformation);
    }

    @Test
    public void nameOnlyWithThreeDescriptors() {
        String originalName = "ZINC_12893_c001_i003_t021";
        String name = deployment.parseName(originalName);
        assertEquals("ZINC_12893", name);
        assertEquals(1, deployment.conformation);
        assertEquals(3, deployment.isomer);
        assertEquals(21, deployment.tautormerism);
    }

    @Test
    public void nameOnlyWithThreeDescriptorsInWeirdOrder() {
        String originalName = "ZINC_12893_t021_c001_i003";
        String name = deployment.parseName(originalName);
        assertEquals("ZINC_12893", name);
        assertEquals(1, deployment.conformation);
        assertEquals(3, deployment.isomer);
        assertEquals(21, deployment.tautormerism);
    }

    @Test
    public void trickyName() {
        String originalName = "ZINC_k12893__t021_c001_i003";
        String name = deployment.parseName(originalName);
        assertEquals("ZINC_k12893_", name);
        assertEquals(1, deployment.conformation);
        assertEquals(3, deployment.isomer);
        assertEquals(21, deployment.tautormerism);
    }

    @Test
    public void nameWithoutUnderscoreAndDescriptor() {
        String originalName = "ZINC12893_c001";
        String name = deployment.parseName(originalName);
        assertEquals("ZINC12893", name);
        assertEquals(1, deployment.conformation);
    }

    @Test
    public void shouldNotBreakWith_c() {
        String originalName = "fragment_c";
        String name = deployment.parseName(originalName);
        assertEquals(originalName, name);
    }

    @Test
    public void createDeploymentWithoutFile() {
        deployment.parseName("ZINC12893_c001");
    }

    @Test
    public void findDeploymentsByExperimentId() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(user);
        List<Deployment> deployments = Deployment.findDatabaseDeploymentsByExperiment(experiment);
        assertEquals(7, deployments.size());
    }

    @Test
    public void findDeploymentsByExperimentIdAndMolecule() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        ComparisonExperiment experiment = dataCreator.getSmallEvaluatedExperiment(user);
        Molecule molecule = experiment.probeMolecules.molecules.get(1);
        List<Deployment> deployments = Deployment.findDatabaseDeploymentsByExperimentAndMolecule(experiment, molecule);
        assertNotNull(deployments);
        assertEquals(1, deployments.size());
    }

    @Test
    public void findDatabaseDeploymentsByPropertyName() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add(ChemicalProperty.NPOL);
        propertyNames.add(ChemicalProperty.NHEA);
        propertyNames.add("Activity");
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithPropertyNamesAndValues(user, propertyNames);

        List<Deployment> deploymentsSelected =
                Deployment.findDatabaseDeploymentsByPropertyName(database, ChemicalProperty.NPOL);
        assertEquals(3, deploymentsSelected.size());
        assertEquals("probe1", deploymentsSelected.get(0).name);
        assertEquals("probe2", deploymentsSelected.get(1).name);
        assertEquals("probe3", deploymentsSelected.get(2).name);
        deploymentsSelected = Deployment.findDatabaseDeploymentsByPropertyName(database, ChemicalProperty.NHEA);
        assertEquals(3, deploymentsSelected.size());
        assertEquals("probe1", deploymentsSelected.get(0).name);
        assertEquals("probe2", deploymentsSelected.get(1).name);
        assertEquals("probe3", deploymentsSelected.get(2).name);
    }

    @Test
    public void findDatabaseDeploymentsByPropertyNameAndPropertyValue() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add(ChemicalProperty.NPOL);
        propertyNames.add(ChemicalProperty.NHEA);
        propertyNames.add("Activity");
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithPropertyNamesAndValues(user, propertyNames);

        List<Deployment> deploymentsSelected =
                Deployment.findDatabaseDeploymentsByPropertyNameAndPropertyValue(database, ChemicalProperty.NPOL, "0");
        assertEquals(2, deploymentsSelected.size());
        assertEquals("probe1", deploymentsSelected.get(0).name);
        assertEquals("probe2", deploymentsSelected.get(1).name);
        assertEquals("0", deploymentsSelected.get(0).properties.get(0).value);
        assertEquals("1", deploymentsSelected.get(0).properties.get(1).value);
        assertEquals("2", deploymentsSelected.get(0).properties.get(2).value);
        assertEquals("0", deploymentsSelected.get(1).properties.get(0).value);
        assertEquals("1", deploymentsSelected.get(1).properties.get(1).value);
        assertEquals("2", deploymentsSelected.get(1).properties.get(2).value);
        deploymentsSelected = Deployment.findDatabaseDeploymentsByPropertyNameAndPropertyValue(database, ChemicalProperty.NPOL, "2");
        assertEquals(1, deploymentsSelected.size());
        assertEquals("2", deploymentsSelected.get(0).properties.get(0).value);
        assertEquals("3", deploymentsSelected.get(0).properties.get(1).value);
        assertEquals("4", deploymentsSelected.get(0).properties.get(2).value);
        deploymentsSelected = Deployment.findDatabaseDeploymentsByPropertyNameAndPropertyValue(database, ChemicalProperty.NPOL, "1");
        assertEquals(0, deploymentsSelected.size());
    }

    @Test
    public void findDeploymentsByDatabaseAndMoleculeName() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithoutProperties(user);
        List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndMoleculeName(database, "MCMC00000019");
        assertEquals(2, deploymentList.size());
        assertEquals("MCMC00000019_t001_i001_c001", deploymentList.get(0).name);
        assertEquals("MCMC00000019_t001_i001_c002", deploymentList.get(1).name);
    }

    @Test
    public void findDeploymentsByDatabaseAndName() {
        User user = User.findByUserName("aperreau");
        assertNotNull(user);
        MoleculeDatabase database = dataCreator.createSmallDatabaseWithoutProperties(user);
        List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "MCMC00000019_t001_i001_c001");
        assertEquals(1, deploymentList.size());
        assertEquals("MCMC00000019_t001_i001_c001", deploymentList.get(0).name);
    }

    @Test(expected = NullPointerException.class)
    public void appendPropertyAddsPropertyWhenNotExistent() {
        Deployment deployment = new Deployment();
        deployment.appendProperty(ChemicalProperty.NPOL, "0");
    }

    @Test
    public void appendPropertyDoesNotAddWhenExistent() {
        Deployment deployment = new Deployment();
        deployment.putProperty(ChemicalProperty.NPOL, "0");
        deployment.appendProperty(ChemicalProperty.NPOL, "0");
        assertEquals("0", deployment.getProperty(ChemicalProperty.NPOL).value);
    }

    @Test
    public void appendPropertyAppendsWhenExistent() {
        Deployment deployment = new Deployment();
        deployment.putProperty(ChemicalProperty.NPOL, "0");
        deployment.appendProperty(ChemicalProperty.NPOL, "1");
        assertEquals("0\n1", deployment.getPropertyValue(ChemicalProperty.NPOL));
    }

    @Test
    public void putEmptyPropertySetsNullWhenNotExisted() {
        Deployment deployment = new Deployment();
        deployment.putEmptyPropertyIfNotSet("prop1");
        assertEquals(1, deployment.properties.size());
        assertEquals("", deployment.getPropertyValue("prop1"));
    }

    @Test
    public void putEmptyPropertyMaintainsValueWhenExisted() {
        Deployment deployment = new Deployment();
        deployment.putProperty("prop1", "2");
        deployment.putEmptyPropertyIfNotSet("prop1");
        assertEquals(1, deployment.properties.size());
        assertEquals("2", deployment.getPropertyValue("prop1"));
    }        
}
