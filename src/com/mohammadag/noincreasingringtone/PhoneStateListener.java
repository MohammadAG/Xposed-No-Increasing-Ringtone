package com.mohammadag.noincreasingringtone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateListener extends BroadcastReceiver {

	public boolean isRinging = false;

	@Override
	public void onReceive(Context arg0, Intent intent) {
		Log.d("Xposed", "phone state changed");
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