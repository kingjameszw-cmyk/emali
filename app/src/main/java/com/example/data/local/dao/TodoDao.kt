package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.TodoTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {

    @Query("SELECT * FROM todo_tasks ORDER BY isCompleted ASC, priority = 'HIGH' DESC, dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TodoTaskEntity>>

    @Query("SELECT * FROM todo_tasks WHERE isCompleted = 0 ORDER BY priority = 'HIGH' DESC, dueDateMillis ASC")
    fun getPendingTasks(): Flow<List<TodoTaskEntity>>

    @Query("SELECT * FROM todo_tasks WHERE category = :category ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getTasksByCategory(category: String): Flow<List<TodoTaskEntity>>

    @Query("SELECT * FROM todo_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TodoTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TodoTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TodoTaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: TodoTaskEntity)

    @Query("UPDATE todo_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, completed: Boolean)

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM todo_tasks WHERE isCompleted = 1")
    suspend fun clearCompletedTasks()
}
