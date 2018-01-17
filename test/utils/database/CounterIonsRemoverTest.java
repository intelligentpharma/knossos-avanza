package utils.database;

import java.io.File;
import java.io.IOException;

import junitx.framework.FileAssert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import play.test.UnitTest;
import utils.FactoryImpl;
import utils.database.CounterIonsRemover;

public class CounterIonsRemoverTest extends UnitTest{

	@Test
	public void counterIonsAreRemovedCorrectly() throws IOException{
		CounterIonsRemover counterIonsRemover = new FactoryImpl().getCounterIonsRemover();

		File originalFile = new File("test-files/SmilesWithCounterIons.smi");
		File tempFile = new File("test-files/SmilesWithCounterIonsToWork.smi");
		File expectedFile = new File("test-files/SmilesWithCounterIonsRemoved_withCAN.smi");

		FileUtils.copyFile(originalFile, tempFile);
		
		counterIonsRemover.removeCounterIons(tempFile.getCanonicalPath());
		
		FileAssert.assertEquals(expectedFile,tempFile);
		tempFile.delete();
	}
	
	@Test
	public void counterIonsRemovalFailsWhenSomeMoleculeIsntLikedByOpenBabel() throws IOException{
		CounterIonsRemover counterIonsRemover = new FactoryImpl().getCounterIonsRemover();

		File originalFile = new File("test-files/SmilesWithErrorCounterIons.smi");
		File tempFile = new File("test-files/SmilesWithCounterIonsToWork.smi");

		FileUtils.copyFile(originalFile, tempFile);
		tempFile.deleteOnExit();
		
		try{
			counterIonsRemover.removeCounterIons(tempFile.getCanonicalPath());
			fail("Should have thrown exception");
		}catch(RuntimeException e){			
			FileAssert.assertEquals(originalFile, tempFile);
		}
	}
	
}
