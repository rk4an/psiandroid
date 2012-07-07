package com.phpsysinfo.xml;

import java.util.ArrayList;
import java.util.List;

public class Hosts  {

	public List<Host> host = new ArrayList<Host>();

	
	public Hosts() {
		super();
	}
	
	public Hosts(List<Host> host) {
		super();
		this.host = host;
	}

	public List<Host> getHost() {
		return host;
	}

	public void setHost(List<Host> host) {
		this.host = host;
	}
}
