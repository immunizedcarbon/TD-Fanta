package com.tdfanta.game.entity.shot

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.effect.GlueEffect
import com.tdfanta.game.util.math.Vector2

class GlueShot(
    origin: Entity,
    position: Vector2,
    private val mTarget: Vector2,
    private val mIntensity: Float,
    private val mDuration: Float,
) : Shot(origin), SpriteTransformation {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mSprite: AnimatedSprite

    private val mSound: Sound = getSoundFactory().createSound(R.raw.gas1_pff)

    init {
        setPosition(position)

        setSpeed(MOVEMENT_SPEED)
        setDirection(getDirectionTo(mTarget))

        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createAnimated(Layers.SHOT, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setSequenceForward()
        mSprite.setFrequency(ANIMATION_SPEED)
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.glueShot, 6)
        s.mSpriteTemplate.setMatrix(0.33f, 0.33f, null, null)

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

        mSprite.tick()

        if (getDistanceTo(mTarget) < getSpeed() / GameEngine.TARGET_FRAME_RATE) {
            getGameEngine().add(GlueEffect(getOrigin(), mTarget, mIntensity, mDuration))
            mSound.play()
            remove()
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
    }

    companion object {
        const val MOVEMENT_SPEED = 4.0f
        private const val ANIMATION_SPEED = 1.0f
    }
}
