package com.tdfanta.game.entity.tower

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.map.MapPath
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.AnimatedSprite
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.sound.Sound
import com.tdfanta.game.entity.enemy.WeaponType
import com.tdfanta.game.entity.shot.Mine
import com.tdfanta.game.util.RandomUtils
import com.tdfanta.game.util.container.KeyValueStore
import com.tdfanta.game.util.math.Intersections
import com.tdfanta.game.util.math.Line
import com.tdfanta.game.util.math.Vector2

class MineLayer private constructor(gameEngine: GameEngine) : Tower(gameEngine, TOWER_PROPERTIES), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = MineLayer(gameEngine)
    }

    class Persister : TowerPersister() {
        override fun writeEntityData(entity: Entity): KeyValueStore {
            val data = super.writeEntityData(entity)!!
            val mineLayer = entity as MineLayer

            val minePositions = ArrayList<Vector2>()
            for (mine in mineLayer.mMines) {
                if (!mine.isFlying()) {
                    minePositions.add(mine.getPosition())
                }
            }
            data.putVectorList("minePositions", minePositions)

            return data
        }

        override fun readEntityData(entity: Entity, entityData: KeyValueStore) {
            super.readEntityData(entity, entityData)
            val mineLayer = entity as MineLayer

            for (minePosition in entityData.getVectorList("minePositions")) {
                val mine = Mine(mineLayer, minePosition, mineLayer.getDamage(), mineLayer.mExplosionRadius)
                mineLayer.mMines.add(mine)
                mine.addListener(mineLayer.mMineListener)
                mineLayer.getGameEngine().add(mine)
            }
        }
    }

    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mAngle = RandomUtils.next(360f)
    private var mMaxMineCount: Int
    var mExplosionRadius: Float
    private var mShooting = false
    private var mSections: Collection<Line> = ArrayList()
    val mMines: MutableCollection<Mine> = ArrayList()

    private val mSprite: AnimatedSprite
    private val mSound: Sound

    val mMineListener: Entity.Listener = object : Entity.Listener {
        override fun entityRemoved(entity: Entity) {
            val mine = entity as Mine
            mine.removeListener(this)
            mMines.remove(mine)
        }
    }

    init {
        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createAnimated(Layers.TOWER_BASE, s.mSpriteTemplate)
        mSprite.setListener(this)
        mSprite.setSequenceForwardBackward()
        mSprite.setInterval(ANIMATION_DURATION)

        mMaxMineCount = MAX_MINE_COUNT
        mExplosionRadius = EXPLOSION_RADIUS

        mSound = getSoundFactory().createSound(R.raw.gun2_donk)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.mineLayer, 6)
        s.mSpriteTemplate.setMatrix(1f, 1f, null, null)

        return s
    }

    override fun init() {
        super.init()

        getGameEngine().add(mSprite)
    }

    override fun clean() {
        super.clean()

        getGameEngine().remove(mSprite)

        for (m in mMines) {
            m.removeListener(mMineListener)
            m.remove()
        }

        mMines.clear()
    }

    override fun setPosition(position: Vector2) {
        super.setPosition(position)
        val paths = getGameEngine().getGameMap()!!.getPaths()
        mSections = getPathSectionsInRange(paths)
    }

    override fun move(offset: Vector2) {
        super.move(offset)
        val paths = getGameEngine().getGameMap()!!.getPaths()
        mSections = getPathSectionsInRange(paths)
    }

    override fun enhance() {
        super.enhance()
        mMaxMineCount += ENHANCE_MAX_MINE_COUNT
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS
    }

    override fun tick() {
        super.tick()

        if (isReloaded() && mMines.size < mMaxMineCount && mSections.isNotEmpty()) {
            mShooting = true
            setReloaded(false)
        }

        if (mShooting) {
            mSprite.tick()

            if (mSprite.getSequenceIndex() == 5) {
                val mine = Mine(this, getPosition(), getTarget()!!, getDamage(), mExplosionRadius)
                mine.addListener(mMineListener)
                mMines.add(mine)
                getGameEngine().add(mine)
                mSound.play()

                mShooting = false
            }
        }

        if (mSprite.getSequenceIndex() != 0) {
            mSprite.tick()
        }
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
        canvas.rotate(mAngle)
    }

    override fun preview(canvas: Canvas) {
        mSprite.draw(canvas)
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

    private fun getTarget(): Vector2? {
        var totalLen = 0f

        for (section in mSections) {
            totalLen += section.length()
        }

        var dist = RandomUtils.next(totalLen)

        for (section in mSections) {
            val length = section.length()

            if (dist > length) {
                dist -= length
            } else {
                return section
                    .direction()
                    .mul(dist)
                    .add(section.getPoint1())
            }
        }

        return null
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
        val ENTITY_NAME = "mineLayer"

        private const val ANIMATION_DURATION = 1f
        private const val MAX_MINE_COUNT = 3
        private const val ENHANCE_MAX_MINE_COUNT = 1
        private const val EXPLOSION_RADIUS = 2.0f
        private const val ENHANCE_EXPLOSION_RADIUS = 0.05f

        private val TOWER_PROPERTIES = TowerProperties.Builder()
            .setValue(10250)
            .setDamage(3100)
            .setRange(2.5f)
            .setReload(2.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.4f)
            .setEnhanceCost(750)
            .setEnhanceDamage(270)
            .setEnhanceRange(0.0f)
            .setEnhanceReload(0.05f)
            .setUpgradeTowerName(RocketLauncher.ENTITY_NAME)
            .setUpgradeCost(102850)
            .setUpgradeLevel(2)
            .build()
    }
}
