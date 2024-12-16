package com.example.parklog.repository

import android.net.Uri
import com.example.parklog.model.ParkingLocationData
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ParkingLocationRepository {

    fun saveParkingLocation(
        photoUri: Uri,
        location: String,
        fee: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
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
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure("저장 실패: ${e.message}") }
                }
            }
            .addOnFailureListener { e -> onFailure("사진 업로드 실패: ${e.message}") }
    }
}
