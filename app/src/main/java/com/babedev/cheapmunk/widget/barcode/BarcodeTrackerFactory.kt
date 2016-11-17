package com.babedev.cheapmunk.widget.barcode

import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode

/**
 * @author BabeDev
 */
class BarcodeTrackerFactory(val barcodeGraphicOverlay: GraphicOverlay<BarcodeGraphic>,
                            val onDetected: (barcode: Barcode?) -> Unit) : MultiProcessor.Factory<Barcode> {

    override fun create(barcode: Barcode): Tracker<Barcode> {
        val graphic = BarcodeGraphic(barcodeGraphicOverlay)
        return BarcodeGraphicTracker(barcodeGraphicOverlay, graphic, onDetected)
    }
}