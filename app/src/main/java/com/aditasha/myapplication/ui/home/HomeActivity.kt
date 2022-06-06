package com.aditasha.myapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkManager
import com.aditasha.myapplication.R
import com.aditasha.myapplication.adapter.LoadingStateAdapter
import com.aditasha.myapplication.adapter.StoryListAdapter
import com.aditasha.myapplication.databinding.ActivityHomeBinding
import com.aditasha.myapplication.preferences.UserPreference
import com.aditasha.myapplication.ui.camera.CameraActivity
import com.aditasha.myapplication.ui.login.LoginActivity
import com.aditasha.myapplication.ui.maps.MapsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val storyAdapter by lazy { StoryListAdapter() }
    private lateinit var preference: UserPreference
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workManager = WorkManager.getInstance(this)

        storyAdapter.refresh()

        preference = UserPreference(this@HomeActivity)
        val cred = preference.getCred()

        if (!cred.token.isNullOrEmpty()) {
            val view: HomeViewModel by viewModels { HomeViewModelFactory(this@HomeActivity) }
            homeViewModel = view
            binding.nameTextView.text = getString(R.string.welcome, cred.name)

            homeViewModel.getStory(cred.token)

            observe()
        } else {
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        val layout = LinearLayoutManager(this@HomeActivity)

        binding.storyRecycler.apply {
            adapter = storyAdapter.withLoadStateHeaderAndFooter(
                footer = LoadingStateAdapter {
                    storyAdapter.retry()
                },
                header = LoadingStateAdapter {
                    storyAdapter.retry()
                }
            )
            layoutManager = layout
        }

        binding.fabCam.setOnClickListener {
            intent = Intent(this@HomeActivity, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.locale -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.location -> {
                val intent = Intent(this@HomeActivity, MapsActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> {
                workManager.cancelAllWork()
                preference.wipeCred()
                Toast.makeText(this@HomeActivity, R.string.logout_success, Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observe() {
        homeViewModel.getStory(preference.getCred().token.toString()).observe(this@HomeActivity) { storyList ->
                storyAdapter.submitData(lifecycle, storyList)
            }

        storyAdapter.addLoadStateListener { loadState ->
            when (loadState.mediator?.refresh) {
                is LoadState.Error -> {
                    showFailed(true, getString(R.string.empty_recycler))
                    binding.emptyTextView.visibility = View.INVISIBLE
                }
                is LoadState.Loading -> {
                    showLoading(true)
                    binding.emptyTextView.visibility = View.INVISIBLE
                }
                else -> {
                    if (storyAdapter.itemCount < 1) {
                        binding.emptyTextView.visibility = View.VISIBLE
                    } else {
                        binding.emptyTextView.visibility = View.INVISIBLE
                        showFailed(false, "")
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.visibility = View.VISIBLE
            binding.storyRecycler.visibility = View.INVISIBLE
        } else {
            binding.loading.visibility = View.INVISIBLE
            binding.storyRecycler.visibility = View.VISIBLE
        }
    }

    private fun showFailed(isFailed: Boolean, e: String) {
        if (isFailed) {
            val text = getString(R.string.error, e)
            errorDialog(text).show()
        }
    }

    private fun errorDialog(e: String): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(this@HomeActivity)
            .setMessage(e)
            .setPositiveButton(resources.getString(R.string.close_dialog)) { dialog, _ ->
                dialog.dismiss()
            }
    }
}