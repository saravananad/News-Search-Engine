/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author NaveenKumar
 *
 */
public class SpecialCharsRule extends TokenFilter {

	private static final Pattern specialCharsPattern = Pattern.compile("[\\.,@$%^&\\*!\\(\\)\\[\\]\\{\\};:<>\\/=\\|__]+");
	private static final Pattern hyphenCase1 = Pattern.compile("[0-9]+\\-[0-9]+");
	private static final Pattern hyphenCase2 = Pattern.compile("\\s*\\-[a-zA-Z0-9]+");
	private static final Pattern junkAlphabetsPattern = Pattern.compile("([\\.\\*]+[A-Za-z]+\\.*[a-zA-Z]*)");
	private static final Pattern junkNumbersPattern = Pattern.compile("^([.]+\\d+)");

	/**
	 * 
	 */
	public SpecialCharsRule() {
		super();
	}

	/**
	 * @param stream
	 */
	public SpecialCharsRule(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())) {
			String tokenText = token.getTermText();
			if (specialCharsPattern.matcher(tokenText).matches()){
				tokenStream.remove(); // Remove the token if the token in itself is a word of non-alphanumeric characters
			}
			else {
				/* Remove junk characters from alphabetical words */
				if (junkAlphabetsPattern.matcher(tokenText).matches()){
					tokenText = tokenText.replaceAll("[.*]", "");
				}
				/* Remove junk characters from numerical strings*/
				Matcher junkNumbersMatch = junkNumbersPattern.matcher(tokenText);
				if (junkNumbersMatch.find()){
					String replacedText = junkNumbersMatch.group().replaceAll("[.*]", "");
					tokenText = tokenText.replace(junkNumbersMatch.group(), replacedText);
				}
				tokenText = tokenText.replaceAll("[^A-Za-z0-9\\.\\-]", ""); // Remove non-alphanumeric characters except a dot and hyphen
				if (!(hyphenCase1.matcher(tokenText).matches() || hyphenCase2.matcher(tokenText).matches())){
					tokenText = tokenText.replaceAll("-", ""); // Handle special cases of hyphen
				}
				token.setTermText(tokenText);
				if (tokenText.equals("")){
					tokenStream.remove(); // Clean-up of empty tokens
				}
			}
		}
		return true;
	}
}
