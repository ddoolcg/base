package com.lcg.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.lcg.base.net.post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FilenameFilter
import java.io.PrintWriter
import java.io.StringWriter

/**
 * 异常退出信息收集类
 */
@SuppressLint("StaticFieldLeak")
object CrashHandler : Thread.UncaughtExceptionHandler {
    private const val CRASH_REPORTER_EXTENSION = ".log"
    private lateinit var context: Context
    private var url: String? = null
    private var intercept: ((String, StringBuffer) -> Boolean)? = null
    private var packageName = "未知"
    private var versionName = "未知"
    private var versionCode = -1

    /**
     * @param attach 拦截处理: 保存在files路径下的文件名、异常信息、return：是否已被拦截处理
     *
     *  最好加入下面代码解决：activity创建生命周期时奔溃导致无限重启，在创建中的时候奔溃会把上一个activity关闭
     * ```
     * val activities: ArrayList<Activity> = BaseActivity.getActivities()
     * if (activities.isNotEmpty()) {
     *    val activity = activities[activities.size - 1]
     *    activity.finish()
     * }
     * ```
     */
    operator fun invoke(
        application: Application,
        url: String? = null,
        attach: ((String, StringBuffer) -> Boolean)? = null
    ): CrashHandler {
        context = application
        this.url = url
        this.intercept = attach
        packageName = application.packageName
        try {
            val pm = application.packageManager
            val pi = pm.getPackageInfo(
                packageName,
                PackageManager.GET_ACTIVITIES
            )
            if (pi != null) {
                versionName = pi.versionName
                versionCode = pi.versionCode
            }
        } catch (ignored: Exception) {
        }
        return this
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        ex.printStackTrace()
        //如果上报地址为空则让系统默认的异常处理器来处理
        if (url.isNullOrEmpty() || Core.activities.isEmpty()) {
            val handler = Thread.getDefaultUncaughtExceptionHandler()
            handler?.uncaughtException(thread, ex)
        } else {
            saveCrashInfoToFile(ex)
            Core.activities.lastOrNull()?.finish()
            Process.killProcess(Process.myPid())
        }
    }

    /**
     * 保存错误报告文件
     */
    private fun saveCrashInfoToFile(ex: Throwable) {
        val sbTag = StringBuilder()
        val info = StringWriter()
        val printWriter: PrintWriter = object : PrintWriter(info) {
            override fun println(x: String?) {
                super.println(x)
                x?.let { add(it) }
            }

            override fun println(x: Any?) {
                super.println(x)
                if (x != null) add(x.toString())
            }

            private fun add(s: String) {
                if (!s.startsWith("\tat android") && !s.startsWith("\tat com.android.") && !s.startsWith(
                        "\tat java"
                    )
                ) {
                    sbTag.append(s)
                }
            }
        }
        ex.printStackTrace(printWriter)
        val sb = info.buffer
        printWriter.close()
        //缓存到文件
        val fileNameString = MD5.GetMD5Code(sbTag.toString() + versionCode)
        val fileName = fileNameString + CRASH_REPORTER_EXTENSION
        if (intercept?.invoke(fileName, sb) != true) {
            try {
                val trace = context.openFileOutput(fileName, Context.MODE_PRIVATE)
                trace.write(sb.toString().toByteArray())
                trace.flush()
                trace.close()
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * 发送之前的异常
     */
    fun sendPreviousReportsToServer() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val filesDir = context.filesDir
            val filter = FilenameFilter { _, name ->
                name.endsWith(CRASH_REPORTER_EXTENSION)
            }
            filesDir.list(filter)?.forEach {
                try {
                    val cr = File(filesDir, it)
                    postReport(cr)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(Exception::class)
    private suspend fun postReport(file: File) {
        val fr = FileReader(file)
        val reader = BufferedReader(fr)
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line!!.trim { it <= ' ' }).append("<br/>")
        }
        reader.close()
        sendMsg(file, sb.toString())
    }

    /**
     * 发送消息实体
     */
    @Throws(Exception::class)
    private suspend fun sendMsg(file: File, msg: String) {
        url?.let {
            val params = HashMap<String, String?>()
            //            params.put("toemail", "475825657@qq.com");
            params["title"] = file.name
            params["app_name"] = packageName
            params["version_code"] = versionCode.toString() + ""
            params["version_name"] = versionName
            params["content"] = msg
            post<Unit>(it, params)
            file.delete()
        }
    }
}