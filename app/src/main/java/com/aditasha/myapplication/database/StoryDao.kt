package com.aditasha.myapplication.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryEntity>)

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getAllStory(): PagingSource<Int, StoryEntity>

    @Query("SELECT * FROM story WHERE lat IS NOT NULL AND lon IS NOT NULL")
    suspend fun getMapStory(): List<StoryEntity>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}