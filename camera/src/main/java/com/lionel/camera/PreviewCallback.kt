package com.lionel.camera

interface PreviewCallback {
    fun onPreview(data: ByteArray, width: Int, height: Int)
}