package utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import models.CalculatedMif;
import models.ComparisonExperiment;
import models.MapsSimilarities;
import models.User;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.MIFRemover;
import utils.experiment.TestDataCreator;

public class MIFRemoverTest extends UnitTest {

    MapsSimilarities similarities1;
    ComparisonExperiment experiment;
    String mapPath = "test-files/mifs/whatever";
    MIFRemover mIFRemover;
    File mapFile;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");

        TestDataCreator dataCreator = new TestDataCreator();
        User owner = User.findByUserName("lnavarro");
        experiment = dataCreator.getSmallEvaluatedExperiment(owner);

        similarities1 = (MapsSimilarities) experiment.alignments.get(0);

        experiment.save();

        try {
            mapFile = new File(mapPath + ".map");
            if (!mapFile.exists()) {
                mapFile.createNewFile();
            }
        } catch (IOException e) {
            Logger.error("Unable to create map file for test");
        }
    }

    @Test
    public void oldMIFIsRemoved() {
        CalculatedMif calculatedMif = new CalculatedMif(experiment, similarities1, "test-files/mifs/whatever");
        calculatedMif.save();
        List<CalculatedMif> calculatedMifs = CalculatedMif.findAll();
        assertEquals(1, calculatedMifs.size());
        mIFRemover = new MIFRemover(calculatedMif.timeStamp + MIFRemover.daysToMillis(1) + 1, 1);
        mIFRemover.deleteOldMaps();
        calculatedMifs = CalculatedMif.findAll();
        assertEquals(0, calculatedMifs.size());
    }

    @Test
    public void newMIFIsNotRemoved() {
        CalculatedMif calculatedMif = new CalculatedMif(experiment, similarities1, "test-files/mifs/whatever");
        calculatedMif.save();
        List<CalculatedMif> calculatedMifs = CalculatedMif.findAll();
        assertEquals(1, calculatedMifs.size());
        mIFRemover = new MIFRemover(calculatedMif.timeStamp, 1);
        mIFRemover.deleteOldMaps();
        calculatedMifs = CalculatedMif.findAll();
        assertEquals(1, calculatedMifs.size());
    }
    
    @Test
    public void oldMIFFileIsRemoved() {
        CalculatedMif calculatedMif = new CalculatedMif(experiment, similarities1, "test-files/mifs/whatever");
        calculatedMif.save();        
        mIFRemover = new MIFRemover(calculatedMif.timeStamp + MIFRemover.daysToMillis(1) + 1, 1);
        mIFRemover.deleteOldMaps();
        assertFalse(mapFile.exists());
    }
    
    @Test
    public void newMIFFileIsNotRemoved() {
        CalculatedMif calculatedMif = new CalculatedMif(experiment, similarities1, "test-files/mifs/whatever");
        calculatedMif.save();        
        mIFRemover = new MIFRemover(calculatedMif.timeStamp, 1);
        mIFRemover.deleteOldMaps();
        assertTrue(mapFile.exists());
    }
    
    @Test
    public void daysToMillisIsCorrect() {        
        assertEquals(MIFRemover.daysToMillis(2), 172800000);
    }
}
