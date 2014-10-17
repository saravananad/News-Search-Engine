/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import edu.buffalo.cse.irf14.document.FieldNames;

/**
 * @author nikhillo
 * This factory class is responsible for instantiating "chained" {@link Analyzer} instances
 */
public class AnalyzerFactory {

	//Single Instance of Analyzer Factory.
	public static AnalyzerFactory analyserFactory = null;

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

	public static AnalyzerFactory getInstance() {
		if(analyserFactory == null) {
			analyserFactory = new AnalyzerFactory();
		}
		return analyserFactory;
	}

	/**
	 * Returns a fully constructed and chained {@link Analyzer} instance
	 * for a given {@link FieldNames} field
	 * Note again that the singleton factory instance allows you to reuse
	 * {@link TokenFilter} instances if need be
	 * @param name: The {@link FieldNames} for which the {@link Analyzer}
	 * is requested
	 * @param TokenStream : Stream for which the Analyzer is requested
	 * @return The built {@link Analyzer} instance for an indexable {@link FieldNames}
	 * null otherwise
	 */
	public Analyzer getAnalyzerForField(FieldNames fieldName, TokenStream stream) {
		switch (fieldName){
		case CONTENT: return getContentAnalyser(stream);
		case PLACE: return getPlaceAnalyser(stream);
		case AUTHOR: return getDefaultAnalyser(stream);
		case CATEGORY: return getDefaultAnalyser(stream);
		default: return null;
		}
	}

	public static Analyzer getContentAnalyser(TokenStream stream) {
		ContentAnalyser contentAnalyser = new ContentAnalyser();
		contentAnalyser.setStream(stream);
		return contentAnalyser;
	}

	public static Analyzer getDefaultAnalyser(TokenStream stream) {
		DefaultAnalyzer defaultAnalyser = new DefaultAnalyzer();
		defaultAnalyser.setStream(stream);
		return defaultAnalyser;
	}
	
	public static Analyzer getPlaceAnalyser(TokenStream stream) {
		PlaceAnalyser placeAnalyser = new PlaceAnalyser();
		placeAnalyser.setStream(stream);
		return placeAnalyser;
	}
}

