package com.example.upmessenger.UpBroadcastReciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.upmessenger.Networks.NetworkUtil
import com.example.upmessenger.Networks.NetworkUtil.getConnectivityStatusString
import com.example.upmessenger.OnNetworkGone


class NetworkChangeReciever : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
//        TODO("MyReceiver.onReceive() is not implemented")

        val status = getConnectivityStatusString(context)
        Log.e("Network_STATUS_CHANGED", "caught network reciever")
        if ("android.net.conn.CONNECTIVITY_CHANGE" == intent.action) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                (context as OnNetworkGone ).setUserStatusOffline()
            }else
                (context as OnNetworkGone ).setUserOnline()
        }
    }
}