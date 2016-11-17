package com.babedev.cheapmunk.features.feed.adapter

import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.RelativeLayout
import android.widget.TextView
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.domain.model.Post
import com.babedev.cheapmunk.features.feed.PostDetailActivity
import com.babedev.cheapmunk.features.feed.adapter.PostAdapter.PostHolder
import com.babedev.cheapmunk.utils.loadUrl
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoContext.Companion
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.view
import org.jetbrains.anko.wrapContent
import org.parceler.Parcels
import java.util.ArrayList

/**
 * @author BabeDev
 */
class PostAdapter(val posts: ArrayList<Post>) : RecyclerView.Adapter<PostHolder>() {

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        return PostHolder(PostItemUI().createView(Companion.create(parent.context, parent)))
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class PostItemUI : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            return with(ui) {
                cardView {
                    layoutParams = RelativeLayout.LayoutParams(matchParent, wrapContent).apply {
                        marginStart = dip(16)
                        marginEnd = dip(16)
                        topMargin = dip(8)
                        bottomMargin = dip(8)
                    }

                    verticalLayout {
                        lparams(width = matchParent)

                        textView {
                            id = R.id.product
                            textColor = Color.BLACK
                            padding = dip(16)
                        }
                        imageView {
                            lparams(width = matchParent, height = dip(188))
                            id = R.id.image
                            scaleType = CENTER_CROP
                        }
                        linearLayout {
                            padding = dip(8)

                            textView {
                                padding = dip(8)
                                id = R.id.avg
                                textColor = Color.BLACK
                                textSize = 28F
                            }
                            view {
                                lparams(width = dip(1), height = matchParent)
                                backgroundColor = ContextCompat.getColor(context, R.color.yellow)
                            }
                            verticalLayout {
                                layoutParams.apply {
                                    leftPadding = dip(8)
                                }

                                textView {
                                    id = R.id.response
                                    textColor = Color.BLACK
                                }
                                linearLayout {
                                    layoutParams.apply {
                                        topPadding = dip(8)
                                    }

                                    textView {
                                        id = R.id.min
                                        textColor = Color.BLACK
                                    }
                                    textView {
                                        layoutParams.apply {
                                            leftPadding = dip(16)
                                        }

                                        id = R.id.max
                                        textColor = Color.BLACK
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class PostHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvAvg = itemView.findViewById(R.id.avg) as TextView
        val tvMin = itemView.findViewById(R.id.min) as TextView
        val tvMax = itemView.findViewById(R.id.max) as TextView
        val tvResponse = itemView.findViewById(R.id.response) as TextView
        val tvProduct = itemView.findViewById(R.id.product) as TextView
        val ivImage = itemView.findViewById(R.id.image) as ImageView

        fun bind(post: Post) {
            ivImage.loadUrl(post.imageUrl)

            ivImage.setOnClickListener {
                val intent = Intent(ivImage.context, PostDetailActivity::class.java)
                intent.putExtra("post", Parcels.wrap(post))
                ivImage.context.startActivity(intent)
            }

            tvProduct.text = post.product

            val database = FirebaseDatabase.getInstance()!!
            val postRef = database.getReference("post")!!
            val priceRef = postRef.child("${post.id}/prices")

            priceRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    // Do nothing
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var min = 9999999.0
                        var max = 0.0
                        var sum = 0.0

                        snapshot.children.forEach {
                            val price = it.value.toString().toDouble()

                            if (min > price) {
                                min = price
                            }

                            if (max < price) {
                                max = price
                            }

                            sum += price
                        }

                        tvAvg.text = "%.2f".format(sum / snapshot.children.count())
                        tvMin.text = "Min $min"
                        tvMax.text = "Max $max"
                        tvResponse.text = "${snapshot.children.count()} responses"
                    }
                }
            })
        }

    }
}