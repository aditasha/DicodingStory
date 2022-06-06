package com.aditasha.myapplication.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aditasha.myapplication.DataDummy
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.api.LoginResult
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
class LoginViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var loginViewModel: LoginViewModel

    @Before
    fun setUp() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        loginViewModel = LoginViewModel(storyRepository)
    }

    @Test
    fun `when login should not return null and failed`() {
        val dummy = DataDummy.generateDummyLoginResponse().loginResult
        val expected = MutableLiveData<Result<LoginResult>>()
        expected.value = Result.Success(dummy)

        Mockito.`when`(storyRepository.login("123@Gmail.com", "123456")).thenReturn(expected)
        val actual = loginViewModel.login("123@Gmail.com", "123456").getOrAwaitValue()

        Mockito.verify(storyRepository).login("123@Gmail.com", "123456")
        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(dummy.userId, (actual as Result.Success).data.userId)
    }
}