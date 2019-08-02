package com.test.blesampleproject.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.test.blesampleproject.R
import com.test.blesampleproject.databinding.ActivityMainBinding
import com.test.blesampleproject.pulse.PulseViewModel
import com.test.blesampleproject.pulse.StatusBTConnection
import com.test.blesampleproject.pulse.StatusDeviceConnection

const val REQUEST_ENABLE_BT = 1

class MainActivity : AppCompatActivity() {

    private lateinit var pulseViewModel: PulseViewModel
    private lateinit var bindingPulse: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingPulse = DataBindingUtil.setContentView(this, R.layout.activity_main)
        pulseViewModel = PulseViewModel(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 3)
        }

        bindingPulse.button.setOnClickListener {
            pulseViewModel.startScanDevices()
        }

        pulseViewModel.broadcastReceiverBTState.statusBtConnection.observe(this, Observer {
            when (it){
                StatusBTConnection.OFF -> {
                    bindingPulse.statusBtTextview.text = getString(R.string.bt_off)
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
                StatusBTConnection.ON -> bindingPulse.statusBtTextview.text = getString(R.string.bt_on)
                StatusBTConnection.TURNING_OFF -> bindingPulse.statusBtTextview.text = getString(R.string.bt_turning_off)
                StatusBTConnection.TURNING_ON -> bindingPulse.statusBtTextview.text = getString(R.string.bt_turning_on)
            }
        })

        pulseViewModel.statusDeviceConnection.observe(this, Observer {
            when (it){
                StatusDeviceConnection.CONNECTING -> bindingPulse.statusDeviceTextview.text = getString(R.string.device_connecting)
                StatusDeviceConnection.CONNECTED-> bindingPulse.statusDeviceTextview.text = getString(R.string.device_connected)
                StatusDeviceConnection.DISCONNECTED -> bindingPulse.statusDeviceTextview.text = getString(R.string.device_disconnected)
            }
        })

        pulseViewModel.pulse.observe(this, Observer {
            bindingPulse.pulseTextview.text = it.toString()
        })
    }

    override fun onPause() {
        super.onPause()
        pulseViewModel.stopScanDevices()
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(pulseViewModel.broadcastReceiverBTState, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(pulseViewModel.broadcastReceiverBTState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == REQUEST_ENABLE_BT){
            when (resultCode){
                Activity.RESULT_OK -> pulseViewModel.setBluetoothChecked()
                Activity.RESULT_CANCELED -> Toast.makeText(this, "Please turn on Bluetooth", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 3)
        } else {
            //viewModel.getCurrentLocate()
        }
    }
}
