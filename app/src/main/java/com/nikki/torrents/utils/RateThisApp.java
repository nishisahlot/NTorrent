
package com.nikki.torrents.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;

import com.nikki.torrents.R;

import java.util.Date;


public class RateThisApp {
	
	private static final String TAG = RateThisApp.class.getSimpleName();
	
	private static final String PREF_NAME = "RateThisApp";
	private static final String KEY_INSTALL_DATE = "rta_install_date";
	private static final String KEY_LAUNCH_TIMES = "rta_launch_times";
	private static final String KEY_OPT_OUT = "rta_opt_out";
	
	private static Date mInstallDate = new Date();
	private static int mLaunchTimes = 0;
	private static boolean mOptOut = false;
	

	public static final int INSTALL_DAYS = 7;

	public static final int LAUNCH_TIMES = 3;
	

	public static final boolean DEBUG = false;
	

	public static void onStart(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();

		if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
			Date now = new Date();
			editor.putLong(KEY_INSTALL_DATE, now.getTime());
			log("First install: " + now.toString());
		}

		int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
		launchTimes++;
		editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
		log("Launch times; " + launchTimes);
		
		editor.commit();
		
		mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
		mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
		mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
		
		printStatus(context);
	}
	

	public static void showRateDialogIfNeeded(final Context context) {
		if (shouldShowRateDialog()) {
			showRateDialog(context);
		}
	}
	

	private static boolean shouldShowRateDialog() {
		if (mOptOut) {
			return false;
		} else {
			if (mLaunchTimes >= LAUNCH_TIMES) {
				return true;
			}
			long threshold = INSTALL_DAYS * 24 * 60 * 60 * 1000L;
			if (new Date().getTime() - mInstallDate.getTime() >= threshold) {
				return true;
			}
			return false;
		}
	}
	

	public static void showRateDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.r_t_app_title));
		builder.setMessage(context.getResources().getString(R.string.r_t_app_message));
		builder.setPositiveButton(context.getResources().getString(R.string.r_t_app_ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String appPackage = context.getPackageName();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackage));
				context.startActivity(intent);
				setOptOut(context, true);
			}
		});
		builder.setNeutralButton(context.getResources().getString(R.string.r_t_app_neutral), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clearSharedPreferences(context);
			}
		});
		builder.setNegativeButton(context.getResources().getString(R.string.r_t_app_cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setOptOut(context, true);
			}
		});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				clearSharedPreferences(context);
			}
		});
		builder.create().show();
	}
	

	private static void clearSharedPreferences(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.remove(KEY_INSTALL_DATE);
		editor.remove(KEY_LAUNCH_TIMES);
		editor.commit();
        mLaunchTimes=0;
	}
	

	private static void setOptOut(final Context context, boolean optOut) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean(KEY_OPT_OUT, optOut);
		editor.commit();
        mOptOut=true;
	}
	

	private static void printStatus(final Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		log("*** RateThisApp Status ***");
		log("Install Date: " + new Date(pref.getLong(KEY_INSTALL_DATE, 0)));
		log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0));
		log("Opt out: " + pref.getBoolean(KEY_OPT_OUT, false));
	}
	

	private static void log(String message) {
		if (DEBUG) {
			Log.v(TAG, message);
		}
	}
}
