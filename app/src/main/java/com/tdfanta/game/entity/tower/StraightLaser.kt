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
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class StraightLaser private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = StraightLaser(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private var mAngle = 90f
    private val mAimer = Aimer(this)

    private val mSpriteBase: StaticSprite
    private val mSpriteCanon: StaticSprite
    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setIndex(RandomUtils.next(4))
        mSpriteBase.setListener(this)

        mSpriteCanon = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateCanon)
        mSpriteCanon.setIndex(RandomUtils.next(4))
        mSpriteCanon.setListener(this)

        mSound = getSoundFactory().createSound(R.raw.laser3_szh)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base5, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, -90f)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.laserTower3, 4)
        s.mSpriteTemplateCanon.setMatrix(0.4f, 1.2f, Vector2(0.2f, 0.2f), -90f)

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
                val laserFrom = Vector2.polar(LASER_SPAWN_OFFSET, mAngle).add(getPosition())
                val laserTo = Vector2.polar(LASER_LENGTH, mAngle).add(getPosition())
                getGameEngine().add(com.tdfanta.game.entity.effect.StraightLaser(this, laserFrom, laserTo, getDamage()))
                setReloaded(false)
                mSound.play()
            }
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
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
        val ENTITY_NAME = "straightLaser"

        private const val LASER_SPAWN_OFFSET = 0.8f
        private const val LASER_LENGTH = 100f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(103600)
            .setDamage(44000)
            .setRange(3.0f)
            .setReload(3.0f)
            .setMaxLevel(15)
            .setWeaponType(WeaponType.Laser)
            .setEnhanceBase(1.5f)
            .setEnhanceCost(950)
            .setEnhanceDamage(410)
            .setEnhanceRange(0.07f)
            .setEnhanceReload(0.07f)
            .setUpgradeLevel(3)
            .build()
    }
}
