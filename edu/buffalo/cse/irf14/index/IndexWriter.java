/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.Token;
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
	private static Map<String, List<Long>> termIndex = new TreeMap<String, List<Long>>();
	private static Map<String, List<Long>> authorIndex = new TreeMap<String, List<Long>>();
	private static Map<String, List<Long>> categoryIndex = new TreeMap<String, List<Long>>();
	private static Map<String, List<Long>> placeIndex = new TreeMap<String, List<Long>>();
	
	private Tokenizer tokenizer = new Tokenizer();

	private String indexWriteDir = null;
	
	public IndexWriter(String indexDir) {
		indexWriteDir = indexDir;
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
			String docID = doc.getField(FieldNames.FILEID)[0];
			if(!Util.hasDocInMap(docID)) {
				String title = doc.getField(FieldNames.TITLE)[0];
				String content = doc.getField(FieldNames.CONTENT)[0];
				TokenStream titleTokenStream = null, termTokenStream = null;

				/* Tokenize the parsed fields */
				if (title != null && Util.isValidString(title)) {
					titleTokenStream = tokenizer.consume(title);
				}

				if (content != null && Util.isValidString(content)){
					termTokenStream = tokenizer.consume(content);
				}

				if (termTokenStream != null){
					if (titleTokenStream != null) {
						titleTokenStream.toLowerCase();
						termTokenStream.append(titleTokenStream);
					}

					AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
					Analyzer contentAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, termTokenStream);
					contentAnalyzer.increment();
					termTokenStream = contentAnalyzer.getStream();		
				}
				//Index Creation
				createTermIndex(doc, termTokenStream);
				handleAuthorIndex(doc);
				handlePlaceIndex(doc);
			}
			handleCategoryIndex(doc);
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
		PrintWriter writer = null;
		try {
			File termIndexFile = new File(indexWriteDir + "/term.txt");
			File categoryIndexFile = new File(indexWriteDir + "/category.txt");
			File authorIndexFile = new File(indexWriteDir + "/author.txt");
			File placeIndexFile = new File(indexWriteDir + "/place.txt");
			termIndexFile.getParentFile().mkdir();
			
			if(termIndex != null) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(termIndexFile)));
				writeToFile(writer, termIndex);
				writer.close();				
			}
			
			if(categoryIndex != null) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(categoryIndexFile)));
				writeToFile(writer, categoryIndex);
				writer.close();	
			}
			
			if(authorIndex != null) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(authorIndexFile)));
				writeToFile(writer, authorIndex);
				writer.close();	
			}
			
			if(placeIndex != null) {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(placeIndexFile)));
				writeToFile(writer, placeIndex);
				writer.close();
			}
			
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private static void createTermIndex(Document doc, TokenStream tokenStream) {
		String docName = doc.getField(FieldNames.FILEID)[0];
		long docID = Util.getDocID(docName);
		handleTermIndex(docID, tokenStream);
	}

	private static void handleTermIndex(long currentDocID, TokenStream tokenStream) {
		if(tokenStream != null && currentDocID != -1) {
			while(tokenStream.hasNext()) {
				Token token = tokenStream.next();
				if(Util.isValidString(token.toString())) {
					if(termIndex.containsKey(token.toString())) {
						List<Long> docList = termIndex.get(token.toString());
						if(!docList.contains(currentDocID)) {
							docList.add(currentDocID);
						}
					} else {
						List<Long> list = new LinkedList<Long>();
						list.add(currentDocID);
						termIndex.put(token.toString(), list);
					}
				}
			}
		}
	}
	
	private static void handleAuthorIndex(Document doc) {
		String authors[] = doc.getField(FieldNames.AUTHOR);
		long docID = Util.getDocID(doc.getField(FieldNames.FILEID)[0]);
		if(authors != null && authors.length > 0) {
			for(String author : authors) {
				if(authorIndex.containsKey(author)) {
					List<Long> list = authorIndex.get(author);
					if(!list.contains(author)) {
						list.add(docID);
					}
				} else {
					List<Long> list = new LinkedList<Long>();
					list.add(docID);
					authorIndex.put(author, list);
				}
			}
		}
	}
	
	private static void handleCategoryIndex(Document doc) {
		String currentCategory = doc.getField(FieldNames.CATEGORY)[0];
		long docID = Util.getDocID(doc.getField(FieldNames.FILEID)[0]);
		if(categoryIndex.containsKey(currentCategory)) {
			List<Long> list = categoryIndex.get(currentCategory);
			if(!list.contains(docID)) {
				list.add(docID);
			}
		} else {
			List<Long> list = new LinkedList<Long>();
			list.add(docID);
			categoryIndex.put(currentCategory, list);
		}
	}
	
	private static void handlePlaceIndex(Document doc) {
		String currentPlace = doc.getField(FieldNames.PLACE)[0];
		long docID = Util.getDocID(doc.getField(FieldNames.FILEID)[0]);
		if(placeIndex.containsKey(currentPlace)) {
			List<Long> list = placeIndex.get(currentPlace);
			if(!list.contains(docID)) {
				list.add(docID);
			}
		} else {
			List<Long> list = new LinkedList<Long>();
			list.add(docID);
			placeIndex.put(currentPlace, list);
		}
	}
	
	private static void writeToFile(PrintWriter writer, Map<String,List<Long>> stream) {
		Iterator<Entry<String, List<Long>>> iterator = stream.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, List<Long>> next = iterator.next();
			writer.write(next.getKey() + ":");
			String postings = next.getValue().toString();
			postings = postings.substring(1, postings.lastIndexOf("]")).replace(", ", ",");
			writer.write((String) postings + "\n");
		}
	}
}