package com.tdfanta.game.entity.shot

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.effect.Explosion
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.Flyer
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction
import com.tdfanta.game.util.math.Vector2

class Mine : Shot, SpriteTransformation {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mDamage: Float
    private val mRadius: Float
    private var mAngle = 0f
    private var mFlying: Boolean
    private var mRotationStep = 0f
    private val mHeightScalingFunction: SampledFunction

    private lateinit var mSpriteFlying: StaticSprite
    private lateinit var mSpriteMine: StaticSprite

    private val mUpdateTimer = TickTimer.createInterval(0.1f)

    constructor(origin: Entity, position: Vector2, target: Vector2, damage: Float, radius: Float) : super(origin) {
        setPosition(position)
        setSpeed(getDistanceTo(target) / TIME_TO_TARGET)
        setDirection(getDirectionTo(target))

        mFlying = true
        mDamage = damage
        mRadius = radius

        mRotationStep = RandomUtils.next(ROTATION_RATE_MIN, ROTATION_RATE_MAX) * 360f / GameEngine.TARGET_FRAME_RATE

        val x1 = kotlin.math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_START)
        val x2 = kotlin.math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_STOP)
        mHeightScalingFunction = Function.quadratic()
            .multiply(-1f)
            .offset(HEIGHT_SCALING_PEAK)
            .shift(-x1)
            .stretch(GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET / (x1 + x2))
            .sample()

        createAssets()
    }

    constructor(origin: Entity, position: Vector2, damage: Float, radius: Float) : super(origin) {
        setPosition(position)
        setDirection(Vector2())

        mFlying = false
        mDamage = damage
        mRadius = radius
        mAngle = RandomUtils.next(0f, 360f)

        mHeightScalingFunction = Function.constant(HEIGHT_SCALING_STOP).sample()

        createAssets()
    }

    private fun createAssets() {
        val s = getStaticData() as StaticData

        val index = RandomUtils.next(4)

        mSpriteFlying = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate)
        mSpriteFlying.setListener(this)
        mSpriteFlying.setIndex(index)

        mSpriteMine = getSpriteFactory().createStatic(Layers.BOTTOM, s.mSpriteTemplate)
        mSpriteMine.setListener(this)
        mSpriteMine.setIndex(index)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.mine, 4)
        s.mSpriteTemplate.setMatrix(0.7f, 0.7f, null, null)

        return s
    }

    override fun init() {
        super.init()

        if (mFlying) {
            getGameEngine().add(mSpriteFlying)
        } else {
            getGameEngine().add(mSpriteMine)
        }
    }

    override fun clean() {
        super.clean()

        if (mFlying) {
            getGameEngine().remove(mSpriteFlying)
        } else {
            getGameEngine().remove(mSpriteMine)
        }
    }

    override fun tick() {
        super.tick()

        if (mFlying) {
            mAngle += mRotationStep
            mHeightScalingFunction.step()

            if (mHeightScalingFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET) {
                getGameEngine().remove(mSpriteFlying)
                getGameEngine().add(mSpriteMine)

                mFlying = false
                setSpeed(0f)
            }
        } else if (mUpdateTimer.tick()) {
            val enemiesInRange = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(inRange(getPosition(), TRIGGER_RADIUS))
                .cast(Enemy::class.java)
                .filter { value -> value !is Flyer }

            if (!enemiesInRange.isEmpty()) {
                getGameEngine().add(Explosion(getOrigin(), getPosition(), mDamage, mRadius))
                remove()
            }
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        val s = mHeightScalingFunction.getValue()
        SpriteTransformer.translate(canvas, getPosition())
        SpriteTransformer.scale(canvas, s)
        canvas.rotate(mAngle)
    }

    fun isFlying(): Boolean = mFlying

    companion object {
        private const val TRIGGER_RADIUS = 0.7f

        private const val TIME_TO_TARGET = 1.5f
        private const val ROTATION_RATE_MIN = 0.5f
        private const val ROTATION_RATE_MAX = 2.0f
        private const val HEIGHT_SCALING_START = 0.5f
        private const val HEIGHT_SCALING_STOP = 1.0f
        private const val HEIGHT_SCALING_PEAK = 1.5f
    }
}
