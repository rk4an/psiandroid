package com.phpsysinfo.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import android.os.AsyncTask;
import android.util.Log;

import com.phpsysinfo.activity.PSIActivity;

public class PSIDownloadData
extends AsyncTask<String, Void, Void>
{
	private PSIErrorCode errorCode = PSIErrorCode.NO_ERROR;
	private PSIActivity activity;
	private PSIHostData psiObject;

	public PSIDownloadData(PSIActivity psiaa) {
		super();
		this.activity = psiaa;
	}

	@Override
	protected Void doInBackground(String... strs) {
		String address = strs[0];
		SAXParser parser = null;
		InputStream input = null;
		try {
			input = getUrl(address);
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "Url error", e);
			errorCode = PSIErrorCode.BAD_URL;
			return null;
		}

		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "Error while creating xml parser", e);
			errorCode = PSIErrorCode.XML_PARSER_CREATE;
			return null;
		}

		DefaultHandler handler = new PSIXmlParse();
		try {
			if(input == null) {
				Log.d("PSIAndroid", "Cannot retrieve xml file");
				errorCode = PSIErrorCode.CANNOT_GET_XML;
				return null;
			}
			else {
				parser.parse(input, handler);
				psiObject = ((PSIXmlParse) handler).getData();
			}
		}
		catch (Exception e) {
			Log.d("PSIAndroid", "Error while parsing xml", e);
			errorCode = PSIErrorCode.XML_PARSER_ERROR;
			return null;
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		if (this.errorCode.equals(PSIErrorCode.NO_ERROR))
			this.activity.displayInfo(psiObject);
		else
			this.activity.displayError(errorCode);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.disableButton();
	}

	private static InputStream getUrl(String url)
			throws MalformedURLException, IOException
			{
		int code = 0;
		URL u = new URL(url); 
		HttpURLConnection huc = (HttpURLConnection) u.openConnection(); 
		huc.setRequestMethod("GET");
		huc.connect();
		code = huc.getResponseCode();

		if (code == 200)
			return huc.getInputStream();
		else
			return null;
			}
}
