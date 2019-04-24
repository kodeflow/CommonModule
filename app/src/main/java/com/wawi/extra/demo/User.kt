package com.wawi.extra.demo

import com.wawi.api.delegate.Preference

object User {
    /** 将登录返回的token 本地化 */
    var token: String by Preference("token", "")
    /** 将用户是否绑定账号的行为 本地化 */
    var bind: Boolean by Preference("bind", false)

    /**
     * 清除token本地化数据，重置登录状态
     */
    fun logout() {
        // 清空缓存token
        token = ""
    }
}