package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand.KeytermExpander;

public class CthayurPosTagKeytermExpander extends AbstractKeytermExtractor {

  private NewPosTagNamedEntityRecognizer posTaggerAnno;
  private KeytermExpander expander2;
  private Logger logger = Logger.getLogger(CthayurPosTagKeytermExpander.class);
  
  public CthayurPosTagKeytermExpander() throws ResourceInitializationException {
    super();
    posTaggerAnno = new NewPosTagNamedEntityRecognizer();
  }
  
  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    
    DOMConfigurator configurator = new DOMConfigurator();
    InputStream log4jConfigIn = this.getClass().getClassLoader()
            .getResourceAsStream("configuration/log4j.xml");
    configurator.doConfigure(log4jConfigIn, LogManager.getLoggerRepository());
    String expanderClassName2 = (String) c.getConfigParameterValue("expander2");
    try {
      @SuppressWarnings("unchecked")
      Class<KeytermExpander> clz2 = (Class<KeytermExpander>) Class.forName(expanderClassName2);
      expander2 = clz2.getConstructor(new Class[] { UimaContext.class }).newInstance(c);
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {

    Set<String> extendStrSet = new HashSet<String>();
    
    List<Keyterm> keyterms = new LinkedList<Keyterm>();
    
    Map<Integer, Integer> spans = posTaggerAnno.getGeneSpans(question);
    Set<Entry<Integer, Integer>> entrySet = spans.entrySet();
    Set<String> dupSet = new HashSet<String>();
    
    for (Entry<Integer, Integer> entry : entrySet) {
      String presentTerm = question.substring(entry.getKey(), entry.getValue());
      dupSet.add(presentTerm);
      Keyterm kt1 = new Keyterm(presentTerm);
      kt1.setProbablity(1f);
      keyterms.add(kt1);
      extendStrSet.addAll(expander2.expandKeyterm(presentTerm, posTaggerAnno.getPOS(presentTerm)));
    }
    
    System.out.println("EXPANDER: " +  extendStrSet);
    for (String extendedKeyterm : extendStrSet) {
      if (!dupSet.contains(extendedKeyterm)) {
        Keyterm kt = new Keyterm(extendedKeyterm);
        kt.setProbablity(0.6f);
        keyterms.add(kt);
      }
    }
    
    return keyterms;
  }
}