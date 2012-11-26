package edu.cmu.lti.oaqa.openqa.test.team15.retrieval;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class MingyansSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected final List<RetrievalResult> retrieveDocuments(String questionText,
          List<Keyterm> keyterms) {
    String query = formulateQuery(keyterms);
    return retrieveDocuments(query);
  };

  protected String formulateQuery(List<Keyterm> keyterms) {
    StringBuffer result = new StringBuffer();

    for (Keyterm keyterm : keyterms) {
      System.out.println(keyterm.getText());
      List<String> json = HttpGet(keyterm.getText());
      result.append(keyterm.getText() + " ");
      if (json != null) {
        for (String j : json) {
          result.append("\"" + j + "\"" + " ");
          System.out.println(j);
        }
      }
    }
    
    
    System.out.println("@@@@@@@@@@");
    String query = result.toString();
    System.out.println(" QUERY: " + query);
    return query;
  }

  private List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }

  public static List<String> HttpGet(String word) {
    List<String> strResult = null;
    try {
      URL url = new URL("http://words.bighugelabs.com/api/2/e292deaa9aed63cbd584671802cfcfd9/"
              + word + "/");
      System.out.println(url);
      URLConnection urlConnection = url.openConnection();
      urlConnection.setDoInput(true);
      InputStream in = urlConnection.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "gbk"));
      String line;
      strResult = new LinkedList<String>();
      while ((line = br.readLine()) != null) {
        String[] res = line.split("\\|");
        if (res[1].equals("syn")) {
          strResult.add(res[2]);
        }
      }
    } catch (IOException e) {
    }
    return strResult;
  }
}