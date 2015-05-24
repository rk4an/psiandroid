package com.phpsysinfo.xml;

public class PSIPrinterItem {

	private String description = "";
	private String supplyUnit = "";
	private String maxCapacity = "";
	private String level = "";
	
	public PSIPrinterItem(String description, String supplyUnit, String maxCapacity, String level) {
		this.description = description;
		this.supplyUnit = supplyUnit;
		this.maxCapacity = maxCapacity;
		this.level = level;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSupplyUnit() {
		return supplyUnit;
	}

	public void setSupplyUnit(String supplyUnit) {
		this.supplyUnit = supplyUnit;
	}

	public String getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(String maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
}