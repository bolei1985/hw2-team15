package edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand;

import java.util.List;

public interface KeytermExpander {
	/**
	 * expand an input key term
	 * @param keyterm
	 * @return words that are related to the input keyterm
	 */
	public List<String> expandKeyterm(String keyterm, String pos);
}
