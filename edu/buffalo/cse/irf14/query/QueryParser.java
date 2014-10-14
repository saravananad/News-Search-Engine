/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {

	private static List<String> operators = new ArrayList<String>();

	private static final String AND = "AND";
	private static final String OR = "OR";
	private static final String NOT = "NOT";
	
	private static final String TERM = "Term:";
	private static final String AUTHOR = "Author:";
	private static final String CATEGORY = "Category:";
	private static final String PLACE = "Place:";

	private static final String[] operatorsArray = { AND, OR, NOT};
	private static final String[] indexTypes = {TERM, AUTHOR, CATEGORY, PLACE};
	
	public static boolean isFacetedTerm(String token){
		for (String indexType : indexTypes){
			if (token.contains(indexType)){
				return true;
			}
		}
		return false;	
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
		
		query.add("{");
		
		for(int i = 0; i < inputList.size(); i++) {
			String currentToken = inputList.get(i);
			boolean isNotQuery = false;
			boolean addOpenBrace = false;
			boolean addCloseBrace = false;
			
			// Handle Quotes
			if(currentToken.startsWith("\"")) {
				int j = i + 1;
				while(j < inputList.size()) {
					currentToken += " " + inputList.get(j);
					inputList.remove(j);
					if(currentToken.endsWith("\"")) {
						break;
					}
				}
			} else if(currentToken.startsWith("(")) {
				// Starts with "("
				currentToken = currentToken.replace("(", "");
				addOpenBrace = true;
			} else if(currentToken.equalsIgnoreCase(NOT)) {
				currentToken = inputList.get(i + 1);
				inputList.remove(i + 1);
				isNotQuery = true;
			}
			
			if(currentToken.contains("(")) {
				String[] tokenSplit = currentToken.split("\\(");
				if(isFacetedTerm(tokenSplit[0])) {
					currentToken = tokenSplit[0] + tokenSplit[1];
					int next = i + 1;
					boolean isFirstTeam = true;
					while(next < inputList.size()) {
						if(NOT.equalsIgnoreCase(inputList.get(next))) {
							currentToken += " " + AND;
						} else if(isOperator(inputList.get(next))) {
							currentToken += " " + inputList.get(next).toUpperCase();
						} else {
							String token = tokenSplit[0] + inputList.get(next);
							currentToken += " " + token;
						}
						
						if(isFirstTeam) {
							isFirstTeam = false;
							currentToken = "[" + currentToken;
						}
						
						inputList.remove(next);
						if(currentToken.endsWith(")")) {
							break;
						}
					}
				}
			}

			if (currentToken.endsWith(")")) {
				// Not handled in else if since the same token can have the close braces.
				currentToken = currentToken.replace(")", "");
				addCloseBrace = true;
			}
			
			if(isOperator(currentToken)) {
				currentToken = currentToken.toUpperCase();
			} else if(!isFacetedTerm(currentToken)) {
				currentToken = TERM + currentToken;
			}
			
			if(isNotQuery) {
				currentToken = "<" + currentToken + ">";
			}
			
			if(addOpenBrace) {
				currentToken = "[" + currentToken;
			}
			
			if(addCloseBrace) {
				currentToken += "]";
			}
			
			query.add(currentToken);
			if(!isOperator(currentToken) && i+1 < inputList.size()) {
				if(!isOperator(inputList.get(i + 1))) {
					query.add(defaultOperator);
				} else if(NOT.equalsIgnoreCase(inputList.get(i + 1))) {
					query.add(AND);
				}
			}
		}
		query.add("}");
		return query;
	}
}