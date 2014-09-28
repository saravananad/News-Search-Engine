/**
 * 
 */
package edu.buffalo.cse.irf14.index;

/**
 * @author nikhillo
 * Generic wrapper exception class for indexing exceptions
 */
public class IndexerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3012675871474097239L;

	public IndexerException() {
		System.err.println("General Error :: Exception is Indexer");
	}
	
	public IndexerException(String message) {
		System.err.println("Error in Indexer :: " + message);
	}
	
}
