		package qa.qcri.qf.pipeline.qademo;
		
		import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.uima.UIMAException;
		
		/**
		 * Interactive version of the Qa pipeline.
		 * 
		 * Build your own model using: 
		 *  - the TrecPipelineRunner program to generate training data from TREC
		 *  - svm_learn -t 5 -F 3 -C + -W R -V R -S 0 -N 1 svm.train svm.model
		 *    to produce the reranking model from TREC training data.
		 *    
		 * VM arguments: -Xss128m   
		 * @author antonio
		 *
		 */
		public class InteractivePipeline {
			/** The language option name. */
			private static final String LANG = "lang";
			/** The index option name. */
			private static final String INDEX_PATH = "index";
			/** The model file option name. */
			private static final String MODEL_FILE = "modelFile";
			/** The candidates number option name */
			private static final String CANDIDATES_NUMBER = "candidatesNumber";
			/** The max candidates number option name */
			private static final String MAX_CANDIDATES_NUMBER = "maxCandidatesNumber";
			/** An option indicating whether duplicate candidates should be stored. */
			private static final String FILTER_DUPLICATES = "filterDuplicates";
			
			/** Log4j logger */
			private static final Logger logger = Logger.getLogger(InteractivePipeline.class);
			
			/** Retrieve answer candidates from an indexed lucene corpus (VERSION 4.6) */ 
			private final CandidatesRetriever candidatesRetriever;			
			
			/** The directory containing the lucene index. */
			//private final String indexDir;
			
			/** The reranker settings. */
			private final Settings settings;
						
			/** The reranker pipeline */
			private Reranker rerankerPipeline;
			
			/** Store the candidates passages retrieved so far
			 *   and keep tracks of their docs' id.
			 */
			//private Map<String, String> text2docId = new HashMap<>();
						
			/**
			 * Build the interactive version of the pipeline.
			 * 
			 * @param modelFile A string holding the model file
			 * @param indexDir A string holding the index dir
			 * @throws UIMAException
			 */
			public InteractivePipeline(String lang, String indexDir, String modelFile) throws UIMAException {
				/** Settings for the specified language. */
				this.settings = Settings.valueOf(lang.toUpperCase());
				
				/** Print settings. */
				System.out.println(settings.toString());
				
				System.out.println("loading settings.... " + (settings != null));
				System.out.println(" (" + this.settings.getLanguage().toUpperCase() + ")");
				
				/** Initialize candidates retriever */
				this.candidatesRetriever = new CandidatesRetriever(
						indexDir,
						IPOptions.getInstance().candidatesNumber,
						IPOptions.getInstance().maxCandidatesNumber,
						IPOptions.getInstance().filterDuplicates,
						settings.getSimilarity(),
						settings.getLuceneAnalyzer());
				
				/** Set the reraneker Q/a pipeline */
				this.rerankerPipeline = new RerankerOnlyRel(modelFile, settings);
			}
			
			/**
			 * Retrieve the Answer Candidates for the specified questions and
			 *  rerank the results.
			 *  
			 * @param question
			 * @throws CandidatesReatrieveException 
			 * @throws UIMAException
			 */
			private List<? extends Candidate> processQuestion(String question) throws CandidatesReatrieveException { 
				checkNotNull(question, "question is null");
				checkArgument(!question.trim().equals(""), "question not specified");
				
				/** Retrieve answer candidates from the indexed corpus. */
				
				List<Candidate> candidateAnswers = null; 
				try { 
					candidateAnswers = retrieveCandidates(question);
				} catch (ParseException | IOException e) {
					throw new CandidatesReatrieveException("Error while reatriving candidates", e);
				}
				/** Rerank the answer candidates */ 
				List<? extends Candidate> rerankedCandidates = rerankPassages(candidateAnswers);
				return rerankedCandidates;
			}
			
			/**
			 * Rerank the answer candidates by using the specified model. 
			 * 
			 * @param question
			 * @param candidateAnswers
			 * @return
			 */
			private List<? extends Candidate> rerankPassages(List<Candidate> candidateAnswers) {
				// rerank the candidates
				return this.rerankerPipeline.rerankPassages(candidateAnswers);
			}
		
			/**
			 * Retrieve a list of candidates for the specified question. 
			 * 
			 * @param question
			 * @return
			 * @throws IOException
			 * @throws ParseException
			 */
			private List<Candidate> retrieveCandidates(String question) throws IOException, ParseException {
				return this.candidatesRetriever.retrieveCandidates(question);
			}
			
				
			private InteractivePipeline inputLoop() {
				/*
				BufferedReader in = 
						new BufferedReader(
								new InputStreamReader(System.in));
				*/
				Scanner in = new Scanner(System.in);
				System.err.println("Enter you question (Or press C^d for quitting)");
				System.err.print(">>> ");
			
				while (in.hasNextLine()) { 
					String question = in.nextLine();
					System.out.println(">>> " + question);
					try {
						long start = System.nanoTime();
						List<? extends Candidate> rerankedCandidates = processQuestion(question);
						long end = System.nanoTime();
						double elapsedSeconds = (end - start) / 1000000000.0;
						System.out.printf("Runtime in seconds: %.2f\n", elapsedSeconds);
						for (Candidate candidate : rerankedCandidates) { 
							System.out.println(candidate.toString());
							System.out.println();
						}
					} catch (CandidatesReatrieveException e) { 
						System.err.println("Error while reatriving candidates for the input question: " + e.getMessage());
						e.printStackTrace();
					}
					System.err.println("Enter you question (Or press C^d for quitting)");
					System.err.print(">>> ");
				}		
				in.close();
				return this;
			}
			
			public static void main(String[] args) throws UIMAException {
				Options options = new Options();
				options.addOption("help", true, "Print the help");
				options.addOption(LANG, true, "The language of the processing data");
				options.addOption(INDEX_PATH, true, "The path of the index directory");
				options.addOption(MODEL_FILE, true, "The path of the smvlighttk model file");
				options.addOption(CANDIDATES_NUMBER, true, 
						"The number of answer candidates to retrieve per question");
				options.addOption(OptionBuilder
						.hasArg()
						.withLongOpt(CANDIDATES_NUMBER)
						.isRequired(false)
						.withDescription("The number of answer candidates to return, after filtering.")
						.create());
				
				options.addOption(OptionBuilder
						.hasArg()
						.withLongOpt(MAX_CANDIDATES_NUMBER)
						.isRequired(false)
						.withDescription(
						"The max number of answer candidates to retrieve per question, " + 
								"before filtering")
						.create());
						
				options.addOption(OptionBuilder
						.hasArg()
						.withLongOpt(FILTER_DUPLICATES)
						.isRequired(false)
						.withDescription("A boolean indicating wheth"
								+ "er duplicate " 
								+ "candidates should be filter out")
						.create());
				
				CommandLineParser parser = new BasicParser();
				try {
					CommandLine cmd = parser.parse(options, args);
					if (cmd.hasOption("help")) {
						new HelpFormatter().printHelp("TrecPipelineRunner", options);
						System.exit(0);
					}
					
					String lang = cmd.getOptionValue(LANG);
					// Check lang is not null or empty
					if (lang == null || lang.trim().equals("")) { 
						System.err.println("lang not specified");
						System.exit(1);
					}
					// Check that language is english or italian
					if (!lang.equals("it") && !lang.equals("en")) { 
						System.err.println("lang \""  + lang + "\" not recognized. " + 
										   "Only \"it\" and \"en\" languages are supported.");
						System.exit(1);
					}
					
					String indexDir = cmd.getOptionValue(INDEX_PATH);
					// Check indexDir is not null or empty
					if (indexDir == null || indexDir.trim().equals("")) { 
						System.err.println("index not specified");
						System.exit(1);
					}
					
					// Check that index exists, is a directory and can be read.
					File indexFile = new File(indexDir);
					if (!indexFile.exists() || !indexFile.isDirectory() || !indexFile.canRead()) {
						System.err.println("index " + indexDir + " does not exist, " +  
										   "is not a directory or cannot be read.");
						System.exit(1);
					}
					
					String modelPath = cmd.getOptionValue(MODEL_FILE);
					// Check that the model file is not null or empty
					if (modelPath == null || modelPath.trim().equals("")) { 
						System.err.println("modelFile not specified");
						System.exit(1);
					}
					// Check that the model path exists, is a file and can be read
					File modelFile = new File(modelPath);
					if (!modelFile.exists() || !modelFile.isFile() || !modelFile.canRead()) {
						System.err.println("modelPath does not exist, if not a file or cannot be read.");
						System.exit(1);
					}				
					
					// init ip (interactive pipeline) options
					IPOptions.init(args);
					
					// print ip options
					System.out.println(IPOptions.getInstance().toString());
					
					// Initialize the interactive pipeline
					InteractivePipeline ip = 
						new InteractivePipeline(
							IPOptions.getInstance().lang,
							IPOptions.getInstance().index,
							IPOptions.getInstance().modelFile);
					
					// Wait for user input, Start looping
					ip.inputLoop();					
				} catch (org.apache.commons.cli.ParseException e) {
					e.printStackTrace();
				}
			}
		}
