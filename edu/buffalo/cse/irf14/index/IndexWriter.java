/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
	private static Map<String, Map<Long,Long>> termOccurrence = new TreeMap<String, Map<Long,Long>>();

	private Tokenizer tokenizer = new Tokenizer();
	private Tokenizer author_tokenizer = new Tokenizer("\\s+[a|A][n|N][d|D]\\s+");
	private Tokenizer place_tokenizer = new Tokenizer(",");

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
			String fileID = doc.getField(FieldNames.FILEID)[0];
			String category = null;
			if (Util.isValidArray(doc.getField(FieldNames.CATEGORY))){
				category = doc.getField(FieldNames.CATEGORY)[0];
			}
			AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
			long currentDocID = -1; //Initialized in the later part so that we don't make entry in the map. This filter will not run if initialized here.
			TokenStream categoryTokenStream = null;
			if (category != null && Util.isValidString(category)){
				categoryTokenStream = tokenizer.consume(category);
			}
			
			if(category != null) {
				Analyzer categoryAnalyser = analyzerFactory.getAnalyzerForField(FieldNames.CATEGORY, categoryTokenStream);
				while(categoryAnalyser.increment()) {}
				categoryTokenStream = categoryAnalyser.getStream();
			}

			if(!Util.hasDocInMap(fileID)) {
				String title = null, content = null, author = null, place = null;
				if (Util.isValidArray(doc.getField(FieldNames.TITLE))){
					title = doc.getField(FieldNames.TITLE)[0];
				}
				if (Util.isValidArray(doc.getField(FieldNames.CONTENT))){
					content = doc.getField(FieldNames.CONTENT)[0];
				}
				if(Util.isValidArray(doc.getField(FieldNames.AUTHOR))) {
					author = doc.getField(FieldNames.AUTHOR)[0];
				}
				if(Util.isValidArray(doc.getField(FieldNames.PLACE))) {
					place = doc.getField(FieldNames.PLACE)[0];
				}

				TokenStream titleTokenStream = null, termTokenStream = null,authorTokenStream = null, placeTokenStream = null;

				/* Tokenize the parsed fields */
				if (title != null && Util.isValidString(title)) {
					titleTokenStream = tokenizer.consume(title);
				}

				if (content != null && Util.isValidString(content)){
					termTokenStream = tokenizer.consume(content);
				}

				if(author != null && Util.isValidString(author)) {
					authorTokenStream = author_tokenizer.consume(author);
				}

				if(place != null && Util.isValidString(place)) {
					placeTokenStream = place_tokenizer.consume(place);
				}

				if(authorTokenStream != null) {
					Analyzer authorAnalyser = analyzerFactory.getAnalyzerForField(FieldNames.AUTHOR, authorTokenStream);
					while(authorAnalyser.increment()) {}
					authorTokenStream = authorAnalyser.getStream();
				}

				if(placeTokenStream != null) {
					Analyzer placeAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.PLACE, placeTokenStream);
					while(placeAnalyzer.increment()) {}
					placeTokenStream = placeAnalyzer.getStream();
				}

				if (termTokenStream != null){
					if (titleTokenStream != null) {
						titleTokenStream.toLowerCase();
						termTokenStream.append(titleTokenStream);
					}

					Analyzer contentAnalyzer = analyzerFactory.getAnalyzerForField(FieldNames.CONTENT, termTokenStream);
					while(contentAnalyzer.increment()) {}
					termTokenStream = contentAnalyzer.getStream();		
				}
				//Index Creation
				Map<FieldNames,TokenStream> map = new HashMap<FieldNames, TokenStream>();
				map.put(FieldNames.CONTENT, termTokenStream);
				if(authorTokenStream != null) {
					map.put(FieldNames.AUTHOR, authorTokenStream);
				}
				if(placeTokenStream != null) {
					map.put(FieldNames.PLACE, placeTokenStream);
				}
				currentDocID = Util.getDocID(fileID);
				createIndex(currentDocID, map);
			}
			
			if(currentDocID == -1) {
				currentDocID = Util.getDocID(fileID);
			}
			handleCategoryIndex(currentDocID, categoryTokenStream);
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
		handleFileWrite(indexWriteDir);
	}

	private static void createIndex(long currentDocID, Map<FieldNames, TokenStream> tokenStreamMap) {
		Iterator<Entry<FieldNames, TokenStream>> iterator = tokenStreamMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<FieldNames, TokenStream> next = iterator.next();
			FieldNames field = next.getKey();
			TokenStream tokenStream = next.getValue();
			if(FieldNames.CONTENT == field) {
				handleTermIndex(currentDocID, tokenStream);
			} else if(FieldNames.AUTHOR == field) {
				handleAuthorIndex(currentDocID, tokenStream);
			} else {
				handlePlaceIndex(currentDocID, tokenStream);
			}
		}

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

						// Term Occurrence 
						Map<Long, Long> termOccurrenceMap = termOccurrence.get(token.toString());
						Long occurrence = termOccurrenceMap.get(currentDocID);
						occurrence = occurrence == null ? 1L : ++occurrence;

						termOccurrenceMap.put(Long.valueOf(currentDocID), occurrence);
						termOccurrence.put(token.toString(), termOccurrenceMap);
					} else {
						List<Long> list = new LinkedList<Long>();
						list.add(currentDocID);
						termIndex.put(token.toString(), list);

						//Term Occurrence
						Map<Long, Long> termOccurrenceMap = new TreeMap<Long, Long>();
						termOccurrenceMap.put(currentDocID, 1L);
						termOccurrence.put(token.toString(), termOccurrenceMap);
					}
				}
			}
		}
	}

	private static void handleAuthorIndex(long currentDocID, TokenStream tokenStream) {
		if(tokenStream != null && currentDocID != -1) {
			while(tokenStream.hasNext()) {
				Token token = tokenStream.next();
				if(token != null && Util.isValidString(token.toString())) {
					if(authorIndex.containsKey(token.toString())) {
						List<Long> list = authorIndex.get(token.toString());
						if(!list.contains(token.toString())) {
							list.add(currentDocID);
						}
					} else {
						List<Long> list = new LinkedList<Long>();
						list.add(currentDocID);
						authorIndex.put(token.toString(), list);
					}
				}
			}
		}
	}

	private static void handleCategoryIndex(long currentDocID, TokenStream tokenStream) {
		if(tokenStream != null && currentDocID != -1) {
			while(tokenStream.hasNext()) {
				Token token = tokenStream.next();
				if(token != null && Util.isValidString(token.toString())) {
					if(categoryIndex.containsKey(token.toString())) {
						List<Long> list = categoryIndex.get(token.toString());
						if(!list.contains(currentDocID)) {
							list.add(currentDocID);
						}
					} else {
						List<Long> list = new LinkedList<Long>();
						list.add(currentDocID);
						categoryIndex.put(token.toString(), list);
					}
				}
			}
		}
	}

	private static void handlePlaceIndex(long currentDocID, TokenStream tokenStream) {
		if(tokenStream != null && currentDocID != -1) {
			while(tokenStream.hasNext()) {
				Token token = tokenStream.next();
				if(token != null && Util.isValidString(token.toString())) {
					if(placeIndex.containsKey(token.toString())) {
						List<Long> list = placeIndex.get(token.toString());
						if(!list.contains(currentDocID)) {
							list.add(currentDocID);
						}
					} else {
						List<Long> list = new LinkedList<Long>();
						list.add(currentDocID);
						placeIndex.put(token.toString(), list);
					}
				}
			}
		}
	}

	private static void writeToFile(PrintWriter writer, Map<String,List<Long>> stream) {
		Iterator<Entry<String, List<Long>>> iterator = stream.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, List<Long>> next = iterator.next();
			writer.write(next.getKey() + Util.dictionaryDelimiter);
			String postings = next.getValue().toString();
			postings = postings.substring(1, postings.lastIndexOf("]")).replace(", ", ",");
			writer.write((String) postings + "\n");
		}
	}

	public static void handleFileWrite(String indexWriteDir) {
		PrintWriter writer = null;
		try {
			if(termIndex != null) {
				File termIndexFile = new File(indexWriteDir + File.separator + Util.termIndexFile);
				termIndexFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(termIndexFile)));
				writeToFile(writer, termIndex);
				writer.close();
			}

			if(categoryIndex != null) {
				File categoryIndexFile = new File(indexWriteDir + File.separator + Util.categoryIndexFile);
				categoryIndexFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(categoryIndexFile)));
				writeToFile(writer, categoryIndex);
				writer.close();	
			}

			if(authorIndex != null) {
				File authorIndexFile = new File(indexWriteDir + File.separator + Util.authorIndexFile);
				authorIndexFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(authorIndexFile)));
				writeToFile(writer, authorIndex);
				writer.close();	
			}

			if(placeIndex != null) {
				File placeIndexFile = new File(indexWriteDir + File.separator + Util.placeIndexFile);
				placeIndexFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(placeIndexFile)));
				writeToFile(writer, placeIndex);
				writer.close();
			}

			if(termOccurrence != null) {
				File termOccurrenceFile = new File(indexWriteDir + File.separator + Util.termOccurenceFile);
				termOccurrenceFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(termOccurrenceFile)));
				Iterator<Entry<String, Map<Long, Long>>> iterator = termOccurrence.entrySet().iterator();
				while(iterator.hasNext()) {
					Entry<String, Map<Long, Long>> next = iterator.next();
					String termName = next.getKey();
					Map<Long, Long> docOccurrences = next.getValue();
					Iterator<Entry<Long, Long>> innerIterator = docOccurrences.entrySet().iterator();
					StringBuilder line = new StringBuilder();
					line.append(termName + Util.dictionaryDelimiter);
					while(innerIterator.hasNext()) {
						Entry<Long, Long> innerNext = innerIterator.next();
						line.append(innerNext.getKey() + Util.invidualDoc_OccurDelimiter + innerNext.getValue());
						line.append(Util.occurenceDelimiter);
					}
					String substring = line.substring(0, line.length() - Util.occurenceDelimiter.length()) + "\n";
					writer.write(substring);
				}
				writer.close();
			}

			Map<String, Long> docIDMap = Util.getDocIDMap();
			if(docIDMap != null) {
				File docIndexFile = new File(indexWriteDir + File.separator + Util.docDictionaryFile);
				docIndexFile.getParentFile().mkdir();
				writer = new PrintWriter(new BufferedWriter(new FileWriter(docIndexFile)));
				Iterator<Entry<String, Long>> iterator = docIDMap.entrySet().iterator();
				while(iterator.hasNext()) {
					Entry<String, Long> next = iterator.next();
					writer.write(next.getKey() + Util.dictionaryDelimiter);
					String postings = next.getValue().toString();
					postings = postings.replace(", ", ",");
					writer.write((String) postings + "\n");
				}
				writer.close();
			}

		} catch (IOException e) {
			System.err.println(e);
		}

	}
}