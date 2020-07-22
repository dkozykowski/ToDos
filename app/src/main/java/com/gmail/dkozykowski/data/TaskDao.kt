package com.gmail.dkozykowski.data

import androidx.room.*
import com.gmail.dkozykowski.data.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE done = 'false'")
    fun getAllActiveTasks(): List<Task>

    @Query("SELECT * FROM task WHERE done = 'true'")
    fun getDoneTasks(): List<Task>

    @Query("SELECT * FROM task WHERE important = 'true' AND done = 'false'")
    fun getImportantActiveTasks(): List<Task>

    @Insert
    fun insertTask(task: Task): Long

    @Delete
    fun deleteTask(task: Task)

    @Update
    fun updateTask(task: Task)
}