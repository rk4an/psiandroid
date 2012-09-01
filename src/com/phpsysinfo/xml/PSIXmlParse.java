package com.phpsysinfo.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PSIXmlParse extends DefaultHandler {

	private PSIHostData entry;

	private boolean inPluginImpi = false;
	private boolean inPluginImpiTemperature = false;
	
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
		else if (localName.equalsIgnoreCase("Generation")){
			this.entry.setPsiVersion(attributes.getValue("version"));
		}
		else if (localName.equalsIgnoreCase("Plugin_ipmi")){
			inPluginImpi = true;
		}
		else if (inPluginImpi && localName.equalsIgnoreCase("Temperature")){
			inPluginImpiTemperature = true;
		}
		else if (inPluginImpiTemperature){
			if(localName.equalsIgnoreCase("Item")) {
				this.entry.addImpi(attributes.getValue("Label"), attributes.getValue("Value"));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase("Plugin_ipmi")){
			inPluginImpi = false;
		}
		else if(localName.equalsIgnoreCase("Temperature")){
			inPluginImpiTemperature = false;
		}
	}

	public PSIHostData getData(){
		return entry;
	}
}

