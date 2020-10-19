package com.netlsd.moneytracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.netlsd.moneytracker.Const
import com.netlsd.moneytracker.db.AppDatabase


object DatabaseResolver {
    // disable write ahead logging, it will cache in wal file
    fun provideDatabase(context: Context) =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, Const.DB_NAME)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

    fun provideNoteDao(database: AppDatabase) = database.noteDao()
}