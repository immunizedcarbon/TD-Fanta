package com.tdfanta.game.entity.shot

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.effect.Explosion
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction
import com.tdfanta.game.util.math.Vector2

class MortarShot(
    origin: Entity,
    position: Vector2,
    target: Vector2,
    private val mDamage: Float,
    private val mRadius: Float,
) : Shot(origin), SpriteTransformation {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mAngle = RandomUtils.next(360f)
    private val mHeightScalingFunction: SampledFunction

    private val mSprite: StaticSprite

    init {
        setPosition(position)
        setSpeed(getDistanceTo(target) / TIME_TO_TARGET)
        setDirection(getDirectionTo(target))

        val s = getStaticData() as StaticData

        val x1 = kotlin.math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_START)
        val x2 = kotlin.math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_STOP)
        mHeightScalingFunction = Function.quadratic()
            .multiply(-1f)
            .offset(HEIGHT_SCALING_PEAK)
            .shift(-x1)
            .stretch(GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET / (x1 + x2))
            .sample()

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.grenade, 4)
        s.mSpriteTemplate.setMatrix(0.7f, 0.7f, null, null)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSprite)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSprite)
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        val s = mHeightScalingFunction.getValue()
        SpriteTransformer.translate(canvas, getPosition())
        SpriteTransformer.scale(canvas, s)
        canvas.rotate(mAngle)
    }

    override fun tick() {
        super.tick()

        mHeightScalingFunction.step()
        if (mHeightScalingFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET) {
            getGameEngine().add(Explosion(getOrigin(), getPosition(), mDamage, mRadius))
            remove()
        }
    }

    companion object {
        const val TIME_TO_TARGET = 1.5f
        private const val HEIGHT_SCALING_START = 0.5f
        private const val HEIGHT_SCALING_STOP = 1.0f
        private const val HEIGHT_SCALING_PEAK = 1.5f
    }
}
