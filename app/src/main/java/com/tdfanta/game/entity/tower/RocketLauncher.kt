package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.Rocket
import com.tdfanta.game.util.RandomUtils

class RocketLauncher private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = RocketLauncher(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
        lateinit var mSpriteTemplateRocket: SpriteTemplate
    }

    private var mExplosionRadius: Float
    private var mAngle = 90f
    private var mRocket: Rocket? = null
    private val mRocketLoadTimer: TickTimer
    private val mAimer = Aimer(this)

    private val mSprite: StaticSprite
    private val mSpriteRocket: StaticSprite
    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setIndex(RandomUtils.next(4))

        mSpriteRocket = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateRocket)
        mSpriteRocket.setListener(this)
        mSpriteRocket.setIndex(RandomUtils.next(4))

        mExplosionRadius = EXPLOSION_RADIUS
        mRocketLoadTimer = TickTimer.createInterval(ROCKET_LOAD_TIME)

        mSound = getSoundFactory().createSound(R.raw.explosive2_tsh)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.rocketLauncher, 4)
        s.mSpriteTemplate.setMatrix(1.1f, 1.1f, null, -90f)

        s.mSpriteTemplateRocket = getSpriteFactory().createTemplate(R.attr.rocket, 4)
        s.mSpriteTemplateRocket.setMatrix(0.8f, 1f, null, -90f)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSprite)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSprite)

        if (mRocket != null) {
            mRocket!!.remove()
        }
    }

    override fun enhance() {
        super.enhance()
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        if (mRocket == null && mRocketLoadTimer.tick()) {
            mRocket = Rocket(this, getPosition(), getDamage(), mExplosionRadius)
            mRocket!!.setAngle(mAngle)
            getGameEngine().add(mRocket!!)
        }

        val target = mAimer.getTarget()
        if (target != null) {
            mAngle = getAngleTo(target)

            if (mRocket != null) {
                mRocket!!.setAngle(mAngle)

                if (isReloaded()) {
                    mRocket!!.setTarget(target)
                    mRocket!!.setEnabled(true)
                    mRocket = null
                    mSound.play()

                    setReloaded(false)
                }
            }
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun preview(canvas: Canvas) {
        mSprite.draw(canvas)
        mSpriteRocket.draw(canvas)
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
        val ENTITY_NAME = "rocketLauncher"

        private const val ROCKET_LOAD_TIME = 1.0f
        private const val EXPLOSION_RADIUS = 1.7f
        private const val ENHANCE_EXPLOSION_RADIUS = 0.05f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(113100)
            .setDamage(48000)
            .setRange(3.0f)
            .setReload(3.0f)
            .setMaxLevel(15)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.5f)
            .setEnhanceCost(950)
            .setEnhanceDamage(410)
            .setEnhanceRange(0.1f)
            .setEnhanceReload(0.07f)
            .setUpgradeLevel(3)
            .build()
    }
}
