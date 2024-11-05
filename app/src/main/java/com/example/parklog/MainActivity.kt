package com.example.parklog

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parking_location_input)

        // 카메라 촬영 결과를 받을 콜백 설정
        val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.setPhotoUri(photoUri) // 촬영된 사진 URI 저장
            }
        }

        // 카메라 버튼 클릭 시 사진 촬영 시작
        findViewById<ImageButton>(R.id.btnCamera).setOnClickListener {
            photoUri = createImageFileUri()
            takePictureLauncher.launch(photoUri)
        }

        // ViewModel의 photoUri를 관찰하여 이미지 뷰 업데이트
        viewModel.photoUri.observe(this, Observer { uri ->
            if (uri != null) {
                findViewById<ImageView>(R.id.savePhotoText).setImageURI(uri)
            }
        })
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
