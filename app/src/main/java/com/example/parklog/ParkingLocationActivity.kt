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
import android.content.pm.PackageManager

class ParkingLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationBinding // ViewBinding 객체 선언
    private var photoUri: Uri? = null // Uri를 nullable로 선언하여 null 체크 강화

    // 권한 요청을 처리하는 런처 초기화
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityParkingLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 카메라 권한 요청
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        // TakePicture 런처 설정
        val takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoUri?.let {
                        binding.savePhotoText.setImageURI(it) // 이미지 뷰에 사진 설정
                    } ?: run {
                        Toast.makeText(this, "Failed to capture photo.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        // 카메라 버튼 클릭 이벤트 설정
        binding.btnCamera.setOnClickListener {
            // 권한 확인 후 카메라 실행
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                photoUri = createImageFileUri()
                photoUri?.let { uri -> // null 체크와 함께 Smart cast를 수행
                    takePictureLauncher.launch(uri) // 사진 촬영 시작
                } ?: run {
                    Toast.makeText(this, "Failed to create file for photo.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // 권한이 없으면 요청
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }


        // Save 버튼 클릭 이벤트 설정
        binding.btnSave.setOnClickListener {
            val currentPhotoUri = photoUri
            if (currentPhotoUri == null) {
                Toast.makeText(this, "No photo to save.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileName = "images/${System.currentTimeMillis()}.jpg"
            val imageRef = storageRef.child(fileName)

            imageRef.putFile(currentPhotoUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val database = FirebaseDatabase.getInstance()
                        val dbRef = database.reference.child("parking_images").push()

                        dbRef.setValue(downloadUri.toString())
                            .addOnSuccessListener {
                                Toast.makeText(this, "Successfully saved!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // 홈 버튼 클릭 이벤트 설정
        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    // 이미지 파일 URI 생성 함수
    private fun createImageFileUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_photo_${System.currentTimeMillis()}.jpg") // 고유 파일 이름
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
}
