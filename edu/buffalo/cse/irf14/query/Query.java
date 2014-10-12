package edu.buffalo.cse.irf14.query;

import java.util.ArrayList;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query {
	
	private ArrayList<String> searchQuery = new ArrayList<String>();
	
	/**
	 * Method that adds a query term to the searchQuery list
	 */
	public void add(String singleTerm){
		searchQuery.add(singleTerm);
	}
	/**
	 * Method to convert given parsed query into string
	 */
	public String toString() {
		StringBuilder transformedString = new StringBuilder();
		for (String eachQueryTerm : searchQuery){
			transformedString.append(eachQueryTerm);
			transformedString.append(" ");
		}
		return transformedString.toString();
	}
}