package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class BoLeiDatabaseKeytermUpdater extends AbstractKeytermUpdater {
  private static final String GENE_NAME_FILE = "geneNameFile";

  private Set<String> geneNames;

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    Iterator<Keyterm> it = keyterms.iterator();
    while (it.hasNext()) {
      if (!isGeneName(it.next().getText())) {
        it.remove();
      }
    }
    return keyterms;
  }

  @Override
  public void initialize(UimaContext c) throws ResourceInitializationException {
    super.initialize(c);
    BufferedReader in = null;
    try {
      if (geneNames == null) {

        String geneNameFilePath = (String) c.getConfigParameterValue(GENE_NAME_FILE);
        File file = new File(geneNameFilePath);

        in = new BufferedReader(new FileReader(file));
        geneNames = new HashSet<String>();
        String line;
        while ((line = in.readLine()) != null) {
          geneNames.add(line);
        }

      }
    } catch (IOException e) {
      new ResourceInitializationException(e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private boolean isGeneName(String name) {
    String stemName = stemmer(name);
    return geneNames.contains(stemName);
  }

  private String stemmer(String name) {
    if (name.endsWith("ies")) {
      return name.substring(0, name.lastIndexOf("ies")) + "y";
    }
    if (name.endsWith("oes") || name.endsWith("xes") || name.endsWith("ses")) {
      return name.substring(0, name.lastIndexOf("es"));
    }
    if (name.endsWith("es")) {
      return name.substring(0, name.lastIndexOf("es")) + "e";
    }
    if (name.endsWith("s")) {
      return name.substring(0, name.lastIndexOf("s"));
    }
    return name;
  }
}
