package com.wawi.api.extensions

import android.content.Context
import com.kaopiz.kprogresshud.KProgressHUD
import com.wawi.api.CommonModule

/**
 * 显示hud (KProgressHUD)
 * @param msg { hud显示的文字 }
 *
 */
fun Context.hud(msg: String?): KProgressHUD = KProgressHUD
    .create(this)
    .setLabel(msg)
    // 菊花样式
    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
    .setAnimationSpeed(2)
    .setDimAmount(0.5f)
    .show()

/**
 *
 * @in Int.this 输入为dp
 * @out px 值
 *
 * 将dip转成px
 */
val Int.dp: Int
    get() = (this * CommonModule.getContext().resources.displayMetrics.density + 0.5f).toInt()