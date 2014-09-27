/**
 * 
 */
package edu.buffalo.cse.irf14.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
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
	private static final String authorSplitRegex = "\\s+[a|A]nd\\s+";
	private static final String remove_By_From_Author_Pattern = "\\S*[by|BY|By|bY]\\s+";
	private static final Pattern titlePattern = Pattern.compile("\\s*[a-zA-Z0-9]*;+\\s*");
	private static final Pattern datePattern = Pattern.compile("\\s*(?:[Jj]an(?:uary)?|[Ff]eb(?:ruary)?|[mM]ar(?:ch)?|[aA]pr(?:il)?|[mM]ay?|[jJ]un(?:e)?|[jJ]ul(?:y)?|[aA]ug(?:ust)?|[sS]ep(?:tember)?|[Oo]ct(?:ober)?|([Nn]ov|[Dd]ec)(?:ember))+\\s*[0-9]{1,2}+\\s*");

	public static Document parse(String filename) throws ParserException {
		BufferedReader reader = null;
		try {
			if (filename == null || filename.isEmpty()){
				throw new ParserException("File name cannot be null or empty.");
			}
			File inputFile = new File(filename);
			if (!inputFile.isFile()){
				throw new ParserException("File name cannot contain special characters.");
			}
			reader = new BufferedReader(new FileReader(filename));
			Document doc = new Document();
			if(reader != null) {
				String line = null;
				String place = "";
				String content = "";
				String publishedDate = "";
				boolean isTitleLine = true, isSecondLine = true, isPlaceDateLine = true;
				String[] placeDateString = null;
				doc.setField(FieldNames.FILEID, inputFile.getName());
				doc.setField(FieldNames.CATEGORY, inputFile.getParentFile().getName());
				while((line = reader.readLine()) != null) {
					if(!line.trim().isEmpty()) {
						if(isTitleLine) {
							if (!(titlePattern.matcher(line).matches())){
								doc.setField(FieldNames.TITLE, line.trim());
								isTitleLine = false;	
							}
						} else if(isSecondLine && authorLinePattern.matcher(line).matches()) {
							line = line.replaceAll(remove_AUTHOR_Tag_Pattern, "").replaceFirst(remove_By_From_Author_Pattern, "");
							String authorParams[] = line.split(",");
							if(authorParams.length > 1) {
								doc.setField(FieldNames.AUTHORORG, authorParams[1].trim());
							}					
							String[] authors = authorParams[0].split(authorSplitRegex);
							doc.setField(FieldNames.AUTHOR, authors);
							isSecondLine = false;
						} else if (isPlaceDateLine){
							//TODO Parse and get place and date.
							if (line.contains(" - ")) {
								String[] contentFirstLine = line.split("-", 2);
								content += contentFirstLine[contentFirstLine.length - 1].trim();
								placeDateString = contentFirstLine[0].split(",");
								int length = placeDateString.length;
								if (datePattern.matcher(placeDateString[placeDateString.length - 1]).matches()){
									length = placeDateString.length - 1;
									publishedDate = placeDateString[placeDateString.length - 1];
								}
								if (length >= 1){
									place = placeDateString[0];
								}
								for (int i = 1; i < length; i++){
									place = place + ", " + placeDateString[i].trim();
								}
								doc.setField(FieldNames.NEWSDATE, publishedDate.trim());
							} else {
								Matcher dateMatch = datePattern.matcher(line);
								if (dateMatch.find()){
									doc.setField(FieldNames.NEWSDATE, dateMatch.group().trim());
									String[] textArray = line.split(dateMatch.group());
									content += textArray[textArray.length - 1].trim();	
								}		
								else {
									content += line.trim();
								}
							}	
							doc.setField(FieldNames.PLACE, place.trim());
							isPlaceDateLine = false;
						} else {
							content = content + " " + line.trim();
						}
					}
				}
				doc.setField(FieldNames.CONTENT, content);
				return doc;
			}
		} 
		catch(IOException ex) {
			System.err.println(ex);
		}
		finally {
			try {
				if(reader != null) reader.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}	
		return null;
	}

}
