package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberRule extends TokenFilter {

	private static final Pattern specialFormatPattern = Pattern.compile("(%|\\/)");
	private static final Pattern numberPattern1 = Pattern.compile("(\\d{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])");
	private static final Pattern numberPattern2 = Pattern.compile("(\\d*[\\/\\.\\,]*\\d+[\\.%#]*\\d*)");

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
			if (!numberPattern1.matcher(tokenString).matches() && numberPattern2.matcher(tokenString).matches()){
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
		return true;
	}
}
