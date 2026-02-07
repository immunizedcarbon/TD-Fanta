package com.tdfanta.game.util

import android.content.res.Resources
import com.tdfanta.game.R
import java.text.DecimalFormat

object StringUtils {
    private val fmt0 = DecimalFormat("0")
    private val fmt1 = DecimalFormat("0.0")

    @JvmStatic
    fun formatSuffix(value: Int): String = formatSuffix(value.toFloat(), true)

    @JvmStatic
    fun formatSuffix(value: Float): String = formatSuffix(value, false)

    @JvmStatic
    fun formatSuffix(value: Float, integer: Boolean): String {
        var currentValue = value
        var suffix = ""
        var big = false

        if (currentValue >= 1e10f) {
            suffix = "G"
            currentValue /= 1e9f
            big = true
        } else if (currentValue >= 1e7f) {
            suffix = "M"
            currentValue /= 1e6f
            big = true
        } else if (currentValue >= 1e4f) {
            suffix = "k"
            currentValue /= 1e3f
            big = true
        }

        val formatter = if (currentValue < 1e2f && (!integer || big)) fmt1 else fmt0
        return formatter.format(currentValue) + suffix
    }

    @JvmStatic
    fun formatBoolean(value: Boolean, resources: Resources): String =
        resources.getString(if (value) R.string.on else R.string.off)

    @JvmStatic
    fun formatSwitchButton(name: String, value: String): String =
        String.format("%1\$s (%2\$s)", name, value)

    @JvmStatic
    fun isNullOrEmpty(string: String?): Boolean = string == null || string.isEmpty()
}
