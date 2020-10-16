package com.netlsd.moneytracker

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import java.io.File
import java.io.InputStream

fun <T> Context.startActivityAndFinish(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
    (this as Activity).finish()
}

fun Context.toast(text: String?) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Context.toast(textId: Int) = Toast.makeText(this, textId, Toast.LENGTH_LONG).show()

fun File.saveStream(inputStream: InputStream) = this.outputStream().use { inputStream.copyTo(it) }
