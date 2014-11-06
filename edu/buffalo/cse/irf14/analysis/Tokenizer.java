/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author nikhillo
 * Class that converts a given string into a {@link TokenStream} instance
 */
public class Tokenizer {
	/**
	 * Default constructor. Assumes tokens are whitespace delimited
	 */
	private String delimiter = " ";
	private boolean addRawTerms = false;
	
	public Tokenizer() { }
	
	public Tokenizer(boolean addRawTerms) {
		this.addRawTerms = addRawTerms;
	}

	/**
	 * Overloaded constructor. Creates the tokenizer with the given delimiter
	 * @param delim : The delimiter to be used
	 */
	public Tokenizer(String delim) {
		this.delimiter = delim;
	}

	/**
	 * Method to convert the given string into a TokenStream instance.
	 * This must only break it into tokens and initialize the stream.
	 * No other processing must be performed. Also the number of tokens
	 * would be determined by the string and the delimiter.
	 * So if the string were "hello world" with a whitespace delimited
	 * tokenizer, you would get two tokens in the stream. But for the same
	 * text used with lets say "~" as a delimiter would return just one
	 * token in the stream.
	 * @param str : The string to be consumed
	 * @return : The converted TokenStream as defined above
	 * @throws TokenizerException : In case any exception occurs during
	 * tokenization
	 */
	public TokenStream consume(String str) throws TokenizerException {
		if(!Util.isValidString(str)) {
			throw new TokenizerException("Invalid String passed");
		}

		String[] tokenStringArray = str.split(delimiter);
		if(tokenStringArray != null) {
			TokenStream tokenStream = new TokenStream();
			for(String currentTokenString : tokenStringArray) {
				if(Util.isValidString(currentTokenString)) {
					currentTokenString = currentTokenString.trim();
					Token token = new Token(currentTokenString);
					
					if(addRawTerms) {
						currentTokenString = currentTokenString.replaceAll("\\-|\\(|\\)|\\<|\\>|\\-|\\_|[0-9]|&|#|!|;|:", "");
						if(Util.isValidString(currentTokenString)) {
							Util.addTermToRawTermIndex(currentTokenString.toLowerCase());
						}
					}
					tokenStream.add(token);
				}
			}
			return tokenStream;
		}
		return null;
	}
}
