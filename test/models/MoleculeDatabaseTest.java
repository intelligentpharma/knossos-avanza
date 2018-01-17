package models;

import controllers.MoleculeDatabaseManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import play.Logger;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class MoleculeDatabaseTest extends UnitTest {

    private static User user;
    private static MoleculeDatabase db;
    private static TestDataCreator testFactory;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("aperreau");
        testFactory = new TestDataCreator();
        db = testFactory.createSmallDatabaseWithoutProperties(user);
    }

    @Test
    public void moleculeDatabasesOwnedByUserFoundCorrectly() {
        List<MoleculeDatabase> databases = MoleculeDatabase.findAllOwnedBy(user);
        assertEquals(3, databases.size());
        assertEquals("Proteins", databases.get(0).name);
        assertEquals("Salts", databases.get(1).name);
        assertEquals("Small without properties", databases.get(2).name);
    }

    @Test
    public void moleculeDatabaseAddedCorrectly() {
        MoleculeDatabase readDB = MoleculeDatabase.findById(db.id);
        assertEquals(db.name, readDB.name);
    }

    @Test
    public void moleculesArePopulatedWhenDatabaseIsAdded() {
        MoleculeDatabase readDB = MoleculeDatabase.findById(db.id);
        assertEquals(6, readDB.molecules.size());
        Molecule mol = readDB.findMoleculeByName("MCMC00000008");
        assertNotNull(mol);
    }

    @Test
    public void findMoleculeReturnsNullIfMoleculeNameIsNotFound() {
        MoleculeDatabase readDB = MoleculeDatabase.findById(db.id);
        assertEquals(6, readDB.molecules.size());
        Molecule mol = readDB.findMoleculeByName("MCMC00000008decoy");
        assertNull(mol);
    }    
    
    @Test
    public void deploymentsAreAddedToMoleculeCorrectly() {
        MoleculeDatabase readDB = MoleculeDatabase.findById(db.id);
        Molecule mol = readDB.findMoleculeByName("MCMC00000019");
        List<Deployment> deployments = mol.deployments;
        assertNotNull(deployments);
        assertEquals(2, deployments.size());
    }

    @Test
    public void deploymentsAreStoredCorrectly() {
        List<Deployment> deployments = Deployment.findAll();
        assertEquals(9, deployments.size());
    }

    @Test
    public void deploymentReferencesCorrectMolecule() {
        Deployment deployment = Deployment.find("byConformation", 1).first();
        assertNotNull(deployment.molecule);
    }

    @Test
    public void databaseHasCorrectMoleculesInIt() {
        MoleculeDatabase readDB = MoleculeDatabase.find("byName", "Alcohols").first();
        List<Molecule> mols = Molecule.find("byDatabase", readDB).fetch();
        assertEquals(2, mols.size());
        assertEquals("Ethanol", mols.get(0).name);
        assertEquals("Methanol", mols.get(1).name);
    }

    @Test
    public void deploymentsRetrieval() {
        List<Deployment> deployments = db.getAllDeployments();
        assertEquals(7, deployments.size());
    }

    @Test
    public void databaseWithPropertiesIsSavedCorrectly() {
        db = new MoleculeDatabase();
        db.name = "SomeName";
        db.owner = User.findByUserName("aperreau");
        db.originalFileName = "test-files/mol12_13_test.sdf";
        db.transientFile = new File(db.originalFileName);

        Deployment deployment = new Deployment();
        deployment.name = "testName_mol12";
        deployment.transientFile = new File("test-files/mol12_13_test.sdf");

        Molecule molecule = new Molecule();
        molecule.database = db;
        molecule.name = "testName_mol12";
        molecule.addDeployment(deployment);

        db.addMolecule(molecule);

        ChemicalProperty propertyValue = new ChemicalProperty();
        propertyValue.value = "testName_mol12";
        propertyValue.name = "propName";
        propertyValue.deployment = deployment;
        deployment.properties.add(propertyValue);

        db.save();
    }

    @Test
    public void returnsEmptyListOfValuesWhenAttributeDoesNotExist() {
        Set<String> values = db.getPropertyValues("non-existing-attribute");
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void returnsListOfDifferentValuesForTheGivenAttribute() {
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add("class");
        db = testFactory.createSmallDatabaseWithProperties(user, propertyNames);
        Set<String> values = db.getPropertyValues("class");
        assertEquals(1, values.size());
        assertTrue(values.contains("don't care"));
    }

    @Test
    public void SmileDatabaseIsDetectedCorrectly() {
        MoleculeDatabase db = new MoleculeDatabase();
        db.name = "SomeName";
        db.originalFileName = "test-files/10mol.smi";
        assertTrue(db.isSmile());
        db.originalFileName = "something.notsmi.sdf";
        assertFalse(db.isSmile());
        db.originalFileName = null;
        assertFalse(db.isSmile());
    }

    @Test
    public void statusIsCorrectlySet() {
        MoleculeDatabase db = new MoleculeDatabase();
        db.name = "SomeName";

        assertEquals(db.status, MoleculeDatabaseStatus.AVAILABLE);

        db.setStatusNotAvailable();

        assertEquals(db.status, MoleculeDatabaseStatus.NOT_AVAILABLE);

        db.setStatusAvailable();

        assertEquals(db.status, MoleculeDatabaseStatus.AVAILABLE);
    }
    
    @Test
    public void isTypeOfObjectCorrect() {
        MoleculeDatabase db = new MoleculeDatabase();
        assertEquals(db.getType(), "moleculeDB");
    }
    
    @Test
    public void distinctPropertyValuesReturnsCorrectly(){
        db = new MoleculeDatabase();
        db.name = "SomeName";
        db.owner = User.findByUserName("aperreau");
        db.originalFileName = "test-files/mol12_13_test.sdf";
        db.transientFile = new File(db.originalFileName);

        Deployment deployment = new Deployment();
        deployment.name = "testName_mol12";
        deployment.transientFile = new File("test-files/mol12_13_test.sdf");

        Deployment deployment2 = new Deployment();
        deployment2.name = "testName_mol12";
        deployment2.transientFile = new File("test-files/mol12_13_test.sdf");
        
        Deployment deployment3 = new Deployment();
        deployment3.name = "testName_mol12";
        deployment3.transientFile = new File("test-files/mol12_13_test.sdf");
        
        Molecule molecule = new Molecule();
        molecule.database = db;
        molecule.name = "testName_mol12";
        molecule.addDeployment(deployment);
        molecule.addDeployment(deployment2);
        molecule.addDeployment(deployment3);

        db.addMolecule(molecule);

        ChemicalProperty propertyValue = new ChemicalProperty();
        propertyValue.value = "A";
        propertyValue.name = "propName";
        propertyValue.deployment = deployment;
        deployment.properties.add(propertyValue);
        
        ChemicalProperty propertyValue2 = new ChemicalProperty();
        propertyValue2.value = "A";
        propertyValue2.name = "propName";
        propertyValue2.deployment = deployment2;
        deployment2.properties.add(propertyValue2);

        ChemicalProperty propertyValue3 = new ChemicalProperty();
        propertyValue3.value = "B";
        propertyValue3.name = "propName";
        propertyValue3.deployment = deployment3;
        deployment3.properties.add(propertyValue3);
        
        assertEquals(2, db.getDistinctPropertyValues("propName").size());
        assertEquals("A", db.getDistinctPropertyValues("propName").get(0));
        assertEquals("B", db.getDistinctPropertyValues("propName").get(1));
    }

    @Test
    public void distinctPropertyValuesFromQueryReturnsCorrectly(){
        db = new MoleculeDatabase();
        db.name = "SomeName";
        db.owner = User.findByUserName("aperreau");
        db.originalFileName = "test-files/mol12_13_test.sdf";
        db.transientFile = new File(db.originalFileName);

        Deployment deployment = new Deployment();
        deployment.name = "testName_mol12";
        deployment.transientFile = new File("test-files/mol12_13_test.sdf");

        Deployment deployment2 = new Deployment();
        deployment2.name = "testName_mol12";
        deployment2.transientFile = new File("test-files/mol12_13_test.sdf");
        
        Deployment deployment3 = new Deployment();
        deployment3.name = "testName_mol12";
        deployment3.transientFile = new File("test-files/mol12_13_test.sdf");
        
        Molecule molecule = new Molecule();
        molecule.database = db;
        molecule.name = "testName_mol12";
        molecule.addDeployment(deployment);
        molecule.addDeployment(deployment2);
        molecule.addDeployment(deployment3);

        db.addMolecule(molecule);

        ChemicalProperty propertyValue = new ChemicalProperty();
        propertyValue.value = "A";
        propertyValue.name = "propName";
        propertyValue.deployment = deployment;
        deployment.properties.add(propertyValue);
        
        ChemicalProperty propertyValue2 = new ChemicalProperty();
        propertyValue2.value = "A";
        propertyValue2.name = "propName";
        propertyValue2.deployment = deployment2;
        deployment2.properties.add(propertyValue2);

        ChemicalProperty propertyValue3 = new ChemicalProperty();
        propertyValue3.value = "B";
        propertyValue3.name = "propName";
        propertyValue3.deployment = deployment3;
        deployment3.properties.add(propertyValue3);
        
        db.save();
        assertEquals(2, db.getDistinctPropertyValuesFromQuery("propName").size());
        assertEquals("A", db.getDistinctPropertyValuesFromQuery("propName").get(0));
        assertEquals("B", db.getDistinctPropertyValuesFromQuery("propName").get(1));
        db.delete();
    }

    @Test
    public void deleteDeploymentWorksCorrectly() {
        User user = User.findByUserName("lnavarro");
        assertNotNull(user);
        
        MoleculeDatabase database = testFactory.createSmallDatabaseWithActivityAndClustering(user);
        
        List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
        assertEquals(1, deploymentList.size());
        
        long deploymentId = deploymentList.get(0).id;        
        
        database.removeDeployment(deploymentId);
        
        deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
        
        assertEquals(0, deploymentList.size());
    }
    
    @Test
    public void deleteDeploymentDeletesAllChemicalPropertiesAssociated() {
        User user = User.findByUserName("lnavarro");
        assertNotNull(user);
        MoleculeDatabase database = testFactory.createSmallDatabaseWithActivityAndClustering(user);
        List<Deployment> deploymentList = Deployment.findDeploymentsByDatabaseAndName(database, "mol51");
        
        List<ChemicalProperty> propertiesList = deploymentList.get(0).properties;
        assertEquals(2, propertiesList.size());
        
        long deploymentId = deploymentList.get(0).id;        
        
        database.removeDeployment(deploymentId);
        
        ChemicalProperty chemicalProperty = ChemicalProperty.findById(propertiesList.get(0).id);        
        assertNull(chemicalProperty);
        chemicalProperty = ChemicalProperty.findById(propertiesList.get(1).id);        
        assertNull(chemicalProperty);
    }    
}
