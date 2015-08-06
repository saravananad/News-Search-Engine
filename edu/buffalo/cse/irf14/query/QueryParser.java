/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.buffalo.cse.irf14.analysis.Util;

/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {

	private static List<String> operators = new ArrayList<String>();

	private static final String TERM = "Term:";
	private static final String AUTHOR = "Author:";
	private static final String CATEGORY = "Category:";
	private static final String PLACE = "Place:";

	public static final String NOT = "NOT";
	private static final String[] operatorsArray = { Util.AND, Util.OR, NOT};
	private static final String[] indexTypes = {TERM, AUTHOR, CATEGORY, PLACE};
	
	public static boolean isFacetedTerm(String token){
		for (String indexType : indexTypes){
			if (token.contains(indexType)){
				return true;
			}
		}
		return false;
	}
	
	public static String getFacetOfTerm(String token) {
		for (String indexType : indexTypes){
			if (token.contains(indexType)){
				return indexType;
			}
		}
		return null;
	}
	
	public static void initOperators() {
		if(operators.isEmpty()) {
			for(String oper : operatorsArray) {
				operators.add(oper);
			}
		}
	}
	
	public static boolean isOperator(String token){
		if(operators.isEmpty()) {
			initOperators();
		}
		
		return operators.contains(token.toUpperCase());
	}

	/**
	 * MEthod to parse the given user query into a Query object
	 * @param userQuery : The query to parse
	 * @param defaultOperator : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator) {
		Query query = new Query();
		String[] input = userQuery.split(" ");
		ArrayList<String> inputList = new ArrayList<String>(Arrays.asList(input));
		
		query.add(Util.OPEN_BRACES);
		
		for(int i = 0; i < inputList.size(); i++) {
			String currentToken = inputList.get(i);
			boolean isNotQuery = false;
			int addOpenBrace = 0;
			int addCloseBrace = 0;
			
			if(currentToken.equalsIgnoreCase(NOT)) {
				currentToken = inputList.get(i + 1);
				inputList.remove(i + 1);
				isNotQuery = true;
			}
			
			if(currentToken.startsWith("(")) {
				// Starts with "("
				addOpenBrace += currentToken.length() - currentToken.replace("(", "").length();
				currentToken = currentToken.replace("(", "");
			}
			
			if(currentToken.contains("\"")) {
				int j = i + 1;
				while(j < inputList.size()) {
					currentToken += " " + inputList.get(j);
					String nextString = inputList.get(j);
					inputList.remove(j);
					if(nextString.contains("\"")) {
						break;
					}
				}
			}
			
			if (currentToken.endsWith(")")) {
				// Not handled in else if since the same token can have the close braces.
				addCloseBrace += currentToken.length() - currentToken.replace(")", "").length();
				currentToken = currentToken.replace(")", "");
			}
			
			if(isOperator(currentToken)) {
				currentToken = currentToken.toUpperCase();
			} else if(!isFacetedTerm(currentToken)) {
				currentToken = TERM + currentToken;
			}
			
			if(currentToken.contains("(")) {
				String[] tokenSplit = currentToken.split("\\(");
				if(isFacetedTerm(tokenSplit[0])) {
					currentToken = tokenSplit[0] + tokenSplit[1];
					addOpenBrace++;
					int next = i + 1;
					while(next < inputList.size() && !isFacetedTerm(inputList.get(next))) {
						if(!isOperator(inputList.get(next))) {
							String token = tokenSplit[0] + inputList.get(next);
							inputList.remove(next);
							inputList.add(next, token);
						}
						next++;
					}
				}
			}

			if(isNotQuery) {
				currentToken = "<" + currentToken + ">";
			}
			
			while(addOpenBrace > 0) {
				currentToken = Util.OPEN_SQUARE_BRACKETS + currentToken;
				addOpenBrace--;
			}
			
			while(addCloseBrace > 0) {
				currentToken += Util.CLOSE_SQUARE_BRACKETS;
				addCloseBrace--;
			}
			
			query.add(currentToken);
			if(!isOperator(currentToken) && i+1 < inputList.size()) {
				if(!isOperator(inputList.get(i + 1))) {
					query.add(defaultOperator);
				} else if(NOT.equalsIgnoreCase(inputList.get(i + 1))) {
					query.add(Util.AND);
				}
			}
		}
		query.add(Util.CLOSE_BRACES);
		
		List<String> searchQuery = query.getSearchQuery();
		int firstIndex = -1;
		String firstOper = null;
		String facet = null;
		int i = 0;
		int startingIndex = 0;
		while(i < searchQuery.size()) {
			if("{".equals(searchQuery.get(i))) {
				i++;
				startingIndex++;
			}
			
			if(isFacetedTerm(searchQuery.get(i))) {
				if(isOperator(searchQuery.get(i + 1))) {
					
					if(searchQuery.get(i).startsWith(Util.OPEN_SQUARE_BRACKETS)) {
						while(i < searchQuery.size()) {
							if(searchQuery.get(i).contains(Util.CLOSE_SQUARE_BRACKETS)) {
								break;
							}
							i += 2;
						}
						i += 2;
					} else if(firstIndex == -1 && firstOper == null) {
						firstIndex = i;
						firstOper = searchQuery.get(i + 1);
						facet = getFacetOfTerm(searchQuery.get(i));
						i += 2;
					} else {
						String currentFacet = getFacetOfTerm(searchQuery.get(i));
						String oper = searchQuery.get(i + 1);
						if(!currentFacet.equalsIgnoreCase(facet) || !firstOper.equalsIgnoreCase(oper)) {
							if(firstIndex + 2 < i) {
								String newTerm = Util.OPEN_SQUARE_BRACKETS + searchQuery.get(firstIndex);
								searchQuery.remove(firstIndex);
								searchQuery.add(firstIndex, newTerm);
								
								newTerm = searchQuery.get(i - 2) + Util.CLOSE_SQUARE_BRACKETS;
								searchQuery.remove(i - 2);
								searchQuery.add(i - 2, newTerm);
							}
							firstIndex = -1;
							firstOper = null;
							facet = null;
						} else {
							i += 2;
						}
					}					
				} else {
					String currFacet = getFacetOfTerm(searchQuery.get(i));
					if(facet != null && firstIndex != -1 &&  firstIndex != startingIndex) {
						if(facet.equalsIgnoreCase(currFacet) && firstIndex + 2 <= i) {
							String newTerm = Util.OPEN_SQUARE_BRACKETS + searchQuery.get(firstIndex);
							searchQuery.remove(firstIndex);
							searchQuery.add(firstIndex, newTerm);
							
							newTerm = searchQuery.get(i) + Util.CLOSE_SQUARE_BRACKETS;
							searchQuery.remove(i);
							searchQuery.add(i, newTerm);
						} else if(firstIndex + 2 < i) {
							String newTerm = Util.OPEN_SQUARE_BRACKETS + searchQuery.get(firstIndex);
							searchQuery.remove(firstIndex);
							searchQuery.add(firstIndex, newTerm);
							
							newTerm = searchQuery.get(i - 2) + Util.CLOSE_SQUARE_BRACKETS;
							searchQuery.remove(i - 2);
							searchQuery.add(i - 2, newTerm);
						}						
					}
					i += 2;
				}
			}
		}
		
		query.setQuery(searchQuery);
		return query;
	}
}