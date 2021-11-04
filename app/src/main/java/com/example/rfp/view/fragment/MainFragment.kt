package com.example.rfp.view.fragment

import android.animation.Animator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.fragment.app.setFragmentResultListener
import com.example.rfp.data.SocketApplication
import com.example.rfp.databinding.FragmentMainBinding
import com.example.rfp.view.activity.MainActivity
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.IOException
import java.util.*
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.jetbrains.anko.support.v4.toast


class MainFragment : Fragment() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    lateinit var mSocket: Socket
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val graphFragmentChange: GraphFragment = GraphFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        var resultDeviceAddress: String

        setFragmentResultListener("resultKey") { requestKey, bundle ->
            resultDeviceAddress = bundle.getString("bundleKey").toString()

            toast("ok")

            m_address = resultDeviceAddress
            ConnectToDevice().execute()
        }

        mSocket = SocketApplication.get()
        mSocket.connect()

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("data", onConnect)

        mSocket.emit("login", "android")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()
    }

    private fun setOnClickListener() {

        binding.ePlane.setOnClickListener {
            e_plane.visibility = View.GONE

            main_first_progress.visibility = View.VISIBLE
            main_first_progress.playAnimation()

            mSocket.emit("send", 1)
            mSocket.emit("logout", "logout")
            sendArduino()

        }
        binding.hPlane.setOnClickListener {
            h_plane.visibility = View.GONE
            main_second_progress.visibility = View.VISIBLE
            main_second_progress.playAnimation()

            mSocket.emit("send", 2)
            mSocket.emit("logout", "logout")
            sendArduino()

            main_second_progress.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator?) {
                    TODO("Not yet implemented")
                }

                override fun onAnimationEnd(p0: Animator?) {
                    animationStart()
                }

                override fun onAnimationCancel(p0: Animator?) {
                    TODO("Not yet implemented")
                }

                override fun onAnimationRepeat(p0: Animator?) {
                    TODO("Not yet implemented")
                }

            })

        }

        main_success.setOnClickListener {
            val mainActivity = (activity as MainActivity)
            mainActivity.successUI()
            mainActivity.openFragment(graphFragmentChange)
        }
    }

    fun animationStart() {

        main_button_layout_one.visibility = View.GONE
        main_button_layout_two.visibility = View.GONE

        animationMainButton(true)
    }

    fun animationMainButton(sine: Boolean) {
        if (sine) {
            val anim = TranslateAnimation(0f, 0f, main_button_success.height.toFloat(), 0f)
            anim.duration = 500
            anim.fillAfter = true
            main_frame_layout.animation = anim
            main_button_success.visibility = View.VISIBLE
        } else {
            val anim = TranslateAnimation(0f, main_button_success.width.toFloat(), 0f, 0f)
            anim.duration = 500
            anim.fillAfter = true
            main_frame_layout.animation = anim
            main_button_success.visibility = View.GONE
        }
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

    val onConnect: Emitter.Listener = Emitter.Listener {
        Log.d("on", "connect")
    }

    @SuppressLint("StaticFieldLeak")
    inner class ConnectToDevice : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun onPreExecute() {
            super.onPreExecute()
            toast("연결 중..")
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
            toast("연결 성공")
        }
    }
}