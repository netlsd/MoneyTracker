package com.netlsd.moneytracker

import androidx.datastore.preferences.preferencesKey

class Const {
    companion object {
        const val WEB_DAV_DATASTORE_NAME = "webdav_settings"
        const val WEB_DAV_DATABASE_DIR_NAME = "MoneyTracker"
        const val BACKUP_DIR_NAME = "backups/MoneyTracker"

        val WEB_DAV_ADDRESS = preferencesKey<String>(name = "web_dav_address")
        val WEB_DAV_ACCOUNT = preferencesKey<String>(name = "web_dav_account")
        val WEB_DAV_PASSWORD = preferencesKey<String>(name = "web_dav_password")

        const val KEY_ACCOUNT = "account"
        const val KEY_PASSWORD = "password"
        const val KEY_DATABASE_ADDRESS = "database_address"
        const val KEY_PEOPLE_NAME = "people_name"

        const val CODE_DB_UPDATED = 200

        const val DB_NAME = "note.db"

        const val PEOPLE_FILE_NAME = "people.txt"

        const val SPACE = " "
    }
}