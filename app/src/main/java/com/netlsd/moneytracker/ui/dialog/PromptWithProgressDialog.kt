package com.netlsd.moneytracker.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.netlsd.moneytracker.databinding.DialogPromptWithProgresBinding

class PromptWithProgressDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogPromptWithProgresBinding
    var confirmListener: (()->Unit)? = null
    var message = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogPromptWithProgresBinding.inflate(LayoutInflater.from(context))
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.okButton.setOnClickListener {
            showProgressBar()
            confirmListener?.invoke()
        }

        binding.promptTextView.setText(message)

        setContentView(binding.root)

        val width = (context.resources.displayMetrics.widthPixels * 0.8).toInt()
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun showProgressBar() {
        binding.okButton.visibility = View.GONE
        binding.cancelButton.visibility = View.GONE
        binding.promptTextView.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

}