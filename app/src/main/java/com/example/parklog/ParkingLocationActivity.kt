package com.example.parklog

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ParkingLocationActivity : AppCompatActivity() {

    private lateinit var photoUri: Uri // 사진 URI를 저장하는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking_location)

        val homeButton: ImageButton = findViewById(R.id.homeButton)

        // 카메라 촬영 결과를 받을 콜백 설정
        val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                findViewById<ImageView>(R.id.savePhotoText).setImageURI(photoUri) // 촬영된 사진 URI로 이미지 뷰 업데이트
            }
        }

        // 카메라 버튼 클릭 시 사진 촬영 시작
        findViewById<ImageButton>(R.id.btnCamera).setOnClickListener {
            photoUri = createImageFileUri() // URI 생성
            takePictureLauncher.launch(photoUri) // 사진 촬영 시작
        }

        // 홈 버튼 클릭 시 메인 화면으로 이동
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // 사진을 저장할 파일의 URI 생성 함수
    private fun createImageFileUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "captured_photo.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }
}
