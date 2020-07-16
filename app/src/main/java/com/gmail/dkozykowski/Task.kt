package com.gmail.dkozykowski

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val description: String,
    @ColumnInfo val date: Long,
    @ColumnInfo val important: Boolean,
    @ColumnInfo val done: Boolean
)