package com.tdfanta.game.entity.shot

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.effect.Explosion
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class Rocket(
    origin: Entity,
    position: Vector2,
    private val mDamage: Float,
    private val mRadius: Float,
) : Shot(origin), SpriteTransformation, TargetTracker.Listener {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
        lateinit var mSpriteTemplateFire: SpriteTemplate
    }

    private var mAngle = 0f
    private val mTracker = TargetTracker(this, this)

    private val mSprite: StaticSprite
    private val mSpriteFire: AnimatedSprite

    init {
        setPosition(position)
        setSpeed(MOVEMENT_SPEED)
        setEnabled(false)

        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))

        mSpriteFire = getSpriteFactory().createAnimated(Layers.SHOT, s.mSpriteTemplateFire)
        mSpriteFire.setListener(this)
        mSpriteFire.setSequenceForward()
        mSpriteFire.setFrequency(ANIMATION_SPEED)
    }

    fun setAngle(angle: Float) {
        mAngle = angle
    }

    fun setTarget(target: Enemy) {
        mTracker.setTarget(target)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.rocket, 4)
        s.mSpriteTemplate.setMatrix(0.8f, 1f, null, -90f)

        s.mSpriteTemplateFire = getSpriteFactory().createTemplate(R.attr.rocketFire, 4)
        s.mSpriteTemplateFire.setMatrix(0.3f, 0.3f, Vector2(0.15f, 0.6f), -90f)

        return s
    }

    override fun init() {
        super.init()
        getGameEngine().add(mSprite)

        if (isEnabled()) {
            getGameEngine().add(mSpriteFire)
        }
    }

    override fun clean() {
        super.clean()
        getGameEngine().remove(mSprite)

        if (isEnabled()) {
            getGameEngine().remove(mSpriteFire)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (!isEnabled()) {
            getGameEngine().remove(mSpriteFire)
        }

        if (isEnabled()) {
            getGameEngine().add(mSpriteFire)
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun tick() {
        if (isEnabled()) {
            val direction = mTracker.getTargetDirection()
            setDirection(direction)
            mAngle = direction.angle()

            mSpriteFire.tick()
        }

        super.tick()
        mTracker.tick()
    }

    override fun targetLost(target: Enemy) {
        val closest = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
            .min(distanceTo(getPosition())) as Enemy?

        if (closest == null) {
            remove()
        } else {
            mTracker.setTarget(closest)
        }
    }

    override fun targetReached(target: Enemy) {
        getGameEngine().add(Explosion(getOrigin(), target.getPosition(), mDamage, mRadius))
        remove()
    }

    companion object {
        private const val MOVEMENT_SPEED = 2.5f
        private const val ANIMATION_SPEED = 3f
    }
}
