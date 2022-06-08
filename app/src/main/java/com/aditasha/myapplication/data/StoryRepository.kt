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
import org.json.JSONObject

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) {

    fun register(name: String, email: String, pass: String): LiveData<Result<GeneralResponse>> =
        liveData {
            emit(Result.Loading)
            wrapEspressoIdlingResource {
                try {
                    val credential = Register(name, email, pass)
                    val client = apiService.register(credential)
                    if (client.isSuccessful) {
                        emit(Result.Success(client.body()))
                    } else {
                        val jsonObj = JSONObject(client.errorBody()!!.charStream().readText())
                        val message = jsonObj.getString("message")
                        emit(Result.Error(message))
                    }
                } catch (e: Exception) {
                    emit(Result.Error("Server timeout!"))
                }
            }
        }

    fun login(email: String, pass: String): LiveData<Result<LoginResult>> = liveData {
        emit(Result.Loading)
        wrapEspressoIdlingResource {
            try {
                val credential = Login(email, pass)
                val client = apiService.login(credential)
                if (client.isSuccessful) {
                    emit(Result.Success(client.body()?.loginResult))
                } else {
                    val jsonObj = JSONObject(client.errorBody()!!.charStream().readText())
                    val message = jsonObj.getString("message")
                    emit(Result.Error(message))
                }

            } catch (e: Exception) {
                emit(Result.Error("Server timeout!"))
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
                val storyData = apiService.stories("Bearer $token", 1, 15, 1)

                if (storyData.isSuccessful) {
                    val list = storyData.body()?.listStory
                    val arrayList: ArrayList<StoryEntity> = ArrayList()

                    if (list != null) {
                        for (story in list) {
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
                    }

                    val entity: List<StoryEntity> = arrayList
                    storyDatabase.storyDao().deleteAll()
                    storyDatabase.storyDao().insertStory(entity)
                    val result = storyDatabase.storyDao().getMapStory()
                    emit(Result.Success(result))
                } else {
                    val jsonObj = JSONObject(storyData.errorBody()!!.charStream().readText())
                    val message = jsonObj.getString("message")
                    emit(Result.Error(message))
                }
            } catch (e: Exception) {
                emit(Result.Error("Server timeout!"))
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
                if (client.isSuccessful) {
                    emit(Result.Success(client.body()))
                } else {
                    val jsonObj = JSONObject(client.errorBody()!!.charStream().readText())
                    val message = jsonObj.getString("message")
                    emit(Result.Error(message))
                }
            } catch (e: Exception) {
                emit(Result.Error("Server timeout!"))
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