package com.babedev.cheapmunk.utils

/**
 * @author BabeDev
 */
fun Int.barcode(): String {
    when (this) {
        32 -> return "EAN 13"
        512 -> return "UPC A"
        else -> return ""
    }
}