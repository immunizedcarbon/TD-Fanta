package com.tdfanta.game.entity.plateau

import android.graphics.Canvas
import com.tdfanta.game.R
import com.tdfanta.game.engine.logic.GameEngine
import com.tdfanta.game.engine.logic.entity.Entity
import com.tdfanta.game.engine.logic.entity.EntityFactory
import com.tdfanta.game.engine.logic.entity.EntityPersister
import com.tdfanta.game.engine.render.Layers
import com.tdfanta.game.engine.render.sprite.SpriteInstance
import com.tdfanta.game.engine.render.sprite.SpriteTemplate
import com.tdfanta.game.engine.render.sprite.SpriteTransformation
import com.tdfanta.game.engine.render.sprite.SpriteTransformer
import com.tdfanta.game.engine.render.sprite.StaticSprite
import com.tdfanta.game.util.RandomUtils

class BasicPlateau private constructor(gameEngine: GameEngine) : Plateau(gameEngine), SpriteTransformation {
    class Factory : EntityFactory() {
        override fun create(gameEngine: GameEngine): Entity = BasicPlateau(gameEngine)
    }

    class Persister : EntityPersister()

    private class StaticData {
        lateinit var mSpriteTemplate: SpriteTemplate
    }

    private val mSprite: StaticSprite

    init {
        val s = getStaticData() as StaticData

        mSprite = getSpriteFactory().createStatic(Layers.PLATEAU, s.mSpriteTemplate)
        mSprite.setIndex(RandomUtils.next(4))
        mSprite.setListener(this)
    }

    override fun getEntityName(): String = ENTITY_NAME

    override fun initStatic(): Any {
        val s = StaticData()

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.plateau1, 4)
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
    }

    override fun draw(sprite: SpriteInstance, canvas: Canvas) {
        SpriteTransformer.translate(canvas, getPosition())
    }

    companion object {
        @JvmField
        val ENTITY_NAME = "basic"
    }
}
