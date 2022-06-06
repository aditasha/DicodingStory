package com.aditasha.myapplication.ui.maps

import androidx.lifecycle.ViewModel
import com.aditasha.myapplication.data.StoryRepository

class MapsViewModel(private val storyRepository: StoryRepository) : ViewModel() {
    fun getStories(token: String) = storyRepository.getMapStories(token)

    fun deleteStories() = storyRepository.deleteData()
}