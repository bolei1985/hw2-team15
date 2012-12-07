package edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;

public class EmptyKeytermExpander extends KeytermExpander {

  public EmptyKeytermExpander(UimaContext c) {
    super(c);
  }

  @Override
  public List<String> expandKeyterm(String keyterm, String pos) {
    return new LinkedList<String>(Arrays.asList(new String[] { keyterm }));
  }

}
