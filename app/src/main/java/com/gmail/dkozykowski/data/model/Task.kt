package com.gmail.dkozykowski.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String,
    @ColumnInfo val date: Long,
    @ColumnInfo var important: Boolean,
    @ColumnInfo var done: Boolean
)