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

class SimpleLaser private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = SimpleLaser(gameEngine)
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

        mSound = getSoundFactory().createSound(R.raw.laser1_zz)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base5, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, -90f)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.laserTower1, 4)
        s.mSpriteTemplateCanon.setMatrix(0.4f, 0.9f, Vector2(0.2f, 0.2f), -90f)

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
                val from = Vector2.polar(LASER_SPAWN_OFFSET, mAngle).add(getPosition())
                getGameEngine().add(com.tdfanta.game.entity.effect.BouncingLaser(this, from, target, getDamage()))
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
        val ENTITY_NAME = "simpleLaser"

        private const val LASER_SPAWN_OFFSET = 0.7f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(150)
            .setDamage(230)
            .setRange(3.0f)
            .setReload(1.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Laser)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(50)
            .setEnhanceDamage(40)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.1f)
            .setUpgradeTowerName(BouncingLaser.ENTITY_NAME)
            .setUpgradeCost(7000)
            .setUpgradeLevel(1)
            .build()
    }
}
