package utils.grinds;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.vecmath.Point3d;
import models.AlignmentBox;
import models.Distance;
import models.GrindPoint;
import models.User;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.junit.Before;
import org.junit.Test;
import play.Logger;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class DistanceCalculatorTest{/* extends UnitTest {

    AlignmentBox box;
    DistanceCalculatorImpl distanceCalculator;
    TestDataCreator creator;
    User user;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("xmaresma");

        box = new AlignmentBox();

        box.centerX = 4.893;
        box.centerY = -1.537;
        box.centerZ = 0.986;
        box.sizeX = 128;
        box.sizeY = 128;
        box.sizeZ = 128;

        distanceCalculator = new DistanceCalculatorImpl();
        distanceCalculator.setBoxCenter(new Vector3D(box.centerX, box.centerY, box.centerZ));
        distanceCalculator.setBoxSize(new Vector3D(box.sizeX, box.sizeY, box.sizeZ));
        distanceCalculator.setGridSpacing(0.5);
        creator = new TestDataCreator();
    }

    @Test
    public void distanceIsZeroForTheSamePoint() {
        GrindPoint point1 = new GrindPoint(54, 100.0, "OA", 0, 0);

        assertEquals(new Double(0.0), distanceCalculator.calculateDistance(point1, point1));
    }

    @Test
    public void distanceABisEqualToDistanceBA() {

        GrindPoint point1 = new GrindPoint(1154, 100.0, "OA", 0, 0);
        GrindPoint point2 = new GrindPoint(333654, 100.0, "OA", 0, 0);

        assertEquals(distanceCalculator.calculateDistance(point2, point1), distanceCalculator.calculateDistance(point1, point2));
    }

    @Test
    public void maxXDistanceIsCorrectlyCalculated() {

        GrindPoint point1 = new GrindPoint(0, 100.0, "OA", 0, 0);
        GrindPoint point2 = new GrindPoint((int) distanceCalculator.boxSize.getX() - 1, 100.0, "OA", 0, 0);

        assertEquals(new Double((distanceCalculator.boxSize.getX() - 1) * distanceCalculator.getGridSpacing()), distanceCalculator.calculateDistance(point1, point2), 0.0001);
    }

//    @Test
    public void distanceBetweenAPointAndTheCenterIsCorrectlyCalculated() {
        //In this test we check the distance between the center of the box and the point (x/2)
        GrindPoint point1 = new GrindPoint((int) distanceCalculator.boxSize.getX() / 2, 100.0, "OA", 0, 0);
        GrindPoint point2 = new GrindPoint((((int) distanceCalculator.boxSize.getX()) * ((int) distanceCalculator.boxSize.getY()) * ((int) distanceCalculator.boxSize.getZ()) / 2), 100.0, "OA", 0, 0);

        Double hypotenuse = Math.sqrt(Math.pow((int) distanceCalculator.boxSize.getX() * distanceCalculator.getGridSpacing() / 2, 2) + Math.pow((int) distanceCalculator.boxSize.getY() * distanceCalculator.getGridSpacing() / 2, 2));

        assertEquals(hypotenuse, distanceCalculator.calculateDistance(point1, point2), 0.0001);
    }

    @Test
    public void getDistancesSorted() {

        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(2, -2.3, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(3, -2.3, "OA", 0, 0);

        GrindPoint point4 = new GrindPoint(4, -2.3, "OA", 0, 0);

        GrindPoint point5 = new GrindPoint(5, -2.3, "OA", 0, 0);

        GrindPoint point6 = new GrindPoint(6, -2.3, "OA", 0, 0);

        List<GrindPoint> points = new ArrayList<GrindPoint>();
        points.add(point1);
        points.add(point2);
        points.add(point3);
        points.add(point4);
        points.add(point5);
        points.add(point6);

        List<Distance> distances = distanceCalculator.getDistancesFromMaps(points, points);
        assertEquals(36, distances.size());
        double dist = 0;
        for (Distance distance : distances) {
            assertTrue(dist <= distance.value);
            dist = distance.value;
        }

    }

    @Test
    public void getOffsetFromPositionIsProperlyCalculated() {
        Random random = new Random();

        int sizeX = (int) Math.round(distanceCalculator.getBoxSize().getX());
        int sizeY = (int) Math.round(distanceCalculator.getBoxSize().getY());
        int sizeZ = (int) Math.round(distanceCalculator.getBoxSize().getZ());


        int offset = random.nextInt(sizeX * sizeY * sizeZ);
        Point3d vector = distanceCalculator.get3DCoordinatesFromPosition(offset);
        int calculatedOffset = distanceCalculator.getPositionFrom3DCoordinates(vector);
        assertEquals(offset, calculatedOffset);

    }*/
}
