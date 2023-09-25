# QAndroid

一个基本的android框架。

框架实现网络请求、日志输出、SharedPreferences快捷操作、异常处理和toast等。

框架已经集成okhttp和fastjson。

# 关于使用

~~~gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
~~~

~~~gradle
dependencies {
    api 'com.gitee.leicg:base:0.3'
}
~~~

# 核心类

~~~kotlin
Core.init(this)
    .initHttp("https://gitee.com/leicg/base/raw/master") { _, _ ->
        TODO()
    }// 初始化http host和统一的接口异常处理
    .initToken {
        TODO("认证失败请求认证")
    }//网络请求token认证
    .setDebug(true)//debug日志打印
    .setCrashURL(url)// 异常上报地址
// 需要在每个activity Create之前和Destroyed之后调用，防止activity在创建过程中奔溃，导致无限重启。
// 如果从不调用，会让系统处理异常。
Core.activityCreateStart(this)
Core.activityDestroyed(this)
~~~

# 联网调用

~~~kotlin
// 面向对象风格
HttpUrl("完整url|相对host的路径").join(Loading).formBody(map).post<T> { TODO() }
// flow风格
val flow: Flow<T> = Loading?.get<T>("完整url|相对host的路径")
TODO()
// 协程风格
launch(httpThrowableHandler) {
    isLoading = true
    val date: T = com.lcg.base.net.get<T>("完整url|相对host的路径")
    TODO()
    isLoading = false
}
~~~

# 日志

#### 操作类

~~~kotlin
L
~~~

# SharedPreferences操作

#### 操作类

~~~kotlin
PreferenceKTX
~~~

#### 多次put采用Any扩展

~~~kotlin
preferenceEdit {
    putBoolean()
    putString()
}
~~~

# toast

~~~kotlin
// 初始化：toastStyle显示风格
Core.init(this, toastStyle)
// 显示toast：线程安全的
showToast("消息")
~~~