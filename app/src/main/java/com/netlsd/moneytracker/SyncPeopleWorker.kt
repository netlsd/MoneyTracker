package com.netlsd.moneytracker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.netlsd.moneytracker.di.Injector
import java.io.File

class SyncPeopleWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val context = appContext
    private val dao = Injector.provideNoteDao(context)

    override fun doWork(): Result {
        val peopleNameFile = context.getPeopleNameFile()
        val name = inputData.getString(Const.KEY_PEOPLE_NAME)!!

        if (peopleNameFile.exists()) {
            addName(peopleNameFile, name)
        } else {
            syncAll(peopleNameFile, name)
        }

        return Result.success()
    }

    private fun syncAll(file: File, name: String) {
        val nameList = removeTheDuplicates(dao.getAllName())
        for (i in nameList.indices) {
            file.appendText(nameList[i])
            if (i != nameList.size - 1) {
                file.appendText(Const.SPACE)
            }
        }

        if (!nameList.contains(name)) {
            file.appendText("${Const.SPACE}$name")
        }
    }

    private fun addName(file: File, name: String) {
        val nameList = dao.getAllName()

        if (!nameList.contains(name)) {
            file.appendText("${Const.SPACE}$name")
        }
    }

    private fun removeTheDuplicates(nameList: List<String>): List<String> {
        return nameList.distinct()
    }
}
