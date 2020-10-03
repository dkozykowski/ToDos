package com.gmail.dkozykowski.data

import androidx.room.*
import com.gmail.dkozykowski.data.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE not done")
    fun getAllActiveTasks(): List<Task>

    @Query("SELECT * FROM task WHERE done ")
    fun getDoneTasks(): List<Task>

    @Query("SELECT * FROM task WHERE date(datetime(date / 1000, 'unixepoch')) = date('now')")
    fun getTodaysTasks(): List<Task>

    @Query("SELECT * FROM task WHERE instr(lower(title), lower(:title)) > 0 AND instr(lower(description), lower(:description)) > 0 AND date > :olderThan AND date < :newerThan")
    fun getFilteredTasksWithDate(
        title: String,
        description: String,
        olderThan: Long,
        newerThan: Long
    ): List<Task>

    @Query("SELECT * FROM task WHERE instr(lower(title), lower(:title)) > 0 AND instr(lower(description), lower(:description)) > 0")
    fun getFilteredTasksWithoutDate(
        title: String,
        description: String
    ): List<Task>

    @Query("SELECT * FROM task WHERE uid = :id")
    fun getTaskById(id: Long): Task

    @Insert
    fun insertTask(task: Task): Long

    @Delete
    fun deleteTask(task: Task)

    @Update
    fun updateTask(task: Task)
}