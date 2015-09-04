package qa.qcri.qf.pipeline.qademo;

import java.util.List;

import qa.qcri.qf.features.PairFeatureFactory;

public interface Reranker {
	
	public String getExample(String question, String passage);
	
	public PairFeatureFactory getFeatureFactory();
	
	public Candidate rankPassage(Candidate candidate);
	
	public List<? extends Candidate> rerankPassages(List<Candidate> candidates);
	

}
