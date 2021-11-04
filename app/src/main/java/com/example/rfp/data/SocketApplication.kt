package com.example.rfp.data

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException


class SocketApplication {
    companion object{
        private lateinit var socket: Socket
        fun get(): Socket{
            try {
                socket = IO.socket("http://3.36.64.198:8080/")
            } catch (e : URISyntaxException){
                Log.d("url", e.toString())
                e.printStackTrace()
            }
            return socket
        }
    }
}