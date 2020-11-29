package com.netlsd.moneytracker

import androidx.datastore.preferences.core.preferencesKey

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

        const val BROADCAST_BACKUP_DB = "com.netlsd.moneytracker.backup_db"
        const val BROADCAST_NOTE_UPDATE = "com.netlsd.moneytracker.note_update"
        const val EXTRA_NOTE = "note"

        const val DB_NAME = "note.db"

        const val PEOPLE_FILE_NAME = "people.txt"

        const val SPACE = " "
    }
}