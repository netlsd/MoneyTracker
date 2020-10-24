package com.netlsd.moneytracker.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netlsd.moneytracker.Const
import com.netlsd.moneytracker.databinding.ActivityNoteBinding
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.getCurrentDateString
import com.netlsd.moneytracker.getPeopleNameFile
import com.netlsd.moneytracker.showDatePicker
import com.netlsd.moneytracker.ui.adapter.NoteListAdapter
import com.netlsd.moneytracker.ui.adapter.PinyinArrayAdapter

class NoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = Injector.provideNoteDao(this)
        val listAdapter = NoteListAdapter()


        val nameList = getPeopleNameFile().readText().split(Const.SPACE)
        val adapter = PinyinArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            nameList
        )

        val binding = ActivityNoteBinding.inflate(layoutInflater)
        binding.nameAutoTv.setAdapter(adapter)
        binding.dateTv.text = getCurrentDateString()
        binding.listView.adapter = listAdapter
        binding.listView.layoutManager = LinearLayoutManager(this)
        binding.listView.setHasFixedSize(true)


        binding.dateTv.setOnClickListener {
            showDatePicker(it as TextView)
        }

        setContentView(binding.root)
    }
}