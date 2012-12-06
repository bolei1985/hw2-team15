package edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class MingyansSiteQPassageFinder {
  private String text;

  private String docId;

  public MingyansSiteQPassageFinder(String docId, String text) {
    super();
    this.text = text;
    this.docId = docId;
  }

  public List<PassageCandidate> extractPassages(String[] keyterms) {
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageSpan> sentences = new ArrayList<PassageSpan>();
    List<PassageSpan> sentencewindows = new ArrayList<PassageSpan>();
    // Get Sentence from Document text
    BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
    iterator.setText(text);
    int start = iterator.first();
    for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
      System.out.println("!!!!!!!!!!!!!!!!!!" + text.substring(start, end));
      sentences.add(new PassageSpan(text.substring(start, end), start, end));
    }

    // one sentence to three sentence window
    if (sentences.size() <= 2) {
      sentencewindows = sentences;
    } else {
      for (int i = 0; i < sentences.size() - 2; i++) {
        String window = sentences.get(i).text + " " + sentences.get(i + 1).text + " "
                + sentences.get(i + 2).text;
        int begin = sentences.get(i).begin;
        int end = sentences.get(i + 2).end + 2;
        sentencewindows.add(new PassageSpan(window, begin, end));
      }
    }
    
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    for (PassageSpan sentence : sentencewindows) {

      System.out.println("@@@@@@@@@@@@@@@"+sentence.text);
      int k = 0;
      int matched_cnt = 0;
      double Score1 = 0.0;
      // Find all keyterm matches.

      for (String keyterm : keyterms) {
        Pattern p = Pattern.compile(keyterm);
        Matcher m = p.matcher(sentence.text);

        while (m.find()) {
          PassageSpan match = new PassageSpan(keyterm, m.start(), m.end());
          matchedSpans.add(match);
          k++;
        }
      }

      if (!matchedSpans.isEmpty()) {
        Score1 += 0.25;
        matched_cnt++;
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
    double alpha = 1.0;
    int dist = 0;
    double Score2_wgt=0.0;

    ComparatorPassageSpan comparator = new ComparatorPassageSpan();
    Collections.sort(matchedSpans, comparator);

    for(int i=0;i<matchedSpans.size()-1;i++){
      dist=matchedSpans.get(i+1).begin-matchedSpans.get(i).end;
      Score2_wgt+=0.25*2/(alpha*dist*dist);
    }
    Score2=Score2_wgt*matched_cnt/(k-1);
    Score=Score1+Score2;
    
    return Score;
  }

  protected class PassageSpan {
    private int begin, end;

    private String text;

    public PassageSpan(String text, int begin, int end) {
      this.begin = begin;
      this.end = end;
      this.text = text;
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