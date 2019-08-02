package com.test.blesampleproject.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class ScannerBLE(private val context: Context, private val scanPeriod: Long) {

    private var scanning = false
    private val handler = Handler()
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private val _device = MutableLiveData<BluetoothDevice>()
    val device: LiveData<BluetoothDevice>
        get() = _device

    fun isScanning() : Boolean{
        return scanning
    }

    fun start(){
        scanLeDevice(true)
    }

    fun stop(){
        scanLeDevice(false)
    }

    private var mScanning: Boolean = false

    private fun scanLeDevice(enable: Boolean) {

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        when (enable) {
            true -> {
                handler.postDelayed({
                    mScanning = false
                    bluetoothLeScanner.stopScan(scanCallback)
                }, scanPeriod)
                mScanning = true
                bluetoothLeScanner.startScan(scanCallback)
            }
            else -> {
                mScanning = false
                bluetoothLeScanner.stopScan(scanCallback)
            }
        }
    }

    private val scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            Log.i("blescan", "scan failed $errorCode")
            super.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.i("blescan", "scan success")
            _device.value = result!!.device
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.i("blescan", "scan batched")
        }
    }
}