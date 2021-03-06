package com.netlsd.moneytracker.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.netlsd.moneytracker.R
import com.netlsd.moneytracker.databinding.ItemNoteBinding
import com.netlsd.moneytracker.db.Note
import com.netlsd.moneytracker.ui.dialog.NoteDialog
import java.util.*

class NoteListAdapter : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {
    private var noteList = Collections.emptyList<Note>()
    var onNoteDeletedListener: ((noteList: List<Note>) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = noteList[position]
        holder.bind(note)
    }

    fun addNotes(noteList: List<Note>) {
        this.noteList = noteList
        notifyDataSetChanged()
    }

    fun deleteNote(note: Note) {
        val index = noteList.indexOf(note)
        if (index != -1) {
            noteList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun addNote(note: Note) {
        if (!noteList.contains(note)) {
            noteList.add(0, note)
            notifyItemInserted(0)
        }
    }

    fun updateNote(note: Note) {
        // indexOf can't find note in list, so i compare id
        for ((index, n) in noteList.withIndex()) {
            if (note.id == n.id) {
                noteList[index] = note
                notifyItemChanged(index)
                break
            }
        }
    }

    fun getAllNote(): List<Note> {
        return noteList
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context

        fun bind(note: Note) {
            binding.nameTv.text = context.getString(R.string.holder_name, note.name)
            binding.typeMoneyTv.text =
                context.getString(R.string.holder_type_money, note.type, note.money.toString())
            binding.dateTv.text = context.getString(R.string.holder_date, note.date)
            binding.commentTv.text = context.getString(R.string.holder_comment, note.comment)

            if (note.type == context.getString(R.string.loan)) {
                binding.typeMoneyTv.setTextColor(ContextCompat.getColor(context, R.color.red_a200))
            } else {
                binding.typeMoneyTv.setTextColor(ContextCompat.getColor(context, R.color.green_500))
            }

            if (note.repay == null) {
                binding.repayTv.visibility = View.GONE
            } else {
                binding.repayTv.visibility = View.VISIBLE
                binding.repayTv.text =
                    context.getString(R.string.holder_part_repay, note.repay.toString())
            }

            if (note.comment.isNullOrEmpty()) {
                binding.commentTv.visibility = View.GONE
            } else {
                binding.commentTv.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                val dialog = NoteDialog(context, note)
                dialog.onDeleteListener = {
                    deleteNote(note)
                    onNoteDeletedListener?.invoke(noteList)
                }
                dialog.show()
            }
        }
    }
}