package com.lionel.camera

import android.content.Context

/**
 * 可同时打开两个摄像头
 */
class DoubleCamera internal constructor(
    context: Context,
    private val cam1: AbstractCamera,
    private val cam2: AbstractCamera? = null
) {
    constructor(context: Context, cfg1: CameraConfig) : this(context, cfg1, null)

    constructor(context: Context, cfg1: CameraConfig, cfg2: CameraConfig?) : this(
        context,
        when (cfg1.camType) {
            CamType.CAMERA -> SystemCamera(cfg1)
            CamType.CAMERA2 -> System2Camera(context,cfg1)
            CamType.UVC -> SystemCamera(cfg1)
        },
        when (cfg2?.camType) {
            CamType.CAMERA -> SystemCamera(cfg2)
            CamType.CAMERA2 -> System2Camera(context,cfg2)
            CamType.UVC -> SystemCamera(cfg2)
            else -> null
        }
    )

    val preview = CameraPreview(context, cam1.createView(context), cam2?.createView(context))

    fun start() {
        cam1.start()
        cam2?.start()
    }

    fun stop() {
        cam1.stop()
        cam2?.stop()
    }
}