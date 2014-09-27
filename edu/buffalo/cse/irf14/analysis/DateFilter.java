package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.analysis.Util.Month;

public class DateFilter extends TokenFilter {

	// Date Patterns
	public static final Pattern monthPattern = Pattern.compile("(J|j)(anuary|an)|(F|f)(ebruary|eb)|(M|m)(arch|ar|ay)"
			+ "|(A|a)(pril|pr|ugust|ug)|(J|j)(une|un|uly|ul)"
			+ "|(S|s)(eptember|ep)|(O|o)(ctober|ct)|(N|n)(ovember|ov)"
			+ "|(D|d)(ecember|ec)");
	public static final Pattern isDateOfMonth = Pattern.compile("(0{0,1}[1-9]|[12][0-9]|3[01]),*");
	public static final Pattern isYear = Pattern.compile("(19|20)[0-9]{2},*");
	public static final Pattern isADBCYear = Pattern.compile("[0-9]{0,4}");
	public static final Pattern isDateInADBC = Pattern.compile("\\d{1,4}(AD|BC).*");
	public static final Pattern isADBC = Pattern.compile("AD|BC");
	public static final Pattern isYearToYear = Pattern.compile("\\d{4}-\\d{2,4}.*");

	// Time patterns
	public static final Pattern isTime = Pattern.compile("(2[0-4]{1}|[0-1]*{2}[0-9]{1}):([0-5]*{1}[0-9]*{1})\\s*(A|a|P|p)*{1}(M|m)*{1}.*{1}");
	public static final Pattern isAMorPM = Pattern.compile("(A|a|p|P)(m|M)\\s*.*");
	public static final Pattern isPM = Pattern.compile("(p|P)(m|M)\\s*.*");

	public DateFilter() { 
		super(); 
	}

	public DateFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
		if(!(tokenStream.next() instanceof Token) && !tokenStream.hasNext()) {
			return false;
		}
		Token token = tokenStream.getCurrent();
		if(token == null || (token != null && "".equals(token.toString().trim()))) {
			tokenStream.remove();
			return true;
		}

		if(token != null && Util.isValidString(token.getTermText())) {
			Token previous = tokenStream.getPrevious();
			Token next = tokenStream.getNextWithOutMovingPointer();
			// TODO Not required.. Need to test
//			while((next == null && tokenStream.getCurrentPosition() + 1 < tokenStream.size()) || (next != null && !Util.isValidString(next.toString()))) {
//				tokenStream.remove(tokenStream.getCurrentPosition() + 1);
//				next = tokenStream.getNextWithOutMovingPointer();
//			}

			if(monthPattern.matcher(token.toString()).matches()) {
				if(tokenStream.hasPrevious() && isDateOfMonth.matcher(previous.toString()).matches()) {
					String year = (tokenStream.hasNext() && isYear.matcher(next.toString()).matches()) ? next.toString() : "1900";
					Month month = Month.valueOf(token.toString().toLowerCase());
					String dateOfMonth = previous.toString().length() == 2 ? previous.toString() : "0" + previous.toString();
					String date = year + month.getValue() + dateOfMonth;
					tokenStream.remove(tokenStream.getCurrentPosition() - 1);
					tokenStream.remove(tokenStream.getCurrentPosition() + 1);
					token.setTermText(date);	
				} else if(tokenStream.hasNext() && isDateOfMonth.matcher(next.toString().trim()).matches()) {
					Token nextOfNext = tokenStream.getTokenAtPosition(tokenStream.getCurrentPosition() + 2);

					if(next.toString().contains(",")) {
						next.setTermText(next.toString().replace(",", "").trim());
					}
					
					String year = "1900";
					boolean addEndindComma = false;
					if(isYear.matcher(nextOfNext.toString()).matches()) {
						if(nextOfNext.toString().contains(",")) {
							addEndindComma = true;
							nextOfNext.setTermText(nextOfNext.toString().replace(",", "").trim());
						}
						year = nextOfNext.toString();
						tokenStream.remove(tokenStream.getCurrentPosition() + 2);
					}
					
					Month month = Month.valueOf(token.toString().toLowerCase());
					String dateOfMonth = next.toString().length() == 2 ? next.toString() : "0" + next.toString();
					dateOfMonth = dateOfMonth.replace(",", "");
					String date = year + month.getValue() + dateOfMonth;
					if(addEndindComma) {
						date += ",";
					}
					tokenStream.remove(tokenStream.getCurrentPosition() + 1);
					token.setTermText(date);	
				}
			} else if(isDateInADBC.matcher(token.toString()).matches() || isADBC.matcher(token.toString()).matches()) {
				if(isDateInADBC.matcher(token.toString()).matches()) {
					String date = token.toString();
					boolean isBC = token.toString().contains("BC");
					date = date.replace("AD", "").replace("BC", "");
					boolean isEndingWithDot = date.contains(".");
					date = date.replace(".", "");
					while(date.length() < 4) {
						date = "0" + date;
					}
					if(isBC) {
						date = "-" + date;
					}
					date += "0101";
					if(isEndingWithDot) {
						date += ".";
					}
					token.setTermText(date);
				} else if(isADBC.matcher(token.toString()).matches() && isADBCYear.matcher(previous.toString()).matches()) {
					String date = previous.toString();
					while(date.length() < 4) {
						date = "0" + date;
					}
					if("BC".equals(token.toString().trim())) {
						date = "-" + date;
					}
					date += "0101";
					tokenStream.remove(tokenStream.getCurrentPosition() - 1);
					token.setTermText(date);
				}
			} else if(isYearToYear.matcher(token.toString()).matches()) {
				boolean addEndDot = false;
				String yearsString = token.toString();
				if(yearsString.contains(".")) {
					yearsString = yearsString.replace(".", "");
					addEndDot = true;
				}
				String[] years = yearsString.split("-");
				String finalYearString = "";
				for(String year : years) {
					if(year.length() == 2) {
						year = years[0].substring(0, 2) + year;
					}
					year += "0101";
					finalYearString += year + "-";
				}
				finalYearString = finalYearString.substring(0, finalYearString.length() - 1);
				if(addEndDot) {
					finalYearString += ".";
				}
				token.setTermText(finalYearString);
			} else if(isYear.matcher(token.toString()).matches()) {
				// All other instances would have satisfied the above conditions itself.
				// Hence the year with no date or month comes in this case.
				token.setTermText(token.toString().trim() + "0101");
			}

			//Time
			if(isTime.matcher(token.toString()).matches() && token.toString().split(":").length == 2) {
				if(token.toString().contains("PM") || token.toString().contains("pm") || (tokenStream.hasNext() && isPM.matcher(next.toString()).matches())) {
					String time = token.toString();
					String[] split = time.split(":");
					if(split != null && split.length > 1) {
						Integer hours = Integer.valueOf(split[0]);
						if(hours < 12) {
							split[0] = String.valueOf(hours + 12);
						}
						time = "";
						for(String value : split) {
							time += value + ":";
						}
						time = time.substring(0,time.length() - 1);
						token.setTermText(time);
					}
				}
				String tokenValue = token.toString().replaceAll("(A|a|P|p)(m|M)", "");
				boolean addEndingDot = false;
				if(tokenValue.contains(".")) {
					addEndingDot = true;
					tokenValue = tokenValue.replace(".", "");
				}
				tokenValue += addEndingDot ? ":00." : ":00";

				if(tokenStream.hasNext() && isAMorPM.matcher(next.toString()).matches()) {
					next.setTermText(next.toString().replaceAll("(A|a|P|p)(m|M)", "").trim());
					if(".".equals(next.toString())) {
						tokenValue += ".";
						tokenStream.remove(tokenStream.getCurrentPosition() + 1);
					}
				}
				token.setTermText(tokenValue);
			}
		}
		return true;
	}

}