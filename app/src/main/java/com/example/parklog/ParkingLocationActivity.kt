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
import java.util.*

class ParkingLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationBinding
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParkingLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TakePicture 런처 설정
        val takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    photoUri?.let {
                        binding.savePhotoText.setImageURI(it) // 이미지 뷰에 사진 설정
                    }
                }
            }

        // 홈 버튼 클릭 이벤트 추가
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // MainActivity로 이동
            startActivity(intent) // 새 액티비티 시작
        }

        // 카메라 버튼 클릭 이벤트
        binding.btnCamera.setOnClickListener {
            photoUri = createImageFileUri()
            photoUri?.let { takePictureLauncher.launch(it) }
        }

        // Save 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            val location = binding.etLocationDescription.text.toString()
            val fee = binding.etFee.text.toString()

            if (photoUri == null || location.isEmpty() || fee.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }  // 모두 저장하지 않아도 save할 수 있도록 코드 수정!

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
            // images 폴더 아래에 고유이름으로 jpg 저장 (파일이름 중복방지)

            // 사진 Firebase Storage에 업로드
            photoUri?.let { uri ->
                storageRef.putFile(uri)
                    .addOnSuccessListener { // 파일 업로드가 성공했을 때 아래 작업 실행
                        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            val database = FirebaseDatabase.getInstance()
                            val dbRef = database.reference.child("parking_locations").push()

                            // ParkingLocationData 객체 생성
                            val parkingData = ParkingLocationData(
                                photoUri = downloadUri.toString(),
                                location = location,
                                fee = fee
                            )

                            // Firebase Database에 데이터 저장
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
            } ?: run {
                // photoUri가 null인 경우 처리
                Toast.makeText(this, "사진이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
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
