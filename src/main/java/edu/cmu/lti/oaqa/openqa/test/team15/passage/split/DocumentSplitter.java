package edu.cmu.lti.oaqa.openqa.test.team15.passage.split;

import java.util.List;

import edu.cmu.lti.oaqa.openqa.test.team15.passage.DocumentParagraph;

public interface DocumentSplitter {
  public List<DocumentParagraph> splitDocument(String documentText);
}
