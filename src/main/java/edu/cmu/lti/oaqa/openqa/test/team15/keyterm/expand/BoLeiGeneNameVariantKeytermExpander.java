package edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand;

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
import org.apache.uima.UimaContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BoLeiGeneNameVariantKeytermExpander extends KeytermExpander {

	private static Logger logger = Logger
			.getLogger(BoLeiGeneNameVariantKeytermExpander.class);

	private HttpClient httpclient;

	private javax.xml.parsers.SAXParser sp;

	private String queryServerRootUrl;

	private String queryServerPath;

	private String format;

	public BoLeiGeneNameVariantKeytermExpander(UimaContext c) {
		super(c);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		httpclient = new DefaultHttpClient();
		try {
			sp = spf.newSAXParser();
		} catch (Exception e) {
			logger.error("", e);
		}
		queryServerRootUrl = (String) context
				.getConfigParameterValue("query-server-root-url");
		queryServerPath = (String) context
				.getConfigParameterValue("query-server-path");
		format = (String) context.getConfigParameterValue("format");
	}

	@Override
	public List<String> expandKeyterm(String keyterm, String pos) {
		Set<String> expandedKeyterms = new HashSet<String>();
		HttpGet httpBasicGet = null;
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		expandedKeyterms.add(keyterm);
		try {
			URI uri = getQueryUrl(keyterm);
			httpBasicGet = new HttpGet(uri);
			String responseBody = httpclient.execute(httpBasicGet,
					responseHandler);
			sp.parse(new InputSource(new StringReader(responseBody)),
					new BasicQueryResultXmlHandler(expandedKeyterms));
		} catch (Exception e) {
			logger.error("", e);
		}
		return new ArrayList<String>(expandedKeyterms);
	}

	private URI getQueryUrl(String name) throws URISyntaxException {
		List<NameValuePair> formParams = new LinkedList<NameValuePair>();
		formParams.add(new BasicNameValuePair("format", format));
		formParams.add(new BasicNameValuePair("global_textfield", name));
		URI uri = URIUtils.createURI("http", queryServerRootUrl, -1,
				queryServerPath, URLEncodedUtils.format(formParams, "UTF-8"),
				null);
		return uri;
	}

	private class BasicQueryResultXmlHandler extends DefaultHandler {
		private Set<String> keytermList;

		private String tempVal;

		private String tempTagName;

		private BasicQueryResultXmlHandler(Set<String> expandedKeyterms) {
			keytermList = expandedKeyterms;
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			tempVal = new String(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			tempTagName = qName;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (tempTagName.equals("variant_name")) {
				keytermList.add(tempVal);
			}
		}
	}
}
