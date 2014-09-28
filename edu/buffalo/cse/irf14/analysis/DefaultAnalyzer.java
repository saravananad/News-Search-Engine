package edu.buffalo.cse.irf14.analysis;

public class DefaultAnalyzer implements Analyzer {

	private TokenStream tokenStream = null;
	
	public DefaultAnalyzer(){}
	
	public DefaultAnalyzer(TokenStream stream){
		this.tokenStream = stream;
	}
	public void setStream(TokenStream stream) {
		this.tokenStream = stream;
	}
	
	@Override
	public TokenStream getStream() {
		return this.tokenStream;
	}

	@Override
	public boolean increment() throws TokenizerException {
		return false;
	}
}
