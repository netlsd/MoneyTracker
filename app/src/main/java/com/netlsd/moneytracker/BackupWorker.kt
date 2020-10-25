package com.netlsd.moneytracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.netlsd.moneytracker.di.Injector
import java.io.File

class BackupWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val context = appContext
    private val dbDiffSize = 60 * 1024

    override fun doWork(): Result {
        backupToExternal()
        backupToWebDav()

        return Result.success()
    }

    private fun backupToExternal() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val backupDir = getBackupDir()
        if (!backupDir.exists() && !backupDir.mkdirs()) {
            // todo log it to a file
            return
        }
        val backupDBFile = getBackupDBFile()
        val dbFile = context.getDBFile()

        if (backupDBFile.exists()) {
            if (dbFile.exists()) {
                // 数据库差异过大，进行备份
                if (backupDBFile.length() - dbFile.length() > dbDiffSize) {
                    backupDBFile.renameTo(File(getBackupDir(), timeStampDBFile()))
                }
                dbFile.copyTo(backupDBFile, true)
            }
        } else {
            if (dbFile.exists()) {
                dbFile.copyTo(backupDBFile, true)
            }
        }
    }

    private fun backupToWebDav() {
        val sardine = Injector.provideBetterSardine()

        val account = inputData.getString(Const.KEY_ACCOUNT)
        val password = inputData.getString(Const.KEY_PASSWORD)
        val databaseAddress = inputData.getString(Const.KEY_DATABASE_ADDRESS)

        if (account.isNullOrBlank() || password.isNullOrBlank() || databaseAddress.isNullOrBlank()) {
            return
        }

        val dbFile = context.getDBFile()
        val remoteDBUrl = "${databaseAddress}/${Const.DB_NAME}"

        sardine.setCredentials(account, password)

        if (!sardine.exists(databaseAddress)) {
            sardine.createDirectory(databaseAddress)
        }

        if (!sardine.exists(remoteDBUrl)) {
            putFileToWabDav(sardine, remoteDBUrl, dbFile)
            return
        }

        val fileList = sardine.list(remoteDBUrl) ?: return

        val dbDavResource = fileList[0]
        if (dbDavResource.contentLength - dbFile.length() > dbDiffSize) {
            val isMoveSuccess = sardine.move(
                remoteDBUrl, "${databaseAddress}/${timeStampDBFile()}"
            )
            if (isMoveSuccess) {
                putFileToWabDav(sardine, remoteDBUrl, dbFile)
            }
        } else {
            putFileToWabDav(sardine, remoteDBUrl, dbFile)
        }
    }

    private fun putFileToWabDav(sardine: BetterSardine, remoteDBUrl: String, dbFile: File) {
        val result = sardine.put(remoteDBUrl, dbFile)
        if (!result) {
            // todo log it to a file
            Log.e(BackupWorker::javaClass.name, "upload db error")
        }
    }

    private fun timeStampDBFile() = "${System.currentTimeMillis()}.db"
}