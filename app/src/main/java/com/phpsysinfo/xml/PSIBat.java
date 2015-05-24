package com.phpsysinfo.xml;

public class PSIBat {

	private String designCapacity = "";
	private String remainingCapacity = "";
	private String capacity = "";
	private String chargingState = "";

	public PSIBat(String designCapacity, String remainingCapacity, String capacity, String chargingState) {
		this.designCapacity = designCapacity;
		this.remainingCapacity = remainingCapacity;
		this.capacity = capacity;
		this.chargingState = chargingState;
	}

	public String getRemainingCapacity() {
		return remainingCapacity;
	}

	public void setRemainingCapacity(String remainingCapacity) {
		this.remainingCapacity = remainingCapacity;
	}

	public String getChargingState() {
		return chargingState;
	}

	public void setChargingState(String chargingState) {
		this.chargingState = chargingState;
	}

	public String getDesignCapacity() {
		return designCapacity;
	}

	public void setDesignCapacity(String designCapacity) {
		this.designCapacity = designCapacity;
	}

	public String getCapacity() {
		return capacity;
	}

	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
}