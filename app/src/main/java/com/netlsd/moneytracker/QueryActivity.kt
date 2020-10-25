package com.netlsd.moneytracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.netlsd.moneytracker.databinding.ActivityQueryBinding
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.ui.adapter.NoteListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

private const val EXTRA_NAME = "name"
private const val EXTRA_START_DATE = "startDate"
private const val EXTRA_END_DATE = "endDate"

// todo start method
fun Context.startQueryActivity(name: String?, startDate: String?, endDate: String?) {
    val intent = Intent(this, QueryActivity::class.java)
    intent.putExtra(EXTRA_NAME, name)
    intent.putExtra(EXTRA_START_DATE, startDate)
    intent.putExtra(EXTRA_END_DATE, endDate)
    startActivity(intent)
}

class QueryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQueryBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)

    private val listAdapter = NoteListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBroadcast()

        val dao = Injector.provideNoteDao(this)

        val name = intent.getStringExtra(EXTRA_NAME)
        val startDate = intent.getStringExtra(EXTRA_START_DATE).ifEmpty { "1970-01-01" }
        val endDate = intent.getStringExtra(EXTRA_END_DATE).ifEmpty { "2100-12-12" }

        binding = ActivityQueryBinding.inflate(layoutInflater)
        binding.listView.adapter = listAdapter
        binding.listView.setHasFixedSize(true)
        binding.listView.layoutManager = LinearLayoutManager(this)

        listAdapter.onNoteDeletedListener = {
            sendBackupBroadcast()
            countMoney(it)
        }

        uiScope.launch {
            dao.query(name, startDate, endDate).flowOn(Dispatchers.IO).collect {
                listAdapter.addNotes(it)
                countMoney(it)
            }
        }

        setContentView(binding.root)
    }

    private fun countMoney(notes: List<Note>) {
        var totalLoan = 0.0
        var totalRepay = 0.0
        for (note in notes) {
            if (note.type == getString(R.string.loan)) {
                totalLoan += note.money
                if (note.repay != null) {
                    totalRepay += note.repay!!
                }
            } else {
                totalRepay += note.money
            }
        }

        binding.totalLoanTv.text = getString(R.string.total_loan, totalLoan)
        binding.totalRepayTv.text = getString(R.string.total_repay, totalRepay)
        binding.totalDiffTv.text = getString(R.string.total_diff, totalLoan - totalRepay)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val note = intent?.getParcelableExtra<Note>(Const.EXTRA_NOTE)
            listAdapter.updateNote(note!!)
            countMoney(listAdapter.getAllNote())
        }
    }

    private fun registerBroadcast() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.BROADCAST_NOTE_UPDATE)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}