package com.netlsd.moneytracker.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.netlsd.moneytracker.*
import com.netlsd.moneytracker.databinding.ActivityQueryConditionsBinding
import com.netlsd.moneytracker.ui.adapter.PinyinArrayAdapter

class QueryConditionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQueryConditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueryConditionsBinding.inflate(layoutInflater)

        binding.startDateTv.setOnClickListener { showDatePicker(it as TextView) }
        binding.endDateTv.setOnClickListener { showDatePicker(it as TextView) }
        binding.queryButton.setOnClickListener {
            val name = binding.nameAutoTv.text.toString().trim()
            val startDate = binding.startDateTv.text.toString()
            val endDate = binding.endDateTv.text.toString()
            startQueryActivity(name, startDate, endDate)
        }
        binding.clearImage.setOnClickListener {
            clearQueryDate()
        }

        setContentView(binding.root)
    }

    // todo 减少每次进入加载人名
    override fun onResume() {
        super.onResume()

        val adapter = PinyinArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getPeopleNameList()
        )

        binding.nameAutoTv.setAdapter(adapter)
    }

    private fun clearQueryDate() {
        binding.startDateTv.text = ""
        binding.endDateTv.text = ""
    }
}