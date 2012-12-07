package edu.cmu.lti.oaqa.openqa.test.team15.passage;

public class TextWindow {

  public static final int MAX_WINDOW_LENGTH = 500;

  private static int count = 0;

  private final int id = count++;

  private int begin, end;

  private String documentId;

  public TextWindow(int begin, int end, String docId) {
    this.begin = begin;
    this.end = end;
    documentId = docId;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if ((obj instanceof TextWindow) == false) {
      return false;
    }
    TextWindow span = (TextWindow) obj;
    return span.id == id ? true : false;
  }

  public int getBegin() {
    return begin;
  }

  public int getEnd() {
    return end;
  }

  public String getDocumentId() {
    return documentId;
  }

}
