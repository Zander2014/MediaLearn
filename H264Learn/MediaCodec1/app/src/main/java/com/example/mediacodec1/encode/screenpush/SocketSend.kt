package com.example.mediacodec1.encode.screenpush

import android.media.projection.MediaProjection
import android.util.Log
import com.example.mediacodec1.decode.screenplay.SocketReceive
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class SocketSend(private val mediaProjection: MediaProjection) {
    var webSocket: WebSocket? = null
    lateinit var h264Encoder: H264Encoder
    fun start() {
        webSocketServer.start()
    }

    fun sendData(byteArray: ByteArray){
        webSocket?.apply{
            if(isOpen){
                send(byteArray)
            }
        }
    }

    fun close(){
        webSocket?.close()
        webSocketServer.stop()
    }

    private fun putSocket(conn: WebSocket?){
        this.webSocket = conn
        h264Encoder = H264Encoder(this, mediaProjection)
        h264Encoder.startEncode()
    }
    val webSocketServer = object : WebSocketServer(InetSocketAddress(9007)){
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            Log.i(SocketReceive.TAG, "onOpen: ")
            putSocket(conn)
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            Log.i(SocketReceive.TAG, "onClose: ")
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            Log.i(SocketReceive.TAG, "onMessage: ")
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            Log.i(SocketReceive.TAG, "onError: ")
        }

        override fun onStart() {

        }

    }
}