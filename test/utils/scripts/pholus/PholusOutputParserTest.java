package utils.scripts.pholus;

import java.util.List;

import models.ComparisonExperiment;
import models.Ponderation;
import models.User;

import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;
import utils.Factory;
import utils.experiment.TestDataCreator;
import utils.scripts.pholus.PholusInput;
import utils.scripts.pholus.PholusOutputImpl;

public class PholusOutputParserTest extends UnitTest {

	private static User owner;
	private static ComparisonExperiment experiment;
	private static TestDataCreator creator;

	@BeforeClass
	public static void setup() {
		Fixtures.deleteDatabase();
		Fixtures.loadModels("data.yml");
		creator = new TestDataCreator();
		owner = User.findByUserName("xarroyo");
		experiment = creator.getSmallEvaluatedExperiment(owner);
	}

	public void singlePonderationParsedCorrectly(){
		String ponderationText = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
		"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.2 1";

		PholusInput pholusData = EasyMock.createMock(PholusInput.class);
		EasyMock.expect(pholusData.getTrainingType()).andReturn(Factory.LOGISTIC_REGRESSION).times(2);
		EasyMock.expect(pholusData.getTrainingType()).andReturn(Factory.PHOLUS).once();
		EasyMock.expect(pholusData.getName()).andReturn("pholus").anyTimes();
		EasyMock.expect(pholusData.getExperiment()).andReturn(experiment).anyTimes();

		EasyMock.replay(pholusData);
		
		PholusOutputImpl parser = new PholusOutputImpl(pholusData);
		parser.parse(ponderationText);

		List<Ponderation> ponderations = parser.getPonderations();
		
		
		assertEquals(1, ponderations.size());
		Ponderation ponderation = ponderations.get(0);
		assertNotNull(ponderation);
		assertEquals(experiment, ponderation.source);
		assertEquals(experiment.owner, ponderation.source.owner);
		assertEquals(1, ponderation.getWeights().A, 0.01);
		assertEquals(2, ponderation.getWeights().Br, 0.01);
		assertEquals(3, ponderation.getWeights().C, 0.01);
		assertEquals(4, ponderation.getWeights().Ca, 0.01);
		assertEquals(5, ponderation.getWeights().Cl, 0.01);
		assertEquals(6, ponderation.getWeights().F, 0.01);
		assertEquals(7, ponderation.getWeights().Fe, 0.01);
		assertEquals(8, ponderation.getWeights().HD, 0.01);
		assertEquals(9, ponderation.getWeights().I, 0.01);
		assertEquals(10, ponderation.getWeights().Mg, 0.01);
		assertEquals(11, ponderation.getWeights().Mn, 0.01);
		assertEquals(12, ponderation.getWeights().N, 0.01);
		assertEquals(13, ponderation.getWeights().NA, 0.01);
		assertEquals(14, ponderation.getWeights().NS, 0.01);
		assertEquals(15, ponderation.getWeights().OA, 0.01);
		assertEquals(16, ponderation.getWeights().OS, 0.01);
		assertEquals(17, ponderation.getWeights().P, 0.01);
		assertEquals(18, ponderation.getWeights().S, 0.01);
		assertEquals(19, ponderation.getWeights().SA, 0.01);
		assertEquals(20, ponderation.getWeights().Zn, 0.01);
		assertEquals(21, ponderation.getWeights().d, 0.01);
		assertEquals(22, ponderation.getWeights().e, 0.01);
		assertEquals(1.2, ponderation.bedroc, 0.01);
		assertEquals(experiment, ponderation.source);
		assertEquals(Factory.LOGISTIC_REGRESSION, ponderation.trainingType);

	}

	@Test
	public void multiplePonderationsParsedCorrectly() {
		String ponderationText = "1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
		"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.0\n"+
		"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
		"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.2 1\n"+
		"1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0 11.0 12.0 13.0 " +
				"14.0 15.0 16.0 17.0 18.0 19.0 20.0 21.0 22.0 1.5 2\n";

		PholusInput pholusData = EasyMock.createMock(PholusInput.class);
		EasyMock.expect(pholusData.getTrainingType()).andReturn(Factory.LOGISTIC_REGRESSION).times(2);
		EasyMock.expect(pholusData.getTrainingType()).andReturn(Factory.PHOLUS).once();
		EasyMock.expect(pholusData.getName()).andReturn("pholus").anyTimes();
		EasyMock.expect(pholusData.getExperiment()).andReturn(experiment).anyTimes();

		EasyMock.replay(pholusData);
		
		PholusOutputImpl parser = new PholusOutputImpl(pholusData);
		parser.parse(ponderationText);

		List<Ponderation> ponderations = parser.getPonderations();
		
		assertEquals(3, ponderations.size());
		Ponderation ponderationAll = ponderations.get(0);
		Ponderation ponderationNC1 = ponderations.get(1);
		Ponderation ponderationNC2 = ponderations.get(2);
		assertNotNull(ponderationAll);
		assertNotNull(ponderationNC1);
		assertNotNull(ponderationNC2);
		assertEquals(experiment, ponderationAll.source);
		assertEquals(experiment, ponderationNC1.source);
		assertEquals(experiment, ponderationNC2.source);
		assertEquals(Factory.LOGISTIC_REGRESSION, ponderationAll.trainingType);
		assertEquals(Factory.LOGISTIC_REGRESSION, ponderationNC1.trainingType);
		assertEquals(Factory.PHOLUS, ponderationNC2.trainingType);
		assertEquals(1.0, ponderationAll.bedroc, 0.01);
		assertEquals(1.2, ponderationNC1.bedroc, 0.01);
		assertEquals(1.5, ponderationNC2.bedroc, 0.01);
		assertEquals("Small Experiment_pholus_ALL_LOGISTIC REGRESSION (under evaluation) (1.000)", ponderationAll.name);
		assertEquals("Small Experiment_pholus_NC1_LOGISTIC REGRESSION (under evaluation) (1.200)", ponderationNC1.name);
		assertEquals("Small Experiment_pholus_NC2_PHOLUS (1.500)", ponderationNC2.name);
		
		EasyMock.verify(pholusData);
	}
	
}
