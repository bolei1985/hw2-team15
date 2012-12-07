package edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class MingyansSiteQPassageFinder {
  private String text;

  private String docId;

  double alpha = 2.0;

  public MingyansSiteQPassageFinder(String docId, String text) {
    super();
    this.text = text;
    this.docId = docId;
  }

  public List<PassageCandidate> extractPassages(List<Keyterm> keytermList) {
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<Integer> sentences = new ArrayList<Integer>();
    List<PassageSpan> sentencewindows = new ArrayList<PassageSpan>();

    // Get Sentence from Document text
    Pattern p2 = Pattern.compile("[.?!] ");
    Matcher m2 = p2.matcher(text);
    sentences.add(0);
    while (m2.find()) {
      sentences.add(m2.end());
    }

    // one sentence to three sentence window
    if (sentences.size() <= 4) {
      sentencewindows.add(new PassageSpan(text, 0, text.length(), 0));
    } else {
      for (int i = 0; i < sentences.size() - 4; i++) {
        String window = text.substring(sentences.get(i), sentences.get(i + 3));
        sentencewindows.add(new PassageSpan(window, i, i + 3, 0));
      }
    }

    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    for (PassageSpan sentence : sentencewindows) {

      int k = 0;
      int matched_cnt = 0;
      double Score1 = 0.0;
      // Find all keyterm matches.

      for (Keyterm keyterm : keytermList) {
        Pattern p = Pattern.compile(keyterm.getText());
        Matcher m = p.matcher(sentence.text);

        while (m.find()) {
          PassageSpan match = new PassageSpan(keyterm.getText(), m.start(), m.end(),
                  keyterm.getProbability());
          matchedSpans.add(match);
          k++;
        }

        if (!matchedSpans.isEmpty()) {
          Score1 += keyterm.getProbability();
          matched_cnt++;
        }
      }

      double score = getScore(Score1, k, matched_cnt, matchedSpans);

      PassageCandidate window = null;
      try {
        window = new PassageCandidate(docId, sentence.begin, sentence.end, (float) score, null);
      } catch (AnalysisEngineProcessException e) {
        e.printStackTrace();
      }
      result.add(window);
    }

    // Sort the result in order of decreasing score.
    // Collections.sort ( result , new PassageCandidateComparator() );
    return result;

  }

  protected double getScore(double Score1, int k, int matched_cnt, List<PassageSpan> matchedSpans) {
    double Score = 0.0;
    double Score2 = 0.0;
    int dist = 0;
    double Score2_wgt = 0.0;

    ComparatorPassageSpan comparator = new ComparatorPassageSpan();
    Collections.sort(matchedSpans, comparator);

    for (int i = 0; i < matchedSpans.size() - 1; i++) {
      dist = matchedSpans.get(i + 1).begin - matchedSpans.get(i).end;
      Score2_wgt += (matchedSpans.get(i + 1).prob + matchedSpans.get(i).prob)
              / (alpha * dist * dist);
    }
    Score2 = Score2_wgt * matched_cnt / (k - 1);
    Score = Score1 + Score2;

    return Score;
  }

  protected class PassageSpan {
    private int begin, end;

    private String text;

    private double prob;

    public PassageSpan(String text, int begin, int end, double prob) {
      this.begin = begin;
      this.end = end;
      this.text = text;
      this.prob = prob;
    }
  }

  protected class ComparatorPassageSpan implements Comparator<PassageSpan> {

    public int compare(PassageSpan arg0, PassageSpan arg1) {
      PassageSpan span1 = arg0;
      PassageSpan span2 = arg1;

      if (span1.begin > span2.begin)
        return 1;
      else
        return -1;
    }
  }
}