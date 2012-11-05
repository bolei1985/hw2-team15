package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class BoLeiDatabaseKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    System.out.println("BoLeiDatabaseKeytermExtractor getKeyterms");
    return null;
  }

}
