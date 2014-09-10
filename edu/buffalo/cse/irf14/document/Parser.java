/**
 * 
 */
package edu.buffalo.cse.irf14.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author nikhillo
 * Class that parses a given file into a Document
 */
public class Parser {
	/**
	 * Static method to parse the given file into the Document object
	 * @param filename : The fully qualified filename to be parsed
	 * @return The parsed and fully loaded Document object
	 * @throws ParserException In case any error occurs during parsing
	 */
	
	private static final Pattern authorLinePattern = Pattern.compile("<AUTHOR>(.*)</AUTHOR>");
	private static final String remove_AUTHOR_Tag_Pattern = "\\s*</*AUTHOR>\\s*";
	private static final String remove_By_From_Author_Pattern = "\\S*[by|BY|By|bY]\\s+";
	private static final Pattern titlePattern = Pattern.compile("\\s*[a-zA-Z0-9]?[,-\\.'<>]*");
	
	public static Document parse(String filename) throws ParserException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			Document doc = new Document();
			if(reader != null) {
				String line = null;
				boolean isTitleLine = true, isSecondLine = false;
				File inputFile = new File(filename);
				doc.setField(FieldNames.FILEID, inputFile.getParentFile().getName() + "_" + inputFile.getName());
				doc.setField(FieldNames.CATEGORY, inputFile.getParentFile().getName());
				while((line = reader.readLine()) != null) {
					if(!line.isEmpty()) {
						if(isTitleLine) {
							if(!titlePattern.matcher(line.trim()).matches()) {
								//Continue
								System.out.println(line);
							}
							doc.setField(FieldNames.TITLE, line.trim());
							isTitleLine = false;
							isSecondLine = true;
						} else if(isSecondLine && authorLinePattern.matcher(line).matches()) {
							line = line.replaceAll(remove_AUTHOR_Tag_Pattern, "").replaceFirst(remove_By_From_Author_Pattern, "");
							String authorParams[] = line.split(",");
							if(authorParams.length > 1) {
								doc.setField(FieldNames.AUTHORORG, authorParams[1]);
							}
							doc.setField(FieldNames.AUTHOR, authorParams[0]);
						} else {
							//TODO Parse and get place and date.
							isSecondLine = false;
						}
					}
				}
				return doc;
			}
		} catch(IOException ex) {
			System.err.println(ex);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
		return null;
	}

}
