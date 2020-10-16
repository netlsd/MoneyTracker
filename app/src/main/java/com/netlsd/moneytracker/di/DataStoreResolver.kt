package com.netlsd.moneytracker.di

import android.content.Context
import androidx.datastore.preferences.createDataStore
import com.netlsd.moneytracker.Const

object DataStoreResolver {
    fun provideWebDavDataStore(context: Context) =
        context.createDataStore(name = Const.WEB_DAV_DATASTORE_NAME)
}