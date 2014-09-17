/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import edu.buffalo.cse.irf14.index.IndexWriter;

/**
 * @author nikhillo
 * Class that converts a given string into a {@link TokenStream} instance
 */
public class Tokenizer {
	/**
	 * Default constructor. Assumes tokens are whitespace delimited
	 */

	public Tokenizer() {
		//TODO : YOU MUST IMPLEMENT THIS METHOD
	}

	/**
	 * Overloaded constructor. Creates the tokenizer with the given delimiter
	 * @param delim : The delimiter to be used
	 */
	public Tokenizer(String delim) {
		//TODO : YOU MUST IMPLEMENT THIS METHOD
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
		if(IndexWriter.isValidString(str)) {
			String[] tokenStringArray = str.split("\\s");
			if(tokenStringArray != null) {
				TokenStream tokenStream = new TokenStream();
				for(String currentTokenString : tokenStringArray) {
					if(IndexWriter.isValidString(currentTokenString)) {
						currentTokenString = currentTokenString.trim();
						Token token = new Token(currentTokenString);
						tokenStream.add(token);
					}
				}
				return tokenStream;
			}
		}
		return null;
	}
}
