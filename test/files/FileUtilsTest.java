package files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junitx.framework.FileAssert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import files.FileUtils;
import files.FileUtilsImpl;

import play.Logger;
import play.test.UnitTest;

public class FileUtilsTest extends UnitTest {

	FileUtils fileUtils;
	String outputFileName;
	List<String> createdFiles;
	
	@Before
	public void setup(){
		fileUtils = new FileUtilsImpl();
		createdFiles = new ArrayList<String>();
	}
	
	@After
	public void cleanup(){
		for(String fileName : createdFiles){
			File destinationFile = new File(fileName);
			destinationFile.delete();		
		}
	}
	
	@Test
	public void erasesFileWithNameAsPattern() throws Exception {
		String pathName = "tmp/file1.pdbqt";
		File file = new File(pathName);
		file.createNewFile();
		createdFiles.add(pathName);
		fileUtils.eraseFilesInDirectoryMatchingPattern("tmp", "file1\\.pdbqt");
		assertFalse(file.exists());
	}

	@Test
	public void fileWithDifferentNameIsNotErased() throws Exception {
		String pathName = "tmp/file2.pdbqt";
		File file = new File(pathName);
		file.createNewFile();
		createdFiles.add(pathName);
		fileUtils.eraseFilesInDirectoryMatchingPattern("tmp", "file1\\.pdbqt");
		assertTrue(file.exists());
	}

	@Test
	public void erasesFileWithPattern() throws Exception {
		String pathName = "tmp/file1.pdbqt";
		File file = new File(pathName);
		file.createNewFile();
		createdFiles.add(pathName);
		fileUtils.eraseFilesInDirectoryMatchingPattern("tmp", "file.*pdbqt");
		assertFalse(file.exists());
		Logger.info("erasesFileWithPattern");
	}

	@Test
	public void erasesAllFilesWithPattern() throws Exception {
		Logger.info("erasesAllFilesWithPattern");
		String pathName1 = "tmp/file1.pdbqt";
		File file1 = new File(pathName1);
		file1.createNewFile();
		createdFiles.add(pathName1);
		String pathName2 = "tmp/file2.pdbqt";
		File file2 = new File(pathName2);
		file2.createNewFile();
		createdFiles.add(pathName2);
		String pathName3 = "tmp/file3.pdbqt";
		File file3 = new File(pathName3);
		file3.createNewFile();
		createdFiles.add(pathName3);
		String pathName4 = "tmp/file4.mol2";
		File file4 = new File(pathName4);
		file4.createNewFile();
		createdFiles.add(pathName4);
		fileUtils.eraseFilesInDirectoryMatchingPattern("tmp", "file.*pdbqt");
		assertFalse(file1.exists());
		assertFalse(file2.exists());
		assertFalse(file3.exists());
		assertTrue(file4.exists());
	}

	@Test
	public void erasesAllMapFiles() throws Exception {
		String pathName1 = "tmp/file1.pdbqt";
		File file1 = new File(pathName1);
		file1.createNewFile();
		createdFiles.add(pathName1);
		String pathName2 = "tmp/file2.pdbqt.map";
		File file2 = new File(pathName2);
		file2.createNewFile();
		createdFiles.add(pathName2);
		String pathName3 = "tmp/file3.pdbqt.map";
		File file3 = new File(pathName3);
		file3.createNewFile();
		createdFiles.add(pathName3);
		String pathName4 = "tmp/file4.pdbqt.map.mol2";
		File file4 = new File(pathName4);
		file4.createNewFile();
		createdFiles.add(pathName4);
		fileUtils.eraseFilesInDirectoryMatchingPattern("tmp", "file.*map$");
		assertTrue(file1.exists());
		assertFalse(file2.exists());
		assertFalse(file3.exists());
		assertTrue(file4.exists());
	}
	
	@Test
	public void doesNotBreakWhenAttemptingToEraseFilesInADirectoryWhichIsActuallyAFile(){
		String fakeDirName = "test-files/3NA.mol2h";
		fileUtils.eraseFilesInDirectoryMatchingPattern(fakeDirName, "mol2h");
		File fakeDir = new File(fakeDirName);
		assertTrue(fakeDir.exists());
	}

	@Test
	public void joinTwoFilesAppended() throws IOException {
		File file1 = new File("test-files/file1.txt");
		File file2 = new File("test-files/file2.txt");

		List<File> filesList = new ArrayList<File>();
		filesList.add(file1);
		filesList.add(file2);

		File joinTestFile = new File("test-files/file12.txt");
		File joinFile = new File("tmp/outputFile.txt");
		joinFile.createNewFile();
		createdFiles.add(joinFile.getAbsolutePath());

		fileUtils.joinFiles(filesList, joinFile);
		FileAssert.assertEquals(joinTestFile, joinFile);
		joinFile.delete();
	}
	
	@Test
	public void failsWhenJoiningNotExistingFile() throws IOException {
		try{
			File file1 = new File("test-files/file1.txt");
			File file2 = new File("test-files/nonexistent.txt");

			List<File> filesList = new ArrayList<File>();
			filesList.add(file1);
			filesList.add(file2);

			File joinFile = new File("tmp/outputFile.txt");
			joinFile.createNewFile();
			createdFiles.add(joinFile.getAbsolutePath());
			fileUtils.joinFiles(filesList, joinFile);
		} catch (RuntimeException e) {
			assertEquals("File was not found", e.getMessage());
		}
	}

	@Test
	public void getFileNameWithoutExtensionCorrectly() {
		File file = new File("test-files/file1.txt");
		String fileName = fileUtils.getFileNameWithoutExtension(file);
		String correctFileName = "file1";
		assertEquals(correctFileName, fileName);
	}
	
	@Test
	public void getFileNameWithoutExtensionDoesNotBreakIfThereIsNoExtension() {
		File file = new File("test-files/file1");
		String fileName = fileUtils.getFileNameWithoutExtension(file);
		String correctFileName = "file1";
		assertEquals(correctFileName, fileName);
	}

	@Test
	public void getFileExtensionCorrectly() {
		String file = "test-files/file1.txt";
		String fileExtension = fileUtils.getFileExtension(file);
		String correctFileExtension = "txt";
		assertEquals(correctFileExtension, fileExtension);
	}

	@Test
	public void removeExactLineCorrectly() {
		String modifiedFile = "tmp/removeLineFromFileTest2";
		createdFiles.add(modifiedFile);
		fileUtils.removeAllOccurrencesOfLineFromFile("test-files/removeLineFromFileTest1", modifiedFile, "and we want to", true);
		FileAssert.assertEquals(new File("test-files/removeLineFromFileTest2"), new File(modifiedFile));
	}

	@Test
	public void removeContainedLineCorrectly() {
		String modifiedFile = "tmp/removeLineFromFileTest2";
		createdFiles.add(modifiedFile);
		fileUtils.removeAllOccurrencesOfLineFromFile("test-files/removeLineFromFileTest1", modifiedFile, "and we", false);
		FileAssert.assertEquals(new File("test-files/removeLineFromFileTest2"), new File(modifiedFile));
	}

	@Test
	public void sdfFilesAreDetectedAsSdf(){
		assertTrue(fileUtils.isSdf("something.sdf"));
		assertFalse(fileUtils.isSdf("file.sdf.not"));
	}

	@Test
	public void pdbqtFilesAreDetectedAsPdbqt(){
		assertTrue(fileUtils.isPdbqt("something.pdbqt"));
		assertFalse(fileUtils.isPdbqt("file.pdbqt.not"));
	}

	@Test
	public void smileFilesAreDetectedAsSmile(){
		assertTrue(fileUtils.isSmile("something.smi"));
		assertFalse(fileUtils.isSmile("file.smi.not"));
	}

	@Test
	public void failsToGetBlobFromInexistentFile(){
		try {
			fileUtils.getBlobFromFile("ñlakdsjfa", "someformat");
			fail("Should have failed fail");
		} catch (RuntimeException e) {
			//OK
		}
	}

	@Test 
	public void filesAreCopiedCorrectly() throws InterruptedException{
		String origin = "test-files/file1.txt";
		File originFile = new File(origin);
		String destination = "tmp/copyOfFile1.txt";
		createdFiles.add(destination);
		File destinationFile = new File(destination);
		assertFalse(destinationFile.exists());

		fileUtils.copyFile(origin, destination);

		assertTrue(originFile.exists());
		destinationFile = new File(destination);
		assertTrue(destinationFile.exists());
		FileAssert.assertEquals(originFile, destinationFile);
		destinationFile.delete();
	}

	@Test 
	public void filesCopiedDontPreserveLastModifiedDate() throws InterruptedException{
		String origin = "test-files/file1.txt";
		File originFile = new File(origin);
		String destination = "tmp/copyOfFile1.txt";
		createdFiles.add(destination);
		File destinationFile = new File(destination);
		assertFalse(destinationFile.exists());

		fileUtils.copyFile(origin, destination);

		assertTrue(originFile.exists());
		destinationFile = new File(destination);
		assertTrue(destinationFile.exists());
		FileAssert.assertEquals(originFile, destinationFile);
		assertNotSame(originFile.lastModified(), destinationFile.lastModified());
		destinationFile.delete();
	}
	
	@Test
	public void createsParentDirectoriesWhenCopyingAFile() throws InterruptedException {
		String origin = "test-files/file1.txt";
		File originFile = new File(origin);
		String destinationDir = "tmp/12/23";
		String destination = destinationDir+"/copyOfFile1.txt";
		createdFiles.add(destination);
		File destinationFile = new File(destination);
		assertFalse(destinationFile.exists());
		File destinationDirFile = new File(destinationDir);
		assertFalse(destinationDirFile.exists());
		
		fileUtils.copyFile(origin, destination);
		
		assertTrue(originFile.exists());
		destinationFile = new File(destination);
		assertTrue(destinationFile.exists());
		FileAssert.assertEquals(originFile, destinationFile);
		destinationFile.delete();
		//Removes parent directory structure
		destinationDirFile.delete();
		File destinationParent = destinationDirFile.getParentFile();
		destinationParent.delete();
	}
	
	@Test
	public void createsDirectoryCorrectly() throws InterruptedException{
		String directory = "/tmp/directory";
		File dirFile = new File(directory);
		createdFiles.add(directory);
		assertFalse(dirFile.exists());
		fileUtils.createDirectory(directory);
		//It needs time to create directory
		Thread.sleep(100);
		assertTrue(dirFile.exists());
		dirFile.delete();
	}

	@Test
	public void failsWhenFileToRemoveOcurrencesDoesNotExist(){
		try {
			fileUtils.removeAllOccurrencesOfLineFromFile("/nonexistent", "/tmp/existing.txt", "remove", false);
			fail("Should throw exception");
		} catch (RuntimeException e) {
			assertEquals("File was not found", e.getMessage());
		}
	}
	
	@Test
	public void replaceEquivalencesInFileWithoutEquivalencesReturnsSameFile(){
		String originalFile = "test-files/test_replace_equivalences.sdf";
		String replacedFile = "test-files/test_replace_equivalences.sdf";
		fileUtils.replaceEquivalencesInFile(originalFile, new HashMap<String, String>());
		FileAssert.assertEquals(new File(originalFile), new File(replacedFile));
	}

	@Test
	public void replaceEquivalencesInFileWorksCorrectly(){
		String originalFile = "test-files/test_replace_equivalences.sdf";
		String replacedFile = "test-files/test_replaced_equivalences.sdf";
		HashMap<String, String> equivalences = new HashMap<String, String>();
		equivalences.put("PPR  0", "LARGE NAME");
		equivalences.put("PPR  1", "VERYVERYLAAAAARGE NAME");

		File temporalFile = new File("tmp/replace_equivalences.sdf");
		//temporalFile.createNewFile();
		createdFiles.add(temporalFile.getAbsolutePath());
		fileUtils.copyFile(originalFile, temporalFile.getAbsolutePath());
		
		fileUtils.replaceEquivalencesInFile(temporalFile.getAbsolutePath(), equivalences);
		FileAssert.assertEquals(temporalFile, new File(replacedFile));
	}
	
	@Test
	public void filesAreCopiedToTmpWithCorrectName() throws InterruptedException, IOException{
		String origin = "test-files/file1.txt";
		File originFile = new File(origin);
		String destination = "tmp/copiedFile.txt";
		File expectedDestinationFile = new File(destination);
		createdFiles.add(destination);
		assertFalse(expectedDestinationFile.exists());
		File tmpFile = fileUtils.copyToTmpAs(originFile, "copiedFile.txt");
		assertTrue(originFile.exists());
		assertTrue(tmpFile.exists());
		FileAssert.assertEquals(originFile, tmpFile);
		assertEquals(expectedDestinationFile.getCanonicalPath(), tmpFile.getCanonicalPath());
		tmpFile.delete();
	}
	
	@Test
	public void copyingLargeFilesBeforeReturning() throws InterruptedException, IOException{
		String origin = "test-files/largeFile.sdf";
		File originFile = new File(origin);
		String destination = "tmp/copiedFile.sdf";
		createdFiles.add(destination);
		File expectedDestinationFile = new File(destination);
		assertFalse(expectedDestinationFile.exists());
		File tmpFile = fileUtils.copyToTmpAs(originFile, "copiedFile.sdf");
		assertTrue(tmpFile.exists());
		assertEquals(expectedDestinationFile.getCanonicalPath(), tmpFile.getCanonicalPath());
		tmpFile.delete();
	}
	
	@Test
	public void erasesEmptyDirectory() {
		File originDir = new File("/tmp/someDir");
		originDir.mkdir();
		
		assertTrue(originDir.exists());
		assertTrue(originDir.isDirectory());
		
		fileUtils.deleteDirectory("/tmp/someDir");
		
		assertFalse(originDir.exists());
	}

	@Test
	public void erasesDirectoryRecursively() {
		File deepestDir = new File("/tmp/someDir/with/inside/stuff");
		File baseDir = new File("/tmp/someDir");
		deepestDir.mkdirs();
		
		assertTrue(baseDir.exists());
		assertTrue(baseDir.isDirectory());
		
		fileUtils.deleteDirectory("/tmp/someDir");
		
		assertFalse(baseDir.exists());
	}
	
	@Test
	public void copyDirectory(){
		fileUtils.createDirectory("tmp/tempDir");	
		fileUtils.copyDirectory("test-files/qsar", "tmp/tempDir");
		FileAssert.assertEquals(new File("test-files/qsar/exampleQsarResult.csv"),
				new File("test-files/qsar/exampleQsarResult.csv"));
		assertEquals((new File("test-files/qsar")).list().length, (new File("tmp/tempDir")).list().length);
		fileUtils.deleteDirectory("tmp/tempDir");
	}
}
