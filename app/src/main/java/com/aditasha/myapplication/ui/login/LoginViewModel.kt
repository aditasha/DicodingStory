package com.aditasha.myapplication.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aditasha.myapplication.data.StoryRepository

class LoginViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFailed = MutableLiveData<Boolean>()
    val isFailed: LiveData<Boolean> = _isFailed

    private val _errorText = MutableLiveData<String>()
    val errorText: LiveData<String> = _errorText

    fun login(email: String, pass: String) = storyRepository.login(email, pass)
}