package com.phpsysinfo.xml;

public class PSITemperature {

	private String description = "";
	private float temp = -1;
	private float max = -1;
	
	public PSITemperature(String description, float temp, float max) {
		this.description = description;
		this.temp = temp;
		this.max = max;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getTemp() {
		return temp;
	}

	public void setTemp(float temp) {
		this.temp = temp;
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}
}
