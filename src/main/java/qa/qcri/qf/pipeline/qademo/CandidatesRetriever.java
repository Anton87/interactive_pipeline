package qa.qcri.qf.pipeline.qademo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.maltparser.core.helper.HashMap;

/**
 * Retrieves candidates from a corpus (e.g. enwiki or itwiki)
 *  indexed with Lucene (VERSION 4.6)
 *   
 * @author Antonio Uva
 */
public class CandidatesRetriever {
	
	/** log4j logger */
	private static Logger logger = Logger.getLogger(CandidatesRetriever.class);
	

	/** Store the candidates passages retrieved so far
	 *   and keep tracks of their docs' id.
	 */
	private Map<String, String> text2docId = new HashMap<>();
	
	/** The lucene index directory (VERSION 4.6) */
	private final String indexDir;
	
	/**
	 * The number of answer candidates to return, 
	 * 	after filtering out duplicates.
	 */
	private final int candidatesNumber;
	
	/** The max number of answer candidates to retriever,
	 *   before filtering out duplicates. 
	 */
	private final int maxCandidatesNumber;
	
	/**
	 * A boolean indicating whether answer candidates with duplicate
	 *  text should be filtered out or not.
	 */
	private final boolean filterDuplicates;
	
	/** The similarity measure used by lucene */
	private final Similarity similarity;
	
	/** The document Analyzer used by lucene */ 
	private final Analyzer luceneAnalyzer;
	
	/**
	 * Builds a new CandidatesRetriever.
	 * 
	 * @param indexDir The lucene index directory (VERSION 4.6)
	 * @param candidatesNumber The number of answer candidates to return, 
	 * 	after filtering out duplicates.
	 * @param maxCandidatesNumber The max number of answer candidates to retriever,
	 *   before filtering out duplicates.
	 * @param filterDuplicates A boolean indicating whether answer candidates with duplicate
	 *  text should be filtered out or not. 
	 * @param similarity The similarity measure used by lucene
	 * @param luceneAnalyzer The document Analyzer used by lucene 
	 */
	public CandidatesRetriever(
			String indexDir,
			int candidatesNumber,
			int maxCandidatesNumber, 
			boolean filterDuplicates,
			Similarity similarity,
			Analyzer luceneAnalyzer) {
		// TODO Auto-generated constructor stub
		
		this.indexDir = indexDir;
		this.candidatesNumber = candidatesNumber;
		this.maxCandidatesNumber = maxCandidatesNumber;
		this.filterDuplicates = filterDuplicates;
		this.similarity = similarity;
		this.luceneAnalyzer = luceneAnalyzer;
	}
	
	/**
	 * Retrieve a list of candidates for the specified question. 
	 * 
	 * @param question The question text
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<Candidate> retrieveCandidates(String question) throws IOException, ParseException {
		
		System.out.print("Retrieving candidates");
		IndexReader reader = 
				DirectoryReader.open(
						FSDirectory.open(new File(this.indexDir)));
		IndexSearcher searcher = new IndexSearcher(reader);
		if (similarity != null) {
			// Print similarity measure name
			System.out.println("Using similarity measure " + this.similarity.getClass().getSimpleName() + ".");
			searcher.setSimilarity(this.similarity);
		}
		
		QueryParser parser = null;
		
		// Set luceneAnalyzer if not null				
		Analyzer luceneAnalyzer = this.luceneAnalyzer;
		//System.out.println("lucene analyzer is null? " + (luceneAnalyzer == null));
		if (luceneAnalyzer == null) {
			luceneAnalyzer = new StandardAnalyzer(Version.LUCENE_46);
		}
		System.out.println("Using lucene analyzer " + luceneAnalyzer.getClass().getSimpleName() + ".");
		parser = new QueryParser(Version.LUCENE_46, "text", luceneAnalyzer);
		
		String escapedQuery = QueryParser.escape(question);
		Query query = parser.parse(escapedQuery);
		System.out.print("...");
		
		/** Retrieve a number of documents <= maxCandidatesNumber */
		int maxCandidatesNumber = IPOptions.getInstance().maxCandidatesNumber;
		TopDocs results = searcher.search(query, maxCandidatesNumber);
		ScoreDoc[] hits = results.scoreDocs;
		int numTotalHits = hits.length;
		
		//List<String> resultSet = new ArrayList<>();
		List<Candidate> candidates = new ArrayList<>();
		logger.info("filter dupicates ? " + IPOptions.getInstance().filterDuplicates);
		int seRank = 1;
		int candidatesNumber = IPOptions.getInstance().candidatesNumber;
		/**
		 *  Keep a number of answer candidates <= candidatesNumber 
		 */
		for (int i = 0; i < numTotalHits && seRank <= candidatesNumber; i++) { 
			Document passage = searcher.doc(hits[i].doc);
			String id = passage.get("docId");
			String text = passage.get("text");
			
			/** Filter duplicate candidates */
			if (IPOptions.getInstance().filterDuplicates && !isDuplicate(passage)) {
				Candidate candidate = new Candidate(question, id, seRank++, hits[i].score, text);
				candidates.add(candidate);
			}
		}
		System.out.println("OK. (" + candidates.size() + " candidates retrieved)");
		
		// Clear previously stored candidates texts.
		newQuestionProcessed();
		return candidates; 
	}
	
	/**
	 * Check if the retrieved is a duplicate.
	 * 
	 * @param doc
	 * @return
	 */
	private boolean isDuplicate(Document doc) {
		String text = StringUtils.removeAllNonAlphaChars(doc.get("text"));
		if (text2docId.containsKey(text)) { 
			logger.info(doc.get("id") + " contains same " + text2docId.get(text) + "'s text: " + doc.get("text"));
			return true;
		}
		logger.info("Adding " + doc.get("docId") + "'s text: " + doc.get("text"));
		text2docId.put(text, doc.get("docId"));
		return false;
	}
	
	/**
	 * A new question has been processed.
	 * Clear the set of candidates' texts previously stored.
	 */
	private void newQuestionProcessed() { 
		text2docId.clear();
	}

}
