package utils.grinds;

public class AmandaPointExtractorTest{/* extends UnitTest {

    AmandaPointExtractor amandaPointExtractor;
    AmandaForTest amandaForTest;
    String mapType = "OA";
    DistanceCalculator distanceCalculator;

    @Before
    public void setup() {
        distanceCalculator = new DistanceCalculatorImpl();
        distanceCalculator.setBoxCenter(new Vector3D(0, 0, 0));
        distanceCalculator.setBoxSize(new Vector3D(2, 2, 2));
        amandaPointExtractor = new AmandaPointExtractor(0, 0, distanceCalculator);
        amandaForTest = new AmandaForTest(1, 1, distanceCalculator);
        amandaPointExtractor.setMapType(mapType);
        amandaPointExtractor.setFirstCutoff(0, 0, 0, 0);
    }

//    @Test
    public void testForHDMap() {
        Logger.info("Big data amanda test");
        amandaPointExtractor.setFirstCutoff(-0.55, -0.5, -0.15, -0.2);
        amandaPointExtractor.setMapType("HD");
        String fileNamePath = "test-files/qsar/grinds/amanda/diazepam.HD.map";
        amandaPointExtractor.extractAndRetrievePoints(fileNamePath);
        Logger.info("Big data amanda test finished");
    }

//    @Test
    public void testForOAMap() {
        Logger.info("Big data amanda test");
        amandaPointExtractor.setFirstCutoff(-0.55, -0.5, -0.15, -0.2);
        amandaPointExtractor.setMapType("OA");
        String fileNamePath = "test-files/qsar/grinds/amanda/ampicillin.OA.map";
        amandaPointExtractor.extractAndRetrievePoints(fileNamePath);
        Logger.info("Big data amanda test finished");
    }

//    @Test
    public void testForCMap() {
        Logger.info("Big data amanda test");
        amandaPointExtractor.setFirstCutoff(-0.55, -0.5, -0.15, -0.2);
        amandaPointExtractor.setMapType("C");
        String fileNamePath = "test-files/qsar/grinds/amanda/progesterone.C.map";
        amandaPointExtractor.extractAndRetrievePoints(fileNamePath);
        Logger.info("Big data amanda test finished");
    }

    @Test
    public void extractPointsAppliesFirstCutoffCorrectly() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA.map";
        amandaPointExtractor.extractPointsFromFile(fileNamePath);
        assertEquals(15, amandaPointExtractor.pointsPerAtom.totalSize());
    }

    @Test
    public void extractPointsPerAtomPutsThemInTheCorrespondingAtom() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA.map";
        amandaPointExtractor.extractPointsFromFile(fileNamePath);
        assertEquals(15, amandaPointExtractor.pointsPerAtom.totalSize());
        assertEquals(8, amandaPointExtractor.pointsPerAtom.getCollection(1).size());
        assertEquals(6, amandaPointExtractor.pointsPerAtom.getCollection(2).size());
        assertEquals(1, amandaPointExtractor.pointsPerAtom.getCollection(3).size());
        assertNull(amandaPointExtractor.pointsPerAtom.getCollection(4));
    }

    @Test
    public void extractPointsPerAtomPutsThemInTheCorrespondingAtomAvoidingNonAtoms() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA_withNonAtoms.map";

        amandaPointExtractor.extractPointsFromFile(fileNamePath);
        assertEquals(15, amandaPointExtractor.pointsPerAtom.totalSize());
        assertEquals(8, amandaPointExtractor.pointsPerAtom.getCollection(1).size());
        assertEquals(7, amandaPointExtractor.pointsPerAtom.getCollection(2).size());
        assertNull(amandaPointExtractor.pointsPerAtom.getCollection(3));
        assertNull(amandaPointExtractor.pointsPerAtom.getCollection(4));
    }

    @Test
    public void nPerAtomAreProperlyCalculated() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA.map";
        amandaPointExtractor.extractPointsFromFile(fileNamePath);
        amandaPointExtractor.calculateNValues();
        Logger.info("Size " + amandaPointExtractor.pointsPerAtom.totalSize());
        assertEquals("", 3.0, amandaPointExtractor.nPerAtom.get(1), 0.0001);
        assertEquals("", 3.0, amandaPointExtractor.nPerAtom.get(2), 0.0001);
        assertEquals("", 1.0, amandaPointExtractor.nPerAtom.get(3), 0.0001);
    }

    @Test
    public void twoNHighestEnergyAreSelectedCorrectly() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA.map";
        amandaPointExtractor.extractPointsFromFile(fileNamePath);

        amandaPointExtractor.calculateNValues();
        MultiValueMap map = amandaPointExtractor.select2NNodes(amandaPointExtractor.pointsPerAtom);

        List<GrindPoint> atomList1 = new ArrayList<GrindPoint>();
        atomList1.add(new GrindPoint(21, -0.3, mapType, 0.3, 0));
        atomList1.add(new GrindPoint(18, -0.21, mapType, 0.21, 0));
        atomList1.add(new GrindPoint(20, -0.2, mapType, 0.2, 0));
        atomList1.add(new GrindPoint(17, -0.11, mapType, 0.11, 0));
        atomList1.add(new GrindPoint(2, -0.01, mapType, 0.01, 0));
        atomList1.add(new GrindPoint(15, -0.01, mapType, 0.01, 0));
        atomList1.add(new GrindPoint(16, -0.01, mapType, 0.01, 0));
        atomList1.add(new GrindPoint(19, -0.01, mapType, 0.01, 0));

        List<GrindPoint> atomList2 = new ArrayList<GrindPoint>();
        atomList2.add(new GrindPoint(10, -0.4, mapType, 0.4, 0));
        atomList2.add(new GrindPoint(23, -0.1, mapType, 0.1, 0));
        atomList2.add(new GrindPoint(26, -0.1, mapType, 0.1, 0));
        atomList2.add(new GrindPoint(22, -0.01, mapType, 0.01, 0));
        atomList2.add(new GrindPoint(11, -0.001, mapType, 0.001, 0));
        atomList2.add(new GrindPoint(12, -0.001, mapType, 0.001, 0));

        List<GrindPoint> atomList3 = new ArrayList<GrindPoint>();

        atomList3.add(new GrindPoint(25, -0.01, mapType, 0.01, 0));

        List<GrindPoint> calculatedList = (List<GrindPoint>) map.getCollection(1);

        for (int i = 0; i < calculatedList.size(); i++) {
            assertEquals(0, new GrindPointSortByEnergyComparator().compare(atomList1.get(i), calculatedList.get(i)));
            assertEquals(0, atomList1.get(i).position - calculatedList.get(i).position);
            Logger.info("i atom 1: " + i);
        }
        calculatedList.clear();
        calculatedList = (List<GrindPoint>) map.getCollection(2);

        for (int i = 0; i < calculatedList.size(); i++) {
            assertEquals(0, new GrindPointSortByEnergyComparator().compare(atomList2.get(i), calculatedList.get(i)));
            assertEquals(0, atomList2.get(i).position - calculatedList.get(i).position);
            Logger.info("i atom 2: " + i);
        }
        calculatedList.clear();
        calculatedList = (List<GrindPoint>) map.getCollection(3);

        for (int i = 0; i < calculatedList.size(); i++) {
            assertEquals(0, new GrindPointSortByEnergyComparator().compare(atomList3.get(i), calculatedList.get(i)));
            assertEquals(0, atomList3.get(i).position - calculatedList.get(i).position);
            Logger.info("i atom 3: " + i);
        }

    }

    @Test
    public void finalNodesAreSelectedCorrectly() {
        String fileNamePath = "test-files/qsar/grinds/amanda/test_20_nodes.OA.map";
        amandaPointExtractor.extractAndRetrievePoints(fileNamePath);

        List<GrindPoint> calculatedList = (List<GrindPoint>) amandaPointExtractor.finalPointsPerAtom.getCollection(1);

        assertEquals(3, calculatedList.size());
        assertEquals(21, calculatedList.get(0).position);
        assertEquals(20, calculatedList.get(1).position);
        assertEquals(17, calculatedList.get(2).position);

        calculatedList = (List<GrindPoint>) amandaPointExtractor.finalPointsPerAtom.getCollection(2);

        assertEquals(3, calculatedList.size());
        assertEquals(10, calculatedList.get(0).position);
        assertEquals(26, calculatedList.get(1).position);
        assertEquals(12, calculatedList.get(2).position);
    }

    @Test
    public void normalizeEnergiesIsCorrect() {
        List<GrindPoint> atomList1 = new ArrayList<GrindPoint>();
        atomList1.add(new GrindPoint(0, -0.3, mapType, 0.1, 0));
        atomList1.add(new GrindPoint(1, -0.21, mapType, 0.2, 0));
        atomList1.add(new GrindPoint(2, -0.2, mapType, 0.3, 0));
        atomList1.add(new GrindPoint(3, -0.11, mapType, 0.4, 0));

        List<GrindPoint> atomList2 = amandaPointExtractor.normalizeAmandaScoreAndEnergy(atomList1);

        assertEquals(-1.1628, atomList2.get(0).amandaScore, 0.001);
        assertEquals(-0.3876, atomList2.get(1).amandaScore, 0.001);
        assertEquals(0.3876, atomList2.get(2).amandaScore, 0.001);
        assertEquals(1.1628, atomList2.get(3).amandaScore, 0.001);

    }*/
}
