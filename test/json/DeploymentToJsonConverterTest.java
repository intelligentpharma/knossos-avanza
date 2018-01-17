package json;

import models.Deployment;

import org.junit.Test;

import play.test.UnitTest;

public class DeploymentToJsonConverterTest extends UnitTest {
	
	@Test
	public void deploymentConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "deployMent";
		deployment.putProperty("prop1", "value1");
		deployment.putProperty("prop2", "value2");
		DeploymentToJsonConverter converter = new DeploymentToJsonConverter();
		converter.setData(deployment);
		assertEquals("{\"name\":\"deployMent\",\"prop1\":\"value1\",\"prop2\":\"value2\",\"id\":\"null\"}", 
				converter.getJson());
	}

	@Test
	public void propertyWithQuotesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "deployMent";
		deployment.putProperty("prop1", "value1");
		deployment.putProperty("prop2", "something \"value2\" something else");
		DeploymentToJsonConverter converter = new DeploymentToJsonConverter();
		converter.setData(deployment);
		assertEquals("{\"name\":\"deployMent\",\"prop1\":\"value1\",\"prop2\":\"something \\\"value2\\\" something else\",\"id\":\"null\"}", 
				converter.getJson());
	}

	@Test
	public void propertyWithNewlinesConvertedCorrectly(){
		Deployment deployment = new Deployment();
		deployment.name = "deployMent";
		deployment.putProperty("prop1", "value1\nvalue11");
		deployment.putProperty("prop2", "val\\ue2");
		DeploymentToJsonConverter converter = new DeploymentToJsonConverter();
		converter.setData(deployment);
		assertEquals("{\"name\":\"deployMent\",\"prop1\":\"value1\\nvalue11\",\"prop2\":\"val\\\\ue2\",\"id\":\"null\"}", 
				converter.getJson());
	}
}
