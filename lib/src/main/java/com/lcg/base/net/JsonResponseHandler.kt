package com.lcg.base.net

import com.alibaba.fastjson.JSON
import com.lcg.base.L
import com.lcg.base.emptyTo
import kotlinx.coroutines.CoroutineName
import java.io.Serializable
import java.lang.reflect.Type
import java.util.Locale
import kotlin.coroutines.coroutineContext

/**
 * json对象处理器
 * @param style true=原始对象返回，false=处理成[StatusBody]后返回data
 * @author lei.chuguang Email:475825657@qq.com
 */
@Suppress("UNCHECKED_CAST")
abstract class JsonResponseHandler<T>(private val style: Http.Style = Http.defaultStyle) :
    ResponseHandler<T>() {
    @Throws(Exception::class)
    override suspend fun parse(body: String, type: Type): T {
        L.i(coroutineContext[CoroutineName]?.name ?: "NET", body)
        return if (style == Http.Style.JSON_WHOLE) full(body, type) else status(body, type)
    }

    @Throws(Exception::class)
    private fun full(body: String, type: Type) = if (type == Unit::class.java) {
        Unit as T
    } else {
        if (type == String::class.java) {
            body as T
        } else {
            JSON.parseObject(body, type, *arrayOfNulls(0)) as T
        }
    }

    @Throws(Exception::class)
    private fun status(body: String, type: Type): T {
        val statusBody = JSON.parseObject(body, StatusBody::class.java)
        val data = statusBody.data
        val code = statusBody.code
        if (StatusBody.SUCCESS_CODE == null || StatusBody.SUCCESS_CODE == code) {
            return when {
                type == String::class.java -> {
                    if (data is JSON) {
                        data.toJSONString() as T
                    } else {
                        data.toString() as T
                    }
                }

                type == Unit::class.java -> {
                    Unit as T
                }

                data == null -> {
                    throw HttpStatusException(code ?: 0, "服务器返回空数据")
                }

                type == data.javaClass -> { //相同的类型
                    data as T
                }

                data is JSON -> { //这样做是为了适配基础类型
                    return data.toJavaObject(type)
                }

                data.javaClass.simpleName.lowercase(Locale.getDefault())
                    .startsWith(type.toString()) -> { //基本数据类型及其包装类
                    data as T
                }

                else -> { //返回的是基础数据类型，解析要求不匹配
                    throw HttpStatusException(
                        code ?: 0,
                        data.javaClass.toString() + " 解析不到 " + type
                    )
                }
            }
        } else if (code == null) {
            throw HttpStatusException(0, "服务器数据异常")
        } else {
            throw HttpStatusException(code, statusBody.message emptyTo "服务器繁忙")
        }
    }
}

/**
 * 带状态的响应体
 */
class StatusBody<T> : Serializable {
    var message: String? = null
        get() = field ?: msg
    var code: Int? = null
        get() = field ?: status
    var msg: String? = null
    var status: Int? = null
    var data: T? = null

    companion object {
        /**成功状态码定义，NULL表示不关心状态码，只在意data数据是否匹配*/
        var SUCCESS_CODE: Int? = 1
    }
}