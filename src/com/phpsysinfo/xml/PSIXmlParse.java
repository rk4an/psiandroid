package com.phpsysinfo.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PSIXmlParse extends DefaultHandler {

	private PSIHostData entry;

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		super.processingInstruction(target, data);
	}
	public PSIXmlParse() {
		super();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		entry = new PSIHostData();
	}

	@Override
	public void startElement(String uri, String localName, String name,	Attributes attributes) 
			throws SAXException {

		if (localName.equalsIgnoreCase("Vitals")){
			this.entry.setHostname(attributes.getValue("Hostname"));
			this.entry.setUptime(attributes.getValue("Uptime"));
			this.entry.setLoadAvg(attributes.getValue("LoadAvg"));
			this.entry.setKernel(attributes.getValue("Kernel"));
			this.entry.setDistro(attributes.getValue("Distro"));
			this.entry.setIp(attributes.getValue("IPAddr"));
		}
		else if (localName.equalsIgnoreCase("Memory")){
			this.entry.setAppMemoryTotal(attributes.getValue("Total"));
			this.entry.setAppMemoryPercent(attributes.getValue("Percent"));
			this.entry.setAppMemoryUsed(attributes.getValue("Used"));
		}
		else if (localName.equalsIgnoreCase("Details")){
			this.entry.setAppMemoryPercent(attributes.getValue("AppPercent"));
			this.entry.setAppMemoryUsed(attributes.getValue("App"));
		}
		else if(localName.equalsIgnoreCase("Mount")){
			this.entry.addMountPoint(attributes.getValue("MountPoint"), 
					attributes.getValue("Percent"), 
					attributes.getValue("Used"), 
					attributes.getValue("Total"));
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {

	}

	public PSIHostData getData(){
		return entry;
	}
}

