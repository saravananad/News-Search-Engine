package edu.buffalo.cse.irf14.analysis;

public class Util {

	public enum FieldNameAnalyser {
		TITLE(TitleAnalyser.class.getName()),
		CONTENT(ContentAnalyser.class.getName());
		
		private String className = null;
		
		private FieldNameAnalyser(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
	}
	
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
}
