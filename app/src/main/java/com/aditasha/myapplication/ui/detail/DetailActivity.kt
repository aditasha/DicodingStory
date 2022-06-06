package com.aditasha.myapplication.ui.detail

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aditasha.myapplication.GlideApp
import com.aditasha.myapplication.R
import com.aditasha.myapplication.database.StoryEntity
import com.aditasha.myapplication.databinding.ActivityDetailBinding
import com.aditasha.myapplication.helper.dateFormat

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.language_only, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.locale -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData() {
        val intent = intent.getParcelableExtra<StoryEntity>(STORY)
        if (intent != null) {
            binding.nameTextView.text = getString(R.string.author, intent.name)
            binding.timeTextView.text = dateFormat(intent.createdAt)
            val image = intent.photoUrl

            val color = ContextCompat.getColor(this@DetailActivity, R.color.orange_500)

            val circularProgressDrawable = CircularProgressDrawable(this@DetailActivity)
            circularProgressDrawable.setColorSchemeColors(color)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 15f
            circularProgressDrawable.start()

            GlideApp
                .with(this@DetailActivity)
                .load(image)
                .placeholder(circularProgressDrawable)
                .into(binding.profileImageView)

            if (intent.description.isBlank()) {
                binding.captionTextView.text = getString(R.string.empty_caption, intent.name)
            } else binding.captionTextView.text = intent.description
        }
    }

    companion object {
        const val STORY = "story"
    }
}