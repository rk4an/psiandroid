package com.phpsysinfo.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PSIHostData {

	private String hostname = "";
	private String uptime = "";
	private String loadAvg = "";
	private String kernel = "";
	private String distro = "";
	private String distroIcon = "";
	private String Ip = "";
	private String psiVersion = "";
	private String cpu = "";

	private List<PSIMountPoint> mountPoint = new ArrayList<PSIMountPoint>();
	private int appMemoryPercent = 0;
	private int appMemoryUsed= 0;
	private int appMemoryTotal = 0;
	private int appMemoryFullPercent = 0;
	
	private HashMap<String,String> temperature = new HashMap<String,String>();
	private HashMap<String,String> fans = new HashMap<String,String>();
	
	private List<PSINetworkInterface> networkInterface = new ArrayList<PSINetworkInterface>();
	
	private HashMap<String,String> processStatus = new HashMap<String,String>();

	private HashMap<String,String> smart = new HashMap<String,String>();

	private List<PSIRaid> raid = new ArrayList<PSIRaid>();
	
	private PSIUps ups = null;
	
	private int normalUpdate = -1;
	private int securityUpdate = -1;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		int up = (int)Double.parseDouble(uptime);
		int min = up/60;
		int hours = min/60;
		int days = (int) Math.floor(hours/24);
		hours = (int) Math.floor(hours - (days * 24));
		min = (int) Math.floor(min - (days * 60 * 24) - (hours * 60));

		this.uptime = new String(days+"d "+hours+"h "+min + "m");
	}

	public void setAppMemoryPercent(String value) {
		if(value != null) {
			this.appMemoryPercent = Integer.parseInt(value);
		}
	}

	public int getAppMemoryPercent() {
		return appMemoryPercent;
	}

	public void setAppMemoryFullPercent(String value) {
		if(value != null) {
			this.appMemoryFullPercent = Integer.parseInt(value);
		}
	}

	public int getAppMemoryFullPercent() {
		return appMemoryFullPercent;
	}	
	
	public String getLoadAvg() {
		return loadAvg;
	}

	public void setLoadAvg(String loadAvg) {
		this.loadAvg = loadAvg;
	}

	public String getKernel() {
		return kernel;
	}

	public void setKernel(String kernel) {
		this.kernel = kernel;
	}

	public String getDistro() {
		return distro;
	}
	public void setDistro(String distro) {
		this.distro = distro;
	}

	public String getDistroIcon() {
		return distroIcon;
	}

	public void setDistroIcon(String distroIcon) {
		this.distroIcon = distroIcon;
	}	
	
	public String getIp() {
		return Ip;
	}

	public void setIp(String ip) {
		Ip = ip;
	}

	public String getPsiVersion() {
		return psiVersion;
	}

	public void setPsiVersion(String psiVersion) {
		this.psiVersion = psiVersion;
	}
	
	public String getCpu() {
		return cpu;
	}

	public void setCpu(String processor) {
		this.cpu = processor;
	}
	
	public void addMountPoint(String name, String percentUsed, String used, String total) {
		int _percentUsed = Integer.parseInt(percentUsed);
		int _used = (int) (Long.parseLong(used)/1024/1024);
		int _total = (int) (Long.parseLong(total)/1024/1024);
		mountPoint.add(new PSIMountPoint(name, _percentUsed, _used, _total));
	}

	public List<PSIMountPoint> getMountPoint() {
		return mountPoint;
	}

	public int getAppMemoryUsed() {
		return appMemoryUsed;
	}

	public void setAppMemoryUsed(String appMemoryUsed) {
		if(appMemoryUsed != null) {
			this.appMemoryUsed = (int) (Long.parseLong(appMemoryUsed)/1024/1024);
		}
	}

	public int getAppMemoryTotal() {
		return appMemoryTotal;
	}

	public void setAppMemoryTotal(String appMemoryTotal) {
		this.appMemoryTotal = (int) (Long.parseLong(appMemoryTotal)/1024/1024);
	}
	
	public void addTemperature(String label, String value) {
		temperature.put(label, value);
	}
	
	public HashMap<String, String> getTemperature() {
		return temperature;
	}
	
	public void addFans(String label, String value) {
		fans.put(label, value);
	}
	
	public HashMap<String, String> getFans() {
		return fans;
	}
	
	public List<PSINetworkInterface> getNetworkInterface() {
		return networkInterface;
	}

	public void addNetworkInterface(String name, String rxBytes, String txBytes) {
		int _rxBytes = 0;
		int _txBytes = 0;
		
		if(rxBytes != null && !rxBytes.equals("")) {
			_rxBytes = (int) (Long.parseLong(rxBytes)/1024/1024);
		}
			
		if(txBytes != null && !txBytes.equals("")) {
			_txBytes =  (int) (Long.parseLong(txBytes)/1024/1024);
		}
		
		networkInterface.add(new PSINetworkInterface(name, _rxBytes, _txBytes));
	}
	
	public void addProcessStatus(String label, String value) {
		processStatus.put(label, value);
	}
	
	public HashMap<String, String> getProcessStatus() {
		return processStatus;
	}	
	
	public void addSmart(String attr, String value) {
		smart.put(attr, value);
	}
	
	public HashMap<String, String> getSmart() {
		return smart;
	}
	
	public PSIUps getUps() {
		return ups;
	}

	public void setUps(PSIUps ups) {
		this.ups = ups;
	}
	
	
	public List<PSIRaid> getRaid() {
		return raid;
	}

	public void addRaid(String name, String active, String registered) {
		int _active = 0;
		int _registered = 0;
		
		if(active != null && !active.equals("")) {
			_active = Integer.parseInt(active);
		}
			
		if(registered != null && !registered.equals("")) {
			_registered =  Integer.parseInt(registered);
		}
		
		raid.add(new PSIRaid(name,_active,_registered));
	}
	
	public int getNormalUpdate() {
		return normalUpdate;
	}

	public void setNormalUpdate(int normalUpdate) {
		this.normalUpdate = normalUpdate;
	}

	public int getSecurityUpdate() {
		return securityUpdate;
	}

	public void setSecurityUpdate(int securityUpdate) {
		this.securityUpdate = securityUpdate;
	}
	
}
