package com.phpsysinfo.xml;

public class PSIPrinter {

	private String printer = "";
	private String description = "";
	private String supplyUnit = "";
	private String maxCapacity = "";
	private String level = "";
	
	public PSIPrinter(String printer, String description, String supplyUnit, String maxCapacity, String level) {
		this.printer = printer;
		this.description = description;
		this.supplyUnit = supplyUnit;
		this.maxCapacity = maxCapacity;
		this.level = level;
	}

	public String getPrinter() {
		return printer;
	}

	public void setPrinter(String printer) {
		this.printer = printer;
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
