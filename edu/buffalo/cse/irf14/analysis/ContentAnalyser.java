package edu.buffalo.cse.irf14.analysis;

public class ContentAnalyser implements Analyzer {
	
	public static final String[] contentFilters = {TokenFilterType.STOPWORD.name(), TokenFilterType.ACCENT.name(), TokenFilterType.SYMBOL.name(), TokenFilterType.STEMMER.name(), TokenFilterType.SPECIALCHARS.name(), TokenFilterType.NUMERIC.name(), TokenFilterType.CAPITALIZATION.name()};
	
	private TokenStream tokenStream = null;
	
	public ContentAnalyser() {}

	public ContentAnalyser(TokenStream stream) {
		this.tokenStream = stream;
	}
	
	public void setStream(TokenStream stream) {
		stream.reset();
		this.tokenStream = stream;
	}
	
	@Override
	public TokenStream getStream() {
		this.tokenStream.reset();
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
