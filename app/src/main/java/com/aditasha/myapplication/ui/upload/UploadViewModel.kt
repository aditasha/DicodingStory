package com.aditasha.myapplication.ui.upload

import androidx.lifecycle.ViewModel
import com.aditasha.myapplication.data.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadViewModel(private val storyRepository: StoryRepository, private val token: String) :
    ViewModel() {

    fun upload(
        desc: RequestBody,
        image: MultipartBody.Part,
        lat: Float? = null,
        lon: Float? = null
    ) = storyRepository.upload(token, desc, image, lat, lon)
}