package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.*;


import play.test.*;

public class NonVariantChemicalPropertiesTest extends UnitTest {

	Deployment deployment11, deployment12;
	Deployment deployment21, deployment22;
	Molecule molecule1,molecule2,molecule3;
	MoleculeDatabase database;

	@Before
	public void setup() {

		database = new MoleculeDatabase();
		
		molecule1 = new Molecule();
		molecule1.database = database;
		deployment11 = new Deployment();
		deployment12 = new Deployment();

		molecule2 = new Molecule();
		molecule2.database = database;
		deployment21 = new Deployment();
		deployment22 = new Deployment();
	}
	
	@Test
	public void returnsEmptyListWhenDatabaseIsEmpty(){
		List<String> names = database.getInvariantPropertyNames();
		assertTrue(names.isEmpty());
	}
	
	@Test
	public void returnsEmptyListWhenDeploymentsHaveNoProperties(){
		deployment11 = new Deployment();
		molecule1.addDeployment(deployment11);
		database.addMolecule(molecule1);
		
		List<String> names = database.getInvariantPropertyNames();
		assertTrue(names.isEmpty());
	}
	
	@Test
	public void allPropertiesAreNonVariantInAmoleculeWithASingleDeployment(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		deployment11.putProperty("prop2", "propV22");
		molecule1.addDeployment(deployment11);
		database.addMolecule(molecule1);
		
		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(2, names.size());
		assertEquals("prop1", names.get(0));
		assertEquals("prop2", names.get(1));
	}

	@Test
	public void aPropertyNotPresentInADeploymentIsVariant(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		deployment11.putProperty("prop2", "propV22");
		molecule1.addDeployment(deployment11);		
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		molecule1.addDeployment(deployment12);		
		database.addMolecule(molecule1);

		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(1, names.size());
		assertEquals("prop1", names.get(0));
	}
	
	@Test
	public void aPropertyNotPresentInTheFirstDeploymentIsVariant(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		molecule1.addDeployment(deployment11);
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		deployment12.putProperty("prop2", "propV22");
		molecule1.addDeployment(deployment12);
		database.addMolecule(molecule1);

		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(1, names.size());
		assertEquals("prop1", names.get(0));
	}
	
	@Test
	public void aPropertyWithNullValueInAllDeploymentsIsInvariant(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		deployment11.putProperty("prop2", "");
		molecule1.addDeployment(deployment11);		
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		deployment12.putProperty("prop2", "");
		molecule1.addDeployment(deployment12);		
		database.addMolecule(molecule1);

		deployment21 = new Deployment();
		deployment21.putProperty("prop1", "propV21");
		deployment21.putProperty("prop2", "propV22");
		molecule2.addDeployment(deployment21);		
		deployment22 = new Deployment();
		deployment22.putProperty("prop1", "propV21");
		deployment22.putProperty("prop2", "propV22");
		molecule2.addDeployment(deployment22);		
		database.addMolecule(molecule2);
		
		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(2, names.size());
		assertEquals("prop1", names.get(0));
		assertEquals("prop2", names.get(1));
	}
	

	
	@Test
	public void aPropertyWithDifferentValuesInSameMoleculeIsVariant(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		deployment11.putProperty("prop2", "propV22");
		molecule1.addDeployment(deployment11);		
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		deployment12.putProperty("prop2", "propV2_diff");
		molecule1.addDeployment(deployment12);		
		database.addMolecule(molecule1);

		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(1, names.size());
		assertEquals("prop1", names.get(0));
	}

	@Test
	public void aPropertyWithInvariantValuesWithinMoleculesButVariantInDifferentMoleculesIsInVariant(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV11");
		deployment11.putProperty("prop2", "propV12");
		molecule1.addDeployment(deployment11);		
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV11");
		deployment12.putProperty("prop2", "propV12");
		molecule1.addDeployment(deployment12);		
		database.addMolecule(molecule1);

		deployment21 = new Deployment();
		deployment21.putProperty("prop1", "propV21");
		deployment21.putProperty("prop2", "propV22");
		molecule2.addDeployment(deployment21);		
		deployment22 = new Deployment();
		deployment22.putProperty("prop1", "propV21");
		deployment22.putProperty("prop2", "propV22");
		molecule2.addDeployment(deployment22);		
		database.addMolecule(molecule2);
		
		List<String> names = database.getInvariantPropertyNames();
		
		assertEquals(2, names.size());
		assertEquals("prop1", names.get(0));
		assertEquals("prop2", names.get(1));
	}
	
	@Test
	public void variantPropertiesInAMoleculeHaveDifferentValuesInDeployments(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", "propV21");
		deployment11.putProperty("prop2", "propV22");
		molecule1.addDeployment(deployment11);		
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		deployment12.putProperty("prop2", "propV2_diff");
		molecule1.addDeployment(deployment12);		
		
		List<String> allPropertyNames = new ArrayList<String>();
		allPropertyNames.add("prop1");
		allPropertyNames.add("prop2");
		Set<String> names = molecule1.getVariantPropertyNames(allPropertyNames);
		
		assertEquals(1, names.size());
		assertTrue(names.contains("prop2"));
	}

	@Test
	public void nullVariantPropertiesInAMoleculeHaveDifferentValuesInDeployments(){
		deployment11 = new Deployment();
		deployment11.putProperty("prop1", null);
		deployment11.putProperty("prop2", "propV22");
		deployment11.putProperty("prop3", "propV3");
		molecule1.addDeployment(deployment11);
		deployment12 = new Deployment();
		deployment12.putProperty("prop1", "propV21");
		deployment12.putProperty("prop2", null);
		deployment11.putProperty("prop3", "propV33333");
		molecule1.addDeployment(deployment12);
		
		List<String> allPropertyNames = new ArrayList<String>();
		allPropertyNames.add("prop1");
		allPropertyNames.add("prop2");
		Set<String> names = molecule1.getVariantPropertyNames(allPropertyNames);
		
		assertEquals(2, names.size());
		assertTrue(names.contains("prop1"));
		assertTrue(names.contains("prop2"));
	}
	
	
	
	
	@Test
	public void nullEquality(){
		assertFalse(null != null);
		assertTrue(null == null);
	}
}
