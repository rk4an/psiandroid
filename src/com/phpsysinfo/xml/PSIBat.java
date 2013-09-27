package com.phpsysinfo.xml;

public class PSIBat {

	private String remainingCapacity = "";
	private String chargingState = "";

	public PSIBat(String remainingCapacity, String chargingState) {
		this.remainingCapacity = remainingCapacity;
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
}