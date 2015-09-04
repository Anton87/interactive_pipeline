package qa.qcri.qf.pipeline.qademo;

public class RerankedCandidate extends Candidate {
	
	private int rerankerRank;
	private final double rerankerScore;
	
	public RerankedCandidate(String question, String id, int seRank, double seScore, 
			int rerankerRank, double rerankerScore, String text) {
		super(question, id, seRank, seScore, text);
		this.rerankerRank = rerankerRank;
		this.rerankerScore = rerankerScore;
	}
	
	public int getRerankerRank() {
		return this.rerankerRank;
	}
	
	public double getRerankerScore() { 
		return this.rerankerScore;
	}
	
	void setRerankerScore(int rerankerRank) {
		this.rerankerRank = rerankerRank;
	}
	
	@Override
	public String toString() {
		return getId() + FIELDS_SEPARATOR + getSeRank() + FIELDS_SEPARATOR + 
				getSeScore() + FIELDS_SEPARATOR + this.rerankerRank + FIELDS_SEPARATOR + 
				getRerankerScore() + FIELDS_SEPARATOR + getText();				
	}

}
