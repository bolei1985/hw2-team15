package edu.cmu.lti.oaqa.openqa.test.team15.passage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.split.DocumentSplitter;
import edu.cmu.lti.oaqa.openqa.test.team15.passage.split.ParagraphDocumentSplitter;

public class BoLeiGlobalTfIdfPassageExtractor extends SimplePassageExtractor {

  private static Logger logger = Logger.getLogger(BoLeiGlobalTfIdfPassageExtractor.class);

  private static DocumentSplitter splitter = new ParagraphDocumentSplitter();

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    TreeSet<PassageCandidate> resultSet = new TreeSet<PassageCandidate>(Collections.reverseOrder());
    List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });
    HashMap<String, HashMap<TextWindow, Float>> tfMap = new HashMap<String, HashMap<TextWindow, Float>>();
    for(String keyterm: keytermStrings){
      tfMap.put(keyterm, new HashMap<TextWindow, Float>());
    }
    HashMap<String, Float> idfMap = new HashMap<String, Float>();
    List<TextWindow> passages = new LinkedList<TextWindow>();

    Set<Integer> leftEdges, rightEdges;

    for (RetrievalResult doc : documents) {
      String id = doc.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
        List<DocumentParagraph> paragraphList = splitter.splitDocument(htmlText);
        for (DocumentParagraph paragraph : paragraphList) {
          leftEdges = new HashSet<Integer>();
          rightEdges = new HashSet<Integer>();

          String text = paragraph.getRawText();

          // find passage boundaries
          for (String keyterm : keytermStrings) {
            Pattern p = Pattern.compile(keyterm);
            Matcher m = p.matcher(text);
            while (m.find()) {
              leftEdges.add(m.start());
              rightEdges.add(m.end());
            }
          }

          for (int begin : leftEdges) {
            for (int end : rightEdges) {
              if (end <= begin || (end - begin > TextWindow.MAX_WINDOW_LENGTH)) {
                continue;
              }
              TextWindow currentPassage = new TextWindow(begin + paragraph.getStartPosition(), end
                      + paragraph.getStartPosition(), id);
              passages.add(currentPassage);
              String passageHtmlText = text.substring(begin, end);
              String cleanedText = cleanHtmlTags(passageHtmlText);
              for (String keyterm : keytermStrings) {
                float frequency = getTF(cleanedText, keyterm);
                if (frequency > 0) {
                  tfMap.get(keyterm).put(currentPassage, frequency);
                }
              }
            }
          }
        }
      } catch (SolrServerException e) {
        logger.error("", e);
      }
    }

    // calculate IDF for each key term
    for (String keyterm : keytermStrings) {
      int occurance = tfMap.get(keyterm).size();
      float idf = 0f;
      if (occurance != 0) {
        idf = (float) Math.log10(((float) passages.size()) / ((float) occurance));
      }
      idfMap.put(keyterm, idf);
    }

    // calculate score for each passage
    Iterator<TextWindow> it = passages.iterator();
    while (it.hasNext()) {
      TextWindow passage = it.next();
      float score = 0f;
      for (Keyterm keyterm : keyterms) {
        float keytermTf = 0f;
        if (tfMap.get(keyterm.getText()).containsKey(passage)) {
          keytermTf = tfMap.get(keyterm.getText()).get(passage);
        }
        float keytermIdf = idfMap.get(keyterm.getText());
        score += keytermIdf * keytermTf;
      }

      try {
        PassageCandidate candidate = new PassageCandidate(passage.getDocumentId(),
                passage.getBegin(), passage.getEnd(), score, null);
        resultSet.add(candidate);
      } catch (AnalysisEngineProcessException e) {
        logger.error("", e);
      }
    }

    LinkedList<PassageCandidate> result = new LinkedList<PassageCandidate>(resultSet);
    return result.subList(0, (resultSet.size() / 1000));
  }

  private String cleanHtmlTags(String dirtyText) {
    return dirtyText.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", " ");
  }

  private float getTF(String cleanText, String keyterm) {
    String[] tokens = cleanText.split(" ");

    int wordCount = 0;
    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].equalsIgnoreCase(keyterm)) {
        wordCount++;
      }
    }
    float frequency = ((float) wordCount) / ((float) tokens.length);
    return frequency;
  }
}
