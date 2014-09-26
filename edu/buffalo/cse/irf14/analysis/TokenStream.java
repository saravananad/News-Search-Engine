/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author nikhillo
 * Class that represents a stream of Tokens. All {@link Analyzer} and
 * {@link TokenFilter} instances operate on this to implement their
 * behavior
 */
public class TokenStream implements Iterator<Token> {
    private int currentIndex = 0, lastNextIndex = -1;
    
	private LinkedList<Token> tokenList = new LinkedList<Token>();
	
	/**
	 * Method that adds a Token to the stream
	 */
	public void add(Token singleToken){
		tokenList.add(singleToken);
	}
	
	public String toString() {
		return tokenList.toString();
	}
	
	/**
	 * Method that checks if there is any Token left in the stream
	 * with regards to the current pointer.
	 * DOES NOT ADVANCE THE POINTER
	 * @return true if at least one Token exists, false otherwise
	 */
	@Override
	public boolean hasNext() {
		return tokenList.isEmpty() ? false : currentIndex < tokenList.size();
	}

	/**
	 * Method to return the next Token in the stream. If a previous
	 * hasNext() call returned true, this method must return a non-null
	 * Token.
	 * If for any reason, it is called at the end of the stream, when all
	 * tokens have already been iterated, return null
	 */
	@Override
	public Token next() {
		if (!hasNext()){
			lastNextIndex = -1;
			return null;
		}
		else {
			return tokenList.get(lastNextIndex = currentIndex++);
		}
	}
	
	/**
	 * Method to remove the current Token from the stream.
	 * Note that "current" token refers to the Token just returned
	 * by the next method. 
	 * Must thus be NO-OP when at the beginning of the stream or at the end
	 */
	@Override
	public void remove() {
		if ((lastNextIndex >= 0) && (lastNextIndex < tokenList.size())){
			tokenList.remove(lastNextIndex);	
			currentIndex = lastNextIndex;
			lastNextIndex = -1;
		}
	}
	
	/**
	 * Method to reset the stream to bring the iterator back to the beginning
	 * of the stream. Unless the stream has no tokens, hasNext() after calling
	 * reset() must always return true.
	 */
	public void reset() {
		currentIndex = 0;
	}
	
	/**
	 * Method to append the given TokenStream to the end of the current stream
	 * The append must always occur at the end irrespective of where the iterator
	 * currently stands. After appending, the iterator position must be unchanged
	 * Of course this means if the iterator was at the end of the stream and a 
	 * new stream was appended, the iterator hasn't moved but that is no longer
	 * the end of the stream.
	 * @param stream : The stream to be appended
	 */
	public void append(TokenStream stream) {
		if (stream!= null){
			while(stream.hasNext()){
				tokenList.add(stream.next());
			}
			stream.reset();
		}
	}
	
	public void toLowerCase() {
		this.reset();
		while(hasNext()) {
			Token token = next();
			token.setTermText(token.toString().toLowerCase());
		}
		this.reset();
	}
	
	/**
	 * Method to get the current Token from the stream without iteration.
	 * The only difference between this method and {@link TokenStream#next()} is that
	 * the latter moves the stream forward, this one does not.
	 * Calling this method multiple times would not alter the return value of {@link TokenStream#hasNext()}
	 * @return The current {@link Token} if one exists, null if end of stream
	 * has been reached or the current Token was removed
	 */
	public Token getCurrent() {
		return ((lastNextIndex < 0)? null : tokenList.get(lastNextIndex));
	}
	
	public Token getNextWithOutMovingPointer() {
		if (!hasNext()){
			return null;
		} else {
			return tokenList.get(currentIndex + 1);
		}
	}
	
	public boolean hasPrevious() {
		return tokenList.isEmpty() ? false : currentIndex > 1;
	}
	
	public Token getPrevious() {
		if (!hasPrevious()){
			return null;
		} else {
			return tokenList.get(currentIndex - 2);
		}
	}
	
}