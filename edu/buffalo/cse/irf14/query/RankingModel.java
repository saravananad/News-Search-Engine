package edu.buffalo.cse.irf14.query;

import java.util.Map;

public interface RankingModel {

	public Map<String, String> evaluatePostings();
}