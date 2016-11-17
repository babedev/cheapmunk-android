package com.babedev.cheapmunk.domain.model

import org.parceler.Parcel

/**
 * @author BabeDev
 */
@Parcel
data class Post(var id: String = "",
                var imageUrl: String = "",
                var barcode: String = "",
                var product: String = "",
                var detail: String = "",
                var userId: String = "")