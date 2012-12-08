package edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand;

import java.util.List;

import org.apache.uima.UimaContext;

public abstract class KeytermExpander {
	protected UimaContext context;

	public KeytermExpander(UimaContext c) {
		context = c;
	}

	/**
	 * expand an input key term
	 * 
	 * @param keyterm
	 * @return words that are related to the input keyterm
	 */
	public abstract List<String> expandKeyterm(String keyterm, String pos);
}
