package com.mohammadag.noincreasingringtone;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class NoIncreasingRingtone implements IXposedHookLoadPackage {

	private Context mContext = null;
	private boolean mIsRinging = false;
	
	private BroadcastReceiver mPhoneStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			  if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
				  String newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;
				  mIsRinging = TelephonyManager.EXTRA_STATE_RINGING.equals(newPhoneState);
			  }
		}
	};
	
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName != "android")
			return;
		
		Class<?> AudioService = findClass("android.media.AudioService", lpparam.classLoader);
		XposedBridge.hookAllConstructors(AudioService, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {			
				mContext = (Context) getObjectField(param.thisObject, "mContext");
				
				if (mContext != null) {
					IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
					mContext.registerReceiver(mPhoneStateReceiver, filter);
				}
			}
		});
		
		// public void setStreamVolume(int streamType, int index, int flags)
		findAndHookMethod(AudioService, "setStreamVolume", int.class, int.class, int.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			Integer streamType = (Integer)param.args[0];
    			Integer index = (Integer)param.args[1];
    			Integer flags = (Integer)param.args[2];
    			
    			// SecPhone does a call to AudioManager.setStreamVolume(AudioManager.STREAM_RING, 1, null);
    			// and 1-2 seconds later, does the same call with the original volume level set by the user
    			// We can ignore the first call to disable increasing ringtone, but we shouldn't ignore subsequent calls
    			// as the user could have the "Increase volume in pocket" option enabled, which overrides user-set volume
    			// level.
    			if (streamType == AudioManager.STREAM_RING && index == 1 && flags == 0 && mIsRinging) {
    				param.setResult(null);
    			}
    		}
		});
	}
}