package utils;

import org.junit.*;
import play.test.*;
import utils.MathUtils;

public class MathUtilsTest extends UnitTest{

	@Test
	public void roundNumbers(){
		assertEquals(1,MathUtils.round(1.2,0),0.0000000001);
		assertEquals(1,MathUtils.round(1.2354045,0),0.00000001);
		assertEquals(1.,MathUtils.round(1.2354045,0),0.00000001);
		assertEquals(0.2,MathUtils.round(0.2,1),0.0000000001);
		assertEquals(0.24,MathUtils.round(0.24,2),0.00000001);
		assertEquals(0.25,MathUtils.round(0.247,2),0.0000000001);
		assertEquals(100,MathUtils.round(123.45,-2),0.0000000001);
	}

	@Test //useful just for cobertura purposes, otherwise the class is not fully covered
	public void roundNumbersWithInstantiation(){
		MathUtils utils = new MathUtils();

		assertEquals(1, utils.round(1.2,0),0.0000000001);
	}
	
	@Test
	public void sumVector(){
		int[] vector = {0,2,3,4};
		assertEquals(9,MathUtils.sum(vector));
	}
	
	@Test
	public void sumIntVectorPositionsWhenPositionsAreInBounds(){
		int[] vector = {0,2,3,4};		
		assertEquals(9,MathUtils.sum(vector, 4));
	}
	
	@Test
	public void sumIntVectorPositionsReturnsZeroWhenPositionsAreOffBounds(){
		int[] vector = {0,2,3,4};		
		assertEquals(0,MathUtils.sum(vector, 5));
	}
	
	@Test
	public void sumStringVectorPositionsWhenPositionsAreInBounds(){
		String[] vector = {"0","2","3","4"};		
		assertEquals(9,MathUtils.sum(vector, 4));
	}
	
	@Test
	public void sumStringVectorPositionsReturnsZeroWhenPositionsAreOffBounds(){
		String[] vector = {"0","2","3","4"};		
		assertEquals(0,MathUtils.sum(vector, 5));
	}
	
}
