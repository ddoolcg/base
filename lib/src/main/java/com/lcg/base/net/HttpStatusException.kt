package com.lcg.base.net

/**
 * http状态异常
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
class HttpStatusException(val code: Int, message: String? = "服务器繁忙") : Throwable(message) {
    override val message: String
        get() = "${super.message} code=$code"
}