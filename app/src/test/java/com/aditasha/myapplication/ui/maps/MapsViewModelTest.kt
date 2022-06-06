package com.aditasha.myapplication.ui.maps

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.aditasha.myapplication.DataDummy
import com.aditasha.myapplication.Result
import com.aditasha.myapplication.data.StoryRepository
import com.aditasha.myapplication.database.StoryEntity
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
class MapsViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mapsViewModel: MapsViewModel

    @Before
    fun setUp() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        mapsViewModel = MapsViewModel(storyRepository)
    }

    @Test
    fun `when getMapStories should not return null and empty location`() {
        val dummy = DataDummy.generateDummyStoryEntity()
        val result = Result.Success(dummy)
        val expected = MutableLiveData<Result<List<StoryEntity>>>()
        expected.value = result

        Mockito.`when`(storyRepository.getMapStories("token")).thenReturn(expected)
        val actual = mapsViewModel.getStories("token").getOrAwaitValue()

        Mockito.verify(storyRepository).getMapStories("token")

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertNotNull((actual as Result.Success).data[0].lat)
        Assert.assertNotNull(actual.data[0].lon)
        Assert.assertEquals(dummy[0].id, actual.data[0].id)
    }
}