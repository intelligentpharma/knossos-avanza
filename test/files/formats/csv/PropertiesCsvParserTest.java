package files.formats.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import models.ChemicalProperty;
import models.MoleculeDatabase;
import models.User;

import org.junit.Before;
import org.junit.Test;

import files.formats.csv.DatabaseActionOutputCsvParser;

import play.Play;
import play.db.jpa.Blob;
import play.libs.MimeTypes;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.FactoryImpl;
import utils.experiment.TestDataCreator;

public class PropertiesCsvParserTest extends UnitTest {

    User user;
    DatabaseActionOutputCsvParser parser;
    MoleculeDatabase database;
    TestDataCreator dataCreator;
    Factory factory;

    @Before
    public void setup() throws InterruptedException {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("initial-data.yml");
        
        dataCreator = new TestDataCreator();

        factory = new FactoryImpl();
        user = new User("aperreau", "hola", "adeu");
        user.save();
        List<String> propertyNames = new ArrayList<String>();
        propertyNames.add(ChemicalProperty.NPOL);
        propertyNames.add("Name");
        propertyNames.add(ChemicalProperty.NHEA);
        propertyNames.add("Polarity");
        database = dataCreator.createSmallDatabaseWithPropertyNamesAndValues(user, propertyNames);
        database.save();
    }

    @Test
    public void parserPropertiesFileWithNoMatchingValues() {
        File file = new File("test-files/updateProperties/prueba.csv");
        Blob blobFile = new Blob();
        try {
            blobFile.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        parser = factory.createPropertiesCsvParser(blobFile, ChemicalProperty.NHEA, ChemicalProperty.NHEA);
        parser.setDatabase(database);
        parser.parseFileAndUpdate();


        assertEquals(12, database.molecules.get(0).deployments.get(0).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(1).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(2).getAllChemicalPropertiesThroughSql().size());
        assertEquals("0", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Name"));
        assertEquals("2", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Polarity"));
        assertEquals("", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("SMR_VSA1"));
        assertEquals("0", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Name"));
        assertEquals("2", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Polarity"));
        assertEquals("", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("SMR_VSA1"));
        assertEquals("2", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("3", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Name"));
        assertEquals("4", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("5", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Polarity"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("SMR_VSA1"));
    }

    @Test
    public void parserPropertiesFileWithMatchingValues() {
        File file = new File("test-files/updateProperties/prueba.csv");
        Blob blobFile = new Blob();
        try {
            blobFile.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        parser = factory.createPropertiesCsvParser(blobFile, ChemicalProperty.NPOL, ChemicalProperty.NPOL);
        parser.setDatabase(database);
        parser.parseFileAndUpdate();

        assertEquals(12, database.molecules.get(0).deployments.get(0).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(1).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(2).getAllChemicalPropertiesThroughSql().size());
        assertEquals("0", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1\nB1\nC1", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Name"));
        assertEquals("2\n56\n57", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Polarity"));
        assertEquals("B2\nC2", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("B3\nC3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("B6\nC6", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_ARO"));
        assertEquals("B7\nC7", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_NC"));
        assertEquals("B8\nC8", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("B9\nC9", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("B10\nC10", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("B11\nC11", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("0", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1\nB1\nC1", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Name"));
        assertEquals("2\n56\n57", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Polarity"));
        assertEquals("B2\nC2", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("B3\nC3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("B6\nC6", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_ARO"));
        assertEquals("B7\nC7", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_NC"));
        assertEquals("B8\nC8", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("B9\nC9", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("B10\nC10", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("B11\nC11", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("2", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("3\nD1", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Name"));
        assertEquals("4\n58", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("5", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Polarity"));
        assertEquals("D2", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("D3", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("D6", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_ARO"));
        assertEquals("D7", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_NC"));
        assertEquals("D8", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("D9", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("D10", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("D11", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("SMR_VSA1"));

    }

    @Test
    public void parserPropertiesFileWithSomeMatchingValues() {
        File file = new File("test-files/updateProperties/pruebaWithPartialMatching.csv");
        Blob blobFile = new Blob();
        try {
            blobFile.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        parser = factory.createPropertiesCsvParser(blobFile, ChemicalProperty.NPOL, ChemicalProperty.NPOL);
        parser.setDatabase(database);
        parser.parseFileAndUpdate();

        assertEquals(1, database.molecules.size());
        assertEquals(3, database.molecules.get(0).deployments.size());
        assertEquals(12, database.molecules.get(0).deployments.get(0).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(1).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(2).getAllChemicalPropertiesThroughSql().size());
        assertEquals("0", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1\nB1\nC1\nD1", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Name"));
        assertEquals("2\n56\n57\n58", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Polarity"));
        assertEquals("B2\nC2\nD2", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("B3\nC3\nD3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("B6\nC6\nD6", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_ARO"));
        assertEquals("B7\nC7\nD7", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_NC"));
        assertEquals("B8\nC8\nD8", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("B9\nC9\nD9", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("B10\nC10\nD10", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("B11\nC11\nD11", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("0", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1\nB1\nC1\nD1", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Name"));
        assertEquals("2\n56\n57\n58", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Polarity"));
        assertEquals("B2\nC2\nD2", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("B3\nC3\nD3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("B6\nC6\nD6", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_ARO"));
        assertEquals("B7\nC7\nD7", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_NC"));
        assertEquals("B8\nC8\nD8", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("B9\nC9\nD9", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("B10\nC10\nD10", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("B11\nC11\nD11", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("2", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("3", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Name"));
        assertEquals("4", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("5", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Polarity"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_ARO"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_NC"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("SMR_VSA1"));
    }

    @Test
    public void parserPropertiesFileWithNameAsMatchingProperty() {
        File file = new File("test-files/updateProperties/pruebaWithName.csv");
        Blob blobFile = new Blob();
        try {
            blobFile.set(new FileInputStream(file), MimeTypes.getContentType(file.getName()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        parser = factory.createPropertiesCsvParser(blobFile, "DEPLOYMENT.NAME", "NAME");
        parser.setDatabase(database);
        parser.parseFileAndUpdate();

        assertEquals(1, database.molecules.size());
        assertEquals(3, database.molecules.get(0).deployments.size());
        assertEquals(12, database.molecules.get(0).deployments.get(0).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(1).getAllChemicalPropertiesThroughSql().size());
        assertEquals(12, database.molecules.get(0).deployments.get(2).getAllChemicalPropertiesThroughSql().size());
        assertEquals("probe1", database.molecules.get(0).deployments.get(0).name);
        assertEquals("0\nA5", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Name"));
        assertEquals("2\nA4", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Polarity"));
        assertEquals("A2", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("A3", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("A6", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_ARO"));
        assertEquals("A7", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("A_NC"));
        assertEquals("A8", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("A9", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("A10", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("A11", database.molecules.get(0).deployments.get(0).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("probe2", database.molecules.get(0).deployments.get(1).name);
        assertEquals("0\nB5", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("1", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Name"));
        assertEquals("2\nB4", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Polarity"));
        assertEquals("B2", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("B3", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("B6", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_ARO"));
        assertEquals("B7", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("A_NC"));
        assertEquals("B8", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("B9", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("B10", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("B11", database.molecules.get(0).deployments.get(1).getPropertyValueThroughSql("SMR_VSA1"));

        assertEquals("probe3", database.molecules.get(0).deployments.get(2).name);
        assertEquals("2\nC5", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NPOL));
        assertEquals("3", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Name"));
        assertEquals("4\nC4", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.NHEA));
        assertEquals("5", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Polarity"));
        assertEquals("C2", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("ACTIVITY"));
        assertEquals("C3", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql(ChemicalProperty.MW));
        assertEquals("C6", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_ARO"));
        assertEquals("C7", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("A_NC"));
        assertEquals("C8", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("GCUT_SLOGP_0"));
        assertEquals("C9", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("PEOE_VSA-4"));
        assertEquals("C10", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("Q_VSA_HYD"));
        assertEquals("C11", database.molecules.get(0).deployments.get(2).getPropertyValueThroughSql("SMR_VSA1"));

    }
}
