package com.tdfanta.game.engine.render.sprite

class StaticSprite(layer: Int, template: SpriteTemplate) : SpriteInstance(layer, template) {
    private var mIndex = 0

    fun setIndex(index: Int) {
        mIndex = index
    }

    override fun getIndex(): Int = mIndex
}
