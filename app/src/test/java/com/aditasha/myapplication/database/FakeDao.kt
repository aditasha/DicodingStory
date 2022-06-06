package com.aditasha.myapplication.database

import androidx.paging.PagingSource
import com.aditasha.myapplication.DataDummy

class FakeDao : StoryDao {
    private var storyData = mutableListOf<StoryEntity>()

    override suspend fun insertStory(story: List<StoryEntity>) {
        storyData.addAll(story)
    }

    override fun getAllStory(): PagingSource<Int, StoryEntity> {
        return DataDummy.MyPagingSource(storyData[0])
    }

    override suspend fun getMapStory(): List<StoryEntity> {
        return storyData
    }

    override suspend fun deleteAll() {
        storyData.clear()
    }
}