package com.netlsd.moneytracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.model.PeopleName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class QueryActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        val textView = findViewById<TextView>(R.id.textView)

        val dao = Injector.provideNoteDao(this)

//        ioScope.launch {
//            dao.insert(Note("yang5", 40.00, "1", "2", "3", -30.00))
//        }

        uiScope.launch {
            var result = ""
            dao.getAllNote().flowOn(Dispatchers.IO).collect {
                val note = it[it.size - 1]
                result = note.name + " " + note.money + " " +  note.date + " " + it.size + " " + note.repay
                uiScope.launch {
                    textView.text = result
                }
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener {
//            setResult(Const.CODE_DB_UPDATED)
//            finish()

//            startSyncPeopleWork("张三丰")

            val nameList = getPeopleNameFile().readText().split(Const.SPACE)
            for (name in nameList) {
                Log.e("xxxx", "name is " + name)
            }
        }
    }

    private fun startSyncPeopleWork(name: String) {
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPeopleWorker>()
        val data = Data.Builder()
        data.putString(Const.KEY_PEOPLE_NAME, name)
        syncWorkRequest.setInputData(data.build())
        WorkManager.getInstance(this).enqueue(syncWorkRequest.build())
    }

    override fun onBackPressed() {
//        setResult(Const.CODE_DB_UPDATED)
        super.onBackPressed()
    }
}