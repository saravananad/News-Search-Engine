package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccentRule extends TokenFilter {
    static String[] accentPatterns = {"(Ã´|Ã¢)","(Ã©|Ã¨|Ã«)","((Ã ))","((Ã»)|Ã¼)","(À|Á|Â|Ã|Ä|Å)","Ç","(È|É|Ê|Ë)","(Ì|Í|Î|Ï)","Ð","Ñ","(Ò|Ó|Ô|Õ|Ö)","(Ù|Ú|Û|Ü)","(Ý|Ÿ)","(à|á|â|ã|ä|å)","ç","(è|é|ê|ë)","(ì,í,î,ï)","ð","ñ","(ò|ó|ô|õ|ö)","(ù|ú|û|ü)","(ý|ÿ)","Š","Ž","š","ž"};
    static String[] replacePatternsWith = {"o","a","e","u","A","C","E","I","D","N","O","U","Y","a","c","e","i","d","n","o","u","y","S","Z","s","z"}; 
	static String[] wordPatterns = {"[a-zA-Z0-9]+ACCENT[a-zA-Z0-9]+","\\s+ACCENT[a-zA-Z0-9]+","[a-zA-Z0-9]+ACCENT(\\s+|\\.|\\?|\\!)","[a-zA-Z0-9]+\\s+","[a-zA-Z0-9]+ACCENT","ACCENT[a-zA-Z0-9]+","ACCENT"};
	public static final String ACCENT_TEXT = "ACCENT";
	
	public AccentRule() {
		super(); 
	}

	public AccentRule(TokenStream stream) {
		super(stream);
	}
	
	public static String findAccentMatch(String regExp, String tokenText, String accent, String root) {
		String result = tokenText;
		Pattern accentOccurencePattern = Pattern.compile(regExp);
		Matcher accentOccurenceMatcher = accentOccurencePattern.matcher(tokenText);
		if (accentOccurenceMatcher.find())
			result = result.replaceAll(accent, root);
		return result;
	}
	
	public static String replaceDiacriticAccent(String tokenText) {
		for (int index = 0; index < accentPatterns.length; index++){
			for (int patternIndex = 0; patternIndex < wordPatterns.length; patternIndex++){
				tokenText = findAccentMatch(wordPatterns[patternIndex].replace(ACCENT_TEXT, accentPatterns[index]), tokenText, accentPatterns[index], replacePatternsWith[index]);
			}
		}
		return tokenText;
	}

	@Override
	public boolean increment() throws TokenizerException {
		while(tokenStream.hasNext()){
			String token = tokenStream.next().getTermText();
			token = replaceDiacriticAccent(token);
			tokenStream.getCurrent().setTermText(token);
		}	
		return false;
	}

}
