package com.babedev.cheapmunk.features.feed

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.app.MyApp
import com.babedev.cheapmunk.domain.model.Answer
import com.babedev.cheapmunk.domain.model.Post
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_postdetail.*
import org.parceler.Parcels
import java.io.File
import java.io.IOException
import java.util.*

class PostDetailActivity : AppCompatActivity() {

    var mCurrentPhotoPath = ""

    val database = FirebaseDatabase.getInstance()!!
    val postRef = database.getReference("post")!!
    lateinit var answerRef: DatabaseReference
    lateinit var priceRef: DatabaseReference
    lateinit var barcodePriceRef: DatabaseReference

    lateinit var ivImage: ImageView

    lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postdetail)

        post = Parcels.unwrap(intent.getParcelableExtra("post"))

        product.text = post.product
        detail.text = post.detail
        Glide.with(this).load(post.imageUrl).into(image)

        answerRef = postRef.child(post.id).child("answers")
        priceRef = postRef.child(post.id).child("prices")
        barcodePriceRef = FirebaseDatabase.getInstance().getReference("barcode/${post.barcode}/prices")

        answerRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                // Do nothing
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val comments = ArrayList<Answer>()

                snapshot.children.forEach {
                    comments.add(it.getValue(Answer::class.java))
                }

                with(rv_answers) {
                    layoutManager = LinearLayoutManager(this@PostDetailActivity)
                    adapter = object : RecyclerView.Adapter<AnswerHolder>() {
                        override fun onBindViewHolder(holder: AnswerHolder, position: Int) {
                            holder.render(comments[position])
                        }

                        override fun getItemCount(): Int {
                            return comments.size
                        }

                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerHolder {
                            val inflater = LayoutInflater.from(parent.context)
                            return AnswerHolder(inflater.inflate(R.layout.item_answer, parent, false))
                        }
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 99 && resultCode == RESULT_OK) {
            ivImage.setImageURI(Uri.parse(mCurrentPhotoPath))
        }
    }

    fun answer(v: View) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_answer)
        dialog.show()

        val etPrice = dialog.findViewById(R.id.price) as EditText
        val etDetail = dialog.findViewById(R.id.detail) as EditText
        ivImage = dialog.findViewById(R.id.image) as ImageView

        ivImage.setOnClickListener {

            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(this,
                    "com.babedev.cheapmunk.fileprovider",
                    photoFile)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 99)
            }
        }

        dialog.findViewById(R.id.ok).setOnClickListener {
            if (!mCurrentPhotoPath.isEmpty()) {
                val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://cheapmunk-99614.appspot.com/")
                val answerStorageRef = storageRef.child(post.id).child(Date().time.toString())

                answerStorageRef.putFile(Uri.parse(mCurrentPhotoPath)).addOnSuccessListener { snapshot ->
                    val answer = Answer(etPrice.text.toString().toDouble(),
                            etDetail.text.toString(),
                            snapshot.downloadUrl.toString(),
                            MyApp.me.firebaseId)

                    answerRef.push().setValue(answer)
                    priceRef.push().setValue(answer.price)
                    barcodePriceRef.push().setValue(answer.price)
                }
            } else {
                val answer = Answer(etPrice.text.toString().toDouble(),
                        etDetail.text.toString(),
                        "",
                        MyApp.me.firebaseId)

                answerRef.push().setValue(answer)
                priceRef.push().setValue(answer.price)
                barcodePriceRef.push().setValue(answer.price)
            }

            dialog.dismiss()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                "temp",
                ".jpg",
                storageDir
        )

        mCurrentPhotoPath = "file:" + image.absolutePath
        return image
    }

    class AnswerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvPrice: TextView
        var tvComment: TextView

        init {
            tvPrice = itemView.findViewById(R.id.price) as TextView
            tvComment = itemView.findViewById(R.id.comment) as TextView
        }

        fun render(answer: Answer) {
            tvPrice.text = answer.price.toString()
            tvComment.text = answer.detail
        }
    }
}
