package com.example.toodo.data.local.db

import androidx.room.*
import com.example.toodo.data.local.db.converter.Converters
import com.example.toodo.data.local.db.dao.*
import com.example.toodo.data.local.db.entity.*

@Database(
    entities = [
        TaskEntity::class,
        TaskListEntity::class,
        RecurrenceRuleEntity::class,
        ReminderConfigEntity::class,
        SubTaskEntity::class,
        CompletionRecordEntity::class,
        StreakFreezeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ToodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun taskListDao(): TaskListDao
    abstract fun recurrenceRuleDao(): RecurrenceRuleDao
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun completionRecordDao(): CompletionRecordDao
}
