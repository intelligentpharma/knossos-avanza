package utils.grinds;

public class TIPMapGeneratorTest /*extends UnitTest */{

//    TIPPointExtractor tIPMapGenerator;
//    AlignmentBox box = new AlignmentBox(0, 0, 0, 1, 1, 1);
//    String mapType = "OA";
//    String inputConvexFile = "/tmp/inputHull";
//    String outputConvexFile = "/tmp/outputHull";
//        
//
//    @Before
//    public void setup() {
//        tIPMapGenerator = new TIPPointExtractor(0, 0, new DistanceCalculatorImpl());        
//        tIPMapGenerator.distanceCalculator.setBoxCenter(new Vector3D(box.centerX, box.centerY, box.centerZ));
//        tIPMapGenerator.distanceCalculator.setBoxSize(new Vector3D(box.sizeX + 1, box.sizeY + 1, box.sizeZ + 1));
//        new File(outputConvexFile).delete();
//        new File(inputConvexFile).delete();
//        tIPMapGenerator.setIsosurface(new QConvexParser(outputConvexFile));
//    }
//
////    @Test
//    public void extractPointsFiltersCorrectly() {
//        String fileNamePath = "test-files/qsar/grinds/TIP/test_20_nodes.OA.map";
//        tIPMapGenerator.extractPointsFromFile(fileNamePath);
//        assertEquals(5, tIPMapGenerator.pointsFromFile.size());        
//    }
//    
////    @Test
//    public void extractPointsExtractsACubeCorrectly() {
////        String fileNamePath = "test-files/qsar/grinds/TIP/test_20_nodes.OA_cube.map";
////        tIPMapGenerator.extractPointsFromFile(fileNamePath);
////        for (Vector3D point : tIPMapGenerator.pointsFromFile){
////            Logger.info("Points:" + point.toString());                       
////        }
////        assertEquals(8, tIPMapGenerator.pointsFromFile.size());
////                
////        assertEquals(new Vector3D(-1, -1, -1), tIPMapGenerator.pointsFromFile.get(0));
////        assertEquals(new Vector3D(0, -1, -1), tIPMapGenerator.pointsFromFile.get(1));
////        assertEquals(new Vector3D(-1, 0, -1), tIPMapGenerator.pointsFromFile.get(2));
////        assertEquals(new Vector3D(0, 0, -1), tIPMapGenerator.pointsFromFile.get(3));
////        assertEquals(new Vector3D(-1, -1, 0), tIPMapGenerator.pointsFromFile.get(4));
////        assertEquals(new Vector3D(0, -1, 0), tIPMapGenerator.pointsFromFile.get(5));
////        assertEquals(new Vector3D(-1, 0, 0), tIPMapGenerator.pointsFromFile.get(6));
////        assertEquals(new Vector3D(0, 0, 0), tIPMapGenerator.pointsFromFile.get(7));        
//    }
//    
////    @Test
//    public void generateInputForConvexHullCorrectly() {
//        String fileNamePath = "test-files/qsar/grinds/TIP/test_20_nodes.OA_cube.map";
//        tIPMapGenerator.extractPointsFromFile(fileNamePath);
//        tIPMapGenerator.generateInputForConvexHull();
//        File tmpHull = new File(inputConvexFile);                
//        assertTrue(tmpHull.exists());
//    }
//    
////    @Test
//    public void calculateConvexHullCreatesFile() {
//        String fileNamePath = "test-files/qsar/grinds/TIP/test_20_nodes.OA_cube.map";
//        tIPMapGenerator.extractPointsFromFile(fileNamePath);
//        tIPMapGenerator.generateInputForConvexHull();        
//        tIPMapGenerator.extractConvexHull();              
//        File tmpHull = new File(outputConvexFile);                
//        assertTrue(tmpHull.exists());
//    }
//    
////    @Test
//    public void parseIsosurfaceCorrectly() {
//        String fileNamePath = "test-files/qsar/grinds/TIP/outputConvex";        
//        QConvexParser convexParser = new QConvexParser(fileNamePath);
//        convexParser.parse();
//        assertEquals(8, convexParser.vertices.size());
//        
//        assertNotNull(convexParser.vertices.get("p3(v6)"));
//        
//        assertEquals(0,convexParser.vertices.get("p3(v6)").coordinates.getX(), 0.001);
//        assertEquals(0, convexParser.vertices.get("p3(v6)").coordinates.getY(),0.001);
//        assertEquals(-1, convexParser.vertices.get("p3(v6)").coordinates.getZ(), 0.001);
//        
//        assertNotNull(convexParser.vertices.get("p24(v3)"));
//        assertEquals(-1, convexParser.vertices.get("p24(v3)").coordinates.getX(), 0.001);
//        assertEquals(-1, convexParser.vertices.get("p24(v3)").coordinates.getY(), 0.001);
//        assertEquals(5, convexParser.vertices.get("p24(v3)").coordinates.getZ(), 0.001);
//        
//        assertEquals(7, convexParser.facets.size());
//        
//        assertNotNull(convexParser.facets.get("f16"));
//        assertEquals(0, convexParser.facets.get("f16").normal.getX(), 0.001);
//        assertEquals(0, convexParser.facets.get("f16").normal.getY(), 0.001);
//        assertEquals(1, convexParser.facets.get("f16").normal.getZ(), 0.001);
//        
//        assertEquals(-0.666, convexParser.facets.get("f16").center.getX(), 0.001);
//        assertEquals(-0.666, convexParser.facets.get("f16").center.getY(), 0.001);
//        assertEquals(5, convexParser.facets.get("f16").center.getZ(), 0.001);
//        
//        assertEquals(3, convexParser.facets.get("f16").vertices.size());
//        
//        List<Point> vertices = convexParser.facets.get("f16").vertices;
//        Collections.sort(vertices, new PointIdComparator());
//        assertEquals("p24(v3)", vertices.get(0).id);
//        assertEquals("p25(v7)", vertices.get(1).id);
//        assertEquals("p26(v8)", vertices.get(2).id);
//        
//        assertEquals(3, convexParser.facets.get("f16").neighbors.size());
//        
//        List<String> facets = convexParser.facets.get("f16").neighbors;
//        Collections.sort(facets);
//        assertEquals("f17", facets.get(0));
//        assertEquals("f2", facets.get(1));
//        assertEquals("f3", facets.get(2));
//    }
//    
////    @Test
//    public void findsNeighborsAndCalculatesCurvatureCorrectly() {
//        String fileNamePath = "test-files/qsar/grinds/TIP/test_20_nodes.OA_cube_middle_points.map";
//        tIPMapGenerator.extractPointsFromFile(fileNamePath);
//        tIPMapGenerator.generateInputForConvexHull();        
//        tIPMapGenerator.extractConvexHull();
//        tIPMapGenerator.calculateCurvatures();
//        assertEquals(6, tIPMapGenerator.isosurface.facets.get("f16").partialCurvatures.size());        
//        assertEquals(0.0, tIPMapGenerator.isosurface.facets.get("f16").curvature, 0);
//        
//        assertEquals(7, tIPMapGenerator.isosurface.facets.get("f9").partialCurvatures.size());        
//        assertEquals(0.1133, tIPMapGenerator.isosurface.facets.get("f9").curvature, 0.0001);
//    }
//    
////    @Test
//    public void generateVectFile() {
//        tIPMapGenerator.THRESHOLD = 60000f;
//        String fileNamePath = "test-files/qsar/grinds/TIP/1molec.maps.OA.map";
//        tIPMapGenerator.extractPointsFromFile(fileNamePath);
////        tIPMapGenerator.convertToVectGeometricView(tIPMapGenerator.pointsFromFile, "/home/ip-users/laia/GeometricView/" + tIPMapGenerator.THRESHOLD + ".vect");
//        tIPMapGenerator.setInputConvexFile("/home/ip-users/laia/GeometricView/" + tIPMapGenerator.THRESHOLD + ".vertices");
//        tIPMapGenerator.generateInputForConvexHull();
//        assertTrue(true);
//    }
//    @Test
//    public void generateGeometry() {
////        tIPMapGenerator.THRESHOLD = 60000f;
////        String fileNamePath = "test-files/qsar/grinds/TIP/1molec.maps.OA.map";
////        tIPMapGenerator.extractPointsFromFile(fileNamePath);
////        tIPMapGenerator.generateIsoSurfaceFromPoints();
//        assertTrue(true);
//    }
//        
}
