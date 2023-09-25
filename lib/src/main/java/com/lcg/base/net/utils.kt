package com.lcg.base.net

import com.alibaba.fastjson.JSONException
import com.lcg.base.L
import com.lcg.base.showToast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URLEncoder

/**请求：协程*/
suspend fun <T> request(
    path: String, handler: ResponseHandler<T>, method: (Request.Builder) -> Unit
): T = withContext(CoroutineName("NET[${path.hashCode()}]") + Dispatchers.IO) {
    requestUnmodified(path, handler, method)
}
//region 四大请求：协程风格
/**get*/
suspend inline fun <reified T> get(
    path: String, paramsMap: HashMap<String, String?>? = null, style: Http.Style = Http.defaultStyle
): T = request(urlJoint(path, paramsMap), object : JsonResponseHandler<T>(style) {}) {
    it.get()
}

/**delete*/
suspend inline fun <reified T> delete(
    path: String, paramsMap: HashMap<String, String?>? = null, style: Http.Style = Http.defaultStyle
): T = request(urlJoint(path, paramsMap), object : JsonResponseHandler<T>(style) {}) {
    it.delete()
}

/**post*/
suspend inline fun <reified T> post(
    path: String, json: String, style: Http.Style = Http.defaultStyle
): T = request(path, object : JsonResponseHandler<T>(style) {}) {
    it.post(json.toRequestBody("application/json".toMediaType()))
}

/**post*/
suspend inline fun <reified T> post(
    path: String, paramsMap: HashMap<String, String?>? = null, style: Http.Style = Http.defaultStyle
): T = request(path, object : JsonResponseHandler<T>(style) {}) {
    val f = FormBody.Builder()
    paramsMap?.forEach { (t, u) ->
        if (u != null) f.add(t, u)
    }
    it.post(f.build())
}

/**put*/
suspend inline fun <reified T> put(
    path: String, json: String, style: Http.Style = Http.defaultStyle
): T = request(path, object : JsonResponseHandler<T>(style) {}) {
    it.put(json.toRequestBody("application/json".toMediaType()))
}

/**put*/
suspend inline fun <reified T> put(
    path: String, paramsMap: HashMap<String, String?>? = null, style: Http.Style = Http.defaultStyle
): T = request(path, object : JsonResponseHandler<T>(style) {}) {
    val f = FormBody.Builder()
    paramsMap?.forEach { (t, u) ->
        if (u != null) f.add(t, u)
    }
    it.put(f.build())
}
//endregion
//region 外部扩展
/**根据参数拼接url*/
fun urlJoint(url: String, paramsMap: HashMap<String, String?>?): String {
    return if (!paramsMap.isNullOrEmpty()) {
        val params = paramsMap.entries.joinToString("&") {
            "%s=%s".format(it.key, URLEncoder.encode(it.value, "utf-8"))
        }
        val start = if (url.contains("?")) "&" else "?"
        url + start + params
    } else {
        url
    }
}

/**处理http异常*/
val httpThrowableHandler by lazy { CoroutineExceptionHandler { _, throwable -> throwable.httpHandle() } }

/**http异常处理*/
fun Throwable.httpHandle(handler: ((Int, Throwable) -> Boolean)? = null) {
    if (L.DEBUG) printStackTrace()
    val code = (this as? HttpStatusException)?.code ?: 0
    if (handler?.invoke(code, this) == true) return
    if (Http.failHandler?.invoke(code, this) == true) return
    if (code == 401) {
        Token.loginSubscriber?.let {
            it.invoke(false)
            return
        }
    }
    val msg = when (this) {
        is CancellationException -> null
        is HttpStatusException -> {
            when (code) {
                403 -> "禁止该请求"
                404 -> "该请求不存在"
                else -> "${this.info}:${this.code}"
            }
        }

        is JSONException -> "服务器数据异常"
        is SocketTimeoutException -> "请求超时"
        is IOException -> "网络堵塞"
        else -> this.message ?: this.javaClass.name
    }
    if (!msg.isNullOrEmpty()) showToast(msg)
}
//endregion