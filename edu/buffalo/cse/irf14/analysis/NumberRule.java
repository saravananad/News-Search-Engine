package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberRule extends TokenFilter {

	private static final Pattern specialFormatPattern = Pattern.compile("(%|\\/)");
	public NumberRule() {
		super(); 
	}

	public NumberRule(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())) {
			String tokenString = token.getTermText();
			if (!tokenString.matches("(\\d{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")){	
				if (tokenString.matches("(\\d+[\\/.\\,]*\\d+[%#]*)")){
					Matcher numberMatch = specialFormatPattern.matcher(tokenString);
					if (numberMatch.find()){
						tokenString = tokenString.replaceAll("(\\d+\\.*\\d*)", "");
						token.setTermText(tokenString);
					}
					else {
						tokenStream.remove();					
					}
				}
			}
		}
		return true;
	}
	
	@Override
	public TokenStream getStream() {
		return tokenStream;
	}

}
