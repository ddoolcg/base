package com.lcg.base.net

import android.webkit.MimeTypeMap
import com.lcg.base.Core
import com.lcg.base.MD5
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.coroutineContext

/**
 * 文件上传下载
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
/**文件上传*/
suspend inline fun <reified T> upload(
    path: String,
    paramsMap: HashMap<String, Any?>? = null,
    noinline fail: ((Int, Throwable) -> Boolean)? = null
): T? = request(path, object : JsonResponseHandler<T>() {}, fail) {
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

/**文件下载*/
suspend fun download(
    path: String,
    file: File = File(Core.context!!.cacheDir, MD5.GetMD5Code(path)),
    progress: ((Long, Long) -> Unit)? = null,
    fail: ((Int, Throwable, File) -> Boolean)? = null
): File? {
    val handler = if (progress == null) {
        DownloadHandler(file)
    } else {
        val context = coroutineContext
        DownloadHandler(file) { loadSize, total ->
            withContext(context) {
                progress(loadSize, total)
            }
        }
    }
    val f = if (fail == null) {
        null
    } else {
        fun(code: Int, e: Throwable) = fail(code, e, file)
    }
    return request(path, handler, f) {
        it.addHeader("RANGE", "bytes=" + file.length() + "-")
        it.addHeader("Connection", "keep-alive")
        it.get()
    }
}