package utils.experiment;

import models.MapsSimilarities;
import models.PhysicalSimilarities;
import models.Ponderation;
import models.Scoring;

import org.junit.Test;

import play.test.UnitTest;
import utils.experiment.AlignmentComparator;

public class AlignmentComparatorTest extends UnitTest {

	@Test
	public void comparatorWorksCorrectlyWithPhysicalSimilarities(){
		AlignmentComparator comparator = new AlignmentComparator(new Ponderation());
		PhysicalSimilarities alignment1 = new PhysicalSimilarities();
		PhysicalSimilarities alignment2 = new PhysicalSimilarities();

		alignment1.energy = 1.0;
		alignment2.energy = 2.0;
		assertEquals(-1, comparator.compare(alignment1, alignment2));

		alignment1.energy = 2.0;
		alignment2.energy = 1.0;
		assertEquals(1, comparator.compare(alignment1, alignment2));

		alignment1.energy = 1.0;
		alignment2.energy = 1.0;
		assertEquals(0, comparator.compare(alignment1, alignment2));
	}

	@Test
	public void comparatorWorksCorrectlyWithMapsSimilarities(){
		Ponderation ponderation = new Ponderation();
		AlignmentComparator comparator = new AlignmentComparator(ponderation);
		MapsSimilarities alignment1 = new MapsSimilarities();
		Scoring scoring1 = new Scoring();
		scoring1.ponderation = ponderation;
		alignment1.add(scoring1);
		MapsSimilarities alignment2 = new MapsSimilarities();
		Scoring scoring2 = new Scoring();
		scoring2.ponderation = ponderation;
		alignment2.add(scoring2);

		scoring1.score = 1.0;
		scoring2.score = 2.0;
		assertEquals(1, comparator.compare(alignment1, alignment2));

		scoring1.score = 2.0;
		scoring2.score = 1.0;
		assertEquals(-1, comparator.compare(alignment1, alignment2));

		scoring1.score = 1.0;
		scoring2.score = 1.0;
		assertEquals(0, comparator.compare(alignment1, alignment2));
	}
}
