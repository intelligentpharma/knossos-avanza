package engine.tridimensional.maps;

import java.io.FileNotFoundException;

import org.junit.Test;
import static org.easymock.EasyMock.*;

import play.test.UnitTest;
import engine.factory.FileNameFactory;
import engine.tridimensional.maps.TargetPdbqtRetriever;

public class TargetPdbqtRetrieverTest extends UnitTest {
	
	@Test
	public void retrievesTargetPdbqtViaFileNameFactory() throws FileNotFoundException{
		FileNameFactory factory = createMock(FileNameFactory.class);
		String pdbqt = "something";
		expect(factory.getTargetPdbqt()).andReturn(pdbqt);
		replay(factory);
		
		TargetPdbqtRetriever retriever = new TargetPdbqtRetriever();
		assertEquals(pdbqt, retriever.getPdbqt(factory));
		
		verify(factory);
	}

}
