package com.babedev.cheapmunk.widget.barcode

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.barcode.Barcode

/**
 * @author BabeDev
 */
class BarcodeGraphic(overlay: GraphicOverlay<BarcodeGraphic>) : GraphicOverlay.Graphic(overlay) {

    var id: Int = 0
    var barcode: Barcode? = null

    private val COLOR_CHOICES = intArrayOf(Color.BLUE, Color.CYAN, Color.GREEN)

    private var mCurrentColorIndex = 0

    private var mRectPaint: Paint
    private var mTextPaint: Paint

    init {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]
        mRectPaint = Paint()
        mRectPaint.color = selectedColor
        mRectPaint.style = Paint.Style.STROKE
        mRectPaint.strokeWidth = 4.0f
        mTextPaint = Paint()
        mTextPaint.color = selectedColor
        mTextPaint.textSize = 36.0f
    }

    fun updateItem(barcode: Barcode?) {
        this.barcode = barcode
        postInvalidate()
    }

    override fun draw(canvas: Canvas) {
        // Draws the bounding box around the barcode.
        val rect = RectF(barcode?.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, mRectPaint)

        // Draws a label at the bottom of the barcode indicate the barcode value that was detected.
        canvas.drawText(barcode?.rawValue, rect.left, rect.bottom, mTextPaint)
    }
}