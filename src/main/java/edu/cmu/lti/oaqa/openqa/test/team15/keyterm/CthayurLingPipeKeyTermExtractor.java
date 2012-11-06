package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class CthayurLingPipeKeyTermExtractor extends AbstractKeytermExtractor {

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {

    // Read the trained model
    File modelFile = new File("src/main/resources/model/ne-en-bio-genetag.HmmChunker");

    // TokenShapeChunker
    Chunker chunker = null;
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ArrayList<Keyterm> keyterms = new ArrayList<Keyterm>();

    Chunking chunking = chunker.chunk(question);

    Set<Chunk> chunkSet = chunking.chunkSet();

    Iterator iter = chunkSet.iterator();

    while (iter.hasNext()) {

      Chunk mChunk = (Chunk) iter.next();

      // Pick out all gene type named entities
      if (mChunk.type().equalsIgnoreCase("GENE")) {

        int start = mChunk.start();
        int end = mChunk.end();

        keyterms.add(new Keyterm(question.substring(start, end)));

      }
    }
    
    keyterms.toString();

    return keyterms;
  }
}