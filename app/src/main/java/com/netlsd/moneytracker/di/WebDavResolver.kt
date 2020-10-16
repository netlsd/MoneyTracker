package com.netlsd.moneytracker.di

import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import com.netlsd.moneytracker.BetterSardine
import com.netlsd.moneytracker.Const
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


object WebDavResolver {
    fun provideSardine() = OkHttpSardine()
    fun provideBetterSardine() = BetterSardine()

    //    fun provideWebDavAddress(dataStore: DataStore<Preferences>) = "https://dav.jianguoyun.com/dav/"
    fun provideDatabaseDir() = Const.WEB_DAV_DATABASE_DIR_NAME

    fun provideWebDavAddress(dataStore: DataStore<Preferences>): Flow<String> = dataStore.data
        .map { preferences ->
            preferences[Const.WEB_DAV_ADDRESS] ?: ""
        }

    fun provideWebDavAccount(dataStore: DataStore<Preferences>): Flow<String> = dataStore.data
        .map { preferences ->
            preferences[Const.WEB_DAV_ACCOUNT] ?: ""
        }

    fun provideWebDavPassword(dataStore: DataStore<Preferences>): Flow<String> = dataStore.data
        .map { preferences ->
            preferences[Const.WEB_DAV_PASSWORD] ?: ""
        }

}