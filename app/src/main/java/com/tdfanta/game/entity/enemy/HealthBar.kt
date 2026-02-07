package com.tdfanta.game.entity.enemy

import android.graphics.Canvas
import android.graphics.Paint
import com.tdfanta.game.R
import com.tdfanta.game.engine.render.Drawable
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.theme.Theme
import com.tdfanta.game.util.math.MathUtils

class HealthBar(theme: Theme, private val mEntity: Enemy) : Drawable {
    init {
        if (mHealthBarBg == null) {
            mHealthBarBg = Paint().also {
                it.color = theme.getColor(R.attr.healthBarBackgroundColor)
            }
        }
        if (mHealthBarFg == null) {
            mHealthBarFg = Paint().also {
                it.color = theme.getColor(R.attr.healthBarColor)
            }
        }
    }

    override fun getLayer(): Int = Layers.ENEMY_HEALTHBAR

    override fun draw(canvas: Canvas) {
        if (!MathUtils.equals(mEntity.getHealth(), mEntity.getMaxHealth(), 1f)) {
            canvas.save()
            canvas.translate(
                mEntity.getPosition().x() - HEALTHBAR_WIDTH / 2f,
                mEntity.getPosition().y() + HEALTHBAR_OFFSET,
            )

            canvas.drawRect(0f, 0f, HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT, mHealthBarBg!!)
            canvas.drawRect(
                0f,
                0f,
                mEntity.getHealth() / mEntity.getMaxHealth() * HEALTHBAR_WIDTH,
                HEALTHBAR_HEIGHT,
                mHealthBarFg!!,
            )
            canvas.restore()
        }
    }

    companion object {
        private const val HEALTHBAR_WIDTH = 1.0f
        private const val HEALTHBAR_HEIGHT = 0.1f
        private const val HEALTHBAR_OFFSET = 0.6f

        private var mHealthBarBg: Paint? = null
        private var mHealthBarFg: Paint? = null
    }
}
