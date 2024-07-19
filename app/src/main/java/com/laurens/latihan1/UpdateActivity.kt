package com.laurens.latihan1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.laurens.latihan1.data.DataClass

class UpdateActivity : AppCompatActivity() {

    private lateinit var updateImage: ImageView
    private lateinit var updateButton: Button
    private lateinit var updateDesc: EditText
    private lateinit var updateTitle: EditText
    private lateinit var updateLang: EditText
    private lateinit var title: String
    private lateinit var desc: String
    private lateinit var lang: String
    private var imageUrl: String? = null
    private var key: String? = null
    private var oldImageURL: String? = null
    private var uri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private val activityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    uri = data.data
                    updateImage.setImageURI(uri)
                }
            } else {
                Toast.makeText(this@UpdateActivity, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        updateButton = findViewById(R.id.updateButton)
        updateDesc = findViewById(R.id.updateDesc)
        updateImage = findViewById(R.id.updateImage)
        updateLang = findViewById(R.id.updateLang)
        updateTitle = findViewById(R.id.updateTitle)

        val bundle = intent.extras
        if (bundle != null) {
            Glide.with(this).load(bundle.getString("Image")).into(updateImage)
            updateTitle.setText(bundle.getString("Title"))
            updateDesc.setText(bundle.getString("Description"))
            updateLang.setText(bundle.getString("Language"))
            key = bundle.getString("Key")
            oldImageURL = bundle.getString("Image")
        }

        if (key != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Android Tutorials").child(key!!)
        } else {
            Toast.makeText(this, "Invalid key", Toast.LENGTH_SHORT).show()
            finish()
        }

        updateImage.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        updateButton.setOnClickListener {
            if (uri != null) {
                saveData()
            } else {
                updateData()
            }
            val intent = Intent(this@UpdateActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveData() {
        uri?.let {
            storageReference = FirebaseStorage.getInstance().getReference("Android Images").child(it.lastPathSegment!!)
            val builder = AlertDialog.Builder(this@UpdateActivity)
            builder.setCancelable(false)
            builder.setView(R.layout.progress_layout)
            val dialog = builder.create()
            dialog.show()

            storageReference.putFile(it).addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isComplete);
                val urlImage = uriTask.result
                imageUrl = urlImage.toString()
                updateData()
                dialog.dismiss()
            }.addOnFailureListener { e ->
                dialog.dismiss()
            }
        } ?: run {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateData() {
        title = updateTitle.text.toString().trim()
        desc = updateDesc.text.toString().trim()
        lang = updateLang.text.toString()

        if (title.isNotEmpty() && desc.isNotEmpty() && lang.isNotEmpty() && imageUrl != null) {
            val dataClass = DataClass(title, desc, lang, imageUrl!!)
            databaseReference.setValue(dataClass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    oldImageURL?.let { url ->
                        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                        reference.delete()
                    }
                    Toast.makeText(this@UpdateActivity, "Updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@UpdateActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }
}