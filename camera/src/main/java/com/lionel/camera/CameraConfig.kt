package com.lionel.camera

data class CameraConfig(
    var camId: String,
    var previewWidth: Int,
    var previewHeight: Int,
    val dataRotation: Int = 0,
    val dataMirror: Boolean = false,
    val displayOrientation: Int = 0,
    val previewMirror: Boolean = false,
    var camType: CamType = CamType.CAMERA
)

enum class CamType {
    //系统相机api
    CAMERA,

    //系统相机新api
    CAMERA2,

    //uvc相机
    UVC
}