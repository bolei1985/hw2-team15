package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class MingyangLingPipekeytermextractor extends AbstractKeytermExtractor {

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {

    File modelFile = new File("src/main/resources/model/ne-en-bio-genetag.HmmChunker");
    
    ArrayList<Keyterm> keyterms = new ArrayList<Keyterm>();

    ConfidenceChunker chunker = null;

    try {
      chunker = (ConfidenceChunker) AbstractExternalizable.readObject(modelFile);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    char[] cs = question.toCharArray();
    Iterator<Chunk> it2 = chunker.nBestChunks(cs, 0, cs.length, 20);
    while (it2.hasNext()) {
      Chunk chunk = it2.next();
      /*
       * Choose a threshold to leave the tag which is not Gene Tag.
       */
      if (Math.pow(2.0, chunk.score()) < 0.75)
        continue;
      keyterms.add(new Keyterm(question.substring(chunk.start(), chunk.end())));
    }

    keyterms.toString();
    return keyterms;
  }
}