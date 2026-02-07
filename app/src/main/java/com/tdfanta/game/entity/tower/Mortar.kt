package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.MortarShot
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class Mortar private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = Mortar(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private var mExplosionRadius: Float
    private var mAngle = 90f
    private var mRebounding = false
    private val mAimer = Aimer(this)

    private val mSpriteBase: StaticSprite
    private val mSpriteCanon: AnimatedSprite
    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mExplosionRadius = EXPLOSION_RADIUS

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setIndex(RandomUtils.next(4))
        mSpriteBase.setListener(this)

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon)
        mSpriteCanon.setListener(this)
        mSpriteCanon.setSequenceForwardBackward()
        mSpriteCanon.setInterval(REBOUND_DURATION)

        mSound = getSoundFactory().createSound(R.raw.gas2_thomp)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base2, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.mortar, 8)
        s.mSpriteTemplateCanon.setMatrix(0.8f, null, Vector2(0.4f, 0.2f), -90f)

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

    override fun enhance() {
        super.enhance()
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        val target = mAimer.getTarget()
        if (target != null && isReloaded()) {
            var targetPos = target.getPositionAfter(MortarShot.TIME_TO_TARGET)
            targetPos = Vector2.polar(RandomUtils.next(INACCURACY), RandomUtils.next(360f)).add(targetPos)
            mAngle = getAngleTo(targetPos)
            val shotPos = Vector2.polar(SHOT_SPAWN_OFFSET, mAngle).add(getPosition())

            getGameEngine().add(MortarShot(this, shotPos, targetPos, getDamage(), mExplosionRadius))
            mSound.play()

            setReloaded(false)
            mRebounding = true
        }

        if (mRebounding && mSpriteCanon.tick()) {
            mRebounding = false
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())

        if (sprite == mSpriteCanon) {
            canvas.rotate(mAngle)
        }
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteCanon.draw(canvas)
    }

    override fun getTowerInfoValues(): List<TowerInfoValue> {
        val properties = ArrayList<TowerInfoValue>()
        properties.add(TowerInfoValue(R.string.damage, getDamage()))
        properties.add(TowerInfoValue(R.string.splash, mExplosionRadius))
        properties.add(TowerInfoValue(R.string.reload, getReloadTime()))
        properties.add(TowerInfoValue(R.string.dps, getDamage() / getReloadTime()))
        properties.add(TowerInfoValue(R.string.range, getRange()))
        properties.add(TowerInfoValue(R.string.inflicted, getDamageInflicted()))
        return properties
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "mortar"

        private const val SHOT_SPAWN_OFFSET = 0.6f
        private const val REBOUND_DURATION = 0.5f

        private const val INACCURACY = 1.0f
        private const val EXPLOSION_RADIUS = 1.5f
        private const val ENHANCE_EXPLOSION_RADIUS = 0.05f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(250)
            .setDamage(100)
            .setRange(2.5f)
            .setReload(2.0f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(125)
            .setEnhanceDamage(60)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.05f)
            .setUpgradeTowerName(MineLayer.ENTITY_NAME)
            .setUpgradeCost(10000)
            .setUpgradeLevel(1)
            .build()
    }
}
