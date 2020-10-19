package com.netlsd.moneytracker

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.netlsd.moneytracker.databinding.ActivityMainBinding
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.ui.dialog.PromptWithProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var sardine: BetterSardine

    private lateinit var databaseAddress: String
    private lateinit var account: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        // todo only once
        requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SyncSettingsActivity::class.java))
        }

        sardine = Injector.provideBetterSardine()
        dataStore = Injector.provideWebDavDataStore(this)

        binding.syncButton.setOnClickListener {
            syncDatabase()
        }

        binding.queryButton.setOnClickListener {
            startActivityForResult.launch(Intent(this, QueryActivity::class.java))
        }

        setContentView(binding.root)
    }

    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Const.CODE_DB_UPDATED) {
                startBackupWork()
            }
        }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                autoRestoreBackup()
            } else {
                toast("没有文件写入权限您将无法使用本地数据库恢复功能")
            }
        }

    private fun syncDatabase() {
        if (account.isEmpty()) {
            toast(getString(R.string.go_to_settings))
        } else {
            val dialog = PromptWithProgressDialog(this)
            dialog.message = R.string.sync_confirm
            dialog.confirmListener = {
                sardine.setCredentials(account, password)

                ioScope.launch {
                    if (!sardine.exists(databaseAddress)) {
                        errorToast(dialog, R.string.remote_dir_not_exits)
                    } else {
                        val stream = sardine.get("${databaseAddress}/${Const.DB_NAME}")
                        if (stream == null) {
                            errorToast(dialog, R.string.remote_db_not_exits)
                        } else {
                            getDBFile().saveStream(stream)
                            uiScope.launch {
                                dialog.dismiss()
                                toast(R.string.already_update)
                            }
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun errorToast(dialog: PromptWithProgressDialog, textId: Int) {
        uiScope.launch {
            dialog.dismiss()
            toast(textId)
        }
    }

    override fun onResume() {
        super.onResume()
        loadWebDavAccount(Injector.provideWebDavAccount(this))
        loadWebDavDatabaseAddress(Injector.provideWebDavDatabaseAddress(this))
        loadWebDavPassword(Injector.provideWebDavPassword(this))
    }

    private fun loadWebDavDatabaseAddress(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                databaseAddress = it
            }
        }
    }

    private fun loadWebDavAccount(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                account = it
            }
        }
    }

    private fun loadWebDavPassword(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                password = it
            }
        }
    }

    private fun autoRestoreBackup() {
        val dbFile = getDBFile()
        val backupDBFile = getBackupDBFile()

        if (!dbFile.exists() && backupDBFile.exists()) {
            val dialog = PromptWithProgressDialog(this)
            dialog.message = R.string.auto_restore_backup
            dialog.confirmListener = {
                ioScope.launch {
                    backupDBFile.copyTo(dbFile)
                    uiScope.launch { dialog.dismiss() }
                }
            }
            dialog.show()
        }
    }

    private fun startBackupWork() {
        val backupWorkRequest = OneTimeWorkRequestBuilder<BackupWorker>()
        val data = Data.Builder()
        data.putString(Const.KEY_ACCOUNT, account)
        data.putString(Const.KEY_PASSWORD, password)
        data.putString(Const.KEY_DATABASE_ADDRESS, databaseAddress)
        backupWorkRequest.setInputData(data.build())
        WorkManager.getInstance(this).enqueue(backupWorkRequest.build())
    }

}