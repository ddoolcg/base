package com.lcg.base.net

import android.os.SystemClock
import kotlinx.coroutines.isActive
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.reflect.Type
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.coroutines.coroutineContext

/**
 * 文件下载处理器
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
class DownloadHandler(
    private val file: File,
    private val progressListener: (suspend (Long, Long) -> Unit)? = null
) : JsonResponseHandler<File>() {
    override suspend fun invoke(response: Response): File {
        val code = response.code
        var body: ResponseBody? = null
        if (code in 200..299 && response.body?.also { body = it } != null) {
            val startsPoint = file.length()
            val contentLength = body!!.contentLength()
            if (code != 200 || startsPoint != contentLength) { //还有未下载的数据
                val `in` = body!!.byteStream()
                var channelOut: FileChannel? = null
                var randomAccessFile: RandomAccessFile? = null
                try {
                    randomAccessFile = RandomAccessFile(file, "rwd")
                    channelOut = randomAccessFile.channel
                    var len: Int = -1
                    var bytesWritten: Long = if (code == 200) 0 else startsPoint
                    val totalLength =
                        if (code == 200) contentLength else startsPoint + contentLength
                    val buffer = ByteBuffer.allocate(10240)
                    val array = buffer.array()
                    var pTime = 0L
                    while (coroutineContext.isActive && `in`.read(array).also { len = it } != -1) {
                        buffer.position(0)
                        buffer.limit(len)
                        channelOut.write(buffer, bytesWritten)
                        bytesWritten += len.toLong()
                        val time = SystemClock.elapsedRealtime()
                        if (time - pTime > 1000) {
                            pTime = time
                            progressListener?.invoke(bytesWritten, totalLength)
                        }
                    }
                } catch (e: IOException) {
                    throw e
                } finally {
                    try {
                        `in`.close()
                        channelOut?.close()
                        randomAccessFile?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return file
        } else if (code == 416) {//已经下载完了
            return file
        } else {
            throw HttpStatusException(code)
        }
    }

    override suspend fun parse(body: String, type: Type): File {
        return file
    }
}