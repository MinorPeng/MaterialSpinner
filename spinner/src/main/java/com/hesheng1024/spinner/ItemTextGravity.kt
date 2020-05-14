package com.hesheng1024.spinner

import android.view.Gravity

/**
 *
 * @author hesheng1024
 * @date 2020/5/14 17:38
 */
internal enum class ItemTextGravity(id: Int) {
    START(0),
    CENTER(1),
    END(2);

    companion object {
        fun formId(id: Int): Int = when (id) {
            START.ordinal -> Gravity.START
            CENTER.ordinal -> Gravity.CENTER
            END.ordinal -> Gravity.END
            else -> Gravity.CENTER
        }
    }
}