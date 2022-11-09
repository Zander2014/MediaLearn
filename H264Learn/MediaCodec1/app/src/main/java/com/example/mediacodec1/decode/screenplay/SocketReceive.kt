package com.example.mediacodec1.decode.screenplay

import android.util.Log
import android.view.Surface
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class SocketReceive(val surface: Surface) {
    lateinit var h264Player: H264Player
    lateinit var webSocket: WebSocket

    companion object{
        const val TAG = "zander"
    }
    fun start() {
        webSocketClient.connect()
        h264Player = H264Player(surface)
    }

    private val webSocketClient = object : WebSocketClient(URI("ws://192.168.1.14:9007")){
        override fun onOpen(handshakedata: ServerHandshake?) {
            Log.i(TAG, "打开 socket  onOpen: ");
        }

        override fun onMessage(message: String?) {

        }

        override fun onMessage(bytes: ByteBuffer?) {
            bytes?.apply {

                Log.i(TAG, "消息长度  : "+ remaining())
                val buf = ByteArray(remaining())
                get(buf)
                h264Player.decode(buf)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            Log.i(TAG, "onClose: ");
        }

        override fun onError(ex: Exception?) {
            Log.i(TAG, "onError: ");
        }

    }
}