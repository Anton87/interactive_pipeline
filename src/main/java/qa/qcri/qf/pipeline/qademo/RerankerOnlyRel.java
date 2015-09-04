package qa.qcri.qf.pipeline.qademo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import qa.qcri.qf.classifiers.Classifier;
import qa.qcri.qf.features.FeaturesUtil;
import qa.qcri.qf.features.PairFeatureFactory;
import qa.qcri.qf.pipeline.Analyzer;
import qa.qcri.qf.pipeline.retrieval.SimpleContent;
import qa.qcri.qf.treemarker.MarkTreesOnRepresentation;
import qa.qcri.qf.treemarker.MarkTwoAncestors;
import qa.qcri.qf.trees.TokenTree;
import qa.qcri.qf.trees.TreeSerializer;
import qa.qcri.qf.trees.nodes.RichNode;
import qa.qcri.qf.trees.providers.PosChunkTreeProvider;
import qa.qcri.qf.trees.providers.TokenTreeProvider;
import svmlighttk.SVMLightTK;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;

import com.google.common.base.Joiner;

/**
 * Reranker using only relations between words in
 *  questions and candidate answers.
 *   
 * @author Antonio Uva 
 *
 */
public class RerankerOnlyRel implements Reranker {
	
	/** log4j logger */
	private static final Logger logger = Logger.getLogger(RerankerOnlyRel.class);
	
	/** Rerank score sorting function. */ 
	private static final RerankerScore rerankerScoreSorting = new RerankerScore();
	
	private final String parameterList = Joiner.on(",").join(
		new String[] { RichNode.OUTPUT_PAR_LEMMA, 
				RichNode.OUTPUT_PAR_TOKEN_LOWERCASE });
	
	private TokenTreeProvider treeProvider = new PosChunkTreeProvider();
	
	private TreeSerializer ts = new TreeSerializer().enableRelationalTags();
	
	private JCas passageJCas = null;
	private DocumentTextAndJCas questionTextAndJCas = new DocumentTextAndJCas();
	
	private PairFeatureFactory featureFactory = 
			new PairFeatureFactory(new Alphabet());
	
	/** SVMLightTk-1.5 Classifier */
	private Classifier reranker;
	
	/** The tree marker */
	private MarkTreesOnRepresentation marker;
	
	/** The reranker settings. */
	private final Settings settings;
		
	/**
	 * Builds a new RerankerOnlyRel object.
	 * 
	 * @param modelFile The reranker model file trained with SVM-Light-1.5-rer
	 * @param settings Global settings 
	 */
	public RerankerOnlyRel(String modelFile, Settings settings) {		

		/** init the SVM-Light-Tk-1.5 reranker  */
		this.reranker = new SVMLightTK(modelFile);
		
		// set gloabl settings
		this.settings = settings;
		
		// Set the marker		
		try {
			marker = new MarkTreesOnRepresentation(
					new MarkTwoAncestors()).useStopwords(
							settings.getStopwordsFile());
		} catch (IOException e) {
			System.err.println("Error while loading stopwords from file: " + settings.getStopwordsFile() + ".");
			e.printStackTrace();
		}
		
		try {
			passageJCas = JCasFactory.createJCas();
		} catch (UIMAException e) {
			System.err.println("Error while creating passage JCas: " + e.getMessage());
			e.printStackTrace();
		}
				
	}
	
	
	public boolean hasQuestionAlreadyBeenAnalyzed(String question) { 
		return this.questionTextAndJCas.hasAlreadyBeenProcessed(question);
	}
	
	public JCas getQuestionJCas() { 
		return this.questionTextAndJCas.getDocumentJCas();
	}

	@Override
	public String getExample(String question, String passage) {
		if (!hasQuestionAlreadyBeenAnalyzed(question)) {
			processNewQuestion(question);
		}
		JCas questionJCas = getQuestionJCas();
		
		passageJCas.reset();
		Analyzer analyzer = this.settings.getAnalysisEngine();
		analyzer.analyze(passageJCas, new SimpleContent("", passage));
		
		TokenTree questionTree = treeProvider.getTree(questionJCas);
		TokenTree passageTree = treeProvider.getTree(passageJCas);
		
		marker.markTrees(questionTree, passageTree, parameterList);

		FeatureVector fv = this.featureFactory.getPairFeatures(questionJCas, passageJCas, parameterList);
		StringBuffer sb = new StringBuffer(1024 * 4);
		sb.append("|BT| ");
		sb.append(ts.serializeTree(questionTree, parameterList));
		sb.append(" |BT| ");
		sb.append(ts.serializeTree(passageTree, parameterList));
		sb.append(" |BT| ");
		sb.append(" |BT| ");
		sb.append(" |ET| ");
		sb.append(FeaturesUtil.serialize(fv));
		sb.append(" |BV| ");
		sb.append(" |EV| ");
		
		String example = sb.toString();
		return example;
	}
		
	private void processNewQuestion(String question) {
		this.questionTextAndJCas.processNewDocument(question);
		
	}

	private class DocumentTextAndJCas {
		private String documentText;
		private JCas documentJCas;
		
		public DocumentTextAndJCas() {
			this.documentText = "";
			this.documentJCas = null;
			
			try {
				this.documentJCas = JCasFactory.createJCas();
			} catch  (UIMAException e) {
				System.err.println("Error while creating passage JCas: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		/*
		public DocumentTextAndJCas(String documentText, JCas documentJCas) {
			this.documentText = documentText;
			this.documentJCas = documentJCas;
		}
		*/
		
		public JCas getDocumentJCas() {
			return this.documentJCas;
		}
		
		void processNewDocument(String documentText) {
			this.documentJCas.reset();
			this.documentText = documentText;
			this.process();
		}
		
		
		
		public boolean hasAlreadyBeenProcessed(String documentText) {
			return this.documentText.equals(documentText);
		}
		
		public void process() { 
			Analyzer analyzer = settings.getAnalysisEngine();
			analyzer.analyze(
					this.documentJCas, new SimpleContent("", this.documentText));
		}
	}

	@Override
	public PairFeatureFactory getFeatureFactory() {
		return this.featureFactory;
	}


	/**
	 * Rerank the answer candidates by using the specified model. 
	 * 
	 * @param question
	 * @param candidateAnswers
	 * @return
	 */
	@Override
	public List<? extends Candidate> rerankPassages(
			List<Candidate> candidateAnswers) {
		// rerank the candidates
		List<RerankedCandidate> rerankedCandidates = new ArrayList<>();
		System.out.print("Reranking answer candidates..");
		for (int i = 0; i < candidateAnswers.size(); i++) {
			System.out.print((i + 1) + "..");
			Candidate candidate = candidateAnswers.get(i);
			Candidate rerankedCandidate = rankPassage(candidate); 
			rerankedCandidates.add((RerankedCandidate) rerankedCandidate);
		}
		System.out.println("done.");
		// Sort candidates by reranker score
		sortByRerankerScore(rerankedCandidates);		
		
		// Assing the new (reranking) rank
		for (int i = 0; i < rerankedCandidates.size(); i++) { 
			rerankedCandidates.get(i).setRerankerScore(i + 1);
		}
		System.out.println("done");
		return rerankedCandidates;
	}
	
	@Override
	public Candidate rankPassage(Candidate candidate) {
		String example = getExample(candidate.getQuestion(), candidate.getText());
		logger.info(example);
		
		double rerankerScore = this.reranker.classify(example);
		return new RerankedCandidate(
				candidate.getQuestion(),
				candidate.getId(),
				candidate.getSeRank(),
				candidate.getSeScore(), 
				0,
				rerankerScore,
				candidate.getText());
	}
	
	private static class RerankerScore implements Comparator<RerankedCandidate> {
		
		@Override
		public int compare(RerankedCandidate o1, RerankedCandidate o2) {
			return (int) Math.signum(o2.getRerankerScore() - o1.getRerankerScore());
		}
	}
	
	private void sortByRerankerScore(List<? extends RerankedCandidate> candidates) {
		Collections.sort(candidates, rerankerScoreSorting);
	}

}
