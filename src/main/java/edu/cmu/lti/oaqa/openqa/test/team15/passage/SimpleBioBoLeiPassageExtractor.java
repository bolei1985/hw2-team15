package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class SimpleBioBoLeiPassageExtractor extends SimplePassageExtractor {
  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    DOMConfigurator.configure("configuration/log4j.xml");
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    // TODO
    return new LinkedList<PassageCandidate>();
  }
}
