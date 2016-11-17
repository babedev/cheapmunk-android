package com.babedev.cheapmunk.features.scanprice

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.features.ask.AskActivity
import com.babedev.cheapmunk.utils.barcode
import com.google.android.gms.vision.barcode.Barcode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_fetchprice.*
import kotlinx.android.synthetic.main.item_barcode_price.view.*
import java.util.*

class FetchPriceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fetchprice)

        val database = FirebaseDatabase.getInstance()!!
        val barcodeRef = database.getReference("barcode")!!

        val bc = intent.getParcelableExtra<Barcode>("barcode")
        barcode.text = "${bc.format.barcode()} ${bc.rawValue}"

        barcodeRef.child(bc.rawValue).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val prices = snapshot.child("prices")
                    val mapPrices = HashMap<Double, Int>()

                    if (prices.exists()) {
                        var min = 9999999.0
                        var max = 0.0
                        var sum = 0.0

                        prices.children.forEach {
                            val price = it.value.toString().toDouble()
                            val mapPrice = mapPrices[price]

                            if (mapPrice != null) {
                                mapPrices[price] = mapPrice + 1
                            } else {
                                mapPrices[price] = 1
                            }

                            if (min > price) {
                                min = price
                            }

                            if (max < price) {
                                max = price
                            }

                            sum += price
                        }

                        average.text = (sum / prices.children.count()).toString()

                        for ((key, value) in mapPrices) {
                            val progress = View.inflate(this@FetchPriceActivity, R.layout.item_barcode_price, null)
                            progress.price.text = key.toString()
                            progress.value.max = 2
                            progress.value.progress = value
                            top_prices.addView(progress)
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                // Do nothing
            }
        })
    }

    fun ask(v: View) {
        val intent = Intent(this, AskActivity::class.java)
        intent.putExtra("barcode", barcode.text.toString())
        startActivity(intent)
    }
}
