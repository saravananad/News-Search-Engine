package edu.buffalo.cse.irf14.analysis;

public class TitleAnalyser implements Analyzer {
	
	public static final String[] titleFilters = {TokenFilterType.STOPWORD.name()};
	
	private TokenStream tokenStream = null;
	
	public TitleAnalyser() {}

	public TitleAnalyser(TokenStream stream) {
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
		for(String filterName : titleFilters) {
			TokenFilter filter = tokenFilterFactory.getFilterByType(TokenFilterType.valueOf(filterName), this.tokenStream);
			while (filter.increment()) {}
			setStream(filter.getStream());
		}
		return false;
	}
}
