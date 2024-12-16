package com.example.parklog

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.parklog.databinding.FragmentParkingLocationBinding
import com.example.parklog.viewmodel.ParkingLocationViewModel

class ParkingLocationFragment : Fragment() {

    private var _binding: FragmentParkingLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParkingLocationViewModel by viewModels()
    private var photoUri: Uri? = null

    // 카메라 권한 요청 런처
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                launchCamera()
            } else {
                Toast.makeText(requireContext(), "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    // 사진 촬영 런처
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let {
                    binding.savePhotoText.setImageURI(it) // 이미지 설정
                }
            } else {
                Toast.makeText(requireContext(), "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show()
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

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // 업로드 상태 관찰
        viewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            if (status == "저장 성공!") requireActivity().onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // 홈 버튼 클릭 이벤트
        binding.homeButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        // 카메라 버튼 클릭 이벤트
        binding.btnCamera.setOnClickListener {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        // 저장 버튼 클릭 이벤트
        binding.btnSave.setOnClickListener {
            val location = binding.etLocationDescription.text.toString()
            val fee = binding.etFee.text.toString()

            if (photoUri == null) {
                Toast.makeText(requireContext(), "사진을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveParkingLocation(photoUri!!, location, fee)
        }
    }

    private fun launchCamera() {
        photoUri = createImageFileUri()
        if (photoUri != null) {
            takePictureLauncher.launch(photoUri)
        } else {
            Toast.makeText(requireContext(), "사진 저장 공간을 생성하지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createImageFileUri(): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } catch (e: Exception) {
            Log.e("CameraError", "이미지 URI 생성 실패: ${e.message}")
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
