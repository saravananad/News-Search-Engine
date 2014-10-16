package edu.buffalo.cse.irf14.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
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
	
	public static DecimalFormat newFormat = new DecimalFormat("#.#####");
	public static String ZERO = "0";
	
	public static Map<String, String> documentIDMap = new HashMap<String, String>();
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
		if(Util.isValidString(docName)) {
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
	private static Map<Long, Long> docSizeMap = new HashMap<Long, Long>();

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
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + Util.docDictionaryFile)));
			String line = dictionaryReader.readLine();
			while ((line)!= null){
				String[] docIDPair = line.split(":");
				documentIDMap.put(docIDPair[1], docIDPair[0]); // New ID as Key, Old ID as value
				line = dictionaryReader.readLine();
			}
			dictionaryReader.close();

			/* Create a term occurrence dictionary */
			BufferedReader occurenceReader = new BufferedReader(new FileReader(new File (indexDirName + File.separator + Util.termOccurenceFile)));
			String occurenceLine = occurenceReader.readLine();
			while (occurenceLine != null){
				Map<String, Integer> innerMap = new HashMap<String, Integer>();
				String[] eachLine = occurenceLine.split(Util.dictionaryDelimiter);
				String tokenText = eachLine[0];
				String[] docIdOccurenceArray = eachLine[1].split(Util.occurenceDelimiter);
				for (String occurence : docIdOccurenceArray){
					String[] docIdOccurence = occurence.split(Util.invidualDoc_OccurDelimiter);
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
	
	private static void initMaps(String indexDirName, String fileName, Map<String, ArrayList<String>> mapType){
		try {
			if (documentIDMap.isEmpty() || termOccurrence.isEmpty()){
				Util.initDocOccurrencesMap(indexDirName);
			}
			if(docSizeMap.isEmpty()) {
				initDocumentSizeMap(indexDirName);
			}
			
			BufferedReader indexReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + fileName)));		
			String eachLine = indexReader.readLine();
			while (eachLine != null){
				String[] eachPostingPair = eachLine.split(":");
				ArrayList<String> postingsList = new ArrayList<String>();
				String[] postings = eachPostingPair[1].split(",");
				for (String docID : postings){
					postingsList.add(Util.documentIDMap.get(docID));
				}
				mapType.put(eachPostingPair[0], postingsList);
				eachLine = indexReader.readLine();
			}
			indexReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void initDocumentSizeMap(String indexDirName) {
		try {
			BufferedReader dictionaryReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + Util.docSizeFIle)));
			String line = null;
			while ((line = dictionaryReader.readLine())!= null){
				String[] docIDPair = line.split(dictionaryDelimiter);
				docSizeMap.put(Long.valueOf(docIDPair[1]), Long.valueOf(docIDPair[0])); // New ID as Key, Old ID as value
			}
			dictionaryReader.close();
		} catch(Exception ex) {
			System.err.println(ex);
		}
	}
	
	public static ArrayList<String> getPostings(String indexDirName, IndexType indexType, String term){
		switch (indexType) {
		case  AUTHOR:{
			if (authorMapping.isEmpty()){
				initMaps(indexDirName,Util.authorIndexFile, authorMapping);
			}
			ArrayList<String> postingsList = authorMapping.get(term);
			return postingsList;
		}

		case TERM: {
			if (termMapping.isEmpty()){
				initMaps(indexDirName,Util.termIndexFile, termMapping);
			}
			ArrayList<String> queryList = termMapping.get(term);
			
			String firstCapitalized = term.substring(0,1).toUpperCase() + term.substring(1);
			ArrayList<String> firstCaptitalList = termMapping.get(firstCapitalized);
			if(Util.isValid(firstCaptitalList)) {
				queryList = QueryHandler.performOR(queryList, firstCaptitalList);
			}
			
			String fullCap = term.toUpperCase();
			ArrayList<String> fullCapList = termMapping.get(fullCap);
			if(Util.isValid(fullCapList)) {
				queryList = QueryHandler.performOR(queryList, fullCapList);
			}
			
			return queryList;
		}

		case PLACE: {
			if (placeMapping.isEmpty()){
				initMaps(indexDirName,Util.placeIndexFile, placeMapping);
			}
			ArrayList<String> postingsList = placeMapping.get(term);
			return postingsList;
		}

		case CATEGORY: {
			if (categoryMapping.isEmpty()){
				initMaps(indexDirName,Util.categoryIndexFile, categoryMapping);
			}
			ArrayList<String> postingsList = categoryMapping.get(term);
			return postingsList;
		}
		}
		return null;
	}

}