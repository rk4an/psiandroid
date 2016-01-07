package com.phpsysinfo.xml;

public class PSIUprecords {

	private String uptime = "";
	private String up = "";
	private String down = "";
	private String percent = "";

	public PSIUprecords() {
	}

	public PSIUprecords(String uptime, String up, String down, String percent) {
		this.uptime = uptime;
		this.up = up;
		this.down = down;
		this.percent = percent;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getUp() {
		return up;
	}

	public void setUp(String up) {
		this.up = up;
	}

	public String getDown() {
		return down;
	}

	public void setDown(String down) {
		this.down = down;
	}

	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}
}
