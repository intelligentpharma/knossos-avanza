package utils.complexityEstimation;

import java.io.FileNotFoundException;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.AssertTrue;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;

public class ComplexityEstimationTest extends UnitTest{
	
	@Test
	public void sampleTest() throws FileNotFoundException {
		String type;
		int parameter;
		int complexity;
		
		type="SeleneAutodock";
		parameter = 5;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(4833, complexity);
		
		type="SeleneAutodock";
		parameter = 4;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(4833, complexity);
		
		type="SeleneAutodock";
		parameter = 6;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(76065, complexity);
		
		type="SeleneAutodock";		
		parameter = 152;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(44401, complexity);
		
		type="Hercules";		
		parameter = 5;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(879960, complexity);
		
		type="InverseAD";		
		parameter = 5;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(972760, complexity);
		
		type="SeleneVina";		
		parameter = 5;
		complexity = ComplexityEstimationCore.estimateComplexity(type,parameter);
		assertEquals(832194, complexity);
	}
	
}