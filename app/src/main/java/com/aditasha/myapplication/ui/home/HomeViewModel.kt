package com.aditasha.myapplication.ui.home

import androidx.lifecycle.ViewModel
import com.aditasha.myapplication.data.StoryRepository

class HomeViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStory(token: String) = storyRepository.getStories(token)
}