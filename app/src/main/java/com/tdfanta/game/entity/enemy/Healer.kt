package com.tdfanta.game.entity.enemy

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.loop.TickListener
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.ReplicatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.entity.effect.HealEffect
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction

class Healer private constructor(gameEngine: GameEngine) : Enemy(gameEngine, ENEMY_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Healer(gameEngine)
    }

    class Persister : EnemyPersister()

    private class StaticData : TickListener {
        var mHealing = false
        var mDropEffect = false
        var mAngle = 0f
        var mScale = 1f
        lateinit var mHealTimer: TickTimer
        lateinit var mHealedEnemies: MutableCollection<Enemy>
        lateinit var mScaleFunction: SampledFunction
        lateinit var mRotateFunction: SampledFunction

        lateinit var mSpriteTemplate: SpriteTemplate
        lateinit var mReferenceSprite: AnimatedSprite

        override fun tick() {
            mReferenceSprite.tick()

            if (mHealTimer.tick()) {
                mHealing = true
            }

            if (mHealing) {
                mRotateFunction.step()
                mScaleFunction.step()

                mAngle += mRotateFunction.getValue()
                mScale = mScaleFunction.getValue()

                if (mScaleFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * HEAL_DURATION) {
                    mHealedEnemies.clear()
                    mDropEffect = true
                    mHealing = false
                    mAngle = 0f
                    mScale = 1f

                    mRotateFunction.reset()
                    mScaleFunction.reset()
                }
            } else {
                mDropEffect = false
            }
        }
    }

    private val mStaticData: StaticData = getStaticData() as StaticData

    private val mSprite: ReplicatedSprite

    init {
        mSprite = getSpriteFactory().createReplication(mStaticData.mReferenceSprite)
        mSprite.setListener(this)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun getTextId(): Int = R.string.enemy_healer

    override fun drawPreview(canvas: Canvas) {
        val s = getStaticData() as StaticData
        getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate).draw(canvas)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mHealTimer = TickTimer.createInterval(HEAL_INTERVAL)
        s.mHealedEnemies = HashSet()

        s.mScaleFunction = Function.sine()
            .join(Function.constant(0f), Math.PI.toFloat())
            .multiply(HEAL_SCALE_FACTOR - 1f)
            .offset(1f)
            .stretch(GameEngine.TARGET_FRAME_RATE * HEAL_DURATION * 0.66f / Math.PI.toFloat())
            .invert()
            .sample()

        s.mRotateFunction = Function.constant(0f)
            .join(Function.sine(), Math.PI.toFloat() / 2f)
            .multiply(HEAL_ROTATION / GameEngine.TARGET_FRAME_RATE * 360f)
            .stretch(GameEngine.TARGET_FRAME_RATE * HEAL_DURATION * 0.66f / Math.PI.toFloat())
            .sample()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.healer, 4)
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, null)

        s.mReferenceSprite = getSpriteFactory().createAnimated(Layers.ENEMY, s.mSpriteTemplate)
        s.mReferenceSprite.setSequenceForward()
        s.mReferenceSprite.setFrequency(ANIMATION_SPEED)

        getGameEngine().add(s)

        return s
    }

    override fun getSpeed(): Float {
        if (mStaticData.mHealing) {
            return 0f
        }
        return super.getSpeed()
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSprite)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSprite)
    }

    override fun tick() {
        super.tick()

        if (mStaticData.mDropEffect) {
            getGameEngine().add(
                HealEffect(
                    this,
                    getPosition(),
                    HEAL_AMOUNT,
                    HEAL_RADIUS,
                    mStaticData.mHealedEnemies,
                ),
            )
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mStaticData.mAngle)
        SpriteTransformer.scale(canvas, mStaticData.mScale)
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "healer"

        private const val ANIMATION_SPEED = 1.5f
        private const val HEAL_SCALE_FACTOR = 2f
        private const val HEAL_ROTATION = 2.5f

        private const val HEAL_AMOUNT = 0.1f
        private const val HEAL_INTERVAL = 5.0f
        private const val HEAL_DURATION = 1.5f
        private const val HEAL_RADIUS = 0.7f

        private val ENEMY_PROPERTIES = EnemyProperties.Builder()
            .setHealth(400)
            .setSpeed(1.2f)
            .setReward(30)
            .setWeakAgainst(WeaponType.Laser, WeaponType.Bullet)
            .build()
    }
}
