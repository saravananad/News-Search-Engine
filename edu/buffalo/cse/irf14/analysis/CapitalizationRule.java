package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapitalizationRule extends TokenFilter {

	private static final Pattern camelAcronymPattern = Pattern.compile("(([A-Z]+[a-z]+|[a-z]+[A-Z]+)[,.!']*[a-z]*|[A-Z]+[\\.,!]*)");
	private static final Pattern firstWordPattern = Pattern.compile("(^|([.!?]\\s))(\\w+)");
	private static final Pattern properNounsPattern = Pattern.compile("[A-Z][a-z]+");
	
	public CapitalizationRule() { super(); }

	public CapitalizationRule(TokenStream stream) {
		super(stream);
	}

	public  ArrayList<String> buildNoChangeTokens (String lines){
		ArrayList<String> retainedList = new ArrayList<String>();	
		Matcher camelAcronymMatcher = camelAcronymPattern.matcher(lines);
		Matcher firstWordMatcher = firstWordPattern.matcher(lines);
		while (camelAcronymMatcher.find()){
			retainedList.add(camelAcronymMatcher.group().trim());
		}
		
		/* Remove all the first words from the constructed list of words that 
		 * should be retained */
		while (firstWordMatcher.find()){
			String firstWord = firstWordMatcher.group().replaceAll("[.!?]", "").trim();
			if (retainedList.contains(firstWord)){
				retainedList.remove(firstWord);
			}
		}	
		return retainedList;
	}
	
	private boolean checkForMerge(String element, String successorWord) {
		Matcher properNounMatchOne = properNounsPattern.matcher(element);
		Matcher properNounMatchTwo = properNounsPattern.matcher(successorWord);	
		return (properNounMatchOne.find() && properNounMatchTwo.find());
	}
	
	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		ArrayList<String> noChangesList = buildNoChangeTokens (tokenStream.toString().replaceAll("[\\[\\]]", "").replace(",", ""));
		Token token = tokenStream.getCurrent();
		if (token != null && Util.isValidString(token.getTermText())){
			String element = token.getTermText();
			if (!noChangesList.contains(element)){
				token.setTermText(element.toLowerCase());
			}
			element = token.getTermText();
			if (tokenStream.hasPrevious()){
				String precedingWord = tokenStream.getPrevious().getTermText();
				boolean mergeRequired = checkForMerge(precedingWord, element);
				if (mergeRequired){
					tokenStream.getPrevious().merge(token);
					tokenStream.remove();
				}			
			}
		}
		return true;
	}

	@Override
	public TokenStream getStream() {
		return tokenStream;
	}


}
