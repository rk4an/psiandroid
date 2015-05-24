package com.phpsysinfo.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.xml.sax.helpers.DefaultHandler;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.phpsysinfo.activity.PSIActivity;
import com.phpsysinfo.activity.PSIConfig;

public class PSIDownloadData
extends AsyncTask<String, Void, Void>
{
	private PSIErrorCode errorCode = PSIErrorCode.NO_ERROR;
	private String errorMessage = "";
	private PSIActivity activity;
	private PSIHostData psiObject;
	private String address = "";
	private String alias = "";
	private Boolean ignoreCert = false;
	private static AndroidHttpClient httpClient = null;
	private boolean canceled = false;

	public PSIDownloadData(PSIActivity psiaa) {
		super();
		this.activity = psiaa;
	}

	@Override
	protected Void doInBackground(String... strs) {
		address = strs[0];
		String user = strs[1];
		String password = strs[2];
		alias = strs[3];
		ignoreCert = Boolean.parseBoolean(strs[4]);
		
		SAXParser parser = null;
		InputStream input = null;

		try {
			input = getUrl(address,user,password, ignoreCert);
		}
		catch (SSLHandshakeException e) {
			Log.d("PSIAndroid", "SSL_ERROR");
			errorCode = PSIErrorCode.SSL_ERROR;
			errorMessage = e.getMessage();
			httpClient.close();
			return null;
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "BAD_URL");
			errorCode = PSIErrorCode.BAD_URL;
			errorMessage = e.getMessage();
			httpClient.close();
			return null;
		}

		if(canceled) {
			Log.d("PSIAndroid","Was canceled!");
			errorCode = PSIErrorCode.CANCELED;
			httpClient.close();
			return null;
		}

		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "XML_PARSER_CREATE");
			errorCode = PSIErrorCode.XML_PARSER_CREATE;
			errorMessage = e.getMessage();
			httpClient.close();
			return null;
		}

		DefaultHandler handler = new PSIXmlParse();
		try {
			if(input == null) {
				Log.d("PSIAndroid", "CANNOT_GET_XML");
				errorCode = PSIErrorCode.CANNOT_GET_XML;
				errorMessage = "no xml data received";
				httpClient.close();
				return null;
			}
			else {
				parser.parse(input, handler);
				psiObject = ((PSIXmlParse) handler).getData();
			}
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "XML_PARSER_ERROR");
			e.printStackTrace();
			errorCode = PSIErrorCode.XML_PARSER_ERROR;
			errorMessage = e.getMessage();
			httpClient.close();
			return null;
		}

		httpClient.close();

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		this.activity.completeRefresh();
		if (this.errorCode.equals(PSIErrorCode.NO_ERROR)) {
			this.activity.displayInfo(psiObject);
		}
		else if (this.errorCode.equals(PSIErrorCode.CANCELED)) {
			//nothing
		}
		else {
			this.activity.displayError(alias, errorCode, errorMessage);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	public void stop() {
		canceled = true;
	}

	private static InputStream getUrl(String url, String user, String password, Boolean ignoreCert)
			throws MalformedURLException, IOException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
			{

		//user agent
		httpClient = AndroidHttpClient.newInstance("PSIAndroid");

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), PSIConfig.TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), PSIConfig.TIMEOUT);

		URL urlObj = new URL(url);
		HttpHost host = new HttpHost(urlObj.getHost(), urlObj.getPort(), urlObj.getProtocol());
		AuthScope scope = new AuthScope(urlObj.getHost(), urlObj.getPort());

		//ssl
		if (urlObj.getProtocol().toLowerCase().equals("https")) {

			if(ignoreCert) {
				X509TrustManager tm = new X509TrustManager() { 
					public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
					}

					public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				};
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(null, new TrustManager[]{tm}, null);
				SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
				ClientConnectionManager ccm = httpClient.getConnectionManager();
				SchemeRegistry sr = ccm.getSchemeRegistry();
				sr.register(new Scheme("https", ssf, 443));
			}
			else {
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			}

		}

		//credentials
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(user, password);
		CredentialsProvider cp = new BasicCredentialsProvider();
		cp.setCredentials(scope, creds);
		HttpContext credContext = new BasicHttpContext();
		credContext.setAttribute(ClientContext.CREDS_PROVIDER, cp);

		//get request
		HttpGet job = new HttpGet(url);
		HttpResponse response = httpClient.execute(host,job,credContext);
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		StatusLine status = response.getStatusLine();

		if(status.getStatusCode() == 200) {
			return instream;
		}
		else {
			return null;
		}

			}


}
