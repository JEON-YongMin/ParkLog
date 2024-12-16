package com.example.parklog

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.parklog.databinding.FragmentParkingLocationBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ParkingLocationFragment : Fragment() {

    private var _binding: FragmentParkingLocationBinding? = null
    private val binding get() = _binding!!

    private var photoUri: Uri? = null

    // 사진 촬영 Activity Result Launcher
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let {
                    binding.savePhotoText.setImageURI(it) // 이미지 뷰에 사진 설정
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 홈 버튼 클릭 이벤트
        binding.homeButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        // 카메라 버튼 클릭 이벤트
        binding.btnCamera.setOnClickListener {
            photoUri = createImageFileUri()
            photoUri?.let { takePictureLauncher.launch(it) }
        }

        // 저장 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            val location = binding.etLocationDescription.text.toString()
            val fee = binding.etFee.text.toString()

            if (photoUri == null) {
                Toast.makeText(requireContext(), "사진을 입력해주세요.", Toast.LENGTH_SHORT).show()
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

                            val timestamp =
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                            val parkingData = ParkingLocationData(
                                photoUri = downloadUri.toString(),
                                location = if (location.isEmpty()) "" else location,
                                fee = if (fee.isEmpty()) "" else fee,
                                timestamp = timestamp // 저장시간 추가
                            )

                            dbRef.setValue(parkingData)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "저장 성공!", Toast.LENGTH_SHORT).show()
                                    requireActivity().onBackPressed()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "저장 실패: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "사진 업로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // 이미지 파일 URI 생성
    private fun createImageFileUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
