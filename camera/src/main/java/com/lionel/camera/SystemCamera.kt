package com.lionel.camera

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Size
import android.view.TextureView
import android.view.View

/**
 * 系统相机api
 */
class SystemCamera(override val cfg: CameraConfig) : AbstractCamera {

    private var cam: Camera? = null
    private var cb: PreviewCallback? = null
    private var surfaceTexture: SurfaceTexture? = null
    private val running get() = cam != null

    override fun createView(context: Context): View {
        val view = TextureView(context)
        view.right = cfg.widthInView
        view.bottom = cfg.heightInView
        view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                setSurfaceTexture(surface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                setSurfaceTexture(null)
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
        return view
    }

    private fun setSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        if (this.surfaceTexture == surfaceTexture) return
        this.surfaceTexture = surfaceTexture
        if (!running) return
        if (surfaceTexture == null) {
            cam?.stopPreview()
        } else {
            cam?.setPreviewTexture(surfaceTexture)
            cam?.startPreview()
        }
    }

    override fun start() {
        check(cfg.camId.toInt() in 0..Camera.getNumberOfCameras()) {
            "invalid camera id:${cfg.camId}"
        }

        try {
            cam = Camera.open(cfg.camId.toInt())

            setParameters()

            setDisplayOrientation()

            cam?.setPreviewCallback { data, _ ->
                cb?.onPreview(data, cfg.previewWidth, cfg.previewHeight)
            }

            if (surfaceTexture != null) {
                cam?.setPreviewTexture(surfaceTexture)
                cam?.startPreview()
            }
        } catch (e: Exception) {
            stop()
            throw e
        }
    }

    private fun setDisplayOrientation() = this.cam?.apply {
        setDisplayOrientation(cfg.displayOrientation)
    }

    private fun setParameters() = this.cam?.apply {
        val parameters = this.parameters
        parameters.previewFormat = ImageFormat.NV21
        var sizeOk = false
        for (size in parameters.supportedPreviewSizes) {
            if (size.width == cfg.previewWidth && size.height == cfg.previewHeight) {
                parameters.setPreviewSize(cfg.previewWidth, cfg.previewHeight)
                sizeOk = true
                break
            }
        }
        check(sizeOk) { "invalid preview size:${cfg.previewWidth} x ${cfg.previewHeight}" }
        this.parameters = parameters
    }

    override fun stop() {
        cam?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
        }
        cam = null
    }

    override fun setPreviewCallback(callback: PreviewCallback) {
        this.cb = callback
    }

    override fun getSupportPreviewSizes(): List<Size>? {
        return cam?.parameters?.supportedPreviewSizes?.map {
            Size(it.width, it.height)
        }
    }

    private val CameraConfig.widthInView: Int
        get() = when (displayOrientation) {
            0, 180 -> previewWidth
            else -> previewHeight
        }

    private val CameraConfig.heightInView: Int
        get() = when (displayOrientation) {
            0, 180 -> previewHeight
            else -> previewWidth
        }
}