package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.query.OkapiModel;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryHandler;
import edu.buffalo.cse.irf14.query.QueryParser;
import edu.buffalo.cse.irf14.query.RankingModel;
import edu.buffalo.cse.irf14.query.TFIDFModel;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */
public class SearchRunner {
	public enum ScoringModel {TFIDF, OKAPI};

	public String indexDirectory = null;
	public String corpusDirectory = null;
	public char mode = ' ';
	PrintStream stream = null;

	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir, char mode, PrintStream stream) {
		this.indexDirectory = indexDir;
		this.corpusDirectory = corpusDir;
		this.mode = mode;
		this.stream = stream;
	}

	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) {
		Query query = QueryParser.parse(userQuery, Util.getDefaultBooleanOperator());
		QueryHandler handler = new QueryHandler(indexDirectory, query);
		RankingModel ranking;
		ArrayList<String> postingList = handler.handleQuery(query);
		
		if(ScoringModel.TFIDF == model) {
			ranking = new TFIDFModel(this.indexDirectory, handler.getAnalyzedTermList(), handler.getDocFrequencyMap(), postingList);
		} else {
			ranking = new OkapiModel(this.indexDirectory, handler.getAnalyzedTermList(), handler.getDocFrequencyMap(), postingList);
		}
		
		Map<String, String> results = ranking.evaluatePostings();
	}

	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(queryFile));
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			long noOfResults = 0;
			while((line = reader.readLine()) != null) {
				String[] split = line.split(":");
				if(split.length == 2) {
					String queryString = split[1].replaceAll("\\{|\\}", "").trim();
					Query query = QueryParser.parse(queryString, Util.getDefaultBooleanOperator());
					QueryHandler handler = new QueryHandler(indexDirectory, query);
					ArrayList<String> postingList = handler.handleQuery(query);
					RankingModel ranking = new OkapiModel(this.indexDirectory, handler.getAnalyzedTermList(), handler.getDocFrequencyMap(), postingList);
					Map<String, String> results = ranking.evaluatePostings();
					if(results != null) {
						noOfResults++;
						stringBuilder.append(split[0] + ":");
						stringBuilder.append("{ ");
						Iterator<Entry<String, String>> iterator = results.entrySet().iterator();
						while(iterator.hasNext()) {
							Entry<String, String> next = iterator.next();
							stringBuilder.append(next.getKey() + "#" + next.getValue() + ", ");
						}
						stringBuilder.setCharAt(stringBuilder.length() - 2, ' ');
						stringBuilder.setCharAt(stringBuilder.length() - 1, '}');
						stringBuilder.append("\n");
					}
				}
			}
			stream.append("numResults=" + noOfResults + "\n");
			stream.append(stringBuilder.toString());
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	/**
	 * General cleanup method
	 */
	public void close() {
		//TODO : IMPLEMENT THIS METHOD
	}

	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return false;
	}

	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() {
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		return null;

	}

	/**
	 * Method to indicate if speel correct queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() {
		//TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return false;
	}

	/**
	 * Method to get ordered "full query" substitutions for a given misspelt query
	 * @return : Ordered list of full corrections (null if none present) for the given query
	 */
	public List<String> getCorrections() {
		//TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		return null;
	}
}