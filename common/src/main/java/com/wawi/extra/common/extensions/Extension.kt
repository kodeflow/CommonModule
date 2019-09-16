package com.wawi.api.extensions

import android.content.Context
import com.kaopiz.kprogresshud.KProgressHUD
import com.wawi.api.CommonModule
import okhttp3.MediaType
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 * 显示hud (KProgressHUD)
 * @param msg { hud显示的文字 }
 *
 */
fun Context.hud(msg: String? = null): KProgressHUD = KProgressHUD
    .create(this)
    .setLabel(msg)
    // 菊花样式
    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
    .setAnimationSpeed(2)
    .setDimAmount(0.5f)
    .show()

/**
 * 判断是否是闰年
 * @param this 当前年
 */
fun Int.isLeapYear(): Boolean {
    if ((this % 4 == 0 && this % 100 != 0) || this % 400 == 0) {
        return true
    }
    return false
}

/**
 * 判断今年是否是闰年
 * @param this 当前年
 */
fun Date.isLeapYear(): Boolean {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
        return true
    }
    return false
}

/**
 * 获取英文的星期
 */
fun Date.weekNameEN(): String {
    val names = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    val calendar = Calendar.getInstance()
    calendar.time = this
    var i = calendar.get(Calendar.DAY_OF_WEEK) - 1
    if (i < 0) i = 0
    return  names[i]
}

/**
 * 获取中文的星期
 */
fun Date.weekNameCN(): String {
    val names = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
    val calendar = Calendar.getInstance()
    calendar.time = this
    var i = calendar.get(Calendar.DAY_OF_WEEK) - 1
    if (i < 0) i = 0
    return  names[i]
}

/**
 *
 * @in Int.this 输入为dp
 * @out px 值
 *
 * 将dip转成px
 */
val Int.dp: Int
    get() = (this * CommonModule.getContext().resources.displayMetrics.density + 0.5f).toInt()

/**
 *
 * @in Int.this 输入单位：分
 * @out Float 输出单位：元
 *
 * 将dip转成px
 */
val Int.money: Float
    get() = (this.toFloat() / 100)

fun String.getScheme(): String {
    if (this.isEmpty() || this.indexOf("://") < 0) throw Exception("Not a valid Url.")
    return this.split("://")[0]
}
fun String.getHost(): String {
    if (this.isEmpty() || this.indexOf("://") < 0) throw Exception("Not a valid Url.")
    val segments = this.split("://")[1]
    val first = segments.indexOf("/")
    return segments.substring(0, first)
}

/**
 * 获取带http:// 等前缀的完整Host地址
 */
fun String.getBaseUrl(): String {
    if (this.isEmpty() || this.indexOf("://") < 0) throw Exception("Not a valid Url.")
    return "${this.getScheme()}://${this.getHost()}"
}

fun MediaType.sufix(): String {
    val subtype = this.subtype()
    val regex = "[.|-|+]+".toRegex()
    if (regex.containsMatchIn(subtype)) {
        return when (subtype) {
            "vnd.android.package-archive" -> ".apk"
            else -> ""
        }
    }
    return ".$subtype"
}

fun File.digest(): String? {
    return  digest(16)
}

fun File.digest(radix: Int): String? {
    if (!this.isFile) return null
    if (!this.exists()) return null
    val d = MessageDigest.getInstance("MD5")
    val fi = FileInputStream(this)
    var len = 0
    val buffer = ByteArray(1024 *4)
    while ((fi.read(buffer, 0, buffer.size).also { len = it }) != -1) {
        d.update(buffer, 0, len)
    }
    fi.close()
    val bigInt = BigInteger(1, d.digest())

    return  bigInt.toString(radix)
}