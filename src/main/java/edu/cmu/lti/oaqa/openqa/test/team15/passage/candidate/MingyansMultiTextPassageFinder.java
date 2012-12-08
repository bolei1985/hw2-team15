package edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class MingyansMultiTextPassageFinder implements CandidateFinder {

  private int textword;

  @Override
  public List<PassageCandidate> extractPassages(String documentId, String docText,
          int startPosition, List<Keyterm> keytermList) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    HashMap<String, Double> hash = new HashMap<String, Double>();

    List<String> keytermStrings = Lists.transform(keytermList, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });    
    // Find all keyterm matches.
    for (String keyterm : keytermStrings) {
      int ft = 1;
      Pattern p = Pattern.compile(keyterm);
      Matcher m = p.matcher(docText);
      while (m.find()) {
        PassageSpan match = new PassageSpan(m.start(), m.end(), keyterm);
        matchedSpans.add(match);
        ft++;
      }
      if (!matchedSpans.isEmpty()) {
        hash.put(keyterm, Math.log(textword / ft));
        matchingSpans.add(matchedSpans);
      }
    }

    // create set of left edges and right edges which define possible windows.
    List<Integer> leftEdges = new ArrayList<Integer>();
    List<Integer> rightEdges = new ArrayList<Integer>();
    for (List<PassageSpan> keytermMatches : matchingSpans) {
      for (PassageSpan keytermMatch : keytermMatches) {
        Integer leftEdge = keytermMatch.begin;
        Integer rightEdge = keytermMatch.end;
        if (!leftEdges.contains(leftEdge))
          leftEdges.add(leftEdge);
        if (!rightEdges.contains(rightEdge))
          rightEdges.add(rightEdge);
      }
    }

    // For every possible window, calculate keyterms found, matches found; score window, and create
    // passage candidate.
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (Integer begin : leftEdges) {
      for (Integer end : rightEdges) {
        if (end <= begin)
          continue;
        // This code runs for each window.
        double totalwgts = 0.0;
        int keytermsFound = 0;
        for (List<PassageSpan> keytermMatches : matchingSpans) {
          boolean thisKeytermFound = false;
          double keytermweight = 0.0;
          for (PassageSpan keytermMatch : keytermMatches) {
            if (keytermMatch.containedIn(begin, end)) {
              thisKeytermFound = true;
              keytermweight = hash.get(keytermMatch.text);
            }
          }
          if (thisKeytermFound) {
            keytermsFound++;
            totalwgts += keytermweight;
          }
        }

        double score = totalwgts - keytermsFound * Math.log(end - begin + 1);
        PassageCandidate window = null;
        try {
          window = new PassageCandidate(documentId, begin + startPosition, end + startPosition,
                  (float) score, null);
        } catch (AnalysisEngineProcessException e) {
          e.printStackTrace();
        }
        result.add(window);
      }
    }

    // Sort the result in order of decreasing score.
    // Collections.sort ( result , new PassageCandidateComparator() );
    return result;

  }

  class PassageSpan {
    private int begin, end;

    private String text;

    public PassageSpan(int begin, int end, String text) {
      this.begin = begin;
      this.end = end;
      this.setText(text);
    }

    public boolean containedIn(int begin, int end) {
      if (begin <= this.begin && end >= this.end) {
        return true;
      } else {
        return false;
      }
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }
}