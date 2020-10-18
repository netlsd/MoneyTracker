package com.netlsd.moneytracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class QueryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_query)

        findViewById<Button>(R.id.button).setOnClickListener {
            setResult(Const.CODE_DB_UPDATED)
            finish()
        }
    }
}