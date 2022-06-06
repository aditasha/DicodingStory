package com.aditasha.myapplication.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "story")
data class StoryEntity(
    val photoUrl: String,
    val createdAt: String,
    val name: String,
    val description: String,
    val lon: Double? = null,
    @PrimaryKey
    val id: String,
    val lat: Double? = null
) : Parcelable