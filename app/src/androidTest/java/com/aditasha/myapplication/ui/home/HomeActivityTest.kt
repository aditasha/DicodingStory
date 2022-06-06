package com.aditasha.myapplication.ui.home

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.aditasha.myapplication.*
import com.aditasha.myapplication.api.ApiConfig
import com.aditasha.myapplication.api.LoginResponse
import com.aditasha.myapplication.api.LoginResult
import com.aditasha.myapplication.preferences.UserPreference
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class HomeActivityTest {
    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)
        ApiConfig.base_url = BuildConfig.MOCK_URL
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    // English language only
    @Test
    fun getStory_Success() {
        val userPref = UserPreference(ApplicationProvider.getApplicationContext())
        userPref.wipeCred()
        val credential = LoginResult(
            "Arif Faizin",
            "user-yj5pc_LARC_AgK61",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXlqNXBjX0xBUkNfQWdLNjEiLCJpYXQiOjE2NDE3OTk5NDl9.flEMaQ7zsdYkxuyGbiXjEDXO8kuDTcI__3UjCwt6R_I"
        )
        userPref.setCred(credential)
        ActivityScenario.launch(HomeActivity::class.java)

        val mockStory = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("success_response.json"))
        mockWebServer.enqueue(mockStory)
        mockWebServer.enqueue(mockStory)
        mockWebServer.enqueue(mockStory)

        onView(withId(R.id.storyRecycler))
            .check(matches(isDisplayed()))

        onView(withText("Welcome, Arif Faizin"))
            .check(matches(isDisplayed()))

        onView(withText("By Reisalin"))
            .check(matches(isDisplayed()))

        onView(withText("By Aufa"))
            .check(matches(isDisplayed()))

        onView(withId(R.id.storyRecycler))
            .perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(
                    3
                )
            )

        onView(withText("By Dimas"))
            .check(matches(isDisplayed()))

        userPref.wipeCred()
    }
}