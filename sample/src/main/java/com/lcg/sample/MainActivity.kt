package com.lcg.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.lcg.base.Core
import com.lcg.base.L
import com.lcg.base.Loading
import com.lcg.base.net.HttpUrl
import com.lcg.base.net.flow.download
import com.lcg.base.net.httpHandle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : Activity(), Loading, CoroutineScope by MainScope() {
    private val textView by lazy { TextView(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        Core.activityCreateStart(this)
        super.onCreate(savedInstanceState)
        setContentView(textView.apply { text = "hi!" })
        launch {
            download("https://updatecdn.meeting.qq.com/cos/87439695193afbf8b1faa23202ce7306/TencentMeeting_0300000000_3.15.1.402_arm64_default.publish.deb").onEach {
                if (it is Pair<*, *>) {
                    textView.text = "下载进度：${it.first}/${it.second}"
                }
            }.filterIsInstance<File>().collect {
                textView.text = "下载成功，存储路径：${it.path}"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        HttpUrl("/books.json").get<ArrayList<HashMap<String, String>>>(this) {
            L.i(it.joinToString { it.toString() })
        }
        /*val handler =
            CoroutineExceptionHandler { _, throwable -> throwable.httpHandle() }
        launch(handler) {
            isLoading = true
            val list = com.lcg.base.net.get<ArrayList<HashMap<String, String>>>("/books.json")
            L.i(list.joinToString { it.toString() })
            isLoading = false
        }*/
    }

    override fun onDestroy() {
        cancel(null)
        super.onDestroy()
        Core.activityDestroyed(this)
    }

    override var job: Job? = null
    override var loadingMessage: String = "加载中..."
    override var isLoading: Boolean = false
        set(value) {
            field = value
            textView.text = if (value) loadingMessage else "加载完成"
        }
}