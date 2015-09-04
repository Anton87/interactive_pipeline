package qa.qcri.qf.pipeline.qademo;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.IOException;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.Version;
import org.apache.uima.UIMAException;
import qa.qcri.qf.italian.textpro.TextProWrapper;
import qa.qcri.qf.pipeline.Analyzer;
import qa.qcri.qf.pipeline.serialization.UIMANoPersistence;
import qa.qcri.qf.pipeline.serialization.UIMAPersistence;
import qa.qcri.qf.pipeline.trec.AnalyzerFactory;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

public enum Settings {
	
	IT("it", "resources/stoplist-it.txt", newAnalysisEngine("it"), new ItalianAnalyzer(Version.LUCENE_46), new BM25Similarity()), 
	EN("en", "resources/stoplist-en.txt", newAnalysisEngine("en"), new EnglishAnalyzer(Version.LUCENE_46), null);
	
	/** The lang code for the language used. */
	private final String language;
	
	/** The stopwords file path. */
	private final String stopwordsFile;
	
	/** The analysis engine used. */
	private final Analyzer analysisEngine;
	
	/** The lucene analyzer used. */	
	private org.apache.lucene.analysis.Analyzer luceneAnalyzer;
	
	/** The similarity measure used. */
	private Similarity similarity;
	
	/**
	 * Pipeline settings for the different languages.
	 * 
	 * @param language
	 * @param stopwordsFile
	 * @param analysisEngine
	 * @param luceneAnalyzer
	 * @param similarity
	 */
	Settings(String language, String stopwordsFile, Analyzer analysisEngine, 
			org.apache.lucene.analysis.Analyzer luceneAnalyzer, Similarity similarity) { 
		this.language = language;
		this.stopwordsFile = stopwordsFile;
		this.analysisEngine = analysisEngine;
		this.luceneAnalyzer = luceneAnalyzer;
		this.similarity = similarity;
	}
	
	/** The lang code for the language used. */
	public String getLanguage() {
		return this.language;
	}
	
	/** The lang code for the language used. */
	public String getStopwordsFile() { 
		return this.stopwordsFile;
	}
	
	/** The analysis engine used. */
	public Analyzer getAnalysisEngine() { 
		return this.analysisEngine;
	}
	
	/** The lucene analyzer used. */	
	public org.apache.lucene.analysis.Analyzer getLuceneAnalyzer() {
		return this.luceneAnalyzer;
	}
	
	/** The similarity measure used. */
	public Similarity getSimilarity() { 
		return this.similarity;
	}
	
	/** 
	 * Instatiate a new analysis engine. 
	 * @param lang The lang code (e.g. it or en)
	 * @return
	 */
	private static Analyzer newAnalysisEngine(String lang) { 
		assert lang != null;
		
		Analyzer analyzer = null;
		
		try {
			switch (lang) {
			case "it":
				analyzer = newITAnalyzer(new UIMANoPersistence());
				break;
			case "en":
				AnalyzerFactory.newTrecPipeline(lang, new UIMANoPersistence());
				break;
			default:
				System.out.println("Unknown language: " + lang);
				break;
			}
		} catch (UIMAException e) { 
			System.err.println("Error while instantiating AnalysisEngine for lang \"" + lang + "\".");
			e.printStackTrace();
		}
		
		return analyzer;
	}
	
	private static Analyzer newITAnalyzer(
			UIMAPersistence persistence) throws UIMAException {
		assert persistence != null;

		Analyzer ae = new Analyzer(persistence);
		//final String GRAMMAR_FILE = "./tools/TextPro1.5.2_Linux64bit/ParseBer/italian_parser/BerkeleyParser-Italian/tutall-fulltrain";

		try {
			ae.addAEDesc(createEngineDescription("desc/Iyas/TextProAllInOneDescriptor", 
					TextProWrapper.PARAM_VERBOSE, false));
		} catch (IOException e) {
			throw new UIMAException(e);
		}

		return ae;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName() + "[\n");
		sb.append("    language: " + this.language + ",\n");
		sb.append("    stopwordsFile: "  + this.stopwordsFile + ",\n");
		sb.append("    analysisEngine: " + this.analysisEngine + ",\n");
		sb.append("    luceneAnalyzer: " + this.luceneAnalyzer.getClass().getSimpleName() + ",\n");
		sb.append("    similarity: " + this.similarity + "\n");		
		sb.append("]");
		return sb.toString();
	}

}
