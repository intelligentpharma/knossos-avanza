package models;

import java.util.ArrayList;
import java.util.List;

import models.AlignmentBox;

import org.junit.*;


import play.test.*;

public class AlignmentBoxTest extends UnitTest {

	@Test
	public void constructsCorrectlyWithStrings() {
		List<String> data = new ArrayList<String>();
		data.add("123.3");
		data.add("3221.1");
		data.add("222.2");
		data.add("123");
		data.add("234");
		data.add("345");
		AlignmentBox info = new AlignmentBox(data);
		assertEquals(info.centerX, 123.3, 0.0001);
		assertEquals(info.centerY, 3221.1, 0.0001);
		assertEquals(info.centerZ, 222.2, 0.0001);
		assertEquals(info.sizeX, 123);
		assertEquals(info.sizeY, 234);
		assertEquals(info.sizeZ, 345);
	}

	@Test
	public void constructsCorrectlyWithIntegers() {
		AlignmentBox info = new AlignmentBox(1,2,3,4,5,6);
		assertEquals(info.centerX, 1, 0.0001);
		assertEquals(info.centerY, 2, 0.0001);
		assertEquals(info.centerZ, 3, 0.0001);
		assertEquals(info.sizeX, 4);
		assertEquals(info.sizeY, 5);
		assertEquals(info.sizeZ, 6);
	}

	@Test
	public void constructsCorrectlyWithDoubles() {
		AlignmentBox info = new AlignmentBox(1.0,2.0,3.0,4,5,6);
		assertEquals(info.centerX, 1.0, 0.0001);
		assertEquals(info.centerY, 2.0, 0.0001);
		assertEquals(info.centerZ, 3.0, 0.0001);
		assertEquals(info.sizeX, 4);
		assertEquals(info.sizeY, 5);
		assertEquals(info.sizeZ, 6);
	}

}
