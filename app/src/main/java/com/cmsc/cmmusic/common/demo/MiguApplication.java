package com.cmsc.cmmusic.common.demo;

import java.lang.Thread.UncaughtExceptionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;

import com.cmsc.cmmusic.common.Logger;

public class MiguApplication extends Application implements
		UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	public void onCreate() {
		super.onCreate();
		try {
			System.loadLibrary("mg20pbase");
		} catch (Exception e) {
			e.printStackTrace();
		}

		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ExceptionName", ex.getClass().getName()).put(
					"StackTrace", Log.getStackTraceString(ex));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Logger.log2Preferences(this, jsonObject.toString());

		return true;
	}
}
