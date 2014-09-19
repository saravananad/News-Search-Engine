package edu.buffalo.cse.irf14.analysis;

public class Util {

	public enum fieldNameAnalyser {
		TITLE(TitleAnalyser.class.getName());
		
		private String className = null;
		
		private fieldNameAnalyser(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
	}
	
	public enum filterList {
		STOPWORD(StopWordFilter.class.getName());
		
		private String className = null;
		
		private filterList(String className) {
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
