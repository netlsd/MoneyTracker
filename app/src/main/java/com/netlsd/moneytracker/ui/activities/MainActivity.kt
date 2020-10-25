package com.netlsd.moneytracker.ui.activities

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.netlsd.moneytracker.*
import com.netlsd.moneytracker.databinding.ActivityMainBinding
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.ui.dialog.PromptWithProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var sardine: BetterSardine

    private lateinit var databaseAddress: String
    private lateinit var account: String
    private lateinit var password: String

    private var isNeedBackup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBroadcast()

        sardine = Injector.provideBetterSardine()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        // todo only once
        requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SyncSettingsActivity::class.java))
        }

        binding.syncButton.setOnClickListener {
            syncDatabase()
        }

        binding.queryButton.setOnClickListener {
            startActivity(Intent(this, QueryConditionsActivity::class.java))
        }

        binding.noteButton.setOnClickListener {
            startActivity(Intent(this, NoteActivity::class.java))
        }

        setContentView(binding.root)
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
                                startSyncPeopleWork(null)
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
                    uiScope.launch {
                        dialog.dismiss()
                        startSyncPeopleWork(null)
                    }
                }
            }
            dialog.show()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isNeedBackup = true
        }
    }

    private fun registerBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.BROADCAST_BACKUP_DB)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun unregisterBroadcast() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
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

    override fun onStop() {
        if (isNeedBackup) {
            startBackupWork()
            isNeedBackup = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }
}