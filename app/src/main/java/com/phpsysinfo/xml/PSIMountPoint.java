package com.phpsysinfo.xml;

public class PSIMountPoint {

	public String name = "";
	public int percentUsed = 0;
	public int used = 0;
	public int total = 0;

	/**
	 * 
	 * @param name
	 * @param percentUsed
	 * @param used
	 * @param total
	 */
	public PSIMountPoint(String name, int percentUsed, int used, int total) {
		this.name = name;
		this.percentUsed = percentUsed;
		this.used = used;
		this.total = total;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPercentUsed() {
		return percentUsed;
	}

	public void setPercentUsed(int percentUsed) {
		this.percentUsed = percentUsed;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
