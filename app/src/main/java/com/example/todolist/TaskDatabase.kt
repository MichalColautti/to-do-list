package com.example.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDatabase(context: Context) :
    SQLiteOpenHelper(context, "tasks.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                description TEXT,
                creationTime INTEGER,
                dueTime INTEGER
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun insertTask(task: Task) {
        val values = ContentValues().apply {
            put("title", task.title)
            put("description", task.description)
            put("creationTime", task.creationTime)
            put("dueTime", task.dueTime)
        }
        writableDatabase.insert("tasks", null, values)
    }

    fun getAllTasks(): List<Task> {
        val cursor = readableDatabase.rawQuery("SELECT * FROM tasks", null)
        val tasks = mutableListOf<Task>()

        if (cursor.moveToFirst()) {
            do {
                tasks.add(
                    Task(
                        id = cursor.getInt(0),
                        title = cursor.getString(1),
                        description = cursor.getString(2),
                        creationTime = cursor.getLong(3),
                        dueTime = cursor.getLong(4)
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return tasks
    }
}
