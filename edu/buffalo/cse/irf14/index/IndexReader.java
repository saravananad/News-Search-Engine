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
	private static Map<String, List<String>> categoryMapping = new HashMap<String, List<String>>();
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
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(this.indexDirectory + File.separator + Util.docDictionaryFile)));
			String line = dictionaryReader.readLine();
			while ((line)!= null){
				String[] docIDPair = line.split(":");
				documentIDMap.put(docIDPair[1], docIDPair[0]); // New ID as Key, Old ID as value
				line = dictionaryReader.readLine();
			}
			dictionaryReader.close();
			
			/* Create a term occurrence dictionary */
			BufferedReader occurenceReader = new BufferedReader(new FileReader(new File (this.indexDirectory + File.separator + Util.termOccurenceFile)));
			String occurenceLine = occurenceReader.readLine();
			while (occurenceLine != null){
				int totalOcc = 0;
				Map<String, Integer> innerMap = new HashMap<String, Integer>();
				List<String> tokenList = new ArrayList<String>();
				String[] eachLine = occurenceLine.split(Util.dictionaryDelimiter);
				String tokenText = eachLine[0];
				String[] docIdOccurenceArray = eachLine[1].split(Util.occurenceDelimiter);
				for (String occurence : docIdOccurenceArray){
					String[] docIdOccurence = occurence.split(Util.invidualDoc_OccurDelimiter);
					totalOcc += Integer.parseInt(docIdOccurence[1]);
					innerMap.put(documentIDMap.get(docIdOccurence[0]), Integer.parseInt(docIdOccurence[1]));
				}
				termOccurrence.put(tokenText, innerMap); // Mapping of Token : Doc ID - Occurrence in each document 
				if (termTotalOccurence.containsKey(totalOcc)){
					List<String> eachTokenList = termTotalOccurence.get(totalOcc);
					if (!eachTokenList.contains(tokenText)){
						eachTokenList.add(tokenText);
					}
					termTotalOccurence.put(totalOcc, eachTokenList);
				}
				else {
					if (!tokenList.contains(tokenText)){
						tokenList.add(tokenText);
					}
					termTotalOccurence.put(totalOcc, tokenList);	
				}  
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
				BufferedReader authorIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + File.separator + Util.authorIndexFile)));
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
				BufferedReader termIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + File.separator + Util.termIndexFile)));
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
				BufferedReader placeIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + File.separator + Util.placeIndexFile)));
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
				BufferedReader categoryIndexReader = new BufferedReader(new FileReader(new File(this.indexDirectory + File.separator +  Util.categoryIndexFile)));
				String eachLine = categoryIndexReader.readLine();
				while (eachLine != null){
					String[] categoryPostingPair = eachLine.split(":");
					List<String> postingsList = new ArrayList<String>();
					String[] postings = categoryPostingPair[1].split(",");
					for (String docID : postings){
						postingsList.add(docID);
					}
					categoryMapping.put(categoryPostingPair[0], postingsList);
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
	
	public int getKeySetSize(Map<String, List<String>> map){
		Set<String> totalValueSet = new HashSet<String>();
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			List<String> postings = entry.getValue();
			for (String docID : postings){
				totalValueSet.add(docID);
			}
		}
		return totalValueSet.size();
	}

	public Map<String, Integer> buildPostingsMap(Map<String, List<String>> map, String term, int occurrence){
		Map<String, Integer> postings = new HashMap<String,Integer>();
		List<String> docList = map.get(term);
		for (String docID : docList){
			postings.put(documentIDMap.get(docID), occurrence);
		}
		return postings;
	}
	
	public List<String> buildOccurrenceMap(Map<String, List<String>> map){
		Map<Integer, List<String>> occurrenceMap = new TreeMap<Integer, List<String>>(Collections.reverseOrder());	
		List<String> totalArrayList = new ArrayList<String>();
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			List<String> tokensList = new ArrayList<String>();
			tokensList.add(entry.getKey());
			int total = entry.getValue().size();
			if (occurrenceMap.containsKey(total)){
				List<String> tokenList = occurrenceMap.get(total);
				tokenList.add(entry.getKey());
				occurrenceMap.put(total, tokenList);
			}
			else {
				occurrenceMap.put(total, tokensList);			
			}
		}
		for (Map.Entry<Integer, List<String>> entry : occurrenceMap.entrySet()) {
			List<String> eachList = entry.getValue();
			for (String token : eachList){
				totalArrayList.add(token);
			}
		}
		return totalArrayList;
	}
	
	/**
	 * Get total number of terms from the "key" dictionary associated with this 
	 * index. A postings list is always created against the "key" dictionary
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() {
		switch (indexType){
		case AUTHOR: 
			return authorMapping.size();
		case TERM: 
			return termMapping.size();
		case CATEGORY: 
			return categoryMapping.size();
		case PLACE: 
			return placeMapping.size();
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
		switch (indexType){
		case AUTHOR: 
			return getKeySetSize(authorMapping);
		case TERM: 
			return getKeySetSize(termMapping);
		case CATEGORY: 
			return getKeySetSize(categoryMapping);
		case PLACE: 
			return getKeySetSize(placeMapping);
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
			case CATEGORY:
				return (buildPostingsMap(categoryMapping, term, 0));
			case AUTHOR: 
				return (buildPostingsMap(authorMapping, term, 1));
			case PLACE:
				return (buildPostingsMap(placeMapping, term, 1));
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
			List<String> totalArrayList = buildOccurrenceMap(authorMapping);
			return (k > 0 && k <= totalArrayList.size()) ? totalArrayList.subList(0, k) : null;	
		}
		case PLACE: {
			List<String> totalArrayList = buildOccurrenceMap(placeMapping);
			return (k > 0 && k <= totalArrayList.size()) ? totalArrayList.subList(0, k) : null;			
		}
		case CATEGORY:{
			List<String> totalArrayList = buildOccurrenceMap(categoryMapping);
			return (k > 0 && k <= totalArrayList.size()) ? totalArrayList.subList(0, k) : null;				
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
