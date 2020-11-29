package com.netlsd.moneytracker.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

import com.netlsd.moneytracker.BetterSardine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object Injector {
    private val sardine = WebDavResolver.provideSardine()
    private val betterSardine = WebDavResolver.provideBetterSardine()

    //    private val webDavAddress = WebDavResolver.provideWebDavAddress()
//    private val webDavAccount = WebDavResolver.provideWebDavAccount()
//    private val webDavPassword = WebDavResolver.provideWebDavPassword()
    private val webDavDatabaseDir = WebDavResolver.provideDatabaseDir()

//    fun provideBetterSardine(activity: AppCompatActivity): BetterSardine {
//        sardine.setCredentials(provideWebDavAccount(activity), provideWebDavPassword(activity))
//        betterSardine.setSardine(sardine)
//        return betterSardine
//    }

    fun provideBetterSardine(): BetterSardine {
        betterSardine.setSardine(sardine)
        return betterSardine
    }

//    fun provideWebDavDatabaseAddress(activity: AppCompatActivity): String {
//        return "${provideWebDavAddress(activity)}${webDavDatabaseDir}"
//    }

    fun provideWebDavDataStore(activity: AppCompatActivity): DataStore<Preferences> {
        return DataStoreResolver.provideWebDavDataStore(activity)
    }

    fun provideWebDavAddress(activity: AppCompatActivity): Flow<String> {
        return WebDavResolver.provideWebDavAddress(provideWebDavDataStore(activity))
    }

    fun provideWebDavAccount(activity: AppCompatActivity): Flow<String> {
        return WebDavResolver.provideWebDavAccount(provideWebDavDataStore(activity))
    }

    fun provideWebDavPassword(activity: AppCompatActivity): Flow<String> {
        return WebDavResolver.provideWebDavPassword(provideWebDavDataStore(activity))
    }

    fun provideWebDavDatabaseAddress(activity: AppCompatActivity): Flow<String> {
        return provideWebDavAddress(activity).map { "${it}${webDavDatabaseDir}" }
    }

    private fun provideDatabase(context: Context) =
        DatabaseResolver.provideDatabase(context)

    fun provideNoteDao(context: Context) =
        DatabaseResolver.provideNoteDao(provideDatabase(context))
}