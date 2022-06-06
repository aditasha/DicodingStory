package com.aditasha.myapplication.ui.upload

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aditasha.myapplication.DataDummy
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.api.GeneralResponse
import com.aditasha.myapplication.data.StoryRepository
import com.aditasha.myapplication.getOrAwaitValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class UploadViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var uploadViewModel: UploadViewModel

    @Before
    fun setUp() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        uploadViewModel = UploadViewModel(storyRepository, "token")
    }

    @Test
    fun `when upload should not fail`() {
        val dummy = DataDummy.generateDummyGeneralResponse()
        val expected = MutableLiveData<Result<GeneralResponse>>()
        expected.value = Result.Success(dummy)

        val file = File.createTempFile("abc", "def").asRequestBody("image/jpeg".toMediaTypeOrNull())
        val desc = "desc".toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            "abcdef",
            file
        )

        Mockito.`when`(uploadViewModel.upload(desc, imageMultipart)).thenReturn(expected)
        val actual = uploadViewModel.upload(desc, imageMultipart).getOrAwaitValue()

        Mockito.verify(storyRepository).upload("token", desc, imageMultipart)
        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(dummy.message, (actual as Result.Success).data.message)
    }
}