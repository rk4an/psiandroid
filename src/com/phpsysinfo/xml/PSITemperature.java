package com.phpsysinfo.xml;

public class PSITemperature {

	private String description = "";
	private int temp = -1;
	private int max = -1;
	
	public PSITemperature(String description, int temp, int max) {
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

	public int getTemp() {
		return temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
