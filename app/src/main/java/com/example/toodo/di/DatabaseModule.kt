package com.example.toodo.di

import android.content.Context
import androidx.room.Room
import com.example.toodo.data.local.db.ToodoDatabase
import com.example.toodo.data.local.db.dao.*
import com.example.toodo.data.local.datastore.UserPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ToodoDatabase {
        return Room.databaseBuilder(context, ToodoDatabase::class.java, "toodo_database")
            .build()
    }

    @Provides
    fun provideTaskDao(db: ToodoDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideSubTaskDao(db: ToodoDatabase): SubTaskDao = db.subTaskDao()

    @Provides
    fun provideTaskListDao(db: ToodoDatabase): TaskListDao = db.taskListDao()

    @Provides
    fun provideRecurrenceRuleDao(db: ToodoDatabase): RecurrenceRuleDao = db.recurrenceRuleDao()

    @Provides
    fun provideReminderConfigDao(db: ToodoDatabase): ReminderConfigDao = db.reminderConfigDao()

    @Provides
    fun provideCompletionRecordDao(db: ToodoDatabase): CompletionRecordDao = db.completionRecordDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}
