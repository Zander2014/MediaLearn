package com.example.mediacodec1.encode.camera1

import android.content.Context
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class LocalSurfaceView(context: Context, attr: AttributeSet) : SurfaceView(context, attr), SurfaceHolder.Callback, Camera.PreviewCallback{
    var h264Encode: H264Encode? = null
    lateinit var mCamera: Camera
    lateinit var size: Camera.Size

    lateinit var buffer: ByteArray
    init {
        holder.addCallback(this)
    }

    private fun startPreview(){
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        size = mCamera.parameters.previewSize

        mCamera.setPreviewDisplay(holder)
        mCamera.setDisplayOrientation(0)

        buffer = ByteArray(size.width * size.height * 3 / 2)//Camera采集的数据，YUV一帧的大小
        mCamera.addCallbackBuffer(buffer)
        mCamera.setPreviewCallbackWithBuffer(this)
        mCamera.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if(h264Encode == null){
            h264Encode = H264Encode(size.width, size.height)
            h264Encode?.startLive()
        }
        //编码
        h264Encode?.encodeFrame(data!!)
        mCamera.addCallbackBuffer(data)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        startPreview()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }
}