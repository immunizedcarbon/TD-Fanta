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

class Flyer private constructor(gameEngine: GameEngine) : Enemy(gameEngine, ENEMY_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Flyer(gameEngine)
    }

    class Persister : EnemyPersister()

    private class StaticData : TickListener {
        lateinit var mSpriteTemplate: SpriteTemplate
        lateinit var mReferenceSprite: AnimatedSprite

        override fun tick() {
            mReferenceSprite.tick()
        }
    }

    private var mAngle = 0f

    private val mSprite: ReplicatedSprite

    init {
        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createReplication(s.mReferenceSprite)
        mSprite.setListener(this)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun getTextId(): Int = R.string.enemy_flyer

    override fun drawPreview(canvas: Canvas) {
        val s = getStaticData() as StaticData

        val sprite: StaticSprite = getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate)
        sprite.setIndex(3)
        sprite.draw(canvas)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.flyer, 6)
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, -90f)

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

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "flyer"

        private const val ANIMATION_SPEED = 1.0f

        private val ENEMY_PROPERTIES = EnemyProperties.Builder()
            .setHealth(400)
            .setSpeed(1.3f)
            .setReward(30)
            .setWeakAgainst(WeaponType.Laser, WeaponType.Bullet)
            .setStrongAgainst(WeaponType.Glue)
            .build()
    }
}
