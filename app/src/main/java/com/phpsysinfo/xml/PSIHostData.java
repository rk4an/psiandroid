package com.phpsysinfo.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

public class PSIHostData {

	private String machine = "";
	private String hostname = "";
	private String uptime = "";
	private String loadAvg = "";
	private String kernel = "";
	private String distro = "";
	private String distroIcon = "";
	private String Ip = "";
	private String psiVersion = "";
	private String cpu = "";
	private int cpuCore = 0;
	private int cpuUsage = -1;
	private String users = "";
	private int processes = -1;
	private int processesRunning = -1;
	private int processesSleeping = -1;
	private int processesStopped = -1;
	private int processesZombie = -1;
	private int processesWaiting = -1;
	private int processesOther = -1;

	private List<PSIMountPoint> mountPoint = new ArrayList<PSIMountPoint>();
	private int appMemoryPercent = 0;
	private int appMemoryUsed= 0;
	private int appMemoryTotal = 0;
	private int appMemoryFullPercent = 0;

	private List<PSITemperature> temperature = new ArrayList<PSITemperature>();
	private List<PSIVoltage> voltages = new ArrayList<PSIVoltage>();
	private HashMap<String,String> fans = new HashMap<String,String>();

	private List<PSINetworkInterface> networkInterface = new ArrayList<PSINetworkInterface>();

	private HashMap<String,String> processStatus = new HashMap<String,String>();

	private List<PSISmart> smart = new ArrayList<PSISmart>();

	private List<PSIRaid> raid = new ArrayList<PSIRaid>();

	private List<PSIUps> ups = new ArrayList<PSIUps>();

	private List<PSIPrinter> printer = new ArrayList<PSIPrinter>();

	private PSIBat bat = null;

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
		int up = 0;
		try {
			up = (int)Double.parseDouble(uptime);
			int min = up/60;
			int hours = min/60;
			int days = (int) Math.floor(hours/24);
			hours = (int) Math.floor(hours - (days * 24));
			min = (int) Math.floor(min - (days * 60 * 24) - (hours * 60));

			this.uptime = new String(days+"d "+hours+"h "+min + "m");
		}
		catch(Exception e) {
			Log.d("PSIAndroid","setUptime failed");
		}
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

	public void addCpuCore() {
		this.cpuCore++;
	}

	public int getCpuCore() {
		return this.cpuCore;
	}

	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}	

	public void addMountPoint(String name, String percentUsed, String used, String total) {
		int _percentUsed = 0;
		int _used = 0;
		int _total = 0;
		if(percentUsed != null) {
			_percentUsed = Integer.parseInt(percentUsed);
		}
		if(used != null) {
			_used = (int) (Long.parseLong(used)/1024/1024);
		}
		if(total != null) {
			_total = (int) (Long.parseLong(total)/1024/1024);
		}
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
		if(appMemoryTotal != null) {
			this.appMemoryTotal = (int) (Long.parseLong(appMemoryTotal)/1024/1024);
		}
	}

	public void addTemperature(String description, String temp, String max) {

		try {
			float itemp = Float.parseFloat(temp);

			if(max == null) {
				max = "-1";
			}
			float imax = Float.parseFloat(max);

			temperature.add(new PSITemperature(description, itemp, imax));
		}
		catch(Exception e) {
			Log.d("PSIAndroid",e.toString());
		}
	}

	public void addVoltage(String description, String value) {

		try {
			float val = Float.parseFloat(value);


			voltages.add(new PSIVoltage(description, val));
		}
		catch(Exception e) {
			Log.d("PSIAndroid",e.toString());
		}
	}

	public List<PSITemperature> getTemperature() {
		return temperature;
	}

	public List<PSIVoltage> getVoltages() {
		return voltages;
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

	public void addNetworkInterface(String name, String rxBytes, String txBytes, String err, String drops) {
		int _rxBytes = 0;
		int _txBytes = 0;
		int _err = 0;
		int _drops = 0;

		if(rxBytes != null && !rxBytes.equals("")) {
			_rxBytes = (int) (Long.parseLong(rxBytes)/1024/1024);
		}

		if(txBytes != null && !txBytes.equals("")) {
			_txBytes =  (int) (Long.parseLong(txBytes)/1024/1024);
		}

		if(err != null && !err.equals("")) {
			_err =  Integer.parseInt(err);
		}

		if(drops != null && !drops.equals("")) {
			_drops = Integer.parseInt(drops);
		}

		networkInterface.add(new PSINetworkInterface(name, _rxBytes, _txBytes, _err, _drops));
	}

	public void addProcessStatus(String label, String value) {
		processStatus.put(label, value);
	}

	public HashMap<String, String> getProcessStatus() {
		return processStatus;
	}	

	public void addSmart(PSISmart item) {
		smart.add(item);
	}

	public List<PSISmart> getSmart() {
		return smart;
	}

	public void addPrinter(PSIPrinter item) {
		printer.add(item);
	}

	public List<PSIPrinter> getPrinter() {
		return printer;
	}

	public List<PSIUps> getUps() {
		return ups;
	}

	public void addUps(PSIUps item) {
		ups.add(item);
	}

	public List<PSIRaid> getRaid() {
		return raid;
	}

	public void addRaid(PSIRaid r) {
		raid.add(r);
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

	public PSIBat getBat() {
		return bat;
	}

	public void setBat(PSIBat bat) {
		this.bat = bat;
	}

	public int getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(int cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public int getProcesses() {
		return processes;
	}

	public void setProcesses(int processes) {
		this.processes = processes;
	}


	public int getProcessesRunning() {
		return processesRunning;
	}

	public void setProcessesRunning(int processesRunning) {
		this.processesRunning = processesRunning;
	}

	public int getProcessesSleeping() {
		return processesSleeping;
	}

	public void setProcessesSleeping(int processesSleeping) {
		this.processesSleeping = processesSleeping;
	}

	public int getProcessesStopped() {
		return processesStopped;
	}

	public void setProcessesStopped(int processesStopped) {
		this.processesStopped = processesStopped;
	}

	public int getProcessesZombie() {
		return processesZombie;
	}

	public void setProcessesZombie(int processesZombie) {
		this.processesZombie = processesZombie;
	}

	public int getProcessesOther() {
		return processesOther;
	}

	public void setProcessesOther(int processesOther) {
		this.processesOther = processesOther;
	}

	public int getProcessesWaiting() {
		return processesWaiting;
	}

	public void setProcessesWaiting(int processesWaiting) {
		this.processesWaiting = processesWaiting;
	}
}
