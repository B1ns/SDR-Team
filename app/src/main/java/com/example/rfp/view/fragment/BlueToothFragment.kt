package com.example.rfp.view.fragment

import android.animation.ValueAnimator
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rfp.R

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import org.jetbrains.anko.support.v4.toast

import android.widget.AdapterView
import android.util.Log
import android.widget.ArrayAdapter
import com.example.rfp.databinding.FragmentBlueToothBinding
import kotlin.collections.ArrayList


class BlueToothFragment : Fragment() {

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }


    private var _binding: FragmentBlueToothBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlueToothBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (m_bluetoothAdapter == null) {
            toast("This device dosen't support bluetooth")
        }
        if (!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val search = { _: View ->
            toast("Search")
            search()
        }

        binding.searchBtn.setOnClickListener(search)

    }


    private fun search() {


        val deviceList: ArrayList<BluetoothDevice> = ArrayList()
        val nameList: ArrayList<String> = ArrayList()

        if (m_pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                deviceList.add(device)
                nameList.add(device.name + "(" + device.address + ")")
                Log.i("device", "" + device.name)
            }
        } else {
            toast("페어링된 장치를 찾을 수 없음")
        }

        val listAdapter = ArrayAdapter<String>(
            requireContext(),
            R.layout.bluetooth_list_item,
            R.id.bluetooth_text_item
        )
        binding.bluetoothList.adapter = listAdapter

        binding.bluetoothList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = deviceList[position]
                val address: String = device.address

                val intent = Intent(requireActivity().applicationContext, MainFragment::class.java)
                intent.putExtra(EXTRA_ADDRESS, address)
                startActivity(intent)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    toast("Bluetooth 연결성공 !")
                } else {
                    toast("Bluetooth 연결실패..")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth 활성화가 취소되었습니다.")
            }

        }
    }
}