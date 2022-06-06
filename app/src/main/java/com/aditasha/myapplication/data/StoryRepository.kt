package com.aditasha.myapplication.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.api.*
import com.aditasha.myapplication.database.StoryDatabase
import com.aditasha.myapplication.database.StoryEntity
import com.aditasha.myapplication.wrapEspressoIdlingResource
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) {

    fun register(name: String, email: String, pass: String): LiveData<Result<GeneralResponse>> =
        liveData {
            emit(Result.Loading)
            wrapEspressoIdlingResource {
//                val credential = Register(name, email, pass)
//                val client = apiService.register(credential)
//                if (client.error) {
//                    emit(Result.Error(client.message))
//                } else {
//                    emit(Result.Success(client))
//                }
                try {
                    val credential = Register(name, email, pass)
                    val client = apiService.register(credential)
                    emit(Result.Success(client))
                } catch (e: Exception) {
                    Log.d("StoryRepository", "register: ${e.message.toString()} ")
                    emit(Result.Error(e.message.toString()))
                }
            }
        }

    fun login(email: String, pass: String): LiveData<Result<LoginResult>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                val credential = Login(email, pass)
                val client = apiService.login(credential)
                emit(Result.Success(client.loginResult))

            } catch (e: Exception) {
                Log.d("StoryRepository", "login: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }

        }
    }

    fun getStories(token: String): LiveData<PagingData<StoryEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getMapStories(token: String): LiveData<Result<List<StoryEntity>>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                val storyData = apiService.stories("Bearer $token", 1, 15, 1).listStory
                val arrayList: ArrayList<StoryEntity> = ArrayList()
                for (story in storyData) {
                    story.apply {
                        arrayList.add(
                            StoryEntity(
                                photoUrl,
                                createdAt,
                                name,
                                description,
                                lon,
                                id,
                                lat
                            )
                        )
                    }
                }
                val entity: List<StoryEntity> = arrayList

                storyDatabase.storyDao().deleteAll()
                storyDatabase.storyDao().insertStory(entity)
                val result = storyDatabase.storyDao().getMapStory()
                emit(Result.Success(result))
            } catch (e: Exception) {
                Log.d("StoryRepository", "getMapStories: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    fun upload(
        token: String,
        desc: RequestBody,
        image: MultipartBody.Part,
        lat: Float? = null,
        lon: Float? = null
    ): LiveData<Result<GeneralResponse>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                val client = apiService.upload("Bearer $token", desc, image, lat, lon)
                emit(Result.Success(client))
            } catch (e: Exception) {
                Log.d("StoryRepository", "upload: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    fun deleteData(): LiveData<Result<Boolean>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                storyDatabase.storyDao().deleteAll()
                emit(Result.Success(true))
            } catch (e: Exception) {
                Log.d("StoryRepository", "deleteData: ${e.message.toString()} ")
                emit(Result.Error(e.message.toString()))
            }
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            storyDatabase: StoryDatabase,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(storyDatabase, apiService)
            }.also { instance = it }
    }
}