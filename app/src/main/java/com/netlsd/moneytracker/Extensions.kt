@file:Suppress("DEPRECATION")

package com.netlsd.moneytracker

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.netlsd.moneytracker.db.Note
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

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

fun getFormattedDate(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)

    val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return format.format(calendar.time)
}

fun getCurrentDateString(): String {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    return df.format(Calendar.getInstance().time)
}

fun Context.showDatePicker(textView: TextView) {
    val c = Calendar.getInstance()
    val thisYear = c[Calendar.YEAR]
    val thisMonth = c[Calendar.MONTH]
    val thisDay = c[Calendar.DAY_OF_MONTH]

    val listener =
        DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            textView.text = getFormattedDate(year, month, dayOfMonth)
        }

    val datePickerDialog = DatePickerDialog(this, listener, thisYear, thisMonth, thisDay)
    datePickerDialog.show()
}

fun Context.sendBackupBroadcast() {
    val intent = Intent(Const.BROADCAST_BACKUP_DB)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
}

fun Context.sendNoteUpdateBroadcast(note: Note) {
    val intent = Intent(Const.BROADCAST_NOTE_UPDATE)
    intent.putExtra(Const.EXTRA_NOTE, note)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
}

fun Context.getPeopleNameList(): List<String> {
    return if (!getPeopleNameFile().exists()) {
        ArrayList()
    } else {
        getPeopleNameFile().readText().split(Const.SPACE)
    }
}

fun Context.startSyncPeopleWork(name: String?) {
    val syncWorkRequest = OneTimeWorkRequestBuilder<SyncPeopleWorker>()
    val data = Data.Builder()
    data.putString(Const.KEY_PEOPLE_NAME, name)
    syncWorkRequest.setInputData(data.build())
    WorkManager.getInstance(this).enqueue(syncWorkRequest.build())
}