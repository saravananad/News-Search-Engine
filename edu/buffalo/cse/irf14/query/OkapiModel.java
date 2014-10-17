package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.index.IndexType;

public class OkapiModel {

	private double k1 = 1.2d;
	private double k3 = 1.8d;
	private double b = 0.75d;
	String indexDir;
	ArrayList<String> userQuery;
	ArrayList<String> postings;
	Map<String, String> docFreqMap;
	
	public OkapiModel(String indexDir, ArrayList<String> query, Map<String, String> docMap, ArrayList<String> postingsList) {
		this.indexDir = indexDir;
		this.userQuery = query;
		this.postings = postingsList;
		this.docFreqMap = docMap;
	}

	public Map<String, Map<String, String>> constructTermFreqMap(String[] userQuery, ArrayList<String> postingsArray){
		Map<String, Map<String, String>> termOccurrence = new TreeMap<String, Map<String, String>>();
		for (String docID : postingsArray){
			Map<String, String> idFreqMap = new TreeMap<String, String>();
			for (String queryTerm : userQuery){
				if (queryTerm.equals("null"))
					idFreqMap.put(queryTerm, Util.ZERO);
				else {
					String splitString[] = queryTerm.split(":");
					IndexType indexType = IndexType.valueOf(splitString[0].toUpperCase());
					switch (indexType){
					case TERM: {
						String normalizedQueryTerm = splitString[1];
						String capitalizedQueryTerm = normalizedQueryTerm.toUpperCase();
						String firstCapitalizedTerm = normalizedQueryTerm.substring(0,1).toUpperCase() + normalizedQueryTerm.substring(1);
						Map<String, Integer> termFreqMap = Util.termOccurrence.get(normalizedQueryTerm);
						Map<String, Integer> capTermFreqMap = Util.termOccurrence.get(capitalizedQueryTerm);
						Map<String, Integer> firstLetterUpperMap = Util.termOccurrence.get(firstCapitalizedTerm);
						String occAsAnalyzedTerm = Util.ZERO;
						String occAsFullCapsTerm = Util.ZERO;
						String occAsfirstCharCapTerm = Util.ZERO;
						
						// Record Occurrences Neatly
						if (Util.isValid(termFreqMap)){
							if (Util.isValid(termFreqMap.get(docID)))
								occAsAnalyzedTerm = termFreqMap.get(docID).toString();
						}
						if (Util.isValid(capTermFreqMap)){
							if (Util.isValid(capTermFreqMap.get(docID)))
								occAsFullCapsTerm = capTermFreqMap.get(docID).toString();
						}
						if (Util.isValid(firstLetterUpperMap)){
							if (Util.isValid(firstLetterUpperMap.get(docID)))
								occAsfirstCharCapTerm = firstLetterUpperMap.get(docID).toString();
						}
						Integer totalOccurrences = Integer.parseInt(occAsAnalyzedTerm) + Integer.parseInt(occAsFullCapsTerm) + Integer.parseInt(occAsfirstCharCapTerm);
						if (termFreqMap == null && capTermFreqMap == null && firstLetterUpperMap == null){
							idFreqMap.put(normalizedQueryTerm, Util.ZERO);
						}
						else 
							idFreqMap.put(normalizedQueryTerm, String.valueOf(totalOccurrences));
					}
					break;
					case AUTHOR: {
						String authorName = splitString[1];
						ArrayList<String> postingsList = Util.getPostings(indexDir, indexType, authorName.trim());
						if (postingsList.contains(docID))
							idFreqMap.put(authorName, Util.ONE);
						else
							idFreqMap.put(authorName, Util.ZERO);					
					}
					break;
					case CATEGORY: {
						String categoryName = splitString[1];
						ArrayList<String> postingsList = Util.getPostings(indexDir, indexType, categoryName);
						if (postingsList.contains(docID))
							idFreqMap.put(categoryName, Util.ONE);
						else
							idFreqMap.put(categoryName, Util.ZERO);
					}
					break;
					case PLACE: {
						String placeName = splitString[1].toLowerCase();
						ArrayList<String> postingsList = Util.getPostings(indexDir, indexType, placeName);
						if (postingsList.contains(docID))
							idFreqMap.put(splitString[1], Util.ONE);
						else
							idFreqMap.put(splitString[1], Util.ZERO);
					}
					break;
					}
				}
			}
			termOccurrence.put(docID, idFreqMap);
		}
		return termOccurrence;
	}

	public final double weight(double termFrequency, Integer docLength, double docFrequency) {
		double K = k1 * ((1 - b) + b * docLength / Util.averageDocLength) + termFrequency;
		return (termFrequency / K)* Math.log((Util.totalDocuments - docFrequency + 0.5d) / (docFrequency + 0.5d));
	}

	public final double score(String termFrequency, Integer docLength, String docFrequency, String queryFrequency) {
		double termFreq = Double.parseDouble(termFrequency);
		double docFreq = Double.parseDouble(docFrequency);
		double queryFreq = Double.parseDouble(queryFrequency);
		return ((k3 + 1d) * queryFreq) / ((k3 + queryFreq)) * weight(termFreq, docLength, docFreq);
	}
	
	public Map<String, String> performOkapiRanking(){
		String[] queryArray = userQuery.toArray(new String[userQuery.size()]);
		
		// Construct the TF-IDF Mesh
		Map<String, Map<String, String>> termOccurrence = constructTermFreqMap(queryArray, postings);
		Map<String, String> relevanceMap = new TreeMap<String, String>();
		
		// Construct a map with query term as key and its occurrence in query as value
		Set<String> querySet = new HashSet<String>(userQuery);
		Map<String, String> tfQueryMap = new TreeMap<String, String>();
		for (String queryTerm : querySet) {
			String splitString[] = queryTerm.split(":");
			tfQueryMap.put(splitString[1], String.valueOf(Collections.frequency(querySet, queryTerm)));
		}

		Map<String, String> reverseMap = new TreeMap<String, String>(Collections.reverseOrder());
		
		// Construct Okapi Relevancy Model Scores
		for (String docID : postings){
			double relevanceScore = 0.0d;
			for (String queryterm : queryArray){
				String[] queryTermSplit = queryterm.split(":");
				String termFrequency = termOccurrence.get(docID).get(queryTermSplit[1]);
				Integer docLength = Util.docSizeMap.get(docID);
				String docFrequency = docFreqMap.get(queryTermSplit[1]);
				relevanceScore += score(termFrequency, docLength, docFrequency, tfQueryMap.get(queryTermSplit[1]));
			}
			relevanceMap.put(docID, String.valueOf(Double.valueOf(Util.newFormat.format(relevanceScore))));
			reverseMap.put(String.valueOf(Double.valueOf(Util.newFormat.format(relevanceScore))), docID);
		}
		int size = (reverseMap.size() < 10) ? reverseMap.size() : 10;
		Map<String, String> topDocs = Util.getTopDocs(reverseMap, relevanceMap, size);
		return topDocs;
	}
}
