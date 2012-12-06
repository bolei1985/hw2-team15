package edu.cmu.lti.oaqa.openqa.test.team15.passage.split;

import java.util.LinkedList;
import java.util.List;

import edu.cmu.lti.oaqa.openqa.test.team15.passage.DocumentParagraph;

public class DoNothingSplitter implements DocumentSplitter {

  @Override
  public List<DocumentParagraph> splitDocument(String documentText) {
    List<DocumentParagraph> result = new LinkedList<DocumentParagraph>();
    result.add(new DocumentParagraph(documentText, 0));
    return result;
  }

}
