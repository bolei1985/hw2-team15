package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate.MingyansSiteQPassageFinder;

public class MingyansPassageExtractor extends AbstractPassageExtractor {

  protected SolrWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");

    // String keytermWindowScorer = (String)
    // aContext.getConfigParameterValue("keytermWindowScorer");
    // System.out.println("initialize() : keytermWindowScorer: " + keytermWindowScorer);

    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterm,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    // int count = 1;
    // String[] querykeyterm = null;

    for (RetrievalResult document : documents) {
      // if (count == 1) {
      // String[] query = document.getQueryString().split(" \"");
      // querykeyterm = getQueryKeyTerm(query);
      // count = 0;
      // }

      String id = document.getDocID();
      try {
        String text = wrapper.getDocText(id);
        // cleaning HTML text
        // String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/*
        // * .trim()
        // */;
        // for now, making sure the text isn't too long
        // text = text.substring(0, Math.min(5000, text.length()));

        MingyansSiteQPassageFinder finder = new MingyansSiteQPassageFinder();
        // List<String> keytermStrings = Lists.transform(keyterm, new Function<Keyterm, String>() {
        // public String apply(Keyterm keyterm) {
        // return keyterm.getText();
        // }
        // });

        // for(String a:keytermStrings){
        // System.out.println("@@@@@@@@@@@@"+a);
        // }

        List<PassageCandidate> passageSpans = finder.extractPassages(id, text, 0, keyterm);
        for (PassageCandidate passageSpan : passageSpans)
          result.add(passageSpan);
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    return result;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }

  protected String[] getQueryKeyTerm(String[] query) {
    String[] queryword = new String[1];
    List<String> querykeyterm = new LinkedList<String>();
    for (String a : query) {
      if (!a.contains("\"")) {
        String[] b = a.split(" ");
        for (String c : b) {
          // System.out.println("###########");
          // System.out.println(c);
          querykeyterm.add(c);
        }
      }
      if (a.endsWith("\"")) {
        // System.out.println("!!!!!!!!!!!!");
        // System.out.println(a.substring(0, a.length() - 1));
        querykeyterm.add(a.substring(0, a.length() - 1));
      }
      if (a.contains("\" ")) {
        String[] b = a.split("\" ");
        for (String c : b) {
          // System.out.println("@@@@@@@");
          // System.out.println(c);
          querykeyterm.add(c);
        }
      }
    }
    return querykeyterm.toArray(queryword);
  }
}
