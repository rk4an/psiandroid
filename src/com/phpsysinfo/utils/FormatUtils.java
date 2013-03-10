package com.phpsysinfo.utils;

import java.text.NumberFormat;

import android.content.Context;

import com.phpsysinfo.R;

public class FormatUtils
{
	public static String getFormatedMemory(Context c, int memory) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		String value = "0";

		if(memory > 1024*1024) {
			value = nf.format((float)memory/1024/1024) + "&nbsp;" + c.getString(R.string.lblTio);
		}
		else if(memory > 1024) {
			value = nf.format((float)memory/1024) + "&nbsp;" + c.getString(R.string.lblGio);
		}
		else {
			value = nf.format(memory) + "&nbsp;" + c.getString(R.string.lblMio);
		}

		return value;
	}
}