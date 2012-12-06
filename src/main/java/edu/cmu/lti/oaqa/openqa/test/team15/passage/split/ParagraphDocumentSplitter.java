package edu.cmu.lti.oaqa.openqa.test.team15.passage.split;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.lti.oaqa.openqa.test.team15.passage.DocumentParagraph;

public class ParagraphDocumentSplitter implements DocumentSplitter {

  @Override
  public List<DocumentParagraph> splitDocument(String documentText) {
    List<DocumentParagraph> result = new LinkedList<DocumentParagraph>();
    String tag = "(<p>)|(</p>)|(<P>)|(</P>)";
    Pattern pattern = Pattern.compile(tag);
    Matcher matcher = pattern.matcher(documentText);
    TreeSet<Integer> positions = new TreeSet<Integer>();
    while (matcher.find()) {
      positions.add(matcher.start()); // value of position is sorted
    }
    Integer begin = 0, end = positions.higher(begin);
    while (end != null) {
      String temp = documentText.substring(begin, end);
      int offset = (begin == 0 ? 0 : temp.indexOf('>') + 1);
      result.add(new DocumentParagraph(temp.substring(offset), begin + offset));
      begin = end;
      end = positions.higher(begin);
    }
    return result;
  }

}
