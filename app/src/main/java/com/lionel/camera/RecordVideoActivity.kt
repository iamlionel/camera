package com.lionel.camera

import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.media.MediaRecorder
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class RecordVideoActivity : AppCompatActivity() {
    private var camera: Camera? = null
    private lateinit var surfaceView: SurfaceView
    private var mediaRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video)

        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                openCamera(holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }
        })
        val btnRecord = findViewById<Button>(R.id.btnRecord)
        btnRecord.setOnClickListener {
            startRecord()
        }
    }

    private fun openCamera(holder: SurfaceHolder) {
        val id = 0
        camera = Camera.open(id)

        val parameters = camera?.parameters
        parameters?.setPreviewSize(640, 480)
        camera?.parameters = parameters

        camera?.setDisplayOrientation(getDisplayOrientation(id))
        camera?.setPreviewDisplay(holder)
        camera?.startPreview()
    }

    private fun startRecord() {
        camera?.unlock()
        mediaRecorder = MediaRecorder()
        mediaRecorder?.let { mediaRecorder ->
            mediaRecorder.setCamera(camera)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder.setVideoSize(640, 480)
            mediaRecorder.setVideoFrameRate(24)

            mediaRecorder.setOrientationHint(90)
            val path = externalCacheDir?.path + "/test.mp4"
            mediaRecorder.setOutputFile(path)
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
            mediaRecorder.setPreviewDisplay(surfaceView.holder.surface)
            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getDisplayOrientation(id: Int): Int {
        val info = CameraInfo()
        Camera.getCameraInfo(id, info)
        val degree = when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degree) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degree + 360) % 360
        }
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
        camera?.release()
        camera = null
    }
}