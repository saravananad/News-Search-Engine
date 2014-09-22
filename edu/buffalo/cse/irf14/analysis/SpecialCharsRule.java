/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author NaveenKumar
 *
 */
public class SpecialCharsRule extends TokenFilter {

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
			if (tokenText.matches("[\\.,@$%^&\\*!\\(\\)\\[\\]\\{\\};:<>\\/=\\|__]+")){
				tokenStream.remove(); // Remove the token if the token in itself is a word of non-alphanumeric characters
			}
			else {
				tokenText = tokenText.replaceAll("[^A-Za-z0-9\\.\\-]", ""); // Remove non-alphanumeric characters except a dot and hyphen
				if (!(tokenText.matches("[0-9]+\\-[0-9]+") || tokenText.matches("\\s*\\-[a-zA-Z0-9]+"))){
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
