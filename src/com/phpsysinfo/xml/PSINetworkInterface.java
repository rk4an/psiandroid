package com.phpsysinfo.xml;

public class PSINetworkInterface {

	public String name = "";
	public double rxBytes = 0;
	public double txBytes = 0;
	
	public PSINetworkInterface(String name, double rxBytes, int txBytes) {
		this.name = name;
		this.rxBytes = rxBytes;
		this.txBytes = txBytes;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getRxBytes() {
		return rxBytes;
	}
	public void setRxBytes(double rxBytes) {
		this.rxBytes = rxBytes;
	}
	public double getTxBytes() {
		return txBytes;
	}
	public void setTxBytes(double txBytes) {
		this.txBytes = txBytes;
	}
	
}
