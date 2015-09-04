package qa.qcri.qf.pipeline.qademo;

/**
 * 
 * Store global options for the interative pipeline
 * @author antonio
 *
 */
public class IPOptions {
	
	/** A string holding the pipeline language. */
	String lang;
	
	/** A string holding the index dir path. */
	String index;
	
	/** A string holding the model file path. */
	String modelFile;
		
	/** An integer holding the number of answer candidates to look for */
	int candidatesNumber = 10;
	
	/** An integer holding the maximum number of candidates to retrieve, 
	 *   before filtering duplicates.
	 */
	int maxCandidatesNumber = 20;
	
	/** A boolean indicating whether duplicates should be filter out. */
	boolean filterDuplicates = true;
	
	/** Singleton holding interactive pipeline options. */
	private static IPOptions INSTANCE;
	
	/**
	 * Costruct new ip options object.
	 * 
	 * Parse args and store option values.
	 * @param args
	 */
	private IPOptions(String args[]) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-lang")) {
				lang = args[++i];
			}
			else if (args[i].equals("-modelFile")) {
				modelFile = args[++i];
			}
			else if (args[i].equals("-index")) {
				index = args[++i];
			}
			else if (args[i].equals("-candidatesNumber")) {
				candidatesNumber = Integer.parseInt(args[++i]);
			}
			else if (args[i].equals("-maxCandidatesNumber")) { 
				maxCandidatesNumber = Integer.parseInt(args[++i]);
			}
			else if (args[i].equals("-filterDuplicates")) {
				filterDuplicates = Boolean.parseBoolean(args[++i]);
			}
			else { 
				System.out.println("Option " + args[i] + " not recognized.");
			}
		}
	}
	
	/**
	 * Initialized interactive pipeline options.
	 * @param args
	 * @return
	 */
	public static IPOptions init(String[] args) {
		INSTANCE = new IPOptions(args);
		return INSTANCE;
	}
	 
	/**
	 * Return interactive pipeline options singleton.
	 * 
	 * @return
	 */
	public static IPOptions getInstance() {
		if (INSTANCE == null) {
			System.err.println("options not initalized");
		}
		return INSTANCE; 
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Options [\n");
		sb.append("    lang = " + lang + ",\n");
		sb.append("    index = " + index + ",\n");
		sb.append("    modelFile = " + modelFile + ",\n");
		
		sb.append("    candidatesNumber = " + candidatesNumber + ",\n");
		sb.append("    maxCandidatesNumber = " + maxCandidatesNumber + ",\n");
		sb.append("    filterDuplicates = " + filterDuplicates + "\n");
		sb.append("]");
		
		return sb.toString();
	}
}
