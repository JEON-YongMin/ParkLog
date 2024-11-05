package com.example.parklog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _photoUri = MutableLiveData<Uri?>()
    val photoUri: LiveData<Uri?> = _photoUri

    // 카메라로 촬영한 사진 URI를 설정하는 함수
    fun setPhotoUri(uri: Uri) {
        _photoUri.value = uri
    }
}
