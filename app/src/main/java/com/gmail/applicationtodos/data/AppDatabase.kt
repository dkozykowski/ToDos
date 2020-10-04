package com.gmail.applicationtodos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gmail.applicationtodos.data.model.Task

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}