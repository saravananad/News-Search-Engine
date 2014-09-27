package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.Map;

public class Util {

	private static Map<String, Long> docIDMapping = new HashMap<String, Long>();
	private static long docID = 0;
	
	public static final String dictionaryDelimiter = ":";
	public static final String occurenceDelimiter = ";";
	public static final String invidualDoc_OccurDelimiter = "=";
	
	public static String authorIndexFile = "/AuthorIndex.txt";
	public static String termIndexFile = "/TermIndex.txt";
	public static String categoryIndexFile = "/CategoryIndex.txt";
	public static String placeIndexFile = "/PlaceIndex.txt";
	public static String docDictionaryFile = "/DocDictionary.txt";
	public static String termOccurenceFile = "/TermOccurence.txt";
	
	public enum FilterList {
		STOPWORD(StopWordFilter.class.getName()),
		CAPITALIZATION(CapitalizationRule.class.getName()),
		STEMMER(StemmingRule.class.getName()),
		ACCENT(AccentRule.class.getName()),
		NUMERIC(NumberRule.class.getName()),
		SYMBOL(SymbolFilter.class.getName()),
		SPECIALCHARS(SpecialCharsRule.class.getName());
		
		
		private String className = null;
		
		private FilterList(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
	}
	
	public static boolean isValidString(String value) {
		return value != null && !"".equals(value.trim()) && !"null".equalsIgnoreCase(value.trim());
	}
	
	public static boolean isValidArray(String[] stringArray){
		return stringArray != null;
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
}