package com.lcg.base.net.flow

import com.lcg.base.Loading
import com.lcg.base.net.Http
import com.lcg.base.net.JsonResponseHandler
import com.lcg.base.net.ResponseHandler
import com.lcg.base.net.httpHandle
import com.lcg.base.net.requestUnmodified
import com.lcg.base.net.urlJoint
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**请求：流的方式*/
fun <T> Loading?.request(
    path: String,
    handler: ResponseHandler<T>,
    fail: ((Int, Throwable) -> Boolean)?,
    method: (Request.Builder) -> Unit
): Flow<T> {
    val context = CoroutineName("NET[${path.hashCode()}]") + Dispatchers.IO
    return flow {
        val t = requestUnmodified(path, handler, method)
        emit(t)
    }.flowOn(context)
        .catch { it.httpHandle(fail) }
        .onStart { this@request?.show(context[Job]) }
        .onCompletion { this@request?.dismiss() }
}
//region 四大请求：Flow风格
/**get*/
inline fun <reified T> Loading?.get(
    path: String,
    paramsMap: HashMap<String, String?>? = null,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(urlJoint(path, paramsMap), object : JsonResponseHandler<T>(style) {}, fail) {
    it.get()
}

/**delete*/
inline fun <reified T> Loading?.delete(
    path: String,
    paramsMap: HashMap<String, String?>? = null,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(urlJoint(path, paramsMap), object : JsonResponseHandler<T>(style) {}, fail) {
    it.delete()
}

/**post*/
inline fun <reified T> Loading?.post(
    path: String,
    json: String,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(path, object : JsonResponseHandler<T>(style) {}, fail) {
    it.post(json.toRequestBody("application/json".toMediaType()))
}

/**post*/
inline fun <reified T> Loading?.post(
    path: String,
    paramsMap: HashMap<String, String?>? = null,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(path, object : JsonResponseHandler<T>(style) {}, fail) {
    val f = FormBody.Builder()
    paramsMap?.forEach { (t, u) ->
        if (u != null) f.add(t, u)
    }
    it.post(f.build())
}

inline fun <reified T> Loading?.put(
    path: String,
    json: String,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(path, object : JsonResponseHandler<T>(style) {}, fail) {
    it.put(json.toRequestBody("application/json".toMediaType()))
}

/**put*/
inline fun <reified T> Loading?.put(
    path: String,
    paramsMap: HashMap<String, String?>? = null,
    style: Http.Style = Http.defaultStyle,
    noinline fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<T> = request(path, object : JsonResponseHandler<T>(style) {}, fail) {
    val f = FormBody.Builder()
    paramsMap?.forEach { (t, u) ->
        if (u != null) f.add(t, u)
    }
    it.put(f.build())
}
//endregion