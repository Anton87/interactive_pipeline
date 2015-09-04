package qa.qcri.qf.pipeline.qademo;

import java.util.regex.Pattern;


/**
 * A class holding utility methods for strings.
 * 
 * @author antonio
 *
 */
public class StringUtils {
	
	/** A regex matching only alphabetic chars. */
	private static final String ALPHA_CHARS_REGEX = "[^a-zA-Z]";
	
	/** A pattern matching only alphabetic chars. */
	private static final Pattern ALPHA_CHARS_PATTERN = 
			Pattern.compile(ALPHA_CHARS_REGEX);
	
	// Static class, not instantiable
	private StringUtils() {}
	
	/**
	 * Remove all chars which are not alphabetic.
	 * 
	 * @param text A string holding some text
	 * @return The string whit all not alphabetic chars removed. 
	 */
	public static String removeAllNonAlphaChars(String text) {
		assert text != null;
		
		return ALPHA_CHARS_PATTERN.matcher(text).replaceAll("");
	}
	
	public static void main(String[] args) { 
		String str = "Eccellenza Puglia 2000-2001:1";
		
		System.out.println(StringUtils.removeAllNonAlphaChars(str));
	}

}
