package com.test.blesampleproject.pulse

import android.bluetooth.*
import android.content.Context
import androidx.lifecycle.ViewModel
import com.test.blesampleproject.ble.BroadcastReceiverBTState
import java.util.*
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGatt
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.test.blesampleproject.ble.ScannerBLE
import java.nio.ByteBuffer


private const val SCAN_PERIOD: Long = 10000

enum class StatusBTConnection { TURNING_ON, TURNING_OFF, ON, OFF }
enum class StatusDeviceConnection { CONNECTED, CONNECTING, DISCONNECTED }

class PulseViewModel(private val context: Context) : ViewModel() {

    val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val DESCRIPTOR_CONFIG_UUID = convertFromInteger(0x2902)

    private var scannerBLE: ScannerBLE = ScannerBLE(context, SCAN_PERIOD)
    val broadcastReceiverBTState = BroadcastReceiverBTState()
    private val _statusDeviceConnection = MutableLiveData<StatusDeviceConnection>()
    val statusDeviceConnection: LiveData<StatusDeviceConnection>
        get() = _statusDeviceConnection
    private val _pulse = MutableLiveData<Int>()
    val pulse: LiveData<Int>
        get() = _pulse

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    init {
        setBluetoothChecked()
        scannerBLE.device.observeForever {
            if (it != null)
                setGATT(it)
        }
    }

    fun setBluetoothChecked() {
        broadcastReceiverBTState.setStatusBTConnection(bluetoothAdapter?.isEnabled!!)
    }

    private fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    private fun setGATT(bluetoothDevice: BluetoothDevice) {
        _statusDeviceConnection.value = StatusDeviceConnection.CONNECTING
        bluetoothDevice.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothAdapter.STATE_CONNECTED -> {
                    _statusDeviceConnection.postValue(StatusDeviceConnection.CONNECTED)
                    gatt?.discoverServices()
                }
                BluetoothAdapter.STATE_DISCONNECTED -> {
                    _statusDeviceConnection.postValue(StatusDeviceConnection.DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                val service = gatt!!.getService(HEART_RATE_SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(DESCRIPTOR_CONFIG_UUID)
                        if (descriptor != null) {
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    }
                }
            } else {
                Log.w("serviceBLE", "onServicesDiscovered received: $status")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor, status: Int
        ) {
            val characteristic = gatt.getService(HEART_RATE_SERVICE_UUID)
                .getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
            characteristic.value = byteArrayOf(1, 1)
            gatt.writeCharacteristic(characteristic)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            readCounterCharacteristic(characteristic!!)
        }
    }

    private fun readCounterCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (HEART_RATE_MEASUREMENT_CHAR_UUID == characteristic.uuid) {
            val data = characteristic.value
            val buffer = ByteBuffer.wrap(data)
            val value = buffer.int
            _pulse.value = value
        }
    }

    fun startScanDevices() {
        if (!scannerBLE.isScanning()) {
            scannerBLE.start()
        }
    }

    fun stopScanDevices() {
        scannerBLE.stop()
    }
}