package edu.cmu.lti.oaqa.openqa.test.team15.retrieval;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class FuzzyVariantBioSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SolrWrapper wrapper;

  private static Logger logger = Logger.getLogger(FuzzyVariantBioSolrRetrievalStrategist.class);

  private HttpClient httpclient;

  private javax.xml.parsers.SAXParser sp;

  private String queryServerRootUrl;

  private String queryServerPath;

  private String format;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      this.hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
    DOMConfigurator.configure("configuration/log4j.xml");
    SAXParserFactory spf = SAXParserFactory.newInstance();
    httpclient = new DefaultHttpClient();
    try {
      sp = spf.newSAXParser();
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
    queryServerRootUrl = (String) aContext.getConfigParameterValue("query-server-root-url");
    queryServerPath = (String) aContext.getConfigParameterValue("query-server-path");
    format = (String) aContext.getConfigParameterValue("format");

  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    List<String> keytermStrList = Lists.transform(keyterms, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });
    List<String> expandedKeyterms = expandKeyterms(keytermStrList);
    String query = formulateQuery(expandedKeyterms);

    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        logger.debug(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      logger.error("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  private List<String> expandKeyterms(List<String> keytermStrList) {
    Set<String> expandedKeyterms = new HashSet<String>();
    HttpGet httpBasicGet = null;
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    for (String keyterm : keytermStrList) {
      expandedKeyterms.add(keyterm);
      try {
        URI uri = getQueryUrl(keyterm);
        httpBasicGet = new HttpGet(uri);
        String responseBody = httpclient.execute(httpBasicGet, responseHandler);
        sp.parse(new InputSource(new StringReader(responseBody)), new BasicQueryResultXmlHandler(
                expandedKeyterms));
      } catch (Exception e) {
        logger.error("", e);
      }
    }
    return new LinkedList<String>(expandedKeyterms);
  }

  private URI getQueryUrl(String name) throws URISyntaxException {
    List<NameValuePair> formParams = new LinkedList<NameValuePair>();
    formParams.add(new BasicNameValuePair("format", format));
    formParams.add(new BasicNameValuePair("global_textfield", name));
    URI uri = URIUtils.createURI("http", queryServerRootUrl, -1, queryServerPath,
            URLEncodedUtils.format(formParams, "UTF-8"), null);
    return uri;
  }

  private String formulateQuery(List<String> keytermStrList) {
    StringBuilder sb = new StringBuilder();
    for (String keyterm : keytermStrList) {
      sb.append(keyterm + "~ ");
    }
    String query = sb.toString();
    logger.debug("Fuzzy QUERY: " + query);
    return query;
  }

  private class BasicQueryResultXmlHandler extends DefaultHandler {
    private Set<String> keytermList;

    private String tempVal;

    private String tempTagName;

    private BasicQueryResultXmlHandler(Set<String> expandedKeyterms) {
      keytermList = expandedKeyterms;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      tempVal = new String(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
      tempTagName = qName;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      if (tempTagName.equals("variant_name")) {
        keytermList.add(tempVal);
      }
    }
  }

}
