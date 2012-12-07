package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate.CandidateFinder;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.split.DoNothingSplitter;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.split.DocumentSplitter;

public class SimpleTeam15PassageExtractor extends SimplePassageExtractor {
  private CandidateFinder finder;

  private DocumentSplitter splitter = new DoNothingSplitter();

  private Logger logger = Logger.getLogger(SimpleTeam15PassageExtractor.class);

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String finderClassName = (String) aContext.getConfigParameterValue("candidateFinder");
    String splitterClassName = (String) aContext.getConfigParameterValue("documentSplitter");
    try {
      @SuppressWarnings("unchecked")
      Class<CandidateFinder> candidateClz = (Class<CandidateFinder>) Class.forName(finderClassName);
      finder = candidateClz.getConstructor(new Class[] {}).newInstance();
    } catch (Exception e) {
      logger.error("", e);
    }
    try {
      @SuppressWarnings("unchecked")
      Class<DocumentSplitter> splitterClz = (Class<DocumentSplitter>) Class
              .forName(splitterClassName);
      splitter = splitterClz.getConstructor(new Class[] {}).newInstance();
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (RetrievalResult document : documents) {
      try {
        String docId = document.getDocID();
        String htmlText = wrapper.getDocText(docId);
        List<DocumentParagraph> paragraphList = splitter.splitDocument(htmlText);
        for (DocumentParagraph paragraph : paragraphList) {
          String rawText = paragraph.getRawText();
          logger.debug("splitted document:\n" + rawText);
          // String text = Jsoup.parse(docText).text().replaceAll("([\177-\377\0-\32]*)", "");
          String text = rawText.substring(0, Math.min(5000, rawText.length()));
          List<PassageCandidate> passageSpans = finder.extractPassages(docId, text,
                  paragraph.getStartPosition(), keyterms);
          result.addAll(passageSpans);
        }
      } catch (SolrServerException e) {
        logger.error("", e);
      }
    }
    return result;
  }
}
