package qa.qcri.qf.pipeline.qademo;

import static com.google.common.base.Preconditions.*;

public class Candidate {
	
	protected static String FIELDS_SEPARATOR = "\t";
	
	private final String id;
	private final double seScore;
	private final int seRank;
	private final String text;
	private final String question;
	
	public Candidate(String question, String id, int seRank, double seScore, String text) {
		this.question = question;
		this.id = id;
		this.seRank = seRank;
		this.seScore = seScore;
		this.text = text;
	}
	
	public String getQuestion() {
		return this.question;
	}
	
	public String getId() { 
		return this.id;
	}
	
	public int getSeRank() { 
		return this.seRank;
	}
	
	public double getSeScore() { 
		return this.seScore;		
	}
	
	public String getText() {
		return this.text;
	}
	
	@Override
	public String toString() {
		return this.id + FIELDS_SEPARATOR + this.seRank + FIELDS_SEPARATOR +
				this.seScore + FIELDS_SEPARATOR + text;
	}

}
