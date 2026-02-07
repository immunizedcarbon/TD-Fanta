package com.tdfanta.game.entity.shot

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.EntityTypes
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class CanonShotMg(
    origin: Entity,
    position: Vector2,
    direction: Vector2,
    private val mDamage: Float,
) : Shot(origin), SpriteTransformation {
    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mAngle = direction.angle()

    private val mSprite: StaticSprite

    init {
        setPosition(position)
        setSpeed(MOVEMENT_SPEED)
        setDirection(direction)

        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))
    }

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.canonMgShot, 4)
        s.mSpriteTemplate.setMatrix(0.2f, null, null, -90f)

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

        val enemy = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
            .filter(inRange(getPosition(), HIT_RANGE))
            .first() as Enemy?

        if (enemy != null) {
            enemy.damage(mDamage, getOrigin())
            remove()
        }

        if (!isPositionVisible()) {
            remove()
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    companion object {
        private const val HIT_RANGE = 0.5f
        const val MOVEMENT_SPEED = 8.0f
    }
}
