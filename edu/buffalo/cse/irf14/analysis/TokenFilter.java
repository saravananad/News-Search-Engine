/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * The abstract class that you must extend when implementing your 
 * TokenFilter rule implementations.
 * Apart from the inherited Analyzer methods, we would use the 
 * inherited constructor (as defined here) to test your code.
 * @author nikhillo
 *
 */
public abstract class TokenFilter implements Analyzer {

	public TokenStream tokenStream = null;
	
	public TokenFilter() {}
	/**
	 * Default constructor, creates an instance over the given
	 * TokenStream
	 * @param stream : The given TokenStream instance
	 */
	public TokenFilter(TokenStream stream) {
		this.tokenStream = stream;
	}
	
	public void setStream(TokenStream stream) {
		this.tokenStream = stream;
	}
}
