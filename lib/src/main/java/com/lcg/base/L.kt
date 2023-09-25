package com.lcg.base

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

object L {
    private const val TAG = "fast"
    var DEBUG = false
    fun v(msg: String?) {
        v(TAG, msg)
    }

    fun d(msg: String?) {
        d(TAG, msg)
    }

    fun i(msg: String?) {
        i(TAG, msg)
    }

    fun w(msg: String?) {
        w(TAG, msg)
    }

    fun e(msg: String?) {
        e(TAG, msg)
    }

    fun v(tag: String, msg: String?) {
        if (DEBUG) Log.v(tag, "$msg")
    }

    fun d(tag: String, msg: String?) {
        if (DEBUG) Log.d(tag, "$msg")
    }

    fun i(tag: String, msg: String?) {
        if (DEBUG) Log.i(tag, "$msg")
    }

    fun w(tag: String, msg: String?) {
        Log.w(tag, "$msg")
    }

    fun e(tag: String, msg: String?) {
        Log.e(tag, "$msg")
    }

    fun file(msg: String) {
        save("${Core.context?.packageName ?: "app"}.log", msg, true)
    }

    /**
     * 保存string到文件fileName
     */
    fun save(fileName: String, string: String, append: Boolean) {
        var append1 = append
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + fileName
            )
            if (append1 && file.lastModified() < System.currentTimeMillis() - 3600000) append1 =
                false
            val w = FileWriter(file, append1)
            w.write("\n--------------------------\n")
            w.write(string)
            w.flush()
            w.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun file(error: Throwable) {
        val info = StringWriter()
        val printWriter = PrintWriter(info)
        error.printStackTrace(printWriter)
        val result = info.toString()
        printWriter.close()
        file("$error\n$result")
    }
}