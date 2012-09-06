package com.phpsysinfo.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PSIXmlParse extends DefaultHandler {

	private PSIHostData entry;

	private boolean inPluginImpi = false;
	private boolean inPluginImpiTemperature = false;

	private boolean inMbInfo = false;
	private boolean inMbInfoTemperature = false;

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

			//		 	/home
			String mountPointName = attributes.getValue("MountPoint");
			
			// 			/dev/sda5
			String mountPointPath = attributes.getValue("Name");		

			//if PSI_SHOW_MOUNT_POINT set to false
			if(mountPointName == null) { 
				mountPointName = mountPointPath;
			}

			//display "SWAP"
			if(mountPointPath != null) {
				if(mountPointPath.equals("SWAP")) {
					mountPointName = "SWAP";
				}
			}

			this.entry.addMountPoint(mountPointName, 
					attributes.getValue("Percent"), 
					attributes.getValue("Used"), 
					attributes.getValue("Total"));
		}
		else if (localName.equalsIgnoreCase("Generation")){
			this.entry.setPsiVersion(attributes.getValue("version"));
		}
		else if (localName.equalsIgnoreCase("CpuCore")){
			this.entry.setCpu(attributes.getValue("Model"));
		}

		//ipmi
		else if (localName.equalsIgnoreCase("Plugin_ipmi")){
			inPluginImpi = true;
		}
		else if (inPluginImpi && localName.equalsIgnoreCase("Temperature")){
			inPluginImpiTemperature = true;
		}
		else if (inPluginImpiTemperature){
			if(localName.equalsIgnoreCase("Item")) {
				this.entry.addTemperature(attributes.getValue("Label"), attributes.getValue("Value"));
			}
		}

		//mb
		else if (localName.equalsIgnoreCase("MBInfo")){
			inMbInfo = true;
		}
		else if (inMbInfo && localName.equalsIgnoreCase("Temperature")){
			inMbInfoTemperature = true;
		}
		else if (inMbInfoTemperature){
			if(localName.equalsIgnoreCase("Item")) {
				this.entry.addTemperature(attributes.getValue("Label"), attributes.getValue("Value"));
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase("Plugin_ipmi")){
			inPluginImpi = false;
			inMbInfo = false;
		}
		else if(localName.equalsIgnoreCase("Temperature")){
			inPluginImpiTemperature = false;
			inMbInfoTemperature = false;
		}
	}

	public PSIHostData getData(){
		return entry;
	}
}

