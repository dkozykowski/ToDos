package com.gmail.dkozykowski.data

import androidx.room.*
import com.gmail.dkozykowski.data.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE not done")
    fun getAllActiveTasks(): List<Task>

    @Query("SELECT * FROM task WHERE done ")
    fun getDoneTasks(): List<Task>

    @Query("SELECT * FROM task WHERE important AND not done")
    fun getImportantActiveTasks(): List<Task>

    @Insert
    fun insertTask(task: Task): Long

    @Delete
    fun deleteTask(task: Task)

    @Update
    fun updateTask(task: Task)
}