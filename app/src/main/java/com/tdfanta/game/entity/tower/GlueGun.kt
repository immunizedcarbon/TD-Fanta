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
import com.tdfanta.game.entity.shot.GlueShot
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.math.Vector2

class GlueGun private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = GlueGun(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private var mAngle = 90f
    private var mGlueIntensity: Float
    private var mRebounding = false
    private val mAimer = Aimer(this)

    private val mSpriteBase: StaticSprite
    private val mSpriteCanon: AnimatedSprite
    private val mSound: Sound

    init {
        val s = getStaticData() as StaticData

        mGlueIntensity = GLUE_INTENSITY

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon)
        mSpriteCanon.setListener(this)
        mSpriteCanon.setSequenceForwardBackward()
        mSpriteCanon.setInterval(REBOUND_DURATION)

        mSound = getSoundFactory().createSound(R.raw.explosive1_chk)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.glueGun, 6)
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
        mGlueIntensity += ENHANCE_GLUE_INTENSITY
    }

    override fun tick() {
        super.tick()
        mAimer.tick()

        val target = mAimer.getTarget()
        if (isReloaded() && target != null) {
            val dist = getDistanceTo(target)
            val time = dist / GlueShot.MOVEMENT_SPEED

            val targetPos = target.getPositionAfter(time)

            mAngle = getAngleTo(targetPos)

            val position = Vector2.polar(SHOT_SPAWN_OFFSET, getAngleTo(targetPos)).add(getPosition())
            getGameEngine().add(GlueShot(this, position, targetPos, mGlueIntensity, GLUE_DURATION))
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
        canvas.rotate(mAngle)
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteCanon.draw(canvas)
    }

    override fun getTowerInfoValues(): List<TowerInfoValue> {
        val properties = ArrayList<TowerInfoValue>()
        properties.add(TowerInfoValue(R.string.intensity, mGlueIntensity))
        properties.add(TowerInfoValue(R.string.duration, GLUE_DURATION))
        properties.add(TowerInfoValue(R.string.reload, getReloadTime()))
        properties.add(TowerInfoValue(R.string.range, getRange()))
        return properties
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "glueGun"

        private const val SHOT_SPAWN_OFFSET = 0.7f
        private const val REBOUND_DURATION = 0.5f
        private const val GLUE_INTENSITY = 1.2f
        private const val ENHANCE_GLUE_INTENSITY = 0.3f
        private const val GLUE_DURATION = 2.5f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(1300)
            .setDamage(0)
            .setRange(2.5f)
            .setReload(3.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.Glue)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(200)
            .setEnhanceDamage(0)
            .setEnhanceRange(0.2f)
            .setEnhanceReload(0f)
            .setUpgradeTowerName(Teleporter.ENTITY_NAME)
            .setUpgradeCost(1700)
            .setUpgradeLevel(2)
            .build()
    }
}
