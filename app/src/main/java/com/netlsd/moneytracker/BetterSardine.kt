package com.netlsd.moneytracker

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.Sardine
import java.io.File
import java.io.IOException
import java.io.InputStream

class BetterSardine {
    // 这个类主要是为了解决在协程中使用sardine进行IO操作会有警告的问题
    // todo 断网测试, 断网测试退出
    // todo dataStore 调用优化
    // todo 清除数据，重新测试
    // todo sardine抛出确切的异常
    // todo 确保name包含空行 换行，并测试

    private lateinit var sardine: Sardine

    fun setSardine(sardine: Sardine) {
        this.sardine = sardine
    }

    fun exists(url: String): Boolean {
        return try {
            sardine.exists(url)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun createDirectory(url: String) {
        return try {
            sardine.createDirectory(url)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setCredentials(account: String, password: String) {
        sardine.setCredentials(account, password)
    }

    fun list(url: String): List<DavResource>? {
        return try {
            return sardine.list(url, 1)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun get(url: String): InputStream? {
        return try {
            sardine.get(url)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun put(url: String, file: File): Boolean {
        return try {
            sardine.put(url, file, "application/octet-stream")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun move(source: String, dest: String): Boolean {
        return try {
            sardine.move(source, dest)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

//    // todo warning
//    suspend fun list (url: String) : Boolean = suspendCoroutine {
//        try {
//            sardine.list(url, 1)
//            true
//        } catch (e: IOException) {
//            e.printStackTrace()
//            false
//        }
//    }
}