package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.loop.TickTimer
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.GlueShot
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.iterator.StreamIterator
import com.tdfanta.game.util.math.Intersections
import com.tdfanta.game.util.math.Line
import com.tdfanta.game.util.math.Vector2

class GlueTower private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = GlueTower(gameEngine)
    }

    class Persister : TowerPersister()

    private class StaticData {
        lateinit var mSpriteTemplateBase: SpriteTemplate
        lateinit var mSpriteTemplateTower: SpriteTemplate
        lateinit var mSpriteTemplateCanon: SpriteTemplate
    }

    private inner class SubCanon : SpriteTransformation {
        var mAngle = 0f
        lateinit var mSprite: StaticSprite

        override fun draw(sprite: SpriteInstance, canvas: Canvas) {
            SpriteTransformer.translate(canvas, getPosition())
            canvas.rotate(mAngle)
            canvas.translate(mCanonOffset, 0f)
        }
    }

    private var mGlueIntensity: Float
    private var mShooting = false
    private var mCanonOffset = 0f
    private val mCanons = Array(8) { SubCanon() }
    private val mTargets: MutableCollection<Vector2> = ArrayList()
    private val mSpriteBase: StaticSprite

    private val mSpriteTower: StaticSprite
    private val mUpdateTimer = TickTimer.createInterval(0.1f)

    init {
        val s = getStaticData() as StaticData

        mGlueIntensity = GLUE_INTENSITY

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateBase)
        mSpriteBase.setListener(this)
        mSpriteBase.setIndex(RandomUtils.next(4))

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER_UPPER, s.mSpriteTemplateTower)
        mSpriteTower.setListener(this)
        mSpriteTower.setIndex(RandomUtils.next(6))

        for (i in mCanons.indices) {
            val c = SubCanon()
            c.mAngle = 360f / mCanons.size * i
            c.mSprite = getSpriteFactory().createStatic(Layers.TOWER_LOWER, s.mSpriteTemplateCanon)
            c.mSprite.setListener(c)
            mCanons[i] = c
        }
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base4, 4)
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null)

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.glueShot, 6)
        s.mSpriteTemplateTower.setMatrix(0.3f, 0.3f, null, null)

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.glueTowerGun, 4)
        s.mSpriteTemplateCanon.setMatrix(0.3f, 0.4f, null, -90f)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSpriteBase)
        getGameEngine().add(mSpriteTower)

        for (c in mCanons) {
            getGameEngine().add(c.mSprite)
        }
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSpriteBase)
        getGameEngine().remove(mSpriteTower)

        for (c in mCanons) {
            getGameEngine().remove(c.mSprite)
        }
    }

    override fun setBuilt() {
        super.setBuilt()
        determineTargets()
    }

    override fun enhance() {
        super.enhance()
        mGlueIntensity += ENHANCE_GLUE_INTENSITY
    }

    override fun tick() {
        super.tick()

        if (isReloaded() && mUpdateTimer.tick() && !getPossibleTargets().isEmpty()) {
            mShooting = true
            setReloaded(false)
        }

        if (mShooting) {
            mCanonOffset += CANON_OFFSET_STEP

            if (mCanonOffset >= CANON_OFFSET_MAX) {
                mShooting = false

                for (target in mTargets) {
                    val position = Vector2.polar(SHOT_SPAWN_OFFSET, getAngleTo(target)).add(getPosition())
                    getGameEngine().add(GlueShot(this, position, target, mGlueIntensity, GLUE_DURATION))
                }
            }
        } else if (mCanonOffset > 0f) {
            mCanonOffset -= CANON_OFFSET_STEP
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
    }

    override fun preview(canvas: Canvas) {
        mSpriteBase.draw(canvas)
        mSpriteTower.draw(canvas)
    }

    override fun getTowerInfoValues(): List<TowerInfoValue> {
        val properties = ArrayList<TowerInfoValue>()
        properties.add(TowerInfoValue(R.string.intensity, mGlueIntensity))
        properties.add(TowerInfoValue(R.string.duration, GLUE_DURATION))
        properties.add(TowerInfoValue(R.string.reload, getReloadTime()))
        properties.add(TowerInfoValue(R.string.range, getRange()))
        return properties
    }

    private fun determineTargets() {
        val paths = getGameEngine().getGameMap()!!.getPaths()
        val sections = getPathSectionsInRange(paths)
        var dist = 0f

        mTargets.clear()

        for (sect in sections) {
            val angle = sect.angle()
            val length = sect.length()

            while (dist < length) {
                val target = Vector2.polar(dist, angle).add(sect.getPoint1())

                val free = StreamIterator.fromIterable(mTargets)
                    .filter { value -> value.distanceTo(target) < 0.5f }
                    .isEmpty()

                if (free) {
                    mTargets.add(target)
                }

                dist += 1f
            }

            dist -= length
        }
    }

    private fun getPathSectionsInRange(paths: Collection<MapPath>): Collection<Line> {
        val sections: MutableCollection<Line> = ArrayList()

        for (path in paths) {
            sections.addAll(Intersections.getPathSectionsInRange(path.getWayPoints(), getPosition(), getRange()))
        }

        return sections
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "glueTower"

        private const val SHOT_SPAWN_OFFSET = 0.8f
        private const val CANON_OFFSET_MAX = 0.5f
        private val CANON_OFFSET_STEP = CANON_OFFSET_MAX / GameEngine.TARGET_FRAME_RATE / 0.8f
        private const val GLUE_INTENSITY = 1.2f
        private const val ENHANCE_GLUE_INTENSITY = 0.2f
        private const val GLUE_DURATION = 1.5f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(500)
            .setDamage(0)
            .setRange(1.5f)
            .setReload(2.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.Glue)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(100)
            .setEnhanceDamage(0)
            .setEnhanceRange(0.1f)
            .setEnhanceReload(0f)
            .setUpgradeTowerName(GlueGun.ENTITY_NAME)
            .setUpgradeCost(800)
            .setUpgradeLevel(1)
            .build()
    }
}
