package com.tdfanta.game.engine.render.sprite

class ReplicatedSprite(private val mOriginal: SpriteInstance) :
    SpriteInstance(mOriginal.getLayer(), mOriginal.getTemplate()) {
    override fun getIndex(): Int = mOriginal.getIndex()
}
