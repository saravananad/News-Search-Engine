/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexerException;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter {
	/**
	 * Default constructor
	 * @param indexDir : The root directory to be sued for indexing
	 */
	public IndexWriter(String indexDir) {
		//TODO : YOU MUST IMPLEMENT THIS
	}

	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */
	public void addDocument(Document doc) throws IndexerException {
		try {
			String[] title = doc.getField(FieldNames.TITLE);
			String[] content = doc.getField(FieldNames.CONTENT);
			Tokenizer tokenizer = new Tokenizer();
			TokenStream titleTokenStream = null,contentTokenStream = null;
			if(Util.isValidString(title[0])) {
				titleTokenStream = tokenizer.consume(title[0]);
			}

			if(Util.isValidString(content[0])) {
				contentTokenStream = tokenizer.consume(content[0]);
			}
			
			AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
//			Analyzer titleAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.TITLE, titleTokenStream);
//			while (titleAnalyzer.increment()) {}
//			titleTokenStream = titleAnalyzer.getStream();
			
			if(contentTokenStream != null) {
				Analyzer contentAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, contentTokenStream);
				while (contentAnalyzer.increment()) {}
				contentTokenStream = contentAnalyzer.getStream();
			}
			
			// Have to index after this.
		
		} catch (TokenizerException te) {
			System.err.println(te);
		}
	}

	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException {
		//TODO
	}
}
