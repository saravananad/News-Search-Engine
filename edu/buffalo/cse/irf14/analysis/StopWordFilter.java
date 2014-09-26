package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.List;

public class StopWordFilter extends TokenFilter{
	
	public static final String[] stopWordsArray = {"a","able","about","across","after","all","almost","also","am","among","an","and",
											"any","are","as","at","be","because","been","but","by","can","cannot","could","dear",
											"did","do","does","either","else","ever","every","for","from","get","got","had","has",
											"have","he","her","hers","him","his","how","however","i","if","in","into","is","it",
											"its","just","least","let","like","likely","may","me","might","most","must","my",
											"neither","no","nor","not","of","off","often","on","only","or","other","our","own",
											"rather","said","say","says","she","should","since","so","some","than","that","the",
											"their","them","then","there","these","they","this","tis","to","too","twas","us","wants",
											"was","we","were","what","when","where","which","while","who","whom","why","will","with",
											"would","yet","you","your"};
	
	public static List<String> stopWordsList = new ArrayList<String>();
	
	public StopWordFilter() { 
		super(); 
	}
	
	public StopWordFilter(TokenStream stream) {
		super(stream);
	}

	public boolean isStopWord(String word){
		if(stopWordsList == null) {
			initStopWordsList();
		}
		return stopWordsList.contains(word);
	}
	
	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())) {
			if (isStopWord(token.getTermText().toLowerCase())){
				tokenStream.remove();
			}		
		}
		return true;
	}

	@Override
	public TokenStream getStream() {
		return tokenStream;
	}

	public static void initStopWordsList() {
		if(stopWordsList == null) {
			for(String stopWord : stopWordsArray) {
				stopWordsList.add(stopWord);
			}
		}
	}
}
