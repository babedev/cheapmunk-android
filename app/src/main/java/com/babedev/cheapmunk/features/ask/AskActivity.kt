package com.babedev.cheapmunk.features.ask

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.babedev.cheapmunk.R
import com.babedev.cheapmunk.app.MyApp
import com.babedev.cheapmunk.domain.model.Post
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_ask.*
import java.io.File
import java.io.IOException
import java.util.*


class AskActivity : AppCompatActivity() {

    var mCurrentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 99 && resultCode == RESULT_OK) {
            image.setImageURI(Uri.parse(mCurrentPhotoPath))
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

    fun takePicture(v: View) {
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

    fun proceed(v: View) {
        val barcode = intent.getStringExtra("barcode")
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://cheapmunk-99614.appspot.com/")
        val postStorageRef = storageRef.child("$barcode${Date().time}")

        postStorageRef.putFile(Uri.parse(mCurrentPhotoPath))
                .addOnSuccessListener { snapshot ->
                    val downloadUrl = snapshot.downloadUrl
                    val postRef = FirebaseDatabase.getInstance().getReference("post")!!
                    val barcodeRef = FirebaseDatabase.getInstance().getReference("barcode/$barcode/posts")!!

                    val key = postRef.push().key
                    val post = Post(key,
                            downloadUrl.toString(),
                            barcode.toString(),
                            product.text.toString(),
                            detail.text.toString(),
                            MyApp.me.firebaseId)

                    postRef.child(key).setValue(post)
                    barcodeRef.push().setValue(key)

                    finish()
                }
    }
}
