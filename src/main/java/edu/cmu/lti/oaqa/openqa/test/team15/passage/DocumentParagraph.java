package edu.cmu.lti.oaqa.openqa.test.team15.passage;

public class DocumentParagraph {
  private String rawText;

  private int startPosition;

  public DocumentParagraph(String text, int pos) {
    rawText = text;
    startPosition = pos;
  }

  public String getRawText() {
    return rawText;
  }

  public int getStartPosition() {
    return startPosition;
  }
}
