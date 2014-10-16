package edu.buffalo.cse.irf14.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.analysis.Util;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.IndexType;

public class QueryHandler {

	private Stack<String> operStack = new Stack<String>();
	private Stack<Object> operandStack = new Stack<Object>();
	Tokenizer tokenizer = new Tokenizer();

	private static Map<String, ArrayList<String>> termMapping = new TreeMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> authorMapping= new TreeMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> categoryMapping = new HashMap<String, ArrayList<String>>();
	private static Map<String, ArrayList<String>> placeMapping = new TreeMap<String, ArrayList<String>>();
	public static ArrayList<String> analyzedTermList = new ArrayList<String>();
	public static Map<String, String> docFrequenciesMap = new TreeMap<String, String>();


	String indexDir = null;

	public QueryHandler(String indexDir, Query query) {
		this.indexDir = indexDir;
	}

	public List<Object> handleQuery(Query query) {
		constructStack(query);
		System.out.println( "operStack :: " + operStack.toString());
		return Arrays.asList(operandStack.peek());
	}

	private void constructStack(Query query) {
		List<String> searchQuery = query.getSearchQuery();
		int i = 0;
		while(i < searchQuery.size() && !searchQuery.get(i).equals(Util.CLOSE_BRACES)) {
			String term = searchQuery.get(i);
			if(Util.isOperator(term)) {
				operStack.push(term);
				i++;
			} else if(term.contains(Util.CLOSE_SQUARE_BRACKETS)) {
				term = term.replaceAll("]", "");
				addPostingsAfterAnalysis(term);
				i++;
				while(true) {
					String oper = operStack.pop();
					ArrayList<String> postings1 = null;
					Object pop = operandStack.pop();
					boolean isFirstOperNot = false;
					if(Util.NOT_CLOSE.equals(pop)) {
						oper = Util.NOT_CLOSE;
						pop = operandStack.pop(); // Element
						operandStack.pop(); // "<"
						isFirstOperNot = true;
					}

					if(Util.isValid(pop)) {
						postings1 = (ArrayList<String>) pop;
					}
					pop = operandStack.pop();
					if(Util.NOT_CLOSE.equals(pop)) {
						oper = Util.NOT_CLOSE;
						pop = operandStack.pop(); // Element
						operandStack.pop(); // "<"
					}

					ArrayList<String> postings2 = null;
					if(Util.isValid(pop)) {
						postings2 = (ArrayList<String>) pop;
					}

					List<String> result = doOperation(oper, postings1, postings2, isFirstOperNot);
					boolean isBreak = false;
					if(Util.OPEN_SQUARE_BRACKETS.equals(operandStack.peek())) {
						operandStack.pop();
						isBreak = true;
					}

					if(result == null) {
						operandStack.push("null");
					} else {
						operandStack.push(result);
					}
					if(isBreak) {
						break;
					}
				}
			} else {
				String oper = null;
				if(term.contains(Util.NOT_OPEN) || term.contains(Util.OPEN_SQUARE_BRACKETS)) {
					oper = term.contains(Util.NOT_OPEN) ? Util.NOT_OPEN : Util.OPEN_SQUARE_BRACKETS;
					operandStack.push(oper);
					searchQuery.remove(i);
					term = term.replaceAll("\\[|<|>", "");
					searchQuery.add(i, term);
				} 
				addPostingsAfterAnalysis(term);
				if(Util.NOT_OPEN.equals(oper)) {
					operandStack.push(Util.NOT_CLOSE);
				}

				i++;	
			}				
		}

		while(true) {
			if(!Util.OPEN_BRACES.equals(operStack.peek())) {
				String oper = operStack.pop();
				ArrayList<String> postings1 = null;
				Object pop = operandStack.pop();
				boolean isFirstOperNot = false;
				if(Util.NOT_CLOSE.equals(pop)) {
					oper = Util.NOT_CLOSE;
					pop = operandStack.pop(); // Element
					operandStack.pop(); // "<"
					isFirstOperNot = true;
				}

				if(Util.isValid(pop)) {
					postings1 = (ArrayList<String>) pop;
				}
				pop = operandStack.pop();
				if(Util.NOT_CLOSE.equals(pop)) {
					oper = Util.NOT_CLOSE;
					pop = operandStack.pop(); // Element
					operandStack.pop(); // "<"
				}

				ArrayList<String> postings2 = null;
				if(Util.isValid(pop)) {
					postings2 = (ArrayList<String>) pop;
				}

				List<String> result = doOperation(oper, postings1, postings2, isFirstOperNot);
				boolean isBreak = false;
				if(Util.OPEN_BRACES.equals(operStack.peek())) {
					operStack.pop();
					isBreak = true;
				}

				if(result == null) {
					operandStack.push("null");
				} else {
					operandStack.push(result);
				}
				if(isBreak) {
					break;
				}
			} else {
				operStack.pop();
				break;
			}
		}
	}

	public void addPostingsAfterAnalysis(String term) {
		String[] stringSplit = term.split(":");
		String indexType = stringSplit[0].toUpperCase();
		FieldNames fieldName = null;
		if(indexType.equals(IndexType.TERM.name())) {
			fieldName = FieldNames.CONTENT;
		} else {
			fieldName = FieldNames.valueOf(indexType);
		}
		AnalyzerFactory analyzerFactory = AnalyzerFactory.getInstance();
		try {
			TokenStream token = tokenizer.consume(stringSplit[1]);
			Analyzer analyzer = analyzerFactory.getAnalyzerForField(fieldName, token);
			while(analyzer.increment()) {}
			TokenStream stream = analyzer.getStream();
			term = "";
			while(stream.hasNext()) {
				term += stream.next();
			}
			ArrayList<String> postings = getPostings(this.indexDir , stringSplit[0], term);
			if (term.isEmpty()){
				analyzedTermList.add(null);
				docFrequenciesMap.put("null", Util.ZERO);
			}
			else {
				analyzedTermList.add(term);
				docFrequenciesMap.put(term, String.valueOf(postings.size()));
			}
			operandStack.push(postings);
		} catch (TokenizerException e) {
			System.err.println(e);
		}
	}

	public ArrayList<String> doOperation(String oper, ArrayList<String> postings1, ArrayList<String> postings2, boolean isFirstOperatorNot) {
		ArrayList<String> result = new ArrayList<String>();
		if(Util.AND.equals(oper)) {
			result = performAND(postings1, postings2);
		} else if(Util.OR.equals(oper)) {
			result = performOR(postings1, postings2);
		} else {
			result = performNOT(postings1, postings2, isFirstOperatorNot);
		}
		return result;
	}

	/************************* Searcher - getPostings	*********************************/
	
	private static void initMaps(String indexDirName, String fileName, Map<String, ArrayList<String>> mapType){
		try {
			if (Util.documentIDMap.isEmpty() || Util.termOccurrence.isEmpty()){
				Util.initDocOccurrencesMap(indexDirName);
			}
			BufferedReader indexReader = new BufferedReader(new FileReader(new File(indexDirName + File.separator + fileName)));		
			String eachLine = indexReader.readLine();
			while (eachLine != null){
				String[] eachPostingPair = eachLine.split(":");
				ArrayList<String> postingsList = new ArrayList<String>();
				String[] postings = eachPostingPair[1].split(",");
				for (String docID : postings){
					postingsList.add(Util.documentIDMap.get(docID));
				}
				mapType.put(eachPostingPair[0], postingsList);
				eachLine = indexReader.readLine();
			}
			indexReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	private static ArrayList<String> getPostings(String indexDirName, String indexTypeString, String term) {
		IndexType indexType = IndexType.valueOf(indexTypeString.toUpperCase());
		return getPostings(indexDirName,indexType, term);
	}

	private static ArrayList<String> getPostings(String indexDirName, IndexType indexType, String term){
		switch (indexType) {
		case  AUTHOR:{
			if (authorMapping.isEmpty()){
				initMaps(indexDirName,Util.authorIndexFile, authorMapping);
			}
			ArrayList<String> postingsList = authorMapping.get(term);
			return postingsList;
		}

		case TERM: {
			if (termMapping.isEmpty()){
				initMaps(indexDirName,Util.termIndexFile, termMapping);
			}
			ArrayList<String> postingsList = termMapping.get(term);
			return postingsList;
		}

		case PLACE: {
			if (placeMapping.isEmpty()){
				initMaps(indexDirName,Util.placeIndexFile, placeMapping);
			}
			ArrayList<String> postingsList = placeMapping.get(term);
			return postingsList;
		}

		case CATEGORY: {
			if (categoryMapping.isEmpty()){
				initMaps(indexDirName,Util.categoryIndexFile, categoryMapping);
			}
			ArrayList<String> postingsList = categoryMapping.get(term);
			return postingsList;
		}
		}
		return null;
	}

	/*************************************** And, OR, Not methods ******************************/

	private static ArrayList<String> performOR(ArrayList<String> postings1, ArrayList<String> postings2){
		if(postings1 != null && postings2 != null) {
			ArrayList<String> resultPosting = new ArrayList<String>();
			int i = 0, j = 0;
			while(i < postings1.size() && j < postings2.size())
			{
				if(Integer.parseInt(postings1.get(i)) < Integer.parseInt(postings2.get(j)))
					resultPosting.add(postings1.get(i++));
				else if(Integer.parseInt(postings2.get(j)) < Integer.parseInt(postings1.get(i)))
					resultPosting.add(postings2.get(j++));
				else
				{
					resultPosting.add(postings2.get(j++));
					i++;
				}
			}
			while(i < postings1.size())
				resultPosting.add(postings1.get(i++));
			while(j < postings2.size())
				resultPosting.add(postings2.get(j++));
			return resultPosting;
		}
		if(postings1 == null && postings2 != null) {
			return postings2;
		} else if(postings2 == null && postings1 != null) {
			return postings1;
		}
		return null;
	}

	private static ArrayList<String> performAND(ArrayList<String> postings1, ArrayList<String> postings2){
		if(postings1 != null && postings2 != null) {
			ArrayList<String> resultPosting = new ArrayList<String>();
			int i = 0, j = 0;
			while(i < postings1.size() && j < postings2.size())
			{
				if(Integer.parseInt(postings1.get(i)) < Integer.parseInt(postings2.get(j)))
					i++;
				else if(Integer.parseInt(postings2.get(j)) < Integer.parseInt(postings1.get(i)))
					j++;
				else
				{
					resultPosting.add(postings2.get(j++));
					i++;
				}
			}
			return resultPosting;
		}
		return null;
	}

	private static ArrayList<String> performNOT(ArrayList<String> postings1, ArrayList<String> postings2, boolean isFirstPostingNot){
		if(isFirstPostingNot) {
			if(!Util.isValid(postings1)) {
				return postings2;
			}
		} else {
			if(!Util.isValid(postings2)) {
				return postings1;
			}
		}
		ArrayList<String> intersection = performAND(postings1, postings2);
		if(isFirstPostingNot) {
			postings2.removeAll(intersection);
			return postings2;
		} else {
			postings1.removeAll(intersection);
			return postings1;
		}
	}
}
