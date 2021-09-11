package com.example.rfp.view.fragment

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.IOException
import java.util.*
import com.example.rfp.databinding.FragmentMainBinding
import org.jetbrains.anko.support.v4.toast


class MainFragment : Fragment() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var checkAddress : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)


        val intent = Intent(requireContext(), BlueToothFragment::class.java)
        val bluetoothConnected = intent.getStringExtra(BlueToothFragment.BLUETOOTH_CONNECTED)


        if (bluetoothConnected == "OK"){
            m_address = intent.getStringExtra(BlueToothFragment.EXTRA_ADDRESS).toString()
            if(m_address != checkAddress){
                ConnectToDevice(requireActivity()).execute()
            }
            checkAddress = m_address

        }else if(bluetoothConnected == null){
            toast("블루투스 연결을 해주세요.")
        }else {
            toast("이건 무슨 에러지..?")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()
    }

    fun setOnClickListener() {

    }

    fun animationMainButton(sine: Boolean) {
        if (sine) {
            val anim = TranslateAnimation(main_button_success.width.toFloat(), 0f, 0f, 0f)
            anim.duration = 4000
            anim.fillAfter = true
            main_frame_layout.animation = anim
            main_button_success.visibility = View.VISIBLE
        } else {
            val anim = TranslateAnimation(0f, main_button_success.width.toFloat(), 0f, 0f)
            anim.duration = 4000
            anim.fillAfter = true
            main_frame_layout.animation = anim
            main_button_success.visibility = View.GONE
        }
    }

    fun sendSimulink() {

    }

    fun sendArduino() {
        sendCommand(command = "A")
    }


    private fun sendCommand(command: String) {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(command.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context = c

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "coudln't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}