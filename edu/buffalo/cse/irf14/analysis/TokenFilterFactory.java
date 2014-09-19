/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.analysis.Util.filterList;


/**
 * Factory class for instantiating a given TokenFilter
 * @author nikhillo
 *
 */
public class TokenFilterFactory {
	
	public static TokenFilterFactory tokenFilterFactory = null;
	/**
	 * Static method to return an instance of the factory class.
	 * Usually factory classes are defined as singletons, i.e. 
	 * only one instance of the class exists at any instance.
	 * This is usually achieved by defining a private static instance
	 * that is initialized by the "private" constructor.
	 * On the method being called, you return the static instance.
	 * This allows you to reuse expensive objects that you may create
	 * during instantiation
	 * @return An instance of the factory
	 */
	public static TokenFilterFactory getInstance() {
		if(tokenFilterFactory == null) {
			tokenFilterFactory = new TokenFilterFactory();
		}
		return tokenFilterFactory;
	}
	
	/**
	 * Returns a fully constructed {@link TokenFilter} instance
	 * for a given {@link TokenFilterType} type
	 * @param type: The {@link TokenFilterType} for which the {@link TokenFilter}
	 * is requested
	 * @param stream: The TokenStream instance to be wrapped
	 * @return The built {@link TokenFilter} instance
	 */
	public TokenFilter getFilterByType(TokenFilterType type, TokenStream stream) {
		filterList filter = filterList.valueOf(type.name());
		try {
			TokenFilter tokenFilter = (TokenFilter) Class.forName(filter.getClassName()).newInstance();
			tokenFilter.setStream(stream);
			return tokenFilter;
		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}
}
