package com.lionel.camera

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var camera: DoubleCamera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val flContainer: FrameLayout = findViewById(R.id.flContainer)
        val camCfg = CameraConfig("1", 640, 480, camType = CamType.CAMERA)
        val camCfg2 = CameraConfig("0", 640, 480, camType = CamType.CAMERA)
        camera = DoubleCamera(this, camCfg)
        flContainer.removeAllViews()
        flContainer.addView(camera?.preview)
        camera?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.stop()
    }
}