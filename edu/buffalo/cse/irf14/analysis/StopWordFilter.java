package edu.buffalo.cse.irf14.analysis;

public class StopWordFilter extends TokenFilter{

	public StopWordFilter() { super(); }
	
	public StopWordFilter(TokenStream stream) {
		super(stream);
	}

	@Override
	public boolean increment() throws TokenizerException {
//		System.out.println("Came here");
//		System.out.println("tokenStream :: " + tokenStream);
		return false;
	}

	@Override
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
