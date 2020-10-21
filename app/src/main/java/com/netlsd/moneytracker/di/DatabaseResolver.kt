package com.netlsd.moneytracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.netlsd.moneytracker.Const
import com.netlsd.moneytracker.db.AppDatabase


object DatabaseResolver {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS NoteTmp (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, money REAL NOT NULL, type TEXT NOT NULL, date TEXT NOT NULL, comment TEXT, repay REAL)")
            database.execSQL("INSERT INTO NoteTmp (id, name, money, type, date, comment) select _id, NAME, MONEY, TYPE, DATE, COMMENT FROM NOTE")
            database.execSQL("DROP TABLE NOTE")
            database.execSQL("ALTER TABLE NoteTmp RENAME TO Note")
        }
    }

    // disable write ahead logging, it will cache in wal file
    fun provideDatabase(context: Context) =
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, Const.DB_NAME)
            .addMigrations(MIGRATION_1_2)
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()

    fun provideNoteDao(database: AppDatabase) = database.noteDao()
}