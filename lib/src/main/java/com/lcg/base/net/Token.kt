package com.lcg.base.net

import com.lcg.base.PreferenceKTX

/**
 * token认证
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2018/9/11 17:57
 */
object Token {
    internal var token: String? = null
    var TOKEN = "Authorization"
    var loginSubscriber: ((showToast: Boolean) -> Unit)? = null
    fun init(name: String = "Authorization", login: (showToast: Boolean) -> Unit) {
        TOKEN = name
        loginSubscriber = login
    }
}

/**
 * 认证的token
 */
fun getToken(): String {
    if (Token.token == null) {
        Token.token = PreferenceKTX.getString(Token.TOKEN, "")
    }
    return Token.token ?: ""
}

/**
 * 存储认证的token
 */
fun saveToken(token: String?) {
    Token.token = token ?: ""
    PreferenceKTX.setString(Token.TOKEN, Token.token!!)
}