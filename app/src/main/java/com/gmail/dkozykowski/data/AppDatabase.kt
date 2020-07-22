package com.gmail.dkozykowski.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmail.dkozykowski.data.model.Task

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}