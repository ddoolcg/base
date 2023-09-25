package com.lcg.base

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import com.hjq.toast.style.BlackToastStyle

/**
 * MyToastStyle
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 1.0
 * @since 2019/3/6 20:30
 */
object MyToastStyle : BlackToastStyle() {
    override fun getTextColor(context: Context): Int {
        return -1
    }

    override fun getTextSize(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            14f,
            context.resources.displayMetrics
        )
    }

    override fun getHorizontalPadding(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getVerticalPadding(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5f,
            context.resources.displayMetrics
        ).toInt()
    }

    override fun getBackgroundDrawable(context: Context): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(-0x585859)
        drawable.cornerRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5f,
            context.resources.displayMetrics
        )
        return drawable
    }
}