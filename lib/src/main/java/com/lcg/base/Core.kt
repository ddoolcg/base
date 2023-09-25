package com.lcg.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import com.hjq.toast.ToastUtils
import com.hjq.toast.config.IToastStyle
import com.lcg.base.net.Http
import com.lcg.base.net.Token
import java.io.File
import java.util.Date

/**
 * 核心类
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2023/9/19 15:56
 */
@SuppressLint("StaticFieldLeak")
object Core {
    var context: Context? = null
        private set

    @JvmStatic
    var activities = ArrayList<Activity>()

    /**初始化
     * @param toastStyle toast显示风格
     */
    @JvmOverloads
    fun init(application: Application, toastStyle: IToastStyle<out View> = MyToastStyle): Core {
        context = application
        ToastUtils.init(application, toastStyle)
        return this
    }

    fun setDebug(debug: Boolean): Core {
        L.DEBUG = debug
        return this
    }

    /**设置奔溃信息收集服务器地址
     * @param url post请求， 请求表单：
     *
     * title：file.getName()
     *
     * app_name：app包名
     *
     *version_code：app的versionCode
     *
     * version_name：app的versionName
     *
     * content： 异常内容
     * @param intercept 异常拦截
     */
    @Synchronized
    fun setCrashURL(
        url: String,
        intercept: (String, StringBuffer) -> Boolean = { fileName, sb ->
            if (File(context!!.filesDir.path, fileName).exists()) {
                true
            } else {
                val code = try {
                    val pi = context!!.packageManager.getPackageInfo(
                        context!!.packageName,
                        PackageManager.GET_ACTIVITIES
                    )
                    pi?.versionCode ?: -1
                } catch (e: Exception) {
                    -1
                }
                sb.append(
                    """------------------------------------------------------------------------------------------
                异常时间：${Date().toLocaleString()} 异常版本：$code
                DEVICE：os=${Build.VERSION.SDK_INT} d=${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE} cpu=${Build.CPU_ABI}${Build.CPU_ABI2}
                FINGERPRINT：${Build.FINGERPRINT}
            """.trimIndent()
                )
                false
            }
        }
    ): Core {
        val crashHandler = CrashHandler(context as Application, url, intercept)
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
        crashHandler.sendPreviousReportsToServer()
        return this
    }

    /**初始化Token认证*/
    @JvmOverloads
    fun initToken(
        headerKey: String = "Authorization",
        login: (showToast: Boolean) -> Unit
    ): Core {
        Token.init(headerKey, login)
        return this
    }

    /**设置联网请求统一的失败处理器*/
    @JvmOverloads
    fun initHttp(
        host: String,
        headers: HashMap<String, String>? = hashMapOf(
            "Content-Type" to "application/json",
            "os" to "android"
        ),
        fail: (code: Int, data: Throwable?) -> Boolean
    ): Core {
        Http.host = host
        Http.headers = headers
        Http.failHandler = fail
        return this
    }

    /**activity开始创建*/
    fun activityCreateStart(activity: Activity) {
        activities.add(activity)
    }

    /**activity已经销毁*/
    fun activityDestroyed(activity: Activity) {
        activities.remove(activity)
    }
}

/**
 * 对toast的简易封装。线程安全，可以在非UI线程调用。
 */
fun showToast(str: CharSequence) {
    val mainLooper = Looper.getMainLooper()
    if (Thread.currentThread().id == mainLooper.thread.id) {
        ToastUtils.show(str)
    } else {
        Handler(mainLooper).post { showToast(str) }
    }
}

/**isNullOrEmpty时转为非空string*/
infix fun String?.emptyTo(string: String?): String =
    if (this.isNullOrEmpty()) (string ?: "") else this

