package edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate;

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
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class BoLeiTfIdfCandidateFinder implements CandidateFinder {

  private static Logger logger = Logger.getLogger(BoLeiTfIdfCandidateFinder.class);

  public List<PassageCandidate> extractPassages(String documentId, String text, int startPos,
          List<Keyterm> keyterms) {

    // from high score to low score
    TreeSet<PassageCandidate> resultSet = new TreeSet<PassageCandidate>(Collections.reverseOrder());
    List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });

    HashMap<String, HashMap<PassageSpan, Float>> tfMap = new HashMap<String, HashMap<PassageSpan, Float>>();
    HashMap<String, Float> idfMap = new HashMap<String, Float>();
    List<PassageSpan> passages = new LinkedList<PassageSpan>();

    int passageCount = 0;

    Set<Integer> leftEdges = new HashSet<Integer>();
    Set<Integer> rightEdges = new HashSet<Integer>();

    for (String keyterm : keytermStrings) {
      tfMap.put(keyterm, new HashMap<PassageSpan, Float>());
      Pattern p = Pattern.compile(keyterm);
      Matcher m = p.matcher(text);
      while (m.find()) {
        leftEdges.add(m.start());
        rightEdges.add(m.end());
      }
    }

    for (int begin : leftEdges) {
      for (int end : rightEdges) {
        if (end <= begin) {
          continue;
        }
        // inside one candidate passage
        // calculate its TF:
        passageCount++;
        PassageSpan currentPassage = new PassageSpan(begin, end);

        String passageHtmlText = text.substring(begin, end);
        String cleanedText = cleanHtmlTags(passageHtmlText);
        String[] tokens = cleanedText.split(" ");
        passages.add(currentPassage);
        for (String keyterm : keytermStrings) {
          int wordCount = 0;
          for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase(keyterm)) {
              wordCount++;
            }
          }
          float frequency = ((float) wordCount) / ((float) tokens.length);
          if (frequency > 0) {
            tfMap.get(keyterm).put(currentPassage, frequency);
          }
        }
      }
    }

    // calculate IDF for each key term
    for (String keyterm : keytermStrings) {
      int occurance = tfMap.get(keyterm).size();
      float idf = (float) Math.log10(((double) passageCount) / ((double) occurance));
      idfMap.put(keyterm, idf);
    }

    // calculate score for each passage
    Iterator<PassageSpan> it = passages.iterator();
    while (it.hasNext()) {
      PassageSpan passage = it.next();
      float score = 0f;
      for (Keyterm keyterm : keyterms) {
        float keytermTf = 0f;
        if (tfMap.get(keyterm.getText()).containsKey(passage)) {
          keytermTf = tfMap.get(keyterm.getText()).get(passage);
        }
        float keytermIdf = idfMap.get(keyterm.getText());
        score += keytermIdf * keytermTf * keyterm.getProbability();
      }

      try {
        PassageCandidate candidate = new PassageCandidate(documentId, passage.begin + startPos,
                passage.end + startPos, score, null);
        resultSet.add(candidate);
      } catch (AnalysisEngineProcessException e) {
        logger.error("", e);
      }
    }
    LinkedList<PassageCandidate> result = new LinkedList<PassageCandidate>(resultSet);

    return result.subList(0, (resultSet.size() / 10));

  }

  private String cleanHtmlTags(String dirtyText) {
    return dirtyText.replaceAll("<(\"[^\"]*\"|'[^']*'|[^'\">])*>", "");
  }

  private static class PassageSpan {
    private static int count = 0;

    private final int id = count++;

    private int begin, end;

    public PassageSpan(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    @Override
    public int hashCode() {
      return id;
    }

    @Override
    public boolean equals(Object obj) {
      if ((obj instanceof PassageSpan) == false) {
        return false;
      }
      PassageSpan span = (PassageSpan) obj;
      return span.id == id ? true : false;
    }
  }
}
