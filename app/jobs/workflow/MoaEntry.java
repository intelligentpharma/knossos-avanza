package jobs.workflow;

public class MoaEntry implements Comparable<MoaEntry> {

	Float moaScore;
	String smile;

	public MoaEntry(String smile, Float moaScore) {
		this.moaScore = moaScore;
		this.smile = smile;
	}

	public Float getMoaScore() {
		return this.moaScore;
	}

	public String getsmile() {
		return this.smile;
	}
	
	@Override
	public int compareTo(MoaEntry e) {
		int result = 0;
		if (this.moaScore > e.getMoaScore()) {
			result = -1;
		}
		if (this.moaScore < e.getMoaScore()) {
			result = 1;
		}
		return result;
	}
}
