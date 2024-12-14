package com.example.parklog

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.parklog.databinding.ActivityParkingLocationBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ParkingLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationBinding
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParkingLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoUri?.let {
                        binding.savePhotoText.setImageURI(it) // 이미지 뷰에 사진 설정
                    }
                }
            }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnCamera.setOnClickListener {
            photoUri = createImageFileUri()
            photoUri?.let { takePictureLauncher.launch(it) }
        }

        binding.btnSave.setOnClickListener {
            val location = binding.etLocationDescription.text.toString()
            val fee = binding.etFee.text.toString()

            if (photoUri == null) {
                Toast.makeText(this, "사진을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

            photoUri?.let { uri ->
                storageRef.putFile(uri)
                    .addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val database = FirebaseDatabase.getInstance()
                            val dbRef = database.reference.child("parking_locations").push()

                            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            val parkingData = ParkingLocationData(
                                photoUri = downloadUri.toString(),
                                location = if (location.isEmpty()) "" else location,
                                fee = if (fee.isEmpty()) "" else fee,
                                timestamp = timestamp // 저장시간 추가
                            )

                            dbRef.setValue(parkingData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "사진 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun createImageFileUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
}
