package com.example.parklog

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ParkingLocationActivity : AppCompatActivity() {

    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking_location)

        val homeButton: ImageButton = findViewById(R.id.homeButton)
        val saveButton: Button = findViewById(R.id.btnSave)
        val locationEditText: EditText = findViewById(R.id.etLocationDescription)
        val feeEditText: EditText = findViewById(R.id.etFee)
        val savePhotoText: ImageView = findViewById(R.id.savePhotoText)

        val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                savePhotoText.setImageURI(photoUri)
            }
        }

        findViewById<ImageButton>(R.id.btnCamera).setOnClickListener {
            photoUri = createImageFileUri()
            takePictureLauncher.launch(photoUri)
        }

        saveButton.setOnClickListener {

            Toast.makeText(this, "Successfully saved!", Toast.LENGTH_SHORT).show()
            startActivity(intent) // List 페이지로 이동
        }

        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun createImageFileUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_photo.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }
}
