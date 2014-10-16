package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.buffalo.cse.irf14.analysis.Util;

public class TFIDFModel {

	ArrayList<String> userQuery;
	ArrayList<String> postings;
	Map<String, String> docFreqMap;
	int totalNumberOfDocuments = 130; // TO-DO

	public TFIDFModel(ArrayList<String> query, Map<String, String> docMap, ArrayList<String> postingsList) {
		this.userQuery = query;
		this.postings = postingsList;
		this.docFreqMap = docMap;
	}

	public Map<String, Map<String, String>> constructTermFreqMap(String[] userQuery, String[] postingsArray){
		Map<String, Map<String, String>> termOccurrence = new TreeMap<String, Map<String, String>>();
		for (String docID : postingsArray){
			Map<String, String> idFreqMap = new TreeMap<String, String>();
			for (String queryTerm : userQuery){
				if (queryTerm.equals("null"))
					idFreqMap.put(queryTerm, Util.ZERO);
				else {
					Map<String, Integer> termFreqMap = Util.termOccurrence.get(queryTerm);
					if (termFreqMap.isEmpty())
						idFreqMap.put(queryTerm, Util.ZERO);
					else 
						idFreqMap.put(queryTerm, termFreqMap.get(docID).toString());
				}
			}
			termOccurrence.put(docID, idFreqMap);
		}
		return termOccurrence;
	}

	public Map<String, Map<String, String>> calculateTF(Map<String, Map<String, String>> postings){
		String newFrequency = "";
		for(Entry<String, Map<String, String>> entry : postings.entrySet()) {
			Map<String, String> innerMap = entry.getValue();
			for (Entry<String, String> entryInner : innerMap.entrySet()){
				if (!entryInner.getValue().equals(Util.ZERO)){
					double logFreq = 1 + (Math.log(Double.parseDouble(entryInner.getValue())));			
					newFrequency = String.valueOf(Double.valueOf(Util.newFormat.format(logFreq)));
					entryInner.setValue(newFrequency);
					entry.setValue(innerMap);
				}
			}
		}
		return postings;
	}

	public Map<String, String> calculateIDF(Map<String, String> docFreqMap){
		for (Entry<String, String> entry : docFreqMap.entrySet()){
			if (!entry.getKey().equals("null")){
				String newFrequency = "";
				double logFreq = Math.log(totalNumberOfDocuments/Double.parseDouble(entry.getValue()));			
				newFrequency = String.valueOf(Double.valueOf(Util.newFormat.format(logFreq)));
				entry.setValue(newFrequency);
			}
		}
		return docFreqMap;
	}

	public Map<String, Map<String, String>> calculateTFIDF(Map<String, Map<String, String>> tfMap, Map<String, String> idfMap){
		for(Entry<String, Map<String, String>> entry : tfMap.entrySet()) {
			Map<String, String> innerMap = entry.getValue();
			for (Entry<String, String> entryInner : innerMap.entrySet()){
				if(!entryInner.getValue().equals(Util.ZERO)){
					Double tfIdf = Double.parseDouble(entryInner.getValue())*Double.parseDouble(idfMap.get(entryInner.getKey()));
					entryInner.setValue(String.valueOf(Double.valueOf(Util.newFormat.format(tfIdf))));
				}
			}
		}
		return tfMap;
	}

	public Map<String,String> calculateCosine(Map<String, Map<String, String>> tfIdfMap, Map<String, String> querytfidf){
		Map<String,String> scoreMap = new TreeMap<String,String>();
		for(Entry<String, Map<String, String>> entry : tfIdfMap.entrySet()) {
			Map<String, String> innerMap = entry.getValue();
			Double vectorsProduct = 0.0;
			Double vectorOneLen = 0.0;
			Double vectorTwoLen = 0.0;
			for(Entry<String, String> innerEntry : innerMap.entrySet()){
				vectorsProduct += Double.parseDouble(innerEntry.getValue())*Double.parseDouble(querytfidf.get(innerEntry.getKey()));
				vectorOneLen += Math.pow(Double.parseDouble(innerEntry.getValue()), 2);
				vectorTwoLen += Math.pow(Double.parseDouble(querytfidf.get(innerEntry.getKey())), 2);
			}
			Double score = (vectorsProduct)/((Math.sqrt(vectorOneLen)*(Math.sqrt(vectorTwoLen))));
			scoreMap.put(entry.getKey(), String.valueOf(Util.newFormat.format(score)));
		}
		return scoreMap;
	}

	public Map<String, String> performTFIDFRanking(){
		String[] queryArray = userQuery.toArray(new String[userQuery.size()]);
		String[] postingsArray = postings.toArray(new String[postings.size()]);
		
		// Construct the TF-IDF Mesh
		Map<String, Map<String, String>> termOccurrence = constructTermFreqMap(queryArray, postingsArray);
		Map<String, Map<String, String>> logTF = calculateTF(termOccurrence);
		Map<String, String> logIDF = calculateIDF(docFreqMap);
		Map<String, Map<String, String>> tfIdfDocMatrix = calculateTFIDF(logTF, logIDF);
		
		// Perform the operations that should be done considering query as one document
		Map<String, Map<String, String>> queryMap = new TreeMap<String, Map<String,String>>();
		Set<String> querySet = new HashSet<String>(userQuery);
		Map<String, String> tfQueryMap = new TreeMap<String, String>();
		for (String queryTerm : querySet) {
			tfQueryMap.put(queryTerm, String.valueOf(Collections.frequency(querySet, queryTerm)));
		}
		queryMap.put("Query", tfQueryMap);
		Map<String, Map<String, String>> tfIdfQueryMatrix = calculateTFIDF(queryMap, logIDF);
		tfQueryMap = tfIdfQueryMatrix.get("Query");
		
		// Calculate the Cosine Similarity between the query and every document in the postings list
		Map<String, String> cosineScores = calculateCosine(tfIdfDocMatrix, tfQueryMap);
		return cosineScores;
	}
}
