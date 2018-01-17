package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junitx.framework.FileAssert;

import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;
import utils.CompressionUtils;
import utils.CompressionUtilsImpl;

public class CompressionUtilsTest extends UnitTest {

	CompressionUtils compressionUtils;
	
	@Before
	public void setup() {
		compressionUtils = new CompressionUtilsImpl();
	}
	
	@Test
	//Tar files have a modification date that makes tar files different 
	//so we need to tar & untar and then check the files individually 
	public void tarFileIsCorrect() throws IOException{
		List<File> files = new ArrayList<File>();
		files.add(new File("test-files/json/short.json"));
		files.add(new File("test-files/file1.txt"));
		File outputFile = File.createTempFile("testTar", ".tar");
		//File expectedFile = new File("test-files/testTar.tar");
		
		compressionUtils.tarFiles(files, outputFile);

		List<File> outputFiles = compressionUtils.untarFiles(outputFile);

		FileAssert.assertEquals(files.get(0), outputFiles.get(0));
		FileAssert.assertEquals(files.get(0), outputFiles.get(0));
	}

	@Test
	public void gzFileIsCorrect() throws IOException{
		File inputFile = new File("test-files/json/short.json");
		File outputFile = File.createTempFile("testGz", ".gz");
		File expectedFile = new File("test-files/testGz.gz");
		
		compressionUtils.compress(inputFile, outputFile);

		FileAssert.assertEquals(expectedFile, outputFile);
	}
	
	@Test
	public void ungzipFileCorrectly() throws IOException {
		File inputFile = new File("test-files/testGz.gz");
		File outputFile = File.createTempFile("testGz", ".out");
		File expectedFile = new File("test-files/json/short.json");

		compressionUtils.uncompress(inputFile, outputFile);

		FileAssert.assertEquals(expectedFile, outputFile);
	}
	
	@Test
	public void untarFileCorrectly(){
		List<File> expectedFiles = new ArrayList<File>();
		expectedFiles.add(new File("test-files/json/short.json"));
		expectedFiles.add(new File("test-files/file1.txt"));

		File inputFile = new File("test-files/testTar.tar");
		List<File> actualFiles = compressionUtils.untarFiles(inputFile);

		for(int i=0; i<actualFiles.size(); i++){
			FileAssert.assertEquals(expectedFiles.get(i), actualFiles.get(i));
		}
	}
}
