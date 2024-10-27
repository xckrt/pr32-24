package com.example.pr32_24

import androidx.room.*

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Query("UPDATE tasks SET description =:desc, dueDate = :date WHERE id = :id")
    suspend fun update(id: Int, desc: String, date: String)
    @Query("DELETE FROM tasks where id=:id")
    suspend fun deletebyId(id: Int)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>
    @Query("SELECT * FROM tasks WHERE dueDate BETWEEN :startDate AND :endDate")
    suspend fun getTasksByDateRange(startDate: Long, endDate: Long): List<Task>
}
