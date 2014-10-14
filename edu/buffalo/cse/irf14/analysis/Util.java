package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.Map;

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
	public static final String termOccurenceFile = "TermOccurence.txt";
	
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
	
	public static String getDefaultBooleanOperator() {
		return defaultOper;
	}
}