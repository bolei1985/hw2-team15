package edu.cmu.lti.oaqa.openqa.test.team15.passage.candidate;

import java.util.List;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public interface CandidateFinder {
  public List<PassageCandidate> extractPassages(String documentId, String docText,
          int startPosition, List<Keyterm> keytermList);
}
