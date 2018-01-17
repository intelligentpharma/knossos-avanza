package utils.database;

import java.util.Arrays;
import java.util.List;

import models.Deployment;
import models.Event;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.mvc.Scope.Session;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.database.DatabaseCategorizatorImpl;
import utils.experiment.TestDataCreator;

public class DatabaseCategorizatorTest extends UnitTest {

    User user;
    MoleculeDatabase database;
    DatabaseCategorizatorImpl categorizator;
    TestDataCreator testDataCreator;
    String[] notNumeric = {"0", "1", "hola", "0", "1", "hola"};
    String[] numeric = {"0.1", "1.2", "2.3", "3.4", "4.5", "5.4"};
    String[] numericEmptyValue = {"0.1", "1.2", "2.3", "3.4", "", "4.5", "5.4"};
    Float[] multipleRanges = {0.2f, 1.2f, 4f};
    Float[] multipleRangesNotOrdered = {1.2f, 4f, 0.2f};
    Float[] min = {0.01f};
    Float[] max = {6f};
    Float[] middle = {3f};
    Float[] middleExists = {3.4f};
    Float[] minExists = {0.1f};
    Float[] maxExists = {5.4f};
    final String newPropertyName = "Categorization";

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("initial-data.yml");
        user = User.findByUserName("lnavarro");
        Session.current().put("username", user.username);
        testDataCreator = new TestDataCreator();
        categorizator = new DatabaseCategorizatorImpl();
        testDataCreator.createSequenceForTestDb();
    }

    @Test
    public void errorIfPropertyIsNotNumeric() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, notNumeric, notNumeric);
        categorizator.categorize(database, "activity", Arrays.asList(middle), null);
        List<Event> events = Event.findAllOwnedBySourceId(user.username, database.id);
        assertEquals(1, events.size());
        assertTrue(events.get(0).message.startsWith("Categorization could not be done"));
    }

    @Test
    public void newPropertyIsCreated() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(middle), newPropertyName);
        assertNotNull(database.getAllDeployments().get(0).getPropertyValueThroughSql(newPropertyName));
    }

//    @Test
//    TODO : Change this, now it's asynchronous!
    public void errorIfNewPropertyNameAlreadyExists() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        database.getAllDeployments().get(0).putProperty(newPropertyName, "hola");
        categorizator.categorize(database, "activity", Arrays.asList(middle), newPropertyName);
        List<Event> events = Event.findAllOwnedBySourceId(user.username, database.id);
        assertEquals(1, events.size());
        assertTrue(events.get(0).message.startsWith("Categorization could not be done"));
    }

//    @Test
    public void rangeSmallerThanMinimumIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(min), newPropertyName);
        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(1, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));
    }

    @Test
    public void rangeWithExistingMinimumValueIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(minExists), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(1, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void rangeBiggerThanMaximumIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(max), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));
    }

    @Test
    public void rangeWithExistingBiggerValueIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(maxExists), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void rangeInMiddleIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(middle), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void rangeWithExistingMiddleValueIsProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(middleExists), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(0, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(1, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void multipleRangesAreProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(multipleRanges), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(3, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(3, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void multipleNotOrderedRangesAreProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numeric, numeric);
        categorizator.categorize(database, "activity", Arrays.asList(multipleRangesNotOrdered), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(3, Integer.parseInt(deployments.get(4).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(3, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));

    }

    @Test
    public void multipleRangesWithEmptyValueAreProperlyClassified() {
        database = testDataCreator.createSmallDatabaseWithActivityAndClustering(user, numericEmptyValue, numericEmptyValue);
        categorizator.categorize(database, "activity", Arrays.asList(multipleRangesNotOrdered), newPropertyName);

        List<Deployment> deployments = database.getAllDeployments();

        assertEquals(0, Integer.parseInt(deployments.get(0).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(1).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(2).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(2, Integer.parseInt(deployments.get(3).getPropertyValueThroughSql(newPropertyName)));
        assertEquals("", deployments.get(4).getPropertyValueThroughSql(newPropertyName));
        assertEquals(3, Integer.parseInt(deployments.get(5).getPropertyValueThroughSql(newPropertyName)));
        assertEquals(3, Integer.parseInt(deployments.get(6).getPropertyValueThroughSql(newPropertyName)));

    }
}
