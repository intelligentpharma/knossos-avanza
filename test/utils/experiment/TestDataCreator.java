package utils.experiment;

import java.util.ArrayList;
import java.util.List;

import models.Alignment;
import models.AlignmentBox;
import models.ChemicalProperty;
import models.ComparisonExperiment;
import models.ComparisonExperimentGuest;
import models.Deployment;
import models.ExperimentStatus;
import models.Molecule;
import models.MoleculeDatabase;
import models.MoleculeDatabaseGuest;
import models.QsarExperiment;
import models.QsarExperimentGuest;
import models.QsarResult;
import models.User;
import play.Logger;
import play.db.jpa.JPA;
import utils.Factory;
import utils.FactoryImpl;
import engine.Engine;

public class TestDataCreator {

	private Factory engineFactory;

	public TestDataCreator() {
		engineFactory = new FactoryImpl();
	}

	public ComparisonExperiment getSmallExperiment(User owner) {
		ComparisonExperiment experiment = getExperimentDataOnly(owner);
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	public ComparisonExperiment getSmallExperimentWithGivenProperties(User owner, List<String> propertyNames) {
		ComparisonExperiment experiment = getExperimentDataOnlyWithGivenProperties(owner, propertyNames);
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	public ComparisonExperiment getSmallExperimentWithGivenDatabases(User owner, MoleculeDatabase probeDatabase,
			MoleculeDatabase targetDatabase, MoleculeDatabase referenceDatabase) {
		ComparisonExperiment experiment = getExperimentDataOnlyWithGivenDatabases(owner, probeDatabase, targetDatabase,
				referenceDatabase);
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	private ComparisonExperiment getExperimentDataOnlyWithGivenProperties(User owner, List<String> propertyNames) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Small Experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.similarityCalculationType = Factory.TYPE_OR;
		experiment.owner = owner;
		experiment.probeMolecules = createSmallDatabaseWithProperties(owner, propertyNames);
		experiment.targetMolecules = createSingleMoleculeDatabase(owner);
		return experiment;
	}

	public ComparisonExperiment getNxMExperiment(User owner) {
		ComparisonExperiment experiment = getNxMExperimentDataOnly(owner);
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	private ComparisonExperiment getExperimentDataOnly(User owner) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Small Experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.similarityCalculationType = Factory.TYPE_OR;
		experiment.owner = owner;
		experiment.probeMolecules = createSmallDatabaseWithoutProperties(owner);
		experiment.targetMolecules = createSingleMoleculeDatabase(owner);
		experiment.status = ExperimentStatus.QUEUED;
		experiment.box = new AlignmentBox();
		return experiment;
	}

	private ComparisonExperiment getExperimentDataOnlyWithGivenDatabases(User owner, MoleculeDatabase probeDatabase,
			MoleculeDatabase targetDatabase, MoleculeDatabase referenceDatabase) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Small Experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.similarityCalculationType = Factory.TYPE_OR;
		experiment.owner = owner;
		experiment.probeMolecules = probeDatabase;
		experiment.targetMolecules = targetDatabase;
		experiment.referenceMolecules = referenceDatabase;
		experiment.status = ExperimentStatus.QUEUED;
		experiment.box = new AlignmentBox();
		return experiment;
	}

	private ComparisonExperiment getNxMExperimentDataOnly(User owner) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "NxM Experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.owner = owner;
		experiment.probeMolecules = createSmallDatabaseWithoutProperties(owner);
		experiment.targetMolecules = createTwoMoleculesFourConformationsDatabase(owner);
		experiment.targetMolecules.save();
		experiment.status = ExperimentStatus.QUEUED;
		return experiment;
	}

	private ComparisonExperiment getExperimentWithPropertiesDataOnly(String name, User owner) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = name;
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.similarityCalculationType = Factory.TYPE_OR;
		experiment.owner = owner;
		experiment.probeMolecules = createSmallDatabaseWithActivityAndClustering(owner);
		experiment.targetMolecules = createSingleMoleculeDatabase(owner);
		experiment.box = createAligmentBox();
		return experiment;
	}

	public ComparisonExperiment getEvaluatedInverseDockingExperimentWithProperties(String name, User owner) {
		ComparisonExperiment experiment = getExperimentWithPropertiesDataOnly(name, owner);
		calculateWithFakeInverseDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;
		return experiment.save();
	}

	public ComparisonExperiment getEvaluatedDockingExperimentWithProperties(String name, User owner) {
		ComparisonExperiment experiment = getExperimentWithPropertiesDataOnly(name, owner);
		calculateWithFakeDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;
		return experiment.save();
	}

	public ComparisonExperiment getSmallEvaluatedExperiment(User owner) {
		ComparisonExperiment experiment = getExperimentDataOnly(owner);
		calculateWithFakeInverseDockingEngine(experiment);
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	public ComparisonExperiment getNxMEvaluatedExperiment(User owner) {
		ComparisonExperiment experiment = getNxMExperimentDataOnly(owner);
		calculateWithFakeInverseDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	public ComparisonExperiment getSmallEvaluatedDockingExperiment(User owner) {
		ComparisonExperiment experiment = getExperimentDataOnly(owner);
		calculateWithFakeDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;
		ComparisonExperiment savedExperiment = experiment.save();
		return savedExperiment;
	}

	public MoleculeDatabase createSingleDeploymentDatabase() {
		MoleculeDatabase targets = new MoleculeDatabase();
		Molecule target = new Molecule();
		target.database = targets;
		Deployment target1 = newDeployment("ZINC_i001", "active", "cluster1");
		target.addDeployment(target1);
		targets.addMolecule(target);
		return targets;
	}

	public MoleculeDatabase createSingleDeploymentDatabaseWithActivity() {
		MoleculeDatabase targets = new MoleculeDatabase();
		Molecule target = new Molecule();
		target.database = targets;
		Deployment target1 = newDeployment("ZINC_i001", "active", "cluster1");
		target1.properties = new ArrayList<ChemicalProperty>();
		ChemicalProperty property = new ChemicalProperty("activity", "1", target1);
		target1.properties.add(property);
		target.addDeployment(target1);
		targets.addMolecule(target);
		return targets;
	}

	public MoleculeDatabase createSingleMoleculeDatabase(User owner) {
		MoleculeDatabase db = new MoleculeDatabase();
		db.name = "Single Molecule";
		db.owner = owner;
		db.originalFileName = "single.sdf";

		Molecule mol1 = new Molecule();
		mol1.name = "ZINC01535869";
		db.addMolecule(mol1);
		Deployment mol11 = new Deployment();
		mol11.parseName("ZINC01535869_c001");
		mol1.addDeployment(mol11);

		db.save();
		return db;
	}

	public AlignmentBox createAligmentBox() {
		return new AlignmentBox(1, 2, 3, 4, 5, 6);
	}

	public MoleculeDatabase createTwoMoleculesFourConformationsDatabase(User owner) {
		MoleculeDatabase db = new MoleculeDatabase();
		db.name = "Two Molecules 4 Deployments";
		db.owner = owner;

		Molecule mol1 = new Molecule();
		mol1.database = db;
		mol1.name = "ZINC01";
		db.addMolecule(mol1);
		Deployment mol11 = new Deployment();
		mol11.parseName("ZINC01_c001");
		mol11.putProperty(ChemicalProperty.DEPLOYMENT_ORDER, "1");
		mol1.addDeployment(mol11);

		Molecule mol2 = new Molecule();
		mol2.database = db;
		mol2.name = "ZINC02";
		db.addMolecule(mol2);
		Deployment mol21 = new Deployment();
		mol21.parseName("ZINC02_c001");
		mol21.putProperty(ChemicalProperty.DEPLOYMENT_ORDER, "2");
		mol2.addDeployment(mol21);
		Deployment mol22 = new Deployment();
		mol22.parseName("ZINC02_c002");
		mol22.putProperty(ChemicalProperty.DEPLOYMENT_ORDER, "3");
		mol2.addDeployment(mol22);
		Deployment mol23 = new Deployment();
		mol23.parseName("ZINC02_c003");
		mol23.putProperty(ChemicalProperty.DEPLOYMENT_ORDER, "4");
		mol2.addDeployment(mol23);

		return db;
	}

	public MoleculeDatabase createSmallDatabaseWithoutProperties(User owner) {
		MoleculeDatabase db = new MoleculeDatabase();
		db.name = "Small without properties";
		db.owner = owner;
		db.originalFileName = "file.sdf";

		Molecule mol1 = new Molecule();
		mol1.name = "MCMC00000004";
		db.addMolecule(mol1);
		Deployment mol11 = new Deployment();
		mol11.parseName("MCMC00000004_t001_i001_c001");
		mol1.addDeployment(mol11);

		Molecule mol2 = new Molecule();
		mol2.name = "MCMC00000005";
		db.addMolecule(mol2);
		Deployment mol21 = new Deployment();
		mol21.parseName("MCMC00000005_t001_i001_c001");
		mol2.addDeployment(mol21);

		Molecule mol3 = new Molecule();
		mol3.name = "MCMC00000006";
		db.addMolecule(mol3);
		Deployment mol31 = new Deployment();
		mol31.parseName("MCMC00000006_t001_i001_c001");
		mol3.addDeployment(mol31);

		Molecule mol4 = new Molecule();
		mol4.name = "MCMC00000007";
		db.addMolecule(mol4);
		Deployment mol41 = new Deployment();
		mol41.parseName("MCMC00000007_t001_i001_c001");
		mol4.addDeployment(mol41);

		Molecule mol5 = new Molecule();
		mol5.name = "MCMC00000008";
		db.addMolecule(mol5);
		Deployment mol51 = new Deployment();
		mol51.parseName("MCMC00000008_t001_i001_c001");
		mol5.addDeployment(mol51);

		Molecule mol6 = new Molecule();
		mol6.name = "MCMC00000019";
		db.addMolecule(mol6);
		Deployment mol61 = new Deployment();
		mol61.parseName("MCMC00000019_t001_i001_c001");
		mol6.addDeployment(mol61);
		Deployment mol62 = new Deployment();
		mol62.parseName("MCMC00000019_t001_i001_c002");
		mol6.addDeployment(mol62);

		db.save();
		return db;
	}

	public MoleculeDatabase createSmallDatabaseWithoutPropertiesAndCommasInMoleculeNames(User owner) {
		MoleculeDatabase db = new MoleculeDatabase();
		db.name = "Small without properties";
		db.owner = owner;

		Molecule mol1 = new Molecule();
		mol1.name = "MCMC00000004";
		db.addMolecule(mol1);
		Deployment mol11 = new Deployment();
		mol11.parseName("MCMC00000004_t001_i001_c001");
		mol1.addDeployment(mol11);

		Molecule mol2 = new Molecule();
		mol2.name = "MCMC000,0019";
		db.addMolecule(mol2);
		Deployment mol21 = new Deployment();
		mol21.parseName("MCMC000,0019_t001_i001_c001");
		mol2.addDeployment(mol21);
		Deployment mol22 = new Deployment();
		mol22.parseName("MCMC000,0019_t001_i001_c002");
		mol2.addDeployment(mol22);

		db.save();
		return db;
	}

	public MoleculeDatabase createSmallDatabaseWithActivityAndClustering(User owner) {
		MoleculeDatabase probes = new MoleculeDatabase();
		probes.owner = owner;
		probes.name = "Small with Activity and Clustering";

		Molecule probe1 = new Molecule();
		probe1.name = "MOL01";
		probes.addMolecule(probe1);
		Deployment probe11 = newDeployment("mol11", "active", "cluster1");
		probe1.addDeployment(probe11);

		Molecule probe2 = new Molecule();
		probe2.name = "MOL02";
		probes.addMolecule(probe2);
		Deployment probe21 = newDeployment("mol21", "active", "cluster2");
		probe2.addDeployment(probe21);

		Molecule probe3 = new Molecule();
		probe3.name = "MOL03";
		probes.addMolecule(probe3);
		Deployment probe31 = newDeployment("mol31", "active", null);
		probe3.addDeployment(probe31);

		Molecule probe4 = new Molecule();
		probe4.name = "MOL04";
		probes.addMolecule(probe4);
		Deployment probe41 = newDeployment("mol41", "inActive", "cluster1");
		probe4.addDeployment(probe41);

		Molecule probe5 = new Molecule();
		probe5.name = "MOL05";
		probes.addMolecule(probe5);
		Deployment probe51 = newDeployment("mol51", "inActive", "cluster2");
		probe5.addDeployment(probe51);

		Molecule probe6 = new Molecule();
		probe6.name = "MOL06";
		probes.addMolecule(probe6);
		Deployment probe61 = newDeployment("mol61", "inActive", null);
		probe6.addDeployment(probe61);

		probes.save();
		return probes;
	}

	public MoleculeDatabase createSmallDatabaseWithActivityAndClustering(User owner, String[] activity, String[] clustering) {
		MoleculeDatabase probes = new MoleculeDatabase();
		probes.owner = owner;
		probes.name = "Small with Activity and Clustering";

		for (int i = 0; i < Math.min(activity.length, clustering.length); i++) {
			Molecule probe1 = new Molecule();
			probe1.name = "MOL" + i;
			probes.addMolecule(probe1);
			Deployment probe11 = newDeployment("mol" + i, activity[i], clustering[i]);
			probe1.addDeployment(probe11);
		}

		probes.save();
		return probes;
	}

	private static Deployment newDeployment(String name, String activity, String cluster) {
		Deployment probe = new Deployment();
		probe.parseName(name);
		if (activity != null) {
			probe.putProperty("activity", activity);
		}
		if (cluster != null) {
			probe.putProperty("cluster", cluster);
		}
		return probe;
	}

	public MoleculeDatabase createSmallDatabaseWithProperties(User owner, List<String> propertyNames) {
		MoleculeDatabase probes = new MoleculeDatabase();
		probes.owner = owner;
		probes.name = "Small database with properties";

		Molecule probe1 = new Molecule();
		probe1.name = "MOL01";
		probes.addMolecule(probe1);
		Deployment probe11 = createDeploymentWithProperties(propertyNames);
		probe1.addDeployment(probe11);

		probes.save();
		return probes;
	}

	private Deployment createDeploymentWithProperties(List<String> propertyNames) {
		Deployment probe = new Deployment();
		probe.name = "";
		for (String propertyName : propertyNames) {
			probe.putProperty(propertyName, "don't care");
		}
		return probe;
	}

	public MoleculeDatabase createSmallDatabaseWithPropertyNamesAndValues(User owner, List<String> propertyNames) {
		MoleculeDatabase molecules = new MoleculeDatabase();
		molecules.owner = owner;
		molecules.name = "Small database with properties";

		Molecule molecule1 = new Molecule();
		molecule1.name = "MOL01";
		molecules.addMolecule(molecule1);
		List<String> values = new ArrayList<String>();
		for (Integer i = 0; i < propertyNames.size(); i++) {
			values.add(i.toString());
		}
		Deployment probe1 = createDeploymentWithPropertyNamesAndValue("probe1", propertyNames, values);
		molecule1.addDeployment(probe1);

		for (Integer i = 0; i < propertyNames.size(); i++) {
			Logger.info("Adding deployment " + probe1 + " with property " + propertyNames.get(i) + " with value " + values.get(i));
		}

		Deployment probe2 = createDeploymentWithPropertyNamesAndValue("probe2", propertyNames, values);
		molecule1.addDeployment(probe2);

		for (Integer i = 0; i < propertyNames.size(); i++) {
			Logger.info("Adding deployment " + probe2 + " with property " + propertyNames.get(i) + " with value " + values.get(i));
		}

		values.clear();
		for (Integer i = 2; i < propertyNames.size() + 2; i++) {
			values.add(i.toString());
		}
		Deployment probe3 = createDeploymentWithPropertyNamesAndValue("probe3", propertyNames, values);
		molecule1.addDeployment(probe3);

		for (Integer i = 0; i < propertyNames.size(); i++) {
			Logger.info("Adding deployment " + probe3 + " with property " + propertyNames.get(i) + " with value " + values.get(i));
		}

		molecules.save();
		return molecules;
	}

	public Deployment createDeploymentWithPropertyNamesAndValue(String name, List<String> propertyNames,
			List<String> propertyValues) {
		Deployment probe = new Deployment();
		probe.name = name;

		for (int i = 0; i < propertyNames.size(); i++) {
			probe.putProperty(propertyNames.get(i), propertyValues.get(i));
			// // probe.putPropertyThroughSql(propertyNames.get(i),
			// propertyValues.get(i));
		}
		return probe;
	}

	private void calculateWithFakeInverseDockingEngine(ComparisonExperiment experiment) {
		experiment.engineId = Factory.FAKE_INVERSE_DOCKING;
		Engine fake = engineFactory.getEngine(experiment);
		configureEngineAndCalculate(experiment, fake);
	}

	private void calculateWithFakeDockingEngine(ComparisonExperiment experiment) {
		experiment.engineId = Factory.FAKE_DOCKING;
		Engine fake = engineFactory.getEngine(experiment);
		configureEngineAndCalculate(experiment, fake);
	}

	private void configureEngineAndCalculate(ComparisonExperiment experiment, Engine fake) {
		fake.setExperiment(experiment);
		fake.createAlignments();
		for (Alignment similarity : experiment.alignments) {
			fake.calculate(similarity);
		}
	}

	public ComparisonExperiment createExperiment() {
		return createExperiment(User.findByUserName("aperreau"));
	}
	
	public ComparisonExperiment createExperiment(User user) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Test experiment";
		experiment.engineId = Factory.AUTODOCK_VINA;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.probeMolecules = MoleculeDatabase.find("byName", "Proteins").first();
		experiment.targetMolecules = MoleculeDatabase.find("byName", "Salts").first();
		experiment.owner = user;
		experiment.box = createAligmentBox();
		experiment.status = ExperimentStatus.QUEUED;
		experiment.save();
		return experiment;
	}


	public void addGuestsToExperiment(ComparisonExperiment comparisonExperiment, String[] guests) {
		for (String guest : guests) {
			ComparisonExperimentGuest comparisonExperimentGuest = new ComparisonExperimentGuest(User.findByUserName(guest),
					comparisonExperiment);
			comparisonExperimentGuest.save();
		}
	}

	public void addGuestsToMoleculeDatabase(MoleculeDatabase db, String[] guests) {
		for (String guest : guests) {
			MoleculeDatabaseGuest moleculeDatabaseGuest = new MoleculeDatabaseGuest(User.findByUserName(guest), db);
			moleculeDatabaseGuest.save();
		}
	}

	public void addGuestsToQsarExperiment(QsarExperiment qsarExperiment, String[] guests) {
		for (String guest : guests) {
			QsarExperimentGuest qsarExperimentGuest = new QsarExperimentGuest(User.findByUserName(guest),
					qsarExperiment);
			qsarExperimentGuest.save();
		}
	}

	public String getAllUsersNotInList() {
		List<User> users = User.findAll();
		String usersStr = "";
		for (User user : users) {
			if (!user.username.equals("xmaresma") && !user.username.equals("aperreau")&& !user.username.equals("lnavarro")) {
				usersStr += user.username + ",";
			}
		}
		return usersStr;
	}

	public ComparisonExperiment createFingerprintExperiment(int fingerprintId, String fingerprintName) {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Test experiment";
		experiment.owner = User.findByUserName("aperreau");
		experiment.engineId = fingerprintId;
		experiment.engineName = fingerprintName;
		experiment.chargeType = Factory.GASTEIGER;
		experiment.probeMolecules = this.createSmallDatabaseWithoutProperties(experiment.owner);
		experiment.targetMolecules = this.createSmallDatabaseWithoutProperties(experiment.owner);
		experiment.box = createAligmentBox();
		experiment.status = ExperimentStatus.QUEUED;
		experiment.save();

		return experiment;
	}

	public ComparisonExperiment createInverseDockingExperiment() {
		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.owner = User.findByUserName("aperreau");
		experiment.targetMolecules = createSingleDeploymentDatabase();
		experiment.probeMolecules = createSmallDatabaseWithActivityAndClustering(experiment.owner);
		experiment.engineId = Factory.FAKE_INVERSE_DOCKING;
		experiment.engineName = Factory.FAKE_INVERSE_DOCKING_ENGINE_NAME;
		calculateWithFakeInverseDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;
		return experiment;
	}

	public ComparisonExperiment create1mol1dep1propEvaluatedExperiment() {
		User owner = User.findByUserName("xarroyo");

		MoleculeDatabase database = new MoleculeDatabase();
		database.owner = owner;
		database.name = "Database 1mol-1dep-1prop";

		Molecule molecule = new Molecule();
		molecule.name = "MOL01";
		database.addMolecule(molecule);
		Deployment deployment = new Deployment();
		deployment.name = "DEP01";
		deployment.molecule = molecule;
		molecule.addDeployment(deployment);
		ChemicalProperty property = new ChemicalProperty("PRO01", "value", deployment);
		deployment.properties.add(property);

		ComparisonExperiment experiment = new ComparisonExperiment();
		experiment.name = "Experiment 1mol1dep1prop";
		experiment.engineId = 1;
		experiment.engineName = Factory.AUTODOCK_VINA_NAME;
		experiment.owner = owner;
		experiment.targetMolecules = database;
		experiment.probeMolecules = database;
		experiment.box = createAligmentBox();

		experiment.creationDate = 0;
		experiment.startingDate = 0;
		experiment.endDate = 0;
		experiment.runTime = 0;

		calculateWithFakeInverseDockingEngine(experiment);
		experiment.status = ExperimentStatus.FINISHED;

		return experiment;
	}

	public QsarExperiment create1mol1dep1propEvaluatedQsarExperiment() {
		User owner = User.findByUserName("xmaresma");

		MoleculeDatabase database = new MoleculeDatabase();
		database.owner = owner;
		database.name = "Database 1mol-1dep-1prop";

		Molecule molecule = new Molecule();
		molecule.name = "MOL01";
		database.addMolecule(molecule);
		Deployment deployment = new Deployment();
		deployment.name = "DEP01";
		deployment.molecule = molecule;
		molecule.addDeployment(deployment);
		ChemicalProperty property = new ChemicalProperty("PRO01", "value", deployment);
		deployment.properties.add(property);

		QsarExperiment experiment = new QsarExperiment();
		experiment.owner = owner;
		experiment.name = "Experiment 1mol1dep1prop";
		experiment.molecules = database;
		experiment.externalSelectionType = "Random";
		experiment.externalPercentage = 20;
		experiment.activityProperty = "propertyName";
		experiment.numberOfLatentValues = "30";
		experiment.numPropertiesShown = "26";
		experiment.status = "Running";
		experiment.parent = 0;
		experiment.qsarType = Factory.QSAR_PLS;
		experiment.numberOfLatentValues = "26";
		experiment.scaled = true;
		experiment.searchDepth = 2;
		experiment.atomLabelType = 3;
		experiment.validationType = "LOO";
		experiment.coeficientsFilePath = "noFile";
		experiment.results = new ArrayList<QsarResult>();

		experiment.creationDate = 0;
		experiment.startingDate = 0;
		experiment.endDate = 0;
		experiment.runTime = 0;

		experiment.status = ExperimentStatus.FINISHED;

		return experiment;
	}

	public QsarExperiment createQsarExperiment(User owner) {
		QsarExperiment experiment = new QsarExperiment();
		experiment.owner = owner;
		experiment.name = "Test experiment";
		experiment.molecules = createSmallDatabaseWithoutProperties(owner);
		experiment.externalSelectionType = "Random";
		experiment.externalPercentage = 20;
		experiment.activityProperty = "propertyName";
		experiment.numberOfLatentValues = "30";
		experiment.numPropertiesShown = "26";
		experiment.status = "Running";
		experiment.parent = 0;
		experiment.qsarType = Factory.QSAR_PLS;
		experiment.numberOfLatentValues = "26";
		experiment.scaled = true;
		experiment.searchDepth = 2;
		experiment.atomLabelType = 3;
		experiment.validationType = "LOO";
		experiment.coeficientsFilePath = "noFile";
		experiment.save();
		return experiment;
	}

	public QsarExperiment createQsarExperimentWithRulesAndDeploymentsWithProperties(User owner) {
		QsarExperiment experiment = new QsarExperiment();
		experiment.owner = owner;
		experiment.name = "Test experiment";
		List<String> propertyNames = new ArrayList<String>();
		propertyNames.add("prop1");
		propertyNames.add("prop2");
		propertyNames.add("prop3");
		propertyNames.add("prop4");
		propertyNames.add("deploymentOrder");
		experiment.molecules = createSmallDatabaseWithPropertyNamesAndValues(owner, propertyNames);
		experiment.externalSelectionType = "Random";
		experiment.externalPercentage = 20;
		experiment.activityProperty = "prop4";
		experiment.numberOfLatentValues = "30";
		experiment.numPropertiesShown = "26";
		experiment.status = "Running";
		experiment.parent = 0;
		experiment.qsarType = Factory.QSAR_PLS;
		experiment.numberOfLatentValues = "26";
		experiment.rulesList = "prop1\tless\t2\nprop2\tgreater\t0\nprop3\tlessORequal\t1\nprop456\tlessORequal\t1\n";
		experiment.save();
		return experiment;
	}

	public QsarExperiment createChildQsarExperiment(long parentQsarExperientId, User owner) {
		QsarExperiment experiment = new QsarExperiment();
		experiment.owner = owner;
		experiment.name = "Test Multi Experiment";
		experiment.molecules = createSmallDatabaseWithoutProperties(owner);
		experiment.externalSelectionType = "Random";
		experiment.externalPercentage = 20;
		experiment.parent = parentQsarExperientId;
		experiment.save();
		return experiment;
	}

	public void createSequenceForTestDb() {
		String query = "create sequence if not exists hibernate_sequence";
		try {
			JPA.em().createNativeQuery(query).executeUpdate();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
	}

}