package com.lcg.base.net

import android.os.SystemClock
import com.lcg.base.L
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date
import kotlin.coroutines.coroutineContext

/**
 * 网络
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
object Http {
    /**OkHttpClient实例*/
    var client = OkHttpClient()

    /**http的主地址*/
    var host = ""

    /**http的统一请求头拓展*/
    var headers: HashMap<String, String>? = null

    /**http的统一异常处理拓展*/
    var failHandler: ((Int, Throwable?) -> Boolean)? = null

    /**默认解析数据风格*/
    var defaultStyle = Style.JSON_WHOLE

    /**服务器时间和开机时间差值*/
    internal var timeDifference = Long.MAX_VALUE

    /**服务器时间*/
    val date: Date
        get() = if (timeDifference == Long.MAX_VALUE) Date() else Date(timeDifference + SystemClock.elapsedRealtime())

    enum class Style {
        /**整个json都是有效数据*/
        JSON_WHOLE,

        /**json带错误状态的风格*/
        JSON_STATUS
    }
}
//region 具体实现
/**请求具体实现*/
@Throws(Exception::class)
suspend fun <T> requestUnmodified(
    path: String,
    handler: ResponseHandler<T>,
    method: (Request.Builder) -> Unit
): T {
    val url = if ("https?://[^ \n]+".toRegex() matches (path)) path else Http.host + path
    //
    val builder = Request.Builder()
    builder.url(url)
    method(builder)
    //header
    val token: String = getToken()
    if (token.isNotEmpty()) {
        builder.addHeader(Token.TOKEN, token)
    }
    Http.headers?.forEach {
        builder.addHeader(it.key, it.value)
    }
    //建立请求
    val request = builder.build()
    L.d(
        currentCoroutineContext()[CoroutineName]?.name ?: "NET",
        "${request.method}->$url ${if (token.isEmpty()) "" else "${Token.TOKEN}=$token"}"
    )
    currentCoroutineContext().ensureActive()
    val call = Http.client.newCall(request)
    val response = call.execute()
    currentCoroutineContext().ensureActive()
    //获取服务器时间
    val l = try {
        val date = response.header("date")
        val serverTime = Date(date).time
        serverTime - SystemClock.elapsedRealtime()
    } catch (_: Exception) {
        Http.timeDifference
    }
    if (l < Http.timeDifference) Http.timeDifference = l
    //开始数据处理
    currentCoroutineContext().ensureActive()
    return handler(response)
}
//endregion