package com.babedev.cheapmunk.features.scanprice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.widget.barcode.BarcodeGraphic
import com.babedev.cheapmunk.widget.barcode.BarcodeTrackerFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSource
import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_scan.*
import java.io.IOException

class ScanActivity : AppCompatActivity() {

    private val TAG = "BarCode"
    private val RC_HANDLE_GMS = 9001
    private val RC_HANDLE_CAMERA_PERM = 2

    private lateinit var mCameraSource: CameraSource
    private lateinit var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        mGraphicOverlay = findViewById(R.id.graphicOverlay) as GraphicOverlay<BarcodeGraphic>
        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true)
        } else {
            requestCameraPermission()
        }
    }

    override fun onResume() {
        super.onResume()

        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            startCameraSource()
        } else {
            requestCameraPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        if (preview != null) {
            preview.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) {
            preview.release()
        }
    }

    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
    }

    private fun createCameraSource(autoFocus: Boolean) {
        val context = applicationContext
        val barcodeDetector = BarcodeDetector.Builder(context).build()
        val barcodeFactory = BarcodeTrackerFactory(mGraphicOverlay, fun(barcode: Barcode?) {
            val intent = Intent(this, FetchPriceActivity::class.java)
            intent.putExtra("barcode", barcode)
            startActivity(intent)
            finish()
        })

        barcodeDetector.setProcessor(MultiProcessor.Builder(barcodeFactory).build())

        var builder: CameraSource.Builder = CameraSource.Builder(applicationContext, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null)
        }

        mCameraSource = builder.build()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(applicationContext)

        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

        try {
            preview.start(mCameraSource, mGraphicOverlay)
        } catch (e: IOException) {
            Log.e(TAG, "Unable to start camera source.", e)
            mCameraSource.release()
        }
    }
}
