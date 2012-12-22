package com.phpsysinfo.xml;

public class PSIRaid {

	public String name = "";
	public int disks_active = 0;
	public int disks_registered = 0;
	
	public PSIRaid(String name, int disks_active, int disks_registered) {
		super();
		this.name = name;
		this.disks_active = disks_active;
		this.disks_registered = disks_registered;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getDisks_active() {
		return disks_active;
	}
	
	public void setDisks_active(int disks_active) {
		this.disks_active = disks_active;
	}
	
	public int getDisks_registered() {
		return disks_registered;
	}
	
	public void setDisks_registered(int disks_registered) {
		this.disks_registered = disks_registered;
	}
	
}
