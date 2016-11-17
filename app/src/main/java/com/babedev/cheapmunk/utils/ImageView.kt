package com.babedev.cheapmunk.utils

import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * @author BabeDev
 */
fun ImageView.loadUrl(url: String) {
    Glide.with(context).load(url).into(this)
}
