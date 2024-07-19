package com.laurens.latihan1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class DetailActivity : AppCompatActivity() {
    private lateinit var detailDesc: TextView
    private lateinit var detailTitle: TextView
    private lateinit var detailLang: TextView
    private lateinit var detailImage: ImageView
    private lateinit var deleteButton: FloatingActionButton
    private lateinit var editButton: FloatingActionButton
    private var key: String = ""
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        detailDesc = findViewById(R.id.detailDesc)
        detailImage = findViewById(R.id.detailImage)
        detailTitle = findViewById(R.id.detailTitle)
        deleteButton = findViewById(R.id.deleteButton)
        editButton = findViewById(R.id.editButton)
        detailLang = findViewById(R.id.detailLang)

        val bundle = intent.extras
        if (bundle != null) {
            detailDesc.text = bundle.getString("Description")
            detailTitle.text = bundle.getString("Title")
            detailLang.text = bundle.getString("Language")
            key = bundle.getString("Key").orEmpty()
            imageUrl = bundle.getString("Image").orEmpty()
            Glide.with(this).load(imageUrl).into(detailImage)
        }


        deleteButton.setOnClickListener {
            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials")
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val storageReference: StorageReference = storage.getReferenceFromUrl(imageUrl)
            storageReference.delete().addOnSuccessListener {
                reference.child(key).removeValue()
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }

        editButton.setOnClickListener {
            val intent = Intent(this@DetailActivity, UpdateActivity::class.java).apply {
                putExtra("Title", detailTitle.text.toString())
                putExtra("Description", detailDesc.text.toString())
                putExtra("Language", detailLang.text.toString())
                putExtra("Image", imageUrl)
                putExtra("Key", key)
            }
            startActivity(intent)
        }
    }
}