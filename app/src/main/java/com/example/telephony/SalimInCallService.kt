package com.example.telephony

import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService

class SalimInCallService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        CallManager.setInCallService(this)
    }

    override fun onDestroy() {
        CallManager.setInCallService(null)
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.onCallAdded(call, applicationContext)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.onCallRemoved(call)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        CallManager.onAudioStateChanged(audioState)
    }
}
