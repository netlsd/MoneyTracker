package com.netlsd.moneytracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.di.Injector
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
            dao.getAll().flowOn(Dispatchers.IO).collect {
                val note = it[it.size - 1]
                result = note.name + " " + note.money + " " +  note.date + " " + it.size + " " + note.repay
                uiScope.launch {
                    textView.text = result
                }
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            setResult(Const.CODE_DB_UPDATED)
            finish()
        }
    }

    override fun onBackPressed() {
//        setResult(Const.CODE_DB_UPDATED)
        super.onBackPressed()
    }
}