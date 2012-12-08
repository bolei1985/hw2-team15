package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate.BoLeiTfIdfCandidateFinder;

public class SimpleBioBoLeiPassageExtractor extends SimplePassageExtractor {

  private static Logger logger = Logger.getLogger(SimpleBioBoLeiPassageExtractor.class);

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (RetrievalResult document : documents) {
      String id = document.getDocID();
      logger.debug("RetrievalResult: " + id);
      try {
        String htmlText = wrapper.getDocText(id);
        htmlText = htmlText.substring(0, htmlText.length() > 7000 ? 7000 : htmlText.length());
        BoLeiTfIdfCandidateFinder finder = new BoLeiTfIdfCandidateFinder();
        result.addAll(finder.extractPassages(id, htmlText, 0, keyterms));
      } catch (SolrServerException e) {
        logger.error("", e);
      }

    }

    return result;
  }
}
