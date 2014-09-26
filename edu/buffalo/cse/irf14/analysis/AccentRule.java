package edu.buffalo.cse.irf14.analysis;

import java.text.Normalizer;

public class AccentRule extends TokenFilter {
	
	public AccentRule() {
		super(); 
	}

	public AccentRule(TokenStream stream) {
		super(stream);
	}
	
	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())) {
			String tokenText = token.getTermText();
			tokenText = Normalizer.normalize(tokenText, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			token.setTermText(tokenText);
		}	
		return true;
	}
	
	@Override
	public TokenStream getStream() {
		return tokenStream;
	}

}
