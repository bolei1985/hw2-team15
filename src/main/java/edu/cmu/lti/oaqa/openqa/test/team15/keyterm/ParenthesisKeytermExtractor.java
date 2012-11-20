package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class ParenthesisKeytermExtractor extends AbstractKeytermExtractor {
	@Override
	/*
	 * exploit lingpipe to extract gene-related key terms
	 * @see edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor#getKeyterms(java.lang.String)
	 */
	protected List<Keyterm> getKeyterms(String question){
		File modelFile = new File("src/main/resources/model/ne-en-bio-genia.TokenShapeChunker");
		List<Keyterm> keyterms = new ArrayList<Keyterm>();
		
		String[] questionParts = question.split("\\(|\\)");
		for(String questionPart : questionParts) { 
			System.out.println(questionPart);
			try {
				Chunker chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
				Chunking chunking = chunker.chunk(questionPart);
				Set<Chunk> chunkSet = chunking.chunkSet();
				Iterator<Chunk> it = chunkSet.iterator(); 
			
				while(it.hasNext()) {
					Chunk presentChunk = it.next();
					keyterms.add(new Keyterm(questionPart.substring(presentChunk.start(), presentChunk.end())));
				}
			} catch(ClassNotFoundException e) {
				  System.err.println("No definition for the class has been found.");
				  e.printStackTrace();
				  return null;
			} catch (IOException e) {
				  System.err.println("Error reading model files.");
				  e.printStackTrace();
				  return null;
			}
		}
		return keyterms;
	}
}
