package com.phpsysinfo.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PSIXmlParse extends DefaultHandler {

	private PSIHostData entry;

	private boolean inPluginImpi = false;
	private boolean inPluginImpiTemperature = false;
	private boolean inPluginImpiVoltage = false;

	private boolean inMbInfo = false;
	private boolean inMbInfoTemperature = false;
	private boolean inMbInfoFans = false;

	private boolean inPsStatus = false;

	private boolean inDisk = false;
	private String currentDisk = "";

	private boolean inPackageUpdate = false;
	private boolean inSecurityUpdate = false;

	private boolean inPrinter = false;
	private PSIPrinter currentPrinter = null;

	private PSIRaid currentRaid = null;

	private StringBuilder buffer = new StringBuilder();

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

			try {
				int cpuLoad = (int) Double.parseDouble(attributes.getValue("CPULoad"));
				this.entry.setCpuUsage(cpuLoad);
			}
			catch(Exception e) {}

			try {
				int processes = (int) Integer.parseInt(attributes.getValue("Processes"));
				this.entry.setProcesses(processes);
			}
			catch(Exception e) {}

			try {
				int processesRunning = (int) Integer.parseInt(attributes.getValue("ProcessesRunning"));
				this.entry.setProcessesRunning(processesRunning);
			}
			catch(Exception e) {}

			try {
				int processesSleeping = (int) Integer.parseInt(attributes.getValue("ProcessesSleeping"));
				this.entry.setProcessesSleeping(processesSleeping);
			}
			catch(Exception e) {}

			try {
				int processesStopped = (int) Integer.parseInt(attributes.getValue("ProcessesStopped"));
				this.entry.setProcessesStopped(processesStopped);
			}
			catch(Exception e) {}

			try {
				int processesZombie = (int) Integer.parseInt(attributes.getValue("ProcessesZombie"));
				this.entry.setProcessesZombie(processesZombie);
			}
			catch(Exception e) {}

			try {
				int processesWaiting = (int) Integer.parseInt(attributes.getValue("ProcessesWaiting"));
				this.entry.setProcessesWaiting(processesWaiting);
			}
			catch(Exception e) {}	
			
			try {
				int processesOther = (int) Integer.parseInt(attributes.getValue("ProcessesOther"));
				this.entry.setProcessesOther(processesOther);
			}
			catch(Exception e) {}	
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
			this.entry.addCpuCore();
		}
		else if(localName.equalsIgnoreCase("NetDevice")){
			this.entry.addNetworkInterface(
					attributes.getValue("Name"),
					attributes.getValue("RxBytes"),
					attributes.getValue("TxBytes"),
					attributes.getValue("Err"),
					attributes.getValue("Drops"));
		}

		//ipmi
		else if (localName.equalsIgnoreCase("Plugin_ipmi") || localName.equalsIgnoreCase("Plugin_ipmiinfo")){
			inPluginImpi = true;
		}
		else if ((inPluginImpi && localName.equalsIgnoreCase("Temperature")) || inPluginImpi && localName.equalsIgnoreCase("Temperatures")){
			inPluginImpiTemperature = true;
		}
		else if ((inPluginImpi && localName.equalsIgnoreCase("Voltages"))){
			inPluginImpiVoltage = true;
		}
		else if (inPluginImpiTemperature){
			if(localName.equalsIgnoreCase("Item")) {
				String desc = attributes.getValue("Label");
				String temp = attributes.getValue("Value");
				String max = attributes.getValue("Max");
				this.entry.addTemperature(desc, temp, max);
			}
		}
		else if (inPluginImpiVoltage){
			if(localName.equalsIgnoreCase("Item")) {
				String desc = attributes.getValue("Label");
				String value = attributes.getValue("Value");
				this.entry.addVoltage(desc, value);
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
				String desc = attributes.getValue("Label");
				String temp = attributes.getValue("Value");
				String max = attributes.getValue("Max");
				this.entry.addTemperature(desc, temp, max);
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
		else if (localName.equals("disk")){
			inDisk = true;
			currentDisk = attributes.getValue("name");
		}

		else if (localName.equalsIgnoreCase("Printer")){
			inPrinter = true;
			String pname = attributes.getValue("Name");
			currentPrinter = new PSIPrinter(pname);
			this.entry.addPrinter(currentPrinter);
		}

		else if (inPrinter){
			if (localName.equalsIgnoreCase("MarkerSupplies")) {
				String description = attributes.getValue("Description");
				String supplyUnit = attributes.getValue("SupplyUnit");
				String maxCapacity = attributes.getValue("MaxCapacity");
				String level = attributes.getValue("Level");

				PSIPrinterItem ppi = new PSIPrinterItem(description, supplyUnit, maxCapacity, level);

				if(currentPrinter != null) {
					currentPrinter.addItem(ppi);
				}
			}
			if (localName.equalsIgnoreCase("PrinterMessage")) {
				String message = attributes.getValue("Message");
				if(currentPrinter != null) {
					currentPrinter.addMessages(message);
				}
			}
		}

		else if (inDisk){
			if (localName.equalsIgnoreCase("attribute")) {
				String attr = attributes.getValue("attribute_name");
				String value = attributes.getValue("raw_value");

				this.entry.addSmart(new PSISmart(currentDisk, attr, value));
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

			this.entry.addUps(ups);
		}

		else if(localName.equalsIgnoreCase("Raid")){

			currentRaid = new PSIRaid();
			currentRaid.setName(attributes.getValue("Device_Name"));
			currentRaid.setLevel(attributes.getValue("Level"));

			String active = attributes.getValue("Disks_Active");
			if(active != null && !active.equals("")) {
				currentRaid.setDisksActive(Integer.parseInt(active));
			}
			String registered = attributes.getValue("Disks_Registered");
			if(registered != null && !registered.equals("")) {
				currentRaid.setDisksRegistered(Integer.parseInt(registered));
			}

			this.entry.addRaid(currentRaid);
		}
		else if(localName.equals("Disk")){
			PSIRaidDevice psiRaidDevice = new PSIRaidDevice(
					attributes.getValue("Name"),
					attributes.getValue("Status"));
			currentRaid.addDevices(psiRaidDevice);
		}
		else if(localName.equalsIgnoreCase("packages")){
			inPackageUpdate = true;
			buffer = new StringBuilder();
		}

		else if(localName.equalsIgnoreCase("security")){
			inSecurityUpdate = true;
			buffer = new StringBuilder();
		}
		else if(localName.equalsIgnoreCase("Bat")){
			String dc = attributes.getValue("DesignCapacity");
			String rc = attributes.getValue("RemainingCapacity");
			String c = attributes.getValue("Capacity");
			String cs = attributes.getValue("ChargingState");

			PSIBat bat = new PSIBat(dc, rc, c, cs);
			this.entry.setBat(bat);
		}
		else if (localName.equalsIgnoreCase("Hardware")){
			this.entry.setMachine(attributes.getValue("Name"));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);

		if(inPackageUpdate) {
			buffer.append(ch, start, length);
		}

		if(inSecurityUpdate) {
            buffer.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (localName.equalsIgnoreCase("Plugin_ipmi") || localName.equalsIgnoreCase("Plugin_ipmiinfo")){
			inPluginImpi = false;
		}
		else if(localName.equalsIgnoreCase("MBInfo")) {
			inMbInfo = false;
		}
		else if(localName.equalsIgnoreCase("Temperature") || localName.equalsIgnoreCase("Temperatures")){
			inPluginImpiTemperature = false;
			inMbInfoTemperature = false;
		}
		else if(localName.equalsIgnoreCase("Voltages")){
			inPluginImpiVoltage = false;
		}
		else if(localName.equalsIgnoreCase("Fans")){
			inMbInfoFans = false;
		} else if (localName.equalsIgnoreCase("Plugin_PSStatus")){
			inPsStatus = false;
		}
		else if(localName.equals("disk")){
			inDisk = false;
		}
		else if(localName.equalsIgnoreCase("Printer")){
			inPrinter = false;
		}
		else if(localName.equalsIgnoreCase("packages")){
			inPackageUpdate = false;
			try {
				entry.setNormalUpdate(Integer.parseInt(buffer.toString().trim()));
			}
			catch (Exception e) {
				entry.setNormalUpdate(-1);
			}
		}
		else if(localName.equalsIgnoreCase("security")){
			inSecurityUpdate = false;
			try {
				entry.setSecurityUpdate(Integer.parseInt(buffer.toString().trim()));
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

