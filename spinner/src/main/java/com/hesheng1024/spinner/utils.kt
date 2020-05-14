package com.hesheng1024.spinner

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import kotlin.math.max

/**
 *
 * @author hesheng1024
 * @date 2020/5/11 17:13
 */

fun darker(color: Int, factor: Float): Int {
    val red = max((Color.red(color) * factor).toInt(), 0)
    val green = max((Color.red(color) * factor).toInt(), 0)
    val blue = max((Color.blue(color) * factor).toInt(), 0)
    return Color.argb(Color.alpha(color), red, green, blue)
}

fun lighter(color: Int, factor: Float): Int {
    val red = ((Color.red(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val green = ((Color.green(color) * (1 - factor) / 255 + factor) * 255).toInt()
    val blue = ((Color.blue(color) * (1 - factor) / 255 + factor) * 255).toInt()
    return Color.argb(Color.alpha(color), red, green, blue)
}

fun isRtl(context: Context) = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
        && context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
