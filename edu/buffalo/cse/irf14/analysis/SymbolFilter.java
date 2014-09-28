package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Pattern;

public class SymbolFilter extends TokenFilter {

	public static final Pattern endOfLineRegex = Pattern.compile("[\\p{Punct}*[\\w|\\d]+[.-]*(\\w|\\d)*]+(\\s)*[\\.|?|!]+\\s*");
	public static final Pattern ipEndingWithSpecialChars = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}[\\.|?|!]+");
	public static final Pattern hyphenOnlyToken = Pattern.compile("(-)+");
	public static final Pattern formulaHyphenPattern = Pattern.compile("\\d*\\.*[A-Z0-9]+(-)+\\w+");

	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}

		Token token = tokenStream.getCurrent();
		if(token != null && Util.isValidString(token.getTermText())) {
			if(token.getTermText().indexOf("-") != -1) {
				if(hyphenOnlyToken.matcher(token.getTermText()).matches()) {
					tokenStream.remove();
					return true;
				}
				
				if(!formulaHyphenPattern.matcher(token.getTermText()).matches()) {
					token.setTermText(token.getTermText().replaceAll("-", " ").trim());
				}
			}

			if(endOfLineRegex.matcher(token.getTermText()).matches()) {
				if(ipEndingWithSpecialChars.matcher(token.getTermText()).matches()) {
					token.setTermText(token.getTermText().substring(0,token.getTermText().length() - 1));
				} else {
					token.setTermText(token.getTermText().replaceAll("[\\.|?|!]", ""));
				}
			}

			//Word replace
			if(token.getTermText().endsWith("'s") || token.getTermText().endsWith("s'")) {
				String replaced = token.getTermText().endsWith("'s") ? token.getTermText().replace("'s", "") : token.getTermText().replace("s'", "s");
				token.setTermText(replaced);
			} else if(token.getTermText().indexOf("n't") != -1) {
				String replaced = null;
				if("can't".equals(token.getTermText())) {
					replaced = token.getTermText().replace("'t", " not");
				} else if("won't".equals(token.getTermText())) {
					replaced = "will not";
				} else if("shan't".equals(token.getTermText())) {
					replaced = "shall not";
				} else {
					replaced = token.getTermText().replace("n't", " not");
				}
				token.setTermText(replaced);
			} else if(token.getTermText().endsWith("'ve")) {
				token.setTermText(token.getTermText().replace("'ve", " have"));
			} else if(token.getTermText().endsWith("'re")){
				token.setTermText(token.getTermText().replace("'re", " are"));
			} else if(token.getTermText().endsWith("'ll")) {
				token.setTermText(token.getTermText().replace("'ll", " will"));
			} else if(token.getTermText().endsWith("'d")) {
				token.setTermText(token.getTermText().replace("'d", " would"));
			} else if(token.getTermText().endsWith("'m")) {
				token.setTermText(token.getTermText().replace("'m", " am"));
			} else if(token.getTermText().endsWith("'em")) {
				token.setTermText("them");
			}

			//Quote replace
			if(token.getTermText().indexOf("'") != -1) {
				token.setTermText(token.getTermText().replace("'", ""));
			}
		}
		return true;
	}
}
