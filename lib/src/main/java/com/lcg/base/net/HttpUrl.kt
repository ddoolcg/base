package com.lcg.base.net

import com.lcg.base.Loading
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * HTTP面向对象请求方式
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 3.0
 * @since 2019/12/26 10:28
 */
open class HttpUrl(val path: String) {
    var formMap: HashMap<String, String?>? = null
        private set
    var body: String? = null
        private set
    var loading: Loading? = null
        private set
    var msg: String? = null
        private set
    var cancelEnable = true
        private set
    var fail: ((Int, Throwable) -> Boolean)? = null
        private set
    var style = Http.defaultStyle
        private set

    /**异常处理器*/
    val exceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.httpHandle(fail) }

    /**html表单方式请求*/
    fun formBody(formMap: HashMap<String, String?>?): HttpUrl {
        this.body = null
        this.formMap = formMap
        return this
    }

    /**html表单方式请求*/
    fun formBody(vararg formMap: Pair<String, String?>): HttpUrl {
        this.body = null
        this.formMap = formMap.toMap(hashMapOf())
        return this
    }

    /**application/json 方式请求*/
    fun jsonBody(json: String?): HttpUrl {
        this.body = json
        this.formMap = null
        return this
    }

    /**接入进度对话框，用户关闭对话框会中断请求*/
    fun join(
        loading: Loading?,
        msg: String = "加载中...",
        cancelEnable: Boolean = true
    ): HttpUrl {
        this.loading = loading
        this.msg = msg
        this.cancelEnable = cancelEnable
        return this
    }

    /**application/json 方式请求*/
    fun style(style: Http.Style): HttpUrl {
        this.style = style
        return this
    }

    /**捕获服务器的失败
     * @param success success返回值表示是否中断。<br/>
     * 生命周期为failDefault->success->框架默认实现
     */
    fun catchFail(success: ((Int, Throwable) -> Boolean)?): HttpUrl {
        fail = success
        return this
    }

    inline fun <reified T> get(
        scope: CoroutineScope = MainScope(),
        crossinline success: (data: T) -> Unit
    ) = launch(scope, success) {
        get(path, formMap, style)
    }


    inline fun <reified T> post(
        scope: CoroutineScope = MainScope(),
        crossinline success: (data: T) -> Unit
    ) = launch(scope, success) {
        if (body == null) {
            post(path, formMap, style)
        } else {
            post(path, body!!, style)
        }
    }

    inline fun <reified T> delete(
        scope: CoroutineScope = MainScope(),
        crossinline success: (data: T) -> Unit
    ) = launch(scope, success) {
        delete(path, formMap, style)
    }

    inline fun <reified T> put(
        scope: CoroutineScope = MainScope(),
        crossinline success: (data: T) -> Unit
    ) = launch(scope, success) {
        if (body == null) {
            put(path, formMap, style)
        } else {
            put(path, body!!, style)
        }
    }

    /**通过协程执行请求*/
    inline fun <reified T> launch(
        scope: CoroutineScope,
        crossinline success: (data: T) -> Unit,
        crossinline request: suspend () -> T,
    ) = scope.launch(exceptionHandler) {
        loading?.loadingMessage = msg ?: "加载中..."
        val job = if (cancelEnable) coroutineContext[Job] else null
        loading?.show(job)
        success(request())
        loading?.dismiss()
    }
}
