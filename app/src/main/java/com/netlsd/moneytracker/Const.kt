package com.netlsd.moneytracker

import androidx.datastore.preferences.preferencesKey

class Const {
    companion object {
        const val WEB_DAV_DATASTORE_NAME = "webdav_settings"
        const val WEB_DAV_DATABASE_DIR_NAME = "MoneyTracker"

        val WEB_DAV_ADDRESS = preferencesKey<String>(name = "web_dav_address")
        val WEB_DAV_ACCOUNT = preferencesKey<String>(name = "web_dav_account")
        val WEB_DAV_PASSWORD = preferencesKey<String>(name = "web_dav_password")
    }
}