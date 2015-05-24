package com.phpsysinfo.xml;

public class PSIRaidDevice {

	private String name = "";
	private String status = "";
	
	public PSIRaidDevice(String name, String status) {
		super();
		this.name = name;
		this.status = status;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
