package com.tdfanta.game.util.math

abstract class Function {
    abstract fun calculate(input: Float): Float

    fun multiply(x: Float): Function = object : Function() {
        override fun calculate(input: Float): Float = this@Function.calculate(input) * x
    }

    fun stretch(x: Float): Function = object : Function() {
        override fun calculate(input: Float): Float = this@Function.calculate(input / x)
    }

    fun offset(d: Float): Function = object : Function() {
        override fun calculate(input: Float): Float = this@Function.calculate(input) + d
    }

    fun shift(d: Float): Function = object : Function() {
        override fun calculate(input: Float): Float = this@Function.calculate(input + d)
    }

    fun invert(): Function = object : Function() {
        override fun calculate(input: Float): Float = 1f / this@Function.calculate(input)
    }

    fun absolute(): Function = object : Function() {
        override fun calculate(input: Float): Float = kotlin.math.abs(this@Function.calculate(input))
    }

    fun join(f: Function, at: Float): Function = object : Function() {
        override fun calculate(input: Float): Float {
            if (input < at) {
                return this@Function.calculate(input)
            }
            return f.calculate(input - at)
        }
    }

    fun repeat(at: Float): Function = object : Function() {
        override fun calculate(input: Float): Float = this@Function.calculate(input % at)
    }

    fun sample(): SampledFunction = SampledFunction(this)

    companion object {
        @JvmStatic
        fun constant(value: Float): Function = object : Function() {
            override fun calculate(input: Float): Float = value
        }

        @JvmStatic
        fun linear(): Function = object : Function() {
            override fun calculate(input: Float): Float = input
        }

        @JvmStatic
        fun quadratic(): Function = object : Function() {
            override fun calculate(input: Float): Float = input * input
        }

        @JvmStatic
        fun sine(): Function = object : Function() {
            override fun calculate(input: Float): Float =
                kotlin.math.sin(input.toDouble()).toFloat()
        }
    }
}
