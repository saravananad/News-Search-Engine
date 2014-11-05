package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.index.IndexType;

public class OkapiModel implements RankingModel{

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

	public Map<String, String> evaluatePostings(){
		String[] queryArray = userQuery.toArray(new String[userQuery.size()]);

		// Construct the TF-IDF Mesh
		Map<String, Map<String, String>> termOccurrence = Util.constructTermFreqMap(indexDir, queryArray, postings);
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
		
		//Scale the scores between 0 and 1
		ArrayList<String> valuesList = new ArrayList<String>(topDocs.values());
		Double largestValue = Double.parseDouble(valuesList.get(0));
		for (Map.Entry<String, String> entry : topDocs.entrySet()) {
			Double scaledValue = Double.parseDouble(entry.getValue())/largestValue;
			topDocs.put(entry.getKey(), String.valueOf(Util.newFormat.format(scaledValue)));
		}
		return topDocs;
	}
}
