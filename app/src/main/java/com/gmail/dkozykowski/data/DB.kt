package com.gmail.dkozykowski.data

import android.content.Context
import androidx.room.Room

object DB {
    lateinit var db: AppDatabase

    fun createDatabase(context: Context) {
        db = Room.databaseBuilder(context, AppDatabase::class.java, "db")
            .addMigrations()
            .build()
    }
}