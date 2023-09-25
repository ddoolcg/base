package com.lcg.base.net.flow

import android.webkit.MimeTypeMap
import com.lcg.base.Core
import com.lcg.base.Loading
import com.lcg.base.MD5
import com.lcg.base.net.DownloadHandler
import com.lcg.base.net.JsonResponseHandler
import com.lcg.base.net.httpHandle
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 文件上传下载
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
/**文件上传*/
inline fun <reified T> Loading?.upload(
    path: String,
    paramsMap: HashMap<String, Any?>? = null,
    noinline fail: ((Int, Throwable) -> Boolean)?
): Flow<T> = request(path, object : JsonResponseHandler<T>() {}, fail) {
    val builder = MultipartBody.Builder()
    //设置类型
    builder.setType(MultipartBody.FORM)
    //追加参数
    paramsMap?.forEach { (t, u) ->
        if (u != null) {
            if (u is File) {
                val fileName = u.name
                var contentType: MediaType? = null
                val i = fileName.lastIndexOf(".") + 1
                if (i in 1 until fileName.length) {
                    val substring = fileName.substring(i)
                    val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(substring)
                    contentType = type?.toMediaTypeOrNull()
                }
                builder.addFormDataPart(t, fileName, u.asRequestBody(contentType))
            } else {
                builder.addFormDataPart(t, u.toString())
            }
        }
    }
    it.post(builder.build())
}

/**文件下载
 * ~~~
 *download(url).onEach {
 *  if (it is Pair<*, *>) {
 *      L.i("下载进度：${it.first}/${it.second}")
 *  }
 *}.filterIsInstance<File>().collect {
 *  L.i("下载成功，存储路径：${it.path}")
 *}
 * ~~~
 */
fun Loading?.download(
    path: String,
    file: File = File(Core.context!!.cacheDir, MD5.GetMD5Code(path)),
    fail: ((Int, Throwable) -> Boolean)? = null,
): Flow<Any> {
    val context = CoroutineName("NET[${path.hashCode()}]") + Dispatchers.IO
    return callbackFlow {
        val handler = DownloadHandler(file) { loadSize, total ->
            trySend(loadSize to total)
        }
        this@download?.show(coroutineContext[Job])
        com.lcg.base.net.request(path, handler) {
            it.addHeader("RANGE", "bytes=" + file.length() + "-")
            it.addHeader("Connection", "keep-alive")
            it.get()
        }
        trySend(file)
        cancel()
        this@download?.dismiss()
        awaitClose { }
    }.flowOn(context)
        .catch { it.httpHandle(fail) }
        .onStart { this@download?.show(context[Job]) }
        .onCompletion { this@download?.dismiss() }
}