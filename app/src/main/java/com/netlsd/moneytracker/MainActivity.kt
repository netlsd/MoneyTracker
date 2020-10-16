package com.netlsd.moneytracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
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
import java.io.InputStream

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

        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SyncSettingsActivity::class.java))
        }

        sardine = Injector.provideBetterSardine()

        dataStore = Injector.provideWebDavDataStore(this)

        binding.syncButton.setOnClickListener {
            if (account.isEmpty()) {
                toast(getString(R.string.go_to_settings))
            } else {
                val dialog = PromptWithProgressDialog(this)
                dialog.confirmListener = {
                    sardine.setCredentials(account, password)

                    ioScope.launch {
                        if (!sardine.exists(databaseAddress)) {
                            errorToast(dialog, R.string.remote_dir_not_exits)
                            // todo check this
                            return@launch
                         } else {
                            val stream = sardine.get("https://dav.jianguoyun.com/dav/MoneyTracker/new.txt")
                            if (stream == "Error") {
                                errorToast(dialog, R.string.remote_db_not_exits)
                            } else {
                                File("${filesDir}/file.db").saveStream(stream as InputStream)
                            }
                        }
                    }
                }
                dialog.show()
            }
        }

        setContentView(binding.root)
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
}