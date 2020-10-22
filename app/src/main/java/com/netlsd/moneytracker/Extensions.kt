@file:Suppress("DEPRECATION")

package com.netlsd.moneytracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
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

fun Context.getDBFile(): File = getDatabasePath(Const.DB_NAME)
fun getBackupDir() = File(Environment.getExternalStorageDirectory(), Const.BACKUP_DIR_NAME)
fun getBackupDBFile() = File(getBackupDir(), Const.DB_NAME)
fun Context.getPeopleNameFile() = File(filesDir, Const.PEOPLE_FILE_NAME)