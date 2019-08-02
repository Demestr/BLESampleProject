package com.test.blesampleproject.ble

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.blesampleproject.pulse.StatusBTConnection

class BroadcastReceiverBTState : BroadcastReceiver() {

    private val _statusBTConnection = MutableLiveData<StatusBTConnection>()
    val statusBtConnection: LiveData<StatusBTConnection>
    get() = _statusBTConnection

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action

        if(action == BluetoothAdapter.ACTION_STATE_CHANGED)
        {

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)){
                BluetoothAdapter.STATE_OFF -> {
                    _statusBTConnection.value = StatusBTConnection.OFF
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    _statusBTConnection.value = StatusBTConnection.TURNING_OFF
                }
                BluetoothAdapter.STATE_ON -> {
                    _statusBTConnection.value = StatusBTConnection.ON
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    _statusBTConnection.value = StatusBTConnection.TURNING_ON
                }
            }
        }
    }

    fun setStatusBTConnection(isEnabled: Boolean){
        if (isEnabled)
            _statusBTConnection.value = StatusBTConnection.ON
        else
            _statusBTConnection.value = StatusBTConnection.OFF
    }
}