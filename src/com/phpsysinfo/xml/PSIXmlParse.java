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
	private boolean inMbInfoFans = false;

	private boolean inPsStatus = false;
	
	private boolean inDisk = false;
	private int numDisk = 0;

	private boolean inPackageUpdate = false;
	private boolean inSecurityUpdate = false;
	private StringBuffer buffer = new StringBuffer();
	
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
			this.entry.setUsers(attributes.getValue("Users"));
			this.entry.setDistroIcon(attributes.getValue("Distroicon"));
		}
		else if (localName.equalsIgnoreCase("Memory")){
			this.entry.setAppMemoryTotal(attributes.getValue("Total"));
			this.entry.setAppMemoryPercent(attributes.getValue("Percent"));
			this.entry.setAppMemoryFullPercent(attributes.getValue("Percent"));
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
		else if(localName.equalsIgnoreCase("NetDevice")){
			this.entry.addNetworkInterface(
					attributes.getValue("Name"),
					attributes.getValue("RxBytes"),
					attributes.getValue("TxBytes"));
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
		else if (inMbInfo && localName.equalsIgnoreCase("Fans")){
			inMbInfoFans = true;
		}
		else if (inMbInfoTemperature){
			if(localName.equalsIgnoreCase("Item")) {
				this.entry.addTemperature(attributes.getValue("Label"), attributes.getValue("Value"));
			}
		}
		else if (inMbInfoFans){
			if(localName.equalsIgnoreCase("Item")) {
				this.entry.addFans(attributes.getValue("Label"), attributes.getValue("Value"));
			}
		}

		//process status
		else if (localName.equalsIgnoreCase("Plugin_PSStatus")){
			inPsStatus = true;
		}
		else if (inPsStatus){
			if(localName.equalsIgnoreCase("Process")) {
				this.entry.addProcessStatus(attributes.getValue("Name"), attributes.getValue("Status"));
			}
		}
		
		//smart
		else if (localName.equalsIgnoreCase("disk")){
			inDisk = true;
			numDisk++;
			//attributes.getValue("name");
		}
		
		else if (inDisk){
			if (localName.equalsIgnoreCase("attribute")) {
				String attr = attributes.getValue("attribute_name");
				String value = attributes.getValue("raw_value");
				this.entry.addSmart(numDisk +" " + attr, value);
			}
		}

		else if(localName.equalsIgnoreCase("UPS")){

			PSIUps ups = new PSIUps();

			ups.setName(attributes.getValue("Name"));
			ups.setModel(attributes.getValue("Model"));
			ups.setMode(attributes.getValue("Mode"));
			ups.setStartTime(attributes.getValue("StartTime"));
			ups.setStatus(attributes.getValue("Status"));
			ups.setTemperature(attributes.getValue("Temperature"));
			ups.setOutagesCount(attributes.getValue("OutagesCount"));
			ups.setLastOutage(attributes.getValue("LastOutage"));
			ups.setLastOutageFinish(attributes.getValue("LastOutageFinish"));
			ups.setLineVoltage(attributes.getValue("LineVoltage"));
			ups.setLoadPercent(attributes.getValue("LoadPercent"));
			ups.setBatteryVoltage(attributes.getValue("BatteryVoltage"));
			ups.setBatteryChargePercent(attributes.getValue("BatteryChargePercent"));
			ups.setTimeLeftMinutes(attributes.getValue("TimeLeftMinutes"));

			this.entry.setUps(ups);
		}
		
		else if(localName.equalsIgnoreCase("Raid")){
			this.entry.addRaid(
					attributes.getValue("Device_Name") + " ("  + attributes.getValue("Level") + ")",
					attributes.getValue("Disks_Active"),
					attributes.getValue("Disks_Registered"));
		}
		
		else if(localName.equalsIgnoreCase("packages")){
			inPackageUpdate = true;
			buffer = new StringBuffer();
		}
		
		else if(localName.equalsIgnoreCase("security")){
			inSecurityUpdate = true;
			buffer = new StringBuffer();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		
		if(inPackageUpdate) {
			buffer.append(ch);
		}
		
		if(inSecurityUpdate) {
			buffer.append(ch);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase("Plugin_ipmi")){
			inPluginImpi = false;
		}
		else if(localName.equalsIgnoreCase("MBInfo")) {
			inMbInfo = false;
		}
		else if(localName.equalsIgnoreCase("Temperature")){
			inPluginImpiTemperature = false;
			inMbInfoTemperature = false;
		}
		else if(localName.equalsIgnoreCase("Fans")){
			inMbInfoFans = false;
		}
		else if(localName.equalsIgnoreCase("Plugin_PSStatus")){
			inPsStatus = false;
		}
		else if(localName.equalsIgnoreCase("disk")){
			inDisk = false;
		}
		else if(localName.equalsIgnoreCase("packages")){
			inPackageUpdate = false;
			try {
				entry.setNormalUpdate(Integer.parseInt(buffer.toString()));
			}
			catch(Exception e) {
				entry.setNormalUpdate(-1);
			}
		}
		else if(localName.equalsIgnoreCase("security")){
			inSecurityUpdate = false;
			try {
				entry.setSecurityUpdate(Integer.parseInt(buffer.toString()));
			}
			catch(Exception e) {
				entry.setSecurityUpdate(-1);
			}
		}
	}

	public PSIHostData getData(){
		return entry;
	}
}

