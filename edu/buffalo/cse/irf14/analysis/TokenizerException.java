/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author nikhillo
 * Wrapper exception class for any errors during Tokenization
 */
public class TokenizerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 215747832619773661L;
	
	public TokenizerException() {
		System.err.println("General Error :: Exception is Tokenizer");
	}
	
	public TokenizerException(String message) {
		System.err.println("Error in Tokenizer :: " + message);
	}

}
