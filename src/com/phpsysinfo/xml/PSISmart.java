package com.phpsysinfo.xml;

public class PSISmart {

	private String disk = "";
	private String attribut = "";
	private String value = "";
	
	public PSISmart(String disk, String attribut, String value) {
		this.disk = disk;
		this.attribut = attribut;
		this.value = value;
	}
	
	public String getDisk() {
		return disk;
	}
	public void setDisk(String disk) {
		this.disk = disk;
	}
	public String getAttribut() {
		return attribut;
	}
	public void setAttribut(String attribut) {
		this.attribut = attribut;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
