package com.tdfanta.game.entity.effect

import android.graphics.Canvas
import android.graphics.Paint
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class GlueEffect(
    origin: Entity,
    position: Vector2,
    private val mIntensity: Float,
    duration: Float,
) : Effect(origin, duration), SpriteTransformation, AreaObserver.Listener {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mAngle = RandomUtils.next(360f)
    private val mAlphaStep = (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * duration)).toInt()
    private val mAreaObserver = AreaObserver(getGameEngine(), position, RANGE, this)

    private val mPaint = Paint()
    private val mSprite: StaticSprite

    init {
        setPosition(position)

        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.BOTTOM, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))

        mPaint.alpha = ALPHA_START
        mSprite.setPaint(mPaint)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.glueEffect, 4)
        s.mSpriteTemplate.setMatrix(1f, 1f, null, null)

        return s
    }

    override fun init() {
        super.init()
        getGameEngine().add(mSprite)
    }

    override fun clean() {
        super.clean()
        mAreaObserver.clean()
        getGameEngine().remove(mSprite)
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun tick() {
        super.tick()
        mPaint.alpha = mPaint.alpha - mAlphaStep
        mAreaObserver.tick()
    }

    override fun enemyEntered(e: Enemy) {
        e.modifySpeed(1f / mIntensity, getOrigin())
    }

    override fun enemyExited(e: Enemy) {
        e.modifySpeed(mIntensity, getOrigin())
    }

    companion object {
        private const val ALPHA_START = 150
        private const val RANGE = 1f
    }
}
