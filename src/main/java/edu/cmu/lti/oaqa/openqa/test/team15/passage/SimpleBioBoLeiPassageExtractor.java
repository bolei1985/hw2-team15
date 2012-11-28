package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

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
        htmlText = htmlText.substring(0, 8000);
        BoLeiTfIdfCandidateFinder finder = new BoLeiTfIdfCandidateFinder(id);
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        result.addAll(finder.extractPassages(htmlText, keytermStrings));
      } catch (SolrServerException e) {
        logger.error("", e);
      }

    }

    return result;
  }
}
