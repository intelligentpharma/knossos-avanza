package utils.database;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.easymock.EasyMock;
import org.junit.Test;

import play.mvc.Scope;
import play.test.UnitTest;
import utils.database.MoleculeDatabaseDataTableExport;

public class MoleculeDatabaseDataTableExportTest extends UnitTest{

	//@Test
	public void defaultQuery(){

		String expectedTotalCountDeploymentsQuery = "select count(*) from deployment d, molecule m where d.molecule_id = m.id and m.database_id = 1";
		String expectedAlphaOrderDeploymentsQuery = "select * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results  LIMIT 10 OFFSET 0";
		String expectedFilteredCountDeploymentsQuery = "select count(*) from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field1, d.name as field0 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results ";
		String expectedNumericOrderDeploymentsQuery = "select * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results  LIMIT 10 OFFSET 0";
		
		EntityManager em = EasyMock.createMock(EntityManager.class);
		Query query1 = EasyMock.createMock(Query.class);
		EasyMock.expect(query1.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedTotalCountDeploymentsQuery)).andReturn(query1);
		Query query2 = EasyMock.createMock(Query.class);
		EasyMock.expect(query2.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedAlphaOrderDeploymentsQuery)).andReturn(query2);
		Query query3 = EasyMock.createMock(Query.class);
		EasyMock.expect(query3.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedFilteredCountDeploymentsQuery)).andReturn(query3);
		Query query4 = EasyMock.createMock(Query.class);
		EasyMock.expect(query4.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedNumericOrderDeploymentsQuery)).andReturn(query4);
		
		EasyMock.replay(em, query1, query2, query3, query4);
		
		Scope.Params params = new Scope.Params();
		MoleculeDatabaseDataTableExport moleculeDatabaseDataTableExport = new MoleculeDatabaseDataTableExport(
				1, 0, 10, 1, 0, null, params, em);
		moleculeDatabaseDataTableExport.calculateDeploymentData();

		EasyMock.verify(em, query1, query2, query3, query4);
	}

	//TODO revisit, commented for Jenkins
	//@Test
	public void queryWithOrder(){

		String expectedTotalCountDeploymentsQuery = "select count(*) from deployment d, molecule m where d.molecule_id = m.id and m.database_id = 1";
		String expectedAlphaOrderDeploymentsQuery = "select row_number() over(), * from (select '<a href=\"javascript:openMoleculeInJmol('||deployment_id||', '''||(select name from deployment where id=deployment_id)||''');\">'||deployment_id||'</a>' as field0, d.name as field1 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results LIMIT 10 OFFSET 0";				
		String expectedFilteredCountDeploymentsQuery = "select count(*) from (select '<a href=\"javascript:openMoleculeInJmol('||deployment_id||', '''||(select name from deployment where id=deployment_id)||''');\">'||deployment_id||'</a>' as field0, d.name as field1 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results "; 		
		String expectedNumericOrderDeploymentsQuery = "select row_number() over(ORDER BY CAST(field0 as numeric)  ASC), * from (select '<a href=\"javascript:openMoleculeInJmol('||deployment_id||', '''||(select name from deployment where id=deployment_id)||''');\">'||deployment_id||'</a>' as field0, d.name as field1 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8 ) ) as properties, deployment d where properties.deployment_id = d.id ) as results ORDER BY CAST(field0 as numeric)  ASC LIMIT 10 OFFSET 0";		

		EntityManager em = EasyMock.createMock(EntityManager.class);
		Query query1 = EasyMock.createMock(Query.class);
		EasyMock.expect(query1.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedTotalCountDeploymentsQuery)).andReturn(query1);
		Query query2 = EasyMock.createMock(Query.class);
		EasyMock.expect(query2.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedAlphaOrderDeploymentsQuery)).andReturn(query2);
		Query query3 = EasyMock.createMock(Query.class);
		EasyMock.expect(query3.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedFilteredCountDeploymentsQuery)).andReturn(query3);
		Query query4 = EasyMock.createMock(Query.class);
		EasyMock.expect(query4.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedNumericOrderDeploymentsQuery)).andReturn(query4);
		
		EasyMock.replay(em, query1, query2, query3, query4);
		
		Scope.Params params = new Scope.Params();
		
		MoleculeDatabaseDataTableExport moleculeDatabaseDataTableExport = new MoleculeDatabaseDataTableExport(
				1, 0, 10, 1, 0, "ASC", params, em);
		moleculeDatabaseDataTableExport.calculateDeploymentData();

		EasyMock.verify(em, query1, query2, query3, query4);
	}

	//TODO revisit, commented for Jenkins
	//@Test
	public void queryWithNameAndTwoProperties(){

		String expectedTotalCountDeploymentsQuery = "select count(*) from deployment d, molecule m where d.molecule_id = m.id and m.database_id = 1";
		String expectedAlphaOrderDeploymentsQuery = "select row_number() over(), * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2,field3 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000),field3 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results  LIMIT 10 OFFSET 0";				
		String expectedFilteredCountDeploymentsQuery = "select count(*) from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2,field3 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000),field3 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results ";
		String expectedNumericOrderDeploymentsQuery = "select row_number() over(), * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2,field3 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000),field3 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results  LIMIT 10 OFFSET 0";		

		EntityManager em = EasyMock.createMock(EntityManager.class);
		Query query1 = EasyMock.createMock(Query.class);
		EasyMock.expect(query1.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedTotalCountDeploymentsQuery)).andReturn(query1);
		Query query2 = EasyMock.createMock(Query.class);
		EasyMock.expect(query2.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedAlphaOrderDeploymentsQuery)).andReturn(query2);
		Query query3 = EasyMock.createMock(Query.class);
		EasyMock.expect(query3.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedFilteredCountDeploymentsQuery)).andReturn(query3);
		Query query4 = EasyMock.createMock(Query.class);
		EasyMock.expect(query4.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedNumericOrderDeploymentsQuery)).andReturn(query4);
		
		EasyMock.replay(em, query1, query2, query3, query4);
		
		Scope.Params params = new Scope.Params();
		
		MoleculeDatabaseDataTableExport moleculeDatabaseDataTableExport = new MoleculeDatabaseDataTableExport(
				1, 0, 10, 3, 0, null, params, em);
		moleculeDatabaseDataTableExport.calculateDeploymentData();

		EasyMock.verify(em, query1, query2, query3, query4);
	}

	//TODO revisit, commented for Jenkins
	//@Test
	public void queryWithFilteringAndProperties(){

		String expectedTotalCountDeploymentsQuery = "select count(*) from deployment d, molecule m where d.molecule_id = m.id and m.database_id = 1";
		String expectedAlphaOrderDeploymentsQuery = "select row_number() over(), * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results WHERE (1=1  AND field1 LIKE '%name%'  AND field2 LIKE '%property%' ) AND (1=2  OR field1 LIKE '%all%'  OR field2 LIKE '%all%' ) LIMIT 10 OFFSET 0";				
		String expectedFilteredCountDeploymentsQuery = "select count(*) from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results WHERE (1=1  AND field1 LIKE '%name%'  AND field2 LIKE '%property%' ) AND (1=2  OR field1 LIKE '%all%'  OR field2 LIKE '%all%' )";
		String expectedNumericOrderDeploymentsQuery = "select row_number() over(), * from (select '<a href=\"javascript:showMolecule3D('||deployment_id||');\">'||deployment_id||'</a>' as field0, d.name as field1,field2 from (SELECT * FROM crosstab('select deployment_id, name, value from chemicalproperty where deployment_id IN  (select d.id from deployment d, molecule m, moleculedatabase md where md.id = m.database_id and m.id = d.molecule_id and md.id = 1) and name NOT LIKE ''ECFP%!_%!_%'' escape ''!'' and name NOT LIKE ''Molprint2D%!_%!_%'' escape ''!'' and name NOT LIKE ''GRIND%'' order by deployment_id, name') as (deployment_id int8,field2 varchar(4000) ) ) as properties, deployment d where properties.deployment_id = d.id ) as results WHERE (1=1  AND field1 LIKE '%name%'  AND field2 LIKE '%property%' ) AND (1=2  OR field1 LIKE '%all%'  OR field2 LIKE '%all%' ) LIMIT 10 OFFSET 0";
		
		EntityManager em = EasyMock.createMock(EntityManager.class);
		Query query1 = EasyMock.createMock(Query.class);
		EasyMock.expect(query1.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedTotalCountDeploymentsQuery)).andReturn(query1);
		Query query2 = EasyMock.createMock(Query.class);
		EasyMock.expect(query2.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedAlphaOrderDeploymentsQuery)).andReturn(query2);
		Query query3 = EasyMock.createMock(Query.class);
		EasyMock.expect(query3.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedFilteredCountDeploymentsQuery)).andReturn(query3);
		Query query4 = EasyMock.createMock(Query.class);
		EasyMock.expect(query4.getResultList()).andReturn(new ArrayList());
		EasyMock.expect(em.createNativeQuery(expectedNumericOrderDeploymentsQuery)).andReturn(query4);
		
		EasyMock.replay(em, query1, query2, query3, query4);
		
		Scope.Params params = new Scope.Params();
		params.put("sSearch", "all");
		params.put("sSearch_0", "name");
		params.put("sSearch_1", "property");
		params.put("sSearch_2", "view");
		MoleculeDatabaseDataTableExport moleculeDatabaseDataTableExport = new MoleculeDatabaseDataTableExport(
				1, 0, 10, 2, 0, null, params, em);
		moleculeDatabaseDataTableExport.calculateDeploymentData();

		EasyMock.verify(em, query1, query2, query3, query4);
	}

	@Test
	public void totalDeploymentsQueryIsCorrect(){
		String expectedTotalDeploymentsQuery = "select count(*) from deployment d, molecule m where d.molecule_id = m.id and m.database_id = 1";
		
		EntityManager em = EasyMock.createMock(EntityManager.class);
		Query query = EasyMock.createMock(Query.class);
		EasyMock.expect(query.getSingleResult()).andReturn(new java.math.BigInteger("12"));
		EasyMock.expect(em.createNativeQuery(expectedTotalDeploymentsQuery)).andReturn(query);
		
		EasyMock.replay(em, query);

		Scope.Params params = new Scope.Params();		
		MoleculeDatabaseDataTableExport moleculeDatabaseDataTableExport = new MoleculeDatabaseDataTableExport(
				1, 0, 10, 1000, 0, null, params, em);
		moleculeDatabaseDataTableExport.calculateTotalDeployments();
		assertEquals(12, moleculeDatabaseDataTableExport.getTotalDeployments());
		
		assertEquals(0, moleculeDatabaseDataTableExport.getTotalFilteredDeployments());
		assertNull(moleculeDatabaseDataTableExport.getDeploymentData());

		EasyMock.verify(em, query);
	}
	
}
