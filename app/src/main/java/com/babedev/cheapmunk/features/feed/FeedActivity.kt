package com.babedev.cheapmunk.features.feed

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.domain.model.Post
import com.babedev.cheapmunk.features.feed.adapter.PostAdapter
import com.babedev.cheapmunk.features.scanprice.ScanActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.button
import org.jetbrains.anko.dip
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.onClick
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.textColor
import org.jetbrains.anko.verticalLayout
import java.util.*

class FeedActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()!!
    val postRef = database.getReference("post")!!

    val posts = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        swipeRefreshLayout {
            onRefresh {
                id = R.id.swipe
                refresh()
            }
            verticalLayout {
                recyclerView {
                    lparams(width = matchParent, height = dip(0), weight = 1F)
                    id = R.id.feeds
                    layoutManager = LinearLayoutManager(this@FeedActivity)
                    adapter = PostAdapter(posts)
                }
                button("Scan") {
                    textColor = Color.BLACK
                    backgroundColor = ContextCompat.getColor(context, R.color.yellow)
                    onClick {
                        startActivity(intentFor<ScanActivity>())
                    }
                }
            }
        }

        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                // Do nothing
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val post = it.getValue(Post::class.java)
                    post.id = it.key
                    posts.add(post)
                }

                (findViewById(R.id.feeds) as RecyclerView).adapter.notifyDataSetChanged()
            }
        })
    }

    private fun refresh() {
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                // Do nothing
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                posts.clear()

                snapshot.children.forEach {
                    val post = it.getValue(Post::class.java)
                    post.id = it.key
                    posts.add(post)
                }

                (findViewById(R.id.feeds) as RecyclerView).adapter.notifyDataSetChanged()
                (findViewById(R.id.swipe) as SwipeRefreshLayout).isRefreshing = false
            }
        })
    }
}
