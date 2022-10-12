package com.lionel.camera

import android.content.Context
import android.view.View
import android.widget.RelativeLayout

class CameraPreview constructor(
    context: Context,
    private val firstView: View,
    private val secondView: View?
) : RelativeLayout(context) {

    private val firstLayout = LayoutParams(0, 0).apply { addRule(CENTER_IN_PARENT) }
    private val secondLayout = LayoutParams(0, 0).apply {
        addRule(ALIGN_PARENT_RIGHT);addRule(ALIGN_PARENT_BOTTOM)
    }

    init {
        addView(firstView, firstLayout)
        secondView?.let {
            addView(secondView, secondLayout)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        updateView(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }

    private fun updateView(width: Int, height: Int) {
        if (width * height == 0) return
        layoutFirstView(width, height)
        secondView?.let {
            layoutSecondView(width, height)
        }
    }

    private fun layoutFirstView(width: Int, height: Int) {
        val scale = minOf(width.toFloat() / firstView.width, height.toFloat() / firstView.height)
        firstLayout.width = (firstView.width * scale).toInt()
        firstLayout.height = (firstView.height * scale).toInt()
    }

    private fun layoutSecondView(width: Int, height: Int) {
        val scale = minOf(width.toFloat() / secondView!!.width, height.toFloat() / secondView.height)
        secondLayout.width = (secondView.width * scale / 4).toInt()
        secondLayout.height = (secondView.height * scale / 4).toInt()
    }
}