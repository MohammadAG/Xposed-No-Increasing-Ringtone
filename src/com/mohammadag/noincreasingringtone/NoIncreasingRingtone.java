package com.mohammadag.noincreasingringtone;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import com.mohammadag.noincreasingringtone.PhoneStateListener;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;



public class NoIncreasingRingtone implements IXposedHookLoadPackage {

	private PhoneStateListener listener;
	
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName != "android")
			return;
		
		XposedBridge.log("Hooking process android");
		
		listener = new PhoneStateListener();

		findAndHookMethod("android.media.AudioService", lpparam.classLoader, "setStreamVolume", int.class, int.class, int.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			Integer streamType = (Integer)param.args[0];
    			Integer index = (Integer)param.args[1];
    			Integer flags = (Integer)param.args[2];
    			
    			XposedBridge.log("Requesting volume change to " + index + " isRinging = " + listener.isRinging);
    			
    			if (streamType == AudioManager.STREAM_RING && index == 1 && flags == 0) {
    				XposedBridge.log("Rejecting volume change to " + index);
    				param.setResult(null);
    			}
    		}
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
    			// this will be called after the clock was updated by the original method
    		}
	});
	}
	
	
}