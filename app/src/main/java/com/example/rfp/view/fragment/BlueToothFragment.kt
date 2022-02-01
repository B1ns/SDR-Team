package com.example.rfp.view.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.rfp.R
import com.example.rfp.databinding.FragmentBlueToothBinding
import com.example.rfp.view.activity.MainActivity
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.kakao.sdk.newtoneapi.TextToSpeechClient
import com.kakao.sdk.newtoneapi.TextToSpeechListener
import com.kakao.sdk.newtoneapi.TextToSpeechManager
import org.jetbrains.anko.support.v4.toast
import java.io.IOException


class BlueToothFragment : Fragment() {

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }

    private var m_bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1

    private var _binding: FragmentBlueToothBinding? = null
    private val binding get() = _binding!!


    var ttsClient: TextToSpeechClient? = null
    private val mainFragmentChange: MainFragment = MainFragment()

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
            SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
            TextToSpeechManager.getInstance().initializeLibrary(requireContext())

            ttsClient?.play("이 장치는 블루투스를 지원하지 않습니다.")
            toast("이 장치는 블루투스를 지원하지 않습니다..ㅜ")
            return
        }
        if (!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val search = { _: View ->
            toast("검색..! 찾았다 !")
            search()
        }
        val lottie = { _: View ->
            toast("오잉.. 연결이 해제됬다 !")
            disconnect()
        }

        ttsClient = TextToSpeechClient.Builder()
            .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
            .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
            .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
            .setListener(object : TextToSpeechListener {
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

        binding.bluetoothLottie.setOnClickListener(lottie)
        binding.searchBtn.setOnClickListener(search)
    }

    private fun search() {
        SpeechRecognizerManager.getInstance().initializeLibrary(requireContext())
        TextToSpeechManager.getInstance().initializeLibrary(requireContext())

        ttsClient?.play("기기를 검색합니다.")
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices

        val deviceList: ArrayList<BluetoothDevice> = ArrayList()
        val nameList: ArrayList<String> = ArrayList()

        if (m_pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                deviceList.add(device)
                nameList.add(device.name + "(" + device.address + ")")
                Log.i("device", "" + device.name)
            }
        } else {
            toast("페어링된 장치를 찾을 수 없어요.. 잘 좀 연결해봐..")
        }


        val listAdapter = ArrayAdapter(
            activity?.applicationContext!!,
            R.layout.bluetooth_list_item,
            R.id.bluetooth_text_item,
            nameList
        )
        binding.bluetoothList.adapter = listAdapter

        binding.bluetoothList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = deviceList[position]
                val address: String = device.address

                setFragmentResult("resultKey", bundleOf("bundleKey" to address))

                val mainActivity = (activity as MainActivity)
                mainActivity.changeUI()
                mainActivity.openFragment(mainFragmentChange)
            }
    }

    private fun disconnect() {
        if (MainFragment.m_bluetoothSocket != null) {
            try {
                MainFragment.m_bluetoothSocket!!.close()
                MainFragment.m_bluetoothSocket = null
                MainFragment.m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            toast("연결하고 눌러줄래..? 해제할게 없자나..")
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