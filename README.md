#News Search Engine

###About
This project tries to index and query the RCV-1 corpus and mimics the functionality of a search engine. This project runs in 2 phases.
#####Phase 1
#####Index:
The following sequence of execution occurs in the index phase
1. File is parsed from the given location(This system is trained for RCV 1 corpus).
2. The text in the parsed files are tokenized in the tokenizer phase.
3. After the tokenizer phase is complete, the tokens are run through various filters like Stopwords Filter, Date Filter, Symbol Filter etc.
4. Finally, the token which are filtered are taken in the index phase and the data is indexed and written to the file.

#####Phase 2
#####Query:
The query phase functionality is to query the index and show the top K results with snippets. The execution flow is
1. The index which is written to file is loaded in memory.
2. The parse tree is created to parse the given query.
3. Then the parsed query is processed the same way through the filters as in the index phase.
4. Then the indexes are searched for the matching results and ranked using TF-IDF or Okapi ranking models.
5. The final result is shown along with the snippets from the file.

###Installation.
#####Phase 1
Run Runner.java with 2 program arguments.
The arguments are
1. File input Corpus directory(files to be indexed).
2. Index output directory.

#####Phase 2
Create SearchRunner Object and invoke the query method with the query and scoring model to rank.
