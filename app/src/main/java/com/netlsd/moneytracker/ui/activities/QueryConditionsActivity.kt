package com.netlsd.moneytracker.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netlsd.moneytracker.Const
import com.netlsd.moneytracker.databinding.ActivityQueryConditionsBinding
import com.netlsd.moneytracker.getPeopleNameFile
import com.netlsd.moneytracker.showDatePicker
import com.netlsd.moneytracker.startQueryActivity
import com.netlsd.moneytracker.ui.adapter.PinyinArrayAdapter

class QueryConditionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityQueryConditionsBinding.inflate(layoutInflater)

        val nameList = getPeopleNameFile().readText().split(Const.SPACE)

        val adapter = PinyinArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nameList
        )

        binding.nameAutoTv.setAdapter(adapter)
        binding.startDateTv.setOnClickListener { showDatePicker(it as TextView) }
        binding.endDateTv.setOnClickListener { showDatePicker(it as TextView) }
        binding.queryButton.setOnClickListener {
            val name = binding.nameAutoTv.text.toString().trim()
            val startDate = binding.startDateTv.text.toString()
            val endDate = binding.endDateTv.text.toString()
            startQueryActivity(name, startDate, endDate)
        }

        setContentView(binding.root)
    }
}