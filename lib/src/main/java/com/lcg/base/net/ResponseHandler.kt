package com.lcg.base.net

import okhttp3.Response
import okhttp3.ResponseBody
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * http响应处理器，解决泛型套泛型的问题
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
abstract class ResponseHandler<T> {
    /**数据类型*/
    protected fun getType(): Type {
        val clazz = this.javaClass
        val genericSuperclass = clazz.genericSuperclass
        val type = genericSuperclass as? ParameterizedType
        val arguments = type?.actualTypeArguments
        return arguments?.get(0) ?: String::class.java
    }

    /**处理Response*/
    @Throws(Exception::class)
    internal open suspend operator fun invoke(response: Response): T {
        val code = response.code
        var body: ResponseBody? = null
        if (code in 200..299 && response.body?.also { body = it } != null) {
            val string = body!!.string()
            body.close()
            return parse(string, getType())
        } else {
            throw HttpStatusException(code)
        }
    }

    /**处理器数据解析*/
    @Throws(Exception::class)
    protected abstract suspend fun parse(body: String, type: Type): T
}