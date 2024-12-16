package com.example.parklog.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.model.ParkingLocationData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ParkingLocationViewModel : ViewModel() {

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()

    fun saveParkingLocation(photoUri: Uri, location: String, fee: String) {
        _isLoading.value = true

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")

        storageRef.putFile(photoUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val database = FirebaseDatabase.getInstance()
                    val dbRef = database.reference.child("parking_locations").push()

                    val timestamp =
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

                    val parkingData = ParkingLocationData(
                        photoUri = downloadUri.toString(),
                        location = location,
                        fee = fee,
                        timestamp = timestamp
                    )

                    dbRef.setValue(parkingData)
                        .addOnSuccessListener {
                            _uploadStatus.value = "저장 성공!"
                        }
                        .addOnFailureListener { e ->
                            _uploadStatus.value = "저장 실패: ${e.message}"
                        }
                }
            }
            .addOnFailureListener { e ->
                _uploadStatus.value = "사진 업로드 실패: ${e.message}"
            }
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }
}
