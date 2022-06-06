package com.aditasha.myapplication

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aditasha.myapplication.api.*
import com.aditasha.myapplication.database.StoryEntity
import com.bumptech.glide.load.HttpException
import java.io.IOException

object DataDummy {

    fun generateDummyStoryEntity(): List<StoryEntity> {
        val storyList = ArrayList<StoryEntity>()
        for (i in 0..10) {
            val story = StoryEntity(
                "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                "2022-01-08T06:34:18.598Z",
                "Dimas",
                "Lorem Ipsum",
                -16.002,
                "story-FvU4u0Vp2S3PMsFg",
                -10.212
            )
            storyList.add(story)
        }
        return storyList
    }

    fun generateDummyStoryResponse(): StoryResponse {
        val storyList = ArrayList<StoryItem>()
        for (i in 0..10) {
            val story = StoryItem(
                "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                "2022-01-08T06:34:18.598Z",
                "Dimas",
                "Lorem Ipsum",
                -16.002,
                "story-FvU4u0Vp2S3PMsFg",
                -10.212
            )
            storyList.add(story)
        }
        return StoryResponse(false, "Stories fetched successfully", storyList)
    }

    fun generateDummyGeneralResponse(): GeneralResponse {
        return GeneralResponse(false, "success")
    }

    fun generateDummyLoginResponse(): LoginResponse {
        val credential = LoginResult(
            "Arif Faizin",
            "user-yj5pc_LARC_AgK61",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXlqNXBjX0xBUkNfQWdLNjEiLCJpYXQiOjE2NDE3OTk5NDl9.flEMaQ7zsdYkxuyGbiXjEDXO8kuDTcI__3UjCwt6R_I"
        )
        return LoginResponse(credential, false, "success")
    }

    // placeholder for dao return
    class MyPagingSource(
        val storyEntity: StoryEntity
    ) : PagingSource<Int, StoryEntity>() {
        var story: List<StoryEntity> = emptyList()
            set(value) {
                println("set")
                field = value
                invalidate()
            }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
            return try {
                val pageNumber = params.key ?: 0
                val response = generateDummyStoryEntity()
                val prevKey = if (pageNumber > 0) pageNumber - 1 else null
                val nextKey = if (response.isNotEmpty()) pageNumber + 1 else null
                LoadResult.Page(
                    data = response,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
            return state.anchorPosition?.let {
                state.closestPageToPosition(it)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
            }
        }
    }

}
