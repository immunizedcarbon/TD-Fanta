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
import com.tdfanta.game.entity.enemy.Enemy
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.CanonShotMg
import com.tdfanta.game.entity.shot.Shot
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.MathUtils
import com.tdfanta.game.util.math.Vector2

class MachineGun private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = MachineGun(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private val mBaseReloadTime: Float
    private var mAngle = 90f
    private val mSpriteBase: StaticSprite
    private val mSpriteCanon: AnimatedSprite
    private var mShotCount = 0
    private val mSound: Sound
    private val mAimer = Aimer(this)

    init {
        val s = getStaticData() as StaticData

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon)
        mSpriteCanon.setListener(this)
        mSpriteCanon.setSequenceForward()

        mBaseReloadTime = getReloadTime()
        mSpriteCanon.setFrequency(MG_ROTATION_SPEED)

        mSound = getSoundFactory().createSound(R.raw.gun3_dit)
        mSound.setVolume(0.5f)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.canonMg, 5)
        s.mSpriteTemplateCanon.setMatrix(0.8f, 1.0f, Vector2(0.4f, 0.4f), -90f)

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
        mSpriteCanon.setFrequency(MG_ROTATION_SPEED * mBaseReloadTime / getReloadTime())
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        val target = mAimer.getTarget()
        if (target != null) {
            val shootingDirection = calcShootingDirection(target)
            mAngle = shootingDirection.angle()
            mSpriteCanon.tick()

            if (isReloaded()) {
                val shot: Shot = CanonShotMg(this, getPosition(), shootingDirection, getDamage())
                shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle))
                getGameEngine().add(shot)
                mShotCount++

                if (mShotCount % 2 == 0) {
                    mSound.play()
                }

                setReloaded(false)
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

    private fun calcShootingDirection(target: Enemy): Vector2 {
        val ps = getDirectionTo(target).mul(SHOT_SPAWN_OFFSET).add(getPosition())
        val pt = target.getPosition()
        val ptToPsAngle = pt.angleTo(ps)

        val dt = target.getDirection()
        if (dt == null) {
            return getDirectionTo(target)
        }

        val vs = CanonShotMg.MOVEMENT_SPEED
        val vt = target.getSpeed()

        val alpha = dt.angle() - ptToPsAngle
        val beta = MathUtils.toDegrees(kotlin.math.asin(vt * kotlin.math.sin(MathUtils.toRadians(alpha)) / vs))

        val angle = 180f + ptToPsAngle - beta
        return Vector2.polar(1f, angle)
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "machineGun"

        private const val SHOT_SPAWN_OFFSET = 0.7f
        private const val MG_ROTATION_SPEED = 3f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(94200)
            .setDamage(20000)
            .setRange(3.5f)
            .setReload(0.15f)
            .setMaxLevel(15)
            .setWeaponType(WeaponType.Bullet)
            .setEnhanceBase(1.5f)
            .setEnhanceCost(750)
            .setEnhanceDamage(120)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.005f)
            .setUpgradeLevel(3)
            .build()
    }
}
