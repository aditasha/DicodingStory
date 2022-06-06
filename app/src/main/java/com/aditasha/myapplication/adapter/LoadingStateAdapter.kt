package com.aditasha.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aditasha.myapplication.R
import com.aditasha.myapplication.databinding.LoadingLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding =
            LoadingLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: LoadingStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class LoadingStateViewHolder(private val binding: LoadingLayoutBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener { retry.invoke() }
        }

        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                MaterialAlertDialogBuilder(itemView.context)
                    .setMessage(loadState.error.localizedMessage)
                    .setPositiveButton(itemView.resources.getString(R.string.close_dialog)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            binding.loading.isVisible = loadState is LoadState.Loading
            binding.retryButton.isVisible = loadState is LoadState.Error
        }
    }
}

