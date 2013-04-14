package com.phpsysinfo.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class HeaderTextView extends TextView {

	public HeaderTextView(Context context) {
		super(context);
		custom();
	}

	public HeaderTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		custom();
	}

	public HeaderTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		custom();
	}

	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
	}
	
	public void custom() {
		this.setBackgroundColor(Color.parseColor("#444242"));
		this.setTypeface(null,Typeface.BOLD);
		this.setPadding(5, 5, 5, 5);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		llp.setMargins(0, 5, 0, 5);
		this.setLayoutParams(llp);
	}
}

