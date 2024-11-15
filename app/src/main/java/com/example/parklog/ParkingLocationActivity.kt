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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

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
            // Firebase Storage 인스턴스 생성
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            // 파일 이름을 고유하게 설정 (현재 시간을 사용)
            val fileName = "images/${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child(fileName)

            // Firebase Storage에 파일 업로드
            val uploadTask = imageRef.putFile(photoUri)
            uploadTask.addOnSuccessListener {
                // 업로드 성공 시 다운로드 URL을 가져와 Realtime Database에 저장
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()

                    // Firebase Realtime Database 인스턴스
                    val database = FirebaseDatabase.getInstance()
                    val dbRef = database.reference.child("parking_images").push()

                    // URL을 Firebase Database에 저장
                    dbRef.setValue(imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Successfully Saved!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to save URL: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
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
