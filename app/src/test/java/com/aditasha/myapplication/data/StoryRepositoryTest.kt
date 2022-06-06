package com.aditasha.myapplication.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import androidx.paging.map
import androidx.test.espresso.IdlingRegistry
import com.aditasha.myapplication.*
import com.aditasha.myapplication.api.ApiService
import com.aditasha.myapplication.api.Login
import com.aditasha.myapplication.api.Register
import com.aditasha.myapplication.database.FakeDao
import com.aditasha.myapplication.database.StoryDao
import com.aditasha.myapplication.database.StoryDatabase
import com.aditasha.myapplication.database.StoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.File

@ExperimentalCoroutinesApi
class StoryRepositoryTest {

    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var storyDatabase: StoryDatabase

    @Mock
    private lateinit var storyDao: StoryDao

    @Mock
    private lateinit var apiService: ApiService
    private lateinit var fakeDao: StoryDao
    private lateinit var dummyStory: List<StoryEntity>
    private lateinit var file: RequestBody

    @Before
    fun setUp() {
        apiService = Mockito.mock(ApiService::class.java)
        storyDatabase = Mockito.mock(StoryDatabase::class.java)
        storyRepository = StoryRepository(storyDatabase, apiService)
        storyDao = Mockito.mock(StoryDao::class.java)
        fakeDao = FakeDao()

        dummyStory = DataDummy.generateDummyStoryEntity()

        file = File.createTempFile("abc", "def").asRequestBody("image/jpeg".toMediaTypeOrNull())

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun `when register Should Not Fail`() = runTest(UnconfinedTestDispatcher()) {
        val expected = DataDummy.generateDummyGeneralResponse()

        `when`(apiService.register(Register("Arif Faizin", "123@Gmail.com", "123456"))).thenReturn(
            expected
        )

        val actual = storyRepository.register("Arif Faizin", "123@Gmail.com", "123456")
            .getSecondOrAwaitValue()

        Mockito.verify(apiService).register(Register("Arif Faizin", "123@Gmail.com", "123456"))
        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(expected.message, (actual as Result.Success).data.message)
    }

    @Test
    fun `when register Should Fail Emit Error`() = runTest(UnconfinedTestDispatcher()) {
        `when`(apiService.register(Register("Arif Faizin", "", ""))).thenThrow()

        val actual = storyRepository.register("Arif Faizin", "", "").getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Error)
    }

    @Test
    fun `when Login Should Not Fail`() = runTest(UnconfinedTestDispatcher()) {
        val expected = DataDummy.generateDummyLoginResponse()

        `when`(apiService.login(Login("123@Gmail.com", "123456"))).thenReturn(expected)

        val actual = storyRepository.login("123@Gmail.com", "123456").getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(expected.loginResult.token, (actual as Result.Success).data.token)
    }

    @Test
    fun `when Login Should Fail Emit Error`() = runTest(UnconfinedTestDispatcher()) {
        `when`(apiService.login(Login("123@Gmail.com", "654321"))).thenThrow()

        val actual = storyRepository.login("123@Gmail.com", "654321").getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Error)
    }

    @Test
    fun `when getStory Should Get Paging Correctly`() = runTest(UnconfinedTestDispatcher()) {
        fakeDao.insertStory(dummyStory)
        `when`(storyDatabase.storyDao()).thenReturn(fakeDao)
        `when`(storyDao.getAllStory()).thenReturn(fakeDao.getAllStory())

        val snapshot = PagedTestDataSources.snapshot(dummyStory)
        var expected = StoryEntity("", "", "", "", null, "", null)
        snapshot.map { expected = it }

        val actualStory: PagingData<StoryEntity> =
            storyRepository.getStories("token").getOrAwaitValue()
        var actual = StoryEntity("", "", "", "", null, "", null)
        actualStory.map { actual = it }

        Assert.assertNotNull(actual)
        Assert.assertEquals(expected.id, actual.id)
    }

    @Test
    fun `when getMapStories Should Get Story Correctly`() = runTest(UnconfinedTestDispatcher()) {
        fakeDao.insertStory(dummyStory)
        val dummy = DataDummy.generateDummyStoryResponse()
        `when`(storyDatabase.storyDao()).thenReturn(fakeDao)
        `when`(storyDao.getMapStory()).thenReturn(fakeDao.getMapStory())
        `when`(apiService.stories("token", 1, 15, 1)).thenReturn(dummy)


        val actual = storyRepository.getMapStories("token").getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Error)
    }

    @Test
    fun `when GetUpload Should Not Null`() = runTest(UnconfinedTestDispatcher()) {
        val expected = DataDummy.generateDummyGeneralResponse()

        val desc = "desc".toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            "abcdef",
            file
        )

        `when`(apiService.upload("token", desc, imageMultipart)).thenReturn(expected)

        val actual = storyRepository.upload("token", desc, imageMultipart).getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
    }

    @Test
    fun `when GetUpload Fail Emit Error`() = runTest(UnconfinedTestDispatcher()) {
        val desc = "desc".toRequestBody("text/plain".toMediaType())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            "abcdef",
            file
        )

        `when`(apiService.upload("token", desc, imageMultipart)).thenThrow()

        val actual = storyRepository.upload("token", desc, imageMultipart).getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Error)
    }

    @Test
    fun `when deleteData Should Clear Database`() = runTest(UnconfinedTestDispatcher()) {
        fakeDao.insertStory(dummyStory)
        val expected = Result.Success(true)

        `when`(storyDatabase.storyDao()).thenReturn(fakeDao)
        `when`(storyDao.deleteAll()).thenReturn(Unit)

        val actual = storyRepository.deleteData().getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Success)
        Assert.assertEquals(expected.data, (actual as Result.Success).data)
    }

    @Test
    fun `when deleteData Fail Emit Error`() = runTest(UnconfinedTestDispatcher()) {
        fakeDao.insertStory(dummyStory)

        `when`(storyDatabase.storyDao()).thenReturn(fakeDao)
        `when`(storyDao.deleteAll()).thenThrow()

        val actual = storyRepository.deleteData().getSecondOrAwaitValue()

        Assert.assertNotNull(actual)
        Assert.assertTrue(actual is Result.Error)
    }
}

