package com.lionel.camera

import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import android.view.Surface
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var camera: DoubleCamera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nV21ToBitmap = NV21ToBitmap(this)
        val flContainer: FrameLayout = findViewById(R.id.flContainer)
        val ivData: ImageView = findViewById(R.id.ivData)
        val id = "0"
//        val id = "1"
        val camCfg = CameraConfig(
            id,
            640,
            480,
            displayOrientation = getDisplayOrientation(id.toInt()),
            camType = CamType.CAMERA
        )
        camera = DoubleCamera(this, camCfg)
        camera?.setPreviewCallback(object : PreviewCallback {
            override fun onPreview(data: ByteArray, width: Int, height: Int) {
                ivData.setImageBitmap(nV21ToBitmap.nv21ToBitmap(data, width, height))
            }
        }, null)
        flContainer.removeAllViews()
        flContainer.addView(camera?.preview)
        camera?.start()
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
        camera?.stop()
    }
}