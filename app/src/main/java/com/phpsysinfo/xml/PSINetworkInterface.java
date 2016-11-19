package com.phpsysinfo.xml;

public class PSINetworkInterface {

	public String name = "";
	public double rxBytes = 0;
	public double txBytes = 0;
	public double err = 0;
	public double drops = 0;
	public String info = "";

	public PSINetworkInterface(String name, double rxBytes, double txBytes, double err, double drops, String info) {
		this.name = name;
		this.rxBytes = rxBytes;
		this.txBytes = txBytes;
		this.err = err;
		this.drops = drops;
		this.info = info;
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
	public double getErr() {
		return err;
	}
	public void setErr(int err) {
		this.err = err;
	}
	public double getDrops() {
		return drops;
	}
	public void setDrops(int drops) {
		this.drops = drops;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
}
