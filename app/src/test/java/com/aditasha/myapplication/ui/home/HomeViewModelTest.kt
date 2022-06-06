package com.aditasha.myapplication.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.map
import com.aditasha.myapplication.DataDummy
import com.aditasha.myapplication.data.PagedTestDataSources
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
class HomeViewModelTest {

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setUp() {
        storyRepository = Mockito.mock(StoryRepository::class.java)
        homeViewModel = HomeViewModel(storyRepository)
    }

    @Test
    fun `when getStory should fetch the correct data`() {
        val dummy = DataDummy.generateDummyStoryEntity()
        val snapshot = PagedTestDataSources.snapshot(dummy)

        var expectedEntity = StoryEntity("", "", "", "", null, "", null)
        snapshot.map { expectedEntity = it }

        val expected = MutableLiveData<PagingData<StoryEntity>>()
        expected.value = snapshot

        Mockito.`when`(storyRepository.getStories("token")).thenReturn(expected)

        val data = homeViewModel.getStory("token").getOrAwaitValue()
        var actualEntity = StoryEntity("", "", "", "", null, "", null)
        data.map { actualEntity = it }

        Mockito.verify(storyRepository).getStories("token")
        Assert.assertNotNull(data)
        Assert.assertEquals(expectedEntity.id, actualEntity.id)
    }
}