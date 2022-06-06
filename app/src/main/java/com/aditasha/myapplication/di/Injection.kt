package com.aditasha.myapplication.di

import android.content.Context
import com.aditasha.myapplication.api.ApiConfig
import com.aditasha.myapplication.data.StoryRepository
import com.aditasha.myapplication.database.StoryDatabase

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(database, apiService)
    }
}