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
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class CanonShot(
    origin: Entity,
    position: Vector2,
    target: Enemy,
    private val mDamage: Float,
) : Shot(origin), SpriteTransformation, TargetTracker.Listener {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private var mAngle = 0f
    private val mTracker = TargetTracker(target, this, this)

    private val mSprite: StaticSprite

    init {
        setPosition(position)
        setSpeed(MOVEMENT_SPEED)

        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.canonShot, 4)
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
        setDirection(mTracker.getTargetDirection())
        mAngle += ROTATION_STEP
        super.tick()
        mTracker.tick()
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun targetLost(target: Enemy) {
        remove()
    }

    override fun targetReached(target: Enemy) {
        target.damage(mDamage, getOrigin())
        remove()
    }

    companion object {
        private const val MOVEMENT_SPEED = 4.0f
        private const val ROTATION_SPEED = 1.0f
        private val ROTATION_STEP = ROTATION_SPEED * 360f / GameEngine.TARGET_FRAME_RATE
    }
}
