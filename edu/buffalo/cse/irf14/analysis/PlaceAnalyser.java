/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author NaveenKumar
 *
 */
public class PlaceAnalyser implements Analyzer {
	public static final String[] placeFilters = {TokenFilterType.SPECIALCHARS.name()};
	
	private TokenStream tokenStream = null;

	public PlaceAnalyser() {}
	
	public PlaceAnalyser(TokenStream stream){
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
		for(String filterName : placeFilters) {
			TokenFilter filter = tokenFilterFactory.getFilterByType(TokenFilterType.valueOf(filterName), this.tokenStream);
			while (filter.increment()) {}
			setStream(filter.getStream());
		}
		return false;
	}
}
