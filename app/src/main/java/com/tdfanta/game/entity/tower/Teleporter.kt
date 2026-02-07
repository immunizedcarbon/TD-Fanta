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
import com.tdfanta.game.entity.effect.TeleportEffect
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.iterator.StreamIterator

class Teleporter private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Teleporter(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateTower: SpriteTemplate
    }

    private var mTeleportDistance: Float
    private val mAimer = Aimer(this)

    private val mSpriteBase: StaticSprite
    private val mSpriteTower: StaticSprite
    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mTeleportDistance = TELEPORT_DISTANCE

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateTower)
        mSpriteTower.setListener(this)
        mSpriteTower.setIndex(RandomUtils.next(4))

        mSound = getSoundFactory().createSound(R.raw.gas3_hht)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base4, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.teleportTower, 4)
        s.mSpriteTemplateTower.setMatrix(0.8f, 0.8f, null, null)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSpriteBase)
        getGameEngine().add(mSpriteTower)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSpriteBase)
        getGameEngine().remove(mSpriteTower)
    }

    override fun enhance() {
        super.enhance()
        mTeleportDistance += ENHANCE_TELEPORT_DISTANCE
    }

    override fun tick() {
        super.tick()

        mAimer.tick()
        val target = mAimer.getTarget()

        if (isReloaded() && target != null) {
            if (!target.isBeingTeleported() && getDistanceTo(target) <= getRange()) {
                getGameEngine().add(TeleportEffect(this, getPosition(), target, mTeleportDistance))
                mSound.play()
                setReloaded(false)
            } else {
                mAimer.setTarget(null)
            }
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteTower.draw(canvas)
    }

    override fun getTowerInfoValues(): List<TowerInfoValue> {
        val properties = ArrayList<TowerInfoValue>()
        properties.add(TowerInfoValue(R.string.distance, mTeleportDistance))
        properties.add(TowerInfoValue(R.string.reload, getReloadTime()))
        properties.add(TowerInfoValue(R.string.range, getRange()))
        return properties
    }

    override fun getPossibleTargets(): StreamIterator<Enemy> = super.getPossibleTargets()
        .filter { enemy -> !enemy.isBeingTeleported() && !enemy.wasTeleported() }

    companion object {
        @JvmField
        val ENTITY_NAME = "teleporter"

        private const val TELEPORT_DISTANCE = 15f
        private const val ENHANCE_TELEPORT_DISTANCE = 5f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(3000)
            .setDamage(0)
            .setRange(3.5f)
            .setReload(5.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.None)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(2000)
            .setEnhanceDamage(0)
            .setEnhanceRange(0f)
            .setEnhanceReload(0.5f)
            .setUpgradeLevel(3)
            .build()
    }
}
