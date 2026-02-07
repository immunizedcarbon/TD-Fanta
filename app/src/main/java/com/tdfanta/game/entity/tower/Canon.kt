package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.CanonShot
import com.tdfanta.game.entity.shot.Shot
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Function
import com.tdfanta.game.util.math.SampledFunction
import com.tdfanta.game.util.math.Vector2

class Canon private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Canon(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private var mAngle = 90f
    private var mReboundActive = false
    private val mAimer = Aimer(this)

    private val mReboundFunction: SampledFunction

    private val mSpriteBase: StaticSprite
    private val mSpriteCanon: StaticSprite

    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mReboundFunction = Function.sine()
            .multiply(REBOUND_RANGE)
            .stretch(GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION / Math.PI.toFloat())
            .sample()

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteCanon = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateCanon)
        mSpriteCanon.setListener(this)
        mSpriteCanon.setIndex(RandomUtils.next(4))

        mSound = getSoundFactory().createSound(R.raw.gun3_dit)
        mSound.setVolume(0.5f)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.canon, 4)
        s.mSpriteTemplateCanon.setMatrix(0.4f, 1.0f, Vector2(0.2f, 0.2f), -90f)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSpriteBase)
        getGameEngine().add(mSpriteCanon)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSpriteBase)
        getGameEngine().remove(mSpriteCanon)
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        val target = mAimer.getTarget()
        if (target != null) {
            mAngle = getAngleTo(target)

            if (isReloaded()) {
                val shot: Shot = CanonShot(this, getPosition(), target, getDamage())
                shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle))
                getGameEngine().add(shot)
                mSound.play()

                setReloaded(false)
                mReboundActive = true
            }
        }

        if (mReboundActive) {
            mReboundFunction.step()
            if (mReboundFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION) {
                mReboundFunction.reset()
                mReboundActive = false
            }
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)

        if (sprite == mSpriteCanon && mReboundActive) {
            canvas.translate(-mReboundFunction.getValue(), 0f)
        }
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteCanon.draw(canvas)
    }

    override fun getTowerInfoValues(): List<TowerInfoValue> {
        val properties = ArrayList<TowerInfoValue>()
        properties.add(TowerInfoValue(R.string.damage, getDamage()))
        properties.add(TowerInfoValue(R.string.reload, getReloadTime()))
        properties.add(TowerInfoValue(R.string.dps, getDamage() / getReloadTime()))
        properties.add(TowerInfoValue(R.string.range, getRange()))
        properties.add(TowerInfoValue(R.string.inflicted, getDamageInflicted()))
        return properties
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "canon"

        private const val SHOT_SPAWN_OFFSET = 0.7f
        private const val REBOUND_RANGE = 0.25f
        private const val REBOUND_DURATION = 0.2f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(100)
            .setDamage(100)
            .setRange(2.5f)
            .setReload(1.0f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Bullet)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(50)
            .setEnhanceDamage(40)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.05f)
            .setUpgradeTowerName(DualCanon.ENTITY_NAME)
            .setUpgradeCost(5600)
            .setUpgradeLevel(1)
            .build()
    }
}
