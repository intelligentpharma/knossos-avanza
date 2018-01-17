package jobs.workflow;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator {
	
	Map<String, Float> map;
	boolean reverse;

	public ValueComparator(Map<String, Float> map) {
		this.map = map;
		this.reverse = false;
	}
	
	public ValueComparator(Map<String, Float> map, Boolean reverse) {
		this.map = map;
		this.reverse = reverse;
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		Float value1 = map.get(o1);
		Float value2 = map.get(o2);
		if (reverse){
			return -1 * value1.compareTo(value2);
		}
		return value1.compareTo(value2);
	}

}
