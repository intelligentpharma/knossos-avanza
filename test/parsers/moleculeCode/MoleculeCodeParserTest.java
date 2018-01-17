package parsers.moleculeCode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import play.test.UnitTest;

public class MoleculeCodeParserTest extends UnitTest{

	MoleculeCodeParser parser;
	
	
	@Test
	public void singleNpolAtom(){
		assertEquals(1, getNPolFromFormula("O"));
	}

	@Test
	public void nPolCountsNumberOfOxigensWhenThereIsNothingElse(){
		
		assertEquals(2, getNPolFromFormula("O2"));
		assertEquals(12, getNPolFromFormula("O12"));
	}
	
	@Test
	public void nPolCountsNumberOfOxigensWhenThereAreOtherAtoms(){
		assertEquals(2, getNPolFromFormula("O2C13"));
		assertEquals(12, getNPolFromFormula("HO12C13"));
	}

	@Test
	public void nPolCountsNumberOfNitrogensWhenThereIsNothingElse(){
		assertEquals(2, getNPolFromFormula("N2"));
		assertEquals(12, getNPolFromFormula("N12"));
	}

	@Test
	public void nPolCountsNumberOfNitrogensWhenThereAreOtherAtoms(){
		assertEquals(2, getNPolFromFormula("N2C13"));
		assertEquals(12, getNPolFromFormula("HN12C13"));
	}

	@Test
	public void nPolAddsNumberOfOxigensAndNitrogens(){
		assertEquals(15, getNPolFromFormula("N2O13"));
		assertEquals(13, getNPolFromFormula("HN12Cl13OC"));
	}
	
	@Test
	public void nheaDoesNotCountHidrogens(){
		assertEquals(0, getNheaFromFormula("H"));
		assertEquals(0, getNheaFromFormula("H2"));
		assertEquals(1, getNheaFromFormula("H2O"));
		assertEquals(2, getNheaFromFormula("ClH2O"));
	}
	
	@Test
	public void moleculesCanHaveCharges(){
		assertEquals(2, getNheaFromFormula("ClH2O+"));
		assertEquals(2, getNheaFromFormula("ClH2O+2"));
		assertEquals(2, getNheaFromFormula("ClH2O-2"));
	}
	
	@Test
	public void moleculeThatBreaksParser(){
		
		assertEquals(24, getNheaFromFormula("C15H18N4O5"));
		assertEquals(9, getNPolFromFormula("C15H18N4O5"));
	}

	private int getNPolFromFormula(String string) {
		parseString(string);
		return parser.npol;
	}

	private int getNheaFromFormula(String string) {
		parseString(string);
		return parser.nhea;
	}

	private void parseString(String string) {
		InputStream stream = null;
		try {
			stream = new ByteArrayInputStream(string.getBytes("UTF-8"));
			parser = new MoleculeCodeParser(stream);
			parser.parse();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
