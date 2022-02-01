package com.example.rfp.view.fragment

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import com.example.rfp.data.SocketApplication
import com.example.rfp.databinding.FragmentMainBinding
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.kakao.sdk.newtoneapi.TextToSpeechClient
import com.kakao.sdk.newtoneapi.TextToSpeechListener
import com.kakao.sdk.newtoneapi.TextToSpeechManager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_main.*
import org.jetbrains.anko.support.v4.toast
import java.io.IOException
import java.util.*


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
    var ttsClient: TextToSpeechClient? = null

    private var checkedButtonState = 0

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

        mSocket.emit("login", "ACCESS_OK_RESULT_OK")

        checkedButtonState = 0

        ttsClient = TextToSpeechClient.Builder()
            .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
            .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
            .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
            .setListener(object : TextToSpeechListener {
                //아래 두개의 메소드만 구현해 주면 된다. 음성합성이 종료될 때 호출된다.
                override fun onFinished() {
                    val intSentSize = ttsClient?.getSentDataSize()      //세션 중에 전송한 데이터 사이즈
                    val intRecvSize = ttsClient?.getReceivedDataSize()  //세션 중에 전송받은 데이터 사이즈

                    val strInacctiveText =
                        "handleFinished() SentSize : $intSentSize  RecvSize : $intRecvSize"

                    Log.i("TAG", strInacctiveText)
                }

                override fun onError(code: Int, message: String?) {
                    Log.d("TAG", code.toString())
                }
            })
            .build()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        TextToSpeechManager.getInstance().finalizeLibrary()
    }

    private fun setOnClickListener() {

        refresh_fab.setOnClickListener {
            val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
            ft.detach(this).attach(this).commit()
        }

        binding.ePlane.setOnClickListener {

            SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
            TextToSpeechManager.getInstance().initializeLibrary(requireContext())

            ttsClient?.play("측정을 시작합니다.")


            e_plane.visibility = View.GONE

            main_first_progress.visibility = View.VISIBLE
            main_first_progress.playAnimation()

            mSocket.emit("send", 1)
            sendArduino()

            checkedButtonState += 1

            if (checkedButtonState == 2) {
                checkedButton()
            }

        }
        binding.hPlane.setOnClickListener {
            SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
            TextToSpeechManager.getInstance().initializeLibrary(requireContext())

            ttsClient?.play("측정을 시작합니다.")


            h_plane.visibility = View.GONE
            main_second_progress.visibility = View.VISIBLE
            main_second_progress.playAnimation()

            mSocket.emit("send", 2)
            sendArduino()

            checkedButtonState += 1

            if (checkedButtonState == 2) {
                checkedButton()
            }
        }

//        main_success.setOnClickListener {
//            val mainActivity = (activity as MainActivity)
//            mainActivity.successUI()
//            mainActivity.openFragment(graphFragmentChange)
//        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun checkedButton() {
        val handler: Handler = Handler()
        handler.postDelayed({
            SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
            TextToSpeechManager.getInstance().initializeLibrary(requireContext())

            ttsClient?.play("측정이 완료되었습니다.")
            mSocket.emit("send", 3)

            if (mainWebViewBG.visibility == View.VISIBLE) {
                mainWebViewBG.visibility = View.GONE
            }
            if (main_graph.visibility == View.GONE) {
                main_graph.visibility = View.VISIBLE
            }

            main_graph.loadUrl("http://rfp2022.loca.lt")

            main_graph.settings.javaScriptEnabled = true
            main_graph.settings.allowContentAccess = true
            main_graph.settings.domStorageEnabled = true
            main_graph.settings.useWideViewPort = true
            main_graph.settings.loadWithOverviewMode = true
            main_graph.isHorizontalScrollBarEnabled = false
            main_graph.isVerticalScrollBarEnabled = false
        }, 32000)
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