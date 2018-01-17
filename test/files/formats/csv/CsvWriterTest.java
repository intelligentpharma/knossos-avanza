package files.formats.csv;

import java.io.File;
import java.io.IOException;

import junitx.framework.FileAssert;

import org.junit.Test;

import files.FileUtilsImpl;
import files.formats.csv.CsvWriter;
import files.formats.csv.CsvWriterImpl;

import play.test.UnitTest;

public class CsvWriterTest extends UnitTest {

	@Test
	public void writeCorrectly() throws IOException{
		String outputFilePath = "/tmp/properties.csv";
		CsvWriter writer = new CsvWriterImpl("hola,adeu\n1,2\n", outputFilePath);
		writer.setFileUtils(new FileUtilsImpl());
		writer.writeToCsv();
		FileAssert.assertEquals(new File("test-files/provaCsvWriter.csv"),new File(outputFilePath));
	}
}
