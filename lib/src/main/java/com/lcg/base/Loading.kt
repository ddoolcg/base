package com.lcg.base

import kotlinx.coroutines.Job

/**
 * 耗时IO进度对话框接口
 *
 * @author lei.chuguang Email:475825657@qq.com
 */
interface Loading {
    var job: Job?

    /**对话框提示语*/
    var loadingMessage: String

    /**load中*/
    var isLoading: Boolean

    /** 显示加载进度框 */
    fun show(job: Job? = null) {
        this.job = job
        isLoading = true
    }

    /**关闭加载进度框*/
    fun dismiss() {
        isLoading = false
        job = null
    }

    /**取消协程任务*/
    fun cancelJob() {
        job?.cancel()
        dismiss()
    }
}