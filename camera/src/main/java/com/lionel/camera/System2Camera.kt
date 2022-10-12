package com.lionel.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import java.util.Arrays

/**
 * 系统相机新api实现
 */
@SuppressLint("MissingPermission", "NewApi")
class System2Camera(private val context: Context, override val cfg: CameraConfig) : AbstractCamera {

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private lateinit var cameraManager: CameraManager
    private var mCameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private var previewSizes = arrayOf<Size>()
    private var mCaptureSession: CameraCaptureSession? = null
    private var surfaceTexture: SurfaceTexture? = null

    private var cb: PreviewCallback? = null

    override fun createView(context: Context): View {
        Log.i("flf","createView")
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
                createCameraPreviewSession()
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
        this.surfaceTexture = surfaceTexture
    }

    override fun start() {
        try {
            mBackgroundThread = HandlerThread("CameraBackground")
            mBackgroundThread!!.start()
            mBackgroundHandler = Handler(mBackgroundThread!!.looper)

            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            setCameraParameters()

            imageReader =
                ImageReader.newInstance(cfg.previewWidth, cfg.previewHeight, ImageFormat.YUV_420_888, 2)
            imageReader!!.setOnImageAvailableListener({

            }, mBackgroundHandler)

            cameraManager.openCamera(cfg.camId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    mCameraDevice = camera
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    mCameraDevice?.close()
                    mCameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    mCameraDevice?.close()
                    mCameraDevice = null
                }

            }, null)
        } catch (e: Exception) {
            stop()
            throw e
        }
    }

    private fun createCameraPreviewSession() {
        try {
            if (surfaceTexture == null) {
                return
            }
            val mPreviewBuilder =
                mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val surface = Surface(surfaceTexture)
            mPreviewBuilder?.addTarget(surface)

            mCameraDevice!!.createCaptureSession(
                Arrays.asList(surface,imageReader!!.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (null == mCameraDevice) {
                            return
                        }

                        // When the session is ready, we start displaying the preview.
                        mCaptureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            mPreviewBuilder?.set<Int>(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Flash is automatically enabled when necessary.

                            // Finally, we start displaying the camera preview.
                            mCaptureSession?.setRepeatingRequest(
                                mPreviewBuilder?.build()!!,
                                null, mBackgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(
                        cameraCaptureSession: CameraCaptureSession
                    ) {
                        Log.d("DeviceCamera2", "onConfigureFailed: ")
                    }
                }, mBackgroundHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCameraParameters() {
        val cameraCharacteristics = cameraManager.getCameraCharacteristics(cfg.camId)
        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        previewSizes = map!!.getOutputSizes(ImageFormat.YUV_420_888)
        var sizeOk = false
        for (size in previewSizes) {
            if (size.width == cfg.previewWidth && size.height == cfg.previewHeight) {
                sizeOk = true
                break
            }
        }
        check(sizeOk) { "invalid preview size: ${cfg.previewWidth} x ${cfg.previewHeight}" }
    }

    override fun stop() {
        try {
            mCaptureSession?.close()
            mCaptureSession = null

            Log.i("flf","stop")
            mCameraDevice?.close()
            mCameraDevice = null

            mBackgroundThread?.quitSafely()
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null

            cb = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun setPreviewCallback(callback: PreviewCallback) {
        this.cb = callback
    }

    override fun getSupportPreviewSizes(): List<Size> {
        return previewSizes.asList()
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