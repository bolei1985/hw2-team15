package edu.cmu.lti.oaqa.openqa.test.team15.retrieval;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class FuzzyVariantBioSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapper wrapper;

  private static Logger logger = Logger.getLogger(FuzzyVariantBioSolrRetrievalStrategist.class);

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
  protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    List<String> keytermStrList = Lists.transform(keyterms, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });
    String query = formulateQuery(keytermStrList);

    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        logger.debug(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      logger.error("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  private String formulateQuery(List<String> keytermStrList) {
    StringBuilder sb = new StringBuilder();
    for (String keyterm : keytermStrList) {
      if (keyterm.startsWith("p.")) {
        keyterm = keyterm.substring(2);
      }
      sb.append("\"" + keyterm + "\"~ ");
    }
    String query = sb.toString();
    logger.debug("Fuzzy QUERY: " + query);
    return query;
  }

}
