package com.wawi.extra.demo

data class ParamGetCity(val parentId: Int?)
/**
 * @param captcha 验证码
 */
data class ParamLogin(
    val userName: String,
    val password: String,
    val captcha: String? = null,
    val loginType: String = "password"
)

data class ParamSmsCode(
    val phone: String,
    /**
     * 默认短信类型为：注册
     * R：注册短信
     * L：登录短信
     * Z：找回支付密码短信
     * F：找回密码短信
     */
    val smsType: String = "R"
)

data class ParamRegister(
    val code: String,
    val inviteCode: String,
    val merchantName: String,
    /** 需要用AES加密 */
    val password: String,
    val phone: String,
    val qq: String = ""
)