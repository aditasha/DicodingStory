package com.aditasha.myapplication.ui.register

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aditasha.myapplication.DataDummy
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.api.GeneralResponse
import com.aditasha.myapplication.data.StoryRepository
import com.aditasha.myapplication.getOrAwaitValue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        registerViewModel = RegisterViewModel(storyRepository)
    }

    @Test
    fun `when register should not return null and failed`() {
        val dummy = DataDummy.generateDummyGeneralResponse()
        val expected = MutableLiveData<Result<GeneralResponse>>()
        expected.value = Result.Success(dummy)

        Mockito.`when`(registerViewModel.register("Arif Faizin", "123@Gmail.com", "123456"))
            .thenReturn(expected)
        val actual =
            registerViewModel.register("Arif Faizin", "123@Gmail.com", "123456").getOrAwaitValue()

        Mockito.verify(storyRepository).register("Arif Faizin", "123@Gmail.com", "123456")
        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(dummy.message, (actual as Result.Success).data.message)
    }
}