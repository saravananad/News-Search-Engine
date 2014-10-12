/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser {

	static String[] indexTypes = {"Term:", "Author:", "Category:", "Place:"};
	static String[] operators = {"and", "or", "not"};

	public static boolean isFacetedTerm(String token){
		for (String indexType : indexTypes){
			if (token.contains(indexType)){
				return true;
			}
		}
		return false;	
	}

	public static boolean isOperator(String token){
		return (Arrays.asList(operators).contains(token.toLowerCase()));
	}

	/**
	 * MEthod to parse the given user query into a Query object
	 * @param userQuery : The query to parse
	 * @param defaultOperator : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator) {
		Query query = new Query();
		String[] inputQuery = userQuery.split(" ");
		query.add("{");
		String indexType = "";
		boolean isMixedFacetedQuery = false;
		ArrayList<String> phraseQueryList = new ArrayList<String>();
		ArrayList<String> multiIndexQuery = new ArrayList<String>();
		ArrayList<String> tempList = new ArrayList<String>();

		for (int i = 0 ; i < inputQuery.length; i++){
			String currentToken = inputQuery[i];
			if (!currentToken.toLowerCase().equals("not")){
				String transformedToken = currentToken.replaceAll("\\(", "").replaceAll("\\)", "");
				int countOfLeftBraces = currentToken.length() - currentToken.replaceAll("\\(", "").length();
				int countOfRightBraces = currentToken.length() - currentToken.replaceAll("\\)", "").length();

				//Handle Phrase Queries of form : "Hello World!", "San Fransisco"
				if (currentToken.contains("\"") && !currentToken.endsWith("\"")){
					int j = i + 1;
					while (j < inputQuery.length){
						phraseQueryList.add(inputQuery[j]);
						if (inputQuery[j].endsWith("\""))
							break;
						j++;
					}
				}

				// Handle the Mixed Facet query of form Category:(movies AND films); Author:(Riggs OR Ryan)
				if (currentToken.contains("(") && !(currentToken.startsWith("("))){
					isMixedFacetedQuery = true;
					indexType = currentToken.substring(0, currentToken.indexOf("(")); //Extract the Index Type
				}

				// Handle the case sensitivity of operators in the query - AnD, noT, oR 
				if (isOperator(transformedToken)){
					transformedToken = transformedToken.toUpperCase();
				}
				// Modify the token if the token is not a Faceted term
				else if (!isFacetedTerm(transformedToken)){
					if (isMixedFacetedQuery){
						transformedToken = indexType + transformedToken;
					}
					else if (!phraseQueryList.contains(currentToken)){
						transformedToken = "Term:" + transformedToken;				
					}
				}

				// If the tokens of form Term:XYZ occur consecutively, then build an array of those
				// tokens to group them using square brackets later
				if (!(isFacetedTerm(currentToken) || isOperator(currentToken))){
					int k = i + 1;
					while (k < inputQuery.length){
						if (!isFacetedTerm(inputQuery[k]) && !isOperator(inputQuery[k])){
							multiIndexQuery.add(inputQuery[i]);	
							multiIndexQuery.add(inputQuery[k]);	
						}
						else
							break;
						k++;
					}
					if (!multiIndexQuery.isEmpty()){
						tempList = new ArrayList<String>(multiIndexQuery);
						if (currentToken.equals(multiIndexQuery.get(multiIndexQuery.size() - 1))){
							multiIndexQuery.clear();
						}
					}
				}

				// To end the Mixed Facet query
				if (currentToken.contains(")"))
					isMixedFacetedQuery = false;

				// Replace the NOT term with AND <term>
				if (!isOperator(transformedToken) && ((i - 1) > 0)){
					String previousToken = inputQuery[i - 1];
					if (previousToken.toLowerCase().equals("not")){
						transformedToken = "AND <" + transformedToken + ">";
					}				
				}

				// Group the terms and enclose them with square brackets
				if (currentToken.contains("(")){
					if (!currentToken.startsWith("(")){
						isMixedFacetedQuery = true;
					}
					StringBuilder extra = new StringBuilder();
					for (int index = 0; index < countOfLeftBraces; index++){
						extra.append("[");
					}
					transformedToken = extra.toString() + transformedToken;
				}
				if (currentToken.contains(")")){
					StringBuilder extra = new StringBuilder();
					for (int index = 0; index < countOfRightBraces; index++){
						extra.append("]");
					}
					transformedToken = transformedToken + extra.toString();
				}

				// Group the consecutively occurring tokens of form Term:XYZ using square brackets
				if (!tempList.isEmpty()){
					if ((tempList.get(0)).contains(currentToken)){
						transformedToken = "[" + transformedToken;
					}
					if ((tempList.get(tempList.size() - 1).contains(currentToken))){
						transformedToken = transformedToken + "]";
					}
				}
				query.add(transformedToken);

				// Insert "OR/AND" Operator if the current token and the next token are not any of the operators 
				if (!isOperator(transformedToken) && ((i + 1) < inputQuery.length)){
					String nextToken = inputQuery[i + 1];
					if (!(isOperator(nextToken) || (phraseQueryList.contains(nextToken)))){
						query.add(defaultOperator);
					}
				}		
			}
		}
		query.add("}");
		return query;
		//TODO returning NULL
	}
}