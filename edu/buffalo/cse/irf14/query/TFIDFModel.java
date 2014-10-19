package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.index.IndexType;

public class TFIDFModel implements RankingModel {

	String indexDir;
	ArrayList<String> userQuery;
	ArrayList<String> postings;
	Map<String, String> docFreqMap;

	public TFIDFModel(String indexDirectory, ArrayList<String> query, Map<String, String> docMap, ArrayList<String> postingsList) {
		this.indexDir = indexDirectory;
		this.userQuery = query;
		this.postings = postingsList;
		this.docFreqMap = docMap;
	}

	public Map<String, Map<String, String>> constructTermFreqMap(String[] userQuery, ArrayList<String> postingsArray){
		Map<String, Map<String, String>> termOccurrence = new TreeMap<String, Map<String, String>>();
		for (String docID : postingsArray){
			Map<String, String> idFreqMap = new TreeMap<String, String>();
			for (String queryTerm : userQuery){
				String splitString[] = queryTerm.split(":");
				if (splitString[1].equals("null")){
					idFreqMap.put(splitString[1], Util.ZERO);
				}
				else {
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
				double logFreq = Math.log(Util.totalDocuments/Double.parseDouble(entry.getValue()));			
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

	public ArrayList<Map<String,String>> calculateCosine(Map<String, Map<String, String>> tfIdfMap, Map<String, String> querytfidf){
		ArrayList<Map<String,String>> maps = new ArrayList<Map<String,String>>();
		Map<String,String> scoreMap = new TreeMap<String,String>();
		Map<String, String> reverseMap = new TreeMap<String, String>(Collections.reverseOrder());
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
			Integer docLength = Util.docSizeMap.get(entry.getKey());
			score = score/(docLength);
			scoreMap.put(entry.getKey(), String.valueOf(Util.newFormat.format(score)));
			reverseMap.put(String.valueOf(Util.newFormat.format(score)), entry.getKey());
		}
		maps.add(scoreMap);
		maps.add(reverseMap);
		return maps;
	}

	public Map<String, String> evaluatePostings(){
		String[] queryArray = userQuery.toArray(new String[userQuery.size()]);

		// Construct the TF-IDF Mesh
		Map<String, Map<String, String>> termOccurrence = constructTermFreqMap(queryArray, postings);
		Map<String, Map<String, String>> logTF = calculateTF(termOccurrence);
		Map<String, String> logIDF = calculateIDF(docFreqMap);
		Map<String, Map<String, String>> tfIdfDocMatrix = calculateTFIDF(logTF, logIDF);

		// Perform the operations that should be done considering query as one document
		Map<String, Map<String, String>> queryMap = new TreeMap<String, Map<String,String>>();
		Set<String> querySet = new HashSet<String>(userQuery);
		Map<String, String> tfQueryMap = new TreeMap<String, String>();
		for (String queryTerm : querySet) {
			String splitString[] = queryTerm.split(":");
			tfQueryMap.put(splitString[1], String.valueOf(Collections.frequency(querySet, queryTerm)));
		}
		queryMap.put("Query", tfQueryMap);
		Map<String, Map<String, String>> queryTFMap = calculateTF(queryMap);
		Map<String, Map<String, String>> tfIdfQueryMatrix = calculateTFIDF(queryTFMap, logIDF);
		tfQueryMap = tfIdfQueryMatrix.get("Query");

		// Calculate the Cosine Similarity between the query and every document in the postings list
		ArrayList<Map<String, String>> cosineScores = calculateCosine(tfIdfDocMatrix, tfQueryMap);
		Map<String, String> reverseMap = cosineScores.get(1);
		int size = (reverseMap.size() < 10) ? reverseMap.size() : 10;
		Map<String, String> topDocs = Util.getTopDocs(reverseMap,cosineScores.get(0), size);
		return topDocs;
	}
}
