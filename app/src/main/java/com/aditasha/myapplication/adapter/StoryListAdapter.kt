package com.aditasha.myapplication.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.aditasha.myapplication.GlideApp
import com.aditasha.myapplication.R
import com.aditasha.myapplication.database.StoryEntity
import com.aditasha.myapplication.databinding.StoryLayoutBinding
import com.aditasha.myapplication.helper.dateFormat
import com.aditasha.myapplication.ui.detail.DetailActivity
import com.bumptech.glide.request.RequestOptions

class StoryListAdapter :
    PagingDataAdapter<StoryEntity, StoryListAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = StoryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: StoryLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: StoryEntity) {
            val image = data.photoUrl
            val text = itemView.context.getString(R.string.author, data.name)
            binding.nameTextView.text = text
            binding.timeTextView.text = dateFormat(data.createdAt)
            val color = ContextCompat.getColor(itemView.context, R.color.orange_500)

            val circularProgressDrawable = CircularProgressDrawable(itemView.context).apply {
                setColorSchemeColors(color)
                strokeWidth = 5f
                centerRadius = 15f
                start()
            }

            GlideApp.with(itemView.context)
                .load(image)
                .apply(
                    RequestOptions().dontTransform()
                )
                .placeholder(circularProgressDrawable)
                .into(binding.profileImageView)

            itemView.setOnClickListener {
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(binding.nameTextView, "name"),
                        Pair(binding.profileImageView, "image"),
                        Pair(binding.timeTextView, "date")
                    )
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra(DetailActivity.STORY, data)
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}