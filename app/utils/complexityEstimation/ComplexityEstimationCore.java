package utils.complexityEstimation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class ComplexityEstimationCore {
	public static final Logger logger = Logger.getLogger(ComplexityEstimationCore.class);
	
	private class Complexity{
		String experimentType;
		HashMap<Integer, Integer> complexitySet;
	}
		
	//Estimate experiment's job complexity based on experiment type and parameter
	public static int estimateComplexity(String type, int parameter) throws FileNotFoundException{
		System.out.println(String.format("EstimateExperiment Input: '%s : %s'", type, parameter));
		int result = -1;
		Integer compValue = -1;
		Integer compKey;
		
		//Load configuration file with job complexity information and parse JSON
		String filename = "./conf/complexities.json";
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader(filename));
		Complexity[] jsonArray = gson.fromJson(reader, Complexity[].class);		
		List<Complexity> jsonList = Arrays.asList(jsonArray);		
		Iterator<Complexity> it = jsonList.iterator();
		//Look for experiment type
		while (it.hasNext()){
			Complexity comp = it.next();
			System.out.println(String.format("Experiment type: '%s'", comp.experimentType));
			if (comp.experimentType.equalsIgnoreCase(type)){
				//Sort keySet and explore looking for pesimistic complexity
				Iterator<Integer> itt = new TreeSet(comp.complexitySet.keySet()).iterator();
				//Look for complexity, pesimistic estimation			
				while (itt.hasNext() && result == -1){
					compKey = itt.next();
					compValue = comp.complexitySet.get(compKey);
					System.out.println(String.format("Complexity: '%s : %s'", compKey, compValue));
					if (compKey >=  parameter){
						result = compValue;
					}
				}
				if (result == -1){
					result = compValue;
				}
			}
		}		
		System.out.println(String.format("EstimateExperiment Output: '%s ", compValue));
		return result;
	}
	
}