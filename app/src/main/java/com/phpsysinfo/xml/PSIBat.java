package com.phpsysinfo.xml;

public class PSIBat {

	private String designCapacity = "";
	private String remainingCapacity = "";
	private String capacity = "";
	private String chargingState = "";
	private String fullCapacity = "";

	public PSIBat(String designCapacity, String fullCapacity, String remainingCapacity, String capacity, String chargingState) {
		this.designCapacity = designCapacity;
		this.fullCapacity = fullCapacity;

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

	public String getFullCapacity() {
		return fullCapacity;
	}

	public void setFullCapacity(String fullCapacity) {
		this.fullCapacity = fullCapacity;
	}
}