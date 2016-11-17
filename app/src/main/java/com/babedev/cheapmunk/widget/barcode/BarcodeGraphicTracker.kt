package com.babedev.cheapmunk.widget.barcode

import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode

/**
 * @author BabeDev
 */
class BarcodeGraphicTracker(val overlay: GraphicOverlay<BarcodeGraphic>,
                            val graphic: BarcodeGraphic,
                            val onDetected: (barcode: Barcode?) -> Unit) : Tracker<Barcode>() {

    override fun onNewItem(id: Int, item: Barcode?) {
        graphic.id = id
    }

    override fun onUpdate(detectionResults: Detector.Detections<Barcode>?, item: Barcode?) {
        overlay.add(graphic)
        graphic.updateItem(item)
        onDetected(item)
    }

    override fun onMissing(detectionResults: Detector.Detections<Barcode>?) {
        overlay.remove(graphic)
    }

    override fun onDone() {
        overlay.remove(graphic)
    }
}