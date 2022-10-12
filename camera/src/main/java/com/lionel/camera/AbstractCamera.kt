package com.lionel.camera

import android.content.Context
import android.util.Size
import android.view.View

interface AbstractCamera {
    val cfg: CameraConfig

    fun start()

    fun stop()

    fun createView(context: Context): View

    fun setPreviewCallback(callback: PreviewCallback)

    fun getSupportPreviewSizes(): List<Size>?
}

