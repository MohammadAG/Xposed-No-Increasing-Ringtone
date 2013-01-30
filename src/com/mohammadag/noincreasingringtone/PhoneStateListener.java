package com.mohammadag.noincreasingringtone;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;

public class PhoneStateListener extends BroadcastReceiver {

	public boolean isRinging = false;
	@SuppressWarnings("unused")
	private Object pmSvc;
	
	public static void initHooks(final PhoneStateListener listener) {
		/* Hook to the PackageManager service in order to
		* - Listen for broadcasts to apply new settings and restart the app
		* - Intercept the permission granting function to remove disabled permissions
		*/
		
		try {
			final Class<?> clsPMS = findClass("com.android.server.pm.PackageManagerService", NoIncreasingRingtone.class.getClassLoader());
			
			// Listen for broadcasts from the Settings part of the mod, so it's applied immediately
			findAndHookMethod(clsPMS, "systemReady", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {			
					Context mContext = (Context) getObjectField(param.thisObject, "mContext");
					listener.pmSvc = param.thisObject;
			        IntentFilter filter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
					
			        XposedBridge.log("Registering NoIncreasingRingtone broadcast receiver");
					mContext.registerReceiver(listener, filter, null, null);
				}
			});
			
		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}
	
	@Override
	public void onReceive(Context arg0, Intent intent) {
		  if (intent != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			  String newPhoneState = intent.hasExtra(TelephonyManager.EXTRA_STATE) ? intent.getStringExtra(TelephonyManager.EXTRA_STATE) : null;
			  if (newPhoneState != null) {
				  if (newPhoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
					  isRinging = true;
				  } else {
					  isRinging = false;
				  }
			  }
		  }
	}
}