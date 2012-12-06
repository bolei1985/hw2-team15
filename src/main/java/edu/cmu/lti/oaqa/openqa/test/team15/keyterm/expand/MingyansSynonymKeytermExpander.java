package edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;

import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class MingyansSynonymKeytermExpander extends KeytermExpander {

  private String synAPI;

  public MingyansSynonymKeytermExpander(UimaContext c) {
    super(c);
    synAPI = (String) c.getConfigParameterValue("synapi");
  }

  @Override
  public List<String> expandKeyterm(String keyterm, String pos) {
    List<String> strResult = new LinkedList<String>();
    strResult.add(keyterm);
    try {
      URL url = new URL(synAPI + keyterm + "/");
      System.out.println(url);
      URLConnection urlConnection = url.openConnection();
      urlConnection.setDoInput(true);
      InputStream in = urlConnection.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(in, "gbk"));
      String line;
      int count = 2;
      while (((line = br.readLine()) != null) && count > 0) {
        String[] res = line.split("\\|");
        if (res[1].equals("syn") && (res[0].equals("noun"))) {
          strResult.add(res[2]);
          count--;
        }
        if (res[1].equals("syn") && res[0].equals("verb")) {
          System.out.println("@@@@@@@@@@@"+res[2]);
          System.out.println("$$$$$$$$$$$"+PorterStemmerTokenizerFactory.stem(res[2]));
          strResult.add(PorterStemmerTokenizerFactory.stem(res[2]));
          count--;
        }
      }
    } catch (IOException e) {
    }
    return strResult;

  }

}
