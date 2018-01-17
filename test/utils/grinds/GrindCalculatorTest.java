package utils.grinds;

import java.util.ArrayList;
import java.util.List;
import models.AlignmentBox;
import models.Deployment;
import models.Distance;
import models.GrindPoint;
import models.QsarExperiment;
import models.User;
import org.apache.commons.collections.map.MultiValueMap;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.experiment.TestDataCreator;

public class GrindCalculatorTest{ /*extends UnitTest {

    Deployment deployment;
    QsarExperiment experiment;
    AlignmentBox box;
    List<String> grindMaps;
    GrindCalculator grindCalculator;
    TestDataCreator dataCreator;
    User user;
    DistanceCalculator distanceCalculator;
    MultiValueMap points;

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("data.yml");
        user = User.findByUserName("xmaresma");
        dataCreator = new TestDataCreator();
        box = new AlignmentBox();
        box.centerX = 4.893;
        box.centerY = -1.537;
        box.centerZ = 0.986;
        box.sizeX = 128;
        box.sizeY = 128;
        box.sizeZ = 128;
        long experimentId = 1L;
        List<String> grindMaps = new ArrayList<String>();
        grindMaps.add("OA");
        grindMaps.add("HD");
        grindMaps.add("C");
        points = new MultiValueMap();
        experiment = dataCreator.createQsarExperiment(user);
        deployment = experiment.molecules.getAllDeployments().get(0);
        distanceCalculator = createNiceMock(DistanceCalculator.class);
        grindCalculator = new GrindCalculator(deployment, points, distanceCalculator, grindMaps, experimentId);
    }
    
    @Test
    public void dummyTest(){
        assertTrue(true);
    }        

    @Test
    public void emptyDistancesReturnsEmptyGrindVector() {
        List<Distance> distances = new ArrayList<Distance>();
        expect(distanceCalculator.getDistancesFromMaps(new ArrayList<GrindPoint>(), new ArrayList<GrindPoint>())).andReturn(distances);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVectorOfAPairOfMaps("OA", "OA");
        verify(distanceCalculator);

        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("", energies);
        assertEquals("", sourcePoints);
        assertEquals("", targetPoints);
    }

	//@Test
    public void PairOfMapsGrindVectorWithOneBinIsCorrect() {
        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(2, -5.8, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(3, -0.4, "OA", 0, 0);


        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point1, point1, 0));
        distances.add(new Distance(point2, point2, 0));
        distances.add(new Distance(point3, point3, 0));
        distances.add(new Distance(point1, point2, 0.375));
        distances.add(new Distance(point2, point1, 0.375));
        distances.add(new Distance(point2, point3, 0.375));
        distances.add(new Distance(point3, point2, 0.375));
        distances.add(new Distance(point1, point3, 0.75));
        distances.add(new Distance(point3, point1, 0.75));

        List<GrindPoint> points = new ArrayList<GrindPoint>();
        points.add(point1);
        points.add(point2);
        points.add(point3);

        expect(distanceCalculator.getDistancesFromMaps(points, points)).andReturn(distances);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVectorOfAPairOfMaps("OA", "OA");
        verify(distanceCalculator);
        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("33.64", energies);
        assertEquals("2", sourcePoints);
        assertEquals("2", targetPoints);
    }

//	@Test
    public void fillWith0distancesOfSeparatedBins() {
        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(2, -5.8, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(10, -0.4, "OA", 0, 0);


        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point1, point1, 0));
        distances.add(new Distance(point2, point2, 0));
        distances.add(new Distance(point3, point3, 0));
        distances.add(new Distance(point1, point2, 0.375));
        distances.add(new Distance(point2, point1, 0.375));
        distances.add(new Distance(point2, point3, 3));
        distances.add(new Distance(point3, point2, 3));
        distances.add(new Distance(point1, point3, 3.375));
        distances.add(new Distance(point3, point1, 3.375));

        List<GrindPoint> points = new ArrayList<GrindPoint>();
        points.add(point1);
        points.add(point2);
        points.add(point3);

        expect(distanceCalculator.getDistancesFromMaps(points, points)).andReturn(distances);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVectorOfAPairOfMaps("OA", "OA");
        verify(distanceCalculator);
        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("33.64,0,0,2.32,0.92", energies);
        assertEquals("2,0,0,2,1", sourcePoints);
        assertEquals("2,0,0,10,10", targetPoints);

    }

//	@Test
    public void GrindVectorOfTwoPointsOfDifferentMaps() {
        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(10, -5.8, "HC", 0, 0);


        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point1, point2, 3.375));
        distances.add(new Distance(point2, point1, 3.375));

        List<GrindPoint> pointsOA = new ArrayList<GrindPoint>();
        pointsOA.add(point1);

        List<GrindPoint> pointsHD = new ArrayList<GrindPoint>();
        pointsHD.add(point2);

        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(distances);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVectorOfAPairOfMaps("OA", "HC");
        verify(distanceCalculator);

        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("0,0,0,0,13.34", energies);
        assertEquals("0,0,0,0,1", sourcePoints);
        assertEquals("0,0,0,0,10", targetPoints);
    }

//	@Test
    public void GrindVectorOfMultiplePointsOfDifferentMaps() {
        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(5, -0.4, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(10, -5.8, "HC", 0, 0);

        GrindPoint point4 = new GrindPoint(37, -1.9, "HC", 0, 0);


        List<GrindPoint> pointsOA = new ArrayList<GrindPoint>();
        pointsOA.add(point1);
        pointsOA.add(point2);

        List<GrindPoint> pointsHD = new ArrayList<GrindPoint>();
        pointsHD.add(point3);
        pointsHD.add(point4);

        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point2, point3, 1.875));
        distances.add(new Distance(point3, point2, 1.875));
        distances.add(new Distance(point1, point3, 3.375));
        distances.add(new Distance(point3, point1, 3.375));
        distances.add(new Distance(point2, point4, 12));
        distances.add(new Distance(point4, point2, 12));
        distances.add(new Distance(point1, point4, 13.5));
        distances.add(new Distance(point4, point1, 13.5));

        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(distances);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVectorOfAPairOfMaps("OA", "HC");
        verify(distanceCalculator);
        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("0,0,2.32,0,13.34,0,0,0,0,0,0,0,0,0,0,0.76,4.37", energies);
        assertEquals("0,0,5,0,1,0,0,0,0,0,0,0,0,0,0,5,1", sourcePoints);
        assertEquals("0,0,10,0,10,0,0,0,0,0,0,0,0,0,0,37,37", targetPoints);
    }

//	@Test
    public void GrindVectorOfMultiplePointsOfDifferentMapsCombinationWithEmptyDistances() {

        List<String> grindMaps = new ArrayList<String>();
        grindMaps.add("OA");
        grindMaps.add("HC");

        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(5, -0.4, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(10, -5.8, "HD", 0, 0);

        GrindPoint point4 = new GrindPoint(37, -1.9, "HD", 0, 0);


        List<GrindPoint> pointsOA = new ArrayList<GrindPoint>();
        pointsOA.add(point1);
        pointsOA.add(point2);

        List<GrindPoint> pointsHD = new ArrayList<GrindPoint>();
        pointsHD.add(point3);
        pointsHD.add(point4);

        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point2, point3, 1.875));
        distances.add(new Distance(point3, point2, 1.875));
        distances.add(new Distance(point1, point3, 3.375));
        distances.add(new Distance(point3, point1, 3.375));
        distances.add(new Distance(point2, point4, 12));
        distances.add(new Distance(point4, point2, 12));
        distances.add(new Distance(point1, point4, 13.5));
        distances.add(new Distance(point4, point1, 13.5));

        List<Distance> emptyDistance = new ArrayList<Distance>();

        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(distances);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(emptyDistance);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVector();
        verify(distanceCalculator);
        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("0,0,2.32,0,13.34,0,0,0,0,0,0,0,0,0,0,0.76,4.37", energies);
        assertEquals("0,0,5,0,1,0,0,0,0,0,0,0,0,0,0,5,1", sourcePoints);
        assertEquals("0,0,10,0,10,0,0,0,0,0,0,0,0,0,0,37,37", targetPoints);
    }

//	@Test
    public void GrindVectorOfMultiplePointsOfDifferentMapsCombinationWithFullAndEmptyDistances() {

        List<String> grindMaps = new ArrayList<String>();
        grindMaps.add("OA");
        grindMaps.add("HC");

        GrindPoint point1 = new GrindPoint(1, -2.3, "OA", 0, 0);

        GrindPoint point2 = new GrindPoint(5, -0.4, "OA", 0, 0);

        GrindPoint point3 = new GrindPoint(10, -5.8, "HD", 0, 0);

        GrindPoint point4 = new GrindPoint(37, -1.9, "HD", 0, 0);

        List<GrindPoint> pointsOA = new ArrayList<GrindPoint>();
        pointsOA.add(point1);
        pointsOA.add(point2);

        List<GrindPoint> pointsHD = new ArrayList<GrindPoint>();
        pointsHD.add(point3);
        pointsHD.add(point4);

        List<GrindPoint> pointsC = new ArrayList<GrindPoint>();

        List<Distance> distances = new ArrayList<Distance>();
        distances.add(new Distance(point2, point3, 1.875));
        distances.add(new Distance(point3, point2, 1.875));
        distances.add(new Distance(point1, point3, 3.375));
        distances.add(new Distance(point3, point1, 3.375));
        distances.add(new Distance(point2, point4, 12));
        distances.add(new Distance(point4, point2, 12));
        distances.add(new Distance(point1, point4, 13.5));
        distances.add(new Distance(point4, point1, 13.5));

        List<Distance> distances2 = new ArrayList<Distance>();
        distances2.add(new Distance(point1, point1, 0));
        distances2.add(new Distance(point2, point2, 0));
        distances2.add(new Distance(point1, point2, 0));
        distances2.add(new Distance(point2, point1, 0));

        List<Distance> distances3 = new ArrayList<Distance>();
        distances3.add(new Distance(point3, point3, 0));
        distances3.add(new Distance(point4, point4, 0));
        distances3.add(new Distance(point3, point4, 0));
        distances3.add(new Distance(point4, point3, 0));

        List<Distance> emptyDistance = new ArrayList<Distance>();

        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsHD)).andReturn(distances);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsOA)).andReturn(distances2);
        expect(distanceCalculator.getDistancesFromMaps(pointsHD, pointsHD)).andReturn(distances3);
        expect(distanceCalculator.getDistancesFromMaps(pointsOA, pointsC)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsC, pointsC)).andReturn(emptyDistance);
        expect(distanceCalculator.getDistancesFromMaps(pointsHD, pointsC)).andReturn(emptyDistance);
        replay(distanceCalculator);
        grindCalculator.calculateGrindVector();
        verify(distanceCalculator);
        String energies = grindCalculator.getEnergyString();
        String sourcePoints = grindCalculator.getSourcePointsString();
        String targetPoints = grindCalculator.getTargetPointsString();

        assertEquals("5.29,0,0,2.32,0,13.34,0,0,0,0,0,0,0,0,0,0,0.76,4.37,33.64", energies);
        assertEquals("1,0,0,5,0,1,0,0,0,0,0,0,0,0,0,0,5,1,10", sourcePoints);
        assertEquals("1,0,0,10,0,10,0,0,0,0,0,0,0,0,0,0,37,37,10", targetPoints);
    }*/
}
