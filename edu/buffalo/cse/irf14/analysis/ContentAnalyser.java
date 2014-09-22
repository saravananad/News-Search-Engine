package edu.buffalo.cse.irf14.analysis;

public class ContentAnalyser implements Analyzer {
	
	public static final String[] contentFilters = {TokenFilterType.SYMBOL.name()};
	
	private TokenStream tokenStream = null;
	
	public ContentAnalyser() {}

	public ContentAnalyser(TokenStream stream) {
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
		TokenFilterFactory tokenFilterFactory = TokenFilterFactory.getInstance();
		for(String filterName : contentFilters) {
			TokenFilter filter = tokenFilterFactory.getFilterByType(TokenFilterType.valueOf(filterName), this.tokenStream);
			while (filter.increment()) {}
			setStream(filter.getStream());
		}
		return false;
	}
}
