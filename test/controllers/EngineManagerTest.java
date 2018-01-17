package controllers;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import models.ModelForListing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;
import utils.Factory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType;

public class EngineManagerTest extends FunctionalTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
	}

	@After
	public void teardown() {
		Fixtures.deleteDatabase();
	}

	@Test
	public void listOfEnginesReturned() {
		List<ModelForListing> list = getListOfEngines();
		assertEquals(12, list.size());
		assertEquals("Inverse AD", list.get(0).name);
		assertEquals("Hercules", list.get(1).name);
		assertEquals("Selene Autodock 4", list.get(2).name);
		assertEquals("Selene Autodock 4.2.3", list.get(3).name);
		assertEquals("Selene Vina", list.get(4).name);
		assertEquals("Pegasus Lingo", list.get(5).name);
		assertEquals("Pegasus maccs", list.get(6).name);
		assertEquals("Pegasus graph", list.get(7).name);
		assertEquals("Pegasus hybridization", list.get(8).name);
		assertEquals("Molprint2D", list.get(9).name);
		assertEquals("ECFP", list.get(10).name);
		assertEquals("ECFPVariant", list.get(11).name);
	}

	private List<ModelForListing> getListOfEngines() {
		Response response = GET("/engine");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		return list;
	}
	
	@Test
	public void listOfChargeTypesReturned(){
		Response response = GET("/chargeType");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		
		assertEquals(4, list.size());
		assertEquals(Factory.ORIGINAL_NAME, list.get(0).name);
		assertEquals(Factory.GASTEIGER_NAME, list.get(1).name);
		assertEquals(Factory.EEM_NAME, list.get(2).name);
		assertEquals(Factory.MMFF94_NAME, list.get(3).name);
	}
	
	@Test
	public void listOfTrainingTypesReturned(){
		Response response = GET("/trainingType");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		
		assertEquals(2, list.size());
		assertEquals(Factory.LOGISTIC_REGRESSION_NAME, list.get(0).name);
		assertEquals(Factory.PHOLUS_NAME, list.get(1).name);
	}

	@Test
	public void listOfQsarTypesReturned(){
		Response response = GET("/qsarType");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		
		assertEquals(7, list.size());
		assertEquals(Factory.QSAR_PLS_NAME, list.get(0).name);
		assertEquals(Factory.QSAR_SPARSE_PLS_NAME, list.get(1).name);
		assertEquals(Factory.QSAR_PCA_NAME, list.get(2).name);
		assertEquals(Factory.QSAR_SVM_REGRESSION_NAME, list.get(3).name);
		assertEquals(Factory.QSAR_SVM_CLASSIFICATION_NAME, list.get(4).name);
		assertEquals(Factory.QSAR_RULE_BASED_NAME, list.get(5).name);
		assertEquals(Factory.QSAR_ALL_NAME, list.get(6).name);
	}
	
	@Test
	public void listOfAtomLabelTypesReturned(){
		Response response = GET("/atomLabelType");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		
		assertEquals(6, list.size());
		assertEquals(AtomLabelType.DAYLIGHT_INVARIANT_RING.name(), list.get(0).name);
		assertEquals(AtomLabelType.CDK_ATOM_TYPES.name(), list.get(1).name);
		assertEquals(AtomLabelType.DAYLIGHT_INVARIANT.name(), list.get(2).name);
		assertEquals(AtomLabelType.ELEMENT_NEIGHBOR.name(), list.get(3).name);
		assertEquals(AtomLabelType.ELEMENT_NEIGHBOR_RING.name(), list.get(4).name);
		assertEquals(AtomLabelType.ELEMENT_SYMBOL.name(), list.get(5).name);
	}
	
	@Test
	public void listOfDescriptorTypesReturned(){
		Response response = GET("/descriptorType");
		assertIsOk(response);
		assertContentType("application/json", response);
		String content = getContent(response);
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<ModelForListing>>() {
		}.getType();
		List<ModelForListing> list = gson.fromJson(content, collectionType);
		
		assertEquals(6, list.size());
		assertEquals(Factory.RCDK_NAME, list.get(0).name);
		assertEquals(Factory.LINGO_NAME, list.get(1).name);
		assertEquals(Factory.GRINDs_NAME, list.get(2).name);
		assertEquals( "Fingerprint " + Factory.FINGERPRINTS_MOLPRINT2D_NAME,list.get(3).name);
		assertEquals( "Fingerprint " + Factory.FINGERPRINTS_ECFP_NAME,list.get(4).name);
		assertEquals( "Fingerprint " + Factory.FINGERPRINTS_ECFPVARIANT_NAME,list.get(5).name);
		
	}
	
}
