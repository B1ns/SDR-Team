package com.example.rfp.view.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rfp.R
import com.example.rfp.databinding.FragmentBlueToothBinding
import android.widget.ArrayAdapter

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent

import android.content.IntentFilter
import android.content.BroadcastReceiver
import org.jetbrains.anko.support.v4.toast
import com.example.rfp.data.ConnectedThread

import android.widget.Toast

import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import java.io.IOException

import android.bluetooth.BluetoothSocket

class BlueToothFragment : Fragment() {

    private lateinit var pairedDevices: Set<BluetoothDevice>
    private lateinit var btArrayAdapter: ArrayAdapter<String>
    private lateinit var deviceAddressArray: ArrayList<String>

    private lateinit var btAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1

    private var _binding: FragmentBlueToothBinding? = null
    private val binding get() = _binding!!

    private lateinit var btSocket: BluetoothSocket
    private lateinit var connectedThread: ConnectedThread


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlueToothBinding.inflate(inflater, container, false)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        btArrayAdapter = ArrayAdapter(activity?.applicationContext!!, R.layout.bluetooth_list_item)
        deviceAddressArray = ArrayList()
        binding.bluetoothList.adapter = btArrayAdapter

//        binding.bluetoothList.onItemClickListener = myOnItemClickListener()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val send = { _: View ->

        }

        val search = { _: View ->
            toast("Search")
            search()
        }

        val paired = { _: View ->
            toast("Paired Device")
            paired()
        }
        binding.sendBtn.setOnClickListener(send)
        binding.searchBtn.setOnClickListener(search)
        binding.pairedBtn.setOnClickListener(paired)

    }

    private fun paired(): Unit {
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

    private fun search(): Unit {
        binding.bluetoothLottie.repeatCount = ValueAnimator.INFINITE

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
                requireActivity().registerReceiver(receiver, filter)
            } else {
            }
        }
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
        activity?.unregisterReceiver(receiver)
    }

//    inner class myOnItemClickListener : OnItemClickListener {
//        override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
//            textStatus.setText("try...")
//            val name: String = btArrayAdapter.getItem(position).toString() // get name
//            val address: String = deviceAddressArray[position] // get address
//            var flag = true
//            val device: BluetoothDevice = btAdapter.getRemoteDevice(address)
//
//            // create & connect socket
//            try {
//                btSocket = createBluetoothSocket(device)
//                btSocket.connect()
//            } catch (e: IOException) {
//                flag = false
//                textStatus.setText("connection failed!")
//                e.printStackTrace()
//            }
//            if (flag) {
//                textStatus.setText("connected to $name")
//                connectedThread = ConnectedThread(btSocket)
//                connectedThread.start()
//            }
//        }
//    }
}