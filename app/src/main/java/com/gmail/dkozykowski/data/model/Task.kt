package com.gmail.dkozykowski.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo var title: String,
    @ColumnInfo var description: String,
    @ColumnInfo var date: Long?,
    @ColumnInfo var important: Boolean,
    @ColumnInfo var done: Boolean
)