package com.tdfanta.game.util.math

class SampledFunction(private val mFunction: Function) {
    private var mPosition: Int = 0
    private var mValue: Float = 0f

    init {
        setPosition(0)
    }

    fun getPosition(): Int = mPosition

    fun getValue(): Float = mValue

    fun setPosition(position: Int): SampledFunction {
        mPosition = position
        mValue = mFunction.calculate(mPosition.toFloat())
        return this
    }

    fun step(): SampledFunction {
        mPosition++
        mValue = mFunction.calculate(mPosition.toFloat())
        return this
    }

    fun reset(): SampledFunction = setPosition(0)
}
