package engine.tridimensional.maps;

import java.io.FileNotFoundException;

import org.junit.Test;
import static org.easymock.EasyMock.*;

import play.test.UnitTest;
import engine.factory.FileNameFactory;
import engine.tridimensional.maps.OutputPdbqtRetriever;

public class OutputPdbqtRetrieverTest extends UnitTest {
	
	@Test
	public void retrievesTargetPdbqtViaFileNameFactory() throws FileNotFoundException{
		FileNameFactory factory = createMock(FileNameFactory.class);
		String pdbqt = "something";
		expect(factory.getOutputPdbqt()).andReturn(pdbqt);
		replay(factory);
		
		OutputPdbqtRetriever retriever = new OutputPdbqtRetriever();
		assertEquals(pdbqt, retriever.getPdbqt(factory));
		
		verify(factory);
	}

}
