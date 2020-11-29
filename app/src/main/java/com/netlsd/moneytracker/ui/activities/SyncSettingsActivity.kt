package com.netlsd.moneytracker.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.netlsd.moneytracker.BetterSardine
import com.netlsd.moneytracker.Const
import com.netlsd.moneytracker.R
import com.netlsd.moneytracker.databinding.ActivitySyncSettingsBinding
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SyncSettingsActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var binding: ActivitySyncSettingsBinding
    private lateinit var sardine: BetterSardine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySyncSettingsBinding.inflate(layoutInflater)
        sardine = Injector.provideBetterSardine()

        val webDavAddressFlow = Injector.provideWebDavAddress(this)
        val accountFlow = Injector.provideWebDavAccount(this)
        val passwordFlow = Injector.provideWebDavPassword(this)

        dataStore = Injector.provideWebDavDataStore(this)

        binding.saveButton.setOnClickListener {
            with(binding) {
                val address = webDavEditText.text.toString()
                val account = accountEditText.text.toString()
                val password = passwordEditText.text.toString()

                if (address.isBlank() || account.isBlank() || password.isBlank()) {
                    toast(getString(R.string.complete_info))
                } else {
                    sardine.setCredentials(account, password)

                    ioScope.launch {
                        val result = sardine.list(address)
                        uiScope.launch {
                            if (result != null) {
                                saveWebDavInfo(address, account, password)
                                toast(getString(R.string.save_success))
                                finish()
                            } else {
                                toast(getString(R.string.check_network_and_account))
                            }
                        }
                    }
                }
            }
        }

        // todo 第一个collect会阻塞后面的flow，尝试其他操作符
//        uiScope.launch {
//            Injector.provideWebDavAddress(this@SyncActivity).flowOn(Dispatchers.IO).collect {
//                binding.webDavEditText.setText(it)
//            }
//            Injector.provideWebDavAccount(this@SyncActivity).flowOn(Dispatchers.IO).collect {
//                binding.accountEditText.setText(it)
//            }
//            Injector.provideWebDavPassword(this@SyncActivity).flowOn(Dispatchers.IO).collect {
//                binding.passwordEditText.setText(it)
//            }
//        }

        loadWebDavAddress(webDavAddressFlow)
        loadWebDavAccount(accountFlow)
        loadWebDavPassword(passwordFlow)

        setContentView(binding.root)
    }

    private fun saveWebDavInfo(address: String, account: String, password: String) {
        ioScope.launch {
            dataStore.edit { preferences ->
                preferences[Const.WEB_DAV_ADDRESS] = address
                preferences[Const.WEB_DAV_ACCOUNT] = account
                preferences[Const.WEB_DAV_PASSWORD] = password
            }
        }
    }

    // todo LiveData 双向绑定
    private fun loadWebDavAddress(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                if (it.isNotEmpty()) {
                    binding.webDavEditText.setText(it)
                }
            }
        }
    }

    private fun loadWebDavAccount(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                binding.accountEditText.setText(it)
            }
        }
    }

    private fun loadWebDavPassword(flow: Flow<String>) {
        uiScope.launch {
            flow.flowOn(Dispatchers.IO).collect {
                binding.passwordEditText.setText(it)
            }
        }
    }
}