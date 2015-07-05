package com.phpsysinfo.xml;

public class PSIVoltage {

	private String description = "";
	private float value = -1;

	public PSIVoltage(String description, float value) {
		this.description = description;
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

}
