package com.phpsysinfo.xml;

public class PSIUps {

	String name;
	String model;
	String mode;
	String startTime;
	String status;
	String temperature;
	String outagesCount;
	String lastOutage;
	String lastOutageFinish;
	String lineVoltage;
	String loadPercent;
	String batteryVoltage;
	String batteryChargePercent;
	String timeLeftMinutes;
	
	public PSIUps(){
		super();
	};
	
	public PSIUps(String name, String model, String mode, String startTime,
			String status, String temperature, String outagesCount,
			String lastOutage, String lastOutageFinish, String lineVoltage,
			String loadPercent, String batteryVoltage,
			String batteryChargePercent, String timeLeftMinutes) {
		super();
		this.name = name;
		this.model = model;
		this.mode = mode;
		this.startTime = startTime;
		this.status = status;
		this.temperature = temperature;
		this.outagesCount = outagesCount;
		this.lastOutage = lastOutage;
		this.lastOutageFinish = lastOutageFinish;
		this.lineVoltage = lineVoltage;
		this.loadPercent = loadPercent;
		this.batteryVoltage = batteryVoltage;
		this.batteryChargePercent = batteryChargePercent;
		this.timeLeftMinutes = timeLeftMinutes;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getOutagesCount() {
		return outagesCount;
	}
	public void setOutagesCount(String outagesCount) {
		this.outagesCount = outagesCount;
	}
	public String getLastOutage() {
		return lastOutage;
	}
	public void setLastOutage(String lastOutage) {
		this.lastOutage = lastOutage;
	}
	public String getLastOutageFinish() {
		return lastOutageFinish;
	}
	public void setLastOutageFinish(String lastOutageFinish) {
		this.lastOutageFinish = lastOutageFinish;
	}
	public String getLineVoltage() {
		return lineVoltage;
	}
	public void setLineVoltage(String lineVoltage) {
		this.lineVoltage = lineVoltage;
	}
	public String getLoadPercent() {
		return loadPercent;
	}
	public void setLoadPercent(String loadPercent) {
		this.loadPercent = loadPercent;
	}
	public String getBatteryVoltage() {
		return batteryVoltage;
	}
	public void setBatteryVoltage(String batteryVoltage) {
		this.batteryVoltage = batteryVoltage;
	}
	public String getBatteryChargePercent() {
		return batteryChargePercent;
	}
	public void setBatteryChargePercent(String batteryChargePercent) {
		this.batteryChargePercent = batteryChargePercent;
	}
	public String getTimeLeftMinutes() {
		return timeLeftMinutes;
	}
	public void setTimeLeftMinutes(String timeLeftMinutes) {
		this.timeLeftMinutes = timeLeftMinutes;
	}
}
