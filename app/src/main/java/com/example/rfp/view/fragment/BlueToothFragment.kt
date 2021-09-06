package com.example.rfp.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieDrawable
import com.example.rfp.R
import com.example.rfp.databinding.FragmentBlueToothBinding
import android.widget.ArrayAdapter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import android.widget.Toast

import androidx.test.core.app.ApplicationProvider.getApplicationContext

import android.content.IntentFilter
import android.content.BroadcastReceiver














class BlueToothFragment : Fragment() {

    private lateinit var pairedDevices: Set<BluetoothDevice>
    private lateinit var btArrayAdapter: ArrayAdapter<String>
    private lateinit var deviceAddressArray: ArrayList<String>

    private lateinit var btAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1

    private var _binding: FragmentBlueToothBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btArrayAdapter = ArrayAdapter(activity?.applicationContext!!, R.layout.bluetooth_list_item)
        deviceAddressArray = ArrayList()
        binding.bluetoothList.adapter = btArrayAdapter

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlueToothBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val send  = {_: View ->

        }

        val search = {_: View ->
            binding.bluetoothLottie.repeatCount = LottieDrawable.INFINITE

            if (btAdapter.isDiscovering) {
                btAdapter.cancelDiscovery()
            } else {
                if (btAdapter.isEnabled) {
                    btAdapter.startDiscovery()
                    btArrayAdapter.clear()
                    if (deviceAddressArray.isNotEmpty()) {
                        deviceAddressArray.clear()
                    }
                    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                    registerReceiver(receiver, filter)
                } else {
                    Toast.makeText(
                        ApplicationProvider.getApplicationContext<Context>(),
                        "bluetooth not on",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        val paired = {_: View ->
            btArrayAdapter.clear()
            if (deviceAddressArray.isNotEmpty()) {
                deviceAddressArray.clear()
            }
            pairedDevices = btAdapter.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                for (device in pairedDevices) {
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address
                    btArrayAdapter.add(deviceName)
                    deviceAddressArray.add(deviceHardwareAddress)
                }
            }
        }
        binding.sendBtn.setOnClickListener(send)
        binding.searchBtn.setOnClickListener(search)
        binding.pairedBtn.setOnClickListener(paired)

    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device!!.name
                val deviceHardwareAddress = device.address
                btArrayAdapter.add(deviceName)
                deviceAddressArray.add(deviceHardwareAddress)
                btArrayAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}