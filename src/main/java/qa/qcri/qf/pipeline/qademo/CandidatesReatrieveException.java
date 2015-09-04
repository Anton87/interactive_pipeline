package qa.qcri.qf.pipeline.qademo;

/**
 * Thrown when there are problem when retrieving candidates.
 * 
 * @author antonio
 *
 */
public class CandidatesReatrieveException extends Exception {
	
	private static final long serialVersionUID = -7015294551840163276L;

	public CandidatesReatrieveException(String msg) {
		super(msg);
	}
	
	public CandidatesReatrieveException(String msg, Exception e) {
		super(msg, e);
	}

}
