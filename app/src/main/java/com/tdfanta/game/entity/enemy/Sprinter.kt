package com.tdfanta.game.entity.enemy

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.loop.TickListener
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.ReplicatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction

class Sprinter private constructor(gameEngine: GameEngine) : Enemy(gameEngine, ENEMY_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Sprinter(gameEngine)
    }

    class Persister : EnemyPersister()

    private class StaticData : TickListener {
        lateinit var mSpeedFunction: SampledFunction

        lateinit var mSpriteTemplate: SpriteTemplate
        lateinit var mReferenceSprite: AnimatedSprite

        override fun tick() {
            mReferenceSprite.tick()
            mSpeedFunction.step()
        }
    }

    private var mAngle = 0f
    private val mStatic: StaticData = getStaticData() as StaticData
    private val mSprite: ReplicatedSprite

    init {
        mSprite = getSpriteFactory().createReplication(mStatic.mReferenceSprite)
        mSprite.setListener(this)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun getTextId(): Int = R.string.enemy_sprinter

    override fun drawPreview(canvas: Canvas) {
        val s = getStaticData() as StaticData

        val sprite: StaticSprite = getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate)
        sprite.setIndex(3)
        sprite.draw(canvas)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpeedFunction = Function.sine()
            .multiply(0.9f)
            .offset(0.1f)
            .repeat(Math.PI.toFloat())
            .stretch(GameEngine.TARGET_FRAME_RATE / ANIMATION_SPEED / Math.PI.toFloat())
            .sample()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.sprinter, 6)
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, null)

        s.mReferenceSprite = getSpriteFactory().createAnimated(Layers.ENEMY, s.mSpriteTemplate)
        s.mReferenceSprite.setSequenceForwardBackward()
        s.mReferenceSprite.setFrequency(ANIMATION_SPEED)

        getGameEngine().add(s)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSprite)
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSprite)
    }

    override fun tick() {
        super.tick()

        if (hasWayPoint()) {
            mAngle = getDirection()!!.angle()
        }
    }

    override fun getSpeed(): Float = super.getSpeed() * mStatic.mSpeedFunction.getValue()

    companion object {
        @JvmField
        val ENTITY_NAME = "sprinter"

        private const val ANIMATION_SPEED = 0.7f

        private val ENEMY_PROPERTIES = EnemyProperties.Builder()
            .setHealth(200)
            .setSpeed(3.0f)
            .setReward(15)
            .setWeakAgainst(WeaponType.Explosive)
            .setStrongAgainst(WeaponType.Laser)
            .build()
    }
}
