package com.phpsysinfo.xml;

import java.util.ArrayList;
import java.util.List;

public class PSIPrinter {

	private String name = "";
	private List<PSIPrinterItem> item = new ArrayList<PSIPrinterItem>();
	private List<String> messages = new ArrayList<String>();
	
	public PSIPrinter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<PSIPrinterItem> getItem() {
		return item;
	}

	public void addItem(PSIPrinterItem item) {
		this.item.add(item);
	}

	public List<String> getMessages() {
		return messages;
	}

	public void addMessages(String messages) {
		this.messages.add(messages);
	}

	public void setPrinter(String printer) {
		this.name = printer;
	}
}
