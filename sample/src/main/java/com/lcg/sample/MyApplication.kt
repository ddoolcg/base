package com.lcg.sample

import android.app.Application
import com.lcg.base.Core

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Core.init(this)
            .initHttp("https://gitee.com/leicg/base/raw/master/sample") { _, _ ->
                false
            }
            .setDebug(true)
            .setCrashURL("https://yourhost.com/error")
    }
}