package com.netlsd.moneytracker

import com.thegrizzlylabs.sardineandroid.Sardine
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.suspendCoroutine

class BetterSardine {
    // 这个类主要是为了解决在协程中使用sardine进行IO操作会有警告的问题
    // todo 断网测试, intentService 断网测试退出
    // todo dataStore 调用优化
    // todo 清除数据，重新测试
    // todo sardine抛出确切的异常

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

    fun list (url: String) : Boolean {
        return try {
            sardine.list(url, 1)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun get(url: String) : Any {
        return try {
            sardine.get(url)
        } catch (e: IOException) {
            e.printStackTrace()
            "Error"
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