/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Util;

/**
 * @author nikhillo
 * Class that emulates reading data back from a written index
 */
public class IndexReader {
	
	private String indexDirectory;
	private IndexType indexType;
	private Map<String, String> documentIDMap = new HashMap<String, String>();
	private static Map<String, List<String>> termMapping = new TreeMap<String, List<String>>();
	private static Map<String, List<String>> authorMapping= new TreeMap<String, List<String>>();
	private static Map<String, String> categoryMapping = new HashMap<String, String>();
	private static Map<String, List<String>> placeMapping = new TreeMap<String, List<String>>();
	private static Map<String, Map<String, Integer>> termOccurrence = new TreeMap<String, Map<String, Integer>>();	
	private static Map<Integer, List<String>> termTotalOccurence = new TreeMap<Integer, List<String>>();
	
	/**
	 * Default constructor
	 * @param indexDir : The root directory from which the index is to be read.
	 * This will be exactly the same directory as passed on IndexWriter. In case 
	 * you make subdirectories etc., you will have to handle it accordingly.
	 * @param type The {@link IndexType} to read from
	 */
	public IndexReader(String indexDir, IndexType type) {
		this.indexDirectory = indexDir;
		this.indexType = type;
		try {
			
			/* Create a document dictionary*/
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(this.indexDirectory + Util.docDictionaryFile)));
			String line = dictionaryReader.readLine();
			while ((line)!= null){
				String[] docIDPair = line.split(":");
				documentIDMap.put(docIDPair[1], docIDPair[0]); // New ID as Key, Old ID as value
				line = dictionaryReader.readLine();
			}
			dictionaryReader.close();
			
			/* Create a term occurrence dictionary */
			BufferedReader occurenceReader = new BufferedReader(new FileReader(new File (this.indexDirectory + Util.termOccurenceFile)));
			String occurenceLine = occurenceReader.readLine();
			while (occurenceLine != null){
				int totalOcc = 0;
				Map<String, Integer> innerMap = new HashMap<String, Integer>();
				List<String> tokenList = new ArrayList<String>();
				String[] eachLine = occurenceLine.split(Util.dictionaryDelimiter);
				String tokenText = eachLine[0];
				tokenList.add(tokenText);
				String[] docIdOccurenceArray = eachLine[1].split(Util.occurenceDelimiter);
				for (String occurence : docIdOccurenceArray){
					String[] docIdOccurence = occurence.split(Util.invidualDoc_OccurDelimiter);
					totalOcc += Integer.parseInt(docIdOccurence[1]);
					innerMap.put(documentIDMap.get(docIdOccurence[0]), Integer.parseInt(docIdOccurence[1]));
				}
				termOccurrence.put(tokenText, innerMap); // Mapping of Token : Doc ID - Occurrence in each document 
				termTotalOccurence.put(totalOcc, tokenList); // Mapping of occurrence - list of tokens 
				occurenceLine = occurenceReader.readLine();	
			}
			occurenceReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		switch (type) {
		case AUTHOR: {
			try {
				BufferedReader authorIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + Util.authorIndexFile)));
				String eachLine = authorIndexReader.readLine();
				while (eachLine != null){
					String[] authorPostingPair = eachLine.split(":");
					List<String> postingsList = new ArrayList<String>();
					String[] postings = authorPostingPair[1].split(",");
					for (String docID : postings){
						postingsList.add(docID);
					}
					authorMapping.put(authorPostingPair[0], postingsList);
					eachLine = authorIndexReader.readLine();
				}
				authorIndexReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		
		case TERM: {
			try {
				BufferedReader termIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + Util.termIndexFile)));
				String eachLine = termIndexReader.readLine();
				while (eachLine != null){
					String[] termPostingPair = eachLine.split(":");
					List<String> postingsList = new ArrayList<String>();
					String[] postings = termPostingPair[1].split(",");
					for (String docID : postings){
						postingsList.add(docID);
					}
					termMapping.put(termPostingPair[0], postingsList);
					eachLine = termIndexReader.readLine();
				}
				termIndexReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		
		case PLACE: {
			try {
				BufferedReader placeIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + Util.placeIndexFile)));
				String eachLine = placeIndexReader.readLine();
				while (eachLine != null){
					String[] placePostingPair = eachLine.split(":");
					List<String> postingsList = new ArrayList<String>();
					String[] postings = placePostingPair[1].split(",");
					for (String docID : postings){
						postingsList.add(docID);
					}
					placeMapping.put(placePostingPair[0], postingsList);
					eachLine = placeIndexReader.readLine();
				}
				placeIndexReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		
		case CATEGORY: {
			try {
				BufferedReader categoryIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + Util.categoryIndexFile)));
				String eachLine = categoryIndexReader.readLine();
				while (eachLine != null){
					String[] categoryPostingPair = eachLine.split(":");
					categoryMapping.put(categoryPostingPair[0], categoryPostingPair[1]);
					eachLine = categoryIndexReader.readLine();
				}
				categoryIndexReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;	
		}
		}
	}
	
	/**
	 * Get total number of terms from the "key" dictionary associated with this 
	 * index. A postings list is always created against the "key" dictionary
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() {
		switch (indexType){
		case AUTHOR: return authorMapping.size();
		case TERM: return termMapping.size();
		case CATEGORY: return categoryMapping.size();
		case PLACE: return placeMapping.size();
		default:
			return -1;
		}
	}
	
	/**
	 * Get total number of terms from the "value" dictionary associated with this 
	 * index. A postings list is always created with the "value" dictionary
	 * @return The total number of terms
	 */
	public int getTotalValueTerms() {
		Set<String> totalValueSet = new HashSet<String>();
		switch (indexType){
		case AUTHOR: {
			for(Map.Entry<String, List<String>> entry : authorMapping.entrySet()) {
				List<String> postings = entry.getValue();
				for (String docID : postings){
					totalValueSet.add(docID);
				}
			}
			return totalValueSet.size();		
		}
		case TERM: {
			for(Map.Entry<String, List<String>> entry : termMapping.entrySet()) {
				List<String> postings = entry.getValue();
				for (String docID : postings){
					totalValueSet.add(docID);
				}
			}
			return totalValueSet.size();	
		}
		case CATEGORY: {
			for(Map.Entry<String, String> entry : categoryMapping.entrySet()) {
				totalValueSet.add(entry.getValue());
			}
			return totalValueSet.size();	
		}
		case PLACE: {
			for(Map.Entry<String, List<String>> entry : placeMapping.entrySet()) {
				List<String> postings = entry.getValue();
				for (String docID : postings){
					totalValueSet.add(docID);
				}
			}
			return totalValueSet.size();	
		}
		default: return -1;
		}
	}
	
	/**
	 * Method to get the postings for a given term. You can assume that
	 * the raw string that is used to query would be passed through the same
	 * Analyzer as the original field would have been.
	 * @param term : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the 
	 * number of occurrences as values if the given term was found, null otherwise.
	 */
	public Map<String, Integer> getPostings(String term) {
		if (Util.isValidString(term)){
			switch (indexType){
			case TERM: 
				return (termOccurrence.containsKey(term)) ? termOccurrence.get(term) : null;
			default : return null;
			}
		}
		return null;
	}
	
	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * @param k : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k fr valid k values
	 * null for invalid k values
	 */
	public List<String> getTopK(int k) {
		switch (indexType){
		case TERM: {
			Map<Integer, List<String>> newReverseMap = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
			List<String> totalArrayList = new ArrayList<String>();
			newReverseMap.putAll(termTotalOccurence);
			for (Map.Entry<Integer, List<String>> entry : newReverseMap.entrySet()) {
				List<String> eachList = entry.getValue();
				for (String token : eachList){
					totalArrayList.add(token);
				}
			}
			return (k > 0 && k <= totalArrayList.size()) ? totalArrayList.subList(0, k) : null;
		}
		case AUTHOR:{
			
		}
		case PLACE: {
			
		}
		case CATEGORY:{
			
		}
		}
		return null;
	}
	
	/**
	 * Method to implement a simple boolean AND query on the given index
	 * @param terms The ordered set of terms to AND, similar to getPostings()
	 * the terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key 
	 * and number of occurrences as the value, the number of occurrences 
	 * would be the sum of occurrences for each participating term. return null
	 * if the given term list returns no results
	 * BONUS ONLY
	 */
	public Map<String, Integer> query(String...terms) {
		//TODO : BONUS ONLY
		return null;
	}
}
