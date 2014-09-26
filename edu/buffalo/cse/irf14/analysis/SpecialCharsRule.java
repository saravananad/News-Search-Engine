/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Pattern;

/**
 * @author NaveenKumar
 *
 */
public class SpecialCharsRule extends TokenFilter {

	private static final Pattern specialCharsPattern = Pattern.compile("[\\.,@$%^&\\*!\\(\\)\\[\\]\\{\\};:<>\\/=\\|__]+");
	private static final Pattern hyphenCase1 = Pattern.compile("[0-9]+\\-[0-9]+");
	private static final Pattern hyphenCase2 = Pattern.compile("\\s*\\-[a-zA-Z0-9]+");

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

	@Override
	public TokenStream getStream() {
		return tokenStream;
	}

}
