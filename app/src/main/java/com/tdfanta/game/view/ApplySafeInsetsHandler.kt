package com.tdfanta.game.view

import android.graphics.Insets
import android.view.View
import android.view.WindowInsets

class ApplySafeInsetsHandler(private val mAdditionalPadding: Int = 0) : View.OnApplyWindowInsetsListener {
    override fun onApplyWindowInsets(view: View, windowInsets: WindowInsets): WindowInsets {
        val systemBars = windowInsets.getInsets(WindowInsets.Type.systemBars())
        val displayCutout = windowInsets.getInsets(WindowInsets.Type.displayCutout())

        val top = maxOf(systemBars.top, displayCutout.top)
        val bottom = maxOf(systemBars.bottom, displayCutout.bottom)
        val left = maxOf(systemBars.left, displayCutout.left)
        val right = maxOf(systemBars.right, displayCutout.right)

        view.setPadding(
            left + mAdditionalPadding,
            top + mAdditionalPadding,
            right + mAdditionalPadding,
            bottom + mAdditionalPadding,
        )

        return windowInsets
    }
}
