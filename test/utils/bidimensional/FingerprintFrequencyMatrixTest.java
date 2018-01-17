package utils.bidimensional;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junitx.framework.FileAssert;

import org.junit.Before;
import org.junit.Test;

import play.Logger;
import play.test.UnitTest;
import utils.Factory;
import utils.bidimensional.Compound;
import utils.bidimensional.FingerprintFrequencyMatrixImpl;
import utils.bidimensional.Smile;

public class FingerprintFrequencyMatrixTest extends UnitTest {

	public FingerprintFrequencyMatrixImpl table;
	public Factory factory;

	@Before
	public void setup() {
		table = new FingerprintFrequencyMatrixImpl();
		factory = createMock(Factory.class);
		table.setFactory(factory);
	}

	@Test
	public void singleAddedSmileIsReturnedInList() {

		Smile smile = new Smile("CCOOCCOO","mol1");
		expect(factory.createSmile("CCOOCCOO","mol1")).andReturn(smile);

		replay(factory);
		table.addCompound("CCOOCCOO","mol1");
		List<Compound> smiles = table.getCompounds();
		assertEquals(1, smiles.size());
		assertTrue(smiles.contains(smile));
	}

//	@Test
	public void returnsAllLingosFromSingleAddedSmiles() {
		Smile smile = createMock(Smile.class);
		expect(smile.getFrequencyList().getFingerprints()).andReturn(null);

	}
	
	@Test 
	public void generatesTotalLingoFrequencyMatrixFileCorrectly() throws IOException{
		expect(factory.createSmile("CCC","mol1")).andReturn(new Smile("CCC","mol1"));
		expect(factory.createSmile("CCC1CCC1","mol2")).andReturn(new Smile("CCC1CCC1","mol2"));
		expect(factory.createSmile("CClCClCCl","mol3")).andReturn(new Smile("CClCClCCl","mol3"));
		replay(factory);
		table.addCompound("CCC","mol1");
		table.addCompound("CCC1CCC1","mol2");
		table.addCompound("CClCClCCl","mol3");
		
		File outputFile = table.getTotalFingerprintFrequencyMatrixFile(23);
		Logger.debug("outputFile name %s",outputFile.getAbsolutePath());
		File correctFile = new File("test-files/exampleLingoFrequencyMatrix");
		
		FileAssert.assertEquals(correctFile, outputFile);
	}

	@Test 
	public void generatesTotalLingoFrequencyMatrixxFileOfExternalSetCorrectly() throws IOException{
		expect(factory.createSmile("CCC","mol1")).andReturn(new Smile("CCC","mol1"));
		expect(factory.createSmile("CCC1CCC1","mol2")).andReturn(new Smile("CCC1CCC1","mol2"));
		expect(factory.createSmile("CClCClCCl","mol3")).andReturn(new Smile("CClCClCCl","mol3"));
		replay(factory);
		table.addCompound("CCC","mol1");
		table.addCompound("CCC1CCC1","mol2");
		table.addCompound("CClCClCCl","mol3");
		
		ArrayList<Compound> smileSet = new ArrayList<Compound>();
		smileSet.add(new Smile("0CCCClCCl","mol4"));
		File outputFile = table.getTotalFingerprintFrequencyMatrixFileOfCompoundsSet(23, smileSet);
		Logger.debug("outputFile name %s",outputFile.getAbsolutePath());
		File correctFile = new File("test-files/exampleLingoFrequencyMatrixExternalSet");
		
		FileAssert.assertEquals(correctFile, outputFile);
	}

}
