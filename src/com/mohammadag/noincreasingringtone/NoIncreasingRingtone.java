package com.mohammadag.noincreasingringtone;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.media.AudioManager;
import com.mohammadag.noincreasingringtone.PhoneStateListener;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class NoIncreasingRingtone implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	private PhoneStateListener listener;
	
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName != "android")
			return;
		
		findAndHookMethod("android.media.AudioService", lpparam.classLoader, "setStreamVolume", int.class, int.class, int.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			Integer streamType = (Integer)param.args[0];
    			Integer index = (Integer)param.args[1];
    			Integer flags = (Integer)param.args[2];
    			
    			XposedBridge.log("Requesting volume change to " + index + " isRinging = " + listener.isRinging);
    			
    			// SecPhone does a call to AudioManager.setStreamVolume(AudioManager.STREAM_RING, 1, null);
    			// and 1-2 seconds later, does the same call with the original volume level set by the user
    			// We can ignore the first call to disable increasing ringtone, but we shouldn't ignore subsequent calls
    			// as the user could have the "Increase volume in pocket" option enabled, which overrides user-set volume
    			// level.
    			if (streamType == AudioManager.STREAM_RING && index == 1 && flags == 0 && listener.isRinging) {
    				XposedBridge.log("Rejecting volume change to " + index);
    				param.setResult(null);
    			}
    		}
	});
	}

	public void initZygote(StartupParam startupParam) throws Throwable {
		XposedBridge.log("Initializing NoIncreasingRingtone hooks");
		listener = new PhoneStateListener();
		PhoneStateListener.initHooks(listener);
	}
}