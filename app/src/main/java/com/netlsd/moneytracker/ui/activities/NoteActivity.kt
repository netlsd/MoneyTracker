package com.netlsd.moneytracker.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.netlsd.moneytracker.*
import com.netlsd.moneytracker.databinding.ActivityNoteBinding
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.db.NoteDao
import com.netlsd.moneytracker.di.Injector
import com.netlsd.moneytracker.ui.adapter.NoteListAdapter
import com.netlsd.moneytracker.ui.adapter.PinyinArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val EXTRA_NOTE = "note"
private const val INIT_REPAY = 0.0

// todo toolbar back
fun Context.startNoteActivity(note: Note) {
    val intent = Intent(this, NoteActivity::class.java)
    intent.putExtra(EXTRA_NOTE, note)
    startActivity(intent)
}

// todo !! ?.
class NoteActivity : AppCompatActivity() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var binding: ActivityNoteBinding
    private val listAdapter = NoteListAdapter()
    private lateinit var dao: NoteDao
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = Injector.provideNoteDao(this)

        binding = ActivityNoteBinding.inflate(layoutInflater)
        binding.dateTv.text = getCurrentDateString()
        binding.listView.adapter = listAdapter
        binding.listView.layoutManager = LinearLayoutManager(this)
        // if has fixed size, adapter addNote not working
//        binding.listView.setHasFixedSize(true)
        binding.dateTv.setOnClickListener {
            showDatePicker(it as TextView)
        }

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            binding.batchCheckbox.visibility = View.GONE
            binding.batchText.visibility = View.GONE
            binding.nameAutoTv.setText(note!!.name)
            binding.moneyEdit.setText(note!!.money.toString())
            binding.typeSpinner.isEnabled = false
            binding.dateTv.text = note!!.date
            binding.commentEdit.setText(note!!.comment)

            if (note!!.type == getString(R.string.loan)) {
                binding.typeSpinner.setSelection(0)
                if (note!!.repay != null) {
                    binding.partRepayEdit.setText(note!!.repay.toString())
                } else {
                    binding.partRepayEdit.setText("")
                }
            } else {
                binding.typeSpinner.setSelection(1)
                binding.partRepayEdit.visibility = View.GONE
                binding.partRepayTv.visibility = View.GONE
            }
        } else {
            // only register for new note
            registerBroadcast()
            binding.typeSpinner.onItemSelectedListener = spinnerSelectedListener
            binding.batchCheckbox.setOnCheckedChangeListener(checkedChangeListener)
        }

        binding.okButton.setOnClickListener {
            hideSoftKeyboard(it)

            if (!isAllSet()) {
                toast(R.string.filed_not_set)
                return@setOnClickListener
            }
            if (!isCorrectMoney()) {
                toast(R.string.wrong_money)
                return@setOnClickListener
            }

            if (note == null) {
                insertNote()
            } else {
                updateNote()
            }
        }

        listAdapter.addNotes(ArrayList())
        listAdapter.onNoteDeletedListener = {
            sendBackupBroadcast()
        }

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        updatePeopleName()
    }

    // todo 每次插入新名字不能及时更新，live data？
    private fun updatePeopleName() {
        val adapter = PinyinArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            getPeopleNameList()
        )

        binding.nameAutoTv.setAdapter(adapter)
    }

    private fun updateNote() {
        val isSameName = note!!.name == binding.nameAutoTv.text.toString().trim()
        note!!.name = binding.nameAutoTv.text.toString().trim()
        note!!.date = binding.dateTv.text.toString()
        note!!.comment = binding.commentEdit.text.toString().trim()
        note!!.money = binding.moneyEdit.text.toString().toDouble()
        note!!.repay = getRepay()

        ioScope.launch {
            dao.update(note!!)
            uiScope.launch {
                toast(R.string.edit_success)
                if (!isSameName) {
                    startSyncPeopleWork(note!!.name)
                }
                sendNoteUpdateBroadcast(note!!)
                sendBackupBroadcast()
                finish()
            }
        }
    }

    private fun insertNote() {
        val note = Note(
            binding.nameAutoTv.text.toString().trim(),
            binding.moneyEdit.text.toString().toDouble(),
            binding.typeSpinner.selectedItem.toString(),
            binding.dateTv.text.toString(),
            binding.commentEdit.text.toString().trim(),
            getRepay()
        )

        ioScope.launch {
            dao.insert(note)
            uiScope.launch {
                toast(R.string.note_success)
                resetField()
                listAdapter.addNote(note)
                startSyncPeopleWork(note.name)
                sendNoteUpdateBroadcast(note)
                sendBackupBroadcast()
            }
        }
    }

    private fun getRepay(): Double? {
        val repayText = binding.partRepayEdit.text.toString()
        val repay: Double?

        repay = if (repayText.isBlank() || repayText == INIT_REPAY.toString()) {
            null
        } else {
            repayText.toDouble()
        }

        return repay
    }

    private fun resetField() {
        if (!binding.batchCheckbox.isChecked) {
            binding.nameAutoTv.setText("")
        }
        binding.moneyEdit.setText("")
        binding.partRepayEdit.setText("")
        binding.typeSpinner.setSelection(0)
        binding.dateTv.text = getCurrentDateString()
        binding.commentEdit.setText("")
    }

    private fun isAllSet(): Boolean {
        return !binding.nameAutoTv.text.isBlank() && !binding.moneyEdit.text.isBlank()
    }

    private fun isCorrectMoney(): Boolean {
        try {
            binding.moneyEdit.text.toString().toDouble()
            if (binding.partRepayEdit.text.isNotEmpty()) {
                binding.partRepayEdit.text.toString().toDouble()
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private val checkedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (binding.nameAutoTv.text.isBlank()) {
                    buttonView.isChecked = false
                    toast(R.string.name_empty_tips)
                } else {
                    binding.nameAutoTv.isEnabled = false
                }
            } else {
                binding.nameAutoTv.isEnabled = true
                binding.nameAutoTv.setText("")
            }
        }

    private val spinnerSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            if (position == 0) {
                binding.partRepayEdit.setText("")
                binding.partRepayEdit.isEnabled = true
            } else {
                binding.partRepayEdit.setText(INIT_REPAY.toString())
                binding.partRepayEdit.isEnabled = false
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val note = intent?.getParcelableExtra<Note>(Const.EXTRA_NOTE)
            listAdapter.updateNote(note!!)
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
        if (note == null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        }
    }
}