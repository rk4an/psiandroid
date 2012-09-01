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
	private String Ip = "";
	private String psiVersion = "";
	private String cpu = "";

	private List<PSIMountPoint> mountPoint = new ArrayList<PSIMountPoint>();
	private int appMemoryPercent = 0;
	private int appMemoryUsed= 0;
	private int appMemoryTotal = 0;
	
	private HashMap<String,String> temperature = new HashMap<String,String>();

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
}
