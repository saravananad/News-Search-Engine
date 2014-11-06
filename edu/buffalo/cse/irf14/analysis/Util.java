package edu.buffalo.cse.irf14.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.query.QueryHandler;

public class Util {

	private static Map<String, Long> docIDMapping = new HashMap<String, Long>();
	private static long docID = 0;

	public static final String dictionaryDelimiter = ":";
	public static final String occurenceDelimiter = ";";
	public static final String invidualDoc_OccurDelimiter = "=";

	public static final String authorIndexFile = "AuthorIndex.txt";
	public static final String termIndexFile = "TermIndex.txt";
	public static final String categoryIndexFile = "CategoryIndex.txt";
	public static final String placeIndexFile = "PlaceIndex.txt";
	public static final String docDictionaryFile = "DocDictionary.txt";
	public static final String docSizeFIle = "docSize.txt";
	public static final String termOccurenceFile = "TermOccurence.txt";
	public static final String rawTermIndexFile = "rawTerms.txt";

	public static DecimalFormat newFormat = new DecimalFormat("#.#####");
	public static String ZERO = "0";
	public static String ONE = "1";
	public static Integer totalDocuments = 0;
	public static Double averageDocLength = 0.0;

	public static Map<String, String> documentIDMap = new HashMap<String, String>();
	public static List<String> rawTermList = new LinkedList<String>();
	public static Map<String, Map<String, Integer>> termOccurrence = new TreeMap<String, Map<String, Integer>>();

	public enum FilterList {
		STOPWORD(StopWordFilter.class.getName()),
		CAPITALIZATION(CapitalizationRule.class.getName()),
		STEMMER(StemmingRule.class.getName()),
		ACCENT(AccentRule.class.getName()),
		NUMERIC(NumberRule.class.getName()),
		SYMBOL(SymbolFilter.class.getName()),
		SPECIALCHARS(SpecialCharsRule.class.getName()),
		DATE(DateFilter.class.getName());


		private String className = null;

		private FilterList(String className) {
			this.className = className;
		}

		public String getClassName() {
			return this.className;
		}
	}

	public static boolean isValidString(String value) {
		return value != null && !"".equals(value.trim());
	}

	public static boolean isValid(Object value) {
		return value != null && !"null".equals(value);
	}

	public static boolean isValidArray(String[] stringArray){
		return stringArray != null && stringArray.length > 0;
	}

	public static boolean hasDocInMap(String docName) {
		return docIDMapping.containsKey(docName);
	}

	public static long getDocID(String docName) {
		if(isValidString(docName)) {
			Long docIDInMap = docIDMapping.get(docName);
			if(docIDInMap == null) {
				docIDMapping.put(docName, ++docID);
				return docID;
			}
			return docIDInMap;
		}
		return -1;
	}

	public static Map<String, Long> getDocIDMap() {
		return docIDMapping;
	}

	public enum Month {
		january("01"),jan("01"),
		february("02"),feb("02"), 
		march("03"), mar("03"),
		april("04"), apr("04"),
		may("05"), 
		june("06"), jun("06"),
		july("07"), jul("07"),
		august("08"), aug("08"),
		september("09"), sep("09"),
		october("10"), oct("10"),
		november("11"),nov("11"),
		december("12"), dec("12");

		private String value = null;

		private Month(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/************************** Searcher Project Methods *************************************/

	public static final String defaultOper = "OR";

	public static final String NOT_OPEN = "<";
	public static final String NOT_CLOSE = ">";
	private static final int NOT_PRIORITY = 5;

	public static final String OR = "OR";
	private static final int OR_PRIORITY = 6;

	public static final String AND = "AND";
	private static final int AND_PRIORITY = 7;

	public static final String OPEN_SQUARE_BRACKETS = "[";

	public static final String CLOSE_SQUARE_BRACKETS = "]";

	public static final String OPEN_BRACES = "{";

	public static final String CLOSE_BRACES = "}";

	private static Map<String, Integer> operPriorityMap = new HashMap<String, Integer>();
	public static Map<String, Integer> docSizeMap = new HashMap<String, Integer>();

	private static Map<String, ArrayList<String>> termMapping = new TreeMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> authorMapping= new TreeMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> categoryMapping = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> placeMapping = new TreeMap<String, ArrayList<String>>();
	public static String getDefaultBooleanOperator() {
		return defaultOper;
	}

	public static int getPriority(String value) {
		if(operPriorityMap.isEmpty()) {
			initPriorityMap();
		}

		return operPriorityMap.get(value);
	}

	public static boolean isOperator(String key) {
		if(operPriorityMap.isEmpty()) {
			initPriorityMap();
		}

		if(key.contains("{") || key.contains("}")) {
			return true;
		}

		return operPriorityMap.containsKey(key);
	}

	private static void initPriorityMap() {
		if(operPriorityMap.isEmpty()) {
			operPriorityMap.put(AND, AND_PRIORITY);
			operPriorityMap.put(OR, OR_PRIORITY);
			operPriorityMap.put(NOT_CLOSE, NOT_PRIORITY);
		}
	}

	/********************************* Build Term Occurrences Map *************************************/

	public static void initDocOccurrencesMap(String indexDirName){
		try {

			/* Create a document dictionary*/
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + docDictionaryFile)));
			String line = dictionaryReader.readLine();
			while ((line)!= null){
				String[] docIDPair = line.split(":");
				documentIDMap.put(docIDPair[1], docIDPair[0]); // New ID as Key, Old ID as value
				line = dictionaryReader.readLine();
			}
			dictionaryReader.close();

			/* Create a term occurrence dictionary */
			BufferedReader occurenceReader = new BufferedReader(new FileReader(new File (indexDirName + File.separator + termOccurenceFile)));
			String occurenceLine = occurenceReader.readLine();
			while (occurenceLine != null){
				Map<String, Integer> innerMap = new HashMap<String, Integer>();
				String[] eachLine = occurenceLine.split(dictionaryDelimiter);
				String tokenText = eachLine[0];
				String[] docIdOccurenceArray = eachLine[1].split(occurenceDelimiter);
				for (String occurence : docIdOccurenceArray){
					String[] docIdOccurence = occurence.split(invidualDoc_OccurDelimiter);
					innerMap.put(documentIDMap.get(docIdOccurence[0]), Integer.parseInt(docIdOccurence[1]));
				}
				termOccurrence.put(tokenText, innerMap); // Mapping of Token : Doc ID - Occurrence in each document 
				occurenceLine = occurenceReader.readLine();	
			}
			occurenceReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void calcAvgDocLength(){
		Integer sumOfAllLengths = 0;
		for (Integer length : docSizeMap.values()){
			sumOfAllLengths += length;
		}
		averageDocLength = (double) (sumOfAllLengths / totalDocuments);
	}

	public static Map<String, String> getTopDocs(Map<String, String> reverseMap, Map<String, String> relevanceMap, int size){
		Map<String, String> topDocsMap = new LinkedHashMap<String, String>();
		if (isValid(reverseMap) && isValid(relevanceMap)){
			for (Map.Entry<String, String> entry : reverseMap.entrySet()) {
				String docID = entry.getValue();
				topDocsMap.put(docID, relevanceMap.get(docID));
				if (topDocsMap.size() == size)
					break;
			}
			return topDocsMap;
		}
		return null;
	}

	private static void initMaps(String indexDirName, String fileName, Map<String, ArrayList<String>> mapType){
		try {
			if (documentIDMap.isEmpty() || termOccurrence.isEmpty()){
				initDocOccurrencesMap(indexDirName);
			}
			if(docSizeMap.isEmpty()) {
				initDocumentSizeMap(indexDirName);
			}
			totalDocuments = docSizeMap.size();
			calcAvgDocLength();
			BufferedReader indexReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + fileName)));		
			String eachLine = indexReader.readLine();
			while (Util.isValid(eachLine)){
				String[] eachPostingPair = eachLine.split(":");
				ArrayList<String> postingsList = new ArrayList<String>();
				String[] postings = eachPostingPair[1].split(",");
				if(postings != null) {
					for (String docID : postings){
						postingsList.add(documentIDMap.get(docID));
					}
					mapType.put(eachPostingPair[0], postingsList);
				}
				eachLine = indexReader.readLine();
			}
			indexReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void initDocumentSizeMap(String indexDirName) {
		try {
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + docSizeFIle)));
			String line = null;
			while ((line = dictionaryReader.readLine())!= null){
				String[] docIDPair = line.split(dictionaryDelimiter);
				docSizeMap.put(documentIDMap.get(docIDPair[0]), Integer.valueOf(docIDPair[1])); 
			}
			dictionaryReader.close();
		} catch(Exception ex) {
			System.err.println(ex);
		}
	}

	public static Map<String, Map<String, String>> constructTermFreqMap(String indexDir, String[] userQuery, ArrayList<String> postingsArray){
		Map<String, Map<String, String>> termOccMap = new TreeMap<String, Map<String, String>>();
		for (String docID : postingsArray){
			Map<String, String> idFreqMap = new TreeMap<String, String>();
			for (String queryTerm : userQuery){
				String splitString[] = queryTerm.split(":");
				if ("null".equals(splitString[1])){
					idFreqMap.put(splitString[1], ZERO);
				} else {
					IndexType indexType = IndexType.valueOf(splitString[0].toUpperCase());
					switch (indexType){
					case TERM: {
						String normalizedQueryTerm = splitString[1];
						String capitalizedQueryTerm = normalizedQueryTerm.toUpperCase();
						String firstCapitalizedTerm = normalizedQueryTerm.substring(0,1).toUpperCase() + normalizedQueryTerm.substring(1);
						Map<String, Integer> termFreqMap = termOccurrence.get(normalizedQueryTerm);
						Map<String, Integer> capTermFreqMap = termOccurrence.get(capitalizedQueryTerm);
						Map<String, Integer> firstLetterUpperMap = termOccurrence.get(firstCapitalizedTerm);
						String occAsAnalyzedTerm = ZERO;
						String occAsFullCapsTerm = ZERO;
						String occAsfirstCharCapTerm = ZERO;

						// Record Occurrences Neatly
						if (isValid(termFreqMap) && isValid(termFreqMap.get(docID))){
							occAsAnalyzedTerm = termFreqMap.get(docID).toString();
						}
						if (isValid(capTermFreqMap) && isValid(capTermFreqMap.get(docID))){
							occAsFullCapsTerm = capTermFreqMap.get(docID).toString();
						}
						if (isValid(firstLetterUpperMap) && isValid(firstLetterUpperMap.get(docID))){
							occAsfirstCharCapTerm = firstLetterUpperMap.get(docID).toString();
						}

						Integer totalOccurrences = Integer.parseInt(occAsAnalyzedTerm) + Integer.parseInt(occAsFullCapsTerm) + Integer.parseInt(occAsfirstCharCapTerm);
						if (termFreqMap == null && capTermFreqMap == null && firstLetterUpperMap == null){
							idFreqMap.put(normalizedQueryTerm, ZERO);
						} else {
							idFreqMap.put(normalizedQueryTerm, String.valueOf(totalOccurrences));
						}
					}
					break;
					case AUTHOR: {
						String authorName = splitString[1];
						ArrayList<String> postingsList = getPostings(indexDir, indexType, authorName.trim());
						if (isValid(postingsList)){
							if (postingsList.contains(docID)) {
								idFreqMap.put(authorName, ONE);
							} else {
								idFreqMap.put(authorName, ZERO);
							}
						} else {
							idFreqMap.put(authorName, ZERO);					
						}
					}
					break;
					case CATEGORY: {
						String categoryName = splitString[1];
						ArrayList<String> postingsList = getPostings(indexDir, indexType, categoryName);
						if (isValid(postingsList)){
							if (postingsList.contains(docID)) {
								idFreqMap.put(categoryName, ONE);
							} else {
								idFreqMap.put(categoryName, ZERO);
							}
						} else {
							idFreqMap.put(categoryName, ZERO);
						}
					}
					break;
					case PLACE: {
						String placeName = splitString[1].toLowerCase();
						ArrayList<String> postingsList = getPostings(indexDir, indexType, placeName);
						if (isValid(postingsList)){
							if (postingsList.contains(docID)) {
								idFreqMap.put(splitString[1], ONE);
							} else {
								idFreqMap.put(splitString[1], ZERO);
							}
						} else {
							idFreqMap.put(splitString[1], ZERO);
						}
					}
					break;
					}
				}
			}
			termOccMap.put(docID, idFreqMap);
		}
		return termOccMap;
	}

	public static ArrayList<String> getPostings(String indexDirName, IndexType indexType, String term){
		switch (indexType) {
		case  AUTHOR:{
			if (authorMapping.isEmpty()){
				initMaps(indexDirName, authorIndexFile, authorMapping);
			}
			ArrayList<String> queryList = authorMapping.get(term);

			String firstCapitalized = term.substring(0,1).toUpperCase() + term.substring(1);
			ArrayList<String> firstCaptitalList = authorMapping.get(firstCapitalized);
			if(isValid(firstCaptitalList)) {
				queryList = QueryHandler.performOR(queryList, firstCaptitalList);
			}

			String fullCap = term.toUpperCase();
			ArrayList<String> fullCapList = authorMapping.get(fullCap);
			if(isValid(fullCapList)) {
				queryList = QueryHandler.performOR(queryList, fullCapList);
			}

			String fulllowercase = term.toLowerCase();
			ArrayList<String> fullLowList = authorMapping.get(fulllowercase);
			if(isValid(fullLowList)) {
				queryList = QueryHandler.performOR(queryList, fullLowList);
			}

			return queryList;
		}

		case TERM: {
			if (termMapping.isEmpty()){
				initMaps(indexDirName, termIndexFile, termMapping);
			}
			ArrayList<String> queryList = termMapping.get(term);

			String firstCapitalized = term.substring(0,1).toUpperCase() + term.substring(1);
			ArrayList<String> firstCaptitalList = termMapping.get(firstCapitalized);
			if(isValid(firstCaptitalList)) {
				queryList = QueryHandler.performOR(queryList, firstCaptitalList);
			}

			String fullCap = term.toUpperCase();
			ArrayList<String> fullCapList = termMapping.get(fullCap);
			if(isValid(fullCapList)) {
				queryList = QueryHandler.performOR(queryList, fullCapList);
			}

			return queryList;
		}

		case PLACE: {
			if (placeMapping.isEmpty()){
				initMaps(indexDirName, placeIndexFile, placeMapping);
			}
			ArrayList<String> queryList = placeMapping.get(term);

			String firstCapitalized = term.substring(0,1).toUpperCase() + term.substring(1);
			ArrayList<String> firstCaptitalList = placeMapping.get(firstCapitalized);
			if(isValid(firstCaptitalList)) {
				queryList = QueryHandler.performOR(queryList, firstCaptitalList);
			}

			String fullCap = term.toUpperCase();
			ArrayList<String> fullCapList = placeMapping.get(fullCap);
			if(isValid(fullCapList)) {
				queryList = QueryHandler.performOR(queryList, fullCapList);
			}
			String fulllowercase = term.toLowerCase();
			ArrayList<String> fullLowList = placeMapping.get(fulllowercase);
			if(isValid(fullLowList)) {
				queryList = QueryHandler.performOR(queryList, fullLowList);
			}

			return queryList;
		}

		case CATEGORY: {
			if (categoryMapping.isEmpty()){
				initMaps(indexDirName,categoryIndexFile, categoryMapping);
			}
			ArrayList<String> postingsList = categoryMapping.get(term);
			return postingsList;
		}
		}
		return null;
	}
	
	public static void addTermToRawTermIndex(String term) {
		if(!rawTermList.contains(term)) {
			rawTermList.add(term);
		}
	}
	
	public static List<String> getRawTermIndex() {
		return rawTermList;
	}
	
	public static void initRawTerms(String indexDirName) {
		BufferedReader indexReader = null;
		try {
			if(rawTermList.isEmpty()) {
				indexReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + rawTermIndexFile)));
				String line = indexReader.readLine();
				if (line != null && !line.isEmpty()){
					String[] split = line.split(",");
					if(split != null) {
						for(String term : split) {
							if(!rawTermList.contains(term)) {
								rawTermList.add(term);
							}
						}
					}					
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
				indexReader.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
	
	public static Map<String, List<String>> getWildCardTerms(String indexDirName, String terms) {
		if(terms != null) {
			Map<String,List<String>> finalTerms = new HashMap<String, List<String>>();
			String[] split = terms.split(" ");
			if(split != null) {
				for(String term : split) {
					String indexType = null;
					String[] querySplit = term.split(":");
					if(querySplit.length == 2) {
						indexType = querySplit[0];
						term = term.split(":")[1];
					}
					
					List<String> queryTerms = new LinkedList<String>();
					if(isWildCardTerm(term)) {
						if(rawTermList.isEmpty()) {
							initRawTerms(indexDirName);
						}
						
						boolean isQuestionMarkQuery = false;
						if(term.contains("?")) {
							isQuestionMarkQuery = true;
						}
						
						if(!rawTermList.isEmpty()) {
							if((term.startsWith("*") && term.endsWith("*")) || (term.startsWith("?") && term.endsWith("?"))) {
								term = term.replaceAll("\\*|\\?", "");
								for(String rawTerm : rawTermList) {
									if(queryTerms != null && queryTerms.size() >= 5) {
										break;
									}
									
									if(rawTerm.contains(term) && !rawTerm.startsWith(term) && !rawTerm.endsWith(term)) {
										if(isQuestionMarkQuery && (rawTerm.length() != term.length() + 2)) {
											continue;
										}
										
										String addingTerm = rawTerm;
										if(isValid(indexType)) {
											addingTerm = indexType + ":" + rawTerm;
										}
										queryTerms.add(addingTerm);
									}
								}
							} else if(term.startsWith("*") || term.startsWith("?")) {
								term = term.replaceAll("\\*|\\?", "");
								for(String rawTerm : rawTermList) {
									if(queryTerms != null && queryTerms.size() >= 5) {
										break;
									}
									
									if(rawTerm.endsWith(term)) {
										if(isQuestionMarkQuery && (rawTerm.length() != term.length() + 1)) {
											continue;
										}
										String addingTerm = rawTerm;
										if(isValid(indexType)) {
											addingTerm = indexType + ":" + rawTerm;
										}
										queryTerms.add(addingTerm);
									}
								}
							} else {
								term = term.replaceAll("\\*|\\?", "");
								for(String rawTerm : rawTermList) {
									if(queryTerms != null && queryTerms.size() >= 5) {
										break;
									}
									
									
									if(rawTerm.startsWith(term)) {
										if(isQuestionMarkQuery && (rawTerm.length() != term.length() + 1)) {
											continue;
										}
										String addingTerm = rawTerm;
										if(isValid(indexType)) {
											addingTerm = indexType + ":" + rawTerm;
										}
										queryTerms.add(addingTerm);
									}
								}
							}
						}
						finalTerms.put(term, queryTerms);
					}
				}
				return finalTerms;
			}
		}
		return null;
	}
	
	public static boolean isWildCardTerm(String term) {
		if(Util.isValidString(term) && (term.contains("*") || term.contains("?"))) {
			return true;
		}
		return false;
	}
}