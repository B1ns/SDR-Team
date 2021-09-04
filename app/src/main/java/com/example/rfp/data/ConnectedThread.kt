package com.example.rfp.data

import android.bluetooth.BluetoothSocket
import android.os.SystemClock
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(socket: BluetoothSocket) : Thread(){

    private var mmSocket : BluetoothSocket = socket
    private lateinit var mmInStream : InputStream
    private lateinit var mmOutStream : OutputStream

    init {
        var tmpIn : InputStream? = null
        var tmpOut : OutputStream? = null

        try {
            tmpIn = socket.inputStream
            tmpOut = socket.outputStream

        }catch (e : IOException){
        }

        mmInStream = tmpIn!!
        mmOutStream = tmpOut!!
    }

    override fun run() {
        var buffer = ByteArray(1024)
        var bytes : Int

        while(true){
            try {
                bytes = mmInStream.available()
                if (bytes != 0){
                    buffer = ByteArray(1024)
                    SystemClock.sleep(100)
                    bytes = mmInStream.available()
                    bytes = mmInStream.read(buffer, 0 , bytes)
                }

            }catch (e : IOException){
                e.printStackTrace()
                break
            }
        }
    }

    fun send(input : String){
        val bytes: ByteArray = input.toByteArray()
        try {
            mmOutStream.write(bytes)
        }catch (e : IOException){
            e.printStackTrace()
        }
    }

    fun cancel(){
        try {
            mmSocket.close()
        }catch (e : IOException){
            e.printStackTrace()
        }
    }
}