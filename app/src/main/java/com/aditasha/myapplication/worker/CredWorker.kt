package com.aditasha.myapplication.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.aditasha.myapplication.preferences.UserPreference

class CredWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val c = context

    override fun doWork(): Result {
        val pref = UserPreference(c)
        pref.wipeCred()
        return Result.success()
    }
}