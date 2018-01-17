package controllers;

import java.util.List;

import engine.factory.EngineFactoryImpl;

import models.ComparisonExperiment;
import models.ModelForListing;
import models.MoleculeDatabase;
import models.QsarExperiment;
import models.Scoring;
import models.User;

public class ModelReadingUtils {

	public static List<ComparisonExperiment> getExperimentsOwnedBy(String username) {
		User user = User.findByUserName(username);
		List<ComparisonExperiment> experiments = ComparisonExperiment.findAllOwnedBy(user);
		return experiments;
	}

	public static List<Scoring> getScoringsFromExperiment(long experimentId) {
		ComparisonExperiment experiment = ComparisonExperiment.findById(experimentId);
		List<Scoring> scorings = Scoring.findByExperiment(experiment);
		return scorings;
	}

	public static List<ModelForListing> getListOfEngines() {
		List<ModelForListing> engines = EngineFactoryImpl.findAll();
		return engines;
	}

	public static List<MoleculeDatabase> getMoleculeDatabasesOwnedBy(String username) {
		User user = User.findByUserName(username);
		List<MoleculeDatabase> moleculeDatabases = MoleculeDatabase.findAllOwnedBy(user);
		return moleculeDatabases;
	}
	
	public static List<QsarExperiment> getQsarExperimentsOwnedBy(String username) {
		User user = User.findByUserName(username);
		List<QsarExperiment> experiments = QsarExperiment.findAllOwnedBy(user);
		return experiments;
	}
}
