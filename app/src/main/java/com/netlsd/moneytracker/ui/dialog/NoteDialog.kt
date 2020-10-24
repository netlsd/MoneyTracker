package com.netlsd.moneytracker.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netlsd.moneytracker.R
import com.netlsd.moneytracker.databinding.DialogNoteBinding
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.di.Injector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// todo callback hell
class NoteDialog(context: Context, private val note: Note) : Dialog(context) {
    var onDeleteListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val uiScope = CoroutineScope(Dispatchers.Main)
        val ioScope = CoroutineScope(Dispatchers.IO)

        val binding = DialogNoteBinding.inflate(LayoutInflater.from(context))
        val dao = Injector.provideNoteDao(context)
        setCanceledOnTouchOutside(false)

        binding.nameTv.text = note.name
        binding.moneyTv.text = note.money.toString()
        binding.typeTv.text = note.type
        binding.dateTv.text = note.date
        binding.commentTv.text = note.comment

        if (note.repay != null) {
            binding.repayLayout.visibility = View.VISIBLE
            binding.repayTv.text = note.repay.toString()
        }

        binding.closeButton.setOnClickListener { dismiss() }
        binding.deleteButton.setOnClickListener {
            val promptDialog = PromptWithProgressDialog(context)
            promptDialog.message = R.string.confirm_del_note
            promptDialog.confirmListener = {
                ioScope.launch {
                    dao.delete(note)
                    uiScope.launch {
                        onDeleteListener?.invoke()
                        promptDialog.dismiss()
                        dismiss()
                    }
                }
            }
            promptDialog.show()
        }

        setContentView(binding.root)

        val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}