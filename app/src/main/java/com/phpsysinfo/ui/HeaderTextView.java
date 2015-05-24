package com.phpsysinfo.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.phpsysinfo.R;

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
		this.setBackgroundColor(getResources().getColor(R.color.header));
		this.setTypeface(null,Typeface.BOLD);
		this.setPadding(5, 5, 5, 5);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		llp.setMargins(0, 5, 0, 5);
		this.setLayoutParams(llp);
		
		Resources res = getResources();
		int resourceId = res.getIdentifier("up", "drawable", getContext().getPackageName());
		Drawable img = res.getDrawable(resourceId);
		this.setCompoundDrawablesWithIntrinsicBounds(img, null , null, null);
	}
}

