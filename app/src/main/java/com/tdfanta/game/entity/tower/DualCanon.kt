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

class DualCanon private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = DualCanon(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateTower: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private class SubCanon {
        var reboundActive = false
        lateinit var reboundFunction: SampledFunction
        lateinit var sprite: StaticSprite
    }

    private var mAngle = 90f
    private var mShoot2 = false
    private val mCanons = arrayOf(SubCanon(), SubCanon())
    private val mAimer = Aimer(this)

    private val mSpriteBase: StaticSprite
    private val mSpriteTower: StaticSprite

    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        val reboundFunction = Function.sine()
            .multiply(REBOUND_RANGE)
            .stretch(GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION / Math.PI.toFloat())

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER_LOWER, s.mSpriteTemplateTower)
        mSpriteTower.setListener(this)
        mSpriteTower.setIndex(RandomUtils.next(4))

        for (canon in mCanons) {
            canon.reboundFunction = reboundFunction.sample()
            canon.reboundActive = false

            canon.sprite = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateCanon)
            canon.sprite.setListener(this)
            canon.sprite.setIndex(RandomUtils.next(4))
        }

        mSound = getSoundFactory().createSound(R.raw.gun3_dit)
        mSound.setVolume(0.5f)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.canonDual, 4)
        s.mSpriteTemplateTower.setMatrix(0.5f, 0.5f, null, -90f)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.canon, 4)
        s.mSpriteTemplateCanon.setMatrix(0.3f, 1.0f, Vector2(0.15f, 0.4f), -90f)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSpriteBase)
        getGameEngine().add(mSpriteTower)

        for (c in mCanons) {
            getGameEngine().add(c.sprite)
        }
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSpriteBase)
        getGameEngine().remove(mSpriteTower)

        for (c in mCanons) {
            getGameEngine().remove(c.sprite)
        }
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        val target = mAimer.getTarget()
        if (target != null) {
            mAngle = getAngleTo(target)

            if (isReloaded()) {
                if (!mShoot2) {
                    val shot: Shot = CanonShot(this, getPosition(), target, getDamage())
                    shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle))
                    shot.move(Vector2.polar(0.3f, mAngle + 90f))
                    getGameEngine().add(shot)

                    setReloaded(false)
                    mCanons[0].reboundActive = true
                    mShoot2 = true
                } else {
                    val shot: Shot = CanonShot(this, getPosition(), target, getDamage())
                    shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle))
                    shot.move(Vector2.polar(0.3f, mAngle - 90f))
                    getGameEngine().add(shot)

                    setReloaded(false)
                    mCanons[1].reboundActive = true
                    mShoot2 = false
                }

                mSound.play()
            }
        }

        if (mCanons[0].reboundActive) {
            mCanons[0].reboundFunction.step()
            if (mCanons[0].reboundFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION) {
                mCanons[0].reboundFunction.reset()
                mCanons[0].reboundActive = false
            }
        }

        if (mCanons[1].reboundActive) {
            mCanons[1].reboundFunction.step()
            if (mCanons[1].reboundFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION) {
                mCanons[1].reboundFunction.reset()
                mCanons[1].reboundActive = false
            }
        }
    }

    override fun getAimer(): Aimer = mAimer

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)

        if (sprite == mCanons[0].sprite) {
            canvas.translate(0f, 0.3f)

            if (mCanons[0].reboundActive) {
                canvas.translate(-mCanons[0].reboundFunction.getValue(), 0f)
            }
        }

        if (sprite == mCanons[1].sprite) {
            canvas.translate(0f, -0.3f)

            if (mCanons[1].reboundActive) {
                canvas.translate(-mCanons[1].reboundFunction.getValue(), 0f)
            }
        }
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteTower.draw(canvas)
        mCanons[0].sprite.draw(canvas)
        mCanons[1].sprite.draw(canvas)
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
        val ENTITY_NAME = "dualCanon"

        private const val SHOT_SPAWN_OFFSET = 0.7f
        private const val REBOUND_RANGE = 0.25f
        private const val REBOUND_DURATION = 0.2f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(5700)
            .setDamage(3400)
            .setRange(3.0f)
            .setReload(0.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Bullet)
            .setEnhanceBase(1.4f)
            .setEnhanceCost(470)
            .setEnhanceDamage(160)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.03f)
            .setUpgradeTowerName(MachineGun.ENTITY_NAME)
            .setUpgradeCost(88500)
            .setUpgradeLevel(2)
            .build()
    }
}
