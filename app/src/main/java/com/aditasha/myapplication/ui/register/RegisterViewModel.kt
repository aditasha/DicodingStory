package com.aditasha.myapplication.ui.register

import androidx.lifecycle.ViewModel
import com.aditasha.myapplication.data.StoryRepository

class RegisterViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    fun register(name: String, email: String, pass: String) =
        storyRepository.register(name, email, pass)
}