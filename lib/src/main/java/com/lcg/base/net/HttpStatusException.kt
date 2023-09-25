package com.lcg.base.net

/**
 * http状态异常
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
class HttpStatusException(val code: Int, val info: String? = "服务器繁忙") :
    Exception("$info code=$code")