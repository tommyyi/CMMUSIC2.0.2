package com.cmsc.cmmusic.common.demo;


import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class MyGroupView extends ViewGroup{

	private Context context;
	
	public MyGroupView(Context context) {
		super(context);
		this.context = context;
	}



	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);	
		
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			view.measure(widthMeasureSpec, heightMeasureSpec);
		}
		
		int measuredHeight = measureHeight(heightMeasureSpec);  
	
		int measuredWidth = measureWidth(widthMeasureSpec);  
	 
		setMeasuredDimension(measuredHeight, measuredWidth); 

		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			View view = getChildAt(i);
			view.layout(0, i*70, view.getMeasuredWidth(), view.getMeasuredHeight()+i*70);
		}
	}

	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}
	
	
	private int measureHeight(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);  
		  
		int specSize = MeasureSpec.getSize(measureSpec);  
		int result = 300;  
		  
		if (specMode == MeasureSpec.AT_MOST) 
		{
			result = specSize; 
		}
		else if (specMode == MeasureSpec.EXACTLY)    
		{  
			result = specSize;  
		}

		return result;
	}
	
	private int measureWidth(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);  
		
		int specSize = MeasureSpec.getSize(measureSpec);  
		int result = 300;  
		
		if (specMode == MeasureSpec.AT_MOST) 
		{
			result = specSize; 
		}
		else if (specMode == MeasureSpec.EXACTLY)    
		{  
			result = specSize;  
		}
		
		return result;
	}
}
