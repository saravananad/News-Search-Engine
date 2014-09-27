/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author NaveenKumar
 *
 */
public class StemmingRule extends TokenFilter {
	
	/**
	 * 
	 */
	public StemmingRule() {
		super();
	}

	/**
	 * @param stream
	 */
	public StemmingRule(TokenStream stream) {
		super(stream);
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.irf14.analysis.Analyzer#increment()
	 */
	@Override
	public boolean increment() throws TokenizerException {
		Stemmer stemInstance;
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())) {
			if (token.getTermText().matches("[A-Za-z]*")){
				stemInstance = new Stemmer();
				char[] characterArray = token.getTermBuffer();
				for (char singleLetter : characterArray){
					stemInstance.add(singleLetter);
				}
				stemInstance.stem();
				token.setTermText(stemInstance.toString());	
			}			
		}
		return true;
	}
}
