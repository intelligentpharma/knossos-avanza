package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Ponderation;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.Factory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PonderationManagerTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}

	@Test
	public void listOfPonderationsOwnedByUserReturned(){
		List<Map<String, String>> ponderations = getPonderationsAccesibleTo("dbermudez");
		assertEquals(4, ponderations.size());
		assertEquals("prueba1", ponderations.get(0).get("name"));
		assertEquals("prueba2", ponderations.get(1).get("name"));
		assertEquals("inverseAD", ponderations.get(2).get("name"));
		assertEquals("improved_inverseAD", ponderations.get(3).get("name"));
	}

	@Test
	public void addNewPonderation() {
		String username = "dbermudez"; 
		emulatePonderationFormSubmit(username);
		
		List<Map<String, String>> ponderations = getPonderationsAccesibleTo(username);
		assertEquals(5, ponderations.size());
		
		Ponderation ponderation = Ponderation.getPonderationByName("prueba4");
		Ponderation expectedPonderation = new Ponderation();
		
		expectedPonderation.name = "prueba4";
		expectedPonderation.id = ponderation.id;
		expectedPonderation.owner = User.findByUserName("dbermudez");
    	expectedPonderation.weights.A = 1;
    	expectedPonderation.weights.Br = 2;
    	expectedPonderation.weights.C = 3;
    	expectedPonderation.weights.Ca = 4;
    	expectedPonderation.weights.Cl = 5;
    	expectedPonderation.weights.d = 6;
    	expectedPonderation.weights.e = 7;
    	expectedPonderation.weights.F = 8;
    	expectedPonderation.weights.Fe = 9;
    	expectedPonderation.weights.HD = 10;
    	expectedPonderation.weights.I = 11;
    	expectedPonderation.weights.Mg = 12;
    	expectedPonderation.weights.Mn = 13;
    	expectedPonderation.weights.N = 14;
    	expectedPonderation.weights.NA = 15;
    	expectedPonderation.weights.NS = 16;
    	expectedPonderation.weights.OA = 17;
    	expectedPonderation.weights.OS = 18;
    	expectedPonderation.weights.P = 19;
    	expectedPonderation.weights.S = 20;
    	expectedPonderation.weights.SA = 21;
    	expectedPonderation.weights.Zn = 22;
		
		assertEquals(expectedPonderation, ponderation);
	}

	private void emulatePonderationFormSubmit(String username) {
		Map<String, String> databaseParams = new HashMap<String, String>();
		databaseParams.put("username", username);
    	databaseParams.put("ponderation.name", "prueba4");
    	databaseParams.put("ponderation.weights.A", "1");
    	databaseParams.put("ponderation.weights.Br", "2");
    	databaseParams.put("ponderation.weights.C", "3");
    	databaseParams.put("ponderation.weights.Ca", "4");
    	databaseParams.put("ponderation.weights.Cl", "5");
    	databaseParams.put("ponderation.weights.d", "6");
    	databaseParams.put("ponderation.weights.e", "7");
    	databaseParams.put("ponderation.weights.F", "8");
    	databaseParams.put("ponderation.weights.Fe", "9");
    	databaseParams.put("ponderation.weights.HD", "10");
    	databaseParams.put("ponderation.weights.I", "11");
    	databaseParams.put("ponderation.weights.Mg", "12");
    	databaseParams.put("ponderation.weights.Mn", "13");
    	databaseParams.put("ponderation.weights.N", "14");
    	databaseParams.put("ponderation.weights.NA", "15");
    	databaseParams.put("ponderation.weights.NS", "16");
    	databaseParams.put("ponderation.weights.OA", "17");
    	databaseParams.put("ponderation.weights.OS", "18");
    	databaseParams.put("ponderation.weights.P", "19");
    	databaseParams.put("ponderation.weights.S", "20");
    	databaseParams.put("ponderation.weights.SA", "21");
    	databaseParams.put("ponderation.weights.Zn", "22");    	

		Response response = POST("/ponderation", databaseParams);
		assertIsOk(response);
	}	
	
	
	private List<Map<String, String>> getPonderationsAccesibleTo(String username) {
		Response response = GET("/ponderation?username=" + username);
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Logger.debug(content);
		ObjectMapper jsonParser = new ObjectMapper();

		List<Map<String, String>> parsedPonderations = null;
		try {
			parsedPonderations = jsonParser.readValue(content, List.class);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error in Json");
		}
		
		return parsedPonderations;
	}
	
	@Test
	public void manualPonderationHasCorrectTrainingType() {
		String username = "dbermudez";
		emulatePonderationFormSubmit(username);
		
		Ponderation ponderation = Ponderation.getPonderationByName("prueba4");

		assertEquals(Factory.MANUAL,ponderation.trainingType);
	}
	
}