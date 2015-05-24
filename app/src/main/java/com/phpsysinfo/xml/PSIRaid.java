package com.phpsysinfo.xml;

import java.util.ArrayList;
import java.util.List;

public class PSIRaid {

	public String name = "";
	public String level = "";
	public int disks_active = 0;
	public int disks_registered = 0;
	public List<PSIRaidDevice> devices = new ArrayList<PSIRaidDevice>();
	
	public PSIRaid() {
	}
	
	public PSIRaid(String name, String level, int disks_active, int disks_registered) {
		this.name = name;
		this.level = level;
		this.disks_active = disks_active;
		this.disks_registered = disks_registered;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getDisksActive() {
		return disks_active;
	}
	
	public void setDisksActive(int disks_active) {
		this.disks_active = disks_active;
	}
	
	public int getDisksRegistered() {
		return disks_registered;
	}
	
	public void setDisksRegistered(int disks_registered) {
		this.disks_registered = disks_registered;
	}
	
	public List<PSIRaidDevice> getDevices() {
		return this.devices;
	}
	
	public void addDevices(PSIRaidDevice device) {
		this.devices.add(device);
	}
}
