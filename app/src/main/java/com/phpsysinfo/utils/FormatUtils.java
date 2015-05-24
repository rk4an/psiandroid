package com.phpsysinfo.utils;

import java.text.NumberFormat;

import com.phpsysinfo.R;
import com.phpsysinfo.activity.PSIActivity;

public class FormatUtils
{
	public static String getFormatedMemory(int memory) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		String value = "0";

		if(memory > 1024*1024) {
			value = nf.format((float)memory/1024/1024) + "&nbsp;" + PSIActivity.getAppContext().getString(R.string.lblTio);
		}
		else if(memory > 1024) {
			value = nf.format((float)memory/1024) + "&nbsp;" + PSIActivity.getAppContext().getString(R.string.lblGio);
		}
		else {
			value = nf.format(memory) + "&nbsp;" + PSIActivity.getAppContext().getString(R.string.lblMio);
		}

		return value;
	}
}