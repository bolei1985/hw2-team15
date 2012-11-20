package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class BoLeiStanfordKeytermExtractor extends AbstractKeytermExtractor {
  private PosTagNamedEntityRecognizer posTaggerAnno;

  public BoLeiStanfordKeytermExtractor() throws ResourceInitializationException {
    super();
    posTaggerAnno = new PosTagNamedEntityRecognizer();
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    System.out.println(question);
    List<Keyterm> keyTermList = new LinkedList<Keyterm>();
    Map<Integer, Integer> spans = posTaggerAnno.getGeneSpans(question);
    Set<Entry<Integer, Integer>> entrySet = spans.entrySet();
    for (Entry<Integer, Integer> entry : entrySet) {
      keyTermList.add(new Keyterm(question.substring(entry.getKey(), entry.getValue())));
    }
    return keyTermList;
  }

}
